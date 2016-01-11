package com.cabolabs.ehrserver.query

import grails.test.mixin.*
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification
import grails.test.mixin.Mock
import com.cabolabs.ehrserver.query.*
import com.cabolabs.ehrserver.query.datatypes.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@Mock([Query,DataGet,DataCriteria,DataCriteriaDV_QUANTITY])
class QuerySpec extends Specification {

    def setup()
    {
       def q = new Query(
          name:'my query',
          type:'composition'
       )
       q.save(failOnError:true)
    }

    def cleanup()
    {
    }

    void "add dataget to query"()
    {
       when:
          def q = Query.get(1)
          
          def dg = new DataGet(
             archetypeId: 'openEHR-EHR-OBSERVATION.blood_pressure.v1',
             path:'/data')
          q.addToSelect(dg)
          
          dg = new DataGet(
             archetypeId: 'openEHR-EHR-OBSERVATION.blood_pressure.v1',
             path:'/data/event')
          q.addToSelect(dg)
          
          dg = new DataGet(
             archetypeId: 'openEHR-EHR-OBSERVATION.blood_pressure.v1',
             path:'/data/event/data')
          q.addToSelect(dg)
          
          q.save(failOnError:true)
          
          DataGet.list().each { println it.path }
       
       then:
          q.select.size() == 3
          DataGet.count() == 3
    }
    
    void "add datacriteria to query"()
    {
       when:
          def q = Query.get(1)
          
          def dc = new DataCriteriaDV_QUANTITY(
             archetypeId: 'openEHR-EHR-OBSERVATION.blood_pressure.v1',
             path:'/data',
             rmTypeName: 'DV_QUANTITY',
             spec: 0,
             alias: 'dqi',
             magnitudeValue: [123.0d],
             magnitudeOperand: 'eq',
             unitsValue: 'mm[Hg]',
             unitsOperand: 'eq')
          q.addToWhere(dc)
          
          dc = new DataCriteriaDV_QUANTITY(
             archetypeId: 'openEHR-EHR-OBSERVATION.blood_pressure.v1',
             path:'/data/event',
             rmTypeName: 'DV_QUANTITY',
             spec: 0,
             alias: 'dqi',
             magnitudeValue: [456.0d],
             magnitudeOperand: 'eq',
             unitsValue: 'mm[Hg]',
             unitsOperand: 'eq')
          q.addToWhere(dc)
          
          dc = new DataCriteriaDV_QUANTITY(
             archetypeId: 'openEHR-EHR-OBSERVATION.blood_pressure.v1',
             path:'/data/event/data',
             rmTypeName: 'DV_QUANTITY',
             spec: 0,
             alias: 'dqi',
             magnitudeValue: [789.0d],
             magnitudeOperand: 'eq',
             unitsValue: 'mm[Hg]',
             unitsOperand: 'eq')
          q.addToWhere(dc)
          

          q.save(failOnError:true)
          
          DataCriteria.list().each { println it.magnitudeValue }
       
       then:
          q.where.size() == 3
          DataCriteria.count() == 3
    }
}
