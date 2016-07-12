<%@ page import="com.cabolabs.util.DateDifference" %>
<div class="control-group">
    <label class="control-label"><g:message code="person.name.label" default="Patient name" /></label>
    <div class="controls">
        <p class="form-control-static">${person.firstName} ${person.lastName} (${DateDifference.numberOfYears(person.dob, new Date())})</p>
    </div>
</div>
<div class="control-group">
    <label class="control-label"><g:message code="person.name.label" default="Patient UID" /></label>
    <div class="controls">
        <p class="form-control-static"><g:link controller="person" action="show" params="[uid: person.uid]">${person.uid}</g:link></p>
    </div>
</div>
