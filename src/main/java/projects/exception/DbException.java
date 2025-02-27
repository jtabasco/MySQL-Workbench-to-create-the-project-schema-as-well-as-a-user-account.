package projects.exception;

// Suppress warnings for serialization
@SuppressWarnings("serial")
public class DbException extends RuntimeException {

    // Constructor with a message
    public DbException(String message) {
        super(message); // Call the superclass constructor with the message
    }

    // Constructor with a cause (e.g., wrapped SQLException)
    public DbException(Throwable cause) {
        super(cause); // Call the superclass constructor with the cause
    }

    // Constructor with both message and cause
    public DbException(String message, Throwable cause) {
        super(message, cause); // Call the superclass constructor with both message and cause
    }
}