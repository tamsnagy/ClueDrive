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
 * ClueDrive for accessing Dropbox's cloud storage.
 */
public class DropBoxDrive extends ClueDrive {
    private transient DbxRequestConfig config;
    private transient DbxClient client;
    private transient DbxWebAuthNoRedirect webAuth;

    /**
     * Creates a drive which is able to start auth flow.
     */
    public DropBoxDrive() {
        provider = ClueDriveProvider.DROPBOX;
        config = new DbxRequestConfig("Clue Drive/1.0", Locale.getDefault().toString());
        DbxAppInfo appInfo = new DbxAppInfo(PropertiesUtility.apiProperty("dropBox.appKey"), PropertiesUtility.apiProperty("dropBox.clientSecret"));
        webAuth = new DbxWebAuthNoRedirect(config, appInfo);
    }

    /**
     * Use just for unit tests.
     * @param test value is not used.
     */
    public DropBoxDrive(boolean test) {
        provider = ClueDriveProvider.DROPBOX;
        config = new DbxRequestConfig("Clue Drive/1.0", Locale.getDefault().toString());
    }

    @Override
    public String startAuth() {
        return webAuth.start();
    }

    @Override
    public void finishAuth(String accessToken) throws ClueException {
        try {
            DbxAuthFinish authFinish = webAuth.finish(accessToken);
            setAccessToken(authFinish.accessToken);
        } catch (DbxException e) {
            throw new ClueException(e);
        }
    }

    @Override
    public void initialize() {
        config = new DbxRequestConfig("ClueDrive", Locale.getDefault().toString());
        DbxAppInfo appInfo = new DbxAppInfo(PropertiesUtility.apiProperty("dropBox.appKey"), PropertiesUtility.apiProperty("dropBox.clientSecret"));
        webAuth = new DbxWebAuthNoRedirect(config, appInfo);
        client = new DbxClient(config, accessToken);
    }

    @Override
    public CAccountInfo getAccountInfo() throws ClueException {
        try {
            DbxAccountInfo accountInfo = client.getAccountInfo();
            return new CAccountInfo(accountInfo.displayName, accountInfo.quota.total);
        } catch (DbxException e) {
            throw new ClueException(e);
        }
    }

    @Override
    public List<CResource> list(CPath path) throws ClueException {
        try {
            List<CResource> entries = new ArrayList<>();
            DbxEntry.WithChildren dbxEntries = client.getMetadataWithChildren(path.toString());
            if (dbxEntries == null) {
                throw new NotExistingPathException(path);
            }
            for (DbxEntry dbxEntry : dbxEntries.children) {
                if (dbxEntry.isFolder()) {
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
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        this.client = new DbxClient(config, accessToken);
    }

    @Override
    public CFolder createFolder(CFolder parentFolder, String folderName) throws ClueException {
        try {
            CPath path = CPath.create(parentFolder.getRemotePath(), folderName);
            this.client.createFolder(path.toString()); // returns null if folder with same path already existed
            // Anyway if exception was not thrown, folder will exist with this path.
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
        try (FileInputStream inputStream = new FileInputStream(file)) {
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
    public CFile downloadFile(CFile remoteFile, Path localPath) throws ClueException {
        try (FileOutputStream outputStream = new FileOutputStream(localPath.toFile())) {
            DbxEntry.File downloadedFile = client.getFile(remoteFile.getRemotePath().toString(),
                    null, outputStream);
            CFile cFile = new CFile(CPath.create(downloadedFile.path), downloadedFile.numBytes, downloadedFile.lastModified);
            cFile.setLocalPath(localPath);
            return cFile;
        } catch (DbxException | IOException e) {
            throw new ClueException(e);
        }
    }

    public DbxClient getClient() {
        return client;
    }

    public DbxRequestConfig getConfig() {
        return config;
    }
}
