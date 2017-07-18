package com.cabolabs.ehrserver.api

import grails.transaction.Transactional
import grails.converters.*
import groovy.xml.MarkupBuilder
import java.io.StringWriter
import grails.util.Environment

@Transactional
class ApiResponsesService {
   
   def grailsApplication
   
   def feedback(message, type, code, format)
   {
      if (!format) format = 'json'
      format = format.toLowerCase()
      
      if (format == 'json') return feedback_json(message, type, code)
      if (format == 'xml') return feedback_xml(message, type, code)
      return feedback_json(message, type, code) // default
   }
   
   def feedback_json(message, type, code)
   {
      return feedback_json(message, type, code, [])
   }
   
   def feedback_json(message, type, code, details)
   {
      def feedback = [
         result: [
            type: type,
            message: message,
            code: 'EHRSERVER::API::RESPONSE_CODES::'+ code
         ]
      ]
      
      if (details)
      {
         def g = grailsApplication.mainContext.getBean('org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib')
         feedback.result.details = []
         details.each {
            if (it instanceof String)
            {
               feedback.result.details << it
            }
            else if (it instanceof org.springframework.validation.FieldError) // validation error
            {
               feedback.result.details << g.message(error: it)
            }
         }
      }
      
      return feedback as JSON
   }
   
   def feedback_json(message, type, code, details, exception)
   {
      def feedback = [
         result: [
            type: type,
            message: message,
            code: 'EHRSERVER::API::RESPONSE_CODES::'+ code
         ]
      ]
      
      if (details)
      {
         def g = grailsApplication.mainContext.getBean('org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib')
         feedback.result.details = []
         details.each { // TODO: add this to XML feedback
            if (it instanceof String)
            {
               feedback.result.details << it
            }
            else if (it instanceof org.springframework.validation.FieldError) // validation error
            {
               feedback.result.details << g.message(error: it) // resolves each validation error!
            }
         }
      }
      
      if (exception && Environment.current == Environment.DEVELOPMENT)
      {
         StringWriter ewriter = new StringWriter()
         PrintWriter printWriter = new PrintWriter( ewriter )
         org.codehaus.groovy.runtime.StackTraceUtils.sanitize(exception).printStackTrace(printWriter)
         printWriter.flush()
         String _trace = ewriter.toString()
         feedback.result.trace = _trace
      }
      
      return feedback as JSON
   }
   
   def feedback_xml(message, type, code)
   {
      return feedback_xml(message, type, code, [])
   }
   
   def feedback_xml(message, type, code, details)
   {
      def writer = new StringWriter()
      def xml = new MarkupBuilder(writer)
      xml.result() {
         delegate.type(type)
         delegate.message(message)
         delegate.code('EHR_SERVER::API::RESPONSE_CODES::'+ code)
         if (details)
         {
            delegate.details {
               details.each { error ->
                  item(error)
               }
            }
         }
      }

      def feedback = writer.toString()

      return feedback
   }
   
   def feedback_xml(message, type, code, details, exception)
   {
      def writer = new StringWriter()
      def xml = new MarkupBuilder(writer)
      xml.result() {
         delegate.type(type)
         delegate.message(message)
         delegate.code('EHR_SERVER::API::RESPONSE_CODES::'+ code)
         if (details)
         {
            delegate.details {
               details.each { error ->
                  item(error)
               }
            }
         }
         if (exception && Environment.current == Environment.DEVELOPMENT)
         {
            StringWriter ewriter = new StringWriter()
            PrintWriter printWriter = new PrintWriter( ewriter )
            org.codehaus.groovy.runtime.StackTraceUtils.sanitize(exception).printStackTrace(printWriter)
            printWriter.flush()
            String _trace = ewriter.toString()
            
            trace( _trace )
         }
      }

      def feedback = writer.toString()

      return feedback
   }
}
