package com.cluedrive.commons;

import com.cluedrive.exception.ClueException;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.List;

/**
 * Created by Tamas on 2015-09-24.
 */
public interface ClueDrive {
    List<CResource> list(CPath path) throws ClueException;
    void setClient(String accessToken);
    CFolder createFolder(CFolder parentFolder, String folderName) throws ClueException;
    CFolder getRootFolder() throws ClueException;
    CFile uploadFile(CFolder remoteFolder, Path localPath) throws ClueException, FileNotFoundException;
    void delete(CResource resource) throws ClueException;
    CFile downloadFile(CFile remoteFile, Path localPath) throws ClueException;

}
