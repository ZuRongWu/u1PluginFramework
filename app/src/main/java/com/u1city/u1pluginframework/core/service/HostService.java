package com.u1city.u1pluginframework.core.service;

import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.u1city.u1pluginframework.IPluginBinderProvider;
import com.u1city.u1pluginframework.core.ContextTransverter;
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
    private PluginServiceContainer pluginContainer;
    private PluginBinderProvider pluginBinderProvider = new PluginBinderProvider();

    private class PluginBinderProvider extends IPluginBinderProvider.Stub{

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {
            //do nothing
        }

        @Override
        public IBinder getPluginBinder(Intent intent) throws RemoteException {
            if(initPlugin(intent)){
                return pluginContainer.onPluginBind(intent);
            }
            return null;
        }
    }

    public HostService(){
        devIsOpen = PluginManager.getInstance(this).getDevIsOpen();
    }

    void setPlugin(IPlugin pluginContainer){
//        if(devIsOpen){
//            this.pluginContainer = pluginContainer;
//        }else{
//            Log.w(TAG,"只有开发模式可以设置plugin");
//        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return pluginBinderProvider;
    }

    @Override
    public void onCreate() {
        if(devIsOpen){
            pluginContainer.onPluginCreate();
        }
    }

    final void onSuperCreate(){
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent,int flags, int startId) {
        if(initPlugin(intent)){
            return pluginContainer.onPluginStartCommand(intent,flags,startId);
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
            pluginContainer.onPluginStart(intent,startId);
        }else{
            super.onStart(intent, startId);
        }
    }

    final void onSuperStart(Intent intent, int startId){
        super.onStart(intent,startId);
    }

    @Override
    public void onDestroy() {
        if(pluginContainer != null){
            pluginContainer.onPluginDestroy();
        }else{
            super.onDestroy();
        }
    }

    final void onSuperDestroy(){
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if(pluginContainer != null){
            pluginContainer.onPluginConfigurationChanged(newConfig);
        }else{
            super.onConfigurationChanged(newConfig);
        }
    }

    final void onSuperConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public Resources getResources() {
        return super.getResources();
    }

    @Override
    public void onLowMemory() {
        if(pluginContainer != null){
            pluginContainer.onPluginLowMemory();
        }else{
            super.onLowMemory();
        }
    }

    final void onSuperLowMemory(){
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        if(pluginContainer != null){
            pluginContainer.onPluginTrimMemory(level);
        }else{
            super.onTrimMemory(level);
        }
    }

    final void onSuperTrimMemory(int level){
        super.onTrimMemory(level);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if(pluginContainer != null){
            return pluginContainer.onPluginUnbind(intent);
        }else{
            return super.onUnbind(intent);
        }
    }

    final boolean onSuperUnbind(Intent intent){
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        if(pluginContainer != null){
            pluginContainer.onPluginRebind(intent);
        }else{
            super.onRebind(intent);
        }
    }

    final void onSuperRebind(Intent intent){
        super.onRebind(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        if(pluginContainer != null){
            pluginContainer.onPluginTaskRemoved(rootIntent);
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
        if(pluginContainer == null){
            //plugin不为空时说明plugin已经初始化完成
            pluginContainer = new PluginServiceContainer();
            PluginManager.getInstance(null).applyHostServiceStarted(this,pluginContainer);
            pluginContainer.setHost(this);
        }
        String pluginName = intent.getStringExtra(PluginService.KEY_PLUGIN_NAME);
        if(TextUtils.isEmpty(pluginName)){
            //没有传插件名称不允许，暂停服务
            Log.w(TAG,"没有传插件名称不允许");
            if(pluginContainer.size() == 0){
                stopSelf();
            }
            return false;
        }
        ServiceInfo si = intent.getParcelableExtra(PluginService.KEY_PLUGIN_SERVICE_INFO);
        if(si == null){
            //没有传serveInfo不允许，暂停服务
            Log.w(TAG,"没有传serveInfo不允许");
            if(pluginContainer.size() == 0){
                stopSelf();
            }
            return false;
        }
        String id = PluginServiceContainer.generateServiceId(si.packageName,si.name);
        if(pluginContainer.containServicePlugin(id)){
            //如果container中包含将要启动的service说明service已经启动不需要重复启动
            return true;
        }
        PluginApk apk = PackageManager.getInstance(this).getPlugin(pluginName);
        if(apk == null){
            Log.w(TAG,"插件不存在：" + pluginName);
            if(pluginContainer.size() == 0){
                stopSelf();
            }
            return false;
        }
        try {
            Class sClazz = apk.getClassLoader().loadClass(si.name);
            IPlugin plugin = (IPlugin) sClazz.newInstance();
            plugin.setApk(apk);
            plugin.setHost(ContextTransverter.transformService(apk,this));
            plugin.setPluginContainer(pluginContainer);
            plugin.onPluginCreate();
            pluginContainer.addPluginService(id,plugin);
//            PluginManager.getInstance(null).applyHostServiceStopped(si,this);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if(pluginContainer.size() == 0){
                stopSelf();
            }
            return false;
        }
    }
}
