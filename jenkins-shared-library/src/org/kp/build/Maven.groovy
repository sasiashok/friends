package org.kp.build

import org.kp.utils.*
import org.kp.constants.*

/**
 * Build maven project
 * @param repo directory where maven app is checked out
 * @param buildParams maven build parameters
 * @return
 */
def build(String repo, def buildParams) {
  def goals = Default.MAVENGOALS
  def properties = null
  if(buildParams != null) {
    goals = CommonUtils.getKeyValue(buildParams, "goalsTasks", Default.MAVENGOALS)
    properties = CommonUtils.getKeyValue(buildParams, "properties")
  } else {
    if(new CommonUtils().getParamValue("disableUnit")) {
      goals = goals + " -Dmaven.test.skip=true"
    }
  }

  def rtMaven = getTool(properties)
  def buildInfo = new Builder().getBuildInfo()

  if(isUnix() ) {
    buildInfo = rtMaven.run pom: "${repo}/pom.xml".toString(), goals: goals.toString(),  buildInfo: buildInfo
  } else {
    withMaven(jdk: Constant.JDK_WIN, maven: Constant.MAVENTOOL) {
      buildInfo = rtMaven.run pom: "${repo}\\pom.xml".toString(), goals: goals.toString(),  buildInfo: buildInfo
    }
  }

  BuildData.instance.buildTool = rtMaven
  BuildData.instance.buildInfo = buildInfo
}

/**
 * Run Maven goal
 * @param goal goal to invoke during Maven run
 * @param directory path to directory containing pom.xml
 * @return
 */
def runMaven(String goal, String directory = env.APPLICATION_DIR) {
  def rtMaven = getTool()
  if(isUnix() ) {
    rtMaven.run pom: "${directory}/pom.xml".toString(), goals: goal.toString()
  } else {
    withMaven(jdk: Constant.JDK_WIN, maven: Constant.MAVENTOOL) {
      rtMaven.run pom: "${directory}\\pom.xml".toString(), goals: goal.toString()
    }
  }
}

/**
 * Perform code analysis for maven project
 * @param sonarOptions sonarqube analysis parameters
 * @return
 */
def analyze(String sonarOptions) {
  withSonarQubeEnv(Constant.SONAR_SERVER) {
    runMaven("""org.sonarsource.scanner.maven:sonar-maven-plugin:3.5.0.1254:sonar ${sonarOptions} -U""")
  }
}

/**
 * Sets maven tool
 * @return artifactory maven build instance
 */
def getTool(def properties = null) {
  def server = new ArtifactoryUtils().getServer()
  def rtMaven = Artifactory.newMavenBuild()
  rtMaven.resolver server: server, releaseRepo: Constant.ARTIFACTORY_RESOLVER_RELEASE, snapshotRepo: Constant.ARTIFACTORY_RESOLVER_SNAPSHOT
  rtMaven.deployer server: server, releaseRepo: Constant.ARTIFACTORY_DEPLOYER_RELEASE, snapshotRepo: Constant.ARTIFACTORY_DEPLOYER_SNAPSHOT
  rtMaven.deployer.deployArtifacts = false

  rtMaven.tool = Constant.MAVENTOOL
  def tools = new CommonUtils().getParamValue("tools")
  if(tools != null) {
    tools.keySet().each { toolName ->
      switch (toolName.toString().toUpperCase()) {
        case "MAVEN":
          rtMaven.tool = CommonUtils.getKeyValue(tools, toolName, Constant.MAVENTOOL).toString()
          break
      }
    }
  }

  if(properties != null) {
    rtMaven = new Builder().setBuildProperties(rtMaven, properties)
  }
  return rtMaven
}