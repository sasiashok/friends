timestamps {
  agent any {
    try {
      deleteDir()
      properties([buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '', numToKeepStr: '15'))])
      def gradleTool = tool name: 'Gradle46', type: 'gradle'
      stage("🎬 Checkout") {
        checkout scm
      }
      stage("🔬 Unit Testing") {
        try {
          String envStageName = env.STAGE_NAME.substring(3, env.STAGE_NAME.length())
          withEnv(["STAGE_NAME=${envStageName}"]) {
            sh "${gradleTool}/bin/gradle clean test"
          }
        } finally {
          junit allowEmptyResults: true, testResults: 'build/test-results/test/*.xml'
        }
      }
      stage("🔎 Code Coverage and Analysis") {
        try {
          String envStageName = env.STAGE_NAME.substring(3, env.STAGE_NAME.length())
          withEnv(["STAGE_NAME=${envStageName}"]) {
            withSonarQubeEnv('Sonar Server') {
              sh "${gradleTool}/bin/gradle sonar"
            }
          }
        } finally {
          jacoco()
        }
      }
    } finally {
      deleteDir()
    }
  }
}
