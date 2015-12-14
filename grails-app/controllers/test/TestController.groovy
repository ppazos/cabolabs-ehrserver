package test

//import com.thoughtworks.xstream.XStream
import ehr.Ehr
import ehr.clinical_documents.CompositionIndex
import ehr.clinical_documents.IndexDefinition
import common.change_control.Contribution
import common.change_control.Version
import common.generic.AuditDetails
import query.DataCriteria
import query.Query
//import support.identification.CompositionRef // T0004
import common.generic.DoctorProxy
import ehr.clinical_documents.data.DataValueIndex
import java.text.SimpleDateFormat
import grails.util.Holders

class TestController {

   def xmlService
   
   // Para acceder a las opciones de localizacion
   def config = Holders.config.app
   
   // Para hacer consultas en la base
   def formatterDateDB = new SimpleDateFormat( Holders.config.app.l10n.db_date_format )
   
   
   /**
    * Usada desde EMRAPP para obtener compositions de un paciente.
    * 
    * Utiliza CompositionIndex para buscar entre las compositions y devuelve el XML de las compositions que matchean.
    * 
    * @param ehrUid
    * @param subjectId
    * @param fromDate yyyyMMdd
    * @param toDate yyyyMMdd
    * @param archetypeId
    * @return
    */
   def findCompositions(String ehrUid, String subjectId, 
                        String fromDate, String toDate, 
                        String archetypeId, String category)
   {
      
      // 1. Todos los parametros son opcionales pero debe venir por lo menos 1
      // 2. La semantica de pasar 2 o mas parametros es el criterio de and
      // 3. Para implementar como un OR se usaria otro parametro booleano (TODO)
      //
      
      def dFromDate
      def dToDate
      
      // FIXME: cuando sea servicio no hay ui
      if (!ehrUid && !subjectId && !fromDate && !toDate && !archetypeId && !category)
      {
         return // muestro ui para testear busqueda
         //throw new Exception("Debe enviar por lo menos un dato para el criterio de busqueda")
      }
      
      // FIXME: Si el formato esta mal va a tirar una except!
      if (fromDate)
      {
         dFromDate = Date.parse(config.l10n.date_format, fromDate)
      }
      
      if (toDate)
      {
         dToDate = Date.parse(config.l10n.date_format, toDate)
      }
      
      //println dFromDate
      //println dToDate
      
      def idxs = CompositionIndex.withCriteria {
         
         if (ehrUid)
            eq('ehrUid', ehrUid)
         
         if (subjectId)
            eq('subjectId', subjectId)
         
         if (archetypeId)
            eq('archetypeId', archetypeId)
         
         if (category)
            eq('category', category)
            
         if (dFromDate)
            ge('startTime', dFromDate) // greater or equal
         
         if (dToDate)
            le('startTime', dToDate) // lower or equal
            
         eq('lastVersion', true)
      }
      
      // TODO: ui o xml o json (solo index o contenido), ahora tira solo index y en XML
      render(text: idxs as grails.converters.XML, contentType:"text/xml", encoding:"UTF-8")
   }
   
   
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
