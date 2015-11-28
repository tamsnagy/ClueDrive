package com.cluedrive.exception;

import com.cluedrive.commons.CPath;

/**
 * This exception is thrown when asked path does not exist on cloud provider.
 */
public class NotExistingPathException extends ClueException {
    public NotExistingPathException(CPath path) {
        super("Required remotePath does not exists on cloud provider: " + path.toString());
    }
}
