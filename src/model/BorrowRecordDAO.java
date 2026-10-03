package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BorrowRecordDAO {

    // =========================
    // CREATE
    // =========================

    public static void addBorrowRecord(BorrowRecord record) {

        String sql = "INSERT INTO BorrowRecords "
                   + "(user_id, isbn, borrow_date, due_date, renewed, on_hold) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        try {
            Connection connection =
                    DatabaseConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            statement.setInt(
                    1,
                    record.getUser().getUserId()
            );

            statement.setString(
                    2,
                    record.getBook().getIsbn()
            );

            statement.setDate(
                    3,
                    java.sql.Date.valueOf(
                            record.getBorrowDate()
                    )
            );

            statement.setDate(
                    4,
                    java.sql.Date.valueOf(
                            record.getDueDate()
                    )
            );

            statement.setBoolean(
                    5,
                    record.isRenewed()
            );

            statement.setBoolean(
                    6,
                    record.isOnHold()
            );

            statement.executeUpdate();

            System.out.println(
                    "Borrow record added to database!"
            );

            statement.close();
            connection.close();

        } catch (Exception e) {

            System.out.println(
                    "Failed to add borrow record."
            );

            e.printStackTrace();
        }
    }


    // =========================
    // READ - ALL RECORDS
    // =========================

    public static List<BorrowRecord> getAllBorrowRecords() {

        List<BorrowRecord> records =
                new ArrayList<>();

        String sql =
                "SELECT * FROM BorrowRecords";

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

                String isbn =
                        result.getString("isbn");

                LocalDate borrowDate =
                        result.getDate("borrow_date")
                              .toLocalDate();

                LocalDate dueDate =
                        result.getDate("due_date")
                              .toLocalDate();

                boolean renewed =
                        result.getBoolean("renewed");

                boolean onHold =
                        result.getBoolean("on_hold");


                // Get corresponding User
                User user =
                        UserDAO.getUserById(userId);

                // Get corresponding Book
                Book book =
                        BookDAO.getBookByIsbn(isbn);


                if (user != null && book != null) {

                    BorrowRecord record =
                            new BorrowRecord(
                                    user,
                                    book,
                                    borrowDate
                            );

                    /*
                     * The normal constructor sets the
                     * due date automatically, so we need
                     * the exact database due date.
                     */
                    record.setDueDate(dueDate);

                    record.setRenewed(renewed);

                    record.setOnHold(onHold);

                    records.add(record);
                }
            }

            result.close();
            statement.close();
            connection.close();

        } catch (Exception e) {

            System.out.println(
                    "Failed to get borrow records."
            );

            e.printStackTrace();
        }

        return records;
    }


    // =========================
    // FIND RECORD
    // =========================

    public static BorrowRecord getBorrowRecord(
            int userId,
            String isbn) {

        String sql =
                "SELECT * FROM BorrowRecords "
              + "WHERE user_id = ? AND isbn = ?";

        try {

            Connection connection =
                    DatabaseConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            statement.setInt(1, userId);
            statement.setString(2, isbn);

            ResultSet result =
                    statement.executeQuery();

            if (result.next()) {

                LocalDate borrowDate =
                        result.getDate("borrow_date")
                              .toLocalDate();

                LocalDate dueDate =
                        result.getDate("due_date")
                              .toLocalDate();

                boolean renewed =
                        result.getBoolean("renewed");

                boolean onHold =
                        result.getBoolean("on_hold");

                User user =
                        UserDAO.getUserById(userId);

                Book book =
                        BookDAO.getBookByIsbn(isbn);

                if (user != null && book != null) {

                    BorrowRecord record =
                            new BorrowRecord(
                                    user,
                                    book,
                                    borrowDate
                            );

                    record.setDueDate(dueDate);
                    record.setRenewed(renewed);
                    record.setOnHold(onHold);

                    result.close();
                    statement.close();
                    connection.close();

                    return record;
                }
            }

            result.close();
            statement.close();
            connection.close();

        } catch (Exception e) {

            System.out.println(
                    "Failed to find borrow record."
            );

            e.printStackTrace();
        }

        return null;
    }


    // =========================
    // UPDATE DUE DATE
    // =========================

    public static void updateBorrowRecord(
            int recordId,
            java.sql.Date newDueDate) {

        String sql =
                "UPDATE BorrowRecords SET due_date = ? "
              + "WHERE record_id = ?";

        try {

            Connection connection =
                    DatabaseConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            statement.setDate(
                    1,
                    newDueDate
            );

            statement.setInt(
                    2,
                    recordId
            );

            statement.executeUpdate();

            System.out.println(
                    "Borrow record updated successfully!"
            );

            statement.close();
            connection.close();

        } catch (Exception e) {

            System.out.println(
                    "Failed to update borrow record."
            );

            e.printStackTrace();
        }
    }


    // =========================
    // UPDATE STATUS
    // =========================

    public static void updateBorrowStatus(
            int userId,
            String isbn,
            LocalDate dueDate,
            boolean renewed,
            boolean onHold) {

        String sql =
                "UPDATE BorrowRecords "
              + "SET due_date = ?, renewed = ?, on_hold = ? "
              + "WHERE user_id = ? AND isbn = ?";

        try {

            Connection connection =
                    DatabaseConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            statement.setDate(
                    1,
                    java.sql.Date.valueOf(dueDate)
            );

            statement.setBoolean(
                    2,
                    renewed
            );

            statement.setBoolean(
                    3,
                    onHold
            );

            statement.setInt(
                    4,
                    userId
            );

            statement.setString(
                    5,
                    isbn
            );

            statement.executeUpdate();

            statement.close();
            connection.close();

        } catch (Exception e) {

            System.out.println(
                    "Failed to update borrow status."
            );

            e.printStackTrace();
        }
    }


    // =========================
    // DELETE BY USER + ISBN
    // =========================

    public static void deleteBorrowRecord(
            int userId,
            String isbn) {

        String sql =
                "DELETE FROM BorrowRecords "
              + "WHERE user_id = ? AND isbn = ?";

        try {

            Connection connection =
                    DatabaseConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            statement.setInt(
                    1,
                    userId
            );

            statement.setString(
                    2,
                    isbn
            );

            statement.executeUpdate();

            System.out.println(
                    "Borrow record deleted successfully!"
            );

            statement.close();
            connection.close();

        } catch (Exception e) {

            System.out.println(
                    "Failed to delete borrow record."
            );

            e.printStackTrace();
        }
    }


    // =========================
    // DELETE BY RECORD ID
    // =========================

    public static void deleteBorrowRecord(
            int recordId) {

        String sql =
                "DELETE FROM BorrowRecords "
              + "WHERE record_id = ?";

        try {

            Connection connection =
                    DatabaseConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            statement.setInt(
                    1,
                    recordId
            );

            statement.executeUpdate();

            System.out.println(
                    "Borrow record deleted successfully!"
            );

            statement.close();
            connection.close();

        } catch (Exception e) {

            System.out.println(
                    "Failed to delete borrow record."
            );

            e.printStackTrace();
        }
    }
}