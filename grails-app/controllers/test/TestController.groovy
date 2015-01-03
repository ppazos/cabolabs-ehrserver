package test

//import com.thoughtworks.xstream.XStream
import ehr.Ehr
import ehr.clinical_documents.CompositionIndex
import ehr.clinical_documents.DataIndex
import common.change_control.Contribution
import common.change_control.Version
import common.generic.AuditDetails
import query.DataCriteria
import query.Query
//import support.identification.CompositionRef // T0004
import common.generic.DoctorProxy
import ehr.clinical_documents.data.DataValueIndex
import java.text.SimpleDateFormat
import org.codehaus.groovy.grails.commons.ApplicationHolder

class TestController {

   def xmlService
   
   // Para acceder a las opciones de localizacion
   def config = ApplicationHolder.application.config.app
   
   // Para hacer consultas en la base
   def formatterDateDB = new SimpleDateFormat( ApplicationHolder.application.config.app.l10n.db_date_format )
   
   
   
   /**
    * UI test
    */
   /*
   def rollbackContribution(String uid)
   {
      if (!uid)
      {
         throw new Exception("UID de la contribution es obligatorio")
      }
      
      // TODO
   }
   */
   
   
   /**
    * Usada desde EMRAPP para obtener compositions de un paciente.
    * 
    * Utiliza CompositionIndex para buscar entre las compositions y devuelve el XML de las compositions que matchean.
    * 
    * @param ehrId
    * @param subjectId
    * @param fromDate yyyyMMdd
    * @param toDate yyyyMMdd
    * @param archetypeId
    * @return
    */
   def findCompositions(String ehrId, String subjectId, 
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
      if (!ehrId && !subjectId && !fromDate && !toDate && !archetypeId && !category)
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
         
         if (ehrId)
            eq('ehrId', ehrId)
         
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
            
         eq('isLastVersion', true)
      }
      
      // TODO: ui o xml o json (solo index o contenido), ahora tira solo index y en XML
      render(text: idxs as grails.converters.XML, contentType:"text/xml", encoding:"UTF-8")
   }
   
   
   /**
    * Busqueda de compositions por datos simples (DataValueIndex)
    * 
    * TODO: pasarle criterio de contexto ehrId, composition archId
    * TODO: pasar lista de archId, path, operand, value
    * 
    * @param qehrId filtro de nivel 1
    * @param qarchetypeId filtro de nivel 1
    * @param retrieveData null o false no devuelve datos, true devuelve datos
    * @param showUI true si muestra listado de resultados en UI, false por defecto devuelve XMLs
    * 
    * @return
    */
   /*
    * Se implemento en RestController.queryCompositions
    *
   //def queryByData(String archetypeId, String path, String operand, String value, boolean retrieveData)
   def queryByData(String qehrId, String qarchetypeId, String fromDate, String toDate, boolean retrieveData, boolean showUI)
   {
      println params
      
      // muestra gui
      if (!params.doit)
      {
         return
      }
      
      
      // Viene una lista de cada parametro
      // String archetypeId, String path, String operand, String value
      // El mismo indice en cada lista corresponde con un atributo del mismo criterio de busqueda
      
      
      // Datos de criterios
      List archetypeIds = params.list('archetypeId')
      List paths = params.list('path')
      List operands = params.list('operand')
      List values = params.list('value')
      
      DataIndex dataidx
      String idxtype

      
      // parse de dates
      Date qFromDate
      Date qToDate

      if (fromDate)
      {
         qFromDate = Date.parse(config.l10n.date_format, fromDate)
      }
      
      if (toDate)
      {
         qToDate = Date.parse(config.l10n.date_format, toDate)
      }
      
      
      // Armado de la query
      String q = "FROM CompositionIndex ci WHERE "
      
      // ===============================================================
      // Criteria nivel 1 ehrId
      if (qehrId) q += "ci.ehrId = '" + qehrId + "' AND "
      
      // Criteria nivel 1 archetypeId (solo de composition)
      if (qarchetypeId) q += "ci.archetypeId = '" + qarchetypeId +"' AND "
      
      // Criterio de rango de fechas para ci.startTime
      // Formatea las fechas al formato de la DB
      if (qFromDate) q += "ci.startTime >= '"+ formatterDateDB.format( qFromDate ) +"' AND " // higher or equal
      if (qToDate) q += "ci.startTime <= '"+ formatterDateDB.format( qToDate ) +"' AND " // lower or equal
      
      //
      // ===============================================================
      
      archetypeIds.eachWithIndex { archId, i ->
         
         // Lookup del tipo de objeto en la path para saber los nombres de los atributos
         // concretos por los cuales buscar (la path apunta a datavalue no a sus campos).
         dataidx = DataIndex.findByArchetypeIdAndPath(archId, paths[i])
         idxtype = dataidx?.rmTypeName
         
         println "idxtype: $idxtype"
         
         // Subqueries sobre los DataValueIndex de los CompositionIndex
         q +=
         " EXISTS (" +
         "  SELECT dvi.id" +
         "  FROM DataValueIndex dvi" +
         "  WHERE dvi.owner.id = ci.id" + // Asegura de que todos los EXISTs se cumplen para el mismo CompositionIndex (los criterios se consideran AND, sin esta condicion es un OR y alcanza que se cumpla uno de los criterios que vienen en params)
         "        AND dvi.archetypeId = '"+ archId +"'" +
         "        AND dvi.path = '"+ paths[i] +"'"
         
         // Consulta sobre atributos del DataIndex dependiendo de su tipo
         switch (idxtype)
         {
            case 'DV_DATE_TIME':
               q += "        AND dvi.value"+ operands[i] + values[i] // TODO: verificar formato, transformar a SQL
            break
            case 'DV_QUANTITY':
               q += "        AND dvi.magnitude"+ operands[i] + new Float(values[i])
            break
            case 'DV_CODED_TEXT':
               q += "        AND dvi.code"+ operands[i] +"'"+ values[i]+"'"
            break
            default:
              throw new Exception("type $idxtype not supported")
         }
         q += ")"
         
         
         // Agrega ANDs para los EXISTs, menos el ultimo
         if (i+1 < archetypeIds.size()) q += " AND "
      }
      
      
      println q
      
      
//      EXISTS (
//        SELECT dvi.id
//        FROM DataIndex dvi
//        WHERE dvi.owner.id = ci.id
//              AND dvi.archetypeId = openEHR-EHR-COMPOSITION.encounter.v1
//              AND dvi.path = /content/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value
//              AND dvi.magnitude>140.0
//      ) AND EXISTS (
//        SELECT dvi.id
//        FROM DataIndex dvi
//        WHERE dvi.owner.id = ci.id
//              AND dvi.archetypeId = openEHR-EHR-COMPOSITION.encounter.v1
//              AND dvi.path = /content/data[at0001]/events[at0006]/data[at0003]/items[at0005]/value
//              AND dvi.magnitude<130.0
//      ) AND EXISTS (
//        SELECT dvi.id
//        FROM DataIndex dvi
//        WHERE dvi.owner.id = ci.id
//              AND dvi.archetypeId = openEHR-EHR-COMPOSITION.encounter.v1
//              AND dvi.path = /content/data[at0001]/origin
//              AND dvi.value>20080101
//      )
      
      
      
      // TODO: criterio por atributos del ci
      def cilist = CompositionIndex.findAll( q )

      
      // Muestra compositionIndex/list
      if (showUI)
      {
         // FIXME: hay que ver el tema del paginado
         render(view:'/compositionIndex/list',
                model:[compositionIndexInstanceList: cilist, compositionIndexInstanceTotal:cilist.size()])
         return
      }
      
      // Devuelve CompositionIndex, si quiere el contenido es buscar las
      // compositions que se apuntan por el index
      if (!retrieveData)
      {
         render(text:(cilist as grails.converters.XML), contentType:"text/xml", encoding:"UTF-8")
      }
      else
      {
         // FIXME: hay que armar bien el XML: declaracion de xml solo al
         //        inicio y namespaces en el root.
         //
         //  REQUERIMIENTO:
         //  POR AHORA NO ES NECESARIO ARREGLARLO, listando los index y luego
         //  haciendo get por uid de la composition alcanza. Esto es mas para XRE
         //  para extraer datos con reglas sobre un conjunto de compositions en un
         //  solo XML.
         //
         // FIXME: no genera xml valido porque las compos se guardan con:
         // <?xml version="1.0" encoding="UTF-8"?>
         //
         String buff
         String out = '<?xml version="1.0" encoding="UTF-8"?><list xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://schemas.openehr.org/v1">\n'
         cilist.each { compoIndex ->
            
            // FIXME: verificar que esta en disco, sino esta hay un problema
            //        de sincronizacion entre la base y el FS, se debe omitir
            //        el resultado y hacer un log con prioridad alta para ver
            //        cual fue el error.
            
            // Tiene declaracion de xml
            // Tambien tiene namespace, eso deberia estar en el nodo root 
            //buff = new File("compositions\\"+compoIndex.uid+".xml").getText()
            buff = new File(config.composition_repo + compoIndex.uid +".xml").getText()
            
            buff = buff.replaceFirst('<\\?xml version="1.0" encoding="UTF-8"\\?>', '')
            buff = buff.replaceFirst('xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"', '')
            buff = buff.replaceFirst('xmlns="http://schemas.openehr.org/v1"', '')
            
//            
//             Composition queda:
//               <data archetype_node_id="openEHR-EHR-COMPOSITION.encounter.v1" xsi:type="COMPOSITION">
//             
            
            out += buff + "\n"
         }
         out += '</list>'
         
         render(text: out, contentType:"text/xml", encoding:"UTF-8")
      }
      
   } // queryByData
   */
   
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
   
   
   /**
    * Devuelve una lista de DataIndex.
    * 
    * Accion AJAX/JSON, se usa desde queryByData GUI.
    * 
    * Cuando el usuario selecciona el arquetipo, esta accion
    * le devuelve la informacion de los indices definidos para
    * ese arquetipo; path, nombre, tipo rm, ...
    * 
    * @param archetypeId
    * @return
    */
   def getIndexDefinitions(String archetypeId)
   {
      // TODO: checkear params
      
      def list = DataIndex.findAllByArchetypeId(archetypeId)
      
      // Devuelve solo datos necesarios (sin id de DataIndex, ...)
      def rlist = [] 
      
      for(di in list)
         rlist << [archetypeId: di.archetypeId, archetypePath: di.archetypePath, rmTypeName: di.rmTypeName, name: di.name]
      
      
      render(text:(rlist as grails.converters.JSON), contentType:"application/json", encoding:"UTF-8")
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