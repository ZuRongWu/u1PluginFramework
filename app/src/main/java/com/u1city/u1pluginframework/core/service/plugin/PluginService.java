package com.u1city.u1pluginframework.core.service.plugin;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * plugin service
 * Created by wuzr on 2016/12/2.
 */
public class PluginService extends Service{
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
