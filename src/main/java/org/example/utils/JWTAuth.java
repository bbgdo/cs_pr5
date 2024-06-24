package org.example.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;

import java.util.Date;

public class JWTAuth extends Authenticator {
    private static final String SECRET_KEY = "qRZdMfNTq47BW4eD2lEeUQ5Q6QCKDSVepItYwUf50ZA=";
    private static final long EXPIRATION_TIME = 86400000; // 1 day (or day 1)

    public static String createJWTToken(String subject) {
        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
        return JWT.create()
                .withSubject(subject)
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(algorithm);
    }

    public static boolean validateJWTToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
            JWTVerifier verifier = JWT.require(algorithm).build();
            verifier.verify(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Result authenticate(HttpExchange exchange) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return new Failure(403);
        }

        String token = authHeader.substring(7);
        if (validateJWTToken(token)) {
            DecodedJWT jwt = JWT.decode(token);
            return new Success(new HttpPrincipal(jwt.getSubject(), ""));
        } else {
            return new Failure(403);
        }
    }
}