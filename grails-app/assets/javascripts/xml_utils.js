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
// FIXME: this started to throw "too much recursion" on latest FF
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

// same function but optimized for ES6
function formatXml2(xml)
{
  // Remove all the newlines and then remove all the spaces between tags
  xml = xml.replace(/(\r\n|\n|\r)/gm, " ").replace(/>\s+</g,'><');

  var PADDING = ' '.repeat(4); // set desired indent size here
  var reg = /(>)(<)(\/*)/g;
  var pad = 0;

  xml = xml.replace(reg, '$1\r\n$2$3');

  return xml.split('\r\n').map(function (node, index) {
    var indent = 0;
    if (node.match(/.+<\/\w[^>]*>$/)) {
      indent = 0;
    } else if (node.match(/^<\/\w/) && pad > 0) {
      pad -= 1;
    } else if (node.match(/^<\w[^>]*[^\/]>.*$/)) {
      indent = 1;
    } else {
      indent = 0;
    }

    pad += indent;

    return PADDING.repeat(pad - indent) + node;
  }).join('\r\n');
}