package com.u1city.u1pluginframework.core.reciever;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.u1city.u1pluginframework.core.PluginManager;

/**
 * Created by wuzr on 2017/7/20.
 * BroadCastReciever宿主，所有的静态广播都会由宿主来接收，宿主接收到广播后在派发到插件的广播接收者中
 */

public class BroadCastReceiverHost extends BroadcastReceiver{
    private PluginManager mPluginManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (mPluginManager == null){
            mPluginManager = PluginManager.getInstance(context);
        }
        mPluginManager.sendBroadCastReceiver(this,intent);
    }
}
