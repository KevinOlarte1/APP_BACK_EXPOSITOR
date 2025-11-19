package com.gestorventas.deposito.controllers;


import com.gestorventas.deposito.config.JwtUtil;
import com.gestorventas.deposito.dto.in.LoginRequestDto;
import com.gestorventas.deposito.dto.out.LoginResponseDto;
import com.gestorventas.deposito.models.Vendedor;
import com.gestorventas.deposito.repositories.VendedorRepository;
import com.gestorventas.deposito.services.MailService;
import com.gestorventas.deposito.services.VendedorService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final VendedorRepository vendedorRepository;
    private final VendedorService vendedorService;
    private final MailService emailService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    @Value("${security.jwt.refresh-expiration}") long refreshExpirationMillis;
;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto request){
        var auth =  new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
        authenticationManager.authenticate(auth);

        Vendedor user = vendedorRepository.findByEmail(request.getEmail()).orElseThrow();

        String acccessToken = jwtUtil.generateAccessToken(user.getEmail(), vendedorService.userClaims(user));
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(LocalDateTime.now().plusNanos(refreshExpirationMillis));

        vendedorRepository.save(user);
        return ResponseEntity.ok(new LoginResponseDto(acccessToken,refreshToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> request){
        String oldRefreshToken = request.get("refreshToken");

        //Verificar validez del token
        if(!jwtUtil.validateToken(oldRefreshToken)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Refresh token invalido "));
        }

        //Extraer el usuario
        String email = jwtUtil.getSubject(oldRefreshToken);
        Vendedor user = vendedorRepository.findByEmail(email).orElseThrow();

        //Comprobar si el token coincide con el guardado en la BBDD
        if (!oldRefreshToken.equals(user.getRefreshToken())){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Refresh token no coincide con el guardado en la BBDD"));
        }

        //Comprobar si el token ha expirado
        if (user.getRefreshTokenExpiry().isBefore(LocalDateTime.now())) {
            // Borrar el token caducado
            user.setRefreshToken(null);
            user.setRefreshTokenExpiry(null);
            vendedorRepository.save(user);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Refresh token caducado, inicia sesión nuevamente"));
        }

        //Generar nuevo token
        String newAccessToken = jwtUtil.generateAccessToken(email, vendedorService.userClaims(user));
        String newRefreshToken = jwtUtil.generateRefreshToken(email);

        //Actualizar BBDD
        user.setRefreshToken(newRefreshToken);
        user.setRefreshTokenExpiry(LocalDateTime.now().plusNanos(refreshExpirationMillis));
        vendedorRepository.save(user);

        return ResponseEntity.ok(new LoginResponseDto(newAccessToken,newRefreshToken));


    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request){
        String email = request.get("email");
        Vendedor user = vendedorRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No user found with that email"));
        }

        // Generar código de verificación (6 dígitos)
        String code = String.format("%06d", new Random().nextInt(999999));
        user.setResetCode(code);
        user.setResetCodeExpiry(LocalDateTime.now().plusMinutes(15));
        vendedorRepository.save(user);

        // Enviar correo
        String subject = "Código de recuperación de contraseña";
        String body = "Tu código de verificación es: " + code + "\nExpira en 10 minutos.";

        emailService.sendEmail(email, subject, body);

        return ResponseEntity.ok(Map.of("message", "Codigo de verificación enviado."));
    }

        @PostMapping("/reset_password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request){
        String email = request.get("email");
        String code = request.get("code");
        String newPassword = request.get("newPassword");

        Vendedor user = vendedorRepository.findByEmail(email).orElse(null);

        if(user == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Usuario no encontrado"));
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetCode(null);
        user.setResetCodeExpiry(null);
        vendedorRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Password cambiada correctamente"));
    }
}
