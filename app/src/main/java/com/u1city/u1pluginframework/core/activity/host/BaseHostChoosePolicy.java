package com.u1city.u1pluginframework.core.activity.host;

import android.content.pm.ActivityInfo;

/**
 * 默认的宿主activity选择策略，默认只有一种宿主activity，以后会扩展
 * Created by wuzr on 2016/12/2.
 */
public class BaseHostChoosePolicy implements HostChoosePolicy {
    @Override
    public Class<? extends HostActivity> choose(ActivityInfo ai) {
        return HostActivity.class;
    }
}
