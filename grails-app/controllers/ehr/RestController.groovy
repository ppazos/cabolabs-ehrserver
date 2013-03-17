package ehr

import grails.converters.*
import java.text.SimpleDateFormat
import demographic.Person

class RestController {

   
   // TODO: un index con la lista de servicios y parametros de cada uno (para testing)
   
   def formatter = new SimpleDateFormat("yyyyMMdd'T'hhmmss.SSSSZ")
   def formatterDate = new SimpleDateFormat("yyyyMMdd")
   
   def ehrList(String format)
   {
      // TODO: fromDate, toDate
      // TODO: paginacion (offset, max)
      def _ehrs = Ehr.list(readOnly: true)
      
      // ===========================================================================
      // 3. Discusion por formato de salida
      //
      if (!format || format == "xml")
      {
         /*
          <ehrs>
            <ehr>
              <ehrId>33b94e05-3da5-4291-872e-07b3a4664837</ehrId>
              <dateCreated>20121105T113730.0890-0200</dateCreated>
              <subjectUid>bf529d1c-b74a-4c4f-b6dd-c44c44cd9a3f</subjectUid>
              <systemId>ISIS_EHR_SERVER</systemId>
            </ehr>
            <ehr>
              <ehrId>d06e3256-d65e-436e-95da-5c9bffd05dbd</ehrId>
              <dateCreated>20121105T113732.0171-0200</dateCreated>
              <subjectUid>43a399c9-a5e0-4b51-9422-99c3991ea941</subjectUid>
              <systemId>ISIS_EHR_SERVER</systemId>
            </ehr>
          </ehrs>
          */
         //render(text: ehrs as XML, contentType:"text/xml", encoding:"UTF-8")
         render(contentType:"text/xml", encoding:"UTF-8") {
            'ehrs' {
               _ehrs.each { _ehr ->
                  'ehr'{
                     ehrId(_ehr.ehrId)
                     dateCreated( this.formatter.format( _ehr.dateCreated ) ) // TODO: format
                     subjectUid(_ehr.subject.value)
                     systemId(_ehr.systemId)
                  }
               }
            }
         }
      }
      else if (format == "json")
      {
         /*
          [
            {
              "ehrId": "33b94e05-3da5-4291-872e-07b3a4664837",
              "dateCreated": "20121105T113730.0890-0200",
              "subjectUid": "bf529d1c-b74a-4c4f-b6dd-c44c44cd9a3f",
              "systemId": "ISIS_EHR_SERVER"
            },
            {
              "ehrId": "d06e3256-d65e-436e-95da-5c9bffd05dbd",
              "dateCreated": "20121105T113732.0171-0200",
              "subjectUid": "43a399c9-a5e0-4b51-9422-99c3991ea941",
              "systemId": "ISIS_EHR_SERVER"
            }
          ]
          */
         def data = []
         _ehrs.each { _ehr ->
            data << [
               ehrId: _ehr.ehrId,
               dateCreated: this.formatter.format( _ehr.dateCreated ) , // TODO: format
               subjectUid: _ehr.subject.value,
               systemId: _ehr.systemId
            ]
         }
         
         render(text: data as JSON, contentType:"application/json", encoding:"UTF-8")
      }
      else
      {
         render(status: 500, text:"<result><code>error</code><message>formato '$format' no reconocido, debe ser exactamente 'xml' o 'json'</result>", contentType:"text/xml", encoding:"UTF-8")
      }
   } // ehrList
   
   
   def ehrForSubject(String subjectUid, String format)
   {
      // ===========================================================================
      // 1. Paciente existe?
      //
      def _subject = Person.findByUidAndRole(subjectUid, 'pat')
      if (!_subject)
      {
         render(status: 500, text:'<result><code>error</code><message>No existe el paciente $subjectId</message></result>', contentType:"text/xml", encoding:"UTF-8")
         return
      }
      
      // ===========================================================================
      // 2. Paciente tiene EHR?
      //
      def c = Ehr.createCriteria()
      def _ehr = c.get {
         subject {
            eq ('value', subjectUid)
         }
      }
      if (!_ehr)
      {
         render(status: 500, text:'<result><code>error</code><message>EHR no encontrado para el paciente $subjectId, se debe crear un EHR para el paciente</message></result>', contentType:"text/xml", encoding:"UTF-8")
         return
      }
      
      // ===========================================================================
      // 3. Discusion por formato de salida
      //
      if (!format || format == "xml")
      {
         render(contentType:"text/xml", encoding:"UTF-8") {
            'ehr'{
               ehrId(_ehr.ehrId)
               dateCreated( this.formatter.format( _ehr.dateCreated ) )
               delegate.subjectUid(_ehr.subject.value) // delegate para que no haya conflicto con la variable con el mismo nombre
               systemId(_ehr.systemId)
            }
         }
      }
      else if (format == "json")
      {
         def data = [
            ehrId: _ehr.ehrId,
            dateCreated: this.formatter.format( _ehr.dateCreated ) , // TODO: format
            subjectUid: _ehr.subject.value,
            systemId: _ehr.systemId
         ]
         render(text: data as JSON, contentType:"application/json", encoding:"UTF-8")
      }
      else
      {
         render(status: 500, text:"<result><code>error</code><message>formato '$format' no reconocido, debe ser exactamente 'xml' o 'json'</result>", contentType:"text/xml", encoding:"UTF-8")
      }
   } // ehrForSubject
   
   
   def ehrGet(String ehrUid, String format)
   {
      // 1. EHR existe?
      def c = Ehr.createCriteria()
      def _ehr = c.get {
         eq ('ehrId', ehrUid)
      }
      if (!_ehr)
      {
         render(status: 500, text:'<result><code>error</code><message>EHR no encontrado para el ehrUid $ehrUid</message></result>', contentType:"text/xml", encoding:"UTF-8")
         return
      }
      
      // ===========================================================================
      // 2. Discusion por formato de salida
      //
      if (!format || format == "xml")
      {
         render(contentType:"text/xml", encoding:"UTF-8") {
            'ehr'{
               ehrId(_ehr.ehrId)
               dateCreated( this.formatter.format( _ehr.dateCreated ) ) // TODO: format
               subjectUid(_ehr.subject.value)
               systemId(_ehr.systemId)
            }
         }
      }
      else if (format == "json")
      {
         def data = [
            ehrId: _ehr.ehrId,
            dateCreated: this.formatter.format( _ehr.dateCreated ) , // TODO: format
            subjectUid: _ehr.subject.value,
            systemId: _ehr.systemId
         ]
         render(text: data as JSON, contentType:"application/json", encoding:"UTF-8")
      }
      else
      {
         render(status: 500, text:"<result><code>error</code><message>formato '$format' no reconocido, debe ser exactamente 'xml' o 'json'</result>", contentType:"text/xml", encoding:"UTF-8")
      }
   } // ehrGet
   
   
   
   def patientList(String format)
   {
      // ===========================================================================
      // 1. Lista personas con rol paciente
      //
      def subjects = Person.findAllByRole('pat')
      
      
      // ===========================================================================
      // 2. Discusion por formato de salida
      //
      if (!format || format == "xml")
      {
         render(contentType:"text/xml", encoding:"UTF-8") {
            'patients' {
               subjects.each { person ->
                  'patient'{
                     uid(person.uid)
                     firstName(person.firstName)
                     lastName(person.lastName)
                     dob(this.formatterDate.format( person.dob ) )
                     sex(person.sex)
                     idCode(person.idCode)
                     idType(person.idType)
                  }
               }
            }
         }
      }
      else if (format == "json")
      {
         def data = []
         subjects.each { person ->
            data << [
               uid: person.uid,
               firstName: person.firstName,
               lastName: person.lastName,
               dob: this.formatterDate.format( person.dob ),
               sex: person.sex,
               idCode: person.idCode,
               idType: person.idType
            ]
         }
         render(text: data as JSON, contentType:"application/json", encoding:"UTF-8")
      }
      else
      {
         render(status: 500, text:"<result><code>error</code><message>formato '$format' no reconocido, debe ser exactamente 'xml' o 'json'</result>", contentType:"text/xml", encoding:"UTF-8")
      }
   } // patientList
}