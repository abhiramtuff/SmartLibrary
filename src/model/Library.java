package model;

import java.util.ArrayList;
import java.util.List;

public class Library {

    private List<Book> books;
    private List<User> users;
    private List<BorrowRecord> borrowRecords;

    // Maximum number of books a user can borrow
    private static final int BORROWING_LIMIT = 3;


    // =========================
    // CONSTRUCTOR
    // =========================

    public Library() {

        books = new ArrayList<>();
        users = new ArrayList<>();
        borrowRecords = new ArrayList<>();

        // Load data from database
        loadFromDatabase();
    }


    // =========================
    // LOAD DATABASE DATA
    // =========================

    private void loadFromDatabase() {

        // Load books
        books = BookDAO.getAllBooks();

        // Load users
        users = UserDAO.getAllUsers();

        // Load borrow records
        List<BorrowRecord> databaseRecords =
                BorrowRecordDAO.getAllBorrowRecords();

        /*
         * Reconnect the borrow records to the
         * Book and User objects already stored
         * in this Library.
         */
        for (BorrowRecord databaseRecord : databaseRecords) {

            User user = searchUserById(
                    databaseRecord.getUser().getUserId()
            );

            Book book = searchByIsbn(
                    databaseRecord.getBook().getIsbn()
            );

            if (user != null && book != null) {

                BorrowRecord record =
                        new BorrowRecord(
                                user,
                                book,
                                databaseRecord.getBorrowDate()
                        );

                record.setDueDate(
                        databaseRecord.getDueDate()
                );

                record.setRenewed(
                        databaseRecord.isRenewed()
                );

                record.setOnHold(
                        databaseRecord.isOnHold()
                );

                borrowRecords.add(record);

                // Mark book as borrowed
                book.borrowBook();

                // Add book to user's borrowed list
                user.addBorrowedBook(book);
            }
        }

        System.out.println("Library data loaded from database.");
    }


    // =========================
    // BOOK MANAGEMENT
    // =========================

    // Add a book
    public void addBook(Book book) {

        books.add(book);

        // Save to database
        BookDAO.addBook(book);

        System.out.println("Book added successfully.");
    }


    // Remove a book
    public void removeBook(Book book) {

        if (books.remove(book)) {

            // Delete from database
            BookDAO.deleteBook(book.getIsbn());

            System.out.println(
                    "Book removed successfully."
            );

        } else {

            System.out.println("Book not found.");
        }
    }


    // Update book details
    public boolean updateBook(
            String isbn,
            String title,
            String author,
            String genre) {

        Book book = searchByIsbn(isbn);

        if (book != null) {

            // Update Java object
            book.setTitle(title);
            book.setAuthor(author);
            book.setGenre(genre);

            // Update database
            BookDAO.updateBook(isbn, title, author, genre);

            return true;
        }

        return false;
    }


    // Search book by title
    public Book searchByTitle(String title) {

        for (Book book : books) {

            if (book.getTitle().equalsIgnoreCase(title)) {

                return book;
            }
        }

        return null;
    }


    // Search book by author
    public List<Book> searchByAuthor(String author) {

        List<Book> results = new ArrayList<>();

        for (Book book : books) {

            if (book.getAuthor().equalsIgnoreCase(author)) {

                results.add(book);
            }
        }

        return results;
    }


    // Search book by ISBN
    public Book searchByIsbn(String isbn) {

        for (Book book : books) {

            if (book.getIsbn().equals(isbn)) {

                return book;
            }
        }

        return null;
    }


    // Search book by genre
    public List<Book> searchByGenre(String genre) {

        List<Book> results = new ArrayList<>();

        for (Book book : books) {

            if (book.getGenre().equalsIgnoreCase(genre)) {

                results.add(book);
            }
        }

        return results;
    }


    // =========================
    // USER MANAGEMENT
    // =========================

    // Register a user
    public void registerUser(User user) {

        users.add(user);

        // Save to database
        UserDAO.addUser(user);

        System.out.println(
                "User registered successfully."
        );
    }


    // Remove a user
    public void removeUser(User user) {

        if (users.remove(user)) {

            // Delete from database
            UserDAO.deleteUser(user.getUserId());

            System.out.println(
                    "User removed successfully."
            );

        } else {

            System.out.println("User not found.");
        }
    }


    // Update user details
    public boolean updateUser(
            int userId,
            String name,
            String contact) {

        User user = searchUserById(userId);

        if (user != null) {

            // Update Java object
            user.setName(name);
            user.setContact(contact);

            // Update database
            UserDAO.updateUser(userId, name, contact);

            return true;
        }

        return false;
    }


    // Search user by ID
    public User searchUserById(int userId) {

        for (User user : users) {

            if (user.getUserId() == userId) {

                return user;
            }
        }

        return null;
    }


    // Search user by name
    public List<User> searchUserByName(String name) {

        List<User> results = new ArrayList<>();

        for (User user : users) {

            if (user.getName().equalsIgnoreCase(name)) {

                results.add(user);
            }
        }

        return results;
    }


    // =========================
    // BORROWING
    // =========================

    // Borrow a book
    public boolean borrowBook(int userId, String isbn) {

        User user = searchUserById(userId);
        Book book = searchByIsbn(isbn);


        // Check if user exists
        if (user == null) {

            System.out.println("User not found.");
            return false;
        }


        // Check if book exists
        if (book == null) {

            System.out.println("Book not found.");
            return false;
        }


        // Check if book is available
        if (book.getStatus() != BookStatus.AVAILABLE) {

            System.out.println(
                    "Book is already borrowed."
            );

            return false;
        }


        // Check borrowing limit
        if (user.getBorrowedBooks().size()
                >= BORROWING_LIMIT) {

            System.out.println(
                    "Borrowing limit reached. "
                    + "Maximum 3 books allowed."
            );

            return false;
        }


        // Borrow the book
        if (book.borrowBook()) {

            // Add book to user's borrowed list
            user.addBorrowedBook(book);

            // Create borrowing record
            BorrowRecord record =
                    new BorrowRecord(user, book);

            // Add to Java list
            borrowRecords.add(record);

            // Save to database
            BorrowRecordDAO.addBorrowRecord(record);

            System.out.println(
                    "Book borrowed successfully."
            );

            return true;
        }

        return false;
    }


    // =========================
    // RETURNING
    // =========================

    // Return a book
    public boolean returnBook(
            int userId,
            String isbn) {

        User user = searchUserById(userId);
        Book book = searchByIsbn(isbn);


        // Check if user exists
        if (user == null) {

            System.out.println("User not found.");
            return false;
        }


        // Check if book exists
        if (book == null) {

            System.out.println("Book not found.");
            return false;
        }


        // Check if user borrowed the book
        if (!user.getBorrowedBooks().contains(book)) {

            System.out.println(
                    "This user has not borrowed this book."
            );

            return false;
        }


        // Find borrowing record
        BorrowRecord record =
                findBorrowRecord(user, book);


        // Calculate late fee
        if (record != null) {

            double lateFee =
                    record.calculateLateFee();

            if (lateFee > 0) {

                System.out.println(
                        "Late fee: Rs. " + lateFee
                );

            } else {

                System.out.println(
                        "No late fee."
                );
            }

            // Remove from Java list
            borrowRecords.remove(record);

            // Delete from database
            BorrowRecordDAO.deleteBorrowRecord(
                    userId,
                    isbn
            );
        }


        // Return book
        if (book.returnBook()) {

            // Remove from user's borrowed list
            user.removeBorrowedBook(book);

            System.out.println(
                    "Book returned successfully."
            );

            return true;
        }

        return false;
    }


    // =========================
    // RENEWAL
    // =========================

    // Renew a borrowed book
    public boolean renewBook(
            int userId,
            String isbn) {

        User user = searchUserById(userId);
        Book book = searchByIsbn(isbn);


        // Check if user exists
        if (user == null) {

            System.out.println("User not found.");
            return false;
        }


        // Check if book exists
        if (book == null) {

            System.out.println("Book not found.");
            return false;
        }


        // Check if user borrowed the book
        if (!user.getBorrowedBooks().contains(book)) {

            System.out.println(
                    "This user has not borrowed this book."
            );

            return false;
        }


        // Find borrowing record
        BorrowRecord record =
                findBorrowRecord(user, book);


        if (record == null) {

            System.out.println(
                    "Borrowing record not found."
            );

            return false;
        }


        // Try to renew
        if (record.renew()) {

            // Update database
            BorrowRecordDAO.updateBorrowStatus(
                    userId,
                    isbn,
                    record.getDueDate(),
                    record.isRenewed(),
                    record.isOnHold()
            );

            System.out.println(
                    "Book renewed successfully."
            );

            System.out.println(
                    "New due date: "
                    + record.getDueDate()
            );

            return true;
        }


        // Explain why renewal failed
        if (record.isRenewed()) {

            System.out.println(
                    "Book has already been renewed once."
            );

        } else if (record.isOnHold()) {

            System.out.println(
                    "Book cannot be renewed because "
                    + "another user has placed a hold."
            );
        }

        return false;
    }


    // =========================
    // HOLD
    // =========================

    // Place a hold on a borrowed book
    public boolean placeHold(
            int userId,
            String isbn) {

        User user = searchUserById(userId);
        Book book = searchByIsbn(isbn);


        // Check if user exists
        if (user == null) {

            System.out.println("User not found.");
            return false;
        }


        // Check if book exists
        if (book == null) {

            System.out.println("Book not found.");
            return false;
        }


        // Find borrowing record
        BorrowRecord record = null;

        for (BorrowRecord r : borrowRecords) {

            if (r.getBook() == book) {

                record = r;
                break;
            }
        }


        // Check if book is currently borrowed
        if (record == null) {

            System.out.println(
                    "Book is not currently borrowed."
            );

            return false;
        }


        // Current borrower cannot place a hold
        if (record.getUser() == user) {

            System.out.println(
                    "The current borrower cannot place a hold."
            );

            return false;
        }


        // Check if hold already exists
        if (record.isOnHold()) {

            System.out.println(
                    "A hold has already been placed "
                    + "on this book."
            );

            return false;
        }


        // Place hold
        record.setOnHold(true);


        // Update database
        BorrowRecordDAO.updateBorrowStatus(
                record.getUser().getUserId(),
                record.getBook().getIsbn(),
                record.getDueDate(),
                record.isRenewed(),
                record.isOnHold()
        );


        System.out.println(
                "Hold placed successfully."
        );

        return true;
    }


    // =========================
    // BORROW RECORD SEARCH
    // =========================

    // Find borrowing record
    private BorrowRecord findBorrowRecord(
            User user,
            Book book) {

        for (BorrowRecord record : borrowRecords) {

            if (
                    record.getUser() == user
                    && record.getBook() == book
            ) {

                return record;
            }
        }

        return null;
    }


    // =========================
    // GETTERS
    // =========================

    // Get all books
    public List<Book> getBooks() {

        return books;
    }


    // Get all users
    public List<User> getUsers() {

        return users;
    }


    // Get all borrowing records
    public List<BorrowRecord> getBorrowRecords() {

        return borrowRecords;
    }
}