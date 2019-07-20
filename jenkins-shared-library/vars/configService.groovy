import com.cwctravel.hudson.plugins.extended_choice_parameter.ExtendedChoiceParameterDefinition
import org.kp.constants.*
import org.kp.utils.*
import org.kp.deploy.Bluemix

def call(Map params = [:]) {
  CommonUtils commonUtils = new CommonUtils()
  Bluemix bluemix = new Bluemix()
  commonUtils.resolveConfigurations(params)

  timestamps {
    try {
      def envParams = new CommonUtils().getParamValue("envParams")
      def envList = getEnvList(envParams)
      def envSelect= new ExtendedChoiceParameterDefinition("environment", "PT_RADIO", envList.join(","), "","", "","", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", false,false, 5, "Environment for config refresh", ",")
      def appSelect= new ExtendedChoiceParameterDefinition("apps", "PT_CHECKBOX", commonUtils.getParamValue("appKeys"), "","", "","", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", false,false, 5, "Applications for config refresh", ",")
      def patternInput = text(defaultValue: '', description: 'Array of pattern keys and values', name: 'patterns')
      def actionInput = new ExtendedChoiceParameterDefinition("action", "PT_CHECKBOX", "paas,refresh,restart,app-start,stop,delete", "","", "","", "", "", "", "", "", "", "", "", "", "", "Paas Encrypt,Config-Refresh,Restart (Apps to restart will be prompted in separate stage),Start,Stop,Delete", "", "", "", "", "", "", "", "", false,false, 8, "Actions to perform", ",")

      def userInput
      stage(Stage.CONFIGSERVICE_INPUT_CONFIG_SERVICE) {
        userInput = input  id: 'configService', message: 'Config Service Parameters', ok: Message.INPUT_SUBMIT, submitter: BuildData.instance.emailRecipients.replaceAll("\\s", ""), parameters: [envSelect, appSelect, patternInput, actionInput]
      }

      if(userInput.action.contains("paas")) {
        stage(Stage.CONFIGSERVICE_PAAS_ENCRYPT) {
          String patterns = userInput.patterns
          String service = commonUtils.getParamValue("configRepo")
          String apps = userInput.apps
          String env = userInput.environment
          params.put("production", getEnvValue(envParams, env, "production"))
          params.put("url", getEnvValue(envParams, env, "paasUrl"))
          params.put("secureHeaders", getEnvValue(envParams, env, "secureHeaders"))
          params.put("headers", getEnvValue(envParams, env, "headers"))
          params.put("credentials", getEnvValue(envParams, env, "configServiceCredentials"))
          params.put("httpMethod", getEnvValue(envParams, env, "httpMethod", Default.DEPLOYMENT_CONFIG_SERVICE_HTTP_METHOD))
          params.put("contentType", getEnvValue(envParams, env, "contentType", Default.DEPLOYMENT_CONFIG_SERVICE_CONTENT_TYPE))
          apps.tokenize(",").each { appKey ->
            String body = """[{"service": "${service}",\n"profile": "${appKey}-${env}",\n"patterns": ${patterns}}]"""
            params.put("body", body)
            try {
              bluemix.paasEncryptService(params)
            } catch (e) {
              echo "🚨 [ERROR] For ${appKey}-${env} " + e.getMessage()
            }
          }
        }
      }

      if(userInput.action.contains("refresh")) {
        stage(Stage.CONFIGSERVICE_CONFIG_REFRESH) {
          String configRepo = commonUtils.getParamValue("configRepo")
          String apps = userInput.apps
          String env = userInput.environment
          String configBranch = commonUtils.getParamValue("configBranch", Default.BRANCH)
          params.put("production", getEnvValue(envParams, env, "production"))
          params.put("url", getEnvValue(envParams, env, "configUrl"))
          params.put("secureHeaders", getEnvValue(envParams, env, "secureHeaders"))
          params.put("headers", getEnvValue(envParams, env, "headers"))
          params.put("credentials", getEnvValue(envParams, env, "configServiceCredentials"))
          params.put("httpMethod", getEnvValue(envParams, env, "httpMethod", Default.DEPLOYMENT_CONFIG_SERVICE_HTTP_METHOD))
          params.put("contentType", getEnvValue(envParams, env, "contentType", Default.DEPLOYMENT_CONFIG_SERVICE_CONTENT_TYPE))
          apps.tokenize(",").each { appKey ->
            String body = """
            [{
              "uri":"${configRepo}/${appKey}-${env}/${configBranch}",
              "comments":"Jenkins update {BUILD_DISPLAY_NAME}"
             }]"""
            params.put("body", body)
            try {
              bluemix.refreshConfigService(params)
            } catch (e) {
              echo "🚨 [ERROR] For ${appKey}-${env} " + e.getMessage()
            }
          }
        }
      }

      if (!userInput.action.contains('restart')) {
        // Only occurs if "restart" was not chosen
        if (userInput.action.contains("app-start")) {
          performCfAction(envParams, params, userInput, "start")
        }
        if (userInput.action.contains("stop")) {
          performCfAction(envParams, params, userInput, "stop")
        }
      }

      if (userInput.action.contains("delete")) {
        performCfAction(envParams, params, userInput,"delete")
      }

//      if (userInput.action.contains("map")) {
//        echo "Trying to map..."
//        def hostNameInput = text(defaultValue: '', description: 'Please enter Host Name.', name: 'hostName')
//        def domainNameInput = text(defaultValue: '', description: 'Please enter Domain Name.', name: 'domainName')
//        stage(Stage.CONFIGSERVICE_INPUT_CONFIG_SERVICE) {
//          userInput = input  id: 'configService', message: 'Config Service Parameters', ok: Message.INPUT_SUBMIT, submitter: BuildData.instance.emailRecipients.replaceAll("\\s", ""), parameters: [hostNameInput,domainNameInput]
//        }
//
//        echo "this is the host name: " + userInput.hostName
//        //performCfAction(envParams, params, userInput,action)
//      }
//      if (userInput.action.contains("unmap")) {
//        echo "Trying to unmap..."
//        //performCfAction(envParams, params, userInput,"unmap-route")
//      }

      if(userInput.action.contains("restart")) {
        performCfAction(envParams, params, userInput, "restart")
      }

      if(currentBuild.result == null) {
        currentBuild.result = currentBuild.currentResult
      }
    } catch (exception) {
      commonUtils.handleException(exception)
    } finally {
      if(!BuildData.instance.exceptionCaught) {
        new NotificationUtils().sendMail("Build Complete", BuildData.instance.emailRecipients, "build complete")
      }
    }
  }
}

/**
 *  Perform cf action based on given user choice
 * @param params
 * @param action
 * @return
 */
def performCfAction(def envParams, def params, def userInput, String action){
  Bluemix bluemix = new Bluemix()
  node (BuildData.instance.agent) {
    withEnv(["CF_HOME=${WORKSPACE}"]) {
      try {
        def appActionInput
        stage(getInputStageName(action)) {
          String env = userInput.environment
          space = getEnvValue(envParams, env, "space", env)
          params.put("credentials", getEnvValue(envParams, env, "credentials"))
          params.put("org", getEnvValue(envParams, env, "org"))
          params.put("space", space)
          params.put("apiUrl", getEnvValue(envParams, env, "apiUrl"))
          bluemix.processLogin(params)
          String appsList = bluemix.getAppsList().split("name\n").getAt(1).split('\n').join(',')
          if (action.equals('restart')){
            new NotificationUtils().sendMail("App Restart Selection", BuildData.instance.emailRecipients, "Please select which apps need to be restarted in ${space} space.\n\n⏳ Timer of 10 minutes has started for you to provide an input before the build is auto-aborted. \n\n Please provide your input here")
          }
          timeout(10) {
            try {
              def appsSelect = new ExtendedChoiceParameterDefinition("apps", "PT_CHECKBOX", appsList, "","", "","", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", false,false, 10, "Applications for restart", ",")
              appActionInput = input  id: 'appRestart', message: "Select applications to ${action} in ${space}", ok: Message.INPUT_SUBMIT, submitter: BuildData.instance.emailRecipients.replaceAll("\\s", ""), parameters: [appsSelect]
            } catch (e) {
              error "🚨 [ERROR] Timeout. Please provide input within 10 minutes else the build will be aborted in order to free executors."
            }
          }
        }

        stage(getStageName(action)) {
          List<String> appChoices = appActionInput.toString().tokenize(",")
          appChoices.each { appName ->
            try {
              bluemix.processCfAction(action, appName)
            } catch (e) {
              echo "🚨 [ERROR] For ${appName} in ${space}" + e.getMessage()
            }
          }
        }
      } finally {
        bluemix.processLogout()
        deleteDir()
      }
    }
  }
}

//helper function to get app action stage name
def getStageName(action) {
  switch (action){
    case "restart":
      return Stage.CONFIGSERVICE_APP_RESTART
    case "start":
      return Stage.CONFIGSERVICE_APP_START
    case "stop":
      return Stage.CONFIGSERVICE_APP_STOP
    case "delete":
      return Stage.CONFIGSERVICE_APP_DELETE
  }
}

//helper function to determine input stage name
def getInputStageName(action) {
  switch (action) {
    case "restart":
      return Stage.CONFIGSERVICE_INPUT_APP_RESTART
    case "start":
      return Stage.CONFIGSERVICE_INPUT_APP_START
    case "stop":
      return Stage.CONFIGSERVICE_INPUT_APP_STOP
    case "delete":
      return Stage.CONFIGSERVICE_INPUT_APP_DELETE
  }
}

def getEnvList(def envParams) {
  def envList = []
  envParams.each { envParam ->
    String env = CommonUtils.getKeyValue(envParam, "env")
    envList.add(env)
  }
  return envList
}

def getEnvValue(def envParams, String targetEnv, String key, String defaultValue = null) {
  def value
  envParams.each { envParam ->
    String env = CommonUtils.getKeyValue(envParam, "env")
    if(targetEnv.equals(env)) {
      value = CommonUtils.getKeyValue(envParam, key, CommonUtils.getKeyValue(new CommonUtils().getParamValue("common_envParams"),key, defaultValue))
    }
  }
  return value
}
