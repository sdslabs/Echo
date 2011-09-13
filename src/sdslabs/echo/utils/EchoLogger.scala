package sdslabs.echo.utils
 
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

object EchoLogger {

  val log : Log = LogFactory.getLog("Echo")
  
  private def prepareMessage(t: Throwable, message: Any):Any = {
    val elements : Array[StackTraceElement] = t.getStackTrace()
    
    val callerMethod = elements(1).getMethodName()
    val callerLineNumber = elements(1).getLineNumber()
    val callerClassName = elements(1).getClassName()
    
    return "class: " + callerClassName + " method " + callerMethod + " " + "(" + callerLineNumber + ") :" + message
  }
  
  def info(message: Any){
    val t : Throwable = new Throwable()    
    log.info(prepareMessage(t, message))
  }
  
  def fatal(message: Any){
    val t : Throwable = new Throwable()    
    log.fatal(prepareMessage(t, message))
  }
  
  def error(message: Any){
    val t : Throwable = new Throwable()    
    log.error(prepareMessage(t, message))
  }
  
  def warn(message: Any){
    val t : Throwable = new Throwable()    
    log.warn(prepareMessage(t, message))
  }
  
  def debug(message: Any){
    val t : Throwable = new Throwable()    
    log.debug(prepareMessage(t, message))
  }
  
  def trace(message: Any){  
    val t : Throwable = new Throwable()    
    log.trace(prepareMessage(t, message))
  }
  
}