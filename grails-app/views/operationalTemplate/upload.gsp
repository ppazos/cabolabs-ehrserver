<!DOCTYPE HTML>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="opt.upload.title" /></title>
    <style>
      #alternative_opts {
        display: none;
      }
      #validation_errors.row {
        margin-bottom: 15px;
      }
    </style>
    <asset:link rel="stylesheet" href="font-awesome.min.css" type="text/css" />
    <asset:stylesheet src="animate.css" />
    <asset:stylesheet src="pnotify.custom.min.css" />
    <asset:javascript src="pnotify.custom.min.js" />
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

        <div id="validation_errors"></div>

        <g:form action="upload" enctype="multipart/form-data" useToken="true" name="upload_form">

          <input type="hidden" name="doit" value="now" />

          <div class="form-group">
            <div class="input-group">
              <label class="input-group-btn">
                <span class="btn btn-primary">
                  Browse&hellip; <input type="file" name="opt" style="display: none;" /><!-- multiple /> -->
                </span>
              </label>
              <input type="text" class="form-control" readonly />
            </div>
            <span class="help-block">
              <g:message code="opt.upload.file.help" />
            </span>
          </div>

          <%-- this will be filled with alternatives when duplicates are detected on upload, the user should resolve the conflict --%>
          <div class="form-group" id="alternative_opts">
            <label class="control-label"><g:message code="opt.upload.label.opt_alternatives" /></label>
            <select name="versionOfTemplateUid" class="form-control">
              <option value=""></option>
            </select>
          </div>

          <div class="btn-toolbar" role="toolbar">
            <button type="submit" class="upload btn btn-default btn-md" name="doit">${g.message(code:'opt.upload.label.upload')}</button>
          </div>

        </g:form>
      </div>
    </div>

    <asset:javascript src="jquery.iframe-transport.js" /><!-- AJAX file upload -->
    <asset:javascript src="loading-button.js" />
    <script>

    $(function() {

      // registers loading spinner on submit button
      $('[name="doit"]').loading();

      // We can attach the `fileselect` event to all file inputs on the page
      $(document).on('change', ':file', function() {
        var input = $(this),
            numFiles = input.get(0).files ? input.get(0).files.length : 1,
            label = input.val().replace(/\\/g, '/').replace(/.*\//, '');
        input.trigger('fileselect', [numFiles, label]);
      });

      // We can watch for our custom `fileselect` event like this
      $(':file').on('fileselect', function(event, numFiles, label) {

        var input = $(this).parents('.input-group').find(':text'),
            log = numFiles > 1 ? numFiles + ' files selected' : label;

        if( input.length ) {
          input.val(log);
        } else {
          if (log)
          {
            new PNotify({
              title: 'Warning',
              text : log,
              type : 'error',
              styling: 'bootstrap3',
              history: false,
              stack: {
                "dir1": "down", "dir2": "left", "push": "bottom", "spacing1": 25, "spacing2": 25, "context": $("body"),
                modal: true, overlay_close: true
              },
              addclass: 'pnotify-center'
            });
          }
        }
      });
    });


    $('form[name="upload_form"]').on('submit', function(e) {

      e.preventDefault();

      form = $(this);
      data = form.serializeArray();
      data.push({name:'doit', value:true}); // the submit button is not serialized, we add it manually

      // client side check for file size
      if ($(":file", this)[0].files[0].size > 5242880)
      {
        $('body').prepend(`
          <div class="alert alert-danger alert-dismissible" role="alert" style="position: fixed; top: 10px; z-index: 1099; display: block; width:80%; left:10%;">
            <button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            <span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>
            The template size is bigger than 5Mb (you can minify big XMLs by removing new lines and indentation)
          </div>
        `);

        $('[name="doit"]').loading('stop');

        return false;
      }

      // https://cmlenz.github.io/jquery-iframe-transport/
      $.ajax({
        url: '${createLink(action:"upload")}',
        dataType: 'json',
        data: data,
        processData: false,
        files: $(":file", this),
        iframe: true
      })
      .always(function(data) {

        console.log('complete', data);
      })
      .done(function( data ) {

        //console.log('done', data);

        // clean validation errors
        $('#validation_errors').empty();

        if (data.status == "ok")
        {
          $('body').prepend(
            '<div class="alert alert-info alert-dismissible" role="alert" style="position: fixed; top: 10px; z-index: 1099; display: block; width:80%; left:10%;"><button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button><span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span> '+
            data.message +'</div>'
          );
          location.href = '${createLink("action": "show")}/'+ data.opt.localUid;
        }
        else if (data.status == "error")
        {
          // removes loading spinner from button
          $('[name="doit"]').loading('stop');

          $('body').prepend(
            '<div class="alert alert-danger alert-dismissible" role="alert" style="position: fixed; top: 10px; z-index: 1099; display: block; width:80%; left:10%;"><button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button><span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span> '+
            data.message +'</div>'
          );
          // clean and display data.errors
          row = $('#validation_errors').addClass('row');

          col1 = $('<div class="col-md-4" />');
          error_container = $('<ul class="nav nav-pills nav-stacked"></ul>');
          col1.append(error_container);
          row.append(col1);

          col2 = $('<div class="col-md-8" />');
          details_container = $('<div class="tab-content"></div>');
          col2.append(details_container);
          row.append(col2);

          for (i in data.errors)
          {
            j = data.errors[i].indexOf(':');
            parts = [data.errors[i].substring(0, j), data.errors[i].substring(j+1)];

            error_container.append('<li role="presentation"'+ (i == 0 ? ' class="active"' : '') +'><a href="#ed'+ i +'" aria-controls="ed'+ i +'" role="tab" data-toggle="tab">'+ parts[0] +'</a></li>');
            details_container.append('<div role="tabpanel" class="tab-pane'+ (i == 0 ? ' active' : '') +'" id="ed'+ i +'">'+ parts[1] +'</div>');
          }
        }
        else if (data.status == "resolve_duplicate")
        {
          // TODO: versioning
          console.log('version alternatives', data.alternatives);

          // removes loading spinner from button
          $('[name="doit"]').loading('stop');

          $('body').prepend(
            '<div class="alert alert-warning alert-dismissible" role="alert" style="position: fixed; top: 10px; z-index: 1099; display: block; width:80%; left:10%;"><button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button><span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span> '+
            data.message +'</div>'
          );

          $.each(data.alternatives, function( i, opt ) {
            $('select', '#alternative_opts').append('<option value="'+ opt.localUid +'">'+ opt.concept +' ('+ opt.templateId +')</option>');
          });

          $('#alternative_opts').show();
        }
      })
      .fail(function(resp,status,status_msg) {

        // removes loading spinner from button
        $('[name="doit"]').loading('stop');

        console.log('fail', resp);
        $('body').prepend(
          '<div class="alert alert-danger alert-dismissible" role="alert" style="position: fixed; top: 10px; z-index: 1099; display: block; width:80%; left:10%;"><button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button><span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span> '+
          resp.responseJSON.message +'</div>'
        );
      });
    });
    </script>
  </body>
</html>
