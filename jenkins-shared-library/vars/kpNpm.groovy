import org.kp.constants.Constant

/**
 * Perform npm command with double wrapper nodejs plugin
 * @param command
 */
def call(Closure body){
  withNPM(npmrcConfig: Constant.NPMRC_FILE){
    nodejs(Constant.NODEJSTOOL) {
      body()
    }
  }
}
