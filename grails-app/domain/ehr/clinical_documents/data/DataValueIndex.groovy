package ehr.clinical_documents.data

import ehr.clinical_documents.CompositionIndex

class DataValueIndex {

   // index
   String archetypeId
   String path
   
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
}