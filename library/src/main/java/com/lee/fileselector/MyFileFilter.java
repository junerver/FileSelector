package com.lee.fileselector;

import java.io.File;
import java.io.FileFilter;

/**
 * 不显示隐藏文件、文件夹
 *
 * @author Lee
 */
public class MyFileFilter implements FileFilter {

    private final String[] mTypes;
    private final boolean isShow;

    public MyFileFilter(String[] types) {
        this(types, false);
    }

    public MyFileFilter(String[] types, boolean isShow) {
        this.mTypes = types;
        this.isShow = isShow;
    }

    @Override
    public boolean accept(File file) {
        if (isShow) {
            if (file.isDirectory()) {
                return true;
            }
        } else {
            if (file.isDirectory() && !file.isHidden()) {
                return true;
            }
        }
        if (file.isDirectory() && !file.isHidden()) {
            return true;
        }
        if (mTypes != null && mTypes.length > 0) {
            for (String mType : mTypes) {
                if (isShow) {
                    if ((FileUtils.getExtensionByName(file.getName()).equals(mType.toLowerCase()) || FileUtils.getExtensionByName(file.getName()).equals(mType.toUpperCase())) && !file.isHidden()) {
                        return true;
                    }
                } else {
                    if ((FileUtils.getExtensionByName(file.getName()).equals(mType.toLowerCase()) || FileUtils.getExtensionByName(file.getName()).equals(mType.toUpperCase()))) {
                        return true;
                    }
                }
            }
        } else {
            return true;
        }
        return false;
    }
}
