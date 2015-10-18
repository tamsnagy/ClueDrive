package com.cluedrive.commons;

/**
 * Created by Tamas on 2015-09-30.
 */
public class CFolder extends CResource {

    public CFolder(CPath remotePath) {
        super(remotePath);
    }

    public CFolder(CPath remotePath, String id) {
        super(remotePath, id);
    }

    @Override
    public String toString() {
        return "CFolder{remotePath=" + super.toString() + "} ";
    }
}
