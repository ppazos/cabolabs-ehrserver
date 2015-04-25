package ehr

//import com.thoughtworks.xstream.XStream
import common.generic.PatientProxy
import demographic.Person
import ehr.Ehr
import ehr.clinical_documents.CompositionIndex
import ehr.clinical_documents.data.DvTextIndex
import ehr.clinical_documents.data.DvCodedTextIndex
import ehr.clinical_documents.data.DvDateTimeIndex
import ehr.clinical_documents.data.DvQuantityIndex
import groovy.util.slurpersupport.GPathResult
import groovy.xml.MarkupBuilder
import common.change_control.Contribution

import grails.util.Holders

class EhrController {

   
   // Para acceder a las opciones de localizacion 
   def config = Holders.config.app
   
   
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
      // necesito consultar el arquetipo para poder hacerlo o puedo consultar los IndexDefinition (temporal)
      
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
}