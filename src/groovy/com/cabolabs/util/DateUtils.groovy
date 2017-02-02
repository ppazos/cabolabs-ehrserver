
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

package com.cabolabs.util

import java.util.Date
import java.util.Calendar

class DateUtils {

   static Date toFirstDateOfMonth(Date date)
   {
      Calendar c = Calendar.getInstance()
      c.setTime(date)
      // first day of month
      c.set(Calendar.DAY_OF_MONTH, 1)
      // time to zero
      c.set(Calendar.HOUR_OF_DAY, 0)
      c.set(Calendar.MINUTE, 0)
      c.set(Calendar.SECOND, 0)
      return c.getTime()
   }
   
   static Date toLastDateOfMonth(Date date)
   {
      Calendar c = Calendar.getInstance()
      c.setTime(date)
      // next month
      c.add(Calendar.MONTH, 1)
      // first day
      c.set(Calendar.DAY_OF_MONTH, 1)
      // the previous day of next month's first day is the current month last day
      c.add(Calendar.DAY_OF_MONTH, -1)
      // set the time to the last second of the day
      c.set(Calendar.HOUR_OF_DAY, 23)
      c.set(Calendar.MINUTE, 59)
      c.set(Calendar.SECOND, 59)
      return c.getTime()
   }
}
