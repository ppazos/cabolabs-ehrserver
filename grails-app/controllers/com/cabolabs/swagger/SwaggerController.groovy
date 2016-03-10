package com.cabolabs.swagger

import com.cabolabs.ehrserver.swagger.ApiDocumentationService



class SwaggerController {
    def ApiDocumentationService apiDocumentationService
    OutputStream swaggerFile
    def index() {
        apiDocumentationService.init()
        ByteArrayInputStream is = new ByteArrayInputStream(apiDocumentationService.init().getBytes());
        OutputStream os = new ByteArrayOutputStream();
        byte b;
        while ((b = is.read()) != -1) {
            os.write(b);
        }
        swaggerFile=os;
        downloadFile();
    }
    def downloadFile() {
        InputStream contentStream
        //def file = new File("src/pruebaSwagger.json")  
        response.setHeader "Content-disposition", "attachment; filename=pruebaSwagger.json"
        response.setHeader("Content-Length", "file-size")
        response.setContentType("application/json")
       // contentStream = file.newInputStream()
        response.outputStream << swaggerFile
        webRequest.renderView = true
 }
    
}
