package com.u1city.u1pluginframework.core.service;

import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.u1city.u1pluginframework.core.PluginIntent;
import com.u1city.u1pluginframework.core.PluginManager;
import com.u1city.u1pluginframework.core.pm.PackageManager;
import com.u1city.u1pluginframework.core.pm.PluginApk;

/**
 * host service
 * Created by wuzr on 2016/12/2.
 */
public class HostService extends Service {
    private static final String TAG = "HostService";
    private boolean devIsOpen;
    private IPlugin plugin;

    public HostService(){
        devIsOpen = PluginManager.getInstance(this).getDevIsOpen();
    }

    void setPlugin(IPlugin plugin){
        if(devIsOpen){
            this.plugin = plugin;
        }else{
            Log.w(TAG,"只有开发模式可以设置plugin");
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if(initPlugin(intent)){
            return plugin.onPluginBind(intent);
        }
        return null;
    }

    @Override
    public void onCreate() {
        if(devIsOpen){
            plugin.onPluginCreate();
        }
    }

    final void onSuperCreate(){
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent,int flags, int startId) {
        if(initPlugin(intent)){
            return plugin.onPluginStartCommand(intent,flags,startId);
        }else {
           return super.onStartCommand(intent,flags,startId);
        }
    }

    final int onSuperStartCommand(Intent intent,int flags, int startId){
        return super.onStartCommand(intent,flags,startId);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        if(initPlugin(intent)){
            plugin.onPluginStart(intent,startId);
        }else{
            super.onStart(intent, startId);
        }
    }

    final void onSuperStart(Intent intent, int startId){
        super.onStart(intent,startId);
    }

    @Override
    public void onDestroy() {
        if(plugin != null){
            plugin.onPluginDestroy();
        }else{
            super.onDestroy();
        }
    }

    final void onSuperDestroy(){
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if(plugin != null){
            plugin.onPluginConfigurationChanged(newConfig);
        }else{
            super.onConfigurationChanged(newConfig);
        }
    }

    final void onSuperConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public Resources getResources() {
        if(plugin != null&&!devIsOpen){
            return plugin.getPluginResources();
        }
        return super.getResources();
    }

    @Override
    public void onLowMemory() {
        if(plugin != null){
            plugin.onPluginLowMemory();
        }else{
            super.onLowMemory();
        }
    }

    final void onSuperLowMemory(){
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        if(plugin != null){
            plugin.onPluginTrimMemory(level);
        }else{
            super.onTrimMemory(level);
        }
    }

    final void onSuperTrimMemory(int level){
        super.onTrimMemory(level);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if(plugin != null){
            return plugin.onPluginUnbind(intent);
        }else{
            return super.onUnbind(intent);
        }
    }

    final boolean onSuperUnbind(Intent intent){
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        if(plugin != null){
            plugin.onPluginRebind(intent);
        }else{
            super.onRebind(intent);
        }
    }

    final void onSuperRebind(Intent intent){
        super.onRebind(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        if(plugin != null){
            plugin.onPluginTaskRemoved(rootIntent);
        }else{
            super.onTaskRemoved(rootIntent);
        }
    }

    final void onSuperTaskRemoved(Intent rootIntent){
        super.onTaskRemoved(rootIntent);
    }

    /**
     * 初始化插件service
     * @param intent intent
     * @return true：初始化成功；false：初始化失败
     */
    private boolean initPlugin(Intent intent){
        if(plugin != null){
            //plugin不为空时说明plugin已经初始化完成
            return true;
        }
        String pluginName = intent.getStringExtra(PluginService.KEY_PLUGIN_NAME);
        if(TextUtils.isEmpty(pluginName)){
            //没有传插件名称不允许，暂停服务
            Log.w(TAG,"没有传插件名称不允许，暂停服务");
            stopSelf();
            return false;
        }
        ServiceInfo si = intent.getParcelableExtra(PluginService.KEY_PLUGIN_SERVICE_INFO);
        if(si == null){
            //没有传serveInfo不允许，暂停服务
            Log.w(TAG,"没有传serveInfo不允许，暂停服务");
            stopSelf();
            return false;
        }
        PluginApk apk = PackageManager.getInstance(this).getPlugin(pluginName);
        if(apk == null){
            Log.w(TAG,"插件不存在：" + pluginName);
            stopSelf();
            return false;
        }
        try {
            Class sClazz = apk.getClassLoader().loadClass(si.name);
            plugin = (IPlugin) sClazz.newInstance();
            plugin.onPluginCreate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            stopSelf();
            return false;
        }
    }
}
