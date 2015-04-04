<%@ page import="common.change_control.Contribution" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'contribution.label', default: 'Contribution')}" />
    <title><g:message code="default.list.label" args="[entityName]" /></title>
  </head>
  <body>
    <a href="#list-contribution" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
    <div class="nav" role="navigation">
      <ul>
        <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
        <!--
        <li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
         -->
      </ul>
    </div>
    
    <div id="list-contribution" class="content scaffold-list" role="main">
      <h1><g:message code="contribution.list.title" /></h1>
      
      <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message}</div>
      </g:if>
      
      <table>
        <thead>
          <tr>
            <g:sortableColumn property="uid" title="${message(code: 'contribution.uid.label', default: 'UID')}" />
            <th>EHR</th>
            <th>Time Committed</th>
            <th># Versions</th>
          </tr>
        </thead>
        <tbody>
        <g:each in="${contributionInstanceList}" status="i" var="contributionInstance">
          <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
            <td><g:link action="show" id="${contributionInstance.id}">${fieldValue(bean: contributionInstance, field: "uid")}</g:link></td>
            <td>${contributionInstance.ehr.ehrId}</td>
            <td>${contributionInstance.audit.timeCommitted}</td>
            <td>${contributionInstance.versions.size()}</td>
          </tr>
        </g:each>
        </tbody>
      </table>
      
      <div class="pagination">
        <g:paginate total="${contributionInstanceTotal}" />
      </div>
    </div>
  </body>
</html>