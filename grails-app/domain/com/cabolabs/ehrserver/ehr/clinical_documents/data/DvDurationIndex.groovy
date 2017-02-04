/*
 * Copyright 2011-2017 CaboLabs Health Informatics
 *
 * The EHRServer was designed and developed by Pablo Pazos Gutierrez <pablo.pazos@cabolabs.com> at CaboLabs Health Informatics (www.cabolabs.com).
 *
 * You can't remove this notice from the source code, you can't remove the "Powered by CaboLabs" from the UI, you can't remove this notice from the window that appears then the "Powered by CaboLabs" link is clicked.
 *
 * Any modifications to the provided source code can be stated below this notice.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
