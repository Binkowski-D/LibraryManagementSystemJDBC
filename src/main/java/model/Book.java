package model;

import java.util.Objects;

public class Book {
    private int id;
    private String title;
    private String author;
    private int yearOfPublication;
    private int quantity;
    private BookLocation location;

    public Book() {}

    public Book(String title, String author, int yearOfPublication) {
        this.title = title;
        this.author = author;
        this.yearOfPublication = yearOfPublication;
    }

    public Book(String title, String author, int yearOfPublication, int quantity, BookLocation location) {
        this(title, author, yearOfPublication);
        this.quantity = quantity;
        this.location = location;
    }

    public Book(int id, String title, String author, int yearOfPublication, int quantity, BookLocation location) {
        this(title, author, yearOfPublication, quantity, location);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public int getYearOfPublication() {
        return yearOfPublication;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BookLocation getLocation() {
        return location;
    }

    public void setLocation(BookLocation location) {
        this.location = location;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Book book = (Book) object;
        return yearOfPublication == book.yearOfPublication && Objects.equals(title, book.title) && Objects.equals(author, book.author);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, author, yearOfPublication);
    }

    @Override
    public String toString() {
        return "Book{" +
                "title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", yearOfPublication=" + yearOfPublication +
                ", location=" + location +
                '}';
    }
}
