
package com.cabolabs.swagger;

/**
 * @author cabolabs
 * @since 0.7
 * 
 * Encargada de recoger la información de un controller que tiene metodos del api rest
 */
class ControllerDocumentation {
   //Nombre del controlador del que vamos a obtener el archivo swagger
   String nameController=''
   //Ulr a swagger.io.generator apuntando a archivo swagger subido a github
   String urlToSwaggerGeneratorWitUrlFile
   //Contenido del archivo swagger para este controlador.
   String swaggerFileContent=''   		  
   //Indica si el archivo swagger generado del controlador es valido.
   Boolean isValidSwaggerFileContent=false
   
   public ControllerDocumentation (String name,String fileContent, Boolean validContent,urlToSwaggerGenerator){
   		nameController=name
   		swaggerFileContent=fileContent
   		urlToSwaggerGeneratorWitUrlFile=urlToSwaggerGenerator
   		isValidSwaggerFileContent=validContent
   }
}
