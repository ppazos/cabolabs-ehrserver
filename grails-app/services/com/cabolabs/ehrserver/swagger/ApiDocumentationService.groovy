
package com.cabolabs.ehrserver.swagger
import com.cabolabs.ehrserver.api.RestController
import grails.rest.RestfulController
import grails.transaction.Transactional
import org.codehaus.groovy.grails.commons.GrailsControllerClass
import org.codehaus.groovy.grails.commons.GrailsDomainClass
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty
import org.codehaus.groovy.grails.validation.ConstrainedProperty
import com.cabolabs.swagger.annotations.ApiOperation
import com.cabolabs.swagger.annotations.ApiParam
import com.cabolabs.swagger.annotations.ApiParams
import com.cabolabs.swagger.annotations.ApiResponse
import com.cabolabs.swagger.annotations.ApiResponses
import com.cabolabs.swagger.annotations.DeleteMethod
import com.cabolabs.swagger.annotations.ApiDescription
import com.cabolabs.swagger.annotations.GetMethod
import com.cabolabs.swagger.annotations.ApiIgnore
import com.cabolabs.swagger.annotations.PostMethod
import com.cabolabs.swagger.annotations.ApiProperty
import com.cabolabs.swagger.annotations.PutMethod
import com.cabolabs.swagger.DomainDocumentation
import com.cabolabs.swagger.DomainPropertyDocumentation
import com.cabolabs.swagger.ControllerDocumentation
import com.cabolabs.swagger.ControllerActionDocumentation
import com.cabolabs.swagger.ControllerActionParameterDocumentation
import com.cabolabs.swagger.ControllerActionResponseDocumentation

import java.lang.annotation.Annotation
import java.lang.reflect.Method
import java.lang.reflect.Field
//Para el validador, quitarlas si al final se elimina
import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.core.report.ProcessingReport;

class ApiDocumentationService {
    //Para obtener credenciales usuario
    def springSecurityService
    //Para generar el token
    def statelessTokenProvider
    def grailsApplication
    static String urlToSwaggerIOGenerator="https://generator.swagger.io/?url="
    static String urlToFileJson="https://raw.githubusercontent.com/casiodbx/quizjc/master/"
    static IGNORE_PROPERTIES = ['class','errors','metaClass','version','']
    static def controllerClasses
    static String token

    def init() {
        def RestController restController= new RestController()
        def auth = springSecurityService.authentication
        token=" Bearer "+ statelessTokenProvider.generateToken(auth.getPrincipal().username, null, [organization: auth.organization])
        String resultado=""
        controllerClasses =[:] 
        for (GrailsControllerClass controller in grailsApplication.controllerClasses) {
          resultado=addDomainAndControllerClass(controller, grailsApplication)
          if (resultado){
            //Relleno Bean con información de la clase
            controllerClasses.put(controller.clazz.simpleName,new ControllerDocumentation(controller.clazz.simpleName,resultado,validatorJson(resultado),urlToSwaggerIOGenerator+urlToFileJson+controller.clazz.simpleName+".json"))
          }
        }
    }



    protected static String addDomainAndControllerClass(GrailsControllerClass controller, grailsApplication) {
      
      ApiDescription apiDescription = controller.clazz.getAnnotation(ApiDescription)
      String contenidoInfoNotaciones=""
      if (apiDescription) {
           contenidoInfoNotaciones='{'+getHeaderSwagger(controller,'2.0')+','+ generatePaths(controller,grailsApplication)+'}' 
         }
        return contenidoInfoNotaciones
    }

    static String getHeaderSwagger(GrailsControllerClass controller,String versionSwagger){
       //Define tag swagger
      def tagSwagger = {version -> 
         return "\"swagger\":\"${version}\" \n" 
      } 
      //Define tag Info    
      def infoHeader = { title, description,version -> 
         return "\"info\":{\n \"title\": \"${title}\",\n \"description\": \"${description}\",\n\"version\": \"${version}\"}\n" 
      }
      //Define tag del Host
      def hostHeader = { host -> 
         return "\"host\": \"${host}\"\n" 
      }      
       //Define tag del Host
      def schemesHeader = { schemes -> 
         return "\"schemes\": [\"${schemes}\"]\n" 
      }
      //Define tag del basePath 
      def basePathHeader = { basePath -> 
         return "\"basePath\": \"${basePath}\"\n" 
      }
      def tagsHeader = { tags -> 
          def contentTags=""
          if (tags){
              def values =tags.split(',')
              int i
              for (i = 0; i <values.size(); i++) {
                   if (i==0){
                      contentTags+= "{\"name\":\"${values[i]}\"}"
                   }else{
                      contentTags+= ",{\"name\":\"${values[i]}\"}"
                   }
              }
          }
         return "\"tags\": [${contentTags}]\n" 
      }      
      //Define tag produces 
      def producesHeader = { produces -> 
         return "\"produces\": [\"${produces}\"]\n" 
      }
        //Creación cabecera
       ApiDescription description
      //Add tag swagger with version
       def header=tagSwagger(versionSwagger)
      //Add tag info
         //Obtengo la descripción
         description=controller.clazz.getAnnotation(ApiDescription);
         header+=","+infoHeader(description.title(),description.description(),description.version()) 
      //Add to tag host
         header+=","+hostHeader(description.host())
      //Add to tag schemes
         header+=","+schemesHeader(description.schemes())
      //Add to basepath 
         header+=","+basePathHeader(description.basePath())
      //Add to tags 
         header+=","+tagsHeader(description.tags())
      //Add to produces 
         header+=","+producesHeader(description.produces())     
        return header
    }



    static generatePaths(GrailsControllerClass controller,grailsApplication){

        //Defino paths
        def paths={paths->
             return   "\"paths\": {\n${paths}}"     
         }
        def definitions={elementDefinitions,elementError->
            return   "\"definitions\": {${elementDefinitions},${elementError}}"     
        }
        //Defino error definitions
        def errorDefinitions={type,properties->
          return   "\"Error\": {\n\"type\": \"${type}\",\n${properties}\n}"          
        }
        def errorProperties={code,message,fields->
          return   "\"properties\": {\n${code},\n${message},\n${fields}\n}"          
        }      
        //Defino code property of error definitions
        def errorPropertiesCodeDefinitions={type,format->

          return   "\"code\": {\n\"type\": \"${type}\",\n\"format\": \"${format}\"\n}"          
        }
        //Defino type property of error definitions
        def errorMenssage={type->
         return   "\"message\": {\n\"type\": \"${type}\"}\n"          
        }
        //Defino type property of error definitions
        def errorFields={type->
          return   "\"fields\": {\n\"type\": \"${type}\"}\n"          
        }

        def typeOperationRestMethod={methodControllerRest->
           Annotation annotation 
           if (methodControllerRest.isAnnotationPresent(GetMethod.class)) {
              annotation = methodControllerRest.getAnnotation(GetMethod.class);
              return(GetMethod) annotation;    
          }else if (methodControllerRest.isAnnotationPresent(PostMethod.class)) {
              annotation = methodControllerRest.getAnnotation(PostMethod.class);
              return (PostMethod) annotation;
          }else if (methodControllerRest.isAnnotationPresent(PutMethod.class)) {
              annotation = methodControllerRest.getAnnotation(PutMethod.class);
             return (PutMethod) annotation;    
          }else if (methodControllerRest.isAnnotationPresent(DeleteMethod.class)) {
              annotation = methodControllerRest.getAnnotation(DeleteMethod.class);
              return (DeleteMethod) annotation;   
          }         
         }
        def contenidoPaths=''
        def contenidoDefinitions=''
        int contador=0
        def methods = getSortedMethods(controller.clazz)
        def methodsToUris = [:]
        for (String uri in controller.getURIs()) {
            def methodName = controller.getMethodActionName(uri)
            if (!methodsToUris[methodName]) {
                methodsToUris[methodName] = []
            }
            methodsToUris[methodName] << uri
        }
        def uri2method = [:]
        for (methodName in methodsToUris.keySet()) {
            def shortestUrl = methodsToUris[methodName].sort({ it.size() })[0]
            uri2method[shortestUrl] = methodName
        }
        for (uri in uri2method.keySet().sort()) {
            def methodName = uri2method[uri]
            Method method = methods.find({ it.name == methodName })
            if (!method) {
                continue
            }
         //Hacemos un for recorriendo los metodos
        //for (Method method : controller.getDeclaredMethods()) {
            def httpMethod=''
            if (method.isAnnotationPresent(ApiIgnore)) {
                continue
            } else if (method.isAnnotationPresent(GetMethod)) {
                httpMethod = 'get'
            } else if (method.isAnnotationPresent(PostMethod)) {
                httpMethod = 'post'
            } else if (method.isAnnotationPresent(PutMethod)) {
                httpMethod = 'put'
            } else if (method.isAnnotationPresent(DeleteMethod)) {
                httpMethod = 'delete'
            } else if (httpMethod == null) {
                // If nobody assigned a httpMethod, skip this method
                continue
            }
            if (httpMethod in ['get','post','put','delete']){
                if (contador >0){
                    contenidoPaths+=','
                }
                contenidoPaths+=generatePath(method,httpMethod,grailsApplication)
                contador++
                //Para construir la parte del definitions
                if (!''.equals(typeOperationRestMethod(method).domainClass())){
                    //def errorPropertiesDefinitions=errorProperties(errorPropertiesCodeDefinitions('typeCode','formatCode'),errorMenssage('type'),errorFields('type'))
                    //contenidoDefinitions=definitions(generateDefinitions(Method method,grailsApplication),errorDefinitions('type',errorPropertiesDefinitions))
                    contenidoDefinitions+=((contenidoDefinitions)?",":"")+generateDefinitions(method,grailsApplication)
                }
            }          

        }   
        //Para añadir los defitions
            //Lo de error lo más seguro me lo cargue para la primera versión
            def errorPropertiesDefinitions=errorProperties(errorPropertiesCodeDefinitions('integer','int32'),errorMenssage('string'),errorFields('string'))
        return paths(contenidoPaths)+','+definitions(contenidoDefinitions,errorDefinitions('object',errorPropertiesDefinitions))       
    }

static String generatePath(Method method,String httpMethod,grailsApplication){
      //Defino Element of Api Rest
     def elementApiRestPaths={nameElementApiRest,contentElementApiRest->
         return   "\"${nameElementApiRest}\": {\n${contentElementApiRest}\n}"     
     }
     //Defino Type of Element of Api Rest     
      def typeElementApiRest={type,summary,description,parameters,tags,responses->
         return   "\"${type}\": {\"summary\": \"${summary}\",\n\"description\": \"${description}\",\n${parameters},${tags},${responses}}"     
      }
      //Defino Paramters Type of Element of Api Rest     
      def parametersElementApiRest={parameters->
         return   "\"parameters\": [\n${parameters}\n]"     
      }
      //Defino Paramters Type of Element of Api Rest     
      def parameterElementApiRest={name,ind,description,required,type,format,items->
         String contentFormatParameters="";
         String contentDefaultValue="";
         String contentEnum="";
         if (!"".equals(format)){
            contentFormatParameters=",\"format\": \"${format}\""
         } 
         //Para elemento Authorization del Header
         if ("Authorization".equals(name)){
            contentDefaultValue=",\"default\": \"${token}\""
         } 
         //Cuando un elemento tiene un listado cerrado de valores
         if (items){
              def values =items.split(',')
              int i
              contentEnum=",\"enum\": ["
              for (i = 0; i <values.size(); i++) {
                   if (i==0){
                      contentEnum+= "\"${values[i]}\""
                   }else{
                      contentEnum+= ",\"${values[i]}\""
                   }
              }
             contentEnum+="]"
         }

         return   "{\"name\": \"${name}\",\n"+
                  "\"in\": \"${ind}\",\n" +
                  "\"description\": \"${description}\",\n" +
                  "\"required\": ${required},\n" +
                  "\"type\": \"${type}\"\n"+
                  contentEnum+
                  contentFormatParameters+contentDefaultValue+"}"
      }
      //Defino tags of Element of Api Rest     
      def tagsElementApiRest={tag->
         return   "\"tags\": [\"${tag}\"]\n"     
      }
       //Defino tags of Element of Api Rest     
      def responsesElementApiRest={responses->
         return   "\"responses\": {\n${responses}\n}"     
      }
      //Defino tags of Element of Api Rest     
      def responseElementApiRest={type,description,schema->
         return   "\"${type}\": { \n\"description\": \"${description}\",\n\"schema\": {${schema}}\n}"     
      }
      //Defino tags of Element of Api Rest     
      def schemaResponseElementApiRest={type,items->
         String contentItems="";
         if (!"".equals(items)){
                 contentItems=",\n\"items\": {\n${items}\n}"
         } 

         return   "\"type\": \"${type}\""+contentItems     
      }
      //Defino tags of Element of Api Rest     
      def itemSchemaResponseElementApiRest={name,contentItem->
          if ("".equals(contentItem)){
            return ""
         } 
         return   "\"${name}\": \"${contentItem}\""     
      }
      
       def typeOperationRestMethod={methodControllerRest->
           Annotation annotation 
           if (methodControllerRest.isAnnotationPresent(GetMethod.class)) {
              annotation = methodControllerRest.getAnnotation(GetMethod.class);
              return(GetMethod) annotation;    
          }else if (methodControllerRest.isAnnotationPresent(PostMethod.class)) {
              annotation = methodControllerRest.getAnnotation(PostMethod.class);
              return (PostMethod) annotation;
          }else if (methodControllerRest.isAnnotationPresent(PutMethod.class)) {
              annotation = methodControllerRest.getAnnotation(PutMethod.class);
             return (PutMethod) annotation;    
          }else if (methodControllerRest.isAnnotationPresent(DeleteMethod.class)) {
              annotation = methodControllerRest.getAnnotation(DeleteMethod.class);
              return (DeleteMethod) annotation;   
          }         
      }
           
      
     //1) Contruyo el elemento de los parametros
          def parametros=''
          def parametro=''
          int contadorParametro=0
          Annotation annotation 
               annotation = method.getAnnotation(ApiParams.class);
               ApiParams apiParams = (ApiParams) annotation;           
               //Recorremos las posibles respuestas
               apiParams.value().each {
                    if (contadorParametro >0){
                       parametro+=','
                    }
                    parametro+=parameterElementApiRest(((ApiParam)it).name(),((ApiParam)it).in(),((ApiParam)it).value(),((ApiParam)it).required(),((ApiParam)it).type(),((ApiParam)it).format(),((ApiParam)it).items()) 
                   contadorParametro++
               }   

          parametros=parametersElementApiRest(parametro)
        //3 Creo el elemento tag
           def tagMetodo=tagsElementApiRest(typeOperationRestMethod(method).tags())
        //4 Creo responses  
           int contador=0          
            def responsesMetodo=''
            if (method.isAnnotationPresent(ApiResponses.class)) {
                annotation = method.getAnnotation(ApiResponses.class);
                ApiResponses apiResponses = (ApiResponses) annotation;
               //Recorremos las posibles respuestas
                apiResponses.value().each {
                    if (contador >0){
                       responsesMetodo+=','
                    }
                    responsesMetodo+=responseElementApiRest(((ApiResponse)it).code(),((ApiResponse)it).message(),schemaResponseElementApiRest(((ApiResponse)it).typeSchema(),itemSchemaResponseElementApiRest(((ApiResponse)it).nameItemsSchema(),((ApiResponse)it).valueItemsSchema())))
                   contador++
                }
            }
         //5 Construyo tag tipo del elemento path
          def tagMethod=typeElementApiRest(httpMethod,typeOperationRestMethod(method).summary(),typeOperationRestMethod(method).description(),parametros,tagMetodo,responsesElementApiRest(responsesMetodo))
        //6 Contruyo metodo
          def elementoPath=elementApiRestPaths(typeOperationRestMethod(method).pathApiRest(),tagMethod) 
     return elementoPath   
     generateDefinitions(method,grailsApplication)
  }



 static String generateDefinitions(Method method,grailsApplication){
    //Defino definitions
     
      //Defino element of definitions
      def elementDefinitions={name,type,properties->
         return   "\"${name}\": {\n\"type\": \"${type}\",\n${properties}}"     
      }
      //Defino properties of definitions
      def propertiesElementDefinitions={properties->
         return   "\"properties\": {\n${properties}\n}"     
      } 
      //Defino property of definitions
      def propertyElementDefinitions={name,type,format,description->
         String contentFormat="";
         if (!"".equals(format)){
            contentFormat=",\"format\":\"${format}\""
         } 
         return   "\"${name}\": {\n\"type\":\"${type}\"\n${contentFormat},\n\"description\":\"${description}\"\n}"     
      }
      def typeOperationRestMethod={methodControllerRest->
           Annotation annotation 
           if (methodControllerRest.isAnnotationPresent(GetMethod.class)) {
              annotation = methodControllerRest.getAnnotation(GetMethod.class);
              return(GetMethod) annotation;    
          }else if (methodControllerRest.isAnnotationPresent(PostMethod.class)) {
              annotation = methodControllerRest.getAnnotation(PostMethod.class);
              return (PostMethod) annotation;
          }else if (methodControllerRest.isAnnotationPresent(PutMethod.class)) {
              annotation = methodControllerRest.getAnnotation(PutMethod.class);
             return (PutMethod) annotation;    
          }else if (methodControllerRest.isAnnotationPresent(DeleteMethod.class)) {
              annotation = methodControllerRest.getAnnotation(DeleteMethod.class);
              return (DeleteMethod) annotation;   
          }         
      }
    //Deberemos recibir un directorio y trataremos cada una de las clases.
    GrailsDomainClass domainClass = grailsApplication.domainClasses.find({ it.clazz.simpleName == typeOperationRestMethod(method).domainClass()})    
    //1)Generamos todos los definitions.element
      def contenidoProperties=""
      def contadorProperties=0
      Annotation annotation
        ApiDescription testerInfo
      for (Field field: domainClass.clazz.getDeclaredFields()) {   
         if (field.isAnnotationPresent(ApiProperty.class)) {
           annotation = field.getAnnotation(ApiProperty.class)
            
            ApiProperty apiProperty= (ApiProperty) annotation;            
            if (contadorProperties >0){
                contenidoProperties+=','
            }
            contenidoProperties+=propertyElementDefinitions(field.getName(),apiProperty.type(),apiProperty.format(),apiProperty.description())
            contadorProperties++
        }
      }
    //2 los añado a su elemento.
        def properties=propertiesElementDefinitions(contenidoProperties)
     //3 Elemento del definitions
        if (domainClass.clazz.isAnnotationPresent(ApiDescription.class)) {
            annotation = domainClass.clazz.getAnnotation(ApiDescription.class)
            testerInfo = (ApiDescription) annotation;              
        }
        def definitionsElement=elementDefinitions(testerInfo.nameElementDefinitions(),testerInfo.typeElementDefinitions(),properties)
     /////////Fin del for de las clases de dominio///////////////        
     //def errorPropertiesDefinitions=errorProperties(errorPropertiesCodeDefinitions('typeCode','formatCode'),errorMenssage('type'),errorFields('type'))
     //print definitions(definitionsElement,errorDefinitions('type',errorPropertiesDefinitions))
     //return definitions(definitionsElement,errorDefinitions('type',errorPropertiesDefinitions))     
     return definitionsElement
}


    static getSortedMethods(Class aClass) {
        def r = []
        aClass.getDeclaredMethods().each { m ->
            r << m
        }
        if (aClass.superclass) {
            r.addAll getSortedMethods(aClass.superclass)
        }
        return r
    }

    //Sustituirlo por validación online http://online.swagger.io/validator/debug?url=http://petstore.swagger.io/v2/swagger.json
    //<img src="http://online.swagger.io/validator?url={YOUR_URL}">
    private Boolean validatorJson(String content) {
      Boolean validoJson=false;
     try { 
          JsonNode mySchema=JsonLoader.fromResource('/src/Schema.json')
          JsonNode jsonFile=JsonLoader.fromString(content)
          JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
          JsonSchema schema = factory.getJsonSchema(mySchema);
          ProcessingReport report = schema.validate(jsonFile);
          if (report.isSuccess()) {
              validoJson=true
          }
       } catch (Exception e) {
          println 'Se ha producido un error'+e
       } 
       return validoJson
    }
}
