package com.cluedrive.commons;

/**
 * Created by Tamas on 2015-11-11.
 */
public class URLUtility {
    public final String URI_BASE;
    private StringBuilder urlBuilder;
    private boolean optionAdded;
    private boolean pathAdded;
    private String accessToken;

    public URLUtility(String UriBase, String accessToken) {
        this.URI_BASE = UriBase;
        this.accessToken = accessToken;
        this.optionAdded = false;
        this.pathAdded = false;
    }

    public URLUtility base() {
        urlBuilder = new StringBuilder(URI_BASE);
        optionAdded = false;
        pathAdded = false;
        return this;
    }

    public URLUtility route(String route) {
        if(optionAdded) {
            return null;
        }
        if(pathAdded) {
            urlBuilder.append(":");
        }
        urlBuilder.append("/").append(route);
        return this;
    }

    public URLUtility segment(CPath path) {
        if(optionAdded) {
            return null;
        }
        pathAdded = true;
        urlBuilder.append(":/").append(path.toString());
        return this;
    }

    public  URLUtility segment(CPath parentPath, String name) {
        if(optionAdded) {
            return null;
        }
        urlBuilder.append(":/").append(parentPath.toString()).append("/").append(name);
        return this;
    }

    public URLUtility query(String parameter, String value) {
        if(optionAdded) {
            urlBuilder.append("&");
        } else {
            urlBuilder.append("?");
            optionAdded = true;
        }
        urlBuilder.append(parameter).append("=").append(value);
        return this;
    }

    public URLUtility filter(String filters) {
        if(optionAdded) {
            urlBuilder.append("&select=");
        } else {
            urlBuilder.append("?select=");
            optionAdded = true;
        }
        urlBuilder.append(filters);
        return this;
    }

    public String toString() {
        if(accessToken != null) {
            if(optionAdded) {
                urlBuilder.append("&access_token=");
            } else {
                urlBuilder.append("?access_token=");
                optionAdded = true;
            }
            urlBuilder.append(accessToken);
        }
        return urlBuilder.toString();
    }

    public String simpleString(){
        return urlBuilder.toString();
    }
}
