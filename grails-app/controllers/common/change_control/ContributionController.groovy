package common.change_control

import org.springframework.dao.DataIntegrityViolationException

class ContributionController {

   static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

   def index() {
      redirect(action: "list", params: params)
   }

   def list(Integer max)
   {
      params.max = Math.min(max ?: 10, 100)
     
      def lst = Contribution.list(params)
      def cnt = Contribution.count()
     
      // =========================================================================
      // For charting
      
      // Show 1 year by month
      def now = new Date()
      def oneyearbehind = now - 365
      
      def data = Contribution.withCriteria {
          projections {
              count('id')
              groupProperty('yearMonthGroup') // count contributions in the same month
          }
          audit {
             between('timeCommitted', oneyearbehind, now)
          }
      }
      
      println data
      // =========================================================================

      return [contributionInstanceList: lst, contributionInstanceTotal: cnt, data: data, start: oneyearbehind, end: now]
   }

   def show(Long id) {
      def contributionInstance = Contribution.get(id)
      if (!contributionInstance) {
         flash.message = message(code: 'default.not.found.message', args: [message(code: 'contribution.label', default: 'Contribution'), id])
         redirect(action: "list")
         return
      }

      [contributionInstance: contributionInstance]
   }

}
