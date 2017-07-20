package com.u1city.u1pluginframework.core.pk;

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

    public static class Package {
        public String packageName;
        public boolean baseHardwareAccelerated;
        public ApplicationInfo applicationInfo;
        public int versionCode;
        public String versionName;
        public final ArrayList<Activity> activities = new ArrayList<>(0);
        public final ArrayList<Activity> receivers = new ArrayList<>(0);
        public final ArrayList<Provider> providers = new ArrayList<>(0);
        public final ArrayList<Service> services = new ArrayList<>(0);
    }

    public static class Service extends Component<IntentFilter> {
        public ServiceInfo serviceInfo;
    }

    public static class Provider extends Component<IntentFilter> {
        public ProviderInfo providerInfo;
    }

    public static class Activity extends Component<IntentFilter> {
        public ActivityInfo activityInfo;
    }

    public static class Component<II extends IntentFilter> {
        public Package owner;
        public List<II> intents = new ArrayList<>();
        public String className;
        public Bundle metaData;
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
    public List<Activity> getPluginRecievers() {
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

    public void addDepended(String pluginName) {
        if (dependended == null) {
            dependended = new ArrayList<>(3);
        }
        dependended.add(pluginName);
    }

    public List<String> getDependended() {
        return dependended;
    }

    public List<Dependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<Dependency> dependencies) {
        this.dependencies = dependencies;
    }

    public void setApkPath(String apkPath) {
        this.apkPath = apkPath;
    }

    public String getApkPath() {
        return apkPath;
    }

    public void setNativeLibDir(String nativeLibDir) {
        this.nativeLibDir = nativeLibDir;
    }

    public String getNativeLibDir() {
        return nativeLibDir;
    }

    public Resources getResources() {
        return resources;
    }

    public void setResources(Resources resources) {
        this.resources = resources;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public String getCodeDir() {
        return codeDir;
    }

    public void setCodeDir(String codeDir) {
        this.codeDir = codeDir;
    }

    public String getResDir() {
        return resDir;
    }

    public void setResDir(String resDir) {
        this.resDir = resDir;
    }

    public String getPluginName() {

        return pluginName;
    }

    public void setPackageManager(PackageManager packageManager) {
        this.packageManager = packageManager;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

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
