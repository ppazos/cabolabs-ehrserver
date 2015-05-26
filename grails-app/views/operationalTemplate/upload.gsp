<!DOCTYPE HTML>
<html>
  <head>
    <meta name="layout" content="main">
    <title><g:message code="opt.upload.title" /></title>
  </head>
  <body>
    <a href="#list-template" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
    <div class="nav" role="navigation">
      <ul>
        <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
        <li><g:link class="list" action="list"><g:message code="opt.list.label" /></g:link></li>
      </ul>
    </div>
    <div id="upload-operationalTemplate" class="content scaffold-create" role="main">
	    <h1><g:message code="opt.upload.title" /></h1>
	    
	    <g:if test="${flash.message}">
	      <div class="message" role="status"><g:message code="${flash.message}" /></div>
	    </g:if>
	    
	    <g:if test="${errors}">
	      <ul>
	        <g:each in="${errors}">
	          <li>${it.encodeAsHTML()}</li>
	        </g:each>
	      </ul>
	    </g:if>
	    <g:form action="upload" enctype="multipart/form-data" useToken="true">
	      
	      Overwrite if OPT exists:
	      
		   <label><input type="radio" name="overwrite" value="false" checked="true" />No</label>
		   <label><input type="radio" name="overwrite" value="true" />Yes</label>
		   
		   <br/>
	
	      <span class="button">
	        <input type="file" name="opt" value="${params.opt}" />
	        <input type="submit" class="upload" name="doit" value="upload" />
	      </span>
	    </g:form>
    
    </div>
  </body>
</html>
