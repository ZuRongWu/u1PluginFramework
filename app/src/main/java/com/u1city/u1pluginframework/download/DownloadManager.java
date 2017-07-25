package com.u1city.u1pluginframework.download;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.u1city.u1pluginframework.utils.StorageUtil;

import net.tsz.afinal.FinalDb;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by wuzr on 2017/7/17.
 * 用于下载，支持多线程下载
 */

public class DownloadManager {

    public interface DownloadListener {
        /**
         * 进度改变时回调
         *
         * @param total 下载内容总大小
         * @param done  已经完成的大小
         */
        void onProgress(long total, long done);

        /**
         * 发生错误时回调
         *
         * @param msg 错误信息
         */
        void onError(String msg);

        /**
         * 下载完成回调
         *
         * @param path 下载保存的文件绝对路径
         */
        void onFinish(String path);
    }

    private class DownloadEntry {
        //保存下载文件的路径
        private String path;
        //url的路径
        private String urlStr;
        //当前下载的位置，用于断点续传
        private long currentPosition;
        //为空时发送广播通知
        private DownloadListener listener;
        //唯一标识一个下载任务，用时间戳
        private long id;
        //需要下载文件的总大小
        private long total;
        //已经完成的大小
        private long done;
        //1：等待下载；2：正在下载
        private int status;
        //true：如果应用退出，下次进入时会从退出时的状态继续下载，不需要重新下载
        private boolean forEver;
    }

    private static final int FINISH = 1;
    private static final int ERROR = 2;
    private static final int PROGRESS = 3;
    private static final String DB_NAME = "download_db";
    private static final int STATUS_WAITTING = 1;
    private static final int STATUS_LOADDING = 2;
    private static final String TAG = "DownloadManager";
    private static final String ERROR_ACTION = "action_download_error";
    private static final String PROGRESS_ACTION = "action_download_progress";
    private static final String FINISH_ACTION = "action_download_finish";
    //发送PROGRESS_ACTION通过intent传递
    public static final String KEY_TOTAL = "key_total";
    public static final String KEY_DONE = "key_done";
    //发送FINISH_ACTION通过intent传递
    public static final String KEY_PATH = "key_path";
    //发送ERROR_ACTION通过intent传递
    public static final String KEY_REASON = "key_reason";
    //每次通过广播通知状态时都会通过intent传递
    public static final String KEY_ID = "key_id";

    private static DownloadManager sManager;
    private FinalDb mDb;
    private Context mContext;
    //等待下载的队列
    private List<DownloadEntry> mWaitingEntries = new ArrayList<>();
    //暂停下载的队列
    private ThreadPoolExecutor mThreadPool;
    //当前正在下载的任务
    private DownloadEntry mCurrentEntry;
    private List<DownLoadTask> mWorkingTask = new ArrayList<>(3);
    private String mDownloadDir;
    private boolean mIsRunnig;
    private Object mLock = new Object();
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (what) {
                case FINISH:
                    mIsRunnig = false;
                    mWorkingTask.clear();
                    if (mCurrentEntry.forEver) {
                        mDb.delete(mCurrentEntry);
                    }
                    //当listener不为空的时候调用listener的回调函数，否则发生一条广播
                    if (mCurrentEntry.listener != null) {
                        mCurrentEntry.listener.onFinish(mCurrentEntry.path);
                    } else {
                        Intent i0 = new Intent();
                        i0.setAction(FINISH_ACTION);
                        i0.putExtra(KEY_PATH, mCurrentEntry.path);
                        i0.putExtra(KEY_ID, mCurrentEntry.id);
                        mContext.sendBroadcast(i0);
                    }
                    //是否还有等待下载的任务，如果有下载下一个
                    if (mWaitingEntries.size() > 0) {
                        start();
                    }
                    break;
                case ERROR:
                    mIsRunnig = false;
                    if (mCurrentEntry.forEver) {
                        mDb.delete(mCurrentEntry);
                    }
                    //一个线程出现错误，需要停掉所有线程
                    for (DownLoadTask t : mWorkingTask) {
                        t.stop = true;
                    }
                    mWorkingTask.clear();
                    //删除创建的下载文件
                    File f = new File(mCurrentEntry.path);
                    if (f.exists()) {
                        if (!f.delete()) {
                            Log.w(TAG, "删除文件失败：" + f.getAbsolutePath());
                        }
                    }
                    //当listener不为空的时候调用listener的回调函数，否则发生一条广播
                    if (mCurrentEntry.listener != null) {
                        mCurrentEntry.listener.onError((String) msg.obj);
                    } else {
                        Intent i1 = new Intent();
                        i1.setAction(ERROR_ACTION);
                        i1.putExtra(KEY_REASON, (String) msg.obj);
                        i1.putExtra(KEY_ID, mCurrentEntry.id);
                        mContext.sendBroadcast(i1);
                    }
                    if (mWaitingEntries.size() > 0) {
                        start();
                    }
                    break;
                case PROGRESS:
                    mCurrentEntry.done += (long) msg.obj;
                    mCurrentEntry.currentPosition = mCurrentEntry.done;
                    if (mCurrentEntry.forEver) {
                        mDb.update(mCurrentEntry);
                    }
                    //当listener不为空的时候调用listener的回调函数，否则发生一条广播
                    if (mCurrentEntry.listener != null) {
                        mCurrentEntry.listener.onProgress(mCurrentEntry.total, mCurrentEntry.done);
                    } else {
                        Intent i2 = new Intent();
                        i2.setAction(PROGRESS_ACTION);
                        i2.putExtra(KEY_DONE, mCurrentEntry.done);
                        i2.putExtra(KEY_TOTAL, mCurrentEntry.total);
                        i2.putExtra(KEY_ID, mCurrentEntry.id);
                        mContext.sendBroadcast(i2);
                    }
                    if (mCurrentEntry.done == mCurrentEntry.total) {
                        //全部下载完成
                        Message finish = mHandler.obtainMessage(FINISH);
                        mHandler.sendMessage(finish);
                    }
                    break;
            }
        }
    };

    public static DownloadManager getInstance(Context context) {
        if (sManager == null) {
            synchronized (DownloadEntry.class) {
                if (sManager == null) {
                    sManager = new DownloadManager(context.getApplicationContext());
                }
            }
        }
        return sManager;
    }

    /**
     * 异步下载
     *
     * @param urlStr   url地址
     * @param listener 回调对象
     * @param forEver  true：如果下载过程中突然退出应用，下次进入应用时可以恢复之前的下载状态
     */
    public synchronized void download(String urlStr, DownloadListener listener, boolean forEver) {
        DownloadEntry entry = new DownloadEntry();
        entry.urlStr = urlStr;
        entry.listener = listener;
        entry.id = generateEntryId();
        String name = generateFileName(urlStr);
        entry.path = mDownloadDir + name;
        entry.status = STATUS_WAITTING;
        synchronized (this) {
            mWaitingEntries.add(entry);
        }
        entry.forEver = forEver;
        if (forEver) {
            //true时需要进行断点续传
            mDb.save(entry);
        }
        if (mWaitingEntries.size() > 0) {
            start();
        }
    }

    /**
     * 同步下载，会等待任务下载完成在返回
     *
     * @param urlStr url地址
     * @return null:下载失败，否则返回保存下载内容的文件的绝对地址
     */
    public String downloadSync(String urlStr) {
        final String[] res = new String[1];
        download(urlStr, new DownloadListener() {
            @Override
            public void onProgress(long total, long done) {
                Log.d(TAG, "downloadSync progress：" + done + "/" + total);
            }

            @Override
            public void onError(String msg) {
                Log.d(TAG, "downloadSync error：" + msg);
            }

            @Override
            public void onFinish(String path) {
                Log.d(TAG, "downloadSync finish：" + path);
                res[0] = path;
                synchronized (mLock){
                    mLock.notify();
                }
            }
        }, false);
        try {
            synchronized (mLock){
                mLock.wait();
            }
            return res[0];
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取保存下载内容的文件名称。url = http://test.com/test.apk。
     * 先取test.apk,如果存在取test(1).apk，存在test(2).apk，一直追加直到
     * 不能存在为止
     *
     * @param url url地址
     * @return 文件名
     */
    private String generateFileName(String url) {
        String name = url.substring(url.lastIndexOf("/"));
        int dotIndex = name.lastIndexOf(".");
        String pureName = name.substring(0, dotIndex);
        String suffix = name.substring(dotIndex);
        File f = new File(mDownloadDir, name);
        int flag = 1;
        while (f.exists()) {
            pureName = String.format(pureName + "(%s).", flag);
            f = new File(mDownloadDir, pureName + suffix);
            flag += 1;
        }
        return pureName + suffix;
    }

    /**
     * 生成唯一的下载任务Id
     *
     * @return 时间戳
     */
    private long generateEntryId() {
        return System.currentTimeMillis();
    }

    private DownloadManager(Context context) {
        this.mContext = context;
        mDb = FinalDb.create(mContext, DB_NAME);
        File root;
        //当外部存储可用时保存的外部存储，否则保存到内部存储
        if (StorageUtil.checkExternalStorageAvaliable(mContext)) {
            root = StorageUtil.getExternalFileDir(mContext);
            assert root != null;
            mDownloadDir = root.getAbsolutePath() + "/downloads/";
        } else {
            root = StorageUtil.getOwnerFileDir(mContext);
            assert root != null;
            mDownloadDir = root.getAbsolutePath() + "/downloads/";
        }
        File f = new File(mDownloadDir);
        if (!f.exists()) {
            f.mkdirs();
        }
    }

    /**
     * 在{@link Application#onCreate()}中调用这个方法，可以继续之前没完成的下载任务
     */
    public synchronized void init() {
        mWaitingEntries = mDb.findAllByWhere(DownloadEntry.class, String.format("status = %s||status = %s", STATUS_LOADDING, STATUS_WAITTING));
        for (DownloadEntry entry : mWaitingEntries) {
            if (entry.forEver) {
                File file = new File(entry.path);
                if (!file.exists()) {
                    entry.done = 0;
                    entry.currentPosition = 0;
                } else {
                    entry.done = file.length();
                    entry.currentPosition = file.length();
                }
            }
        }
        if (mWaitingEntries != null && mWaitingEntries.size() > 0) {
            start();
        }
    }

    /**
     * 取消下载任务
     *
     * @param id 下载的任务id
     */
    public synchronized void cancel(long id) {
        if (mCurrentEntry.id == id) {
            //如果当前任务正在下载，则停止
            for (DownLoadTask task : mWorkingTask) {
                task.stop = true;
            }
            if (mCurrentEntry.forEver) {
                mDb.delete(mCurrentEntry);
            }
            if (mWaitingEntries.size() > 0) {
                start();
            }
        } else {
            //如果在等待队列，则从队列中移除
            for (DownloadEntry entry : mWaitingEntries) {
                if (entry.id == id) {
                    mWaitingEntries.remove(entry);
                    if (entry.forEver) {
                        mDb.delete(entry);
                    }
                    return;
                }
            }
        }
    }

    private void start() {
        if (mIsRunnig) {
            return;
        }
        mIsRunnig = true;
        if (mThreadPool == null) {
            mThreadPool = new ThreadPoolExecutor(3, 5, 60 * 10, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>());
        }
        synchronized (this) {
            mCurrentEntry = mWaitingEntries.remove(0);
        }
        mCurrentEntry.status = STATUS_LOADDING;
        DownLoadTask task = new DownLoadTask();
        task.urlStr = mCurrentEntry.urlStr;
        task.file = new File(mCurrentEntry.path);
        task.isRoot = true;
        if (mCurrentEntry.forEver) {
            mDb.update(mCurrentEntry);
        }
        mWorkingTask.add(task);
        mThreadPool.execute(task);
    }

    private class DownLoadTask implements Runnable {
        //开始位置
        private long startPos;
        //结束位置
        private long endPos;
        //url地址
        private String urlStr;
        //下载内容保存的目标文件
        private File file;
        //true时中断下载
        private volatile boolean stop;
        //true：负责把下载任务分割成几个子任务，实现多线程下载
        private boolean isRoot;

        @Override
        public void run() {
            BufferedInputStream bis = null;
            RandomAccessFile raf = null;
            try {
                if (stop) {
                    return;
                }
                URLConnection conn = new URL(urlStr).openConnection();
                conn.setConnectTimeout(60 * 1000);
                conn.setReadTimeout(60 * 1000);
                if (isRoot) {
                    //为true的task负责拆分成几个子任务
                    mCurrentEntry.total = (long) conn.getContentLength();
                    long undone = mCurrentEntry.total - mCurrentEntry.currentPosition;
                    long perLength = undone / 3;
                    for (int i = 0; i < 2; i++) {
                        DownLoadTask task = new DownLoadTask();
                        task.file = new File(mCurrentEntry.path);
                        task.urlStr = urlStr;
                        task.startPos = mCurrentEntry.currentPosition + i * perLength;
                        task.endPos = mCurrentEntry.currentPosition + (i + 1) * perLength;
                        mWorkingTask.add(task);
                        if (task.stop) {
                            return;
                        }
                        mThreadPool.execute(task);
                    }
                    startPos = 2 * perLength + mCurrentEntry.currentPosition;
                    endPos = undone + mCurrentEntry.currentPosition;
                    conn.setRequestProperty("Range", "bytes=" + startPos + "-" + endPos);
                }
                if (stop) {
                    return;
                }
                bis = new BufferedInputStream(conn.getInputStream());
                raf = new RandomAccessFile(file, "rwd");
                raf.seek(startPos);
                int len;
                byte[] buffer = new byte[1024];
                while ((len = bis.read(buffer)) != -1 && !stop) {
                    raf.write(buffer, 0, len);
                    Message msg = mHandler.obtainMessage(PROGRESS);
                    msg.obj = len;
                    mHandler.sendMessage(msg);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Message msg = mHandler.obtainMessage(ERROR);
                msg.obj = "连接错误";
                mHandler.sendMessage(msg);
            } finally {
                try {
                    if (bis != null) {
                        bis.close();
                    }
                    if (raf != null) {
                        raf.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
