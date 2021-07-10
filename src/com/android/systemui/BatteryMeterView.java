package com.android.systemui;

import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Debug;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.lifecycle.LifecycleOwner;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.settings.CurrentUserTracker;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.util.SysuiLifecycle;
import com.android.systemui.util.Utils;
import com.oneplus.battery.OpBatteryDashChargeView;
import com.oneplus.battery.OpBatteryMeterDrawable;
import com.oneplus.plugin.OpLsState;
import com.oneplus.systemui.OpBatteryMeterView;
import com.oneplus.util.OpUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.text.NumberFormat;
public class BatteryMeterView extends OpBatteryMeterView {
    private BatteryController mBatteryController;
    private final ImageView mBatteryIconView;
    private boolean mBatteryPercentShow;
    private TextView mBatteryPercentView;
    private boolean mCharging;
    private float mDarkIntensity;
    private boolean mDirty;
    private DualToneHandler mDualToneHandler;
    private boolean mForceShowPercent;
    private boolean mIgnoreTunerUpdates;
    protected int mIsInvalidCharge;
    private boolean mIsNeedForceUpdateBattery;
    private boolean mIsOptimizatedCharge;
    private boolean mIsSubscribedForTunerUpdates;
    private int mLevel;
    private int mNonAdaptedBackgroundColor;
    private int mNonAdaptedSingleToneColor;
    private final int mPercentageStyleId;
    private Rect mRect;
    private SettingObserver mSettingObserver;
    private int mShowPercentMode;
    private final String mSlotBattery;
    private int mTextColor;
    private int mTint;
    private int mUser;
    private final CurrentUserTracker mUserTracker;
    private WakefulnessLifecycle mWakefulnessLifecycle;
    private final WakefulnessLifecycle.Observer mWakefulnessObserver;

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    public BatteryMeterView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public BatteryMeterView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mShowPercentMode = 0;
        this.mDirty = true;
        this.mDarkIntensity = 0.0f;
        this.mIsInvalidCharge = 0;
        this.mWakefulnessObserver = new WakefulnessLifecycle.Observer() { // from class: com.android.systemui.BatteryMeterView.1
            @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
            public void onFinishedWakingUp() {
                if (Build.DEBUG_ONEPLUS) {
                    Log.i("BatteryMeterView", "onFinishedWakingUp, " + BatteryMeterView.this.getParent());
                }
                if (BatteryMeterView.this.mIsNeedForceUpdateBattery) {
                    BatteryMeterView.this.mIsNeedForceUpdateBattery = false;
                    BatteryMeterView batteryMeterView = BatteryMeterView.this;
                    batteryMeterView.onBatteryLevelChanged(batteryMeterView.mLevel, BatteryMeterView.this.mCharging, BatteryMeterView.this.mCharging);
                }
            }

            @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
            public void onStartedGoingToSleep() {
                if (Build.DEBUG_ONEPLUS) {
                    Log.i("BatteryMeterView", "onStartedGoingToSleep, " + BatteryMeterView.this.getParent());
                }
            }
        };
        this.mBatteryPercentShow = false;
        this.mIsOptimizatedCharge = false;
        BroadcastDispatcher broadcastDispatcher = (BroadcastDispatcher) Dependency.get(BroadcastDispatcher.class);
        setOrientation(0);
        setGravity(8388627);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.BatteryMeterView, i, 0);
        int color = obtainStyledAttributes.getColor(R$styleable.BatteryMeterView_frameColor, context.getColor(C0004R$color.meter_background_color));
        this.mPercentageStyleId = 0;
        this.mDrawable = new OpBatteryMeterDrawable(context, color);
        obtainStyledAttributes.recycle();
        this.mSettingObserver = new SettingObserver(new Handler(context.getMainLooper()));
        context.getResources().getBoolean(17891375);
        addOnAttachStateChangeListener(new Utils.DisableStateTracker(0, 2, (CommandQueue) Dependency.get(CommandQueue.class)));
        setupLayoutTransition();
        this.mSlotBattery = context.getString(17041302);
        ImageView imageView = new ImageView(context);
        this.mBatteryIconView = imageView;
        imageView.setImageDrawable(this.mDrawable);
        ViewGroup.MarginLayoutParams marginLayoutParams = new ViewGroup.MarginLayoutParams(getResources().getDimensionPixelSize(C0005R$dimen.status_bar_battery_icon_width), getResources().getDimensionPixelSize(C0005R$dimen.status_bar_battery_icon_height));
        int dimensionPixelSize = getResources().getDimensionPixelSize(C0005R$dimen.battery_margin_bottom);
        marginLayoutParams.setMargins(0, 0, getResources().getDimensionPixelSize(C0005R$dimen.op_status_bar_battery_icon_margin_right), getResources().getConfiguration().densityDpi <= 400 ? dimensionPixelSize + getResources().getDimensionPixelSize(C0005R$dimen.oneplus_battery_margin_bottom) : dimensionPixelSize);
        this.mBatteryIconView.setLayerType(1, null);
        addView(this.mBatteryIconView, marginLayoutParams);
        this.mBatteryDashChargeView = new OpBatteryDashChargeView(context, null);
        ViewGroup.MarginLayoutParams marginLayoutParams2 = new ViewGroup.MarginLayoutParams(getResources().getDimensionPixelSize(C0005R$dimen.op_status_bar_battery_dash_icon_width), getResources().getDimensionPixelSize(C0005R$dimen.op_status_bar_battery_dash_icon_height));
        marginLayoutParams2.setMargins(0, 0, 0, 0);
        addView(this.mBatteryDashChargeView, marginLayoutParams2);
        this.mFontScale = ((LinearLayout) this).mContext.getResources().getConfiguration().fontScale;
        updateShowPercent();
        this.mDualToneHandler = new DualToneHandler(context);
        onDarkChanged(new Rect(), 0.0f, -1);
        this.mUserTracker = new CurrentUserTracker(broadcastDispatcher) { // from class: com.android.systemui.BatteryMeterView.2
            @Override // com.android.systemui.settings.CurrentUserTracker
            public void onUserSwitched(int i2) {
                BatteryMeterView.this.mUser = i2;
                BatteryMeterView.this.getContext().getContentResolver().unregisterContentObserver(BatteryMeterView.this.mSettingObserver);
                BatteryMeterView.this.getContext().getContentResolver().registerContentObserver(Settings.System.getUriFor("status_bar_show_battery_percent"), false, BatteryMeterView.this.mSettingObserver, i2);
                BatteryMeterView.this.updateShowPercent();
            }
        };
        setClipChildren(false);
        setClipToPadding(false);
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).observe(SysuiLifecycle.viewAttachLifecycle(this), (LifecycleOwner) this);
        this.mWakefulnessLifecycle = (WakefulnessLifecycle) Dependency.get(WakefulnessLifecycle.class);
    }

    private void setupLayoutTransition() {
        LayoutTransition layoutTransition = new LayoutTransition();
        layoutTransition.setDuration(200);
        layoutTransition.setAnimator(2, ObjectAnimator.ofFloat((Object) null, "alpha", 0.0f, 1.0f));
        layoutTransition.setInterpolator(2, Interpolators.ALPHA_IN);
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat((Object) null, "alpha", 1.0f, 0.0f);
        layoutTransition.setInterpolator(3, Interpolators.ALPHA_OUT);
        layoutTransition.setAnimator(3, ofFloat);
        setLayoutTransition(layoutTransition);
    }

    public void setForceShowPercent(boolean z) {
        Log.i("BatteryMeterView", "setForceShowPercent / mForceShowPercent:" + this.mForceShowPercent + " / show:" + z);
        this.mForceShowPercent = z;
        setPercentShowMode(z ? 1 : 0);
    }

    public void setPercentShowMode(int i) {
        this.mShowPercentMode = i;
        updateShowPercent();
    }

    public void setIgnoreTunerUpdates(boolean z) {
        this.mIgnoreTunerUpdates = z;
        updateTunerSubscription();
    }

    private void updateTunerSubscription() {
        if (this.mIgnoreTunerUpdates) {
            unsubscribeFromTunerUpdates();
        } else {
            subscribeForTunerUpdates();
        }
    }

    private void subscribeForTunerUpdates() {
        if (!this.mIsSubscribedForTunerUpdates && !this.mIgnoreTunerUpdates) {
            ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "icon_blacklist");
            this.mIsSubscribedForTunerUpdates = true;
        }
    }

    private void unsubscribeFromTunerUpdates() {
        if (this.mIsSubscribedForTunerUpdates) {
            ((TunerService) Dependency.get(TunerService.class)).removeTunable(this);
            this.mIsSubscribedForTunerUpdates = false;
        }
    }

    public void setColorsFromContext(Context context) {
        if (context != null) {
            this.mDualToneHandler.setColorsFromContext(context);
        }
    }

    @Override // com.oneplus.systemui.OpBatteryMeterView, com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        if ("icon_blacklist".equals(str)) {
            setVisibility(StatusBarIconController.getIconBlacklist(getContext(), str2).contains(this.mSlotBattery) ? 8 : 0);
        }
        updateBatteryMeterVisibility();
        updateShowPercent();
    }

    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.i("BatteryMeterView", "onAttachedToWindow / ParentView:" + getParent());
        this.mWakefulnessLifecycle.addObserver(this.mWakefulnessObserver);
        BatteryController batteryController = (BatteryController) Dependency.get(BatteryController.class);
        this.mBatteryController = batteryController;
        batteryController.addCallback(this);
        this.mUser = ActivityManager.getCurrentUser();
        boolean z = false;
        getContext().getContentResolver().registerContentObserver(Settings.System.getUriFor("status_bar_show_battery_percent"), false, this.mSettingObserver, this.mUser);
        getContext().getContentResolver().registerContentObserver(Settings.Global.getUriFor("battery_estimates_last_update_time"), false, this.mSettingObserver);
        if (Settings.System.getIntForUser(getContext().getContentResolver(), "status_bar_show_battery_percent", 0, 0) != 0) {
            z = true;
        }
        this.mBatteryPercentShow = z;
        updateShowPercent();
        subscribeForTunerUpdates();
        this.mUserTracker.startTracking();
        scaleBatteryMeterViews();
        updateBatteryMeterVisibility();
    }

    @Override // android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.i("BatteryMeterView", "/ onDetachedFromWindow / ParentView:" + getParent());
        this.mWakefulnessLifecycle.removeObserver(this.mWakefulnessObserver);
        this.mUserTracker.stopTracking();
        this.mBatteryController.removeCallback(this);
        getContext().getContentResolver().unregisterContentObserver(this.mSettingObserver);
        unsubscribeFromTunerUpdates();
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
    public void onBatteryLevelChanged(int i, boolean z, boolean z2) {
        if (Build.DEBUG_ONEPLUS && this.mLevel != i) {
            Log.i("BatteryMeterView", "onBatteryLevelChanged / preLevel:" + this.mLevel + " / level:" + i + " / pluggedIn:" + z + " / charging:" + z2 + " / mIsNeedForceUpdateBattery:" + this.mIsNeedForceUpdateBattery + " / ParentView:" + getParent());
        }
        if (this.mLevel == i && this.mCharging == z) {
            this.mIsNeedForceUpdateBattery = true;
            return;
        }
        this.mIsNeedForceUpdateBattery = false;
        this.mDrawable.setCharging(z);
        this.mDrawable.setBatteryLevel(i);
        this.mCharging = z;
        this.mLevel = i;
        updatePercentText();
        updateDashChargeView();
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
    public void onBatteryStyleChanged(int i) {
        this.mDrawable.onBatteryStyleChanged(i);
        this.mBatteryStyle = i;
        updateBatteryMeterVisibility();
        scaleBatteryMeterViews();
        updateViews();
        postInvalidate();
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
    public void onBatteryPercentShowChange(boolean z) {
        if (this.mBatteryPercentShow != z) {
            this.mBatteryPercentShow = z;
            updateShowPercent();
        }
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
    public void onFastChargeChanged(int i) {
        boolean z = i > 0;
        if (this.mFastCharge != z) {
            this.mFastCharge = z;
            updateDashChargeView();
            updateBatteryMeterVisibility();
        }
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
    public void onWirelessWarpChargeChanged(boolean z) {
        if (this.mWirelessWarpCharging != z) {
            this.mWirelessWarpCharging = z;
            updateDashChargeView();
            updateBatteryMeterVisibility();
        }
    }

    @Override // com.oneplus.systemui.OpBatteryMeterView, com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
    public void onPowerSaveChanged(boolean z) {
        super.onPowerSaveChanged(z);
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
    public void onOptimizatedStatusChange(boolean z) {
        if (this.mIsOptimizatedCharge != z) {
            this.mIsOptimizatedCharge = z;
            Log.i("BatteryMeterView", "onOptimizatedStatusChange isOptimizatedCharge" + z + " / ParentView:" + getParent());
        }
        updateDashChargeView();
        updateBatteryMeterVisibility();
        this.mDrawable.onOptimizatedStatusChange(z);
        scaleBatteryMeterViews();
        updateViews();
        postInvalidate();
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
    public void onInvalidChargeChanged(int i) {
        if (this.mIsInvalidCharge != i) {
            this.mIsInvalidCharge = i;
            Log.i("BatteryMeterView", "onInvalidChargeChanged isInvalidCharge" + i + " / ParentView:" + getParent());
        }
        updateDashChargeView();
        updateBatteryMeterVisibility();
        this.mDrawable.setIsInvalidCharge(i);
        scaleBatteryMeterViews();
        updateViews();
        postInvalidate();
    }

    private TextView loadPercentView() {
        return (TextView) LayoutInflater.from(getContext()).inflate(C0011R$layout.battery_percentage_view, (ViewGroup) null);
    }

    public void updatePercentView() {
        TextView textView = this.mBatteryPercentView;
        if (textView != null) {
            removeView(textView);
            this.mBatteryPercentView = null;
        }
        updateShowPercent();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void updatePercentText() {
        int i;
        if (this.mBatteryController != null) {
            if (this.mBatteryPercentView == null) {
                Context context = getContext();
                if (this.mCharging) {
                    i = C0015R$string.accessibility_battery_level_charging;
                } else {
                    i = C0015R$string.accessibility_battery_level;
                }
                setContentDescription(context.getString(i, Integer.valueOf(this.mLevel)));
            } else if (this.mShowPercentMode != 3 || this.mCharging) {
                setPercentTextAtCurrentLevel();
            } else {
                this.mBatteryController.getEstimatedTimeRemainingString(new BatteryController.EstimateFetchCompletion() { // from class: com.android.systemui.-$$Lambda$BatteryMeterView$yZDQalqWJG2q_49RDLUqR8bhWwM
                    @Override // com.android.systemui.statusbar.policy.BatteryController.EstimateFetchCompletion
                    public final void onBatteryRemainingEstimateRetrieved(String str) {
                        BatteryMeterView.this.lambda$updatePercentText$0$BatteryMeterView(str);
                    }
                });
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$updatePercentText$0 */
    public /* synthetic */ void lambda$updatePercentText$0$BatteryMeterView(String str) {
        if (str != null) {
            this.mBatteryPercentView.setText(str);
            setContentDescription(getContext().getString(C0015R$string.accessibility_battery_level_with_estimate, Integer.valueOf(this.mLevel), str));
            return;
        }
        setPercentTextAtCurrentLevel();
    }

    private void setPercentTextAtCurrentLevel() {
        int i;
        this.mBatteryPercentView.setText(NumberFormat.getPercentInstance().format((double) (((float) this.mLevel) / 100.0f)));
        Context context = getContext();
        if (this.mCharging) {
            i = C0015R$string.accessibility_battery_level_charging;
        } else {
            i = C0015R$string.accessibility_battery_level;
        }
        setContentDescription(context.getString(i, Integer.valueOf(this.mLevel)));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateShowPercent() {
        boolean z = this.mBatteryPercentView != null;
        if (OpUtils.DEBUG_ONEPLUS) {
            Log.i("BatteryMeterView", "updateShowPercent showing:" + z + ", mBatteryPercentShow:" + this.mBatteryPercentShow + ", mForceShowPercent:" + this.mForceShowPercent + Debug.getCallers(5));
        }
        if (this.mBatteryPercentShow || this.mForceShowPercent) {
            if (!z) {
                TextView loadPercentView = loadPercentView();
                this.mBatteryPercentView = loadPercentView;
                int i = this.mPercentageStyleId;
                if (i != 0) {
                    loadPercentView.setTextAppearance(i);
                }
                int i2 = this.mTextColor;
                if (i2 != 0) {
                    this.mBatteryPercentView.setTextColor(i2);
                }
                updatePercentText();
                addView(this.mBatteryPercentView, 0, new ViewGroup.LayoutParams(-2, -1));
                scaleBatteryMeterViews();
            }
            TextView textView = this.mBatteryPercentView;
            if (textView != null) {
                textView.requestLayout();
            }
        } else if (z) {
            removeView(this.mBatteryPercentView);
            this.mBatteryPercentView = null;
        }
    }

    public void setTextColor(int i) {
        this.mTextColor = i;
        TextView textView = this.mBatteryPercentView;
        if (textView != null) {
            textView.setTextColor(i);
        }
    }

    public void updateDashChargeView() {
        if (showFastCharge()) {
            this.mBatteryDashChargeView.setLevel(this.mLevel);
            this.mBatteryDashChargeView.setVisibility(0);
            return;
        }
        this.mBatteryDashChargeView.setVisibility(8);
    }

    private void updateBatteryMeterVisibility() {
        if (this.mBatteryStyle == 2 || showFastCharge()) {
            this.mBatteryIconView.setVisibility(8);
        } else {
            this.mBatteryIconView.setVisibility(0);
        }
    }

    private boolean showFastCharge() {
        return (this.mFastCharge || this.mWirelessWarpCharging) && !this.mIsOptimizatedCharge && this.mIsInvalidCharge == 0;
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onDensityOrFontScaleChanged() {
        float f = ((LinearLayout) this).mContext.getResources().getConfiguration().fontScale;
        if (this.mFontScale != f) {
            this.mFontScale = f;
        }
        scaleBatteryMeterViews();
    }

    private void scaleBatteryMeterViews() {
        int i;
        Resources resources = getContext().getResources();
        TypedValue typedValue = new TypedValue();
        resources.getValue(C0005R$dimen.status_bar_icon_scale_factor, typedValue, true);
        float f = typedValue.getFloat();
        int dimensionPixelSize = resources.getDimensionPixelSize(C0005R$dimen.status_bar_battery_icon_height);
        if (this.mBatteryStyle == 1) {
            i = dimensionPixelSize;
        } else {
            i = resources.getDimensionPixelSize(C0005R$dimen.status_bar_battery_icon_width);
        }
        int dimensionPixelSize2 = resources.getDimensionPixelSize(C0005R$dimen.battery_margin_bottom);
        if (resources.getConfiguration().densityDpi <= 420) {
            dimensionPixelSize2 += resources.getDimensionPixelSize(C0005R$dimen.oneplus_battery_margin_bottom);
        }
        int dimensionPixelSize3 = getResources().getDimensionPixelSize(C0005R$dimen.op_status_bar_battery_icon_margin_right);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams((int) (((float) i) * f), (int) (((float) dimensionPixelSize) * f));
        layoutParams.setMargins(0, 0, dimensionPixelSize3, dimensionPixelSize2);
        this.mBatteryIconView.setLayoutParams(layoutParams);
        float dimensionPixelSize4 = ((float) ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.battery_level_text_size)) * this.mFontScale;
        TextView textView = this.mBatteryPercentView;
        if (textView != null) {
            textView.setTextSize(0, dimensionPixelSize4);
        }
        this.mBatteryDashChargeView.updateViews();
        this.mBatteryDashChargeView.setLayoutParams((LinearLayout.LayoutParams) this.mBatteryDashChargeView.getLayoutParams());
    }

    @Override // com.oneplus.systemui.OpBatteryMeterView, com.android.systemui.plugins.DarkIconDispatcher.DarkReceiver
    public void onDarkChanged(Rect rect, float f, int i) {
        this.mRect = rect;
        this.mDarkIntensity = f;
        this.mTint = i;
        updateAllBatteryColors();
    }

    public void updateColors(int i, int i2, int i3) {
        updateColors(i, i2);
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        String str;
        CharSequence charSequence = null;
        if (this.mDrawable == null) {
            str = null;
        } else {
            str = this.mDrawable.getPowerSave() + "";
        }
        TextView textView = this.mBatteryPercentView;
        if (textView != null) {
            charSequence = textView.getText();
        }
        printWriter.println("  BatteryMeterView:");
        printWriter.println("    mDrawable.getPowerSave: " + str);
        printWriter.println("    mBatteryPercentView.getText(): " + ((Object) charSequence));
        printWriter.println("    mTextColor: #" + Integer.toHexString(this.mTextColor));
        printWriter.println("    mLevel: " + this.mLevel);
        printWriter.println("    mForceShowPercent: " + this.mForceShowPercent);
    }

    /* access modifiers changed from: private */
    public final class SettingObserver extends ContentObserver {
        public SettingObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri) {
            super.onChange(z, uri);
            BatteryMeterView.this.updateShowPercent();
            if (TextUtils.equals(uri.getLastPathSegment(), "battery_estimates_last_update_time")) {
                BatteryMeterView.this.updatePercentText();
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.View, android.view.ViewGroup
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        if (OpLsState.getInstance().getPhoneStatusBar().isInMultiWindow() && this.mDirty && getWidth() > 0) {
            updateAllBatteryColors();
            this.mDirty = false;
        }
    }

    private void updateAllBatteryColors() {
        Rect rect = this.mRect;
        if (rect != null) {
            float f = DarkIconDispatcher.isInArea(rect, this) ? this.mDarkIntensity : 0.0f;
            this.mNonAdaptedSingleToneColor = this.mDualToneHandler.getSingleColor(f);
            this.mDualToneHandler.getFillColor(f);
            this.mNonAdaptedBackgroundColor = this.mDualToneHandler.getBackgroundColor(f);
            int tint = DarkIconDispatcher.getTint(this.mRect, this, this.mTint);
            setTextColor(tint);
            this.mBatteryDashChargeView.setIconTint(tint);
            updateColors(tint, this.mNonAdaptedBackgroundColor, this.mNonAdaptedSingleToneColor);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onConfigurationChanged(Configuration configuration) {
        this.mDirty = true;
    }
}
