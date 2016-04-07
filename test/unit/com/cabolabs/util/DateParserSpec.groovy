package com.cabolabs.util

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
class DateParserSpec extends Specification {

    def setup()
    {
    }

    def cleanup()
    {
    }

    // TODO: user datatables http://spockframework.github.io/spock/docs/1.0/data_driven_testing.html
    void "parsed UTC date is the same as utc formatted output"()
    {
       when: 
          def utc_format = "yyyy-MM-dd'T'HH:mm:ss'Z'" // UTC
          def utc_date_string = "2016-01-10T22:30:00Z"
          def utc_date = DateParser.tryParse(utc_date_string)

          /*
          println tryParse("2016-01-10T22:30:00").format(utc_format, TimeZone.getTimeZone('UTC'))  // Sun Jan 10 00:00:00 GFT 2016 (NO TZ specified can't process time, it matches just the date)
          println tryParse("2016-01-10T22:30:00-0300").format(utc_format, TimeZone.getTimeZone('UTC'))
          println tryParse("2016-01-10T22:30:00-0500").format(utc_format, TimeZone.getTimeZone('UTC'))
          
          // without format it uses my locale TZ -0300
          println tryParse("2016-01-10T22:30:00Z") // Sun Jan 10 19:30:00 GFT 2016
          println tryParse("2016-01-10T22:30:00")  // Sun Jan 10 00:00:00 GFT 2016
          println tryParse("2016-01-10T22:30:00-0300") // Sun Jan 10 22:30:00 GFT 2016 (same TZ!)
          println tryParse("2016-01-10T22:30:00-0500") // Mon Jan 11 00:30:00 GFT 2016
          */
          
       then:
          utc_date_string == utc_date.format(utc_format, TimeZone.getTimeZone('UTC'))
    }
    
    void "parse dates with valid ISO 8601 time zones"()
    {
       when:
         def valid_dates = ['2016-01-01T22:30:00-03', '2016-01-01T22:30:00-0300', '2016-01-01T22:30:00-03:00']
         def dates = []
         valid_dates.each {
            dates << DateParser.tryParse(it)
         }
         
       then:
         '2016-01-02T01:30:00Z' == dates[0].format("yyyy-MM-dd'T'HH:mm:ss'Z'", TimeZone.getTimeZone('UTC'))
         '2016-01-02T01:30:00Z' == dates[1].format("yyyy-MM-dd'T'HH:mm:ss'Z'", TimeZone.getTimeZone('UTC'))
         '2016-01-02T01:30:00Z' == dates[2].format("yyyy-MM-dd'T'HH:mm:ss'Z'", TimeZone.getTimeZone('UTC'))
         
    }
}
