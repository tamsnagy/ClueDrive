package com.cluedrive.application;

import com.cluedrive.commons.CFolder;
import com.cluedrive.commons.CPath;
import com.cluedrive.commons.ClueDrive;
import com.cluedrive.exception.ClueException;

import java.io.Serializable;

/**
 * Created by Tamas on 2015-11-25.
 */
public class AppDrive implements Serializable{
    private ClueDrive drive;
    private CFolder rootFolder;

    public AppDrive() {
    }

    public AppDrive(ClueDrive drive, CFolder rootFolder) {
        this.drive = drive;
        this.rootFolder = rootFolder;
    }

    public ClueDrive getDrive() {
        return drive;
    }

    public void setDrive(ClueDrive drive) {
        this.drive = drive;
    }

    public CFolder getRootFolder() {
        return rootFolder;
    }

    public void setRootFolder(CFolder rootFolder) {
        this.rootFolder = rootFolder;
    }
}
