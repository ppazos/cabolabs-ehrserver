<g:if test="${groupedByEhr}"><%-- is a map of lists of compo indexes, the key is the ehrUid --%>
  <g:each in="${compositionIndexInstanceList}" status="j" var="ehrCis">
    <div class="row">
      <div class="col-lg-12">
        <label>EHR: ${ehrCis.key}</label>
        <div class="table-responsive">
          <table class="table table-striped table-bordered table-hover">
            <thead>
              <tr>
                <g:sortableColumn property="category" title="${message(code: 'compositionIndex.category.label', default: 'Category')}" />
                <g:sortableColumn property="startTime" title="${message(code: 'compositionIndex.startTime.label', default: 'Start Time')}" />
                <g:sortableColumn property="archetypeId" title="${message(code: 'compositionIndex.archetypeId.label', default: 'Archetype Id')}" />
                <g:sortableColumn property="ehrUid" title="${message(code: 'compositionIndex.ehrUid.label', default: 'Ehr')}" />
                <g:sortableColumn property="subjectId" title="${message(code: 'compositionIndex.subjectId.label', default: 'Subject')}" />
                <g:sortableColumn property="uid" title="${message(code: 'compositionIndex.uid.label', default: 'Uid')}" />
                <th></th>
              </tr>
            </thead>
            <tbody>
              <g:each in="${ehrCis.value}" status="i" var="compositionIndexInstance">
                <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
                  <td><g:link action="show" id="${compositionIndexInstance.id}">${fieldValue(bean: compositionIndexInstance, field: "category")}</g:link></td>
                  <td><g:formatDate date="${compositionIndexInstance.startTime}" /></td>
                  <td>${fieldValue(bean: compositionIndexInstance, field: "archetypeId")}</td>
                  <td>${fieldValue(bean: compositionIndexInstance, field: "ehrUid")}</td>
                  <td>${fieldValue(bean: compositionIndexInstance, field: "subjectId")}</td>
                  <td>${fieldValue(bean: compositionIndexInstance, field: "uid")}</td>
                  <td>
                    <g:link controller="ehr" action="showComposition" params="[uid:compositionIndexInstance.uid]" title="Ver XML ${compositionIndexInstance.uid}" target="_blank"><img src="${assetPath(src:'xml.png')}" class="icon" /></g:link>
                    <g:link controller="ehr" action="showCompositionUI" params="[uid:compositionIndexInstance.uid]" title="Ver Documento ${compositionIndexInstance.uid}" target="_blank"><img src="${assetPath(src:'doc.png')}" class="icon" /></g:link>
                  </td>
                </tr>
              </g:each>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </g:each>
</g:if>
<g:else>
  <div class="table-responsive">
    <table class="table table-striped table-bordered table-hover">
      <thead>
        <tr>
          <g:sortableColumn property="category" title="${message(code: 'compositionIndex.category.label', default: 'Category')}" />
          <g:sortableColumn property="startTime" title="${message(code: 'compositionIndex.startTime.label', default: 'Start Time')}" />
          <g:sortableColumn property="archetypeId" title="${message(code: 'compositionIndex.archetypeId.label', default: 'Archetype Id')}" />
          <g:sortableColumn property="ehrUid" title="${message(code: 'compositionIndex.ehrUid.label', default: 'Ehr')}" />
          <g:sortableColumn property="subjectId" title="${message(code: 'compositionIndex.subjectId.label', default: 'Subject')}" />
          <g:sortableColumn property="uid" title="${message(code: 'compositionIndex.uid.label', default: 'Uid')}" />
          <th></th>
        </tr>
      </thead>
      <tbody>
       <g:each in="${compositionIndexInstanceList}" status="i" var="compositionIndexInstance">
         <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
           <td><g:link action="show" id="${compositionIndexInstance.id}">${fieldValue(bean: compositionIndexInstance, field: "category")}</g:link></td>
           <td><g:formatDate date="${compositionIndexInstance.startTime}" /></td>
           <td>${fieldValue(bean: compositionIndexInstance, field: "archetypeId")}</td>
           <td>${fieldValue(bean: compositionIndexInstance, field: "ehrUid")}</td>
           <td>${fieldValue(bean: compositionIndexInstance, field: "subjectId")}</td>
           <td>${fieldValue(bean: compositionIndexInstance, field: "uid")}</td>
           <td>
             <g:link controller="ehr" action="showComposition" params="[uid:compositionIndexInstance.uid]" title="Ver XML ${compositionIndexInstance.uid}" target="_blank"><img src="${assetPath(src:'xml.png')}" class="icon" /></g:link>
             <g:link controller="ehr" action="showCompositionUI" params="[uid:compositionIndexInstance.uid]" title="Ver Documento ${compositionIndexInstance.uid}" target="_blank"><img src="${assetPath(src:'doc.png')}" class="icon" /></g:link>
           </td>
         </tr>
       </g:each>
      </tbody>
    </table>
  </div>
</g:else>
