package com.android.systemui.statusbar.notification.row.wrapper;

import android.content.Context;
import android.graphics.drawable.Icon;
import android.service.notification.StatusBarNotification;
import android.view.View;
import com.android.systemui.statusbar.notification.ImageTransformState;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
public class NotificationBigPictureTemplateViewWrapper extends NotificationTemplateViewWrapper {
    protected NotificationBigPictureTemplateViewWrapper(Context context, View view, ExpandableNotificationRow expandableNotificationRow) {
        super(context, view, expandableNotificationRow);
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationTemplateViewWrapper, com.android.systemui.statusbar.notification.row.wrapper.NotificationHeaderViewWrapper, com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    public void onContentUpdated(ExpandableNotificationRow expandableNotificationRow) {
        super.onContentUpdated(expandableNotificationRow);
        updateImageTag(expandableNotificationRow.getEntry().getSbn());
    }

    private void updateImageTag(StatusBarNotification statusBarNotification) {
        Icon icon = (Icon) statusBarNotification.getNotification().extras.getParcelable("android.largeIcon.big");
        if (icon != null) {
            this.mPicture.setTag(ImageTransformState.ICON_TAG, icon);
        }
    }
}
