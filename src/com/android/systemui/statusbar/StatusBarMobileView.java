package com.android.systemui.statusbar;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Build;
import android.os.Debug;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settingslib.graph.SignalDrawable;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0011R$layout;
import com.android.systemui.DualToneHandler;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.statusbar.phone.StatusBarSignalPolicy;
import com.android.systemui.util.ProductUtils;
import com.oneplus.plugin.OpLsState;
import com.oneplus.signal.OpSignalIcons;
import com.oneplus.util.OpUtils;
public class StatusBarMobileView extends FrameLayout implements DarkIconDispatcher.DarkReceiver, StatusIconDisplayable {
    static final boolean DEBUG = Build.DEBUG_ONEPLUS;
    private static final int NOSIM_EDGE_RESID;
    private static final int NOSIM_RESID;
    private float mDarkIntensity;
    private boolean mDirty = true;
    private StatusBarIconView mDotView;
    private DualToneHandler mDualToneHandler;
    private boolean mIsRtlPropertiesChanged = false;
    private ImageView mMobile;
    private SignalDrawable mMobileDrawable;
    private ImageView mMobileFiveGUWB;
    private ViewGroup mMobileFiveGUWBGroup;
    private LinearLayout mMobileGroup;
    private ImageView mMobileInOut;
    private ImageView mMobileInOutFiveGUWB;
    private ViewGroup mMobileSingleGroup;
    private ViewGroup mMobileStackedGroup;
    private ImageView mMobileType;
    private ViewGroup mMobileVzwGroup;
    private Rect mRect;
    private String mSlot;
    private ViewGroup mStackedDataGroup;
    private ImageView mStackedDataStrengthView;
    private ImageView mStackedDataTypeView;
    private ViewGroup mStackedVoiceGroup;
    private ImageView mStackedVoiceStrengthView;
    private ImageView mStackedVoiceTypeView;
    private StatusBarSignalPolicy.MobileIconState mState;
    private int mTint;
    private int mVisibleState = -1;
    private ImageView mVolte;

    static {
        int i = C0006R$drawable.stat_sys_no_sims_edge;
        NOSIM_RESID = i;
        NOSIM_EDGE_RESID = i;
    }

    public static StatusBarMobileView fromContext(Context context, String str) {
        StatusBarMobileView statusBarMobileView;
        LayoutInflater from = LayoutInflater.from(context);
        if (ProductUtils.isUsvMode()) {
            statusBarMobileView = (StatusBarMobileView) from.inflate(C0011R$layout.op_vzw_status_bar_mobile_signal_group, (ViewGroup) null);
        } else {
            statusBarMobileView = (StatusBarMobileView) from.inflate(C0011R$layout.status_bar_mobile_signal_group, (ViewGroup) null);
        }
        statusBarMobileView.setSlot(str);
        statusBarMobileView.init();
        statusBarMobileView.setVisibleState(0);
        return statusBarMobileView;
    }

    public StatusBarMobileView(Context context) {
        super(context);
    }

    public StatusBarMobileView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public StatusBarMobileView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public StatusBarMobileView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
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
        this.mDualToneHandler = new DualToneHandler(getContext());
        this.mMobileGroup = (LinearLayout) findViewById(C0008R$id.mobile_group);
        this.mMobile = (ImageView) findViewById(C0008R$id.mobile_signal);
        this.mMobileType = (ImageView) findViewById(C0008R$id.mobile_type);
        ImageView imageView = (ImageView) findViewById(C0008R$id.mobile_roaming);
        findViewById(C0008R$id.mobile_roaming_space);
        ImageView imageView2 = (ImageView) findViewById(C0008R$id.mobile_in);
        ImageView imageView3 = (ImageView) findViewById(C0008R$id.mobile_out);
        findViewById(C0008R$id.inout_container);
        this.mVolte = (ImageView) findViewById(C0008R$id.mobile_volte);
        SignalDrawable signalDrawable = new SignalDrawable(getContext());
        this.mMobileDrawable = signalDrawable;
        this.mMobile.setImageDrawable(signalDrawable);
        initDotView();
        this.mStackedDataGroup = (ViewGroup) findViewById(C0008R$id.mobile_signal_data);
        this.mStackedVoiceGroup = (ViewGroup) findViewById(C0008R$id.mobile_signal_voice);
        this.mStackedDataStrengthView = (ImageView) this.mStackedDataGroup.findViewById(C0008R$id.mobile_signal);
        this.mStackedDataTypeView = (ImageView) this.mStackedDataGroup.findViewById(C0008R$id.mobile_type);
        this.mStackedVoiceStrengthView = (ImageView) this.mStackedVoiceGroup.findViewById(C0008R$id.mobile_signal);
        this.mStackedVoiceTypeView = (ImageView) this.mStackedVoiceGroup.findViewById(C0008R$id.mobile_type);
        this.mMobileSingleGroup = (ViewGroup) findViewById(C0008R$id.mobile_signal_single);
        this.mMobileStackedGroup = (ViewGroup) findViewById(C0008R$id.mobile_signal_stacked);
        this.mMobileInOut = (ImageView) findViewById(C0008R$id.mobile_inout);
        ((FrameLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.statusbar_mobile_type_overlap);
        ((FrameLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.statusbar_mobile_type_overlap_plus);
        if (ProductUtils.isUsvMode()) {
            ViewGroup viewGroup = (ViewGroup) findViewById(C0008R$id.mobile_signal_single_5g_uwb);
            this.mMobileFiveGUWBGroup = viewGroup;
            this.mMobileInOutFiveGUWB = (ImageView) viewGroup.findViewById(C0008R$id.mobile_inout);
            this.mMobileFiveGUWB = (ImageView) this.mMobileFiveGUWBGroup.findViewById(C0008R$id.mobile_type);
            this.mMobileVzwGroup = (ViewGroup) findViewById(C0008R$id.mobile_signal_vzw);
        }
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

    public void applyMobileState(StatusBarSignalPolicy.MobileIconState mobileIconState) {
        String str;
        if (DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("applyMobileState / state:");
            String str2 = "null";
            if (mobileIconState == null) {
                str = str2;
            } else {
                str = mobileIconState.toString();
            }
            sb.append(str);
            sb.append(" / mState:");
            StatusBarSignalPolicy.MobileIconState mobileIconState2 = this.mState;
            if (mobileIconState2 != null) {
                str2 = mobileIconState2.toString();
            }
            sb.append(str2);
            Log.d("StatusBarMobileView", sb.toString());
        }
        boolean z = true;
        if (mobileIconState == null) {
            if (getVisibility() == 8) {
                z = false;
            }
            setVisibility(8);
            this.mState = null;
        } else {
            StatusBarSignalPolicy.MobileIconState mobileIconState3 = this.mState;
            if (mobileIconState3 == null) {
                this.mState = mobileIconState.copy();
                initViewState();
            } else {
                z = !mobileIconState3.equals(mobileIconState) ? updateState(mobileIconState.copy()) : false;
            }
        }
        if (z) {
            requestLayout();
        }
        if (needFixVisibleState()) {
            Log.d("StatusBarMobileView", "fix VisibleState width=" + getWidth() + " height=" + getHeight());
            this.mVisibleState = 0;
            setVisibility(0);
            requestLayout();
        } else if (needFixInVisibleState()) {
            Log.d("StatusBarMobileView", "fix InVisibleState width=" + getWidth() + " height=" + getHeight());
            this.mVisibleState = -1;
            setVisibility(4);
            requestLayout();
        }
    }

    private void initViewState() {
        setContentDescription(this.mState.contentDescription);
        int i = 8;
        if (!this.mState.visible) {
            this.mMobileGroup.setVisibility(8);
        } else {
            this.mMobileGroup.setVisibility(0);
        }
        if (this.mState.strengthId >= 0) {
            this.mMobile.setVisibility(0);
            this.mMobileDrawable.setLevel(this.mState.strengthId);
        } else {
            this.mMobile.setVisibility(8);
        }
        StatusBarSignalPolicy.MobileIconState mobileIconState = this.mState;
        if (mobileIconState.typeId > 0) {
            this.mMobileType.setContentDescription(mobileIconState.typeContentDescription);
            this.mMobileType.setImageResource(this.mState.typeId);
            this.mMobileType.setVisibility(0);
        } else {
            this.mMobileType.setVisibility(8);
        }
        int i2 = this.mState.volteId;
        if (i2 > 0) {
            this.mVolte.setImageResource(i2);
            this.mVolte.setVisibility(0);
        } else {
            this.mVolte.setVisibility(8);
        }
        if (showStatck(this.mState)) {
            this.mStackedDataStrengthView.setImageResource(this.mState.stackedDataStrengthId);
            this.mStackedDataTypeView.setImageResource(this.mState.stackedDataTypeId);
            this.mStackedVoiceStrengthView.setImageResource(this.mState.stackedVoiceStrengthId);
            this.mStackedVoiceTypeView.setImageResource(this.mState.stackedVoiceTypeId);
            this.mMobileSingleGroup.setVisibility(8);
            this.mMobileStackedGroup.setVisibility(0);
        } else {
            StatusBarSignalPolicy.MobileIconState mobileIconState2 = this.mState;
            if (mobileIconState2.showNoSim) {
                this.mMobile.setImageResource(mobileIconState2.phoneId == 0 ? NOSIM_RESID : NOSIM_EDGE_RESID);
            } else {
                int i3 = mobileIconState2.strengthId;
                if (i3 != 0) {
                    this.mMobile.setImageResource(i3);
                }
            }
            this.mMobileSingleGroup.setVisibility(0);
            this.mMobileStackedGroup.setVisibility(8);
        }
        ImageView imageView = this.mMobileInOut;
        StatusBarSignalPolicy.MobileIconState mobileIconState3 = this.mState;
        imageView.setImageResource(getInOutIndicator(mobileIconState3.activityIn, mobileIconState3.activityOut));
        if (ProductUtils.isUsvMode()) {
            int i4 = this.mState.typeId;
            if (i4 == C0006R$drawable.stat_sys_data_fully_connected_5g || i4 == C0006R$drawable.stat_sys_data_fully_connected_5g_uwb || i4 == C0006R$drawable.op_stat_sys_data_idle_5g_uwb) {
                this.mMobileVzwGroup.setVisibility(8);
                this.mMobileFiveGUWBGroup.setVisibility(0);
                this.mMobileFiveGUWB.setImageResource(this.mState.typeId);
                ImageView imageView2 = this.mMobileInOutFiveGUWB;
                StatusBarSignalPolicy.MobileIconState mobileIconState4 = this.mState;
                imageView2.setImageResource(getFiveGUWBInOutIndicator(mobileIconState4.activityIn, mobileIconState4.activityOut));
                ImageView imageView3 = this.mMobileInOutFiveGUWB;
                if (this.mState.dataConnected) {
                    i = 0;
                }
                imageView3.setVisibility(i);
            } else {
                this.mMobileFiveGUWBGroup.setVisibility(8);
                this.mMobileVzwGroup.setVisibility(0);
                ImageView imageView4 = this.mMobileInOut;
                if (this.mState.dataConnected) {
                    i = 0;
                }
                imageView4.setVisibility(i);
            }
        } else {
            ImageView imageView5 = this.mMobileInOut;
            if (this.mState.dataConnected) {
                i = 0;
            }
            imageView5.setVisibility(i);
        }
        if (!ProductUtils.isUsvMode()) {
            updateMobileIconPadding();
        }
        updateInOutIndicatorPadding();
    }

    private boolean updateState(StatusBarSignalPolicy.MobileIconState mobileIconState) {
        boolean z;
        setContentDescription(mobileIconState.contentDescription);
        boolean z2 = this.mState.visible;
        boolean z3 = mobileIconState.visible;
        boolean z4 = true;
        int i = 8;
        if (z2 != z3) {
            this.mMobileGroup.setVisibility(z3 ? 0 : 8);
            z = true;
        } else {
            z = false;
        }
        int i2 = mobileIconState.strengthId;
        if (i2 >= 0) {
            this.mMobileDrawable.setLevel(i2);
            this.mMobile.setVisibility(0);
        } else {
            this.mMobile.setVisibility(8);
        }
        if (this.mState.typeId != mobileIconState.typeId || this.mIsRtlPropertiesChanged) {
            this.mIsRtlPropertiesChanged = false;
            z |= mobileIconState.typeId == 0 || this.mState.typeId == 0;
            if (mobileIconState.typeId != 0) {
                this.mMobileType.setContentDescription(mobileIconState.typeContentDescription);
                this.mMobileType.setImageResource(mobileIconState.typeId);
                this.mMobileType.setVisibility(0);
            } else {
                this.mMobileType.setVisibility(8);
            }
        }
        int i3 = this.mState.volteId;
        int i4 = mobileIconState.volteId;
        if (i3 != i4) {
            if (i4 != 0) {
                this.mVolte.setImageResource(i4);
                this.mVolte.setVisibility(0);
            } else {
                this.mVolte.setVisibility(8);
            }
        }
        if (showStatck(mobileIconState)) {
            this.mStackedDataStrengthView.setImageResource(mobileIconState.stackedDataStrengthId);
            this.mStackedDataTypeView.setImageResource(mobileIconState.stackedDataTypeId);
            this.mStackedVoiceStrengthView.setImageResource(mobileIconState.stackedVoiceStrengthId);
            this.mStackedVoiceTypeView.setImageResource(mobileIconState.stackedVoiceTypeId);
            this.mMobileSingleGroup.setVisibility(8);
            this.mMobileStackedGroup.setVisibility(0);
        } else {
            if (mobileIconState.showNoSim) {
                this.mMobile.setImageResource(this.mState.phoneId == 0 ? NOSIM_RESID : NOSIM_EDGE_RESID);
            } else {
                int i5 = mobileIconState.strengthId;
                if (i5 != 0) {
                    this.mMobile.setImageResource(i5);
                }
            }
            this.mMobileSingleGroup.setVisibility(0);
            this.mMobileStackedGroup.setVisibility(8);
        }
        this.mMobileInOut.setImageResource(getInOutIndicator(mobileIconState.activityIn, mobileIconState.activityOut));
        if (ProductUtils.isUsvMode()) {
            int i6 = mobileIconState.typeId;
            if (i6 == C0006R$drawable.stat_sys_data_fully_connected_5g || i6 == C0006R$drawable.stat_sys_data_fully_connected_5g_uwb || i6 == C0006R$drawable.op_stat_sys_data_idle_5g_uwb) {
                this.mMobileVzwGroup.setVisibility(8);
                this.mMobileFiveGUWBGroup.setVisibility(0);
                this.mMobileFiveGUWB.setImageResource(mobileIconState.typeId);
                this.mMobileInOutFiveGUWB.setImageResource(getFiveGUWBInOutIndicator(mobileIconState.activityIn, mobileIconState.activityOut));
                ImageView imageView = this.mMobileInOutFiveGUWB;
                if (mobileIconState.dataConnected) {
                    i = 0;
                }
                imageView.setVisibility(i);
            } else {
                this.mMobileFiveGUWBGroup.setVisibility(8);
                this.mMobileVzwGroup.setVisibility(0);
                ImageView imageView2 = this.mMobileInOut;
                if (mobileIconState.dataConnected) {
                    i = 0;
                }
                imageView2.setVisibility(i);
            }
        } else {
            ImageView imageView3 = this.mMobileInOut;
            if (mobileIconState.dataConnected) {
                i = 0;
            }
            imageView3.setVisibility(i);
        }
        boolean z5 = mobileIconState.roaming;
        StatusBarSignalPolicy.MobileIconState mobileIconState2 = this.mState;
        if (z5 == mobileIconState2.roaming && mobileIconState.activityIn == mobileIconState2.activityIn && mobileIconState.activityOut == mobileIconState2.activityOut) {
            z4 = false;
        }
        boolean z6 = z | z4;
        this.mState = mobileIconState;
        if (!ProductUtils.isUsvMode()) {
            updateMobileIconPadding();
        }
        updateInOutIndicatorPadding();
        return z6;
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
            this.mMobileDrawable.setDarkIntensity(f);
            ColorStateList valueOf = ColorStateList.valueOf(DarkIconDispatcher.getTint(rect, this, i));
            this.mMobileType.setImageTintList(valueOf);
            this.mMobileInOut.setImageTintList(valueOf);
            this.mStackedDataStrengthView.setImageTintList(valueOf);
            this.mStackedDataTypeView.setImageTintList(valueOf);
            this.mStackedVoiceStrengthView.setImageTintList(valueOf);
            this.mStackedVoiceTypeView.setImageTintList(valueOf);
            this.mMobile.setImageTintList(valueOf);
            this.mDotView.setDecorColor(i);
            this.mDotView.setIconColor(i, false);
            if (ProductUtils.isUsvMode()) {
                this.mMobileFiveGUWB.setImageTintList(valueOf);
                this.mMobileInOutFiveGUWB.setImageTintList(valueOf);
            }
        }
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

    @Override // com.android.systemui.statusbar.StatusIconDisplayable
    public String getSlot() {
        return this.mSlot;
    }

    public void setSlot(String str) {
        this.mSlot = str;
    }

    @Override // com.android.systemui.statusbar.StatusIconDisplayable
    public void setStaticDrawableColor(int i) {
        ColorStateList valueOf = ColorStateList.valueOf(i);
        this.mMobileDrawable.setTintList(ColorStateList.valueOf(this.mDualToneHandler.getSingleColor(i == -1 ? 0.0f : 1.0f)));
        this.mMobileType.setImageTintList(valueOf);
        this.mMobileInOut.setImageTintList(valueOf);
        this.mStackedDataStrengthView.setImageTintList(valueOf);
        this.mStackedDataTypeView.setImageTintList(valueOf);
        this.mStackedVoiceStrengthView.setImageTintList(valueOf);
        this.mStackedVoiceTypeView.setImageTintList(valueOf);
        this.mMobile.setImageTintList(valueOf);
        if (ProductUtils.isUsvMode()) {
            this.mMobileFiveGUWB.setImageTintList(valueOf);
            this.mMobileInOutFiveGUWB.setImageTintList(valueOf);
        }
    }

    @Override // com.android.systemui.statusbar.StatusIconDisplayable
    public void setDecorColor(int i) {
        this.mDotView.setDecorColor(i);
    }

    @Override // com.android.systemui.statusbar.StatusIconDisplayable
    public boolean isIconVisible() {
        return this.mState.visible;
    }

    @Override // com.android.systemui.statusbar.StatusIconDisplayable
    public void setVisibleState(int i, boolean z) {
        if (i != this.mVisibleState) {
            this.mVisibleState = i;
            if (i == 0) {
                this.mMobileGroup.setVisibility(0);
                this.mDotView.setVisibility(8);
            } else if (i != 1) {
                if (OpUtils.DEBUG_ONEPLUS) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("setVisibleState, state:");
                    sb.append(i);
                    sb.append(", animate:");
                    sb.append(z);
                    sb.append(", mState.visible:");
                    StatusBarSignalPolicy.MobileIconState mobileIconState = this.mState;
                    sb.append(mobileIconState != null ? Boolean.valueOf(mobileIconState.visible) : "null");
                    sb.append(", mState:");
                    sb.append(this.mState);
                    sb.append(", stack:");
                    sb.append(Debug.getCallers(5));
                    Log.i("StatusBarMobileView", sb.toString());
                }
                StatusBarSignalPolicy.MobileIconState mobileIconState2 = this.mState;
                if (mobileIconState2 == null || !mobileIconState2.visible) {
                    this.mMobileGroup.setVisibility(8);
                    this.mDotView.setVisibility(8);
                    return;
                }
                this.mMobileGroup.setVisibility(4);
                this.mDotView.setVisibility(4);
            } else {
                this.mMobileGroup.setVisibility(4);
                this.mDotView.setVisibility(0);
            }
        }
    }

    @Override // com.android.systemui.statusbar.StatusIconDisplayable
    public int getVisibleState() {
        return this.mVisibleState;
    }

    @VisibleForTesting
    public StatusBarSignalPolicy.MobileIconState getState() {
        return this.mState;
    }

    private boolean needFixVisibleState() {
        return this.mState.visible && getVisibility() != 0;
    }

    private boolean needFixInVisibleState() {
        return !this.mState.visible && getVisibility() == 0;
    }

    @Override // android.view.View, java.lang.Object
    public String toString() {
        return "StatusBarMobileView(slot=" + this.mSlot + " state=" + this.mState + ")";
    }

    private void updateMobileIconPadding() {
        ImageView imageView = this.mMobile;
        if (imageView != null) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) imageView.getLayoutParams();
            int i = 0;
            if (this.mMobileType.getDrawable() != null && this.mMobileType.getVisibility() == 0) {
                int intrinsicWidth = this.mMobileType.getDrawable().getIntrinsicWidth();
                int i2 = this.mState.typeId;
                if (i2 == OpSignalIcons.FOUR_G_LTE || i2 == OpSignalIcons.FOUR_G_PLUS_LTE || OpUtils.isUSS() || (OpUtils.isUST() && this.mState.typeId == C0006R$drawable.stat_sys_data_fully_connected_5g)) {
                    i = intrinsicWidth;
                }
            }
            if (marginLayoutParams.getMarginStart() != i) {
                marginLayoutParams.setMarginStart(i);
                this.mMobile.setLayoutParams(marginLayoutParams);
            }
        }
    }

    private void updateInOutIndicatorPadding() {
        ImageView imageView = this.mMobileInOut;
        if (imageView != null) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) imageView.getLayoutParams();
            int i = marginLayoutParams.topMargin;
            int dimensionPixelSize = (!OpUtils.isUST() || this.mState.typeId != C0006R$drawable.stat_sys_data_fully_connected_5g) ? 0 : ((FrameLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.op_5g_inout_indicator_margin_top);
            if (marginLayoutParams.topMargin != dimensionPixelSize) {
                marginLayoutParams.topMargin = dimensionPixelSize;
                this.mMobileInOut.setLayoutParams(marginLayoutParams);
            }
        }
    }

    private int getFiveGUWBInOutIndicator(boolean z, boolean z2) {
        int i = z ? 1 : 0;
        if (z2) {
            i += 2;
        }
        if (i == 1) {
            return C0006R$drawable.stat_sys_signal_stacked_in_5g_uwb;
        }
        if (i == 2) {
            return C0006R$drawable.stat_sys_signal_stacked_out_5g_uwb;
        }
        if (i != 3) {
            return C0006R$drawable.stat_sys_signal_stacked_none_5g_uwb;
        }
        return C0006R$drawable.stat_sys_signal_stacked_inout_5g_uwb;
    }

    @Override // android.view.View
    public void onRtlPropertiesChanged(int i) {
        ImageView imageView;
        super.onRtlPropertiesChanged(i);
        if (this.mState != null && (imageView = this.mMobile) != null && this.mStackedDataStrengthView != null && this.mStackedVoiceStrengthView != null) {
            this.mIsRtlPropertiesChanged = true;
            imageView.setImageDrawable(null);
            this.mStackedDataStrengthView.setImageDrawable(null);
            this.mStackedVoiceStrengthView.setImageDrawable(null);
            updateState(this.mState);
        }
    }

    private int getInOutIndicator(boolean z, boolean z2) {
        int i = z ? 1 : 0;
        if (z2) {
            i += 2;
        }
        if (i == 1) {
            return C0006R$drawable.stat_sys_signal_stacked_in;
        }
        if (i == 2) {
            return C0006R$drawable.stat_sys_signal_stacked_out;
        }
        if (i != 3) {
            return C0006R$drawable.stat_sys_signal_stacked_none;
        }
        return C0006R$drawable.stat_sys_signal_stacked_inout;
    }

    private boolean showStatck(StatusBarSignalPolicy.MobileIconState mobileIconState) {
        return (mobileIconState.stackedDataStrengthId == 0 || mobileIconState.stackedVoiceStrengthId == 0 || mobileIconState.stackedDataTypeId == 0 || mobileIconState.stackedVoiceTypeId == 0 || mobileIconState.showNoSim) ? false : true;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onConfigurationChanged(Configuration configuration) {
        this.mDirty = true;
    }
}
