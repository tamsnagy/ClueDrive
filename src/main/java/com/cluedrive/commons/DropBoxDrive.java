package com.cluedrive.commons;

import com.cluedrive.exception.ClueException;
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
    public List<String> list(String path) throws ClueException {
        try {
            List<String> entries = new ArrayList<>();
            client.getMetadataWithChildren(path).children.forEach((dbxEntry -> entries.add(dbxEntry.path)));
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
    public void createFolder(String path) {
        try {
            DbxEntry.Folder folder =this.client.createFolder(path);
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
