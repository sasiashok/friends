package org.kp.constants

import spock.lang.Specification

class AppTypeSpec extends Specification {

  static String newValue = "new-value"

  def "verify AEM appType keyword value and is static and final"() {
    expect:
    AppType.AEM.getClass().toString() == "class java.lang.String"

    when:
    AppType.AEM = newValue
    then:
    thrown(ReadOnlyPropertyException)
    AppType.AEM == "AEM"
  }

  def "verify MAVEN appType keyword value and is static and final"() {
    expect:
    AppType.MAVEN.getClass().toString() == "class java.lang.String"

    when:
     AppType.MAVEN = newValue
    then:
    thrown(ReadOnlyPropertyException)
     AppType.MAVEN == "MAVEN"
  }

  def "verify GRADLE appType keyword value and is static and final"() {
    expect:
    AppType.GRADLE.getClass().toString() == "class java.lang.String"

    when:
     AppType.GRADLE = newValue
    then:
    thrown(ReadOnlyPropertyException)
     AppType.GRADLE == "GRADLE"
  }

  def "verify DOTNET appType keyword value and is static and final"() {
    expect:
    AppType.DOTNET.getClass().toString() == "class java.lang.String"

    when:
     AppType.DOTNET = newValue
    then:
    thrown(ReadOnlyPropertyException)
     AppType.DOTNET == "DOTNET"
  }

  def "verify ANGULAR appType keyword value and is static and final"() {
    expect:
    AppType.ANGULAR.getClass().toString() == "class java.lang.String"

    when:
     AppType.ANGULAR = newValue
    then:
    thrown(ReadOnlyPropertyException)
     AppType.ANGULAR == "ANGULAR"
  }

  def "verify NODEJS appType keyword value and is static and final"() {
    expect:
    AppType.NODEJS.getClass().toString() == "class java.lang.String"

    when:
     AppType.NODEJS = newValue
    then:
    thrown(ReadOnlyPropertyException)
     AppType.NODEJS == "NODEJS"
  }

  def "verify APIC appType keyword value and is static and final"() {
    expect:
    AppType.APIC.getClass().toString() == "class java.lang.String"

    when:
     AppType.APIC = newValue
    then:
    thrown(ReadOnlyPropertyException)
     AppType.APIC == "APIC"
  }

  def "verify REMOTE appType keyword value and is static and final"() {
    expect:
    AppType.REMOTE.getClass().toString() == "class java.lang.String"

    when:
     AppType.REMOTE = newValue
    then:
    thrown(ReadOnlyPropertyException)
     AppType.REMOTE == "REMOTE"
  }

  def "verify OTHER appType keyword value and is static and final"() {
    expect:
    AppType.OTHER.getClass().toString() == "class java.lang.String"

    when:
     AppType.OTHER = newValue
    then:
    thrown(ReadOnlyPropertyException)
     AppType.OTHER == "OTHER"
  }

  def "verify AEM marker file value and is static and final"() {
    expect:
    AppType.MARKER_AEM.getClass().toString() == "class java.lang.String"

    when:
     AppType.MARKER_AEM = newValue
    then:
    thrown(ReadOnlyPropertyException)
     AppType.MARKER_AEM == "ui.apps"
  }

  def "verify MAVEN marker file value and is static and final"() {
    expect:
    AppType.MARKER_MAVEN.getClass().toString() == "class java.lang.String"

    when:
     AppType.MARKER_MAVEN = newValue
    then:
    thrown(ReadOnlyPropertyException)
     AppType.MARKER_MAVEN == "pom.xml"
  }

  def "verify GRADLE marker file value and is static and final"() {
    expect:
    AppType.MARKER_GRADLE.getClass().toString() == "class java.lang.String"

    when:
     AppType.MARKER_GRADLE = newValue
    then:
    thrown(ReadOnlyPropertyException)
     AppType.MARKER_GRADLE == "build.gradle"
  }

  def "verify DOTNET marker file value and is static and final"() {
    expect:
    AppType.MARKER_DOTNET.getClass().toString() == "class java.lang.String"

    when:
     AppType.MARKER_DOTNET = newValue
    then:
    thrown(ReadOnlyPropertyException)
     AppType.MARKER_DOTNET == "*.sln"
  }

  def "verify ANGULAR marker file value and is static and final"() {
    expect:
    AppType.MARKER_ANGULAR.getClass().toString() == "class java.lang.String"

    when:
     AppType.MARKER_ANGULAR = newValue
    then:
    thrown(ReadOnlyPropertyException)
     AppType.MARKER_ANGULAR == ".angular-cli.json"
  }

  def "verify NODEJS marker file value and is static and final"() {
    expect:
    AppType.MARKER_NODEJS.getClass().toString() == "class java.lang.String"

    when:
     AppType.MARKER_NODEJS = newValue
    then:
    thrown(ReadOnlyPropertyException)
     AppType.MARKER_NODEJS == "package.json"
  }
}
