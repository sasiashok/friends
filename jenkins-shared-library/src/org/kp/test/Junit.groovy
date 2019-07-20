package org.kp.test
import org.kp.build.*
import org.kp.utils.*
import org.kp.constants.*

/**
 * Orchestrates Junit tests for various build tools
 * @return
 */
def test() {
  ApplicationUtils applicationUtils = new ApplicationUtils()
  def unitArchiveOnly = new CommonUtils().getParamValue("unitArchiveOnly")
  String lockName = applicationUtils.getProjectKey()+"_"+applicationUtils.getUnifiedAppName()+"_UnitTests_"+env.NODE_NAME
  kpLock(lockName) {
    stage(Stage.UNIT_TEST) {
      try {
        if(!unitArchiveOnly) {
          switch(BuildData.instance.appType) {
            case AppType.GRADLE:
              new Gradle().runGradle(Constant.TESTGOAL)
              break
            case AppType.ANGULAR:
              new Angular().performRun(Constant.NPMTESTPARAMS)
              break
            case AppType.NODEJS:
              new NodeJS().performRun(Constant.NPMTESTPARAMS)
              break
            case AppType.AEM:
            case AppType.MAVEN:
              new Maven().runMaven(Constant.TESTGOAL)
              break
          }
        }
      } catch (e) {
        error "🚨 [ERROR] JUnit tests failed : " + e.getMessage()
      } finally {
        evaluateArchive()
      }
    }
  }
}

/**
 * Archives Junit test results for various build tools
 * @return
 */
def evaluateArchive() {
  switch(BuildData.instance.appType) {
    case AppType.GRADLE:
      archiveResults(Constant.GRADLEJUNIT)
      break
    case AppType.AEM:
    case AppType.MAVEN:
      archiveResults(Constant.MAVENJUNIT)
      break
    case AppType.ANGULAR:
      archiveResults(Constant.ANGULARJUNIT)
      break
  }
}

/**
 * Performs actual Junit result publishing for given result path
 * @param resultPath path of junit report file
 * @return
 */
def archiveResults(String resultPath) {
  dir(env.APPLICATION_DIR) {
    junit allowEmptyResults: true, testResults: resultPath
  }
}