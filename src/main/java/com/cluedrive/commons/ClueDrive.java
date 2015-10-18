package com.cluedrive.commons;

import com.cluedrive.exception.ClueException;

import java.util.List;

/**
 * Created by Tamas on 2015-09-24.
 */
public interface ClueDrive {
    List<CResource> list(CPath path) throws ClueException;
    void setClient(String accessToken);
    CFolder createFolder(CFolder parentFolder, String folderName) throws ClueException;
    CFolder getRootFolder() throws ClueException;

}
