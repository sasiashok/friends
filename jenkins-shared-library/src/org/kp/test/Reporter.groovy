package org.kp.test

import org.kp.constants.Constant
import org.kp.utils.*
import groovy.text.GStringTemplateEngine
import org.kp.analytics.ElasticsearchUtils

/**
 * Retrieve measures and publish mini SonarQube report.
 * @param statusClass Quality gate status
 * @return
 */
def publishSonarReport(String statusClass) {
  dir(env.APPLICATION_DIR) {
    String component = new ApplicationUtils().getApplicationComponent()
    String sonarUrl = Constant.SONAR_URL
    String encodedComponent = java.net.URLEncoder.encode(component, "UTF-8")

    def measures = httpRequest quiet: true, authentication: 'sonar_admin', ignoreSslErrors: true, url: "${sonarUrl}/api/measures/component?component=${component}&metricKeys=bugs,new_bugs,vulnerabilities,new_vulnerabilities,code_smells,new_code_smells,coverage,new_coverage,new_lines_to_cover,tests,duplicated_lines_density,duplicated_blocks,new_duplicated_lines_density,new_lines"
    def second_measures = httpRequest quiet: true, authentication: 'sonar_admin', ignoreSslErrors: true, url: "${sonarUrl}/api/measures/component?component=${component}&metricKeys=major_violations,minor_violations,blocker_violations,critical_violations,info_violations,reliability_rating,security_rating,new_reliability_rating,new_security_rating"
    def metrics_response = readJSON text: measures.content
    def second_metrics = readJSON text: second_measures.content

    String projectName = metrics_response.component.name
    String reportLink = "${sonarUrl}/dashboard?id=${encodedComponent}"
    String status = "Passed"
    if(statusClass != "OK") {
      status = "Failed"
    }
    def binding = [projectName: projectName, reportLink: reportLink, statusClass: statusClass, status: status, bugs: 0, vulnerabilities: 0, new_bugs: 0, 
      new_vulnerabilities: 0, code_smells: 0, new_code_smells: 0, coverage: 0, tests: 0, new_coverage: 0, new_lines_to_cover: 0, duplicated_lines_density: 0, 
      duplicated_blocks: 0, new_duplicated_lines_density: 0, new_lines: 0, major_violations: 0 , minor_violations: 0, blocker_violations: 0, critical_violations: 0, 
      info_violations: 0, security_rating: "A", new_security_rating: "A", reliability_rating: "A", new_reliability_rating: "A"]

    def anayticsBinding = [:]
    anayticsBinding.putAll(binding)
    def measuresMap = getMeasuresMap(metrics_response.component.measures.toString(), second_metrics.component.measures.toString())

    measuresMap.each { measure ->
      def value = measure.containsKey('value') ? measure['value'] : measure?.periods[0]?.value
      if(value.isNumber()) {
        if(value.isInteger()) {
          value = Integer.parseInt(value)
        } else {
          floatValue = Float.parseFloat(value)
          value = floatValue.round(2)
        }
      }
      if(measure?.metric != null) {
        switch (measure?.metric) {
          case "security_rating":
            value = getRating(value.toString().trim())
            break
          case "new_security_rating":
            value = getRating(value.toString().trim())
            break
          case "reliability_rating":
            value = getRating(value.toString().trim())
            break
          case "new_reliability_rating":
            value = getRating(value.toString().trim())
            break
        }
        anayticsBinding.put(measure?.metric, value)
        value = value.toString().trim()
        binding.put(measure?.metric, value)
      }
    }
    String report = makeSonarReportString(binding)
    dir('sonar_report') {
      writeFile file: 'sonar_report.html', text: report
    }
    String cspProp = System.getProperty("hudson.model.DirectoryBrowserSupport.CSP")
    if(cspProp == null || !cspProp.equals("")) {
      System.setProperty("hudson.model.DirectoryBrowserSupport.CSP", "")
    }
    publishHTML([allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true, reportDir: 'sonar_report', reportFiles: 'sonar_report.html', reportName: 'Sonar Report', reportTitles: 'Sonar Report'])
    new ElasticsearchUtils().postGenericData(anayticsBinding, "sonar")
  }
}

/**
 * Generates html string to be written to html file
 * @param binding map of measures to be used in template binding
 * @return html string
 */
def makeSonarReportString(def binding) {
  def template = libraryResource "templates/sonar-report.html"
  def engine = new GStringTemplateEngine()
  def report = engine.createTemplate(template).make(binding)
  return report.toString()
}

/**
 * Returns a single map of 2 json of measures
 * @param measuresString 1st set of json measures
 * @param second_measuresString 2nd set of json measures
 * @return map of all measures
 */
def getMeasuresMap(def measuresString, def second_measuresString) {
  def measures = readJSON text: measuresString
  def secondMeasures = readJSON text: second_measuresString
  measures.addAll(secondMeasures)
  return measures
}

/**
 * Maps numerical values of ratings to letters
 * @param value numerical value of rating
 * @return letter of rating
 */
def getRating(String value) {
  switch (value) {
    case "1.0": return "A"
    break
    case "2.0": return "B"
    break
    case "3.0": return "C"
    break
    case "4.0": return "D"
    break
    case "5.0": return "E"
    break
  }
}

/**
 * Publishes test results
 * @param testParams test parameters
 * @return
 */
def publishTestReports(def testParams) {
  def results = CommonUtils.getKeyValue(testParams, "results")
  if(results != null) {
    results.each { result ->
      String path = CommonUtils.getKeyValue(result, "path")
      String publisher = CommonUtils.getKeyValue(result, "publisher")
      if(path != null && publisher != null) {
        path = path.trim()
        switch (publisher.toLowerCase().trim()) {
          case 'junit':
            junit allowEmptyResults: true, testResults: path
            break
          case 'testng':
            step([$class: 'Publisher', reportFilenamePattern: path])
            break
          case 'archive':
            archiveArtifacts allowEmptyArchive: true, artifacts: path, caseSensitive: false, fingerprint: true
            break
          case 'html':
            String name = CommonUtils.getKeyValue(testParams, "name", "Deployment Test").toString().trim().replaceAll("\\s", "-")
            publishHTML([allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true, reportDir: '', reportFiles: path, reportName: "${name} Report", reportTitles: ''])
            break
        }
      }
    }
  }
}
