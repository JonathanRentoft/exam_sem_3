package app.security;

import app.entities.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

public class JwtUtil {
    private final Algorithm algorithm;
    private final String issuer = "candidate-matcher-api";
    private final long expirationTime = 3600000; // 1 hour

    public JwtUtil(String secret) {
        this.algorithm = Algorithm.HMAC256(secret);
    }

    public String createToken(User user) {
        return JWT.create()
                .withIssuer(issuer)
                .withSubject(user.getUsername())
                .withClaim("userId", user.getId())
                .withClaim("roles", user.getRoles().stream()
                        .map(Enum::name)
                        .collect(Collectors.toList()))
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + expirationTime))
                .sign(algorithm);
    }

    public DecodedJWT verifyToken(String token) throws JWTVerificationException {
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(issuer)
                .build();
        return verifier.verify(token);
    }

    public String extractUsername(String token) {
        DecodedJWT jwt = verifyToken(token);
        return jwt.getSubject();
    }

    public Set<String> extractRoles(String token) {
        DecodedJWT jwt = verifyToken(token);
        return jwt.getClaim("roles").asList(String.class).stream()
                .collect(Collectors.toSet());
    }
}
