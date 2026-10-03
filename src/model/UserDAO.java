package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // =========================
    // CREATE
    // =========================

    public static void addUser(User user) {

        String sql = "INSERT INTO Users (user_id, name, contact) "
                   + "VALUES (?, ?, ?)";

        try {
            Connection connection =
                    DatabaseConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            statement.setInt(1, user.getUserId());
            statement.setString(2, user.getName());
            statement.setString(3, user.getContact());

            statement.executeUpdate();

            System.out.println("User added to database!");

            statement.close();
            connection.close();

        } catch (Exception e) {
            System.out.println("Failed to add user.");
            e.printStackTrace();
        }
    }


    // =========================
    // READ
    // =========================

    public static List<User> getAllUsers() {

        List<User> users = new ArrayList<>();

        String sql = "SELECT * FROM Users";

        try {
            Connection connection =
                    DatabaseConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            ResultSet result =
                    statement.executeQuery();

            while (result.next()) {

                int userId =
                        result.getInt("user_id");

                String name =
                        result.getString("name");

                String contact =
                        result.getString("contact");

                User user =
                        new User(userId, name, contact);

                users.add(user);

                System.out.println("-------------------------");
                System.out.println("User ID: " + userId);
                System.out.println("Name: " + name);
                System.out.println("Contact: " + contact);
            }

            result.close();
            statement.close();
            connection.close();

        } catch (Exception e) {
            System.out.println("Failed to get users.");
            e.printStackTrace();
        }

        return users;
    }


    // =========================
    // FIND USER BY ID
    // =========================

    public static User getUserById(int userId) {

        String sql =
                "SELECT * FROM Users WHERE user_id = ?";

        try {
            Connection connection =
                    DatabaseConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            statement.setInt(1, userId);

            ResultSet result =
                    statement.executeQuery();

            if (result.next()) {

                User user = new User(
                        result.getInt("user_id"),
                        result.getString("name"),
                        result.getString("contact")
                );

                result.close();
                statement.close();
                connection.close();

                return user;
            }

            result.close();
            statement.close();
            connection.close();

        } catch (Exception e) {
            System.out.println("Failed to find user.");
            e.printStackTrace();
        }

        return null;
    }


    // =========================
    // UPDATE
    // =========================

    public static void updateUser(
            int userId,
            String newName) {

        String sql =
                "UPDATE Users SET name = ? WHERE user_id = ?";

        try {
            Connection connection =
                    DatabaseConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            statement.setString(1, newName);
            statement.setInt(2, userId);

            statement.executeUpdate();

            System.out.println("User updated successfully!");

            statement.close();
            connection.close();

        } catch (Exception e) {
            System.out.println("Failed to update user.");
            e.printStackTrace();
        }
    }


    public static void updateUser(
            int userId,
            String name,
            String contact) {

        String sql =
                "UPDATE Users SET name = ?, contact = ? WHERE user_id = ?";

        try {
            Connection connection =
                    DatabaseConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            statement.setString(1, name);
            statement.setString(2, contact);
            statement.setInt(3, userId);

            statement.executeUpdate();

            System.out.println("User updated successfully (all fields)!");

            statement.close();
            connection.close();

        } catch (Exception e) {
            System.out.println("Failed to update user details.");
            e.printStackTrace();
        }
    }


    // =========================
    // DELETE
    // =========================

    public static void deleteUser(int userId) {

        String sql =
                "DELETE FROM Users WHERE user_id = ?";

        try {
            Connection connection =
                    DatabaseConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            statement.setInt(1, userId);

            statement.executeUpdate();

            System.out.println("User deleted successfully!");

            statement.close();
            connection.close();

        } catch (Exception e) {
            System.out.println("Failed to delete user.");
            e.printStackTrace();
        }
    }
}