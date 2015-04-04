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
    <th>change type</th>
    <th></th>
  </tr>
  <g:each in="${contributions}" var="contribution">
  
    <g:render template="../contribution/contributionRow" model="[contribution:contribution]"/>
  
  </g:each>
</table>

<%-- Modal para mostrar el contenido de una composition --%>
<div id="composition_modal" style="width:960px; height:600px; display:none;"><iframe src="" style="padding:0; margin:0; width:960px; height:600px; border:0;"></iframe></div>
