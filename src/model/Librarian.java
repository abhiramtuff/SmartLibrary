package model;

public class Librarian extends User {

    // Constructor
    public Librarian(int userId, String name, String contact) {

        super(userId, name, contact);

    }

    // Add a book to the library
    public void addBook(Library library, Book book) {

        library.addBook(book);

    }

    // Remove a book from the library
    public void removeBook(Library library, Book book) {

        library.removeBook(book);

    }

    // Add a user to the library
    public void addUser(Library library, User user) {

        library.registerUser(user);

    }

    // Remove a user from the library
    public void removeUser(Library library, User user) {

        library.removeUser(user);

    }

}