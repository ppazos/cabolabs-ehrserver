package ehr

import grails.util.Holders

class OperationalTemplateController {

   // Para acceder a las opciones de localizacion
   def config = Holders.config.app
   
    def index()
    {   
       render "hola mundo!"
    }
    
    def list()
    {
       return [opts: ehr.clinical_documents.OperationalTemplateIndex.list(),
               total: ehr.clinical_documents.OperationalTemplateIndex.count()]
    }
    
    /**
     * 
     * @param overwrite
     * @return
     */
    def upload(boolean overwrite)
    {
       //println "upload "+ params
       //println "destination folder "+ config.opt_repo
       
       if (params.doit)
       {
          def errors = []
          
          // http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/multipart/commons/CommonsMultipartFile.html
          def f = request.getFile('opt')
          
          // Add file empty check
          // if(!file.empty){ //do something }
          // else { //do something }
          
          def xml = new String( f.getBytes() )
          
          //println xml
          
          // Destination
          // Puedo cambiarle el nombre del archivo agregandolo a la ruta ...
          // FIXME: no deberia depender del nombre y los nombres en disco deberian ser generados,
          //        cuando se sube un OPT, habria que crear un opt_index en la bd con el nombre / id original
          //        y ver contra eso si ya lo tenemos, no contra el archivo fisico en disco como aqui.
          def destination = config.opt_repo + f.getOriginalFilename()
          
          //println "destination: "+ destination
          
          File fileDest = new File( destination )
          
          // FIXME: overwrite check should happen using the template uid not the filename.
          if (!overwrite && fileDest.exists())
          {
             // FIXME: overwrite might cause inconsistencies with currently saved data for the previous version of the template
             errors << "The OPT already exists"
             return [errors: errors]
          }
          
          // Validate
          def validator = new parsers.OPTValidator()
          if (!validator.validate(xml))
          {
             errors = validator.getErrors() // Important to keep the correspondence between version index and error reporting.
             return [errors: errors]
          }
          
          if (errors.size() == 0)
          {
             // Hago lo mismo que el transferTo pero a mano.
             if (overwrite) // file exists and the user wants to overwrite
             {
                def copy = new File(destination + ".old")
                fileDest.renameTo(copy)
                copy.delete()
             }
             
             fileDest << xml
             
             // http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/multipart/commons/CommonsMultipartFile.html#transferTo-java.io.File-
             // If the file exists, it will be deleted first
             //f.transferTo(fileDest)
             // Tira excepcion si el archivo existe:
             // Message: opts\Signos.opt (Access is denied)
             //    Line | Method
             //->>  221 | <init>    in java.io.FileOutputStream
             
             flash.message = "opt.upload.success"
             
             
             def ti = new com.cabolabs.archetype.OperationalTemplateIndexer()
             ti.indexAll()
          }
       }
    }
}
