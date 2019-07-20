package org.kp.constants

class Constant {
  static final String MAVENTOOL = "Maven353"
  static final String MAVENJUNIT = "**/surefire-reports/**/*.xml"

  static final String GRADLETOOL = "Gradle46"
  static final String GRADLEJUNIT = "**/test-results/**/*.xml"
  static final String GRADLESWITCHES = "--info --refresh-dependencies -Dorg.gradle.daemon=false"

  static final String NODEJSTOOL = "NodeJS89"
  static final String MSBUILDTOOL = "MSBuild15"
  static final String MSSONARTOOL = "MSSonar"
  static final String MSCOVERAGETOOL = "OpenCover"

  static final String NPMTESTPARAMS = "test --code-coverage"
  static final String ANGULARJUNIT = "**/karma-results.xml"

  static final String NUGETTOOL = "Nuget"
  static final String NUNITTOOL = "NUnit3"

  static final String TESTGOAL = "test"

  static final String ARTIFACTORY_SERVER = "ART_JENKINS"
  static final String ARTIFACTORY_DEPLOYER_RELEASE = "libs-release-local"
  static final String ARTIFACTORY_DEPLOYER_SNAPSHOT = "libs-snapshot-local"
  static final String ARTIFACTORY_DEPLOYABLE_RELEASE = "deploy-zip-releases"
  static final String ARTIFACTORY_DEPLOYABLE_SNAPSHOT = "deploy-zip-snapshots"
  static final String ARTIFACTORY_DEPLOYABLE_TEST = "deploy-zips-feature-snapshots"
  static final String ARTIFACTORY_RESOLVER_RELEASE = "libs-release"
  static final String ARTIFACTORY_RESOLVER_SNAPSHOT = "libs-snapshot"
  static final String ARTIFACTORY_DATEFORMAT = "yyyyMMdd.HHmmss"
  static final String ARTIFACTORY_NUGET_LOCAL = "nuget-local-repo"
  static final String ARTIFACTORY_NPM_LOCAL_RELEASE = "npm-release-local"
  static final String ARTIFACTORY_NPM_LOCAL_SNAPSHOT = "npm-snapshot-local"

  static final String NPMRC_FILE = "ads-npmrc"

  static final String SONAR_SERVER = "Sonar Server"
  static final String SONAR_URL = "https://sonar.ads.kp.org"
  static final String SONAR_SCANNER = "sonar-scanner"

  static final String NEXUS_IQ_CREDENTIALS = "svc-nexus-iq"
  static final String NEXUS_IQ_SERVER = "https://componentsecurity.appsec.kp.org:8443"

  static final String JDK_WIN = "JDK-WIN"

  static final String ELASTICSEARCH_URL = "http://cskpcloudxp2771.cloud.kp.org:9200"

  static final String DATEFORMAT = "yyyyMMddHHmm"

  static final String DEPLOYMENT_CONFIG_SERVICE_REFRESH = "configRefresh"
  static final String DEPLOYMENT_CONFIG_SERVICE_PAASENCRYPT = "paasEncrypt"
  static final String DEPLOYMENT_CONFIG_SERVICE_URL_NONPROD = "https://kpconfig-np.bmxp.appl.kp.org/kpconfig/"
  static final String DEPLOYMENT_CONFIG_SERVICE_URL_PROD = "https://kpconfig.bmxp.appl.kp.org/kpconfig/"
}