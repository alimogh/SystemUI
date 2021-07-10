package com.verizon.loginenginesvc.clientsdk.internal;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
public class Settings {
    private static final ComponentName[] SVC_COMPONENTS = {new ComponentName("com.verizon.mips.services", "com.verizon.loginenginesvc.LoginEngineService"), new ComponentName("com.verizon.loginengine.unbranded", "com.verizon.loginenginesvc.LoginEngineService"), new ComponentName("com.motricity.verizon.ssoengine", "com.verizon.loginenginesvc.LoginEngineService")};

    public static ComponentName findService(Context context) {
        ComponentName[] componentNameArr = SVC_COMPONENTS;
        for (ComponentName componentName : componentNameArr) {
            if (isAvailable(context, componentName)) {
                return componentName;
            }
        }
        return null;
    }

    private static boolean isAvailable(Context context, ComponentName componentName) {
        if (componentName == null) {
            return false;
        }
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(componentName.getPackageName(), 4);
            if (packageInfo != null) {
                if (packageInfo.services != null) {
                    ServiceInfo[] serviceInfoArr = packageInfo.services;
                    for (ServiceInfo serviceInfo : serviceInfoArr) {
                        if ("com.verizon.loginenginesvc.LoginEngineService".equals(serviceInfo.name)) {
                            return serviceInfo.enabled;
                        }
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException unused) {
        }
        return false;
    }
}
