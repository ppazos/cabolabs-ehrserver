<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="person_id_type.show.title" /></title>
  </head>
  <body>

    <div class="row">
      <div class="col-lg-12">
        <h1><g:message code="person_id_type.show.title" /></h1>
      </div>
    </div>
      
    <div class="row">
      <div class="col-lg-12">
        <g:if test="${flash.message}">
	       <div class="message" role="status">${flash.message}</div>
	     </g:if>
	     
        <table class="table">
          <tbody>
            <tr>
              <th><g:message code="person_id_type.attr.name" default="Name" /></th>
              <td><g:fieldValue bean="${personIdTypeInstance}" field="name"/></td>
            </tr>
            <tr>
              <th><g:message code="person_id_type.attr.code" default="Code" /></th>
              <td><g:fieldValue bean="${personIdTypeInstance}" field="code"/></td>
            </tr>
          </tbody>
        </table>

	     <div class="btn-toolbar" role="toolbar">
	       <g:link class="edit" action="edit" resource="${personIdTypeInstance}"><button type="button" class="btn btn-default btn-md"><span class="fa fa-edit fa-fw" aria-hidden="true"></span> <g:message code="default.button.edit.label" default="Edit" /></button></g:link>
	       <g:link action="delete" id="${personIdTypeInstance?.id}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
	         <button type="button" class="btn btn-default btn-md">
	           <span class="fa fa-minus-circle fa-fw" aria-hidden="true"></span> <g:message code="default.button.delete.label" default="Delete" />
	         </button>
	       </g:link>
	     </div>
	   </div>
	 </div>
  </body>
</html>
