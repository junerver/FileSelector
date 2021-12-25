package com.lee.fileselector;

/**
 * Description:
 *
 * @author Junerver
 * date: 2021/12/24-16:15
 * Email: junerver@gmail.com
 * Version: v1.0
 */

import android.util.Log;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 全局使用的线程池
 */
public class GlobalThreadPools {

    private static String TAG = GlobalThreadPools.class.getSimpleName();
    //线程池
    private static ThreadPoolExecutor THREAD_POOL_EXECUTOR;
    //CPU数量
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    //核心线程数
    private static final int CORE_POOL_SIZE = CPU_COUNT;
    //最大线程数
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2;
    //线程闲置后的存活时间
    private static final int KEEP_ALIVE_SECONDS = 60;
    //任务队列
    private static final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue<>(CPU_COUNT);
    //线程工厂
    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "MangoTask #" + mCount.getAndIncrement());
        }
    };

    //初始化线程池
    private void initThreadPool() {
        THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(
                CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_SECONDS, TimeUnit.SECONDS,
                sPoolWorkQueue, sThreadFactory, new RejectedHandler()) {
            @Override
            public void execute(Runnable command) {
                super.execute(command);
                //执行完毕后排队+活动应该等于零
                Log.d(TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~" );
                Log.d(TAG, "活动总数ActiveCount=" + getActiveCount());
                Log.d(TAG, "排队总数Queue=" + getQueue().size());
                Log.d(TAG, "完成总数CompletedTaskCount=" + getCompletedTaskCount());
                Log.d(TAG, "线程池大小PoolSize=" + getPoolSize());
                Log.d(TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~" );
            }
        };
    }

    private static class RejectedHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            //可在这里做一些提示用户的操作
//            Log.v("+++","is over the max task...");
        }
    }

    private static GlobalThreadPools instance;

    private GlobalThreadPools() {
        initThreadPool();
    }

    public static GlobalThreadPools getInstance() {
        if (instance == null) {
            instance = new GlobalThreadPools();
        }
        return instance;
    }

    public void execute(Runnable command) {
        THREAD_POOL_EXECUTOR.execute(command);
    }

    /**
     * 排队总数+活动总数数量为则可以认定全部任务执行完毕
     * @return
     */
    public boolean hasDone() {
        return THREAD_POOL_EXECUTOR.getQueue().size() + THREAD_POOL_EXECUTOR.getActiveCount() == 0;
    }

    /**
     * 通过interrupt方法尝试停止正在执行的任务，但是不保证真的终止正在执行的任务
     * 停止队列中处于等待的任务的执行
     * 不再接收新的任务
     * @return 等待执行的任务列表
     */
    public List<Runnable> shutdownNow(){
        return THREAD_POOL_EXECUTOR.shutdownNow();
    }

    /**
     * 停止队列中处于等待的任务
     * 不再接收新的任务
     * 已经执行的任务会继续执行
     * 如果任务已经执行完了没有必要再调用这个方法
     */
    public void shutDown(){
        THREAD_POOL_EXECUTOR.shutdown();
        sPoolWorkQueue.clear();
    }

}