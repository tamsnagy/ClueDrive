package com.cluedrive.commons;

import com.cluedrive.exception.ClueException;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.List;

/**
 * Created by Tamas on 2015-09-24.
 */
public abstract class ClueDrive implements Serializable {
    protected String accessToken = null;
    protected ClueDriveProvider provider;

    public abstract List<CResource> list(CPath path) throws ClueException;

    public abstract CFolder createFolder(CFolder parentFolder, String folderName) throws ClueException;
    public abstract CFolder getRootFolder() throws ClueException;
    public abstract CFile uploadFile(CFolder remoteFolder, Path localPath) throws ClueException, FileNotFoundException;
    public abstract void delete(CResource resource) throws ClueException;
    public abstract CFile downloadFile(CFile remoteFile, Path localPath) throws ClueException;

    public void setAccessToken(String token) {
        accessToken = token;
    }
    public String getAccessToken() {
        return accessToken;
    }

    public ClueDriveProvider getProvider() {
        return provider;
    }

    public void setProvider(ClueDriveProvider provider) {
        this.provider = provider;
    }
}
