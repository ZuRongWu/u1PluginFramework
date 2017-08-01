package com.u1city.u1pluginframework.core.pm;

import android.content.res.XmlResourceParser;
import android.util.Log;

import com.u1city.u1pluginframework.core.error.ConfigFileFormatError;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.List;

/**
 * 解析plugin.xml
 * 格式:
 * <?xml version="1.0" encoding="utf-8"?>
 * <plugin>
 * <dependencies>
 * <item name="">
 * <path>http:</path>
 * </item>
 * .
 * .
 * .
 * </dependencies>
 * </plugin>
 * Created by wzr on 2016/12/6.
 */
class BaseConfigParser implements ConfigParser {
    private static final String TAG = "BaseConfigParser";

    @Override
    public void parse(PluginApk apk) {
        int confId = apk.getResources().getIdentifier("plugin", "xml", apk.getPluginName());
        if (confId == 0) {
            Log.i(TAG, "没有找到配置文件：plugin.xml");
            return;
        }
        XmlResourceParser parser = apk.getResources().getXml(confId);
        try {
            List<PluginApk.Dependency> dependencies = new ArrayList<>(3);
            PluginApk.Dependency dependency = null;
            boolean parseDep = false;
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        Log.d(TAG, "======start document======");
                        break;
                    case XmlPullParser.START_TAG:
                        String name = parser.getName();
                        if (name.equalsIgnoreCase("dependencies")) {
                            parseDep = true;
                        } else if (name.equalsIgnoreCase("item")) {
                            if (parseDep) {
                                dependency = new PluginApk.Dependency();
                                dependency.name = parser.getAttributeValue(null, "name");
                            }
                        } else if (name.equalsIgnoreCase("path")) {
                            if (dependency != null) {
                                dependency.path = parser.nextText();
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        String tagName = parser.getName();
                        if (tagName.equalsIgnoreCase("item") && dependency != null) {
                            dependencies.add(dependency);
                            dependency = null;
                        } else if (tagName.equalsIgnoreCase("dependencies")) {
                            parseDep = false;
                        }
                        break;
                }
                eventType = parser.next();
            }
            apk.setDependencies(dependencies);
        } catch (Exception e) {
            throw new ConfigFileFormatError();
        }
    }
}
