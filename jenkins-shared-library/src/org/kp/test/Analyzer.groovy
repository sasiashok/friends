package org.kp.test

import org.kp.build.*
import org.kp.utils.*
import org.kp.constants.*

/**
 * Orchestrates code analysis and unit tests
 * @return
 */
def testAnalyze() {
  CommonUtils commonUtils = new CommonUtils()
  def disableCodeAnalysis = commonUtils.getParamValue("disableCodeAnalysis")
  def disableUnit = commonUtils.getParamValue("disableUnit")
  if (BuildData.instance.appType.equals(AppType.DOTNET)) {
    if(!disableCodeAnalysis) {
      analyze()
    }
    if(!disableUnit) {
      unitTests()
    }
  } else {
    if(!disableUnit) {
      unitTests()
    }
    if(!disableCodeAnalysis) {
      analyze()
    }
  }
}

/**
* Orchestrates code analysis for all build tools
* @return
*/
def analyze() {
  timeout(time: 2, unit: 'HOURS') {
    ApplicationUtils applicationUtils = new ApplicationUtils()
    try {
      String lockName = applicationUtils.getProjectKey() + "_" + applicationUtils.getUnifiedAppName() + "_CodeAnalysis"
      String sonarOptions = getSonarOptions()
      kpLock(lockName) {
        stage(Stage.CODE_ANALYSIS) {
          switch (BuildData.instance.appType) {
            case AppType.MAVEN:
              new Maven().analyze(sonarOptions)
              break
            case AppType.GRADLE:
              new Gradle().analyze(sonarOptions)
              break
            case AppType.AEM:
              new AEM().analyze(sonarOptions)
              break
            case AppType.DOTNET:
              new MSBuild().analyze()
              break
            case AppType.ANGULAR:
            case AppType.NODEJS:
              new NodeJS().analyze()
              break
            case AppType.PHP:
              new Php().analyze()
              break
            default:
              error Message.UNKNOWN_APP
              break
          }
          evaluateSonarResults()
        }
      }
    } catch (e) {
      String errorMessage = "Code Coverage and Analysis using SonarQube failed. "
      if (e.getMessage() != null) {
        error errorMessage + e.getMessage()
      } else {
        error errorMessage
      }
    } finally {
      publishCoverageReports()
    }
  }
}

/**
 * Publish code coverage reports for respective build tools
 * @return
 */
def publishCoverageReports() {
  switch (BuildData.instance.appType) {
    case AppType.MAVEN:
    case AppType.GRADLE:
    case AppType.AEM:
      publishJacocoReports()
      break
    case AppType.ANGULAR:
    case AppType.NODEJS:
      publishCoberturaReports()
      break
  }
}

/**
 * Publishes Jacoco coverage reports
 * @return
 */
def publishJacocoReports() {
  dir(env.APPLICATION_DIR) {
    def files = findFiles glob: '**/**.exec'
    def jacocoExists = files.length > 0
    if (jacocoExists) {
      jacoco()
    }
  }
}

/**
 * Publishes Cobertura coverage reports
 * @return
 */
def publishCoberturaReports() {
  dir(env.APPLICATION_DIR) {
    def coberturaReportsExists = fileExists Default.CORBETURA_ANGULAR_COBERTURAREPORTFILE
    if (coberturaReportsExists) {
      def coberturaParams = [:]
      coberturaParams.put("coberturaReportFile", Default.CORBETURA_ANGULAR_COBERTURAREPORTFILE)
      BuildData.instance.branchParams.put("cobertura", [coberturaParams])
      new Cobertura().publish()
    }
  }
}

/**
 * Get sonar exclusions to be passed as options to sonarqube scanners
 * @return
 */
def getSonarOptions() {
  String sonarOptions = ""
  String exclusions = new CommonUtils().getParamValue("sonarExclusions")
  if(exclusions != null) {
    switch(BuildData.instance.appType) {
      case AppType.MAVEN:
      case AppType.AEM:
        sonarOptions = "-Dsonar.coverage.exclusions=${exclusions}"
        break
      case AppType.GRADLE:
        sonarOptions = "-Dsonar.exclusions=${exclusions}"
        break
    }
  }
  return sonarOptions
}

/**
 * Evaluate sonar quality gates
 * @return
 */
def evaluateSonarResults() {
  CommonUtils commonUtils = new CommonUtils()
  String sonarApprovers = commonUtils.getParamValue("sonarApprovers", BuildData.instance.emailRecipients)

  def qualityGate = null
  if(BuildData.instance.appType.equals(AppType.ANGULAR) || BuildData.instance.appType.equals(AppType.NODEJS) || BuildData.instance.appType.equals(AppType.PHP)) {
    sleep 3
  }
  timeout(10) {
    qualityGate = waitForQualityGate()
    echo 'ℹ️ [INFO] SonarQube Quality Gate status: ' + qualityGate.status
  }
  if (qualityGate != null) {
    try {
      new Reporter().publishSonarReport(qualityGate.status)
    } catch (e) {
      echo "ℹ️ [INFO] Failed to publish Sonar Report. Ignoring error and proceeding. For reference cause: " + e.getMessage()
    }
    if (qualityGate.status != 'OK') {
      String errorMessage = "Aborted due to SonarQube Quality Gate failure."
      if (new GitUtils().isPR()) {
        error errorMessage
      } else {
        if (sonarApprovers != null) {
          int timeLimit = 10
          new NotificationUtils().sendMail("SonarQube Failure", sonarApprovers, "⚠️ Code Coverage and Analysis using SonarQube failed. SonarQube Quality Gate status ${qualityGate.status} \n\n ⏳ Timer of ${timeLimit} minutes has started for you to provide an input before the build is auto-aborted. \n\n Please provide your authorization and acknowledgement to proceed with SonarQube Quality Gate failure here")
          timeout(timeLimit) {
            try {
              input id: 'sonarqube', message: "⚠️ Proceed acknowledging SonarQube Quality Gate Failure?", ok: Message.INPUT_PROCEED, submitter: sonarApprovers.replaceAll("\\s", "")
            } catch (e) {
              error errorMessage
            }
          }
        } else {
          error errorMessage + " If you wish to proceed with SonarQube Quality Gate failure please set sonarApprovers who will authorize to proceed."
        }
      }
    }
  }
}

/**
 * Orchestrates unit tests for various build tools
 * @return
 */
def unitTests() {
  timeout(time: 1, unit: 'HOURS') {
    switch (BuildData.instance.appType) {
      case AppType.DOTNET:
        new Nunit().test()
        break
      default:
        new Junit().test()
        break
    }
  }
}

/**
 * Orchestrates unit result archival for various build tools
 * @return
 */
def archiveUnitResults() {
  switch (BuildData.instance.appType) {
    case AppType.DOTNET:
      Nunit nunit = new Nunit()
      nunit.archiveResults(nunit.getResultPath(new CommonUtils().getParamValue("unitTestParams")))
      break
    default:
      new Junit().evaluateArchive()
      break
  }
}
