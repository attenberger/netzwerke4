package edu.hm.cs.breakfasttothelimit.google.exception;

import java.io.IOException;

public class RequestURLException extends Exception {

	public RequestURLException(IOException e) {
		super(e);
	}

}
