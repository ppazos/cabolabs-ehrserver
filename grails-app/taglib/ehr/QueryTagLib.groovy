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

import com.cabolabs.util.QueryUtils
import com.cabolabs.ehrserver.ehr.clinical_documents.ArchetypeIndexItem

class QueryTagLib {

   /**
    * Displays the expression tree in an ul li table tree structure
    */
   def query_criteria = { attrs, body ->

      if (!attrs.query) throw new Exception("Attribute 'query' is required for query_criteria taglib")

      def qu = new QueryUtils()
      def tree = qu.getCriteriaTree(attrs.query)

      def html = new groovy.xml.MarkupBuilder(out)
      query_criteria_recursive(tree, html) // renders to out
   }

   private void query_criteria_recursive(tree, html)
   {
      html.div(class:'expression_container row-eq-height') {
         if (['AND', 'OR'].contains( tree.value ))
         {
            div(class:'expression_column_complex col-md-1', tree.value)
            div(class:'expression_column_sub col-md-11') {
               //div(class:'expression_row_left row') {
                  query_criteria_recursive(tree.left, html)
               //}
               //div(class:'expression_row_right row')
               //{
                  query_criteria_recursive(tree.right, html)
               //}
            }
         }
         else // sinple criteria
         {
            div(class:'expression_criteria col-md-12') {
               div(class:'table-responsive') {
                  table(class:'table table-striped table-bordered table-hover') {
                     tr {
                        th(message(code:'query.show.archetype_id.attr'))
                        th(message(code:'query.show.path.attr'))
                        th(message(code:'query.show.name.attr'))
                        th(message(code:'query.show.criteria.attr'))
                     }
                     tr {
                        td(tree.value.archetypeId)
                        td(tree.value.path)
                        td(ArchetypeIndexItem.findByArchetypeIdAndPath(tree.value.archetypeId, tree.value.path).name[session.lang])
                        td(tree.value.toGUI())
                     }
                  }
               }
            }
         }
      }
   }
}
