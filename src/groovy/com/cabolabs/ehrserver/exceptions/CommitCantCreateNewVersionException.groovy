package com.cabolabs.ehrserver.exceptions

public class CommitCantCreateNewVersionException extends RuntimeException {

   public CommitCantCreateNewVersionException(String message)
   {
      super(message)
   }
}
