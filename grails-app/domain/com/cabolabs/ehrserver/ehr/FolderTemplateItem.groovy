package com.cabolabs.ehrserver.ehr

class FolderTemplateItem {

   String name
   static hasMany = [folders: FolderTemplateItem]

   static constraints = {
      name(nullable: true, blank: false)
   }
}
