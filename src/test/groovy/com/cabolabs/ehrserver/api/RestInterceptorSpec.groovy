package com.cabolabs.ehrserver.api

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class RestInterceptorSpec extends Specification implements InterceptorUnitTest<RestInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test rest interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"rest")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}
