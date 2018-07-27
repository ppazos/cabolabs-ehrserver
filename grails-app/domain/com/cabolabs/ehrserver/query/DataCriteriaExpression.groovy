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

import com.cabolabs.ehrserver.query.datatypes.*

class DataCriteriaExpression {

   String left_assoc
   String right_assoc
   DataCriteria criteria

   // transients that reference to tree nodes before setting the final left and right assocs
   def left_assoc_tmp
   def right_assoc_tmp
   static transients = ['left_assoc_tmp', 'right_assoc_tmp']

   static constraints = {
      left_assoc(nullable: true)
      right_assoc(nullable: true)
   }
   static mapping = {
      criteria cascade: "all-delete-orphan"
   }

   // TODO: move logic to com.cabolabs.util.QueryUtils

   // sets the parents of each node, that is needed for the algorithm to get the expression from the tree
   private static def prepareCriteriaTree(org.codehaus.groovy.grails.web.json.JSONObject criteria,
                                          org.codehaus.groovy.grails.web.json.JSONObject parent = null)
   {
      if (parent) // criteria is not root
      {
         criteria.parent = parent // this converts the JSON tree in a graph
      }

      if (criteria._type != 'COND') // continue recursion if node is complex
      {
         prepareCriteriaTree(criteria.left, criteria)
         prepareCriteriaTree(criteria.right, criteria)
      }
   }


   // Process a complex criteria binary tree from the GUI and generates
   // a list of DataCriteriaExpression, that is the associative expression
   // to be stored in the database as the binary tree for the complex
   // boolean expression in Query.where
   static List treeToExpression(org.codehaus.groovy.grails.web.json.JSONObject criteria)
   {
      //println criteria.toString()
      prepareCriteriaTree(criteria) // sets all node.parent
      //println criteria.toString() // this gives a stack overflow because of the parent links, is a graph not a tree

      def queue = [] as Queue
      def expression = getInitialExpression(criteria)

      def expression_item, current = 0, node
      while (current < expression.size()-1) // process all but the last leaf, because that is right sibling
      {
         node = expression[current].criteria

         if (node.parent.left == node.tree_node) // node is left, sibling is right, in the parent
         {
            println "node is left "+ node.tree_node._type
            if (node.parent.right._type == 'COND') // sibling is simple
            {
               println "sibling is simple"
               // expression_item of the sibling of the current node
               expression_item = expression.find { it.criteria.tree_node == node.parent.right }

               println "sibling expression "+ expression_item.criteria.tree_node._type

               println "sibling left assoc set to parent "+ node.parent._type

               // left assoc = parent (this is BinaryTree needs to be transformed to it's value when finished)
               expression_item.left_assoc_tmp = node.parent

               println "after sibling left assoc is set"
            }
            else
            {
               println "sibling is complex"
               // row of the current node
               expression_item = expression[current]

               println "sibling right assoc set to parent "+ node.parent._type

               // right assoc = parent
               expression_item.right_assoc_tmp = node.parent
            }

            println "add parent to queue "+ node.parent._type

            // add parent to queue
            queue.offer(node.parent) // node.parent is JSONObject
         }
         else
         {
            println "node is right "+ node.tree_node._type
         }

         // current = next
         current ++
      }

      while (!queue.isEmpty())
      {
         node = queue.poll()

         // avoid procesing root or right siblings
         if (!node.parent || node.parent.right == node) continue // node is root or right, avoid

         if (node.parent.right._type != 'COND') // sibling is complex
         {
            // if sibling is complex, the current node should be on left assoc of some row
            expression_item = expression.find { it.left_assoc_tmp == node } // left assoc

            expression_item.right_assoc_tmp = node.parent
         }
         else
         {
            // if sibling is simple, it should be on the value space in the item, item.criteria
            expression_item = expression.find { it.criteria.tree_node == node.parent.right }

            expression_item.left_assoc_tmp = node.parent
         }

         // add parent to queue
         queue.offer(node.parent)
      }

      // expression with the left/right associations resolved to the value AND/OR
      expression.each { exp_item ->

         //println 'assocs '+ exp_item.left_assoc_tmp?._type +' '+ exp_item.right_assoc_tmp?._type
         if (exp_item.left_assoc_tmp) exp_item.left_assoc = exp_item.left_assoc_tmp._type
         if (exp_item.right_assoc_tmp) exp_item.right_assoc = exp_item.right_assoc_tmp._type
      }

      return expression
   }


   // returns the expression, just with the simple criterias on it
   private static List getInitialExpression(org.codehaus.groovy.grails.web.json.JSONObject criteria)
   {
      List leaves = []
      if (criteria._type != 'COND') // is complex
      {
         leaves += getInitialExpression(criteria.left)
         leaves += getInitialExpression(criteria.right)
      }
      else // simple criteria builder
      {
         // removes the version for the archetype id and saves it
         if (criteria.allowAnyArchetypeVersion)
         {
            criteria.archetypeId = criteria.archetypeId.replaceAll(/\.v(\d)*/, '')
         }

         //println "Criteria "+ criteria
         def condition
         switch (criteria['class']) {
            case 'DataCriteriaDV_QUANTITY':
               def magnitudeValue = []
               if (criteria.magnitudeValue instanceof String)
               {
                  magnitudeValue << new Double(criteria.magnitudeValue)
               }
               else
               {
                  criteria.magnitudeValue.each {
                     magnitudeValue << new Double(it)
                  }
               }

               criteria.magnitudeValue = magnitudeValue
               condition = new DataCriteriaDV_QUANTITY(criteria)
            break
            case 'DataCriteriaDV_CODED_TEXT':
               condition = new DataCriteriaDV_CODED_TEXT(criteria)
            break
            case 'DataCriteriaDV_TEXT':
               condition = new DataCriteriaDV_TEXT(criteria)
            break
            case 'DataCriteriaDV_DATE_TIME':

               def dateValues = dateValues(criteria.valueValue)

               // Set the values converted to Date
               criteria.valueValue = dateValues
               condition = new DataCriteriaDV_DATE_TIME(criteria)
            break
            case 'DataCriteriaDV_DATE':

               def dateValues = dateValues(criteria.valueValue)

               // Set the values converted to Date
               criteria.valueValue = dateValues
               condition = new DataCriteriaDV_DATE(criteria)
            break
            case 'DataCriteriaDV_BOOLEAN':
               condition = new DataCriteriaDV_BOOLEAN(criteria)
            break
            case 'DataCriteriaDV_COUNT':
               condition = new DataCriteriaDV_COUNT(criteria)
            break
            case 'DataCriteriaDV_PROPORTION':
               condition = new DataCriteriaDV_PROPORTION(criteria)
            break
            case 'DataCriteriaDV_ORDINAL':
               condition = new DataCriteriaDV_ORDINAL(criteria)
            break
            case 'DataCriteriaDV_DURATION':
               condition = new DataCriteriaDV_DURATION(criteria)
            break
            case 'DataCriteriaDV_IDENTIFIER':
               condition = new DataCriteriaDV_IDENTIFIER(criteria)
            break
            case 'DataCriteriaDV_MULTIMEDIA':
               condition = new DataCriteriaDV_MULTIMEDIA(criteria)
            break
            case 'DataCriteriaDV_PARSABLE':
               condition = new DataCriteriaDV_PARSABLE(criteria)
            break
            case 'DataCriteriaString':
               condition = new DataCriteriaString(criteria)
            break
            case 'DataCriteriaLOCATABLE_REF':
               condition = new DataCriteriaLOCATABLE_REF(criteria)
            break
         }

         // parent is needed in simple criteria by the algorithm
         // parent is transient in criteria, it wont save to DB
         // criteria.parent is JSONObject
         condition.tree_node = criteria
         condition.parent = criteria.parent

         leaves << new DataCriteriaExpression(criteria: condition)
      }
      return leaves
   }
}
