<!DOCTYPE HTML>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="opt.upload.title" /></title>
    <style>
      #alternative_opts {
        display: none;
      }
    </style>
  </head>
  <body>
    <div class="row">
      <div class="col-lg-12">
      <h1><g:message code="opt.upload.title" /></h1>
      </div>
    </div>

    <div class="row row-grid">
      <div class="col-lg-12">
        <g:if test="${flash.message}">
          <div class="alert alert-info" role="alert"><g:message code="${flash.message}" /></div>
        </g:if>
      
        <g:if test="${errors}">
          <ul>
            <g:each in="${errors}">
              <li>${it.encodeAsHTML()}</li>
            </g:each>
          </ul>
        </g:if>
        
        <g:form action="upload" enctype="multipart/form-data" useToken="true" name="upload_form">
          
          <input type="hidden" name="doit" value="now" />
          
          <div class="form-group">
            <label class="control-label"><g:message code="opt.upload.label.overwrite" /></label>
            <fieldset class="form-control">
              <label><input type="radio" name="overwrite" value="false" checked="true" /><g:message code="default.no" /></label>
              <label><input type="radio" name="overwrite" value="true" /><g:message code="default.yes" /></label>
            </fieldset>
          </div>

          <sec:ifAnyGranted roles="ROLE_ADMIN">
            <div class="form-group">
              <label class="control-label"><g:message code="opt.upload.label.isPublic" />
                <g:checkBox name="isPublic" value="${false}" class="form-control" />
              </label>
            </div>
          </sec:ifAnyGranted>
          
          <div class="form-group">
            <label class="control-label"><g:message code="opt.upload.label.opt" /></label>
            <input type="file" name="opt" value="${params.opt}" class="btn btn-default btn-md form-control" required="required" />
          </div>
          
          <%-- this will be filled with alternatives when duplicates are detected on upload, the user should resolve the conflict --%>
          <div class="form-group" id="alternative_opts">
            <label class="control-label"><g:message code="opt.upload.label.opt_alternatives" /></label>
            <select name="versionOfTemplateUid" class="form-control">
              <option value=""></option>
            </select>
          </div>
    
          <div class="btn-toolbar" role="toolbar">
            <input type="submit" class="upload btn btn-default btn-md" name="doit" value="${g.message(code:'opt.upload.label.upload')}" />
          </div>
          
        </g:form>
      </div>
    </div>
    
    <asset:javascript src="jquery.iframe-transport.js" /><!-- AJAX file upload -->
    
    <script>
    
    $('form[name="upload_form"]').on('submit', function(e) {
   
      e.preventDefault();
      
      form = $(this);
    
      // https://cmlenz.github.io/jquery-iframe-transport/
      $.ajax({
        url: '${createLink(action:"upload")}',
        dataType: 'json',
        data: form.serializeArray(),//form.serialize(),
        processData: false,
        files: $(":file", this),
        iframe: true
      })
      .complete(function(data) {
      
        console.log('complete', data);
      })
      .done(function( data ) {
      
        console.log('done', data);
      
        if (data.status == "ok")
        {
          $('body').prepend(
            '<div class="alert alert-info alert-dismissible" role="alert" style="position: fixed; top: 10px; z-index: 1099; display: block; width:80%; left:10%;"><button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button><span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>'+
            data.message +'</div>'
          );
          location.href = '${createLink("action": "show")}?uid='+ data.opt.uid;
        }
        else if (data.status == "error")
        {
          $('body').prepend(
            '<div class="alert alert-danger alert-dismissible" role="alert" style="position: fixed; top: 10px; z-index: 1099; display: block; width:80%; left:10%;"><button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button><span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>'+
            data.message +'</div>'
          );
          // display data.errors
        }
        else if (data.status == "resolve_duplicate")
        {
          // TODO: versioning
          console.log('version alternatives', data.alternatives);
          
          $('body').prepend(
            '<div class="alert alert-warning alert-dismissible" role="alert" style="position: fixed; top: 10px; z-index: 1099; display: block; width:80%; left:10%;"><button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button><span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>'+
            data.message +'</div>'
          );
          
          $.each(data.alternatives, function( i, opt ) {
            $('select', '#alternative_opts').append('<option value="'+ opt.uid +'">'+ opt.concept +' ('+ opt.templateId +')</option>');
          });
          
          $('#alternative_opts').show();
        }
      
      })
      .fail(function(resp,status,status_msg) {
      
         console.log('fail', resp);
         $('body').prepend(
           '<div class="alert alert-danger alert-dismissible" role="alert" style="position: fixed; top: 10px; z-index: 1099; display: block; width:80%; left:10%;"><button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button><span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>'+
           resp.responseJSON.message +'</div>'
         );
      });

    });
    </script>
    
  </body>
</html>
