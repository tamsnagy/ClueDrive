package com.cluedrive.commons;

import java.util.Date;

/**
 * Created by Tamas on 2015-09-30.
 */
public class CFile extends CResource {
    private long fileSize;  // bytes
    private Date lastModified;

    public CFile(CPath remotePath, long fileSize, Date lastModified) {
        super(remotePath);
        this.fileSize = fileSize;
        this.lastModified = lastModified;
    }

    public CFile(CPath remotePath, String id, long fileSize, Date lastModified) {
        super(remotePath, id);
        this.fileSize = fileSize;
        this.lastModified = lastModified;
    }

    @Override
    public boolean isFolder() {
        return false;
    }

    @Override
    public boolean isFile() {
        return true;
    }

    @Override
    public String toString() {
        return "CFile{" +
                "fileSize=" + fileSize +
                ", lastModified=" + lastModified +
                ", remotePath=" + super.toString() + "} ";
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }
}
