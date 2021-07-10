package com.android.systemui.dagger;

import android.content.Context;
import android.os.Handler;
import android.os.PowerManager;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.power.EnhancedEstimates;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.RecentsImplementation;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.phone.HeadsUpManagerPhone;
import com.android.systemui.statusbar.phone.KeyguardBypassController;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.BatteryControllerImpl;
import com.android.systemui.statusbar.policy.ConfigurationController;
public abstract class SystemUIDefaultModule {
    static boolean provideAllowNotificationLongPress() {
        return true;
    }

    static String provideLeakReportEmail() {
        return null;
    }

    static BatteryController provideBatteryController(Context context, EnhancedEstimates enhancedEstimates, PowerManager powerManager, BroadcastDispatcher broadcastDispatcher, Handler handler, Handler handler2) {
        BatteryControllerImpl batteryControllerImpl = new BatteryControllerImpl(context, enhancedEstimates, powerManager, broadcastDispatcher, handler, handler2);
        batteryControllerImpl.init();
        return batteryControllerImpl;
    }

    static HeadsUpManagerPhone provideHeadsUpManagerPhone(Context context, StatusBarStateController statusBarStateController, KeyguardBypassController keyguardBypassController, NotificationGroupManager notificationGroupManager, ConfigurationController configurationController) {
        return new HeadsUpManagerPhone(context, statusBarStateController, keyguardBypassController, notificationGroupManager, configurationController);
    }

    static Recents provideRecents(Context context, RecentsImplementation recentsImplementation, CommandQueue commandQueue) {
        return new Recents(context, recentsImplementation, commandQueue);
    }
}
