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
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

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
    public CopyOnWriteArraySet<FileModel> mFileModelSet = new CopyOnWriteArraySet<>();
    //对应路径下的文件 index 为路径
    private HashMap<String, List<FileModel>> mFilesIndexMap = new HashMap<>();

    public OnResultListener<FileModel> listener;
    public boolean isForEachStop  = false;

    public FileSelector(Activity activity) {
        mActivity = activity;
        selectPaths = new String[]{"/storage/emulated/0/DCIM", "/storage/emulated/0/Android/data/" + mActivity.getPackageName() + "/"};
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

    public Worker getWorker() {
        return new Worker();
    }

    interface FilesScanCallBack {
        void onNext(List<FileModel> fileModels);

        void onCompleted(List<FileModel> fileModels);
    }

    class Worker {

        private FilesScanCallBack mCallBack;

        public Worker setCallBack(FilesScanCallBack callBack) {
            mCallBack = callBack;
            return this;
        }

        private FileFilter mFileFilter = null;

        private List<FileModel> getFiles() {
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
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            List list = new ArrayList<>(mFileModelSet);
            if (mCallBack != null) {
                mCallBack.onCompleted(list);
            }
            return list;
        }

        private void getFolderFiles(String path) {
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
                //加入集合
                mFileModelSet.addAll(fms);
                if (!mFilesIndexMap.keySet().contains(path)) {
                    mFilesIndexMap.put(path, fms);
                    //此处加入回调函数，从回调中获取每次的增量，通知UI更新
                    if (mCallBack != null) {
                        mCallBack.onNext(fms);
                    }
                }
                //遍历其余目录
                for (File dir : dirs) {
                    String dirPath = dir.getAbsolutePath();
                    if (!mFilesIndexMap.keySet().contains(dirPath)) {
                        //该路径下的文件未被遍历
                        if (ignorePaths != null && ignorePaths.length > 0) {
                            for (String ignorePath : ignorePaths) {
                                if (!(dirPath.toLowerCase().contains(ignorePath.toLowerCase()))) {
                                    //线程池开启任务
                                    GlobalThreadPools.getInstance().execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            getFolderFiles(dirPath);
                                        }
                                    });
                                }
                            }
                        } else {
                            //线程池开启任务
                            GlobalThreadPools.getInstance().execute(new Runnable() {
                                @Override
                                public void run() {
                                    getFolderFiles(dirPath);
                                }
                            });
                        }
                    }

                }
            }
        }

        public void work() {
            mFileModelSet.clear();
            mFilesIndexMap.clear();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    getFiles();
                }
            }).start();
        }
    }
}
