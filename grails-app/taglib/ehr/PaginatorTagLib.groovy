package ehr

class PaginatorTagLib {

    /**
     * Creates a new password field.
     *
     * @attr currentPage         OPTIONAL the field value mark the current page
     * @attr numberOfPages       REQUIRED the field value numeric pages (with type page as stated in the 
     *                                    Structure Exaplained section) should be rendered.
     * @attr totalPages          REQUIRED the field value define the upper limit of the page range
     * @attr urlPage             REQUIRED the field value url to service for get data
     * @attr numberItemsDisplay  OPTIONAL the field value number of elements to show by table
     */

    def paginator = { attrs, body ->
      def currentPage=1;
      //Calculo la pagina actual del paginador,ya que al recargar la página se pierden la posición actual en paginador.
      if (attrs.currentPage !=null){
         if ((attrs.currentPage.toInteger() > 0)){
               currentPage=((attrs.currentPage.toInteger().intdiv(attrs.numberItemsDisplay.toInteger())) +1)
            }
      }
      //Si no se pone nada en este parametro. Por defecto le ponemos 10.
      def numberItemsDisplay=attrs.numberItemsDisplay? attrs.numberItemsDisplay:10 
     //Calculamos el maximo número de elementos que tendra el paginador.
      def totalPages =(attrs.total.toInteger()).intdiv(numberItemsDisplay.toInteger())+ ((attrs.total.toInteger()%numberItemsDisplay.toInteger()) ==0?0:1)
      def urlPage=g.createLink(action: actionName);
      //Código que se genera para poder utilizar boostrap-paginator
         //Codigo necesario para poder aplicar bootstrap
          out << "<div>"
          out << "<ul id='bp-3-element-test'></ul>"
          out << "</div>"
          
          //Librerias a cargar     
          out << asset.javascript(src:'bootstrap-paginator.min.js')
          //Codigo del script
          out << "<script type='text/javascript'>"
          out << "var element = \$(\'#bp-3-element-test\');"
          out << "var options = {"
          out << "bootstrapMajorVersion:3," 
          out << "currentPage: " +currentPage                   
          out << ",numberOfPages: " + attrs.numberOfPages         
          out << ",totalPages: " + totalPages   
          //opción del paginador que asignar url a cada elemento del paginador, en función de la página pulsada.       
          out << ",pageUrl: function(type, page, current){"   
          out << "return \"${urlPage}?offset=\"+(page-1)*${attrs.numberItemsDisplay}+\"&max=${attrs.numberItemsDisplay}\";"               
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