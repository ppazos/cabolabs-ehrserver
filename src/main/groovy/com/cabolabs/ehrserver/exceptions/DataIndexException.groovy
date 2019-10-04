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

   public DataIndexException(String message, Errors e)
   {
      super(message, e)
   }

   @Override
   public String getMessage()
   {
      return super.getMessage() + (index ? '. Index: '+ index : '')
   }
}
