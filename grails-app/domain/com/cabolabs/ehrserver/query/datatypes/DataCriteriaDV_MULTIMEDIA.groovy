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

package com.cabolabs.ehrserver.query.datatypes

import com.cabolabs.ehrserver.query.DataCriteria
import com.cabolabs.ehrserver.ehr.clinical_documents.data.DvMultimediaIndex

class DataCriteriaDV_MULTIMEDIA extends DataCriteria {

   List mediaTypeValue // one or more media types in the criteria
   String alternateTextValue
   List sizeValue // can be a range
   String uriValue

   String mediaTypeOperand
   String alternateTextOperand
   String sizeOperand
   String uriOperand

   boolean mediaTypeNegation = false
   boolean alternateTextNegation = false
   boolean sizeNegation = false
   boolean uriNegation = false

   DataCriteriaDV_MULTIMEDIA()
   {
      rmTypeName = 'DV_MULTIMEDIA'
      alias = 'dvmmd'
   }

   static hasMany = [mediaTypeValue: String, sizeValue: Integer]

   static constraints = {
      mediaTypeOperand nullable: true
      alternateTextOperand nullable: true
      alternateTextValue nullable: true
      sizeOperand nullable: true
   }

   static List criteriaSpec(String archetypeId, String path, boolean returnCodes = true)
   {
      // FIXME: the OPT can have more constrained mediaTypes, need to get them from there also.
      def mediaTypes = DvMultimediaIndex.constraints.mediaType.inList
      def mediaTypesMap = [:]
      mediaTypes.each {
         mediaTypesMap[it] = it
      }
      def spec = [
        [
          size: [
            eq:  'value',
            lt:  'value',
            gt:  'value',
            neq: 'value',
            le:  'value',
            ge:  'value',
            between: 'range'
          ]
        ],
        [
          mediaType: [
            eq: 'value',
            in_list: 'list',
            mediaTypes: mediaTypesMap
          ]
        ],
        [
          alternateText: [
            contains: 'value'
          ]
        ],
        [
          uri: [
            contains: 'value'
          ]
        ]
      ]

      return spec
   }

   static List attributes()
   {
      return ['alternateText', 'mediaType', 'size', 'uri']
   }

   static List functions()
   {
      return []
   }

   boolean containsFunction()
   {
      return false
   }
}
