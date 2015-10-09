/* Copyright 2004, 2005, 2006 Acegi Technology Pty Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cabolabs.security;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;


/**
 * Based on {@link http://grepcode.com/file_/repo1.maven.org/maven2/org.springframework.security/spring-security-core/3.0.2.RELEASE/org/springframework/security/authentication/UsernamePasswordAuthenticationToken.java/?v=source}
 *
 * @author Pablo Pazos <pablo.pazos@cabolabs.com>
 */
public class UserPassOrgAuthToken extends AbstractAuthenticationToken {

    private final Object credentials;
    private final Object principal; // username before login or GrailsUser after login
    private final Object organization;


    /**
     * This constructor can be safely used by any code that wishes to create a
     * <code>UserPassOrgAuthToken</code>, as the {@link
     * #isAuthenticated()} will return <code>false</code>.
     *
     */
    public UserPassOrgAuthToken(Object principal, Object credentials, Object organization) {
        super(null);
        this.principal = principal;
        this.credentials = credentials;
        this.organization = organization;
        setAuthenticated(false);
    }

    /**
     * @deprecated use the list of authorities version
     */
    public UserPassOrgAuthToken(Object principal, Object credentials, Object organization, GrantedAuthority[] authorities) {
        this(principal, credentials, organization, Arrays.asList(authorities));
    }

    /**
     * This constructor should only be used by <code>AuthenticationManager</code> or <code>AuthenticationProvider</code>
     * implementations that are satisfied with producing a trusted (i.e. {@link #isAuthenticated()} = <code>true</code>)
     * authentication token.
     *
     * @param principal
     * @param credentials
     * @param authorities
     */
    public UserPassOrgAuthToken(Object principal, Object credentials, Object organization, Collection<GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        this.organization = organization;
        super.setAuthenticated(true); // must use super, as we override
    }


    public Object getCredentials() {
        return this.credentials;
    }

    public Object getPrincipal() {
        return this.principal;
    }
    
    public Object getOrganization() {
        return this.organization;
    }

    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        //if (isAuthenticated) {
        //    throw new IllegalArgumentException(
        //        "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
        //}

        super.setAuthenticated(false);
    }
}
