#!/usr/bin/env groovy

import org.kp.utils.*
import org.kp.constants.*
import org.kp.deploy.*

def call(Map params = [:]) {
  CommonUtils commonUtils = new CommonUtils()
  commonUtils.resolveConfigurations(params)
  boolean environmentSet = true

  timestamps {
    node (BuildData.instance.agent) {
      try {
          commonUtils.setupNode(false)
          stage(Stage.CHECKOUT) {
            def gitUtil = new GitUtils()
            gitUtil.cloneCurrentRepo(AppType.APIC)
          }

          String envParam = commonUtils.getParamValue("environment")
          String allEnvs = ""

          if(envParam != null) {
            allEnvs = envParam.tokenize(',').join("\n").toString()
          } else {
            dir(env.APPLICATION_DIR) {
              def files = findFiles(glob: '*api_*.yaml')
              Map<String, Object> datas = readYaml file: "${env.APPLICATION_DIR}/${files[0].name}"
              def envs = datas.get("x-ibm-configuration").get("catalogs").keySet() as List
              allEnvs = envs.join("\n").toString()
            }
          }

          commonUtils.setJobProperties(parameters([choice(choices: allEnvs, description: 'Environment to Deploy on', name: 'ENVIRONMENT')]))

          try {
            if(ENVIRONMENT != null) {
              def ucdParams = [:]
              String componentName = commonUtils.getParamValue("componentName")
              String application = commonUtils.getParamValue("application")

              if(componentName == null && application != null) {
                ucdParams.put("componentName", application)
              }

              if(componentName != null && application == null) {
                ucdParams.put("application", componentName)
              }

              ucdParams.put("baseDir", ".")
              ucdParams.put("include", "*.yaml")
              ucdParams.put("environment", ENVIRONMENT)
              ucdParams.put("process", commonUtils.getParamValue("process", "DeployAPIConnect"))
              ucdParams.put("siteName", commonUtils.getParamValue("siteName", Default.UCD_SITENAME))
              ucdParams.put("tagInfix", commonUtils.getParamValue("tagInfix"))
              ucdParams.put("tagLabel", commonUtils.getParamValue("tagLabel", commonUtils.getTagLabel(commonUtils.getParamValue("tagInfix"))))
              ucdParams.put("exclude", commonUtils.getParamValue("exclude", Default.UCD_EXCLUDE))
              ucdParams.put("pushProperties", commonUtils.getParamValue("pushProperties", Default.UCD_PUSHPROPERTIES))
              ucdParams.put("pushDescription", commonUtils.getParamValue("pushDescription"))
              ucdParams.put("pushIncremental", commonUtils.getParamValue("pushIncremental", Default.UCD_PUSHINCREMENTAL))
              ucdParams.put("version", commonUtils.getParamValue("version"))
              ucdParams.put("deployOnlyChanged", commonUtils.getParamValue("deployOnlyChanged", Default.UCD_DEPLOYONLYCHANGED))

              def ucdObj = new UCD()
              ucdObj.performPushFile(ucdParams)
              ucdObj.performDeployment(ucdParams)
            }
          } catch (MissingPropertyException e) {
            environmentSet = false
            new NotificationUtils().sendMail("Rebuild required", BuildData.instance.emailRecipients, "ENVIRONMENT build parameter has been set. Please start another build and select the build parameter to deploy on.")
            echo "ℹ️ [INFO] ENVIRONMENT build parameter set."
            echo "ℹ️ [INFO] Please start another build and select the build parameter to deploy on."
          }
        commonUtils.setResult()
      } catch (exception) {
        commonUtils.handleException(exception)
      } finally {
        deleteDir()
        if(environmentSet) {
          currentBuild.description = ENVIRONMENT
        }
        commonUtils.quit()
      }
    }
  }
}
