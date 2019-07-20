package org.kp.deploy

import org.kp.analytics.ElasticsearchUtils
import org.kp.utils.*
import org.kp.constants.*
import groovy.json.JsonOutput

/**
 * Orchestrates deployment to bluemix platform
 * @param envParams Environment parameters
 * @return
 */
def deploy(envParams) {
  String deployEnv = CommonUtils.getKeyValue(envParams, "environment", Default.DEPLOYMENT_ENVIRONMENT)
  boolean isRollback = CommonUtils.getDeployKeyValue(envParams, "isRollback", false)
  String deploymentType = CommonUtils.getKeyValue(envParams, "deploymentType", "normal").toString()

  String stageName
  echo "selected version is: " + BuildData.instance.selectedVersion
  if (BuildData.instance.selectedVersion?.trim()){
    stageName = Stage.DEPLOY + deployEnv + " [Version: " + BuildData.instance.selectedVersion +"]"
  } else {
    stageName = Stage.DEPLOY + deployEnv
  }
  if(isRollback) {
    stageName = Stage.ROLLBACK + deployEnv
  }
  if(deploymentType.equalsIgnoreCase("blue-to-green")) {
    stageName = Stage.BLUE_GREEN + deployEnv
  }
  try {
    stage(stageName) {
      echo "deployment Type " + deploymentType
      processLogin(envParams)
      if(deploymentType.equalsIgnoreCase("blue-to-green")) {
        processBlueGreen(envParams)
      } else {
        processPush(envParams)
      }
      processLogout()
      new ElasticsearchUtils().postGenericData([deployEnv: deployEnv, rollback: isRollback, platform: BuildData.instance.currentPlatform], "deploy")
    }
  } catch (e) {
    error "Bluemix Deployment failed on environment ${deployEnv}\n" + e.getMessage()
  }
}

/**
 * Performs login
 * @param envParams Environment parameters
 * @return
 */
def processLogin(def envParams) {
  String apiUrl = CommonUtils.getDeployKeyValue(envParams, "apiUrl", Default.BLUEMIX_API)
  String space = CommonUtils.getDeployKeyValue(envParams, "space")
  String credentials = CommonUtils.getDeployKeyValue(envParams, "credentials")
  String org = CommonUtils.getDeployKeyValue(envParams, "org", Default.BLUEMIX_ORG)

  withCredentials([usernamePassword(credentialsId: credentials, passwordVariable: 'password', usernameVariable: 'username')]) {
    wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [[password: username]]]) {
      wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [[password: password]]]) {
        sh "cf login -a ${apiUrl} -o ${org} -s '${space}' -u '${username}' -p '${password}'"
      }
    }
  }
}

/**
 * Performs cf push operation
 * @param envParams Environment parameters
 * @return
 */
def processPush(def envParams) {
  def applicationDir = env.APPLICATION_DIR.toString().trim()
  String appName = CommonUtils.getDeployKeyValue(envParams, "appName")
  String path = CommonUtils.getDeployKeyValue(envParams, "path", BuildData.instance.appType.equals(AppType.ANGULAR) ? Default.BLUEMIX_ANGULAR_PATH : Default.BLUEMIX_PATH )
  def envs = CommonUtils.getDeployKeyValue(envParams, "envs")
  def services = CommonUtils.getDeployKeyValue(envParams, "services")
  boolean ignorePath = CommonUtils.getDeployKeyValue(envParams, "ignorePath")
  String options = getOptions(applicationDir, envParams, appName, envs, services)

  processConfigRefresh(envParams)

  if(options.equals("")) {
    try {
      if (ignorePath) {
        sh "cf push -f ${applicationDir}/manifest.yml"
      }
      else {
        sh "cf push -f ${applicationDir}/manifest.yml -p ${applicationDir}/${path}"
      }
    } catch (e) {
      error "Failed to perform cf push: " + e.getMessage()
    }
  } else {
    processWithOptions(envParams, applicationDir, options, appName, envs, services, path)
  }
}

/**
 * Get additional options for cf push
 * @param hostName hostname to be set
 * @param domain domain to be set
 * @param appName appName that is going to be affected
 * @param envs env properties that affect application start behavior
 * @param services services that affect application start behavior
 * @return push options
 */
def getOptions (String applicationDir, def envParams, String appName, def envs, def services) {
  String hostName = getDefaultRouteKey(envParams, "hostName")
  String domain = getDefaultRouteKey(envParams, "domain")
  def varFiles = CommonUtils.getDeployKeyValue(envParams, "varFiles")
  def scale = CommonUtils.getDeployKeyValue(envParams, "scale")
  String options = ""
  if(hostName != null) {
    options = options.concat(" --hostname ${hostName}")
  }
  if(domain != null) {
    options = options.concat(" -d ${domain}")
  }
  if(varFiles != null) {
    varFiles.each { varFile ->
      options = options.concat(" --vars-file ${applicationDir} ${varFile}")
    }
  }
  if(scale != null) {
    String instances = CommonUtils.getKeyValue(scale, "instances")
    String disk = CommonUtils.getKeyValue(scale, "disk")
    String memory = CommonUtils.getKeyValue(scale, "memory")
    if(instances != null) {
      options = options.concat(" -i ${instances}")
    }
    if(disk != null) {
      options = options.concat(" -k ${disk}")
    }
    if(memory != null) {
      options = options.concat(" -m ${memory}")
    }
  }
  if((envs != null || services != null) && appName != null) {
    options = options.concat(" ${appName} --no-start")
  }
  return options
}

/**
 * Processes push operation if options are present
 * @param envParams Environment parameters
 * @param applicationDir path to application directory
 * @param options options affecting push
 * @param appName appName that is going to be affected
 * @param envs env properties that affect application start behavior
 * @param services services that affect application start behavior
 * @param path path to deployable artifact
 * @return
 */
def processWithOptions(def envParams, String applicationDir, String options, String appName, def envs, def services, String path) {
  processCustomServices(services)
  processServiceCreation(services)
  boolean ignorePath = CommonUtils.getDeployKeyValue(envParams, "ignorePath")

  try {
    if (ignorePath) {
      sh "cf push -f ${applicationDir}/manifest.yml ${options}"
    }
    else {
      sh "cf push -f ${applicationDir}/manifest.yml -p ${applicationDir}/${path} ${options}"
    }
  } catch (e) {
    error "Failed to perform cf push with options: " + e.getMessage()
  }

  setEnvironments(envs, appName)
  processBindServices(services, appName)
  processAutoScalePolicy(envParams, appName)
  if((envs != null || services != null) && appName != null) {
    processCfAction("restart", appName)
  }
}

/**
 * Process custom service creation / updation
 * @param services array of custom services and corresponding parameters
 * @return
 */
def processCustomServices(def services) {
  try {
    if(services != null) {
      String serviceList = getServiceList()
      services.each { service ->
        boolean custom = CommonUtils.getKeyValue(service, "custom", false)
        String syslog = CommonUtils.getKeyValue(service, "syslog")
        if(custom) {
          String name = CommonUtils.getKeyValue(service, "name")
          def params = CommonUtils.getKeyValue(service, "params")
          def secureParams = CommonUtils.getKeyValue(service, "secureParams")
          def customParams = [:]
          customParams = getServiceParams(customParams, secureParams, true)
          customParams = getServiceParams(customParams, params)
          if(serviceList.contains(name)) {
            performCustomServiceAction("uups", name, customParams)
          } else {
            performCustomServiceAction("cups", name, customParams)
          }
        } else if (syslog != null) {
          String name = CommonUtils.getKeyValue(service, "name")
          if(!(serviceList.contains(name))) {
            sh "cf cups ${name} -l syslog://${syslog}"
          }
        }
      }
    }
  } catch (e) {
    error "Failed to create / update custom services: " + e.getMessage()
  }
}

/**
 * Performs specified action for a custom service
 * @param action action to perform e.g. update or create
 * @param name name of service against which action is to be performed
 * @param params service parameters to be passed as json
 * @return
 */
def performCustomServiceAction(String action, String name, def params) {
  if(params!= null) {
    def jsonParams = JsonOutput.toJson(params)
    echo "ℹ️ [INFO] Performing ${action} action on ${name}"
    sh """
      set +x
      cf ${action} ${name} -p '${jsonParams}'
    """
  } else {
    sh "cf ${action} ${name}"
  }
}

/**
 * Creates a non-custom service
 * @param services array of non-custom services and corresponding parameters
 * @return
 */
def processServiceCreation(def services) {
  try {
    if(services != null) {
      String serviceList = getServiceList()
      services.each { service ->
        def create = CommonUtils.getKeyValue(service, "create")
        if(create != null) {
          String name = CommonUtils.getKeyValue(service, "name")
          String service_name = CommonUtils.getKeyValue(create, "service_name")
          String service_plan = CommonUtils.getKeyValue(create, "service_plan")
          if(!(serviceList.contains(name))) {
            sh "cf cs '${service_name}' '${service_plan}' '${name}'"
          }
        }
      }
    }
  } catch (e) {
    error "Failed to create non-custom services: " + e.getMessage()
  }
}

/**
 * Processes auto-scale policy for given application
 * @param envParams Environment parameters
 * @param appName appName whose auto-scale policy is going to be affected
 * @return
 */
def processAutoScalePolicy(def envParams, String appName) {
  try {
    if(appName != null) {
      String autoScalerPolicy = CommonUtils.getDeployKeyValue(envParams, "autoScalerPolicy")
      String autoScalerPolicyPrefix = CommonUtils.getDeployKeyValue(envParams, "autoScalerPolicyPrefix")
      String oauthToken = getOauthToken()
      if(autoScalerPolicy != null || autoScalerPolicyPrefix != null) {
        def applicationDir = env.APPLICATION_DIR.toString().trim()
        String appGuid = getAppGuid(appName)
        String appUrl = getAppUrl(appName)
        String policyPath = ""
        if(autoScalerPolicyPrefix != null) {
          String profile = CommonUtils.getEnvProfile()
          policyPath = autoScalerPolicyPrefix + profile + ".json"
        } else {
          policyPath = autoScalerPolicy
        }

        String requestBody = readFile applicationDir+"/"+policyPath
        def requestUrl = "${appUrl}/v1/autoscaler/apps/${appGuid}/policy"
        httpRequest acceptType: 'APPLICATION_JSON', contentType: 'APPLICATION_JSON', requestBody: """${requestBody}""", customHeaders: [[maskValue: true, name: 'Authorization', value: oauthToken]], httpMode: 'PUT', quiet: true, responseHandle: 'NONE', url: requestUrl
      }
    }
  } catch (e) {
    error "Failed to add autoscale policy: " + e.getMessage()
  }
}

/**
 * Returns list of services on bluemix
 * @return service list
 */
def getServiceList() {
  String serviceList = sh returnStdout: true, script: 'cf services'
  return serviceList
}

/**
 * Returns list of applications on bluemix
 * @return application list
 */
def getAppsList() {
  String appsList = sh returnStdout: true, script: "cf apps | awk '{print \$1}'"
  return appsList
}

/**
 * Returns OauthToken
 * @return OauthToken
 */
def getOauthToken() {
  String oauthToken = sh returnStdout: true, script: 'cf oauth-token'
  return oauthToken.trim()
}

/**
 * Returns application's guid
 * @param appName appName whose guid is required
 * @return application guid
 */
def getAppGuid(String appName) {
  String appGuid = sh returnStdout: true, script: "cf app ${appName} --guid"
  return appGuid.trim()
}

/**
 * Returns application's url
 * @param appName appName whose url is required
 * @return application url
 */
def getAppUrl(String appName) {
  String appUrl = sh returnStdout: true, script: """cf env ${appName} | grep 'api_url' | awk '{print \$2}' | cut -d ',' -f1 | cut -d '"' -f2"""
  return appUrl.trim()
}

/**
 * Sets environment for given application
 * @param envs array of env properties to be set
 * @param appName appName for which environment needs to be set
 * @return
 */
def setEnvironments(def envs, String appName) {
  try {
    if(envs != null && appName != null) {
      envs.each { envVar ->
        if(envVar.size() > 1) {
          String key = envVar[0]
          String value = envVar[1]
          sh "cf set-env ${appName} ${key} '${value}'"
        }
      }
    }
  } catch (e) {
    error "Failed to set-env: " + e.getMessage()
  }
}

/**
 * Process service binding to given application
 * @param services array of services that need to be bound
 * @param appName appName to which services will be bound
 * @return
 */
def processBindServices(def services, String appName) {
  try {
    if(services != null && appName != null) {
      services.each { service ->
        String name = CommonUtils.getKeyValue(service, "name")
        def params = CommonUtils.getKeyValue(service, "params")
        def secureParams = CommonUtils.getKeyValue(service, "secureParams")
        def customParams = [:]
        customParams = getServiceParams(customParams, secureParams, true)
        customParams = getServiceParams(customParams, params)
        if(customParams!= null) {
          def jsonParams = JsonOutput.toJson(customParams)
          echo "ℹ️ [INFO] Performing bind-service for ${appName} with ${name}"
          sh """
            set +x
            cf bind-service ${appName} '${name}' -c '${jsonParams}'
          """
        } else {
          sh "cf bind-service ${appName} '${name}'"
        }
      }
    }
  } catch (e) {
    error "Failed to bind-service: " + e.getMessage()
  }
}

/**
 * Perform cf action on bluemix
 * @param action action that needs to be on app
 * @param appName appName upon which action is to be done
 * @return
 */
def processCfAction(String action, String appName) {
  try {
    if (action.equalsIgnoreCase("delete")){
      sh "cf ${action} ${appName} -f"
    } else{
      sh "cf ${action} ${appName}"
    }
//    if (action.equalsIgnoreCase("map") || action.equalsIgnoreCase("unmap")){
//      sh "cf ${action} ${appName} "
//    }
  } catch (e) {
    error "Failed to ${action} application ${appName}: " + e.getMessage()
  }
}

/**
 * Process configRefresh
 * @param envParams
 * @return
 */
def processConfigRefresh(def envParams) {
  try {
    def configRefresh = CommonUtils.getDeployKeyValue(envParams, "configRefresh")
    refreshConfigService(configRefresh)
  } catch (e) {
    echo "ℹ️ [INFO] " + e.getMessage() + " Ignoring error and proceeding."
  }
}

/**
 * Performs logout
 * @return
 */
def processLogout() {
  try {
    sh "cf logout"
  } catch (e) {
    echo "ℹ️ [INFO] Failed to complete logout. Ignoring error and proceeding. Error for reference: " + e.getMessage()
  }
}

/**
 * Get map of service parameters
 * @param serviceParams existing service parameters
 * @param params array of additional service parameters to be added to serviceParams
 * @param secure boolean to determine if params is a secure param to be extracted from credentials
 * @return map of service parameters
 */
def getServiceParams(def serviceParams, def params, boolean secure = false) {
  if(params != null) {
    params.each { param ->
      def key
      def value
      if(secure) {
        withCredentials([usernamePassword(credentialsId: param, passwordVariable: 'password', usernameVariable: 'username')]) {
          key = username
          value = password
        }
      } else {
        key = param.getKey()
        value = param.getValue()
      }
      serviceParams.put(key, value)
    }
    // checks if config refresh credentials are set
    if (Default.CONFIG_REFRESH_CREDENTIALS){
      def key
      def value
      // add user name and password to custom params
      key = "configUser"
      value = Default.CONFIG_REFRESH_USER
      serviceParams.put(key, value)

      key = "configPassword"
      value = Default.CONFIG_REFRESH_PASSWORD
      serviceParams.put(key, value)
    }
  }
  return serviceParams
}

/**
 * Process a configuration service
 * @param configServiceParams configuration service parameters
 * @return
 */
def processConfigService(def configServiceParams) {
  if(configServiceParams != null) {
    String service = CommonUtils.getKeyValue(configServiceParams, "service")
    try {
      String url = CommonUtils.getKeyValue(configServiceParams, "url")
      String body = CommonUtils.getDeployNestedKeyValue(configServiceParams, service, "body")
      if(body == null) {
        String configRepo = CommonUtils.getDeployNestedKeyValue(configServiceParams, service, "configRepo").toString().trim()
        String profile = getEnvProfile()
        body = """[{"uri":"${configRepo}/${profile}/master", "comments":"Jenkins Refresh"}]"""
      }
      def secureHeaders = CommonUtils.getDeployNestedKeyValue(configServiceParams, service, "secureHeaders")
      def headers = CommonUtils.getDeployNestedKeyValue(configServiceParams, service, "headers")
      def credentials = CommonUtils.getDeployNestedKeyValue(configServiceParams, service, "credentials")
      if (credentials != null){
        Default.CONFIG_REFRESH_CREDENTIALS = true
        storeCredentials(credentials)
      }
      storeCredentials(credentials) // used for custom service params
      def httpMethod = CommonUtils.getDeployNestedKeyValue(configServiceParams, service, "httpMethod", Default.DEPLOYMENT_CONFIG_SERVICE_HTTP_METHOD)
      String contentType = CommonUtils.getDeployNestedKeyValue(configServiceParams, service, "contentType", Default.DEPLOYMENT_CONFIG_SERVICE_CONTENT_TYPE)
      def customHeaders = []
      customHeaders.addAll(getCustomHeaders(secureHeaders, true))
      customHeaders.addAll(getCustomHeaders(headers))
      def response

      if(customHeaders.size() > 0 && credentials != null) {
        response = httpRequest httpMode: httpMethod, requestBody: body, authentication: credentials, contentType: contentType, customHeaders: customHeaders, ignoreSslErrors: true, quiet: true, responseHandle: 'NONE', url: url, validResponseCodes: '100:599'
      } else {
        if(customHeaders.size() > 0) {
          response = httpRequest httpMode: httpMethod, requestBody: body, contentType: contentType, customHeaders: customHeaders, ignoreSslErrors: true, quiet: true, responseHandle: 'NONE', url: url, validResponseCodes: '100:599'
        } else {
          response = httpRequest httpMode: httpMethod, requestBody: body, authentication: credentials, contentType: contentType, ignoreSslErrors: true, quiet: true, responseHandle: 'NONE', url: url, validResponseCodes: '100:599'
        }
      }

      if(response.getStatus().equals(200)) {
        echo "ℹ️ [INFO] Successfully ${service} the configuration service: " + response.toString()
      } else {
        error "Response code invalid: " + response.toString()
      }
    } catch (e) {
      error "Failed to ${service} the configuration service: " + e.getMessage()
    }
  }
}

/**
 * Returns map of custom headers to be used in configuration service
 * @param headers array of headers be parsed
 * @param maskValue boolean to determine if header should be masked in http request
 * @return map of custom headers
 */
def getCustomHeaders(def headers, boolean maskValue = false) {
  def customHeaders = []
  if(headers != null) {
    headers.each { header ->
      String name = ""
      String value = ""
      if(maskValue) {
        withCredentials([usernamePassword(credentialsId: header, passwordVariable: 'password', usernameVariable: 'username')]) {
          name = username
          value = password
        }
      } else {
        if(header.size()>1) {
          name = header[0]
          value = header[1]
        }
      }
      def headerItem = [:]
      headerItem.put("name", name)
      headerItem.put("value", value)
      headerItem.put("maskValue", maskValue)
      customHeaders.add(headerItem)
    }
  }
  return customHeaders
}

/**
 * Get profile from envs
 * @return
 */
static String getEnvProfile() {
  String profile = BuildData.instance.envProfile
  if(profile == null) {
    def envs = CommonUtils.getDeployKeyValue(null, "envs")
    if(envs != null) {
      for(int i=0;i<envs.size();i++) {
        def envVar = envs.get(i)
        if(envVar.size() > 1) {
          String key = envVar[0]
          String value = envVar[1]
          if(key.equalsIgnoreCase("profile")) {
            profile = value
            break
          }
        }
      }
    }
    BuildData.instance.envProfile = profile
  }
  return profile
}

/**
 * Invokes config refresh configuration service
 * @param configRefresh config refresh parameters
 * @return
 */
def refreshConfigService(def configRefresh) {
  if (configRefresh != null) {
    configRefresh.put("service", Constant.DEPLOYMENT_CONFIG_SERVICE_REFRESH)
    String url = CommonUtils.getKeyValue(configRefresh, "url")
    Boolean production = CommonUtils.getKeyValue(configRefresh, "production")
    if (url == null) {
      if (production) {
        url = Constant.DEPLOYMENT_CONFIG_SERVICE_URL_PROD + Constant.DEPLOYMENT_CONFIG_SERVICE_REFRESH.toLowerCase()
      } else {
        url = Constant.DEPLOYMENT_CONFIG_SERVICE_URL_NONPROD + Constant.DEPLOYMENT_CONFIG_SERVICE_REFRESH.toLowerCase()
      }
    }
    configRefresh.put("url", url)
    processConfigService(configRefresh)
  }
}

/**
 * Invokes paas encrypt configuration service
 * @param paasEncrypt paas encrypt parameters
 * @return
 */
def paasEncryptService(def paasEncrypt) {
  if(paasEncrypt != null) {
    paasEncrypt.put("service", Constant.DEPLOYMENT_CONFIG_SERVICE_PAASENCRYPT)
    String url = CommonUtils.getKeyValue(paasEncrypt, "url")
    Boolean production = CommonUtils.getKeyValue(paasEncrypt, "production")
    if(url == null) {
      if(production){
        url = Constant.DEPLOYMENT_CONFIG_SERVICE_URL_PROD+ Constant.DEPLOYMENT_CONFIG_SERVICE_PAASENCRYPT.toLowerCase()
      }else {
        url = Constant.DEPLOYMENT_CONFIG_SERVICE_URL_NONPROD+ Constant.DEPLOYMENT_CONFIG_SERVICE_PAASENCRYPT.toLowerCase()
      }
    }
    paasEncrypt.put("url",url)
    processConfigService(paasEncrypt)
  }
}

/**
 * Gets file pattern for deployable artifacts
 * @param envParams Environment parameters
 * @return deployable artifact file pattern
 */
def getDeployFilePatterns(envParams) {
  String filePattern = CommonUtils.getDeployKeyValue(envParams, "path",  Default.BLUEMIX_PATH)
  if(BuildData.instance.appType.equals(AppType.ANGULAR)) {
    filePattern = CommonUtils.getDeployKeyValue(envParams, "path",  Default.BLUEMIX_ANGULAR_PATH)
  }
  String autoScalerPolicy = CommonUtils.getDeployKeyValue(envParams, "autoScalerPolicy")
  if(autoScalerPolicy != null) {
    filePattern = filePattern.concat(",manifest.yml").concat(",${autoScalerPolicy}")
  } else {
    filePattern = filePattern.concat(",manifest.yml")
  }
  return filePattern
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
 * Returns the first value in a comma-separated key from envParams.
 * If key's value is not comma-separated, then returns original value.
 * Provides support for normal vs blue-green deployment parameters.
 * @param envParams Map from which key is to be extracted.
 * @param key key whose default value is required.
 * @return default value from key.
 */
def getDefaultRouteKey(def envParams, String key) {
  String keyValue = CommonUtils.getDeployKeyValue(envParams, key)
  String defaultValue = keyValue
  if(keyValue != null && keyValue.contains(",")) {
    def values = keyValue.split(",")
    defaultValue = values[0].trim()
  }
  return defaultValue
}

/**
 * Processes blue to green map routing
 * @param envParams envParams for given env
 */
def processBlueGreen(def envParams) {
  String hostName = CommonUtils.getDeployKeyValue(envParams, "hostName")
  String domain = CommonUtils.getDeployKeyValue(envParams, "domain")
  String appName = CommonUtils.getDeployKeyValue(envParams, "appName")

  String defaultHost = hostName
  String stableHost = hostName

  if(hostName.contains(",")) {
    def hosts = hostName.split(",")
    defaultHost = hosts[0].trim()
    stableHost = hosts[1].trim()
  }

  String defaultDomain = domain
  String stableDomain = domain

  // Domain is optional to be default and stable. Else single domain will be treated as both default and stable.
  if(domain.contains(",")) {
    def domains = domain.split(",")
    defaultDomain = domains[0].trim()
    stableDomain = domains[1].trim()
  }

  String appWithRoute = getAppWithRoute(stableDomain, stableHost)

    // Map stable hostname and stable domain to app
  performRouteAction("map-route", appName, stableDomain, stableHost)

    // Unmap the default hostname and domain from the app
  performRouteAction("unmap-route", appName, defaultDomain, defaultHost)

  def envPair = ["ROLLBACK", appWithRoute]
  def envs = [envPair]
  setEnvironments(envs, appName)

  // Restart the application for changes to take effect
  //processCfAction("restart", appName)

  if (appWithRoute?.trim() && !appName.equalsIgnoreCase(appWithRoute)){
    // Unmap the stable hostname and domain route from existing app
    performRouteAction("unmap-route", appWithRoute, stableDomain, stableHost)

    // Stop the app with the previously attached route
    processCfAction("stop", appWithRoute)
  }


  // Delete the default domain and hostname since stable app version is receiving traffic on stable domain and hostname.
  // TODO: Needs review if deletion is required during blue to green switching. It may be required for next blue deployment and should not result in error.
  performRouteAction("delete-route", null, defaultDomain, defaultHost)

}

/**
 * Performs mapping / unmapping / deletion of routes for given app.
 * @param action map-route / unmap-route / delete-route
 * @param appName appName
 * @param domain domain
 * @param hostName hostname
 */
def performRouteAction(String action, String appName, String domain, String hostName) {
  try {
    if(action.equalsIgnoreCase("delete-route")) {
      sh "cf ${action} -f ${domain} --hostname ${hostName}"
    } else {
      sh "cf ${action} ${appName} ${domain} --hostname ${hostName}"
    }
  } catch (e) {
    error "Failed to unmap route for application ${appName} on domain ${domain} and hostname ${hostname}: " + e.getMessage()
  }
}

/**
 * Returns existing app name that has the domain and hostname route mapped to it.
 * @param domain domain
 * @param hostName hostname
 * @return appName with mapped route
 */
def getAppWithRoute(String domain, String hostName) {
  String appWithRoute = sh returnStdout: true, script: "cf routes | grep -w ${domain} | grep -w ${hostName} | awk '{print \$4}' | tr ',' ' '"
  return appWithRoute.trim()
}

/**
 * Helper function to parse and store credentials from config refresh
 *@param credentials from config refresh
 */

def storeCredentials(def credentials){
  withCredentials([usernamePassword(credentialsId: credentials, passwordVariable: 'password', usernameVariable: 'username')]) {
    Default.CONFIG_REFRESH_USER = username
    Default.CONFIG_REFRESH_PASSWORD = password
  }
}
