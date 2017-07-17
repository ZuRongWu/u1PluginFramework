package com.u1city.u1pluginframework.core;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import com.u1city.u1pluginframework.core.activity.host.BaseHostChoosePolicy;
import com.u1city.u1pluginframework.core.activity.host.HostActivity;
import com.u1city.u1pluginframework.core.activity.host.HostChoosePolicy;
import com.u1city.u1pluginframework.core.activity.plugin.PluginActivity;
import com.u1city.u1pluginframework.core.error.InstallPluginException;
import com.u1city.u1pluginframework.core.error.UpdatePluginException;
import com.u1city.u1pluginframework.core.pk.PackageManager;
import com.u1city.u1pluginframework.download.DownloadManager;

import java.io.File;
import java.util.List;

/**
 * plugin manager
 * Created by wuzr on 2016/12/2.
 */
public class PluginManager {
    private static final String TAG = "PluginManager";
    private static PluginManager sPluginManager;
    private PackageManager packageManager;
    private Context context;
    private HostChoosePolicy hostChoosePolicy;

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
    }

    /**
     * 加载已经安装的插件，应该在{@link Application#onCreate()}中调用
     */
    public void init() {
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
    public void installPluginAsyn(String urlStr, final URLInstallListener listener) {
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
    public void installPluginSync(String url) throws InstallPluginException {
        String path = DownloadManager.getInstance(context).downloadSync(url);
        if (TextUtils.isEmpty(path)) {
            //为空时下载失败
            throw new RuntimeException("下载插件失败：" + url);
        }
        installPlugin(path);
    }

    public void startPluginActivityForResult(Context context, PluginIntent intent, int requestCode) {
        try {
            List<ActivityInfo> ais = packageManager.findPluginActivity(intent);
            if (ais.size() > 1) {
                //找到多于一个匹配的activity，弹出选择框供用户选择
                showChooseDialog();
                return;
            }
            ActivityInfo ai = ais.get(0);
            if (ai != null) {
                Class<? extends HostActivity> hostClazz = hostChoosePolicy.choose(ai);
                intent.setClass(this.context, hostClazz);
                intent.putExtra(PluginActivity.KEY_PLUGIN_ACTIVITY_INFO, ai);
                //pluginName不一定等于ai.packageName,也有可能是它所依赖的插件的pluginName
                intent.putExtra(PluginActivity.KEY_PLUGIN_NAME, ai.packageName);
                intent.addPluginFlag(PluginIntent.FLAG_LAUNCH_ACTUAL_ACTIVITY);
                if (context instanceof Activity) {
                    ((Activity) context).startActivityForResult(intent, requestCode);
                } else {
                    context.startActivity(intent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    private void showChooseDialog() {
        //启动选择框activity
    }

    public void startPluginService(Context context, PluginIntent intent) {
    }

    private boolean checkPluginPath(String pluginPath) {
        File plugin = new File(pluginPath);
        if (!plugin.exists()) {
            return false;
        }
        String fileName = plugin.getName();
        return fileName.endsWith(".zip") || fileName.endsWith(".apk") || fileName.endsWith(".jar");
    }
}
