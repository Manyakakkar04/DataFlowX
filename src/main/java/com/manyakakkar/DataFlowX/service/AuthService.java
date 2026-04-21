package com.manyakakkar.DataFlowX.service;

import com.manyakakkar.DataFlowX.dto.*;
import com.manyakakkar.DataFlowX.entity.RegUsers;
import com.manyakakkar.DataFlowX.repository.RegUsersRepository;
import com.manyakakkar.DataFlowX.security.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;



@Slf4j
@Service
public class AuthService {

    private final RegUsersRepository regUsersRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authManager;

    public AuthService(RegUsersRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtils jwtUtils,
                       AuthenticationManager authManager) {
        this.regUsersRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.authManager = authManager;
    }

    public void signup(SignupRequest request) {
        log.info("Signup attempt for email={}", request.getEmail());

        if (regUsersRepository.existsByEmail(request.getEmail())) {
            log.warn("Signup failed: email already exists [{}]", request.getEmail());
            throw new RuntimeException("Email already exists");
        }

        RegUsers user = RegUsers.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("ROLE_USER")
                .build();

        regUsersRepository.save(user);
        log.info("User registered successfully with email={}", request.getEmail());
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email={}", request.getEmail());

        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        }
        catch (BadCredentialsException e) {
            log.warn("Login failed due to invalid credentials for email={}", request.getEmail());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during login for email={}", request.getEmail(), e);
            throw e;
        }


        String token = jwtUtils.generateTokenFromUsername(request.getEmail());
        log.info("JWT generated successfully for email={}", request.getEmail());

        return new AuthResponse(token);
    }
}