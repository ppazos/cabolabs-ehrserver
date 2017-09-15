
/*
 * Copyright 2011-2017 CaboLabs Health Informatics
 *
 * The EHRServer was designed and developed by Pablo Pazos Gutierrez <pablo.pazos@cabolabs.com> at CaboLabs Health Informatics (www.cabolabs.com).
 *
 * You can't remove this notice from the source code, you can't remove the "Powered by CaboLabs" from the UI, you can't remove this notice from the window that appears then the "Powered by CaboLabs" link is clicked.
 *
 * Any modifications to the provided source code can be stated below this notice.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cabolabs.ehrserver.openehr.directory

import static org.springframework.http.HttpStatus.*
import grails.transaction.Transactional
import com.cabolabs.security.Organization
import com.cabolabs.ehrserver.openehr.directory.Folder
import com.cabolabs.ehrserver.openehr.ehr.Ehr
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.util.Holders

@Transactional(readOnly = true)
class FolderController {

   def springSecurityService
   def configurationService
   
   static allowedMethods = [save: "POST", update: "PUT"]
   
   def config = Holders.config.app

   def index()
   {
      params.max = configurationService.getValue('ehrserver.console.lists.max_items')
      
      def list, count
      
      // All folders for admins, filtered by org uid for other roles
      if (SpringSecurityUtils.ifAllGranted("ROLE_ADMIN"))
      {
         list = Folder.list(params)
         count = Folder.count()
      }
      else
      {
         // auth token used to login
         def auth = springSecurityService.authentication
         def org = Organization.findByNumber(auth.organization)
         
         list = Folder.findAllByOrganizationUid(org.uid, params)
         count = Folder.countByOrganizationUid(org.uid)
      }
      
      respond list, model:[total: count]
   }

   def show(Folder folderInstance)
   {
      respond folderInstance
   }

   def create()
   {
      // Filter ehrs by the ehrs that don't have a root folder and are ehrs the user can see by it's org uids
      def user = springSecurityService.getCurrentUser()
      def ehrs = Ehr.findAllByDirectoryIsNullAndOrganizationUidInList(user.organizations.uid)
      respond new Folder(params), model: [ehrs: ehrs]
   }

   @Transactional
   def save(Folder folderInstance)
   {
      if (folderInstance == null)
      {
         notFound()
         return
      }

      /*
      // FIXME: the org uid of the folder should be the same as the owning ehr...
      // admins can select the org uid, for other roles is the org used to login
      if (!SpringSecurityUtils.ifAllGranted("ROLE_ADMIN"))
      {
         // auth token used to login
         def auth = springSecurityService.authentication
         def org = Organization.findByNumber(auth.organization)
         //println "org "+ org
         //println "org uid "+ org.uid
         
         folderInstance.organizationUid = org.uid
      }
      */
      
      if (folderInstance.ehr)
      {
         println "folder ehr "+ folderInstance.ehr
         //def ehr = ehr.Ehr.get(folderInstance.ehrUid)
         folderInstance.ehr.directory = folderInstance
         // root folder has the org uid of the ehr
         folderInstance.organizationUid = folderInstance.ehr.organizationUid
         
         // saves the folder
         if (!folderInstance.ehr.save(flush:true)) println folderInstance.ehr.errors
      }
      else // take the org uid from the parent
      {
         println "folder tiene parent"
         folderInstance.organizationUid = folderInstance.parent.organizationUid
         
         if (!folderInstance.save(flush:true))
         {
            respond folderInstance, view:'create'
            return
         }
      }
      
      
      
      
      request.withFormat {
         form multipartForm {
            flash.message = message(code: 'default.created.message', args: [message(code: 'folder.label', default: 'Folder'), folderInstance.id])
            redirect folderInstance
         }
         '*' { respond folderInstance, [status: CREATED] }
      }
   }

   def edit(Folder folderInstance)
   {
      respond folderInstance
   }

   @Transactional
   def update(Folder folderInstance)
   {
      if (folderInstance == null) {
         notFound()
         return
      }

      if (folderInstance.hasErrors()) {
         respond folderInstance.errors, view:'edit'
         return
      }

      folderInstance.save flush:true

      request.withFormat {
         form multipartForm {
            flash.message = message(code: 'default.updated.message', args: [message(code: 'Folder.label', default: 'Folder'), folderInstance.id])
            redirect folderInstance
         }
         '*'{ respond folderInstance, [status: OK] }
      }
   }

   @Transactional
   def delete(Folder folderInstance)
   {
      if (folderInstance == null)
      {
         notFound()
         return
      }

      // root folders can't be deleted (to make things easier for the case below)
      if (folderInstance.ehr)
      {
         flash.message = message(code: 'folder.delete.cantDeleteRootFolder')
         redirect (action:'show', id:folderInstance.id)
         return
      }
      
      // TODO: this can be added on Folder.beforeDelete
      
      // If the folder has children, just delete the folder and let
      // the children parent be the parent of the current folder.
      // This works because the root folder can't be deleted, so we
      // know the deleted folder has a parent folder.
      //(root folders don't have a parent folder)
      if (folderInstance.folders)
      {
         def parent = folderInstance.parent
         
         parent.removeFromFolders(folderInstance) // deletes parent => folderInstance
         folderInstance.parent = null             // deletes folderInstnace => parent
         
         folderInstance.folders.each { child ->
            
            folderInstance.removeFromFolders(child) // deletes folder => child
            child.parent = parent                   // deletes child => folder
            parent.addToFolders(child)
         }
         
         if (!parent.save(flush:true))
         {
            // TODO: handle this
            println parent.errors
         }
      }
      
      // Physical delete!
      folderInstance.delete(flush:true)

      
      flash.message = message(code: 'default.deleted.message', args: [message(code: 'Folder.label', default: 'Folder'), folderInstance.id])
      redirect action:"index", method:"GET"
   }

   protected void notFound()
   {
      request.withFormat {
         form multipartForm {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'folder.label', default: 'Folder'), params.id])
            redirect action: "index", method: "GET"
         }
         '*'{ render status: NOT_FOUND }
      }
   }
   
   @Transactional
   def addItems(Long id)
   {
      println params
      
      List vouids = params.list('versioned_object_uids')
      
      println vouids
      
      def folder = Folder.get(id)
      
      vouids.each {
        if (!folder.items.contains(it)) // avoid adding the same item twice
        {
          folder.items.add(it) // addToItems dont work over simple type hasMany
        }
      }
      
      if (!folder.save(flush:true)) println folder.errors
      
      // FIXME
      render "ok"
   }
}
