<html>
  <head>
    <meta name="layout" content="main">
    <title><g:message code="addVersion test" /></title>
  </head>
  <body>
    <div class="nav" role="navigation">
      <ul>
        <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
      </ul>
    </div>
    
    <div id="create-ehr" class="content scaffold-create" role="main">
      <h1>addVersion test</h1>
      <g:if test="${flash.message}">
        <div class="alert alert-info" role="alert">${flash.message}</div>
      </g:if>
    </div>
    
    La version debe tener el id de la contribution adentro<br/>
    <ul>
      <g:each in="${common.change_control.Contribution.list()}">
        <li>${it.uid}</li>
      </g:each>
    </ul>
    
    <g:form action="addVersion" >
      <fieldset class="form">
        <div class="fieldcontain">
          <label for="version">version</label>
          <g:textArea name="version" rows="10" cols="50"><?xml version="1.0" encoding="UTF-8"?>
<version xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://schemas.openehr.org/v1">
   <!-- OBJECT_REF -->
   <contribution>
     <id>
       <value>10aec661-5458-4ff6-8e63-c22655371234</value>
     </id>
     <namespace>isis.ehr.contributions</namespace>
     <type>CONTRIBUTION</type>
   </contribution>
   
   <!-- AUDIT_DETAILS -->
   <commit_audit>
     <!-- Identity of the system where the change was committed -->
     <system_id>ISIS EHR SERVER</system_id>
     
     <!-- DV_DATE_TIME -->
     <time_committed>
       <value>20070920T011901</value>
     </time_committed>
     
     <!-- DV_CODED_TEXT -->
     <change_type>
       <value>creation</value>
       <defining_code>
         <terminology_id>
           <value>openehr</value>
         </terminology_id>
         <code_string>249</code_string>
       </defining_code>
     </change_type>
     
     <!-- PARTY_IDENTIFIED -->
     <committer>
       <name>Dr. Pablo Pazos</name>
     </committer>
   </commit_audit>
   
   <!-- OBJECT_VERSION_ID -->
   <uid>
     <!-- <value>object_id::creating_system_id::version_tree_id</value> -->
     <value>10aec661-5458-4ff6-8e63-c22655371222::ISIS_EMR::1</value>
     <!-- <value>10aec661-5458-4ff6-8e63-c22655371444::ISIS_EMR::1</value> -->
   </uid>
   
   <!-- COMPOSITION -->
   <data xsi:type="COMPOSITION" archetype_node_id="openEHR-EHR-COMPOSITION.encounter.v1">
      <name>
         <value>Blood Pressure</value>
      </name>
      <language>
         <terminology_id>
            <value/>
         </terminology_id>
         <code_string/>
      </language>
      <territory>
         <terminology_id>
            <value>ISO 1366</value>
         </terminology_id>
         <code_string>036</code_string>
      </territory>
      <category>
         <value>event</value>
         <defining_code>
            <terminology_id>
               <value>openehr</value>
            </terminology_id>
            <code_string>433</code_string>
         </defining_code>
      </category>
      <composer xsi:type="PARTY_IDENTIFIED">
         <name>EhrGateUnit</name>
      </composer>
      <context>
         <start_time>
            <value>20070920T104614,0000+0930</value>
         </start_time>
         <setting>
            <value>event</value>
            <defining_code>
               <terminology_id>
                  <value>openEHR</value>
               </terminology_id>
               <code_string>433</code_string>
            </defining_code>
         </setting>
      </context>
      <content xsi:type="OBSERVATION" archetype_node_id="openEHR-EHR-OBSERVATION.blood_pressure.v1">
         <name>
            <value>Blood Pressure</value>
         </name>
         <language>
            <terminology_id>
               <value>ISO 639</value>
            </terminology_id>
            <code_string>en</code_string>
         </language>
         <encoding>
            <terminology_id>
               <value>IANA</value>
            </terminology_id>
            <code_string>utf-8</code_string>
         </encoding>
         <subject xsi:type="PARTY_SELF"/>
         <protocol xsi:type="ITEM_LIST" archetype_node_id="at0011">
            <name>
               <value>protocol</value>
            </name>
            <items archetype_node_id="at0014">
               <name>
                  <value>Location of measurement</value>
               </name>
               <value xsi:type="DV_CODED_TEXT">
                  <value>Right arm</value>
                  <defining_code>
                     <terminology_id>
                        <value>local</value>
                     </terminology_id>
                     <code_string>at0025</code_string>
                  </defining_code>
               </value>
            </items>
         </protocol>
         <data archetype_node_id="at0001">
            <name>
               <value>data</value>
            </name>
            <origin>
               <value>20070920T104614,0156+0930</value>
            </origin>
            <events xsi:type="POINT_EVENT" archetype_node_id="at0006">
               <name>
                  <value>any event</value>
               </name>
               <time>
                  <value>20070920T104614,0156+0930</value>
               </time>
               <data xsi:type="ITEM_LIST" archetype_node_id="at0003">
                  <name>
                     <value>data</value>
                  </name>
                  <items archetype_node_id="at0004">
                     <name>
                        <value>systolic</value>
                     </name>
                     <value xsi:type="DV_QUANTITY">
                        <magnitude>120</magnitude>
                        <units>mm[Hg]</units>
                     </value>
                  </items>
                  <items archetype_node_id="at0005">
                     <name>
                        <value>diastolic</value>
                     </name>
                     <value xsi:type="DV_QUANTITY">
                        <magnitude>90</magnitude>
                        <units>mm[Hg]</units>
                     </value>
                  </items>
               </data>
            </events>
         </data>
      </content>
   </data>
   
   <!-- DV_CODED_TEXT -->
   <lifecycle_state>
     <value>complete</value>
     <defining_code>
       <terminology_id>
         <value>openehr</value>
       </terminology_id>
       <code_string>532</code_string>
     </defining_code>
   </lifecycle_state>
 </version></g:textArea>
        </div>
      </fieldset>
      <fieldset class="buttons">
        <g:submitButton name="addVersion" class="save" value="Enviar" />
      </fieldset>
    </g:form>
  </body>
</html>