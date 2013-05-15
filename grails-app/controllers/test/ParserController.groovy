package test

//import com.thoughtworks.xstream.XStream

class ParserController {

   def xmlService
   
   def index() {
      
      def file = new File("test\\resource\\contribution.xml")
      def xml = file.getText()
      
      //def contribution = xmlService.parseConstribution()
      def contribution = xmlService.parseContribution(xml)
      
	  /*
      XStream xstream = new XStream()
      String txt = xstream.toXML(contribution)
      
      //println txt
      render (text:txt, contentType:"text/xml", encoding:"UTF-8")
	  */
	  render (text:'', contentType:"text/xml", encoding:"UTF-8")
   }
}