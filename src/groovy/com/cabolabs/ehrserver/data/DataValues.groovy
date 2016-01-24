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
   DV_DURATION,
   DV_DATE,
   DV_IDENTIFIER
   
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
   
   public static boolean contains(String test)
   {
      for (DataValues dv : DataValues.values())
      {
         if (dv.name().equals(test)) return true
      }
      return false
   }
   
   public static List valuesList()
   {
      DataValues.values() as List
   }
   
   public static List valuesStringList()
   {
      ( DataValues.values() as List).collect{ it.toString() }
   }
}
