package com.cluedrive.commons;

/**
 * Created by Tamas on 2015-09-30.
 */
public class CDirectory extends CResource {

    public CDirectory(CPath remotePath) {
        super(remotePath);
    }

    @Override
    public String toString() {
        return "CDirectory{remotePath=" + super.toString() + "} ";
    }
}
