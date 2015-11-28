package com.cluedrive.exception;

/**
 * This exception is thrown only when something really bad happened at cloud provider.
 */
public class InternalErrorException extends ClueException {
    public InternalErrorException(String s) {
        super(s);
    }
}
