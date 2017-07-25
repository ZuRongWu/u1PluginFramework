package com.u1city.u1pluginframework.core.pm;

import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.u1city.u1pluginframework.core.PluginIntent;
import com.u1city.u1pluginframework.core.PluginManager;
import com.u1city.u1pluginframework.core.error.PluginActivityNotFindException;
import com.u1city.u1pluginframework.core.error.PluginServiceNotFindException;
import com.u1city.u1pluginframework.core.error.UpLevelException;
import com.u1city.u1pluginframework.utils.ReflectUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * package manager
 * Created by wuzr on 2016/12/2.
 */
public class PackageManager implements ComponentCallbacks {
    private static final String TAG = "PackageManager";
    private static PackageManager sPackageManager;

    /*插件的根目录:data/data/packageName/plugin/*/
    private String pluginBaseDir;
    /*插件代码的安装目录：data/data/packageName/plugin/pluginName/code/*/
    private static final String pluginCodeSubDir = "code/";
    /*插件原生代码的安装目录:data/data/packageName/plugin/pluginName/nativeLib/*/
    private static final String pluginNativeLibSubDir = "nativeLib/";
    private Map<String, PluginApk> pluginsByName;
    private Context context;
    private ConfigParser configParser;
    private DependencyLoader dependencyLoader;

    public static PackageManager getInstance(Context context) {
        if (sPackageManager == null) {
            synchronized (PackageManager.class) {
                if (sPackageManager == null) {
                    sPackageManager = new PackageManager(context);
                }
            }
        }
        return sPackageManager;
    }

    private PackageManager(Context context) {
        //单例
        this.context = context.getApplicationContext();
        pluginBaseDir = context.getFilesDir().getParent() + "/plugin/";
        File f = new File(pluginBaseDir);
        if (!f.exists()) {
            if (!f.mkdirs()) {
                throw new RuntimeException("无法创建插件目录");
            }
        }
        pluginsByName = new HashMap<>();
        configParser = new BaseConfigParser();
        dependencyLoader = new BaseDependencyLoader();
        this.context.registerComponentCallbacks(this);
    }

    public String getPluginBaseDir() {
        return pluginBaseDir;
    }

    /**
     * 从插件中查找activity
     *
     * @param intent intent
     * @return 匹配的activityInfo数组
     * @throws PluginActivityNotFindException 没有找到匹配的activity时抛出异常
     */
    public List<ActivityInfo> findPluginActivity(Intent intent) throws PluginActivityNotFindException {
        List<ActivityInfo> activityInfos = new ArrayList<>();
        if (intent instanceof PluginIntent) {
            PluginIntent pIntent = (PluginIntent) intent;
            String pluginName = pIntent.getPluginName();
            String componentName = pIntent.getPluginComponentName();
            //处理显式启动
            if (!TextUtils.isEmpty(pluginName) && !TextUtils.isEmpty(componentName)) {
                //如果pluginName和componentName都不为空则是显式启动
                if (componentName.startsWith(".")) {
                    componentName = pluginName + componentName;
                }
                PluginApk apk = getPlugin(pluginName);
                if (apk == null) {
                    throw new PluginActivityNotFindException(String.format("没有找到匹配的activity，pluginName：%s；componentName：%s", pluginName, componentName));
                }
                List<PluginApk.Activity> activities = apk.getPluginActivities();
                for (PluginApk.Activity activity : activities) {
                    if (activity.className.equals(componentName)) {
                        activityInfos.add(activity.activityInfo);
                        return activityInfos;
                    }
                }
                //如果没找到，从依赖中查找
                List<PluginApk.Dependency> dependencies = apk.getDependencies();
                if (dependencies != null) {
                    for (PluginApk.Dependency dependency : dependencies) {
                        PluginApk dpapk = getPlugin(dependency.name);
                        if (dpapk != null) {
                            //正常情况应该一定可以执行到这里
                            for (PluginApk.Activity activity : dpapk.getPluginActivities()) {
                                if (activity.className.equals(componentName)) {
                                    activityInfos.add(activity.activityInfo);
                                    return activityInfos;
                                }
                            }
                        }
                    }
                }
                throw new PluginActivityNotFindException(String.format("没有找到匹配的activity，pluginName：%s；componentName：%s", pluginName, componentName));
            }
            //处理隐式启动
            if (!TextUtils.isEmpty(componentName)) {
                //pluginName为空componentName不为空时，从所有插件中查找与componentName匹配的activity
                List<PluginApk> apks = getPlugins();
                for (PluginApk apk : apks) {
                    for (PluginApk.Activity activity : apk.getPluginActivities()) {
                        if (activity.className.equals(componentName)) {
                            activityInfos.add(activity.activityInfo);
                        }
                    }
                }
                if (activityInfos.size() > 0) {
                    return activityInfos;
                }
                throw new PluginActivityNotFindException(String.format("没有找到匹配的activity，componentName：%s", componentName));
            }
            if (!TextUtils.isEmpty(pluginName)) {
                //pluginName不为空componentName为空时，从指定的插件apk中查找activity
                PluginApk apk = getPlugin(pluginName);
                if (apk == null) {
                    throw new PluginActivityNotFindException(String.format("没有找到匹配的activity，pluginName：%s", pluginName));
                }
                activityInfos.addAll(resolveActivity(intent, apk));
                if (activityInfos.size() > 0) {
                    return activityInfos;
                }
                throw new PluginActivityNotFindException(String.format("没有找到匹配的activity，pluginName：%s", pluginName));
            }
        }
        List<PluginApk> apks = getPlugins();
        for (PluginApk apk : apks) {
            activityInfos.addAll(resolveActivity(intent, apk));
        }
        if (activityInfos.size() > 0) {
            return activityInfos;
        }
        throw new PluginActivityNotFindException(String.format("没有找到匹配的activity，intent：%s", intent.toString()));
    }

    /**
     * 隐式查找，从指定的apk中查找和intent匹配的activity
     *
     * @param intent intent
     * @param apk    指定的apk
     * @return 所有匹配的activity
     */
    private List<ActivityInfo> resolveActivity(Intent intent, PluginApk apk) {
        List<ActivityInfo> activityInfos = new ArrayList<>();
        /*
            * 获取action，
            * 1.如果intent是PluginIntent，获取PluginIntent的pluginAction，如果为空走2，
            * 2.获取key为PluginIntent.KEY_PLUGIN_ACTION的StringExtra，如果为空走3
            * 3.获取intent的Acton
            * */
        String action = null;
        if(intent instanceof PluginIntent){
            action = ((PluginIntent)intent).getPluginAction();
        }
        if(TextUtils.isEmpty(action)){
            action = intent.getStringExtra(PluginIntent.KEY_PLUGIN_ACTION);
        }
        if(TextUtils.isEmpty(action)){
            action = intent.getAction();
        }
        if (TextUtils.isEmpty(action)) {
            //如果action为空则不再往下找
            return activityInfos;
        }
        String dataType = intent.getType();
        String scheme = intent.getScheme();
        Uri data = intent.getData();
        Set<String> categories = intent.getCategories();
        for (PluginApk.Activity activity : apk.getPluginActivities()) {
            for (IntentFilter filter : activity.intents) {
                int res = filter.match(action, dataType, scheme, data, categories, TAG);
                if ((res & IntentFilter.NO_MATCH_ACTION) == 0 &&
                        (res & IntentFilter.NO_MATCH_CATEGORY) == 0 &&
                        (res & IntentFilter.NO_MATCH_DATA) == 0) {
                    activityInfos.add(activity.activityInfo);
                    break;
                }
            }
        }
        return activityInfos;
    }

    /**
     * 查找所有和intent匹配的receiver
     *
     * @param intent intent
     * @return 所有与intent匹配的receiver列表
     */
    public List<ActivityInfo> findReceivers(Intent intent) {
        List<PluginApk> apks = getPlugins();
        List<ActivityInfo> receivers = new ArrayList<>();
        for (PluginApk apk : apks) {
            /*
            * 获取action，
            * 1.如果intent是PluginIntent，获取PluginIntent的pluginAction，如果为空走2，
            * 2.获取key为PluginIntent.KEY_PLUGIN_ACTION的StringExtra，如果为空走3
            * 3.获取intent的Acton
            * */
            String action = null;
            if(intent instanceof PluginIntent){
                action = ((PluginIntent)intent).getPluginAction();
            }
            if(TextUtils.isEmpty(action)){
                action = intent.getStringExtra(PluginIntent.KEY_PLUGIN_ACTION);
            }
            if(TextUtils.isEmpty(action)){
                action = intent.getAction();
            }
            if (TextUtils.isEmpty(action)) {
                //如果action为空则不再往下找
                return receivers;
            }
            String dataType = intent.getType();
            String scheme = intent.getScheme();
            Uri data = intent.getData();
            Set<String> categories = intent.getCategories();
            for (PluginApk.Activity activity : apk.getPluginReceivers()) {
                for (IntentFilter filter : activity.intents) {
                    int res = filter.match(action, dataType, scheme, data, categories, TAG);
                    if ((res & IntentFilter.NO_MATCH_ACTION) == 0 &&
                            (res & IntentFilter.NO_MATCH_CATEGORY) == 0 &&
                            (res & IntentFilter.NO_MATCH_DATA) == 0) {
                        receivers.add(activity.activityInfo);
                        break;
                    }
                }
            }
        }
        return receivers;
    }

    public ServiceInfo findPluginService(PluginIntent intent) throws PluginServiceNotFindException{
        throw new RuntimeException("此方法暂未实现");
    }

    /**
     * 安装插件,供内部调用，外部应该调用{@link com.u1city.u1pluginframework.core.PluginManager#installPlugin(String)}
     *
     * @param pluginPath 插件包的绝对路径
     */
    public void installPlugin(String pluginPath, boolean shouldCopy) throws Exception {
        PluginApk apk = new PluginApk();
        apk.setPackageManager(this);
        PluginApk.Package pluginPackage = parsePackage(pluginPath);
        if (pluginPackage == null) {
            throw new RuntimeException("解析apk失败" + pluginPath);
        }
        apk.setPluginName(pluginPackage.packageName);
        apk.setPluginPackage(pluginPackage);
        //检查此插件是否已经安装，如果已经安装则不再安装
        if (pluginsByName.get(apk.getPluginName()) != null) {
            Log.w(TAG, "插件" + pluginPath + "已安装");
            return;
        }
        if (shouldCopy) {
            //把插件包拷贝到data/data/packageName/plugin/目录下
            File from = new File(pluginPath);
            FileInputStream in = new FileInputStream(from);
            File to = new File(pluginBaseDir + apk.getPluginName() + ".apk");
            FileOutputStream out = new FileOutputStream(to);
            int len;
            byte[] buffer = new byte[1024];
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            in.close();
            out.close();
            //设置apkPath
            apk.setApkPath(to.getAbsolutePath());
        } else {
            apk.setApkPath(pluginPath);
        }
        //初始化Plugin的Resource
        Class<AssetManager> assetClazz = AssetManager.class;
        Constructor<AssetManager> assetCons = assetClazz.getConstructor();
        assetCons.setAccessible(true);
        AssetManager asset = assetCons.newInstance();
        Method addAsset = assetClazz.getDeclaredMethod("addAssetPath", String.class);
        addAsset.setAccessible(true);
        addAsset.invoke(asset, apk.getApkPath());
        Resources hostRes = context.getResources();
        Resources resources = new Resources(asset, hostRes.getDisplayMetrics(), hostRes.getConfiguration());
        apk.setResources(resources);
        /*
        * 在此处解决plugin之间的依赖问题
        * */
        configParser.parse(apk);
        dependencyLoader.load(context, apk);
        //初始化codeDir和nativeLibDir
        String codeDir = pluginBaseDir + apk.getPluginName() + File.separator + pluginCodeSubDir;
        File f = new File(codeDir);
        if (!f.exists()) {
            if (!f.mkdirs()) {
                throw new RuntimeException("无法创建插件代码路径");
            }
        }
        apk.setCodeDir(codeDir);
        String nativeLibDir = pluginBaseDir + apk.getPluginName() + File.separator + pluginNativeLibSubDir;
        f = new File(nativeLibDir);
        if (!f.exists()) {
            if (!f.mkdirs()) {
                throw new RuntimeException("无法创建插件原生代码路径");
            }
        }
        apk.setNativeLibDir(nativeLibDir);
        //复制nativeCode到nativeLibDir目录下
        copyNativeLib(apk);
        //初始化classLoader
        PluginClassLoader classLoader = new PluginClassLoader(apk.getApkPath(), codeDir, nativeLibDir, PackageManager.class.getClassLoader());
        //把依赖的插件的代码和资源加入到当前插件
        List<PluginApk.Dependency> dependencies = apk.getDependencies();
        if (dependencies != null) {
            for (PluginApk.Dependency dependency : dependencies) {
                PluginApk dpapk = getPlugin(dependency.name);
                if (dpapk == null) {
                    //依赖安装失败，则不安装插件
                    Log.e(TAG, "安装依赖失败：" + dependency.name);
                    return;
                }
                dpapk.addDepended(apk.getPluginName());
                classLoader.addDependency(dpapk.getPluginName());
            }
        }
        apk.setClassLoader(classLoader);
        //保存plugin
        pluginsByName.put(apk.getPluginName(), apk);
    }

    public void unInstallPlugin(String pluginName) {
        unInstallPlugin(pluginName, false);
    }

    /**
     * 卸载插件，但有其他插件依赖此插件时不能卸载
     *
     * @param isUpdate   true时：表示此次卸载是为了安装新的插件包，所以不管有没有插件依赖此插件都可以卸载，因为马上会安装新的
     * @param pluginName 指定要卸载的插件
     */
    public void unInstallPlugin(String pluginName, boolean isUpdate) {
        //检查是否安装过pluginName指定的插件，如果没有安装则直接返回
        if (pluginsByName.get(pluginName) == null) {
            Log.w(TAG, "没有安装此插件");
            return;
        }

        PluginApk apk = pluginsByName.get(pluginName);
        //检查是否有插件依赖此插件
        if (apk.getDependended() != null && apk.getDependended().size() > 0 && !isUpdate) {
            String logMsg = "不可以卸载插件，有插件依赖此插件：";
            for (String dName : apk.getDependended()) {
                logMsg = logMsg + "[" + dName + "]";
            }
            Log.w(TAG, logMsg);
            return;
        }
        //删除与这个插件关联的文件:code;nativeLib;插件apk文件
        File apkFile = new File(apk.getApkPath());
        if (!apkFile.delete()) {
            Log.w(TAG, "删除文件" + apkFile.getAbsolutePath() + "失败");
        }
        File codeFile = new File(apk.getCodeDir());
        if (!codeFile.delete()) {
            Log.w(TAG, "删除文件" + codeFile.getAbsolutePath() + "失败");
        }
        File nativeLib = new File(apk.getNativeLibDir());
        if (!nativeLib.delete()) {
            Log.w(TAG, "删除文件" + nativeLib.getAbsolutePath() + "失败");
        }

        //从map中移除apk
        pluginsByName.remove(pluginName);
        //卸载插件apk依赖的插件
        if (apk.getDependencies() != null && apk.getDependencies().size() > 0) {
            for (PluginApk.Dependency dependency : apk.getDependencies()) {
                PluginManager.getInstance(context).unInstallPlugin(dependency.name);
            }
        }
    }

    /**
     * 更新插件，如果newPlugin指定的插件已安装则卸载旧插件，再安装先插件，
     * 如果newPlugin指定的插件没有安装过则直接安装此插件
     * tip：只有当新插件的versionCode大于旧插件的versionCode才执行安装
     * 否则抛出UpLevelException
     *
     * @param newPlugin 新插件的绝对路径
     */
    public void updatePlugin(String newPlugin) throws Exception {
        PluginApk.Package newPackage = parsePackage(newPlugin);
        if (newPackage == null) {
            throw new RuntimeException("解析apk失败： " + newPlugin);
        }
        if (!pluginsByName.containsKey(newPackage.packageName)) {
            installPlugin(newPlugin, true);
            return;
        }
        int newVersion = newPackage.versionCode;
        int oldVersion = pluginsByName.get(newPackage.packageName).getVersionCode();
        if (newVersion <= oldVersion) {
            throw new UpLevelException();
        }
        this.unInstallPlugin(newPackage.packageName, true);
        installPlugin(newPlugin, true);
        //更新依赖它的插件的classLoader
        List<String> names = getPlugin(newPlugin).getDependended();
        for (String n : names) {
            PluginApk apk = getPlugin(n);
            ((PluginClassLoader) apk.getClassLoader()).invalidateDependency(newPlugin);
        }
    }

    public PluginApk getPlugin(String pluginName) {
        return pluginsByName.get(pluginName);
    }

    /**
     * 把nativeLib 复制到nativeLibDir目录下
     *
     * @param apk 表示安装的插件apk，里面存有nativeLib的原始路径和目标路径
     */
    private void copyNativeLib(PluginApk apk) throws Exception {
        ZipFile apkFile = new ZipFile(apk.getApkPath());
        String supportAbi = Build.CPU_ABI;
        Enumeration entrys = apkFile.entries();
        ZipEntry entry;
        while (entrys.hasMoreElements()) {
            entry = (ZipEntry) entrys.nextElement();
            if (entry.getName().endsWith(".so") && entry.getName().contains(supportAbi)) {
                int index = entry.getName().indexOf(File.separator);
                String libName = entry.getName().substring(index + 1);
                FileOutputStream out = new FileOutputStream(new File(apk.getNativeLibDir() + libName));
                InputStream in = apkFile.getInputStream(entry);
                int len;
                byte[] buffers = new byte[1024];
                while ((len = in.read(buffers)) != -1) {
                    out.write(buffers, 0, len);
                }
            }
        }
    }

    /**
     * 返回所有的插件apk
     */
    public List<PluginApk> getPlugins() {
        List<PluginApk> apks = new ArrayList<>();
        Set<String> keySet = this.pluginsByName.keySet();
        for (String aKeySet : keySet) {
            apks.add(pluginsByName.get(aKeySet));
        }
        return apks;
    }

    /**
     * 通过PackageParser利用反射来解析apk，生成{@link com.u1city.u1pluginframework.core.pm.PluginApk.Package}对象
     *
     * @param path apk对应的路径
     * @return package对象
     */
    public PluginApk.Package parsePackage(String path) {
        try {
            File apkFile = new File(path);
            Class<?> parserClazz = Class.forName("android.content.pm.PackageParser");
            Constructor<?> constructor = parserClazz.getConstructor();
            constructor.setAccessible(true);
            Object parser = constructor.newInstance();
            Method method = parserClazz.getDeclaredMethod("parseMonolithicPackage", File.class, int.class);
            method.setAccessible(true);
            Object rawPkg = method.invoke(parser, apkFile, 0);
            if (rawPkg != null) {
                PluginApk.Package pluginPkg = new PluginApk.Package();
                pluginPkg.packageName = (String) ReflectUtils.readField("packageName", rawPkg);
                pluginPkg.baseHardwareAccelerated = (boolean) ReflectUtils.readField("baseHardwareAccelerated", rawPkg);
                pluginPkg.applicationInfo = (ApplicationInfo) ReflectUtils.readField("applicationInfo", rawPkg);
                pluginPkg.versionCode = (int) ReflectUtils.readField("mVersionCode", rawPkg);
                pluginPkg.versionName = (String) ReflectUtils.readField("mVersionName", rawPkg);
                ArrayList rawActivities = (ArrayList) ReflectUtils.readField("activities", rawPkg);
                //获取activities
                if (rawActivities != null) {
                    for (Object obj : rawActivities) {
                        PluginApk.Activity activity = new PluginApk.Activity();
                        activity.activityInfo = (ActivityInfo) ReflectUtils.readField("info", obj);
                        activity.owner = pluginPkg;
                        activity.className = (String) ReflectUtils.readField("className", obj);
                        activity.metaData = (Bundle) ReflectUtils.readField("metaData", obj);
                        ArrayList intents = (ArrayList) ReflectUtils.readField("intents", obj);
                        if (intents != null) {
                            for (Object in : intents) {
                                activity.intents.add((IntentFilter) in);
                            }
                        }
                        pluginPkg.activities.add(activity);
                    }
                }
                //获取services
                ArrayList rawServices = (ArrayList) ReflectUtils.readField("services", rawPkg);
                if (rawServices != null) {
                    for (Object obj : rawServices) {
                        PluginApk.Service service = new PluginApk.Service();
                        service.serviceInfo = (ServiceInfo) ReflectUtils.readField("info", obj);
                        service.owner = pluginPkg;
                        service.className = (String) ReflectUtils.readField("className", obj);
                        service.metaData = (Bundle) ReflectUtils.readField("metaData", obj);
                        ArrayList intents = (ArrayList) ReflectUtils.readField("intents", obj);
                        if (intents != null) {
                            for (Object in : intents) {
                                service.intents.add((IntentFilter) in);
                            }
                        }
                        pluginPkg.services.add(service);
                    }
                }
                //获取receivers
                ArrayList rawReceivers = (ArrayList) ReflectUtils.readField("receivers", rawPkg);
                if (rawReceivers != null) {
                    for (Object obj : rawReceivers) {
                        PluginApk.Activity reciver = new PluginApk.Activity();
                        reciver.activityInfo = (ActivityInfo) ReflectUtils.readField("info", obj);
                        reciver.owner = pluginPkg;
                        reciver.className = (String) ReflectUtils.readField("className", obj);
                        reciver.metaData = (Bundle) ReflectUtils.readField("metaData", obj);
                        ArrayList intents = (ArrayList) ReflectUtils.readField("intents", obj);
                        if (intents != null) {
                            for (Object in : intents) {
                                reciver.intents.add((IntentFilter) in);
                            }
                        }
                        pluginPkg.receivers.add(reciver);
                    }
                }
                //获取provider
                ArrayList rawProviders = (ArrayList) ReflectUtils.readField("providers", rawPkg);
                if (rawProviders != null) {
                    for (Object obj : rawProviders) {
                        PluginApk.Provider provider = new PluginApk.Provider();
                        provider.providerInfo = (ProviderInfo) ReflectUtils.readField("info", obj);
                        provider.owner = pluginPkg;
                        provider.className = (String) ReflectUtils.readField("className", obj);
                        provider.metaData = (Bundle) ReflectUtils.readField("metaData", obj);
                        ArrayList intents = (ArrayList) ReflectUtils.readField("intents", obj);
                        if (intents != null) {
                            for (Object in : intents) {
                                provider.intents.add((IntentFilter) in);
                            }
                        }
                        pluginPkg.providers.add(provider);
                    }
                }
                return pluginPkg;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        //do nothing
    }

    @Override
    public void onLowMemory() {
        //清理缓存
    }
}
