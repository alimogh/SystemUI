package com.oneplus.systemui.statusbar.phone;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import com.android.internal.util.ContrastColorUtil;
import com.android.systemui.C0008R$id;
import com.android.systemui.Dependency;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.statusbar.NotificationMediaManager;
import com.android.systemui.statusbar.StatusBarIconView;
import com.android.systemui.statusbar.notification.NotificationUtils;
import com.android.systemui.statusbar.notification.NotificationWakeUpCoordinator;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.oneplus.aod.OpAodNotificationIconAreaController;
import com.oneplus.notification.OpNotificationController;
import com.oneplus.util.OpUtils;
import java.util.Objects;
public class OpNotificationIconAreaController {
    protected OpAodNotificationIconAreaController mAodNotificationIconCtrl;
    private final OpNotificationController mOpNotificationController = ((OpNotificationController) Dependency.get(OpNotificationController.class));

    /* access modifiers changed from: protected */
    public boolean shouldShowNotificationIcon(NotificationEntry notificationEntry, boolean z, boolean z2, boolean z3, boolean z4, boolean z5, boolean z6, boolean z7, boolean z8, boolean z9, NotificationMediaManager notificationMediaManager, StatusBarIconView statusBarIconView, BubbleController bubbleController, NotificationWakeUpCoordinator notificationWakeUpCoordinator) {
        boolean z10 = (statusBarIconView == null || notificationEntry.getIcons().getCenteredIcon() == null || !Objects.equals(notificationEntry.getIcons().getCenteredIcon(), statusBarIconView)) ? false : true;
        if (z8) {
            return z10;
        }
        if (z6 && z10 && !notificationEntry.isRowHeadsUp()) {
            return false;
        }
        if (notificationEntry.getRanking().isAmbient() && !z) {
            return false;
        }
        if (z5 && notificationEntry.getKey().equals(notificationMediaManager.getMediaNotificationKey())) {
            return false;
        }
        if ((!z2 && notificationEntry.getImportance() < 3) || !notificationEntry.isTopLevelChild() || notificationEntry.getRow().getVisibility() == 8) {
            return false;
        }
        if (notificationEntry.isRowDismissed() && z3) {
            return false;
        }
        if (z4 && notificationEntry.isLastMessageFromReply()) {
            return false;
        }
        if (!z && notificationEntry.shouldSuppressStatusBar()) {
            return false;
        }
        if ((z7 && notificationEntry.showingPulsing() && (!notificationWakeUpCoordinator.getNotificationsFullyHidden() || !notificationEntry.isPulseSuppressed())) || bubbleController.isBubbleExpanded(notificationEntry)) {
            return false;
        }
        if (z9) {
            Bundle bundle = null;
            if (notificationEntry.getSbn().getNotification() != null) {
                bundle = notificationEntry.getSbn().getNotification().extras;
            }
            if (bundle != null && bundle.getBoolean("hide_icon", false)) {
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void updateTintForIconInternal(StatusBarIconView statusBarIconView, int i, Context context, ContrastColorUtil contrastColorUtil, Rect rect) {
        boolean equals = Boolean.TRUE.equals(statusBarIconView.getTag(C0008R$id.icon_is_pre_L));
        boolean z = true;
        int i2 = 0;
        if (!this.mOpNotificationController.shouldColorizeIcon(statusBarIconView.getNotification() != null ? statusBarIconView.getNotification().getPackageName() : "") ? (equals || !OpUtils.isGlobalROM(context)) && !NotificationUtils.isGrayscale(statusBarIconView, contrastColorUtil) : equals && !NotificationUtils.isGrayscale(statusBarIconView, contrastColorUtil)) {
            z = false;
        }
        if (z) {
            i2 = DarkIconDispatcher.getTint(rect, statusBarIconView, i);
        }
        statusBarIconView.setStaticDrawableColor(i2);
        statusBarIconView.setDecorColor(i);
    }

    public void setAodIconController(OpAodNotificationIconAreaController opAodNotificationIconAreaController) {
        this.mAodNotificationIconCtrl = opAodNotificationIconAreaController;
    }
}
