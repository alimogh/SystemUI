package com.android.keyguard;

import android.app.ActivityManager;
import android.app.IActivityManager;
import android.app.IStopUserCallback;
import android.content.Context;
import android.content.res.Resources;
import android.os.Debug;
import android.os.Handler;
import android.os.RemoteException;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Slog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.graphics.ColorUtils;
import com.android.internal.widget.LockPatternUtils;
import com.android.systemui.C0004R$color;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0015R$string;
import com.android.systemui.C0016R$style;
import com.android.systemui.Dependency;
import com.android.systemui.assist.ui.DisplayUtils;
import com.android.systemui.statusbar.KeyguardIndicationController;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.oneplus.util.OpUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.TimeZone;
public class KeyguardStatusView extends GridLayout implements ConfigurationController.ConfigurationListener {
    private boolean mCharging;
    private TextView mChargingInfo;
    private TextView mChargingInfoLevel;
    private KeyguardClockSwitch mClockView;
    private float mDarkAmount;
    private Handler mHandler;
    private final IActivityManager mIActivityManager;
    private int mIconTopMargin;
    private KeyguardUpdateMonitorCallback mInfoCallback;
    private ViewGroup mInfoView;
    private KeyguardAssistantView mKeyguardSlice;
    private ViewGroup mLockIconContainer;
    private final LockPatternUtils mLockPatternUtils;
    private TextView mLogoutView;
    private View mNotificationIcons;
    private TextView mOwnerInfo;
    private Runnable mPendingMarqueeStart;
    private boolean mPulsing;
    private boolean mShowingHeader;
    private int mTextColor;

    public KeyguardStatusView(Context context) {
        this(context, null, 0);
    }

    public KeyguardStatusView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public KeyguardStatusView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mDarkAmount = 0.0f;
        this.mInfoCallback = new KeyguardUpdateMonitorCallback() { // from class: com.android.keyguard.KeyguardStatusView.1
            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onTimeChanged() {
                KeyguardStatusView.this.refreshTime();
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onTimeZoneChanged(TimeZone timeZone) {
                KeyguardStatusView.this.updateTimeZone(timeZone);
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onKeyguardVisibilityChanged(boolean z) {
                if (z) {
                    Slog.v("KeyguardStatusView", "refresh statusview showing:" + z);
                    KeyguardStatusView.this.refreshTime();
                    KeyguardStatusView.this.updateOwnerInfo();
                    KeyguardStatusView.this.updateLogoutView();
                }
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onStartedWakingUp() {
                KeyguardStatusView.this.setEnableMarquee(true);
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onFinishedGoingToSleep(int i2) {
                KeyguardStatusView.this.setEnableMarquee(false);
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onUserSwitchComplete(int i2) {
                KeyguardStatusView.this.refreshFormat();
                KeyguardStatusView.this.updateOwnerInfo();
                KeyguardStatusView.this.updateLogoutView();
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onLogoutEnabledChanged() {
                KeyguardStatusView.this.updateLogoutView();
            }
        };
        if (OpUtils.DEBUG_ONEPLUS) {
            Log.i("KeyguardStatusView", "init KeyguardStatusView, this:" + this);
        }
        this.mIActivityManager = ActivityManager.getService();
        this.mLockPatternUtils = new LockPatternUtils(getContext());
        this.mHandler = new Handler();
        onDensityOrFontScaleChanged();
    }

    public boolean hasCustomClock() {
        return this.mClockView.hasCustomClock();
    }

    public void setHasVisibleNotifications(boolean z) {
        this.mClockView.setHasVisibleNotifications(z);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setEnableMarquee(boolean z) {
        StringBuilder sb = new StringBuilder();
        sb.append("Schedule setEnableMarquee: ");
        sb.append(z ? "Enable" : "Disable");
        Log.v("KeyguardStatusView", sb.toString());
        if (!z) {
            Runnable runnable = this.mPendingMarqueeStart;
            if (runnable != null) {
                this.mHandler.removeCallbacks(runnable);
                this.mPendingMarqueeStart = null;
            }
            setEnableMarqueeImpl(false);
        } else if (this.mPendingMarqueeStart == null) {
            $$Lambda$KeyguardStatusView$ps9yj97ShIVR2u2hJB8SKuKkkQ r3 = new Runnable() { // from class: com.android.keyguard.-$$Lambda$KeyguardStatusView$ps9yj97ShIVR2u2hJB8SKuKk-kQ
                @Override // java.lang.Runnable
                public final void run() {
                    KeyguardStatusView.this.lambda$setEnableMarquee$0$KeyguardStatusView();
                }
            };
            this.mPendingMarqueeStart = r3;
            this.mHandler.postDelayed(r3, 2000);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setEnableMarquee$0 */
    public /* synthetic */ void lambda$setEnableMarquee$0$KeyguardStatusView() {
        setEnableMarqueeImpl(true);
        this.mPendingMarqueeStart = null;
    }

    private void setEnableMarqueeImpl(boolean z) {
        StringBuilder sb = new StringBuilder();
        sb.append(z ? "Enable" : "Disable");
        sb.append(" transport text marquee");
        Log.v("KeyguardStatusView", sb.toString());
        TextView textView = this.mOwnerInfo;
        if (textView != null) {
            textView.setSelected(z);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        LinearLayout linearLayout = (LinearLayout) findViewById(C0008R$id.status_view_container);
        this.mLogoutView = (TextView) findViewById(C0008R$id.logout);
        this.mNotificationIcons = findViewById(C0008R$id.clock_notification_icon_container);
        TextView textView = this.mLogoutView;
        if (textView != null) {
            textView.setOnClickListener(new View.OnClickListener() { // from class: com.android.keyguard.-$$Lambda$KeyguardStatusView$Pryio69yVoRI9F153p5QiMZe-bw
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    KeyguardStatusView.m3lambda$Pryio69yVoRI9F153p5QiMZebw(KeyguardStatusView.this, view);
                }
            });
        }
        KeyguardClockSwitch keyguardClockSwitch = (KeyguardClockSwitch) findViewById(C0008R$id.keyguard_clock_container);
        this.mClockView = keyguardClockSwitch;
        keyguardClockSwitch.setShowCurrentUserTime(true);
        if (KeyguardClockAccessibilityDelegate.isNeeded(((GridLayout) this).mContext)) {
            this.mClockView.setAccessibilityDelegate(new KeyguardClockAccessibilityDelegate(((GridLayout) this).mContext));
        }
        ViewGroup viewGroup = (ViewGroup) findViewById(C0008R$id.charging_and_owner_info_view);
        this.mInfoView = viewGroup;
        if (viewGroup != null) {
            Log.d("KeyguardStatusView", "onFinishInflate, mInfoView.getVisibility():" + this.mInfoView.getVisibility() + ", mInfoViewID:" + this.mInfoView.getId() + ", resId:" + C0008R$id.charging_and_owner_info_view + ", mInfoView:" + this.mInfoView + ", mClockView:" + this.mClockView);
        }
        this.mOwnerInfo = (TextView) findViewById(C0008R$id.owner_info);
        this.mTextColor = this.mClockView.getCurrentTextColor();
        onSliceContentChanged();
        setEnableMarquee(((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isDeviceInteractive());
        refreshFormat();
        updateOwnerInfo();
        updateLogoutView();
        updateDark();
        this.mChargingInfo = (TextView) findViewById(C0008R$id.charging_info);
        this.mChargingInfoLevel = (TextView) findViewById(C0008R$id.charging_info_level);
        this.mLockIconContainer = (ViewGroup) findViewById(C0008R$id.lock_icon_container);
        updateLayout();
        updateLayoutColor();
    }

    private void onSliceContentChanged() {
        this.mClockView.setKeyguardShowingHeader(false);
        if (this.mShowingHeader) {
            this.mShowingHeader = false;
            View view = this.mNotificationIcons;
            if (view != null) {
                ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
                marginLayoutParams.setMargins(marginLayoutParams.leftMargin, this.mIconTopMargin, marginLayoutParams.rightMargin, marginLayoutParams.bottomMargin);
                this.mNotificationIcons.setLayoutParams(marginLayoutParams);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.GridLayout, android.view.View, android.view.ViewGroup
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        layoutOwnerInfo();
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onDensityOrFontScaleChanged() {
        updateLayout();
        updateLayoutColor();
        loadBottomMargin();
    }

    private void updateLayout() {
        int width = DisplayUtils.getWidth(((GridLayout) this).mContext);
        ViewGroup viewGroup = this.mLockIconContainer;
        if (viewGroup != null) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) viewGroup.getLayoutParams();
            if (width > 1080) {
                marginLayoutParams.bottomMargin = OpUtils.convertPxByResolutionProportion((float) ((GridLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.op_keyguard_clock_info_view_content_lock_icon_margin_bottom), 1080);
                marginLayoutParams.leftMargin = OpUtils.convertPxByResolutionProportion((float) ((GridLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.op_keyguard_clock_info_view_content_padding_for_lock_icon_start), 1080);
                marginLayoutParams.rightMargin = OpUtils.convertPxByResolutionProportion((float) ((GridLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.op_keyguard_clock_info_view_content_padding_for_lock_icon_start), 1080);
            } else {
                marginLayoutParams.bottomMargin = ((GridLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.op_keyguard_clock_info_view_content_lock_icon_margin_bottom);
                marginLayoutParams.leftMargin = ((GridLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.op_keyguard_clock_info_view_content_padding_for_lock_icon_start);
                marginLayoutParams.rightMargin = ((GridLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.op_keyguard_clock_info_view_content_padding_for_lock_icon_start);
            }
            this.mLockIconContainer.setLayoutParams(marginLayoutParams);
        }
        ViewGroup viewGroup2 = this.mInfoView;
        if (viewGroup2 != null) {
            ViewGroup.MarginLayoutParams marginLayoutParams2 = (ViewGroup.MarginLayoutParams) viewGroup2.getLayoutParams();
            if (width > 1080) {
                marginLayoutParams2.leftMargin = OpUtils.convertPxByResolutionProportion((float) ((GridLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.op_keyguard_clock_info_view_content_padding), 1080);
                marginLayoutParams2.rightMargin = OpUtils.convertPxByResolutionProportion((float) ((GridLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.op_keyguard_clock_info_view_content_padding), 1080);
                return;
            }
            marginLayoutParams2.leftMargin = ((GridLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.op_keyguard_clock_info_view_content_padding);
            marginLayoutParams2.rightMargin = ((GridLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.op_keyguard_clock_info_view_content_padding);
        }
    }

    private void updateLayoutColor() {
        boolean z = ((GridLayout) this).mContext.getThemeResId() == C0016R$style.Theme_SystemUI_Light;
        int color = ((GridLayout) this).mContext.getResources().getColor(z ? C0004R$color.op_control_text_color_secondary_light : C0004R$color.op_control_text_color_secondary_dark);
        Log.i("KeyguardStatusView", "lightWpTheme:" + z + ", op_control_text_color_secondary_dark:" + ((GridLayout) this).mContext.getString(C0004R$color.op_control_text_color_secondary_dark) + ", op_control_text_color_secondary_light:" + ((GridLayout) this).mContext.getString(C0004R$color.op_control_text_color_secondary_light));
        KeyguardClockSwitch keyguardClockSwitch = this.mClockView;
        if (keyguardClockSwitch != null) {
            keyguardClockSwitch.setTextSize(0, (float) getResources().getDimensionPixelSize(OpUtils.isMCLVersion() ? C0005R$dimen.oneplus_widget_big_font_size_mcl : C0005R$dimen.oneplus_widget_big_font_size));
        }
        TextView textView = this.mOwnerInfo;
        if (textView != null) {
            textView.setTextAppearance(C0016R$style.op_control_text_style_body1);
            this.mOwnerInfo.setTextColor(color);
        }
        TextView textView2 = this.mChargingInfo;
        if (textView2 != null) {
            textView2.setTextAppearance(C0016R$style.op_control_text_style_body1);
            this.mChargingInfo.setTextColor(color);
        }
        TextView textView3 = this.mChargingInfoLevel;
        if (textView3 != null) {
            textView3.setTextAppearance(C0016R$style.op_control_text_style_body1);
            this.mChargingInfoLevel.setTextColor(color);
        }
    }

    public void dozeTimeTick() {
        refreshTime();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void refreshTime() {
        this.mClockView.refresh();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateTimeZone(TimeZone timeZone) {
        this.mClockView.onTimeZoneChanged(timeZone);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void refreshFormat() {
        Patterns.update(((GridLayout) this).mContext);
        this.mClockView.setFormat12Hour(Patterns.clockView12);
        this.mClockView.setFormat24Hour(Patterns.clockView24);
    }

    public float getClockTextSize() {
        return this.mClockView.getTextSize();
    }

    public int getClockPreferredY(int i) {
        return this.mClockView.getPreferredY(i);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateLogoutView() {
        TextView textView = this.mLogoutView;
        if (textView != null) {
            textView.setVisibility(shouldShowLogout() ? 0 : 8);
            this.mLogoutView.setText(((GridLayout) this).mContext.getResources().getString(17040258));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateOwnerInfo() {
        if (OpUtils.DEBUG_ONEPLUS) {
            Log.i("KeyguardStatusView", "updateOwnerInfo, mOwnerInfo:" + this.mOwnerInfo + Debug.getCallers(2));
        }
        if (this.mOwnerInfo != null) {
            String deviceOwnerInfo = this.mLockPatternUtils.getDeviceOwnerInfo();
            if (deviceOwnerInfo == null && this.mLockPatternUtils.isOwnerInfoEnabled(KeyguardUpdateMonitor.getCurrentUser())) {
                deviceOwnerInfo = this.mLockPatternUtils.getOwnerInfo(KeyguardUpdateMonitor.getCurrentUser());
            }
            this.mOwnerInfo.setText(deviceOwnerInfo);
            updateDark();
            updateInfoViewVisibility();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        KeyguardIndicationController keyguardIndicationController;
        super.onAttachedToWindow();
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).registerCallback(this.mInfoCallback);
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
        updateLayoutColor();
        updateLayout();
        Log.d("KeyguardStatusView", "KeyguardStatusView onAttachedToWindow view tag:" + getTag());
        if ("keyguardStatusView".equals(getTag()) && (keyguardIndicationController = ((StatusBar) Dependency.get(StatusBar.class)).getKeyguardIndicationController()) != null) {
            Log.d("KeyguardStatusView", "reset keyguard status view");
            keyguardIndicationController.setKeyguardStatusView(this);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).removeCallback(this.mInfoCallback);
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).removeCallback(this);
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onLocaleListChanged() {
        refreshFormat();
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        Object obj;
        printWriter.println("KeyguardStatusView:");
        StringBuilder sb = new StringBuilder();
        sb.append("  mOwnerInfo: ");
        TextView textView = this.mOwnerInfo;
        boolean z = true;
        if (textView == null) {
            obj = "null";
        } else {
            obj = Boolean.valueOf(textView.getVisibility() == 0);
        }
        sb.append(obj);
        printWriter.println(sb.toString());
        printWriter.println("  mPulsing: " + this.mPulsing);
        printWriter.println("  mDarkAmount: " + this.mDarkAmount);
        printWriter.println("  mTextColor: " + Integer.toHexString(this.mTextColor));
        printWriter.println("  mInfoView: " + this.mInfoView);
        StringBuilder sb2 = new StringBuilder();
        sb2.append("  mInfoView.getVisibility : ");
        ViewGroup viewGroup = this.mInfoView;
        sb2.append(viewGroup == null ? null : Integer.valueOf(viewGroup.getVisibility()));
        printWriter.println(sb2.toString());
        if (this.mLogoutView != null) {
            StringBuilder sb3 = new StringBuilder();
            sb3.append("  logout visible: ");
            if (this.mLogoutView.getVisibility() != 0) {
                z = false;
            }
            sb3.append(z);
            printWriter.println(sb3.toString());
        }
        KeyguardClockSwitch keyguardClockSwitch = this.mClockView;
        if (keyguardClockSwitch != null) {
            keyguardClockSwitch.dump(fileDescriptor, printWriter, strArr);
        }
        KeyguardAssistantView keyguardAssistantView = this.mKeyguardSlice;
        if (keyguardAssistantView != null) {
            keyguardAssistantView.dump(fileDescriptor, printWriter, strArr);
        }
    }

    private void loadBottomMargin() {
        this.mIconTopMargin = getResources().getDimensionPixelSize(C0005R$dimen.widget_vertical_padding);
        getResources().getDimensionPixelSize(C0005R$dimen.widget_vertical_padding_with_header);
    }

    /* access modifiers changed from: private */
    public static final class Patterns {
        static String cacheKey;
        static String clockView12;
        static String clockView24;

        static void update(Context context) {
            Locale locale = Locale.getDefault();
            Resources resources = context.getResources();
            String string = resources.getString(C0015R$string.clock_12hr_format);
            String string2 = resources.getString(C0015R$string.clock_24hr_format);
            String str = locale.toString() + string + string2;
            if (!str.equals(cacheKey)) {
                clockView12 = DateFormat.getBestDateTimePattern(locale, string);
                if (!string.contains("a")) {
                    clockView12 = clockView12.replaceAll("a", "").trim();
                }
                String bestDateTimePattern = DateFormat.getBestDateTimePattern(locale, string2);
                clockView24 = bestDateTimePattern;
                clockView24 = bestDateTimePattern.replace(':', (char) 42889);
                clockView12 = clockView12.replace(':', (char) 42889);
                cacheKey = str;
            }
        }
    }

    public void setDarkAmount(float f) {
        if (this.mDarkAmount != f) {
            this.mDarkAmount = f;
            this.mClockView.setDarkAmount(f);
            updateDark();
        }
    }

    private void updateDark() {
        int i = 0;
        boolean z = this.mDarkAmount == 1.0f;
        TextView textView = this.mLogoutView;
        if (textView != null) {
            textView.setAlpha(z ? 0.0f : 1.0f);
        }
        TextView textView2 = this.mOwnerInfo;
        if (textView2 != null) {
            boolean z2 = !TextUtils.isEmpty(textView2.getText());
            TextView textView3 = this.mOwnerInfo;
            if (!z2 || this.mDarkAmount == 1.0f || this.mCharging) {
                i = 8;
            }
            textView3.setVisibility(i);
            Log.i("KeyguardStatusView", "updateDark, hasText: " + z2 + ", stack:" + Debug.getCallers(3));
            layoutOwnerInfo();
        }
        this.mClockView.setTextColor(ColorUtils.blendARGB(this.mTextColor, -1, this.mDarkAmount));
    }

    private void layoutOwnerInfo() {
        TextView textView = this.mOwnerInfo;
        if (textView == null || textView.getVisibility() == 8) {
            View view = this.mNotificationIcons;
            if (view != null) {
                view.setScrollY(0);
                return;
            }
            return;
        }
        this.mOwnerInfo.setAlpha(1.0f - this.mDarkAmount);
        int bottom = (int) (((float) ((this.mOwnerInfo.getBottom() + this.mOwnerInfo.getPaddingBottom()) - (this.mOwnerInfo.getTop() - this.mOwnerInfo.getPaddingTop()))) * this.mDarkAmount);
        setBottom(getMeasuredHeight() - bottom);
        View view2 = this.mNotificationIcons;
        if (view2 != null) {
            view2.setScrollY(bottom);
        }
    }

    public void setPulsing(boolean z) {
        if (this.mPulsing != z) {
            this.mPulsing = z;
        }
    }

    private boolean shouldShowLogout() {
        return ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isLogoutEnabled() && KeyguardUpdateMonitor.getCurrentUser() != 0;
    }

    /* access modifiers changed from: private */
    public void onLogoutClicked(View view) {
        int currentUser = KeyguardUpdateMonitor.getCurrentUser();
        try {
            this.mIActivityManager.switchUser(0);
            this.mIActivityManager.stopUser(currentUser, true, (IStopUserCallback) null);
        } catch (RemoteException e) {
            Log.e("KeyguardStatusView", "Failed to logout user", e);
        }
    }

    public void setCharging(boolean z) {
        Log.d("KeyguardStatusView", "setCharging: " + z);
        this.mCharging = z;
        updateInfoViewVisibility();
    }

    public void updateInfoViewVisibility() {
        if (this.mInfoView == null) {
            Log.d("KeyguardStatusView", "updateInfoViewVisibility, mInfoView == null");
        } else {
            Log.d("KeyguardStatusView", "updateInfoViewVisibility, mInfoView.getVisibility():" + this.mInfoView.getVisibility() + ", mInfoViewID:" + this.mInfoView.getId() + ", resId:" + C0008R$id.charging_and_owner_info_view + ", mInfoView:" + this.mInfoView + ", mOwnerInfo:" + this.mOwnerInfo + ", mClockView:" + this.mClockView + ", mCharging:" + this.mCharging);
        }
        this.mInfoView = (ViewGroup) findViewById(C0008R$id.charging_and_owner_info_view);
        TextView textView = this.mOwnerInfo;
        if (textView != null) {
            if (this.mCharging) {
                textView.setVisibility(8);
            } else {
                this.mOwnerInfo.setVisibility((!(TextUtils.isEmpty(textView.getText()) ^ true) || this.mDarkAmount == 1.0f || this.mCharging) ? 8 : 0);
            }
        }
        TextView textView2 = this.mOwnerInfo;
        if ((textView2 == null || textView2.getVisibility() != 0) && !this.mCharging) {
            this.mInfoView.setVisibility(8);
        } else {
            this.mInfoView.setVisibility(0);
        }
    }

    public boolean hasOwnerInfo() {
        if (!(!TextUtils.isEmpty(this.mOwnerInfo.getText())) || this.mDarkAmount == 1.0f) {
            return false;
        }
        return true;
    }

    public ViewGroup getLockIconContainer() {
        return this.mLockIconContainer;
    }
}
