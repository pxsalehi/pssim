package de.tum.msrg.overlay;

public class RuntimeSimException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public RuntimeSimException() {
		super();
	}

	public RuntimeSimException(String message) {
		super(message);
	}

	public RuntimeSimException(String message, Throwable cause) {
		super(message, cause);
	}

	public RuntimeSimException(Throwable cause) {
		super(cause);
	}

}
