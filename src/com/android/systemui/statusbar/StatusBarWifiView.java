package com.android.systemui.statusbar;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0011R$layout;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.statusbar.phone.StatusBarSignalPolicy;
import com.android.systemui.util.ProductUtils;
import com.oneplus.plugin.OpLsState;
import com.oneplus.util.OpUtils;
public class StatusBarWifiView extends FrameLayout implements DarkIconDispatcher.DarkReceiver, StatusIconDisplayable {
    private View mAirplaneSpacer;
    private float mDarkIntensity = 0.0f;
    private boolean mDirty = true;
    private StatusBarIconView mDotView;
    private View mInoutContainer;
    private Rect mRect;
    private View mSignalSpacer;
    private String mSlot;
    private StatusBarSignalPolicy.WifiIconState mState;
    private int mTint;
    private int mVisibleState = -1;
    private ImageView mWifiActivity;
    private int mWifiActivityId = 0;
    private LinearLayout mWifiGroup;
    private ImageView mWifiIcon;

    public static StatusBarWifiView fromContext(Context context, String str) {
        StatusBarWifiView statusBarWifiView;
        LayoutInflater from = LayoutInflater.from(context);
        if (ProductUtils.isUsvMode()) {
            statusBarWifiView = (StatusBarWifiView) from.inflate(C0011R$layout.op_vzw_status_bar_wifi_group, (ViewGroup) null);
        } else {
            statusBarWifiView = (StatusBarWifiView) from.inflate(C0011R$layout.status_bar_wifi_group, (ViewGroup) null);
        }
        statusBarWifiView.setSlot(str);
        statusBarWifiView.init();
        statusBarWifiView.setVisibleState(0);
        return statusBarWifiView;
    }

    public StatusBarWifiView(Context context) {
        super(context);
    }

    public StatusBarWifiView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public StatusBarWifiView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public StatusBarWifiView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
    }

    public void setSlot(String str) {
        this.mSlot = str;
    }

    @Override // com.android.systemui.statusbar.StatusIconDisplayable
    public void setStaticDrawableColor(int i) {
        ColorStateList valueOf = ColorStateList.valueOf(i);
        this.mWifiIcon.setImageTintList(valueOf);
        this.mWifiActivity.setImageTintList(valueOf);
        this.mDotView.setDecorColor(i);
    }

    @Override // com.android.systemui.statusbar.StatusIconDisplayable
    public void setDecorColor(int i) {
        this.mDotView.setDecorColor(i);
    }

    @Override // com.android.systemui.statusbar.StatusIconDisplayable
    public String getSlot() {
        return this.mSlot;
    }

    @Override // com.android.systemui.statusbar.StatusIconDisplayable
    public boolean isIconVisible() {
        StatusBarSignalPolicy.WifiIconState wifiIconState = this.mState;
        return wifiIconState != null && wifiIconState.visible;
    }

    @Override // com.android.systemui.statusbar.StatusIconDisplayable
    public void setVisibleState(int i, boolean z) {
        if (i != this.mVisibleState) {
            this.mVisibleState = i;
            if (i == 0) {
                this.mWifiGroup.setVisibility(0);
                this.mDotView.setVisibility(8);
            } else if (i != 1) {
                this.mWifiGroup.setVisibility(8);
                this.mDotView.setVisibility(8);
            } else {
                this.mWifiGroup.setVisibility(8);
                this.mDotView.setVisibility(0);
            }
        }
    }

    @Override // com.android.systemui.statusbar.StatusIconDisplayable
    public int getVisibleState() {
        return this.mVisibleState;
    }

    @Override // android.view.View
    public void getDrawingRect(Rect rect) {
        super.getDrawingRect(rect);
        float translationX = getTranslationX();
        float translationY = getTranslationY();
        rect.left = (int) (((float) rect.left) + translationX);
        rect.right = (int) (((float) rect.right) + translationX);
        rect.top = (int) (((float) rect.top) + translationY);
        rect.bottom = (int) (((float) rect.bottom) + translationY);
    }

    private void init() {
        this.mWifiGroup = (LinearLayout) findViewById(C0008R$id.wifi_group);
        this.mWifiIcon = (ImageView) findViewById(C0008R$id.wifi_signal);
        this.mWifiActivity = (ImageView) findViewById(C0008R$id.wifi_inout);
        this.mSignalSpacer = findViewById(C0008R$id.wifi_signal_spacer);
        this.mAirplaneSpacer = findViewById(C0008R$id.wifi_airplane_spacer);
        this.mInoutContainer = findViewById(C0008R$id.inout_container);
        initDotView();
    }

    private void initDotView() {
        StatusBarIconView statusBarIconView = new StatusBarIconView(((FrameLayout) this).mContext, this.mSlot, null);
        this.mDotView = statusBarIconView;
        statusBarIconView.setVisibleState(1);
        int dimensionPixelSize = ((FrameLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.status_bar_icon_size);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(dimensionPixelSize, dimensionPixelSize);
        layoutParams.gravity = 8388627;
        addView(this.mDotView, layoutParams);
    }

    public void applyWifiState(StatusBarSignalPolicy.WifiIconState wifiIconState) {
        boolean z = true;
        if (wifiIconState == null) {
            if (getVisibility() == 8) {
                z = false;
            }
            setVisibility(8);
            this.mState = null;
        } else {
            StatusBarSignalPolicy.WifiIconState wifiIconState2 = this.mState;
            if (wifiIconState2 == null) {
                this.mState = wifiIconState.copy();
                initViewState();
            } else {
                z = !wifiIconState2.equals(wifiIconState) ? updateState(wifiIconState.copy()) : false;
            }
        }
        if (z) {
            requestLayout();
        }
    }

    private boolean updateState(StatusBarSignalPolicy.WifiIconState wifiIconState) {
        setContentDescription(wifiIconState.contentDescription);
        if (OpUtils.DEBUG_ONEPLUS) {
            Log.i("StatusBarWifiView", "wify icon res name:" + OpUtils.getResourceName(((FrameLayout) this).mContext, wifiIconState.resId) + ", mState.resId:" + this.mState.resId + ", state.resId:" + wifiIconState.resId);
        }
        int i = this.mState.resId;
        int i2 = wifiIconState.resId;
        if (i != i2 && i2 >= 0) {
            this.mWifiIcon.setImageDrawable(((FrameLayout) this).mContext.getDrawable(i2));
        }
        int wifiActivityId = getWifiActivityId(wifiIconState.activityIn, wifiIconState.activityOut);
        this.mWifiActivityId = wifiActivityId;
        if (wifiActivityId != 0) {
            this.mWifiActivity.setImageResource(wifiActivityId);
        }
        int i3 = 8;
        this.mInoutContainer.setVisibility((wifiIconState.activityIn || wifiIconState.activityOut) ? 0 : 8);
        this.mAirplaneSpacer.setVisibility(wifiIconState.airplaneSpacerVisible ? 0 : 8);
        this.mSignalSpacer.setVisibility(wifiIconState.signalSpacerVisible ? 0 : 8);
        boolean z = wifiIconState.activityIn;
        StatusBarSignalPolicy.WifiIconState wifiIconState2 = this.mState;
        boolean z2 = (z == wifiIconState2.activityIn && wifiIconState.activityOut == wifiIconState2.activityOut) ? false : true;
        boolean z3 = this.mState.visible;
        boolean z4 = wifiIconState.visible;
        if (z3 != z4) {
            z2 |= true;
            if (z4) {
                i3 = 0;
            }
            setVisibility(i3);
        }
        this.mState = wifiIconState;
        return z2;
    }

    private void initViewState() {
        setContentDescription(this.mState.contentDescription);
        int i = this.mState.resId;
        if (i >= 0) {
            this.mWifiIcon.setImageDrawable(((FrameLayout) this).mContext.getDrawable(i));
        }
        StatusBarSignalPolicy.WifiIconState wifiIconState = this.mState;
        int wifiActivityId = getWifiActivityId(wifiIconState.activityIn, wifiIconState.activityOut);
        this.mWifiActivityId = wifiActivityId;
        if (wifiActivityId != 0) {
            this.mWifiActivity.setImageResource(wifiActivityId);
        }
        View view = this.mInoutContainer;
        StatusBarSignalPolicy.WifiIconState wifiIconState2 = this.mState;
        int i2 = 8;
        view.setVisibility((wifiIconState2.activityIn || wifiIconState2.activityOut) ? 0 : 8);
        this.mAirplaneSpacer.setVisibility(this.mState.airplaneSpacerVisible ? 0 : 8);
        this.mSignalSpacer.setVisibility(this.mState.signalSpacerVisible ? 0 : 8);
        if (this.mState.visible) {
            i2 = 0;
        }
        setVisibility(i2);
    }

    @Override // com.android.systemui.plugins.DarkIconDispatcher.DarkReceiver
    public void onDarkChanged(Rect rect, float f, int i) {
        this.mRect = rect;
        this.mDarkIntensity = f;
        this.mTint = i;
        applyColors();
    }

    private void applyColors() {
        Rect rect = this.mRect;
        if (rect != null) {
            float f = this.mDarkIntensity;
            int i = this.mTint;
            this.mDarkIntensity = f;
            this.mWifiIcon.setImageTintList(ColorStateList.valueOf(DarkIconDispatcher.getTint(rect, this, i)));
            this.mWifiActivity.setImageTintList(ColorStateList.valueOf(DarkIconDispatcher.getTint(rect, this, i)));
            this.mDotView.setDecorColor(i);
            this.mDotView.setIconColor(i, false);
        }
    }

    @Override // android.view.View, java.lang.Object
    public String toString() {
        return "StatusBarWifiView(slot=" + this.mSlot + " state=" + this.mState + ")";
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.FrameLayout, android.view.View, android.view.ViewGroup
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        if (OpLsState.getInstance().getPhoneStatusBar().isInMultiWindow() && this.mDirty && getWidth() > 0) {
            applyColors();
            this.mDirty = false;
        }
    }

    @Override // android.view.View
    public void setVisibility(int i) {
        super.setVisibility(i);
        if (OpLsState.getInstance().getPhoneStatusBar().isInMultiWindow() && i == 0) {
            if (getWidth() > 0) {
                applyColors();
            }
            this.mDirty = true;
        }
    }

    private int getWifiActivityId(boolean z, boolean z2) {
        int i = z ? 1 : 0;
        if (z2) {
            i += 2;
        }
        if (i == 1) {
            return C0006R$drawable.stat_sys_signal_in;
        }
        if (i == 2) {
            return C0006R$drawable.stat_sys_signal_out;
        }
        if (i != 3) {
            return C0006R$drawable.stat_sys_signal_none;
        }
        return C0006R$drawable.stat_sys_signal_inout;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onConfigurationChanged(Configuration configuration) {
        this.mDirty = true;
    }
}
