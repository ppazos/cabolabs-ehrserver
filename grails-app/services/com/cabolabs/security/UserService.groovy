package com.cabolabs.security

import grails.transaction.Transactional
import com.cabolabs.security.User
import org.springframework.security.core.authority.AuthorityUtils

@Transactional
class UserService {

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
}
