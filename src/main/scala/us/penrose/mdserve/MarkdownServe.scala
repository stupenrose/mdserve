package us.penrose.mdserve

import org.httpobjects.jetty.HttpObjectsJettyHandler
import org.httpobjects.{HttpObject, Request}
import org.httpobjects.DSL._
import java.io.File
import org.apache.commons.io.FileUtils
import org.httpobjects.util.ClasspathResourceObject
import org.httpobjects.util.MimeTypeTool
import org.commonmark.ext.heading.anchor.HeadingAnchorExtension
import java.util.Arrays

object MarkdownServe extends App {
  val port = Integer.parseInt(args(0))
  val root = new File(args(1))
  
   HttpObjectsJettyHandler.launchServer(8080, 
       new ClasspathResourceObject("/styles.css", "/styles.css", getClass()),
       new HttpObject("/"){
         override def get(r:Request) = SEE_OTHER(Location("/home"))
       },
       new HttpObject("/{resource*}"){
         override def get(r:Request) = {
           val path = r.path().valueFor("resource")
           val finalPath = extension(path) match {
             case None => path + ".md"
             case Some(_) => path
           }
           val f = new File(root, finalPath)
           
           if(!f.exists()){
             NOT_FOUND
           }else{
             val data = if(f.getName.endsWith(".md")){
             Html(s"""<html>
                        <title>${f.getName}</title>
                        <link rel="stylesheet" href="styles.css"/>
                        <body>
                          ${renderMarkdown(FileUtils.readFileToString(f))}
                        </body>
                      </html>""")
             }else{
               val mimeTypes = new MimeTypeTool()
               
               Bytes(mimeTypes.guessMimeTypeFromName(f.getName), FileUtils.readFileToByteArray(f))
             }
             OK(data)
           }
         }
      }
  )
   
   def extension(name:String) = {
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
    val parser = Parser.builder().build();
    
    val document = parser.parse(text);
    val renderer = HtmlRenderer.builder()
                    .extensions(Arrays.asList(HeadingAnchorExtension.create())).build();
    renderer.render(document); 
  }
}