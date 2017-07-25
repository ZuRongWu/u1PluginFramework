package com.u1city.u1pluginframework.core.service;

import android.app.Application;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.u1city.u1pluginframework.core.PluginManager;
import com.u1city.u1pluginframework.core.pm.PluginApk;

/**
 * plugin service
 * Created by wuzr on 2016/12/2.
 */
public class PluginService extends HostService implements IPlugin {
    public static final String KEY_PLUGIN_NAME = "key_plugin_name";
    public static final String KEY_PLUGIN_SERVICE_INFO = "key_service_info";

    protected HostService mHost;
    private boolean mIsDevOpen;
    private PluginApk mApk;

    public PluginService(){
        mIsDevOpen = PluginManager.getInstance(null).getDevIsOpen();
        if(mIsDevOpen){
            setHost(this);
        }
    }

    public void setHost(HostService host){
        this.mHost = host;
    }

    @Override
    public Application getPluginApplication() {
        return mHost.getApplication();
    }

    @Override
    public IBinder onPluginBind(Intent intent) {
        return null;
    }

    @Override
    public void onPluginConfigurationChanged(Configuration newConfig) {
        mHost.onSuperConfigurationChanged(newConfig);
    }

    @Override
    public void onPluginCreate() {
        mHost.onSuperCreate();
    }

    @Override
    public void onPluginDestroy() {
        mHost.onSuperDestroy();
    }

    @Override
    public void onPluginLowMemory() {
        mHost.onSuperLowMemory();
    }

    @Override
    public void onPluginRebind(Intent intent) {
        mHost.onSuperRebind(intent);
    }

    @Override
    public void setApk(PluginApk apk) {
        mApk = apk;
    }

    @Override
    public void onPluginStart(Intent intent, int startId) {
        mHost.onSuperStart(intent,startId);
    }

    @Override
    public int onPluginStartCommand(Intent intent, int flags, int startId) {
        return mHost.onSuperStartCommand(intent,flags,startId);
    }

    @Override
    public void onPluginTaskRemoved(Intent rootIntent) {
        mHost.onSuperTaskRemoved(rootIntent);
    }

    @Override
    public void stopPluginSelf() {
        mHost.stopSelf();
    }

    @Override
    public void onPluginTrimMemory(int level) {
        mHost.onSuperTrimMemory(level);
    }

    @Override
    public boolean onPluginUnbind(Intent intent) {
        return mHost.onSuperUnbind(intent);
    }

    @Override
    public Resources getPluginResources() {
        if(mApk != null){
            return mApk.getResources();
        }
        return null;
    }

    @Override
    public void startPluginForeground(int id, Notification notification) {
        mHost.startForeground(id,notification);
    }

    @Override
    public void stopPluginForeground(boolean removeNotification) {
        mHost.stopForeground(removeNotification);
    }

    @Override
    public void stopPluginSelf(int startId) {
        mHost.stopSelf(startId);
    }

    @Override
    public boolean stopPluginSelfResult(int startId) {
        return mHost.stopSelfResult(startId);
    }
}
