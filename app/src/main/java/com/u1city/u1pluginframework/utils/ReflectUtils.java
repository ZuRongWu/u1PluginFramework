package com.u1city.u1pluginframework.utils;

import java.lang.reflect.Field;

/**
 * Created by wuzr on 2017/7/14.
 * 反射工具类
 */

public class ReflectUtils {
    /**
     * 获取属性对应的值
     * @param fieldName 指定属性的名称
     * @param obj 对应的对象
     * @return 属性值
     */
    public static Object readField(String fieldName,Object obj){
        for(Class<?> c = obj.getClass(); c != Object.class; c = c.getSuperclass()){
            try{
                Field info = c.getDeclaredField(fieldName);
                info.setAccessible(true);
                return info.get(obj);
            }catch (Exception e){
                //do nothing
            }
        }
        return null;
    }

    /**
     * 设置对应属性的值
     * @param fieldName 属性名称
     * @param obj 对应的对象
     * @param value 值
     * @return true 设置成功；false 设置失败
     */
    public static boolean setField(String fieldName,Object obj,Object value){
        for(Class<?> c = obj.getClass(); c != Object.class; c.getSuperclass()){
            try{
                Field info = c.getDeclaredField(fieldName);
                info.setAccessible(true);
                info.set(obj,value);
                return true;
            }catch (Exception e){
                //do nothing
            }
        }
        return false;
    }
}
