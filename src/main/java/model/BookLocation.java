package model;

import java.util.Objects;

public class BookLocation {
    private int id;
    private String section;
    private int shelf;

    public BookLocation(){}

    public BookLocation(String section, int shelf){
        this.section = section;
        this.shelf = shelf;
    }

    public BookLocation(int id, String section, int shelf){
        this(section, shelf);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public int getShelf() {
        return shelf;
    }

    public void setShelf(int shelf) {
        this.shelf = shelf;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        BookLocation that = (BookLocation) object;
        return shelf == that.shelf && Objects.equals(section, that.section);
    }

    @Override
    public int hashCode() {
        return Objects.hash(section, shelf);
    }

    @Override
    public String toString() {
        return "BookLocation{" +
                "section='" + section + '\'' +
                ", shelf=" + shelf +
                '}';
    }
}
