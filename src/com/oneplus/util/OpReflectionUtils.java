package com.oneplus.util;

import android.util.Log;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
public class OpReflectionUtils {
    public static Object getValue(Class cls, Object obj, String str) {
        try {
            Field declaredField = cls.getDeclaredField(str);
            declaredField.setAccessible(true);
            try {
                return declaredField.get(obj);
            } catch (IllegalAccessException unused) {
                Log.e("OpReflectionUtils", "getValue IllegalAccess " + cls + " field " + str);
                return null;
            }
        } catch (NoSuchFieldException unused2) {
            Log.e("OpReflectionUtils", "getValue NoSuchField " + cls + " field " + str);
            return null;
        }
    }

    public static void setValue(Class cls, Object obj, String str, Object obj2) {
        try {
            Field declaredField = cls.getDeclaredField(str);
            declaredField.setAccessible(true);
            try {
                declaredField.set(obj, obj2);
            } catch (IllegalAccessException unused) {
                Log.e("OpReflectionUtils", "getValue IllegalAccess " + cls + " field " + str);
            }
        } catch (NoSuchFieldException unused2) {
            Log.e("OpReflectionUtils", "getValue NoSuchField " + cls + " field " + str);
        }
    }

    public static Object methodInvokeVoid(Class cls, Object obj, String str, Object... objArr) {
        try {
            Method declaredMethod = cls.getDeclaredMethod(str, new Class[0]);
            declaredMethod.setAccessible(true);
            try {
                return declaredMethod.invoke(obj, objArr);
            } catch (IllegalAccessException unused) {
                Log.e("OpReflectionUtils", "methodInvokeVoid IllegalAccess " + cls + " method " + str);
                return null;
            } catch (InvocationTargetException unused2) {
                Log.e("OpReflectionUtils", "methodInvokeVoid InvocationTarget Class " + cls + " method " + str);
                return null;
            }
        } catch (NoSuchMethodException unused3) {
            Log.e("OpReflectionUtils", "methodInvokeVoid NoSuchMethod " + cls + " method " + str);
            return null;
        }
    }

    public static Method getMethodWithParams(Class cls, String str, Class<?>... clsArr) {
        try {
            return cls.getDeclaredMethod(str, clsArr);
        } catch (NoSuchMethodException unused) {
            Log.e("OpReflectionUtils", "getMethodWithParams NoSuchMethod " + cls + " method " + str);
            return null;
        }
    }

    public static Object methodInvokeWithArgs(Object obj, Method method, Object... objArr) {
        method.setAccessible(true);
        try {
            return method.invoke(obj, objArr);
        } catch (IllegalAccessException e) {
            Log.e("OpReflectionUtils", "methodInvokeWithArgs IllegalAccess Object " + obj.toString() + " method " + method.getName());
            StringBuilder sb = new StringBuilder();
            sb.append("methodInvokeWithArgs IllegalAccess e ");
            sb.append(e.toString());
            Log.e("OpReflectionUtils", sb.toString());
            e.printStackTrace();
            return null;
        } catch (InvocationTargetException e2) {
            Log.e("OpReflectionUtils", "methodInvokeWithArgs InvocationTarget Object " + obj.toString() + " method " + method.getName());
            StringBuilder sb2 = new StringBuilder();
            sb2.append("methodInvokeWithArgs ");
            sb2.append(e2.toString());
            Log.e("OpReflectionUtils", sb2.toString());
            return null;
        }
    }
}
