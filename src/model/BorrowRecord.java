package model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class BorrowRecord {

    private User user;
    private Book book;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private boolean renewed;
    private boolean onHold;

    // Borrowing period
    private static final int BORROWING_DAYS = 14;

    // Renewal extension
    private static final int RENEWAL_DAYS = 7;

    // Late fee per day
    private static final double LATE_FEE_PER_DAY = 10.0;


    // =========================
    // CONSTRUCTOR
    // =========================

    public BorrowRecord(User user, Book book) {

        this.user = user;
        this.book = book;

        this.borrowDate = LocalDate.now();
        this.dueDate = borrowDate.plusDays(BORROWING_DAYS);

        this.renewed = false;
        this.onHold = false;
    }


    // =========================
    // CONSTRUCTOR FOR TESTING
    // =========================

    public BorrowRecord(
            User user,
            Book book,
            LocalDate borrowDate) {

        this.user = user;
        this.book = book;

        this.borrowDate = borrowDate;
        this.dueDate = borrowDate.plusDays(BORROWING_DAYS);

        this.renewed = false;
        this.onHold = false;
    }


    // =========================
    // GET USER
    // =========================

    public User getUser() {
        return user;
    }


    // =========================
    // GET BOOK
    // =========================

    public Book getBook() {
        return book;
    }


    // =========================
    // GET BORROW DATE
    // =========================

    public LocalDate getBorrowDate() {
        return borrowDate;
    }


    // =========================
    // GET DUE DATE
    // =========================

    public LocalDate getDueDate() {
        return dueDate;
    }


    // =========================
    // SET DUE DATE
    // =========================

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }


    // =========================
    // CHECK RENEWED
    // =========================

    public boolean isRenewed() {
        return renewed;
    }


    // =========================
    // SET RENEWED
    // =========================

    public void setRenewed(boolean renewed) {
        this.renewed = renewed;
    }


    // =========================
    // CHECK HOLD
    // =========================

    public boolean isOnHold() {
        return onHold;
    }


    // =========================
    // SET HOLD
    // =========================

    public void setOnHold(boolean onHold) {
        this.onHold = onHold;
    }


    // =========================
    // RENEW BOOK
    // =========================

    public boolean renew() {

        // Book can only be renewed once
        if (renewed) {
            return false;
        }

        // Cannot renew if another user has placed a hold
        if (onHold) {
            return false;
        }

        // Extend due date by 7 days
        dueDate = dueDate.plusDays(RENEWAL_DAYS);

        renewed = true;

        return true;
    }


    // =========================
    // CALCULATE LATE DAYS
    // =========================

    public long getLateDays() {

        LocalDate today = LocalDate.now();

        if (today.isAfter(dueDate)) {

            return ChronoUnit.DAYS.between(
                    dueDate,
                    today
            );
        }

        return 0;
    }


    // =========================
    // CALCULATE LATE FEE
    // =========================

    public double calculateLateFee() {

        long lateDays = getLateDays();

        return lateDays * LATE_FEE_PER_DAY;
    }


    // =========================
    // DISPLAY RECORD
    // =========================

    public void displayRecord() {

        System.out.println(
                "Book: " + book.getTitle()
        );

        System.out.println(
                "Borrowed by: " + user.getName()
        );

        System.out.println(
                "Borrow date: " + borrowDate
        );

        System.out.println(
                "Due date: " + dueDate
        );

        System.out.println(
                "Renewed: " + renewed
        );

        System.out.println(
                "On hold: " + onHold
        );

        System.out.println(
                "Late days: " + getLateDays()
        );

        System.out.println(
                "Late fee: Rs. " + calculateLateFee()
        );
    }
}