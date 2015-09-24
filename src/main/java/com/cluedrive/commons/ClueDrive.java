package com.cluedrive.commons;

import com.cluedrive.exception.ClueException;

import java.util.List;

/**
 * Created by Tamas on 2015-09-24.
 */
public interface ClueDrive {
    List<String> list(String path) throws ClueException;
    void setClient(String accessToken);
    void createFolder(String path);

}
