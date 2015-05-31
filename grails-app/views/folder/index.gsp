
<%@ page import="directory.Folder" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'folder.label', default: 'Folder')}" />
    <title><g:message code="default.list.label" args="[entityName]" /></title>
  </head>
  <body>
    <a href="#list-folder" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
    <div class="nav" role="navigation">
      <ul>
        <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
        <li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
      </ul>
    </div>
    <div id="list-folder" class="content scaffold-list" role="main">
      <h1><g:message code="default.list.label" args="[entityName]" /></h1>
      <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message}</div>
      </g:if>
      <table>
        <thead>
          <tr>
            <g:sortableColumn property="uid" title="${message(code: 'folder.uid.label', default: 'Uid')}" />
            <g:sortableColumn property="name" title="${message(code: 'folder.name.label', default: 'Name')}" />
            <th><g:message code="folder.parent.label" default="Parent" /></th>
          </tr>
        </thead>
        <tbody>
	        <g:each in="${folderInstanceList}" status="i" var="folderInstance">
	          <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
	            <td><g:link action="show" id="${folderInstance.id}">${fieldValue(bean: folderInstance, field: "uid")}</g:link></td>
	            <td>${fieldValue(bean: folderInstance, field: "name")}</td>
	            <td>${fieldValue(bean: folderInstance, field: "parent")}</td>
	          </tr>
	        </g:each>
        </tbody>
      </table>
      <div class="pagination">
        <g:paginate total="${folderInstanceCount ?: 0}" />
      </div>
    </div>
  </body>
</html>
