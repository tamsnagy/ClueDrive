package com.cluedrive.exception;

/**
 * This exception is thrown when authorization to provider expired.
 */
public class UnAuthorizedException extends ClueException {
    public UnAuthorizedException(String e) {
        super(e);
    }

    public UnAuthorizedException(Exception e){
        super(e);
    }
}
