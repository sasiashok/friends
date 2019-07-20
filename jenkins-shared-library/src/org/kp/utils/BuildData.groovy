package org.kp.utils

@Singleton
class BuildData {

  // Common BuildData
  def initialEnv
  String emailRecipients
  String agent
  boolean exceptionCaught

  // SCM BuildData
  def branchParams
  def repoParams
  def projectParams
  def buildRepos

  // Build BuildData
  def buildInfo
  def buildTool
  String appType
  String appVersion
  String appGroupId
  String appArtifactId
  String targetRepo
  def appMarkerFile
  String unifiedAppName

  // Deploy BuildData

  def branchDeployParams
  def branchCommonDeployParams
  def repoDeployParams
  def repoCommonDeployParams
  def projectDeployParams
  def projectCommonDeployParams

  def deploy
  String deployableArtifactsPattern
  String deployableTarget
  def deployableFile
  String currentPlatform
  String currentDeployEnv
  boolean isAdhocDeployment
  String envProfile
  boolean attemptDeployment
  boolean isSkipped
  String deploymentApprover
  def stableReleaseData
  def selectedVersion

  // UCD Deploy Stable MetaData
  String UCD_siteName
  String UCD_application
  String UCD_process
  def UCD_regions
  String UCD_version
  boolean UCD_deployOnlyChanged

  // UCD Deploy BuildData
  def UCD_deployConfigData
  boolean UCD_pushFile
  String UCD_pushVersion
  String UCD_componentName

  def setProps (def props) {
    def propList = props.keySet() as List
    propList.each { prop ->
      if(prop != 'class') {
        this[prop] = props.get(prop)
      }
    }
  }
}