package org.kp.utils

import spock.lang.Specification

class ApplicationUtilsSpec extends Specification {

  ApplicationUtils applicationUtils

  def setup() {
    applicationUtils = new ApplicationUtils()
  }

  def "verify isSnapShot() for java and nodejs apps is determined correctly"() {
    when:
    BuildData.instance.appVersion = "1.1.0"
    then:
    applicationUtils.isSnapShot() == false

    when:
    BuildData.instance.appVersion = "1.1.0-SNAPSHOT"
    then:
    applicationUtils.isSnapShot() == true
  }

  def "verify getTrimmedApplicationVersion() trims app version correctly"() {
    when:
    BuildData.instance.appVersion = "1.1.0"
    then:
    applicationUtils.getTrimmedApplicationVersion() == "1.1.0"

    when:
    BuildData.instance.appVersion = "1.1.0-SNAPSHOT"
    then:
    applicationUtils.getTrimmedApplicationVersion() == "1.1.0"
  }
}