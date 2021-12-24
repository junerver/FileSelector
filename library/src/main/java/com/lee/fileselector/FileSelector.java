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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

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
    public List<FileModel> mFileModels = Collections.synchronizedList(new ArrayList<>());
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
                getFolderFiles(selectPath);
            }
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

                            getFolderFiles(dir.getAbsolutePath());


                        }
                    }
                } else {
                    //线程池开启任务

                    getFolderFiles(dir.getAbsolutePath());

                }
            }
        }
    }


    /**
     * 文件搜索器,第二种实现
     * 让一个线程,检索一个文件.当某个线程检索完文件时,再协助其他线程检索
     */
    static class FileSearcher2 implements Runnable{
        final ThreadPoolExecutor executorService;
        final ExecutorCompletionService completionService;
        final File folder;
        //任务启动时间
        final long startTime;
        long currentTime;
        long elapsedTime;

        public FileSearcher2(File folder) {
            this.folder = folder;
            this.startTime =System.currentTimeMillis();
            this.currentTime=System.currentTimeMillis();
            //遍历线程池对象
            executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()+1);
            completionService =new ExecutorCompletionService(executorService);
        }

        @Override
        public void run() {
            //遍历用户目录下所有文件,但查找到指定文件
            //当前线程负责纷发需要检索目录
            final AtomicInteger searchCount = new AtomicInteger();
            completionService.submit(new FileSearchWorker(completionService,executorService,folder,searchCount));
            try {
                Future<SearchResult> result;
                int resultCount=0;
                int fileCount=0;
                final long l=System.currentTimeMillis();
                while(null!=(result = completionService.take())){
                    SearchResult searchResult = result.get();
                    fileCount+=searchResult.totalCount;
                    resultCount+=searchResult.result.size();
                    //当原子计数器为0时,代表当前检索任务完成
                    if(0==searchCount.get()){
                        executorService.shutdownNow();
                        break;
                    }
                }
                System.out.println("最终结果-耗时:"+(System.currentTimeMillis()-l)+" 当前共扫描文件:"+fileCount+"个"+",搜索结果:"+resultCount+"个");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            System.out.println("扫描任务完成!");
        }

    }

    static class FileSearchWorker implements Callable<SearchResult> {
        final ExecutorCompletionService completionService;
        final ThreadPoolExecutor executorService;
        final AtomicInteger searchCount;
        final File folder;
        int fileCount;

        public FileSearchWorker(ExecutorCompletionService completionService,ThreadPoolExecutor executorService, File folder,AtomicInteger searchCount) {
            this.completionService=completionService;
            this.executorService=executorService;
            this.searchCount =searchCount;
            this.folder = folder;
        }

        @Override
        public SearchResult call() {
            //当前扫描计数
            this.searchCount.incrementAndGet();
            //开始扫描
            final List<File> result=new ArrayList<>();
            startSearch(folder,result);
            //在所有工作目录队列内,移除当前工作目录,当工作目录为空则,代表所有的遍历任务完成,这里或许有更好的设计
            // 只是因为CompletionService需要一个任务执行完成状态
            this.searchCount.decrementAndGet();
            return new SearchResult(result,fileCount);
        }

        /**
         * 正常线程检索
         * @param file
         */
        void startSearch(File file,List<File> result){
            if(file.isDirectory()){
                File[] files = file.listFiles();
                for(int i=0;i<files.length;i++){
                    if(executorService.getActiveCount()<executorService.getCorePoolSize()&&files[i].isDirectory()){
                        //当前线程池有未工作线程,启动新的工作任务,这个新的线程会抢占当前线程想要遍历的目录,而当前目录则跳过此目录继续遍历其他目录
                        //以此达到只要线程池有线程不工作,其他线程在遍历时检测到了,立即分配一个目录给其遍历的目的,尽可能提升线程间协同遍历
                        completionService.submit(new FileSearchWorker(completionService,executorService, files[i],searchCount));
                    } else {
                        startSearch(files[i],result);
                    }
                }
            } else if(file.getName().endsWith("gif")){
                //检索文件
                result.add(file);
            }
            fileCount++;
        }
    }

    static class SearchResult{
        final List<File> result;
        final int totalCount;

        public SearchResult(List<File> result, int totalCount) {
            this.result = result;
            this.totalCount = totalCount;
        }
    }

}
