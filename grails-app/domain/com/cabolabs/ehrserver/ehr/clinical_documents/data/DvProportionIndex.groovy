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
