package org.kp.constants

import spock.lang.Specification

class PlatformSpec extends Specification {

  static String newValue = "new-value"

  def "verify BLUEMIX platform keyword value and is static and final"() {
    expect:
    Platform.BLUEMIX.getClass().toString() == "class java.lang.String"

    when:
    Platform.BLUEMIX = newValue
    then:
    thrown(ReadOnlyPropertyException)
    Platform.BLUEMIX == "BLUEMIX"
  }

  def "verify UCD platform keyword value and is static and final"() {
    expect:
    Platform.UCD.getClass().toString() == "class java.lang.String"

    when:
    Platform.UCD = newValue
    then:
    thrown(ReadOnlyPropertyException)
    Platform.UCD == "UCD"
  }

  def "verify CRX platform keyword value and is static and final"() {
    expect:
    Platform.CRX.getClass().toString() == "class java.lang.String"

    when:
    Platform.CRX = newValue
    then:
    thrown(ReadOnlyPropertyException)
    Platform.CRX == "CRX"
  }
}