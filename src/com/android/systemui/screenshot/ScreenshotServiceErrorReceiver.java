package com.android.systemui.screenshot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.WindowManager;
import com.android.systemui.C0015R$string;
public class ScreenshotServiceErrorReceiver extends BroadcastReceiver {
    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        new ScreenshotNotificationsController(context, (WindowManager) context.getSystemService("window")).notifyScreenshotError(C0015R$string.screenshot_failed_to_save_unknown_text);
    }
}
