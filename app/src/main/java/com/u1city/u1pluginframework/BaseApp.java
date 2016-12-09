package com.u1city.u1pluginframework;

import android.app.Application;

import com.u1city.u1pluginframework.core.PluginManager;

/**
 * 自定义application，在{@link #onCreate()}中初始化插件
 * Created by wuzr on 2016/12/8.
 */
public class BaseApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        try {
            PluginManager.getInstance(getApplicationContext()).init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
