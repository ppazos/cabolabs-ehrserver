<%@ page import="com.cabolabs.ehrserver.query.EhrQuery" %>

<div class="form-group ${hasErrors(bean: ehrQueryInstance, field: 'name', 'error')} required">
  <label for="name" class="control-label">
    <g:message code="ehrquery.attr.name" default="Name" />
    <span class="required-indicator">*</span>
  </label>
  <g:textField name="name" required="" value="${ehrQueryInstance?.name}" class="form-control"/>
</div>

<div class="form-group ${hasErrors(bean: ehrQueryInstance, field: 'description', 'error')} ">
  <label for="description" class="control-label">
    <g:message code="ehrquery.attr.description" default="Description" />
  </label>
  <g:textArea name="description" rows="3" value="${ehrQueryInstance?.description}" class="form-control"/>
</div>

<div class="form-group ${hasErrors(bean: ehrQueryInstance, field: 'queries', 'error')} ">
  <label for="queries" class="control-label">
    <g:message code="ehrquery.attr.queries" default="Queries" />
  </label>
  <g:set var="queries" value="${com.cabolabs.ehrserver.query.Query.findAllByType('composition')}" />
  <g:select name="queries"
            from="${queries}"
            multiple="multiple"
            optionKey="id" size="5"
            value="${ehrQueryInstance?.queries*.id}"
            optionValue="${{it.name +' ('+ it.uid +')'}}"
            class="form-control"/>
  <g:if test="${queries.size() == 0}">
    <span class="help-block"><g:message code="ehrquery.create.noCompoQueries" /></span>
  </g:if>
</div>



