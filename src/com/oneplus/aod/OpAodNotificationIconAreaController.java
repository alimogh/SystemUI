package com.oneplus.aod;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.util.ContrastColorUtil;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0008R$id;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.StatusBarIconView;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.NotificationIconDozeHelper;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.phone.NotificationIconContainer;
import com.oneplus.aod.utils.OpAodDimenHelper;
import com.oneplus.util.OpUtils;
public class OpAodNotificationIconAreaController {
    private Context mContext;
    private final ContrastColorUtil mContrastColorUtil;
    private NotificationEntryManager mEntryManager = ((NotificationEntryManager) Dependency.get(NotificationEntryManager.class));
    private int mIconHPadding;
    private int mIconSize;
    private TextView mMoreIcon;
    private NotificationLockscreenUserManager mNLockScreenUserManager = ((NotificationLockscreenUserManager) Dependency.get(NotificationLockscreenUserManager.class));
    private View mNotificationIconArea;
    private NotificationIconDozeHelper mNotificationIconDozeHelper;
    private OpIconMerger mNotificationIcons;
    private OpClockViewCtrl mOpClockViewCtrl;

    public void onUserSwitchComplete(int i) {
    }

    public OpAodNotificationIconAreaController(Context context, OpClockViewCtrl opClockViewCtrl) {
        this.mContrastColorUtil = ContrastColorUtil.getInstance(context);
        this.mNotificationIconDozeHelper = new NotificationIconDozeHelper(context);
        this.mOpClockViewCtrl = opClockViewCtrl;
        this.mContext = context;
        KeyguardUpdateMonitor.getCurrentUser();
    }

    public void initViews(ViewGroup viewGroup) {
        View findViewById = viewGroup.findViewById(C0008R$id.notification_icon_area_inner);
        this.mNotificationIconArea = findViewById;
        this.mNotificationIcons = (OpIconMerger) findViewById.findViewById(C0008R$id.notificationIcons);
        this.mMoreIcon = (TextView) this.mNotificationIconArea.findViewById(C0008R$id.moreIcon);
        reloadDimens();
    }

    private LinearLayout.LayoutParams generateIconLayoutParams(int i) {
        int i2 = this.mIconSize;
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(i2, i2);
        if (i != 0) {
            layoutParams.setMarginStart(this.mIconHPadding);
        }
        return layoutParams;
    }

    private void reloadDimens() {
        this.mIconSize = OpAodDimenHelper.convertDpToFixedPx(this.mContext, C0005R$dimen.aod_notification_icon_fixed_size);
        this.mIconHPadding = OpAodDimenHelper.convertDpToFixedPx(this.mContext, C0005R$dimen.aod_notification_icon_fixed_padding);
        ((LinearLayout.LayoutParams) this.mMoreIcon.getLayoutParams()).setMarginStart(this.mIconHPadding);
    }

    public void updateNotificationIcons(NotificationIconContainer notificationIconContainer) {
        NotificationEntry activeNotificationUnfiltered;
        if (this.mOpClockViewCtrl.getController() == null || !this.mOpClockViewCtrl.getController().shouldShowNotification()) {
            this.mNotificationIconArea.setVisibility(8);
            return;
        }
        reloadDimens();
        int childCount = notificationIconContainer.getChildCount();
        this.mNotificationIcons.removeAllViews();
        boolean z = childCount > 3;
        if (OpUtils.DEBUG_ONEPLUS) {
            Log.d("AodNotificationIconArea", "updateNotificationIcons: iconSize=" + childCount + ", maxIconAmounts=3, showMore=" + z);
        }
        if (childCount == 0) {
            Log.d("AodNotificationIconArea", "updateNotificationIcons: setVisibility to gone");
            this.mNotificationIconArea.setVisibility(8);
            return;
        }
        this.mNotificationIconArea.setVisibility(0);
        if (z) {
            this.mMoreIcon.setVisibility(0);
            TextView textView = this.mMoreIcon;
            StringBuilder sb = new StringBuilder();
            sb.append("+");
            sb.append(childCount - 3);
            textView.setText(sb.toString());
        } else {
            this.mMoreIcon.setVisibility(8);
        }
        if (z) {
            childCount = 3;
        }
        this.mNotificationIcons.removeAllViews();
        for (int i = 0; i < childCount; i++) {
            StatusBarIconView statusBarIconView = (StatusBarIconView) notificationIconContainer.getChildAt(i);
            if (!(statusBarIconView == null || (activeNotificationUnfiltered = this.mEntryManager.getActiveNotificationUnfiltered(statusBarIconView.getNotification().getKey())) == null || !this.mNLockScreenUserManager.shouldShowOnKeyguard(activeNotificationUnfiltered))) {
                OpAodNotificationIconView opAodNotificationIconView = new OpAodNotificationIconView(this.mContext, statusBarIconView.getSlot(), statusBarIconView.getNotification().getNotification());
                opAodNotificationIconView.set(statusBarIconView.getStatusBarIcon());
                LinearLayout.LayoutParams generateIconLayoutParams = generateIconLayoutParams(i);
                opAodNotificationIconView.setAllowAnimation(false);
                if (this.mContrastColorUtil.isGrayscaleIcon(opAodNotificationIconView.getDrawable())) {
                    opAodNotificationIconView.setImageTintList(ColorStateList.valueOf(-1));
                } else {
                    this.mNotificationIconDozeHelper.setImageDark(opAodNotificationIconView, true, false, 0, true);
                }
                this.mNotificationIcons.addView(opAodNotificationIconView, generateIconLayoutParams);
            }
        }
    }

    public void setTextSettings(int i, Typeface typeface, int i2) {
        this.mMoreIcon.setTextAppearance(i);
        if (typeface != null) {
            this.mMoreIcon.setTypeface(typeface);
        }
        if (i2 != 0) {
            this.mMoreIcon.setTextSize(0, (float) i2);
        }
    }

    public boolean hasNotifications() {
        OpIconMerger opIconMerger = this.mNotificationIcons;
        return opIconMerger != null && opIconMerger.getChildCount() > 0;
    }
}
