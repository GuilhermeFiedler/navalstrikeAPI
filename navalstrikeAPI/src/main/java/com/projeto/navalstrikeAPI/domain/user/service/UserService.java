package com.projeto.navalstrikeAPI.domain.user.service;

import com.projeto.navalstrikeAPI.domain.user.dto.LoginRequest;
import com.projeto.navalstrikeAPI.domain.user.dto.RegisterRequest;
import com.projeto.navalstrikeAPI.domain.user.dto.TokenResponse;
import com.projeto.navalstrikeAPI.domain.user.dto.UserResponse;
import com.projeto.navalstrikeAPI.domain.user.entity.User;
import com.projeto.navalstrikeAPI.domain.user.repository.UserRepository;
import com.projeto.navalstrikeAPI.infra.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService
    jwtService;


    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public TokenResponse register(RegisterRequest request) {
        if (!request.password().equals(request.passwordConfirmation())){
            throw new IllegalArgumentException("Senhas não são iguais");
        }
        if (userRepository.existsByEmail(request.email())){
            throw new IllegalArgumentException("E-mail já cadastrado");
        }
        var user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        userRepository.save(user);
        return new TokenResponse(jwtService
                .generateToken(user.getId(), user.getEmail(), user.getName()));
    }

    public TokenResponse login(LoginRequest request){
        var user = userRepository.findByEmail(request.email()).orElseThrow(()->
                new IllegalArgumentException("Credenciais inválidas"));
        if (!passwordEncoder.matches(request.password(), user.getPassword())){
            throw new IllegalArgumentException("Credenciais inválidas");
        }
        return new TokenResponse(jwtService
                .generateToken(user.getId(), user.getEmail(), user.getName()));
    }
    public void logout(String token) { jwtService.revokeToken(token);}

    public UserResponse getUserFromToken(String token) {
        var decoded = com.auth0.jwt.JWT.decode(token);
        return new UserResponse(
                java.util.UUID.fromString(decoded.getSubject()),
                decoded.getClaim("name").asString()
        );
    }
}



