package org.kp.constants

import spock.lang.Specification


class MessageSpec extends Specification {
  static String newValue = "new-value"

  def "verify UNKNOWN_APP message keyword value and is static and final"() {
    expect:
    Message.UNKNOWN_APP.getClass().toString() == "class java.lang.String"

    when:
    Message.UNKNOWN_APP = newValue
    then:
    thrown(ReadOnlyPropertyException)
    Message.UNKNOWN_APP == "Unknown application type"
  }

  def "verify UNKNOWN_PLATFORM message keyword value and is static and final"() {
    expect:
    Message.UNKNOWN_PLATFORM.getClass().toString() == "class java.lang.String"

    when:
    Message.UNKNOWN_PLATFORM = newValue
    then:
    thrown(ReadOnlyPropertyException)
    Message.UNKNOWN_PLATFORM == "Unknown deployment platform"
  }

  def "verify BUILD_FAILURE message keyword value and is static and final"() {
    expect:
    Message.BUILD_FAILURE.getClass().toString() == "class java.lang.String"

    when:
    Message.BUILD_FAILURE = newValue
    then:
    thrown(ReadOnlyPropertyException)
    Message.BUILD_FAILURE == "Failed to build the application."
  }

  def "verify INPUT_SUBMIT message keyword value and is static and final"() {
    expect:
    Message.INPUT_SUBMIT.getClass().toString() == "class java.lang.String"

    when:
    Message.INPUT_SUBMIT = newValue
    then:
    thrown(ReadOnlyPropertyException)
    Message.INPUT_SUBMIT == "👉 Submit"
  }

  def "verify INPUT_CROSSED_SUBMIT message keyword value and is static and final"() {
    expect:
    Message.INPUT_CROSSED_SUBMIT.getClass().toString() == "class java.lang.String"

    when:
    Message.INPUT_CROSSED_SUBMIT = newValue
    then:
    thrown(ReadOnlyPropertyException)
    Message.INPUT_CROSSED_SUBMIT == "🤞 Submit"
  }

  def "verify INPUT_DEPLOY message keyword value and is static and final"() {
    expect:
    Message.INPUT_DEPLOY.getClass().toString() == "class java.lang.String"

    when:
    Message.INPUT_DEPLOY = newValue
    then:
    thrown(ReadOnlyPropertyException)
    Message.INPUT_DEPLOY == "🤞 Deploy"
  }

  def "verify INPUT_PROCEED message keyword value and is static and final"() {
    expect:
    Message.INPUT_PROCEED.getClass().toString() == "class java.lang.String"

    when:
    Message.INPUT_PROCEED = newValue
    then:
    thrown(ReadOnlyPropertyException)
    Message.INPUT_PROCEED == "👉 Proceed"
  }

  def "verify INPUT_YES message keyword value and is static and final"() {
    expect:
    Message.INPUT_YES.getClass().toString() == "class java.lang.String"

    when:
    Message.INPUT_YES = newValue
    then:
    thrown(ReadOnlyPropertyException)
    Message.INPUT_YES == "👌 YES"
  }

}