package com.u1city.u1pluginframework.core.service;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.IBinder;

import com.u1city.u1pluginframework.core.PluginManager;
import com.u1city.u1pluginframework.core.pm.PluginApk;

/**
 * 所有的插件的service都要继承这个类，开发中使用host的方法
 * Created by wuzr on 2016/12/2.
 */
public class PluginService extends HostService implements IPlugin {
    public static final String KEY_PLUGIN_NAME = "key_plugin_name";
    public static final String KEY_PLUGIN_SERVICE_INFO = "key_service_info";

    protected HostService host;
    private PluginApk apk;
    private PluginServiceContainer container;

    public PluginService(){
        boolean devIsOpen = PluginManager.getInstance(null).getDevIsOpen();
        if(devIsOpen){
            setHost(this);
        }
    }

    public void setHost(HostService host){
        this.host = host;
    }

    @Override
    public void setPluginContainer(PluginServiceContainer container) {
        this.container = container;
    }

    @Override
    public PluginServiceContainer getPluginContainer() {
        return this.container;
    }

    @Override
    public IBinder onPluginBind(Intent intent) {
        return null;
    }

    @Override
    public void onPluginConfigurationChanged(Configuration newConfig) {
        host.onSuperConfigurationChanged(newConfig);
    }

    @Override
    public void onPluginCreate() {
        host.onSuperCreate();
    }

    @Override
    public void onPluginDestroy() {
        host.onSuperDestroy();
    }

    @Override
    public void onPluginLowMemory() {
        host.onSuperLowMemory();
    }

    @Override
    public void onPluginRebind(Intent intent) {
        host.onSuperRebind(intent);
    }

    @Override
    public void setApk(PluginApk apk) {
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
    public String getPluginName() {
        return apk != null?apk.getPluginName():null;
    }

    @Override
    public void onPluginStart(Intent intent, int startId) {
        host.onSuperStart(intent,startId);
    }

    @Override
    public int onPluginStartCommand(Intent intent, int flags, int startId) {
        return host.onSuperStartCommand(intent,flags,startId);
    }

    @Override
    public void onPluginTaskRemoved(Intent rootIntent) {
        host.onSuperTaskRemoved(rootIntent);
    }

    @Override
    public void onPluginTrimMemory(int level) {
        host.onSuperTrimMemory(level);
    }

    @Override
    public boolean onPluginUnbind(Intent intent) {
        return host.onSuperUnbind(intent);
    }
}
