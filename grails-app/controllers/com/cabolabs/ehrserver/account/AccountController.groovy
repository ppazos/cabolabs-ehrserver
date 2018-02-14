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

package com.cabolabs.ehrserver.account

import grails.transaction.Transactional
import com.cabolabs.security.*

@Transactional(readOnly = true)
class AccountController {

   // Only admins can see the list of all the Accounts, each AccountManager 
   // will see just his Account, other Roles won't have access to the Account.
   // Permissions are checked in the SecurityFilter.
   def index()
   {
      def accounts = Account.list()
      [accounts: accounts]
   }
   
   def show(Long id)
   {
      if (!id)
      {
         flash.message = message(code:'account.show.idIsRequired')
         redirect(url:request.getHeader('referer'))
         return
      }
      
      def account = Account.get(id)
      if (!account)
      {
         flash.message = message(code:'account.show.accountNotFound')
         redirect(url:request.getHeader('referer'))
         return
      }
      
      [account: account]
   }
   
   def create()
   {
   }
   
   @Transactional
   def save(Account account)
   {
      if (!account)
      {
         flash.message = message(code:'account.save.error')
         render (view: 'create')
         return
      }
      
      if (!account.save(flush:true))
      {
         flash.message = message(code:'account.save.error')
         render (view: 'create', model: [account: account])
         return
      }

      flash.message = message(code:'account.save.ok', args:[account.id])
      redirect action:'index'
   }
   
   def edit(Account account)
   {
      [account: account]
   }
   
   @Transactional
   def update(Account account)
   {
      if (!account)
      {
         flash.message = message(code:'account.update.error')
         render (view: 'edit')
         return
      }
      
      if (!account.save(flush:true))
      {
         flash.message = message(code:'account.update.error')
         render (view: 'edit', model: [account: account])
         return
      }

      flash.message = message(code:'account.update.ok', args:[account.id])
      redirect action:'show', id: account.id
   }
   
   // TODO: delete, we need to define rules
   
}
