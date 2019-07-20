import org.kp.deploy.Deployer
import org.kp.test.TestDeployment
import org.kp.utils.*
import org.kp.constants.*

def call(Map params = [:]) {
  timestamps {
    BuildData.instance.isAdhocDeployment = true
    BuildData.instance.UCD_pushFile = true
    CommonUtils commonUtils = new CommonUtils()
    ArtifactoryUtils artifactoryUtils = new ArtifactoryUtils()
    try {
      commonUtils.resolveConfigurations(params, 'deploy')
      def envParams = [:]
      BuildData.instance.deploymentApprover = ""
      def selectedRelease = [:]
      String targetEnv = ""
      String source = ""
      String selectedVersion = ""
      def approvers

      stage(Stage.TARGET_SELECTION) {
        def deployParams = commonUtils.getDeployEnvs()

        targetEnv = input id: 'targetSelect', message: "Select target environment to deploy", ok: Message.INPUT_SUBMIT, parameters: [choice(choices: deployParams.join('\n'), description: 'Target Environment', name: 'targetEnv')]
        BuildData.instance.currentDeployEnv = targetEnv
        echo "Selected target environment: " + targetEnv
      }

      stage(Stage.SOURCE_SELECTION) {
        def sources = CommonUtils.getDeployKeyValue(null, "source", ['*'])
        if(!sources.contains(targetEnv)) {
          sources.add(targetEnv)
        }
        approvers = CommonUtils.getDeployKeyValue(null, "approvers", ['*'])
        source = input id: 'sourceSelect', message: "Select source environment to deploy from", ok: Message.INPUT_SUBMIT, submitter: approvers.replaceAll("\\s", ""), parameters: [choice(choices: sources, description: 'Source Environment', name: 'sourceEnv')]
        echo "Selected source environment: " + source
      }

      stage(Stage.VERSION_SELECTION) {
        String sourceBranch = CommonUtils.getDeployKeyValue(null, "sourceBranch")
        if (sourceBranch != null && sourceBranch.endsWith("_*")) {
          sourceBranch = sourceBranch.substring(0, sourceBranch.length() - 2)
        }
        def stableMetaData = commonUtils.getStableMetadata(source, sourceBranch)
        if(stableMetaData == null) {
          error "🚨 [ERROR] No known stable version for environment ${source}. Please deploy and mark at least 1 version as stable by verifying tests or select another source environment from the correct branch."
        }

        def stableReleases = []
        stableMetaData.each { stableMetaDataHit ->
          def releaseData = stableMetaDataHit._source
          stableReleases.add(CommonUtils.getKeyValue(releaseData, "fileName").toString().trim())
        }

        stableReleases = stableReleases.toUnique()

        def fileInput = input id: 'versionSelect', message: "Select version to deploy", ok: Message.INPUT_SUBMIT, submitter: approvers.replaceAll("\\s", ""), parameters: [choice(choices: stableReleases, description: 'Stable Version', name: 'version')], submitterParameter: 'deploymentApprover'
        selectedVersion = fileInput['version']
        BuildData.instance.selectedVersion = selectedVersion
        echo "Selected version to deploy: " + selectedVersion
        BuildData.instance.deploymentApprover = fileInput['deploymentApprover']

        for (int i = 0; i < stableMetaData.size(); i++) {
          def releaseData = stableMetaData.get(i)._source
          String fileName = CommonUtils.getKeyValue(releaseData, "fileName").toString().trim()
          if(selectedVersion.equalsIgnoreCase(fileName)) {
            selectedRelease = releaseData
            break
          }
        }
      }

      node(BuildData.instance.agent) {
        try {
          String stableArtifactPath = CommonUtils.getKeyValue(selectedRelease, "path").toString().trim()
          boolean download = CommonUtils.getKeyValue(selectedRelease, "download", true)
          BuildData.instance.appVersion = CommonUtils.getKeyValue(selectedRelease, "version", true)
          if (download) {
            artifactoryUtils.downloadArtifact(selectedVersion, stableArtifactPath)
            BuildData.instance.deployableFile = selectedVersion
            unzip dir: '', glob: '', zipFile: BuildData.instance.deployableFile
          }
          selectedRelease.remove("timestamp")

          env.APPLICATION_DIR = pwd()
          try {
            envParams.put("environment", targetEnv)
            BuildData.instance.attemptDeployment = true
            new Deployer().performDeployment(envParams)

            env.BUILD_NUMBER = selectedRelease.environment.BUILD_NUMBER
            env.BRANCH_NAME = selectedRelease.environment.BRANCH_NAME
            BuildData.instance.stableReleaseData = selectedRelease
            if (BuildData.instance.deploymentApprover != null) {
              artifactoryUtils.promoteBuild("Deploy to ${targetEnv} - SUCCESSFUL", "Successful deployment. Approved by ${BuildData.instance.deploymentApprover}")
            } else {
              artifactoryUtils.promoteBuild("Deploy to ${targetEnv} - SUCCESSFUL", "Successful deployment.")
            }

          } catch (e) {
            if (BuildData.instance.deploymentApprover != null) {
              artifactoryUtils.promoteBuild("Deploy to ${targetEnv} - FAILED", "Failed deployment. Approved by ${BuildData.instance.deploymentApprover}")
            } else {
              artifactoryUtils.promoteBuild("Deploy to ${targetEnv} - FAILED", "Failed deployment")
            }
            error e.getMessage()
          }
        } finally {
          if (isUnix()) {
            deleteDir()
          }
        }

      }

      new TestDeployment().testDeployment(envParams)

      // Begin blue green deployment if neccessary
      if (BuildData.instance.attemptDeployment){
        node(BuildData.instance.agent){
          try {
            boolean blueGreen = CommonUtils.getDeployKeyValue(envParams, "blueGreen", false)
            if(blueGreen) {
            stage(Stage.BLUE_GREEN_APPROVAL){
              String verifySwitchToGreen = input id: 'blueToGreen', message: "Switch to Green?", ok: Message.INPUT_YES, submitter: approvers.replaceAll("\\s", ""), submitterParameter: 'approver'
            }
            // Check if blue to green is neccessary
            String platform = CommonUtils.getDeployKeyValue(null, "platform", Platform.BLUEMIX).toString()
            if(platform.equalsIgnoreCase(Platform.BLUEMIX)) {
                envParams.put("deploymentType", "blue-to-green")
                //invokes blue to green from Deployer
                new Deployer().performDeployment(envParams)
              }
            }
          } catch (org.jenkinsci.plugins.workflow.steps.FlowInterruptedException e) {
            cause = e.causes.get(0)
            String abortUser = cause.getUser().toString()
            echo "ℹ️ [INFO] Not switching to green. Deployment was successful. " + abortUser
          } finally {
            if (isUnix()) {
              deleteDir()
            }
          }
        }
      }


      if (currentBuild.result == null) {
        currentBuild.result = currentBuild.currentResult
      }
    } catch (exception) {
      commonUtils.handleException(exception)
    } finally {
      commonUtils.quit()
    }
  }
}
