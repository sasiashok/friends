package org.kp.deploy

import org.kp.constants.*
import org.kp.utils.*
import org.kp.test.TestDeployment

/**
 * Invokes deploy functions based on deployment platform
 * @param envParams Environment parameters to be passed to deploy function
 * @return
 */
def performDeployment(envParams) {
  BuildData.instance.currentDeployEnv = CommonUtils.getKeyValue(envParams, "environment")
  String platform = CommonUtils.getDeployKeyValue(envParams,"platform", Default.DEPLOYMENT_PLATFORM).toString().trim().toUpperCase()
  BuildData.instance.currentPlatform = platform
  switch (platform) {
    case Platform.BLUEMIX:
      withEnv(["CF_HOME=${WORKSPACE}"]) {
        new Bluemix().deploy(envParams)
      }
      break
    case Platform.UCD:
      new UCD().deploy(envParams)
      break
    case Platform.CRX:
      new CRX().deploy(envParams)
    default:
      error Message.UNKNOWN_PLATFORM
      break
  }
}

/**
 * Orchestrates deployment and testing for all environments and platforms
 * @param deployments array of deployment and testing parameters
 * @return
 */
def runDeployments() {
  def deployments = BuildData.instance.deploy
  ArtifactoryUtils artifactoryUtils = new ArtifactoryUtils()
  BuildData.instance.attemptDeployment = true
  deployments.each { deployParams ->
    if (BuildData.instance.attemptDeployment) {
      String environment = CommonUtils.getKeyValue(deployParams, "environment", Default.DEPLOYMENT_ENVIRONMENT)
      def autoDeploy = CommonUtils.getKeyValue(deployParams, "autoDeploy", false)
      BuildData.instance.isSkipped = false
      if (!autoDeploy) {
        try {
          if (currentBuild.result == null) {
            currentBuild.result = currentBuild.currentResult
          }
          String approvers = CommonUtils.getKeyValue(deployParams, "approvers", BuildData.instance.emailRecipients)
          boolean skippable = CommonUtils.getKeyValue(deployParams, "skippable", false)
          processDeployApproval(environment, approvers, skippable)
        } catch (org.jenkinsci.plugins.workflow.steps.FlowInterruptedException e) {
          String abortUser = e.causes.get(0).getUser().toString()
          echo "ℹ️ [INFO] Deployment Aborted by " + abortUser
          artifactoryUtils.promoteBuild("Deploy to ${environment} - ABORTED", "Aborted by ${abortUser}")
          BuildData.instance.attemptDeployment = false
          return
        }
      }
      if (BuildData.instance.isSkipped) {
        artifactoryUtils.promoteBuild("Deploy to ${environment} - SKIPPED", "Skipped by ${BuildData.instance.deploymentApprover}")
      } else {
        processDeployment(environment, deployParams)
      }
    }
  }
}

/**
 * Processes deployment and tests for a given environment
 * @param environment Environment to be deployed on.
 * @param deployParams Deployment parameters
 * @return
 */
def processDeployment(String environment, def deployParams) {
  String platform = CommonUtils.getDeployKeyValue(deployParams,"platform", Default.DEPLOYMENT_PLATFORM).toString().trim().toUpperCase()
  String lockName = getLockName(environment)
  String label = new CommonUtils().getParamValue("agent", BuildData.instance.agent)
  ArtifactoryUtils artifactoryUtils = new ArtifactoryUtils()
  kpLock(lockName) {
    node(label) {
      try {
        deleteDir()
        echo "process deployment, deployable file and target " + BuildData.instance.deployableFile +  BuildData.instance.deployableTarget
        artifactoryUtils.downloadArtifact(BuildData.instance.deployableFile, BuildData.instance.deployableTarget)
        unzip dir: '', glob: '', zipFile: BuildData.instance.deployableFile
        env.APPLICATION_DIR = pwd()
        try {
          performDeployment(deployParams)
          if (BuildData.instance.deploymentApprover != null) {
            artifactoryUtils.promoteBuild("Deploy to ${environment} - SUCCESSFUL", "Successful deployment. Approved by ${BuildData.instance.deploymentApprover}")
          } else {
            artifactoryUtils.promoteBuild("Deploy to ${environment} - SUCCESSFUL", "Successful deployment.")
          }
        } catch (e) {
          if (BuildData.instance.deploymentApprover != null) {
            artifactoryUtils.promoteBuild("Deploy to ${environment} - FAILED", "Failed deployment. Approved by ${BuildData.instance.deploymentApprover}")
          } else {
            artifactoryUtils.promoteBuild("Deploy to ${environment} - FAILED", "Failed deployment")
          }
          error e.getMessage()
        }
      } finally {
        deleteDir()
      }
    }
  }
  new TestDeployment().testDeployment(deployParams)
}

/**
 * Process deployment approval
 * @param environment Environment for which approval is required
 * @param approvers Authorized approvers
 * @param skippable boolean value if environment is skippable
 * @return
 */
def processDeployApproval(String environment, String approvers, boolean skippable) {
  NotificationUtils notificationUtils = new NotificationUtils()
  stage(Stage.DEPLOY_APPROVAL + environment) {
    notificationUtils.sendMail("Deployment Authorization", approvers, "ready to deploy on ${environment}. \n\n Please provide your authorization here")
    if (approvers != null) {
      BuildData buildData = BuildData.instance
      if (skippable) {
        def inputData = input id: 'Deploy' + environment + env.BUILD_NUMBER, parameters: [choice(choices: "👍 Deploy\n👋 Skip\n", description: 'Deploy to/Skip this environment', name: 'skipChoice')], message: "🤔 Deploy to ${environment}?", ok: Message.INPUT_CROSSED_SUBMIT, submitter: approvers.replaceAll("\\s", ""), submitterParameter: 'deploymentApprover'
        BuildData newBuildData = BuildData.instance
        newBuildData.setProps(buildData.getProperties())
        if (inputData['skipChoice'].equals("👋 Skip")) {
          BuildData.instance.isSkipped = true
        }
        BuildData.instance.deploymentApprover = inputData['deploymentApprover']
      } else {
        def approver = input id: 'Deploy' + environment + env.BUILD_NUMBER, message: "🤔 Deploy to ${environment}?", ok: Message.INPUT_DEPLOY, submitter: approvers.replaceAll("\\s", ""), submitterParameter: 'deploymentApprover'
        BuildData newBuildData = BuildData.instance
        newBuildData.setProps(buildData.getProperties())
        BuildData.instance.deploymentApprover = approver
      }
    } else {
      error "No Approver provided for authorised deployment. Please provide approvers or set autoDeploy to true if no authorization is required."
    }
  }
}

/**
 * Processes rollback for failed deployments
 * @param deployParams deployment parameters for given environment
 * @return
 */
def processRollBack(def deployParams) {
  CommonUtils commonUtils = new CommonUtils()
  String platform = CommonUtils.getDeployKeyValue(deployParams,"platform", Default.DEPLOYMENT_PLATFORM).toString().trim().toUpperCase()
  String environment = CommonUtils.getDeployKeyValue(deployParams, "environment", Default.DEPLOYMENT_ENVIRONMENT).toString().trim()
  String lockName = getLockName(environment)
  String label = BuildData.instance.agent

  ArtifactoryUtils artifactoryUtils = new ArtifactoryUtils()
  kpLock(lockName) {
    node(label) {
      try {
        deleteDir()
        def stableMetaData = commonUtils.getStableMetadata(environment)
        if(stableMetaData == null) {
          error "🚨 [ERROR] No known stable version to roll-back to for environment ${environment}. Please deploy and mark at least 1 version as stable by verifying tests."
        }
        def envProps = stableMetaData.get(0)._source
        boolean download = CommonUtils.getKeyValue(envProps, "download", true)
        String stableFileName = CommonUtils.getKeyValue(envProps, "fileName").toString().trim()
        String rollBackFile = BuildData.instance.deployableFile.replace(".zip", "")
        if(download) {
          String stableArtifactPath = CommonUtils.getKeyValue(envProps, "path").toString().trim()
          artifactoryUtils.downloadArtifact(stableFileName, stableArtifactPath)
          BuildData.instance.deployableFile = stableFileName
          unzip dir: '', glob: '', zipFile: BuildData.instance.deployableFile
        }
        env.APPLICATION_DIR = pwd()
        try {
          deployParams.put('isRollback', true)
          deployParams.put('rollbackProps', envProps)
          performDeployment(deployParams)
          if (BuildData.instance.deploymentApprover != null) {
            artifactoryUtils.promoteBuild("Rollback on ${environment} - SUCCESSFUL", "Successful rollback to ${rollBackFile}. Approved by ${BuildData.instance.deploymentApprover}")
          } else {
            artifactoryUtils.promoteBuild("Rollback on ${environment} - SUCCESSFUL", "Successful rollback to ${rollBackFile}.")
          }
        } catch (e) {
          if (BuildData.instance.deploymentApprover != null) {
            artifactoryUtils.promoteBuild("Rollback on ${environment} - FAILED", "Failed rollback to ${rollBackFile}. Approved by ${BuildData.instance.deploymentApprover}")
          } else {
            artifactoryUtils.promoteBuild("Rollback on ${environment} - FAILED", "Failed rollback to ${rollBackFile}.")
          }
          error e.getMessage()
        }
      } finally {
        deleteDir()
      }
    }
  }
}

/**
 * Sets file pattern for deployable artifacts for each platform and each environment
 * @return
 */
def setDeployFilePatterns() {
  def deployments = BuildData.instance.deploy
  if (deployments != null && BuildData.instance.deployableArtifactsPattern == null) {
    deployments.each { deployParams ->
      String pattern = ""
      String platform = CommonUtils.getKeyValue(deployParams, "platform", Default.DEPLOYMENT_PLATFORM).toString().trim().toUpperCase()
      switch (platform) {
        case Platform.BLUEMIX:
          pattern = new Bluemix().getDeployFilePatterns(deployParams)
          break
        case Platform.UCD:
          pattern = new UCD().getDeployFilePatterns(deployParams)
          break
        case Platform.CRX:
          pattern = new CRX().getDeployFilePatterns(deployParams)
        default:
          error Message.UNKNOWN_PLATFORM
          break
      }
      if (BuildData.instance.deployableArtifactsPattern == null) {
        BuildData.instance.deployableArtifactsPattern = ""
      }
      if (!(BuildData.instance.deployableArtifactsPattern.contains(pattern))) {
        BuildData.instance.deployableArtifactsPattern = BuildData.instance.deployableArtifactsPattern.concat("${pattern},")
      }
    }
    if (BuildData.instance.deployableArtifactsPattern.length() > 2) {
      BuildData.instance.deployableArtifactsPattern = BuildData.instance.deployableArtifactsPattern.substring(0, BuildData.instance.deployableArtifactsPattern.length() - 1)
    }
  } else {
    CommonUtils commonUtils = new CommonUtils()
    def pushFileParams = commonUtils.getParamValue("pushFile")
    String pattern = new UCD().getFilePattern(pushFileParams)
    if (pattern.length() > 2) {
      BuildData.instance.deployableArtifactsPattern = pattern
    }
  }
}

/**
 * Returns lockable resource name that can be used for deployment
 * @param environment environment name for which the lock is required
 * @param platform platform name to determine special use case
 * @return lock name
 */
def getLockName(String environment) {
  ApplicationUtils applicationUtils = new ApplicationUtils()
  String lockName = applicationUtils.getProjectKey() + "_" + applicationUtils.getUnifiedAppName() + "_" + environment
  return lockName
}