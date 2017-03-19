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

object MarkdownServe extends App {
  val port = Integer.parseInt(args(0))
  val rootDirectory = new FilesystemPath(args(1))
  
   HttpObjectsJettyHandler.launchServer(8080, 
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
           val filePath = new FilesystemPath(rootDirectory, pathPlusFileExtension)
           
           if(!filePath.exists()){
             NOT_FOUND
           }else{
             val data = if(filePath.getName.endsWith(".md")){
               Html(s"""<html>
                        <title>${filePath.getName}</title>
                        <link rel="stylesheet" href="styles.css"/>
                        <body>
                          ${renderMarkdown(FileUtils.readFileToString(filePath, Charset.forName("ASCII")))}
                        </body>
                      </html>""")
             }else{
               val mimeTypes = new MimeTypeTool()
               
               Bytes(mimeTypes.guessMimeTypeFromName(filePath.getName), FileUtils.readFileToByteArray(filePath))
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