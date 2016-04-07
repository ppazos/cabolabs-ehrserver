package ehr

class PaginatorTagLib {

    /**
     * Creates a new password field.
     *
     * @attr currentPage         OPTIONAL the field value mark the current page
     * @attr numberOfPages       OPTIONAL the field value numeric pages (with type page as stated in the 
     *                                    Structure Exaplained section) should be rendered.
     * @attr totalPages          REQUIRED the field value define the upper limit of the page range
     * @attr urlPage             REQUIRED the field value url to service for get data
     */
    def paginator = { attrs, body ->
    
      def numberOfPages = (attrs.numberOfPages ?: 10)
      
      //println "paginator total/offset: " + (params.offset ?: 1).intdiv( params.max )
      def max = Math.min(params.max?.toInteger() ?: 10, 100)
      def offset = (params.offset?.toInteger() ?: 1)
      def currentPage = offset.intdiv(max) + 1
      //println "paginator currPage: " + currentPage
      
      //Calculamos el maximo número de elementos que tendra el paginador.
      def totalPages = (attrs.total.toInteger()).intdiv(max) + ((attrs.total.toInteger() % max) == 0 ? 0 : 1)
      def urlPage=g.createLink(action: actionName);
      
      //Código que se genera para poder utilizar boostrap-paginator
      //Codigo necesario para poder aplicar bootstrap
      out << '<div align="center">'
      out << '<ul id="bp-3-element-test"></ul>'
      out << '</div>'
       
      //Librerias a cargar     
      out << asset.javascript(src:'bootstrap-paginator.min.js')
      
      //Codigo del script
      out << "<script type='text/javascript'>"
      out << "var element = \$(\'#bp-3-element-test\');"
      out << "var options = {"
      out << "bootstrapMajorVersion:3," 
      out << "currentPage: " +currentPage                   
      out << ",numberOfPages: " + numberOfPages         
      out << ",totalPages: " + totalPages   
      
      //opción del paginador que asignar url a cada elemento del paginador, en función de la página pulsada.       
      out << ",pageUrl: function(type, page, current){"   
      out << "return \"${urlPage}?offset=\"+(page-1)*${max}+\"&max=${max}\";"               
      out << "},"
      
      //opción del paginador para mostrar o ocultar partes del paginador.
      out << "shouldShowPage:function(type, page, current){"
      out << "switch(type){"
      out <<"case \"first\":"
      out <<"case \"last\":return false;"
      out <<"default:return true;}"
      out << "}};"       
      out << "element.bootstrapPaginator(options);"       
      out << "</script>"
   }
}