package com.cluedrive.application;

import com.cluedrive.commons.CAccountInfo;
import com.cluedrive.commons.CFolder;
import com.cluedrive.commons.ClueDrive;

import java.io.Serializable;

/**
 * Wrapper for ClueDriveAPIs ClueDrive.
 * Holds the data of account information, and a given rootFolder shown to Application which is placed at the actual clouds root folder.
 */
public class AppDrive implements Serializable {
    /**
     * The wrapped drive.
     */
    private ClueDrive drive;
    /**
     * Root folder shown to application.
     */
    private CFolder rootFolder;
    /**
     * Account information for the registered drive.
     */
    private transient CAccountInfo accountInfo;

    /**
     * For serialization purposes.
     */
    public AppDrive() {
    }

    /**
     * Use this constructor to create an AppDrive.
     * @param drive wrapped drive
     * @param rootFolder Actual folder shown as root towards application model.
     */
    public AppDrive(ClueDrive drive, CFolder rootFolder) {
        this.drive = drive;
        this.rootFolder = rootFolder;
    }

    /**
     * Returns wrapped drive.
     * @return wrapped drive.
     */
    public ClueDrive getDrive() {
        return drive;
    }

    /**
     * Sets wrapped drive. Serialization purposes.
     * @param drive The drive to wrap.
     */
    public void setDrive(ClueDrive drive) {
        this.drive = drive;
    }

    /**
     * Returns the root folder of wrapped drive.
     * @return root folder shown towards application model.
     */
    public CFolder getRootFolder() {
        return rootFolder;
    }

    /**
     * Sets the root folder. Serialization purposes.
     * @param rootFolder the folder to set as root.
     */
    public void setRootFolder(CFolder rootFolder) {
        this.rootFolder = rootFolder;
    }

    /**
     * Returns AccountInfo of wrapped drive.
     * @return accountInfo.
     */
    public CAccountInfo getAccountInfo() {
        return accountInfo;
    }

    /**
     * Sets account info. Serialization purposes.
     * @param accountInfo Account info to be set.
     */
    public void setAccountInfo(CAccountInfo accountInfo) {
        this.accountInfo = accountInfo;
    }
}
