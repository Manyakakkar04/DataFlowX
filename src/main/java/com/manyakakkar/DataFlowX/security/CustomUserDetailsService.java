package com.manyakakkar.DataFlowX.security;

import com.manyakakkar.DataFlowX.repository.RegUsersRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final RegUsersRepository regUsersRepository;

    public CustomUserDetailsService(RegUsersRepository regUsersRepository) {
        this.regUsersRepository = regUsersRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        return regUsersRepository.findByEmail(email)
                .map(CustomUserDetails::new)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));
    }
}
