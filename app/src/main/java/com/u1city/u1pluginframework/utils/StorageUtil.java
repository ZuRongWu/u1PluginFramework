package com.u1city.u1pluginframework.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * 用于管理存储器路径
 * Created by wuzr on 2016/9/8.
 */
public class StorageUtil {
    private static final String TAG = "StorageUtil";
    private static final String EXTERNAL_STORAGE_PERMISSION = "android.permission.WRITE_EXTERNAL_STORAGE";

    /**
     * 获取应用缓存目录
     *
     * @param context        application context
     * @param preferExternal 是否优先返回外部存储
     * @return 缓存路径
     */
    public static File getCacheDir(Context context, boolean preferExternal) {
        File cacheDir = null;
        if (preferExternal && checkExternalStorageAvaliable(context)) {
            cacheDir = getExternalCacheDir(context);
        }
        if(cacheDir == null) {
            cacheDir = context.getCacheDir();
        }
        return cacheDir;
    }

    public static File getExternalCacheDir(Context context) {
        if(!checkExternalStorageAvaliable(context)){
            return null;
        }
        File dataDir = new File(new File(Environment.getExternalStorageDirectory(), "Android"), "data");
        File appCacheDir = new File(new File(dataDir, context.getPackageName()), "cache");
        if (!appCacheDir.exists()) {
            if (!appCacheDir.mkdirs()) {
                Log.w(TAG, "无法创建外部存储缓存目录");
                return null;
            }
            try {
                new File(appCacheDir, ".nomedia").createNewFile();
            } catch (IOException e) {
                Log.e(TAG, "无法在" + appCacheDir.getAbsolutePath() + "中创建文件");
            }
        }
        return appCacheDir;
    }

    /**
     * 取得应用在外部存储器的根目录
     */
    public static File getAppExternalRootDir(Context context){
        File rootDir = null;
        if(checkExternalStorageAvaliable(context)){
            rootDir = new File(new File(Environment.getExternalStorageDirectory(),"Android"),"data");
            rootDir = new File(rootDir,context.getPackageName());
        }
        return rootDir;
    }

    /**
     * 取得应用的内部存储根目录
     */
    public static File getAppOwnerRootDir(Context context){
        return context.getFilesDir().getParentFile();
    }

    private static boolean hasExternalStoragePermission(Context context) {
        int perm = context.checkCallingOrSelfPermission(EXTERNAL_STORAGE_PERMISSION);
        return perm == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean checkExternalStorageAvaliable(Context context){
        String externalStorageState;
        try {
            externalStorageState = Environment.getExternalStorageState();
        } catch (Exception e) {
            externalStorageState = "";
        }
        return  externalStorageState.equals(Environment.MEDIA_MOUNTED) && hasExternalStoragePermission(context);
    }

    /**
     * 获得内部临时文件目录
     */
    public static File getOwnerTempDir(Context context){
        File rootDir = getAppOwnerRootDir(context);
        if(rootDir == null){
            return null;
        }
        if(!rootDir.exists()){
            try {
                if(!rootDir.mkdirs()){
                    Log.w(TAG,"创建文件" + rootDir.getAbsolutePath() + "失败");
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        File tempDir = new File(rootDir,"temp");
        if(!tempDir.exists()){
            if(!tempDir.mkdirs()){
                return null;
            }
        }
        return tempDir;
    }

    /**
     * 获取外部存储上的临时文件目录
     */
    public static File getExternalTempDir(Context context){
        File rootDir = getAppExternalRootDir(context);
        if(rootDir == null){
            return null;
        }
        if(!rootDir.exists()){
            if(!rootDir.mkdirs()){
                return null;
            }
        }
        File tempDir = new File(rootDir,"temp");
        if(!tempDir.exists()){
            if(!tempDir.mkdirs()){
                return null;
            }
        }
        return tempDir;
    }

    /**
     * 获取外部存储图片文件目录
     */
    public static File getExternalImageDir(Context context){
        File rootDir = getAppExternalRootDir(context);
        if(rootDir == null){
            return null;
        }
        if(!rootDir.exists()){
            if(!rootDir.mkdirs()){
                return null;
            }
        }
        File imageDir = new File(rootDir,"image");
        if(!imageDir.exists()){
            if(!imageDir.mkdirs()){
                return null;
            }
        }
        return imageDir;
    }

    /**
     * 获取内部存储图片文件目录
     */
    public static File getOwnerImageDir(Context context){
        File rootDir = getAppOwnerRootDir(context);
        if(rootDir == null){
            return null;
        }
        if(!rootDir.exists()){
            if(!rootDir.mkdirs()){
                return null;
            }
        }
        File imageDir = new File(rootDir,"image");
        if(!imageDir.exists()){
            if(!imageDir.mkdirs()){
                return null;
            }
        }
        return imageDir;
    }

    /**
     * 获取文件外部存储目录
     */
    public static File getExternalFileDir(Context context){
        File rootDir = getAppExternalRootDir(context);
        if(rootDir == null){
            return null;
        }
        if(!rootDir.exists()){
            if(!rootDir.mkdirs()){
                return null;
            }
        }
        File fileDir = new File(rootDir,"file");
        if(!fileDir.exists()){
            if(!fileDir.mkdirs()){
                return null;
            }
        }
        return fileDir;
    }

    /**
     * 获取文件内部目录
     */
    public static File getOwnerFileDir(Context context){
        File rootDir = getAppOwnerRootDir(context);
        if(rootDir == null){
            return null;
        }
        File fileDir = new File(rootDir,"file");
        if(!fileDir.exists()){
            if(!fileDir.mkdirs()){
                return null;
            }
        }
        return fileDir;
    }
}
