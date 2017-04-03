package us.penrose.mdserve

import org.httpobjects.jetty.HttpObjectsJettyHandler
import org.httpobjects.{HttpObject, Request}
import org.httpobjects.DSL._
import java.io.{File => FilesystemPath}
import org.apache.commons.io.FileUtils
import org.httpobjects.util.ClasspathResourceObject
import org.httpobjects.util.MimeTypeTool
import org.commonmark.ext.heading.anchor.HeadingAnchorExtension
import java.util.Arrays
import java.nio.charset.Charset
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.apache.commons.io.IOUtils

object MarkdownServe extends App {
  val port = Integer.parseInt(args(0))
  val fs:Filesystem = args(1) match {
    case local if local.startsWith("/") => LocalFilesystem
    case web if web.startsWith("http") => WebFilesystem
  }
  val rootDirectory:FSPath = fs.of(args(1))
  
   HttpObjectsJettyHandler.launchServer(port, 
       new ClasspathResourceObject("/styles.css", "/styles.css", getClass()),
       new HttpObject("/"){
         override def get(r:Request) = SEE_OTHER(Location("/home"))
       },
       new HttpObject("/{resource*}"){
         override def get(r:Request) = {
           val path = r.path().valueFor("resource")
           val pathPlusFileExtension = fileExtension(path) match {
             case None => path + ".md"
             case Some(_) => path
           }
           val filePath = rootDirectory.child(pathPlusFileExtension)
           
           if(!filePath.exists()){
             NOT_FOUND
           }else{
             val data = if(filePath.getName.endsWith(".md")){
               val text = fs.read(filePath, IOUtils.toString(_, Charset.forName("ASCII")))
               Html(s"""<html>
                        <title>${filePath.getName}</title>
                        <link rel="stylesheet" href="styles.css"/>
                        <body>
                          ${renderMarkdown(text)}
                        </body>
                      </html>""")
             }else{
               val mimeTypes = new MimeTypeTool()
               val bytes = fs.read(filePath, IOUtils.toByteArray(_))
               
               Bytes(mimeTypes.guessMimeTypeFromName(filePath.getName), bytes)
             }
             OK(data)
           }
         }
      }
  )
   
   def fileExtension(name:String):Option[String] = {
    val idx = name.lastIndexOf('.')
    if(idx == -1){
      None
    }else{
      Some(name.substring(idx, name.length()))
    }
  }
   
   def renderMarkdown(text:String) = {
    import org.commonmark.renderer.html.HtmlRenderer
    import org.commonmark.parser.Parser
    val extensions = Arrays.asList(
                        HeadingAnchorExtension.create(),
                        StrikethroughExtension.create())
                        
    val parser = Parser.builder().extensions(extensions).build();
    
    val document = parser.parse(text);
    val renderer = HtmlRenderer.builder()
                    .extensions(extensions).build();
    renderer.render(document); 
  }
}