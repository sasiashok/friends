package org.kp.build

import org.kp.utils.*
import org.kp.constants.*
import org.kp.test.Nunit

/**
 * Build .NET project
 * @param repo directory where .NET app is checked out
 * @param buildParams .NET build parameters
 * @return
 */
def build(String repo, def buildParams) {

  String buildToolName = Constant.MSBUILDTOOL
  String msSonarToolName = Constant.MSSONARTOOL
  String nugetToolName = Constant.NUGETTOOL

  def tools = new CommonUtils().getParamValue("tools")
  if(tools != null) {
    tools.keySet().each { toolName ->
      switch (toolName.toString().toUpperCase()) {
        case "MSBUILD":
          buildToolName = CommonUtils.getKeyValue(tools, toolName, Constant.MSBUILDTOOL).toString()
          break
        case "MSSONAR":
          msSonarToolName = CommonUtils.getKeyValue(tools, toolName, Constant.MSSONARTOOL).toString()
          break
        case "NUGET":
          nugetToolName = CommonUtils.getKeyValue(tools, toolName, Constant.NUGETTOOL).toString()
          break
      }
    }
  }

  String msBuildTool = tool name: buildToolName.toString(), type: 'msbuild'
  String msSonarTool = tool name: msSonarToolName.toString(), type: 'hudson.plugins.sonar.MsBuildSQRunnerInstallation'
  String nugetTool = tool name: nugetToolName.toString(), type: 'com.cloudbees.jenkins.plugins.customtools.CustomTool'
  String switches = getSwitches(buildParams)

  dir(repo) {
    def disableCodeAnalysis = new CommonUtils().getParamValue("disableCodeAnalysis")
    String buildCommand = """\"${msBuildTool}\" ${switches} /m"""
    if(disableCodeAnalysis) {
      performBuild(nugetTool, buildCommand)
    } else {
      withSonarQubeEnv(Constant.SONAR_SERVER) {
        beginSonar(msSonarTool, getSonarSwitches())
        performBuild(nugetTool, buildCommand)
      }
    }
  }
}

def beginSonar(def msSonarTool, String sonarSwitches) {
  bat """
  ${msSonarTool}\\SonarScanner.MSBuild.exe begin ${sonarSwitches}
  """
}

def performBuild(def nugetTool, String buildCommand) {
  bat """
  ${nugetTool} restore -Source ${new ArtifactoryUtils().getServer().url}/api/nuget/${Constant.ARTIFACTORY_NUGET_LOCAL} -NoCache
  ${buildCommand}
  """
  boolean isLibrary = new CommonUtils().getParamValue("isLibrary", false)
  if(isLibrary) {
    bat """
    ${nugetTool} pack
    """
  }
}

/**
 * Returns sonarqube switches used with begin step of MSBuild
 * @return sonar switches
 */
String getSonarSwitches() {
  ApplicationUtils applicationUtils = new ApplicationUtils()
  String projectKey = applicationUtils.getAssemblyName()
  String appVersion = applicationUtils.getApplicationVersion()
  String codeCoverageTool = Constant.MSCOVERAGETOOL.toLowerCase()
  String sonarSwitches = "/k:${projectKey} /v:${appVersion} /d:sonar.vbnet.${codeCoverageTool}.reportsPaths=${codeCoverageTool}.xml /d:sonar.cs.${codeCoverageTool}.reportsPaths=${codeCoverageTool}.xml"
  return sonarSwitches
}

/**
 * Return build switches for MSBuild
 * @param buildParams map of user-defined switches
 * @return
 */
String getSwitches(def buildParams) {
  String switches = ""

  String target = Default.MSTARGET
  def properties = null
  def validate = null
  String verbosity = null
  String consoleLogger = ""
  def distributedLogger = null
  def logger = null
  String extra = ""
  if (buildParams != null) {
    target = CommonUtils.getKeyValue(buildParams, "target", Default.MSTARGET).toString().trim()
    properties = CommonUtils.getKeyValue(buildParams, "properties")
    validate = CommonUtils.getKeyValue(buildParams, "validate")
    verbosity = CommonUtils.getKeyValue(buildParams, "verbosity", Default.MSBUILD_VERBOSITY).toString().trim()
    consoleLogger = CommonUtils.getKeyValue(buildParams, "consoleLogger", "").toString().trim()
    distributedLogger = CommonUtils.getKeyValue(buildParams, "distributedLogger", null)
    logger = CommonUtils.getKeyValue(buildParams, "logger", null)
    extra = CommonUtils.getKeyValue(buildParams, "extra", "").toString().trim()
  }

  if (isSwitchSet(target)) {
    switches = switches.concat("/t:${target}")
  }

  if (isSwitchSet(properties)) {
    String propertySwitch = getKeyValueSwitch(properties)
    switches = switches.concat(" /property:${propertySwitch}")
  }

  if (isSwitchSet(validate)) {
    String schema = CommonUtils.getKeyValue(validate, "schema")
    if (schema != null) {
      switches = switches.concat(" /validate:${schema}")
    } else {
      switches = switches.concat(" /validate")
    }
  }

  if (isSwitchSet(verbosity)) {
    switches = switches.concat(" /verbosity:${verbosity}")
  }

  if (isSwitchSet(consoleLogger)) {
    switches = switches.concat(" /consoleloggerparameters:${consoleLogger}")
  }

  if (isSwitchSet(distributedLogger)) {
    String distributedLoggerSwitch = getKeyValueSwitch(distributedLogger)
    switches = switches.concat(" /distributedlogger:${distributedLoggerSwitch}")
  }

  if (isSwitchSet(logger)) {
    String loggerSwitch = getKeyValueSwitch(logger)
    switches = switches.concat(" /logger:${loggerSwitch}")
  }

  if (isSwitchSet(extra)) {
    switches = switches.concat(" " + extra)
  }
  return switches
}

/**
 * Return switch equivalent string for build properties
 * @param props user-defined build properites
 * @return switch equivalent string
 */
String getKeyValueSwitch(def props) {
  String switchValue = ""
  props.each { prop ->
    if (prop.size() > 1) {
      switchValue = switchValue.concat("${prop[0]}=${prop[1]};")
    } else {
      switchValue = switchValue.concat("${prop[0]};")
    }
  }
  if (switchValue.endsWith(";")) {
    switchValue = switchValue.substring(0, switchValue.length() - 1)
  }
  return switchValue
}

/**
 * Return boolean value if a given switch was set.
 * @param switchKey swith to test
 * @return true | false if the switch was set
 */
boolean isSwitchSet(def switchKey) {
  if (switchKey != null) {
    if (switchKey instanceof String) {
      return switchKey.length() > 0 ? true : false
    }
    if (switchKey instanceof Map || switchKey instanceof List) {
      return switchKey.size() > 0 ? true : false
    }
  }
  return false
}

/**
 * Perform code analysis for .NET project
 * @return
 */
def analyze() {
  String msSonarTool = tool name: Constant.MSSONARTOOL, type: 'hudson.plugins.sonar.MsBuildSQRunnerInstallation'
  withSonarQubeEnv(Constant.SONAR_SERVER) {
    String codeCoverageTool = Constant.MSCOVERAGETOOL
    Nunit nUnit = new Nunit()
    def sonarParams = new CommonUtils().getParamValue("sonarParams")
    sonarParams.put("withSonar", true)
    def nUnitParams = nUnit.getParams(sonarParams)
    def disableUnit = new CommonUtils().getParamValue("disableUnit")
    String sonarCommand = """\"${tool codeCoverageTool}\" -output:${codeCoverageTool.toLowerCase()}.xml -register:user"""
    if(!disableUnit) {
      sonarCommand = sonarCommand + """ -target:${tool Constant.NUNITTOOL} ${nUnitParams}"""
    }
    bat """
    ${sonarCommand}
    """
    bat """
    \"${msSonarTool}\"\\SonarScanner.MSBuild.exe end
    """
  }
}
