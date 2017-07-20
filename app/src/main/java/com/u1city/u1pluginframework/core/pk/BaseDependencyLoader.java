package com.u1city.u1pluginframework.core.pk;

import android.content.Context;
import android.util.Log;

import com.u1city.u1pluginframework.core.PluginManager;

import java.util.List;

/**
 * 加载依赖plugin
 * Created by wuzr on 2016/12/6.
 */
public class BaseDependencyLoader implements DependencyLoader {
    private static final String TAG = "DependencyLoader";

    @Override
    public void load(Context context, PluginApk apk) {
        List<PluginApk.Dependency> dependencies = apk.getDependencies();
        if (dependencies == null || dependencies.size() == 0) {
            Log.i(TAG, "没有依赖");
            return;
        }
        for (PluginApk.Dependency dependency : dependencies) {
            Log.i(TAG, "开始加载依赖：" + dependency.name);
            PluginManager pluginManager = PluginManager.getInstance(context);
            if (dependency.path.startsWith("http://") || dependency.path.startsWith("https://")) {
                try {
                    //同步下载插件并安装
                    pluginManager.installPluginRemoteSync(dependency.path);
                } catch (Exception e) {
                    Log.e(TAG, "插件路径不正确：" + dependency.path);
                    Log.e(TAG, e.getMessage());
                }
            } else {
                try {
                    pluginManager.installPlugin(dependency.path);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
