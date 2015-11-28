package com.cluedrive.commons;

/**
 * Class representation of account information
 */
public class CAccountInfo {
    /**
     * The name under the account is registered.
     */
    private String name;
    /**
     * Total available space on cloud in bytes.
     */
    private long total;

    public CAccountInfo() {
    }

    public CAccountInfo(String name, long total) {
        this.name = name;
        this.total = total;
    }

    ////////////////////////////////////////////////////////////////////
    // getters and setters

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
