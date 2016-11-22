package com.cabolabs.ehrserver.reporting

import static org.springframework.http.HttpStatus.*
import grails.transaction.Transactional
import grails.util.Holders

@Transactional(readOnly = true)
class ActivityLogController {

   def config = Holders.config.app
   
   def index(Integer max) {
      params.max = Math.min(max ?: config.list_max, 100)
      respond ActivityLog.list(params), model:[activityLogInstanceCount: ActivityLog.count()]
   }

   def show(ActivityLog activityLogInstance) {
      respond activityLogInstance
   }

   protected void notFound() {
      request.withFormat {
         form multipartForm {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'activityLog.label', default: 'ActivityLog'), params.id])
            redirect action: "index", method: "GET"
         }
         '*'{ render status: NOT_FOUND }
      }
   }
}
