<%-- usado desde ehr/_ehrContributions.gsp y contribution/show --%>
<g:if test="${hideTimeAndCommitter}"><!-- para listar las versions sin agrupacion de contribution -->

  <g:each in="${contribution.versions}" var="version" status="i">
    <g:set var="cindex" value="${version.data}" />
    <tr>
      <td>${version.uid}</td>
      <td>${cindex.startTime}</td>
      <td>${cindex.archetypeId}</td>
      <td>${version.commitAudit.changeType}</td>
      <td>
        <input type="checkbox" name="versioned_object.uid" value="${version.objectId}" />
        <g:link controller="ehr" action="showComposition" params="[uid:cindex.uid]" title="Ver XML ${cindex.uid}" target="_blank"><asset:image src="xml.png" class="icon" /></g:link>
        <g:link controller="ehr" action="showCompositionUI" params="[uid:cindex.uid]" title="Ver Documento ${cindex.uid}" class="showCompo"><asset:image src="doc.png" class="icon" /></g:link>
      </td>
    </tr>
  </g:each>

</g:if>
<g:else>
	<tr>
	  <td rowspan="${contribution.versions.size()}">${contribution.audit.timeCommitted}</td>
	  <td rowspan="${contribution.versions.size()}">${contribution.audit.committer.name}</td>
	
	  <%-- procesa primer composition --%>
	  <g:set var="cindex" value="${contribution.versions[0].data}" />
	  <td class="contribution_data_date">${cindex.startTime}</td>
	  <td class="contribution_data_archid">${cindex.archetypeId}</td>
	  <td>${contribution.versions[0].commitAudit.changeType}</td>
	  <td>
	    <input type="checkbox" name="versioned_object.uid" value="${contribution.versions[0].objectId}" />
	    <g:link controller="ehr" action="showComposition" params="[uid:cindex.uid]" title="Ver XML ${cindex.uid}" target="_blank"><asset:image src="xml.png" class="icon" /></g:link>
	    <g:link controller="ehr" action="showCompositionUI" params="[uid:cindex.uid]" title="Ver Documento ${cindex.uid}" class="showCompo"><asset:image src="doc.png" class="icon" /></g:link>
	  </td>
	</tr>
	<%-- procesa el resto de las compositions si hay alguna --%>
	<g:each in="${contribution.versions}" var="version" status="i">
	  <g:if test="${i != 0}">
	   <g:set var="cindex" value="${version.data}" />
	   <tr>
	     <td class="contribution_data_date">${cindex.startTime}</td>
	     <td class="contribution_data_archid">${cindex.archetypeId}</td>
	     <td>${version.commitAudit.changeType}</td>
	     <td>
	       <input type="checkbox" name="versioned_object.uid" value="${version.objectId}" />
	       <g:link controller="ehr" action="showComposition" params="[uid:cindex.uid]" title="Ver XML ${cindex.uid}" target="_blank"><asset:image src="xml.png" class="icon" /></g:link>
	       <g:link controller="ehr" action="showCompositionUI" params="[uid:cindex.uid]" title="Ver Documento ${cindex.uid}" class="showCompo"><asset:image src="doc.png" class="icon" /></g:link>
	     </td>
	   </tr>
	  </g:if>
	</g:each>
</g:else>