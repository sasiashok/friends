package org.kp.test

import org.kp.analytics.ElasticsearchUtils
import org.kp.build.*
import org.kp.constants.*
import org.kp.deploy.*
import org.kp.utils.*

/**
 * Invokes tests for a given environment
 * @param testParams test parameters
 * @return
 */
def processTests(testParams) {
  CommonUtils commonUtils = new CommonUtils()
  String label = CommonUtils.getKeyValue(testParams, "label", BuildData.instance.agent).toString().trim()
  boolean remoteTest = CommonUtils.getKeyValue(testParams, "remoteTest", false)
  node(label) {
    dir('testRepo') {
      try {
        deleteDir()
        def applicationDir = pwd()
        String appType = AppType.REMOTE
        if (!remoteTest){
          new GitUtils().cloneSingleRepo(testParams)
          appType = new ApplicationUtils().getApplicationType(applicationDir)
        }
        commonUtils.setupAppEnvironment(appType)
        def suites = CommonUtils.getKeyValue(testParams, "suites")
        boolean ignoreFailure = CommonUtils.getKeyValue(testParams, "ignoreFailure", false)
        if (suites != null) {
          suites.each { suite ->
            try {
              if(remoteTest) {
                suite.put("url", CommonUtils.getKeyValue(testParams, "url"))
                suite.put("serverName", CommonUtils.getKeyValue(testParams, "serverName", Default.REMOTE_TEST_SERVER))
              }
              executeTests(suite, appType, applicationDir)
            } catch (e) {
              if(!ignoreFailure) {
                error e.getMessage()
              }
            }
          }
        } else {
          try {
            executeTests(testParams, appType, applicationDir)
          } catch (e) {
            if(!ignoreFailure) {
              error e.getMessage()
            }
          }
        }
        if(ignoreFailure) {
          currentBuild.result = 'SUCCESS'
        }
      } finally {
        new Reporter().publishTestReports(testParams)
        deleteDir()
      }
    }
  }
}

def executeTests(def executeParams, String appType, String appDir) {
  String name = CommonUtils.getKeyValue(executeParams, "name", "Deployment Test").toString().trim()
  stage(Stage.TEST_DEPLOYMENT + name + " - " + BuildData.instance.currentDeployEnv) {
    String script = CommonUtils.getKeyValue(executeParams, "script")
    switch (appType) {
      case AppType.ANGULAR:
      case AppType.NODEJS:
        kpNpm {
          kpSh "npm install"
        }
        break
    }
    if (script != null) {
      kpSh script
    }
    else if (appType.equals(AppType.REMOTE)){
      String buildUrl = executeRemoteTest(executeParams)
      archiveRemoteArtifacts(buildUrl)
    }
    else {
      String options = getTestOptions(executeParams, appType)
      switch (appType) {
        case AppType.AEM:
        case AppType.MAVEN:
          new Maven().runMaven("""test ${options}""", appDir)
          break
        case AppType.GRADLE:
          new Gradle().runGradle("""test ${options}""", appDir)
          break
        case AppType.ANGULAR:
          new Angular().performRun(options, appDir)
          break
        case AppType.NODEJS:
          new NodeJS().performRun(options, appDir)
          break
        default:
          error "Unsupported build tool used for testing or no script was provided."
          break
      }
    }
  }
}

def executeRemoteTest(def executeParams){
  String url = CommonUtils.getKeyValue(executeParams, "url")
  String serverName = CommonUtils.getKeyValue(executeParams, "serverName", Default.REMOTE_TEST_SERVER)
  String jobName = CommonUtils.getKeyValue(executeParams, "jobName")
  String tokenId = CommonUtils.getKeyValue(executeParams, "tokenId")
  String buildParameters = getTestOptions(executeParams, AppType.REMOTE)
  String credentials = CommonUtils.getKeyValue(executeParams, "credentials")
  def handle
  def remoteParams = [abortTriggeredJob: true, job: jobName, shouldNotFailBuild: true, maxConn: 1, pollInterval: 60, useCrumbCache: true, useJobInfoCache: true]
  if (credentials != null) {
    CommonUtils.addToMap(remoteParams, "auth", "CredentialsAuth(credentials: '${credentials}')")
  }
  if (tokenId != null){
    withCredentials([string(credentialsId: tokenId, variable: 'token')]) {
      CommonUtils.addToMap(remoteParams, "token", token)
    }
  }
  CommonUtils.addToMap(remoteParams, "remoteJenkinsUrl", url)
  CommonUtils.addToMap(remoteParams, "remoteJenkinsName", serverName)
  CommonUtils.addToMap(remoteParams, "jobName", jobName)
  CommonUtils.addToMap(remoteParams, "parameters", buildParameters)

  handle = triggerRemoteJob(remoteParams)
  currentBuild.result = handle.getBuildResult()
  String buildUrl = handle.getBuildUrl().toString()
  return buildUrl
}

def archiveRemoteArtifacts(String buildUrl){
  def artifactUrl = buildUrl.toString() + "artifact/*zip*/archive.zip"
  httpRequest quiet: true, outputFile: 'archive.zip', url: artifactUrl, validResponseCodes: '100:599'
  boolean validZip = unzip zipFile: "archive.zip", test: true
  if(validZip) {
    unzip zipFile: "archive.zip"
    dir("archive") {
      archiveArtifacts artifacts: "**/*", allowEmptyArchive: true
    }
  } else {
    echo "ℹ️ [INFO] No files archived on remote job."
  }
}


def getTestOptions(def executeParams, String appType) {
  CommonUtils commonUtils = new CommonUtils()
  def properties = commonUtils.getKeyValue(executeParams, "properties")
  String options = ""
  properties.each { property ->
    String propKey = property[0]
    if (property.size() > 1) {
      String propValue = property[1]
      switch (appType) {
        case AppType.REMOTE:
          options = options + "${propKey}=${propValue}\n"
          break
        case AppType.AEM:
        case AppType.MAVEN:
        case AppType.GRADLE:
          options = options + " -D${propKey}=\"${propValue}\""
          break
        default:
          options = options + " ${propKey}=\"${propValue}\""
          break
      }
    } else {
      switch (appType) {
        case AppType.AEM:
        case AppType.MAVEN:
        case AppType.GRADLE:
          options = options + " -D${propKey}"
          break
        default:
          options = options + " ${propKey}"
          break
      }
    }
  }
  return options.trim()
}

/**
 * Orchestrates tests for each environment
 * @param deployParams deploy parameters that may have tests declared and for invoking rollback
 * @return
 */
def testDeployment(def deployParams) {
  ArtifactoryUtils artifactoryUtils = new ArtifactoryUtils()
  Deployer deployer = new Deployer()
  String environment = BuildData.instance.currentDeployEnv
  String approvers = CommonUtils.getDeployKeyValue(deployParams, "approvers", BuildData.instance.emailRecipients)
  def testParams = CommonUtils.getDeployKeyValue(deployParams, "tests")
  if(testParams != null) {
    sleep 30
  }
  testParams.each { testParam ->
    int count = 1
    boolean reAttemptTest = true
    String name = CommonUtils.getKeyValue(testParam, "name", "Deployment Test")
    while (reAttemptTest) {
      try {
        processTests(testParam)
        reAttemptTest = false
      } catch (e) {
        count++
        if (count < 6) {
          try {
            echo "🚨 [ERROR] " + e.getMessage()
            new NotificationUtils().sendMail("Deployment Tests Failed", approvers, "deployment tests ${name} for environment ${environment} failed. \n\n Please provide your authorization to re-run the tests here")
            BuildData buildData = BuildData.instance
            input message: "Re-run tests?", ok: Message.INPUT_YES
            BuildData newBuildData = BuildData.instance
            newBuildData.setProps(buildData.getProperties())
          } catch (org.jenkinsci.plugins.workflow.steps.FlowInterruptedException flowInterrupted) {
            String abortUser = flowInterrupted.causes.get(0).getUser().toString()
            echo "ℹ️ [INFO] Testing Marked Failure by " + abortUser
            artifactoryUtils.promoteBuild("Testing in ${environment} - FAILED", "Marked failure by ${abortUser}")
            BuildData.instance.attemptDeployment = false
            reAttemptTest = false
            if (CommonUtils.getDeployKeyValue(deployParams, "rollback", false)) {
              deployer.processRollBack(deployParams)
            }
            return
          }
        } else {
          echo "ℹ️ [INFO] Testing Marked Failure by SYSTEM"
          artifactoryUtils.promoteBuild("Testing in ${environment} - FAILED", "Marked failure by SYSTEM")
          if (CommonUtils.getDeployKeyValue(deployParams, "rollback", false)) {
            deployer.processRollBack(deployParams)
          }
          error "${name} failed after ${count - 1} tries."
        }
      }
    }
  }
  verifyTestDeployment(deployParams)
}

/**
 * Verify test results for a given deployment.
 * @param deployParams deployment parameters
 * @return
 */
def verifyTestDeployment(def deployParams) {
  String environment = CommonUtils.getKeyValue(deployParams, "environment", Default.DEPLOYMENT_ENVIRONMENT)
  String approvers = CommonUtils.getDeployKeyValue(deployParams, "approvers", BuildData.instance.emailRecipients)
  NotificationUtils notificationUtils = new NotificationUtils()
  ArtifactoryUtils artifactoryUtils = new ArtifactoryUtils()
  Deployer deployer = new Deployer()
  if (BuildData.instance.attemptDeployment) {
    try {
      stage(Stage.TEST_VERIFICATION + environment) {
        notificationUtils.sendMail("Test Verification", approvers, "Deployed to ${environment} and tested. \n\n Please provide your verification whether the testing was successful or not here")
        if (approvers != null) {
          BuildData buildData = BuildData.instance
          String approver = input id: 'Deploy' + environment + env.BUILD_NUMBER, message: "🤔 Testing in ${environment} was successful?", ok: Message.INPUT_YES, submitter: approvers.replaceAll("\\s", ""), submitterParameter: 'approver'
          BuildData newBuildData = BuildData.instance
          newBuildData.setProps(buildData.getProperties())
          artifactoryUtils.promoteBuild("Testing in ${environment} - SUCCESSFUL", "Test Marked successful by ${approver}")
          setStableMetaData(environment)
        } else {
          error "No Approver provided for verification of deployment. Please provide provide approvers or set autoDeploy to true if no verification is required."
        }
      }
    } catch (org.jenkinsci.plugins.workflow.steps.FlowInterruptedException e) {
      cause = e.causes.get(0)
      String abortUser = cause.getUser().toString()
      echo "ℹ️ [INFO] Deployment Marked Failure by " + abortUser
      artifactoryUtils.promoteBuild("Testing in ${environment} - FAILED", "Marked failure by ${abortUser}")
      if (CommonUtils.getDeployKeyValue(deployParams, "rollback", false)) {
        deployer.processRollBack(deployParams)
      }
      BuildData.instance.attemptDeployment = false
      return
    }
    notificationUtils.sendMail("Deployment Report", BuildData.instance.emailRecipients, "deployed and tested successfully on environment ${environment}")
  }
}

/**
 * Sets a deployment as stable against a given environment
 * @param environment environment to be marked stable.
 * @return
 */
def setStableMetaData(String environment) {
  def releaseJsonMap = [:]
  if(BuildData.instance.isAdhocDeployment && BuildData.instance.stableReleaseData != null) {
    def envMap = [:]
    def stableEnv = BuildData.instance.stableReleaseData.environment
    envMap.put("BRANCH_NAME",stableEnv.BRANCH_NAME)
    envMap.put("BUILD_ID",stableEnv.BUILD_ID)
    envMap.put("BUILD_NUMBER",stableEnv.BUILD_NUMBER)
    envMap.put("BUILD_TAG",stableEnv.BUILD_TAG)
    envMap.put("BUILD_URL",stableEnv.BUILD_URL)
    releaseJsonMap.put("environment", envMap)
    releaseJsonMap.put("version", BuildData.instance.stableReleaseData.version)
    releaseJsonMap.put("fileName", BuildData.instance.stableReleaseData.fileName)
    releaseJsonMap.put("path", BuildData.instance.stableReleaseData.path)
    releaseJsonMap.put("download", BuildData.instance.stableReleaseData.download)
  } else {
    switch (BuildData.instance.currentPlatform) {
      case Platform.BLUEMIX:
        releaseJsonMap = new Bluemix().getStableMetaData()
        break
      case Platform.UCD:
        releaseJsonMap = new UCD().getStableMetaData()
        break
      case Platform.CRX:
        releaseJsonMap = new CRX().getStableMetaData()
      default:
        error Message.UNKNOWN_PLATFORM
        break
    }

    String version = new ApplicationUtils().getApplicationVersion()
    releaseJsonMap.put("version", version)
    releaseJsonMap.put("fileName", BuildData.instance.deployableFile)
    releaseJsonMap.put("path", BuildData.instance.deployableTarget)
  }
  releaseJsonMap.put("deployEnv", environment.toLowerCase().trim())
  new ElasticsearchUtils().postGenericData(releaseJsonMap, "stable-release")
}
