package org.example.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.dao.ProductDaoImpl;
import org.example.entity.Product;
import org.example.utils.DatabaseConnection;
import org.example.utils.JWTAuth;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;

public class ProductHandler implements HttpHandler {
    private final ProductDaoImpl productDao;

    public ProductHandler() throws SQLException {
        DatabaseConnection dbConnection = new DatabaseConnection();
        this.productDao = new ProductDaoImpl(dbConnection);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        JWTAuth jwtUtil = new JWTAuth();
        Authenticator.Result authResult = jwtUtil.authenticate(exchange);

        if (authResult instanceof Authenticator.Failure) {
            exchange.sendResponseHeaders(403, -1);
            exchange.close();
            return;
        }

        String method = exchange.getRequestMethod();
        String[] pathParts = exchange.getRequestURI().getPath().split("/");

        switch (method.toUpperCase()) {
            case "GET" -> getProduct(exchange);
            case "POST" -> postProduct(exchange);
            case "DELETE" -> deleteProduct(exchange);
            case "PUT" -> {
                if (pathParts.length == 3) {
                    putProduct(exchange);
                } else {
                    exchange.sendResponseHeaders(404, -1);
                    exchange.close();
                }
            }
            default -> {
                exchange.sendResponseHeaders(404, -1);
                exchange.close();
            }
        }
    }

    private void getProduct(HttpExchange exchange) throws IOException, NumberFormatException {
        int id;
        id = parseIdFromURI(exchange.getRequestURI().getPath());
        Product product = productDao.readProduct(id);
        if (product == null) {
            sendErrorCode(exchange, 404);
            return;
        }
        String jsonResponse = product.toString();
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(jsonResponse.getBytes());
        os.close();
        exchange.close();
    }
    private void putProduct(HttpExchange exchange) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(exchange.getRequestBody());
        try {
            Product product = productFromJson(jsonNode);
            productDao.createProduct(product.getName(), product.getPrice());
            // I can't show proper id since I have poor quality DAO. So I show name of the product.
            // I'm sorry.
            String response = "Product created. Name of product: " + product.getName();
            exchange.sendResponseHeaders(201, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
        } catch (IllegalArgumentException ex) {
            sendErrorCode(exchange, 409);
        }
    }

    private void postProduct(HttpExchange exchange) throws IOException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(exchange.getRequestBody());
            int id = parseIdFromURI(exchange.getRequestURI().getPath());
            if (productDao.readProduct(id) == null) {
                sendErrorCode(exchange, 404);
                return;
            }
            Product product = productFromJson(jsonNode);
            product.setId(id);
            productDao.updateProduct(id, product.getName(), product.getPrice());
            exchange.sendResponseHeaders(204, 0);
            exchange.close();
        } catch (IllegalArgumentException e) {
            sendErrorCode(exchange, 409);
        }
    }

    private void deleteProduct(HttpExchange exchange) throws IOException, NumberFormatException {
        int id = parseIdFromURI(exchange.getRequestURI().getPath());
        if (productDao.readProduct(id) == null) {
            sendErrorCode(exchange, 404);
            return;
        }
        productDao.deleteProduct(id);
        exchange.sendResponseHeaders(204, 0);
        exchange.close();
    }

    private int parseIdFromURI(String uri) {
        String[] pathParts = uri.split("/");
        String pathProductId = pathParts[3];
        return Integer.parseInt(pathProductId);
    }

    private void sendErrorCode(HttpExchange exchange, int code) throws IOException {
        exchange.sendResponseHeaders(code, 0);
        exchange.close();
    }

    private Product productFromJson(JsonNode jsonNode) throws IllegalArgumentException {
        int productId = jsonNode.get("product_id").asInt();
        String productName = jsonNode.get("product_name").asText();
        double productPrice = jsonNode.get("product_price").asDouble();

        if (productPrice < 0) {
            throw new IllegalArgumentException();
        }

        return new Product(productId, productName, productPrice);
    }
}