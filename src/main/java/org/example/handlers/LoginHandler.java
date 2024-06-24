package org.example.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.utils.JWTAuth;

import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginHandler implements HttpHandler {
    private static final String LOGIN = "admin";
    private static final String PASSWORD_MD5 = "21232f297a57a5a743894a0e4a801fc3"; // "admin" in md5

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            exchange.close();
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode requestBody = mapper.readTree(exchange.getRequestBody());

        String login = requestBody.get("login").asText();
        String password = requestBody.get("password").asText();

        try {
            if (authenticate(login, password)) {
                String token = JWTAuth.createJWTToken(login);
                String responseText = "Ok " + token;
                byte[] response = responseText.getBytes();

                exchange.sendResponseHeaders(200, response.length);
                OutputStream os = exchange.getResponseBody();
                os.write(response);
                os.close();
            } else {
                String responseText = "Unauthorized";
                byte[] response = responseText.getBytes();

                exchange.sendResponseHeaders(401, response.length);
                OutputStream os = exchange.getResponseBody();
                os.write(response);
                os.close();
            }
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean authenticate(String login, String password) throws NoSuchAlgorithmException {
        return LOGIN.equals(login) && md5(password).equals(PASSWORD_MD5);
    }

    private String md5(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] messageDigest = md.digest(input.getBytes());
        StringBuilder hexString = new StringBuilder();
        for (byte b : messageDigest) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }
}
