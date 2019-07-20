/**
 * Custom wrapper to provide a lockable resource and finally delete it from global configuration.
 * Work around till https://issues.jenkins-ci.org/browse/JENKINS-38906 is solved.
 * @param lockName resource name to use for lock
 * @param body code to be executed in locked state
 */
def call(String lockName, Closure body){
  try {
    lock(lockName) {
      body()
    }
  } finally {
    cleanResource(lockName)
  }
}

/**
 * Cleans up the idle resource from global lockable resource list.
 * Helps keep Jenkins configuration in an optimum state by removing redundant resources
 * @param resourceName dynamically created resource name
 */
def cleanResource(String resourceName) {
  try {
    def manager = org.jenkins.plugins.lockableresources.LockableResourcesManager.get()
    def resource = manager.fromName(resourceName)
    if(resource != null && !(resource?.isLocked() || resource?.isQueued() || resource?.isReserved())) {
      manager.getResources().remove(resource)
    }
  } catch (e) {
    echo "ℹ️ [INFO] Unable to delete lockable resource: " + resourceName
  }
}