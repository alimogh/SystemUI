package com.android.systemui.statusbar.phone.dagger;

import com.android.systemui.statusbar.phone.NotificationPanelViewController;
import com.android.systemui.statusbar.phone.NotificationShadeWindowView;
import com.android.systemui.statusbar.phone.NotificationShadeWindowViewController;
import com.android.systemui.statusbar.phone.StatusBarWindowController;
public interface StatusBarComponent {

    public interface Builder {
        StatusBarComponent build();

        Builder statusBarWindowView(NotificationShadeWindowView notificationShadeWindowView);
    }

    NotificationPanelViewController getNotificationPanelViewController();

    NotificationShadeWindowViewController getNotificationShadeWindowViewController();

    StatusBarWindowController getStatusBarWindowController();
}
