package com.cabolabs.security

import grails.transaction.Transactional
import com.cabolabs.security.User
import org.springframework.security.core.authority.AuthorityUtils

@Transactional
class UserService {

   def notificationService
   
   def getByUsername(String username)
   {
      def u = User.findByUsername(username) // can be null
      
      return u
   }
    
   def getUserAuthorities(User user)
   {
      def aus = user.authorities // Set<Role>
      def authstr = aus.authority // List<String> with role names
      
      // http://docs.spring.io/autorepo/docs/spring-security/3.2.1.RELEASE/apidocs/org/springframework/security/core/authority/AuthorityUtils.html
      return AuthorityUtils.createAuthorityList(authstr as String[]) // List<AuthorityUtils>
   }
    
   def saveAndNotify(User userInstance, params)
   {
      if (!userInstance.password)
      {
         userInstance.enabled = false
         userInstance.setPasswordToken()
      }
      
      // Associate orgs
      def orgUids = params.list("organizationUid")
      def newOrgs = Organization.findAllByUidInList(orgUids)
      newOrgs.each { newOrg ->
         userInstance.addToOrganizations(newOrg)
      }
      
      userInstance.save(failOnError:true)

      // TODO: UserRole ORG_* needs a reference to the org, since the user
      //      can be ORG_ADMIN in one org and ORG_STAFF in another org.
      //UserRole.create( userInstance, (Role.findByAuthority('ROLE_ORG_STAFF')), true )

      // Add selected roles
      def roles = params.list('role')
      roles.each { authority ->
         
         UserRole.create( userInstance, (Role.findByAuthority(authority)), true )
      }

      // TODO: schedule emails
      // token to create the URL for the email is in the userInstance
      notificationService.sendUserCreatedEmail( userInstance.email, [userInstance] )
   }
}
