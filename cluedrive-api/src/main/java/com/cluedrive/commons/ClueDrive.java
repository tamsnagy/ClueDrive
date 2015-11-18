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



    public abstract void initialize();
    public abstract List<CResource> list(CPath path) throws ClueException;
    public abstract CFolder createFolder(CFolder parentFolder, String folderName) throws ClueException;
    public abstract CFolder getRootFolder() throws ClueException;
    public abstract CFile uploadFile(CFolder remoteFolder, Path localPath) throws ClueException, FileNotFoundException;
    public abstract void delete(CResource resource) throws ClueException;
    public abstract CFile downloadFile(CFile remoteFile, Path localPath) throws ClueException;

    /**
     * Starts OAuth2 flow.
     * @return Url, where access can be granted to application.
     */
    public abstract String startAuth();

    public void finishAuth(String accessToken) throws ClueException {
        this.accessToken = accessToken;
    }

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
