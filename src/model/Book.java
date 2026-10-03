package model;

public class Book {

    private String title;
    private String author;
    private String isbn;
    private String genre;
    private BookStatus status;

    public Book(String title, String author, String isbn, String genre) {

        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.genre = genre;
        this.status = BookStatus.AVAILABLE;

    }

    public void displayBookInfo() {

        System.out.println("Title: " + title);
        System.out.println("Author: " + author);
        System.out.println("ISBN: " + isbn);
        System.out.println("Genre: " + genre);
        System.out.println("Status: " + status);

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public BookStatus getStatus() {
        return status;
    }

    public void setStatus(BookStatus status) {
        this.status = status;
    }

    public boolean borrowBook() {

        if (status == BookStatus.AVAILABLE) {

            status = BookStatus.BORROWED;
            return true;

        }

        return false;
    }

    public boolean returnBook() {

        if (status == BookStatus.BORROWED) {

            status = BookStatus.AVAILABLE;
            return true;

        }

        return false;
    }

}