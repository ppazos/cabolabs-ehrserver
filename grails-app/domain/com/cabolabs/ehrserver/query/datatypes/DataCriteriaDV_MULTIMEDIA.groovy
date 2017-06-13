package com.cabolabs.ehrserver.query.datatypes

import com.cabolabs.ehrserver.query.DataCriteria
import com.cabolabs.ehrserver.ehr.clinical_documents.data.DvMultimediaIndex

class DataCriteriaDV_MULTIMEDIA extends DataCriteria {

   List mediaTypeValue // one or more media types in the criteria
   String alternateTextValue
   List sizeValue // can be a range
   
   String mediaTypeOperand
   String alternateTextOperand
   String sizeOperand
   
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
   
   static List criteriaSpec(String archetypeId, String path)
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
        ]
      ]
      
      return spec
   }
   
   static List attributes()
   {
      return ['alternateText', 'mediaType', 'size']
   }
}
