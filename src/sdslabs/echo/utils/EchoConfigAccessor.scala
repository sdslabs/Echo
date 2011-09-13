package sdslabs.echo.utils
import org.apache.commons.configuration.Configuration
import org.apache.commons.configuration.PropertiesConfiguration

object EchoConfigAccessor {
  val env = System.getenv() 
  val appRoot = env.get("APPROOT")
  val configDirectory = appRoot + "/config/"
  val configFile = configDirectory + "Echo.cfg"
  
  val config : Configuration = new PropertiesConfiguration(configFile)
  
  def getString(key: String):String = {
    return config.getString("*.*." + key)
  }
  
  def getInt(key: String):Int = {
    return config.getInt("*.*." + key)
  }
 
}