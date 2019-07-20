package org.kp.utils

import org.kp.analytics.ElasticsearchUtils
import org.kp.constants.*

/**
 * Sets up the node by cleaning up the workspace and capturing initial environment status when build begins.
 * @return
 */
def setupNode() {
  env.NODE_NAME = env.NODE_NAME
  env.NODE_LABELS = env.NODE_LABELS
  env.NODE_LABEL = BuildData.instance.agent
  env.CHANGE_ID = env.CHANGE_ID
  env.CHANGE_URL = env.CHANGE_URL
  env.CHANGE_TITLE = env.CHANGE_TITLE
  env.CHANGE_AUTHOR = env.CHANGE_AUTHOR
  env.CHANGE_AUTHOR_DISPLAY_NAME = env.CHANGE_AUTHOR_DISPLAY_NAME
  env.CHANGE_AUTHOR_EMAIL = env.CHANGE_AUTHOR_EMAIL
  env.CHANGE_TARGET = env.CHANGE_TARGET
  if(env.BRANCH_NAME == null) {
    env.BRANCH_NAME = scm.GIT_BRANCH
  }

  if(isUnix()) {
    env.PLATFORM_OS = "Unix"
  } else {
    env.PLATFORM_OS = "Windows"
  }

  def initialEnv = env.getEnvironment()
  deleteDir()
  BuildData.instance.initialEnv = initialEnv
}

/**
 * Sets up environment for given application type
 * @param appType Application type if known; defaults to BuildData.instance.appType if not passed
 * @return
 */
def setupAppEnvironment(String appType = null) {
  if(appType == null) {
    appType = BuildData.instance.appType
  }
  // Any additional tools or specific versions of tools required will be added to path.
  def tools = getParamValue("tools")
  if(tools != null) {
      tools.keySet().each { toolName ->
        switch (toolName.toString().toUpperCase()) {
        case "JDK":
          def jdkTool = getKeyValue(tools, toolName)
          if(jdkTool != null) {
            if(isUnix()) {
              env.JAVA_HOME = tool name: jdkTool.toString(), type: 'jdk'
              env.PATH="${env.JAVA_HOME}/bin:${env.PATH}"
            }
          }
          break
        case AppType.MAVEN:
          if(isUnix()) {
            env.M2_HOME = tool name: getKeyValue(tools, toolName, Constant.MAVENTOOL).toString(), type: 'maven'
            env.PATH="${env.M2_HOME}/bin:${env.PATH}"
          }
          break
        case AppType.GRADLE:
          if(isUnix()) {
            env.GRADLE_HOME = tool name: getKeyValue(tools, toolName, Constant.GRADLETOOL).toString(), type: 'gradle'
            env.PATH="${env.GRADLE_HOME}/bin:${env.PATH}"
          }
          break
        case AppType.NODEJS:
          setNodeJS(getKeyValue(tools, toolName, Constant.NODEJSTOOL).toString())
          break
      }
    }
  }

  // Set actual appType specific tools.
  // Values will not be overridden from explicit tool declaration as names are re-derived with respect to tools object.
  // If tools is not declared, below execution will not fail as getKeyValue is null safe for null obj
  switch (appType) {
    case AppType.AEM:
      setNodeJS((getKeyValue(tools, appType.toUpperCase(), Constant.NODEJSTOOL)))
    case AppType.MAVEN:
      if(isUnix()) {
        env.M2_HOME = tool name: getKeyValue(tools, AppType.MAVEN.toLowerCase(), Constant.MAVENTOOL).toString(), type: 'maven'
        env.PATH="${env.M2_HOME}/bin:${env.PATH}"
      }
      break
    case AppType.GRADLE:
      if(isUnix()) {
        env.GRADLE_HOME = tool name: getKeyValue(tools, AppType.GRADLE.toLowerCase(), Constant.GRADLETOOL).toString(), type: 'gradle'
        env.PATH="${env.GRADLE_HOME}/bin:${env.PATH}"
      }
      break
    case AppType.ANGULAR:
    case AppType.NODEJS:
      setNodeJS((getKeyValue(tools, AppType.NODEJS.toLowerCase(), Constant.NODEJSTOOL)).toString())
      break
    case AppType.DOTNET:
      env.JAVA_HOME="${tool Constant.JDK_WIN}"
      break
  }
}

/**
 * Sets NodeJS environment variable and adds it to PATH
 * @return
 */
def setNodeJS(String toolName = Constant.NODEJSTOOL) {
  env.NODEJS_HOME = tool name: toolName, type: 'nodejs'
  if(isUnix()) {
    env.PATH="${env.NODEJS_HOME}/bin:${env.PATH}"
  } else {
    env.PATH="${env.NODEJS_HOME}:${env.PATH}"
  }
}

/**
 * Sets job properties. Typically discarding to 15 builds
 * @param jobProperties additional job properites to be set.
 * @return
 */
def setJobProperties(def jobProperties = null) {
  if(jobProperties == null) {
    properties([buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '', numToKeepStr: '15'))])
  } else {
    properties([buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '', numToKeepStr: '15')), jobProperties])
  }
}

/**
 * Returns the tag label for given infix.
 * @param tagInfix Infix to be included in the label
 * @return tag label
 */
String getTagLabel(String tagInfix = null) {
  def dateStamp = new Date().format(Constant.DATEFORMAT)
  String tagLabel = "${dateStamp}_${BUILD_NUMBER}"
  if(tagInfix != null) {
    tagLabel = "${dateStamp}_${tagInfix}_${BUILD_NUMBER}"
  }
  return tagLabel
}

/**
 * Sets build result if null
 * @return
 */
def setResult() {
  if(currentBuild.result == null) {
    currentBuild.result = currentBuild.currentResult
  }
}

/**
 * Resolves pipeline configurations from various possible locations.
 * @param branchParams map of branch level configurations
 * @param config file to read, pipeline for ci, deploy for ad-hoc deploy pipelines
 * @return
 */
def resolveConfigurations(def branchParams, String config = 'pipeline') {
  BuildData.instance.agent = getKeyValue(branchParams, "agent", Default.AGENT)
  def projectParams = branchParams
  def projectCommonParams = branchParams
  def repoParams = branchParams
  def repoCommonParams = branchParams

  if(!config.equalsIgnoreCase('pipeline')) {
    repoParams = getKeyValue(branchParams, 'deployParams')
    repoCommonParams = getKeyValue(branchParams, 'common_deployParams')
  }

  try {
    ApplicationUtils applicationUtils = new ApplicationUtils()
    String applicationName = applicationUtils.getApplicationName()
    String projectKey = applicationUtils.getProjectKey()
    String pipelineConfigRepoUrl = applicationUtils.getRepoUrl().replace(applicationName, "pipeline-configurations")
    if (projectKey != null && projectKey.equalsIgnoreCase(applicationName)){
      String toReplace = applicationName
      String replacement = "pipeline-configurations.git"
      def start = applicationUtils.getRepoUrl().lastIndexOf(toReplace)
      def applicationUrl = applicationUtils.getRepoUrl().substring(0, start)

      pipelineConfigRepoUrl = applicationUrl << replacement
    }
    String credentials = applicationUtils.getCredentialsId()
    int status = 0
    withCredentials([usernamePassword(credentialsId: credentials, passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
      String encoded_username = java.net.URLEncoder.encode(GIT_USERNAME, "UTF-8")
      String encoded_password = java.net.URLEncoder.encode(GIT_PASSWORD, "UTF-8")

      node(Default.AGENT) {
        wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [[password: encoded_password]]]) {
          wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [[password: encoded_username]]]) {
            String encodedUrl = new GitUtils().getTagUrl(pipelineConfigRepoUrl, encoded_username, encoded_password)
            status = sh returnStatus: true, script: """
              set +x
              git ls-remote -h ${encodedUrl} >/dev/null 2>&1"""
          }
        }
      }
    }

    if(status == 0) {
      fileLoader.withGit(pipelineConfigRepoUrl, 'master', credentials, BuildData.instance.agent) {
        echo "ℹ️ [INFO] Project configurations repo found."
        def projectFileExists = fileExists "${config}.groovy"
        if (projectFileExists) {
          def projectFile = fileLoader.load("${config}.groovy")
          if(config.equalsIgnoreCase('pipeline')) {
            projectParams = projectFile.getParams()
            repoParams = projectParams
          } else {
            projectParams = projectFile.getDeployParams()
            repoParams = projectParams
            projectCommonParams = projectFile.getCommonDeployParams()
            repoCommonParams = projectCommonParams
          }
          echo "ℹ️ [INFO] Project configurations found and loaded."
        } else {
          echo "ℹ️ [INFO] Project configurations do not exist. Checking for repo configurations."
        }

        def repoFiles = findFiles(glob: applicationName+"/${config}.groovy")
        if(repoFiles.length > 0) {
          def repoFile = fileLoader.load(applicationName+"/${config}.groovy")
          if(config.equalsIgnoreCase('pipeline')) {
            repoParams = repoFile.getParams()
          } else {
            repoParams = repoFile.getDeployParams()
            repoCommonParams = repoFile.getCommonDeployParams()
          }
          echo "ℹ️ [INFO] " + applicationName + " configurations found and loaded."
        } else {
          echo "ℹ️ [INFO] " + applicationName + " configurations not found. Will use branch / project / default configurations."
        }
        deleteDir()
      }
    }

  } catch (e) {
    echo "ℹ️ [INFO] Project configurations repo not found or empty. Ignoring error and proceeding using branch / default configurations"
  }

  if(config.equalsIgnoreCase('pipeline')) {
    BuildData.instance.branchParams = branchParams
    BuildData.instance.repoParams = repoParams
    BuildData.instance.projectParams = projectParams
    BuildData.instance.emailRecipients = getParamValue("emailRecipients")
    BuildData.instance.agent = getParamValue("agent", Default.AGENT).toString().trim()
    boolean setProperties = getKeyValue(branchParams, "setProperties", true)
    if(setProperties) {
      setJobProperties()
    }
  } else {
    BuildData.instance.branchDeployParams = getKeyValue(branchParams, 'deployParams')
    BuildData.instance.branchCommonDeployParams = getKeyValue(branchParams, 'common_deployParams')
    BuildData.instance.repoDeployParams = repoParams
    BuildData.instance.repoCommonDeployParams = repoCommonParams
    BuildData.instance.projectDeployParams = projectParams
    BuildData.instance.projectCommonDeployParams = projectCommonParams
    BuildData.instance.emailRecipients = getDeployCommonParam("approvers",getDeployCommonParam("emailRecipients"))
    BuildData.instance.agent = getDeployCommonParam("agent",Default.AGENT)
    setJobProperties()
  }
}

/**
 * Get value of root level configuration
 * @param key key declared at root level
 * @param defaultValue default value to return if key is not defined
 * @return value of key
 */
def getParamValue(String key, def defaultValue = null) {
  def paramValue = getKeyValue(BuildData.instance.branchParams, key, getKeyValue(BuildData.instance.repoParams, key, getKeyValue(BuildData.instance.projectParams, key, defaultValue)))
  return paramValue
}

/**
 * Returns all deploy environment params
 * @return deploy params for all environments
 */
def getDeployEnvs() {
  def effectiveDeployParams = [:]
  if(BuildData.instance.projectDeployParams != null) {
    effectiveDeployParams.putAll(BuildData.instance.projectDeployParams)
  }
  if(BuildData.instance.repoDeployParams != null) {
    effectiveDeployParams.putAll(BuildData.instance.repoDeployParams)
  }
  if(BuildData.instance.branchDeployParams != null) {
    effectiveDeployParams.putAll(BuildData.instance.branchDeployParams)
  }

  def deployEnvsSet = effectiveDeployParams.keySet() as String[]
  return deployEnvsSet
}

/**
 * Get value of key for utils deploy parameter. This would not be attached to any specific deploy env but is utils across all deploy envs.
 * @param key key whose value is required
 * @param defaultValue default value to return if key is not defined
 * @return value of utils key
 */
def getDeployCommonParam(String key, def defaultValue = null) {
  def paramValue = getKeyValue(BuildData.instance.branchCommonDeployParams, key, getKeyValue(BuildData.instance.repoCommonDeployParams, key, getKeyValue(BuildData.instance.projectCommonDeployParams, key, defaultValue)))
  return paramValue
}

/**
 * Gets nested key's value from for deploy env param. Depending on ad-hoc flow or not, the behaviour of this function changes and so does it's use of parameters.
 * @param obj object from which key is to be extracted
 * @param parent parent from deploy env from which nested key is to be extracted
 * @param key key whose value is required
 * @param defaultValue default value to return if key is not defined
 * @return value of key
 */
def static getDeployNestedKeyValue(def obj, String parent, String key, def defaultValue = null) {
  String targetEnv = BuildData.instance.currentDeployEnv
  def paramValue
  if(BuildData.instance.isAdhocDeployment) {
    paramValue = getKeyValue(getKeyValue(getKeyValue(BuildData.instance.branchDeployParams, targetEnv), parent), key, getKeyValue(getKeyValue(BuildData.instance.branchCommonDeployParams, parent), key, getKeyValue(getKeyValue(getKeyValue(BuildData.instance.repoDeployParams, targetEnv), parent), key, getKeyValue(getKeyValue(BuildData.instance.repoCommonDeployParams, parent), key, getKeyValue(getKeyValue(getKeyValue(BuildData.instance.projectDeployParams, targetEnv), parent), key, getKeyValue(getKeyValue(BuildData.instance.projectCommonDeployParams, parent), key, defaultValue))))))
  } else {
    paramValue = getKeyValue(obj, key, defaultValue)
  }
  return paramValue
}

/**
 * Get's key's value from given obj for deployEnv. To be used when immediate key is required from object and no nesting is expected.
 * @param obj object from which key is to be extracted
 * @param key key whose value is required
 * @param defaultValue default value to return if key is not defined
 * @return value of key
 */
def static getDeployKeyValue(def obj, String key, def defaultValue = null) {
  def paramValue
  if(BuildData.instance.isAdhocDeployment) {
    String targetEnv = BuildData.instance.currentDeployEnv
    paramValue = getKeyValue(getKeyValue(BuildData.instance.branchDeployParams, targetEnv), key, getKeyValue(BuildData.instance.branchCommonDeployParams, key, getKeyValue(getKeyValue(BuildData.instance.repoDeployParams, targetEnv), key, getKeyValue(BuildData.instance.repoCommonDeployParams, key, getKeyValue(getKeyValue(BuildData.instance.projectDeployParams, targetEnv), key, getKeyValue(BuildData.instance.projectCommonDeployParams, key, defaultValue))))))
  } else {
    paramValue = getKeyValue(obj, key, defaultValue)
  }
  return paramValue
}

/**
 * Get's key's value from given obj.
 * @param obj object from which key is to be extracted
 * @param key key whose value is required
 * @param defaultValue default value to return if key is not defined
 * @return value of key
 */
def static getKeyValue(def obj, String key, def defaultValue = null) {
  if(obj != null) {
    def value = obj.containsKey(key) ? obj[key] : defaultValue
    return value
  } else {
    return defaultValue
  }
}

/**
 * Get build cause of current build.
 * Yet to be used.
 * @return build cause
 */
def getBuildCause() {
  def buildCauses = currentBuild.rawBuild.getCauses()
  buildCauses.each { buildCause ->
    String causeClass = buildCause.getClass().toString().tokenize('$').last().tokenize('.').last().trim()
    // TO BE USED LATER
    switch (causeClass) {
      case "UserIdCause":
        break
      case "RemoteCause":
        break
      case "DeeplyNestedUpstreamCause":
      case "UpstreamCause":
        break
      case "SCMTriggerCause":
        break
      case "TimerTriggerCause":
        break
      case "ReplayCause":
        break
    }
    return causeClass
  }
}

/**
 * Get stable artifact meta-data for given environment from elasticsearch
 * @param deployEnv deploy env whose stable artifact meta-data needs to be pulled
 * @param branch branch from which stable artifact should have been generated.
 * @return stable artifact meta-data
 */
def getStableMetadata(String deployEnv, String branch = null) {
  ApplicationUtils applicationUtils = new ApplicationUtils()
  String projectKey = applicationUtils.getProjectKey()
  String repo = applicationUtils.getApplicationName()
  String body = ""
  deployEnv = deployEnv.toLowerCase()
  if(deployEnv.equalsIgnoreCase("*")) {
    body = """{"sort": [{"timestamp": {"order": "desc"}}],"query": {"bool": {"must": [
              {"term":{"repository.keyword": {"value": "${repo}"}}},
              {"prefix": {"environment.BRANCH_NAME.keyword": {"value": "${branch}"}}}]}}}"""
    if(branch == null) {
      body = """{"sort": [{"timestamp": {"order": "desc"}}],"query": {"bool": {"must": [
              {"term":{"repository.keyword": {"value": "${repo}"}}}]}}}"""
    }
  } else {
    body = """{"sort": [{"timestamp": {"order": "desc"}}],"query": {"bool": {"must": [
              {"term":{"repository.keyword": {"value": "${repo}"}}},
              {"term": {"deployEnv.keyword": {"value": "${deployEnv}"}}},
              {"prefix": {"environment.BRANCH_NAME.keyword": {"value": "${branch}"}}}]}}}"""
    if(branch == null) {
      body = """{"sort": [{"timestamp": {"order": "desc"}}],"query": {"bool": {"must": [
              {"term":{"repository.keyword": {"value": "${repo}"}}},
              {"term": {"deployEnv.keyword": {"value": "${deployEnv}"}}}]}}}"""
    }
  }

  def response = httpRequest quiet: true, httpMode: 'POST', acceptType: 'APPLICATION_JSON', contentType: 'APPLICATION_JSON', requestBody: body, responseHandle: 'LEAVE_OPEN', url: "${Constant.ELASTICSEARCH_URL}/stable-release-${projectKey}/_search?size=10&filter_path=hits.hits._source"

  String stableMetaData = response.getContent()
  response.close()
  def metaData = readJSON text: stableMetaData
  return getKeyValue(getKeyValue(metaData, 'hits'),'hits')
}

/**
 * Generic method to send notification upon global exception
 * @param exception exception that occured.
 * @return
 */
def handleException (exception) {
  BuildData.instance.exceptionCaught = true
  currentBuild.result = "FAILURE"
  new NotificationUtils().sendMail("Error Notification", BuildData.instance.emailRecipients, "failed with error: " + exception.getMessage())
  error "Exception occured: " + exception.getMessage()
}

/**
 * Perform pipeline end tasks of sending final email and posting build data to elasticsearch.
 * @return
 */
def quit() {
  if(!BuildData.instance.exceptionCaught) {
    new NotificationUtils().sendMail("Build Complete", BuildData.instance.emailRecipients, "build complete")
  }
  if(!BuildData.instance.isAdhocDeployment) {
    new ElasticsearchUtils().postBuildData()
  }
}

/**
 * Add key value pairs for given map, default to true
 * @param map
 * @param key
 * @param value
 * @param elseValue
 * @return
 */
def static addToMap(def map, String key, String value, def elseValue = true) {
  value ? map.put(key, value) : elseValue
}
