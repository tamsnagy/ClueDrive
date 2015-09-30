package com.cluedrive.commons;

/**
 * Created by Tamas on 2015-09-30.
 */
public class CDirectory extends CResource {

    public CDirectory(CPath path) {
        super(path);
    }

    @Override
    public String toString() {
        return "CDirectory{path=" + super.toString() + "} ";
    }
}
