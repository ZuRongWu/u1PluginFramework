package com.u1city.u1pluginframework.core.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.u1city.u1pluginframework.core.PluginIntent;
import com.u1city.u1pluginframework.core.pm.PluginApk;

/**
 * pluginActivity的接口
 * Created by wuzr on 2016/12/5.
 */
public interface IPlugin {
    void onPluginCreate(Bundle savedInstanceState);

    void onPluginStart();

    void onPluginRestart();

    void onPluginRestoreInstanceState(Bundle savedInstanceState);

    void onPluginResume();

    void onPluginPause();

    void onPluginSaveInstanceState(Bundle outState);

    void onPluginStop();

    void onPluginDestroy();

    boolean onPluginCreateThumbnail(Bitmap outBitmap, Canvas canvas);

    CharSequence onPluginCreateDescription();

    void onPluginUserLeaveHint();

    void onPluginNewIntent(Intent intent);

    void onPluginStateNotSaved();

    void onPluginConfigurationChanged(Configuration newConfig);

    void onPluginLowMemory();

    void onPluginTrimMemory(int level);

    boolean onPluginKeyDown(int keyCode, KeyEvent event);

    boolean onPluginKeyLongPress(int keyCode, KeyEvent event);

    boolean onPluginKeyUp(int keyCode, KeyEvent event);

    boolean onPluginKeyMultiple(int keyCode, int repeatCount, KeyEvent event);

    void onPluginBackPressed();

    boolean onPluginKeyShortcut(int keyCode, KeyEvent event);

    boolean onPluginTouchEvent(MotionEvent event);

    void onPluginUserInteraction();

    void onPluginWindowAttributesChanged(WindowManager.LayoutParams params);

    void onPluginContentChanged();

    void onPluginWindowFocusChanged(boolean hasFocus);

    void onPluginAttachedToWindow();

    void onPluginDetachedFromWindow();

    View onPluginCreatePanelView(int featureId);

    boolean onPluginCreatePanelMenu(int featureId, Menu menu);

    boolean onPluginPreparePanel(int featureId, View view, Menu menu);

    boolean onPluginMenuOpened(int featureId, Menu menu);

    boolean onPluginMenuItemSelected(int featureId, MenuItem item);

    void onPluginOptionsMenuClosed(Menu menu);

    boolean onPluginContextItemSelected(MenuItem item);

    void onPluginContextMenuClosed(Menu menu);

    void onPluginActivityResult(int requestCode, int resultCode, Intent data);

    void onPluginActivityReenter(int resultCode, Intent data);

    void onPluginTitleChanged(CharSequence title, int color);

    View onPluginCreateView(String name, Context context, AttributeSet attrs);

    View onPluginCreateView(View parent, String name, Context context, AttributeSet attrs);

    void onPluginVisibleBehindCanceled();

    void onPluginPanelClosed(int featureId, Menu menu);

    boolean onPluginCreateOptionsMenu(Menu menu);

    boolean onPluginPrepareOptionsMenu(Menu menu);

    boolean onPluginOptionsItemSelected(MenuItem item);

    void onPluginEnterAnimationComplete();

    void setPluginContentView(int res);

    void setPluginContentView(View view);

    Resources getPluginResource();

    Resources getHostResource();

    Resources getPluginResource(String pluginName);

    void startPluginActivity(PluginIntent intent);

    void startPluginActivityForResult(PluginIntent intent, int requestCode);

    View findPluginViewById(int id);

    void setHost(HostActivity host);

    void setApk(PluginApk apk);
}
