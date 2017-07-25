package com.u1city.u1pluginframework.core.service;

import android.app.Application;
import android.app.Notification;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.IBinder;

import com.u1city.u1pluginframework.core.pm.PluginApk;

/**
 * Created by wuzr on 2017/7/21.
 * service的插件接口，开发插件apk时应该只使用这个接口提供的api
 */

public interface IPlugin {
    Application getPluginApplication();

    IBinder onPluginBind(Intent intent);

    void onPluginConfigurationChanged(Configuration newConfig);

    void onPluginCreate();

    void onPluginDestroy();

    void onPluginLowMemory();

    void onPluginRebind(Intent intent);

    void setApk(PluginApk apk);

    @Deprecated
    void onPluginStart(Intent intent, int startId);

    int onPluginStartCommand(Intent intent, int flags, int startId);

    void onPluginTaskRemoved(Intent rootIntent);

    void stopPluginSelf();

    void onPluginTrimMemory(int level);

    boolean onPluginUnbind(Intent intent);

    Resources getPluginResources();

    void startPluginForeground(int id, Notification notification);

    void stopPluginForeground(boolean removeNotification);

    void stopPluginSelf(int startId);

    boolean stopPluginSelfResult(int startId);

    void setHost(HostService host);
}
