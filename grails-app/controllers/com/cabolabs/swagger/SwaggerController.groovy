package com.cabolabs.swagger

import com.cabolabs.ehrserver.swagger.ApiDocumentationService
import com.cabolabs.swagger.ControllerDocumentation

import groovy.util.slurpersupport.GPathResult
import groovyx.net.http.HTTPBuilder
import org.apache.http.client.HttpClient
import org.apache.http.client.ResponseHandler
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.BasicResponseHandler
import org.apache.http.impl.client.DefaultHttpClient

class SwaggerController {
    def ApiDocumentationService apiDocumentationService
    //OutputStream swaggerFile
    def index() {
      apiDocumentationService.init()
      def controllerClasses=apiDocumentationService.controllerClasses
      // mapControllers.each { entry ->
      //      println "Nombre de la clase: $entry.key "//Age: $entry.value"
      //  }

        return [opts:controllerClasses]
                //ByteArrayInputStream is = new ByteArrayInputStream(apiDocumentationService.init().getBytes());
        //OutputStream os = new ByteArrayOutputStream();
       // byte b;
       // while ((b = is.read()) != -1) {
      //      os.write(b);
      //  }
       // swaggerFile=os;
      //  downloadFile();
    }
    def downloadFile() {
        InputStream contentStream
        def file = new File("src/pruebaSwagger.json")  
        response.setHeader "Content-disposition", "attachment; filename=pruebaSwagger.json"
        response.setHeader("Content-Length", "file-size")
        response.setContentType("application/json")
        contentStream = file.newInputStream()
        response.outputStream << swaggerFile
        webRequest.renderView = true
        return file
    }

    /**
     * 
     * @return
     */
    def show(String fileNameSwagger)
    {
      def controllerClasses=apiDocumentationService.controllerClasses
      render("<div class=\"col-md-6\">Contenido del Fichero ${fileNameSwagger}:<pre><code id=\"json\">"+controllerClasses.get(fileNameSwagger).swaggerFileContent+"</code></pre></div>")
    }

    /***
    *
    */
    def uploadFileToGitHub(){
      //https://raw.github.com/username/project/master/script.js "https://raw.github.com/casiodbx/quizjc/master/"+downloadFile()
        final HttpURLConnection connection = new URL('https://rawgithub.com/casiodbx/quizjc/master/swagger.json').openConnection()
        //connection.inputStream.withReader { Reader reader ->
        //    html = reader.text
        //}
        println 'LA RESPUETA DEL LA CONEXIÓN HA SIDO '+connection.responseCode 
       // String response = connection.inputStream.withReader { Reader reader -> reader.text }
    }
}
