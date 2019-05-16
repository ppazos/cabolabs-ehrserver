package com.cabolabs.ehrserver.reporting

class ErrorLog extends ActivityLog {

   String message
   String trace

   static constraints = {
      trace(maxSize: 4096)
   }

   /*
   trace will save something like this:
   try
   {
      throw new Exception("probanding")
   } catch (Exception e)
   {
      println e.traceString(10)
   }
   */

}
