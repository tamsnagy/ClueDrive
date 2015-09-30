package com.cluedrive.commons;

import java.util.Date;

/**
 * Created by Tamas on 2015-09-30.
 */
public class CFile extends CResource{
    private long fileSize;  // bytes
    private Date lastModified;

    public CFile(CPath path, long fileSize, Date lastModified) {
        super(path);
        this.fileSize = fileSize;
    }

    @Override
    public String toString() {
        return "CFile{" +
                "fileSize=" + fileSize +
                ", lastModified=" + lastModified +
                ", path=" + super.toString() + "} ";
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