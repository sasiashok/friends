package org.kp.utils

import org.kp.constants.*
import org.apache.commons.lang.StringUtils

/**
 * Determines the application type for a given repo.
 * @param repo Directory to where a given repo was checked out
 * @return application type of repo
 */
def getApplicationType(String repo = env.APPLICATION_DIR) {
  dir(repo) {
    isMaven = fileExists AppType.MARKER_MAVEN
    isGradle = fileExists AppType.MARKER_GRADLE
    isAEM = fileExists AppType.MARKER_AEM
    isAngular = fileExists AppType.MARKER_ANGULAR
    isAngularPost6 = fileExists AppType.MARKER_ANGULAR_POST6
    isNodeJS = fileExists AppType.MARKER_NODEJS
    isPhp = fileExists AppType.MARKER_PHP
    def files = findFiles(glob: AppType.MARKER_DOTNET)
    isDotNet = false
    if(files.size() > 0) {
      isDotNet = true
    }
    if(isAEM && isMaven) {
      return AppType.AEM
    } else if(isMaven) {
      return AppType.MAVEN
    } else if(isGradle) {
      return AppType.GRADLE
    } else if(isDotNet) {
      return AppType.DOTNET
    } else if(isNodeJS && (isAngular || isAngularPost6)) {
      return AppType.ANGULAR
    } else if(isNodeJS) {
      return AppType.NODEJS
    } else if(isPhp) {
      return AppType.PHP
    } else {
      error Message.UNKNOWN_APP
      return AppType.OTHER
    }
  }
}

/**
 * Get Bitbucket project key
 * @return project key
 */
String getProjectKey() {
  return getRepoUrl().tokenize('/')[-2].split("\\.")[0].toString().toLowerCase().trim()
}

/**
 * Get Bitbucket repo name
 * @return repo name
 */
String getApplicationName() {
  return getRepoUrl().tokenize('/').last().split("\\.")[0].toString().toLowerCase().trim()
}

/**
 * Get Bitbucket repo url
 * @return repo url
 */
String getRepoUrl() {
  return scm.getUserRemoteConfigs()[0].getUrl().toString().toLowerCase().trim()
}

/**
 * Get Jenkins credential Id used to checkout repo
 * @return checkout credentials id
 */
String getCredentialsId() {
  return scm.getUserRemoteConfigs()[0].getCredentialsId().toString().toLowerCase().trim()
}

/**
 * Returns branch name without /
 * @return branch name
 */
def getCleanBranchName() {
  def branchName = ("${env.BRANCH_NAME}").replace("/","_")
  return branchName
}

/**
 * Returns :: separated application name and branch name
 * @return
 */
def getUnifiedAppName() {
  String unifiedAppName = BuildData.instance.unifiedAppName
  if(unifiedAppName == null) {
    unifiedAppName = getApplicationName()+"::"+getCleanBranchName()
    BuildData.instance.unifiedAppName = unifiedAppName
  }
  return unifiedAppName
}

/**
 * Get application version number
 * @return app version
 */
def getApplicationVersion() {
  String version = BuildData.instance.appVersion
  if (version == null) {
    switch (BuildData.instance.appType) {
      case AppType.DOTNET:
        dir(env.APPLICATION_DIR) {
          version = getDotNetAppVersion()
        }
        break
      case AppType.GRADLE:
        version = getGradleProperty('version')
        break
      case AppType.PHP:
        version = getAppMarkerFile()["sonar.projectVersion"]
        break
      default:
        version = getAppMarkerFile().version
        break
    }
    if(version == null) {
      error "Unable to determine application version"
    } else {
      version = version.replaceAll("'", "")
      BuildData.instance.appVersion = version
    }
  }
  return version
}

/**
 * Get property value from Gradle project.
 * @param prop property whose value is required
 * @return
 */
def getGradleProperty(String prop) {
  def propValue = null
  def buildProps = getAppMarkerFile()
  propValue = buildProps[prop]
  if(propValue == null) {
    def gradlePropsCheck = fileExists 'gradle.properties'
    if(gradlePropsCheck) {
      def gradleProps = readProperties file: 'gradle.properties'
      propValue = gradleProps[prop]
    }
  }
  if(propValue == null) {
    def settingsCheck = fileExists 'settings.gradle'
    if(settingsCheck) {
      def settingsProps = readProperties file: 'settings.gradle'
      propValue = settingsProps[prop]
    }
  }
  if(propValue == null) {
    error "Not able to find property ${prop} in build.gradle, gradle.properties, settings.gradle"
  } else {
    return propValue
  }
}

/**
 * Get application version number for .NET application
 * @return app version
 */
def getDotNetAppVersion() {
  String assemblyInfo = ""
  def itemGroups = getAppMarkerFile().ItemGroup
  outerloop:
  for (int i = 0; i < itemGroups.size(); i++) {
    def compiles = itemGroups.getAt(i).Compile
    if (compiles != null && compiles.size() > 0) {
      for (j = 0; j < compiles.size(); j++) {
        String include = compiles.getAt(j)['@Include']
        if (include != null && include.contains('AssemblyInfo')) {
          assemblyInfo = include
          break outerloop
        }
      }
    }
  }
  assemblyInfo = assemblyInfo.replace('\\', '\\\\')
  String assemblyInfoContent = readFile file: assemblyInfo
  def contentLines = assemblyInfoContent.tokenize("\n")
  String version = ""
  for (int i = 0; i < contentLines.size(); i++) {
    String line = contentLines.get(i)
    if (line.startsWith("<Assembly: AssemblyVersion(")) {
      version = line.substring(line.indexOf("(") + 2, line.indexOf(")") - 1)
      break
    }
  }
  return version
}

/**
 * Returns semantic style version number of app without any text
 * @return version number
 */
def getTrimmedApplicationVersion() {
  String version = getApplicationVersion()
  if(isSnapShot()) {
    version = version.replace("-SNAPSHOT", "")
  }
  return version
}

/**
 * Get application group id
 * @return groupdID
 */
def getApplicationGroupId() {
  String groupId = BuildData.instance.appGroupId
  if(groupId == null) {
    switch (BuildData.instance.appType) {
      case AppType.GRADLE:
        dir(env.APPLICATION_DIR) {
          groupId = getGradleProperty('group')
        }
        break
      default:
        groupId = getAppMarkerFile().groupId
        break
    }
    if(groupId == null) {
      error "Unable to determine application group ID"
    } else {
      groupId = groupId.replaceAll("'", "")
      BuildData.instance.appGroupId = groupId
    }
  }
  return groupId
}

/**
 * Get assembly name for .NET application
 * @return assembly name
 */
def getAssemblyName() {
  return getAppMarkerFile().PropertyGroup.AssemblyName.text().trim()
}

/**
 * Get component for application. Used for SonarQube API call.
 * @return component of application used in SonarQube
 */
def getApplicationComponent() {
  String component = ""
  switch(BuildData.instance.appType) {
    case AppType.DOTNET:
      component = getAssemblyName()
      break
    case AppType.ANGULAR:
    case AppType.NODEJS:
    case AppType.PHP:
      def appMarkerFile = readProperties file: 'sonar-project.properties'
      component = appMarkerFile['sonar.projectKey']
      break
    default:
      String groupId = getApplicationGroupId()
      String artifactId = getApplicationArtifactId()
      component = "${groupId}:${artifactId}"
      break
  }
  return component
}

/**
 * Get application artifact Id
 * @return artifactId
 */
String getApplicationArtifactId() {
  String artifactId = BuildData.instance.appArtifactId
  if(artifactId == null) {
    switch (BuildData.instance.appType) {
      case AppType.GRADLE:
        dir(env.APPLICATION_DIR) {
          artifactId = getGradleProperty('rootProject.name')
        }
        break
      default:
        artifactId = getAppMarkerFile().artifactId
        break
    }
    if(artifactId == null) {
      error "Unable to determine application artifact ID"
    } else {
      artifactId = artifactId.replaceAll("'", "")
      artifactId = artifactId.replaceAll("\"", "")
      BuildData.instance.appArtifactId = artifactId
    }
  }
  return artifactId
}

/**
 * Check if version of the app is a snapshot version.
 * @return true | false if app is snapshot
 */
def isSnapShot() {
  return (getApplicationVersion().contains("-SNAPSHOT")) ? true : false
}

/**
 * Returns the key marker file for a given app type that will contain most app meta-data
 * @return content of marker file
 */
def getAppMarkerFile() {
  def appMarkerFile = BuildData.instance.appMarkerFile
  if (appMarkerFile == null) {
    dir(env.APPLICATION_DIR) {
      switch (BuildData.instance.appType) {
        case AppType.AEM:
        case AppType.MAVEN:
          appMarkerFile = readMavenPom file: 'pom.xml'
          break
        case AppType.GRADLE:
          appMarkerFile = readProperties file: 'build.gradle'
          break
        case AppType.DOTNET:
          appMarkerFile = getProjectFile()
          break
        case AppType.ANGULAR:
        case AppType.NODEJS:
          appMarkerFile = readJSON file: 'package.json'
          break
        case AppType.PHP:
          appMarkerFile = readProperties file: 'sonar-project.properties'
          break
        default:
          error Message.UNKNOWN_APP
          break
      }
    }
    BuildData.instance.appMarkerFile = appMarkerFile
  }
  return appMarkerFile
}

/**
 * Returns main project file content for .NET application
 * @return project file content
 */
def getProjectFile() {
  def slnFiles = findFiles(glob: '*.sln')
  if (slnFiles.size() > 0) {
    def slnFile = readFile file: "${slnFiles[0].path}"
    def slnFileContent = slnFile.tokenize("\n")
    String projFileName = ""

    for (int i = 0; i < slnFileContent.size(); i++) {
      String line = slnFileContent.get(i)
      if ((line.contains(".vbproj") || line.contains(".csbproj")) && !(line.contains("UnitTests"))) {
        if (line.contains(".vbproj")) {
          projFileName = StringUtils.substringBetween(line, ",", ".vbproj").trim().substring(1) + ".vbproj"
        } else {
          projFileName = StringUtils.substringBetween(line, ",", ".csbproj").trim().substring(1) + ".csproj"
        }
        break
      }
    }

    String projFile = readFile file: projFileName
    if (!projFile.startsWith("<?xml")) {
      projFile = projFile.substring(projFile.indexOf("<?xml"))
    }

    def proj = new XmlParser().parseText(projFile)
    return proj
  }
}

/**
 * Determine if a project is a genuine project and not a test project
 * @return true | false if project is genuine
 */
boolean isValidProject() {
  switch (getProjectKey()) {
    case "adt":
    case "adtp":
    case "test":
      return false
    default:
      return true
  }
}
