package com.cluedrive.commons;

import java.io.Serializable;
import java.nio.file.Path;

/**
 * Created by Tamas on 2015-09-30.
 */
public abstract class CResource implements Serializable {
    protected String id;
    protected CPath remotePath;
    protected Path localPath;

    public CResource(CPath remotePath) {
        this.remotePath = remotePath;
    }

    public CResource(CPath remotePath, String id) {
        this.remotePath = remotePath;
        this.id = id;
    }

    public abstract boolean isFolder();

    public abstract boolean isFile();

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

    @Override
    public String toString() {
        return remotePath.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CResource cResource = (CResource) o;

        if (id != null ? !id.equals(cResource.id) : cResource.id != null) return false;
        return remotePath.equals(cResource.remotePath);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + remotePath.hashCode();
        return result;
    }
}
