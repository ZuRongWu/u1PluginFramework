package com.u1city.u1pluginframework.core.reciever;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

/**
 * Created by wuzr on 2017/7/20.
 * 插件支持的接口
 */

public interface IPlugin {
    void abortPluginBroadcast();

    void clearPluginAbortBroadcast();

    boolean isPluginOrderedBroadcast();

    boolean isPluginInitialStickyBroadcast();

    boolean getPluginAbortBroadcast();

    boolean getPluginDebugUnregister();

    int getPluginResultCode();

    String getPluginResultData();

    Bundle getPluginResultExtras(boolean makeMap);

    BroadcastReceiver.PendingResult goPluginAsync();

    IBinder peekPluginService(Context myContext, Intent service);

    void setPluginDebugUnregister(boolean debugUnregister);

    void setPluginOrderedHint(boolean isOrdered);

    void setPluginResult(int code, String data, Bundle extras) ;

    void setPluginResultCode(int code);

    void setPluginResultData(String data);

    void setPluginResultExtras(Bundle extras);
}
