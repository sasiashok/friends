package org.kp.constants

import spock.lang.Specification

class DefaultSpec extends Specification {

  static String newValue = "new-value"

  def "verify AGENT value and is static and not final"() {
    expect:
    Default.AGENT == "ads-agent"
    Default.AGENT.getClass().toString() == "class java.lang.String"

    when:
    Default.AGENT = newValue
    then:
    notThrown(ReadOnlyPropertyException)
    Default.AGENT == newValue
  }

  def "verify BRANCH value and is static and not final"() {
    expect:
    Default.BRANCH == "master"
    Default.BRANCH.getClass().toString() == "class java.lang.String"

    when:
    Default.BRANCH = newValue
    then:
    notThrown(ReadOnlyPropertyException)
    Default.BRANCH == newValue
  }

  def "verify DEPLOYMENT_PLATFORM value and is static and not final"() {
    expect:
    Default.DEPLOYMENT_PLATFORM == "BLUEMIX"
    Default.DEPLOYMENT_PLATFORM.getClass().toString() == "class java.lang.String"

    when:
    Default.DEPLOYMENT_PLATFORM = newValue
    then:
    notThrown(ReadOnlyPropertyException)
    Default.DEPLOYMENT_PLATFORM == newValue
  }

  def "verify DEPLOYMENT_ENVIRONMENT value and is static and not final"() {
    expect:
    Default.DEPLOYMENT_ENVIRONMENT == "UNKNOWN"
    Default.DEPLOYMENT_ENVIRONMENT.getClass().toString() == "class java.lang.String"

    when:
    Default.DEPLOYMENT_ENVIRONMENT = newValue
    then:
    notThrown(ReadOnlyPropertyException)
    Default.DEPLOYMENT_ENVIRONMENT == newValue
  }

  def "verify DEPLOYMENT_CONFIG_SERVICE_CONTENT_TYPE value and is static and not final"() {
    expect:
    Default.DEPLOYMENT_CONFIG_SERVICE_CONTENT_TYPE == "APPLICATION_JSON"
    Default.DEPLOYMENT_CONFIG_SERVICE_CONTENT_TYPE.getClass().toString() == "class java.lang.String"

    when:
    Default.DEPLOYMENT_CONFIG_SERVICE_CONTENT_TYPE = newValue
    then:
    notThrown(ReadOnlyPropertyException)
    Default.DEPLOYMENT_CONFIG_SERVICE_CONTENT_TYPE == newValue
  }

  def "verify DEPLOYMENT_CONFIG_SERVICE_HTTP_METHOD value and is static and not final"() {
    expect:
    Default.DEPLOYMENT_CONFIG_SERVICE_HTTP_METHOD == "PUT"
    Default.DEPLOYMENT_CONFIG_SERVICE_HTTP_METHOD.getClass().toString() == "class java.lang.String"

    when:
    Default.DEPLOYMENT_CONFIG_SERVICE_HTTP_METHOD = newValue
    then:
    notThrown(ReadOnlyPropertyException)
    Default.DEPLOYMENT_CONFIG_SERVICE_HTTP_METHOD == newValue
  }

  def "verify CORBETURA_AUTOUPDATEHEALTH value and is static and not final"() {
    expect:
    Default.CORBETURA_AUTOUPDATEHEALTH == false
    Default.CORBETURA_AUTOUPDATEHEALTH.getClass().toString() == "class java.lang.Boolean"

    when:
    Default.CORBETURA_AUTOUPDATEHEALTH = true
    then:
    notThrown(ReadOnlyPropertyException)
    Default.CORBETURA_AUTOUPDATEHEALTH == true
  }

  def "verify CORBETURA_AUTOUPDATESTABILITY value and is static and not final"() {
    expect:
    Default.CORBETURA_AUTOUPDATESTABILITY == false
    Default.CORBETURA_AUTOUPDATESTABILITY.getClass().toString() == "class java.lang.Boolean"

    when:
    Default.CORBETURA_AUTOUPDATESTABILITY = true
    then:
    notThrown(ReadOnlyPropertyException)
    Default.CORBETURA_AUTOUPDATESTABILITY == true
  }

  def "verify CORBETURA_COBERTURAREPORTFILE value and is static and not final"() {
    expect:
    Default.CORBETURA_COBERTURAREPORTFILE == "**/target/site/cobertura/coverage.xml"
    Default.CORBETURA_COBERTURAREPORTFILE.getClass().toString() == "class java.lang.String"

    when:
    Default.CORBETURA_COBERTURAREPORTFILE = newValue
    then:
    notThrown(ReadOnlyPropertyException)
    Default.CORBETURA_COBERTURAREPORTFILE == newValue
  }

  def "verify CORBETURA_ANGULAR_COBERTURAREPORTFILE value and is static and not final"() {
    expect:
    Default.CORBETURA_ANGULAR_COBERTURAREPORTFILE == "coverage/cobertura-coverage.xml"
    Default.CORBETURA_ANGULAR_COBERTURAREPORTFILE.getClass().toString() == "class java.lang.String"

    when:
    Default.CORBETURA_ANGULAR_COBERTURAREPORTFILE = newValue
    then:
    notThrown(ReadOnlyPropertyException)
    Default.CORBETURA_ANGULAR_COBERTURAREPORTFILE == newValue
  }

  def "verify CORBETURA_CONDITIONALCOVERAGETARGETS value and is static and not final"() {
    expect:
    Default.CORBETURA_CONDITIONALCOVERAGETARGETS == "70, 0, 0"
    Default.CORBETURA_CONDITIONALCOVERAGETARGETS.getClass().toString() == "class java.lang.String"

    when:
    Default.CORBETURA_CONDITIONALCOVERAGETARGETS = newValue
    then:
    notThrown(ReadOnlyPropertyException)
    Default.CORBETURA_CONDITIONALCOVERAGETARGETS == newValue
  }

  def "verify CORBETURA_FAILUNHEALTHY value and is static and not final"() {
    expect:
    Default.CORBETURA_FAILUNHEALTHY == false
    Default.CORBETURA_FAILUNHEALTHY.getClass().toString() == "class java.lang.Boolean"

    when:
    Default.CORBETURA_FAILUNHEALTHY = true
    then:
    notThrown(ReadOnlyPropertyException)
    Default.CORBETURA_FAILUNHEALTHY == true
  }

  def "verify CORBETURA_FAILUNSTABLE value and is static and not final"() {
    expect:
    Default.CORBETURA_FAILUNSTABLE == false
    Default.CORBETURA_FAILUNSTABLE.getClass().toString() == "class java.lang.Boolean"

    when:
    Default.CORBETURA_FAILUNSTABLE = true
    then:
    notThrown(ReadOnlyPropertyException)
    Default.CORBETURA_FAILUNSTABLE == true
  }

  def "verify CORBETURA_LINECOVERAGETARGETS value and is static and not final"() {
    expect:
    Default.CORBETURA_LINECOVERAGETARGETS == "80, 0, 0"
    Default.CORBETURA_LINECOVERAGETARGETS.getClass().toString() == "class java.lang.String"

    when:
    Default.CORBETURA_LINECOVERAGETARGETS = newValue
    then:
    notThrown(ReadOnlyPropertyException)
    Default.CORBETURA_LINECOVERAGETARGETS == newValue
  }

  def "verify CORBETURA_MAXNUMBEROFBUILDS value and is static and not final"() {
    expect:
    Default.CORBETURA_MAXNUMBEROFBUILDS == 0
    Default.CORBETURA_MAXNUMBEROFBUILDS.getClass().toString() == "class java.lang.Integer"

    when:
    Default.CORBETURA_MAXNUMBEROFBUILDS = 10
    then:
    notThrown(ReadOnlyPropertyException)
    Default.CORBETURA_MAXNUMBEROFBUILDS == 10
  }

  def "verify CORBETURA_METHODCOVERAGETARGETS value and is static and not final"() {
    expect:
    Default.CORBETURA_METHODCOVERAGETARGETS == "80, 0, 0"
    Default.CORBETURA_METHODCOVERAGETARGETS.getClass().toString() == "class java.lang.String"

    when:
    Default.CORBETURA_METHODCOVERAGETARGETS = newValue
    then:
    notThrown(ReadOnlyPropertyException)
    Default.CORBETURA_METHODCOVERAGETARGETS == newValue
  }

  def "verify CORBETURA_ONLYSTABLE value and is static and not final"() {
    expect:
    Default.CORBETURA_ONLYSTABLE == false
    Default.CORBETURA_ONLYSTABLE.getClass().toString() == "class java.lang.Boolean"

    when:
    Default.CORBETURA_ONLYSTABLE = true
    then:
    notThrown(ReadOnlyPropertyException)
    Default.CORBETURA_ONLYSTABLE == true
  }

  def "verify CORBETURA_SOURCEENCODING value and is static and not final"() {
    expect:
    Default.CORBETURA_SOURCEENCODING == "ASCII"
    Default.CORBETURA_SOURCEENCODING.getClass().toString() == "class java.lang.String"

    when:
    Default.CORBETURA_SOURCEENCODING = newValue
    then:
    notThrown(ReadOnlyPropertyException)
    Default.CORBETURA_SOURCEENCODING == newValue
  }

  def "verify CORBETURA_ZOOMCOVERAGECHART value and is static and not final"() {
    expect:
    Default.CORBETURA_ZOOMCOVERAGECHART == false
    Default.CORBETURA_ZOOMCOVERAGECHART.getClass().toString() == "class java.lang.Boolean"

    when:
    Default.CORBETURA_ZOOMCOVERAGECHART = true
    then:
    notThrown(ReadOnlyPropertyException)
    Default.CORBETURA_ZOOMCOVERAGECHART == true
  }

  def "verify CRX_INSTALL value and is static and not final"() {
    expect:
    Default.CRX_INSTALL == true
    Default.CRX_INSTALL.getClass().toString() == "class java.lang.Boolean"

    when:
    Default.CRX_INSTALL = false
    then:
    notThrown(ReadOnlyPropertyException)
    Default.CRX_INSTALL == false
  }

  def "verify CRX_ACHANDLING value and is static and not final"() {
    expect:
    Default.CRX_ACHANDLING == "IGNORE"
    Default.CRX_ACHANDLING.getClass().toString() == "class java.lang.String"

    when:
    Default.CRX_ACHANDLING = newValue
    then:
    notThrown(ReadOnlyPropertyException)
    Default.CRX_ACHANDLING == newValue
  }

  def "verify CRX_BEHAVIOR value and is static and not final"() {
    expect:
    Default.CRX_BEHAVIOR == "Overwrite"
    Default.CRX_BEHAVIOR.getClass().toString() == "class java.lang.String"

    when:
    Default.CRX_BEHAVIOR = newValue
    then:
    notThrown(ReadOnlyPropertyException)
    Default.CRX_BEHAVIOR == newValue
  }

  def "verify CRX_PACKAGEIDFILTERS value and is static and not final"() {
    expect:
    Default.CRX_PACKAGEIDFILTERS == ["**/*.zip"]
    Default.CRX_PACKAGEIDFILTERS.getClass().toString() == "class java.util.ArrayList"

    when:
    Default.CRX_PACKAGEIDFILTERS = ["**/*.tar"]
    then:
    notThrown(ReadOnlyPropertyException)
    Default.CRX_PACKAGEIDFILTERS == ["**/*.tar"]
  }

  def "verify CRX_BASEURL value and is static and not final"() {
    expect:
    Default.CRX_BASEURL == "http://localhost:4502"
    Default.CRX_BASEURL.getClass().toString() == "class java.lang.String"

    when:
    Default.CRX_BASEURL = newValue
    then:
    notThrown(ReadOnlyPropertyException)
    Default.CRX_BASEURL == newValue
  }

  def "verify BLUEMIX_API value and is static and not final"() {
    expect:
    Default.BLUEMIX_API == "https://api.kpsj001.us-west.bluemix.net"
    Default.BLUEMIX_API.getClass().toString() == "class java.lang.String"

    when:
    Default.BLUEMIX_API = newValue
    then:
    notThrown(ReadOnlyPropertyException)
    Default.BLUEMIX_API == newValue
  }

  def "verify BLUEMIX_ORG value and is static and not final"() {
    expect:
    Default.BLUEMIX_ORG == "CDTS-ADS-SJ001"
    Default.BLUEMIX_ORG.getClass().toString() == "class java.lang.String"

    when:
    Default.BLUEMIX_ORG = newValue
    then:
    notThrown(ReadOnlyPropertyException)
    Default.BLUEMIX_ORG == newValue
  }

  def "verify BLUEMIX_PATH value and is static and not final"() {
    expect:
    Default.BLUEMIX_PATH == "target/*.war"
    Default.BLUEMIX_PATH.getClass().toString() == "class java.lang.String"

    when:
    Default.BLUEMIX_PATH = newValue
    then:
    notThrown(ReadOnlyPropertyException)
    Default.BLUEMIX_PATH == newValue
  }

  def "verify BLUEMIX_ANGULAR_PATH value and is static and not final"() {
    expect:
    Default.BLUEMIX_ANGULAR_PATH == "dist/**"
    Default.BLUEMIX_ANGULAR_PATH.getClass().toString() == "class java.lang.String"

    when:
    Default.BLUEMIX_ANGULAR_PATH = newValue
    then:
    notThrown(ReadOnlyPropertyException)
    Default.BLUEMIX_ANGULAR_PATH == newValue
  }

  def "verify UCD_FILETYPE value and is static and not final"() {
    expect:
    Default.UCD_FILETYPE == "Artifact"
    Default.UCD_FILETYPE.getClass().toString() == "class java.lang.String"

    when:
    Default.UCD_FILETYPE = newValue
    then:
    notThrown(ReadOnlyPropertyException)
    Default.UCD_FILETYPE == newValue
  }

  def "verify UCD_SITENAME value and is static and not final"() {
    expect:
    Default.UCD_SITENAME == "UCD-Devops.kp.org"
    Default.UCD_SITENAME.getClass().toString() == "class java.lang.String"

    when:
    Default.UCD_SITENAME = newValue
    then:
    notThrown(ReadOnlyPropertyException)
    Default.UCD_SITENAME == newValue
  }

  def "verify UCD_PROCESS_NATIONAL value and is static and not final"() {
    expect:
    Default.UCD_PROCESS_NATIONAL == "Deploy"
    Default.UCD_PROCESS_NATIONAL.getClass().toString() == "class java.lang.String"

    when:
    Default.UCD_PROCESS_NATIONAL = newValue
    then:
    notThrown(ReadOnlyPropertyException)
    Default.UCD_PROCESS_NATIONAL == newValue
  }

  def "verify UCD_PROCESS_REGIONAL value and is static and not final"() {
    expect:
    Default.UCD_PROCESS_REGIONAL == "DeployApplication_"
    Default.UCD_PROCESS_REGIONAL.getClass().toString() == "class java.lang.String"

    when:
    Default.UCD_PROCESS_REGIONAL = newValue
    then:
    notThrown(ReadOnlyPropertyException)
    Default.UCD_PROCESS_REGIONAL == newValue
  }

  def "verify UCD_BASEDIR value and is static and not final"() {
    expect:
    Default.UCD_BASEDIR == "target"
    Default.UCD_BASEDIR.getClass().toString() == "class java.lang.String"

    when:
    Default.UCD_BASEDIR = newValue
    then:
    notThrown(ReadOnlyPropertyException)
    Default.UCD_BASEDIR == newValue
  }

  def "verify UCD_ANGULAR_BASEDIR value and is static and not final"() {
    expect:
    Default.UCD_ANGULAR_BASEDIR == "dist"
    Default.UCD_ANGULAR_BASEDIR.getClass().toString() == "class java.lang.String"

    when:
    Default.UCD_ANGULAR_BASEDIR = newValue
    then:
    notThrown(ReadOnlyPropertyException)
    Default.UCD_ANGULAR_BASEDIR == newValue
  }

  def "verify UCD_INCLUDE value and is static and not final"() {
    expect:
    Default.UCD_INCLUDE == "*"
    Default.UCD_INCLUDE.getClass().toString() == "class java.lang.String"

    when:
    Default.UCD_INCLUDE = newValue
    then:
    notThrown(ReadOnlyPropertyException)
    Default.UCD_INCLUDE == newValue
  }

  def "verify UCD_EXCLUDE value and is static and not final"() {
    expect:
    Default.UCD_EXCLUDE == ""
    Default.UCD_EXCLUDE.getClass().toString() == "class java.lang.String"

    when:
    Default.UCD_EXCLUDE = newValue
    then:
    notThrown(ReadOnlyPropertyException)
    Default.UCD_EXCLUDE == newValue
  }

  def "verify UCD_PUSHPROPERTIES value and is static and not final"() {
    expect:
    Default.UCD_PUSHPROPERTIES == "jenkins.server=Local\njenkins.reviewed=false"
    Default.UCD_PUSHPROPERTIES.getClass().toString() == "class java.lang.String"

    when:
    Default.UCD_PUSHPROPERTIES = newValue
    then:
    notThrown(ReadOnlyPropertyException)
    Default.UCD_PUSHPROPERTIES == newValue
  }

  def "verify UCD_PUSHINCREMENTAL value and is static and not final"() {
    expect:
    Default.UCD_PUSHINCREMENTAL == false
    Default.UCD_PUSHINCREMENTAL.getClass().toString() == "class java.lang.Boolean"

    when:
    Default.UCD_PUSHINCREMENTAL = true
    then:
    notThrown(ReadOnlyPropertyException)
    Default.UCD_PUSHINCREMENTAL == true
  }

  def "verify UCD_DEPLOYONLYCHANGED value and is static and not final"() {
    expect:
    Default.UCD_DEPLOYONLYCHANGED == false
    Default.UCD_DEPLOYONLYCHANGED.getClass().toString() == "class java.lang.Boolean"

    when:
    Default.UCD_DEPLOYONLYCHANGED = true
    then:
    notThrown(ReadOnlyPropertyException)
    Default.UCD_DEPLOYONLYCHANGED == true
  }

  def "verify MAVENGOALS value and is static and not final"() {
    expect:
    Default.MAVENGOALS == "clean install -U"
    Default.MAVENGOALS.getClass().toString() == "class java.lang.String"

    when:
    Default.MAVENGOALS = newValue
    then:
    notThrown(ReadOnlyPropertyException)
    Default.MAVENGOALS == newValue
  }

  def "verify GRADLETASKS value and is static and not final"() {
    expect:
    Default.GRADLETASKS == "bootRepackage"
    Default.GRADLETASKS.getClass().toString() == "class java.lang.String"

    when:
    Default.GRADLETASKS = newValue
    then:
    notThrown(ReadOnlyPropertyException)
    Default.GRADLETASKS == newValue
  }

  def "verify MSTARGET value and is static and not final"() {
    expect:
    Default.MSTARGET == "Rebuild"
    Default.MSTARGET.getClass().toString() == "class java.lang.String"

    when:
    Default.MSTARGET = newValue
    then:
    notThrown(ReadOnlyPropertyException)
    Default.MSTARGET == newValue
  }

  def "verify MSBUILD_VERBOSITY value and is static and not final"() {
    expect:
    Default.MSBUILD_VERBOSITY == "normal"
    Default.MSBUILD_VERBOSITY.getClass().toString() == "class java.lang.String"

    when:
    Default.MSBUILD_VERBOSITY = newValue
    then:
    notThrown(ReadOnlyPropertyException)
    Default.MSBUILD_VERBOSITY == newValue
  }

  def "verify NUNIT_RESULT value and is static and not final"() {
    expect:
    Default.NUNIT_RESULT == "TestResult.xml"
    Default.NUNIT_RESULT.getClass().toString() == "class java.lang.String"

    when:
    Default.NUNIT_RESULT = newValue
    then:
    notThrown(ReadOnlyPropertyException)
    Default.NUNIT_RESULT == newValue
  }
}
