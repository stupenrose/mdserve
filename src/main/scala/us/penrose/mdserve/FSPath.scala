package us.penrose.mdserve

trait FSPath {
  def child(name:String):FSPath
  def exists():Boolean
  def getName():String
}