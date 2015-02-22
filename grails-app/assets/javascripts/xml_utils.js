// Convierte un documento XML a un string XML
// http://stackoverflow.com/questions/6507293/convert-xml-to-string-with-jquery
function xmlToString(xmlData) { 

  var xmlString;
  //IE
  if (window.ActiveXObject) {
      xmlString = xmlData.xml;
  }
  // code for Mozilla, Firefox, Opera, etc.
  else {
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