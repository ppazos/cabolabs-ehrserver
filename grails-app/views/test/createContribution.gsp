<html>
  <head>
    <meta name="layout" content="main">
    <title><g:message code="createContribution test" /></title>
  </head>
  <body>
    <div class="nav" role="navigation">
      <ul>
        <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
      </ul>
    </div>
    
    <div id="create-ehr" class="content scaffold-create" role="main">
      <h1>createContribution test</h1>
      <g:if test="${flash.message}">
        <div class="alert alert-info" role="alert">${flash.message}</div>
      </g:if>
    </div>
    
    <g:form action="createContribution" >
      <fieldset class="form">
        <div class="fieldcontain">
          <label for="uid">UID</label>
          <g:select name="uid"
                    from="${ehr.Ehr.list()}"
                    multiple="multiple"
                    optionKey="uid"
                    size="5"
                    value="${ehrInstance?.compositions*.id}" />
        </div>
        <div class="fieldcontain">
          <label for="contribution">contribution</label>
          <g:textArea name="contribution" rows="10" cols="60"><contribution>
   <!-- HIER_OBJECT_ID -->
   <uid>
     <value>10aec661-5458-4ff6-8e63-c22655371234</value>
   </uid>
   
   <!-- AUDIT_DETAILS -->
   <audit>
     <system_id>10aec661-5458-4ff6-8e63-c2265537196d</system_id>
     
     <!-- DV_DATE_TIME -->
     <time_committed /><!-- lo debe setear el servidor -->
     
     <!-- DV_CODED_TEXT -->
     <change_type>creation</change_type>
     
     <!-- PARTY_IDENTIFIED -->
     <committer>
       <name>Dr. Pablo Pazos</name>
     </committer>
   </audit>
   
   <!-- OBJECT_REF -->
   <versions>
     <!-- OBJECT_ID > OBJECT_VERSION_ID -->
     <id>
       <!-- <value>object_id::creating_system_id::version_tree_id</value>-->
       <value>10aec661-5458-4ff6-8e63-c22655371222::ISIS_EMR::1</value>
     </id>
     <namespace>isis.ehr.versions</namespace>
     <type>VERSION</type>
   </versions>
   <versions>
     <!-- OBJECT_ID > OBJECT_VERSION_ID -->
     <id>
       <!-- <value>object_id::creating_system_id::version_tree_id</value>-->
       <value>10aec661-5458-4ff6-8e63-c22655371444::ISIS_EMR::1</value>
     </id>
     <namespace>isis.ehr.versions</namespace>
     <type>VERSION</type>
   </versions>
 </contribution></g:textArea>
        </div>
      </fieldset>
      <fieldset class="buttons">
        <g:submitButton name="createContribution" class="save" value="Enviar" />
      </fieldset>
    </g:form>
  </body>
</html>