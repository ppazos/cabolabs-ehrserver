<!DOCTYPE html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title>Esto es una prueba para swagger</title>
  </head>
  <body>
	 <div class="table-responsive">
           <table class="table table-striped table-bordered table-hover">
		        <thead>
		          <tr>
		            <th>
		               Probar
		            </th>
		             <th>
		              Descargar Contenido
		            </th>
		             <th>
		               Valido
		            </th>
		          </tr>
		        </thead>
		        <tbody>
			     	<g:each in="${opts}" status="i" var="templateInstance">
			          <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
			            <td>
			              <g:link url="${templateInstance.value.urlToSwaggerGeneratorWitUrlFile}">${templateInstance.key}</g:link>	   
			            </td>
			             <td>
			              <g:link action="show" params="[fileNameSwagger:templateInstance.key]" title="Ver archivo json">Mostrar Swagger controlador ${templateInstance.key}</g:link>
			            </td>
			             <td>
			                 ${templateInstance.value.isValidSwaggerFileContent}
			            </td>							            
			          </tr>
			        </g:each>
		        </tbody>
		      </table>
		  </div>
  </body>
</html>
