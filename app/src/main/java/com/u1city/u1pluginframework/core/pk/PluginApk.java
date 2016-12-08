package com.u1city.u1pluginframework.core.pk;

import android.content.pm.PackageInfo;
import android.content.res.Resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * plugin apk
 * Created by wuzr on 2016/12/2.
 */
public class PluginApk {

    public static class Dependency{
        public String name;
        public String path;
    }

    private PackageInfo packageInfo;
    private String pluginName;
    private Resources resources;
    private ClassLoader classLoader;
    private String codeDir;
    private String nativeLibDir;
    private String resDir;
    private String apkPath;
    private List<Dependency> dependencies;
    private Map<String,Resources> otherResources = new HashMap<>(3);
    //依赖此插件的插件名称
    private List<String> dependended;

    public void addDepended(String pluginName){
        if(dependended == null){
            dependended = new ArrayList<>(3);
        }
        dependended.add(pluginName);
    }

    public List<String> getDependended(){
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

    public void setPackageInfo(PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
    }

    public void setNativeLibDir(String nativeLibDir) {
        this.nativeLibDir = nativeLibDir;
    }

    public String getNativeLibDir() {
        return nativeLibDir;
    }

    public PackageInfo getPackageInfo() {
        return packageInfo;
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

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    public Resources getResources(String pluginName){
        return otherResources.get(pluginName);
    }

    public void addResources(String pluginName,Resources resources){
        otherResources.put(pluginName,resources);
    }
}
