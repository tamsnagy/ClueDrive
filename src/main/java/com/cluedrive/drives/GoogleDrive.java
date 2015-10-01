package com.cluedrive.drives;

import com.cluedrive.commons.CPath;
import com.cluedrive.commons.CResource;
import com.cluedrive.commons.ClueDrive;
import com.cluedrive.exception.ClueException;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.util.List;

/**
 * Created by Tamas on 2015-10-01.
 */
public class GoogleDrive implements ClueDrive {
    private Drive client;

    public GoogleDrive(Drive client) {
        this.client = client;
    }

    @Override
    public List<CResource> list(CPath path) throws ClueException {
        FileList result = null;
        try {
            result = client.files().list()
                    .setMaxResults(10)
                    .execute();
        } catch (IOException e) {
            throw new ClueException(e);
        }
        List<File> files = result.getItems();
        if (files == null || files.size() == 0) {
            System.out.println("No files found.");
        } else {
            System.out.println("Files:");
            for (File file : files) {
                System.out.printf("%s (%s)\n", file.getTitle(), file.getId());
            }
        }
        return null;
    }

    @Override
    public void setClient(String accessToken) {

    }

    @Override
    public void createFolder(CPath path) {

    }
}
