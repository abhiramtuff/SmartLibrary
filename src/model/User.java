package model;

import java.util.ArrayList;
import java.util.List;

public class User {

    private int userId;
    private String name;
    private String contact;
    private List<Book> borrowedBooks;

    // Constructor
    public User(int userId, String name, String contact) {

        this.userId = userId;
        this.name = name;
        this.contact = contact;
        this.borrowedBooks = new ArrayList<>();

    }

    // Get user ID
    public int getUserId() {
        return userId;
    }

    // Set user ID
    public void setUserId(int userId) {
        this.userId = userId;
    }

    // Get name
    public String getName() {
        return name;
    }

    // Set name
    public void setName(String name) {
        this.name = name;
    }

    // Get contact
    public String getContact() {
        return contact;
    }

    // Set contact
    public void setContact(String contact) {
        this.contact = contact;
    }

    // Get borrowed books
    public List<Book> getBorrowedBooks() {
        return borrowedBooks;
    }

    // Add a book to borrowed books
    public void addBorrowedBook(Book book) {
        borrowedBooks.add(book);
    }

    // Remove a book from borrowed books
    public void removeBorrowedBook(Book book) {
        borrowedBooks.remove(book);
    }

    // Display user information
    public void displayUserInfo() {

        System.out.println("User ID: " + userId);
        System.out.println("Name: " + name);
        System.out.println("Contact: " + contact);
        System.out.println("Number of borrowed books: " + borrowedBooks.size());

    }
}