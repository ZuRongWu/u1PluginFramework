package com.u1city.u1pluginframework.core.activity.host;

import android.content.pm.ActivityInfo;

import com.u1city.u1pluginframework.core.activity.host.HostActivity;
import com.u1city.u1pluginframework.core.activity.host.HostChoosePolicy;

/**
 * 默认的宿主activity选择策略，默认只有一种宿主activity，以后会扩展
 * Created by wuzr on 2016/12/2.
 */
public class DefaultHostChoosePolicy implements HostChoosePolicy {
    @Override
    public Class<HostActivity>choose(ActivityInfo ai) {
        return HostActivity.class;
    }
}