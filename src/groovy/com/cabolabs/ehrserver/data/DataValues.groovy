
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
   DV_IDENTIFIER,
   DV_MULTIMEDIA,
   DV_PARSABLE,
   String, // Need string for the String attributes that need to be indexed from OPTs like INSTRUCTION_DETAILS.activity_id
   LOCATABLE_REF // Not a DV but we have indexed it for querying INSTRUCTION_DETAILS.instruction_id
   
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
