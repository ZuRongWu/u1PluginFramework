package com.u1city.u1pluginframework.core.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Parcelable;

import com.u1city.u1pluginframework.core.PluginIntent;
import com.u1city.u1pluginframework.core.error.PluginActivityNotFindException;
import com.u1city.u1pluginframework.core.pk.PackageManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuzr on 2017/7/13.
 * 暴露出来启动的activity；外部以正常方式启动这个activity，根据启动activity的intent
 * 选择一个需要代理的PluginActivity，当有多个满足的时候，弹出选择框供用户选择
 */

public class ExposeHostActivity extends HostActivity {
    private PackageManager mPackageManager;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        mPackageManager = PackageManager.getInstance(getApplicationContext());
        List<ActivityInfo> infos = findPluginActivity();
        if(infos == null||infos.size() == 0){
            //没有找到满足条件的activity
            throw new RuntimeException("没有找到PluginActivity：" + getIntent());
        }
        if(infos.size() > 1){
            //有多个满足条件的activity，弹出选择框由用户选择
            showChooseDialog(infos);
            return;
        }
        Intent intent = getIntent();
        intent.putExtra(PluginActivity.KEY_PLUGIN_NAME,infos.get(0).packageName);
        intent.putExtra(PluginActivity.KEY_PLUGIN_ACTIVITY_INFO,infos.get(0));
    }

    /**
     * 查找满足条件的PluginActivity
     * @return 所有满足条件的activity集合
     */
    private List<ActivityInfo> findPluginActivity(){
        try {
            return mPackageManager.findPluginActivity(getIntent());
        } catch (PluginActivityNotFindException e) {
            e.printStackTrace();
            finish();
        }
        return null;
    }

    /**
     * 弹出选择框让用户选择一个activity
     * @param infos 可供选择的activity
     */
    private void showChooseDialog(List<ActivityInfo> infos){
        PluginIntent intent = new PluginIntent(getIntent());
        intent.addPluginFlag(PluginIntent.FLAG_LAUNCH_ACTUAL_ACTIVITY);
        intent.putParcelableArrayListExtra(ChooseActivityDialog.KEY_ACTIVITIES, (ArrayList<? extends Parcelable>) infos);
        startActivity(intent);
        finish();
    }
}
