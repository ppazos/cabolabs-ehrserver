package com.cabolabs.platform.notifications

import grails.transaction.Transactional

import static groovyx.net.http.Method.GET
import groovyx.net.http.HTTPBuilder
import grails.util.Holders

@Transactional
class RemoteNotificationsService {

   def getNotifications(String service, String lang, Date from)
   {
      def res = []
      def error = false
      def status
      def http = new HTTPBuilder('http://notifications.cloudehrserver.com')

      def fromStr = ''
      if (from)
      {
         def formatterDateDB = new java.text.SimpleDateFormat(Holders.config.app.l10n.ext_datetime_format_nof)
         fromStr = formatterDateDB.format(from)
      }

      try
      {
         http.request( GET ) {
            uri.path = '/pull.php'
            uri.query = [sys: service, lang: lang, from: fromStr]
            headers.Accept = 'application/json'
            //headers.Authorization = 'Bearer '+ api_key // FIXME: get from config

            response.success = { resp, json ->

               res = json
            }

            // FIXME: log/report errors correctly
            response.failure = { resp, reader ->
               println 'request failed'
               println resp
               println resp.statusLine
               println resp.status
               println reader.text

               status = resp.status

               error = true
            }
         }
      }
      catch (Exception e)
      {
         throw new Exception('remoteNotifications.error.connectionError', e)
      }

      if (error)
      {
         throw new Exception(status)
      }

      return res
   }
}
