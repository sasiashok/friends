package org.kp.utils

import org.kp.constants.*

/**
 * Clones all build repos and returns buildRepos array
 * @return array of buildRepos meta-data
 */
def clone() {
  stage(Stage.CHECKOUT) {
    CommonUtils commonUtils = new CommonUtils()
    def buildDependencies = commonUtils.getParamValue("buildDependencies")
    def buildParams = commonUtils.getParamValue("buildParams")
    def buildRepos = []
    def repoCount = 1
    buildDependencies.each { dependency ->
      dir("repo"+repoCount) {
        cloneSingleRepo(dependency)
        def dependencyBuildParams = CommonUtils.getKeyValue(dependency, "buildParams")
        def applicationDir = pwd()
        
        String applicationType = new ApplicationUtils().getApplicationType(applicationDir)
        buildRepos.push([directory: "repo"+repoCount, buildParams: dependencyBuildParams, appType: applicationType])
        repoCount++
      }
    }

    if(buildDependencies == null || repoCount == 1) {
      cloneCurrentRepo()
    } else {
      dir("repo"+repoCount) {
        cloneCurrentRepo()
      }
    }
    buildRepos.push([directory: env.APPLICATION_DIR, buildParams: buildParams, appType: BuildData.instance.appType])
    BuildData.instance.buildRepos = buildRepos
    return buildRepos
  }
}

/**
 * Clones a single repository
 * @param paramSet repository parameters
 * @return
 */
def cloneSingleRepo(def paramSet) {
  String credentialsId = getCredentialsId()
  String url = CommonUtils.getKeyValue(paramSet, "url")
  String branch = CommonUtils.getKeyValue(paramSet, "branch", Default.BRANCH)
  def sameBranch = CommonUtils.getKeyValue(paramSet, "sameBranch", false)
  boolean failMissing = CommonUtils.getKeyValue(paramSet, "failMissing", false)

  def targetBranches = [branch, Default.BRANCH]
  if(failMissing) {
    targetBranches = [branch]
  }
  
  if(url.startsWith("http:")) {
    url = url.replace("http:", "https:")
  }

  if(sameBranch && env.BRANCH_NAME != null) {
    branch = env.BRANCH_NAME
  }

  try {
    def scmObj = resolveScm source: [$class: 'GitSCMSource',
         credentialsId: credentialsId,
         id: '_',
         gitTool: 'Git',
         remote: url,
         traits: [[$class: 'jenkins.plugins.git.traits.BranchDiscoveryTrait'],[$class: 'CloneOptionTrait', extension: [noTags: true, reference: '', shallow: true]]]],
         targets: targetBranches
    checkout scmObj
  } catch (e) {
    echo "ℹ️ [INFO] Falling back to simplified checkout"
    git branch: branch, credentialsId: credentialsId, url: url
  }
}

/**
 * Clones the main application repository
 * @param applicationType application type if application type is known
 * @return
 */
def cloneCurrentRepo(String applicationType = null) {
  checkout scm
  if(env.BRANCH_NAME == null) {
    env.BRANCH_NAME = scm.GIT_BRANCH
  }
  def applicationDir = pwd()
  env.APPLICATION_DIR = applicationDir.toString().trim()
  if(applicationType == null) {
    BuildData.instance.appType = new ApplicationUtils().getApplicationType()
  } else {
    BuildData.instance.appType = applicationType
  }
  new CommonUtils().setupAppEnvironment()
}

/**
 * Tag's a given repository
 * @param tag tag parameters
 * @param repo repo url to be tagged
 * @param applicationDir directory of application where repo was checked out
 * @return
 */
def tagBuild(def tag, String repo, String applicationDir = env.APPLICATION_DIR) {
  CommonUtils commonUtils = new CommonUtils()
  String stageName = Stage.TAG_BUILD
  def tagInfix = CommonUtils.getKeyValue(tag, "tagInfix")
  def tagLabel = CommonUtils.getKeyValue(tag, "tagLabel", commonUtils.getTagLabel(tagInfix))
  if(applicationDir == env.APPLICATION_DIR) {
    env.APPLICATION_TAG = tagLabel
  }
  if(tagInfix != null) {
    stageName = "${stageName}-${tagInfix}"
  }
  try {
    stage (stageName) {
      dir(applicationDir) {
        withCredentials([usernamePassword(credentialsId: getCredentialsId(), passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
          String encoded_username = java.net.URLEncoder.encode(GIT_USERNAME, "UTF-8")
          String encoded_password = java.net.URLEncoder.encode(GIT_PASSWORD, "UTF-8")

          wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [[password: encoded_password]]]) {
            wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [[password: encoded_username]]]) {
              String url = getTagUrl(repo, encoded_username, encoded_password)
              kpSh """
                git config user.email "${getCredentialsId()}@kp.org"
                git config user.name "Jenkins"
                git tag -a ${tagLabel} -m 'Tag by Jenkins'
                git push ${url} ${tagLabel}
              """
            }
          }
        }
      }
    }
  } catch (e) {
    error "Git Tagging failed at : " + stageName + " with error: " + e.getMessage()
  }
  return tagLabel
}

/**
 * Returns repo url with username and password
 * @param repo Original repo url
 * @param encoded_username git username of repo
 * @param encoded_password git password of repo
 * @return repo url with username and password
 */
def getTagUrl(String repo, String encoded_username, String encoded_password) {
  if(repo.contains("//${encoded_username}@") && repo.startsWith("http")) {
    repo = repo.replace("//${encoded_username}@", "//${encoded_username}:${encoded_password}@")
  } else {
    if(repo.startsWith("http")) {
      repo = repo.replace("//", "//${encoded_username}:${encoded_password}@")
    }
  }
  return repo
}

/**
 * Get Jenkins credential Id used to checkout repo
 * @return checkout credentials id
 */
String getCredentialsId() {
  return scm.getUserRemoteConfigs()[0].getCredentialsId().toString().toLowerCase().trim()
}

/**
 * Determines if current branch is a PR
 * @return true | false if PR
 */
def isPR() {
  if(env.BRANCH_NAME != null && env.BRANCH_NAME.startsWith("PR-")) {
    return true
  }
  return false
}
