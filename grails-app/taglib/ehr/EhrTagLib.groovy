package ehr

import directory.Folder

class EhrTagLib {
   
   def springSecurityService

   def hasEhr = { attrs, body ->
      
      if (!attrs.patientUID) throw new Exception("patientUID es obligatorio")
      
      //println patientUID
      
      def c = Ehr.createCriteria()
      
      def ehr = c.get {
         subject {
            eq ('value', attrs.patientUID)
         }
      }
      
      //println ehr
      
      if (ehr) out << body()
   }
   
   def dontHasEhr = { attrs, body ->

      if (!attrs.patientUID) throw new Exception("patientUID es obligatorio")
      
      //println attrs.patientUID
      
      def c = Ehr.createCriteria()
      
      def ehr = c.get {
         subject {
            eq ('value', attrs.patientUID)
         }
      }
      
      //println ehr
      
      if (!ehr) out << body()
   }
   
   def ehr_directory = { attrs, body ->
      
      if (!attrs.directory) return
      
      out << recursive_directory(attrs.directory)
   }
   
   private String recursive_directory(Folder folder)
   {
      def html = $/
      |<div class="folder">
      |  <div class="folder_name">
      |    <input type="radio" name="folder.id" value="${folder.id}" />
      |    ${folder.name} (${folder.items.size()})
      |  </div>
      |  <div class="folder_items">
      /$.stripMargin()

      folder.items.each {
         // Versioned Composition: UID
         html += '<div class="folder_item">Versioned Composition: '+ g.link(controller:'versionedComposition', action:'show', params:[uid:it], it) +'</div>'
      }
      
      html += '</div><div class="folder_folders">'
      
      folder.folders.each {
         html += recursive_directory(it)
      }
      
      html += '</div></div>' // /folder_folders, /folder
      
      return html
   }
   
   
   /**
    * Renders a select with the organizations of the current logged user.
    * This is used in the query test UID to filter by organizationUid.
    */
   def selectWithCurrentUserOrganiations = { attrs, body ->
      
      def loggedInUser = springSecurityService.currentUser
  
      if(loggedInUser)
      {
         def orgs = loggedInUser.organizationObjects
         out << g.select(name:attrs.name, from:orgs, optionKey:'uid', optionValue:'name')
      }
   }
}