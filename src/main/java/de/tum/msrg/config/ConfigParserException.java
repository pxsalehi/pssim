package de.tum.msrg.config;

public class ConfigParserException extends Exception {

	private static final long serialVersionUID = 1L;

    public ConfigParserException() {
        super();
    }

    public ConfigParserException(String message) {
        super(message);
    }

    public ConfigParserException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigParserException(Throwable cause) {
        super(cause);
    }
}
