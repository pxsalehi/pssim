package de.tum.msrg.underlay;

public class UnderlayException extends Exception {

    private static final long serialVersionUID = 1L;

    public UnderlayException() {
        super();
    }

    public UnderlayException(String message) {
        super("Underlay: " + message);
    }

    public UnderlayException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnderlayException(Throwable cause) {
        super(cause);
    }

}
