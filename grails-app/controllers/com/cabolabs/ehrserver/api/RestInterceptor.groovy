package com.cabolabs.ehrserver.api


class RestInterceptor {

   int order = HIGHEST_PRECEDENCE + 150

   def apiResponsesService
   def messageSource

   public RestInterceptor()
   {
      match controller: 'rest'
   }

   /**
    * Checks the format is supported for all the Rest requests.
    * Empty format is allowed and will return the default format (JSON)
    */
   boolean before()
   {
      if (!['', null, 'xml', 'json', 'html'].contains(params.format)) // queryCompositions support html
      {
         // bad request in XML
         // render(status: 400, contentType:"text/xml", encoding:"UTF-8") {
         //    result {
         //       type('AR')                         // application reject
         //       message(
         //          messageSource.getMessage('rest.error.formatNotSupported', [params.format] as Object[], getRequestLocale(request))
         //       )
         //       code('EHR_SERVER::API::ERRORS::0066') // sys::service::concept::code
         //    }
         // }

         def result = apiResponsesService.feedback_json(
            messageSource.getMessage('rest.error.formatNotSupported', [params.format] as Object[], getRequestLocale(request)),
            'AR',
            'EHR_SERVER::API::ERRORS::0066')

         response.status = 400
         render(text: result, contentType:"application/json", encoding:"UTF-8")

         return false
      }

      // Default format
      if (!params.format) params.format = 'json'

      return true
   }

   boolean after() { true }

   void afterView() {
       // no-op
   }

   def getRequestLocale(request)
   {
      org.springframework.web.servlet.support.RequestContextUtils.getLocale(request)
   }
}
