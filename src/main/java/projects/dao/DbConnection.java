package projects.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import projects.exception.DbException;

public class DbConnection {
    private static final String HOST = "localhost"; // Database host
    private static final int PORT = 3306; // Database port
    private static final String SCHEMA = "projects"; // Database schema name
    private static final String USER = "projects"; // Database username
    private static final String PASSWORD = "projects"; // Database password

    public static Connection getConnection() {
        // Construct the connection URI
        String uri = String.format("jdbc:mysql://%s:%d/%s?useSSL=false", HOST, PORT, SCHEMA);

        try {
            // Obtain connection from DriverManager
            Connection conn = DriverManager.getConnection(uri, USER, PASSWORD);
            String message = String.format("Database connection to schema %s established.", SCHEMA);
            System.out.println(message); // Print success message
            return conn; // Return the established connection
        } catch (SQLException e) {
            // Handle connection errors
            System.err.println("The connection to "+uri+" could not be established: \n" + e.getMessage()); // Print error message
            throw new DbException(e); // Throw custom exception with the original exception
        }
    }
}