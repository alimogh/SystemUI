package com.android.systemui.statusbar.notification.row;

import android.animation.TimeInterpolator;
import android.app.INotificationManager;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.metrics.LogMaker;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.UiEventLogger;
import com.android.systemui.C0004R$color;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0015R$string;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.statusbar.notification.VisualStabilityManager;
import com.android.systemui.statusbar.notification.row.NotificationGuts;
import com.android.systemui.statusbar.policy.ConfigurationController;
import java.util.List;
import java.util.Set;
public class NotificationInfo extends LinearLayout implements NotificationGuts.GutsContent {
    private static final Uri INSTANT_APP_BASE_URI = Uri.parse("content://com.nearme.instant.setting/notification");
    private int mAccentButtonTextColor;
    private int mActualHeight;
    private String mAppName;
    private OnAppSettingsClickListener mAppSettingsClickListener;
    private int mAppUid;
    private ChannelEditorDialogController mChannelEditorDialogController;
    private Integer mChosenImportance;
    private int mCurrentAlertingBehavior = -1;
    private String mDelegatePkg;
    private NotificationGuts mGutsContainer;
    private INotificationManager mINotificationManager;
    private int mImportanceButtonStroke;
    private String mInstantAppPkg;
    private boolean mIsDeviceProvisioned;
    private boolean mIsInstantApp = false;
    private boolean mIsNonblockable;
    private boolean mIsSingleDefaultChannel;
    private MetricsLogger mMetricsLogger;
    private int mNumUniqueChannelsInRow;
    private View.OnClickListener mOnAlert = new View.OnClickListener() { // from class: com.android.systemui.statusbar.notification.row.-$$Lambda$NotificationInfo$-lxdNUTZhRsTq1qLdFuCftTaKsI
        @Override // android.view.View.OnClickListener
        public final void onClick(View view) {
            NotificationInfo.this.lambda$new$0$NotificationInfo(view);
        }
    };
    private View.OnClickListener mOnDismissSettings = new View.OnClickListener() { // from class: com.android.systemui.statusbar.notification.row.-$$Lambda$NotificationInfo$p3qjyEUB89vA_NRs8XRVogtSM4k
        @Override // android.view.View.OnClickListener
        public final void onClick(View view) {
            NotificationInfo.this.lambda$new$2$NotificationInfo(view);
        }
    };
    private OnSettingsClickListener mOnSettingsClickListener;
    private View.OnClickListener mOnSilent = new View.OnClickListener() { // from class: com.android.systemui.statusbar.notification.row.-$$Lambda$NotificationInfo$x1Q8n0IIdzsrzqhyaxjftYvWg5M
        @Override // android.view.View.OnClickListener
        public final void onClick(View view) {
            NotificationInfo.this.lambda$new$1$NotificationInfo(view);
        }
    };
    private int mOneplusAccentColor;
    private String mPackageName;
    private Drawable mPkgIcon;
    private PackageManager mPm;
    private boolean mPresentingChannelEditorDialog = false;
    private boolean mPressedApply;
    private TextView mPriorityDescriptionView;
    private StatusBarNotification mSbn;
    private int mSecondaryColor;
    private TextView mSilentDescriptionView;
    private NotificationChannel mSingleNotificationChannel;
    @VisibleForTesting
    boolean mSkipPost = false;
    private int mStartingChannelImportance;
    private UiEventLogger mUiEventLogger;
    private Set<NotificationChannel> mUniqueChannelsInRow;
    private VisualStabilityManager mVisualStabilityManager;
    private boolean mWasShownHighPriority;

    public interface CheckSaveListener {
    }

    public interface OnAppSettingsClickListener {
        void onClick(View view, Intent intent);
    }

    public interface OnSettingsClickListener {
        void onClick(View view, NotificationChannel notificationChannel, int i);
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationGuts.GutsContent
    public View getContentView() {
        return this;
    }

    @VisibleForTesting
    public boolean isAnimating() {
        return false;
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationGuts.GutsContent
    public boolean needsFalsingProtection() {
        return true;
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationGuts.GutsContent
    public boolean willBeRemoved() {
        return false;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$NotificationInfo(View view) {
        this.mChosenImportance = 3;
        applyAlertingBehavior(0, true);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$1 */
    public /* synthetic */ void lambda$new$1$NotificationInfo(View view) {
        this.mChosenImportance = 2;
        applyAlertingBehavior(1, true);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$2 */
    public /* synthetic */ void lambda$new$2$NotificationInfo(View view) {
        this.mPressedApply = true;
        this.mGutsContainer.closeControls(view, true);
    }

    public NotificationInfo(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mPriorityDescriptionView = (TextView) findViewById(C0008R$id.alert_summary);
        this.mSilentDescriptionView = (TextView) findViewById(C0008R$id.silence_summary);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x00f1, code lost:
        if (r1 == null) goto L_0x00ff;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x00fa, code lost:
        if (r1 != null) goto L_0x00fc;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x00fc, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00ff, code lost:
        ((android.widget.ImageView) findViewById(com.android.systemui.C0008R$id.pkg_icon)).setImageDrawable(r0.mPkgIcon);
        ((android.widget.TextView) findViewById(com.android.systemui.C0008R$id.pkg_name)).setText(r0.mAppName);
        ((android.widget.TextView) findViewById(com.android.systemui.C0008R$id.channel_name)).setText(r0.mAppName);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void bindNotification(android.content.pm.PackageManager r1, android.app.INotificationManager r2, com.android.systemui.statusbar.notification.VisualStabilityManager r3, com.android.systemui.statusbar.notification.row.ChannelEditorDialogController r4, java.lang.String r5, android.app.NotificationChannel r6, java.util.Set<android.app.NotificationChannel> r7, com.android.systemui.statusbar.notification.collection.NotificationEntry r8, com.android.systemui.statusbar.notification.row.NotificationInfo.OnSettingsClickListener r9, com.android.systemui.statusbar.notification.row.NotificationInfo.OnAppSettingsClickListener r10, com.android.internal.logging.UiEventLogger r11, boolean r12, boolean r13, boolean r14) throws android.os.RemoteException {
        /*
        // Method dump skipped, instructions count: 505
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.row.NotificationInfo.bindNotification(android.content.pm.PackageManager, android.app.INotificationManager, com.android.systemui.statusbar.notification.VisualStabilityManager, com.android.systemui.statusbar.notification.row.ChannelEditorDialogController, java.lang.String, android.app.NotificationChannel, java.util.Set, com.android.systemui.statusbar.notification.collection.NotificationEntry, com.android.systemui.statusbar.notification.row.NotificationInfo$OnSettingsClickListener, com.android.systemui.statusbar.notification.row.NotificationInfo$OnAppSettingsClickListener, com.android.internal.logging.UiEventLogger, boolean, boolean, boolean):void");
    }

    private void bindInlineControls() {
        int i = 8;
        if (this.mIsNonblockable) {
            findViewById(C0008R$id.non_configurable_text).setVisibility(0);
            findViewById(C0008R$id.non_configurable_multichannel_text).setVisibility(8);
            findViewById(C0008R$id.interruptiveness_settings).setVisibility(8);
            ((TextView) findViewById(C0008R$id.done)).setText(C0015R$string.inline_done_button);
            findViewById(C0008R$id.turn_off_notifications).setVisibility(8);
        } else if (this.mNumUniqueChannelsInRow > 1) {
            findViewById(C0008R$id.non_configurable_text).setVisibility(8);
            findViewById(C0008R$id.interruptiveness_settings).setVisibility(8);
            findViewById(C0008R$id.non_configurable_multichannel_text).setVisibility(0);
        } else if (this.mIsInstantApp) {
            findViewById(C0008R$id.non_configurable_text).setVisibility(8);
            findViewById(C0008R$id.non_configurable_multichannel_text).setVisibility(8);
            findViewById(C0008R$id.interruptiveness_settings).setVisibility(8);
            findViewById(C0008R$id.bottom_buttons).setVisibility(0);
        } else {
            findViewById(C0008R$id.non_configurable_text).setVisibility(8);
            findViewById(C0008R$id.non_configurable_multichannel_text).setVisibility(8);
            findViewById(C0008R$id.interruptiveness_settings).setVisibility(0);
        }
        View findViewById = findViewById(C0008R$id.turn_off_notifications);
        findViewById.setOnClickListener(getTurnOffNotificationsClickListener());
        if (findViewById.hasOnClickListeners() && !this.mIsNonblockable) {
            i = 0;
        }
        findViewById.setVisibility(i);
        View findViewById2 = findViewById(C0008R$id.done);
        findViewById2.setOnClickListener(this.mOnDismissSettings);
        findViewById2.setAccessibilityDelegate(this.mGutsContainer.getAccessibilityDelegate());
        View findViewById3 = findViewById(C0008R$id.silence);
        View findViewById4 = findViewById(C0008R$id.alert);
        findViewById3.setOnClickListener(this.mOnSilent);
        findViewById4.setOnClickListener(this.mOnAlert);
        applyAlertingBehavior(!this.mWasShownHighPriority ? 1 : 0, false);
    }

    private void bindHeader() {
        this.mPkgIcon = null;
        try {
            ApplicationInfo applicationInfo = this.mPm.getApplicationInfo(this.mPackageName, 795136);
            if (applicationInfo != null) {
                this.mAppName = String.valueOf(this.mPm.getApplicationLabel(applicationInfo));
                this.mPkgIcon = this.mPm.getApplicationIcon(applicationInfo);
            }
        } catch (PackageManager.NameNotFoundException unused) {
            this.mPkgIcon = this.mPm.getDefaultActivityIcon();
        }
        ((ImageView) findViewById(C0008R$id.pkg_icon)).setImageDrawable(this.mPkgIcon);
        ((TextView) findViewById(C0008R$id.pkg_name)).setText(this.mAppName);
        bindDelegate();
        View findViewById = findViewById(C0008R$id.app_settings);
        Intent appSettingsIntent = getAppSettingsIntent(this.mPm, this.mPackageName, this.mSingleNotificationChannel, this.mSbn.getId(), this.mSbn.getTag());
        int i = 8;
        boolean z = false;
        if (appSettingsIntent == null || TextUtils.isEmpty(this.mSbn.getNotification().getSettingsText())) {
            findViewById.setVisibility(8);
        } else {
            findViewById.setVisibility(0);
            findViewById.setOnClickListener(new View.OnClickListener(appSettingsIntent) { // from class: com.android.systemui.statusbar.notification.row.-$$Lambda$NotificationInfo$1n0u5clDG1rrcb2QJPV4T7x9OY0
                public final /* synthetic */ Intent f$1;

                {
                    this.f$1 = r2;
                }

                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    NotificationInfo.this.lambda$bindHeader$3$NotificationInfo(this.f$1, view);
                }
            });
        }
        ImageView imageView = (ImageView) findViewById(C0008R$id.info);
        imageView.setOnClickListener(getSettingsOnClickListener());
        if (imageView.hasOnClickListeners()) {
            i = 0;
        }
        imageView.setVisibility(i);
        Configuration configuration = getResources().getConfiguration();
        if (configuration != null) {
            if ((configuration.uiMode & 48) == 32) {
                z = true;
            }
            if (z) {
                imageView.setColorFilter(getResources().getColor(C0004R$color.op_control_icon_color_active_dark));
            } else {
                imageView.setColorFilter(getResources().getColor(C0004R$color.op_control_icon_color_active_light));
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$bindHeader$3 */
    public /* synthetic */ void lambda$bindHeader$3$NotificationInfo(Intent intent, View view) {
        this.mAppSettingsClickListener.onClick(view, intent);
    }

    private View.OnClickListener getSettingsOnClickListener() {
        int i = this.mAppUid;
        if (i < 0 || this.mOnSettingsClickListener == null || !this.mIsDeviceProvisioned) {
            return null;
        }
        return new View.OnClickListener(i) { // from class: com.android.systemui.statusbar.notification.row.-$$Lambda$NotificationInfo$hxpyY8vJ1JgmBKZbtsmY4xt8GSo
            public final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                NotificationInfo.this.lambda$getSettingsOnClickListener$4$NotificationInfo(this.f$1, view);
            }
        };
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$getSettingsOnClickListener$4 */
    public /* synthetic */ void lambda$getSettingsOnClickListener$4$NotificationInfo(int i, View view) {
        this.mOnSettingsClickListener.onClick(view, this.mNumUniqueChannelsInRow > 1 ? null : this.mSingleNotificationChannel, i);
    }

    private View.OnClickListener getTurnOffNotificationsClickListener() {
        return new View.OnClickListener() { // from class: com.android.systemui.statusbar.notification.row.-$$Lambda$NotificationInfo$qmudzW8HIf3NdQ5m_rRVs9-9Xwo
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                NotificationInfo.this.lambda$getTurnOffNotificationsClickListener$6$NotificationInfo(view);
            }
        };
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$getTurnOffNotificationsClickListener$6 */
    public /* synthetic */ void lambda$getTurnOffNotificationsClickListener$6$NotificationInfo(View view) {
        ChannelEditorDialogController channelEditorDialogController;
        if (!this.mPresentingChannelEditorDialog && (channelEditorDialogController = this.mChannelEditorDialogController) != null) {
            this.mPresentingChannelEditorDialog = true;
            channelEditorDialogController.prepareDialogForApp(this.mAppName, this.mPackageName, this.mAppUid, this.mUniqueChannelsInRow, this.mPkgIcon, this.mOnSettingsClickListener, (ConfigurationController) Dependency.get(ConfigurationController.class));
            this.mChannelEditorDialogController.setOnFinishListener(new OnChannelEditorDialogFinishedListener() { // from class: com.android.systemui.statusbar.notification.row.-$$Lambda$NotificationInfo$qDAhbvIGRRm3TXYceFiIFzSgIH0
                @Override // com.android.systemui.statusbar.notification.row.OnChannelEditorDialogFinishedListener
                public final void onChannelEditorDialogFinished() {
                    NotificationInfo.this.lambda$getTurnOffNotificationsClickListener$5$NotificationInfo();
                }
            });
            this.mChannelEditorDialogController.show();
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$getTurnOffNotificationsClickListener$5 */
    public /* synthetic */ void lambda$getTurnOffNotificationsClickListener$5$NotificationInfo() {
        this.mPresentingChannelEditorDialog = false;
        this.mGutsContainer.closeControls(this, false);
    }

    private void bindChannelDetails() throws RemoteException {
        bindName();
        bindGroup();
    }

    private void bindName() {
        TextView textView = (TextView) findViewById(C0008R$id.channel_name);
        if (this.mIsSingleDefaultChannel || this.mNumUniqueChannelsInRow > 1) {
            textView.setVisibility(8);
        } else {
            textView.setText(this.mSingleNotificationChannel.getName());
        }
    }

    private void bindDelegate() {
        TextView textView = (TextView) findViewById(C0008R$id.delegate_name);
        if (!TextUtils.equals(this.mPackageName, this.mDelegatePkg)) {
            textView.setVisibility(0);
        } else {
            textView.setVisibility(8);
        }
    }

    private void bindGroup() throws RemoteException {
        NotificationChannelGroup notificationChannelGroupForPackage;
        NotificationChannel notificationChannel = this.mSingleNotificationChannel;
        CharSequence name = (notificationChannel == null || notificationChannel.getGroup() == null || (notificationChannelGroupForPackage = this.mINotificationManager.getNotificationChannelGroupForPackage(this.mSingleNotificationChannel.getGroup(), this.mPackageName, this.mAppUid)) == null) ? null : notificationChannelGroupForPackage.getName();
        TextView textView = (TextView) findViewById(C0008R$id.group_name);
        if (name != null) {
            textView.setText(name);
            textView.setVisibility(0);
            return;
        }
        textView.setVisibility(8);
    }

    private void saveImportance() {
        if (!this.mIsNonblockable) {
            if (this.mChosenImportance == null) {
                this.mChosenImportance = Integer.valueOf(this.mStartingChannelImportance);
            }
            updateImportance();
        }
    }

    private void updateImportance() {
        if (this.mChosenImportance != null) {
            logUiEvent(NotificationControlsEvent.NOTIFICATION_CONTROLS_SAVE_IMPORTANCE);
            this.mMetricsLogger.write(importanceChangeLogMaker());
            int intValue = this.mChosenImportance.intValue();
            if (this.mStartingChannelImportance != -1000 && ((this.mWasShownHighPriority && this.mChosenImportance.intValue() >= 3) || (!this.mWasShownHighPriority && this.mChosenImportance.intValue() < 3))) {
                intValue = this.mStartingChannelImportance;
            }
            new Handler((Looper) Dependency.get(Dependency.BG_LOOPER)).post(new UpdateImportanceRunnable(this.mINotificationManager, this.mPackageName, this.mAppUid, this.mNumUniqueChannelsInRow == 1 ? this.mSingleNotificationChannel : null, this.mStartingChannelImportance, intValue, ((LinearLayout) this).mContext, this.mSbn, this.mIsInstantApp));
            this.mVisualStabilityManager.temporarilyAllowReordering();
        }
    }

    @Override // android.view.View
    public boolean post(Runnable runnable) {
        if (!this.mSkipPost) {
            return super.post(runnable);
        }
        runnable.run();
        return true;
    }

    private void applyAlertingBehavior(int i, boolean z) {
        int i2;
        boolean z2 = true;
        if (z && this.mCurrentAlertingBehavior != i) {
            TransitionSet transitionSet = new TransitionSet();
            transitionSet.setOrdering(0);
            transitionSet.addTransition(new Fade(2)).addTransition(new ChangeBounds()).addTransition(new Fade(1).setStartDelay(150).setDuration(200).setInterpolator(Interpolators.FAST_OUT_SLOW_IN));
            transitionSet.setDuration(350L);
            transitionSet.setInterpolator((TimeInterpolator) Interpolators.FAST_OUT_SLOW_IN);
            TransitionManager.beginDelayedTransition(this, transitionSet);
        }
        View findViewById = findViewById(C0008R$id.alert);
        View findViewById2 = findViewById(C0008R$id.silence);
        this.mCurrentAlertingBehavior = i;
        if (i == 0) {
            this.mPriorityDescriptionView.setVisibility(0);
            this.mSilentDescriptionView.setVisibility(8);
            post(new Runnable(findViewById, findViewById2) { // from class: com.android.systemui.statusbar.notification.row.-$$Lambda$NotificationInfo$YIsQp1RaloxgHB_A-HkyJ651jqE
                public final /* synthetic */ View f$1;
                public final /* synthetic */ View f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    NotificationInfo.this.lambda$applyAlertingBehavior$7$NotificationInfo(this.f$1, this.f$2);
                }
            });
        } else if (i == 1) {
            this.mSilentDescriptionView.setVisibility(0);
            this.mPriorityDescriptionView.setVisibility(8);
            post(new Runnable(findViewById, findViewById2) { // from class: com.android.systemui.statusbar.notification.row.-$$Lambda$NotificationInfo$rGPwwJwiYMnU-S7douf1BWTqpnY
                public final /* synthetic */ View f$1;
                public final /* synthetic */ View f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    NotificationInfo.this.lambda$applyAlertingBehavior$8$NotificationInfo(this.f$1, this.f$2);
                }
            });
        } else {
            throw new IllegalArgumentException("Unrecognized alerting behavior: " + i);
        }
        if (this.mWasShownHighPriority == (i == 0)) {
            z2 = false;
        }
        TextView textView = (TextView) findViewById(C0008R$id.done);
        if (z2) {
            i2 = C0015R$string.inline_ok_button;
        } else {
            i2 = C0015R$string.inline_done_button;
        }
        textView.setText(i2);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$applyAlertingBehavior$7 */
    public /* synthetic */ void lambda$applyAlertingBehavior$7$NotificationInfo(View view, View view2) {
        view.setSelected(true);
        view2.setSelected(false);
        if (this.mOneplusAccentColor != 0) {
            ((GradientDrawable) view2.getBackground()).setStroke(this.mImportanceButtonStroke, this.mSecondaryColor);
            ((ImageView) findViewById(C0008R$id.silence_icon)).setColorFilter(this.mSecondaryColor);
            ((TextView) findViewById(C0008R$id.silence_label)).setTextColor(this.mSecondaryColor);
            ((GradientDrawable) view.getBackground()).setStroke(this.mImportanceButtonStroke, this.mOneplusAccentColor);
            ((ImageView) findViewById(C0008R$id.alert_icon)).setColorFilter(this.mAccentButtonTextColor);
            ((TextView) findViewById(C0008R$id.alert_label)).setTextColor(this.mAccentButtonTextColor);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$applyAlertingBehavior$8 */
    public /* synthetic */ void lambda$applyAlertingBehavior$8$NotificationInfo(View view, View view2) {
        view.setSelected(false);
        view2.setSelected(true);
        if (this.mOneplusAccentColor != 0) {
            ((GradientDrawable) view.getBackground()).setStroke(this.mImportanceButtonStroke, this.mSecondaryColor);
            ((ImageView) findViewById(C0008R$id.alert_icon)).setColorFilter(this.mSecondaryColor);
            ((TextView) findViewById(C0008R$id.alert_label)).setTextColor(this.mSecondaryColor);
            ((GradientDrawable) view2.getBackground()).setStroke(this.mImportanceButtonStroke, this.mOneplusAccentColor);
            ((ImageView) findViewById(C0008R$id.silence_icon)).setColorFilter(this.mAccentButtonTextColor);
            ((TextView) findViewById(C0008R$id.silence_label)).setTextColor(this.mAccentButtonTextColor);
        }
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationGuts.GutsContent
    public void onFinishedClosing() {
        Integer num = this.mChosenImportance;
        if (num != null) {
            this.mStartingChannelImportance = num.intValue();
        }
        bindInlineControls();
        logUiEvent(NotificationControlsEvent.NOTIFICATION_CONTROLS_CLOSE);
        this.mMetricsLogger.write(notificationControlsLogMaker().setType(2));
    }

    @Override // android.view.View
    public void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        super.onInitializeAccessibilityEvent(accessibilityEvent);
        if (this.mGutsContainer != null && accessibilityEvent.getEventType() == 32) {
            if (this.mGutsContainer.isExposed()) {
                accessibilityEvent.getText().add(((LinearLayout) this).mContext.getString(C0015R$string.notification_channel_controls_opened_accessibility, this.mAppName));
            } else {
                accessibilityEvent.getText().add(((LinearLayout) this).mContext.getString(C0015R$string.notification_channel_controls_closed_accessibility, this.mAppName));
            }
        }
    }

    private Intent getAppSettingsIntent(PackageManager packageManager, String str, NotificationChannel notificationChannel, int i, String str2) {
        Intent intent = new Intent("android.intent.action.MAIN").addCategory("android.intent.category.NOTIFICATION_PREFERENCES").setPackage(str);
        List<ResolveInfo> queryIntentActivities = packageManager.queryIntentActivities(intent, 65536);
        if (queryIntentActivities == null || queryIntentActivities.size() == 0 || queryIntentActivities.get(0) == null) {
            return null;
        }
        ActivityInfo activityInfo = queryIntentActivities.get(0).activityInfo;
        intent.setClassName(activityInfo.packageName, activityInfo.name);
        if (notificationChannel != null) {
            intent.putExtra("android.intent.extra.CHANNEL_ID", notificationChannel.getId());
        }
        intent.putExtra("android.intent.extra.NOTIFICATION_ID", i);
        intent.putExtra("android.intent.extra.NOTIFICATION_TAG", str2);
        return intent;
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationGuts.GutsContent
    public void setGutsParent(NotificationGuts notificationGuts) {
        this.mGutsContainer = notificationGuts;
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationGuts.GutsContent
    public boolean shouldBeSaved() {
        return this.mPressedApply;
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationGuts.GutsContent
    public boolean handleCloseControls(boolean z, boolean z2) {
        ChannelEditorDialogController channelEditorDialogController;
        if (this.mPresentingChannelEditorDialog && (channelEditorDialogController = this.mChannelEditorDialogController) != null) {
            this.mPresentingChannelEditorDialog = false;
            channelEditorDialogController.setOnFinishListener(null);
            this.mChannelEditorDialogController.close();
        }
        if (z) {
            saveImportance();
        }
        return false;
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationGuts.GutsContent
    public int getActualHeight() {
        return this.mActualHeight;
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.View, android.view.ViewGroup
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        this.mActualHeight = getHeight();
    }

    /* access modifiers changed from: private */
    public static class UpdateImportanceRunnable implements Runnable {
        private final int mAppUid;
        private boolean mCancelInstantApp = false;
        private final NotificationChannel mChannelToUpdate;
        private final Context mContext;
        private final int mCurrentImportance;
        private final INotificationManager mINotificationManager;
        private final boolean mIsInstantApp;
        private final int mNewImportance;
        private final String mPackageName;
        private final StatusBarNotification mSbn;

        public UpdateImportanceRunnable(INotificationManager iNotificationManager, String str, int i, NotificationChannel notificationChannel, int i2, int i3, Context context, StatusBarNotification statusBarNotification, boolean z) {
            this.mContext = context;
            this.mSbn = statusBarNotification;
            this.mIsInstantApp = z;
            this.mINotificationManager = iNotificationManager;
            this.mPackageName = str;
            this.mAppUid = i;
            this.mChannelToUpdate = notificationChannel;
            this.mCurrentImportance = i2;
            this.mNewImportance = i3;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:27:0x007d, code lost:
            if (r1 == null) goto L_0x008c;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:28:0x007f, code lost:
            r1.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:32:0x0089, code lost:
            if (r1 != null) goto L_0x007f;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:34:0x008e, code lost:
            if (r11.mCancelInstantApp == false) goto L_?;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:35:0x0090, code lost:
            r5 = r11.mSbn.getTag();
            r6 = r11.mSbn.getId();
            android.util.Log.d("InfoGuts", "cancel Notification: " + r11.mPackageName);
            r11.mINotificationManager.cancelNotificationWithTag(r11.mPackageName, r11.mPackageName, r5, r6, -1);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:40:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:41:?, code lost:
            return;
         */
        @Override // java.lang.Runnable
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            /*
                r11 = this;
                java.lang.String r0 = "InfoGuts"
                android.app.NotificationChannel r1 = r11.mChannelToUpdate     // Catch:{ RemoteException -> 0x00bd }
                r2 = 1
                r3 = 0
                if (r1 == 0) goto L_0x0021
                android.app.NotificationChannel r1 = r11.mChannelToUpdate     // Catch:{ RemoteException -> 0x00bd }
                int r4 = r11.mNewImportance     // Catch:{ RemoteException -> 0x00bd }
                r1.setImportance(r4)     // Catch:{ RemoteException -> 0x00bd }
                android.app.NotificationChannel r1 = r11.mChannelToUpdate     // Catch:{ RemoteException -> 0x00bd }
                r4 = 4
                r1.lockFields(r4)     // Catch:{ RemoteException -> 0x00bd }
                android.app.INotificationManager r1 = r11.mINotificationManager     // Catch:{ RemoteException -> 0x00bd }
                java.lang.String r4 = r11.mPackageName     // Catch:{ RemoteException -> 0x00bd }
                int r5 = r11.mAppUid     // Catch:{ RemoteException -> 0x00bd }
                android.app.NotificationChannel r6 = r11.mChannelToUpdate     // Catch:{ RemoteException -> 0x00bd }
                r1.updateNotificationChannelForPackage(r4, r5, r6)     // Catch:{ RemoteException -> 0x00bd }
                goto L_0x0033
            L_0x0021:
                android.app.INotificationManager r1 = r11.mINotificationManager     // Catch:{ RemoteException -> 0x00bd }
                java.lang.String r4 = r11.mPackageName     // Catch:{ RemoteException -> 0x00bd }
                int r5 = r11.mAppUid     // Catch:{ RemoteException -> 0x00bd }
                int r6 = r11.mNewImportance     // Catch:{ RemoteException -> 0x00bd }
                int r7 = r11.mCurrentImportance     // Catch:{ RemoteException -> 0x00bd }
                if (r6 < r7) goto L_0x002f
                r6 = r2
                goto L_0x0030
            L_0x002f:
                r6 = r3
            L_0x0030:
                r1.setNotificationsEnabledWithImportanceLockForPackage(r4, r5, r6)     // Catch:{ RemoteException -> 0x00bd }
            L_0x0033:
                boolean r1 = r11.mIsInstantApp     // Catch:{ RemoteException -> 0x00bd }
                if (r1 == 0) goto L_0x00c3
                android.service.notification.StatusBarNotification r1 = r11.mSbn     // Catch:{ RemoteException -> 0x00bd }
                android.app.Notification r1 = r1.getNotification()     // Catch:{ RemoteException -> 0x00bd }
                android.os.Bundle r1 = r1.extras     // Catch:{ RemoteException -> 0x00bd }
                java.lang.String r4 = "small_app_package"
                java.lang.String r1 = r1.getString(r4)     // Catch:{ RemoteException -> 0x00bd }
                android.content.Context r4 = r11.mContext     // Catch:{ RemoteException -> 0x00bd }
                android.content.ContentResolver r5 = r4.getContentResolver()     // Catch:{ RemoteException -> 0x00bd }
                android.net.Uri r4 = com.android.systemui.statusbar.notification.row.NotificationInfo.access$000()     // Catch:{ RemoteException -> 0x00bd }
                android.net.Uri r6 = android.net.Uri.withAppendedPath(r4, r1)     // Catch:{ RemoteException -> 0x00bd }
                r7 = 0
                r8 = 0
                r9 = 0
                r10 = 0
                android.database.Cursor r1 = r5.query(r6, r7, r8, r9, r10)     // Catch:{ RemoteException -> 0x00bd }
                if (r1 == 0) goto L_0x0089
                boolean r4 = r1.moveToFirst()     // Catch:{ Exception -> 0x0077 }
                if (r4 == 0) goto L_0x0089
                java.lang.String r4 = "notify"
                int r4 = r1.getColumnIndex(r4)     // Catch:{ Exception -> 0x0077 }
                int r4 = r1.getInt(r4)     // Catch:{ Exception -> 0x0077 }
                if (r4 != 0) goto L_0x0071
                goto L_0x0072
            L_0x0071:
                r2 = r3
            L_0x0072:
                r11.mCancelInstantApp = r2     // Catch:{ Exception -> 0x0077 }
                goto L_0x0089
            L_0x0075:
                r11 = move-exception
                goto L_0x0083
            L_0x0077:
                r2 = move-exception
                java.lang.String r3 = "Fail to query data from Instant App base URI"
                android.util.Log.d(r0, r3, r2)     // Catch:{ all -> 0x0075 }
                if (r1 == 0) goto L_0x008c
            L_0x007f:
                r1.close()
                goto L_0x008c
            L_0x0083:
                if (r1 == 0) goto L_0x0088
                r1.close()
            L_0x0088:
                throw r11
            L_0x0089:
                if (r1 == 0) goto L_0x008c
                goto L_0x007f
            L_0x008c:
                boolean r1 = r11.mCancelInstantApp
                if (r1 == 0) goto L_0x00c3
                android.service.notification.StatusBarNotification r1 = r11.mSbn
                java.lang.String r5 = r1.getTag()
                android.service.notification.StatusBarNotification r1 = r11.mSbn
                int r6 = r1.getId()
                java.lang.StringBuilder r1 = new java.lang.StringBuilder
                r1.<init>()
                java.lang.String r2 = "cancel Notification: "
                r1.append(r2)
                java.lang.String r2 = r11.mPackageName
                r1.append(r2)
                java.lang.String r1 = r1.toString()
                android.util.Log.d(r0, r1)
                android.app.INotificationManager r2 = r11.mINotificationManager
                java.lang.String r3 = r11.mPackageName
                java.lang.String r4 = r11.mPackageName
                r7 = -1
                r2.cancelNotificationWithTag(r3, r4, r5, r6, r7)
                goto L_0x00c3
            L_0x00bd:
                r11 = move-exception
                java.lang.String r1 = "Unable to update notification importance"
                android.util.Log.e(r0, r1, r11)
            L_0x00c3:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.row.NotificationInfo.UpdateImportanceRunnable.run():void");
        }
    }

    private void logUiEvent(NotificationControlsEvent notificationControlsEvent) {
        StatusBarNotification statusBarNotification = this.mSbn;
        if (statusBarNotification != null) {
            this.mUiEventLogger.logWithInstanceId(notificationControlsEvent, statusBarNotification.getUid(), this.mSbn.getPackageName(), this.mSbn.getInstanceId());
        }
    }

    private LogMaker getLogMaker() {
        StatusBarNotification statusBarNotification = this.mSbn;
        if (statusBarNotification == null) {
            return new LogMaker(1621);
        }
        return statusBarNotification.getLogMaker().setCategory(1621);
    }

    private LogMaker importanceChangeLogMaker() {
        Integer num = this.mChosenImportance;
        return getLogMaker().setCategory(291).setType(4).setSubtype(Integer.valueOf(num != null ? num.intValue() : this.mStartingChannelImportance).intValue() - this.mStartingChannelImportance);
    }

    private LogMaker notificationControlsLogMaker() {
        return getLogMaker().setCategory(204).setType(1).setSubtype(0);
    }
}
