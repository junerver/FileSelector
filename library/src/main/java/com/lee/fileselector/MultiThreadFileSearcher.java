package com.lee.fileselector;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author Junerver
 * @Date 2021/12/25-08:22
 * @Email junerver@gmail.com
 * @Version v1.0
 * @Description
 */
class MultiThreadFileSearcher {
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
