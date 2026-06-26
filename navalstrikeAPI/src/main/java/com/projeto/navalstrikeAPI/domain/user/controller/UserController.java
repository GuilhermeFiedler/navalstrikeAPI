package com.projeto.navalstrikeAPI.domain.user.controller;

import com.projeto.navalstrikeAPI.domain.user.dto.LoginRequest;
import com.projeto.navalstrikeAPI.domain.user.dto.RegisterRequest;
import com.projeto.navalstrikeAPI.domain.user.dto.TokenResponse;
import com.projeto.navalstrikeAPI.domain.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {this.userService = userService; }

    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(@RequestBody @Valid RegisterRequest request){
        return ResponseEntity.ok(userService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String header){
        userService.logout(header.substring(7));
        return ResponseEntity.noContent().build();

    }
}
