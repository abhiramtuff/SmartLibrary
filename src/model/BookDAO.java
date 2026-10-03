package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {

    // =========================
    // CREATE
    // =========================

    public static void addBook(Book book) {

        String sql = "INSERT INTO Books "
                   + "(isbn, title, author, genre, status) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try {
            Connection connection = DatabaseConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            statement.setString(1, book.getIsbn());
            statement.setString(2, book.getTitle());
            statement.setString(3, book.getAuthor());
            statement.setString(4, book.getGenre());
            statement.setString(5, book.getStatus().toString());

            statement.executeUpdate();

            System.out.println("Book added to database!");

            statement.close();
            connection.close();

        } catch (Exception e) {
            System.out.println("Failed to add book.");
            e.printStackTrace();
        }
    }


    // =========================
    // READ
    // =========================

    public static List<Book> getAllBooks() {

        List<Book> books = new ArrayList<>();

        String sql = "SELECT * FROM Books";

        try {
            Connection connection = DatabaseConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            ResultSet result = statement.executeQuery();

            while (result.next()) {

                String isbn = result.getString("isbn");
                String title = result.getString("title");
                String author = result.getString("author");
                String genre = result.getString("genre");

                Book book = new Book(
                        title,
                        author,
                        isbn,
                        genre
                );

                books.add(book);

                // Display database record
                System.out.println("-------------------------");
                System.out.println("ISBN: " + isbn);
                System.out.println("Title: " + title);
                System.out.println("Author: " + author);
                System.out.println("Genre: " + genre);
                System.out.println("Status: "
                        + result.getString("status"));
            }

            result.close();
            statement.close();
            connection.close();

        } catch (Exception e) {
            System.out.println("Failed to get books.");
            e.printStackTrace();
        }

        return books;
    }


    // =========================
    // FIND BOOK BY ISBN
    // =========================

    public static Book getBookByIsbn(String isbn) {

        String sql = "SELECT * FROM Books WHERE isbn = ?";

        try {
            Connection connection =
                    DatabaseConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            statement.setString(1, isbn);

            ResultSet result = statement.executeQuery();

            if (result.next()) {

                Book book = new Book(
                        result.getString("title"),
                        result.getString("author"),
                        result.getString("isbn"),
                        result.getString("genre")
                );

                result.close();
                statement.close();
                connection.close();

                return book;
            }

            result.close();
            statement.close();
            connection.close();

        } catch (Exception e) {
            System.out.println("Failed to find book.");
            e.printStackTrace();
        }

        return null;
    }


    // =========================
    // UPDATE
    // =========================

    public static void updateBook(
            String isbn,
            String newTitle) {

        String sql =
                "UPDATE Books SET title = ? WHERE isbn = ?";

        try {
            Connection connection =
                    DatabaseConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            statement.setString(1, newTitle);
            statement.setString(2, isbn);

            statement.executeUpdate();

            System.out.println("Book updated successfully!");

            statement.close();
            connection.close();

        } catch (Exception e) {
            System.out.println("Failed to update book.");
            e.printStackTrace();
        }
    }


    public static void updateBook(
            String isbn,
            String title,
            String author,
            String genre) {

        String sql =
                "UPDATE Books SET title = ?, author = ?, genre = ? WHERE isbn = ?";

        try {
            Connection connection =
                    DatabaseConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            statement.setString(1, title);
            statement.setString(2, author);
            statement.setString(3, genre);
            statement.setString(4, isbn);

            statement.executeUpdate();

            System.out.println("Book updated successfully (all fields)!");

            statement.close();
            connection.close();

        } catch (Exception e) {
            System.out.println("Failed to update book details.");
            e.printStackTrace();
        }
    }


    // =========================
    // DELETE
    // =========================

    public static void deleteBook(String isbn) {

        String sql =
                "DELETE FROM Books WHERE isbn = ?";

        try {
            Connection connection =
                    DatabaseConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            statement.setString(1, isbn);

            statement.executeUpdate();

            System.out.println("Book deleted successfully!");

            statement.close();
            connection.close();

        } catch (Exception e) {
            System.out.println("Failed to delete book.");
            e.printStackTrace();
        }
    }
}