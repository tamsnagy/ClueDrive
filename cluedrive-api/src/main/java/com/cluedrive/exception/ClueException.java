package com.cluedrive.exception;

/**
 * Base of all exceptions that can be thrown by API.
 */
public class ClueException extends Exception {
    public ClueException(Exception e) {
        super(e);
    }

    public ClueException(String e) {
        super(e);
    }
}
