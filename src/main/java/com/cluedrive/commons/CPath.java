package com.cluedrive.commons;

import com.cluedrive.exception.IllegalPathException;

/**
 * Created by Tamas on 2015-09-30.
 */
public class CPath {
    private String absolutePath;

    private CPath(String absolutePath){
        this.absolutePath = absolutePath;
    }

    public static CPath create(String absolutePath) throws IllegalPathException {
        validatePath(absolutePath);
        return new CPath(absolutePath);
    }

    public static CPath create(CPath path, String leaf) throws IllegalPathException {
        String result = path.absolutePath + "/" + leaf;
        validatePath(result);
        return new CPath(result);
    }

    @Override
    public String toString() {
        return absolutePath;
    }

    protected static void validatePath(String path) throws IllegalPathException {
        if(path.charAt(0) != '/') {
            throw new IllegalPathException("Path must begin with '/'");
        }
        String[] parts = path.split("/");
        for(int i = 1; i < parts.length; i++) {
            if(! (parts[i].matches("^\\.[ \\.a-zA-Z0-9_-]{1,62}$") ||
                    parts[i].matches("^[a-zA-Z0-9_][ \\.a-zA-Z0-9_-]{0,62}$"))) {
                throw new IllegalPathException("Part of path contains illegal character: " + parts[i]);
            }
        }
    }
}
