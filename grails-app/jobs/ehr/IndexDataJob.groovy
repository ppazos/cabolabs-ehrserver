package ehr

import java.util.List;

import ehr.clinical_documents.CompositionIndex
import ehr.clinical_documents.data.*
import groovy.util.slurpersupport.GPathResult

import org.codehaus.groovy.grails.commons.ApplicationHolder

/**
 * Indexa datos de compositions commiteadas.
 * 
 * @author Pablo Pazos Gutierrez <pablo.pazos@cabolabs.com>
 */
class IndexDataJob {
   
   static triggers = {
      simple repeatInterval: 60000l // execute job once in 60 seconds
   }
   
   // Para acceder a la config que dice de donde leer las compositions a indexar
   def config = ApplicationHolder.application.config.app

   def execute()
   {
      println "IndexDataJob"
      
      // Lista de CompositionIndex para las que no se han creado indices de DataValue
      def compoIdxs = CompositionIndex.findAllByDataIndexed(false)
      
      // Donde se van a ir guardando los indices
      def indexes = []
      
      def compoFile
      def compoXML
      def compoParsed
      
      // Para cada composition
      // El compoIndex se crea en el commit
      compoIdxs.each { compoIndex ->
      
         println "Indexacion de composition: " + compoIndex.uid
         
         indexes = []
         
         compoFile = new File(config.composition_repo + compoIndex.uid +".xml")
         compoXML = compoFile.getText()
         compoParsed = new XmlSlurper(true, false).parseText(compoXML)
         
         //println "root parent: " + compoParsed.'..' // .. es gpath para parent
         
         println "templateId 0: "+ compoIndex.templateId
         
         recursiveIndexData( '', '', compoParsed, indexes, compoIndex.templateId, compoIndex.archetypeId, compoIndex )
         
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
         
         // Marca como indexado
         compoIndex.dataIndexed = true
         
         if (!compoIndex.save())
         {
            println "Error al guardar compoIndex: "+ compoIndex.errors
         }
      }
   } // execute
   
   /**
    * 
    * @param path parent node path, absolute to the template, empty for the root node.
    * @param archetypePath absolute path to a root archetype but not to the template, used for querying.
    * @param node
    * @param indexes will contain all the indexes created by the recursion
    * @param templateId
    * @param archetypeId
    * @param owner
    */
   private void recursiveIndexData(
      String path, String archetypePath,
      GPathResult node, List indexes, 
      String templateId, String archetypeId,
      CompositionIndex owner)
   {
      println "templateId 1: "+ templateId
      
      // TODO:
      // Como no todos los nodos tienen el tipo de forma explicita (xsi:type)
      // tengo que consultar al arquetipo para saber el tipo del nodo.
      // Necesito el archetype_id (esta en el XML) y la path (es la idxpath).
      
      // En realidad el tipo lo dice la definicion de los indices (DataIndex),
      // y puedo hacer lookup del tipo usando archetypeId+path.
      

      // Path del template para el indice (absoluta)
      String idxpath
      
      // La path del root va sin nombre, ej. sin esto el root que es / seria /data
      if (path == '')
      {
         idxpath = '/'
         archetypePath = '/'
      }
      else if (!node.'@archetype_node_id'.isEmpty()) // Si tiene archetype_node_id
      {
         // Para que los hijos de la raiz no empiecen con //
         if (path == '/') path = ''
         if (archetypePath == '/') archetypePath = ''
         
         // Si es un nodo atNNNN
         if (node.'@archetype_node_id'.text().startsWith('at'))
         {
            idxpath = path + '/' + node.name() + '[' + node.'@archetype_node_id'.text() + ']'
            archetypePath = archetypePath + '/' + node.name() + '[' + node.'@archetype_node_id'.text() + ']'
         }
         else // Si es un archetypeId
         {
            //idxpath = path + '/' + node.name()
            idxpath = path + '/' + node.name() + '[archetype_id='+ node.'@archetype_node_id'.text() +']'
            archetypePath = '/' // This node is an archetype root because it has an archetypeId
            archetypeId = node.'@archetype_node_id'.text()
         }
      }
      else // No tiene archetype_node_id
      {
         // Para que los hijos de la raiz no empiecen con //
         if (path == '/') path = ''
         if (archetypePath == '/') archetypePath = ''
         
         idxpath = path + '/' + node.name()
         archetypePath = archetypePath + '/' + node.name()
      }
      
      println "tempPath: "+ idxpath
      println "archPath: "+ archetypePath
      
      // TODO: instead of calculating the archetypePath, I can use the templateId and path
      //       to query DataIndex and get the archetypeId and archetypePath from there.
      // DataIndex uses the archetypeId and archetypePath to search
      def dataidx = ehr.clinical_documents.DataIndex.findByArchetypeIdAndArchetypePath(archetypeId, archetypePath)
      
      if (!dataidx)
      {
         println "DataIndex not found for "+ archetypeId +" and "+ archetypePath
      }
      
      // Si dataidx es nulo, idxtype sera nulo y luego idvalue tambien,
      // pero la recursion sigue. Esos valores solo se necesitan cuando
      // hay un nodo indexable, no para todos los parents.
      
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
         // FIXME: this is a bug on adl parser it uses Java types instead of RM ones
         switch (idxtype)
         {
            case 'DvDateTime':  idxtype = 'DV_DATE_TIME'
            break
            case 'DvQuantity':  idxtype = 'DV_QUANTITY'
            break
            case 'DvCodedText': idxtype = 'DV_CODED_TEXT'
            break
            case 'DvText':      idxtype = 'DV_TEXT'
            break
            case 'DvBoolean':   idxtype = 'DV_BOOLEAN'
            break
         }
         
         // Si es de un tipo de dato indizable por valor
         if (['DV_DATE_TIME', 'DV_QUANTITY', 'DV_CODED_TEXT', 'DV_TEXT', 'DV_BOOLEAN'].contains(idxtype))
         {
            def method = 'create_'+idxtype+'_index' // ej. create_DV_CODED_TEXT_index(...)
            def dataIndex = this."$method"(node, templateId, idxpath, archetypeId, archetypePath, owner)
            indexes << dataIndex
         }
         else // Si no es indizable por valor, sigue la recursion
         {
            node.children().each { subnode ->
               
               recursiveIndexData( idxpath, archetypePath, subnode, indexes, templateId, archetypeId, owner )
            }
         }
      }
   } // recursiveIndexData
   
   
   private DvQuantityIndex create_DV_QUANTITY_index(
      GPathResult node,
      String templateId, String path,
      String archetypeId, String archetypePath,
      CompositionIndex owner)
   {
      println "templateId 2: "+ templateId
      
      /*
       * WARNING: el nombre de la tag contenedor puede variar segun el nombre del atributo de tipo DV_QUANTITY
      <value xsi:type="DV_QUANTITY">
         <magnitude>120</magnitude>
         <units>mm[Hg]</units>
      </value>
      */
      return new DvQuantityIndex(
         templateId: templateId,
         archetypeId: archetypeId,
         path: path,
         archetypePath: archetypePath,
         owner: owner,
         magnitude: new Float( node.magnitude.text() ), // float a partir de string
         units: node.units.text()
      )
   }
   
   private DvTextIndex create_DV_TEXT_index(
      GPathResult node, 
      String templateId, String path,
      String archetypeId, String archetypePath,
      CompositionIndex owner)
   {
      /*
       * WARNING: el nombre de la tag contenedor puede variar segun el nombre del atributo de tipo DV_TEXT.
      <value xsi:type="DV_TEXT">
         <value>Right arm</value>
      </value>
      */
      
      return new DvTextIndex(
         templateId: templateId,
         archetypeId: archetypeId,
         path: path,
         archetypePath: archetypePath,
         owner: owner,
         value: node.value.text()
      )
   }
   
   private DvBooleanIndex create_DV_BOOLEAN_index(
      GPathResult node, 
      String templateId, String path,
      String archetypeId, String archetypePath,
      CompositionIndex owner)
   {
      /*
       * WARNING: el nombre de la tag contenedor puede variar segun el nombre del atributo de tipo DV_TEXT.
       * <value xsi:type="DV_BOOLEAN">
       *   <value>true</value>
       * </value>
       */
      
      return new DvBooleanIndex(
         templateId: templateId,
         archetypeId: archetypeId,
         path: path,
         archetypePath: archetypePath,
         owner: owner,
         value: new Boolean(node.value.text())
      )
   }
   
   private DvCodedTextIndex create_DV_CODED_TEXT_index(
      GPathResult node, 
      String templateId, String path,
      String archetypeId, String archetypePath,
      CompositionIndex owner)
   {
      /*
       * WARNING: el nombre de la tag contenedor puede variar segun el nombre del atributo de tipo DV_CODED_TEXT.
      <value xsi:type="DV_CODED_TEXT">
         <value>Right arm</value>
         <defining_code>
            <terminology_id>
               <value>local</value>
            </terminology_id>
            <code_string>at0025</code_string>
         </defining_code>
      </value>
      */
      
      return new DvCodedTextIndex(
         templateId: templateId,
         archetypeId: archetypeId,
         path: path,
         archetypePath: archetypePath,
         owner: owner,
         value: node.value.text(),
         code: node.defining_code.code_string.text(),
         terminologyId: node.defining_code.terminology_id.value.text()
      )
   }
   
   private DvDateTimeIndex create_DV_DATE_TIME_index(
      GPathResult node, 
      String templateId, String path,
      String archetypeId, String archetypePath,
      CompositionIndex owner)
   {
      /*
       * WARNING: el nombre de la tag contenedor puede variar segun el nombre del atributo de tipo DV_DATE_TIME.
      <time>
         <value>20070920T104614,0156+0930</value>
      </time>
      */
      return new DvDateTimeIndex(
         templateId: templateId,
         archetypeId: archetypeId,
         path: path,
         archetypePath: archetypePath,
         owner: owner,
         value: Date.parse(config.l10n.datetime_format, node.value.text())
         //value: Date.parse("yyyyMMdd'T'HHmmss,SSSSZ", node.value.text())
      )
   }
}