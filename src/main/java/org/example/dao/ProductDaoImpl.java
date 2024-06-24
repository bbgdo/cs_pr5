package org.example.dao;

import org.example.entity.Product;
import org.example.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductDaoImpl implements ProductDao {
    private DatabaseConnection dbConnection;

    public ProductDaoImpl(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public void createProduct(String name, double price) {
        String query = "INSERT INTO products (product_name, product_price) VALUES (?, ?)";
        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, name);
            statement.setDouble(2, price);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Product readProduct(int id) {
        String query = "SELECT * FROM products WHERE product_id = ?";
        try (Connection connection = dbConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new Product(
                            resultSet.getInt("product_id"),
                            resultSet.getString("product_name"),
                            resultSet.getDouble("product_price")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void updateProduct(int id, String name, double price) {
        String query = "UPDATE products SET product_name = ?, product_price = ? WHERE product_id = ?";
        try (Connection connection = dbConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, name);
            statement.setDouble(2, price);
            statement.setInt(3, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteProduct(int id) {
        String query = "DELETE FROM products WHERE product_id = ?";
        try (Connection connection = dbConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Product> listProductsByPrice(double maxPrice) {
        List<Product> products = new ArrayList<>();
        String query = "SELECT * FROM products WHERE product_price <= ?";
        try (Connection connection = dbConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setDouble(1, maxPrice);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    products.add(new Product(resultSet.getInt("product_id"), resultSet.getString("product_name"), resultSet.getDouble("product_price")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }
}
