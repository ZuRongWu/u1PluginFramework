package com.u1city.u1pluginframework.core.activity.host;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;

import com.u1city.u1pluginframework.core.activity.plugin.PluginActivity;
import com.u1city.u1pluginframework.core.error.PluginActivityNotFindException;
import com.u1city.u1pluginframework.core.pk.PackageManager;
import com.u1city.u1pluginframework.core.pk.PluginApk;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuzr on 2017/7/13.
 * 暴露出来启动的activity；外部以正常方式启动这个activity，根据启动activity的intent
 * 选择一个需要代理的PluginActivity，当有多个满足的时候，弹出选择框供用户选择
 */

public class ExposeHostActivity extends HostActivity {
    private PackageManager mPackageManager;
    private ActivityDescribe mActivityDescribe;

    private class ActivityDescribe{
        private PluginApk apk;
        private ActivityInfo activityInfo;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        mPackageManager = PackageManager.getInstance(getApplicationContext());
        List<ActivityDescribe> infos = findPluginActivity();
        if(infos.size() == 0){
            //没有找到满足条件的activity
            throw new RuntimeException("没有找到PluginActivity");
        }
        if(infos.size() > 1){
            //有多个满足条件的activity，弹出选择框由用户选择
            showChooseDialog(infos);
            return;
        }
        mActivityDescribe = infos.get(0);
        Intent intent = getIntent();
        intent.putExtra(PluginActivity.KEY_PLUGIN_NAME,mActivityDescribe.apk.getPluginName());
        intent.putExtra(PluginActivity.KEY_PLUGIN_ACTIVITY_INFO,mActivityDescribe.activityInfo);
    }

    /**
     * 查找满足条件的PluginActivity
     * @return 所有满足条件的activity集合
     */
    private List<ActivityDescribe> findPluginActivity(){
        List<ActivityDescribe> results = new ArrayList<>();
        try {
            List<ActivityInfo> activityInfos = mPackageManager.findPluginActivity(getIntent());
            for(ActivityInfo info:activityInfos){
                ActivityDescribe describe = new ActivityDescribe();
                describe.apk = mPackageManager.getPlugin(info.packageName);
                describe.activityInfo = info;
            }
        } catch (PluginActivityNotFindException e) {
            e.printStackTrace();
            //没有找到匹配的activity
            finish();
        }
        return results;
    }

    /**
     * 弹出选择框让用户选择一个activity
     * @param infos 可供选择的activity
     */
    private void showChooseDialog(List<ActivityDescribe> infos){

    }
}
