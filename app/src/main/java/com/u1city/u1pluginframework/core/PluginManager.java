package com.u1city.u1pluginframework.core;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Configuration;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.Process;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.u1city.u1pluginframework.IPluginBinderProvider;
import com.u1city.u1pluginframework.core.activity.ChooseActivityDialog;
import com.u1city.u1pluginframework.core.activity.HostActivity;
import com.u1city.u1pluginframework.core.activity.PluginActivity;
import com.u1city.u1pluginframework.core.error.InstallPluginException;
import com.u1city.u1pluginframework.core.error.PluginServiceNotFindException;
import com.u1city.u1pluginframework.core.error.UpdatePluginException;
import com.u1city.u1pluginframework.core.pm.PackageManager;
import com.u1city.u1pluginframework.core.pm.PluginApk;
import com.u1city.u1pluginframework.core.reciever.BroadCastReceiverHost;
import com.u1city.u1pluginframework.core.reciever.BroadCastReceiverPlugin;
import com.u1city.u1pluginframework.core.service.HostService;
import com.u1city.u1pluginframework.core.service.PluginServiceContainer;
import com.u1city.u1pluginframework.core.service.IPlugin;
import com.u1city.u1pluginframework.core.service.PluginService;
import com.u1city.u1pluginframework.download.DownloadManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * plugin manager
 * Created by wuzr on 2016/12/2.
 */
public class PluginManager implements ComponentCallbacks {
    private static final String TAG = "PluginManager";
    private static PluginManager sPluginManager;
    private PackageManager packageManager;
    private Context context;
    private HostChoosePolicy hostChoosePolicy;
    private Map<String, BroadCastReceiverPlugin> receiversById;
    private Map<HostService, PluginServiceContainer> pluginServiceContainers;
    private Map<IPlugin, List<ServiceConnectionContainer>> serviceConnections;
    //标识是否处于开发模式，当处于开发模式时，所有的插件apk都可以像正常apk一样，默认关闭
    private boolean devIsOpen;

    public interface URLInstallListener {
        /**
         * 下载进度回调函数
         *
         * @param total 总大小
         * @param done  已经完成的大小
         */
        void onDownloadProgress(long total, long done);

        /**
         * 下载结束回调
         */
        void onDownloadFinish();

        /**
         * 下载失败
         *
         * @param reason 失败的描述信息
         */
        void onDownloadError(String reason);

        /**
         * 插件安装完成
         */
        void onInstallFinish();

        /**
         * 安装插件失败
         *
         * @param reason 失败的描述信息
         */
        void onInstallError(String reason);
    }

    private class ServiceConnectionContainer {
        //宿主的serviceConnection
        ServiceConnection hostConnection;
        //插件的serviceConnection
        ServiceConnection pluginConnection;
    }

    public static PluginManager getInstance(Context context) {
        if (sPluginManager == null) {
            synchronized (PluginManager.class) {
                if (sPluginManager == null) {
                    sPluginManager = new PluginManager(context);
                }
            }
        }
        return sPluginManager;
    }

    private PluginManager(Context context) {
        this.context = context.getApplicationContext();
        packageManager = PackageManager.getInstance(this.context);
        hostChoosePolicy = new BaseHostChoosePolicy();
        this.context.registerComponentCallbacks(this);
        pluginServiceContainers = new HashMap<>(0);
        receiversById = new HashMap<>(0);
        serviceConnections = new HashMap<>(0);
    }

    public boolean getDevIsOpen() {
        return devIsOpen;
    }

    public void openDevMode() {
        devIsOpen = true;
    }

    public void closeDevMode() {
        devIsOpen = false;
    }

    /**
     * 加载已经安装的插件，应该在{@link Application#onCreate()}中调用
     */
    public void init() {
        if (devIsOpen) {
            Log.w(TAG, "现在处于开发模式，不能使用和操作插件");
            return;
        }
        File baseDir = new File(packageManager.getPluginBaseDir());
        if (!baseDir.exists()) {
            return;
        }
        for (File plugin : baseDir.listFiles()) {
            String pluginPath = plugin.getAbsolutePath();
            if (pluginPath.endsWith(".apk")) {
                Log.d(TAG, pluginPath);
                try {
                    packageManager.installPlugin(pluginPath, false);
                } catch (Exception e) {
                    //出现异常则应用出现错误，直接退出应用
                    e.printStackTrace();
                    Process.killProcess(Process.myPid());
                }
            }
        }
    }

    /**
     * 安装插件，首先检查插件包是否存在以及各式是否正确，只接收.apk;.zip;.jar各式的文件
     * 建议不要在ui线程运行此方法，因为在安装依赖包时可能要下载依赖包
     *
     * @param pluginPath 插件对应的绝对路径
     */
    public void installPlugin(String pluginPath) throws InstallPluginException {
        if (devIsOpen) {
            Log.w(TAG, "现在处于开发模式，不能安装插件");
            return;
        }
        if (checkPluginPath(pluginPath)) {
            try {
                packageManager.installPlugin(pluginPath, true);
            } catch (Exception e) {
                e.printStackTrace();
                throw new InstallPluginException(e.getMessage());
            }
        } else {
            throw new InstallPluginException(pluginPath + "指定的插件不存在或者格式不正确");
        }
    }

    /**
     * 卸载指定插件
     *
     * @param pluginName 指定要卸载的插件
     */
    public void unInstallPlugin(String pluginName) {
        if (devIsOpen) {
            Log.w(TAG, "现在处于开发模式，不能卸载插件");
            return;
        }
        if (pluginName == null || pluginName.equals("")) {
            throw new IllegalArgumentException("插件名称不能为空");
        }
        packageManager.unInstallPlugin(pluginName);
    }

    /**
     * 更新指定的插件
     *
     * @param pluginPath 更新包的路径
     */
    public void updatePlugin(String pluginPath) throws UpdatePluginException {
        if (devIsOpen) {
            Log.w(TAG, "现在处于开发模式，不能更新插件");
            return;
        }
        if (checkPluginPath(pluginPath)) {
            try {
                packageManager.updatePlugin(pluginPath);
            } catch (Exception e) {
                e.printStackTrace();
                throw new UpdatePluginException(e.getMessage());
            }
        } else {
            throw new UpdatePluginException(pluginPath + "指定的插件不存在或者格式不正确");
        }
    }

    /**
     * 从远程下载插件并安装，异步
     *
     * @param urlStr 远程的url
     */
    public void installPluginRemote(String urlStr, final URLInstallListener listener) {
        if (devIsOpen) {
            Log.w(TAG, "现在处于开发模式，不能安装插件");
            return;
        }
        DownloadManager.getInstance(context).download(urlStr, new DownloadManager.DownloadListener() {
            @Override
            public void onProgress(long total, long done) {
                if (listener != null) {
                    listener.onDownloadProgress(total, done);
                }
            }

            @Override
            public void onError(String msg) {
                if (listener != null) {
                    listener.onDownloadError(msg);
                }
            }

            @Override
            public void onFinish(String path) {
                if (listener != null) {
                    listener.onDownloadFinish();
                }
                try {
                    installPlugin(path);
                    if (listener != null) {
                        listener.onInstallFinish();
                    }
                } catch (InstallPluginException e) {
                    e.printStackTrace();
                    if (listener != null) {
                        listener.onInstallError(e.getMessage());
                    }
                }
            }
        }, false);
    }

    /**
     * 从远程下载并安装插件，同步，不能在ui线程调用
     *
     * @param url url地址
     * @throws InstallPluginException 安装失败时抛出
     */
    public void installPluginRemoteSync(String url) throws InstallPluginException {
        if (devIsOpen) {
            Log.w(TAG, "现在处于开发模式不能安装插件");
            return;
        }
        String path = DownloadManager.getInstance(context).downloadSync(url);
        if (TextUtils.isEmpty(path)) {
            //为空时下载失败
            throw new InstallPluginException("下载插件失败：" + url);
        }
        installPlugin(path);
    }

    public void startPluginActivityForResult(Context context, PluginIntent intent, int requestCode) {
        if (devIsOpen) {
            Log.w(TAG, "现在处于开发模式");
            return;
        }
        try {
            List<ActivityInfo> ais = packageManager.findPluginActivity(intent);
            if (ais.size() > 1) {
                //找到多于一个匹配的activity，弹出选择框供用户选择
                showChooseDialog(context, ais, intent);
                return;
            }
            ActivityInfo ai = ais.get(0);
            if (ai != null) {
                Class<? extends HostActivity> hostClazz = hostChoosePolicy.chooseHostActivity(ai);
                intent.setClass(this.context, hostClazz);
                intent.putExtra(PluginActivity.KEY_PLUGIN_ACTIVITY_INFO, ai);
                //pluginName不一定等于ai.packageName,也有可能是它所依赖的插件的pluginName
                intent.putExtra(PluginActivity.KEY_PLUGIN_NAME, ai.packageName);
                intent.addPluginFlag(PluginIntent.FLAG_LAUNCH_ACTUAL);
                if (context instanceof Activity) {
                    ((Activity) context).startActivityForResult(intent, requestCode);
                } else {
                    context.startActivity(intent);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 启动选择框activity
     *
     * @param context    对应{@link PluginManager#startPluginActivityForResult(Context, PluginIntent, int)}中的context
     * @param activities 可供选择的所有activity
     * @param original   对应{@link PluginManager#startPluginActivityForResult(Context, PluginIntent, int)}中的intent
     *                   为了通过intent传递给目标activity的数据不丢失要把它复制到启动ChooseDialog的intent中
     */
    private void showChooseDialog(Context context, List<ActivityInfo> activities, Intent original) {
        PluginIntent intent = new PluginIntent(original);
        intent.addPluginFlag(PluginIntent.FLAG_LAUNCH_ACTUAL);
        intent.putParcelableArrayListExtra(ChooseActivityDialog.KEY_ACTIVITIES, (ArrayList<? extends Parcelable>) activities);
        context.startActivity(intent);
    }

    /**
     * 启动service
     *
     * @param context context
     * @param intent  intent
     * @return ComponentName
     */
    ComponentName startPluginService(Context context, PluginIntent intent) {
        if (devIsOpen) {
            Log.w(TAG, "现在处于开发模式");
            return null;
        }
        try {
            ServiceInfo si = packageManager.findPluginService(intent);
            if (si != null) {
                Class<? extends HostService> clazz = hostChoosePolicy.chooseHostService(si);
                intent.putExtra(PluginService.KEY_PLUGIN_SERVICE_INFO, si);
                //pluginName不一定等于ai.packageName,也有可能是它所依赖的插件的pluginName
                intent.putExtra(PluginService.KEY_PLUGIN_NAME, si.packageName);
                intent.addPluginFlag(PluginIntent.FLAG_LAUNCH_ACTUAL);
                intent.setClass(context.getApplicationContext(), clazz);
                return context.startService(intent);
            }
        } catch (PluginServiceNotFindException e) {
            throw new RuntimeException(e.getMessage());
        }
        return null;
    }

    boolean bindPluginService(Context context, final PluginIntent intent, final ServiceConnection connection, int flag) {
        if (devIsOpen) {
            Log.w(TAG, "现在处于开发模式");
            return false;
        }
        try {
            final ServiceInfo si = packageManager.findPluginService(intent);
            if (si != null) {
                Class<? extends HostService> clazz = hostChoosePolicy.chooseHostService(si);
                intent.putExtra(PluginService.KEY_PLUGIN_SERVICE_INFO, si);
                //pluginName不一定等于ai.packageName,也有可能是它所依赖的插件的pluginName
                intent.putExtra(PluginService.KEY_PLUGIN_NAME, si.packageName);
                intent.addPluginFlag(PluginIntent.FLAG_LAUNCH_ACTUAL);
                intent.setClass(context.getApplicationContext(), clazz);
                return context.bindService(intent, new ServiceConnection() {
                    @Override
                    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                        IBinder pluginBinder; //这里将获取pluginService的IBinder
                        try {
                            pluginBinder = IPluginBinderProvider.Stub.asInterface(iBinder).getPluginBinder(intent);
                        } catch (RemoteException e) {
                            //pluginBinder获取失败
                            e.printStackTrace();
                            return;
                        }
                        //把connection到connections map中
                        IPlugin plugin = getServicePlugin(si);
                        if (plugin == null) {
                            //plugin service启动失败
                            return;
                        }
                        List<ServiceConnectionContainer> cs = serviceConnections.get(plugin);
                        if (cs == null) {
                            cs = new ArrayList<>(1);
                            serviceConnections.put(plugin, cs);
                        }
                        ServiceConnectionContainer container = new ServiceConnectionContainer();
                        container.hostConnection = this;
                        container.pluginConnection = connection;
                        cs.add(container);
                        ComponentName pluginComponentName = new ComponentName(si.packageName, si.name);
                        connection.onServiceConnected(pluginComponentName, pluginBinder);
                    }

                    @Override
                    public void onServiceDisconnected(ComponentName componentName) {
                        IPlugin plugin = getServicePlugin(si);
                        if (plugin == null) {
                            return;
                        }
                        List<ServiceConnectionContainer> cs = serviceConnections.get(plugin);
                        ServiceConnectionContainer container = null;
                        for (ServiceConnectionContainer c : cs) {
                            if (c.pluginConnection == connection) {
                                container = c;
                                break;
                            }
                        }
                        if (container == null) {
                            return;
                        }
                        cs.remove(container);
                        ComponentName pluginComponentName = new ComponentName(si.packageName, si.name);
                        connection.onServiceDisconnected(pluginComponentName);
                        if (cs.size() == 0) {
                            //如果没有其他地方连接pluginService则停止pluginService
                            stopPluginServiceInternal(si, intent);
                        }
                    }
                }, flag);
            }
        } catch (PluginServiceNotFindException e) {
            throw new RuntimeException(e.getMessage());
        }
        return false;
    }

    void unbindPluginService(ServiceConnection conn) {
        if (conn == null) {
            return;
        }
        Set<IPlugin> plugins = serviceConnections.keySet();
        ServiceConnectionContainer container = null;
        for (IPlugin p : plugins) {
            for (ServiceConnectionContainer c : serviceConnections.get(p)) {
                if (conn == c.pluginConnection) {
                    container = c;
                    break;
                }
            }
            if (container != null) {
                break;
            }
        }
        if (container == null) {
            //没有找到对应的hostConnection
            return;
        }
        this.context.unbindService(container.hostConnection);
    }

    public void applyHostServiceStopped(HostService host) {
        pluginServiceContainers.remove(host);
    }

    public void applyHostServiceStarted(HostService host, PluginServiceContainer container) {
        if (host == null || container == null) {
            return;
        }
        pluginServiceContainers.put(host, container);
    }

    boolean stopPluginService(Intent intent) {
        try {
            ServiceInfo si = packageManager.findPluginService(intent);
            return stopPluginServiceInternal(si, intent);
        } catch (PluginServiceNotFindException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean stopPluginServiceInternal(ServiceInfo si, Intent intent) {
        IPlugin plugin = getServicePlugin(si);
        if (plugin == null) {
            //对应的service没有启动，不需要停止
            return true;
        }
        List<ServiceConnectionContainer> connections = serviceConnections.get(plugin);
        if (connections != null && connections.size() > 0) {
            //在其他地方有绑定到service，不能停止service
            return false;
        }
        PluginServiceContainer container = plugin.getPluginContainer();
        container.stopService(intent);
        return true;
    }

    private IPlugin getServicePlugin(ServiceInfo si) {
        String id = PluginServiceContainer.generateServiceId(si.packageName, si.name);
        Set<HostService> keys = pluginServiceContainers.keySet();
        for (HostService h : keys) {
            PluginServiceContainer container = pluginServiceContainers.get(h);
            IPlugin plugin = container.getServicePlugin(id);
            if (plugin != null) {
                return plugin;
            }
        }
        return null;
    }

    private boolean checkPluginPath(String pluginPath) {
        File plugin = new File(pluginPath);
        if (!plugin.exists()) {
            return false;
        }
        String fileName = plugin.getName();
        return fileName.endsWith(".zip") || fileName.endsWith(".apk") || fileName.endsWith(".jar");
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        //do nothing
    }

    @Override
    public void onLowMemory() {
        //清理缓存
        receiversById.clear();
    }

    /**
     * 处理静态广播，当{@link com.u1city.u1pluginframework.core.reciever.BroadCastReceiverHost#onReceive(Context, Intent)}
     * 接受到广播后，将会调用这个方法。这个方法负责将广播派发给插件apk的满足出发条件的静态广播接收者
     *
     * @param intent intent
     */
    public void sendBroadCastReceiver(Context context, BroadCastReceiverHost host, Intent intent) {
        if (devIsOpen) {
            Log.w(TAG, "现在处于开发模式，不能通过这种方式发送广播");
            return;
        }
        List<ActivityInfo> receivers = packageManager.findReceivers(intent);
        for (ActivityInfo receiverInfo : receivers) {
            BroadCastReceiverPlugin receiver;
            //查找有没有缓存receiver对象
            String id = generateReceiverId(receiverInfo);
            receiver = receiversById.get(id);
            if (receiver != null) {
                receiver.setHost(host);
                receiver.onReceive(context, intent);
                continue;
            }
            //如果没找到则实例化一个receiver
            PluginApk apk = packageManager.getPlugin(receiverInfo.packageName);
            if (apk == null) {
                continue;
            }
            ContextDelegate delegate = new ContextDelegate(apk, context);
            try {
                Class<?> clazz = apk.getClassLoader().loadClass(receiverInfo.name);
                receiver = (BroadCastReceiverPlugin) clazz.newInstance();
                receiver.setHost(host);
                receiver.onReceive(delegate, intent);
                receiversById.put(id, receiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String generateReceiverId(ActivityInfo info) {
        return info.packageName + "::" + info.name;
    }
}
