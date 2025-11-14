package com.example.speech.service;

import com.example.speech.model.User;
import com.example.speech.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserService {
    public static boolean registerUser(User user) {
        String querySQL = "insert into user_ (Email_User, Visible_Name_User, Name_User, Password_User, Birthday_User)" +
                "values (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(querySQL)){

            statement.setString(1, user.getEmail());
            statement.setString(2, user.getVisibleName());
            statement.setString(3, user.getUserName());
            statement.setString(4, user.getPassword());
            //Конвертируем LocalDate в Date
            statement.setDate(5, java.sql.Date.valueOf(user.getBirthday()));

            // Выполняем запрос и проверяем результат
            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка транзакции с БД: " + e.getMessage());
            return false;
        }
    }

    public static User getUserByEmail(String email) {
        String querySQL = "select * from user_ where Email_User = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(querySQL)) {

            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next())
                    return mapResultSetToUser(resultSet);
            }

        } catch (SQLException e) {
            System.err.println("Ошибка транзакции с БД: " + e.getMessage());
        }
        return null;
    }

    public static User getUserByUserName(String userName) {
        String querySQL = "select * from user_ where Name_User = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(querySQL)) {

            statement.setString(1, userName);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next())
                    return mapResultSetToUser(resultSet);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка транзакции с БД: " + e.getMessage());
        }

        return null;
    }

    private static User mapResultSetToUser(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getInt("Id_User"));
        user.setEmail(resultSet.getString("Email_User"));
        user.setVisibleName(resultSet.getString("Visible_Name_User"));
        user.setUserName(resultSet.getString("Name_User"));
        user.setPassword(resultSet.getString("Password_User"));
        user.setBirthday(resultSet.getDate("Birthday_User").toLocalDate());

        return user;
    }
}
