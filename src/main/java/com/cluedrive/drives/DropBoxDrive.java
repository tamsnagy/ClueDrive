package com.cluedrive.drives;

import com.cluedrive.commons.*;
import com.cluedrive.exception.ClueException;
import com.cluedrive.exception.NotExistingPathException;
import com.dropbox.core.*;

import java.io.*;
import java.nio.file.Path;
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
                    entries.add(new CFolder(CPath.create(dbxEntry.path)));
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
    public CFolder createFolder(CFolder parentFolder, String folderName) throws ClueException {
        try {
            CPath path = CPath.create(parentFolder.getRemotePath(), folderName);
            DbxEntry.Folder folder = this.client.createFolder(path.toString());
            return new CFolder(path);
        } catch (DbxException e) {
            throw new ClueException(e);
        }
    }

    @Override
    public CFolder getRootFolder() throws ClueException {
        try {
            String rootFolder = "/";
            DbxEntry root = client.getMetadata(rootFolder);
            return new CFolder(CPath.create(root.path));
        } catch (DbxException e) {
            throw new ClueException(e);
        }
    }

    @Override
    public CFile uploadFile(CFolder remoteParent, Path localPath) throws ClueException, FileNotFoundException {
        CPath remoteTarget = CPath.create(remoteParent.getRemotePath(), localPath.getFileName().toString());
        File file = localPath.toFile();
        try(FileInputStream inputStream = new FileInputStream(file)) {
            DbxEntry.File uploadedFile = client.uploadFile(remoteTarget.toString(),
                    DbxWriteMode.add(), file.length(), inputStream);
            CFile cFile = new CFile(CPath.create(uploadedFile.path), uploadedFile.numBytes, uploadedFile.lastModified);
            cFile.setLocalPath(localPath);
            return cFile;
        } catch (DbxException | IOException e) {
            throw new ClueException(e);
        }
    }

    @Override
    public void delete(CResource resource) throws ClueException {
        try {
            client.delete(resource.getRemotePath().toString());
        } catch (DbxException e) {
            throw new ClueException(e);
        }
    }

    @Override
    public CFile downloadFile(CPath remotePath, Path localPath) throws ClueException {
        try(FileOutputStream outputStream = new FileOutputStream(localPath.toFile())) {
            DbxEntry.File downloadedFile = client.getFile(remotePath.toString(),
                    null, outputStream);
            CFile cFile = new CFile(CPath.create(downloadedFile.path), downloadedFile.numBytes, downloadedFile.lastModified);
            cFile.setLocalPath(localPath);
            return cFile;
        } catch (DbxException| IOException e) {
            throw new ClueException(e);
        }
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
