package com.u1city.u1pluginframework.core.service;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;

import com.u1city.u1pluginframework.core.PluginContext;

/**
 * Created by wuzr on 2017/7/21.
 * service的插件接口，开发插件apk时应该只使用这个接口提供的api
 */

public interface IPlugin extends PluginContext{
    IBinder onPluginBind(Intent intent);

    void onPluginConfigurationChanged(Configuration newConfig);

    void onPluginCreate();

    void onPluginDestroy();

    void onPluginLowMemory();

    void onPluginRebind(Intent intent);

    @Deprecated
    void onPluginStart(Intent intent, int startId);

    int onPluginStartCommand(Intent intent, int flags, int startId);

    void onPluginTaskRemoved(Intent rootIntent);

    void onPluginTrimMemory(int level);

    boolean onPluginUnbind(Intent intent);

    void setHost(HostService host);

    void setPluginContainer(PluginServiceContainer container);

    PluginServiceContainer getPluginContainer();
}
