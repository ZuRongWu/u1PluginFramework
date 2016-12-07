package com.u1city.u1pluginframework.core.pk;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ServiceInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;

import com.u1city.u1pluginframework.core.PluginIntent;
import com.u1city.u1pluginframework.core.PluginManager;
import com.u1city.u1pluginframework.core.error.PluginActivityNotFindException;
import com.u1city.u1pluginframework.core.error.UpLevelException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
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
    private ConfigParser configParser;
    private DependencyLoader dependencyLoader;

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
        configParser = new BaseConfigParser();
        dependencyLoader = new BaseDependencyLoader();
    }

    public ActivityInfo findPluginActivity(String compnentName,PluginApk apk) throws PluginActivityNotFindException{
        //暂时不支持隐式启动
        //首先在本插件中查找
        for(ActivityInfo ai:apk.getPackageInfo().activities){
            if(ai.name.equals(compnentName)){
                return ai;
            }
        }
        //如果没找到在它所依赖的插件中查找
        List<PluginApk.Dependency> dependencies = apk.getDependencies();
        if(dependencies != null){
            for(PluginApk.Dependency dependency:dependencies){
                PluginApk dpapk = getPlugin(dependency.name);
                if(dpapk != null){
                    //正常情况应该一定可以执行到这里
                    for(ActivityInfo ai:dpapk.getPackageInfo().activities){
                        if(ai.name.equals(compnentName)){
                            return ai;
                        }
                    }
                }
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
        //把插件包拷贝到data/data/packageName/plugin/目录下
        File from = new File(pluginPath);
        FileInputStream in = new FileInputStream(from);
        File to = new File(pluginBaseDir + apk.getPluginName() + ".apk");
        FileOutputStream out = new FileOutputStream(to);
        int len;
        byte[] buffer = new byte[1024];
        while ((len = in.read(buffer)) != -1){
            out.write(buffer,0,len);
        }
        in.close();
        out.close();
        //设置apkPath
        apk.setApkPath(to.getAbsolutePath());
        //初始化Plugin的Resource
        Class<AssetManager> assetClazz = AssetManager.class;
        Constructor<AssetManager> assetCons = assetClazz.getConstructor();
        assetCons.setAccessible(true);
        AssetManager asset = assetCons.newInstance();
        Method addAsset = assetClazz.getDeclaredMethod("addAssetPath",String.class);
        addAsset.setAccessible(true);
        addAsset.invoke(asset, apk.getApkPath());
        Resources hostRes = context.getResources();
        Resources resources = new Resources(asset,hostRes.getDisplayMetrics(),hostRes.getConfiguration());
        apk.setResources(resources);
        /*
        * 在此处解决plugin之间的依赖问题
        * */
        configParser.parse(apk);
        dependencyLoader.load(context,apk);
        //初始化codeDir和nativeLibDir
        String codeDir = pluginBaseDir + apk.getPluginName() + File.separator + pluginCodeSubDir;
        File f = new File(codeDir);
        if(!f.exists()){
            if(!f.mkdirs()){
                throw new RuntimeException("无法创建插件代码路径");
            }
        }
        apk.setCodeDir(codeDir);
        String nativeLibDir = pluginBaseDir + apk.getPluginName() + File.separator + pluginNativeLibSubDir;
        f = new File(nativeLibDir);
        if(!f.exists()){
            if(!f.mkdirs()){
                throw new RuntimeException("无法创建插件原生代码路径");
            }
        }
        apk.setNativeLibDir(nativeLibDir);
        //复制nativeCode到nativeLibDir目录下
        copyNativeLib(apk);
        //初始化classLoader
        DexClassLoader primaryLoader = new DexClassLoader(apk.getApkPath(),codeDir,nativeLibDir,PackageManager.class.getClassLoader());
        PluginClassLoader classLoader = new PluginClassLoader(primaryLoader);
        //把依赖的插件的代码和资源加入到当前插件
        List<PluginApk.Dependency> dependencies = apk.getDependencies();
        if(dependencies != null){
            for(PluginApk.Dependency dependency:dependencies){
                PluginApk dpapk = getPlugin(dependency.name);
                if(dpapk == null){
                    Log.e(TAG,"安装依赖失败：" + dependency.name);
                    return;
                }
                dpapk.addDepended(apk.getPluginName());
                apk.addResources(dpapk.getPluginName(),dpapk.getResources());
                classLoader.addOtherLoader(dpapk.getClassLoader());
            }
        }
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

        PluginApk apk = pluginsByName.get(pluginName);
        //检查是否有插件依赖此插件
        if(apk.getDependended() != null&&apk.getDependended().size() > 0){
            String logMsg = "不可以卸载插件，有插件依赖此插件：";
            for(String dName:apk.getDependended()){
                logMsg = logMsg + "[" +dName + "]";
            }
            Log.w(TAG,logMsg);
            return;
        }
        //删除与这个插件关联的文件:code;nativeLib;插件apk文件
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
        //卸载插件apk依赖的插件
        if(apk.getDependencies() != null&&apk.getDependencies().size() > 0){
            for(PluginApk.Dependency dependency:apk.getDependencies()){
                PluginManager.getInstance(context).uninstallPlugin(dependency.name);
            }
        }
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

    /**
     * 把nativeLib 复制到nativeLibDir目录下
     * @param apk 表示安装的插件apk，里面存有nativeLib的原始路径和目标路径
     */
    private void copyNativeLib(PluginApk apk) throws Exception{
        ZipFile apkFile = new ZipFile(apk.getApkPath());
        String supportAbi = Build.CPU_ABI;
        Enumeration entrys = apkFile.entries();
        ZipEntry entry;
        while (entrys.hasMoreElements()){
            entry = (ZipEntry) entrys.nextElement();
            if(entry.getName().endsWith(".so")&&entry.getName().contains(supportAbi)){
                int index = entry.getName().indexOf(File.separator);
                String libName = entry.getName().substring(index + 1);
                FileOutputStream out = new FileOutputStream(new File(apk.getNativeLibDir() + libName));
                InputStream in = apkFile.getInputStream(entry);
                int len;
                byte[] buffers = new byte[1024];
                while((len = in.read(buffers)) != -1){
                    out.write(buffers,0,len);
                }
            }
        }
    }
}
