package com.cabolabs.util

import java.util.Date
import java.util.TimeZone
import grails.util.Holders
import java.text.ParseException
import java.text.SimpleDateFormat

class DateParser {

   static def config = Holders.config.app
   
   public DateParser() {
      // TODO Auto-generated constructor stub
   }
   
   static Date tryParse(String dateString)
   {
      //println "tryParse "+ dateString
      def supported_formats = [
         config.l10n.datetime_format,
         config.l10n.ext_datetime_format,
         config.l10n.ext_datetime_format_point,
         config.l10n.ext_datetime_utcformat,
         config.l10n.ext_datetime_utcformat_point
      ]
      def d
      for (String format : supported_formats)
      {
         try
         {
            // If the date ends in Z, it's timezone is UTC
            // It the TZ is not present, the parser sets the local timezone and should be UTC.
            // This forces to use UTC.
            if (dateString.endsWith('Z'))
            {
               //def tz = TimeZone.getTimeZone('UTC')
               //println "tryParse TZ "+ tz + ' ' + tz.class
               //d = Date.parse(format, dateString, tz) // get an exception method doesnt exists

               SimpleDateFormat sdf = new SimpleDateFormat(format)
               sdf.setTimeZone(TimeZone.getTimeZone("UTC"))
               d = sdf.parse(dateString)
            }
            else
            {
               d = Date.parse(format, dateString)
            }

            return d
         }
         catch (ParseException e) {}
      }

      return null
   }

}
