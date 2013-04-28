<%@ page import="util.DateDifference" %>
<div id="datosPaciente" class="hidden_uid sex_${person.sex}">
  ${person.firstName} ${person.lastName}
  (${DateDifference.numberOfYears(person.dob, new Date())})
  <span class="uid">uid: ${person.uid}</span>
</div>