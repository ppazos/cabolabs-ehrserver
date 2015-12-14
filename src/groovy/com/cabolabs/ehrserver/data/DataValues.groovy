package com.cabolabs.ehrserver.data

public enum DataValues {
   
   DV_DATE_TIME,
   DV_QUANTITY,
   DV_CODED_TEXT,
   DV_TEXT,
   DV_BOOLEAN,
   DV_COUNT,
   DV_PROPORTION,
   DV_ORDINAL,
   DV_DURATION
   
   /**
    * This is to solve a bug on adl parser it uses Java types instead of RM ones
    * @param type
    * @return
    */
   static DataValues valueOfString(String type)
   {
      // Avoids null exception
      if (!type) throw new IllegalArgumentException("Type is null or empty")
      
      DataValues dv = DataValues.valueOf(type) // IllegalArgumentException if not valid
      
      return dv
   }
}
