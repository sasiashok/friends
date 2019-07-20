package org.kp.constants

import spock.lang.Specification

class ConstantSpec extends Specification {
  
  static String newValue = "new-value"

  def "verify MAVENTOOL value and is static and final"() {
    expect:
    Constant.MAVENTOOL.getClass().toString() == "class java.lang.String"
    
    when:
     Constant.MAVENTOOL = newValue
    then:
    thrown(ReadOnlyPropertyException)
     Constant.MAVENTOOL == "Maven353"
  }

  def "verify GRADLETOOL value and is static and final"() {
    expect:
    Constant.GRADLETOOL.getClass().toString() == "class java.lang.String"
    
    when:
     Constant.GRADLETOOL = newValue
    then:
    thrown(ReadOnlyPropertyException)
     Constant.GRADLETOOL == "Gradle46"
  }

  def "verify NODEJSTOOL value and is static and final"() {
    expect:
    Constant.NODEJSTOOL.getClass().toString() == "class java.lang.String"
    
    when:
     Constant.NODEJSTOOL = newValue
    then:
    thrown(ReadOnlyPropertyException)
     Constant.NODEJSTOOL == "NodeJS89"
  }

  def "verify MSBUILDTOOL value and is static and final"() {
    expect:
    Constant.MSBUILDTOOL.getClass().toString() == "class java.lang.String"
    
    when:
     Constant.MSBUILDTOOL = newValue
    then:
    thrown(ReadOnlyPropertyException)
     Constant.MSBUILDTOOL == "MSBuild15"
  }
  def "verify MSSONARTOOL value and is static and final"() {
    expect:
    Constant.MSSONARTOOL.getClass().toString() == "class java.lang.String"
    
    when:
     Constant.MSSONARTOOL = newValue
    then:
    thrown(ReadOnlyPropertyException)
     Constant.MSSONARTOOL == "MSSonar"
  }

  def "verify MSCOVERAGETOOL value and is static and final"() {
    expect:
    Constant.MSCOVERAGETOOL.getClass().toString() == "class java.lang.String"
    
    when:
     Constant.MSCOVERAGETOOL = newValue
    then:
    thrown(ReadOnlyPropertyException)
     Constant.MSCOVERAGETOOL == "OpenCover"
  }

  def "verify NUGETTOOL value and is static and final"() {
    expect:
    Constant.NUGETTOOL.getClass().toString() == "class java.lang.String"
    
    when:
     Constant.NUGETTOOL = newValue
    then:
    thrown(ReadOnlyPropertyException)
     Constant.NUGETTOOL == "Nuget"
  }

  def "verify NUNITTOOL value and is static and final"() {
    expect:
    Constant.NUNITTOOL.getClass().toString() == "class java.lang.String"
    
    when:
     Constant.NUNITTOOL = newValue
    then:
    thrown(ReadOnlyPropertyException)
     Constant.NUNITTOOL == "NUnit3"
  }

  def "verify MAVENJUNIT value and is static and final"() {
    expect:
    Constant.MAVENJUNIT.getClass().toString() == "class java.lang.String"
    
    when:
     Constant.MAVENJUNIT = newValue
    then:
    thrown(ReadOnlyPropertyException)
     Constant.MAVENJUNIT == "**/surefire-reports/**/*.xml"
  }

  def "verify GRADLEJUNIT value and is static and final"() {
    expect:
    Constant.GRADLEJUNIT.getClass().toString() == "class java.lang.String"
    
    when:
     Constant.GRADLEJUNIT = newValue
    then:
    thrown(ReadOnlyPropertyException)
     Constant.GRADLEJUNIT == "**/test-results/**/*.xml"
  }

  def "verify ANGULARJUNIT value and is static and final"() {
    expect:
    Constant.ANGULARJUNIT.getClass().toString() == "class java.lang.String"
    
    when:
     Constant.ANGULARJUNIT = newValue
    then:
    thrown(ReadOnlyPropertyException)
     Constant.ANGULARJUNIT == "**/karma-results.xml"
  }

  def "verify ANGULARTESTPARAMS value and is static and final"() {
    expect:
    Constant.NPMTESTPARAMS.getClass().toString() == "class java.lang.String"
    
    when:
     Constant.NPMTESTPARAMS = newValue
    then:
    thrown(ReadOnlyPropertyException)
     Constant.NPMTESTPARAMS == "test --code-coverage"
  }

  def "verify TESTGOAL value and is static and final"() {
    expect:
    Constant.TESTGOAL.getClass().toString() == "class java.lang.String"
    
    when:
     Constant.TESTGOAL = newValue
    then:
    thrown(ReadOnlyPropertyException)
     Constant.TESTGOAL == "test"
  }

  def "verify ARTIFACTORY_SERVER value and is static and final"() {
    expect:
    Constant.ARTIFACTORY_SERVER.getClass().toString() == "class java.lang.String"
    
    when:
     Constant.ARTIFACTORY_SERVER = newValue
    then:
    thrown(ReadOnlyPropertyException)
     Constant.ARTIFACTORY_SERVER == "ART_JENKINS"
  }

  def "verify ARTIFACTORY_DEPLOYER_RELEASE value and is static and final"() {
    expect:
    Constant.ARTIFACTORY_DEPLOYER_RELEASE.getClass().toString() == "class java.lang.String"
    
    when:
     Constant.ARTIFACTORY_DEPLOYER_RELEASE = newValue
    then:
    thrown(ReadOnlyPropertyException)
     Constant.ARTIFACTORY_DEPLOYER_RELEASE == "libs-release-local"
  }

  def "verify ARTIFACTORY_DEPLOYER_SNAPSHOT value and is static and final"() {
    expect:
    Constant.ARTIFACTORY_DEPLOYER_SNAPSHOT.getClass().toString() == "class java.lang.String"

    when:
     Constant.ARTIFACTORY_DEPLOYER_SNAPSHOT = newValue
    then:
    thrown(ReadOnlyPropertyException)
     Constant.ARTIFACTORY_DEPLOYER_SNAPSHOT == "libs-snapshot-local"
  }

  def "verify ARTIFACTORY_DEPLOYABLE_RELEASE value and is static and final"() {
    expect:
    Constant.ARTIFACTORY_DEPLOYABLE_RELEASE.getClass().toString() == "class java.lang.String"
    
    when:
     Constant.ARTIFACTORY_DEPLOYABLE_RELEASE = newValue
    then:
    thrown(ReadOnlyPropertyException)
     Constant.ARTIFACTORY_DEPLOYABLE_RELEASE == "deploy-zip-releases"
  }

  def "verify ARTIFACTORY_DEPLOYABLE_SNAPSHOT value and is static and final"() {
    expect:
    Constant.ARTIFACTORY_DEPLOYABLE_SNAPSHOT.getClass().toString() == "class java.lang.String"
    
    when:
     Constant.ARTIFACTORY_DEPLOYABLE_SNAPSHOT = newValue
    then:
    thrown(ReadOnlyPropertyException)
     Constant.ARTIFACTORY_DEPLOYABLE_SNAPSHOT == "deploy-zip-snapshots"
  }

  def "verify ARTIFACTORY_DEPLOYABLE_TEST value and is static and final"() {
    expect:
    Constant.ARTIFACTORY_DEPLOYABLE_TEST.getClass().toString() == "class java.lang.String"
    
    when:
     Constant.ARTIFACTORY_DEPLOYABLE_TEST = newValue
    then:
    thrown(ReadOnlyPropertyException)
     Constant.ARTIFACTORY_DEPLOYABLE_TEST == "deploy-zips-feature-snapshots"
  }

  def "verify ARTIFACTORY_RESOLVER_RELEASE value and is static and final"() {
    expect:
    Constant.ARTIFACTORY_RESOLVER_RELEASE.getClass().toString() == "class java.lang.String"
    
    when:
     Constant.ARTIFACTORY_RESOLVER_RELEASE = newValue
    then:
    thrown(ReadOnlyPropertyException)
     Constant.ARTIFACTORY_RESOLVER_RELEASE == "libs-release"
  }

  def "verify ARTIFACTORY_RESOLVER_SNAPSHOT value and is static and final"() {
    expect:
    Constant.ARTIFACTORY_RESOLVER_SNAPSHOT.getClass().toString() == "class java.lang.String"
    
    when:
     Constant.ARTIFACTORY_RESOLVER_SNAPSHOT = newValue
    then:
    thrown(ReadOnlyPropertyException)
     Constant.ARTIFACTORY_RESOLVER_SNAPSHOT == "libs-snapshot"
  }

  def "verify ARTIFACTORY_NUGET_LOCAL value and is static and final"() {
    expect:
    Constant.ARTIFACTORY_NUGET_LOCAL.getClass().toString() == "class java.lang.String"
    
    when:
     Constant.ARTIFACTORY_NUGET_LOCAL = newValue
    then:
    thrown(ReadOnlyPropertyException)
     Constant.ARTIFACTORY_NUGET_LOCAL == "nuget-local-repo"
  }

  def "verify ARTIFACTORY_NPM_LOCAL_SNAPSHOT value and is static and final"() {
    expect:
    Constant.ARTIFACTORY_NPM_LOCAL_SNAPSHOT.getClass().toString() == "class java.lang.String"

    when:
    Constant.ARTIFACTORY_NPM_LOCAL_SNAPSHOT = newValue
    then:
    thrown(ReadOnlyPropertyException)
    Constant.ARTIFACTORY_NPM_LOCAL_SNAPSHOT == "npm-snapshot-local"
  }

  def "verify ARTIFACTORY_NPM_LOCAL_RELEASE value and is static and final"() {
    expect:
    Constant.ARTIFACTORY_NPM_LOCAL_RELEASE.getClass().toString() == "class java.lang.String"

    when:
    Constant.ARTIFACTORY_NPM_LOCAL_RELEASE = newValue
    then:
    thrown(ReadOnlyPropertyException)
    Constant.ARTIFACTORY_NPM_LOCAL_RELEASE == "npm-release-local"
  }

  def "verify ARTIFACTORY_DATEFORMAT value and is static and final"() {
    expect:
    Constant.ARTIFACTORY_DATEFORMAT.getClass().toString() == "class java.lang.String"
    
    when:
     Constant.ARTIFACTORY_DATEFORMAT = newValue
    then:
    thrown(ReadOnlyPropertyException)
     Constant.ARTIFACTORY_DATEFORMAT == "yyyyMMdd.HHmmss"
  }

  def "verify DEPLOYMENT_CONFIG_SERVICE_REFRESH value and is static and final"() {
    expect:
    Constant.DEPLOYMENT_CONFIG_SERVICE_REFRESH.getClass().toString() == "class java.lang.String"
    
    when:
    Constant.DEPLOYMENT_CONFIG_SERVICE_REFRESH = newValue
    then:
    thrown(ReadOnlyPropertyException)
    Constant.DEPLOYMENT_CONFIG_SERVICE_REFRESH == "configRefresh"
  }

  def "verify DEPLOYMENT_CONFIG_SERVICE_PAASENCRYPT value and is static and final"() {
    expect:
    Constant.DEPLOYMENT_CONFIG_SERVICE_PAASENCRYPT.getClass().toString() == "class java.lang.String"
    
    when:
    Constant.DEPLOYMENT_CONFIG_SERVICE_PAASENCRYPT = newValue
    then:
    thrown(ReadOnlyPropertyException)
    Constant.DEPLOYMENT_CONFIG_SERVICE_PAASENCRYPT == "paasEncrypt"
  }

  def "verify DEPLOYMENT_CONFIG_SERVICE_URL_NONPROD value and is static and final"() {
    expect:
    Constant.DEPLOYMENT_CONFIG_SERVICE_URL_NONPROD.getClass().toString() == "class java.lang.String"

    when:
    Constant.DEPLOYMENT_CONFIG_SERVICE_URL_NONPROD = newValue
    then:
    thrown(ReadOnlyPropertyException)
    Constant.DEPLOYMENT_CONFIG_SERVICE_URL_NONPROD == "https://kpconfig-np.bmxp.appl.kp.org/kpconfig/"
  }

  def "verify DEPLOYMENT_CONFIG_SERVICE_URL_PROD value and is static and final"() {
    expect:
    Constant.DEPLOYMENT_CONFIG_SERVICE_URL_PROD.getClass().toString() == "class java.lang.String"

    when:
    Constant.DEPLOYMENT_CONFIG_SERVICE_URL_PROD = newValue
    then:
    thrown(ReadOnlyPropertyException)
    Constant.DEPLOYMENT_CONFIG_SERVICE_URL_PROD == "https://kpconfig.bmxp.appl.kp.org/kpconfig/"
  }

  def "verify SONAR_SERVER value and is static and final"() {
    expect:
    Constant.SONAR_SERVER.getClass().toString() == "class java.lang.String"
    
    when:
     Constant.SONAR_SERVER = newValue
    then:
    thrown(ReadOnlyPropertyException)
     Constant.SONAR_SERVER == "Sonar Server"
  }

  def "verify SONAR_SCANNER value and is static and final"() {
    expect:
    Constant.SONAR_SCANNER.getClass().toString() == "class java.lang.String"
    
    when:
     Constant.SONAR_SCANNER = newValue
    then:
    thrown(ReadOnlyPropertyException)
     Constant.SONAR_SCANNER == "sonar-scanner"
  }

  def "verify NEXUS_IQ_CREDENTIALS value and is static and final"() {
    expect:
    Constant.NEXUS_IQ_CREDENTIALS.getClass().toString() == "class java.lang.String"
    
    when:
     Constant.NEXUS_IQ_CREDENTIALS = newValue
    then:
    thrown(ReadOnlyPropertyException)
     Constant.NEXUS_IQ_CREDENTIALS == "svc-nexus-iq"
  }

  def "verify NEXUS_IQ_SERVER value and is static and final"() {
    expect:
    Constant.NEXUS_IQ_SERVER.getClass().toString() == "class java.lang.String"
    
    when:
     Constant.NEXUS_IQ_SERVER = newValue
    then:
    thrown(ReadOnlyPropertyException)
     Constant.NEXUS_IQ_SERVER == "https://componentsecurity.appsec.kp.org:8443"
  }

  def "verify JDK_WIN value and is static and final"() {
    expect:
    Constant.JDK_WIN.getClass().toString() == "class java.lang.String"
    
    when:
     Constant.JDK_WIN = newValue
    then:
    thrown(ReadOnlyPropertyException)
     Constant.JDK_WIN == "JDK-WIN"
  }

  def "verify ELASTICSEARCH_URL value and is static and final"() {
    expect:
    Constant.ELASTICSEARCH_URL.getClass().toString() == "class java.lang.String"
    
    when:
     Constant.ELASTICSEARCH_URL = newValue
    then:
    thrown(ReadOnlyPropertyException)
     Constant.ELASTICSEARCH_URL == "http://cskpcloudxp2771.cloud.kp.org:9200"
  }

  def "verify DATEFORMAT value and is static and final"() {
    expect:
    Constant.DATEFORMAT.getClass().toString() == "class java.lang.String"
    
    when:
     Constant.DATEFORMAT = newValue
    then:
    thrown(ReadOnlyPropertyException)
     Constant.DATEFORMAT == "yyyyMMddHHmm"
  }
}
