package com.cluedrive.onedrive.response;

/**
 * Created by Tamas on 2015-11-11.
 */
public class CreateFolderResponse {
    private String id;
    private String name;
    private Folder folder;

    public CreateFolderResponse() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Folder getFolder() {
        return folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }
}
