package com.cluedrive.commons;

/**
 * Class representation of a folder.
 */
public class CFolder extends CResource {

    /**
     * Creates a CFolder with given remote path.
     * @param remotePath remote path to set.
     */
    public CFolder(CPath remotePath) {
        super(remotePath);
    }

    /**
     * Creates CFolder with given remote path and id.
     * @param remotePath remote path to set.
     * @param id id to set.
     */
    public CFolder(CPath remotePath, String id) {
        super(remotePath, id);
    }

    @Override
    public boolean isFolder() {
        return true;
    }

    @Override
    public boolean isFile() {
        return false;
    }

    @Override
    public String toString() {
        return "CFolder{remotePath=" + super.toString() + "} ";
    }
}
