package com.cabolabs.ehrserver.conf

import grails.transaction.Transactional

@Transactional
class ConfigurationService {

   def items = [:]

   def get(String key)
   {
      //ConfigurationItem.findByKey(key)
      items[key]
   }
   
   def getValue(String key)
   {
      //def item = ConfigurationItem.findByKey(key)
      def item = items[key]
      if (item)
      {
         return item.typedValue
      }
   }
   
   def refresh()
   {
      this.items = [:]
      ConfigurationItem.list().each {
         this.items[it.key] = it
      }
   }
}
