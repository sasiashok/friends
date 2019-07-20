package org.kp.utils

import org.kp.constants.*
import org.kp.deploy.Deployer

/**
 * Upload artifact to artifactory
 * @param pattern File pattern to upload
 * @param target Repository on artifactory to upload to
 * @return
 */
def uploadArtifact(String pattern, String target) {
  def buildInfo = Artifactory.newBuildInfo()
  buildInfo.name = new ApplicationUtils().getUnifiedAppName()
  buildInfo.number = env.BUILD_NUMBER
  try {
    def server = getServer()
    def uploadSpec = defineSpec(pattern, target)
    server.upload spec: uploadSpec, buildInfo: buildInfo
  } catch (e) {
    error "Failed to upload artifact ${pattern} to artifactory at target ${target}. Error for reference: " + e.getMessage()
  }
  return buildInfo
}

/**
 * Download artifact from artifactory
 * @param pattern File name to download
 * @param target Repository on artifactory to download from
 * @return
 */
def downloadArtifact(String pattern, String target) {
  try {
    httpRequest outputFile: pattern, quiet: true, responseHandle: 'NONE', url: "${getServer().url}/${target}${pattern}"
  } catch (e) {
    error "Failed to download artifact ${pattern} from artifactory from source ${target}. Error for reference: " + e.getMessage()
  }
}

/**
 * Generates spec json for upload
 * @param pattern File pattern to upload
 * @param target Repository on artifactory to upload to
 * @return spec json
 */
def defineSpec(String pattern, String target) {
  String defaultProps = "build.name=${new ApplicationUtils().getUnifiedAppName()};build.number=${BUILD_NUMBER}"
  String spec = """{
    "files": [
      {
        "pattern": "${pattern}",
        "target": "${target}",
        "props": "${defaultProps}"
      }
    ]
  }"""
  return spec
}

/**
 * Upload nuget packages to artifactory
 * @return
 */
def uploadNugetPackage() {
  def files = findFiles(glob: '*.nupkg')
  String nugetPackage =files[0].name
  def uploadInfo = uploadArtifact(nugetPackage, Constant.ARTIFACTORY_NUGET_LOCAL)
  BuildData.instance.buildInfo = uploadInfo
}

/**
 * Upload npm package to artifactory
 * @return
 */
def uploadNpmPackage(String target){
  kpNpm{
    if(new ApplicationUtils().isSnapShot()) {
      kpSh "npm-snapshot ${env.BUILD_NUMBER}"
    }
    kpSh "npm publish --registry ${getServer().url}/api/npm/${target}/"
  }
}

/**
 * Push deployable artifacts to Artifactory
 * push is done using buildTool if it is not null and API upload if deployable artifacts are apply.
 * @return
 */
def deployArtifacts() {
  try {
    dir(env.APPLICATION_DIR) {
      new Deployer().setDeployFilePatterns()
      def buildTool = BuildData.instance.buildTool
      def buildInfo = BuildData.instance.buildInfo
      if (buildTool != null && buildInfo != null) {
        buildTool.deployer.deployArtifacts buildInfo
        publishBuildInfo()
      } else {
        boolean isLibrary = new CommonUtils().getParamValue("isLibrary", false)
        switch (BuildData.instance.appType) {
          case AppType.DOTNET:
            if(isLibrary) {
              uploadNugetPackage()
            }
            break
          case AppType.ANGULAR:
          case AppType.NODEJS:
            if(isLibrary){
              uploadNpmPackage(getArtifactoryTargetRepo(false))
            }
            break
          case AppType.PHP:
            // Nothing to do for PHP
            break
          default:
            error "buildInfo or buildTools is NULL"
            break
        }
      }

      if (BuildData.instance.deployableArtifactsPattern != null) {
        setTargetPath()
        if (BuildData.instance.appType.equals(AppType.DOTNET)) {
          zip dir: '', glob: BuildData.instance.deployableArtifactsPattern.replaceAll(env.APPLICATION_DIR.replace("\\","\\\\"), ""), zipFile: BuildData.instance.deployableFile
        } else {
          def npmrcExists = fileExists ".npmrc"
          if (npmrcExists){
            kpSh "rm .npmrc"
          }
          if (BuildData.instance.appType.equals(AppType.NODEJS) || BuildData.instance.appType.equals(AppType.ANGULAR)) {
            dir("node_modules") {
              deleteDir()
            }
          }
          zip dir: '', glob: BuildData.instance.deployableArtifactsPattern.replaceAll(env.APPLICATION_DIR + "/", ""), zipFile: BuildData.instance.deployableFile
        }
        def uploadInfo = uploadArtifact(BuildData.instance.deployableFile, BuildData.instance.deployableTarget)
        BuildData.instance.buildInfo = uploadInfo
        publishBuildInfo()
      }
    }
  } catch (e) {
    error "Failed to deploy artifact: " + e.getMessage()
  }
}

/**
 * Publish buildInfo on Artifactory and Jenkins build
 * @return
 */
def publishBuildInfo() {
  def buildInfo = BuildData.instance.buildInfo
  try {
    if (buildInfo != null) {
      def server = getServer()
      server.publishBuildInfo buildInfo
    } else {
      if (BuildData.instance.appType.equals(AppType.DOTNET) || BuildData.instance.appType.equals(AppType.ANGULAR) || BuildData.instance.appType.equals(AppType.NODEJS)) {
        echo "ℹ️ [INFO] Library application found. Nothing to publish."
      } else {
        error "Build Info is null"
      }
    }
  } catch (e) {
    echo "ℹ️ [INFO] Failed to publish build info: " + e.getMessage()
  }
}

/**
 * Return artifactory server instance
 * @return artifactory server
 */
def getServer() {
  String artifactoryServer = Constant.ARTIFACTORY_SERVER
  def server = Artifactory.server artifactoryServer.toString()
  return server
}

/**
 * Set deployableFile and deployableTarget for respective application.
 * This is used for API upload of deployable artifacts to artifactory.
 * @return
 */
def setTargetPath() {
  ApplicationUtils applicationUtils = new ApplicationUtils()
  def dateStamp = new Date().format(Constant.ARTIFACTORY_DATEFORMAT)
  String version = applicationUtils.getApplicationVersion()
  String trimmedVersion = applicationUtils.getTrimmedApplicationVersion()
  String targetRepo = getArtifactoryTargetRepo()
  switch(BuildData.instance.appType) {
    case AppType.DOTNET:
      String assemblyName = applicationUtils.getAssemblyName().replace(".", "/")
      BuildData.instance.deployableTarget = "${targetRepo}/${assemblyName}/${version}/"
      BuildData.instance.deployableFile = "${assemblyName}-${trimmedVersion}-${dateStamp}-${env.BUILD_NUMBER}.zip"
      break
    case AppType.ANGULAR:
    case AppType.NODEJS:
    case AppType.PHP:
      String appName = applicationUtils.getApplicationName().replace(".", "/")
      BuildData.instance.deployableTarget = "${targetRepo}/${appName}/${version}/"
      BuildData.instance.deployableFile = "${appName}-${trimmedVersion}-${dateStamp}-${env.BUILD_NUMBER}.zip"
      break
    default:
      String groupId = applicationUtils.getApplicationGroupId().replace(".", "/")
      String artifactId = applicationUtils.getApplicationArtifactId().replace(".", "/")
      BuildData.instance.deployableTarget = "${targetRepo}/${groupId}/${artifactId}/${version}/"
      BuildData.instance.deployableFile = "${artifactId}-${trimmedVersion}-${dateStamp}-${env.BUILD_NUMBER}.zip"
      break
  }
}

/**
 * Promotes a build on artifactory with appropriate status and comment.
 * @param status Status message for promotion
 * @param comment Comment to be included against build in artifactory
 * @return
 */
def promoteBuild(status, comment) {
  try {
    def dryRunResponse = performPromotion(getPromoteJson(status, comment, true))
    if (dryRunResponse != null && dryRunResponse.getStatus().equals(200)) {
      def promoteResponse = performPromotion(getPromoteJson(status, comment))
      if (promoteResponse != null && promoteResponse.getStatus().equals(200)) {
        echo "ℹ️ [INFO] Labelling build with status '${status}' successful in Artifactory"
      } else {
        error "Promotion in active mode failed for '${status}'"
      }
    } else {
      error "Promotion in dry run mode failed for '${status}'"
    }
  } catch (e) {
    echo "ℹ️ [INFO] Failed to label the build in Artifactory: " + e.getMessage()
    echo "ℹ️ [INFO] Ignoring labelling error and proceeding."
  }
}

/**
 * Returns json used to perform build promotion
 * @param status Status message for promotion
 * @param comment Comment to be included against build in artifactory
 * @param dryRun Set dryRun flag for promotion.
 * @return promotion json
 */
def getPromoteJson(String status, String comment, boolean dryRun = false) {
  String targetRepo = getArtifactoryTargetRepo()
  String promoteJson = """
  {
    "status": "${status}",
    "comment": "${comment}",
    "targetRepo" : "${targetRepo}",
    "dryRun" : ${dryRun}
  }
  """
  return promoteJson
}

/**
 * Performs actual build promotion on artifactory by invoking the promotion API
 * @param body promotion json to be sent as body of API call.
 * @return
 */
def performPromotion(String body) {
  String buildName = new ApplicationUtils().getUnifiedAppName()
  def response = httpRequest authentication: 'artifactory-user', requestBody: body, contentType: 'APPLICATION_JSON', httpMode: 'POST', ignoreSslErrors: true, quiet: true, responseHandle: 'NONE', url: "${getServer().url}/api/build/promote/${buildName}/${BUILD_NUMBER}", validResponseCodes: '100:599'
  return response
}

/**
 * Return the repository on Artifactory to which the deployable artifact has to be pushed to.
 * @return repository name on artifactory where deployable artifact will be stored.
 */
def getArtifactoryTargetRepo(boolean deployable = true) {
  ApplicationUtils applicationUtils = new ApplicationUtils()
  String targetRepo = BuildData.instance.targetRepo
  if (targetRepo == null) {
    targetRepo = (applicationUtils.isSnapShot()) ? Constant.ARTIFACTORY_DEPLOYABLE_SNAPSHOT : Constant.ARTIFACTORY_DEPLOYABLE_RELEASE
    BuildData.instance.targetRepo = targetRepo
  }
  if(!(applicationUtils.isValidProject())) {
    targetRepo = Constant.ARTIFACTORY_DEPLOYABLE_TEST
    BuildData.instance.targetRepo = targetRepo
  }
  if(!deployable && (BuildData.instance.appType.equals(AppType.ANGULAR) || BuildData.instance.appType.equals(AppType.NODEJS))) {
    targetRepo = applicationUtils.isSnapShot() ? Constant.ARTIFACTORY_NPM_LOCAL_SNAPSHOT : Constant.ARTIFACTORY_NPM_LOCAL_RELEASE
  }
  return targetRepo
}