/*
 * Copyright 2011-2020 CaboLabs Health Informatics
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
import grails.converters.JSON

class QueryTagLib {

   //static defaultEncodeAs = [all: 'raw']

   /**
    * Displays the expression tree in an ul li table tree structure
    * Used in the show view
    */
   def query_criteria = { attrs, body ->

      if (!attrs.query) throw new Exception("Attribute 'query' is required for query_criteria taglib")

      def tree = QueryUtils.getCriteriaTree(attrs.query)
      def html = new groovy.xml.MarkupBuilder(out)
      html.div(id:'criteria_builder') {
         ul {
            query_criteria_recursive(tree, html) // renders to out
         }
      }
   }

   /**
    * util for show view
    */
   private void query_criteria_recursive(tree, html)
   {
      html.li {
         if (['AND', 'OR'].contains( tree.value ))
         {
            span(tree.value)
            ul {
               query_criteria_recursive(tree.left, html)
               query_criteria_recursive(tree.right, html)
            }
         }
         else // simple criteria
         {
            //println tree.value.archetypeId +" "+ tree.value.path
            def item = ArchetypeIndexItem.findByArchetypeIdAndPath(tree.value.archetypeId, tree.value.path)

            if (!item)
            {
               log.error "there is no ArchetypeIndexItem for "+ tree.value.archetypeId + tree.value.path
               return
            }

            div(class:'table-responsive') {
               table(class:'table table-striped table-bordered table-hover') {
                  tr {
                     th(message(code:'query.show.archetype_id.attr'))
                     th(message(code:'query.show.path.attr'))
                     th(message(code:'query.show.name.attr'))
                     th(message(code:'query.create.type'))
                     th(message(code:'query.show.criteria.attr'))
                  }
                  tr {
                     td(tree.value.archetypeId)
                     td(tree.value.path)
                     td(ArchetypeIndexItem.findByArchetypeIdAndPath(tree.value.archetypeId, tree.value.path).name[session.lang])
                     td(tree.value.rmTypeName)
                     td(tree.value.toGUI())
                  }
               }
            }
         }
      }
   }

   def query_criteria_edit = { attrs, body ->
      if (!attrs.query) throw new Exception("Attribute 'query' is required for query_criteria_edit taglib")

      def tree = QueryUtils.getCriteriaTree(attrs.query)

      Map params = [code:"", criteria_id_gen: 0] // values wrapped in Map to allow out params

      query_criteria_edit_recursive(tree, params)

      // this line updates the GUI by JS
      params.code += 'criteria_builder.render("#criteria_builder");'

      out << raw(params.code)
   }

   // renders JS to update the model on the client side but doesn't do the GUI render
   // the GUI is updated by the JS criteria_builder.render()
   private void query_criteria_edit_recursive(tree, params)
   {
      if (['AND', 'OR'].contains( tree.value )) // complex, has children
      {
         query_criteria_edit_recursive(tree.left, params)
         def id_left = params.criteria_id_gen

         query_criteria_edit_recursive(tree.right, params)
         def id_right = params.criteria_id_gen

         //params.code += 'console.log("Adding complex criteria '+ (params.criteria_id_gen+1) +'");'
         params.code += 'criteria_builder.add_complex_criteria('+ id_left +', '+ id_right +', "'+ tree.value +'");'
      }
      else // create the single criteria
      {
         def data_criteria = tree.value

         def attrs, attrValueField, attrOperandField, attrNegationField, value, operand, name, values, negation

         attrs = data_criteria.criteriaSpec(data_criteria.archetypeId, data_criteria.path)[data_criteria.spec].keySet() // attribute names of the datacriteria

         // module to avoid variable name conflicts on the generated JS
         params.code += '(function(){'
         //params.code += 'console.log("Adding criteria '+ (params.criteria_id_gen+1) +'");'
         params.code += 'var criteria = new Criteria('+ data_criteria.spec +');'

         // simple criteria have conditions for each attribute of the correspondent datatype
         attrs.each { attr ->

            attrValueField = attr + 'Value'
            attrOperandField = attr + 'Operand'
            attrNegationField = attr + 'Negation'
            operand = data_criteria."$attrOperandField"
            value = data_criteria."$attrValueField"

            // DV_BOOLEAN doesn't have negation, just checking
            if (data_criteria.hasProperty(attrNegationField))
               negation = data_criteria."$attrNegationField"

            // TODO
            // date?.format(Holders.config.app.l10n.db_datetime_format)
            // ext_datetime_utcformat_nof = "yyyy-MM-dd'T'HH:mm:ss'Z'"

            if (value instanceof List)
            {
               if (value[0] instanceof Date)
               {
                  values = ( value.collect{ it.format(grailsApplication.config.app.l10n.ext_datetime_utcformat_nof) } as JSON )
                  params.code += 'criteria.add_condition("'+ attr +'", "'+ operand +'", '+ values +', '+ negation +');'
               }
               else
               {
                  // toString to have the items with quotes on JSON, without the quotes I get an error when saving/binding the uptates to criterias.
                  values = ( value.collect{ it.toString() } as JSON )
                  params.code += 'criteria.add_condition("'+ attr +'", "'+ operand +'", '+ values +', '+ negation +');'
               }
            }
            else // value is an array of 1 element
            {
               // FIXME: if the value is not string or date, dont include the quotes
               params.code += 'criteria.add_condition("'+ attr +'", "'+ operand +'", [ "'+ value +'"], '+ negation +');'
            }
         } // each attr

         // the string 'criteria' references the variable defined above
         params.code += 'criteria_builder.add_criteria("'+
                           data_criteria.archetypeId +'", "'+
                           data_criteria.path +'", "'+
                           data_criteria.rmTypeName +'", '+
                           'criteria,'+
                           data_criteria.allowAnyArchetypeVersion +',"'+
                           ArchetypeIndexItem.findByArchetypeIdAndPath(data_criteria.archetypeId, data_criteria.path).name[session.lang] +
                        '");'

         params.code += '}());' // close JS module
      }

      params.criteria_id_gen++
   }
}
