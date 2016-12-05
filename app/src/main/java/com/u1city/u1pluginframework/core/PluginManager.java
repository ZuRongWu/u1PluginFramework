package com.u1city.u1pluginframework.core;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;

import com.u1city.u1pluginframework.core.activity.host.DefaultHostChoosePolicy;
import com.u1city.u1pluginframework.core.activity.host.HostActivity;
import com.u1city.u1pluginframework.core.activity.host.HostChoosePolicy;
import com.u1city.u1pluginframework.core.activity.plugin.PluginActivity;
import com.u1city.u1pluginframework.core.error.PluginActivityNotFindException;
import com.u1city.u1pluginframework.core.pk.PackageManager;
import com.u1city.u1pluginframework.core.pk.PluginApk;
import java.io.File;
import java.net.URL;

/**
 * plugin manager
 * Created by wuzr on 2016/12/2.
 */
public class PluginManager {
    private static PluginManager sPluginManager;
    private PackageManager packageManager;
    private Context context;
    private HostChoosePolicy hostChoosePolicy;

    public static PluginManager getInstance(Context context) {
        if (sPluginManager == null) {
            synchronized (PluginManager.class) {
                if(sPluginManager == null){
                    sPluginManager = new PluginManager(context);
                }
            }
        }
        return sPluginManager;
    }

    private PluginManager(Context context) {
        this.context = context;
        packageManager = PackageManager.getInstance(this.context);
        hostChoosePolicy = new DefaultHostChoosePolicy();
    }

    /**
     * 安装插件，首先检查
     * @param pluginPath
     * @throws Exception
     */
    public void installPlugin(String pluginPath) throws Exception{
        if(checkPluginPath(pluginPath)){
            packageManager.installPlugin(pluginPath);
        }else{
            throw new IllegalArgumentException(pluginPath + "指定的插件不存在或者格式不正确");
        }
    }

    public void uninstallPlugin(String pluginName){
        if(pluginName == null||pluginName.equals("")){
            throw new IllegalArgumentException("插件名称不能为空");
        }
        packageManager.uninstallPlugin(pluginName);
    }

    public void updatePlugin(String pluginPath) throws Exception {
        if(checkPluginPath(pluginPath)){
            packageManager.updatePlugin(pluginPath);
        }else{
            throw new IllegalArgumentException(pluginPath + "指定的插件不存在或者格式不正确");
        }
    }

    public void installPlugin(URL url) {

    }

    public void startPluginActivityForResult(Context context,PluginIntent intent,int requestCode){
        try {
            String pluginName = intent.getPluginName();
            String compnentName = intent.getPluginCompnentName();
            if(pluginName == null||pluginName.equals("")||compnentName == null||compnentName.equals("")){
                throw new PluginActivityNotFindException(compnentName);
            }
            PluginApk apk = packageManager.getPlugin(pluginName);
            if(apk == null){
                throw new PluginActivityNotFindException(compnentName);
            }
            ActivityInfo ai = packageManager.findPluginActivity(compnentName, apk);
            if(ai != null){
                Class<HostActivity> hostClazz = hostChoosePolicy.choose(ai);
                intent.setClass(this.context,hostClazz);
                intent.putExtra(PluginActivity.KEY_PLUGIN_ACTIVITY_INFO, ai);
                intent.putExtra(PluginActivity.KEY_PLUGIN_NAME,pluginName);
                if(context instanceof Activity){
                    ((Activity)context).startActivityForResult(intent,requestCode);
                }else{
                    context.startActivity(intent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startPluginService(Context context,PluginIntent intent) {
    }

    private boolean checkPluginPath(String pluginPath){
        File plugin = new File(pluginPath);
        if(!plugin.exists()){
            return false;
        }
        String fileName = plugin.getName();
        return fileName.endsWith(".zip") || fileName.endsWith(".apk") || fileName.endsWith(".jar");
    }
}
