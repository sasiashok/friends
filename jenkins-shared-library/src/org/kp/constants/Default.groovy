package org.kp.constants

class Default {
  static String AGENT = "ads-agent"
  static String BRANCH = "master"

  static String DEPLOYMENT_PLATFORM = Platform.BLUEMIX
  static String DEPLOYMENT_ENVIRONMENT = "UNKNOWN"

  static String DEPLOYMENT_CONFIG_SERVICE_CONTENT_TYPE = "APPLICATION_JSON"
  static String DEPLOYMENT_CONFIG_SERVICE_HTTP_METHOD = "PUT"

  static Boolean CORBETURA_AUTOUPDATEHEALTH = false
  static Boolean CORBETURA_AUTOUPDATESTABILITY = false
  static String CORBETURA_COBERTURAREPORTFILE = "**/target/site/cobertura/coverage.xml"
  static String CORBETURA_ANGULAR_COBERTURAREPORTFILE = "coverage/cobertura-coverage.xml"
  static String CORBETURA_CONDITIONALCOVERAGETARGETS = "70, 0, 0"
  static Boolean CORBETURA_FAILUNHEALTHY = false
  static Boolean CORBETURA_FAILUNSTABLE = false
  static String CORBETURA_LINECOVERAGETARGETS = "80, 0, 0"
  static Integer CORBETURA_MAXNUMBEROFBUILDS = 0
  static String CORBETURA_METHODCOVERAGETARGETS = "80, 0, 0"
  static Boolean CORBETURA_ONLYSTABLE = false
  static String CORBETURA_SOURCEENCODING = "ASCII"
  static Boolean CORBETURA_ZOOMCOVERAGECHART = false

  static Boolean CRX_INSTALL = true
  static String CRX_ACHANDLING = "IGNORE"
  static String CRX_BEHAVIOR = "Overwrite"
  static List CRX_PACKAGEIDFILTERS = ["**/*.zip"]
  static String CRX_BASEURL = "http://localhost:4502"

  static String BLUEMIX_API = "https://api.kpsj001.us-west.bluemix.net"
  static String BLUEMIX_ORG = "CDTS-ADS-SJ001"
  static String BLUEMIX_PATH = "target/*.war"
  static String BLUEMIX_ANGULAR_PATH = "dist/**"

  static String UCD_FILETYPE = "Artifact"
  static String UCD_SITENAME = "UCD-Devops.kp.org"
  static String UCD_PROCESS_NATIONAL = "Deploy"
  static String UCD_PROCESS_REGIONAL = "DeployApplication_"
  static String UCD_BASEDIR = "target"
  static String UCD_ANGULAR_BASEDIR = "dist"
  static String UCD_INCLUDE = "*"
  static String UCD_EXCLUDE = ""
  static String UCD_PUSHPROPERTIES = "jenkins.server=Local\njenkins.reviewed=false"
  static Boolean UCD_PUSHINCREMENTAL = false
  static Boolean UCD_DEPLOYONLYCHANGED = false

  static String MAVENGOALS = "clean install -U"
  static String GRADLETASKS = "bootRepackage"

  static String MSTARGET = "Rebuild"
  static String MSBUILD_VERBOSITY = "normal"

  static String NUNIT_RESULT = "TestResult.xml"

  static String REMOTE_TEST_SERVER = "ADS-Jenkins"

  static boolean CONFIG_REFRESH_CREDENTIALS = false
  static String CONFIG_REFRESH_USER = ""
  static String CONFIG_REFRESH_PASSWORD = ""
}