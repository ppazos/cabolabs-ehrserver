package ehr

//import com.thoughtworks.xstream.XStream
import common.generic.PatientProxy
import demographic.Person
import common.generic.AuditDetails
import common.generic.DoctorProxy
import ehr.Ehr
import ehr.clinical_documents.CompositionIndex
import ehr.clinical_documents.data.DvCodedTextIndex
import ehr.clinical_documents.data.DvDateTimeIndex
import ehr.clinical_documents.data.DvQuantityIndex
import groovy.util.slurpersupport.GPathResult
import groovy.xml.MarkupBuilder
import common.change_control.Contribution

import org.codehaus.groovy.grails.commons.ApplicationHolder

class EhrController {

   def xmlService
   def apiService
   

   // Para acceder a las opciones de localizacion 
   def config = ApplicationHolder.application.config.app
   
   
   def index() { }
   
   def list(Integer max) {
      params.max = Math.min(max ?: 10, 100)
      [list: Ehr.list(params), total: Ehr.count()]
   }
   
   def show(Long id) {
      def ehrInstance = Ehr.get(id)
      if (!ehrInstance) {
          flash.message = message(code: 'default.not.found.message', args: [message(code: 'ehr.label', default: 'Ehr'), id])
          redirect(action: "list")
          return
      }

      [ehrInstance: ehrInstance]
  }
   
   /**
    * GUI test: devuelve el XML de las compositions commiteadas
    * @param uid
    * @return
    */
   def showComposition(String uid)
   {
      def compo = new File(config.composition_repo + uid +".xml")
      
      render(text:compo.getText(), contentType:"text/xml", encoding:"UTF-8")
   }
   
   // GUI debug
   def showEhr(String patientUID)
   {
      // TODO: patientUID existe?
      
      def c = Ehr.createCriteria()
      def ehr = c.get {
         subject {
            eq ('value', patientUID)
         }
      }
      
      if (!ehr)
      {
         flash.message = "No existe el ehr para el paciente $patientUID"
         redirect(controller:'person', action:'list')
         return
      }

      /*
      // carga lazy
      ehr.contributions.each { contribRef ->
         println contribRef.value
      }
      
      ehr.compositions.each { compoRef ->
         println compoRef.value
      }
      
      XStream xstream = new XStream()
      xstream.omitField(Ehr.class, "errors")
      xstream.omitField(PatientProxy.class, "errors")
      
      String txt = xstream.toXML(ehr)
      
      render(text:txt, contentType:"text/xml", encoding:"UTF-8")
      */
      
      return [ehr: ehr] 
   }
   
   /**
    * Auxiliar de showEhr para mostrar las contributiosn y sus
    * compositions en una tabla y poder filtrarlas.
    * @return
    */
   def ehrContributions(long id, String fromDate, String toDate, String qarchetypeId)
   {
      println "ehrComtrbutions " + params
      def contribs
      def ehr = Ehr.get(id)
      
      // parse de dates
      Date qFromDate
      Date qToDate
      if (fromDate) qFromDate = Date.parse(config.l10n.date_format, fromDate)
      if (toDate) qToDate = Date.parse(config.l10n.date_format, toDate)
      
      // TODO: filtro de 
      //if (qarchetypeId || fromDate || toDate)
      //{
         contribs = Contribution.withCriteria {
            
            eq('ehr', ehr)
            
            // Busca por atributos de CompositionIndex
            // Puede no venir ningun criterio y se deberia devolver
            // todas las contribs del ehr, TODO: paginacion!
            versions {
               data {
                  if (qarchetypeId)
                     eq('archetypeId', qarchetypeId)
                  
                  if (qFromDate)
                     ge('startTime', qFromDate)
                     
                  if (qToDate)
                     le('startTime', qToDate)
               }
            }
         }
      //}
      
      render(template:'ehrContributions', model:[contributions:contribs]) 
   }
   
   /**
    * GUI
    * 
    * @param patientUID uid de la Person con rol paciente
    * @return
    */
   def createEhr(String patientUID)
   {
      // TODO: no tirar excepciones porque pueden llegar a la gui
      if (!patientUID)
      {
         throw new Exception("patientUID es obligatorio")
      }
      
      def person = Person.findByUidAndRole(patientUID, 'pat')
      
      // 1. existe paciente?
      if (!person)
      {
         throw new Exception("el paciente $patientUID no existe")
      }
      
      // 2. el paciente ya tiene EHR?
      def c = Ehr.createCriteria()
      def ehr = c.get {
         subject {
            eq ('value', patientUID)
         }
      }
      
      //def ehr = Ehr.findBySubject(subject)
      
      if (ehr)
      {
         // TODO: ya tiene ehr, no creo nada
         throw new Exception("ya tiene ehr")
      }
      else
      {
         ehr = new Ehr(
            subject: new PatientProxy(
               value: patientUID
            )
         )
         
         if (!ehr.save())
         {
            // TODO: error
            println ehr.errors
         }
      }
      
      redirect(controller:'person', action:'list')
      
   } // createEhr
   
   
   /**
    * Envia una lista de versions para commitear al EHR(ehrId)
    * 
    * @param String ehrId
    * @param List versions
    * @return
    */
   def commit(String ehrId)
   {
      //println "commit "+ params
      
      //new File('params_debug.log') << params.toString()
      
      // TODO: todo debe ser transaccional, se hace toda o no se hace nada...
      
      // 1. ehrId debe venir
      if (!ehrId)
      {
         //render(text:'<result><code>error</code><message>No viene el parametro ehrId</message></result>', contentType:"text/xml", encoding:"UTF-8")
         render(contentType:"text/xml", encoding:"UTF-8") {
            result {
               type {
                  code('AR')                         // application reject
                  codeSystem('HL7::TABLES::TABLE_8') // http://amisha.pragmaticdata.com/~gunther/oldhtml/tables.html
               }
               message('El parametro ehrId es obligatorio')
               code('ISIS_EHR_SERVER::COMMIT::ERRORS::400') // sys::service::concept::code
            }
         }
         
         return
      }
      
      // 2. versions deben venir 1 por lo menos haber una
      if (!params.versions)
      {
         //render(text:'<result><code>error</code><message>No viene el parametro versions</message></result>', contentType:"text/xml", encoding:"UTF-8")
         render(contentType:"text/xml", encoding:"UTF-8") {
            result {
               type {
                  code('AR')                         // application reject
                  codeSystem('HL7::TABLES::TABLE_8') // http://amisha.pragmaticdata.com/~gunther/oldhtml/tables.html
               }
               message('El parametro versions es obligatorio')
               code('ISIS_EHR_SERVER::COMMIT::ERRORS::401') // sys::service::concept::code
            }
         }
         return
      }
      
      def xmlVersions = params.list('versions')
      if (xmlVersions.size() == 0)
      {
         //render(text:'<result><code>error</code><message>No viene ninguna version</message></result>', contentType:"text/xml", encoding:"UTF-8")
         render(contentType:"text/xml", encoding:"UTF-8") {
            result {
               type {
                  code('AR')                         // application reject
                  codeSystem('HL7::TABLES::TABLE_8') // http://amisha.pragmaticdata.com/~gunther/oldhtml/tables.html
               }
               message('El parametro versions esta vacio y debe enviarse por lo menos una version')
               code('ISIS_EHR_SERVER::COMMIT::ERRORS::402') // sys::service::concept::code
            }
         }
         return
      }
      
      
      
      def ehr = Ehr.findByEhrId(ehrId)
      
      // 3. ehr debe existir
      if (!ehr)
      {
         //render(text:'<result><code>error</code><message>EHR no existe</message></result>', contentType:"text/xml", encoding:"UTF-8")
         render(contentType:"text/xml", encoding:"UTF-8") {
            result {
               type {
                  code('AR')                         // application reject
                  codeSystem('HL7::TABLES::TABLE_8') // http://amisha.pragmaticdata.com/~gunther/oldhtml/tables.html
               }
               message('No existe el EHR con ehrId '+ ehrId)
               code('ISIS_EHR_SERVER::COMMIT::ERRORS::403') // sys::service::concept::code
            }
         }
         return
      }
      
      
      // En data esta el XML de la composition recibida
      List parsedCompositions = [] // List<GPathResult>
      def versions = xmlService.parseVersions(ehr, xmlVersions, parsedCompositions)
      
      // test
      // muestra los uids en vacio porque el escritor de xml es lazy,
      // se escribe solo cuando se hace una salida ej. a un stream
      
      /*
      println "ehrController parsedCompositions uids " // + parsedCompositions.uid.value*.text() esto me muestra vacio!!!!
      parsedCompositions.each { pcomp ->
         //println pcomp.uid // vacio
         println new groovy.xml.StreamingMarkupBuilder().bind{ out << pcomp} // mejor que XMLUtil para serializar a String (TODO ver cual es mas rapido)
         println ""
      }
      */
      
      // uid se establece automaticamente
      def contribution = new Contribution(
         audit: new AuditDetails(
            systemId:      'ISIS_EHR_SERVER',
            timeCommitted: new Date(),
            //,
            // changeType solo se soporta 'creation' por ahora
            //
            // El committer de la contribution es el mismo committer de todas
            // las versiones, cada version tiene su committer debe ser el mismo.
            committer: new DoctorProxy(
               name: versions[0].commitAudit.committer.name //parsedContribution.audit.committer.name.text()
            )
         ),
         versions: versions
      )
      
      // test
      if (!contribution.audit.validate())
      {
         // FIXME: debe ser transaccional, sino salva esto, no salva nada...
         println contribution.audit.errors
      }
      
      // FIXME: dejar esta tarea a un job
      // Guarda compositions y crea indices a nivel de documento (nivel 1)
      def compoFile
      def compoIndex
      def startTime
      contribution.versions.eachWithIndex { version, i ->
         
         // Cuidado, genera los xmls con <?xml version="1.0" encoding="UTF-8"?>
         // Guardar la composition en el filesystem
         // TODO: path configurable
         // TODO: guardar en repositorio temporal, no en el de commit definitivo
         // COMPOSITION tiene su uid asignado por el servidor como nombre
         //compoFile = new File("compositions\\"+version.data.value+".xml")
         compoFile = new File(config.composition_repo + version.data.uid +".xml")
         compoFile << groovy.xml.XmlUtil.serialize( parsedCompositions[i] )
         
         
         // Version -> Contribution
         version.contribution = contribution
         
         
         // Agrega composition al EHR
         ehr.addToCompositions( version.data ) // version.data ~ CompositionRef
         
         
         /* 
          * Codigo movido a XmlService
          * 
         // =====================================================================
         // Crea indice para la composition
         // =====================================================================
         
         // -----------------------
         // Obligatorios en el XML: lo garantiza xmlService.parseVersions
         // -----------------------
         //  - composition.category.value con valor 'event' o 'persistent'
         //    - si no esta o tiene otro valor, ERROR
         //  - composition.context.start_time.value
         //    - DEBE ESTAR SI category = 'event'
         //    - debe tener formato completo: 20070920T104614,0156+0930
         //  - composition.@archetype_node_id
         //    - obligatorio el atributo
         //  - composition.'@xsi:type' = 'COMPOSITION'
         // -----------------------
         if (parsedCompositions[i].context.start_time.value)
         {
            // http://groovy.codehaus.org/groovy-jdk/java/util/Date.html#parse(java.lang.String, java.lang.String)
            // Sobre fraccion: http://en.wikipedia.org/wiki/ISO_8601
            // There is no limit on the number of decimal places for the decimal fraction. However, the number of
            // decimal places needs to be agreed to by the communicating parties.
            //
            // TODO: formato de fecha completa que sea configurable
            //       ademas la fraccion con . o , depende del locale!!!
            //startTime = Date.parse("yyyyMMdd'T'HHmmss,SSSSZ", parsedCompositions[i].context.start_time.value.text())
            startTime = Date.parse(config.l10n.datetime_format, parsedCompositions[i].context.start_time.value.text())
         }
         
         compoIndex = new CompositionIndex(
            uid:         version.data.value, // compositionIndex uid
            category:    parsedCompositions[i].category.value.text(), // event o persistent
            startTime:   startTime, // puede ser vacio si category es persistent
            subjectId:   ehr.subject.value,
            ehrId:       ehr.ehrId,
            archetypeId: parsedCompositions[i].@archetype_node_id.text()
         )
         
         if (!compoIndex.save())
         {
            // FIXME: debe ser transaccional, sino salva esto, no salva nada...
            println cindex.errors
         }
         
         // =====================================================================
         // /Crea indice para la composition
         // =====================================================================
         */
      }
      
      
      // Agrega contribution al EHR
      // Ehr -> Contribution (ya salva)
      ehr.addToContributions( contribution ) //new ContributionRef(value: contrib.uid) )
      
      
      //render(text:'<result><code>ok</code><message>EHR guardado</message></result>', contentType:"text/xml", encoding:"UTF-8")
      render(contentType:"text/xml", encoding:"UTF-8") {
         result {
            type {
               code('AA')                         // application reject
               codeSystem('HL7::TABLES::TABLE_8') // http://amisha.pragmaticdata.com/~gunther/oldhtml/tables.html
            }
            message('Commit exitoso al EHR '+ ehrId)
            // ok va sin codigo de error
            //code('ISIS_EHR_SERVER::COMMIT::ERRORS::200') // sys::service::concept::code
         }
      }
      
   } // commit
   
   
   // ===========================================================
   // test: mostrar composition en ui (doc viewer)
   //
   def showCompositionUI(String uid)
   {
      def compoFile
      def compoXML
      def compoParsed
   

      compoFile = new File(config.composition_repo + uid +".xml")
      compoXML = compoFile.getText()
      compoParsed = new XmlSlurper(true, false).parseText(compoXML)
      
      def writer = new StringWriter()
      def xml = new MarkupBuilder(writer)
      toHtml(compoParsed, xml, 'composition')
      
      //render(text: writer.toString(), contentType:"text/html", encoding:"UTF-8")
      return [compositionHtml: writer.toString()]
   }
   
   private void toHtml(GPathResult n, MarkupBuilder builder, String classPath)
   {
      // TODO: clases que sean clase.atributo del RM
      // (ej. OBS.data, HIST.events), asi puedo definir estilos por
      // atributo del RM.
      //
      // TODO: class por tipo del RM.
      //
      // necesito consultar el arquetipo para poder hacerlo o puedo consultar los DataIndex (temporal)
      
      if (n.children().isEmpty())
      {
         builder.div( class:'single_value', n.text() )
      }
      else
      {
         builder.div( class:classPath ) { // TODO: class = rmTypeName
            
            n.children().each { sn ->
               
               toHtml(sn, builder, classPath +'_'+ sn.name())
            }
         }
      }
   }
   
   // /test: showCompositionUI
   // ===========================================================
   
   
   // ===========================================================
   // Esta operacion fue movida a IndexDataJob
   //
   /*
   def indexData() {
      
      // TODO: listar solo las compositions que NO tienen indices
      def compoIdxs = CompositionIndex.list()
      
      // Donde se van a ir guardando los indices
      def indexes = []
      
      def compoFile
      def compoXML
      def compoParsed
      
      // Para cada composition
      // El compoIndex se crea en el commit
      compoIdxs.each { compoIndex ->
      

         compoFile = new File(config.composition_repo + compoIndex.uid +".xml")
         compoXML = compoFile.getText()
         compoParsed = new XmlSlurper(true, false).parseText(compoXML)
         
         //println "root parent: " + compoParsed.'..' // .. es gpath para parent
         
         recursiveIndexData( '', compoParsed, indexes, compoIndex.archetypeId, compoIndex )
      }
      
      indexes.each { didx ->
         
         if (!didx.save())
         {
            println didx.errors
         }
         else
         {
            println " - index created: "+ didx.archetypeId + didx.path +' for compo '+ didx.owner.uid
         }
      }
      
      // test
      render(text: (indexes as grails.converters.XML), contentType:"text/xml", encoding:"UTF-8")
   }
   
   private void recursiveIndexData(String path, GPathResult node, List indexes, String archetypeId, CompositionIndex owner)
   {
      // TODO:
      // Como no todos los nodos tienen el tipo de forma explicita (xsi:type)
      // tengo que consultar al arquetipo para saber el tipo del nodo.
      // Necesito el archetype_id (esta en el XML) y la path (es la idxpath).
      
      // En realidad el tipo lo dice la definicion de los indices (DataIndex),
      // y puedo hacer lookup del tipo usando archetypeId+path.
      

      // Path del arquetipo para el indice
      String idxpath
      
      // La path del root va sin nombre, ej. sin esto el root que es / seria /data
      if (path == '')
      {
         idxpath = '/'
      }
      else if (!node.'@archetype_node_id'.isEmpty()) // Si tiene archetype_node_id
      {
         // Para que los hijos de la raiz no empiecen con //
         if (path == '/') path = ''
         
         // Si es un nodo atNNNN
         if (node.'@archetype_node_id'.text().startsWith('at'))
         {
            idxpath = path + '/' + node.name() + '[' + node.'@archetype_node_id'.text() + ']'
         }
         else // Si es un archetypeId
         {
            idxpath = path + '/' + node.name()
         }
      }
      else // No tiene archetype_node_id
      {
         // Para que los hijos de la raiz no empiecen con //
         if (path == '/') path = ''
         
         idxpath = path + '/' + node.name()
      }
      
      def dataidx = ehr.clinical_documents.DataIndex.findByArchetypeIdAndPath(archetypeId, idxpath)
      //println archetypeId +' '+ idxpath +' '+ dataidx
      String idxtype = dataidx?.rmTypeName
      
      
      // FIXME:
      // Va a haber un problema con multiples datos para las mismas
      // paths de tipos estructurados, ej.: vienen 2 DvQuantity con
      // la misma path (son hermanos), y tengo que garantizar que el
      // magnitude de uno no se mezcla con el units del otro.
      //
      // Esto lo podria manejar con un puntero al element contenedor
      // que seria un id falso de uso interno para indicar que 2 o mas
      // datos simples pertenecen a la misma instancia de un datatype
      // estructurado (en realidad seria como el id de la instancia
      // del datatype)
      
      // Valor a indizar (si es que lo tiene)
      String idxvalue
      
      // Si no tiene hijos, veo si hay valor
      if (node.children().isEmpty())
      {
         idxvalue = node.text()
      }
      else // Si tiene hijos
      {
         // Si es de un tipo de dato indizable por valor 
         if (['DV_DATE_TIME', 'DV_QUANTITY', 'DV_CODED_TEXT'].contains(idxtype))
         {
            def method = 'create_'+idxtype+'_index' // ej. create_DV_CODED_TEXT_index(...)
            def dataIndex = this."$method"(node, archetypeId, idxpath, owner)
            indexes << dataIndex
         }
         else // Si no es indizable por valor, sigue la recursion
         {
            node.children().each { subnode ->
               
               recursiveIndexData( idxpath, subnode, indexes, archetypeId, owner )
            }
         }
      }
      
      
      // Test: indices como datos
      //indexes << [path: idxpath, value: idxvalue, type: idxtype]
      
   } // recursiveIndexData
   
   
   private DvQuantityIndex create_DV_QUANTITY_index(GPathResult node, String archetypeId, String path, CompositionIndex owner)
   {
//      WARNING: el nombre de la tag contenedor puede variar segun el nombre del atributo de tipo DV_QUANTITY
//      <value xsi:type="DV_QUANTITY">
//         <magnitude>120</magnitude>
//         <units>mm[Hg]</units>
//      </value>
      
      return new DvQuantityIndex(
         archetypeId: archetypeId,
         path: path,
         owner: owner,
         magnitude: new Float( node.magnitude.text() ), // float a partir de string
         units: node.units.text()
      )
   }
   
   private DvCodedTextIndex create_DV_CODED_TEXT_index(GPathResult node, String archetypeId, String path, CompositionIndex owner)
   {

//      WARNING: el nombre de la tag contenedor puede variar segun el nombre del atributo de tipo DV_CODED_TEXT.
//      <value xsi:type="DV_CODED_TEXT">
//         <value>Right arm</value>
//         <defining_code>
//            <terminology_id>
//               <value>local</value>
//            </terminology_id>
//            <code_string>at0025</code_string>
//         </defining_code>
//      </value>
      
      return new DvCodedTextIndex(
         archetypeId: archetypeId,
         path: path,
         owner: owner,
         value: node.value.text(),
         code: node.defining_code.code_string.text(),
         terminologyId: node.defining_code.terminology_id.value.text()
      )
   }
   
   private DvDateTimeIndex create_DV_DATE_TIME_index(GPathResult node, String archetypeId, String path, CompositionIndex owner)
   {
//      WARNING: el nombre de la tag contenedor puede variar segun el nombre del atributo de tipo DV_DATE_TIME.
//      <time>
//         <value>20070920T104614,0156+0930</value>
//      </time>
      
      return new DvDateTimeIndex(
         archetypeId: archetypeId,
         path: path,
         owner: owner,
         value: Date.parse(config.l10n.datetime_format, node.value.text())
         //value: Date.parse("yyyyMMdd'T'HHmmss,SSSSZ", node.value.text())
      )
   }
   */
}