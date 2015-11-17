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

    public abstract List<CResource> list(CPath path) throws ClueException;
    public void setToken(String token) {
        accessToken = token;
    }
    public String getToken() {
        return accessToken;
    }
    public abstract CFolder createFolder(CFolder parentFolder, String folderName) throws ClueException;
    public abstract CFolder getRootFolder() throws ClueException;
    public abstract CFile uploadFile(CFolder remoteFolder, Path localPath) throws ClueException, FileNotFoundException;
    public abstract void delete(CResource resource) throws ClueException;
    public abstract CFile downloadFile(CFile remoteFile, Path localPath) throws ClueException;


}
