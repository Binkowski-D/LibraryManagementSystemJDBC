package exception;

public class DatabaseOperationException extends Exception{
    public DatabaseOperationException(String message, Throwable cause){
        super(message, cause);
    }

    public DatabaseOperationException(String message){
        super(message);
    }
}
