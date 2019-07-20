package org.kp.build

import org.kp.utils.*
import org.kp.constants.*
import org.kp.test.Analyzer

/**
 * Checkout all buildRepos
 * Build distribution method.
 * Based on application type for each buildRepo, corresponding build tool is invoked.
 * @return
 */
def build() {
  timeout(time: 2, unit: 'HOURS') {
    def buildRepos = new GitUtils().clone()
    try {
      ApplicationUtils applicationUtils = new ApplicationUtils()
      String lockName = applicationUtils.getProjectKey()+"_"+applicationUtils.getUnifiedAppName()+"_Build_"+env.NODE_NAME
      kpLock(lockName) {
        stage (Stage.BUILD) {
          buildRepos.each { repo ->
            switch (repo?.appType) {
              case AppType.AEM:
              case AppType.MAVEN:
                new Maven().build(repo?.directory.toString().trim(), repo?.buildParams)
                break
              case AppType.GRADLE:
                new Gradle().build(repo?.directory.toString().trim(), repo?.buildParams)
                break
              case AppType.DOTNET:
                new MSBuild().build(repo?.directory.toString().trim(), repo?.buildParams)
                break
              case AppType.ANGULAR:
                new Angular().build(repo?.directory.toString().trim(), repo?.buildParams)
                break
              case AppType.NODEJS:
                new NodeJS().build(repo?.directory.toString().trim(), repo?.buildParams)
                break
              case AppType.PHP:
                new Php().build(repo?.directory.toString().trim(), repo?.buildParams)
                break
              default:
                error Message.UNKNOWN_APP
                break
            }
          }
        }
      }
    } catch (e) {
      new Analyzer().archiveUnitResults()
      error Message.BUILD_FAILURE + e.getMessage()
    }
  }
}

/**
 * Sets buildInfo object
 * @return buildInfo object
 */
def getBuildInfo() {
  def buildInfo = Artifactory.newBuildInfo()
  buildInfo.env.capture = true
  buildInfo.name = new ApplicationUtils().getUnifiedAppName()
  buildInfo.number = env.BUILD_NUMBER
  return buildInfo
}

/**
 * Sets build properites on the buildTool
 * @param buildTool buildTool to be modified
 * @param properties properties to be added
 * @return buildTool with properties
 */
def setBuildProperties(def buildTool, def properties) {
  properties.each { property ->
    if(property.size()>1) {
      buildTool.deployer.addProperty(property[0],property[1])
    } else {
      buildTool.deployer.addProperty(property[0])
    }
  }
  return buildTool
}