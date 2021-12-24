package com.lee.fileselector;

import java.util.List;

/**
 * @author Lee
 */
public interface OnResultListener<T> {

    /**
     * return LocalMedia result
     */
    void onResult(List<T> result);

    /**
     * Cancel
     */
    void onCancel();
}

