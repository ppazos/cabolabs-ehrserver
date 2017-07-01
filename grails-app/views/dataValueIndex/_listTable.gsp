<div class="table-responsive">
 <table class="table table-striped table-bordered table-hover">
   <thead>
     <tr>
       <g:sortableColumn property="templateId" title="${message(code: 'datavalueindex.templateId.label', default: 'Template')}" />
       <g:sortableColumn property="archetypeId" title="${message(code: 'datavalueindex.archetypeId.label', default: 'Archetype')}" />
       <g:sortableColumn property="path" title="${message(code: 'datavalueindex.path.label', default: 'Template Path')}" />
       <g:sortableColumn property="archetypePath" title="${message(code: 'datavalueindex.archetypePath.label', default: 'Arhcetype Path')}" />
       <g:sortableColumn property="rmTypeName" title="${message(code: 'datavalueindex.rmTypeName.label', default: 'Type')}" />
       <th>${message(code: 'datavalueindex.owner.label', default: 'Composition')}</th>
     </tr>
   </thead>
   <tbody>
     <g:each in="${datavalues}" status="i" var="dv">
       <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
         <td>${fieldValue(bean: dv, field: "templateId")}</td>
         <td>${fieldValue(bean: dv, field: "archetypeId")}</td>
         <td>${fieldValue(bean: dv, field: "path")}</td>
         <td>${fieldValue(bean: dv, field: "archetypePath")}</td>
         <td>${fieldValue(bean: dv, field: "rmTypeName")}</td>
         <td>${dv.owner.uid}</td>
       </tr>
     </g:each>
   </tbody>
 </table>
</div>
