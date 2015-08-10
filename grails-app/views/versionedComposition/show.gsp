
<%@ page import="common.change_control.VersionedComposition" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'versionedComposition.label', default: 'VersionedComposition')}" />
    <title><g:message code="default.show.label" args="[entityName]" /></title>
  </head>
  <body>
    <a href="#show-versionedComposition" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
    <div class="nav" role="navigation">
      <ul>
        <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
        <li><g:link class="list" action="index"><g:message code="default.list.label" args="[entityName]" /></g:link></li>
        <li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
      </ul>
    </div>
    <div id="show-versionedComposition" class="content scaffold-show" role="main">
      <h1><g:message code="default.show.label" args="[entityName]" /></h1>
      <g:if test="${flash.message}">
      <div class="message" role="status">${flash.message}</div>
      </g:if>
      <ol class="property-list versionedComposition">
      
        <g:if test="${versionedCompositionInstance?.ehrUid}">
        <li class="fieldcontain">
          <span id="ehrUid-label" class="property-label"><g:message code="versionedComposition.ehrUid.label" default="Ehr Uid" /></span>
          <span class="property-value" aria-labelledby="ehrUid-label"><g:fieldValue bean="${versionedCompositionInstance}" field="ehrUid"/></span>
        </li>
        </g:if>
      
        <g:if test="${versionedCompositionInstance?.isPersistent}">
        <li class="fieldcontain">
          <span id="isPersistent-label" class="property-label"><g:message code="versionedComposition.isPersistent.label" default="Is Persistent" /></span>
          <span class="property-value" aria-labelledby="isPersistent-label"><g:formatBoolean boolean="${versionedCompositionInstance?.isPersistent}" /></span>
        </li>
        </g:if>
      
        <g:if test="${versionedCompositionInstance?.timeCreated}">
        <li class="fieldcontain">
          <span id="timeCreated-label" class="property-label"><g:message code="versionedComposition.timeCreated.label" default="Time Created" /></span>
          <span class="property-value" aria-labelledby="timeCreated-label"><g:formatDate date="${versionedCompositionInstance?.timeCreated}" /></span>
        </li>
        </g:if>
      
        <g:if test="${versionedCompositionInstance?.uid}">
        <li class="fieldcontain">
          <span id="uid-label" class="property-label"><g:message code="versionedComposition.uid.label" default="Uid" /></span>
          <span class="property-value" aria-labelledby="uid-label"><g:fieldValue bean="${versionedCompositionInstance}" field="uid"/></span>
        </li>
        </g:if>
        
        <li class="fieldcontain">
	        <h2>Versions</h2>
	        <table><!-- versioned composition versions -->
	          <tr>
	            <th>uid</th>
	            <th>creation date</th>
	            <th>type</th>
	            <th>change type</th>
	            <th></th>
	          </tr>
	          <g:each in="${versionedCompositionInstance.allVersions}" var="version">
	            <%-- ${version.uid}<br/> --%><!-- TODO: version row template + diff -->
	            <g:render template="../version/versionRow" model="[version:version]"/>
	          </g:each>
	        </table>
	      </li>
      </ol>

    </div>
  </body>
</html>
