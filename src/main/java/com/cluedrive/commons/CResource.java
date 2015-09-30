package com.cluedrive.commons;

/**
 * Created by Tamas on 2015-09-30.
 */
public abstract class CResource {
    protected CPath path;

    public CResource(CPath path) {
        this.path = path;
    }

    public CPath getPath() {
        return path;
    }

    public void setPath(CPath path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return path.toString();
    }
}
