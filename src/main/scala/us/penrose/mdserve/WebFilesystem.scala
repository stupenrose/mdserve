package us.penrose.mdserve

import java.io.InputStream
import java.net.URL

object WebFilesystem extends Filesystem{
  
  private def stream(url:String) = {
    println("Connecting to " + url)
    new URL(url).openStream()
  }
  
  private case class WebPath(url:String) extends FSPath {
    def child(name:String):FSPath = {
      val spacer = if(url.endsWith("/")) "" else "/"
      new WebPath(url + spacer + name)
    }
    
    def exists():Boolean = {
      try{
        
        stream(url).close()
        true
      }catch {
        case _:Throwable => false
      }
    }
    def getName():String = new URL(url).getPath.split("/").last
  }
  def read[T](path:FSPath, fn:(InputStream) => T):T = {
    val data = stream(path.asInstanceOf[WebPath].url)
    try{
      fn(data)
    }finally{
      data.close()
    }
  }
  def of(path:String):FSPath = WebPath(path)
}