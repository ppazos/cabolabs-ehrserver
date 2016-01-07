package com.cabolabs.ehrserver.data

import com.cabolabs.ehrserver.exceptions.DataIndexException
import com.cabolabs.ehrserver.ehr.clinical_documents.CompositionIndex
import com.cabolabs.ehrserver.ehr.clinical_documents.data.DvBooleanIndex
import com.cabolabs.ehrserver.ehr.clinical_documents.data.DvCodedTextIndex
import com.cabolabs.ehrserver.ehr.clinical_documents.data.DvCountIndex
import com.cabolabs.ehrserver.ehr.clinical_documents.data.DvDateTimeIndex
import com.cabolabs.ehrserver.ehr.clinical_documents.data.DvDurationIndex
import com.cabolabs.ehrserver.ehr.clinical_documents.data.DvOrdinalIndex
import com.cabolabs.ehrserver.ehr.clinical_documents.data.DvProportionIndex
import com.cabolabs.ehrserver.ehr.clinical_documents.data.DvQuantityIndex
import com.cabolabs.ehrserver.ehr.clinical_documents.data.DvTextIndex
import grails.transaction.Transactional
import groovy.util.slurpersupport.GPathResult
import grails.util.Holders
import org.xml.sax.ErrorHandler
import com.cabolabs.util.DateParser
import com.cabolabs.ehrserver.data.DataValues
import com.cabolabs.ehrserver.ehr.clinical_documents.IndexDefinition

@Transactional
class DataIndexerService {

   def config = Holders.config.app
   
   def generateIndexes()
   {
      println 'generateIndexes'
     // compositions with data to be indexed
     def compoIdxs = CompositionIndex.findAllByDataIndexed(false)
     
     // created indexes will be loaded here
     def indexes = []

     def version, versionFile, versionXml, parsedVersion, compoParsed
     
     
     // Error handler to avoid:
     // Warning: validation was turned on but an org.xml.sax.ErrorHandler was not
     // set, which is probably not what is desired.  Parser will use a default
     // ErrorHandler to print the first 10 errors.  Please call
     // the 'setErrorHandler' method to fix this.
     def message
     def parser = new XmlSlurper(false, false)
//     parser.setErrorHandler( { message = it.message } as ErrorHandler ) // https://github.com/groovy/groovy-core/blob/master/subprojects/groovy-xml/src/test/groovy/groovy/xml/XmlUtilTest.groovy
     
     
     // Para cada composition
     // El compoIndex se crea en el commit
     compoIdxs.each { compoIndex ->
     
       //println "Indexacion de composition: " + compoIndex.uid
       
       indexes = []
       
       // load xml file from filesystem
       version = compoIndex.getParent()
       versionFile = new File(config.version_repo + version.uid.replaceAll('::', '_') +".xml")
       
       // If the commit record was saved in the database but the file was not yet written to disk,
       // the file doesn't exists. Leave it to the next run of the indexed and continue indexing.
       if (!versionFile.exists())
       {
          log.info "avoid indexing "+ versionFile.absolutePath
          return // Continue with next compoIdx
       }
       
       versionXml = versionFile.getText()
       parsedVersion = parser.parseText(versionXml)
       
       // error from error handler?
//       if (message)
//       {
//         println "IndexDataJob XML ERROR: "+ message
//         message = null // empty for the next parse
//       }
       
       
       compoParsed = parsedVersion.data
       
       recursiveIndexData( '', '', compoParsed, indexes, compoIndex.templateId, compoIndex.archetypeId, compoIndex )
       
       indexes.each { didx ->
         
         if (!didx.save())
         {
            log.info didx.errors.toString()
            // if one index created fails to save, the whole indexing process is rolled back
            throw new DataIndexException('Index failed to save', didx.errors, didx.toString())
         }
         else
         {
            log.info "index created: "+ didx.archetypeId + didx.archetypePath +' for compo '+ didx.owner.uid
         }
       }
       
       // Marca como indexado
       compoIndex.dataIndexed = true
       
       if (!compoIndex.save())
       {
         log.info "Error al guardar compoIndex: "+ compoIndex.errors.toString()
       }
     }
   }
   
   /**
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
      //println "templateId 1: "+ templateId
      
      // TODO:
      // Como no todos los nodos tienen el tipo de forma explicita (xsi:type)
      // tengo que consultar al arquetipo para saber el tipo del nodo.
      // Necesito el archetype_id (esta en el XML) y la path (es la idxpath).
      
      // En realidad el tipo lo dice la definicion de los indices (IndexDefinition),
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
      
      //println "tempPath: "+ idxpath
      //println "archPath: "+ archetypePath
      
      // TODO: instead of calculating the archetypePath, I can use the templateId and path
      //      to query IndexDefinition and get the archetypeId and archetypePath from there.
      // IndexDefinition uses the archetypeId and archetypePath to search
      def dataidx = IndexDefinition.findByArchetypeIdAndArchetypePath(archetypeId, archetypePath)
      
      
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
      

      // if the node has children (all datavalues have children!)
      if (!node.children().isEmpty())
      {
         try
         {
            if (dataidx)
            {
               log.info "IndexDefinition FOUND for "+ archetypeId +" and "+ archetypePath
               
               String idxtype = dataidx.rmTypeName
               def type = DataValues.valueOfString(idxtype)
               def method = 'create_'+type+'_index' // ej. create_DV_CODED_TEXT_index(...)
               
               log.info 'call '+ method
               
               def indexDefinition = this."$method"(node, templateId, idxpath, archetypeId, archetypePath, owner)
               indexes << indexDefinition
            }
         }
         catch (IllegalArgumentException ex)
         {
            // no need to process the except, is just that the current type should not be indexed
         }
         
         // follow the recursion if there are children nodes
         node.children().each { subnode ->
             
            recursiveIndexData( idxpath, archetypePath, subnode, indexes, templateId, archetypeId, owner )
         }
      }
   } // recursiveIndexData
   
   
   /* ---------------------------------------------------------------------
    * Methods to create individual indexes for each datavalue
    */
   
   private DvQuantityIndex create_DV_QUANTITY_index(
      GPathResult node,
      String templateId, String path,
      String archetypeId, String archetypePath,
      CompositionIndex owner)
   {
      //println "templateId 2: "+ templateId
      
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
        magnitude: new Double( node.magnitude.text() ),
        units: node.units.text()
      )
   }
   
   private DvCountIndex create_DV_COUNT_index(
      GPathResult node,
      String templateId, String path,
      String archetypeId, String archetypePath,
      CompositionIndex owner)
   {
      /*
      <value xsi:type="DV_COUNT">
        <magnitude>120</magnitude>
      </value>
      */
      return new DvCountIndex(
        templateId: templateId,
        archetypeId: archetypeId,
        path: path,
        archetypePath: archetypePath,
        owner: owner,
        magnitude: new Long( node.magnitude.text() )
      )
   }
   
   private DvDurationIndex create_DV_DURATION_index(
      GPathResult node,
      String templateId, String path,
      String archetypeId, String archetypePath,
      CompositionIndex owner)
   {
      println "DV_DURATION "+ node.toString()
      /*
      * WARNING: el nombre de la tag contenedor puede variar segun el nombre del atributo de tipo DV_QUANTITY
      <value xsi:type="DV_DURATION">
        <value>PT30M</value> // 30 mins
      </value>
      */
      return new DvDurationIndex(
        templateId: templateId,
        archetypeId: archetypeId,
        path: path,
        archetypePath: archetypePath,
        owner: owner,
        value: node.value.text()
        //magnitude: new Double( node.magnitude.text() ) // TODO: parse duration in seconds using Joda time.
      )
   }
   
 
   private DvProportionIndex create_DV_PROPORTION_index(
      GPathResult node,
      String templateId, String path,
      String archetypeId, String archetypePath,
      CompositionIndex owner)
   {
      /**
      * <xs:complexType name="DV_ORDERED" abstract="true">
           <xs:complexContent>
               <xs:extension base="DATA_VALUE">
                  <xs:sequence>
                      <xs:element name="normal_range" type="DV_INTERVAL" minOccurs="0"/>
                      <xs:element name="other_reference_ranges" type="REFERENCE_RANGE" minOccurs="0" maxOccurs="unbounded"/>
                      <xs:element name="normal_status" type="CODE_PHRASE" minOccurs="0"/>
                  </xs:sequence>
               </xs:extension>
           </xs:complexContent>
       </xs:complexType>
      * <xs:complexType name="DV_QUANTIFIED" abstract="true">
           <xs:complexContent>
               <xs:extension base="DV_ORDERED">
                  <xs:sequence>
                      <xs:element name="magnitude_status" type="xs:string" minOccurs="0"/>
                  </xs:sequence>
               </xs:extension>
           </xs:complexContent>
       </xs:complexType>
      * <xs:complexType name="DV_AMOUNT">
           <xs:complexContent>
               <xs:extension base="DV_QUANTIFIED">
                  <xs:sequence>
                      <xs:element name="accuracy" type="xs:float" minOccurs="0" default="-1.0"/>
                      <xs:element name="accuracy_is_percent" type="xs:boolean" minOccurs="0"/>
                  </xs:sequence>
               </xs:extension>
           </xs:complexContent>
       </xs:complexType>
      * <xs:complexType name="DV_PROPORTION">
           <xs:complexContent>
               <xs:extension base="DV_AMOUNT">
                  <xs:sequence>
                      <xs:element name="numerator" type="xs:float"/>
                      <xs:element name="denominator" type="xs:float"/>
                      <xs:element name="type" type="PROPORTION_KIND"/>
                      <xs:element name="precision" type="xs:int" default="-1" minOccurs="0"/>
                  </xs:sequence>
               </xs:extension>
           </xs:complexContent>
       </xs:complexType>
       
       
       <value xsi:type="DV_PROPORTION">
         <normal_range>...</normal_range>
         <other_reference_ranges>...</other_reference_ranges>
         <normal_status>...</normal_status>
         <magnitude_status>=</magnitude_status>
         <accuracy>0.5</accuracy>
         <accuracy_is_percent>false</accuracy_is_percent>
         <numerator></numerator>
         <denominator></denominator>
         <type></type>
         <precision></precision>
       </value>
      */
      
      // 0 = pk_ration: num and denom may be any value so are float
      int type = ( (node.type.text()) ? (new Integer(node.type.text())) : 0 )
      
      println "DvPropotion parse type: "+ type
      
      // Parsing numerator and denominator considering the type
      // Some checks are done here instead as constraints of DvProportionIndex,
      // that's ok because the checks are done for parsing the data correctly,
      // not to validate the data itself.
      def numerator
      def denominator
      switch (type)
      {
        case 0: // pk_ratio = 0 num and denom may be any value
          numerator = new Double(node.numerator.text())
          denominator = new Double(node.denominator.text())
        break
        case 1: // pk_unitary = 1 denominator must be 1
          numerator = new Double(node.numerator.text())
          if (node.denominator.text() != "1") throw new Exception("DV_PROPORTION For proportion kind unitary, denominator should be 1")
          denominator = 1
        break
        case 2: // pk_percent = 2 denominator is 100, numerator is understood as a percentage
          numerator = new Double(node.numerator.text())
          if (node.denominator.text() != "100") throw new Exception("DV_PROPORTION For proportion kind percent, denominator should be 100")
          denominator = 100
        break
        case 3: // pk_fraction = 3 num and denum are integral and the presentation method used a slash e.g. 1/2
          numerator = new Double(node.numerator.text())
          denominator = new Double(node.denominator.text())
          if (!isIntegral(numerator)) throw new Exception("DV_PROPORTION For proportion kind fraction, numerator should be intetral and is ${numerator.getClass()}")
          if (!isIntegral(denominator)) throw new Exception("DV_PROPORTION For proportion kind fraction, denominator should be intetral and is ${denominator.getClass()}")
        break
        case 4: // pk_integer_fraction = 4 num and denom are integral, usual presentation is n/d; if numerator > denominator, display as “a b/c”, i.e. the integer part followed by the remaining fraction part, e.g. 1 1/2;
          numerator = new Double(node.numerator.text())
          denominator = new Double(node.denominator.text())
          if (!isIntegral(numerator)) throw new Exception("DV_PROPORTION For proportion kind integer fraction, numerator should be intetral and is ${numerator.getClass()}")
          if (!isIntegral(denominator)) throw new Exception("DV_PROPORTION For proportion kind integer fraction, denominator should be intetral and is ${denominator.getClass()}")
        break
        default:
          throw new Exception("DV_PROPORTION type '$type' not valid")
      }
      
      println "DvPropotion parse: "+ numerator +"/"+ denominator
      
      return new DvProportionIndex(
        templateId: templateId,
        archetypeId: archetypeId,
        path: path,
        archetypePath: archetypePath,
        owner: owner,
        numerator: numerator,
        denominator: denominator,
        type: type,
        precision: ((node.precision.text()) ? new Integer(node.precision.text()) : -1)
      )
   }
   
   private boolean isIntegral(double num) {
      return (Math.floor(num1) == num1)
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
      
      // Throws an exception if the node has xsi:type="..." attribute,
      // because the xmlns:xsi is not defined in the node.
      //println "DvCodedTextIndex "+ groovy.xml.XmlUtil.serialize(node)
      
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
   
   
   private DvOrdinalIndex create_DV_ORDINAL_index(
      GPathResult node,
      String templateId, String path,
      String archetypeId, String archetypePath,
      CompositionIndex owner)
   {
      /*
      <value xsi:type="DV_ORDINAL">
       <value>234</value>
       <symbol>
         <value>Right arm</value>
         <defining_code>
          <terminology_id>
            <value>local</value>
          </terminology_id>
          <code_string>at0025</code_string>
         </defining_code>
       </symbol>
      </value>
      */
      return new DvOrdinalIndex(
        templateId: templateId,
        archetypeId: archetypeId,
        path: path,
        archetypePath: archetypePath,
        owner: owner,
        value: new Integer( node.value.text() ),
        symbol_value: node.symbol.value.text(),
        symbol_code: node.symbol.defining_code.code_string.text(),
        symbol_terminology_id: node.symbol.defining_code.terminology_id.value.text()
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
        <value>20070920T104614,156+0930</value>
      </time>
      */
      return new DvDateTimeIndex(
        templateId: templateId,
        archetypeId: archetypeId,
        path: path,
        archetypePath: archetypePath,
        owner: owner,
        value: DateParser.tryParse(node.value.text())
      )
   }
}
