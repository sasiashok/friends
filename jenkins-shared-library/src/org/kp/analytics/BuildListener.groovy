package org.kp.analytics

import com.cloudbees.workflow.rest.external.RunExt
import com.cloudbees.workflow.rest.external.StageNodeExt
import groovy.json.JsonOutput
import hudson.model.Run
import org.jenkinsci.plugins.workflow.job.WorkflowRun

class BuildListener {

  /**
   * Returns json of current build status. Includes stage name, stage duration and stage result.
   * @param currentBuild currentBuild object of Jenkins
   * @param environment environment variables to be added to build json
   * @param appParams common application meta-data to be added to build json
   * @return build status json
   */
  String getBuild(def currentBuild, def environment, Map<String, String> appParams) {
    Run run = currentBuild.rawBuild
    long currentTime = System.currentTimeMillis()
    long duration = (currentTime - run.getStartTimeInMillis()) / 1000
    Map<String, Map> stages = new HashMap<String, Map>()
    
    RunExt runExt = RunExt.create((WorkflowRun) run)
    for (StageNodeExt stage : runExt.getStages()) {
      Map<String, Object> stageInfo = new HashMap<String, Object>()
      stageInfo.put("status", String.valueOf(stage.getStatus()))
      if (stage.getName().contains("Approval")) {
        stageInfo.put("duration", stage.getPauseDurationMillis() / 1000)
      } else {
        stageInfo.put("duration", stage.getDurationMillis() / 1000)
      }
      if (stage.getName().contains("Tag-Build")) {
        if (stage.getName().contains("Config")) {
          stages.put("Tag-Build-Config", stageInfo)
        } else {
          stages.put("Tag-Build", stageInfo)
        }
      } else {
        stages.put(stage.getName().substring(3, stage.getName().length()), stageInfo)
      }
    }

    def buildMap = [:]
    buildMap.put("duration", duration)
    buildMap.put("timestamp", run.getTimestamp())
    buildMap.put("result", currentBuild.result)
    buildMap.put("environment", environment)
    buildMap.put("stages", stages)
    buildMap.putAll(appParams)
    return JsonOutput.toJson(buildMap).trim()
  }
}
