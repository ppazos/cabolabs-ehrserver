<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="folderTemplate.show.title" /></title>
    <style>

    </style>
    
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/jstree/3.2.1/themes/default/style.min.css" />
    <script src="https://cdnjs.cloudflare.com/ajax/libs/jstree/3.2.1/jstree.min.js"></script>
    
    <script type="text/javascript">
    $(function () {
      $('#jstree_container').jstree({
        'core' : {
          'multiple' : false,
          'data' : ${foldersTree}
        }
      })
      .bind("ready.jstree", function (event, data) {
        $(this).jstree("open_all");
      });
    });
    </script>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
        <h1 class="hidden_uid"><g:message code="folderTemplate.show.title" /></h1>
      </div>
    </div>
    <div class="row row-grid">
      <div class="col-lg-12">
        
        <g:if test="${flash.message}">
          <div class="alert alert-info" role="alert">${flash.message}</div>
        </g:if>
        
        <table class="table">
          <tbody>
            <tr>
              <th><g:message code="folderTemplate.attr.name" /></th>
              <td>${folderTemplate.name}</td>
            </tr>
             <tr>
              <th><g:message code="folderTemplate.attr.description" /></th>
              <td>${folderTemplate.description}</td>
            </tr>
            <tr>
              <th><g:message code="folderTemplate.attr.folders" /></th>
              <td>
                <div id="jstree_container"></div>
              </td>
            </tr>
          </tbody>
        </table>

      </div>
    </div>
  </body>
</html>
