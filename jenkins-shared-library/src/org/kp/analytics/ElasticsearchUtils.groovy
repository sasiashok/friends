package org.kp.analytics

import groovy.json.JsonOutput
import org.kp.constants.Constant
import org.kp.utils.*

/**
 * Post data to elasticsearch
 * @param body JSON body to be posted on elasticsearch
 * @param index prefix of index on elasticsearch. Defaults to build.
 * @return
 */
def postData(String body, String index = "build") {
  String ELASTICSEARCH_URL = Constant.ELASTICSEARCH_URL
  String project = new ApplicationUtils().getProjectKey()
  httpRequest quiet: true,
    authentication: 'es-writer',
    responseHandle: 'NONE',
    consoleLogResponseBody: true,
    contentType: 'APPLICATION_JSON',
    httpMode: 'POST',
    ignoreSslErrors: true,
    requestBody: body,
    timeout: 60,
    url: ELASTICSEARCH_URL + "/${index}-${project}/metrics/"
}

/**
 * Post build data to elasticsearch
 * @return
 */
def postBuildData() {
  try {
    postData(new BuildListener().getBuild(currentBuild, BuildData.instance.initialEnv, getAppParams()))
  } catch (e) {
    echo "ℹ️ [INFO] Not able to post build metrics"
  }
}

/**
 * Post generic data to elasticsearch
 * @param dataMap Map of data to be posted on elasticsearch
 * @param index Prefix of index to be stored in elasticsearch
 * @return
 */
def postGenericData(def dataMap, String index) {
  try {
    if(!BuildData.instance.isAdhocDeployment) {
      dataMap.put("environment", BuildData.instance.initialEnv)
    }
    dataMap.putAll(getAppParams())
    dataMap.put("timestamp", currentBuild.rawBuild.getTimestamp())
    postData(JsonOutput.toJson(dataMap).trim(), index)
  } catch (e) {
    echo "ℹ️ [INFO] Not able to post ${index} metrics"
  }
}

/**
 * Creates map of common application meta-data to be added to all data to be posted to elasticsearch
 * @return map of common application meta-data
 */
def getAppParams() {
  return [project: new ApplicationUtils().getProjectKey(), repository: new ApplicationUtils().getApplicationName(), appType: BuildData.instance.appType]
}
