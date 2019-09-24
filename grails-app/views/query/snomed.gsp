<!doctype html>
<html>
  <head>
    <meta name="layout" content="admin">
    <title><g:message code="query.snomed.title" /></title>
    
	<asset:stylesheet src="highlightjs/xcode.css" />
    <asset:javascript src="highlight.pack.js" />
  </head>
	<body>
		<div class="row">
			<div class="col-lg-12">
				<h1><g:message code="query.snomed.title" /></h1>
			</div>
		</div>
		<div class="row">
			<div class="col-md-12">
				<g:form class="form" action="snomed">
					<div class="table-responsive">
						<table class="table table-striped table-bordered table-hover">
							<tr>
								<td class="fieldcontain">
								<label for="name"><g:message code="query.snomed.expression.attr" default="Name" /> *</label>
								</td>
								<td>                   
									<input type="text" class="form-control input-sm query_name" id="ecl"/>                    
								</td>
							</tr>
							<tr>
								<td class="fieldcontain">
								<label for="name"><g:message code="query.snomed.terminology.attr" default="Name" /> *</label>
								</td>
								<td>                   
									<input type="text" class="form-control input-sm query_name" id="term"/>
								</td>
							</tr>
						</table>
					</div>
					<div class="btn-toolbar" role="toolbar">
						<button type="submit" id="submit" name="filter" class="btn btn-primary"><span class="fa fa-share" aria-hidden="true"></span></button>
						<button type="reset" id="reset" class="btn btn-default"><span class="fa fa-trash " aria-hidden="true"></span></button>
					</div>
				</g:form>
				<script>
					$('#submit').on('click', function(e) {
						e.preventDefault();

						button = $(this);
						let ecl = $('#ecl').val;
						let term = $('#term').value();
						console.log(ecl);

						let url = 'executeSnomed?'

						

						$.ajax({
							method: 'GET',
							url,
							dataType: 'json'
						})
						.done(function( res ) {							
						})
						.fail(function(resp,status,status_msg) {
							console.log(resp);
						});
					});
				</script>
			</div>
		</div>
		<div class="row">
			<div class="col-md-12">
				<g:message code="query.snomed.json.title" />
				<pre>
					<code id="json" class="json">
					</code>
				</pre>
				<script type="text/javascript">
					$('#json').addClass('xml');
					// The first replace removes the new lines and empty spaces of indentation
					// The second escapes single quotes that might appear in the text of the XML that breaks the javascript
					$('#json').each(function(i, e) { hljs.highlightBlock(e); });
				</script>
			</div>
		</div>
	</body>
</html>
