package com.u1city.u1pluginframework.core.pk;

import android.content.Context;

/**
 * 负责加载依赖的插件
 * Created by wuzr on 2016/12/6.
 */
public interface DependencyLoader {
    void load(Context context, PluginApk apk);
}
