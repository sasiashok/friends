package org.kp.test

import org.kp.utils.*
import org.kp.constants.*

/**
 * Performs NUnit testing
 * @return
 */
def test() {
  CommonUtils commonUtils = new CommonUtils()
  ApplicationUtils applicationUtils = new ApplicationUtils()
  def unitArchiveOnly = commonUtils.getParamValue("unitArchiveOnly")
  def unitTestParams = commonUtils.getParamValue("unitTestParams", commonUtils.getParamValue("sonarParams"))
  String lockName = applicationUtils.getProjectKey() + "_" + applicationUtils.getUnifiedAppName() + "_UnitTests_" + env.NODE_NAME
  kpLock(lockName) {
    stage(Stage.UNIT_TEST) {
      try {
        if (!unitArchiveOnly) {
          String params = getParams(unitTestParams)
          def nUnitTool = tool name: Constant.NUNITTOOL, type: 'com.cloudbees.jenkins.plugins.customtools.CustomTool'
          bat """
          \"${nUnitTool}\" ${params} 
          """
        }
      } catch (e) {
        error "🚨 [ERROR] NUnit tests failed: " + e.getMessage()
      } finally {
        archiveResults(getResultPath(unitTestParams))
      }
    }
  }
}

/**
 * Parses and returns parameters for NUnit
 * @param unitTestParams map of NUnit test parameters
 * @return
 */
String getParams(def unitTestParams) {
  String params = ""
  String inputFiles = ""
  String result = getResultPath(unitTestParams)
  boolean withSonar = false

  if (unitTestParams != null) {
    inputFiles = CommonUtils.getKeyValue(unitTestParams, "inputFiles", "").toString().trim()
    withSonar = CommonUtils.getKeyValue(unitTestParams, "withSonar", false)
  }

  if (isOptionSet(inputFiles)) {
    if(withSonar) {
      params = params.concat(" -targetargs:"+ inputFiles)
    } else {
      params = params.concat("${inputFiles}")
    }
  } else {
    error "No input files specified."
  }

  if (isOptionSet(result)) {
    if(withSonar) {
      params = params.concat(" -targetargs:"+ "--result=${result}")
    } else {
      params = params.concat("--result=${result}")
    }
  }

  return params
}

/**
 * Checks if an option key was set or not
 * @param optionKey optionKey to check
 * @return true | false if the key was set
 */
boolean isOptionSet(def optionKey) {
  if (optionKey != null) {
    if (optionKey instanceof String) {
      return optionKey.length() > 0 ? true : false
    }
    if (optionKey instanceof Map || optionKey instanceof List) {
      return optionKey.size() > 0 ? true : false
    }
  }
  return false
}

/**
 * Gets Nunit result path from test params
 * @param unitTestParams test params that may have the result path declared
 * @return
 */
String getResultPath(def unitTestParams) {
  if (unitTestParams != null) {
    return CommonUtils.getKeyValue(unitTestParams, "result", Default.NUNIT_RESULT).toString().trim()
  }
  return Default.NUNIT_RESULT
}

/**
 * Publish Nunit results
 * @param resultPath result path to Nunit results
 * @return
 */
def archiveResults(String resultPath) {
  dir(env.APPLICATION_DIR) {
    if (!resultPath.startsWith("**/")) {
      resultPath = "**/" + resultPath
    }
    nunit failIfNoResults: false, testResultsPattern: resultPath
  }
}
