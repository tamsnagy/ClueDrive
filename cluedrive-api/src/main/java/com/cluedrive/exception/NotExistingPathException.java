package com.cluedrive.exception;

import com.cluedrive.commons.CPath;

/**
 * Created by Tamas on 2015-09-30.
 */
public class NotExistingPathException extends ClueException {
    public NotExistingPathException(CPath path) {
        super("Required remotePath does not exists on cloud provider: " + path.toString());
    }
}
