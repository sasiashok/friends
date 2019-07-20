package org.kp.constants

import spock.lang.Specification

class StageSpec extends Specification {

  static String newValue = "new-value"

  def "verify CHECKOUT stage keyword value and is static and final"() {
    expect:
    Stage.CHECKOUT.getClass().toString() == "class java.lang.String"

    when:
    Stage.CHECKOUT = newValue
    then:
    thrown(ReadOnlyPropertyException)
    Stage.CHECKOUT == "🎬 Checkout"
  }

  def "verify TAG_BUILD stage keyword value and is static and final"() {
    expect:
    Stage.TAG_BUILD.getClass().toString() == "class java.lang.String"

    when:
    Stage.TAG_BUILD = newValue
    then:
    thrown(ReadOnlyPropertyException)
    Stage.TAG_BUILD == "🔖 Tag-Build"
  }

  def "verify BUILD stage keyword value and is static and final"() {
    expect:
    Stage.BUILD.getClass().toString() == "class java.lang.String"

    when:
    Stage.BUILD = newValue
    then:
    thrown(ReadOnlyPropertyException)
    Stage.BUILD ==  "🏗️ Build"
  }

  def "verify UNIT_TEST stage keyword value and is static and final"() {
    expect:
    Stage.UNIT_TEST.getClass().toString() == "class java.lang.String"

    when:
    Stage.UNIT_TEST = newValue
    then:
    thrown(ReadOnlyPropertyException)
    Stage.UNIT_TEST == "🔬 Unit Testing"
  }

  def "verify CODE_ANALYSIS stage keyword value and is static and final"() {
    expect:
    Stage.CODE_ANALYSIS.getClass().toString() == "class java.lang.String"

    when:
    Stage.CODE_ANALYSIS = newValue
    then:
    thrown(ReadOnlyPropertyException)
    Stage.CODE_ANALYSIS == "🔎 Code Coverage and Analysis"
  }

  def "verify NEXUS_IQ stage keyword value and is static and final"() {
    expect:
    Stage.NEXUS_IQ.getClass().toString() == "class java.lang.String"

    when:
    Stage.NEXUS_IQ = newValue
    then:
    thrown(ReadOnlyPropertyException)
    Stage.NEXUS_IQ == "👮 Nexus Policy Evaluation"
  }

  def "verify UCD_FILE_PUSH stage keyword value and is static and final"() {
    expect:
    Stage.UCD_FILE_PUSH.getClass().toString() == "class java.lang.String"

    when:
    Stage.UCD_FILE_PUSH = newValue
    then:
    thrown(ReadOnlyPropertyException)
    Stage.UCD_FILE_PUSH == "📦 Push fileType to UCD"
  }

  def "verify CHECKOUT_DEPLOY_CONFIG stage keyword value and is static and final"() {
    expect:
    Stage.CHECKOUT_DEPLOY_CONFIG.getClass().toString() == "class java.lang.String"

    when:
    Stage.CHECKOUT_DEPLOY_CONFIG = newValue
    then:
    thrown(ReadOnlyPropertyException)
    Stage.CHECKOUT_DEPLOY_CONFIG == "📒 Checkout Deploy Config"
  }

  def "verify DEPLOY_APPROVAL stage keyword value and is static and final"() {
    expect:
    Stage.DEPLOY_APPROVAL.getClass().toString() == "class java.lang.String"

    when:
    Stage.DEPLOY_APPROVAL = newValue
    then:
    thrown(ReadOnlyPropertyException)
    Stage.DEPLOY_APPROVAL == "⚠️ Deployment-Approval-"
  }

  def "verify DEPLOY stage keyword value and is static and final"() {
    expect:
    Stage.DEPLOY.getClass().toString() == "class java.lang.String"

    when:
    Stage.DEPLOY = newValue
    then:
    thrown(ReadOnlyPropertyException)
    Stage.DEPLOY == "🚀 Deploy to "
  }

  def "verify ROLLBACK stage keyword value and is static and final"() {
    expect:
    Stage.ROLLBACK.getClass().toString() == "class java.lang.String"

    when:
    Stage.ROLLBACK = newValue
    then:
    thrown(ReadOnlyPropertyException)
    Stage.ROLLBACK == "♻️ Rollback "
  }

  def "verify TEST_DEPLOYMENT stage keyword value and is static and final"() {
    expect:
    Stage.TEST_DEPLOYMENT.getClass().toString() == "class java.lang.String"

    when:
    Stage.TEST_DEPLOYMENT = newValue
    then:
    thrown(ReadOnlyPropertyException)
    Stage.TEST_DEPLOYMENT == "✅ "
  }

  def "verify TEST_VERIFICATION stage keyword value and is static and final"() {
    expect:
    Stage.TEST_VERIFICATION.getClass().toString() == "class java.lang.String"

    when:
    Stage.TEST_VERIFICATION = newValue
    then:
    thrown(ReadOnlyPropertyException)
    Stage.TEST_VERIFICATION == "🕵 Test-Verification-"
  }

  def "verify TARGET_SELECTION stage keyword value and is static and final"() {
    expect:
    Stage.TARGET_SELECTION.getClass().toString() == "class java.lang.String"

    when:
    Stage.TARGET_SELECTION = newValue
    then:
    thrown(ReadOnlyPropertyException)
    Stage.TARGET_SELECTION == "🖋️ Target Environment selection"
  }

  def "verify SOURCE_SELECTION stage keyword value and is static and final"() {
    expect:
    Stage.SOURCE_SELECTION.getClass().toString() == "class java.lang.String"

    when:
    Stage.SOURCE_SELECTION = newValue
    then:
    thrown(ReadOnlyPropertyException)
    Stage.SOURCE_SELECTION == "🖍️️ Source Environment selection"
  }

  def "verify VERSION_SELECTION stage keyword value and is static and final"() {
    expect:
    Stage.VERSION_SELECTION.getClass().toString() == "class java.lang.String"

    when:
    Stage.VERSION_SELECTION = newValue
    then:
    thrown(ReadOnlyPropertyException)
    Stage.VERSION_SELECTION == "🖌️ Version selection"
  }

  def "verify CONFIGSERVICE_INPUT_CONFIG_SERVICE stage keyword value and is static and final"() {
    expect:
    Stage.CONFIGSERVICE_INPUT_CONFIG_SERVICE.getClass().toString() == "class java.lang.String"

    when:
    Stage.CONFIGSERVICE_INPUT_CONFIG_SERVICE = newValue
    then:
    thrown(ReadOnlyPropertyException)
    Stage.CONFIGSERVICE_INPUT_CONFIG_SERVICE ==  "🖌 Input - Config-Service"
  }

  def "verify CONFIGSERVICE_PAAS_ENCRYPT stage keyword value and is static and final"() {
    expect:
    Stage.CONFIGSERVICE_PAAS_ENCRYPT.getClass().toString() == "class java.lang.String"

    when:
    Stage.CONFIGSERVICE_PAAS_ENCRYPT = newValue
    then:
    thrown(ReadOnlyPropertyException)
    Stage.CONFIGSERVICE_PAAS_ENCRYPT == "🔐️ Paas-Encrypt"
  }

  def "verify CONFIGSERVICE_CONFIG_REFRESH stage keyword value and is static and final"() {
    expect:
    Stage.CONFIGSERVICE_CONFIG_REFRESH.getClass().toString() == "class java.lang.String"

    when:
    Stage.CONFIGSERVICE_CONFIG_REFRESH = newValue
    then:
    thrown(ReadOnlyPropertyException)
    Stage.CONFIGSERVICE_CONFIG_REFRESH == "⚙️ Config-Refresh"
  }

  def "verify CONFIGSERVICE_INPUT_APP_RESTART stage keyword value and is static and final"() {
    expect:
    Stage.CONFIGSERVICE_INPUT_APP_RESTART.getClass().toString() == "class java.lang.String"

    when:
    Stage.CONFIGSERVICE_INPUT_APP_RESTART = newValue
    then:
    thrown(ReadOnlyPropertyException)
    Stage.CONFIGSERVICE_INPUT_APP_RESTART == "🖌️ Input - App-Restart"
  }

  def "verify CONFIGSERVICE_INPUT_APP_START stage keyword value and is static and final"() {
    expect:
    Stage.CONFIGSERVICE_INPUT_APP_START.getClass().toString() == "class java.lang.String"

    when:
    Stage.CONFIGSERVICE_INPUT_APP_START = newValue
    then:
    thrown(ReadOnlyPropertyException)
    Stage.CONFIGSERVICE_INPUT_APP_START == "🖌️ Input - App-Start"
  }

  def "verify CONFIGSERVICE_INPUT_APP_STOP stage keyword value and is static and final"() {
    expect:
    Stage.CONFIGSERVICE_INPUT_APP_STOP.getClass().toString() == "class java.lang.String"

    when:
    Stage.CONFIGSERVICE_INPUT_APP_STOP= newValue
    then:
    thrown(ReadOnlyPropertyException)
    Stage.CONFIGSERVICE_INPUT_APP_STOP == "🖌️ Input - App-Stop"
  }

  def "verify CONFIGSERVICE_INPUT_APP_DELETE stage keyword value and is static and final"() {
    expect:
    Stage.CONFIGSERVICE_INPUT_APP_DELETE.getClass().toString() == "class java.lang.String"

    when:
    Stage.CONFIGSERVICE_INPUT_APP_DELETE = newValue
    then:
    thrown(ReadOnlyPropertyException)
    Stage.CONFIGSERVICE_INPUT_APP_DELETE == "🖌️ Input - App-Delete"
  }

  def "verify CONFIGSERVICE_APP_RESTART stage keyword value and is static and final"() {
    expect:
    Stage.CONFIGSERVICE_APP_RESTART.getClass().toString() == "class java.lang.String"

    when:
    Stage.CONFIGSERVICE_APP_RESTART = newValue
    then:
    thrown(ReadOnlyPropertyException)
    Stage.CONFIGSERVICE_APP_RESTART == "♻️ Application Restarts"
  }
  def "verify CONFIGSERVICE_APP_START stage keyword value and is static and final"() {
    expect:
    Stage.CONFIGSERVICE_APP_START.getClass().toString() == "class java.lang.String"

    when:
    Stage.CONFIGSERVICE_APP_START = newValue
    then:
    thrown(ReadOnlyPropertyException)
    Stage.CONFIGSERVICE_APP_START == "🏃‍ Application Starts"
  }
  def "verify CONFIGSERVICE_APP_STOP stage keyword value and is static and final"() {
    expect:
    Stage.CONFIGSERVICE_APP_STOP.getClass().toString() == "class java.lang.String"

    when:
    Stage.CONFIGSERVICE_APP_STOP = newValue
    then:
    thrown(ReadOnlyPropertyException)
    Stage.CONFIGSERVICE_APP_STOP == "✋ Application Stops"
  }
  def "verify CONFIGSERVICE_APP_DELETE stage keyword value and is static and final"() {
    expect:
    Stage.CONFIGSERVICE_APP_DELETE.getClass().toString() == "class java.lang.String"

    when:
    Stage.CONFIGSERVICE_APP_DELETE = newValue
    then:
    thrown(ReadOnlyPropertyException)
    Stage.CONFIGSERVICE_APP_DELETE == "🗑️ Application Deleted"
  }

}