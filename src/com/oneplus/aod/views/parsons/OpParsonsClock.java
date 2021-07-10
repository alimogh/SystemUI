package com.oneplus.aod.views.parsons;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Debug;
import android.os.PowerManager;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0011R$layout;
import com.android.systemui.C0016R$style;
import com.android.systemui.Dependency;
import com.android.systemui.R$styleable;
import com.oneplus.aod.utils.OpAodDimenHelper;
import com.oneplus.aod.utils.OpAodSettings;
import com.oneplus.aod.views.IOpAodClock;
import com.oneplus.systemui.biometrics.OpFodViewSettings;
import com.oneplus.util.OpUtils;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
public class OpParsonsClock extends RelativeLayout implements IOpAodClock {
    private OpParsonsBar mBar;
    private int mClockMarginBottom1;
    private int mClockMarginBottom1Id;
    private int mClockMarginBottom2;
    private int mClockMarginBottom2Id;
    private int mClockMarginTop1;
    private int mClockMarginTop1Id;
    private int mClockMarginTop2;
    private int mClockMarginTop2Id;
    private TextView mDateLabel;
    private LinearLayout mDateTimeContainer;
    private int mDateTimePaddingBottom;
    private int mDateTimePaddingBottomId;
    private int mDateTimePaddingTop;
    private int mDateTimePaddingTopId;
    private boolean mFodVisible;
    private Runnable mHideUnlockMsgRunnable;
    private int mMaxBurnIn;
    private int mMinBurnIn;
    private PowerManager mPowerManager;
    private TextView mTimeLabel;
    private int mUnlockMarginBottom;
    private int mUnlockMarginBottomId;
    private OpParsonsUnlockLabel mUnlocksMsg;
    private KeyguardUpdateMonitor mUpdateMonitor;
    private PowerManager.WakeLock mWakeLock;

    public OpParsonsClock(Context context) {
        this(context, null);
    }

    public OpParsonsClock(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public OpParsonsClock(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mHideUnlockMsgRunnable = new Runnable() { // from class: com.oneplus.aod.views.parsons.OpParsonsClock.1
            @Override // java.lang.Runnable
            public void run() {
                if (Build.DEBUG_ONEPLUS) {
                    Log.d("OpParsonsClock:UnlockMsg", "hide");
                }
                OpParsonsClock.this.mUnlocksMsg.setVisibility(4);
                OpParsonsClock.this.releaseWakeLock();
            }
        };
        View inflate = RelativeLayout.inflate(context, C0011R$layout.op_aod_parsons_clock, null);
        this.mBar = (OpParsonsBar) inflate.findViewById(C0008R$id.bar);
        this.mDateTimeContainer = (LinearLayout) inflate.findViewById(C0008R$id.timeContainer);
        this.mUnlocksMsg = (OpParsonsUnlockLabel) inflate.findViewById(C0008R$id.unlocks);
        this.mTimeLabel = (TextView) inflate.findViewById(C0008R$id.time);
        this.mDateLabel = (TextView) inflate.findViewById(C0008R$id.date);
        this.mBar.setOverlayView(this.mDateTimeContainer);
        this.mBar.setUnlocksMsg(this.mUnlocksMsg);
        setupAttributes(attributeSet);
        addView(inflate, new RelativeLayout.LayoutParams(-1, -2));
        setLayoutParams(new FrameLayout.LayoutParams(context, attributeSet));
        this.mUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        this.mPowerManager = (PowerManager) context.getSystemService(PowerManager.class);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.RelativeLayout, android.view.View, android.view.ViewGroup
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        if (z) {
            this.mBar.calculateBarHeight(this.mUnlocksMsg);
            this.mBar.onTimeChanged(new Date());
        }
    }

    @Override // com.oneplus.aod.views.IOpAodClock
    public void applyLayoutParams(OpAodSettings.OpViewInfo opViewInfo) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) getLayoutParams();
        if (!(layoutParams == null || opViewInfo == null)) {
            layoutParams.setMarginStart(opViewInfo.getMarginStart(((RelativeLayout) this).mContext));
            layoutParams.setMarginEnd(opViewInfo.getMarginEnd(((RelativeLayout) this).mContext));
            layoutParams.topMargin = this.mFodVisible ? this.mClockMarginTop1 : this.mClockMarginTop2;
            int i = this.mClockMarginBottom1;
            layoutParams.bottomMargin = i;
            if (this.mFodVisible) {
                layoutParams.bottomMargin = i + OpFodViewSettings.getFodIconSize(((RelativeLayout) this).mContext) + this.mClockMarginBottom2;
            }
            layoutParams.gravity = opViewInfo.getGravity();
        }
        if (getParent() != null) {
            setLayoutParams(layoutParams);
        }
        updateResource();
        RelativeLayout.LayoutParams layoutParams2 = (RelativeLayout.LayoutParams) this.mUnlocksMsg.getLayoutParams();
        if (layoutParams2 != null) {
            applyRules(layoutParams2, 1);
            layoutParams2.bottomMargin = this.mUnlockMarginBottom;
            this.mUnlocksMsg.setLayoutParams(layoutParams2);
        }
        RelativeLayout.LayoutParams layoutParams3 = (RelativeLayout.LayoutParams) this.mBar.getLayoutParams();
        if (layoutParams3 != null) {
            layoutParams3.width = this.mBar.getBarWidth();
            layoutParams3.height = this.mBar.getBarHeight();
            applyRules(layoutParams3, 1);
            this.mBar.setLayoutParams(layoutParams3);
        }
        int[] iArr = {16842901};
        TypedArray obtainStyledAttributes = ((RelativeLayout) this).mContext.obtainStyledAttributes(C0016R$style.op_parsons_clock_title, iArr);
        this.mTimeLabel.setTextSize(0, (float) OpUtils.convertDpToFixedPx2(obtainStyledAttributes.getDimension(0, 0.0f)));
        obtainStyledAttributes.recycle();
        TypedArray obtainStyledAttributes2 = ((RelativeLayout) this).mContext.obtainStyledAttributes(C0016R$style.op_parsons_clock_subtitle, iArr);
        this.mDateLabel.setTextSize(0, (float) OpUtils.convertDpToFixedPx2(obtainStyledAttributes2.getDimension(0, 0.0f)));
        obtainStyledAttributes2.recycle();
        this.mDateTimeContainer.setPadding(0, this.mDateTimePaddingTop, 0, this.mDateTimePaddingBottom);
        RelativeLayout.LayoutParams layoutParams4 = (RelativeLayout.LayoutParams) this.mDateTimeContainer.getLayoutParams();
        if (layoutParams4 != null) {
            this.mDateTimeContainer.setGravity(1);
            applyRules(layoutParams4, 1);
            this.mDateTimeContainer.setLayoutParams(layoutParams4);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        boolean predictFodVisible = predictFodVisible();
        if (this.mFodVisible != predictFodVisible) {
            updateUIBecauseOfFod(predictFodVisible);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks();
        this.mUnlocksMsg.setVisibility(0);
    }

    @Override // com.oneplus.aod.views.IOpAodClock
    public void onScreenTurnedOn() {
        this.mUnlocksMsg.setVisibility(0);
        waitToHide();
    }

    @Override // com.oneplus.aod.views.IOpAodClock
    public void recoverFromBurnInScreen() {
        boolean predictFodVisible = predictFodVisible();
        if (this.mFodVisible != predictFodVisible) {
            updateUIBecauseOfFod(predictFodVisible);
        }
    }

    @Override // com.oneplus.aod.views.IOpAodClock
    public void onTimeChanged(Calendar calendar) {
        String str;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        Locale locale = Locale.getDefault();
        String locale2 = locale.toString();
        if (locale2.startsWith("zh_") || locale2.startsWith("ko_") || locale2.startsWith("ja_")) {
            str = DateFormat.getBestDateTimePattern(locale, "MMMMd EEE");
        } else {
            str = DateFormat.getBestDateTimePattern(locale, "EEE MMM d");
        }
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat(str.toString(), locale);
        Date time = calendar.getTime();
        this.mTimeLabel.setText(simpleDateFormat.format(time).toString().replace(':', (char) 42889));
        this.mDateLabel.setText(simpleDateFormat2.format(time));
        this.mBar.onTimeChanged(time);
    }

    @Override // com.oneplus.aod.views.IOpAodClock
    public void alignforBurnIn2(ViewGroup viewGroup, int i, int i2) {
        RelativeLayout.LayoutParams layoutParams;
        RelativeLayout.LayoutParams layoutParams2;
        FrameLayout.LayoutParams layoutParams3 = (FrameLayout.LayoutParams) getLayoutParams();
        RelativeLayout.LayoutParams layoutParams4 = (RelativeLayout.LayoutParams) viewGroup.getLayoutParams();
        if (!(layoutParams3 == null || layoutParams4 == null)) {
            int i3 = this.mMinBurnIn;
            layoutParams3.leftMargin = i3;
            layoutParams3.rightMargin = i3;
            layoutParams3.gravity = i;
            layoutParams4.leftMargin = i3;
            layoutParams4.rightMargin = i3;
            if (i == 8388611) {
                float barWidth = ((float) (this.mBar.getBarWidth() * i2)) * 1.0f;
                int i4 = this.mMaxBurnIn;
                if (((float) layoutParams3.leftMargin) + barWidth > ((float) i4)) {
                    layoutParams3.leftMargin = i4;
                } else {
                    layoutParams3.leftMargin = (int) (((float) this.mMinBurnIn) + barWidth);
                }
                layoutParams4.leftMargin = layoutParams3.leftMargin;
            } else if (i == 8388613) {
                float barWidth2 = ((float) (this.mBar.getBarWidth() * (7 - i2))) * 1.0f;
                int i5 = this.mMaxBurnIn;
                if (((float) layoutParams3.rightMargin) + barWidth2 > ((float) i5)) {
                    layoutParams3.rightMargin = i5;
                } else {
                    layoutParams3.rightMargin = (int) (((float) this.mMinBurnIn) + barWidth2);
                }
                layoutParams4.rightMargin = layoutParams3.rightMargin;
            }
            setLayoutParams(layoutParams3);
            viewGroup.setLayoutParams(layoutParams4);
        }
        OpParsonsBar opParsonsBar = this.mBar;
        if (!(opParsonsBar == null || (layoutParams2 = (RelativeLayout.LayoutParams) opParsonsBar.getLayoutParams()) == null)) {
            applyRules(layoutParams2, i);
            this.mBar.setLayoutParams(layoutParams2);
        }
        LinearLayout linearLayout = this.mDateTimeContainer;
        if (linearLayout != null) {
            linearLayout.setGravity(i);
            RelativeLayout.LayoutParams layoutParams5 = (RelativeLayout.LayoutParams) this.mDateTimeContainer.getLayoutParams();
            if (layoutParams5 != null) {
                applyRules(layoutParams5, i);
                this.mDateTimeContainer.setLayoutParams(layoutParams5);
            }
        }
        OpParsonsUnlockLabel opParsonsUnlockLabel = this.mUnlocksMsg;
        if (opParsonsUnlockLabel != null && (layoutParams = (RelativeLayout.LayoutParams) opParsonsUnlockLabel.getLayoutParams()) != null) {
            applyRules(layoutParams, i);
            this.mUnlocksMsg.setLayoutParams(layoutParams);
        }
    }

    private void applyRules(RelativeLayout.LayoutParams layoutParams, int i) {
        if (i == 8388611) {
            layoutParams.addRule(20, -1);
            layoutParams.addRule(14, 0);
            layoutParams.addRule(21, 0);
        } else if (i == 8388613) {
            layoutParams.addRule(20, 0);
            layoutParams.addRule(14, 0);
            layoutParams.addRule(21, -1);
        } else {
            layoutParams.addRule(20, 0);
            layoutParams.addRule(14, -1);
            layoutParams.addRule(21, 0);
        }
    }

    private void updateResource() {
        this.mBar.updateResource();
        this.mUnlocksMsg.updateResource();
        this.mUnlockMarginBottom = OpAodDimenHelper.convertDpToFixedPx2(((RelativeLayout) this).mContext, this.mUnlockMarginBottomId);
        this.mDateTimePaddingTop = OpAodDimenHelper.convertDpToFixedPx2(((RelativeLayout) this).mContext, this.mDateTimePaddingTopId);
        this.mDateTimePaddingBottom = OpAodDimenHelper.convertDpToFixedPx2(((RelativeLayout) this).mContext, this.mDateTimePaddingBottomId);
        this.mClockMarginTop1 = OpAodDimenHelper.convertDpToFixedPx2(((RelativeLayout) this).mContext, this.mClockMarginTop1Id);
        this.mClockMarginBottom1 = OpAodDimenHelper.convertDpToFixedPx2(((RelativeLayout) this).mContext, this.mClockMarginBottom1Id);
        this.mClockMarginTop2 = OpAodDimenHelper.convertDpToFixedPx2(((RelativeLayout) this).mContext, this.mClockMarginTop2Id);
        this.mClockMarginBottom2 = OpAodDimenHelper.convertDpToFixedPx2(((RelativeLayout) this).mContext, this.mClockMarginBottom2Id);
        this.mMinBurnIn = OpAodDimenHelper.convertDpToFixedPx2(((RelativeLayout) this).mContext, C0005R$dimen.op_aod_parsons_clock_burnin_gap);
        this.mMaxBurnIn = OpAodDimenHelper.convertDpToFixedPx2(((RelativeLayout) this).mContext, C0005R$dimen.op_aod_parsons_clock_burnin_max_gap);
    }

    private void setupAttributes(AttributeSet attributeSet) {
        this.mBar.setupAttributes(attributeSet);
        TypedArray obtainStyledAttributes = ((RelativeLayout) this).mContext.obtainStyledAttributes(attributeSet, R$styleable.OpParsonsClock, 0, 0);
        this.mUnlockMarginBottomId = obtainStyledAttributes.getResourceId(R$styleable.OpParsonsClock_unlockMarginBottom, -1);
        this.mDateTimePaddingTopId = obtainStyledAttributes.getResourceId(R$styleable.OpParsonsClock_dateTimePaddingTop, -1);
        this.mDateTimePaddingBottomId = obtainStyledAttributes.getResourceId(R$styleable.OpParsonsClock_dateTimePaddingBottom, -1);
        this.mClockMarginTop1Id = obtainStyledAttributes.getResourceId(R$styleable.OpParsonsClock_clockMarginTop1, -1);
        this.mClockMarginTop2Id = obtainStyledAttributes.getResourceId(R$styleable.OpParsonsClock_clockMarginTop2, -1);
        this.mClockMarginBottom1Id = obtainStyledAttributes.getResourceId(R$styleable.OpParsonsClock_clockMarginBottom1, -1);
        this.mClockMarginBottom2Id = obtainStyledAttributes.getResourceId(R$styleable.OpParsonsClock_clockMarginBottom2, -1);
        obtainStyledAttributes.recycle();
    }

    private void updateUIBecauseOfFod(boolean z) {
        if (this.mFodVisible != z) {
            this.mFodVisible = z;
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) getLayoutParams();
            if (layoutParams != null) {
                if (Build.DEBUG_ONEPLUS) {
                    Log.d("OpParsonsClock", "updateUIBecauseOfFod: mFodVisible= " + this.mFodVisible + ", callers= " + Debug.getCallers(1));
                }
                layoutParams.topMargin = this.mFodVisible ? this.mClockMarginTop1 : this.mClockMarginTop2;
                int i = this.mClockMarginBottom1;
                layoutParams.bottomMargin = i;
                if (this.mFodVisible) {
                    layoutParams.bottomMargin = i + OpFodViewSettings.getFodIconSize(((RelativeLayout) this).mContext) + this.mClockMarginBottom2;
                }
                setLayoutParams(layoutParams);
            }
            removeCallbacks();
            if (Build.DEBUG_ONEPLUS) {
                Log.d("OpParsonsClock:UnlockMsg", "updateUIBecauseOfFod: fodVisible= " + this.mFodVisible);
            }
            this.mUnlocksMsg.setVisibility(this.mFodVisible ? 0 : 4);
        }
    }

    private boolean predictFodVisible() {
        boolean isFingerprintEnrolled = this.mUpdateMonitor.isFingerprintEnrolled(ActivityManager.getCurrentUser());
        boolean isOpFingerprintDisabled = this.mUpdateMonitor.isOpFingerprintDisabled(ActivityManager.getCurrentUser());
        boolean isFingerprintLockout = this.mUpdateMonitor.isFingerprintLockout();
        boolean isFodSupportOnAod = this.mUpdateMonitor.isFodSupportOnAod();
        boolean isFodHintShowing = this.mUpdateMonitor.isFodHintShowing();
        boolean z = (!isOpFingerprintDisabled && ((isFingerprintEnrolled || isFingerprintLockout) && isFodSupportOnAod)) || isFodHintShowing;
        if (Build.DEBUG_ONEPLUS) {
            Log.d("OpParsonsClock", "predictFodVisible: isFingerprintAvaiable= " + isFingerprintEnrolled + ", isFingerprintDisabled= " + isOpFingerprintDisabled + ", isFingerprintLockout= " + isFingerprintLockout + ", isFodSupportOnAod= " + isFodSupportOnAod + ", isFodHintShowing= " + isFodHintShowing + ", fodVisible= " + z);
        }
        return z;
    }

    @Override // com.oneplus.aod.views.IOpAodClock
    public void onFodShowOrHideOnAod(boolean z) {
        if (Build.DEBUG_ONEPLUS) {
            Log.d("OpParsonsClock", "onFodShowOrHideOnAod: fodVisible= " + z);
        }
        if (!z && this.mFodVisible != z) {
            updateUIBecauseOfFod(z);
        }
    }

    @Override // com.oneplus.aod.views.IOpAodClock
    public void onFodIndicationVisibilityChanged(boolean z) {
        boolean isFingerprintEnrolled = this.mUpdateMonitor.isFingerprintEnrolled(ActivityManager.getCurrentUser());
        boolean isOpFingerprintDisabled = this.mUpdateMonitor.isOpFingerprintDisabled(ActivityManager.getCurrentUser());
        boolean isFingerprintLockout = this.mUpdateMonitor.isFingerprintLockout();
        boolean isAlwaysOnEnabled = this.mUpdateMonitor.isAlwaysOnEnabled();
        boolean isFodSupportOnAod = this.mUpdateMonitor.isFodSupportOnAod();
        if (Build.DEBUG_ONEPLUS) {
            Log.d("OpParsonsClock", "onFodIndicationVisibilityChanged:  isFingerprintAvaiable= " + isFingerprintEnrolled + ", isFingerprintDisabled= " + isOpFingerprintDisabled + ", isFingerprintLockout= " + isFingerprintLockout + ", isAlwaysOnEnabled= " + isAlwaysOnEnabled);
        }
        if (!isOpFingerprintDisabled && isFingerprintEnrolled && !isFodSupportOnAod) {
            updateUIBecauseOfFod(predictFodVisible());
        }
    }

    @Override // com.oneplus.aod.views.IOpAodClock
    public void onUserTrigger(int i) {
        if (Build.DEBUG_ONEPLUS) {
            Log.d("OpParsonsClock:UnlockMsg", "onUserTrigger: reason= " + i);
        }
        this.mUnlocksMsg.setVisibility(0);
        waitToHide();
    }

    private void waitToHide() {
        boolean isAlwaysOnEnabled = this.mUpdateMonitor.isAlwaysOnEnabled();
        if (Build.DEBUG_ONEPLUS) {
            Log.d("OpParsonsClock", "waitToHide: isAlwaysOnEnabled= " + isAlwaysOnEnabled + ", callers= " + Debug.getCallers(1));
        }
        removeCallbacks();
        if (isAlwaysOnEnabled && isAttachedToWindow()) {
            acquireWakeLock();
            postDelayed(this.mHideUnlockMsgRunnable, 3000);
        }
    }

    private void removeCallbacks() {
        releaseWakeLock();
        removeCallbacks(this.mHideUnlockMsgRunnable);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void releaseWakeLock() {
        PowerManager.WakeLock wakeLock = this.mWakeLock;
        if (wakeLock != null && wakeLock.isHeld()) {
            this.mWakeLock.release();
            this.mWakeLock = null;
        }
    }

    private void acquireWakeLock() {
        PowerManager.WakeLock newWakeLock = this.mPowerManager.newWakeLock(1, "OpParsonsClock#hideHint");
        this.mWakeLock = newWakeLock;
        newWakeLock.acquire();
    }
}
