package com.cabolabs.ehrserver.ehr.clinical_documents.data

import com.cabolabs.ehrserver.ehr.clinical_documents.CompositionIndex

class DataValueIndex {

   // index
   String templateId
   String archetypeId
   String path
   String archetypePath
   
   CompositionIndex owner
   
   // TODO: ver si puedo sacarle las xpaths al xml
   //       y guardarlas, para un path de arquetipo
   //       pueden haber varias xpaths, esto permite
   //       devolver a los sistemas clientes las xpaths
   //       donde encuentran los datos que buscan en los
   //       xmls que resulten de las queries.
   
   static mapping = {
      tablePerHierarchy false // tabla por subclase
   }
   
   public String toString()
   {
      return this.archetypeId + this.archetypePath
   }
}