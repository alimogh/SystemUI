package com.oneplus.aod;

import android.content.Context;
import android.graphics.Typeface;
import android.icu.text.NumberFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.OpFeatures;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0008R$id;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.BatteryController;
import com.oneplus.plugin.OpLsState;
import com.oneplus.util.OpUtils;
public class OpAodBatteryStatusView extends LinearLayout implements BatteryController.BatteryStateChangeCallback {
    private static final int[] CHARGE_ICONS = {C0006R$drawable.aod_ic_battery_charged, C0006R$drawable.aod_ic_battery_charging, C0006R$drawable.aod_ic_battery_fast_charging};
    private View mBattery;
    private BatteryController mBatteryController;
    private OpAodBatteryDashChargeView mBatteryDashChargeView;
    private int mChargeState;
    private ImageView mChargeView;
    private boolean mFastCharge;
    private Handler mHandler;
    private int mLevel;
    private TextView mPercentage;
    private boolean mWirelessWarpCharging;

    private void initHandler() {
        StatusBar phoneStatusBar;
        OpAodWindowManager aodWindowManager;
        if (this.mHandler == null && (phoneStatusBar = OpLsState.getInstance().getPhoneStatusBar()) != null && (aodWindowManager = phoneStatusBar.getAodWindowManager()) != null && aodWindowManager.getUIHandler() != null) {
            this.mHandler = new Handler(aodWindowManager.getUIHandler().getLooper()) { // from class: com.oneplus.aod.OpAodBatteryStatusView.1
                @Override // android.os.Handler
                public void handleMessage(Message message) {
                    if (message.what == 1) {
                        OpAodBatteryStatusView.this.handleUpdateViewState();
                    }
                }
            };
        }
    }

    public OpAodBatteryStatusView(Context context) {
        this(context, null, 0);
    }

    public OpAodBatteryStatusView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public OpAodBatteryStatusView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mWirelessWarpCharging = false;
        this.mChargeState = -1;
        this.mFastCharge = false;
        initHandler();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mPercentage = (TextView) findViewById(C0008R$id.percentage);
        this.mBattery = findViewById(C0008R$id.battery);
        this.mBatteryDashChargeView = (OpAodBatteryDashChargeView) findViewById(C0008R$id.battery_dash_charge);
        this.mChargeView = (ImageView) findViewById(C0008R$id.battery_charge);
    }

    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        BatteryController batteryController = (BatteryController) Dependency.get(BatteryController.class);
        this.mBatteryController = batteryController;
        batteryController.addCallback(this);
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) this.mChargeView.getLayoutParams();
        marginLayoutParams.width = OpUtils.convertDpToFixedPx(((LinearLayout) this).mContext.getResources().getDimension(C0005R$dimen.aod_battery_charging_icon_width));
        marginLayoutParams.height = OpUtils.convertDpToFixedPx(((LinearLayout) this).mContext.getResources().getDimension(C0005R$dimen.aod_battery_charging_icon_height));
        marginLayoutParams.setMarginStart(OpUtils.convertDpToFixedPx(((LinearLayout) this).mContext.getResources().getDimension(C0005R$dimen.aod_battery_icon_margin_start)));
    }

    @Override // android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mBatteryController.removeCallback(this);
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
    public void onBatteryLevelChanged(int i, boolean z, boolean z2) {
        int i2 = z ? i >= 100 ? 0 : isFastCharge() ? 2 : 1 : -1;
        if (!(this.mLevel == i && this.mChargeState == i2)) {
            this.mLevel = i;
            this.mChargeState = i2;
            updateViewState();
        }
        this.mChargeState = i2;
        this.mLevel = i;
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
    public void onFastChargeChanged(int i) {
        boolean z = i > 0;
        if (this.mFastCharge != z) {
            if (Build.DEBUG_ONEPLUS) {
                Log.i("OpAodBatteryStatusView", " onFastChargeChanged:" + i);
            }
            this.mFastCharge = z;
            updateViewState();
        }
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
    public void onWirelessWarpChargeChanged(boolean z) {
        if (this.mWirelessWarpCharging != z) {
            this.mWirelessWarpCharging = z;
            updateViewState();
        }
    }

    private boolean isFastCharge() {
        return this.mFastCharge || this.mWirelessWarpCharging;
    }

    private void updateViewState() {
        initHandler();
        Handler handler = this.mHandler;
        if (handler != null) {
            handler.removeMessages(1);
            Handler handler2 = this.mHandler;
            handler2.sendMessage(handler2.obtainMessage(1));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleUpdateViewState() {
        TextView textView = this.mPercentage;
        if (textView != null) {
            textView.setText(NumberFormat.getPercentInstance().format((double) (((float) this.mLevel) / 100.0f)));
        }
        if (Build.DEBUG_ONEPLUS) {
            Log.i("OpAodBatteryStatusView", " handleUpdateViewState:" + isFastCharge());
        }
        if (OpFeatures.isSupport(new int[]{74})) {
            if (isFastCharge()) {
                this.mBattery.setVisibility(8);
                this.mBatteryDashChargeView.setLevel(this.mLevel);
                this.mBatteryDashChargeView.setVisibility(0);
            } else {
                this.mBattery.setVisibility(0);
                this.mBatteryDashChargeView.setVisibility(8);
            }
            this.mChargeView.setVisibility(8);
        } else if (this.mChargeState == -1) {
            this.mBattery.setVisibility(0);
            this.mChargeView.setVisibility(8);
            this.mBatteryDashChargeView.setVisibility(8);
        } else {
            this.mChargeView.setVisibility(0);
            this.mChargeView.setImageResource(CHARGE_ICONS[this.mChargeState]);
            this.mBattery.setVisibility(8);
            this.mBatteryDashChargeView.setVisibility(8);
        }
    }

    public void setTextSettings(int i, Typeface typeface, int i2) {
        this.mPercentage.setTextAppearance(i);
        if (typeface != null) {
            this.mPercentage.setTypeface(typeface);
        }
        if (i2 != 0) {
            this.mPercentage.setTextSize(0, (float) i2);
        }
    }
}
