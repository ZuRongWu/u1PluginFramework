package com.u1city.u1pluginframework.core;

import android.content.pm.ActivityInfo;
import android.content.pm.ServiceInfo;

import com.u1city.u1pluginframework.core.activity.HostActivity;
import com.u1city.u1pluginframework.core.service.HostService;

/**
 * 1. 根据PluginActivity 的ActivityInfo选择适当的宿主activity
 * 2. 根据PluginService的ServiceInfo选择适当的宿主Service
 * Created by wuzr on 2016/12/2.
 */
interface HostChoosePolicy {
    Class<? extends HostActivity> chooseHostActivity(ActivityInfo ai);

    Class<? extends HostService> chooseHostService(ServiceInfo si);
}
