package com.u1city.u1pluginframework.core.pk;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * 插件类加载器
 * Created by wuzr on 2016/12/6.
 */
public class PluginClassLoader extends ClassLoader{

    //依赖的插件的classLoader
    private List<ClassLoader> otherLoaders;

    public PluginClassLoader(ClassLoader parent) {
        super(parent);
    }
    public void addOtherLoader(ClassLoader loader){
        if(otherLoaders == null){
            otherLoaders = new ArrayList<>(3);
        }
        otherLoaders.add(loader);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> clazz;
        for(ClassLoader loader:otherLoaders){
            clazz = loader.loadClass(name);
            if(clazz != null){
                return clazz;
            }
        }
        throw new ClassNotFoundException(name);
    }

    @Override
    protected URL findResource(String name) {
        URL result = super.getResource(name);
        if(result != null){
            return result;
        }
        for (ClassLoader loader:otherLoaders){
            result = loader.getResource(name);
            if(result != null){
                return result;
            }
        }
        return null;
    }

    @SuppressWarnings("unused")
    @Override
    protected Enumeration<URL> findResources(String resName) throws IOException {
        Enumeration<URL> results = super.getResources(resName);
        if(results != null&&results.hasMoreElements()){
            return results;
        }
        for(ClassLoader loader:otherLoaders){
            results = loader.getResources(resName);
            if(results != null&&results.hasMoreElements()){
                return results;
            }
        }
        return null;
    }

    @Override
    public URL getResource(String resName) {
        return findResource(resName);
    }

    @Override
    public Class<?> loadClass(String className)
            throws ClassNotFoundException {
        Class<?> clazz = super.loadClass(className);
        return clazz;
    }

    @Override
    public Enumeration<URL> getResources(String resName) throws IOException {
        return findResources(resName);
    }
}
