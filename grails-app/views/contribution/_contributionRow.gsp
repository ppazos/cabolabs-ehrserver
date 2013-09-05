<%-- usado desde ehr/_ehrContributions.gsp y contribution/show --%>
<tr>
  <td rowspan="${contribution.versions.size()}">${contribution.audit.timeCommitted}</td>
  <td rowspan="${contribution.versions.size()}">${contribution.audit.committer.name}</td>

  <%-- procesa primer composition --%>
  <g:set var="cindex" value="${contribution.versions[0].data}" />
  <td class="contribution_data_date">${cindex.startTime}</td>
  <td class="contribution_data_archid">${cindex.archetypeId}</td>
  <td>
    <g:link controller="ehr" action="showComposition" params="[uid:cindex.uid]" title="Ver XML ${cindex.uid}" target="_blank"><img src="${resource(dir: 'images', file: 'xml.png')}" class="icon" /></g:link>
    <g:link controller="ehr" action="showCompositionUI" params="[uid:cindex.uid]" title="Ver Documento ${cindex.uid}" class="showCompo"><img src="${resource(dir: 'images', file: 'doc.png')}" class="icon" /></g:link>
  </td>
</tr>
<%-- procesa el resto de las compositions si hay alguna --%>
<g:each in="${contribution.versions}" var="version" status="i">
  <g:if test="${i != 0}">
   <g:set var="cindex" value="${version.data}" />
   <tr>
     <td class="contribution_data_date">${cindex.startTime}</td>
     <td class="contribution_data_archid">${cindex.archetypeId}</td>
     <td>
       <g:link controller="ehr" action="showComposition" params="[uid:cindex.uid]" title="Ver XML ${cindex.uid}" target="_blank"><img src="${resource(dir: 'images', file: 'xml.png')}" class="icon" /></g:link>
       <g:link controller="ehr" action="showCompositionUI" params="[uid:cindex.uid]" title="Ver Documento ${cindex.uid}" class="showCompo"><img src="${resource(dir: 'images', file: 'doc.png')}" class="icon" /></g:link>
     </td>
   </tr>
  </g:if>
</g:each>