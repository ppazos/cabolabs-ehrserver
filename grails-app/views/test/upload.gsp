<!DOCTYPE HTML>
<html>
  <head>
    <title><g:message code="opt.upload.title" /></title>
  </head>
  <body>
    <h1><g:message code="opt.upload.title" /></h1>
    
    <g:if test="${errors}">
      <ul>
        <g:each in="${errors}">
          <li>${it}</li>
        </g:each>
      </ul>
    </g:if>
    
    <g:form action="upload" enctype="multipart/form-data" useToken="true">
      <g:if test="${ask_overwrite}">
	      <label><input type="radio" name="overwrite" value="false" />No</label>
	      <label><input type="radio" name="overwrite" value="true" />Yes</label>
	      (Submit the form again)
	    </g:if>
      <span class="button">
        <input type="file" name="opt" value="${params.opt}" />
        <input type="submit" class="upload" name="doit" value="upload" />
      </span>
    </g:form>
  </body>
</html>