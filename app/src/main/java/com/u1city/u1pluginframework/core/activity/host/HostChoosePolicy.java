package com.u1city.u1pluginframework.core.activity.host;

import android.content.pm.ActivityInfo;

import com.u1city.u1pluginframework.core.activity.host.HostActivity;

/**
 * 根据PluginActivity 的ActivityInfo选择适当的宿主activity
 * Created by wuzr on 2016/12/2.
 */
public interface HostChoosePolicy {
    Class<HostActivity> choose(ActivityInfo ai);
}
