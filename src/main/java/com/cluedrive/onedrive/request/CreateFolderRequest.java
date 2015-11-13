package com.cluedrive.onedrive.request;

/**
 * Created by Tamas on 2015-11-11.
 */
public class CreateFolderRequest {
    private String name;
    private FolderRequest folder = new FolderRequest();

    public CreateFolderRequest() {
    }

    public CreateFolderRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FolderRequest getFolder() {
        return folder;
    }

    public void setFolder(FolderRequest folder) {
        this.folder = folder;
    }
}
