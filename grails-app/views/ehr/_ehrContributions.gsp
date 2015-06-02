<%@ page import="ehr.Ehr" %>
<%--

in: contributions

=========================================================
          
Muestra tabla: con rowspan por los datos de la contribution,
y muestra todas las compositions de la contrib:

|-------------------------------------------------------------
|                               |                            | 
|    contrib time committed     |          committer         |
|                               |                            |
|-------------------------------------------------------------
| |--------------------------------------------------------| | 
| | version uid 1 | creation date 1 | type 1 | change type | |
| | version uid 1 | creation date 1 | type 1 | change type | |
| |--------------------------------------------------------| |
|------------------------------------------------------------|

--%>
<g:each in="${contributions}" var="contribution">
  <table>
  <tr>
  <th>time committed</th>
  <th>committer</th>
</tr>
    <g:render template="../contribution/contributionRow" model="[contribution:contribution]"/>
  </table>
</g:each>


<%-- Modal para mostrar el contenido de una composition --%>
<div id="composition_modal" style="width:960px; height:600px; display:none;"><iframe src="" style="padding:0; margin:0; width:960px; height:600px; border:0;"></iframe></div>
