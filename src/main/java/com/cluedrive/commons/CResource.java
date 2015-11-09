package com.cluedrive.commons;

import java.nio.file.Path;

/**
 * Created by Tamas on 2015-09-30.
 */
public abstract class CResource {
    protected String id;
    protected CPath remotePath;
    protected Path localPath;

    public CResource(CPath remotePath){
        this.remotePath = remotePath;
    }

    public CResource(CPath remotePath, String id) {
        this.remotePath = remotePath;
        this.id = id;
    }

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
}
