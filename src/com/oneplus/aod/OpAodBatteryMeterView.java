package com.oneplus.aod;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.android.systemui.C0004R$color;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.Dependency;
import com.android.systemui.R$styleable;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.BatteryController;
import com.oneplus.battery.OpBatteryMeterDrawable;
import com.oneplus.plugin.OpLsState;
import com.oneplus.util.OpUtils;
public class OpAodBatteryMeterView extends ImageView implements BatteryController.BatteryStateChangeCallback {
    private BatteryController mBatteryController;
    private int mBatteryStyle;
    private final OpBatteryMeterDrawable mDrawable;
    private int mFrameColor;
    private Handler mHandler;

    @Override // android.widget.ImageView, android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    private void initHandler() {
        StatusBar phoneStatusBar;
        OpAodWindowManager aodWindowManager;
        if (this.mHandler == null && (phoneStatusBar = OpLsState.getInstance().getPhoneStatusBar()) != null && (aodWindowManager = phoneStatusBar.getAodWindowManager()) != null && aodWindowManager.getUIHandler() != null) {
            this.mHandler = new Handler(aodWindowManager.getUIHandler().getLooper()) { // from class: com.oneplus.aod.OpAodBatteryMeterView.1
                @Override // android.os.Handler
                public void handleMessage(Message message) {
                    if (message.what == 1) {
                        OpAodBatteryMeterView.this.handleUpdateViewState();
                    }
                }
            };
        }
    }

    public OpAodBatteryMeterView(Context context) {
        this(context, null, 0);
    }

    public OpAodBatteryMeterView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public OpAodBatteryMeterView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mBatteryStyle = 0;
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.BatteryMeterView, i, 0);
        int color = obtainStyledAttributes.getColor(R$styleable.BatteryMeterView_frameColor, context.getColor(C0004R$color.batterymeter_frame_color));
        this.mDrawable = new OpBatteryMeterDrawable(context, color);
        obtainStyledAttributes.recycle();
        this.mFrameColor = color;
        setImageDrawable(this.mDrawable);
        initHandler();
    }

    @Override // android.widget.ImageView, android.view.View
    public void onAttachedToWindow() {
        int i;
        super.onAttachedToWindow();
        BatteryController batteryController = (BatteryController) Dependency.get(BatteryController.class);
        this.mBatteryController = batteryController;
        batteryController.addCallback(this);
        updateViewState();
        int i2 = 0;
        boolean z = this.mBatteryStyle == 2;
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) getLayoutParams();
        if (z) {
            i = 0;
        } else {
            i = OpUtils.convertDpToFixedPx(((ImageView) this).mContext.getResources().getDimension(C0005R$dimen.aod_battery_icon_width));
        }
        marginLayoutParams.width = i;
        marginLayoutParams.height = OpUtils.convertDpToFixedPx(((ImageView) this).mContext.getResources().getDimension(C0005R$dimen.aod_battery_icon_height));
        if (!z) {
            i2 = OpUtils.convertDpToFixedPx(((ImageView) this).mContext.getResources().getDimension(C0005R$dimen.aod_battery_icon_margin_start));
        }
        marginLayoutParams.setMarginStart(i2);
        marginLayoutParams.bottomMargin = OpUtils.convertDpToFixedPx(((ImageView) this).mContext.getResources().getDimension(C0005R$dimen.aod_battery_icon_margin_bottom));
        setLayoutParams(marginLayoutParams);
    }

    @Override // android.widget.ImageView, android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mBatteryController.removeCallback(this);
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
    public void onBatteryLevelChanged(int i, boolean z, boolean z2) {
        this.mDrawable.setBatteryLevel(i);
        this.mDrawable.setCharging(z);
        updateViewState();
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
        if (this.mBatteryStyle != 2) {
            this.mDrawable.setColors(-1, this.mFrameColor, -1);
            requestLayout();
        }
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
    public void onPowerSaveChanged(boolean z) {
        this.mDrawable.setPowerSaveEnabled(z);
        updateViewState();
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
    public void onBatteryStyleChanged(int i) {
        if (this.mBatteryStyle != i) {
            this.mBatteryStyle = i;
            this.mDrawable.onBatteryStyleChanged(i);
            updateViewState();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.ImageView, android.view.View
    public void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        int size = View.MeasureSpec.getSize(i);
        int size2 = View.MeasureSpec.getSize(i2);
        if (this.mBatteryStyle != 1 || size == size2) {
            onSizeChanged(size, size2, 0, 0);
        } else {
            size = size2;
        }
        setMeasuredDimension(size, size2);
    }
}
