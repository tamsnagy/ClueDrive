package com.cluedrive.commons;

import com.cluedrive.exception.IllegalPathException;

import java.io.Serializable;

/**
 * Path class for cloud usages.
 * Cloud providers usually use unix like paths. This class helps the usage.
 */
public class CPath implements Serializable {
    /**
     * String representation of the path.
     */
    private String absolutePath;

    /**
     * Creates a CPath from a string. Parameter goes through validation.
     * @param absolutePath The path to create.
     * @return The CPath created.
     * @throws IllegalPathException This exception is thrown when validation fails.
     */
    public static CPath create(String absolutePath) throws IllegalPathException {
        validatePath(absolutePath);
        return new CPath(absolutePath);
    }

    /**
     * Creates a CPath with given base path and leaf.
     * @param path The base path.
     * @param leaf The files or resources name.
     * @return The CPath created.
     */
    public static CPath create(CPath path, String leaf) {
        String result;
        if (path.absolutePath.endsWith("/")) {
            result = path.absolutePath + leaf;
        } else {
            result = path.absolutePath + "/" + leaf;
        }
        return new CPath(result);
    }

    /**
     * Returns the leaf of a CPath. Leaf is the last part of the path from last '/' character.
     * @return The String representation of the leaf
     */
    public String getLeaf() {
        if (isRootPath())
            return "/";
        return absolutePath.substring(absolutePath.lastIndexOf('/') + 1);
    }

    /**
     * Returns the parent of this path. Parent is the path before the last '/' character.
     * @return Parent CPath.
     */
    public CPath getParent() {
        if (absolutePath.indexOf('/') == absolutePath.lastIndexOf('/'))
            return new CPath("/");
        return new CPath(absolutePath.substring(0, absolutePath.lastIndexOf('/')));
    }

    /**
     * Checks if this path is root or not.
     * @return True if it is root, false if it is not root.
     */
    public boolean isRootPath() {
        return "/".equals(absolutePath);
    }

    /**
     * Returns the string representation of CPath.
     * @return absolute path.
     */
    @Override
    public String toString() {
        return absolutePath;
    }

    /**
     * Two CPaths are equals if their absolutePath equals.
     * @param o Object to compare to.
     * @return True if they are equal, otherwise false.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CPath cPath = (CPath) o;

        return absolutePath.equals(cPath.absolutePath);

    }

    /**
     * Hashcode of th absolutePath.
     * @return hashcode.
     */
    @Override
    public int hashCode() {
        return absolutePath.hashCode();
    }

    /////////////////////////////////////////////////////////////////////////////
    // protected methods

    /**
     * Validates a path. Path is valid if it begins with '/' and contains just letters, numbers, space, '_', '-', '.', '(', ')', '/'.
     * And between two '/' it does not contain more than 64 characters.
     * @param path the path to be validated.
     * @throws IllegalPathException This exception is thrown if path is not valid.
     */
    protected static void validatePath(String path) throws IllegalPathException {
        if (path == null) {
            throw new IllegalPathException("Path cannot be null");
        }
        if (path.charAt(0) != '/') {
            throw new IllegalPathException("Path must begin with '/'");
        }
        String[] parts = path.split("/");
        for (int i = 1; i < parts.length; i++) {
            if (!(parts[i].matches("^\\.[ \\.a-zA-Z0-9_\\-]{1,62}$") ||
                    parts[i].matches("^[a-zA-Z0-9_][ \\.a-zA-Z0-9_\\(\\)\\-]{0,62}$"))) {
                throw new IllegalPathException("Part of remotePath contains illegal character: " + parts[i]);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // private methods

    /**
     * Constructs a CPath
     * @param absolutePath the path to set.
     */
    private CPath(String absolutePath) {
        this.absolutePath = absolutePath;
    }
}
