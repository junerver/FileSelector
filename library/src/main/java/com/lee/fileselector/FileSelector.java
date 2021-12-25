package com.lee.fileselector;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Lee
 */
public class FileSelector {

    private static volatile FileSelector singleton;

    public String[] mFileTypes;
    public int mSortType;
    public int maxCount = 9;
    public int requestCode;
    public int barColor = Color.parseColor("#1bbc9b");
    public boolean isShow;
    public String[] selectPaths;
    public String[] ignorePaths;
    public Activity mActivity;
    public List<FileModel> mFileModels = Collections.synchronizedList(new ArrayList<FileModel>());
    public OnResultListener<FileModel> listener;
    public boolean isForEachStop;

    public FileSelector(Activity activity) {
        mActivity = activity;
        selectPaths = new String[]{"/storage/emulated/0/DCIM", "/storage/emulated/0/Android/data/" + mActivity.getPackageName() + "/"};
        mFileModels.clear();
    }

    public static FileSelector getInstance(Activity activity) {
        if (singleton == null) {
            synchronized (FileSelector.class) {
                if (singleton == null) {
                    singleton = new FileSelector(activity);
                }
            }
        }
        return singleton;
    }

    public FileSelector setSelectPath(String... selectPaths) {
        this.selectPaths = selectPaths;
        return this;
    }

    public FileSelector setIgnorePath(String... ignorePaths) {
        this.ignorePaths = ignorePaths;
        return this;
    }

    public FileSelector isShowHiddenFile(boolean isShow) {
        this.isShow = isShow;
        return this;
    }

    public FileSelector setBarColorRes(@ColorRes int barColor) {
        return this.setBarColorInt(ContextCompat.getColor(mActivity, barColor));
    }

    public FileSelector setBarColorInt(@ColorInt int barColor) {
        this.barColor = barColor;
        return this;
    }

    public FileSelector setMaxCount(int maxCount) {
        this.maxCount = Math.max(maxCount, 1);
        return this;
    }

    public FileSelector setFileType(String... fileTypes) {
        this.mFileTypes = fileTypes;
        return this;
    }

    public FileSelector setFileTypes(String[] fileTypes) {
        this.mFileTypes = fileTypes;
        return this;
    }

    public FileSelector setSortType(int sortType) {
        this.mSortType = sortType;
        return this;
    }

    public FileSelector requestCode(int requestCode) {
        this.requestCode = requestCode;
        return this;
    }

    public FileSelector forResult(OnResultListener<FileModel> listener) {
        this.listener = listener;
        return this;
    }

    public FileSelector setThreadStop(boolean isForEachStop) {
        this.isForEachStop = isForEachStop;
        return this;
    }

    public void start() {
        Intent intent = new Intent(mActivity, FileSelectorActivity.class);
        mActivity.startActivityForResult(intent, requestCode);
    }

    public List<FileModel> getFiles() {
        if (mFileFilter == null) {
            mFileFilter = new MyFileFilter(mFileTypes, isShow);
        }
        if (selectPaths != null && selectPaths.length > 0) {
            for (String selectPath : selectPaths) {
                //线程池开启任务
                GlobalThreadPools.getInstance().execute(new Runnable() {
                    @Override
                    public void run() {
                        getFolderFiles(selectPath);
                    }
                });
            }
        }
        try {
            Thread.sleep(100);
            while (!GlobalThreadPools.getInstance().hasDone()) {
                Thread.sleep(40);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        return mFileModels;
    }

    private FileFilter mFileFilter = null;

    public void getFolderFiles(String path) {
        if (!isForEachStop) {
            File file = new File(path);
            File[] files = file.listFiles(mFileFilter);
            if (files == null || files.length == 0) {
                return;
            }
            List<File> dirs = new ArrayList();
            List<FileModel> fms = new ArrayList();
            for (File value : files) {
                if (value.isFile()) {
                    FileModel fileModel = new FileModel();
                    String pathStr = value.getAbsolutePath();
                    fileModel.setExtension(FileUtils.getExtension(pathStr));
                    fileModel.setName(FileUtils.getName(pathStr));
                    fileModel.setPath(pathStr);
                    fileModel.setSize(value.length());
                    fileModel.setDate(value.lastModified());
                    fms.add(fileModel);
                } else {
                    //加入目录
                    dirs.add(value);
                }
            }
            mFileModels.addAll(fms);
            for (File dir : dirs) {
                if (ignorePaths != null && ignorePaths.length > 0) {
                    for (String ignorePath : ignorePaths) {
                        if (!(dir.getAbsolutePath().toLowerCase().contains(ignorePath.toLowerCase()))) {
                            //线程池开启任务
                            GlobalThreadPools.getInstance().execute(new Runnable() {
                                @Override
                                public void run() {
                                    getFolderFiles(dir.getAbsolutePath());
                                }
                            });
                        }
                    }
                } else {
                    //线程池开启任务
                    GlobalThreadPools.getInstance().execute(new Runnable() {
                        @Override
                        public void run() {
                            getFolderFiles(dir.getAbsolutePath());
                        }
                    });
                }
            }
        }
    }




}
