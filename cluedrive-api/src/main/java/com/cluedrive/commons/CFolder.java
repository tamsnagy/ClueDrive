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
    public boolean isFolder() {
        return true;
    }

    @Override
    public boolean isFile() {
        return false;
    }

    @Override
    public String toString() {
        return "CFolder{remotePath=" + super.toString() + "} ";
    }
}
