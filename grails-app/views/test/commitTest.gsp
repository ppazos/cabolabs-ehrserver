<html>
  <head>
    <meta name="layout" content="main">
    <title><g:message code="commitTest test" /></title>
    <style type="text/css">
    textarea {
      width: 720px;
      height: 160px;
    }
    .out {
      height: 40px;
      background-color: #ffff00;
    }
    .content_padding {
      padding: 10px;
    }
    </style>
    
    <g:javascript>
    
    // http://stackoverflow.com/questions/6507293/convert-xml-to-string-with-jquery
    function xmlToString(xmlData) { 

	    var xmlString;
	    //IE
	    if (window.ActiveXObject){
	        xmlString = xmlData.xml;
	    }
	    // code for Mozilla, Firefox, Opera, etc.
	    else{
	        xmlString = (new XMLSerializer()).serializeToString(xmlData);
	    }
	    return xmlString;
	 }
    
    // http://grails.1312388.n4.nabble.com/Ajax-formRemote-not-working-td4633608.html
    // en el formRemote se tiene que usar "data" como nombre del parametro para el js
    
    // data es un XMLDocument (http://api.jquery.com/jQuery.parseXML)
    var commitSuccess = function(data, b, c)
    {
       console.log(data);
       console.log(b);
       console.log(c);
       
       $('#commitSuccess').text( xmlToString(data) ); // Paso el XMLDocument a texto
    };
       
    var commitFailure = function(data)
    {
       console.log(data);
       $('#commitSuccess').text('error');
    };
    
    
    $(document).ready(function() {
    
      // http://api.jquery.com/jQuery.post/
      // post( url [, data] [, success(data, textStatus, jqXHR)] [, dataType] )
      /*
      $.post(
        'ajax/test.html',
        {},
        function(data) {
         $('.result').html(data);
         }
       );
       */
       
    });
    </g:javascript>
  </head>
  <body>
    <div class="nav" role="navigation">
      <ul>
        <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
      </ul>
    </div>
    
    <div id="create-ehr" class="content scaffold-create" role="main">
      <h1>commit test</h1>
      <g:if test="${flash.message}">
        <div class="alert alert-info" role="alert">${flash.message}</div>
      </g:if>
    </div>
    
    <h2>Commit Versions</h2>
    
    <div class="content_padding">
    
    <g:formRemote name="myForm"
                  on404="alert('not found!')"
                  url="[controller:'ehr', action:'commit']"
                  onSuccess="commitSuccess(data)"
                  onFailure="commitFailure(data)">
                  
       <label for="uid">UID</label>
       <g:select name="uid"
                 from="${ehr.Ehr.list()}"
                 multiple="multiple"
                 optionKey="uid" />
       <br/><br/>
       
       <label for="version">version</label>
       <g:textArea name="versions" rows="10" cols="60">${version1}</g:textArea>
       <br/><br/>

       <label for="version">version</label>
       <g:textArea name="versions" rows="10" cols="60">${version2}</g:textArea>
       <br/><br/>
       
       <label for="version">version</label>
       <g:textArea name="versions" rows="10" cols="60">${version3}</g:textArea>
       <br/><br/>

       <g:submitButton name="commit" value="Create Contribution" />
    </g:formRemote>
    
    <textarea id="commitSuccess" class="out"></textarea>
    <div id="commitFailure"></div>
    
    [
    <g:link controller="ehr" action="indexData">Index data</g:link> |
    <g:link action="findCompositions">Find Compositions Test</g:link> |
    <g:link controller="rest" action="queryCompositions">Query Compositions by Data</g:link> |
    <g:link controller="rest" action="queryData">Query Data</g:link>
    ]
    
    </div>
  </body>
</html>