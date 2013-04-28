<%@ page import="ehr.Ehr" %>
<%--

in: contributions

=========================================================
          
Muestra tabla: con rowspan por los datos de la contribution,
y muestra todas las compositions de la contrib:

|------------------------------------------------------------
|                |           | creation date 1 | type 1 |
| time committed | committer |-------------------------------
|                |           | creation date 2 | type 2 |
|------------------------------------------------------------

--%>
<table>
  <tr>
    <th>time committed</th>
    <th>committer</th>
    <th>creation date</th>
    <th>type</th>
    <th></th>
  </tr>
  <g:each in="${contributions}" var="contribution">
    <tr>
      <td rowspan="${contribution.versions.size()}">${contribution.audit.timeCommitted}</td>
      <td rowspan="${contribution.versions.size()}">${contribution.audit.committer.name}</td>

      <%-- procesa primer composition --%>
      <g:set var="cindex" value="${contribution.versions[0].data}" />
      <td class="contribution_data_date">${cindex.startTime}</td>
      <td class="contribution_data_archid">${cindex.archetypeId}</td>
      <td>
        <g:link action="showComposition" params="[uid:cindex.uid]" title="Ver XML ${cindex.uid}" target="_blank"><img src="${resource(dir: 'images', file: 'xml.png')}" class="icon" /></g:link>
        <g:link action="showCompositionUI" params="[uid:cindex.uid]" title="Ver Documento ${cindex.uid}" class="showCompo"><img src="${resource(dir: 'images', file: 'doc.png')}" class="icon" /></g:link>
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
          <g:link action="showComposition" params="[uid:cindex.uid]" title="Ver XML ${cindex.uid}" target="_blank"><img src="${resource(dir: 'images', file: 'xml.png')}" class="icon" /></g:link>
          <g:link action="showCompositionUI" params="[uid:cindex.uid]" title="Ver Documento ${cindex.uid}" class="showCompo"><img src="${resource(dir: 'images', file: 'doc.png')}" class="icon" /></g:link>
        </td>
      </tr>
      </g:if>
    </g:each>
  </g:each>
</table>

<%-- Modal para mostrar el contenido de una composition --%>

<div id="composition_modal" style="width:960px; height:600px; display:none;"><iframe src="" style="padding:0; margin:0; width:960px; height:600px; border:0;"></iframe></div>