<!DOCTYPE HTML>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="opt.upload.title" /></title>
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
        <g:form action="upload" enctype="multipart/form-data" useToken="true">
          
          <g:message code="opt.upload.label.overwrite" />
          <label><input type="radio" name="overwrite" value="false" checked="true" /><g:message code="default.no" /></label>
          <label><input type="radio" name="overwrite" value="true" /><g:message code="default.yes" /></label>
          <br/>
         
          <sec:ifAnyGranted roles="ROLE_ADMIN">
            <label><g:message code="opt.upload.label.isPublic" />
              <g:checkBox name="isPublic" value="${false}" />
            </label>
            <br/>
          </sec:ifAnyGranted>
    
          <div class="btn-toolbar" role="toolbar">
            <input type="file" name="opt" value="${params.opt}" class="btn btn-default btn-md" required="required" />
            <br/>
            <input type="submit" class="upload btn btn-default btn-md" name="doit" value="${g.message(code:'opt.upload.label.upload')}" />
          </div>
        </g:form>
      </div>
    </div>
  </body>
</html>
