package com.gestorventas.deposito.config;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    private final Key key;
    private final long expirationMillis;
    private final long refreshExpirationMillis;

    public JwtUtil(
            @Value("${security.jwt.secret-key}") String secret,
            @Value("${security.jwt.expiration-time}") long expirationMillis,
            @Value("${security.jwt.refresh-expiration}") long refreshExpirationMillis
    ) {
        byte[] decodedKey = Base64.getDecoder().decode(secret);
        this.key = Keys.hmacShaKeyFor(decodedKey);
        this.expirationMillis = expirationMillis;
        this.refreshExpirationMillis = refreshExpirationMillis;
    }

    // 🟢 Generar Token de acceso
    public String generateAccessToken(String subject, Map<String,Object> claims) {
        return generateToken(subject, claims, 9000); //expirationMillis
    }

    // 🟢 Generar Refresh Token
    public String generateRefreshToken(String subject) {
        return generateToken(subject, Map.of("refresh", true),refreshExpirationMillis);
    }

    //🔵 Genera Token con claims
    public String generateToken(String subject, Map<String, Object> claims, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .addClaims(claims)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getSubject(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token){
        try{
            parseClaims(token);
            return true;
        }catch(JwtException | IllegalArgumentException e){
            return false;
        }
    }

    private void parseClaims(String token) {
         Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
