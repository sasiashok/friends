# ADS Shared Library Change log

## 📦 1.3.2
- ✳️ General enhancements:
    - Hushing email notification errors.
    - Lowercasing deployEnv when querying stable meta-data for env

## 📦 1.3.1
- ✳️ General enhancements:
    - Providing support for custom versions of tools

## 📦 1.3.0
-  🏗 🌟 Build technology enhancements:
    - Optimizing .Net build behaviour for disabled sonar
    - Enabling buildParams goalsTasks for Angular and NodeJS

## 📦 1.2.10
- ✳️ General enhancements:
    - Adding .git to exclude option in WhiteHat tarball step

## 📦 1.2.9
-🐛 Bug fixes:
    - Handling null values for default route key: hostName and domain

## 📦 1.2.8
-  🏗 🌟 Build technology enhancements:
    - Enhanced groupId, artifactId and version extraction for Gradle projects
    - Better error handling for missing groupId, artifactId and version
    - Timeout of 1 hour for unit tests and 2 hours for build and code analysis

## 📦 1.2.7
-🐛 Bug fixes:
    - appType default in setupAppEnvironment no longer reads default from Singleton property.

## 📦 1.2.6
-🖌 Git features:
    - Hide pipeline-configurations error
- 🚀 🌟 Deploy technology enhancements:
    - Provided support to survive Jenkins restart

## 📦 1.2.5
-  🏗 🌟 Build technology enhancements:
    - Added npm run build alternative for nodejs
- 🚀 🌟 Deploy technology enhancements:
    - Added Blue Green deployment

## 📦 1.2.4
- 🚀 🌟 Deploy technology enhancements:
    - Added support for nodejs deployment

## 📦 1.2.3
- 🖌 Git features:
    - added failMissing parameter to stop jobs if incorrect branch name

## 📦 1.2.2
- 🖌 Git features:
    - added failMissing parameter to stop jobs if incorrect branch name


## 📦 1.2.2
- 🚀 🌟 Deploy technology enhancements:
    - ADSDEVOPS-720 added start/stop/delete for bluemix services

## 📦 1.2.1
- 🔎 Quality gates:
    - Added code-coverage for nodejs projects

## 📦 1.2.0
- 🏗 🌟 Build technology enhancements:
    - ADSDEVOPS-715 NuGet restore support
    - ADSDEVOPS-716 Fix Gradle daemon issue
    - ADSDEVOPS-716 Fix Gradle emoji error on CJE
    
- 🚀 🌟 Deploy technology enhancements:
    - ADSDEVOPS-717 Added support for scaling in bluemix
    
- ✳️ General enhancements:
    - ADSDEVOPS-718 Converting library to Gradle project
    - ADSDEVOPS-718 Adding first set of unit tests
    - Adding .gitignore file
    - Correcting sonar-report template extension

## 📦 1.1.0
- 🏗 New Build technologies supported:
    - MSBuild / DotNet
    - Angular
    - NodeJS
    
- 🏗 🌟 Build technology enhancements:
    - Enforced dependency resolution for Java and NodeJS based from Artifactory
    - Publishing of built artifacts to Artifactory
    
- 🔎 Quality gates:
    - NUnit
    - Re-use of build tools for all automated tests
    - Publisher tool integration for automation reports. Following publishing tools are supported:
        - JUnit
        - TestNG
        - General Archive
        - Publish HTML

- 👮 Security Quality gates:
    - Nexus IQ scans and report
    - WhiteHat archiving
    
- 🚀 🌟 Deploy technology enhancements:
    - Download deployable artifact from Artifactory instead of stash
    - Ad-hoc deployment support
    - Config-refresh support for Bluemix
    - ucdFilePush and apicDeploy support for UCD
    - Handling pushFile once across all UCD environment deployments 
    - Roll-back support
    - Skipping environment support
     
- ⚙ Analytics:
    - Data for following pushed to Elasticsearch:
        - Deployments (National and Regional)
        - SonarQube measures
        - Nexus IQ alerts and issue counts

- 🖍 Refactoring:
    - Migration from common package to constants and utils
    - Split of Defaults to separate files with specific purposes
    - Migration of run time data storage to BuildData
    - Revised analytic structuring
    - SonarQube Report generation using template instead of String
        
## 📦 1.0.0

- 🎬 Initial Release
- 🏗 Build technologies supported:
    - Maven
    - Gradle
    - AEM
- 🔎 Quality gates:
    - SonarQube
    - JUnit
    - Cobertura
    - Script based automated tests
- 🚀 Deploy technologies supported:
    - Bluemix
    - CRX
    - UCD
- 🖌 Git features:
    - Support for multiple dependant repos
    - Git tagging
    - Cloning single repos for automated tests
- ⚙ Analytics:
    - Elasticsearch for build data