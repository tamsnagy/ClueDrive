package com.cluedrive.drives;

import com.cluedrive.commons.*;
import com.cluedrive.exception.ClueException;
import com.cluedrive.exception.NotExistingPathException;
import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Tamas on 2015-09-24.
 */
public class DropBoxDrive implements ClueDrive {
    private DbxRequestConfig config;
    private DbxClient client;
    private String accessToken;

    public DropBoxDrive() {
        config = new DbxRequestConfig("ClueDrive", Locale.getDefault().toString());
    }

    @Override
    public List<CResource> list(CPath path) throws ClueException {
        try {
            List<CResource> entries = new ArrayList<>();
            DbxEntry.WithChildren dbxEntries = client.getMetadataWithChildren(path.toString());
            if(dbxEntries == null) {
                throw new NotExistingPathException(path);
            }
            for(DbxEntry dbxEntry: dbxEntries.children) {
                if(dbxEntry.isFolder()) {
                    entries.add(new CDirectory(CPath.create(dbxEntry.path)));
                } else if (dbxEntry.isFile()) {
                    DbxEntry.File file = dbxEntry.asFile();
                    entries.add(new CFile(CPath.create(file.path), file.numBytes, file.lastModified));
                }
            }
            return entries;
        } catch (DbxException e) {
            throw new ClueException(e);
        }
    }

    @Override
    public void setClient(String accessToken) {
        this.client = new DbxClient(config, accessToken);
    }

    @Override
    public void createFolder(CPath path) {
        try {
            DbxEntry.Folder folder =this.client.createFolder(path.toString());
        } catch (DbxException e) {
            e.printStackTrace();
        }
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}