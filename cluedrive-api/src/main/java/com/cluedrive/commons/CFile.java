package com.cluedrive.commons;

import java.util.Date;

/**
 * Class representation of a file.
 */
public class CFile extends CResource {
    /**
     * Size of the file in bytes.
     */
    private long fileSize;
    /**
     * Date when file was last modified on cloud.
     */
    private Date lastModified;

    /**
     * Creates a CFile with given remote path, fileSize and last modified date.
     * @param remotePath remote path to set.
     * @param fileSize file size to set in bytes.
     * @param lastModified last modified date to set.
     */
    public CFile(CPath remotePath, long fileSize, Date lastModified) {
        super(remotePath);
        this.fileSize = fileSize;
        this.lastModified = lastModified;
    }

    /**
     * Creates a CFile with given remote path, id, fileSize and last modified date.
     * @param remotePath remote path to set.
     * @param id id to set.
     * @param fileSize file size to set in bytes.
     * @param lastModified last modified date to set.
     */
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

    ///////////////////////////////////////////////////////////////////
    // getters and setters

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
