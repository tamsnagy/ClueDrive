package com.cluedrive.commons;

/**
 * Created by Tamas on 2015-11-25.
 */
public class CAccountInfo {
    private String name;
    private long total;

    public CAccountInfo() {
    }

    public CAccountInfo(String name, long total) {
        this.name = name;
        this.total = total;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

}
