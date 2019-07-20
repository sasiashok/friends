package org.kp.build

import org.kp.constants.Constant
import org.kp.utils.CommonUtils

/**
 * Build an NodeJS app
 * @param repo directory where NodeJS app is checked out
 * @return
 */
def build(String repo, def buildParams) {
  String params = ""
  if(buildParams != null) {
    params = CommonUtils.getKeyValue(buildParams, "goalsTasks", "")
  }
  boolean npmRunBuild = new CommonUtils().getParamValue("npmRunBuild")

  dir(repo) {
    kpNpm {
      kpSh "npm install ${params.trim()}"
      if(npmRunBuild) {
        kpSh "npm run build"
      }
    }
  }
}

/**
 * Perform execution for NodeJS
 * @param runParams run time parameters to be passed to npm
 * @param directory path to directory containing package.json
 * @return
 */
def performRun(String runParams, String directory = env.APPLICATION_DIR) {
  dir(directory) {
    kpNpm {
      kpSh """npm ${runParams}"""
    }
  }
}

/**
 * Perform code analysis for NodeJS and Angular project
 * @return
 */
def analyze() {
  dir(env.APPLICATION_DIR) {
    withSonarQubeEnv(Constant.SONAR_SERVER) {
      def sonarScanner = tool name: Constant.SONAR_SCANNER, type: 'hudson.plugins.sonar.SonarRunnerInstallation'
      env.PATH = "${sonarScanner}/bin:${env.PATH}"
      kpNpm {
        kpSh "sonar-scanner"
      }
    }
  }
}

