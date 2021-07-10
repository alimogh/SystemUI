package com.oneplus.common;

import android.util.Log;
public class ReflectUtil {
    public static String get(String str) {
        try {
            return (String) Class.forName("android.os.SystemProperties").getMethod("get", String.class).invoke(null, str);
        } catch (Exception e) {
            Log.e("ReflectUtil", e.getMessage());
            return "";
        }
    }
}
