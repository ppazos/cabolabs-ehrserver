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

package com.cabolabs.ehrserver.ehr.clinical_documents.data

class DvProportionIndex extends DataValueIndex {

   static final float unknownAccuracyValue = -1.0
   
   double numerator
   double denominator
   
   // proportion kind controlled values
   // pk_ratio = 0 num and denom may be any value
   // pk_unitary = 1 denominator must be 1
   // pk_percent = 2 denominator is 100, numerator is understood as a percentage
   // pk_fraction = 3 num and denum are integral and the presentation method used a slash e.g. 1/2
   // pk_integer_fraction = 4 num and denom are integral, usual presentation is n/d; if numerator > denominator, display as “a b/c”, i.e. the integer part followed by the remaining fraction part, e.g. 1 1/2;
   int type
   int precision = -1 // 0 implies integral quantity (num and denom are integers), -1 means no limit of decimal spaces
   
   // From DV_AMOUNT, superclass DV_PROPORTION
   // 0 means 100% of accuracy i.e. no error in the measurement, can be expressed as half range
   // percent (range=max - min where max is 100% and min is x%) of half range quantity (range=max-min), 
   // half range = range / 2, so it's value +- half range.
   float accuracy = unknownAccuracyValue
   
   // true if the accuracy was recorded as percentage, in this case accuracy should be 0..100
   boolean accuracyIsPercent
   
   // From DV_QUANTIFIED, superclass of DV_AMOUNT
   // non quantified indication of accuracy, if not present, meaning is "="
   //  - "=" magnitude is a point value
   //  - "<" values is < magnitude
   //  - ">" value is > magnitude
   //  - "<=" value is <= magnitude
   //  - ">=" value is >= magnitude
   //  - "~" value is approximately magnitude
   String magnitudeStatus = '='
   
   
   // TODO: Maybe saving the magnitude as another data point allows better querying capabilities.
   def magnitude()
   {
      if (denominator == 0) return Double.POSITIVE_INFINITY
      
      return numerator / denominator
   }
   
   
   
   static constraints = {
      precision(nullable: true)
      type(inList:[0,1,2,3,4])
   }
   
   // precision is a mysql reserved word
   static mapping = {
      precision column:'proportion_precision'
   }
}
