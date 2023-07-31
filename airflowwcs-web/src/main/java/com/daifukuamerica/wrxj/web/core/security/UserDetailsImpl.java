package com.daifukuamerica.wrxj.web.core.security;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.daifukuamerica.wrxj.web.model.hibernate.UserAuthGroup;

/**
 * Implementation of User's details with addition of WRx specific fields. 
 * 
 * Author: dystout
 * Created : May 25, 2017
 *
 */
public class UserDetailsImpl implements UserDetails
{
    private static final long serialVersionUID = 1L;
    private String username; // userid to use for actions
    private String password; // Won't really be utilized in session
    private String displayName; // to display to end user
    private String displayRole; // displayable client-specific role (for use with third party applications)
    
//    private Collection<GrantedAuthority> authorities; 
    private List<UserAuthGroup> authGroups; 
 
    public UserDetailsImpl(String username, String displayName, String displayRole, String password,
            /*Collection<GrantedAuthority> authorities, */List<UserAuthGroup> authGroups) {
        this.username = username;
        this.displayName = displayName; 
        this.displayRole = displayRole; 
        this.password = password;
/*        this.authorities = authorities;*/
        this.authGroups = authGroups; 
    }
 
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        return this.authorities;
//    } 
    
    public Collection<? extends GrantedAuthority>  getAuthorities(){ 
    	if(authGroups==null){
    		return Collections.emptySet(); 
    	}
    	Set<SimpleGrantedAuthority> grantedAuthorities = new HashSet<>(); 
    	authGroups.forEach(group->{
    		grantedAuthorities.add(new SimpleGrantedAuthority(group.getAuthGroupName())); 
    	});
    	return grantedAuthorities; 
    }
 
    public String getPassword() {
        return this.password;
    }
 
    public String getUsername() {
        return this.username;
    }
 
    public boolean isAccountNonExpired() {
        return true;
    }
 
    public boolean isAccountNonLocked() {
        return true;
    }
 
    public boolean isCredentialsNonExpired() {
        return true;
    }
 
    public boolean isEnabled() {
        return true;
    }

	public String getDisplayName()
	{
		return displayName;
	}

	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}

	public String getDisplayRole()
	{
		return displayRole;
	}

	public void setDisplayRole(String displayRole)
	{
		this.displayRole = displayRole;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

//	public void setAuthorities(Collection<GrantedAuthority> authorities)
//	{
//		this.authorities = authorities;
//	}
    
    
}
