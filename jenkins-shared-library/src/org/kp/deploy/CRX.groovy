package org.kp.deploy

import org.kp.analytics.ElasticsearchUtils
import org.kp.constants.*
import org.kp.utils.*

/**
 * Orchestrates deployment to CRX platform
 * @param envParams Environment parameters
 * @return
 */
def deploy(envParams) {
  String deployEnv = CommonUtils.getKeyValue(envParams, "environment", Default.DEPLOYMENT_ENVIRONMENT)
  boolean isRollback = CommonUtils.getDeployKeyValue(envParams, "isRollback", false)
  String stageName = Stage.DEPLOY + deployEnv
  if(isRollback) {
    stageName = Stage.ROLLBACK + deployEnv
  }
  try {
    stage(stageName) {
      String baseUrls = CommonUtils.getDeployKeyValue(envParams, "baseUrls", Default.CRX_BASEURL)
      String behavior = CommonUtils.getDeployKeyValue(envParams, "behavior", Default.CRX_BEHAVIOR)
      String acHandling = CommonUtils.getDeployKeyValue(envParams, "acHandling", Default.CRX_ACHANDLING)
      boolean install = CommonUtils.getDeployKeyValue(envParams, "install", Default.CRX_INSTALL)
      def packageIdFilters = CommonUtils.getDeployKeyValue(envParams, "packageIdFilters", Default.CRX_PACKAGEIDFILTERS)
      String credentials = CommonUtils.getDeployKeyValue(envParams, "credentials")
      boolean disableForJobTesting = !install

      dir(env.APPLICATION_DIR) {
        packageIdFilters.each { packageId ->
          crxDeploy acHandling: acHandling,
            baseUrls: baseUrls,
            behavior: behavior,
            credentialsId: credentials,
            disableForJobTesting: disableForJobTesting,
            packageIdFilters: packageId
        }
      }
    }
    new ElasticsearchUtils().postGenericData([deployEnv: deployEnv, rollback: isRollback, platform: BuildData.instance.currentPlatform], "deploy")
  } catch (e) {
    error "CRX Deployment failed on environment ${deployEnv}\n" + e.getMessage()
  }
}

/**
 * Gets stable meta-data map
 * @return stable meta-data map
 */
def getStableMetaData() {
  def releaseJsonMap = [:]
  releaseJsonMap.put("download", true)
  return releaseJsonMap
}


/**
 * Gets file pattern for deployable artifacts
 * @param envParams Environment parameters
 * @return deployable artifact file pattern
 */
def getDeployFilePatterns(envParams) {
  def packageIdFilters = CommonUtils.getDeployKeyValue(envParams, "packageIdFilters", Default.CRX_PACKAGEIDFILTERS)
  String filePattern = packageIdFilters.toString().replace("[","").replace("]","").trim()
  return filePattern
}