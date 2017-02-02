
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

package test

import com.cabolabs.ehrserver.ehr.clinical_documents.CompositionIndex
import com.cabolabs.ehrserver.openehr.common.change_control.Contribution
import com.cabolabs.ehrserver.openehr.common.change_control.Version
import com.cabolabs.ehrserver.openehr.common.generic.AuditDetails
import com.cabolabs.ehrserver.query.DataCriteria
import com.cabolabs.ehrserver.query.Query
import com.cabolabs.ehrserver.openehr.common.generic.DoctorProxy
import com.cabolabs.ehrserver.ehr.clinical_documents.data.DataValueIndex

import java.text.SimpleDateFormat
import com.cabolabs.ehrserver.openehr.ehr.Ehr
import grails.util.Holders

class TestController {

   def xmlService
   
   // Para acceder a las opciones de localizacion
   def config = Holders.config.app
   
   // Para hacer consultas en la base
   def formatterDateDB = new SimpleDateFormat( Holders.config.app.l10n.db_date_format )
   
   
   /**
    * Se implemento completa en QueryController
    * 
    * @param name
    * @param qarchetypeId
    * @return
    */
   def saveQueryByData(String name, String qarchetypeId)
   {
      // Datos de criterios
      List archetypeIds = params.list('archetypeId')
      List paths = params.list('path')
      List operands = params.list('operand')
      
      def query = new Query(name:name, qarchetypeId:qarchetypeId, type:'composition') // qarchetypeId puede ser vacio
      
      archetypeIds.eachWithIndex { archId, i ->
         
         query.addToWhere( new DataCriteria(archetypeId:archId, path:paths[i], operand:operands[i]) )
      }
      
      if (!query.save())
      {
         println "query errors: "+ query.errors
      }

      
      render( query as grails.converters.XML )
   }
   

   
   def commitTest()
   {
      //def contrib = new File("test\\resources\\contribution.xml")
      def version1 = new File("test\\resources\\version1.xml")
      def version2 = new File("test\\resources\\version2.xml")
      def version3 = new File("test\\resources\\version3.xml")
      
      return [version1: version1.getText(),
              version2: version2.getText(),
              version3: version3.getText()]
   }
}
