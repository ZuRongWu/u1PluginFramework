package com.u1city.u1pluginframework.core.service;

import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.IBinder;

import com.u1city.u1pluginframework.core.PluginManager;
import com.u1city.u1pluginframework.core.error.PluginServiceNotFindException;
import com.u1city.u1pluginframework.core.pm.PackageManager;
import com.u1city.u1pluginframework.core.pm.PluginApk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wuzr on 2017/8/1.
 * 管理多个servicePlugin的生命周期，模拟正常service的生命周期
 */

public class PluginServiceContainer implements IPlugin {

    private Map<String, IPlugin> pluginsById = new HashMap<>(0);
    private List<IPlugin> plugins = new ArrayList<>(0);
    private HostService hostService;

    @Override
    public void setApk(PluginApk apk) {
        //do nothing
    }

    @Override
    public Resources getResources(String plugin) {
        return null;
    }

    @Override
    public String getPluginName() {
        //不要调用此方法
        return null;
    }

    @Override
    public IBinder onPluginBind(Intent intent) {
            ServiceInfo si = intent.getParcelableExtra(PluginService.KEY_PLUGIN_SERVICE_INFO);
            if (si != null) {
                IPlugin p = pluginsById.get(generateServiceId(si.packageName, si.name));
                if (p != null) {
                    return p.onPluginBind(intent);
                }
            }
        return null;
    }

    @Override
    public void onPluginConfigurationChanged(Configuration newConfig) {
        for (IPlugin p : plugins) {
            p.onPluginConfigurationChanged(newConfig);
        }
    }

    @Override
    public void onPluginCreate() {
        for (IPlugin p : plugins) {
            p.onPluginCreate();
        }
    }

    @Override
    public void onPluginDestroy() {
        for (IPlugin p : plugins) {
            p.onPluginDestroy();
        }
    }

    @Override
    public void onPluginLowMemory() {
        for (IPlugin p : plugins) {
            p.onPluginLowMemory();
        }
    }

    @Override
    public void onPluginRebind(Intent intent) {
            ServiceInfo si = intent.getParcelableExtra(PluginService.KEY_PLUGIN_SERVICE_INFO);
            if (si != null) {
                IPlugin p = pluginsById.get(generateServiceId(si.packageName, si.name));
                if (p != null) {
                    p.onPluginRebind(intent);
                }
            }
    }

    @Override
    public void onPluginStart(Intent intent, int startId) {
        ServiceInfo si = intent.getParcelableExtra(PluginService.KEY_PLUGIN_SERVICE_INFO);
        if (si != null) {
            IPlugin p = pluginsById.get(generateServiceId(si.packageName, si.name));
            if (p != null) {
                p.onPluginStart(intent, startId);
            }
        }
    }

    @Override
    public int onPluginStartCommand(Intent intent, int flags, int startId) {
        ServiceInfo si = intent.getParcelableExtra(PluginService.KEY_PLUGIN_SERVICE_INFO);
        if (si != null) {
            IPlugin p = pluginsById.get(generateServiceId(si.packageName, si.name));
            if (p != null) {
                return p.onPluginStartCommand(intent, flags, startId);
            }
        }
        return 0;
    }

    @Override
    public void onPluginTaskRemoved(Intent rootIntent) {
        for (IPlugin p : plugins) {
            p.onPluginTaskRemoved(rootIntent);
        }
    }

    @Override
    public void onPluginTrimMemory(int level) {
        for (IPlugin p : plugins) {
            p.onPluginTrimMemory(level);
        }
    }

    @Override
    public boolean onPluginUnbind(Intent intent) {
            ServiceInfo si = intent.getParcelableExtra(PluginService.KEY_PLUGIN_SERVICE_INFO);
            if (si != null) {
                IPlugin p = pluginsById.get(generateServiceId(si.packageName, si.name));
                if (p != null) {
                    return p.onPluginUnbind(intent);
                }
            }
        return false;
    }

    @Override
    public void setHost(HostService host) {
        this.hostService = host;
    }

    @Override
    public void setPluginContainer(PluginServiceContainer container) {
        //do nothing
    }

    @Override
    public PluginServiceContainer getPluginContainer() {
        return this;
    }

    public static String generateServiceId(String packageName, String componentName) {
        return packageName + "::" + componentName;
    }

    void addPluginService(String id, IPlugin plugin) {
        pluginsById.put(id, plugin);
        plugins.add(plugin);
    }

    boolean containServicePlugin(String id) {
        return pluginsById.containsKey(id);
    }

    int size() {
        return plugins.size();
    }

    public IPlugin getServicePlugin(String id) {
        return pluginsById.get(id);
    }

    public void stopService(Intent intent) {
        try {
            ServiceInfo si = PackageManager.getInstance(null).findPluginService(intent);
            IPlugin p = pluginsById.remove(generateServiceId(si.packageName, si.name));
            plugins.remove(p);
            p.onPluginDestroy();
            //如果container为空则停止宿主service
            if (plugins.size() == 0) {
                hostService.stopSelf();
                PluginManager.getInstance(null).applyHostServiceStopped(hostService);
            }
        } catch (PluginServiceNotFindException e) {
            e.printStackTrace();
        }
    }
}
