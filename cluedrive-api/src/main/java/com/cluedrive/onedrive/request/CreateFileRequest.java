package com.cluedrive.onedrive.request;

/**
 * Created by Tamas on 2015-11-13.
 */
public class CreateFileRequest {
    private String name;
    private FileRequest file = new FileRequest();

    public CreateFileRequest() {
    }

    public CreateFileRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FileRequest getFile() {
        return file;
    }

    public void setFile(FileRequest file) {
        this.file = file;
    }
}
