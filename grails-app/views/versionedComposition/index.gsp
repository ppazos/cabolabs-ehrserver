<%@ page import="com.cabolabs.ehrserver.openehr.common.change_control.VersionedComposition" %>
<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="versionedComposition.list.title" /></title>
    <style>
     /* adjusts the filder input width */
     @media (min-width: 768px) {
      #ipt_ehr {
       width: 310px;
      }
     }
    </style>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
        <h1><g:message code="versionedComposition.list.title" /></h1>
      </div>
    </div>
    <div class="row row-grid">
      <div class="col-lg-12">
        <g:form class="form-inline" action="index">
          <input type="hidden" name="sort" value="${params.sort}" />
          <input type="hidden" name="order" value="${params.order}" />
          <div class="form-group">
            <label for="ipt_ehr"><g:message code="versionedComposition.attr.ehr" /></label>
            <input type="text" class="form-control" name="ehdUid" id="ipt_ehr" placeholder="11111111-1111-1111-1111-111111111111" value="${params?.ehdUid}" />
          </div>
          <button type="submit" class="btn btn-default"><g:message code="common.action.filter" /></button>
        </g:form>
      </div>
    </div>
    
    <div class="row row-grid">
      <div class="col-lg-12">
        <g:if test="${flash.message}">
          <div class="message" role="status">${flash.message}</div>
        </g:if>
        
        <div class="table-responsive">
          <table class="table table-striped table-bordered table-hover">
            <thead>
              <tr>
                <g:sortableColumn property="uid" title="${message(code:'versionedComposition.attr.uid')}" params="${params}" />
                <th>${message(code: 'versionedComposition.attr.ehr')}</th>
                <g:sortableColumn property="isPersistent" title="${message(code:'versionedComposition.attr.isPersistent')}" params="${params}" />
                <g:sortableColumn property="timeCreated" title="${message(code:'versionedComposition.attr.timeCreated')}" params="${params}" />
                <th>${message(code: 'composition.attr.templateId')}</th>
                <th>${message(code: 'composition.attr.archetypeId')}</th>
              </tr>
            </thead>
            <tbody>
              <g:each in="${versionedCompositionInstanceList}" status="i" var="versionedCompositionInstance">
                <g:set var="compo" value="${versionedCompositionInstance.latestVersion.data}" />
                <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
                  <td><g:link action="show" params="[uid: versionedCompositionInstance.uid]">${fieldValue(bean: versionedCompositionInstance, field: "uid")}</g:link></td>
                  <td>${versionedCompositionInstance.ehr.uid}</td>
                  <td><g:formatBoolean boolean="${versionedCompositionInstance.isPersistent}" /></td>
                  <td><g:formatDate date="${versionedCompositionInstance.timeCreated}" /></td>
                  <td>${compo.templateId}</td>
                  <td>${compo.archetypeId}</td>
                </tr>
              </g:each>
            </tbody>
          </table>
        </div>
        <g:paginator total="${versionedCompositionInstanceCount}" />
      </div>
    </div>
  </body>
</html>
