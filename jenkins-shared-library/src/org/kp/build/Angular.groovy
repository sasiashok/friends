package org.kp.build

import org.kp.utils.CommonUtils

/**
 * Build an angular app
 * @param repo directory where angular app is checked out
 * @return
 */
def build(String repo, def buildParams) {
  String params = ""
  if(buildParams != null) {
    params = CommonUtils.getKeyValue(buildParams, "goalsTasks", "")
  }
  dir(repo) {
    kpNpm {
      kpSh """npm install
              ng build ${params.trim()}"""
    }
  }
}

/**
 * Perform execution for Angular
 * @param runParams run time parameters to be passed to ng
 * @param directory path to directory containing package.json
 * @return
 */
def performRun(String runParams, String directory = env.APPLICATION_DIR) {
  dir(directory) {
    kpNpm {
      kpSh "ng ${runParams}"
    }
  }
}
