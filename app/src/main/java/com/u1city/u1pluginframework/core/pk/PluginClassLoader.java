package com.u1city.u1pluginframework.core.pk;

import android.text.TextUtils;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dalvik.system.DexClassLoader;

/**
 * 插件类加载器,查找的顺序是：本插件对应的代码->依次查找它依赖的插件的代码
 * Created by wuzr on 2016/12/6.
 */
public class PluginClassLoader extends DexClassLoader {

    //依赖的插件的classLoader
    private List<ClassLoader> otherLoaders = new ArrayList<>(0);
    private Map<String,ClassLoader> classLoaderByPackage = new HashMap<>(0);
    private Set<String> packageNames = new HashSet<>();

    public PluginClassLoader(String dexPath, String optimizedDirectory,
                             String libraryPath, ClassLoader parent) {
        super(dexPath, optimizedDirectory, libraryPath, parent);
    }

    public void addOtherLoader(ClassLoader loader) {
        if (otherLoaders == null) {
            otherLoaders = new ArrayList<>(3);
        }
        otherLoaders.add(loader);
    }

    public void addDependency(String pName){
        if(TextUtils.isEmpty(pName)){
            return;
        }
        packageNames.add(pName);
    }

    public void invalidateDependency(String pName){
        if(TextUtils.isEmpty(pName)){
            return;
        }
        ClassLoader loader = classLoaderByPackage.remove(pName);
        otherLoaders.remove(loader);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> clazz = null;
        try {
            clazz = super.findClass(name);
        } catch (Throwable e) {
            //do nothing
        }
        if (clazz == null) {
            clazz = findFromOthers(name);
        }
        if (clazz != null) {
            return clazz;
        }
        throw new ClassNotFoundException(name);
    }

    //从依赖中查找
    private Class<?> findFromOthers(String name) throws ClassNotFoundException {
        //更新classLoader
        if(packageNames.size() > classLoaderByPackage.size()){
            for(String pName:packageNames){
                ClassLoader newLoader = classLoaderByPackage.get(pName);
                if(newLoader == null){
                    PluginApk apk = PackageManager.getInstance(null).getPlugin(pName);
                    if(apk == null){
                        //为null则pName对应的插件不存在
                        packageNames.remove(pName);
                        classLoaderByPackage.remove(pName);
                        continue;
                    }
                    newLoader = apk.getClassLoader();
                    classLoaderByPackage.put(pName,newLoader);
                    otherLoaders.add(newLoader);
                }
            }
        }
        for (ClassLoader loader : otherLoaders) {
            Class<?> clazz = loader.loadClass(name);
            if (clazz != null) {
                return clazz;
            }
        }
        return null;
    }

    @Override
    protected URL findResource(String name) {
        URL result = super.findResource(name);
        if (result != null) {
            return result;
        }
        for (ClassLoader loader : otherLoaders) {
            result = loader.getResource(name);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @SuppressWarnings("unused")
    @Override
    protected Enumeration<URL> findResources(String resName) {
        Enumeration<URL> results = null;
        results = super.findResources(resName);
        if (results != null && results.hasMoreElements()) {
            return results;
        }
        for (ClassLoader loader : otherLoaders) {
            try {
                results = loader.getResources(resName);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (results != null && results.hasMoreElements()) {
                return results;
            }
        }
        return null;
    }

    @Override
    public Class<?> loadClass(String className)
            throws ClassNotFoundException {
        Class<?> clazz = super.loadClass(className);
        return clazz;
    }
}
