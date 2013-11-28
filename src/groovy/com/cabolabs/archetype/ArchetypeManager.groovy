/**
 * MODIFICADO 28/09/2012 getArchetype por regex ahora retorna una lista de arquetipos.
 * 
 * 
 * Clase SINGLETON para el manejo de arquetipos y medio de acceso al repositorio de arquetipos local.
 * Tiene un cache que carga los arquetipos presentes en el repostorio local a medida que estos son utilizados.
 */
package com.cabolabs.archetype

import org.openehr.am.archetype.Archetype
import org.openehr.rm.support.identification.ArchetypeID

// http://www.openehr.org/wiki/display/projects/Java+ADL+Parser+Guide
import se.acode.openehr.parser.*

import org.apache.log4j.Logger
import java.util.regex.Pattern

import org.openehr.am.archetype.constraintmodel.ArchetypeConstraint

/**
 * @author Pablo Pazos Gutierrez (pablo.swp@gmail.com)
 * @version 1.0
 */
class ArchetypeManager {

   private Logger log = Logger.getLogger(getClass())
   
   // Ruta independiente del SO
   // http://code.google.com/p/open-ehr-gen-framework/issues/detail?id=54
   private static String PS = System.getProperty("file.separator")
   
   /**
    * Directorio donde estan los arquetipos.
    * FIXME: deberia ser un parametro de la aplicacion en un .properties
    */
   private String archetypeRepositoryPath = "archetypes"
    
   // Cache: archetypeId => Archetype
   private static Map<String, Archetype> cache = [:]
    
   // archetypeId => timestamp de cuando fue usado por ultima vez.
   // Sirve para saber si un arquetipo no fue utilizado por mucho tiempo, y bajarlo del cache par optimizar espacio en memoria.
   private static Map<String, Date> timestamps = [:]
    
   // SINGLETON
   private static ArchetypeManager instance = null
    
   private ArchetypeManager() {}
    
   public static ArchetypeManager getInstance()
   {
      if (!instance) instance = new ArchetypeManager()
      return instance
   }
    
   /**
    * Carga todos los arquetipos presentes en el repositorio de arquetipos.
    * No importa si algunos arquetipos ya estan en el cache, carga todo y acutaliza timestamps.
    */
   public void loadAll()
   {
      def path = this.archetypeRepositoryPath
      loadAllRecursive( path )
   }
    
   private loadAllRecursive( String path )
   {
      println "loadAllRecursive: " + path
      def root = new File( path )

      // FIXME: deberia filtrar solo archivos adl
      root.eachFile { f ->

         println "LOAD: [" + f.name + "]"

         // PARSEAR ARQUETIPO
         ADLParser parser = null;
         try
         {
            parser = new ADLParser( f )
         }
         catch (IOException e)
         {
            println "PROBLEMA AL CREAR EL PARSER: " + e.message
         }
         
         Archetype archetype = null
         try
         {
            archetype = parser.archetype()
         }
         catch (Exception e)
         {
            println e.message
         }
         // /PARSEAR ARQUETIPO
             
         if (archetype)
         {
            log.debug("Cargado el arquetipo: " + f.name + " de " + root.path)
            cache[archetype.archetypeId.value] = archetype
            timestamps[archetype.archetypeId.value] = new Date()
         }
         else
         {
            log.error("No se pudo cargar el arquetipo: " + f.name + " de:\n\t " + root.path)
         }
      }
        
      // Recursiva por directorios
      root.eachDir { d ->
         loadAllRecursive( d.path )
      }
   }
   
   /**
    * Carga el arquetipo con id archetypeId
    * @param archetypeId
    * @return el arquetipo cargado o null si no lo encuentra
    */
   public Archetype getArchetype( String archetypeId )
   {
       // FIXME: PROBLEMAS DE CARGAR EL ARQUETIPO: openEHR-EHR-COMPOSITION.prescription.v1
       
       // Si no esta cargado, lo intenta cargar
       if (!this.cache[archetypeId])
       {
           println "No se encuentra el arquetipo " + archetypeId + ", se intenta cargarlo"
           def id = new ArchetypeID( archetypeId ) // a partir del ID saco la ruta que tengo que cargar
           def type = id.rmEntity // cluster, entry, composition, etc...
           
           // FIXME: ojo que si es un subtipo la ruta no es directa (action esta en /ehr/entry/action no es /ehr/action!)
           
           // archetypes/ehr/type/archId.adl
           println "Carga desde: " + this.archetypeRepositoryPath+ PS +getTypePath(type)+ PS +archetypeId+".adl"
           def adlFile = new File( this.archetypeRepositoryPath+ PS +getTypePath(type)+ PS +archetypeId+".adl" )
           
           // PARSEAR ARQUETIPO
           ADLParser parser = null;
           try { parser = new ADLParser( adlFile ) }
           catch (IOException e) { print e.message }
           
           Archetype archetype = null;
           try { archetype = parser.archetype() }
           catch (Exception e) { print e.message }
           // /PARSEAR ARQUETIPO
               
           if (archetype)
           {
              log.debug("Cargado el arquetipo: " + adlFile.name + " de " + adlFile.path)
              cache[archetype.archetypeId.value] = archetype
              timestamps[archetype.archetypeId.value] = new Date()
           }
           else
           {
              log.error("No se pudo cargar el arquetipo: " + adlFile.name + " de " + adlFile.path)
           }
       }
       else
       {
           this.timestamps[archetypeId] = new Date() // actualizo timestamp
       }
       
       return this.cache[archetypeId]
   }
   
   private String getTypePath( String type )
   {
       type = type.toLowerCase()
       switch (type)
       {
           case 'cluster':
           case 'composition':
           case 'element':
           case 'section':
           case 'structure':
               return type
           break
           case 'item_tree':
           case 'item_single':
           case 'item_list':
           case 'item_table':
               return 'structure'
           break
           case 'action':
           case 'evaluation':
           case 'instruction':
           case 'observation':
               return 'entry'+ PS + type
           break
           case 'admin_entry':
               return 'entry'+ PS + type
           break
           default:
               throw new Exception('Tipo no conocido ['+ type +'], se espera uno de: cluster, composition, element, section, item_tree, item_single, item_list, item_table, action, observation, instruction, evaluation' )
       }
   }
   
   /**
    * Obtiene un arquetipo por si tipo y expresion regular.
    * FIXME: type puede venir en mayusculas o en minusculas, y lo necesito en minusculas.
    * FIXME: el resultado de esta operacion deberia ser una lista de arquetipos!
    */
   //public Archetype getArchetype( String type, String idMatchingKey )
   public List getArchetypes(String type, String regex)
   {
       println "=== getArchetypes( "+type+", "+regex+" ) ==="
       //type = type.toLowerCase()
       
       
       // FIXME: no uso el type porque para guardar los arquetipos no lo uso,
       //        seria una optimizacion para buscar.
       //Archetype archetype = null
       def archetypes = []
       def p = Pattern.compile( ".*"+regex+".*\\.adl" ) // agrego .adl porque si hay .adls de ADL1.5 en el dir, intenta cargarlo.
       
       // Busca en los arquetipos cargados:
       def iter = this.cache.keySet().iterator()
       def archetypeId
       while( iter.hasNext() )
       {
           archetypeId = iter.next()
           if ( p.matcher(archetypeId).matches() )
           {
               println "   Encuentra arquetipo en cache: " + archetypeId
               this.timestamps[archetypeId] = new Date()
               return this.cache[archetypeId]
           }
           //else println "NO ES"
       }
   
       // TODO: Si no esta, tendria que ir a cargarlo... ahi uso type.
       println "   No se encuentra el arquetipo que corresponda con " + regex + ", se intenta cargarlo"
       
       // FIXME: ojo que si es un subtipo la ruta no es directa (action esta en /ehr/entry/action no es /ehr/action!)
       
       // archetypes/ehr/type/archId.adl
       def root = new File( this.archetypeRepositoryPath + PS + getTypePath(type) ) // Abre el directorio donde supuestamente esta el arquetipo
       
       // FIXME: varios pueden matchear!
       def adlFiles = []
       root.eachFile { f ->
       
           if ( p.matcher(f.name).matches() )
           {
               adlFiles << f
           }
       }
       
       if (adlFiles.size() == 0) println "   ERROR: No se encuentran archivos que matchee con " + "[.*"+type+".*"+idMatchingKey+".*]" + " desde " + root.path
       else
       {
           def archetype
           adlFiles.each { adlFile ->
              
              println "   Carga desde: " + adlFile.path
   
              // PARSEAR ARQUETIPO
              ADLParser parser = null;
              try { parser = new ADLParser( adlFile ) }
              catch (IOException e) { print e.message }
              
              //Archetype archetype = null;
              try { archetype = parser.archetype() }
              catch (Exception e) { print e.message }
              // /PARSEAR ARQUETIPO
                  
              if (archetype)
              {
                 println "   Cargado el arquetipo: " + adlFile.name + " de " + adlFile.path
                 cache[archetype.archetypeId.value] = archetype
                 timestamps[archetype.archetypeId.value] = new Date()
                 
                 archetypes << archetype
              }
              else
              {
                 println "   ERROR: No se pudo cargar el arquetipo: " + adlFile.name + " de " + adlFile.path
              }
           }
       }
       
       return archetypes
   }
   
   /**
    * La path incluye el id del arquetipo:
    * @param fullPath openEHR-EHR-OBSERVATION.diagnosticos.v1/data[at0001]/events[at0002]/data[at0003]/items[at0004]/value/defining_code
    * @return
    */
   public ArchetypeConstraint getArchetypeNode(String fullPath)
   {
      int i = fullPath.indexOf('/')
      String archetypeId = fullPath.substring(0, i)
      String path = fullPath.substring(i)
      
      Archetype a = this.getArchetype(archetypeId)
      return a?.node(path)
   }
   
   public String toString()
   {
      return this.cache.keySet().toString()
   }
   
   public Map getLoadedArchetypes()
   {
       return this.cache
   }
   
   public Map getLastUse()
   {
       return this.timestamps
   }
   
   public void unloadAll()
   {
       // FIXME: debe estar sincronizada
       this.cache.clear()
       this.timestamps.clear()
   }
}