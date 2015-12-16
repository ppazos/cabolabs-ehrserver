/**
 * 
 */
package com.cabolabs.ehrserver.exceptions

import grails.validation.ValidationException
import org.springframework.validation.Errors

/**
 * @author pab
 *
 */
class DataIndexException extends ValidationException {

   String index

   /**
    * 
    * @param message
    * @param index path associated with the index failted to create.
    */
   public DataIndexException(String message, Errors e, String index)
   {
      super(message, e)
      this.index = index
   }
   
   @Override
   public String getMessage()
   {
      return super.getMessage() +'. Index: '+ index
   }
}
