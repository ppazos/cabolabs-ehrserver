<html>
  <head>
    <meta name="layout" content="main">
    <title><g:message code="Find Compositions" /></title>
    <style type="text/css">
    textarea {
      width: 680px;
      height: 300px;
    }
    .out {
      height: 30px;
      background-color: #ffff00;
    }
    </style>
    <r:require module="jquery" />
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

	 // Formatea un string XML
    // https://gist.github.com/1083506
    function formatXml(xml) {

      var formatted = '';
      var reg = /(>)(<)(\/*)/g;
      xml = xml.replace(reg, '$1\r\n$2$3');
      var pad = 0;

      jQuery.each(xml.split('\r\n'), function(index, node)
      {
          var indent = 0;
          if (node.match( /.+<\/\w[^>]*>$/ ))
          {
              indent = 0;
          }
          else if (node.match( /^<\/\w/ ))
          {
              if (pad != 0) pad -= 1;
          }
          else if (node.match( /^<\w[^>]*[^\/]>.*$/ ))
          {
              indent = 1;
          }
          else
          {
              indent = 0;
          }
          var padding = '';
          for (var i = 0; i < pad; i++)
          {
              padding += '  ';
          }
          formatted += padding + node + '\r\n';
          pad += indent;
      });

      return formatted;
    }


    // http://grails.1312388.n4.nabble.com/Ajax-formRemote-not-working-td4633608.html
    // en el formRemote se tiene que usar "data" como nombre del parametro para el js

    // data es un XMLDocument (http://api.jquery.com/jQuery.parseXML)
    var findCompositionsSuccess = function(data)
    {
       console.log(data);
       $('#findCompositionsSuccess').text( formatXml( xmlToString(data) ) ); // Paso el XMLDocument a texto
    };

    var findCompositionsFailure = function(data, b, c)
    {
       console.log(data);
       $('#findCompositionsFailure').text( formatXml( xmlToString(data) ) );
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
      <h1>Find Compositions</h1>
      <g:if test="${flash.message}">
        <div class="alert alert-info" role="alert">${flash.message}</div>
      </g:if>
    </div>

    <g:formRemote name="myForm"
                  on404="alert('not found!')"
                  url="[controller:'test', action:'findCompositions']"
                  onSuccess="findCompositionsSuccess(data)"
                  onFailure="findCompositionsFailure(data)">

       <label for="archetypeId">archetypeId</label>
       <g:select name="archetypeId"
                 from="${ehr.clinical_documents.CompositionIndex.withCriteria{ projections { distinct('archetypeId') } } }"
                 size="5"
                 noSelection="['':message(code:'defaut.select.selectOne')]" />
       <br/><br/>

       <label for="category">category</label>
       <g:select name="category" from="${['event','persistent']}" noSelection="['':message(code:'defaut.select.selectOne')]"  />
       <br/><br/>

       <label for="uid">UID</label>
       <g:select name="uid"
                 from="${ehr.Ehr.list()}"
                 optionKey="uid"
                 size="5" />
       <br/><br/>

       <label for="fromDate">fromDate (yyyyMMdd)</label>
       <input type="text" name="fromDate" />
       <br/><br/>

       <label for="toDate">toDate (yyyyMMdd)</label>
       <input type="text" name="toDate" />
       <br/><br/>

       <g:submitButton name="findCompositions" value="Find Compositions" />
    </g:formRemote>

    <textarea id="findCompositionsSuccess" class="out"></textarea>
    <div id="findCompositionsFailure" class="error"></div>

  </body>
</html>
