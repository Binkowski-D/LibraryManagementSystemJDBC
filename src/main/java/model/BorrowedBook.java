package model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class BorrowedBook {
    public static final int BORROW_PERIOD_DAYS = 28;

    private int id;
    private int readerID;
    private int bookID;
    private LocalDate borrowDate;
    private LocalDate returnDueDate;

    public BorrowedBook(LocalDate borrowDate){
        this.borrowDate = borrowDate;
        this.returnDueDate = borrowDate.plusDays(BORROW_PERIOD_DAYS);
    }

    public BorrowedBook(int readerID, int bookID, LocalDate borrowDate){
        this(borrowDate);
        this.readerID = readerID;
        this.bookID = bookID;
    }

    public BorrowedBook(int id, int readerID, int bookID, LocalDate borrowDate){
        this(readerID, bookID, borrowDate);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public int getReaderID() {
        return readerID;
    }

    public int getBookID() {
        return bookID;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public LocalDate getReturnDueDate() {
        return returnDueDate;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        BorrowedBook that = (BorrowedBook) object;
        return readerID == that.readerID && bookID == that.bookID && Objects.equals(borrowDate, that.borrowDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(readerID, bookID, borrowDate);
    }

    @Override
    public String toString() {
        return "BorrowedBook{" +
                "readerID=" + readerID +
                ", bookID=" + bookID +
                ", borrowDate=" + borrowDate +
                ", returnDueDate=" + returnDueDate +
                '}';
    }
}
