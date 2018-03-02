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

package com.cabolabs.ehrserver.query

import grails.util.Holders
import com.cabolabs.ehrserver.data.DataValues
import com.cabolabs.ehrserver.ehr.clinical_documents.ArchetypeIndexItem
import com.cabolabs.ehrserver.exceptions.QuerySnomedServiceException

/**
 * WHERE archId/path operand value
 *
 * Para consultas de compositions (queryByData)
 *
 * @author Pablo Pazos Gutierrez <pablo.pazos@cabolabs.com>
 */
class DataCriteria {

   def querySnomedService


   String archetypeId
   String path
   boolean allowAnyArchetypeVersion = false


   // value va a depender del tipo del RM en la path
   // value es parametro de la query sino se setea aqui
   // se setea aqui cuando se hace la consulta como una regla (ej. para ver valores fuera de rango)
   //Map values // valores del criterio que se interpretan segun el tipo de dato
   /**
    * DV_PROPORTION:
    *   values = [value1, value2] se puede usar para operador between sobre magnitude
    *
    * DV_CODED_TEXT:
    *   values = [code::terminology, code::terminology, ...] se puede usar para saber si un dato esta dentro de una lista.
    */

   String rmTypeName

   // TODO: poner name para mostrar en la definicion
   //       de la consulta, se saca de ArchetypeIndexItem o del
   //       arquetipo archetypeId para la path (que
   //       tiene el nodeId)

   int spec // index of the criteria spec selected

   String alias // for the query, private

   static transients = ['indexItem']

   static constraints = {
      rmTypeName(inList: DataValues.valuesStringList() )
   }

   static Map operandMap = [
     'eq': '=',
     'lt': '<',
     'gt': '>',
     'neq': '<>', // http://stackoverflow.com/questions/723195/should-i-use-or-for-not-equal-in-tsql
     'le': '<=',
     'ge': '>=',
     'in_list': 'IN',
     'contains': 'LIKE', // TODO: for MySQL is LIKE, for postgres is ILIKE (for MySQL we might need to use ucase(fieldName) like 'ucase(value)%')
     'between': 'BETWEEN'
   ]

   def sqlOperand(String operandString)
   {
      return operandMap[operandString]
   }

   static belongsTo = [Query]


   String toString()
   {
      return "(archetypeId: "+ this.archetypeId +", path: "+ this.path +", rmTypeName: "+ this.rmTypeName +", class: "+ this.getClass().getSimpleName() +")"
   }

   /*
    * Used to show the query as JSON and XML on the UI.
    */
   Map getCriteriaMap()
   {
      def criteria = [:] // attr -> [ operand : values ]

      def specs = criteriaSpec(this.archetypeId, this.path, false)
      def spec = specs[this.spec] // spec used Map
      def attributes = spec.keySet()
      def operand
      def operandField
      def valueField
      def criteriaValueType // value, list, range ...
      def value

      attributes.each { attr ->
         operandField = attr+'Operand'
         operand = this."$operandField"
         valueField = attr+'Value'
         value = this."$valueField" // can be a list

         criteriaValueType = spec[attr][operand]

         // Date values as Strings formatted in UTC
         // value can be list, is teh type of the attribute in the criteria class
         if (value instanceof List)
         {
            if (value[0] instanceof Date)
            {
               value = value.collect{ it.format(Holders.config.app.l10n.ext_datetime_utcformat_nof, TimeZone.getTimeZone("UTC")) }
            }
         }
         else
         {
            if (value instanceof Date)
            {
               value = value.format(Holders.config.app.l10n.ext_datetime_utcformat_nof, TimeZone.getTimeZone("UTC"))
            }
         }


         // TODO: if value is string, add quotes, if boolean change it to the DB boolean value
         // That can be done in a pre filter, and we can put the dates to utc string also there
         if (criteriaValueType == 'value')
         {
            criteria[attr] =  [(operand): value]
         }
         else if (criteriaValueType == 'list')
         {
            //assert operand == 'in_list'
            assert ['in_list', 'contains_like'].contains(operand)
            criteria[attr] =  [(operand): value]
         }
         else if (criteriaValueType == 'range')
         {
            assert operand == 'between'
            criteria[attr] =  [(operand): value]
         }
         else if (criteriaValueType == 'snomed_exp')
         {
            assert operand == 'in_snomed_exp'
            criteria[attr] =  [(operand): value]
         }
      }

      return criteria
   }

   String toSQL()
   {
      // booleano false no devuelve listas de valores porque aca no las uso ej para coded text
      def specs = criteriaSpec(this.archetypeId, this.path, false)

      // TODO: we need to think another way of referencing the spec that is not by the index,
      // this difficults executing not stored queries, since the spec number should be set.
      def criteria_spec = specs[this.spec] // spec used Map
      def attributes_or_functions = criteria_spec.keySet()
      def sql = ""
      def operand
      def operandField
      def valueField
      def negationField
      def criteriaValueType // value, list, range ...
      def value, attr, negation

      attributes_or_functions.each { attr_or_function ->

         if (this.functions().contains(attr_or_function))
         {
            // is function
            //println "function " + attr_or_function
            //println this.evaluateFunction(attr_or_function)

            sql += this.evaluateFunction(attr_or_function) + '     ' // extra spaces are to avoid cutting the criteria value
         }
         else
         {
            attr = attr_or_function
            operandField = attr+'Operand'
            operand = this."$operandField"
            valueField = attr+'Value'
            value = this."$valueField" // can be a list
            negationField = attr+'Negation'

            // not all DataCriteria have negation (boolean, identifier don't have negation fields)
            if (this.hasProperty(negationField))
               negation = this."$negationField"

            criteriaValueType = criteria_spec[attr][operand]

            // TODO: if value is string, add quotes, if boolean change it to the DB boolean value
            if (criteriaValueType == 'value')
            {
               if (value instanceof List) // it can be a list but have just one value e.g. because it can also have a range
                  sql += (negation ? 'NOT ' : '') + this.alias +'.'+ attr +' '+ sqlOperand(operand) +' '+ value[0].asSQLValue(operand)
               else
                  sql += (negation ? 'NOT ' : '') + this.alias +'.'+ attr +' '+ sqlOperand(operand) +' '+ value.asSQLValue(operand)
            }
            else if (criteriaValueType == 'list')
            {
               assert ['in_list', 'contains_like'].contains(operand)

               if (operand == 'contains_like')
               {
                  sql += (negation ? 'NOT ' : '')
                  sql += '('

                  value.each { singleValue ->
                     sql += this.alias +'.'+ attr +' LIKE '+ singleValue.asSQLValue(operand) +" OR "
                  }
                  sql = sql[0..-5] + ')' // removes last OR
               }
               else
               {
                  sql += this.alias +'.'+ attr + (negation ? ' NOT' : '') +' IN ('

                  value.each { singleValue ->
                     sql += singleValue.asSQLValue(operand) +','
                  }
                  sql = sql[0..-2] + ')' // removes last ,
               }
            }
            else if (criteriaValueType == 'range')
            {
               assert operand == 'between'

               sql += this.alias +'.'+ attr + (negation ? ' NOT' : '') +' BETWEEN '+ value[0].asSQLValue(operand) +' AND '+ value[1].asSQLValue(operand)
            }
            else if (criteriaValueType == 'snomed_exp')
            {
               assert 'in_snomed_exp' == operand

               // for coded text this is List codeValue, value[0] is the expression
               //println value

               // TODO: the result of the expression should be already cached locally
               // if is on the database, we can do a JOIN or EXISTS subq instead of IN.
               def conceptids = []

//               try
//               {
                  // will throw exceptions on any fail case, this should make the whole .toSQL to fail, since the result wouldn't be valid
                  // the exception should reach the top level to return the error to the user on GUI or API, so no catch here!
                  conceptids = querySnomedService.getCodesFromExpression(value[0])
//               }
//               catch (e)
//               {
//                  println e.message
//                  println e
                  // FIXME: if there is an error resolving the snomed expression,
                  //        the whole getSQL should fail and the error should reach
                  //        the upper level to show a friendly message to the user,
                  //        like server not available or max requests reached.
                  //        On a server down situation 'e' will be java.net.UnknownHostException
//               }

               //println conceptids
               // be prepared for communication errors to avoid generating invalid HQL
               if (conceptids.size() > 0)
               {
                  sql += this.alias +'.'+ attr + (negation ? ' NOT' : '') +' IN ('

                  conceptids.each { singleValue ->
                     sql += singleValue.asSQLValue(operand) +','
                  }
                  sql = sql[0..-2] + ')' // removes last ,
               }
               else
               {
                  // conceptually if the list of concepts is empty, the SQL IN operator should return always false since x IN [] is false.
                  // so we throw an exception when no concepts are returned from the service.
                  throw new QuerySnomedServiceException('querySnomedService.error.emptyResults')
                  //sql += ' 1=1 ' // just a placeholder to have a valid query, TODO: this should be an empty list
               }
            }

            sql += ' AND '
         }
      }

      sql = sql[0..-6] // removes the last AND

      return sql
   }


   /**
    * Returns a string form of the criteria for UI display. Has the same logic as toSQL but this is not for evaluating SQL.
    */
   String toGUI()
   {
      // booleano false no devuelve listas de valores porque aca no las uso ej para coded text
      def specs = criteriaSpec(this.archetypeId, this.path, false)

      // TODO: we need to think another way of referencing the spec that is not by the index,
      // this difficults executing not stored queries, since the spec number should be set.
      def criteria_spec = specs[this.spec] // spec used Map
      def attributes_or_functions = criteria_spec.keySet()
      def ui = ""
      def operand
      def operandField
      def valueField
      def negationField
      def criteriaValueType // value, list, range ...
      def value, attr, negation

      attributes_or_functions.each { attr_or_function ->

         attr = attr_or_function
         operandField = attr+'Operand'
         operand = this."$operandField"
         valueField = attr+'Value'
         value = this."$valueField" // can be a list
         negationField = attr+'Negation'

         if (this.functions().contains(attr_or_function))
         {
            ui += attr_or_function +' '+ operand +' '+ value + '     ' // extra spaces are to avoid cutting the criteria value
         }
         else
         {

            // not all DataCriteria have negation (boolean, identifier don't have negation fields)
            if (this.hasProperty(negationField))
               negation = this."$negationField"

            criteriaValueType = criteria_spec[attr][operand]

            // TODO: if value is string, add quotes, if boolean change it to the DB boolean value
            if (criteriaValueType == 'value')
            {
               if (value instanceof List) // it can be a list but have just one value e.g. because it can also have a range
                  ui += (negation ? 'NOT ' : '') + attr +' '+ operand +' '+ value[0]
               else
                  ui += (negation ? 'NOT ' : '') + attr +' '+ operand +' '+ value
            }
            else if (criteriaValueType == 'list')
            {
               assert ['in_list', 'contains_like'].contains(operand)

               if (operand == 'contains_like')
               {
                  ui += (negation ? 'NOT ' : '')
                  ui += '('

                  value.each { singleValue ->
                     ui += attr +' '+ operand +' '+ singleValue +" OR "
                  }
                  ui = ui[0..-5] + ')' // removes last OR
               }
               else
               {
                  ui += attr + (negation ? ' NOT' : '') +' IN ('

                  value.each { singleValue ->
                     ui += singleValue +','
                  }
                  ui = ui[0..-2] + ')' // removes last ,
               }
            }
            else if (criteriaValueType == 'range')
            {
               assert operand == 'between'

               ui += attr + (negation ? ' NOT' : '') +' '+ operand +' '+ value[0] +' AND '+ value[1]
            }
            else if (criteriaValueType == 'snomed_exp')
            {
               assert 'in_snomed_exp' == operand

               ui += attr + (negation ? ' NOT' : '') +' '+ operand +' '+ value[0]
            }

            ui += ' AND '
         }
      }

      ui = ui[0..-6] // removes the last AND

      return ui
   }


   ArchetypeIndexItem getIndexItem()
   {
      if (this.allowAnyArchetypeVersion)
         return ArchetypeIndexItem.findByArchetypeIdLikeAndPathAndRmTypeName(this.archetypeId+'%', this.path, this.rmTypeName)
      else
         return ArchetypeIndexItem.findByArchetypeIdAndPathAndRmTypeName(this.archetypeId, this.path, this.rmTypeName)
   }
}
