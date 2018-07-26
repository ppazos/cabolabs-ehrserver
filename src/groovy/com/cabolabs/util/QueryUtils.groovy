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
   private List cloneExpression(Query query)
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
   Map getCriteriaTree(Query query)
   {
      def expression = cloneExpression(query) // since the map will be modified by the algorithm, we create a new map

      def tree = [expression: expression] // processing will set value and left/right with other Maps [value, left, right expression]
      getCriteriaTreeRecursive(tree) // modifies the tree
      return tree
   }

   /**
    * expression: List<DataCriteriaExpression>
    */
   private Map getCriteriaTreeRecursive(Map tree)
   {
      processExpressionItem(tree)
      if (tree.left) getCriteriaTreeRecursive(tree.left)
      if (tree.right) getCriteriaTreeRecursive(tree.right)
   }
   private Map processExpressionItem(Map tree)
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
   String getStringExpression(Map tree)
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
}
