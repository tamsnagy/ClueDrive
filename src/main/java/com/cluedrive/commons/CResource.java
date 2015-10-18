package com.cluedrive.commons;

/**
 * Created by Tamas on 2015-09-30.
 */
public abstract class CResource {
    protected String id;
    protected CPath remotePath;
    protected CPath localPath;

    public CResource(CPath remotePath) {
        this.remotePath = remotePath;
    }

    public CPath getRemotePath() {
        return remotePath;
    }

    public void setRemotePath(CPath remotePath) {
        this.remotePath = remotePath;
    }

    public CPath getLocalPath() {
        return localPath;
    }

    public void setLocalPath(CPath localPath) {
        this.localPath = localPath;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return remotePath.toString();
    }
}
