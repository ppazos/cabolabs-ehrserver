<%@ page import="ehr.Ehr" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'ehr.label', default: 'Ehr')}" />
    <title><g:message code="default.show.label" args="[entityName]" /></title>
    <style>
    .icon {
      width: 64px;
      border: none;
    }
    </style>
  </head>
  <body>
    <a href="#show-ehr" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
    <div class="nav" role="navigation">
      <ul>
        <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
        <li><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></li>
        <li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
      </ul>
    </div>
    <div id="show-ehr" class="content scaffold-show" role="main">
    
      <h1><g:message code="default.show.label" args="[entityName]" /></h1>
      
      <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message}</div>
      </g:if>
      
      <ol class="property-list ehr">
      
        <g:if test="${ehr?.ehrId}">
        <li class="fieldcontain">
          <span id="ehrId-label" class="property-label"><g:message code="ehr.ehrId.label" default="Ehr Id" /></span>
          <span class="property-value" aria-labelledby="ehrId-label"><g:fieldValue bean="${ehr}" field="ehrId"/></span>
        </li>
        </g:if>
      
        <g:if test="${ehr?.subject}">
        <li class="fieldcontain">
          <span id="subject-label" class="property-label"><g:message code="ehr.subject.label" default="Subject" /></span>
          <span class="property-value" aria-labelledby="subject-label">
            <g:link controller="patientProxy" action="show" id="${ehr?.subject?.id}">${ehr?.subject?.encodeAsHTML()}</g:link>
            <ol class="property-list ehr">
              <li class="fieldcontain">
                <span id="namespace-label" class="property-label">Namespace</span>
                <span class="property-value" aria-labelledby="namespace-label">${ehr.subject.namespace}</span>
              </li>
              <li class="fieldcontain">
                <span id="type-label" class="property-label">Type</span>
                <span class="property-value" aria-labelledby="type-label">${ehr.subject.type}</span>
              </li>
              <li class="fieldcontain">
                <span id="value-label" class="property-label">Value</span>
                <span class="property-value" aria-labelledby="value-label">${ehr.subject.value}</span>
              </li>
            </ol>
          </span>
        </li>
        </g:if>
        
        <g:if test="${ehr?.dateCreated}">
        <li class="fieldcontain">
          <span id="dateCreated-label" class="property-label"><g:message code="ehr.dateCreated.label" default="Date Created" /></span>
          <span class="property-value" aria-labelledby="dateCreated-label"><g:formatDate date="${ehr?.dateCreated}" /></span>
        </li>
        </g:if>
      
        <g:if test="${ehr?.systemId}">
        <li class="fieldcontain">
          <span id="systemId-label" class="property-label"><g:message code="ehr.systemId.label" default="System Id" /></span>
          <span class="property-value" aria-labelledby="systemId-label"><g:fieldValue bean="${ehr}" field="systemId"/></span>
        </li>
        </g:if>
      
        <g:if test="${ehr?.contributions}">
        <li class="fieldcontain">
          <span id="contributions-label" class="property-label"><g:message code="ehr.contributions.label" default="Contributions" /></span>
          <g:each in="${ehr.contributions}" var="contrib">
            <span class="property-value" aria-labelledby="contributions-label">
              
              <h2>Contribution ${contrib.uid}</h2>
              
              <h3>Versions</h3>
              
              <g:each in="${contrib.versions}" var="version">
	             
	             <h4>Version</h4>
	             
	             <g:if test="${version}">
	               version agregada
		             <ul>
		               <li>UID: ${version.uid}</li>
		               <!-- 
		               <li>lifecycle state: ${version.lifecycleState}</li>
		               <li>commitAudit.systemId: ${version.commitAudit.systemId}</li>
		               <li>commitAudit.timeCommitted: ${version.commitAudit.timeCommitted}</li>
		               <li>commitAudit.changeType: ${version.commitAudit.changeType}</li>
		               <li>commitAudit.committer.namespace: ${version.commitAudit.committer.namespace}</li>
		               <li>commitAudit.committer.type: ${version.commitAudit.committer.type}</li>
		               <li>commitAudit.committer.value: ${version.commitAudit.committer.value}</li>
		               <li>commitAudit.committer.name: ${version.commitAudit.committer.name}</li>
		               <li>data.namespace: ${version.data.namespace}</li>
		               <li>data.type: ${version.data.type}</li>
		               <li>data.value: ${version.data.value}</li>
		               -->
		               <li>Composition: <g:link action="showComposition" params="[uid:version.data.value]">${version.data.value}</g:link></li>
		             </ul>
	             </g:if>
	             <g:else>
	               no se agrego la version
	             </g:else>
	             <br/>
	             
              </g:each>
              
            </span>
          </g:each>
        </li>
        </g:if>
        
        <g:if test="${ehr?.compositions}">
        <li class="fieldcontain">
          <span id="compositions-label" class="property-label"><g:message code="ehr.compositions.label" default="Compositions" /></span>
          <g:each in="${ehr.compositions}" var="c">
            <span class="property-value" aria-labelledby="compositions-label">
              <g:link controller="compositionRef" action="show" id="${c.id}">${c?.encodeAsHTML()}</g:link>
            </span>
            
            <g:link action="showComposition" params="[uid:c.value]" title="Ver XML ${c.value}"><img src="${resource(dir: 'images', file: 'xml.png')}" class="icon" /></g:link>
            <g:link action="showCompositionUI" params="[uid:c.value]" title="Ver Documento ${c.value}"><img src="${resource(dir: 'images', file: 'doc.png')}" class="icon" /></g:link>
            <br/><br/>
            
          </g:each>
        </li>
        </g:if>
      
      </ol>
      <g:form>
        <fieldset class="buttons">
          <g:hiddenField name="id" value="${ehr?.id}" />
          <g:link class="edit" action="edit" id="${ehr?.id}"><g:message code="default.button.edit.label" default="Edit" /></g:link>
          <g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
        </fieldset>
      </g:form>
    </div>
  </body>
</html>