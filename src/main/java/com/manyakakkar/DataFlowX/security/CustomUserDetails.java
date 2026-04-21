package com.manyakakkar.DataFlowX.security;

import com.manyakakkar.DataFlowX.entity.RegUsers;
import com.manyakakkar.DataFlowX.entity.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final RegUsers regUsers;

    public CustomUserDetails(RegUsers user) {
        this.regUsers = user;
    }

    @Override
    public List<SimpleGrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(regUsers.getRole()));
    }

    public Long getUserId() {
        return regUsers.getId();
    }


    @Override public String getPassword() { return regUsers.getPassword(); }
    @Override public String getUsername() { return regUsers.getEmail(); }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}

