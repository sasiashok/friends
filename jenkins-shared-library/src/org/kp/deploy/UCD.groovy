package org.kp.deploy

import groovy.json.JsonOutput
import org.kp.analytics.ElasticsearchUtils
import org.kp.constants.*
import org.kp.utils.*

/**
 * Orchestrates deployment to ucd platform
 * @param envParams Environment parameters
 * @return
 */
def deploy(envParams) {
  ApplicationUtils applicationUtils = new ApplicationUtils()
  boolean isRollback = CommonUtils.getDeployKeyValue(envParams, "isRollback", false)
  String environment = CommonUtils.getDeployKeyValue(envParams, "environment", Default.DEPLOYMENT_ENVIRONMENT).toString().trim()
  if (isRollback) {
    def rollbackProps = CommonUtils.getDeployKeyValue(envParams, "rollbackProps")
    performRollback(rollbackProps, environment)
  } else {
    try {
      String application = CommonUtils.getDeployKeyValue(envParams, "application", applicationUtils.getApplicationName())
      if(BuildData.instance.UCD_deployConfigData == null) {
        processDeployConfig(CommonUtils.getDeployKeyValue(envParams, "deployConfig"))
      }
      String tagLabel = getTagLabel(envParams)

      def pushFileParams = CommonUtils.getDeployKeyValue(envParams, "pushFile")
      String componentName = CommonUtils.getDeployKeyValue(envParams, "componentName", application).trim()
      if(BuildData.instance.UCD_pushFile) {
        componentName = BuildData.instance.UCD_componentName
      }
      if (pushFileParams != null && !(BuildData.instance.UCD_pushFile)) {
        application = CommonUtils.getDeployKeyValue(envParams, "application", CommonUtils.getKeyValue(pushFileParams, "componentName", applicationUtils.getApplicationName()).trim()).trim()
        componentName = CommonUtils.getKeyValue(pushFileParams, "componentName", CommonUtils.getDeployKeyValue(envParams, "componentName", application).trim()).trim()
        String pushVersion = CommonUtils.getKeyValue(pushFileParams, "version", tagLabel).trim()
        pushFileParams.put("version", pushVersion)
        pushFileParams.put("componentName", componentName)
        performPushFile(pushFileParams)
        BuildData.instance.UCD_pushFile = true
        BuildData.instance.UCD_pushVersion = pushVersion
      }
      envParams.put("componentName", componentName)

      String version = getVersion(envParams, componentName, tagLabel, application, BuildData.instance.UCD_pushVersion)
      envParams.put("version", version)

      processDeployment(envParams, environment)
    } catch (e) {
      error "UCD Deployment failed\n" + e.getMessage()
    }
  }
}

/**
 * Returns tag label. Tag label is used to set the version of the deployment during push
 * @param envParams Environment parameters
 * @return
 */
String getTagLabel(def envParams) {
  CommonUtils commonUtils = new CommonUtils()
  String tagLabel = env.APPLICATION_TAG
  if (tagLabel == null) {
    if (BuildData.instance.appType.equals(AppType.APIC)) {
      tagLabel = commonUtils.getTagLabel(CommonUtils.getDeployKeyValue(envParams, "tagInfix"))
    } else {
      tagLabel = CommonUtils.getDeployKeyValue(envParams, "tagLabel", commonUtils.getTagLabel(CommonUtils.getDeployKeyValue(envParams, "tagInfix")))
    }
  }
  return tagLabel
}

/**
 * Get version string to be set during deployment operation
 * @param envParams Environment parameters
 * @param componentName componentName to which the version will be attached
 * @param tagLabel tagLabel used during push operation. This will be used with componentName to generate the version string
 * @param application application name if component name was not provided
 * @param pushVersion specific version if tagLabel is to be ignored
 * @return version string for deployment
 */
String getVersion(def envParams, String componentName, String tagLabel, String application, String pushVersion) {
  String version = CommonUtils.getDeployKeyValue(envParams, "version", "${componentName}:${tagLabel}")
  if(pushVersion != null && pushVersion.length() > 0) {
    version = "${componentName}:${pushVersion}"
  }
  def deployConfigData = BuildData.instance.UCD_deployConfigData
  boolean multiVersion = deployConfigData ? true : false
  if (multiVersion) {
    String configComponent = CommonUtils.getKeyValue(deployConfigData, 'componentName', CommonUtils.getDeployKeyValue(envParams, "componentName", application).trim() + "_Config").trim()
    String configTag = CommonUtils.getKeyValue(deployConfigData, "configTag", tagLabel)
    version = "${configComponent}:${configTag}\n${version}"
  }
  return version
}

/**
 * Process deployment for national and regional
 * @param envParams Environment parameters
 * @param environment environment to which deployment is to happen
 * @return
 */
def processDeployment(def envParams, String environment) {
  def regions = CommonUtils.getDeployKeyValue(envParams, "regions")
  String process = CommonUtils.getDeployKeyValue(envParams, "process", Default.UCD_PROCESS_NATIONAL).toString().trim()
  BuildData.instance.UCD_process = process
  String stageName = Stage.DEPLOY + environment
  if (regions != null && regions.size() > 0) {
    BuildData.instance.UCD_regions = regions
    process = CommonUtils.getDeployKeyValue(envParams, "process", Default.UCD_PROCESS_REGIONAL).toString().trim()
    BuildData.instance.UCD_process = process
    regions.each { region ->
      stage("${stageName}_${region}") {
        performDeployment(envParams, region)
      }
    }
  } else {
    stage(stageName) {
      performDeployment(envParams)
    }
  }
}

/**
 * Processes push operation for deployment configuration repo
 * @param deployConfig deploy config repo parameters
 * @return
 */
def processDeployConfig(def deployConfig) {
  String componentName = ""
  String tagLabel = ""
  try {
    if (deployConfig != null) {
      dir('deployConfig') {
        String currentDir = pwd()
        def gitUtil = new GitUtils()
        stage(Stage.CHECKOUT_DEPLOY_CONFIG) {
          gitUtil.cloneSingleRepo(deployConfig)
        }
        def tag = CommonUtils.getKeyValue(deployConfig, "tag")
        if (tag != null) {
          tagLabel = gitUtil.tagBuild(tag, CommonUtils.getKeyValue(deployConfig, "url"), currentDir)
        }

        def pushFileConfig = CommonUtils.getKeyValue(deployConfig, "pushFile")
        if (pushFileConfig != null) {
          pushFileConfig.put('version', tagLabel)
          String fileType = CommonUtils.getKeyValue(pushFileConfig, "fileType", "Config")
          pushFileConfig.put('fileType', fileType)
          componentName = CommonUtils.getKeyValue(pushFileConfig, "componentName", new ApplicationUtils().getApplicationName() + "_Config")
          pushFileConfig.put('componentName', componentName)
          String baseDir = remapDirToParent(CommonUtils.getKeyValue(pushFileConfig, "baseDir", Default.UCD_BASEDIR), 'deployConfig').trim()
          if(BuildData.instance.appType.equals(AppType.ANGULAR)) {
            baseDir = remapDirToParent(CommonUtils.getKeyValue(pushFileConfig, "baseDir", Default.UCD_ANGULAR_BASEDIR), 'deployConfig').trim()
          }
          pushFileConfig.put('baseDir', baseDir)
          performPushFile(pushFileConfig)
        }
        deleteDir()
      }
    }
  } catch (e) {
    error "Failed to process deploy configuration: " + e.getMessage()
  }
  if (componentName.length() > 0 && tagLabel.length() > 0) {
    def deployConfigData = [:]
    deployConfigData.put("componentName", componentName)
    deployConfigData.put("configTag", tagLabel)
    BuildData.instance.UCD_deployConfigData = deployConfigData
  }
}

/**
 * Performs the actual push file operation
 * @param pushFileParams pushFile parameters
 * @return
 */
def performPushFile(def pushFileParams) {
  ApplicationUtils applicationUtils = new ApplicationUtils()
  String componentName = ""
  String version = ""
  String fileType = CommonUtils.getKeyValue(pushFileParams, "fileType", Default.UCD_FILETYPE)
  try {
    String stageName = Stage.UCD_FILE_PUSH.replace("fileType", fileType)
    stage(stageName) {
      String siteName = CommonUtils.getKeyValue(pushFileParams, "siteName", Default.UCD_SITENAME)
      componentName = CommonUtils.getKeyValue(pushFileParams, "componentName", applicationUtils.getApplicationName())
      version = CommonUtils.getKeyValue(pushFileParams, "version", new CommonUtils().getTagLabel())
      String baseDir = remapDirToParent(CommonUtils.getKeyValue(pushFileParams, "baseDir", Default.UCD_BASEDIR).toString())
      if(BuildData.instance.appType.equals(AppType.ANGULAR)) {
        baseDir = remapDirToParent(CommonUtils.getKeyValue(pushFileParams, "baseDir", Default.UCD_ANGULAR_BASEDIR).toString())
      }
      String include = CommonUtils.getKeyValue(pushFileParams, "include", Default.UCD_INCLUDE)
      String exclude = CommonUtils.getKeyValue(pushFileParams, "exclude", Default.UCD_EXCLUDE)
      String pushProperties = CommonUtils.getKeyValue(pushFileParams, "pushProperties", Default.UCD_PUSHPROPERTIES)
      String pushDescription = CommonUtils.getKeyValue(pushFileParams, "pushDescription", applicationUtils.getRepoUrl())
      def pushIncremental = CommonUtils.getKeyValue(pushFileParams, "pushIncremental", Default.UCD_PUSHINCREMENTAL)
      step([$class   : 'UCDeployPublisher',
            siteName : siteName.trim(),
            component: [
                    $class       : 'com.urbancode.jenkins.plugins.ucdeploy.VersionHelper$VersionBlock',
                    componentName: componentName.trim(),
                    delivery     : [
                            $class             : 'com.urbancode.jenkins.plugins.ucdeploy.DeliveryHelper$Push',
                            pushVersion        : version.trim(),
                            baseDir            : baseDir.trim(),
                            fileIncludePatterns: include.trim(),
                            fileExcludePatterns: exclude.trim(),
                            pushProperties     : pushProperties.trim(),
                            pushDescription    : pushDescription.trim(),
                            pushIncremental    : pushIncremental
                    ]
            ]
      ])
    }
  } catch (e) {
    error "Failed to push version ${version} of ${fileType} file to UCD for ${componentName}\n" + e.getMessage()
  }
}

/**
 * Performs actual deployment on UCD
 * @param envParams Environment parameters
 * @param region region to deploy to if regional deployment
 * @return
 */
def performDeployment(def envParams, String region = null) {
  String process = ""
  if (region != null) {
    process = CommonUtils.getDeployKeyValue(envParams, "process", Default.UCD_PROCESS_REGIONAL)
    process = process + region
  } else {
    process = CommonUtils.getDeployKeyValue(envParams, "process", Default.UCD_PROCESS_NATIONAL)
  }

  String application = ""
  String version = ""
  String environment = ""
  boolean isRollback = CommonUtils.getDeployKeyValue(envParams, "isRollback", false)
  try {
    String siteName = CommonUtils.getDeployKeyValue(envParams, "siteName", Default.UCD_SITENAME).toString().trim()
    application = CommonUtils.getDeployKeyValue(envParams, "application", new ApplicationUtils().getApplicationName()).trim()
    environment = CommonUtils.getKeyValue(envParams, "environment", Default.DEPLOYMENT_ENVIRONMENT)
    version = CommonUtils.getKeyValue(envParams, "version").toString().trim()
    boolean deployOnlyChanged = CommonUtils.getDeployKeyValue(envParams, "deployOnlyChanged", Default.UCD_DEPLOYONLYCHANGED)

    if (!isRollback) {
      BuildData.instance.UCD_siteName = siteName
      BuildData.instance.UCD_application = application
      BuildData.instance.UCD_version = version
      BuildData.instance.UCD_deployOnlyChanged = deployOnlyChanged
    }

    step([$class  : 'UCDeployPublisher',
          siteName: siteName,
          deploy  : [
                  $class           : 'com.urbancode.jenkins.plugins.ucdeploy.DeployHelper$DeployBlock',
                  deployApp        : application,
                  deployEnv        : environment,
                  deployProc       : process,
                  deployVersions   : version,
                  deployOnlyChanged: deployOnlyChanged
          ]
    ])

    def deployMap = [deployEnv: environment, rollback: isRollback, platform: BuildData.instance.currentPlatform]
    if (region != null) {
      deployMap.put("region", getStateCode(region))
      deployMap.put("original_region", region)
    }
    new ElasticsearchUtils().postGenericData(deployMap, "deploy")
  } catch (e) {
    error "Failed to deploy version ${version} on UCD for ${application} on environment ${environment}\n" + e.getMessage()
  }
}

/**
 * Processes file pattern for deployable artifacts
 * @param envParams Environment parameters
 * @return deployable artifact file pattern
 */
def getDeployFilePatterns(envParams) {
  def pushFileParams = CommonUtils.getDeployKeyValue(envParams, "pushFile", new CommonUtils().getParamValue("pushFile"))
  return getFilePattern(pushFileParams)
}

/**
 * Gets file pattern for deployable artifacts
 * @param envParams Environment parameters
 * @return deployable artifact file pattern
 */
def getFilePattern(def pushFileParams) {
  String filePattern = ""
  if (pushFileParams != null) {
    String baseDir = remapDirToParent(CommonUtils.getKeyValue(pushFileParams, "baseDir", Default.UCD_BASEDIR).toString())
    if(BuildData.instance.appType.equals(AppType.ANGULAR)) {
      baseDir = remapDirToParent(CommonUtils.getKeyValue(pushFileParams, "baseDir", Default.UCD_ANGULAR_BASEDIR).toString())
    }
    String include = CommonUtils.getKeyValue(pushFileParams, "include", Default.UCD_INCLUDE)
    if (baseDir.endsWith("/")) {
      filePattern = "${baseDir}${include}"
    } else {
      filePattern = "${baseDir}/${include}"
    }
  }
  return filePattern
}

/**
 * Gets stable meta-data map
 * @return stable meta-data map
 */
def getStableMetaData() {
  def releaseJsonMap = [:]
  releaseJsonMap.put("UCD_siteName", BuildData.instance.UCD_siteName)
  releaseJsonMap.put("UCD_application", BuildData.instance.UCD_application)
  releaseJsonMap.put("UCD_process", BuildData.instance.UCD_process)
  releaseJsonMap.put("UCD_version", BuildData.instance.UCD_version)
  releaseJsonMap.put("UCD_deployOnlyChanged", BuildData.instance.UCD_deployOnlyChanged)
  releaseJsonMap.put("download", false)
  if (BuildData.instance.UCD_regions != null) {
    def regions = JsonOutput.toJson(BuildData.instance.UCD_regions)
    releaseJsonMap.put("UCD_regions", regions)
  }
  return releaseJsonMap
}

/**
 * Performs rollback on given environment
 * @param rollbackProps rollback properties
 * @param environment environment to perform rollback on
 * @return
 */
def performRollback(def rollbackProps, String environment) {
  def envParams = [:]
  envParams.put("siteName", CommonUtils.getKeyValue(rollbackProps, "UCD_siteName", Default.UCD_SITENAME).toString().trim())
  envParams.put("application", CommonUtils.getKeyValue(rollbackProps, "UCD_application", new ApplicationUtils().getApplicationName()).toString().trim())
  envParams.put("version", CommonUtils.getKeyValue(rollbackProps, "UCD_version").toString().trim())
  envParams.put("deployOnlyChanged", CommonUtils.getKeyValue(rollbackProps, "UCD_deployOnlyChanged", Default.UCD_DEPLOYONLYCHANGED))
  envParams.put("environment", environment)
  envParams.put("isRollback", true)

  String process = CommonUtils.getKeyValue(rollbackProps, "UCD_process", Default.UCD_PROCESS_NATIONAL).toString().trim()
  def regions = rollbackProps.UCD_regions
  String stageName = Stage.ROLLBACK + environment
  if (regions != null && regions.size() > 0) {
    process = CommonUtils.getKeyValue(rollbackProps, "UCD_process", Default.UCD_PROCESS_REGIONAL).toString().trim()
    regions.each { region ->
      stage("${stageName}_${region}") {
        performDeployment(envParams, region)
      }
    }
  } else {
    stage(stageName) {
      envParams.put("process", process)
      performDeployment(envParams)
    }
  }
}

/**
 * Re-map's baseDir of UCD removing special characters
 * @param baseDir user-provided baseDir
 * @param parent parent path with respect to which the re-mapping is to be done.
 * @return full path of baseDir without special characters
 */
def remapDirToParent(String baseDir, String parent = env.APPLICATION_DIR) {
  baseDir = baseDir.trim()
  parent = parent.trim()
  String fullPath = parent
  if(baseDir != "." && baseDir != "./" && baseDir != "/" && baseDir != "/.") {
    if(baseDir.startsWith("/")) {
      fullPath = parent + baseDir
    } else if(baseDir.startsWith("./")) {
      baseDir = baseDir.replace(".", "")
      fullPath = parent + baseDir
    } else {
      fullPath = parent + "/" + baseDir
    }
  }
  return fullPath.trim()
}

/**
 * Returns 2-character state-code for regions. Used for analytics
 * @param region region code provided by developer
 * @return 2-character state code
 */
String getStateCode(String region) {
  String stateCode = region
  switch (region.toUpperCase()) {
    case "SCA":
    case "NCA":
      stateCode = "CA"
      break
    case "NW":
      stateCode = "WA"
      break
  }
  return stateCode
}

/**
 * Process ucd File push and deploy config before deployment.
 * @return
 */
def processPreDeployment() {
  CommonUtils commonUtils = new CommonUtils()
  def pushFileParams = commonUtils.getParamValue("pushFile")
  if(pushFileParams != null) {
    String version = CommonUtils.getKeyValue(pushFileParams,"version", getTagLabel(pushFileParams))
    pushFileParams.put("version", version)
    performPushFile(pushFileParams)
    BuildData.instance.UCD_pushFile = true
    BuildData.instance.UCD_pushVersion = version
    BuildData.instance.UCD_componentName = CommonUtils.getKeyValue(pushFileParams, "componentName", new ApplicationUtils().getApplicationName())
  }

  def deployConfig = commonUtils.getParamValue("deployConfig")
  if(deployConfig != null) {
    processDeployConfig(deployConfig)
  }
}