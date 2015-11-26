package com.cluedrive.commons;

import com.cluedrive.exception.IllegalPathException;

import java.io.Serializable;

/**
 * Created by Tamas on 2015-09-30.
 */
public class CPath implements Serializable {
    private String absolutePath;

    private CPath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    public static CPath create(String absolutePath) throws IllegalPathException {
        validatePath(absolutePath);
        return new CPath(absolutePath);
    }

    public static CPath create(CPath path, String leaf) {
        String result;
        if (path.absolutePath.endsWith("/")) {
            result = path.absolutePath + leaf;
        } else {
            result = path.absolutePath + "/" + leaf;
        }
        return new CPath(result);
    }

    protected static void validatePath(String path) throws IllegalPathException {
        if (path == null) {
            throw new IllegalPathException("Path cannot be null");
        }
        if (path.charAt(0) != '/') {
            throw new IllegalPathException("Path must begin with '/'");
        }
        String[] parts = path.split("/");
        for (int i = 1; i < parts.length; i++) {
            if (!(parts[i].matches("^\\.[ \\.a-zA-Z0-9_-]{1,62}$") ||
                    parts[i].matches("^[a-zA-Z0-9_][ \\.a-zA-Z0-9_\\(\\)\\-]{0,62}$"))) {
                throw new IllegalPathException("Part of remotePath contains illegal character: " + parts[i]);
            }
        }
    }

    public String getLeaf() {
        if (isRootPath())
            return "/";
        return absolutePath.substring(absolutePath.lastIndexOf('/') + 1);
    }

    public CPath getParent() {
        if (absolutePath.indexOf('/') == absolutePath.lastIndexOf('/'))
            return new CPath("/");
        return new CPath(absolutePath.substring(0, absolutePath.lastIndexOf('/')));
    }

    public boolean isRootPath() {
        return "/".equals(absolutePath);
    }

    @Override
    public String toString() {
        return absolutePath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CPath cPath = (CPath) o;

        return absolutePath.equals(cPath.absolutePath);

    }

    @Override
    public int hashCode() {
        return absolutePath.hashCode();
    }
}
