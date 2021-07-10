package com.android.systemui.qs;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Handler;
import android.service.notification.ZenModeConfig;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.MathUtils;
import android.util.Pair;
import android.view.ContextThemeWrapper;
import android.view.DisplayCutout;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.lifecycle.Observer;
import com.android.settingslib.Utils;
import com.android.systemui.BatteryMeterView;
import com.android.systemui.C0004R$color;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0016R$style;
import com.android.systemui.Dependency;
import com.android.systemui.DualToneHandler;
import com.android.systemui.Interpolators;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.qs.QSDetail;
import com.android.systemui.qs.TouchAnimator;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.phone.StatusBarWindowView;
import com.android.systemui.statusbar.phone.StatusIconContainer;
import com.android.systemui.statusbar.policy.Clock;
import com.android.systemui.statusbar.policy.DateView;
import com.android.systemui.statusbar.policy.NextAlarmController;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.util.RingerModeTracker;
import com.oneplus.systemui.qs.OpQSWidgetAdapter;
import com.oneplus.systemui.statusbar.phone.OpScreenBurnInProtector;
import com.oneplus.util.OpUtils;
import com.oneplus.util.ThemeColorUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
public class QuickStatusBarHeader extends RelativeLayout implements View.OnClickListener, NextAlarmController.NextAlarmChangeCallback, ZenModeController.Callback, LifecycleOwner, OpScreenBurnInProtector.OnBurnInPreventListener {
    private final ActivityStarter mActivityStarter;
    private final NextAlarmController mAlarmController;
    private BatteryMeterView mBatteryRemainingIcon;
    private Clock mClockView;
    private final CommandQueue mCommandQueue;
    private int mContentMarginEnd;
    private int mContentMarginStart;
    private int mCutOutPaddingLeft;
    private int mCutOutPaddingRight;
    private DateView mDateView;
    private DualToneHandler mDualToneHandler;
    private boolean mExpanded;
    private float mExpandedHeaderAlpha = 1.0f;
    protected QuickQSPanel mHeaderQsPanel;
    private TouchAnimator mHeaderTextContainerAlphaAnimator;
    private View mHeaderTextContainerView;
    private StatusBarIconController.TintedIconManager mIconManager;
    private boolean mIsLandscape = false;
    private float mKeyguardExpansionFraction;
    private final LifecycleRegistry mLifecycle = new LifecycleRegistry(this);
    private boolean mListening;
    private AlarmManager.AlarmClockInfo mNextAlarm;
    private View mNextAlarmContainer;
    private ImageView mNextAlarmIcon;
    private TextView mNextAlarmTextView;
    private boolean mQsDisabled;
    private QSPanel mQsPanel;
    private View mQuickQsStatusIcons;
    private View mRingerContainer;
    private int mRingerMode = 2;
    private ImageView mRingerModeIcon;
    private final Observer<Integer> mRingerModeObserver = new Observer<Integer>() { // from class: com.android.systemui.qs.QuickStatusBarHeader.1
        public void onChanged(Integer num) {
            QuickStatusBarHeader.this.mRingerMode = num.intValue();
            QuickStatusBarHeader.this.updateStatusText();
        }
    };
    private TextView mRingerModeTextView;
    private RingerModeTracker mRingerModeTracker;
    private int mRoundedCornerPadding = 0;
    private final StatusBarIconController mStatusBarIconController;
    private TouchAnimator mStatusIconsAlphaAnimator;
    private View mStatusSeparator;
    private View mSystemIconsView;
    private int mWaterfallTopInset;
    private final ZenModeController mZenController;

    public static float getColorIntensity(int i) {
        return i == -1 ? 0.0f : 1.0f;
    }

    public QuickStatusBarHeader(Context context, AttributeSet attributeSet, NextAlarmController nextAlarmController, ZenModeController zenModeController, StatusBarIconController statusBarIconController, ActivityStarter activityStarter, CommandQueue commandQueue, RingerModeTracker ringerModeTracker) {
        super(context, attributeSet);
        new Handler();
        this.mAlarmController = nextAlarmController;
        this.mZenController = zenModeController;
        this.mStatusBarIconController = statusBarIconController;
        this.mActivityStarter = activityStarter;
        this.mDualToneHandler = new DualToneHandler(new ContextThemeWrapper(context, C0016R$style.QSHeaderTheme));
        this.mCommandQueue = commandQueue;
        this.mRingerModeTracker = ringerModeTracker;
        setVisibility(8);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mHeaderQsPanel = (QuickQSPanel) findViewById(C0008R$id.quick_qs_panel);
        this.mSystemIconsView = findViewById(C0008R$id.quick_status_bar_system_icons);
        this.mQuickQsStatusIcons = findViewById(C0008R$id.quick_qs_status_icons);
        StatusIconContainer statusIconContainer = (StatusIconContainer) findViewById(C0008R$id.statusIcons);
        statusIconContainer.addIgnoredSlots(getIgnoredIconSlots());
        statusIconContainer.setShouldRestrictIcons(false);
        this.mIconManager = new StatusBarIconController.TintedIconManager(statusIconContainer, this.mCommandQueue, "qs");
        this.mHeaderTextContainerView = findViewById(C0008R$id.header_text_container);
        this.mStatusSeparator = findViewById(C0008R$id.status_separator);
        this.mNextAlarmIcon = (ImageView) findViewById(C0008R$id.next_alarm_icon);
        this.mNextAlarmTextView = (TextView) findViewById(C0008R$id.next_alarm_text);
        View findViewById = findViewById(C0008R$id.alarm_container);
        this.mNextAlarmContainer = findViewById;
        findViewById.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.qs.-$$Lambda$p8TkVReSUo0LsQ3y-9iKja9mJXE
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                QuickStatusBarHeader.this.onClick(view);
            }
        });
        this.mRingerModeIcon = (ImageView) findViewById(C0008R$id.ringer_mode_icon);
        this.mRingerModeTextView = (TextView) findViewById(C0008R$id.ringer_mode_text);
        View findViewById2 = findViewById(C0008R$id.ringer_container);
        this.mRingerContainer = findViewById2;
        findViewById2.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.qs.-$$Lambda$p8TkVReSUo0LsQ3y-9iKja9mJXE
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                QuickStatusBarHeader.this.onClick(view);
            }
        });
        updateResources();
        Rect rect = new Rect(0, 0, 0, 0);
        int singleColor = this.mDualToneHandler.getSingleColor(getColorIntensity(Utils.getColorAttrDefaultColor(getContext(), 16842800)));
        applyDarkness(C0008R$id.clock, rect, 0.0f, -1);
        this.mIconManager.setTint(singleColor);
        this.mNextAlarmIcon.setImageTintList(ColorStateList.valueOf(singleColor));
        this.mRingerModeIcon.setImageTintList(ColorStateList.valueOf(singleColor));
        Clock clock = (Clock) findViewById(C0008R$id.clock);
        this.mClockView = clock;
        clock.setOnClickListener(this);
        this.mDateView = (DateView) findViewById(C0008R$id.date);
        BatteryMeterView batteryMeterView = (BatteryMeterView) findViewById(C0008R$id.batteryRemainingIcon);
        this.mBatteryRemainingIcon = batteryMeterView;
        batteryMeterView.setIgnoreTunerUpdates(true);
        this.mBatteryRemainingIcon.setPercentShowMode(3);
        this.mBatteryRemainingIcon.setViewPositionType(1);
        this.mBatteryRemainingIcon.setForceShowPercent(true);
        this.mBatteryRemainingIcon.setOnClickListener(this);
        this.mRingerModeTextView.setSelected(true);
        this.mNextAlarmTextView.setSelected(true);
        updateThemeColor();
    }

    public QuickQSPanel getHeaderQsPanel() {
        return this.mHeaderQsPanel;
    }

    private List<String> getIgnoredIconSlots() {
        ArrayList arrayList = new ArrayList();
        arrayList.add(((RelativeLayout) this).mContext.getResources().getString(17041304));
        arrayList.add(((RelativeLayout) this).mContext.getResources().getString(17041317));
        return arrayList;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateStatusText() {
        boolean z = true;
        int i = 0;
        if (updateRingerStatus() || updateAlarmStatus()) {
            boolean z2 = this.mNextAlarmTextView.getVisibility() == 0;
            if (this.mRingerModeTextView.getVisibility() != 0) {
                z = false;
            }
            View view = this.mStatusSeparator;
            if (!z2 || !z) {
                i = 8;
            }
            view.setVisibility(i);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0051  */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0053  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x005b  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x005d  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0065  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0076  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0099  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean updateRingerStatus() {
        /*
            r10 = this;
            android.widget.TextView r0 = r10.mRingerModeTextView
            int r0 = r0.getVisibility()
            r1 = 1
            r2 = 0
            if (r0 != 0) goto L_0x000c
            r0 = r1
            goto L_0x000d
        L_0x000c:
            r0 = r2
        L_0x000d:
            android.widget.TextView r3 = r10.mRingerModeTextView
            java.lang.CharSequence r3 = r3.getText()
            com.android.systemui.statusbar.policy.ZenModeController r4 = r10.mZenController
            int r4 = r4.getZen()
            com.android.systemui.statusbar.policy.ZenModeController r5 = r10.mZenController
            android.app.NotificationManager$Policy r5 = r5.getConsolidatedPolicy()
            boolean r4 = android.service.notification.ZenModeConfig.isZenOverridingRinger(r4, r5)
            if (r4 != 0) goto L_0x004a
            int r4 = r10.mRingerMode
            if (r4 != r1) goto L_0x0039
            android.widget.ImageView r4 = r10.mRingerModeIcon
            int r5 = com.android.systemui.C0006R$drawable.ic_volume_ringer_vibrate
            r4.setImageResource(r5)
            android.widget.TextView r4 = r10.mRingerModeTextView
            int r5 = com.android.systemui.C0015R$string.qs_status_phone_vibrate
            r4.setText(r5)
        L_0x0037:
            r4 = r1
            goto L_0x004b
        L_0x0039:
            if (r4 != 0) goto L_0x004a
            android.widget.ImageView r4 = r10.mRingerModeIcon
            int r5 = com.android.systemui.C0006R$drawable.ic_volume_ringer_mute
            r4.setImageResource(r5)
            android.widget.TextView r4 = r10.mRingerModeTextView
            int r5 = com.android.systemui.C0015R$string.qs_status_phone_muted
            r4.setText(r5)
            goto L_0x0037
        L_0x004a:
            r4 = r2
        L_0x004b:
            android.widget.ImageView r5 = r10.mRingerModeIcon
            r6 = 8
            if (r4 == 0) goto L_0x0053
            r7 = r2
            goto L_0x0054
        L_0x0053:
            r7 = r6
        L_0x0054:
            r5.setVisibility(r7)
            android.widget.TextView r5 = r10.mRingerModeTextView
            if (r4 == 0) goto L_0x005d
            r7 = r2
            goto L_0x005e
        L_0x005d:
            r7 = r6
        L_0x005e:
            r5.setVisibility(r7)
            android.view.View r5 = r10.mRingerContainer
            if (r4 == 0) goto L_0x0066
            r6 = r2
        L_0x0066:
            r5.setVisibility(r6)
            com.android.systemui.qs.QSPanel r5 = r10.mQsPanel
            if (r5 == 0) goto L_0x00a2
            com.oneplus.systemui.qs.OpQSWidgetAdapter r5 = r5.getOpQSWidgetAdapter()
            if (r5 == 0) goto L_0x00a2
            r5 = 3
            if (r4 == 0) goto L_0x0099
            com.oneplus.systemui.qs.OpQSWidgetAdapter$OpWidgetInfo r6 = new com.oneplus.systemui.qs.OpQSWidgetAdapter$OpWidgetInfo
            int r7 = r10.mRingerMode
            if (r7 != r1) goto L_0x007f
            int r7 = com.android.systemui.C0006R$drawable.ic_volume_ringer_vibrate
            goto L_0x0081
        L_0x007f:
            int r7 = com.android.systemui.C0006R$drawable.ic_volume_ringer_mute
        L_0x0081:
            android.widget.TextView r8 = r10.mRingerModeTextView
            java.lang.CharSequence r8 = r8.getText()
            com.android.systemui.qs.-$$Lambda$QuickStatusBarHeader$rmL8c-lTpHdJ0baarb--uw6VVLY r9 = new com.android.systemui.qs.-$$Lambda$QuickStatusBarHeader$rmL8c-lTpHdJ0baarb--uw6VVLY
            r9.<init>()
            r6.<init>(r5, r7, r8, r9)
            com.android.systemui.qs.QSPanel r5 = r10.mQsPanel
            com.oneplus.systemui.qs.OpQSWidgetAdapter r5 = r5.getOpQSWidgetAdapter()
            r5.addItem(r6)
            goto L_0x00a2
        L_0x0099:
            com.android.systemui.qs.QSPanel r6 = r10.mQsPanel
            com.oneplus.systemui.qs.OpQSWidgetAdapter r6 = r6.getOpQSWidgetAdapter()
            r6.removeItem(r5)
        L_0x00a2:
            if (r0 != r4) goto L_0x00b2
            android.widget.TextView r10 = r10.mRingerModeTextView
            java.lang.CharSequence r10 = r10.getText()
            boolean r10 = java.util.Objects.equals(r3, r10)
            if (r10 != 0) goto L_0x00b1
            goto L_0x00b2
        L_0x00b1:
            r1 = r2
        L_0x00b2:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.qs.QuickStatusBarHeader.updateRingerStatus():boolean");
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$updateRingerStatus$0 */
    public /* synthetic */ void lambda$updateRingerStatus$0$QuickStatusBarHeader(View view) {
        this.mActivityStarter.postStartActivityDismissingKeyguard(new Intent("android.settings.SOUND_SETTINGS"), 0);
    }

    private void updateThemeColor() {
        int color = ThemeColorUtils.getColor(0);
        int color2 = ThemeColorUtils.getColor(12);
        int color3 = ThemeColorUtils.getColor(1);
        if (OpUtils.isREDVersion()) {
            this.mDateView.setTextColor(getContext().getColor(C0004R$color.op_turquoise));
        } else {
            this.mDateView.setTextColor(color3);
        }
        this.mNextAlarmIcon.setColorFilter(color);
        this.mNextAlarmTextView.setTextColor(color);
        if (this.mStatusSeparator.getBackground() != null) {
            this.mStatusSeparator.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }
        this.mRingerModeIcon.setColorFilter(color);
        this.mRingerModeTextView.setTextColor(color);
        this.mBatteryRemainingIcon.updateColors(color, color2, color);
        this.mIconManager.setTint(color3);
    }

    private boolean updateAlarmStatus() {
        boolean z;
        boolean z2 = this.mNextAlarmTextView.getVisibility() == 0;
        CharSequence text = this.mNextAlarmTextView.getText();
        AlarmManager.AlarmClockInfo alarmClockInfo = this.mNextAlarm;
        if (alarmClockInfo != null) {
            this.mNextAlarmTextView.setText(formatNextAlarm(alarmClockInfo));
            z = true;
        } else {
            z = false;
        }
        int i = 8;
        this.mNextAlarmIcon.setVisibility(z ? 0 : 8);
        this.mNextAlarmTextView.setVisibility(z ? 0 : 8);
        View view = this.mNextAlarmContainer;
        if (z) {
            i = 0;
        }
        view.setVisibility(i);
        QSPanel qSPanel = this.mQsPanel;
        if (!(qSPanel == null || qSPanel.getOpQSWidgetAdapter() == null)) {
            if (z) {
                this.mQsPanel.getOpQSWidgetAdapter().addItem(new OpQSWidgetAdapter.OpWidgetInfo(1, C0006R$drawable.ic_alarm, this.mNextAlarmTextView.getText(), new View.OnClickListener() { // from class: com.android.systemui.qs.-$$Lambda$QuickStatusBarHeader$DlEFdLUamVNfwOJK5rQuoaBC6yU
                    @Override // android.view.View.OnClickListener
                    public final void onClick(View view2) {
                        QuickStatusBarHeader.this.lambda$updateAlarmStatus$1$QuickStatusBarHeader(view2);
                    }
                }));
            } else {
                this.mQsPanel.getOpQSWidgetAdapter().removeItem(1);
            }
        }
        return z2 != z || !Objects.equals(text, this.mNextAlarmTextView.getText());
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$updateAlarmStatus$1 */
    public /* synthetic */ void lambda$updateAlarmStatus$1$QuickStatusBarHeader(View view) {
        AlarmManager.AlarmClockInfo alarmClockInfo = this.mNextAlarm;
        if (alarmClockInfo == null || alarmClockInfo.getShowIntent() == null) {
            Log.d("QuickStatusBarHeader", "No PendingIntent for next alarm. Using default intent");
            this.mActivityStarter.postStartActivityDismissingKeyguard(new Intent("android.intent.action.SHOW_ALARMS"), 0);
            return;
        }
        this.mActivityStarter.postStartActivityDismissingKeyguard(this.mNextAlarm.getShowIntent());
    }

    private void applyDarkness(int i, Rect rect, float f, int i2) {
        View findViewById = findViewById(i);
        if (findViewById instanceof DarkIconDispatcher.DarkReceiver) {
            ((DarkIconDispatcher.DarkReceiver) findViewById).onDarkChanged(rect, f, i2);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        updateResources();
        this.mClockView.useWallpaperTextColor(false);
    }

    @Override // android.view.View
    public void onRtlPropertiesChanged(int i) {
        super.onRtlPropertiesChanged(i);
        updateResources();
    }

    private void updateMinimumHeight() {
        setMinimumHeight(getStatusBarRelatedHeight(17105481) + ((RelativeLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.qs_quick_header_panel_height));
    }

    private int getStatusBarRelatedHeight(int i) {
        int cutoutPathdataHeight;
        int dimensionPixelSize = ((RelativeLayout) this).mContext.getResources().getDimensionPixelSize(i);
        return (!OpUtils.isSupportCutout() || OpUtils.isCutoutHide(((RelativeLayout) this).mContext) || (cutoutPathdataHeight = OpUtils.getCutoutPathdataHeight(((RelativeLayout) this).mContext)) <= dimensionPixelSize) ? dimensionPixelSize : cutoutPathdataHeight;
    }

    private void updateResources() {
        Resources resources = ((RelativeLayout) this).mContext.getResources();
        updateMinimumHeight();
        this.mRoundedCornerPadding = resources.getDimensionPixelSize(C0005R$dimen.rounded_corner_content_padding);
        resources.getDimensionPixelSize(C0005R$dimen.status_bar_padding_top);
        this.mHeaderTextContainerView.getLayoutParams().height = resources.getDimensionPixelSize(C0005R$dimen.qs_header_tooltip_height);
        View view = this.mHeaderTextContainerView;
        view.setLayoutParams(view.getLayoutParams());
        this.mIsLandscape = resources.getConfiguration().orientation == 2;
        this.mHeaderTextContainerView.setVisibility(8);
        if (this.mIsLandscape) {
            this.mQuickQsStatusIcons.setVisibility(8);
        } else if (!this.mQsDisabled) {
            this.mQuickQsStatusIcons.setVisibility(0);
        }
        updateClockView();
        this.mSystemIconsView.getLayoutParams().height = getStatusBarRelatedHeight(17105481);
        View view2 = this.mSystemIconsView;
        view2.setLayoutParams(view2.getLayoutParams());
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (this.mQsDisabled) {
            layoutParams.height = resources.getDimensionPixelSize(17105432);
        } else {
            layoutParams.height = -2;
        }
        setLayoutParams(layoutParams);
        updateStatusIconAlphaAnimator();
        updateHeaderTextContainerAlphaAnimator();
    }

    private void updateStatusIconAlphaAnimator() {
        TouchAnimator.Builder builder = new TouchAnimator.Builder();
        builder.addFloat(this.mQuickQsStatusIcons, "alpha", 1.0f, 0.0f, 0.0f);
        this.mStatusIconsAlphaAnimator = builder.build();
    }

    private void updateHeaderTextContainerAlphaAnimator() {
        TouchAnimator.Builder builder = new TouchAnimator.Builder();
        builder.addFloat(this.mHeaderTextContainerView, "alpha", 0.0f, 0.0f, this.mExpandedHeaderAlpha);
        this.mHeaderTextContainerAlphaAnimator = builder.build();
    }

    public void setExpanded(boolean z) {
        if (this.mExpanded != z) {
            this.mExpanded = z;
            this.mHeaderQsPanel.setExpanded(z);
            updateClockView();
            updateEverything();
        }
    }

    public void setExpansion(boolean z, float f, float f2) {
        float f3 = z ? 1.0f : f;
        TouchAnimator touchAnimator = this.mStatusIconsAlphaAnimator;
        if (touchAnimator != null) {
            touchAnimator.setPosition(f3);
        }
        if (z) {
            this.mHeaderTextContainerView.setTranslationY(f2);
        } else {
            this.mHeaderTextContainerView.setTranslationY(0.0f);
        }
        TouchAnimator touchAnimator2 = this.mHeaderTextContainerAlphaAnimator;
        if (touchAnimator2 != null) {
            touchAnimator2.setPosition(f3);
        }
        if (f < 1.0f && ((double) f) > 0.99d && this.mHeaderQsPanel.switchTileLayout()) {
            updateResources();
        }
        this.mKeyguardExpansionFraction = f3;
    }

    public void disable(int i, int i2, boolean z) {
        boolean z2 = true;
        int i3 = 0;
        if ((i2 & 1) == 0) {
            z2 = false;
        }
        if (z2 != this.mQsDisabled) {
            this.mQsDisabled = z2;
            this.mHeaderQsPanel.setDisabledByPolicy(z2);
            View view = this.mQuickQsStatusIcons;
            if (this.mQsDisabled) {
                i3 = 8;
            }
            view.setVisibility(i3);
            updateResources();
        }
    }

    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mRingerModeTracker.getRingerModeInternal().observeForever(this.mRingerModeObserver);
        this.mStatusBarIconController.addIconGroup(this.mIconManager);
        requestApplyInsets();
        OpScreenBurnInProtector.getInstance().registerListener(((RelativeLayout) this).mContext, this);
    }

    @Override // android.view.View
    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        int i;
        DisplayCutout displayCutout = windowInsets.getDisplayCutout();
        Pair<Integer, Integer> paddingNeededForCutoutAndRoundedCorner = StatusBarWindowView.paddingNeededForCutoutAndRoundedCorner(displayCutout, StatusBarWindowView.cornerCutoutMargins(displayCutout, getDisplay()), -1);
        this.mCutOutPaddingLeft = ((Integer) paddingNeededForCutoutAndRoundedCorner.first).intValue();
        this.mCutOutPaddingRight = ((Integer) paddingNeededForCutoutAndRoundedCorner.second).intValue();
        if (displayCutout == null) {
            i = 0;
        } else {
            i = displayCutout.getWaterfallInsets().top;
        }
        this.mWaterfallTopInset = i;
        updateClockPadding();
        return super.onApplyWindowInsets(windowInsets);
    }

    private void updateClockPadding() {
        int i;
        int i2;
        int i3;
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) getLayoutParams();
        int i4 = layoutParams.leftMargin;
        int i5 = layoutParams.rightMargin;
        if (!this.mIsLandscape) {
            int i6 = this.mCutOutPaddingLeft;
            if (i6 > 0) {
                i2 = Math.max(Math.max(i6, this.mRoundedCornerPadding) - (isLayoutRtl() ? this.mContentMarginEnd : this.mContentMarginStart), 0);
            } else {
                i2 = 0;
            }
            int i7 = this.mCutOutPaddingRight;
            if (i7 > 0) {
                i = Math.max(Math.max(i7, this.mRoundedCornerPadding) - (isLayoutRtl() ? this.mContentMarginStart : this.mContentMarginEnd), 0);
            } else {
                i = 0;
            }
        } else {
            i2 = 0;
            i = 0;
        }
        boolean isHolePunchCutoutHide = OpUtils.isHolePunchCutoutHide(getContext());
        int dimensionPixelSize = OpUtils.getDimensionPixelSize(getResources(), C0005R$dimen.status_bar_padding_start, 1080);
        int dimensionPixelSize2 = OpUtils.getDimensionPixelSize(getResources(), C0005R$dimen.status_bar_padding_end, 1080);
        if (isHolePunchCutoutHide) {
            dimensionPixelSize = dimensionPixelSize2;
        }
        if (isHolePunchCutoutHide) {
            i3 = 0;
        } else {
            i3 = OpUtils.getDimensionPixelSize(getResources(), C0005R$dimen.op_status_bar_cust_padding_top, 1080);
        }
        int dimensionPixelSize3 = getResources().getDimensionPixelSize(C0005R$dimen.op_notification_side_paddings);
        this.mSystemIconsView.setPadding(Math.max((i2 + dimensionPixelSize) - dimensionPixelSize3, 0), this.mWaterfallTopInset + i3, Math.max((i + dimensionPixelSize2) - dimensionPixelSize3, 0), 0);
    }

    @Override // android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        setListening(false);
        this.mRingerModeTracker.getRingerModeInternal().removeObserver(this.mRingerModeObserver);
        this.mStatusBarIconController.removeIconGroup(this.mIconManager);
        super.onDetachedFromWindow();
        OpScreenBurnInProtector.getInstance().unregisterListener(this);
    }

    public void setListening(boolean z) {
        if (z != this.mListening) {
            this.mHeaderQsPanel.setListening(z);
            if (this.mHeaderQsPanel.switchTileLayout()) {
                updateResources();
            }
            this.mListening = z;
            if (z) {
                this.mZenController.addCallback(this);
                this.mAlarmController.addCallback(this);
                this.mLifecycle.setCurrentState(Lifecycle.State.RESUMED);
                return;
            }
            this.mZenController.removeCallback(this);
            this.mAlarmController.removeCallback(this);
            this.mLifecycle.setCurrentState(Lifecycle.State.CREATED);
        }
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (view == this.mClockView) {
            this.mActivityStarter.postStartActivityDismissingKeyguard(new Intent("android.intent.action.SHOW_ALARMS"), 0);
            return;
        }
        View view2 = this.mNextAlarmContainer;
        if (view != view2 || !view2.isVisibleToUser()) {
            View view3 = this.mRingerContainer;
            if (view == view3 && view3.isVisibleToUser()) {
                this.mActivityStarter.postStartActivityDismissingKeyguard(new Intent("android.settings.SOUND_SETTINGS"), 0);
            } else if (view == this.mBatteryRemainingIcon) {
                ((ActivityStarter) Dependency.get(ActivityStarter.class)).postStartActivityDismissingKeyguard(new Intent("android.intent.action.POWER_USAGE_SUMMARY"), 0);
            }
        } else if (this.mNextAlarm.getShowIntent() != null) {
            this.mActivityStarter.postStartActivityDismissingKeyguard(this.mNextAlarm.getShowIntent());
        } else {
            Log.d("QuickStatusBarHeader", "No PendingIntent for next alarm. Using default intent");
            this.mActivityStarter.postStartActivityDismissingKeyguard(new Intent("android.intent.action.SHOW_ALARMS"), 0);
        }
    }

    @Override // com.android.systemui.statusbar.policy.NextAlarmController.NextAlarmChangeCallback
    public void onNextAlarmChanged(AlarmManager.AlarmClockInfo alarmClockInfo) {
        this.mNextAlarm = alarmClockInfo;
        updateStatusText();
    }

    @Override // com.android.systemui.statusbar.policy.ZenModeController.Callback
    public void onZenChanged(int i) {
        updateStatusText();
    }

    @Override // com.android.systemui.statusbar.policy.ZenModeController.Callback
    public void onConfigChanged(ZenModeConfig zenModeConfig) {
        updateStatusText();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$updateEverything$2 */
    public /* synthetic */ void lambda$updateEverything$2$QuickStatusBarHeader() {
        setClickable(!this.mExpanded);
    }

    public void updateEverything() {
        post(new Runnable() { // from class: com.android.systemui.qs.-$$Lambda$QuickStatusBarHeader$L6Vxn1_c_VF7GvfDmwuayht1Id4
            @Override // java.lang.Runnable
            public final void run() {
                QuickStatusBarHeader.this.lambda$updateEverything$2$QuickStatusBarHeader();
            }
        });
    }

    public void setQSPanel(QSPanel qSPanel) {
        this.mQsPanel = qSPanel;
        updateStatusText();
        setupHost(qSPanel.getHost());
    }

    public void setupHost(QSTileHost qSTileHost) {
        this.mHeaderQsPanel.setQSPanelAndHeader(this.mQsPanel, this);
        this.mHeaderQsPanel.setHost(qSTileHost, null);
        this.mBatteryRemainingIcon.onDarkChanged(new Rect(), 0.0f, -1);
    }

    public void setCallback(QSDetail.Callback callback) {
        this.mHeaderQsPanel.setCallback(callback);
    }

    private String formatNextAlarm(AlarmManager.AlarmClockInfo alarmClockInfo) {
        if (alarmClockInfo == null) {
            return "";
        }
        return DateFormat.format(DateFormat.getBestDateTimePattern(Locale.getDefault(), DateFormat.is24HourFormat(((RelativeLayout) this).mContext, ActivityManager.getCurrentUser()) ? "EHm" : "Ehma"), alarmClockInfo.getTriggerTime()).toString();
    }

    @Override // androidx.lifecycle.LifecycleOwner
    public Lifecycle getLifecycle() {
        return this.mLifecycle;
    }

    public void setContentMargins(int i, int i2) {
        this.mContentMarginStart = i;
        this.mContentMarginEnd = i2;
        for (int i3 = 0; i3 < getChildCount(); i3++) {
            View childAt = getChildAt(i3);
            QuickQSPanel quickQSPanel = this.mHeaderQsPanel;
            if (childAt == quickQSPanel) {
                quickQSPanel.setContentMargins(i, i2);
            } else {
                ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) childAt.getLayoutParams();
                marginLayoutParams.setMarginStart(i);
                marginLayoutParams.setMarginEnd(i2);
                childAt.setLayoutParams(marginLayoutParams);
            }
        }
        updateClockPadding();
    }

    public void setExpandedScrollAmount(int i) {
        float f = 1.0f;
        if (this.mHeaderTextContainerView.getHeight() > 0) {
            f = Interpolators.ALPHA_OUT.getInterpolation(MathUtils.map(0.0f, ((float) this.mHeaderTextContainerView.getHeight()) / 2.0f, 1.0f, 0.0f, (float) i));
        }
        this.mHeaderTextContainerView.setScrollY(i);
        if (f != this.mExpandedHeaderAlpha) {
            this.mExpandedHeaderAlpha = f;
            this.mHeaderTextContainerView.setAlpha(MathUtils.lerp(0.0f, f, this.mKeyguardExpansionFraction));
            updateHeaderTextContainerAlphaAnimator();
        }
    }

    private void updateClockView() {
        if (this.mClockView != null) {
            if (!this.mExpanded || this.mIsLandscape || !OpUtils.needLargeQSClock(getContext())) {
                this.mClockView.setVisibility(0);
            } else {
                this.mClockView.setVisibility(8);
            }
        }
    }

    @Override // com.oneplus.systemui.statusbar.phone.OpScreenBurnInProtector.OnBurnInPreventListener
    public void onBurnInPreventTrigger(int i) {
        View view = this.mSystemIconsView;
        if (view != null) {
            view.setTranslationX((float) i);
        }
    }
}
