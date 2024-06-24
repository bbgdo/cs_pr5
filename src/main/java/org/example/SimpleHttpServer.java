package org.example;

import com.sun.net.httpserver.HttpServer;
import io.jsonwebtoken.io.IOException;
import org.example.handlers.LoginHandler;
import org.example.handlers.ProductHandler;

import java.net.InetSocketAddress;
import java.sql.SQLException;

public class SimpleHttpServer {
    public static void main(String[] args) throws IOException, SQLException, java.io.IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/login", new LoginHandler());
        server.createContext("/api/product", new ProductHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Server is running.");
    }
}
/*
to update the product data, the {product_id} in the json is specified
simply so that the product entity can be created.
it does not affect the id in the database.
all {product_id}s can be viewed in the database.
*/
