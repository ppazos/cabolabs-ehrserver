package test

import com.thoughtworks.xstream.XStream
import ehr.Ehr
import ehr.clinical_documents.CompositionIndex
import ehr.clinical_documents.DataIndex
<<<<<<< HEAD
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
=======
import common.change_control.Commit
import common.change_control.Contribution
import common.change_control.Version
import common.generic.AuditDetails
import support.identification.CompositionRef
import common.generic.DoctorProxy
import ehr.clinical_documents.data.DataValueIndex
>>>>>>> ff42c414310cae9ca7e6f5f714b11310075dfb0f

class TestController {

   def xmlService
   
<<<<<<< HEAD
   // Para acceder a las opciones de localizacion
   def config = ApplicationHolder.application.config.app
   
   // Para hacer consultas en la base
   def formatterDateDB = new SimpleDateFormat( ApplicationHolder.application.config.app.l10n.db_date_format )
   
   
   
   /**
    * UI test
    */
   /*
=======
   /**
    * UI: prueba de EhrController.createContribution
    * 
    * @param ehrId
    * @param contribution XML de contribution como string
    * @return
    */
   /*
   def createContribution(String ehrId, String contribution)
   {
      println params
      
      if (!ehrId || !contribution)
      {
         // Muestra UI
         return
      }
      
      def ehr = Ehr.findByEhrId(ehrId)
      
      // 1. ehr debe existir
      if (!ehr)
      {
         render(text:'<result><code>error</code><message>EHR no existe</message></result>', contentType:"text/xml", encoding:"UTF-8")
         return
      }
      
      //println ehr
      
      def contrib = xmlService.parseContribution(contribution)
      
      // 2. No puede haber una contrib con el mismo uid
      // TODO: es un unique en el uid
      if (Contribution.countByUid(contrib.uid) > 0)
      {
         throw new Exception("Ya existe una contribution con el uid "+ contrib.uid)
      }
      
      // 3. Debe tener por lo menos una versionRef
      // TODO
      
      
//      XStream xstream = new XStream()
//      xstream.omitField(Contribution.class, "errors")
//      xstream.omitField(AuditDetails.class, "errors")
//      xstream.omitField(Version.class, "errors")
//      xstream.omitField(DoctorProxy.class, "errors")
//      println xstream.toXML(contrib)
//      
      
      if (!contrib.save())
      {
         println contrib.errors
      }
      
      
      // FIXME: esto se deberia hacer en commitContribution punto 3.
      // las contributions no deberian quedar asociadas hasta que se commiteen
      // esto se deberia guardar en un CommitBuilder, un objeto que guarde las
      // referencias necesarias para el commit de manera temporal hasta que se
      // haga el commit definitivo.
      //
      // def contribRef = new ContributionRef(
      //   value: contrib.uid
      // )
      //
      // Ya salva
      //ehr.addToContributions(contribRef)
      
      
      // Commit temporal hasta que se finalice el commit o se haga rollback
      def commit = new Commit(
         ehrId: ehrId,
         contributionId: contrib.uid
      )
      
      if (!commit.save())
      {
         println commit.errors
      }
      
      render(text:'<result><code>ok</code><message>...</message></result>', contentType:"text/xml", encoding:"UTF-8")

   } // createContribution
   */
   
   
   /**
    * UI
    *
    * @param Version
    * @return
    */
   /*
   def addVersion(String version)
   {
      // muestra GUI
      if (!version)
      {
         return
      }
      
      // data[0] tiene el XML de la composition recibida
      List data = []
      def ver = xmlService.parseVersion(version, data)
      
      
      println "Version recibida: "+ ver.uid
      
      // 0. Verificaciones sobre el XML recibido
      // TODO
      
      // -----------------------
      // Obligatorios en el XML:
      // -----------------------
      //  - composition.category.value con valor 'event' o 'persistent'
      //    - si no esta o tiene otro valor, ERROR
      //  - composition.context.start_time.value
      //    - DEBE ESTAR SI category = 'event'
      //  - composition.@archetype_node_id
      //    - obligatorio el atributo
      //  - composition.'@xsi:type' = 'COMPOSITION'
      // -----------------------
      
      
      // 1. existe la contribution?
      def contrib = Contribution.findByUid( ver.contribution.value )
      
      if (!contrib)
      {
         throw new Exception("No existe la contribution")
      }
      
      
      // 2. la contribution referencia a la version?
      // TODO: hacer una consulta especifica para no tener que recorrer todas lsa versions de la contrib (que carga de la base).
      def versionRef = contrib.versions.find{ it.value == ver.uid }
      
      println "versionRef encontrado: " + versionRef
      
      if (!versionRef)
      {
         throw new Exception("La contribution no referencia a la version commiteada")
      }
      
      
      // 3. Verificar que ya no existe una Version con ese uid (evita que sea agregada 2 veces)
      // esto es un unique de Version.uid
      if (Version.countByUid( ver.uid ) != 0)
      {
         throw new Exception("Ya existe una version con uid "+ ver.uid)
      }
      
      
      // 4. Guardar la composition en el filesystem
      // TODO: path configurable
      // TODO: guardar en repositorio temporal, no en el de commit definitivo
      // COMPOSITION tiene su uid asignado por el servidor como nombre
      def compo = new File("compositions\\"+ver.data.value+".xml")
      compo << groovy.xml.XmlUtil.serialize( data[0] )
      
      
      // ===================================================================================================================
      // FIXME: las compositions no deberian quedar disponibles para busqueda hasta que no se haya finalizado el commit.
      // ===================================================================================================================
      
      
      // 5. Guarda la version
      if (!ver.save())
      {
         println ver.errors
      }
      
      
//      XStream xstream = new XStream()
//      xstream.omitField(Version.class, "errors")
//      xstream.omitField(AuditDetails.class, "errors")
//      xstream.omitField(CompositionRef.class, "errors")
//      xstream.omitField(ContributionRef.class, "errors")
//      xstream.omitField(DoctorProxy.class, "errors")
//      println xstream.toXML(ver)
      
      
      // TODO
      render(text:'<result><code>ok</code><message>...</message></result>', contentType:"text/xml", encoding:"UTF-8")
   } // addVersion
   */
   
   /**
    * UI test
    * 
    * @param uid identificador de la contribution a commitear
    * @return
    */
   /*
   def commitContribution(String uid)
   {
      // FIXME: timeCommitted deberia establecerse en esta operacion
      // FIXME: sino se dan las condiciones, se debe hacer rollback
      
      if (!uid)
      {
         throw new Exception("UID de la contribution es obligatorio")
      }
      
      // Contribution debe haber sido enviada previamente con createContribution
      def contrib = Contribution.findByUid(uid)
      
      // 1. Contribution existe?
      if (!contrib)
      {
         // TODO: XML
         throw new Exception("No existe conrtribution $uid")
      }
      
      // 2. Tiene todas las versiones referenciadas agregadas?
      def contribCompleta = true
      //def version
      
      // Por precondicion en createContribution, debe tener por lo menos una versionRef
      contrib.versions.each { versionRef ->
         
         if (Version.countByUid(versionRef.value) == 0) contribCompleta = false
      }
      
      if (!contribCompleta)
      {
         // TODO: XML
         throw new Exception("No se agregaron todas las versiones de la contribution")
      }
      
      
      // 3. Se deberian marcar las compositions que vinieron en las versions
      // de la contribution como disponibles para busqueda.
      // En realidad aqui se deberian agregar las contributions y
      // compositions al EHR
      // TODO
      
      def commit = Commit.findByContributionId(contrib.uid)
      def ehr = commit.getEhr()

            
      // Agrega contribution al Ehr
      // Ehr -> Contribution (ya salva)
      ehr.addToContributions( contrib ) //new ContributionRef(value: contrib.uid) )
      
      
      // Agrega todas las compositions de las versiones de la contribution al Ehr
      // Ehr ->* Composition
      def cindex
      def compoFile
      def compoXML
      def startTime
      contrib.versions.each { version ->
         
         //version = Version.findByUid(versionRef.value)
         
         // TODO: porque no guardo la version que tiene la ref a la composition en lugar de guardar la ref a la composition?
         ehr.addToCompositions( version.data ) // version.data ~ CompositionRef
         
         
         // Genera indices de compositions para busqueda
         
         compoFile = new File("compositions\\"+version.data.value+".xml")
         compoXML = new XmlSlurper(true, false).parseText(compoFile.getText())
         
         println "XML: " + compoXML
         
         // -----------------------
         // Obligatorios en el XML:
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
         if (compoXML.context.start_time.value)
         {
            // http://groovy.codehaus.org/groovy-jdk/java/util/Date.html#parse(java.lang.String, java.lang.String)
            // Sobre fraccion: http://en.wikipedia.org/wiki/ISO_8601
            // There is no limit on the number of decimal places for the decimal fraction. However, the number of
            // decimal places needs to be agreed to by the communicating parties.
            //
            // TODO: formato de fecha completa que sea configurable
            //       ademas la fraccion con . o , depende del locale!!!
            startTime = Date.parse("yyyyMMdd'T'HHmmss,SSSSZ", compoXML.context.start_time.value.text())
         }
         
         cindex = new CompositionIndex(
            uid: version.data.value,
            category: compoXML.category.value.text(), // event o persistent
            startTime: startTime, // puede ser vacio si category es persistent
            subjectId: ehr.subject.value,
            ehrId: ehr.ehrId,
            archetypeId: compoXML.@archetype_node_id.text()
         )
         
         if (!cindex.save())
         {
            println cindex.errors
         }
      }
      
      
      // Elimina el commit temporal
      commit.delete()
      
      
      render(text:'<result><code>ok</code><message>...</message></result>', contentType:"text/xml", encoding:"UTF-8")
   } // commitContribution
   */
   
   /**
    * UI test
    */
>>>>>>> ff42c414310cae9ca7e6f5f714b11310075dfb0f
   def rollbackContribution(String uid)
   {
      if (!uid)
      {
         throw new Exception("UID de la contribution es obligatorio")
      }
      
      // TODO
   }
<<<<<<< HEAD
   */
=======
>>>>>>> ff42c414310cae9ca7e6f5f714b11310075dfb0f
   
   
   /**
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
      
<<<<<<< HEAD
      // FIXME: cuando sea servicio no hay ui
=======
>>>>>>> ff42c414310cae9ca7e6f5f714b11310075dfb0f
      if (!ehrId && !subjectId && !fromDate && !toDate && !archetypeId && !category)
      {
         return // muestro ui para testear busqueda
         //throw new Exception("Debe enviar por lo menos un dato para el criterio de busqueda")
      }
      
      // FIXME: Si el formato esta mal va a tirar una except!
      if (fromDate)
      {
<<<<<<< HEAD
         dFromDate = Date.parse(config.l10n.date_format, fromDate)
=======
         dFromDate = Date.parse("yyyyMMdd", fromDate)
>>>>>>> ff42c414310cae9ca7e6f5f714b11310075dfb0f
      }
      
      if (toDate)
      {
<<<<<<< HEAD
         dToDate = Date.parse(config.l10n.date_format, toDate)
      }
      
      //println dFromDate
      //println dToDate
=======
         dToDate = Date.parse("yyyyMMdd", toDate)
      }
      
      println dFromDate
      println dToDate
>>>>>>> ff42c414310cae9ca7e6f5f714b11310075dfb0f
      
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
<<<<<<< HEAD
      }
      
      // TODO: ui o xml o json (solo index o contenido), ahora tira solo index y en XML
=======
         
         // Date.parse("yyyyMMdd", fromDate)
      }
      
      // TODO:
>>>>>>> ff42c414310cae9ca7e6f5f714b11310075dfb0f
      render(text: idxs as grails.converters.XML, contentType:"text/xml", encoding:"UTF-8")
   }
   
   
   /**
    * Busqueda de datos simples dentro de compositions que cumplen cierto criterio.
    * Datos de nivel 2 por criterio nivel 1.
    * Se utiliza para mostrar datos tabulados y graficas.
    * 
    * @param archetypeId arquetipo donde esta la path al dato que se busca, uno o mas
    * @param path ruta dentro del arquetipo al dato que se busca, una o mas
    * @param qehrId id del ehr (obligatorio, los datos deben ser del mismo ehr/paciente)
    * @param qarchetypeId tipo de composition donde buscar (opcional)
    * @param format xml o json, xml por defecto
    * 
    * @return List<DataValueIndex>
    */
   def queryData(String qehrId, String qarchetypeId, String fromDate, String toDate, String format, String group)
   {
      // muestra gui
      if (!params.doit)
      {
         return
      }
      
      //println params
      
<<<<<<< HEAD
      // FIXME: si format es json, el error deberia devolverse como json!
      
=======
>>>>>>> ff42c414310cae9ca7e6f5f714b11310075dfb0f
      // Verifica parametros
      if (!params.qehrId)
      {
         render(status: 500, text:'<error>debe venir un qehrId</error>', contentType:"text/xml", encoding:"UTF-8")
         return
      }
      
      // TODO: verificar que el ehrId existe
      
      
      if (!params.archetypeId)
      {
         render(status: 500, text:'<error>debe venir por lo menos un archetypeId</error>', contentType:"text/xml", encoding:"UTF-8")
         return
      }
      if (!params.path)
      {
         render(status: 500, text:'<error>debe venir por lo menos una path</error>', contentType:"text/xml", encoding:"UTF-8")
         return
      }
      
      
      // En una consulta EQL archetypeId+path seria el SELECT
      List archetypeIds = params.list('archetypeId')
      List paths = params.list('path')
      
      
      if (archetypeIds.size() == 0)
      {
         render(status: 500, text:'<error>debe venir por lo menos un archetypeId</error>', contentType:"text/xml", encoding:"UTF-8")
         return
      }
      if (paths.size() == 0)
      {
         render(status: 500, text:'<error>debe venir por lo menos una path</error>', contentType:"text/xml", encoding:"UTF-8")
         return
      }
      
      
      // parse de dates
      Date qFromDate
      Date qToDate
<<<<<<< HEAD

      if (fromDate)
      {
         dFromDate = Date.parse(config.l10n.date_format, fromDate)
      }
      
      if (toDate)
      {
         dToDate = Date.parse(config.l10n.date_format, toDate)
      }
=======
      if (fromDate) qFromDate = Date.parse("yyyyMMdd", fromDate)
      if (toDate) qToDate = Date.parse("yyyyMMdd", toDate)
>>>>>>> ff42c414310cae9ca7e6f5f714b11310075dfb0f
      
      
      def res = DataValueIndex.withCriteria {
         
         // SELECT
         or { // matchea algun par archId+path
            archetypeIds.eachWithIndex { archId, i ->
               
               and {
                  eq('archetypeId', archId)
                  eq('path', paths[i])
               }
            }
         }
         
         
         // WHERE
         owner { // CompositionIndex
<<<<<<< HEAD
            eq('ehrId', qehrId) // Ya se verifico que viene el param y que el ehr existe
            
            if (qarchetypeId)
            {
               eq('archetypeId', qarchetypeId) // Arquetipo de composition
=======
            eq('ehrId', params.qehrId) // Ya se verifico que viene el param y que el ehr existe
            
            if (params.qarchetypeId)
            {
               eq('archetypeId', params.qarchetypeId) // Arquetipo de composition
>>>>>>> ff42c414310cae9ca7e6f5f714b11310075dfb0f
            }
            
            if (qFromDate)
               ge('startTime', qFromDate) // greater or equal
            
            if (qToDate)
               le('startTime', qToDate) // lower or equal
         }
         
      }
      
<<<<<<< HEAD
      // TODO: sacar las agrupaciones a operaciones externas para no hacer if ...
      
=======
>>>>>>> ff42c414310cae9ca7e6f5f714b11310075dfb0f
      
      // 1. Agrupacion por owner (fila) una columna por cada path
      //    - Para display tabular
      //
      if (group == 'composition')
      {
         def resHeaders = [:]
         def dataidx
         
         // =========================================================================
         // TODO: obtener el nombre del arquetipo en cada path para usar de header
         // =========================================================================
         
         // Headers para la tabla: 1 col por path, y dentro de cada path 1 col por atributo del DataValue
         // h1: | path1 (DvQuantity) | path2 (DvCodedText) | ... |
         // h2: | magnitude | units  |   code   |  value   | ... |
         //
         // [
         //  path1: [ type:'DV_QUANTITY', attrs:['magnitude','units'] ],
         //  path2: [ type:'DV_CODED_TEXT', attrs:['code','value'],
         //  ...
         // ]
         
         archetypeIds.eachWithIndex { archId, i ->
            
            // Lookup del tipo de objeto en la path para saber los nombres de los atributos
            // concretos por los cuales buscar (la path apunta a datavalue no a sus campos).
            dataidx = DataIndex.findByArchetypeIdAndPath(archId, paths[i])
            
            resHeaders[paths[i]] = [:]
            resHeaders[paths[i]]['type'] = dataidx.rmTypeName
            resHeaders[paths[i]]['name'] = dataidx.name
            
            switch (dataidx.rmTypeName)
            {
               case 'DV_QUANTITY':
                  resHeaders[paths[i]]['attrs'] = ['magnitude', 'units']
               break
               case 'DV_CODED_TEXT':
                  resHeaders[paths[i]]['attrs'] = ['value']
               break
               case 'DV_DATE_TIME':
                  resHeaders[paths[i]]['attrs'] = ['code', 'value']
               break
               default:
                  throw new Exception("type "+dataidx.rmTypeName+" not supported")
            }
         }
         
         
         // Filas de la tabla
         def resGrouped = [:]
         
         
         // DEBUG
         //println res as grails.converters.JSON
         

         // dvis por composition (Map[compo.id] = [dvi, dvi, ...])
         // http://groovy.codehaus.org/groovy-jdk/java/util/Collection.html#groupBy(groovy.lang.Closure)
         def rows = res.groupBy { it.owner.id } // as grails.converters.JSON
         
         //println rows
         
         def dvi
         def col // lista de valores de una columna
         rows.each { compoId, dvis ->
            
            //println compoId + ": " + dvis
            
            resGrouped[compoId] = [:]
            
            // Datos de la composition
            // FIXME: deberia haber por lo menos un dvi, sino esto da error
            resGrouped[compoId]['date'] = dvis[0].owner.startTime
<<<<<<< HEAD
            resGrouped[compoId]['uid']  = dvis[0].owner.uid
=======
            resGrouped[compoId]['uid'] = dvis[0].owner.uid
>>>>>>> ff42c414310cae9ca7e6f5f714b11310075dfb0f
            resGrouped[compoId]['cols'] = []
            
            // Las columnas no incluyen la path porque se corresponden en el indice con la path en resHeaders
            // Cada columna de la fila
            resHeaders.each { path, colData -> // colData = [type:'XX', attrs:['cc','vv']]
               
               //println "header: " + path + " " + colData
               //resGrouped[compoId]['cols']['type'] = idxtype
               
               col = [type: colData['type'], path: path] // pongo la path para debug
               
               // dvi para la columna actual
               dvi = dvis.find{it.path == path && it.owner.id == compoId}
               
<<<<<<< HEAD
               if (dvi)
               {
                  // Datos de cada path seleccionada dentro de la composition
                  switch (colData['type'])
                  {
                     case 'DV_QUANTITY':
                        col['magnitude'] = dvi.magnitude
                        col['units'] = dvi.units
                     break
                     case 'DV_CODED_TEXT':
                        col['value'] = dvi.value
                     break
                     case 'DV_DATE_TIME':
                        col['code'] = dvi.code
                        col['value'] = dvi.value
                     break
                     default:
                        throw new Exception("type "+colData['type']+" not supported")
                  }
                  
                  resGrouped[compoId]['cols'] << col
               }
=======
               // Datos de cada path seleccionada dentro de la composition
               switch (colData['type'])
               {
                  case 'DV_QUANTITY':
                     col['magnitude'] = dvi.magnitude
                     col['units'] = dvi.units
                  break
                  case 'DV_CODED_TEXT':
                     col['value'] = dvi.value
                  break
                  case 'DV_DATE_TIME':
                     col['code'] = dvi.code
                     col['value'] = dvi.value
                  break
                  default:
                     throw new Exception("type "+colData['type']+" not supported")
               }
               
               resGrouped[compoId]['cols'] << col
>>>>>>> ff42c414310cae9ca7e6f5f714b11310075dfb0f
            }
         }
         
         if (!format || format == 'xml')
         {
            render(text:([resHeaders, resGrouped] as grails.converters.XML), contentType:"text/xml", encoding:"UTF-8")
         }
         else if (format == 'json')
         {
            render(text:([resHeaders, resGrouped] as grails.converters.JSON), contentType:"application/json", encoding:"UTF-8")
         }
         else
         {
            render(status: 500, text:'<error>formato no soportado $format</error>', contentType:"text/xml", encoding:"UTF-8")
         }
         
         return
      }
      
      
      // 2. Agrupacion por path (serie)
      //    - Para display en grafica
      //
      if (group == 'path')
      {
         // En este caso los headers son las filas
         def resHeaders = [:]
         def dataidx
         
         // Columnas de la tabla (series)
         def resGrouped = [:]
         
         
         // TODO: necesito la fecha de la composition para cada item de la serie,
         //       el mismo indice en distintas series corresponde la misma fecha
         //       la fecha identifica la fila, y cada serie es una columna.
         
         // FIXME: deberia ser archId+path para que sea absoluta
         //        seria mas facil si archId y path fueran un solo campo
         def cols = res.groupBy { it.path }
         
         
         // TODO: cada serie debe tener el nombre de la path (lookup de DataIndex)
         
         archetypeIds.eachWithIndex { archId, i ->
            
            // Lookup del tipo de objeto en la path para saber los nombres de los atributos
            // concretos por los cuales buscar (la path apunta a datavalue no a sus campos).
            dataidx = DataIndex.findByArchetypeIdAndPath(archId, paths[i])
            
            resGrouped[paths[i]] = [:]
            resGrouped[paths[i]]['type'] = dataidx.rmTypeName // type va en cada columna
            resGrouped[paths[i]]['name'] = dataidx.name // name va en cada columna
            
            // FIXME: hay tipos de datos que no deben graficarse
            // TODO: entregar solo valores segun el tipo de dato, en lugar de devolver DataValueIndexes
            //resGrouped[paths[i]]['serie'] = cols[paths[i]]
            
            resGrouped[paths[i]]['serie'] = []
            
            cols[paths[i]].each { dvi ->
               
               // Datos de cada path seleccionada dentro de la composition
               switch (dataidx.rmTypeName)
               {
                  case 'DV_QUANTITY':
                     resGrouped[paths[i]]['serie'] << [magnitude: dvi.magnitude,
                                                       units:     dvi.units,
                                                       date:      dvi.owner.startTime]
                  break
                  case 'DV_CODED_TEXT':
                     resGrouped[paths[i]]['serie'] << [value:     dvi.value,
                                                       date:      dvi.owner.startTime]
                  break
                  case 'DV_DATE_TIME':
                     resGrouped[paths[i]]['serie'] << [code:      dvi.code,
                                                       value:     dvi.value,
                                                       date:      dvi.owner.startTime]
                  break
                  default:
                     throw new Exception("type "+dataidx.rmTypeName+" not supported")
               }
               
               // para cada fila quiero fecha y uid de la composition
            }
         }
         
         if (!format || format == 'xml')
         {
            render(text:(resGrouped as grails.converters.XML), contentType:"text/xml", encoding:"UTF-8")
         }
         else if (format == 'json')
         {
            render(text:(resGrouped as grails.converters.JSON), contentType:"application/json", encoding:"UTF-8")
         }
         else
         {
            render(status: 500, text:'<error>formato no soportado $format</error>', contentType:"text/xml", encoding:"UTF-8")
         }
         return
      }
      
      
<<<<<<< HEAD
      // Por defecto no agrupa (group = null)
=======
      // Por defecto no agrupa
>>>>>>> ff42c414310cae9ca7e6f5f714b11310075dfb0f
      if (!format || format == 'xml')
      {
         render(text:(res as grails.converters.XML), contentType:"text/xml", encoding:"UTF-8")
      }
      else if (format == 'json')
      {
         render(text:(res as grails.converters.JSON), contentType:"application/json", encoding:"UTF-8")
      }
      else
      {
         render(status: 500, text:'<error>formato no soportado $format</error>', contentType:"text/xml", encoding:"UTF-8")
      }
      
   } // querydata
   
<<<<<<< HEAD

=======
>>>>>>> ff42c414310cae9ca7e6f5f714b11310075dfb0f
   
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
   //def queryByData(String archetypeId, String path, String operand, String value, boolean retrieveData)
<<<<<<< HEAD
   def queryByData(String qehrId, String qarchetypeId, String fromDate, String toDate, boolean retrieveData, boolean showUI)
   {
      println params
      
=======
   def queryByData(String qehrId, String qarchetypeId, boolean retrieveData, boolean showUI)
   {
>>>>>>> ff42c414310cae9ca7e6f5f714b11310075dfb0f
      // muestra gui
      if (!params.doit)
      {
         return
      }
      
<<<<<<< HEAD
      
=======
      println params
>>>>>>> ff42c414310cae9ca7e6f5f714b11310075dfb0f
      
      // Viene una lista de cada parametro
      // String archetypeId, String path, String operand, String value
      // El mismo indice en cada lista corresponde con un atributo del mismo criterio de busqueda
      
      /* ya viene el nombre correcto
      String op
      switch (operand)
      {
         case '=': op = 'eq'
         break
         case '<': op = 'lt'
         break
         case '>': op = 'gt'
         break
         case '!=': op = 'neq'
         break
      }
      */
      
      // Datos de criterios
      List archetypeIds = params.list('archetypeId')
      List paths = params.list('path')
      List operands = params.list('operand')
      List values = params.list('value')
      
      DataIndex dataidx
      String idxtype

      
<<<<<<< HEAD
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
      
      
=======
>>>>>>> ff42c414310cae9ca7e6f5f714b11310075dfb0f
      // Armado de la query
      String q = "FROM CompositionIndex ci WHERE "
      
      // ===============================================================
      // Criteria nivel 1 ehrId
      if (qehrId) q += "ci.ehrId = '" + qehrId + "' AND "
      
      // Criteria nivel 1 archetypeId (solo de composition)
      if (qarchetypeId) q += "ci.archetypeId = '" + qarchetypeId +"' AND "
      
<<<<<<< HEAD
      // Criterio de rango de fechas para ci.startTime
      // Formatea las fechas al formato de la DB
      if (qFromDate) q += "ci.startTime >= '"+ formatterDateDB.format( qFromDate ) +"' AND " // higher or equal
      if (qToDate) q += "ci.startTime <= '"+ formatterDateDB.format( qToDate ) +"' AND " // lower or equal
=======
      // TODO: criterio de rango de fechas para ci.startTime
>>>>>>> ff42c414310cae9ca7e6f5f714b11310075dfb0f
      
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
      
      /*
      EXISTS (
        SELECT dvi.id
        FROM DataIndex dvi
        WHERE dvi.owner.id = ci.id
              AND dvi.archetypeId = openEHR-EHR-COMPOSITION.encounter.v1
              AND dvi.path = /content/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value
              AND dvi.magnitude>140.0
      ) AND EXISTS (
        SELECT dvi.id
        FROM DataIndex dvi
        WHERE dvi.owner.id = ci.id
              AND dvi.archetypeId = openEHR-EHR-COMPOSITION.encounter.v1
              AND dvi.path = /content/data[at0001]/events[at0006]/data[at0003]/items[at0005]/value
              AND dvi.magnitude<130.0
      ) AND EXISTS (
        SELECT dvi.id
        FROM DataIndex dvi
        WHERE dvi.owner.id = ci.id
              AND dvi.archetypeId = openEHR-EHR-COMPOSITION.encounter.v1
              AND dvi.path = /content/data[at0001]/origin
              AND dvi.value>20080101
      )
      */
      
      
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
<<<<<<< HEAD
            //buff = new File("compositions\\"+compoIndex.uid+".xml").getText()
            buff = new File(config.composition_repo + compoIndex.uid +".xml").getText()
=======
            buff = new File("compositions\\"+compoIndex.uid+".xml").getText()
>>>>>>> ff42c414310cae9ca7e6f5f714b11310075dfb0f
            
            buff = buff.replaceFirst('<\\?xml version="1.0" encoding="UTF-8"\\?>', '')
            buff = buff.replaceFirst('xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"', '')
            buff = buff.replaceFirst('xmlns="http://schemas.openehr.org/v1"', '')
            
            /**
             * Composition queda:
             *   <data archetype_node_id="openEHR-EHR-COMPOSITION.encounter.v1" xsi:type="COMPOSITION">
             */
            
            out += buff + "\n"
         }
         out += '</list>'
         
         render(text: out, contentType:"text/xml", encoding:"UTF-8")
      }
      
   } // queryByData
   
   
   /**
<<<<<<< HEAD
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
=======
>>>>>>> ff42c414310cae9ca7e6f5f714b11310075dfb0f
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
         rlist << [archetypeId: di.archetypeId, path: di.path, rmTypeName: di.rmTypeName, name: di.name]
      
      
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