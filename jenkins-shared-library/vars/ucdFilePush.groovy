#!/usr/bin/env groovy
import org.kp.constants.*
import org.kp.utils.*
import org.kp.deploy.*

def call(Map params = [:]) {
  CommonUtils commonUtils = new CommonUtils()
  ApplicationUtils applicationUtils = new ApplicationUtils()
  params.put("setProperties", false)
  commonUtils.resolveConfigurations(params)
  boolean propertiesSet = true

  timestamps {
    node (BuildData.instance.agent) {
      try {
        String effectiveBaseDir = ""
        String baseDir = commonUtils.getParamValue("baseDir", Default.UCD_BASEDIR)
        if(BuildData.instance.appType.equals(AppType.ANGULAR)) {
          baseDir = commonUtils.getParamValue("baseDir", Default.UCD_ANGULAR_BASEDIR)
        }
        if(baseDir.contains(",")) {
          def baseDirs = baseDir.tokenize(',').join("\n").toString()
          commonUtils.setupNode()
          commonUtils.setJobProperties(parameters([choice(choices: baseDirs.toString(), description: 'Base Directory to use', name: 'BASE_DIR')]))
          try {
            effectiveBaseDir = BASE_DIR
          } catch (MissingPropertyException e) {
            propertiesSet = false
            new NotificationUtils().sendMail("Rebuild required", BuildData.instance.emailRecipients, "BASE_DIR build parameter has been set. Please start another build and select the build parameter to use as Base Directory.")
            echo "ℹ️ [INFO] BASE_DIR build parameter set."
            echo "ℹ️ [INFO] Please start another build and select the build parameter to use as Base Directory."
          }
        } else {
          effectiveBaseDir = baseDir
          commonUtils.setupNode()
        }

        def gitUtil = new GitUtils()
        String applicationName = new ApplicationUtils().getApplicationName()
        if(propertiesSet) {
          stage(Stage.CHECKOUT) {
            if(applicationName.contains("_config")) {
              gitUtil.cloneCurrentRepo("CONFIG")
            } else {
              gitUtil.cloneCurrentRepo("UCD_ARTIFACT_PUSH")
            }
          }

          def tag = commonUtils.getParamValue("tag")
          String tagInfix = null
          if(tag != null) {
            if(applicationName.contains("_config")) {
              tagInfix = CommonUtils.getKeyValue(tag, "tagInfix", "JDK8_Config")
              tag.put("tagInfix", tagInfix)
            } else {
              tagInfix = CommonUtils.getKeyValue(tag, "tagInfix")
            }
            gitUtil.tagBuild(tag, applicationUtils.getRepoUrl())
          }

          def ucdParams = [:]
          ucdParams.put("siteName", commonUtils.getParamValue("siteName", Default.UCD_SITENAME))
          ucdParams.put("componentName", commonUtils.getParamValue("componentName"))
          ucdParams.put("baseDir", effectiveBaseDir.trim())
          ucdParams.put("include", commonUtils.getParamValue("include", Default.UCD_INCLUDE))
          ucdParams.put("exclude", commonUtils.getParamValue("exclude", Default.UCD_EXCLUDE))
          ucdParams.put("version", commonUtils.getParamValue("version", commonUtils.getTagLabel(tagInfix)))
          ucdParams.put("pushProperties", commonUtils.getParamValue("pushProperties", Default.UCD_PUSHPROPERTIES))
          ucdParams.put("pushDescription", commonUtils.getParamValue("pushDescription", applicationUtils.getRepoUrl()))
          ucdParams.put("pushIncremental", commonUtils.getParamValue("pushIncremental", Default.UCD_PUSHINCREMENTAL))

          def ucdObj = new UCD()
          ucdObj.performPushFile(ucdParams)
        }
        commonUtils.setResult()
      } catch (exception) {
        commonUtils.handleException(exception)
      } finally {
        deleteDir()
        if(propertiesSet) {
          commonUtils.quit()
        }
      }
    }
  }
}
