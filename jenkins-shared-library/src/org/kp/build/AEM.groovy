package org.kp.build

import org.kp.test.Cobertura

/**
 * Perform code analysis for AEM project
 * @param sonarOptions sonarqube analysis parameters
 * @return
 */
def analyze(String sonarOptions) {
  kpNpm {
    new Maven().analyze(sonarOptions)
    new Cobertura().publish()
  }
}