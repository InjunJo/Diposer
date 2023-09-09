package team.moebius.disposer.exception;

public class NotFoundTokenException extends RuntimeException{

    public NotFoundTokenException() {
    }

    public NotFoundTokenException(String message) {
        super(message);
    }
}
