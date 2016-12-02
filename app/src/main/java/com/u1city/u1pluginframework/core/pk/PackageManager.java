package com.u1city.u1pluginframework.core.pk;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ServiceInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

import com.u1city.u1pluginframework.core.PluginIntent;
import com.u1city.u1pluginframework.core.error.PluginActivityNotFindException;
import com.u1city.u1pluginframework.core.error.UpLevelException;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import dalvik.system.DexClassLoader;

/**
 * package manager
 * Created by wuzr on 2016/12/2.
 */
public class PackageManager {
    private static final String TAG = "PackageManager";
    private static PackageManager sPackageManager;

    /*插件的根目录:data/data/packageName/plugin/*/
    private String pluginBaseDir;
    /*插件代码的安装目录：data/data/packageName/plugin/pluginName/code/*/
    private String pluginCodeSubDir = "code/";
    /*插件原生代码的安装目录:data/data/packageName/plugin/pluginName/nativeLib/*/
    private String pluginNativeLibSubDir = "nativeLib/";
    private Map<String,PluginApk> pluginsByName;
    private Context context;

    public static PackageManager getInstance(Context context){
        if(sPackageManager == null){
            synchronized (PackageManager.class){
                if(sPackageManager == null){
                    sPackageManager = new PackageManager(context);
                }
            }
        }
        return sPackageManager;
    }

    private PackageManager(Context context){
        //单例
        this.context = context;
        pluginBaseDir = context.getFilesDir().getParent() + "/plugin/";
        File f = new File(pluginBaseDir);
        if(!f.exists()){
            if(!f.mkdirs()){
                throw new RuntimeException("无法创建插件目录");
            }
        }
        pluginsByName = new HashMap<>();
    }

    public ActivityInfo findPluginActivity(String compnentName,PluginApk apk) throws PluginActivityNotFindException{
        //暂时不支持隐式启动

        for(ActivityInfo ai:apk.getPackageInfo().activities){
            if(ai.targetActivity.equals(compnentName)){
                return ai;
            }
        }
        throw new PluginActivityNotFindException(compnentName);
    }

    public ServiceInfo findPluginService(PluginIntent intent){
        throw new RuntimeException("此方法暂未实现");
    }

    /**
     * 安装插件,供内部调用，外部应该调用{@link com.u1city.u1pluginframework.core.PluginManager#installPlugin(String)}
     * @param pluginPath 插件包的绝对路径
     * @throws Exception
     */
    public void installPlugin(String pluginPath) throws Exception {
        PluginApk apk = new PluginApk();
        android.content.pm.PackageManager pm = context.getPackageManager();
        PackageInfo packageInfo = pm.getPackageArchiveInfo(pluginPath,
                android.content.pm.PackageManager.GET_ACTIVITIES | android.content.pm.PackageManager.GET_SERVICES);
        apk.setPackageInfo(packageInfo);
        apk.setPluginName(packageInfo.packageName);
        //检查此插件是否已经安装，如果已经安装则不再安装
        if(pluginsByName.get(apk.getPluginName()) != null){
            Log.w(TAG,"插件" + pluginPath + "已安装");
            return;
        }
        //设置apkPath
        apk.setApkPath(pluginPath);
        //初始化Plugin的Resource
        Class<AssetManager> assetClazz = AssetManager.class;
        Constructor<AssetManager> assetCons = assetClazz.getConstructor();
        assetCons.setAccessible(true);
        AssetManager asset = assetCons.newInstance();
        Method addAsset = assetClazz.getDeclaredMethod("addAssetPath",String.class);
        addAsset.setAccessible(true);
        addAsset.invoke(asset, pluginPath);
        Resources hostRes = context.getResources();
        Resources resources = new Resources(asset,hostRes.getDisplayMetrics(),hostRes.getConfiguration());
        apk.setResources(resources);
        //初始化codeDir和nativeLibDir
        String codeDir = pluginBaseDir + apk.getPluginName() + pluginCodeSubDir;
        File f = new File(codeDir);
        if(!f.exists()){
            if(!f.mkdirs()){
                throw new RuntimeException("无法创建插件代码路径");
            }
        }
        apk.setCodeDir(codeDir);
        String nativeLibDir = pluginBaseDir + apk.getPluginName() + pluginNativeLibSubDir;
        f = new File(nativeLibDir);
        if(!f.exists()){
            if(!f.mkdirs()){
                throw new RuntimeException("无法创建插件原生代码路径");
            }
        }
        apk.setNativeLibDir(nativeLibDir);
        //初始化classLoader
        DexClassLoader classLoader = new DexClassLoader(pluginPath,codeDir,nativeLibDir,PackageManager.class.getClassLoader());
        apk.setClassLoader(classLoader);
        //保存plugin
        pluginsByName.put(apk.getPluginName(),apk);
    }

    /**
     * 卸载插件
     * @param pluginName 指定要卸载的插件
     */
    public void uninstallPlugin(String pluginName){
        //检查是否安装过pluginName指定的插件，如果没有安装则直接返回
        if(pluginsByName.get(pluginName) == null){
            Log.w(TAG,"没有安装此插件");
            return;
        }

        //删除与这个插件关联的文件:code;nativeLib;插件apk文件
        PluginApk apk = pluginsByName.get(pluginName);
        File apkFile = new File(apk.getApkPath());
        if(!apkFile.delete()){
            Log.w(TAG,"删除文件" + apkFile.getAbsolutePath() + "失败");
        }
        File codeFile = new File(apk.getCodeDir());
        if(!codeFile.delete()){
            Log.w(TAG,"删除文件" + codeFile.getAbsolutePath() + "失败");
        }
        File nativeLib = new File(apk.getNativeLibDir());
        if(!nativeLib.delete()){
            Log.w(TAG,"删除文件" + nativeLib.getAbsolutePath() + "失败");
        }

        //从map中移除apk
        pluginsByName.remove(pluginName);
    }

    /**
     * 更新插件，如果newPlugin指定的插件已安装则卸载旧插件，再安装先插件，
     * 如果newPlugin指定的插件没有安装过则直接安装此插件
     * tip：只有当新插件的versionCode大于旧插件的versionCode才执行安装
     * 否则抛出UpLevelException
     * @param newPlugin 新插件的绝对路径
     */
    public void updatePlugin(String newPlugin) throws Exception{
        android.content.pm.PackageManager pm = context.getPackageManager();
        PackageInfo packageInfo = pm.getPackageArchiveInfo(newPlugin,
                android.content.pm.PackageManager.GET_ACTIVITIES | android.content.pm.PackageManager.GET_SERVICES);
        if(!pluginsByName.containsKey(packageInfo.packageName)){
            installPlugin(newPlugin);
            return;
        }
        int newVersion = packageInfo.versionCode;
        int oldVersion = pluginsByName.get(packageInfo.packageName).getPackageInfo().versionCode;
        if(newVersion <= oldVersion){
            throw new UpLevelException();
        }
        uninstallPlugin(packageInfo.packageName);
        installPlugin(newPlugin);
    }

    public PluginApk getPlugin(String pluginName){
        return pluginsByName.get(pluginName);
    }
}
