package com.projeto.navalstrikeAPI.domain.user.controller;

import com.projeto.navalstrikeAPI.domain.user.dto.AuthResponse;
import com.projeto.navalstrikeAPI.domain.user.dto.LoginRequest;
import com.projeto.navalstrikeAPI.domain.user.dto.RegisterRequest;
import com.projeto.navalstrikeAPI.domain.user.dto.TokenResponse;
import com.projeto.navalstrikeAPI.domain.user.dto.UserResponse;
import com.projeto.navalstrikeAPI.domain.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class UserController {
    private final UserService userService;

    private static final int COOKIE_MAX_AGE = 7200; // 2 horas

    public UserController(UserService userService) {this.userService = userService; }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request, HttpServletResponse response){
        TokenResponse tokenResponse = userService.register(request);
        addTokenCookie(response, tokenResponse.token());
        UserResponse user = userService.getUserFromToken(tokenResponse.token());
        return ResponseEntity.ok(new AuthResponse(user.id(), user.name(), tokenResponse.token()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request, HttpServletResponse response) {
        TokenResponse tokenResponse = userService.login(request);
        addTokenCookie(response, tokenResponse.token());
        UserResponse user = userService.getUserFromToken(tokenResponse.token());
        return ResponseEntity.ok(new AuthResponse(user.id(), user.name(), tokenResponse.token()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response){
        String token = extractTokenFromCookie(request);
        if (token != null) {
            userService.logout(token);
        }
        clearTokenCookie(response);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> me(HttpServletRequest request, HttpServletResponse response) {
        String token = extractTokenFromCookie(request);
        if (token == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            UserResponse user = userService.getUserFromToken(token);
            return ResponseEntity.ok(new AuthResponse(user.id(), user.name(), token));
        } catch (Exception e) {
            clearTokenCookie(response);
            return ResponseEntity.status(401).build();
        }
    }

    private void addTokenCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from("token", token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(COOKIE_MAX_AGE)
                .sameSite("None")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("None")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String extractTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
