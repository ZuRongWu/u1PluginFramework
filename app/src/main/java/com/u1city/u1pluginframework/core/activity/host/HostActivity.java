package com.u1city.u1pluginframework.core.activity.host;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.u1city.u1pluginframework.core.PluginIntent;
import com.u1city.u1pluginframework.core.PluginManager;
import com.u1city.u1pluginframework.core.activity.plugin.IPlugin;
import com.u1city.u1pluginframework.core.activity.plugin.PluginActivity;
import com.u1city.u1pluginframework.core.pk.PackageManager;
import com.u1city.u1pluginframework.core.pk.PluginApk;

/**
 * host activity
 * Created by user on 2016/12/2.
 */
public class HostActivity extends FragmentActivity {
    private static final String TAG = "HostActivity";
    private IPlugin plugin;

    /**
     * 在此处创建pluginActivity对象
     */
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String pluginName = getIntent().getStringExtra(PluginActivity.KEY_PLUGIN_NAME);
        if (pluginName == null || pluginName.equals("")) {
            finish();
            return;
        }
        PackageManager packageManager = PackageManager.getInstance(getApplicationContext());
        PluginApk apk = packageManager.getPlugin(pluginName);
        ActivityInfo ai = getIntent().getParcelableExtra(PluginActivity.KEY_PLUGIN_ACTIVITY_INFO);
        if (ai == null) {
            finish();
            return;
        }
        String acName = ai.name;
        try {
            Class acClazz = apk.getClassLoader().loadClass(acName);
            plugin = (IPlugin) acClazz.newInstance();
            plugin.setApk(apk);
            plugin.setHost(this);
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
        //把activity的theme替换成插件activity的theme
        onApplyThemeResource(getTheme(), ai.theme, false);
        if (plugin != null) {
            plugin.onPluginCreate(savedInstanceState);
        } else {
            super.onCreate(savedInstanceState);
        }
    }

    public void onSuperCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateThumbnail(Bitmap outBitmap, Canvas canvas) {
        if (plugin != null) {
            return plugin.onPluginCreateThumbnail(outBitmap, canvas);
        } else {
            return super.onCreateThumbnail(outBitmap, canvas);
        }
    }

    public boolean onSuperCreateThumbnail(Bitmap outBitmap, Canvas canvas) {
        return super.onCreateThumbnail(outBitmap, canvas);
    }

    @Nullable
    @Override
    public CharSequence onCreateDescription() {
        if (plugin != null) {
            return plugin.onPluginCreateDescription();
        } else {
            return super.onCreateDescription();
        }
    }

    public CharSequence onSuperCreateDescription() {
        return super.onCreateDescription();
    }

    @Override
    protected void onUserLeaveHint() {
        if (plugin != null) {
            plugin.onPluginUserLeaveHint();
        } else {
            super.onUserLeaveHint();
        }
    }

    public void onSuperUserLeaveHint() {
        super.onUserLeaveHint();
    }

    @Override
    protected void onPause() {
        if (plugin != null) {
            plugin.onPluginPause();
        } else {
            super.onPause();
        }
    }

    public void onSuperPause() {
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (plugin != null) {
            plugin.onPluginSaveInstanceState(outState);
        } else {
            super.onSaveInstanceState(outState);
        }
    }

    public void onSuperSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (plugin != null) {
            plugin.onPluginNewIntent(intent);
        } else {
            super.onNewIntent(intent);
        }
    }

    public void onSuperNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        if (plugin != null) {
            plugin.onPluginResume();
        } else {
            super.onResume();
        }
    }

    public void onSuperResume() {
        super.onResume();
    }

    @Override
    public void onStateNotSaved() {
        if (plugin != null) {
            plugin.onPluginStateNotSaved();
        } else {
            super.onStateNotSaved();
        }
    }

    public void onSuperStateNotSaved() {
        super.onStateNotSaved();
    }

    @Override
    protected void onRestart() {
        if (plugin != null) {
            plugin.onPluginRestart();
        } else {
            super.onRestart();
        }
    }

    public void onSuperRestart() {
        super.onRestart();
    }

    @Override
    protected void onStart() {
        if (plugin != null) {
            plugin.onPluginStart();
        } else {
            super.onStart();
        }
    }

    public void onSuperStart() {
        super.onStart();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (plugin != null) {
            plugin.onPluginRestoreInstanceState(savedInstanceState);
        } else {
            super.onRestoreInstanceState(savedInstanceState);
        }
    }

    public void onSuperRestoreInstanceState(Bundle saveInstanceState) {
        super.onRestoreInstanceState(saveInstanceState);
    }

    @Override
    protected void onStop() {
        if (plugin != null) {
            plugin.onPluginStop();
        } else {
            super.onStop();
        }
    }

    public void onSuperStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (plugin != null) {
            plugin.onPluginDestroy();
        } else {
            super.onDestroy();
        }
    }

    public void onSuperDestroy() {
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (plugin != null) {
            plugin.onPluginConfigurationChanged(newConfig);
        } else {
            super.onConfigurationChanged(newConfig);
        }
    }

    public void onSuperConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLowMemory() {
        if (plugin != null) {
            plugin.onPluginLowMemory();
        } else {
            super.onLowMemory();
        }
    }

    public void onSuperLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        if (plugin != null) {
            plugin.onPluginTrimMemory(level);
        } else {
            super.onTrimMemory(level);
        }
    }

    public void onSuperTrimMemory(int level) {
        super.onTrimMemory(level);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (plugin != null) {
            return plugin.onPluginKeyDown(keyCode, event);
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    public boolean onSuperKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (plugin != null) {
            return plugin.onPluginKeyLongPress(keyCode, event);

        } else {
            return super.onKeyLongPress(keyCode, event);
        }
    }

    public boolean onSuperKeyLongPress(int keyCode, KeyEvent event) {
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (plugin != null) {
            return plugin.onPluginKeyUp(keyCode, event);
        } else {
            return super.onKeyUp(keyCode, event);
        }
    }

    public boolean onSuperKeyUp(int keyCode, KeyEvent event) {
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        if (plugin != null) {
            return plugin.onPluginKeyMultiple(keyCode, repeatCount, event);
        } else {
            return super.onKeyMultiple(keyCode, repeatCount, event);
        }
    }

    public boolean onSuperKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        return super.onKeyMultiple(keyCode, repeatCount, event);
    }

    @Override
    public void onBackPressed() {
        if (plugin != null) {
            plugin.onPluginBackPressed();
        } else {
            super.onBackPressed();
        }
    }

    public void onSuperBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onKeyShortcut(int keyCode, KeyEvent event) {
        if (plugin != null) {
            return plugin.onPluginKeyShortcut(keyCode, event);
        } else {
            return super.onKeyShortcut(keyCode, event);
        }
    }

    public boolean onSuperKeyShortcut(int keyCode, KeyEvent event) {
        return super.onKeyShortcut(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (plugin != null) {
            return plugin.onPluginTouchEvent(event);
        } else {
            return super.onTouchEvent(event);
        }
    }

    public boolean onSuperTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    public void onUserInteraction() {
        if (plugin != null) {
            plugin.onPluginUserInteraction();
        } else {
            super.onUserInteraction();
        }
    }

    public void onSuperUserInteraction() {
        super.onUserInteraction();
    }

    @Override
    public void onWindowAttributesChanged(WindowManager.LayoutParams params) {
        if (plugin != null) {
            plugin.onPluginWindowAttributesChanged(params);
        } else {
            super.onWindowAttributesChanged(params);
        }
    }

    public void onSuperWindowAttributesChanged(WindowManager.LayoutParams params) {
        super.onWindowAttributesChanged(params);
    }

    @Override
    public void onContentChanged() {
        if (plugin != null) {
            plugin.onPluginContentChanged();
        } else {
            super.onContentChanged();
        }
    }

    public void onSuperContentChanged() {
        super.onContentChanged();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (plugin != null) {
            plugin.onPluginWindowFocusChanged(hasFocus);
        } else {
            super.onWindowFocusChanged(hasFocus);
        }
    }

    public void onSuperWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    public void onAttachedToWindow() {
        if (plugin != null) {
            plugin.onPluginAttachedToWindow();
        } else {
            super.onAttachedToWindow();
        }
    }

    public void onSuperAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    public void onDetachedFromWindow() {
        if (plugin != null) {
            plugin.onPluginDetachedFromWindow();
        } else {
            super.onDetachedFromWindow();
        }
    }

    public void onSuperDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Nullable
    @Override
    public View onCreatePanelView(int featureId) {
        if (plugin != null) {
            return plugin.onPluginCreatePanelView(featureId);
        } else {
            return super.onCreatePanelView(featureId);
        }
    }

    public View onSuperCreatePanelView(int featureId) {
        return super.onCreatePanelView(featureId);
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        if (plugin != null) {
            return plugin.onPluginCreatePanelMenu(featureId, menu);
        }
        return super.onCreatePanelMenu(featureId, menu);
    }

    public boolean onSuperCreatePanelMenu(int featureId, Menu menu) {
        return super.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public boolean onPreparePanel(int featureId, View view, Menu menu) {
        if (plugin != null) {
            return plugin.onPluginPreparePanel(featureId, view, menu);
        }
        return super.onPreparePanel(featureId, view, menu);
    }

    public boolean onSuperPreparePanel(int featureId, View view, Menu menu) {
        return super.onPreparePanel(featureId, view, menu);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (plugin != null) {
            return plugin.onPluginMenuOpened(featureId, menu);
        }
        return super.onMenuOpened(featureId, menu);
    }

    public boolean onSuperMenuOpened(int featureId, Menu menu) {
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (plugin != null) {
            return plugin.onPluginMenuItemSelected(featureId, item);
        }
        return super.onMenuItemSelected(featureId, item);
    }

    public boolean onSuperMenuItemSelected(int featureId, MenuItem item) {
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public void onPanelClosed(int featureId, Menu menu) {
        if (plugin != null) {
            plugin.onPluginPanelClosed(featureId, menu);
        } else {
            super.onPanelClosed(featureId, menu);
        }
    }

    public void onSuperPanelClosed(int featureId, Menu menu) {
        super.onPanelClosed(featureId, menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (plugin != null) {
            return plugin.onPluginCreateOptionsMenu(menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onSuperCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (plugin != null) {
            return plugin.onPluginPrepareOptionsMenu(menu);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    public boolean onSuperPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (plugin != null) {
            return plugin.onPluginOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }


    public boolean onSuperOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        if (plugin != null) {
            plugin.onPluginOptionsMenuClosed(menu);
        } else {
            super.onOptionsMenuClosed(menu);
        }
    }

    public void onSuperOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (plugin != null) {
            return plugin.onPluginContextItemSelected(item);
        } else {
            return super.onContextItemSelected(item);
        }
    }

    public boolean onSuperContextItemSelected(MenuItem item) {
        return super.onContextItemSelected(item);
    }

    @Override
    public void onContextMenuClosed(Menu menu) {
        if (plugin != null) {
            plugin.onPluginContextMenuClosed(menu);
        } else {
            super.onContextMenuClosed(menu);
        }
    }

    public void onSuperContextMenuClosed(Menu menu) {
        super.onContextMenuClosed(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (plugin != null) {
            plugin.onPluginActivityResult(requestCode, resultCode, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void onSuperActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        if (plugin != null) {
            plugin.onPluginActivityReenter(resultCode, data);
        } else {
            super.onActivityReenter(resultCode, data);
        }
    }

    public void onSuperActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);
    }

    @Override
    protected void onTitleChanged(CharSequence title, int color) {
        if (plugin != null) {
            plugin.onPluginTitleChanged(title, color);
        } else {
            super.onTitleChanged(title, color);
        }
    }

    public void onSuperTitleChanged(CharSequence title, int color) {
        super.onTitleChanged(title, color);
    }

    @Nullable
    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        if (plugin != null) {
            return plugin.onPluginCreateView(name, context, attrs);
        } else {
            return super.onCreateView(name, context, attrs);
        }
    }

    public View onSuperCreateView(String name, Context context, AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        if (plugin != null) {
            return plugin.onPluginCreateView(parent, name, context, attrs);
        } else {
            return super.onCreateView(parent, name, context, attrs);
        }
    }

    public View onSuperCreateView(View parent, String name, Context context, AttributeSet attrs) {
        return super.onCreateView(parent, name, context, attrs);
    }

    @Override
    public void onVisibleBehindCanceled() {
        if (plugin != null) {
            plugin.onPluginVisibleBehindCanceled();
        } else {
            super.onVisibleBehindCanceled();
        }
    }

    public void onSuperVisibleBehindCanceled() {
        super.onVisibleBehindCanceled();
    }

    @Override
    public void onEnterAnimationComplete() {
        if (plugin != null) {
            try {
                plugin.onPluginEnterAnimationComplete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            super.onEnterAnimationComplete();
        }
    }

    public void onSuperEnterAnimationComplete() {
        super.onEnterAnimationComplete();
    }

    @Override
    public Resources getResources() {
        if (plugin != null) {
            return plugin.getPluginResource();
        } else {
            return super.getResources();
        }
    }

    public Resources getHostResource() {
        return super.getResources();
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        if (plugin != null) {
            if ((intent instanceof PluginIntent)&&((PluginIntent)intent).hasFlag(PluginIntent.FLAG_LAUNCH_PLUGIN)) { //启动一个pluginactivity
                PluginIntent pluginIntent = (PluginIntent) intent;
                if (pluginIntent.hasFlag(PluginIntent.FLAG_LAUNCH_ACTUAL_ACTIVITY)) {
                    super.startActivityForResult(pluginIntent, requestCode);
                } else {
                    PluginManager.getInstance(getApplicationContext()).startPluginActivityForResult(this, pluginIntent, requestCode);
                }
            } else { //启动正常activity
                super.startActivityForResult(intent, requestCode);
            }
        } else {
            super.startActivityForResult(intent, requestCode);
        }
    }
}
