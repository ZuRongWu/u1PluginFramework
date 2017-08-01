package com.u1city.u1pluginframework.core;

import android.content.Context;

import com.u1city.u1pluginframework.core.activity.HostActivity;
import com.u1city.u1pluginframework.core.pm.PluginApk;
import com.u1city.u1pluginframework.core.service.HostService;

import java.lang.reflect.Field;

/**
 * Created by wuzr on 2017/7/31.
 * 通过反射替换{@link android.content.ContextWrapper#mBase}
 */

public class ContextTransverter {

    public static HostService transformService(PluginApk apk, HostService origin){
        try {
            Class clazz= Class.forName("android.content.ContextWrapper");
            Field base = clazz.getDeclaredField("mBase");
            base.setAccessible(true);
            ContextDelegate delegate = new ContextDelegate(apk, (Context) base.get(origin));
            base.set(origin,delegate);
        } catch (Exception e) {
            throw new RuntimeException("hook ContextImpl 失败：" + e.getMessage());
        }
        return origin;
    }

    public static HostActivity transformActivity(PluginApk apk, HostActivity origin){
        try {
            Class clazz= Class.forName("android.content.ContextWrapper");
            Field base = clazz.getDeclaredField("mBase");
            base.setAccessible(true);
            ContextDelegate delegate = new ContextDelegate(apk, origin.getBaseContext());
            base.set(origin,delegate);
            //ContextThemeWrapper会保存mResources对象，需要将它替换
            clazz = Class.forName("android.view.ContextThemeWrapper");
            Field resources = clazz.getDeclaredField("mResources");
            resources.setAccessible(true);
            resources.set(origin,delegate.getResources());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return origin;
    }
}
