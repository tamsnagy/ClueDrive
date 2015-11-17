package com.cluedrive.onedrive.response;

import java.util.List;

/**
 * Created by Tamas on 2015-10-11.
 */
public class ChildrenList {
    private List<Item> value;

    public List<Item> getValue() {
        return value;
    }

    public void setValue(List<Item> value) {
        this.value = value;
    }
}
