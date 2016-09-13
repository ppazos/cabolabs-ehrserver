<%@ page import="com.cabolabs.util.DateDifference" %>
<tr>
  <th><g:message code="person.name.label" default="Patient name" /></th>
  <td>${person.firstName} ${person.lastName} (${DateDifference.numberOfYears(person.dob, new Date())})</td>
</tr>
<tr>
  <th><g:message code="person.name.label" default="Patient UID" /></th>
  <td><g:link controller="person" action="show" params="[uid: person.uid]">${person.uid}</g:link></td>
</tr>