package org.kp.build

import org.kp.utils.*
import org.kp.constants.*

/**
 * Build gradle project
 * @param repo directory where gradle app is checked out
 * @param buildParams gradle build parameters
 * @return
 */
def build(String repo, def buildParams) {
  def tasks = Default.GRADLETASKS
  def properties = null
  if(buildParams != null) {
    tasks = CommonUtils.getKeyValue(buildParams, "goalsTasks", Default.GRADLETASKS)
    properties = CommonUtils.getKeyValue(buildParams, "properties")
  } else {
    if(new CommonUtils().getParamValue("disableUnit")) {
      tasks = tasks + " -x test"
    }
  }

  def rtGradle = getTool(properties)
	def buildInfo = new Builder().getBuildInfo()

  String envStageName = env.STAGE_NAME.substring(3, env.STAGE_NAME.length())
  withEnv(["STAGE_NAME=${envStageName}"]) {
    if(isUnix()) {
      rtGradle.run rootDir: "${repo}/".toString(), switches: Constant.GRADLESWITCHES.toString(), buildFile: AppType.MARKER_GRADLE.toString(), tasks: tasks.toString(), buildInfo: buildInfo
    } else {
      // withMaven is used intentionally to provide appropriate JDK and maven-like environment
      withMaven(jdk: Constant.JDK_WIN, maven: Constant.MAVENTOOL) {
        rtGradle.run rootDir: "${repo}\\".toString(), switches: Constant.GRADLESWITCHES.toString(), buildFile: AppType.MARKER_GRADLE.toString(), tasks: tasks.toString(), buildInfo: buildInfo
      }
    }
  }

  BuildData.instance.buildTool = rtGradle
  BuildData.instance.buildInfo = buildInfo
}

/**
* Run Gradle task
* @param tasks tasks to invoke during Gradle run
* @param directory path to directory containing build.gradle
* @return
*/
def runGradle(String tasks, String directory = env.APPLICATION_DIR) {
  def rtGradle = getTool()
  String envStageName = env.STAGE_NAME.substring(3, env.STAGE_NAME.length())
  withEnv(["STAGE_NAME=${envStageName}"]) {
    if(isUnix()) {
      rtGradle.run rootDir: "${directory}/".toString(), switches: Constant.GRADLESWITCHES.toString(), buildFile: AppType.MARKER_GRADLE.toString(), tasks: tasks.toString()
    } else {
      // withMaven is used intentionally to provide appropriate JDK and maven like environment
      withMaven(jdk: Constant.JDK_WIN, maven: Constant.MAVENTOOL) {
        rtGradle.run rootDir: "${directory}\\".toString(), switches: Constant.GRADLESWITCHES.toString(), buildFile: AppType.MARKER_GRADLE.toString(), tasks: tasks.toString()
      }
    }
  }
}

/**
 * Perform code analysis for gradle project
 * @param sonarOptions sonarqube analysis parameters
 * @return
 */
def analyze(String sonarOptions) {
  withSonarQubeEnv(Constant.SONAR_SERVER) {
    runGradle("""sonarqube ${sonarOptions}""")
  }
}

/**
 * Sets gradle tool
 * @return artifactory gradle build instance
 */
def getTool(def properties = null) {
  def server = new ArtifactoryUtils().getServer()
  def rtGradle = Artifactory.newGradleBuild()
  boolean isSnapShot = new ApplicationUtils().isSnapShot()
  if(isSnapShot) {
    rtGradle.resolver server: server, repo: Constant.ARTIFACTORY_RESOLVER_SNAPSHOT
    rtGradle.deployer server: server, repo: Constant.ARTIFACTORY_DEPLOYER_SNAPSHOT
  } else {
    rtGradle.resolver server: server, repo: Constant.ARTIFACTORY_RESOLVER_RELEASE
    rtGradle.deployer server: server, repo: Constant.ARTIFACTORY_DEPLOYER_RELEASE
  }
  rtGradle.deployer.deployArtifacts = false
  rtGradle.tool = Constant.GRADLETOOL

  def tools = new CommonUtils().getParamValue("tools")
  if(tools != null) {
    tools.keySet().each { toolName ->
      switch (toolName.toString().toUpperCase()) {
        case "GRADLE":
          rtGradle.tool = CommonUtils.getKeyValue(tools, toolName, Constant.GRADLETOOL).toString()
          break
      }
    }
  }

  if(properties != null) {
    rtGradle = new Builder().setBuildProperties(rtGradle, properties)
  }
  return rtGradle
}