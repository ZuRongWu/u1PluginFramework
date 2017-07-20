package com.u1city.u1pluginframework.core.reciever;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import com.u1city.u1pluginframework.core.PluginManager;

/**
 * Created by wuzr on 2017/7/20.
 * BroadCastReceiver插件，插件apk使用静态广播时要继承这个类
 * 使用过程中应该调用XXXPluginXXX()的方法
 */

public class BroadCastReceiverPlugin extends BroadcastReceiver implements IPlugin{
    private BroadcastReceiver mHost;
    @Override
    public void onReceive(Context context, Intent intent) {

    }

    public void setHost(BroadCastReceiverHost host){
        if(PluginManager.getInstance(null).getDevIsOpen()){
            mHost = this;
        }else{
            mHost = host;
        }
    }

    @Override
    public final void abortPluginBroadcast() {
        if(mHost != null){
            mHost.abortBroadcast();
        }
    }

    @Override
    public final void clearPluginAbortBroadcast() {
        if(mHost != null){
            mHost.clearAbortBroadcast();
        }
    }

    @Override
    public final boolean isPluginOrderedBroadcast() {
        return mHost != null && mHost.isOrderedBroadcast();
    }

    @Override
    public final boolean isPluginInitialStickyBroadcast() {
        return mHost != null&&mHost.isInitialStickyBroadcast();
    }

    @Override
    public final boolean getPluginAbortBroadcast() {
        return mHost != null&&mHost.getAbortBroadcast();
    }

    @Override
    public final boolean getPluginDebugUnregister() {
        return mHost != null&&mHost.getDebugUnregister();
    }

    @Override
    public final int getPluginResultCode() {
        return mHost != null?mHost.getResultCode():0;
    }

    @Override
    public final String getPluginResultData() {
        return mHost != null?mHost.getResultData():null;
    }

    @Override
    public final Bundle getPluginResultExtras(boolean makeMap) {
        return mHost != null?mHost.getResultExtras(makeMap):null;
    }

    @Override
    public final PendingResult goPluginAsync() {
        return mHost != null?mHost.goAsync():null;
    }

    @Override
    public final IBinder peekPluginService(Context myContext, Intent service) {
        return mHost != null?mHost.peekService(myContext,service):null;
    }

    @Override
    public final void setPluginDebugUnregister(boolean debugUnregister) {
        if(mHost != null){
            mHost.setDebugUnregister(debugUnregister);
        }
    }

    @Override
    public final void setPluginOrderedHint(boolean isOrdered) {
        if(mHost != null){
            mHost.setOrderedHint(isOrdered);
        }
    }

    @Override
    public final void setPluginResult(int code, String data, Bundle extras) {
        if(mHost != null){
            mHost.setResult(code,data,extras);
        }
    }

    @Override
    public final void setPluginResultCode(int code) {
        if(mHost != null){
            mHost.setResultCode(code);
        }
    }

    @Override
    public final void setPluginResultData(String data) {
        if(mHost != null){
            mHost.setResultData(data);
        }
    }

    @Override
    public final void setPluginResultExtras(Bundle extras) {
        if(mHost != null){
            mHost.setResultExtras(extras);
        }
    }
}
