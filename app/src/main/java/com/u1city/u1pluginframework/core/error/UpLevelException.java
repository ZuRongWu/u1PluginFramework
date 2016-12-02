package com.u1city.u1pluginframework.core.error;

/**
 * 调用{@link com.u1city.u1pluginframework.core.pk.PackageManager#updatePlugin(String)}
 * 当已安装的插件的版本号大于更新包的版本号是抛出此异常
 * Created by wuzr on 2016/12/2.
 */
public class UpLevelException extends Exception{
    public UpLevelException(){
        super("已安装的版本号大于更新包的版本号");
    }
}
