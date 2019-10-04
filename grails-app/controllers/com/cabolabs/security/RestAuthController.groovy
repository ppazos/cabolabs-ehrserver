package com.cabolabs.security

import grails.converters.JSON

class RestAuthController {

   def statelessTokenProvider

   // with this, the AuthInterceptor is executed even if the controller is excluded from the matches
   //static allowedMethods = [auth:['POST']]

   def auth(String email, String password)
   {
      if (request.method != 'POST')
      {
         render(status: 405, text:"") // without the text is rendering the login!!!
         //response.sendError(405) // does execute the auth interceptor and redirects to login WTF!?
         return
      }


      if (!email || !password)
      {
         renderError('incomplete credentials', 'e01.0001', 400)
         return
      }

      def user = User.findByEmail(email)

      if (!user)
      {
         renderError('auth failed', 'e01.0001', 401, [], null)
         return
      }

      if (!PasswordUtils.isPasswordValid(user.password, password))
      {
         renderError('auth failed', 'e01.0001', 401, [], null)
         return
      }

      // TODO: put the server id/name in the payload
      def token = statelessTokenProvider.generateToken(email, null, [:])

      withFormat {
         json {
            render (['token': token] as JSON)
         }
         xml {
            render(contentType:"text/xml", encoding:"UTF-8") {
               //result {
                  token(token)
               //}
            }
         }
      }
   }

   private void renderError(String msg, String errorCode, int status, List detailedErrors, Exception ex)
   {
      /*
       * result
       *   type
       *   status
       *   message
       *   details
       *     item detailedErrors[0]
       *     item detailedErrors[1]
       *     ...
       *   trace // DEV ONLY
       */

      def type = ((status in 200..299) ? 'AA' : 'AR')
      def result

      // Format comes from current request
      withFormat {
         xml {
            // FIXME
            //result = apiResponsesService.feedback_xml(msg, type, errorCode, detailedErrors, ex)
            result = msg

            render( status:status, text:result, contentType:"text/xml", encoding:"UTF-8")
         }
         json {
            // FIXME
            //result = apiResponsesService.feedback_json(msg, type, errorCode, detailedErrors, ex)
            result = [
               result: [
                  type: type,
                  message: msg,
                  code: 'EHRSERVER::API::RESPONSE_CODES::'+ errorCode
               ]
            ] as JSON


            // JSONP
            if (params.callback) result = "${params.callback}( ${result} )"

            // with the status in render doesnt return the json to the client
            // http://stackoverflow.com/questions/10726318/easy-way-to-render-json-with-http-status-code-in-grails
            response.status = status
            render(text: result, contentType:"application/json", encoding:"UTF-8")
         }
         '*' {
            // FIXME
            //result = apiResponsesService.feedback_xml(msg, type, errorCode, detailedErrors, ex)
            result = [
               result: [
                  type: type,
                  message: msg,
                  code: 'EHRSERVER::API::RESPONSE_CODES::'+ errorCode
               ]
            ] as JSON

            response.status = status
            render(text: result, contentType:"application/json", encoding:"UTF-8")
         }
      }
   }
}
