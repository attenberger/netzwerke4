package edu.hm.cs.breakfasttothelimit.huebridge;

public class HueBridgeException extends Exception {

	private static final long serialVersionUID = 1L;

	public HueBridgeException() {
		super();
	}

	public HueBridgeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public HueBridgeException(String message, Throwable cause) {
		super(message, cause);
	}

	public HueBridgeException(String message) {
		super(message);
	}

	public HueBridgeException(Throwable cause) {
		super(cause);
	}

	
}
