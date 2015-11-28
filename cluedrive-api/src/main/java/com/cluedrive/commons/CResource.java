package com.cluedrive.commons;

import java.io.Serializable;
import java.nio.file.Path;

/**
 * Representation of a resource on Cloud with possibility to save its path on local file system.
 */
public abstract class CResource implements Serializable {
    /**
     * Id of the resource. Not necessarily used.
     */
    protected String id;
    /**
     * The resources path on cloud.
     */
    protected CPath remotePath;
    /**
     * The resources path on local device.
     */
    protected Path localPath;

    /**
     * Creates CResource with given remote path.
     * @param remotePath remote path of resource.
     */
    public CResource(CPath remotePath) {
        this.remotePath = remotePath;
    }

    /**
     * Creates resource with given remote path and id.
     * @param remotePath remote path of resource.
     * @param id id of resource.
     */
    public CResource(CPath remotePath, String id) {
        this.remotePath = remotePath;
        this.id = id;
    }

    /**
     * Checks if resource is folder.
     * @return True if resource is folder, otherwise false.
     */
    public abstract boolean isFolder();

    /**
     * Checks if resource is file.
     * @return True if resource is file, otherwise false.
     */
    public abstract boolean isFile();

    /**
     * debug purposes.
     * @return the remote paths string representation.
     */
    @Override
    public String toString() {
        return remotePath.toString();
    }

    /**
     * Two resource is equal if their remote path, local path, and id are equal.
     * @param o the object to be compared with.
     * @return True if equals, otherwise false.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CResource cResource = (CResource) o;

        if (id != null ? !id.equals(cResource.id) : cResource.id != null) return false;
        if (remotePath != null ? !remotePath.equals(cResource.remotePath) : cResource.remotePath != null) return false;
        return !(localPath != null ? !localPath.equals(cResource.localPath) : cResource.localPath != null);

    }

    /**
     * Usual hashcode implementation.
     * @return hashcode.
     */
    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (remotePath != null ? remotePath.hashCode() : 0);
        result = 31 * result + (localPath != null ? localPath.hashCode() : 0);
        return result;
    }

    //////////////////////////////////////////////////
    // getters and setters

    public CPath getRemotePath() {
        return remotePath;
    }

    public void setRemotePath(CPath remotePath) {
        this.remotePath = remotePath;
    }

    public Path getLocalPath() {
        return localPath;
    }

    public void setLocalPath(Path localPath) {
        this.localPath = localPath;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return remotePath.getLeaf();
    }
}
