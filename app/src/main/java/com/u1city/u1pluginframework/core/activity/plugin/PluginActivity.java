package com.u1city.u1pluginframework.core.activity.plugin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.u1city.u1pluginframework.core.PluginIntent;
import com.u1city.u1pluginframework.core.activity.host.HostActivity;
import com.u1city.u1pluginframework.core.pk.PluginApk;

/**
 * plugin activity，开发者应该实现onPluginXXX(),而不是onXXX();
 * 例如开发者应该实现onPluginCreate(Bundle),而不是onCreate(Bundle),
 * 应该使用带有“plugin”的API，activity的API在activity作为插件的情况下无效
 * 或者是引发异常，应该避免使用。当以正常模式启动activity时，开发者仍可使用activity的API
 * Created by user on 2016/12/2.
 */
public class PluginActivity extends Activity implements IPlugin{
    public static final String KEY_PLUGIN_ACTIVITY_INFO = "key_plugin_activityInfo";
    public static final String KEY_PLUGIN_NAME = "key_plugin_name";
    private static final String TAG = "PluginActivity";

    protected HostActivity host;
    private PluginApk apk;
    @Override
    public void onPluginCreate(Bundle savedInstanceState) {
        host.onSuperCreate(savedInstanceState);
        Log.d(TAG,"======onPluginCreate======");
    }

    @Override
    public void onPluginStart() {
        host.onSuperStart();
        Log.d(TAG, "======onPluginStart======");
    }

    @Override
    public void onPluginRestart() {
        host.onSuperRestart();
    }

    @Override
    public void onPluginRestoreInstanceState(Bundle savedInstanceState) {
        host.onSuperRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onPluginResume() {
        host.onSuperResume();
        Log.d(TAG, "======onPluginResume======");
    }

    @Override
    public void onPluginPause() {
        host.onSuperPause();
        Log.d(TAG, "======onPluginPause======");
    }

    @Override
    public void onPluginSaveInstanceState(Bundle outState) {
        host.onSuperSaveInstanceState(outState);
    }

    @Override
    public void onPluginStop() {
        host.onSuperStop();
        Log.d(TAG, "======onPluginStop======");
    }

    @Override
    public void onPluginDestroy() {
        host.onSuperDestroy();
        Log.d(TAG, "======onPluginDestroy======");
    }

    @Override
    public boolean onPluginCreateThumbnail(Bitmap outBitmap, Canvas canvas) {
        return host.onSuperCreateThumbnail(outBitmap,canvas);
    }

    @Override
    public CharSequence onPluginCreateDescription() {
        return host.onSuperCreateDescription();
    }

    @Override
    public void onPluginUserLeaveHint() {
        host.onSuperUserLeaveHint();
    }

    @Override
    public void onPluginNewIntent(Intent intent) {
        host.onSuperNewIntent(intent);
    }

    @Override
    public void onPluginStateNotSaved() {
        host.onSuperStateNotSaved();
    }

    @Override
    public void onPluginConfigurationChanged(Configuration newConfig) {
        host.onSuperConfigurationChanged(newConfig);
    }

    @Override
    public void onPluginLowMemory() {
        host.onSuperLowMemory();
    }

    @Override
    public void onPluginTrimMemory(int level) {
        host.onSuperTrimMemory(level);
    }

    @Override
    public boolean onPluginKeyDown(int keyCode, KeyEvent event) {
        return host.onSuperKeyDown(keyCode,event);
    }

    @Override
    public boolean onPluginKeyLongPress(int keyCode, KeyEvent event) {
        return host.onSuperKeyLongPress(keyCode,event);
    }

    @Override
    public boolean onPluginKeyUp(int keyCode, KeyEvent event) {
        return host.onSuperKeyUp(keyCode,event);
    }

    @Override
    public boolean onPluginKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        return host.onSuperKeyMultiple(keyCode,repeatCount,event);
    }

    @Override
    public void onPluginBackPressed() {
        host.onSuperBackPressed();
    }

    @Override
    public boolean onPluginKeyShortcut(int keyCode, KeyEvent event) {
        return host.onSuperKeyShortcut(keyCode, event);
    }

    @Override
    public boolean onPluginTouchEvent(MotionEvent event) {
        return host.onSuperTouchEvent(event);
    }

    @Override
    public void onPluginUserInteraction() {
        host.onSuperUserInteraction();
    }

    @Override
    public void onPluginWindowAttributesChanged(WindowManager.LayoutParams params) {
        host.onSuperWindowAttributesChanged(params);
    }

    @Override
    public void onPluginContentChanged() {
        host.onSuperContentChanged();
    }

    @Override
    public void onPluginWindowFocusChanged(boolean hasFocus) {
        host.onSuperWindowFocusChanged(hasFocus);
    }

    @Override
    public void onPluginAttachedToWindow() {
        host.onSuperAttachedToWindow();
    }

    @Override
    public void onPluginDetachedFromWindow() {
        host.onSuperDetachedFromWindow();
    }

    @Override
    public View onPluginCreatePanelView(int featureId) {
        return host.onSuperCreatePanelView(featureId);
    }

    @Override
    public boolean onPluginCreatePanelMenu(int featureId, Menu menu) {
        return host.onSuperCreatePanelMenu(featureId, menu);
    }

    @Override
    public boolean onPluginPreparePanel(int featureId, View view, Menu menu) {
        return host.onSuperPreparePanel(featureId, view, menu);
    }

    @Override
    public boolean onPluginMenuOpened(int featureId, Menu menu) {
        return host.onSuperMenuOpened(featureId, menu);
    }

    @Override
    public boolean onPluginMenuItemSelected(int featureId, MenuItem item) {
        return host.onSuperMenuItemSelected(featureId, item);
    }

    @Override
    public void onPluginOptionsMenuClosed(Menu menu) {
        host.onSuperOptionsMenuClosed(menu);
    }

    @Override
    public boolean onPluginContextItemSelected(MenuItem item) {
        return host.onSuperContextItemSelected(item);
    }

    @Override
    public void onPluginContextMenuClosed(Menu menu) {
        host.onSuperContextMenuClosed(menu);
    }

    @Override
    public void onPluginActivityResult(int requestCode, int resultCode, Intent data) {
        host.onSuperActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPluginActivityReenter(int resultCode, Intent data) {
        host.onSuperActivityReenter(resultCode, data);
    }

    @Override
    public void onPluginTitleChanged(CharSequence title, int color) {
        host.onSuperTitleChanged(title,color);
    }

    @Override
    public View onPluginCreateView(String name, Context context, AttributeSet attrs) {
        return host.onSuperCreateView(name, context, attrs);
    }

    @Override
    public View onPluginCreateView(View parent, String name, Context context, AttributeSet attrs) {
        return host.onSuperCreateView(parent, name, context, attrs);
    }

    @Override
    public void onPluginVisibleBehindCanceled() {
        host.onSuperVisibleBehindCanceled();
    }

    @Override
    public void onPluginPanelClosed(int featureId, Menu menu) {
        host.onSuperPanelClosed(featureId, menu);
    }

    @Override
    public boolean onPluginCreateOptionsMenu(Menu menu) {
        return host.onSuperCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPluginPrepareOptionsMenu(Menu menu) {
        return host.onSuperPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onPluginOptionsItemSelected(MenuItem item) {
        return host.onSuperOptionsItemSelected(item);
    }

    @Override
    public void onPluginEnterAnimationComplete() {
        host.onSuperEnterAnimationComplete();
    }

    @Override
    public void setPluginContentView(int res) {
        host.setContentView(res);
    }

    @Override
    public void setPluginContentView(View view) {
        host.setContentView(view);
    }

    @Override
    public Resources getPluginResource() {
        return apk.getResources();
    }

    @Override
    public Resources getHostResource() {
        return host.getHostResource();
    }

    @Override
    public void startPluginActivity(PluginIntent intent) {
        startPluginActivityForResult(intent,0);
    }

    @Override
    public void startPluginActivityForResult(PluginIntent intent, int requestCode) {
        host.startActivityForResult(intent,requestCode);
    }

    @Override
    public View findPluginViewById(int id) {
        return host.findViewById(id);
    }

    @Override
    public void setHost(HostActivity host) {
        this.host = host;
    }

    @Override
    public void setApk(PluginApk apk) {
        this.apk = apk;
    }
}
