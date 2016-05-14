package com.cabolabs.swagger

import com.cabolabs.ehrserver.swagger.ApiDocumentationService

import groovy.util.slurpersupport.GPathResult
import groovyx.net.http.HTTPBuilder
import org.apache.http.client.HttpClient
import org.apache.http.client.ResponseHandler
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.BasicResponseHandler
import org.apache.http.impl.client.DefaultHttpClient

class SwaggerController {
    def ApiDocumentationService apiDocumentationService
    def index() {
      apiDocumentationService.init()
      def controllerClasses=apiDocumentationService.controllerClasses
        return [opts:controllerClasses]
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
      render("<div class=\"col-md-6\"><pre><code id=\"json\">"+controllerClasses.get(fileNameSwagger).swaggerFileContent+"</code></pre></div>")
    }

    /***
    *
    */
    def uploadFileToGitHub(){
        final HttpURLConnection connection = new URL('https://rawgithub.com/casiodbx/quizjc/master/swagger.json').openConnection()
    }
}
