package config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    // Method to obtain a database connection using properties from a file
    public static Connection getConnection() throws IOException, SQLException {
        Properties properties = new Properties(); // Create a Properties object to hold database configuration

        // Load properties from the 'database.properties' file
        try (InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream("database.properties")) {

            if (input == null) {
                throw new IOException("Sorry, unable to find database.properties");
            }

            // Load the properties from the file into the Properties object
            properties.load(input);
        }

        // Retrieve the database connection details from the loaded properties
        String url = properties.getProperty("db.url");
        String username = properties.getProperty("db.username");
        String password = properties.getProperty("db.password");

        // Establish a connection to the database using the retrieved details
        return DriverManager.getConnection(url, username, password);
    }
}
