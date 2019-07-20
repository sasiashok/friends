package org.kp.test

import org.kp.constants.Default
import org.kp.utils.CommonUtils

/**
 * Publishes cobertura reports
 * @return
 */
def publish() {
  def reportParams = new CommonUtils().getParamValue("cobertura")
  if(reportParams != null) {
    dir(env.APPLICATION_DIR) {
      reportParams.each { reportParam ->
        Boolean autoUpdateHealth = CommonUtils.getKeyValue(reportParam, "autoUpdateHealth", Default.CORBETURA_AUTOUPDATEHEALTH)
        Boolean autoUpdateStability = CommonUtils.getKeyValue(reportParam, "autoUpdateStability", Default.CORBETURA_AUTOUPDATESTABILITY)
        String coberturaReportFile = CommonUtils.getKeyValue(reportParam, "coberturaReportFile", Default.CORBETURA_COBERTURAREPORTFILE)
        String conditionalCoverageTargets = CommonUtils.getKeyValue(reportParam, "conditionalCoverageTargets", Default.CORBETURA_CONDITIONALCOVERAGETARGETS)
        Boolean failUnhealthy = CommonUtils.getKeyValue(reportParam, "failUnhealthy", Default.CORBETURA_FAILUNHEALTHY)
        Boolean failUnstable = CommonUtils.getKeyValue(reportParam, "failUnstable", Default.CORBETURA_FAILUNSTABLE)
        String lineCoverageTargets = CommonUtils.getKeyValue(reportParam, "lineCoverageTargets", Default.CORBETURA_LINECOVERAGETARGETS)
        Integer maxNumberOfBuilds = CommonUtils.getKeyValue(reportParam, "maxNumberOfBuilds", Default.CORBETURA_MAXNUMBEROFBUILDS)
        String methodCoverageTargets = CommonUtils.getKeyValue(reportParam, "methodCoverageTargets", Default.CORBETURA_METHODCOVERAGETARGETS)
        Boolean onlyStable = CommonUtils.getKeyValue(reportParam, "onlyStable", Default.CORBETURA_ONLYSTABLE)
        String sourceEncoding = CommonUtils.getKeyValue(reportParam, "sourceEncoding", Default.CORBETURA_SOURCEENCODING)
        Boolean zoomCoverageChart = CommonUtils.getKeyValue(reportParam, "zoomCoverageChart", Default.CORBETURA_ZOOMCOVERAGECHART)

        cobertura autoUpdateHealth: autoUpdateHealth,
          autoUpdateStability: autoUpdateStability,
          coberturaReportFile: coberturaReportFile,
          conditionalCoverageTargets: conditionalCoverageTargets,
          failUnhealthy: failUnhealthy,
          failUnstable: failUnstable,
          lineCoverageTargets: lineCoverageTargets,
          maxNumberOfBuilds: maxNumberOfBuilds,
          methodCoverageTargets: methodCoverageTargets,
          onlyStable: onlyStable,
          sourceEncoding: sourceEncoding,
          zoomCoverageChart: zoomCoverageChart
      }
    }
  }
}