package model;

import java.time.LocalDate;
import java.util.Objects;

public class Reader {
    private int id;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;

    public Reader(){}

    public Reader(String firstName, String lastName, LocalDate dateOfBirth){
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
    }

    public Reader(int id, String firstName, String lastName, LocalDate dateOfBirth){
        this(firstName, lastName, dateOfBirth);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public LocalDate getDateOfBirth(){
        return dateOfBirth;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Reader reader = (Reader) object;
        return Objects.equals(firstName, reader.firstName) && Objects.equals(lastName, reader.lastName) && Objects.equals(dateOfBirth, reader.dateOfBirth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, dateOfBirth);
    }

    @Override
    public String toString() {
        return "Reader{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                '}';
    }
}
