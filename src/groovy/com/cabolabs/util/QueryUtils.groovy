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

package com.cabolabs.util

import com.cabolabs.ehrserver.query.*
import com.cabolabs.ehrserver.query.datatypes.*

class QueryUtils {

   // need to copy the expression items and all the attributes to avoid modification of domain classes from the expression2tree algorithm
   private static List cloneExpression(Query query)
   {
      def expression = []
      query.where.each {
         expression << [
            left_assoc: it.left_assoc,
            right_assoc: it.right_assoc,
            criteria: it.criteria // this is still the domain class, but since it is not modified, we don't need to clone internal attrs
         ]
      }
      return expression
   }

   /**
    * Generates a binary tree from the Query.where: List<DataCriteriaExpression>
    * The resulting tree is a Map of value, left, right and expression keys.
    * expression key will be the subexpression to create the subtree from a node
    * REF original algorithm: https://gist.github.com/ppazos/5aaa4bc61dfaa176e4985ef0a7817364
    */
   static Map getCriteriaTree(Query query)
   {
      def expression = cloneExpression(query) // since the map will be modified by the algorithm, we create a new map

      def tree = [expression: expression] // processing will set value and left/right with other Maps [value, left, right expression]
      getCriteriaTreeRecursive(tree) // modifies the tree
      return tree
   }

   /**
    * expression: List<DataCriteriaExpression>
    */
   private static Map getCriteriaTreeRecursive(Map tree)
   {
      processExpressionItem(tree)
      if (tree.left) getCriteriaTreeRecursive(tree.left)
      if (tree.right) getCriteriaTreeRecursive(tree.right)
   }
   private static Map processExpressionItem(Map tree)
   {
      def i, left_expression = [], right_expression = []
      i = tree.expression.findIndexOf { it.right_assoc } // gets the first item with right assoc

      if (i >= 0) // continue processing subtrees, is a right association
      {
         tree.value = tree.expression[i].right_assoc // AND/OR
         tree.expression[i].right_assoc = null // marked as processed

         tree.expression.eachWithIndex { item, idx ->
            if (idx <= i) left_expression << item
            else right_expression << item
         }
      }
      else // continue processing subtrees, is a left association
      {
         i = tree.expression.findLastIndexOf { it.left_assoc }

         if (i < 0) // ends recursion, is leaf node
         {
            tree.value = tree.expression[0].criteria
            return
         }

         tree.value = tree.expression[i].left_assoc
         tree.expression[i].left_assoc = null // marked as processed

         tree.expression.eachWithIndex { item, idx ->
            if (idx < i) left_expression << item
            else right_expression << item
         }
      }

      tree.left = [expression: left_expression]
      tree.right = [expression: right_expression]
   }

   /*
    * Process a Map returned from getCriteriaTree and retrieves the final boolean expression string
    */
   static String getStringExpression(Map tree)
   {
      def expr = ''

      if (tree.left)
      {
         def lexpr = getStringExpression(tree.left)
         def rexpr = getStringExpression(tree.right)

         expr = '('+ lexpr +' '+ tree.value +' '+ rexpr +')' // value is AND/OR
      }
      else
      {
         expr = tree.value.toSQL() // value is DataCriteria
      }

      return expr
   }

   /*
    * Process a Map returned from getCriteriaTree and retrieves the final SQL
    * expression string to be used on the query WHERE
    */
   static getFullCriteriaExpressionToSQL(Map tree)
   {
      def expr = ''

      if (tree.left)
      {
         def lexpr = getFullCriteriaExpressionToSQL(tree.left)
         def rexpr = getFullCriteriaExpressionToSQL(tree.right)

         expr = '('+ lexpr +' '+ tree.value +' '+ rexpr +')' // value is AND/OR
      }
      else
      {
         // each of these is an EXISTS condition
         expr = getFullCriteriaToSQL(tree.value) //.toSQL() // value is DataCriteria
      }

      return expr
   }

   /**
    * Generates EXISTS subquery for one criteria.
    *
    * Final full query would look like:
    *
      SELECT
          ci
      FROM
          CompositionIndex ci
      WHERE
          ci.lastVersion=true
          AND ci.organizationUid = 'e9d13294-bce7-44e7-9635-8e906da0c914'
          AND (
               EXISTS (
                   SELECT
                       dvi.id
                   FROM
                       DataValueIndex dvi ,
                       DvCountIndex dci
                   WHERE
                       dvi.owner.id = ci.id
                       AND dvi.archetypeId = 'openEHR-EHR-OBSERVATION.test_all_datatypes.v1'
                       AND dvi.archetypePath = '/data[at0001]/events[at0002]/data[at0003]/items[at0011]/value'
                       AND dci.id = dvi.id
                       AND dci.magnitude = 54
               )
               OR EXISTS (
                   SELECT
                       dvi.id
                   FROM
                       DataValueIndex dvi ,
                       DvCountIndex dci
                   WHERE
                       dvi.owner.id = ci.id
                       AND dvi.archetypeId = 'openEHR-EHR-OBSERVATION.test_all_datatypes.v1'
                       AND dvi.archetypePath = '/data[at0001]/events[at0002]/data[at0003]/items[at0011]/value'
                       AND dci.id = dvi.id
                       AND dci.magnitude = 98
               )
          )
    */
   static String getFullCriteriaToSQL(DataCriteria dataCriteria)
   {
      def idxtype, fromMap, _subq, _subq_criteria
      fromMap = ['DataValueIndex': 'dvi']
      idxtype = dataCriteria.rmTypeName

      def _where = new StringBuilder()
      _where.append("EXISTS (")

      _subq_criteria = new StringBuilder()
      _subq_criteria.append("WHERE dvi.owner.id = ci.id AND ")
      if (dataCriteria.allowAnyArchetypeVersion)
      {
         // TODO: check if archetypeId has % at the end
         _subq_criteria.append("dvi.archetypeId LIKE '").append(dataCriteria.archetypeId)
      }
      else
      {
         _subq_criteria.append("dvi.archetypeId = '").append(dataCriteria.archetypeId)
      }
      _subq_criteria.append("' AND ")
                    .append("dvi.archetypePath = '")
                    .append(dataCriteria.path)
                    .append("'")

      // this makes the code generic, use the alias and class name from the dataCriteria and doesnt need the switch

      /*
      println "dataCriteria class "+ dataCriteria.getClass().getSimpleName()
      println "dataCriteria index type "+ dataCriteria.indexType
      println "dataCriteria alias "+ dataCriteria.alias
      */

      fromMap[dataCriteria.indexType] = dataCriteria.alias
      _subq_criteria.append(" AND ${dataCriteria.alias}.id = dvi.id")

      /*
      switch (idxtype)
      {
         case 'DV_DATE_TIME':
            fromMap['DvDateTimeIndex'] = 'ddti'
            _subq_criteria.append(" AND ddti.id = dvi.id ")
         break
         case 'DV_DATE':
            fromMap['DvDateIndex'] = 'dcdte'
            _subq_criteria.append(" AND dcdte.id = dvi.id ")
         break
         case 'DV_QUANTITY':
            fromMap['DvQuantityIndex'] = 'dqi'
            _subq_criteria.append(" AND dqi.id = dvi.id ")
         break
         case 'DV_CODED_TEXT':
            fromMap['DvCodedTextIndex'] = 'dcti'
            _subq_criteria.append(" AND dcti.id = dvi.id ")
         break
         case 'DV_TEXT':
            fromMap['DvTextIndex'] = 'dti'
            _subq_criteria.append(" AND dti.id = dvi.id ")
         break
         case 'DV_ORDINAL':
            fromMap['DvOrdinalIndex'] = 'dvol'
            _subq_criteria.append(" AND dvol.id = dvi.id ")
         break
         case 'DV_BOOLEAN':
            fromMap['DvBooleanIndex'] = 'dbi'
            _subq_criteria.append(" AND dbi.id = dvi.id ")
         break
         case 'DV_COUNT':
            fromMap['DvCountIndex'] = 'dci'
            _subq_criteria.append(" AND dci.id = dvi.id ")
         break
         case 'DV_PROPORTION':
            fromMap['DvProportionIndex'] = 'dpi'
            _subq_criteria.append(" AND dpi.id = dvi.id ")
         break
         case 'DV_DURATION':
            fromMap['DvDurationIndex'] = 'dduri'
            _subq_criteria.append(" AND dduri.id = dvi.id ")
         break
         case 'DV_IDENTIFIER':
            fromMap['DvIdentifierIndex'] = 'dvidi'
            _subq_criteria.append(" AND dvidi.id = dvi.id ")
         break
         case 'DV_MULTIMEDIA':
            fromMap['DvMultimediaIndex'] = 'dvmmd'
            _subq_criteria.append(" AND dvmmd.id = dvi.id ")
         break
         case 'DV_PARSABLE':
            fromMap['DvParsableIndex'] = 'dpab'
            _subq_criteria.append(" AND dpab.id = dvi.id ")
         break
         case 'String':
            fromMap['StringIndex'] = 'dstg'
            _subq_criteria.append(" AND dstg.id = dvi.id ")
         break
         case 'LOCATABLE_REF':
            fromMap['LocatableRefIndex'] = 'dlor'
            _subq_criteria.append(" AND dlor.id = dvi.id ")
         break
         default:
            throw new Exception("type $idxtype not supported")
      }
      */

      // toSQL can fail, for instance if the criteria has a SNOMED expression and the SNOMED service
      // return a 429 To Many Requests, that exception should reach the top level to show the error
      // to the user on GUI or API, and the whole query process should stop.
      _subq_criteria.append(" AND ")
                    .append(dataCriteria.toSQL()) // important part: complex criteria to SQL, depends on the datatype

      /*
      FROM ArchetypeIndexItem dvi, ...
      WHERE dvi.owner.id = ci.id AND
            dvi.archetypeId = openEHR-EHR-COMPOSITION.encounter.v1 AND
            dvi.path = /content/data[at0001]/origin AND
            dvi.value>20080101
      */
      _subq = new StringBuilder()
      _subq.append("SELECT dvi.id FROM ")

      fromMap.each { index, alias ->

         _subq.append(index).append(' ').append(alias).append(' , ')
      }

      _subq.setLength(_subq.length() - 2)
      _subq.append(_subq_criteria)

      _where.append(_subq)
      _where.append(')') // closes EXISTS

      return _where.toString()
   }
}
