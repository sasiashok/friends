// This function is a wrapper to shell and batch functions.
def call(String script, String platform = "unix") {
  if(isUnix() && platform.equals("unix")) {
    sh script
  } else {
    bat script
  }
}