package com.cabolabs.ehrserver.ehr.clinical_documents.data

/** 
 * @author Pablo Pazos Gutierrez
 */
class DvDurationIndex extends DataValueIndex {

   String value
   BigDecimal magnitude // calculated, duration in seconds
   
   def beforeValidate() {
      def keys = ['full', 'repeat', 'years', 'year_fraction', 'months', 'month_fraction', 'weeks', 'week_fraction', 'days', 'day_fraction', 'time', 'hours', 'hours_fraction', 'minutes', 'minutes_fraction', 'seconds', 'seconds_fraction']
      def remove_suffix = ['years', 'months', 'weeks', 'days', 'hours',  'minutes', 'seconds']
      
      //ISO 8601 duration regex
      def regexp = /^(R\d*\/)?P(\d+(\.\d+)?Y)?(\d+(\.\d+)?M)?(\d+(\.\d+)?W)?(\d+(\.\d+)?D)?(T(\d+(\.\d+)?H)?(\d+(\.\d+)?M)?(\d+(\.\d+)?S)?)?$/
      
      def matcher = this.value =~ regexp
      def v = [keys, matcher[0]].transpose().collectEntries()
      def values = v.collectEntries{ k, val ->
      
         def avoid = ['full', 'time'].contains(k)
         if (avoid) return [k, val] // avoid time and entry entries
         else
         {
            if (val)
            {
               if (remove_suffix.contains(k)) return [k, new BigDecimal(val[0..-2])] // remove last character "D" / "Y" / etc
               return [k, new BigDecimal(val)] // fractions
            }
            return [k,val]
         }
      }
      
      this.magnitude = (values['year']    ? (values['year']    * 365 * 24 * 60 * 60) : 0) +
                       (values['month']   ? (values['month']   * 30 * 24 * 60 * 60) : 0) +
                       (values['days']    ? (values['days']    * 24 * 60 * 60) : 0) +
                       (values['hours']   ? (values['hours']   * 60 * 60) : 0) +
                       (values['minutes'] ? (values['minutes'] * 60) : 0) + 
                       (values['seconds'] ? values['seconds']  : 0)
   }
   
   static constraints =  {
      //magnitude(nullable:true) // JUST FOR TESING
   }
}
