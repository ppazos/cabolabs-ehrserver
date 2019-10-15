package com.cabolabs.ehrserver.security

class ResetTokenCleanJob {
    static triggers = {
      simple repeatInterval: 5000l // execute job once in 5 seconds
    }

    def execute() {
        // execute job
    }
}
