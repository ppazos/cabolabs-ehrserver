<%@ page import="ehr.Ehr" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'ehr.label', default: 'Ehr')}" />
    <title><g:message code="default.show.label" args="[entityName]" /></title>
  </head>
  <body>
    <a href="#show-ehr" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
    <div class="nav" role="navigation">
      <ul>
        <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
        <li><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></li>
      </ul>
    </div>
    
    <div id="show-ehr" class="content scaffold-show" role="main">
      <h1><g:message code="default.show.label" args="[entityName]" /></h1>
      
      <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message}</div>
      </g:if>
      
      <ol class="property-list ehr">
      
        <%-- T0004
        <g:if test="${ehrInstance?.compositions}">
        <li class="fieldcontain">
          <span id="compositions-label" class="property-label"><g:message code="ehr.compositions.label" default="Compositions" /></span>
          
          <g:each in="${ehrInstance.compositions}" var="c">
            <span class="property-value" aria-labelledby="compositions-label"><g:link controller="compositionRef" action="show" id="${c.id}">${c?.encodeAsHTML()}</g:link></span>
          </g:each>
        </li>
        </g:if>
        --%>
      
        <g:if test="${ehrInstance?.contributions}">
        <li class="fieldcontain">
          <span id="contributions-label" class="property-label"><g:message code="ehr.contributions.label" default="Contributions" /></span>
          <g:each in="${ehrInstance.contributions}" var="c">
            <span class="property-value" aria-labelledby="contributions-label"><g:link controller="contributionRef" action="show" id="${c.id}">${c?.encodeAsHTML()}</g:link></span>
          </g:each>
        </li>
        </g:if>
      
        <g:if test="${ehrInstance?.dateCreated}">
        <li class="fieldcontain">
          <span id="dateCreated-label" class="property-label"><g:message code="ehr.dateCreated.label" default="Date Created" /></span>
          <span class="property-value" aria-labelledby="dateCreated-label"><g:formatDate date="${ehrInstance?.dateCreated}" /></span>
        </li>
        </g:if>
      
        <g:if test="${ehrInstance?.ehrId}">
        <li class="fieldcontain">
          <span id="ehrId-label" class="property-label"><g:message code="ehr.ehrId.label" default="Ehr Id" /></span>
          <span class="property-value" aria-labelledby="ehrId-label"><g:fieldValue bean="${ehrInstance}" field="ehrId"/></span>
        </li>
        </g:if>
      
        <g:if test="${ehrInstance?.subject}">
        <li class="fieldcontain">
          <span id="subject-label" class="property-label"><g:message code="ehr.subject.label" default="Subject" /></span>
          <span class="property-value" aria-labelledby="subject-label"><g:link controller="patientProxy" action="show" id="${ehrInstance?.subject?.id}">${ehrInstance?.subject?.encodeAsHTML()}</g:link></span>
        </li>
        </g:if>
      
        <g:if test="${ehrInstance?.systemId}">
        <li class="fieldcontain">
          <span id="systemId-label" class="property-label"><g:message code="ehr.systemId.label" default="System Id" /></span>
          <span class="property-value" aria-labelledby="systemId-label"><g:fieldValue bean="${ehrInstance}" field="systemId"/></span>
        </li>
        </g:if>
      </ol>
      <!--
      <g:form>
        <fieldset class="buttons">
          <g:hiddenField name="id" value="${ehrInstance?.id}" />
          <g:link class="edit" action="edit" id="${ehrInstance?.id}"><g:message code="default.button.edit.label" default="Edit" /></g:link>
          <g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
        </fieldset>
      </g:form>
      -->
    </div>
  </body>
</html>