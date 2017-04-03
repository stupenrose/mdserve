package us.penrose.mdserve

import java.io.InputStream

trait Filesystem {
  def read[T](path:FSPath, fn:(InputStream) => T):T
  def of(path:String):FSPath
}