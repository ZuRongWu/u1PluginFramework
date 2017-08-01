package com.u1city.u1pluginframework.core.reciever;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.u1city.u1pluginframework.core.PluginManager;

/**
 * Created by wuzr on 2017/7/20.
 * BroadCastReciever宿主，所有的静态广播都会由宿主来接收，宿主接收到广播后在派发到插件的广播接收者中
 */

public class BroadCastReceiverHost extends BroadcastReceiver{
    public static final String ACTION_PLUGIN_FRAMEWORK_RECEIVER_HOST = "action_plugin_framework_receiver_host";
    private PluginManager mPluginManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(!TextUtils.equals(action,ACTION_PLUGIN_FRAMEWORK_RECEIVER_HOST)){
            return;
        }
        if (mPluginManager == null){
            mPluginManager = PluginManager.getInstance(context);
        }
        mPluginManager.sendBroadCastReceiver(context,this,intent);
    }
}
