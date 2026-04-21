package com.manyakakkar.DataFlowX.controller;

import com.manyakakkar.DataFlowX.dto.AuthResponse;
import com.manyakakkar.DataFlowX.dto.LoginRequest;
import com.manyakakkar.DataFlowX.dto.SignupRequest;
import com.manyakakkar.DataFlowX.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {


    private final AuthService authService;


    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(
            @RequestBody SignupRequest request) {

        authService.signup(request);
        return ResponseEntity.ok("User registered successfully");
    }

@PostMapping("/login")
public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {

    System.out.println("LOGIN HIT"); // 👈 ADD THIS

    return ResponseEntity.ok(authService.login(request));
}

}
