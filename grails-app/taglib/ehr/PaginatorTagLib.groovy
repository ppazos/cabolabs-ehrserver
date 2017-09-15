
/*
 * Copyright 2011-2017 CaboLabs Health Informatics
 *
 * The EHRServer was designed and developed by Pablo Pazos Gutierrez <pablo.pazos@cabolabs.com> at CaboLabs Health Informatics (www.cabolabs.com).
 *
 * You can't remove this notice from the source code, you can't remove the "Powered by CaboLabs" from the UI, you can't remove this notice from the window that appears then the "Powered by CaboLabs" link is clicked.
 *
 * Any modifications to the provided source code can be stated below this notice.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ehr

import grails.util.Holders

class PaginatorTagLib {

   def configurationService
   
   def config = Holders.config.app
   
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
    
      def numberOfPages = (attrs.numberOfPages ?: 5)
      
      def max = configurationService.getValue('ehrserver.console.lists.max_items')
      def offset = (params.offset?.toInteger() ?: 1)
      def currentPage = offset.intdiv(max) + 1
      
      //Calculamos el maximo número de elementos que tendra el paginador.
      def totalPages = 1 // if there are 0 elements, totalPages = 1, this avoids set 0 by the logic below
      def total = attrs.total.toInteger()
      if (total > 0) totalPages = total.intdiv(max) + ((total % max == 0) ? 0 : 1) // 1 for total=1..max, 2 for total=max+1..2max, ...
      
      def urlPage = g.createLink(action: actionName)
      
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
      
      
      // Adds only filter and sort params to the paginator, removing other non wanted params
      attrs.args.keySet().removeAll(['controller','action','max','offset']) // this returns boolean
      def args = attrs.args

      def sort_and_filter = ''
      args.each { name, value ->
         sort_and_filter += name +'='+ value +'&'
      }
      
      //opción del paginador que asignar url a cada elemento del paginador, en función de la página pulsada.       
      out << $/,pageUrl: function(type, page, current){
                  return "${urlPage}?${sort_and_filter}offset="+ (page-1)*${max} +"&max=${max}";          
                },/$
      
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