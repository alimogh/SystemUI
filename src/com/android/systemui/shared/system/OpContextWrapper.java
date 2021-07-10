package com.android.systemui.shared.system;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.UserHandle;
import android.util.Log;
public class OpContextWrapper {
    public static Context getCurrentUserContext(Context context) {
        int currentUserId = ActivityManagerWrapper.getInstance().getCurrentUserId();
        Log.d("OpContextWrapper", "getCurrentUserContext# contextUser=" + context.getUserId() + " currentUser=" + currentUserId);
        if (context.getUserId() == currentUserId) {
            return context;
        }
        try {
            return context.createPackageContextAsUser(context.getPackageName(), 0, UserHandle.of(currentUserId));
        } catch (PackageManager.NameNotFoundException unused) {
            return null;
        }
    }
}
