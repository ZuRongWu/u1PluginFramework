package com.u1city.u1pluginframework;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.u1city.u1pluginframework.core.PluginIntent;
import com.u1city.u1pluginframework.core.PluginManager;

public class MainActivity extends Activity {
    private PluginManager pluginManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pluginManager = PluginManager.getInstance(getApplicationContext());
    }

    public void installPlugin(View view) {
        try {
            pluginManager.installPlugin("mnt/sdcard/plugins/app-debug_1.apk");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startPlugin(View view) {
        PluginIntent intent = new PluginIntent();
        intent.setPluginComponentName(".MainActivity");
        intent.setPluginName("wuzr.com.plugin01");
        pluginManager.startPluginActivityForResult(this, intent, 0);
    }

    public void uninstallPlugin(View view) {
        try {
            pluginManager.unInstallPlugin("wuzr.com.plugin01");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
