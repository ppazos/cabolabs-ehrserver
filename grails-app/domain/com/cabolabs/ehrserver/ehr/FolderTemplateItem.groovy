package com.cabolabs.ehrserver.ehr

class FolderTemplateItem {

   String name
   
   List folders = []
   static hasMany = [folders: FolderTemplateItem]

   static constraints = {
      name(nullable: true, blank: false)
   }
}
