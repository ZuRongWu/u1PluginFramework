package com.u1city.u1pluginframework.core.service;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.IBinder;

import com.u1city.u1pluginframework.core.PluginContext;
import com.u1city.u1pluginframework.core.PluginManager;
import com.u1city.u1pluginframework.core.pm.PluginApk;

/**
 * 所有的插件的service都要继承这个类，开发中使用host的方法
 * Created by wuzr on 2016/12/2.
 */
public class PluginService extends HostService implements ServicePlugin{
    public static final String KEY_PLUGIN_NAME = "key_plugin_name";
    public static final String KEY_PLUGIN_SERVICE_INFO = "key_service_info";

    protected HostService mHost;
    private PluginApk apk;

    public PluginService(){
        boolean devIsOpen = PluginManager.getInstance(null).getDevIsOpen();
        if(devIsOpen){
            setHost(this);
        }
    }

    public void setHost(HostService host){
        this.mHost = host;
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
        //do nothing
        this.apk = apk;
    }

    @Override
    public Resources getResources(String plugin) {
        if(apk != null){
            apk.getResources(plugin);
        }
        return null;
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
    public void onPluginTrimMemory(int level) {
        mHost.onSuperTrimMemory(level);
    }

    @Override
    public boolean onPluginUnbind(Intent intent) {
        return mHost.onSuperUnbind(intent);
    }
}
