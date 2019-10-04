
  <g:each in="${ehr_compositionIndexInstanceList}" status="j" var="ehrCis">
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
                    <g:link controller="ehr" action="showComposition" params="[uid:compositionIndexInstance.uid]" title="Ver XML ${compositionIndexInstance.uid}" target="_blank" class="compoXml"><img src="${assetPath(src:'xml.png')}" class="icon" /></g:link>
                    <g:link controller="ehr" action="showCompositionUI" params="[uid:compositionIndexInstance.uid]" title="Ver Documento ${compositionIndexInstance.uid}" target="_blank" class="showCompo"><img src="${assetPath(src:'doc.png')}" class="icon" /></g:link>
                  </td>
                </tr>
              </g:each>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </g:each>

  <div class="modal fade" id="xml_modal" tabindex="-1" role="dialog">
    <div class="modal-dialog modal-lg" role="document">
      <div class="modal-content">
       <div class="modal-body">
         <pre><code id="xml"></code></pre>
       </div>
      </div>
    </div>
  </div>

  <div class="modal fade" id="html_modal" tabindex="-1" role="dialog">
    <div class="modal-dialog modal-lg" role="document">
      <div class="modal-content">
        <div class="modal-body">
          <iframe src="" style="padding:0; margin:0; width:100%; height:540px; border:0;"></iframe>
        </div>
      </div>
    </div>
  </div>

  <script type="text/javascript">
  $(document).ready(function() { // same code as versionedComposition show, TODO: refactor

    $('.showCompo').on('click', function(e) {

      e.preventDefault();

      iframe = $('iframe', '#html_modal');
      iframe[0].src = this.href;

      $('#html_modal').modal();
    });

    $('#html_modal').on('hidden.bs.modal', function (event) {
      iframe = $('iframe', '#html_modal');
      iframe[0].src = '';
    });

    $('.compoXml').on('click', function(e) {

      e.preventDefault();

      $.ajax({
        url: this.href,
        dataType: 'xml',
        success: function(xml, textStatus)
        {
          console.log('xml', xml);
          $('#xml').addClass('xml');
          $('#xml').text(formatXml( xmlToString(xml) ));
          $('#xml').each(function(i, e) { hljs.highlightBlock(e); });

          $('#xml_modal').modal();
        }
      });

    });

    $('#xml_modal').on('hidden.bs.modal', function (event) {
      $('#xml').text('');
    });
  });
  </script>
