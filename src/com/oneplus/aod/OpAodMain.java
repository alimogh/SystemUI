package com.oneplus.aod;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.C0008R$id;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.phone.StatusBar;
import com.oneplus.aod.OpClockViewCtrl;
import com.oneplus.aod.controller.IOpClockController;
import com.oneplus.aod.views.OpTextDate;
import com.oneplus.plugin.OpLsState;
import java.io.PrintWriter;
public class OpAodMain extends RelativeLayout implements OpClockViewCtrl.OpClockOnChangeListener {
    private static final float AOD_ALPHA_VALUE = ((((float) SystemProperties.getInt("debug.aod_alpha_value", 700)) * 1.0f) / 1000.0f);
    private OpAodBatteryStatusView mBatteryContainer;
    private FrameLayout mClockContainer;
    private IOpClockController mController;
    private OpTextDate mDateTimeView;
    private LinearLayout mNotificationIconContainer;
    private TextView mOwnerInfo;
    private LinearLayout mSliceInfoContainer;
    private LinearLayout mSystemInfoContainer;
    private KeyguardUpdateMonitor mUpdateMonitor;

    public OpAodMain(Context context) {
        this(context, null);
    }

    public OpAodMain(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public OpAodMain(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public OpAodMain(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setAlphaDependOnEnvironmentLight();
        updateLayout();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mBatteryContainer = (OpAodBatteryStatusView) findViewById(C0008R$id.battery_container);
        this.mNotificationIconContainer = (LinearLayout) findViewById(C0008R$id.notification_icon_area_inner);
        this.mSliceInfoContainer = (LinearLayout) findViewById(C0008R$id.slice_info_container);
        this.mDateTimeView = (OpTextDate) findViewById(C0008R$id.date_view);
        this.mOwnerInfo = (TextView) findViewById(C0008R$id.owner_info);
        this.mSystemInfoContainer = (LinearLayout) findViewById(C0008R$id.op_aod_system_info_container);
        this.mClockContainer = (FrameLayout) findViewById(C0008R$id.op_aod_clock_container);
        updateRTL();
        updateLayout();
        updateDisplayTextDB();
        setAlphaDependOnEnvironmentLight();
    }

    public void onUserSwitchComplete(int i) {
        updateDisplayTextDB();
        updateRTL();
    }

    private void updateRTL() {
        updateRTL(getResources().getConfiguration().getLayoutDirection());
    }

    /* access modifiers changed from: protected */
    public void updateRTL(int i) {
        if (i == 1) {
            this.mNotificationIconContainer.setLayoutDirection(1);
            this.mBatteryContainer.setLayoutDirection(1);
            this.mSliceInfoContainer.setLayoutDirection(1);
            this.mSystemInfoContainer.setLayoutDirection(1);
            this.mClockContainer.setLayoutDirection(1);
        } else {
            this.mNotificationIconContainer.setLayoutDirection(0);
            this.mBatteryContainer.setLayoutDirection(0);
            this.mSliceInfoContainer.setLayoutDirection(0);
            this.mSystemInfoContainer.setLayoutDirection(0);
            this.mClockContainer.setLayoutDirection(0);
        }
        updateLayout();
        invalidate();
    }

    /* access modifiers changed from: package-private */
    public void updateLayout() {
        if (this.mController != null) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.mSystemInfoContainer.getLayoutParams();
            this.mController.applySystemInfoViewMargin(layoutParams);
            this.mSystemInfoContainer.setLayoutParams(layoutParams);
            this.mController.applyDateInfoTextSettings(this.mDateTimeView);
            LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams) this.mDateTimeView.getLayoutParams();
            this.mController.applyDateInfoViewMargin(layoutParams2);
            this.mDateTimeView.setLayoutParams(layoutParams2);
            LinearLayout.LayoutParams layoutParams3 = (LinearLayout.LayoutParams) this.mSliceInfoContainer.getLayoutParams();
            this.mController.applySliceInfoViewMargin(layoutParams3);
            this.mSliceInfoContainer.setLayoutParams(layoutParams3);
            this.mController.applyBatteryInfoTextSettings(this.mBatteryContainer);
            LinearLayout.LayoutParams layoutParams4 = (LinearLayout.LayoutParams) this.mBatteryContainer.getLayoutParams();
            this.mController.applyBatteryInfoViewMargin(layoutParams4);
            this.mBatteryContainer.setLayoutParams(layoutParams4);
            StatusBar phoneStatusBar = OpLsState.getInstance().getPhoneStatusBar();
            if (phoneStatusBar != null) {
                this.mController.applyNotificationInfoTextSettings(phoneStatusBar.getAodDisplayViewManager().getAodNotificationIconCtrl());
            }
            LinearLayout.LayoutParams layoutParams5 = (LinearLayout.LayoutParams) this.mNotificationIconContainer.getLayoutParams();
            this.mController.applyNotificationInfoViewMargin(layoutParams5);
            this.mNotificationIconContainer.setLayoutParams(layoutParams5);
            this.mController.applyOwnerInfoTextSettings(this.mOwnerInfo);
            LinearLayout.LayoutParams layoutParams6 = (LinearLayout.LayoutParams) this.mOwnerInfo.getLayoutParams();
            this.mController.applyOwnerInfoViewMargin(layoutParams6);
            this.mOwnerInfo.setLayoutParams(layoutParams6);
        }
    }

    private void updateClockStyle() {
        OpAodNotificationIconAreaController aodNotificationIconCtrl;
        IOpClockController iOpClockController = this.mController;
        if (iOpClockController == null) {
            Log.d("OpAodMain", "controller is null. set all to gone!");
            this.mDateTimeView.setVisibility(8);
            this.mBatteryContainer.setVisibility(8);
            this.mNotificationIconContainer.setVisibility(8);
            this.mOwnerInfo.setVisibility(8);
            this.mSliceInfoContainer.setVisibility(8);
            return;
        }
        this.mDateTimeView.setVisibility(iOpClockController.shouldShowDate() ? 0 : 8);
        this.mBatteryContainer.setVisibility(this.mController.shouldShowBattery() ? 0 : 8);
        StatusBar phoneStatusBar = OpLsState.getInstance().getPhoneStatusBar();
        boolean hasNotifications = (phoneStatusBar == null || (aodNotificationIconCtrl = phoneStatusBar.getAodDisplayViewManager().getAodNotificationIconCtrl()) == null) ? false : aodNotificationIconCtrl.hasNotifications();
        if (!this.mController.shouldShowNotification() || !hasNotifications) {
            this.mNotificationIconContainer.setVisibility(8);
        } else {
            this.mNotificationIconContainer.setVisibility(0);
        }
        if (!this.mController.shouldShowOwnerInfo() || TextUtils.isEmpty(this.mOwnerInfo.getText().toString())) {
            this.mOwnerInfo.setVisibility(8);
        } else {
            this.mOwnerInfo.setVisibility(0);
        }
    }

    @Override // com.oneplus.aod.OpClockViewCtrl.OpClockOnChangeListener
    public void onClockChanged(IOpClockController iOpClockController) {
        this.mController = iOpClockController;
        updateClockStyle();
        updateLayout();
    }

    public void onUserInfoChanged(int i) {
        updateDisplayTextDB();
    }

    public void onTimeChanged() {
        this.mDateTimeView.onTimeChanged();
    }

    public void updateDisplayTextDB() {
        int currentUser = KeyguardUpdateMonitor.getCurrentUser();
        String stringForUser = Settings.Secure.getStringForUser(getContext().getContentResolver(), "aod_display_text", currentUser);
        this.mOwnerInfo.setText(stringForUser);
        IOpClockController iOpClockController = this.mController;
        if (iOpClockController != null && iOpClockController.shouldShowOwnerInfo()) {
            this.mOwnerInfo.setVisibility(TextUtils.isEmpty(stringForUser) ? 8 : 0);
        }
        Log.d("OpAodMain", "updateClock: updateDisplayTextDB = " + stringForUser + ", user = " + currentUser);
    }

    public void setAlphaDependOnEnvironmentLight() {
        setAlpha(this.mUpdateMonitor.isLowLightEnv() ? AOD_ALPHA_VALUE : 1.0f);
    }

    public void dump(PrintWriter printWriter) {
        printWriter.print("aod main alpha= " + getAlpha());
    }
}
