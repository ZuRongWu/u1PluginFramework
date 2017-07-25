package com.u1city.u1pluginframework.core.pm;

import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * plugin apk
 * Created by wuzr on 2016/12/2.
 */
public class PluginApk {

    /*package*/ static class Dependency {
        public String name;
        public String path;
    }

    /*package*/ static class Package {
        String packageName;
        boolean baseHardwareAccelerated;
        ApplicationInfo applicationInfo;
        int versionCode;
        String versionName;
        final ArrayList<Activity> activities = new ArrayList<>(0);
        final ArrayList<Activity> receivers = new ArrayList<>(0);
        final ArrayList<Provider> providers = new ArrayList<>(0);
        final ArrayList<Service> services = new ArrayList<>(0);
    }

    /*package*/ static class Service extends Component<IntentFilter> {
        ServiceInfo serviceInfo;
    }

    /*package*/ static class Provider extends Component<IntentFilter> {
        ProviderInfo providerInfo;
    }

    /*package*/ static class Activity extends Component<IntentFilter> {
        ActivityInfo activityInfo;
    }

    /*package*/ static class Component<II extends IntentFilter> {
        Package owner;
        List<II> intents = new ArrayList<>();
        String className;
        Bundle metaData;
    }

    private static final String TAG = "PluginApk";

    private PackageManager packageManager;
    private Package pluginPackage;
    private String pluginName;
    private Resources resources;
    private ClassLoader classLoader;
    private String codeDir;
    private String nativeLibDir;
    private String resDir;
    private String apkPath;
    private List<Dependency> dependencies;
    //依赖此插件的插件名称
    private List<String> dependended;

    /**
     * @return 插件包包名
     */
    public String getPackageName() {
        return pluginPackage.packageName;
    }

    public ApplicationInfo getApplicationInfo() {
        return pluginPackage.applicationInfo;
    }

    /**
     * @return 插件包所有的activity信息
     */
    public List<Activity> getPluginActivities() {
        return pluginPackage.activities;
    }

    /**
     * @return 插件包所有的service信息
     */
    public List<Service> getPluginServices() {
        return pluginPackage.services;
    }

    /**
     * @return 插件包所有ContentProvider信息
     */
    public List<Provider> getPluginProviders() {
        return pluginPackage.providers;
    }

    /**
     * @return 插件包所有的BroadCastReciever信息
     */
    public List<Activity> getPluginReceivers() {
        return pluginPackage.receivers;
    }

    /**
     * @return 插件包的versionCode
     */
    public int getVersionCode() {
        return pluginPackage.versionCode;
    }

    public String getVersionName() {
        return pluginPackage.versionName;
    }

    /**
     * @return 插件包是否开启硬件插件
     */
    public boolean baseHardwareAcceleratedEnable() {
        return pluginPackage.baseHardwareAccelerated;
    }

    /**
     * @param pluginPackage 插件包描述信息
     */
    public void setPluginPackage(Package pluginPackage) {
        this.pluginPackage = pluginPackage;
    }

    /**
     * 添加依赖此插件apk的插件名称
     * @param pluginName 插件名称
     */
    public void addDepended(String pluginName) {
        if (dependended == null) {
            dependended = new ArrayList<>(3);
        }
        dependended.add(pluginName);
    }

    /**
     * 获取所有依赖此插件的插件名称列表
     * @return 插件名称列表
     */
    public List<String> getDependended() {
        return dependended;
    }

    /**
     * 获取此插件依赖的插件信息
     * @return 插件信息列表
     */
    public List<Dependency> getDependencies() {
        return dependencies;
    }

    /**
     * 设置此插件依赖的插件信息
     * @param dependencies 插件信息列表
     */
    public void setDependencies(List<Dependency> dependencies) {
        this.dependencies = dependencies;
    }

    /**
     * 设置apk的路径
     * @param apkPath apk的路径
     */
    public void setApkPath(String apkPath) {
        this.apkPath = apkPath;
    }

    /**
     * 获取apk的路径
     * @return apk的路径
     */
    public String getApkPath() {
        return apkPath;
    }

    /**
     * 设置nativeLib的目录
     * @param nativeLibDir nativeLib的目录
     */
    public void setNativeLibDir(String nativeLibDir) {
        this.nativeLibDir = nativeLibDir;
    }

    /**
     * 获取nativeLib的目录
     * @return nativeLib的目录
     */
    public String getNativeLibDir() {
        return nativeLibDir;
    }

    /**
     * 获取插件的资源Resources对象，只有通过这个对象访问插件的资源
     * @return Resources对象
     */
    public Resources getResources() {
        return resources;
    }

    /**
     * 设置插件的Resources对象，在安装插件的时候完成设置
     * @param resources Resources对象
     */
    public void setResources(Resources resources) {
        this.resources = resources;
    }

    /**
     * 获取插件的类加载器，使用插件的类都要通过这个加载器加载
     * @return classLoader对象
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * 设置classLoader对象，在安装插件的时候进行设置
     * @param classLoader classLoader对象
     */
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * 获取插件的代码路径
     * @return 代码路径
     */
    public String getCodeDir() {
        return codeDir;
    }

    /**
     * 设置插件的代码路径，安装的时候完成设置
     * @param codeDir 代码路径
     */
    public void setCodeDir(String codeDir) {
        this.codeDir = codeDir;
    }

    /**
     * 获取插件的资源路径
     * @return 资源路径
     */
    public String getResDir() {
        return resDir;
    }

    /**
     * 设置插件的资源路径，目前没有设置这路径。所以通过{@link PluginApk#getResDir()}获取到的路径为空
     * @param resDir 资源路径
     */
    public void setResDir(String resDir) {
        this.resDir = resDir;
    }

    /**
     * 获取插件名称，目前设置为和包名一样
     * @return 插件名称
     */
    public String getPluginName() {

        return pluginName;
    }

    /**
     * 设置packageManager对象
     * @param packageManager 对象
     */
    public void setPackageManager(PackageManager packageManager) {
        this.packageManager = packageManager;
    }

    /**
     * 设置插件名称，在安装插件时调用，目前设置为和包名一样
     * @param pluginName 插件名称
     */
    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    /**
     * 根据插件名称获取相应插件apk的资源对象，只能获取此插件依赖的插件的资源对象
     * @param pluginName 插件名称
     * @return 资源对象
     */
    public Resources getResources(String pluginName) {
        boolean isFound = false;
        for (Dependency d : dependencies) {
            if (d.name.equals(pluginName)) {
                isFound = true;
                break;
            }
        }
        if (!isFound) {
            Log.w(TAG, "没有找到resource");
            return null;
        }
        return packageManager.getPlugin(pluginName).getResources();
    }
}
