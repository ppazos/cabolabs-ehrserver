package com.cabolabs.ehrserver.exceptions

public class CommitRequiredValueNotPresentException extends RuntimeException {

   public CommitRequiredValueNotPresentException(String message)
   {
      super(message)
   }
}
