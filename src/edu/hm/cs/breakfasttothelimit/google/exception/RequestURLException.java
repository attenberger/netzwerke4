package edu.hm.cs.breakfasttothelimit.google.exception;

import java.io.IOException;

/**
 * @author Benjamin Eder
 */
public class RequestURLException extends Exception {

	public RequestURLException(IOException e) {
		super(e);
	}

}
