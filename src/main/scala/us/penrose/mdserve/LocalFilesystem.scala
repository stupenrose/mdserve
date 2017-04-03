package us.penrose.mdserve

import java.io.File
import java.io.FileInputStream
import java.io.InputStream

object LocalFilesystem extends Filesystem {
  private case class LocalPath(f:File) extends FSPath {
    def child(name:String):FSPath = new LocalPath(new File(f, name))
    def exists():Boolean = f.exists()
    def getName():String = f.getName()
  }
  
  def read[T](path:FSPath, fn:(InputStream) => T):T = {
    val data = new FileInputStream(path.asInstanceOf[LocalPath].f)
    try{
      fn(data)
    }finally{
      data.close()
    }
  }
  def of(path:String):FSPath = LocalPath(new File(path))
}