<!doctype html>
<html>
    <head>
        <title>Access denied</title>
        <meta name="layout" content="admin">
        <g:if env="development"><asset:stylesheet src="errors.css"/></g:if>
    </head>
    <body>
        <ul class="errors">
            <li>Error: access denied</li>
            <li>Path: ${request.forwardURI}</li>
        </ul>
    </body>
</html>
