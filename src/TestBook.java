import model.Book;
import model.Library;
import model.User;

public class TestBook {

    public static void main(String[] args) {

        // ==========================================
        // CREATE LIBRARY
        // ==========================================

        Library library = new Library();


        // ==========================================
        // DISPLAY DATABASE DATA
        // ==========================================

        System.out.println("==========================================");
        System.out.println("       SMART LIBRARY SYSTEM");
        System.out.println("==========================================");

        System.out.println();
        System.out.println("Books loaded from database: "
                + library.getBooks().size());

        System.out.println("Users loaded from database: "
                + library.getUsers().size());


        // ==========================================
        // SEARCH BOOK
        // ==========================================

        System.out.println();
        System.out.println("=== BOOK SEARCH ===");

        Book searchedBook = library.searchByIsbn("12345");

        if (searchedBook != null) {

            System.out.println("Book found:");
            searchedBook.displayBookInfo();

        } else {

            System.out.println("Book not found.");
        }


        // ==========================================
        // SEARCH USER
        // ==========================================

        System.out.println();
        System.out.println("=== USER SEARCH ===");

        User searchedUser =
                library.searchUserById(101);

        if (searchedUser != null) {

            System.out.println("User found:");
            searchedUser.displayUserInfo();

        } else {

            System.out.println("User not found.");
        }


        // ==========================================
        // BORROW BOOK 1
        // ==========================================

        System.out.println();
        System.out.println("=== BORROWING ===");

        System.out.println();
        System.out.println("Abhiram borrows Book 12345:");

        boolean borrow1 =
                library.borrowBook(101, "12345");

        System.out.println(
                "Borrow successful: " + borrow1
        );


        // ==========================================
        // BORROW BOOK 2
        // ==========================================

        System.out.println();
        System.out.println("Abhiram borrows Book 23456:");

        boolean borrow2 =
                library.borrowBook(101, "23456");

        System.out.println(
                "Borrow successful: " + borrow2
        );


        // ==========================================
        // BORROW BOOK 3
        // ==========================================

        System.out.println();
        System.out.println("Abhiram borrows Book 34567:");

        boolean borrow3 =
                library.borrowBook(101, "34567");

        System.out.println(
                "Borrow successful: " + borrow3
        );


        // ==========================================
        // TEST BORROWING LIMIT
        // ==========================================

        System.out.println();
        System.out.println(
                "Abhiram tries to borrow Book 45678:"
        );

        boolean fourthBorrow =
                library.borrowBook(101, "45678");

        System.out.println(
                "Fourth borrow successful: "
                + fourthBorrow
        );


        // ==========================================
        // DISPLAY USER INFORMATION
        // ==========================================

        System.out.println();
        System.out.println("=== USER INFORMATION ===");

        searchedUser.displayUserInfo();


        // ==========================================
        // RETURN BOOK
        // ==========================================

        System.out.println();
        System.out.println("=== RETURNING ===");

        boolean returned =
                library.returnBook(101, "23456");

        System.out.println(
                "Return successful: " + returned
        );


        // ==========================================
        // BORROW AGAIN
        // ==========================================

        System.out.println();
        System.out.println(
                "Borrowing Book 45678 after return:"
        );

        boolean borrowedAgain =
                library.borrowBook(101, "45678");

        System.out.println(
                "Borrow successful: " + borrowedAgain
        );


        // ==========================================
        // FINAL USER INFORMATION
        // ==========================================

        System.out.println();
        System.out.println(
                "=== FINAL USER INFORMATION ==="
        );

        searchedUser.displayUserInfo();


        // ==========================================
        // FINAL BOOK INFORMATION
        // ==========================================

        System.out.println();
        System.out.println(
                "=== FINAL BOOK INFORMATION ==="
        );

        for (Book book : library.getBooks()) {

            book.displayBookInfo();

            System.out.println();
        }


        // ==========================================
        // FINAL RESULT
        // ==========================================

        System.out.println(
                "=========================================="
        );

        System.out.println(
                "       TEST COMPLETED SUCCESSFULLY"
        );

        System.out.println(
                "=========================================="
        );
    }
}