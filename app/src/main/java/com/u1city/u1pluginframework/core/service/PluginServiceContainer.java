package com.u1city.u1pluginframework.core.service;

import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.IBinder;

import com.u1city.u1pluginframework.core.error.PluginServiceNotFindException;
import com.u1city.u1pluginframework.core.pm.PackageManager;
import com.u1city.u1pluginframework.core.pm.PluginApk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wuzr on 2017/8/1.
 * 管理多个servicePlugin
 */

public class PluginServiceContainer implements ServicePlugin {

    private Map<String, ServicePlugin> pluginsById = new HashMap<>(0);
    private List<ServicePlugin> plugins = new ArrayList<>(0);

    @Override
    public void setApk(PluginApk apk) {
        //do nothing
    }

    @Override
    public Resources getResources(String plugin) {
        return null;
    }

    @Override
    public IBinder onPluginBind(Intent intent) {
        try {
            ServiceInfo si = PackageManager.getInstance(null).findPluginService(intent);
            if (si != null) {
                ServicePlugin p = pluginsById.get(generateServiceId(si.packageName, si.name));
                if (p != null) {
                    return p.onPluginBind(intent);
                }
            }
        } catch (PluginServiceNotFindException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onPluginConfigurationChanged(Configuration newConfig) {
        for (ServicePlugin p : plugins) {
            p.onPluginConfigurationChanged(newConfig);
        }
    }

    @Override
    public void onPluginCreate() {
        for (ServicePlugin p : plugins) {
            p.onPluginCreate();
        }
    }

    @Override
    public void onPluginDestroy() {
        for (ServicePlugin p : plugins) {
            p.onPluginDestroy();
        }
    }

    @Override
    public void onPluginLowMemory() {
        for (ServicePlugin p : plugins) {
            p.onPluginLowMemory();
        }
    }

    @Override
    public void onPluginRebind(Intent intent) {
        try {
            ServiceInfo si = PackageManager.getInstance(null).findPluginService(intent);
            if (si != null) {
                ServicePlugin p = pluginsById.get(generateServiceId(si.packageName, si.name));
                if (p != null) {
                    p.onPluginRebind(intent);
                }
            }
        } catch (PluginServiceNotFindException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPluginStart(Intent intent, int startId) {
        try {
            ServiceInfo si = PackageManager.getInstance(null).findPluginService(intent);
            if (si != null) {
                ServicePlugin p = pluginsById.get(generateServiceId(si.packageName, si.name));
                if (p != null) {
                    p.onPluginStart(intent, startId);
                }
            }
        } catch (PluginServiceNotFindException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onPluginStartCommand(Intent intent, int flags, int startId) {
        try {
            ServiceInfo si = PackageManager.getInstance(null).findPluginService(intent);
            if (si != null) {
                ServicePlugin p = pluginsById.get(generateServiceId(si.packageName, si.name));
                if (p != null) {
                    return p.onPluginStartCommand(intent, flags, startId);
                }
            }
        } catch (PluginServiceNotFindException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void onPluginTaskRemoved(Intent rootIntent) {
        for (ServicePlugin p : plugins) {
            p.onPluginTaskRemoved(rootIntent);
        }
    }

    @Override
    public void onPluginTrimMemory(int level) {
        for (ServicePlugin p : plugins) {
            p.onPluginTrimMemory(level);
        }
    }

    @Override
    public boolean onPluginUnbind(Intent intent) {
        return false;
    }

    @Override
    public void setHost(HostService host) {
        //do nothing
    }

    String generateServiceId(String packageName, String componentName) {
        return packageName + "::" + componentName;
    }

    void addPluginService(String id, ServicePlugin plugin) {
        pluginsById.put(id, plugin);
        plugins.add(plugin);
    }

    boolean containServicePlugin(String id) {
        return pluginsById.containsKey(id);
    }

    int size() {
        return plugins.size();
    }
}
