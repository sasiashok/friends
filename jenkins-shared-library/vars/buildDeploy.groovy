import org.kp.utils.*
import org.kp.build.Builder
import org.kp.deploy.UCD
import org.kp.test.Analyzer
import org.kp.security.NexusIQ
import org.kp.deploy.Deployer

def call(Map params = [:]) {
  CommonUtils commonUtils = new CommonUtils()
  GitUtils gitUtils = new GitUtils()
  commonUtils.resolveConfigurations(params)
  BuildData.instance.deploy = commonUtils.getParamValue("deploy")

  timestamps {
    try {
      node (BuildData.instance.agent) {
        try {
          commonUtils.setupNode()

          new Builder().build()
          new Analyzer().testAnalyze()
          new NexusIQ().evaluate()

          if(!gitUtils.isPR()) {
            new ArtifactoryUtils().deployArtifacts()

            def tag = commonUtils.getParamValue("tag")
            if(tag != null) {
              new GitUtils().tagBuild(tag, new ApplicationUtils().getRepoUrl())
            }

            new UCD().processPreDeployment()
          }
        } finally {
          if(isUnix()) {
            deleteDir()
          }
        }
      }
      if(!gitUtils.isPR()) {
        new Deployer().runDeployments()
      }
      if(currentBuild.result == null) {
        currentBuild.result = currentBuild.currentResult
      }
    } catch (exception) {
      commonUtils.handleException(exception)
    } finally {
      commonUtils.quit()
    }
  }
}
