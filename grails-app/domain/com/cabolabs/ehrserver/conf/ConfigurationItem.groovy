package com.cabolabs.ehrserver.conf

import com.cabolabs.ehrserver.account.Account
import com.cabolabs.security.Organization

class ConfigurationItem {

   String key
   String value
   String type
   boolean blank = false
   String description

   // if this item is for a specific account or organization, account or organization will be set
   // if this is an app config item, both will be null
   Account account
   Organization organization

   /* MySQL reserved words */
   static mapping = {
      key column: "config_key"
      value column: "config_value"
      type column: "config_type"
   }

   static constraints = {
      account nullable: true
      organization nullable: true
      key (unique: true, nullable: false, blank: false)
      value validator: { val, obj, errors ->
         if (!obj.blank && !val) errors.rejectValue('value', 'emptyValueNotAllowedBlankIsFalse')
         switch (obj.type) {
            case 'string':
               // any valid string will validate
            break
            case 'number':
               // TODO: need to consider decimal points depending on the current locale
               // the way to check the number was parsed OK is to serialize and compare with the original value
               // https://gist.github.com/ppazos/9dfb8b856ddd14518de0a4111231a095
               def format = java.text.NumberFormat.getInstance()
               try
               {
                  format.parse(val)
               }
               catch (Exception e)
               {
                  errors.rejectValue('value', 'invalidNumber')
               }
            break
            case 'boolean':
               if (!['true','false'].contains(val))
            break
            case 'url':
               if (!(val =~ /\b(https?|ftp|file):\\/\\/[-a-zA-Z0-9+&@#\\/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#\\/%=~_|]/))
               {
                  errors.rejectValue('value', 'invalidURL')
               }
            break
            case 'uuid':
               if (!(val =~ /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/))
               {
                  errors.rejectValue('value', 'invalidUUID')
               }
            break
            case 'oid':
               if (!(val =~ /^([0-2])(\\.([1-9]([0-9])*))*$/))
               {
                  errors.rejectValue('value', 'invalidOID')
               }
            break
         }
      }
      type(inList:['string', 'number', 'boolean', 'url', 'uuid', 'oid'])
      description(nullable: false)
   }

   static transients = ['typedValue']

   def getTypedValue()
   {
      def tv
      switch (this.type) {
         case 'string':
         case 'url':
         case 'uuid':
         case 'oid':
            tv = this.value
         break
         case 'number':
            // TODO: need to consider decimal points depending on the current locale
            // the way to check the number was parsed OK is to serialize and compare with the original value
            // https://gist.github.com/ppazos/9dfb8b856ddd14518de0a4111231a095
            def format = java.text.NumberFormat.getInstance()
            tv = format.parse(this.value)
         break
         case 'boolean':
            tv = ((this.value == 'true') ? true : false)
         break
      }
      return tv
   }
}
