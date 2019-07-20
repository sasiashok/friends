package org.kp.security

import org.kp.analytics.ElasticsearchUtils
import org.kp.utils.*
import org.kp.constants.*

/**
 * Performs Nexus policy evaluation
 * @return
 */
def evaluate() {
  def disableNexusIQ = new CommonUtils().getParamValue("disableNexusIQ")
  if(!disableNexusIQ) {
    performEvaluation()
  }
}

/**
 * Performs Nexus policy evaluation
 * @return
 */
def performEvaluation() {
  ApplicationUtils applicationUtils = new ApplicationUtils()
  def nexusIQObj = new CommonUtils().getParamValue("nexusIQ")
  
  if(nexusIQObj!= null) {
    String appId = CommonUtils.getKeyValue(nexusIQObj, "appId", applicationUtils.getApplicationName())
    String lockName = applicationUtils.getProjectKey()+"_"+applicationUtils.getUnifiedAppName()+"_NexusIQ"
    kpLock(lockName) {
      stage (Stage.NEXUS_IQ) {
        def policyEvaluationResult = null
        def branchName = applicationUtils.getCleanBranchName()
        String whiteHatFile = isUnix() ? "whitehat_${branchName}.tar.gz" : "whitehat_${branchName}.zip"
        String errorMessage = "Nexus Policy Evaluation failed."
        try {
          dir(env.APPLICATION_DIR) {
            if (BuildData.instance.appType.equals(AppType.NODEJS) || BuildData.instance.appType.equals(AppType.ANGULAR)) {
              dir("node_modules") {
                deleteDir()
              }
              kpNpm {
                kpSh "npm install --production"
              }
            }
            processWhiteHat(whiteHatFile)
            policyEvaluationResult = nexusPolicyEvaluation failBuildOnNetworkError: false,
              iqApplication: appId,
              iqScanPatterns: [[scanPattern: '**/*.jar'], [scanPattern: '**/*.war'], [scanPattern: '**/*.ear'], [scanPattern: '**/*.zip'], [scanPattern: '**/*.tar.gz'], [scanPattern: '**/*.js'], [scanPattern: '**/*.nupkg']],
              iqStage: 'build', 
              jobCredentialsId: Constant.NEXUS_IQ_CREDENTIALS
          }
        } catch (e) {
          if (policyEvaluationResult != null && policyEvaluationResult.applicationCompositionReportUrl != null) {
            error "${errorMessage} Please find the evaluation report at ${policyEvaluationResult.applicationCompositionReportUrl}"
          } else {
            error "Nexus Policy Evaluation failed: " + e.getMessage()
          }
        } finally {
          analyzeResults(policyEvaluationResult)
          archiveNexusReport(appId)
          cleanWhiteHat(whiteHatFile)
        }
      }
    }
  } else {
    echo "ℹ️ [INFO] Nexus IQ Data not provided."
  }
}

/**
 * Creates tarball of current workspace and pushes it to artifactory for white hat team to scan
 * @param whiteHatFile file name of tarball
 * @return
 */
def processWhiteHat(String whiteHatFile) {
  if(isUnix()) {
    touch whiteHatFile
    sh "tar -czf ${whiteHatFile} --exclude='*.tar.gz' --exclude='.git' ."
  } else {
    powershell "Compress-Archive . ${whiteHatFile}"
  }
  String applicationName = new ApplicationUtils().getApplicationName()
  new ArtifactoryUtils().uploadArtifact(whiteHatFile, "ext-snapshot-local/whitehat/${applicationName}/")
}

/**
 * Deletes the tarball from local workspace as clean up action
 * @param whiteHatFile file name of tarball to be deleted
 * @return
 */
def cleanWhiteHat(String whiteHatFile) {
  dir(env.APPLICATION_DIR) {
    def whiteHatFileExists = fileExists whiteHatFile
    if(whiteHatFileExists) {
      if(isUnix()) {
        sh "rm ${whiteHatFile}"
      } else {
        bat "del /f ${whiteHatFile}"
      }
    }
  }
}

/**
 * Analyze nexus evaluation results and post on elasticsearch
 * @param policyEvaluationResult evaluation results
 * @return
 */
def analyzeResults(policyEvaluationResult) {
  if(policyEvaluationResult != null) {
    def nexusMap = [:]
    def policyAlertsMap = [:]
    def policyAlerts = policyEvaluationResult.policyAlerts
    policyAlerts.each { policyAlert ->
      if(policyAlert != null) {
        def trigger = policyAlert.trigger
        if(trigger != null) {
          def triggerMap = [:]
          triggerMap.put("policyId", trigger.policyId)
          triggerMap.put("threatLevel", trigger.threatLevel)
          policyAlertsMap.put(trigger.policyName, triggerMap)
        }
      }
    }
    nexusMap.put("policyAlerts", policyAlertsMap)
    nexusMap.put("applicationCompositionReportUrl", policyEvaluationResult.applicationCompositionReportUrl)
    nexusMap.put("affectedComponentCount", policyEvaluationResult.affectedComponentCount)
    nexusMap.put("criticalComponentCount", policyEvaluationResult.criticalComponentCount)
    nexusMap.put("severeComponentCount", policyEvaluationResult.severeComponentCount)
    nexusMap.put("moderateComponentCount", policyEvaluationResult.moderateComponentCount)
    nexusMap.put("affectedComponentCount", policyEvaluationResult.affectedComponentCount)
    new ElasticsearchUtils().postGenericData(nexusMap, "nexus")
  }
}

/**
 * Retrieves PDF report from Nexus IQ server and archives it
 * @param appId appId whose report needs to be retrieved
 * @return
 */
def archiveNexusReport(String appId) {
  String credentials = Constant.NEXUS_IQ_CREDENTIALS
  try {
    String nexusServer = Constant.NEXUS_IQ_SERVER
    def appMetaData = httpRequest quiet: true, authentication: credentials, url: "${nexusServer}/api/v2/applications?publicId=${appId}"
    def appMetaDataContent = readJSON text: appMetaData.content
    String appInternalId = appMetaDataContent.applications[0].id
    def reports = httpRequest quiet: true, authentication: credentials, url: "${nexusServer}/api/v2/reports/applications/${appInternalId}"
    def reportsContent = readJSON text: reports.content
    String reportPdfUrl = reportsContent[0].reportPdfUrl
    httpRequest quiet: true, authentication: credentials, outputFile: 'Nexus-IQ-Report.pdf', url: "${nexusServer}/${reportPdfUrl}"
    archiveArtifacts allowEmptyArchive: true, artifacts: 'Nexus-IQ-Report.pdf'
  } catch (e) {
    echo "ℹ️ [INFO] Nexus IQ Report could not be archived."
  }
}
