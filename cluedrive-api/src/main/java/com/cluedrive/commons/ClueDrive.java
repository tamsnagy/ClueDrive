package com.cluedrive.commons;

import com.cluedrive.exception.ClueException;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.List;

/**
 * Base class of every provider.
 * If API is extended with new provider, than it needs to extend this class.
 */
public abstract class ClueDrive implements Serializable {
    /**
     * Access token got after OAuth2.0 authorization.
     */
    protected String accessToken = null;
    /**
     * Enum for identifying different providers.
     */
    protected ClueDriveProvider provider;

    /**
     * !!!! Needs to be called after access token is set up, in case Drive is deserialized.
     * Sets up anything specially required by the actual provider.
     * Put here every code which needs to run after calling a constructor.
     */
    public abstract void initialize();

    /**
     * Return account information of the authorized account.
     * @return The account information.
     * @throws ClueException  Problem between client and cloud provider.
     */
    public abstract CAccountInfo getAccountInfo() throws ClueException;

    /**
     * Return all children resources from that path.
     * @param path The path from where the resources are returned.
     * @return List of folders and files.
     * @throws ClueException Problem between client and cloud provider.
     */
    public abstract List<CResource> list(CPath path) throws ClueException;

    /**
     * Creates a new folder under the parentFolder with the name given as parameter.
     * @param parentFolder This will be the parent on the new folder.
     * @param folderName This will be the name of the new folder.
     * @return The created folder.
     * @throws ClueException Problem between client and cloud provider.
     */
    public abstract CFolder createFolder(CFolder parentFolder, String folderName) throws ClueException;

    /**
     * Returns the root folder of the provider.
     * @return The root folder.
     * @throws ClueException Problem between client and cloud provider.
     */
    public abstract CFolder getRootFolder() throws ClueException;

    /**
     * Uploads a file from local file system under the given folder at cloud.
     * @param remoteFolder This will be the parent of the uploaded file.
     * @param localPath Path to the file which needs to be uploaded.
     * @return  The uploaded file.
     * @throws ClueException Problem between client and cloud provider.
     * @throws FileNotFoundException File was not found at the given path.
     */
    public abstract CFile uploadFile(CFolder remoteFolder, Path localPath) throws ClueException, FileNotFoundException;

    /**
     * Deletes a resource on cloud. It can be File or Folder.
     * @param resource The resource which needs to be deleted.
     * @throws ClueException Problem between client and cloud provider.
     */
    public abstract void delete(CResource resource) throws ClueException;

    /**
     * Downloads a file from cloud to the path given at local file system.
     * @param remoteFile The file on cloud.
     * @param localPath The path where file needs to be saved.
     * @return The file from cloud, which has set up the localPath too.
     * @throws ClueException Problem between client and cloud provider.
     */
    public abstract CFile downloadFile(CFile remoteFile, Path localPath) throws ClueException;

    /**
     * Starts OAuth2 flow.
     *
     * @return Url, where access can be granted to application.
     */
    public abstract String startAuth();

    /**
     * Finishes the authorization process.
     * Different classes may override this method.
     * @param accessToken The access token got from provider.
     * @throws ClueException Problem between client and cloud provider.
     */
    public void finishAuth(String accessToken) throws ClueException {
        this.accessToken = accessToken;
    }

    /**
     * Returns the access token.
     * @return the access token
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Sets the access token. Serialization purposes.
     * @param token The token to set.
     */
    public void setAccessToken(String token) {
        accessToken = token;
    }

    /**
     * Returns the provider type.
     * @return The type of the cloud provider.
     */
    public ClueDriveProvider getProvider() {
        return provider;
    }

    /**
     * Sets the cloud provider. Serialization purposes.
     * @param provider The provider to set.
     */
    public void setProvider(ClueDriveProvider provider) {
        this.provider = provider;
    }
}
