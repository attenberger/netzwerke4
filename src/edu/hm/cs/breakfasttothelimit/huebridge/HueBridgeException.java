package edu.hm.cs.breakfasttothelimit.huebridge;

/**
 * To be the thrown if an error occurs in the communication with the Hue Bridge.
 * @author Attenberger
 */
public class HueBridgeException extends Exception {

	private static final long serialVersionUID = 1L;

	public HueBridgeException() {
	}

	public HueBridgeException(String message) {
		super(message);
	}

	public HueBridgeException(Throwable cause) {
		super(cause);
	}

	public HueBridgeException(String message, Throwable cause) {
		super(message, cause);
	}

	public HueBridgeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
