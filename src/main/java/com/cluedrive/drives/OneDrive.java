package com.cluedrive.drives;

import com.cluedrive.commons.CPath;
import com.cluedrive.commons.CResource;
import com.cluedrive.commons.ClueDrive;
import com.cluedrive.exception.ClueException;

import java.util.List;

/**
 * Created by Tamas on 2015-10-01.
 */
public class OneDrive implements ClueDrive {
    @Override
    public List<CResource> list(CPath path) throws ClueException {
        return null;
    }

    @Override
    public void setClient(String accessToken) {

    }

    @Override
    public void createFolder(CPath path) {

    }
}
