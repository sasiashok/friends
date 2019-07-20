package org.kp.build

import org.kp.constants.Constant

/**
 * Place holder
 * @param
 * @return
 */
def build(String repo, def buildParams) {
  echo "Build Php"
}

/**
 * Perform code analysis for Php project
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

