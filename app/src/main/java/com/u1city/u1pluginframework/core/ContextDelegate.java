package com.u1city.u1pluginframework.core;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;

import com.u1city.u1pluginframework.core.pm.PluginApk;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

/**
 * Created by wuzr on 2017/7/25.
 * context的操作全部委托给这个类
 */

public class ContextDelegate extends Context {
    private static final String TAG = "ContextDelegate";

    private PluginApk mApk;
    private Context mHostContext;
    private boolean mDevIsOpen;

    public ContextDelegate(PluginApk apk,Context host){
        this.mApk = apk;
        this.mHostContext = host;
        mDevIsOpen = PluginManager.getInstance(host).getDevIsOpen();
    }

    public String getBasePackageName(){
        //这个方法是隐藏的要通过反射调用
        Class<?> clazz = mHostContext.getClass();
        try {
            Method method = clazz.getDeclaredMethod("getBasePackageName");
            method.setAccessible(true);
            return (String) method.invoke(mHostContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getPackageName();
    }

    public String getOpPackageName(){
        //这个方法是隐藏的通过放射调用
        Class<?> clazz = mHostContext.getClass();
        try {
            Method method = clazz.getDeclaredMethod("getOpPackageName");
            method.setAccessible(true);
            return (String) method.invoke(mHostContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getBasePackageName();
    }

    @Override
    public AssetManager getAssets() {
        if(!mDevIsOpen){
            return mApk.getResources().getAssets();
        }
        return mHostContext.getAssets();
    }

    @Override
    public Resources getResources() {
        if(!mDevIsOpen){
            Log.d(TAG,"返回插件的resources");
            return mApk.getResources();
        }
        Log.d(TAG,"返回正常的resources");
        return mHostContext.getResources();
    }

    @Override
    public PackageManager getPackageManager() {
        return mHostContext.getPackageManager();
    }

    @Override
    public ContentResolver getContentResolver() {
        return mHostContext.getContentResolver();
    }

    @Override
    public Looper getMainLooper() {
        return mHostContext.getMainLooper();
    }

    @Override
    public Context getApplicationContext() {
        return mHostContext.getApplicationContext();
    }

    @Override
    public void setTheme(@StyleRes int i) {
        mHostContext.setTheme(i);
    }

    @Override
    public Resources.Theme getTheme() {
        return mHostContext.getTheme();
    }

    @Override
    public ClassLoader getClassLoader() {
        if(mDevIsOpen){
            return mHostContext.getClassLoader();
        }
        return mApk.getClassLoader();
    }

    @Override
    public String getPackageName() {
        return mHostContext.getPackageName();
    }

    @Override
    public ApplicationInfo getApplicationInfo() {
        return mHostContext.getApplicationInfo();
    }

    @Override
    public String getPackageResourcePath() {
        return mHostContext.getPackageResourcePath();
    }

    @Override
    public String getPackageCodePath() {
        return mHostContext.getPackageCodePath();
    }

    @Override
    public SharedPreferences getSharedPreferences(String s, int i) {
        return mHostContext.getSharedPreferences(s,i);
    }

    @Override
    public FileInputStream openFileInput(String s) throws FileNotFoundException {
        return mHostContext.openFileInput(s);
    }

    @Override
    public FileOutputStream openFileOutput(String s, int i) throws FileNotFoundException {
        return mHostContext.openFileOutput(s,i);
    }

    @Override
    public boolean deleteFile(String s) {
        return mHostContext.deleteFile(s);
    }

    @Override
    public File getFileStreamPath(String s) {
        return mHostContext.getFileStreamPath(s);
    }

    @Override
    public File getFilesDir() {
        return mHostContext.getFilesDir();
    }

    @Override
    public File getNoBackupFilesDir() {
        return mHostContext.getNoBackupFilesDir();
    }

    @Nullable
    @Override
    public File getExternalFilesDir(@Nullable String s) {
        return mHostContext.getExternalFilesDir(s);
    }

    @Override
    public File[] getExternalFilesDirs(String s) {
        return mHostContext.getExternalFilesDirs(s);
    }

    @Override
    public File getObbDir() {
        return mHostContext.getObbDir();
    }

    @Override
    public File[] getObbDirs() {
        return mHostContext.getObbDirs();
    }

    @Override
    public File getCacheDir() {
        return mHostContext.getCacheDir();
    }

    @Override
    public File getCodeCacheDir() {
        return mHostContext.getCodeCacheDir();
    }

    @Nullable
    @Override
    public File getExternalCacheDir() {
        return mHostContext.getExternalCacheDir();
    }

    @Override
    public File[] getExternalCacheDirs() {
        return mHostContext.getExternalCacheDirs();
    }

    @Override
    public File[] getExternalMediaDirs() {
        return mHostContext.getExternalMediaDirs();
    }

    @Override
    public String[] fileList() {
        return mHostContext.fileList();
    }

    @Override
    public File getDir(String s, int i) {
        return mHostContext.getDir(s,i);
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String s, int i, SQLiteDatabase.CursorFactory cursorFactory) {
        return mHostContext.openOrCreateDatabase(s,i,cursorFactory);
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String s, int i, SQLiteDatabase.CursorFactory cursorFactory, @Nullable DatabaseErrorHandler databaseErrorHandler) {
        return mHostContext.openOrCreateDatabase(s,i,cursorFactory,databaseErrorHandler);
    }

    @Override
    public boolean deleteDatabase(String s) {
        return mHostContext.deleteDatabase(s);
    }

    @Override
    public File getDatabasePath(String s) {
        return mHostContext.getDatabasePath(s);
    }

    @Override
    public String[] databaseList() {
        return mHostContext.databaseList();
    }

    @Override
    public Drawable getWallpaper() {
        return mHostContext.getWallpaper();
    }

    @Override
    public Drawable peekWallpaper() {
        return mHostContext.peekWallpaper();
    }

    @Override
    public int getWallpaperDesiredMinimumWidth() {
        return mHostContext.getWallpaperDesiredMinimumWidth();
    }

    @Override
    public int getWallpaperDesiredMinimumHeight() {
        return mHostContext.getWallpaperDesiredMinimumHeight();
    }

    @Override
    public void setWallpaper(Bitmap bitmap) throws IOException {
        mHostContext.setWallpaper(bitmap);
    }

    @Override
    public void setWallpaper(InputStream inputStream) throws IOException {
        mHostContext.setWallpaper(inputStream);
    }

    @Override
    public void clearWallpaper() throws IOException {
        mHostContext.clearWallpaper();
    }

    private Intent prepareIntent(Intent intent){
        if(!(intent instanceof PluginIntent)){
            return intent;
        }
        PluginIntent pIntent = (PluginIntent) intent;
        if(mDevIsOpen){
            //开发模式不能启动其他插件的activity
            String pluginName = pIntent.getPluginName();
            if(!TextUtils.isEmpty(pluginName)&&!TextUtils.equals(pluginName,getPackageName())){
                Log.w(TAG,"开发模式不能启动其他插件的activity和service");
            }
            String componentName = pIntent.getPluginComponentName();
            if(!TextUtils.isEmpty(componentName)){
                if(componentName.startsWith(".")){
                    componentName += getPackageName();
                }
                intent.setClassName(this,componentName);
            }
            pIntent.addPluginFlag(PluginIntent.FLAG_LAUNCH_ACTUAL);
        }else{
            if (!TextUtils.isEmpty(pIntent.getPluginComponentName())&&!TextUtils.isEmpty(pIntent.getPluginName())){
                //当指定插件的名称和组件的名称时，则是要以插件的形式启动
                if(pIntent.getPluginComponentName().startsWith(".")){
                    pIntent.setPluginComponentName(pIntent.getPluginName() + pIntent.getPluginComponentName());
                }
            }
            if(pIntent.getPluginComponentClazz() != null){
                pIntent.setPluginComponentName(pIntent.getPluginComponentClazz().getName());
                pIntent.setPluginName(mApk.getPluginName());
            }
        }
        return pIntent;
    }

    @Override
    public void startActivity(Intent intent) {
        startActivity(intent,null);
    }

    @Override
    public void startActivity(Intent intent, @Nullable Bundle bundle) {
        if(mDevIsOpen){
            prepareIntent(intent);
            mHostContext.startActivity(intent,bundle);
        }else {
            if(intent instanceof PluginIntent){
                if(((PluginIntent)intent).hasFlag(PluginIntent.FLAG_LAUNCH_ACTUAL)){
                    mHostContext.startActivity(intent,bundle);
                }else{
                    prepareIntent(intent);
                    PluginManager.getInstance(this).startPluginActivityForResult(this, (PluginIntent) intent,0);
                }
            }
            mHostContext.startActivity(intent,bundle);
        }
    }

    @Override
    public void startActivities(Intent[] intents) {
        if(intents == null){
            return;
        }
        for(Intent i: intents){
            startActivity(i);
        }
    }

    @Override
    public void startActivities(Intent[] intents, Bundle bundle) {
        if(intents == null){
            return;
        }
        for (Intent i: intents){
            startActivity(i,bundle);
        }
    }

    @Override
    public void startIntentSender(IntentSender intentSender, Intent intent, int i, int i1, int i2) throws IntentSender.SendIntentException {
        mHostContext.startIntentSender(intentSender,intent,i,i1,i2);
    }

    @Override
    public void startIntentSender(IntentSender intentSender, @Nullable Intent intent, int i, int i1, int i2, Bundle bundle) throws IntentSender.SendIntentException {
        mHostContext.startIntentSender(intentSender,intent,i,i1,i2,bundle);
    }

    @Override
    public void sendBroadcast(Intent intent) {
        mHostContext.sendBroadcast(intent);
    }

    @Override
    public void sendBroadcast(Intent intent, @Nullable String s) {
        mHostContext.sendBroadcast(intent,s);
    }

    @Override
    public void sendOrderedBroadcast(Intent intent, @Nullable String s) {
        mHostContext.sendBroadcast(intent,s);
    }

    @Override
    public void sendOrderedBroadcast(@NonNull Intent intent, @Nullable String s, @Nullable BroadcastReceiver broadcastReceiver, @Nullable Handler handler, int i, @Nullable String s1, @Nullable Bundle bundle) {
        mHostContext.sendOrderedBroadcast(intent,s,broadcastReceiver,handler,i,s1,bundle);
    }

    @Override
    public void sendBroadcastAsUser(Intent intent, UserHandle userHandle) {
        mHostContext.sendBroadcastAsUser(intent,userHandle);
    }

    @Override
    public void sendBroadcastAsUser(Intent intent, UserHandle userHandle, @Nullable String s) {
        mHostContext.sendBroadcastAsUser(intent,userHandle,s);
    }

    @Override
    public void sendOrderedBroadcastAsUser(Intent intent, UserHandle userHandle, @Nullable String s, BroadcastReceiver broadcastReceiver, @Nullable Handler handler, int i, @Nullable String s1, @Nullable Bundle bundle) {
        mHostContext.sendOrderedBroadcastAsUser(intent,userHandle,s,broadcastReceiver,handler,i,s1,bundle);
    }

    @Override
    public void sendStickyBroadcast(Intent intent) {
        mHostContext.sendStickyBroadcast(intent);
    }

    @Override
    public void sendStickyOrderedBroadcast(Intent intent, BroadcastReceiver broadcastReceiver, @Nullable Handler handler, int i, @Nullable String s, @Nullable Bundle bundle) {
        mHostContext.sendStickyOrderedBroadcast(intent,broadcastReceiver,handler,i,s,bundle);
    }

    @Override
    public void removeStickyBroadcast(Intent intent) {
        mHostContext.removeStickyBroadcast(intent);
    }

    @Override
    public void sendStickyBroadcastAsUser(Intent intent, UserHandle userHandle) {
        mHostContext.sendStickyBroadcastAsUser(intent,userHandle);
    }

    @Override
    public void sendStickyOrderedBroadcastAsUser(Intent intent, UserHandle userHandle, BroadcastReceiver broadcastReceiver, @Nullable Handler handler, int i, @Nullable String s, @Nullable Bundle bundle) {
        mHostContext.sendStickyOrderedBroadcastAsUser(intent,userHandle,broadcastReceiver,handler,i,s,bundle);
    }

    @Override
    public void removeStickyBroadcastAsUser(Intent intent, UserHandle userHandle) {
        mHostContext.removeStickyBroadcastAsUser(intent,userHandle);
    }

    @Nullable
    @Override
    public Intent registerReceiver(@Nullable BroadcastReceiver broadcastReceiver, IntentFilter intentFilter) {
        return mHostContext.registerReceiver(broadcastReceiver,intentFilter);
    }

    @Nullable
    @Override
    public Intent registerReceiver(BroadcastReceiver broadcastReceiver, IntentFilter intentFilter, @Nullable String s, @Nullable Handler handler) {
        return mHostContext.registerReceiver(broadcastReceiver,intentFilter,s,handler);
    }

    @Override
    public void unregisterReceiver(BroadcastReceiver broadcastReceiver) {
        mHostContext.unregisterReceiver(broadcastReceiver);
    }

    @Nullable
    @Override
    public ComponentName startService(Intent intent) {
        if(mDevIsOpen){
            prepareIntent(intent);
            return mHostContext.startService(intent);
        }else {
            if(intent instanceof PluginIntent){
                if(((PluginIntent)intent).hasFlag(PluginIntent.FLAG_LAUNCH_ACTUAL)){
                    return mHostContext.startService(intent);
                }else{
                    prepareIntent(intent);
                    return PluginManager.getInstance(this).startPluginService(this, (PluginIntent) intent);
                }
            }
            return mHostContext.startService(intent);
        }
    }

    @Override
    public boolean stopService(Intent intent) {
        prepareIntent(intent);
        if(mDevIsOpen){
            return mHostContext.stopService(intent);
        }else {
            return PluginManager.getInstance(this).stopPluginService(intent);
        }
    }

    @Override
    public boolean bindService(Intent intent, @NonNull ServiceConnection serviceConnection, int i) {
        if(mDevIsOpen){
            prepareIntent(intent);
            return mHostContext.bindService(intent,serviceConnection,i);
        }else {
            if(intent instanceof PluginIntent){
                if(((PluginIntent)intent).hasFlag(PluginIntent.FLAG_LAUNCH_ACTUAL)){
                    return mHostContext.bindService(intent,serviceConnection,i);
                }else{
                    //调用pluginManager绑定service
                    prepareIntent(intent);
                    PluginManager.getInstance(null).bindPluginService(this,(PluginIntent)intent,serviceConnection,i);
                }
            }else{
                return mHostContext.bindService(intent,serviceConnection,i);
            }
        }
        return false;
    }

    @Override
    public void unbindService(@NonNull ServiceConnection serviceConnection) {
        if(mDevIsOpen){
            mHostContext.unbindService(serviceConnection);
            return;
        }
        PluginManager.getInstance(null).unbindPluginService(serviceConnection);
    }

    @Override
    public boolean startInstrumentation(@NonNull ComponentName componentName, @Nullable String s, @Nullable Bundle bundle) {
        return mHostContext.startInstrumentation(componentName,s,bundle);
    }

    @Override
    public Object getSystemService(@NonNull String s) {
        return mHostContext.getSystemService(s);
    }

    @Override
    public String getSystemServiceName(Class<?> aClass) {
        return mHostContext.getSystemServiceName(aClass);
    }

    @Override
    public int checkPermission(@NonNull String s, int i, int i1) {
        return mHostContext.checkPermission(s,i,i1);
    }

    @Override
    public int checkCallingPermission(@NonNull String s) {
        return mHostContext.checkCallingPermission(s);
    }

    @Override
    public int checkCallingOrSelfPermission(@NonNull String s) {
        return mHostContext.checkCallingOrSelfPermission(s);
    }

    @Override
    public int checkSelfPermission(@NonNull String s) {
        return mHostContext.checkSelfPermission(s);
    }

    @Override
    public void enforcePermission(@NonNull String s, int i, int i1, @Nullable String s1) {
        mHostContext.enforcePermission(s,i,i1,s);
    }

    @Override
    public void enforceCallingPermission(@NonNull String s, @Nullable String s1) {
        mHostContext.enforceCallingPermission(s,s1);
    }

    @Override
    public void enforceCallingOrSelfPermission(@NonNull String s, @Nullable String s1) {
        mHostContext.enforceCallingOrSelfPermission(s,s1);
    }

    @Override
    public void grantUriPermission(String s, Uri uri,int i) {
        mHostContext.grantUriPermission(s,uri,i);
    }

    @Override
    public void revokeUriPermission(Uri uri,int i) {
        mHostContext.revokeUriPermission(uri,i);
    }

    @Override
    public int checkUriPermission(Uri uri, int i, int i1,int i2) {
        return mHostContext.checkUriPermission(uri,i,i1,i2);
    }

    @Override
    public int checkCallingUriPermission(Uri uri,int i) {
        return mHostContext.checkCallingUriPermission(uri,i);
    }

    @Override
    public int checkCallingOrSelfUriPermission(Uri uri,int i) {
        return mHostContext.checkCallingOrSelfUriPermission(uri,i);
    }

    @Override
    public int checkUriPermission(@Nullable Uri uri, @Nullable String s, @Nullable String s1, int i, int i1,int i2) {
        return mHostContext.checkUriPermission(uri,s,s1,i,i1,i2);
    }

    @Override
    public void enforceUriPermission(Uri uri, int i, int i1,int i2, String s) {
        mHostContext.enforceUriPermission(uri,i,i1,i2,s);
    }

    @Override
    public void enforceCallingUriPermission(Uri uri,int i, String s) {
        mHostContext.enforceCallingUriPermission(uri,i,s);
    }

    @Override
    public void enforceCallingOrSelfUriPermission(Uri uri,int i, String s) {
        mHostContext.enforceCallingOrSelfUriPermission(uri,i,s);
    }

    @Override
    public void enforceUriPermission(@Nullable Uri uri, @Nullable String s, @Nullable String s1, int i, int i1,int i2, @Nullable String s2) {
        mHostContext.enforceUriPermission(uri,s,s1,i,i1,i2,s2);
    }

    @Override
    public Context createPackageContext(String s,int i) throws PackageManager.NameNotFoundException {
        return mHostContext.createPackageContext(s,i);
    }

    @Override
    public Context createConfigurationContext(@NonNull Configuration configuration) {
        return mHostContext.createConfigurationContext(configuration);
    }

    @Override
    public Context createDisplayContext(@NonNull Display display) {
        return mHostContext.createDisplayContext(display);
    }
}
