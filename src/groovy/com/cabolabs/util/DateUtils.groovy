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
