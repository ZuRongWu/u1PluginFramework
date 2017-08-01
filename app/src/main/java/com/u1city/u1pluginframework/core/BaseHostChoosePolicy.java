package com.u1city.u1pluginframework.core;

import android.content.pm.ActivityInfo;
import android.content.pm.ServiceInfo;

import com.u1city.u1pluginframework.core.activity.HostActivity;
import com.u1city.u1pluginframework.core.service.HostService;

/**
 * 默认的宿主activity选择策略，默认只有一种宿主activity
 * 默认的宿主service选择策略，默认只有一种宿主service
 * 可以继承此类进行扩展
 * Created by wuzr on 2016/12/2.
 */
class BaseHostChoosePolicy implements HostChoosePolicy {
    @Override
    public Class<? extends HostActivity> chooseHostActivity(ActivityInfo ai) {
        return HostActivity.class;
    }

    @Override
    public Class<? extends HostService> chooseHostService(ServiceInfo si) {
        return HostService.class;
    }
}
