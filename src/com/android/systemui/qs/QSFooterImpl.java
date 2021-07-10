package com.android.systemui.qs;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserManager;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.internal.logging.MetricsLogger;
import com.android.keyguard.CarrierText;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.settingslib.development.DevelopmentSettingsEnabler;
import com.android.systemui.C0004R$color;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0008R$id;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.qs.TouchAnimator;
import com.android.systemui.statusbar.phone.MultiUserSwitch;
import com.android.systemui.statusbar.phone.SettingsButton;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.oneplus.systemui.util.OpMdmLogger;
import com.oneplus.util.OpUtils;
import com.oneplus.util.ThemeColorUtils;
public class QSFooterImpl extends FrameLayout implements QSFooter, View.OnClickListener, UserInfoController.OnUserInfoChangedListener {
    private final ActivityStarter mActivityStarter;
    private CarrierText mCarrierText;
    private final ContentObserver mDeveloperSettingsObserver;
    private final DeviceProvisionedController mDeviceProvisionedController;
    private View mDivider;
    protected View mEdit;
    private View.OnClickListener mExpandClickListener;
    private boolean mExpanded;
    private float mExpansionAmount;
    protected TouchAnimator mFooterAnimator;
    private boolean mIsGuestUser;
    private boolean mListening;
    private ImageView mMultiUserAvatar;
    protected MultiUserSwitch mMultiUserSwitch;
    private boolean mQsDisabled;
    private QSPanel mQsPanel;
    private SettingsButton mSettingsButton;
    private TouchAnimator mSettingsCogAnimator;
    protected View mSettingsContainer;
    private final UserInfoController mUserInfoController;

    static /* synthetic */ void lambda$onClick$4() {
    }

    public QSFooterImpl(Context context, AttributeSet attributeSet, ActivityStarter activityStarter, UserInfoController userInfoController, DeviceProvisionedController deviceProvisionedController) {
        super(context, attributeSet);
        this.mDeveloperSettingsObserver = new ContentObserver(new Handler(((FrameLayout) this).mContext.getMainLooper())) { // from class: com.android.systemui.qs.QSFooterImpl.1
            @Override // android.database.ContentObserver
            public void onChange(boolean z, Uri uri) {
                super.onChange(z, uri);
                QSFooterImpl.this.setBuildText();
            }
        };
        this.mActivityStarter = activityStarter;
        this.mUserInfoController = userInfoController;
        this.mDeviceProvisionedController = deviceProvisionedController;
    }

    public QSFooterImpl(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, (ActivityStarter) Dependency.get(ActivityStarter.class), (UserInfoController) Dependency.get(UserInfoController.class), (DeviceProvisionedController) Dependency.get(DeviceProvisionedController.class));
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mEdit = findViewById(16908291);
        if (canShowEditIcon()) {
            this.mEdit.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.qs.-$$Lambda$QSFooterImpl$3QBg0cgvu2IRpUDq3RvpL257x8c
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    QSFooterImpl.this.lambda$onFinishInflate$1$QSFooterImpl(view);
                }
            });
            this.mEdit.setVisibility(0);
        } else {
            this.mEdit.setVisibility(8);
        }
        this.mDivider = findViewById(C0008R$id.qs_footer_divider);
        PageIndicator pageIndicator = (PageIndicator) findViewById(C0008R$id.footer_page_indicator);
        this.mSettingsButton = (SettingsButton) findViewById(C0008R$id.settings_button);
        this.mSettingsContainer = findViewById(C0008R$id.settings_button_container);
        this.mSettingsButton.setOnClickListener(this);
        this.mSettingsContainer.setOnClickListener(this);
        MultiUserSwitch multiUserSwitch = (MultiUserSwitch) findViewById(C0008R$id.multi_user_switch);
        this.mMultiUserSwitch = multiUserSwitch;
        this.mMultiUserAvatar = (ImageView) multiUserSwitch.findViewById(C0008R$id.multi_user_avatar);
        this.mCarrierText = (CarrierText) findViewById(C0008R$id.qs_carrier_group_text);
        findViewById(C0008R$id.qs_footer_actions_container);
        ((RippleDrawable) this.mSettingsButton.getBackground()).setForceSoftware(true);
        updateResources();
        addOnLayoutChangeListener(new View.OnLayoutChangeListener() { // from class: com.android.systemui.qs.-$$Lambda$QSFooterImpl$GSAG9gEF755NpvH4khVvAa75uPs
            @Override // android.view.View.OnLayoutChangeListener
            public final void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                QSFooterImpl.this.lambda$onFinishInflate$2$QSFooterImpl(view, i, i2, i3, i4, i5, i6, i7, i8);
            }
        });
        setImportantForAccessibility(1);
        updateThemeColor();
        updateEverything();
        setBuildText();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onFinishInflate$1 */
    public /* synthetic */ void lambda$onFinishInflate$1$QSFooterImpl(View view) {
        this.mActivityStarter.postQSRunnableDismissingKeyguard(new Runnable(view) { // from class: com.android.systemui.qs.-$$Lambda$QSFooterImpl$BPGtDaa2eU-tTCTVDpjGrKOXYOs
            public final /* synthetic */ View f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                QSFooterImpl.this.lambda$onFinishInflate$0$QSFooterImpl(this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onFinishInflate$0 */
    public /* synthetic */ void lambda$onFinishInflate$0$QSFooterImpl(View view) {
        if (this.mQsPanel.getVisibility() == 0) {
            this.mQsPanel.showEdit(view);
        } else {
            Log.i("QSFooterImpl", "Don't show Editor when mQsPanel hide");
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onFinishInflate$2 */
    public /* synthetic */ void lambda$onFinishInflate$2$QSFooterImpl(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        updateAnimator(i3 - i);
    }

    private void updateThemeColor() {
        Context context;
        int color = ThemeColorUtils.getColor(10);
        if (OpUtils.isREDVersion() && (context = ((FrameLayout) this).mContext) != null) {
            color = context.getColor(C0004R$color.op_turquoise);
        }
        this.mSettingsButton.setImageTintList(ColorStateList.valueOf(color));
        ((ImageView) this.mEdit).setImageTintList(ColorStateList.valueOf(color));
        this.mDivider.setBackgroundColor(ThemeColorUtils.getColor(15));
        if (OpUtils.isMCLVersion() && ThemeColorUtils.getCurrentTheme() != 0) {
            setBackgroundResource(C0006R$drawable.op_qs_footer_background_my);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setBuildText() {
        TextView textView = (TextView) findViewById(C0008R$id.build);
        if (textView != null) {
            if (DevelopmentSettingsEnabler.isDevelopmentSettingsEnabled(((FrameLayout) this).mContext)) {
                textView.setText(((FrameLayout) this).mContext.getString(17039772, Build.VERSION.RELEASE_OR_CODENAME, Build.ID));
                textView.setVisibility(0);
                return;
            }
            textView.setVisibility(8);
        }
    }

    private void updateAnimator(int i) {
        setExpansion(this.mExpansionAmount);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        if (canShowEditIcon()) {
            this.mEdit.setVisibility(0);
        } else {
            this.mEdit.setVisibility(8);
        }
        updateResources();
        updateEverything();
    }

    @Override // android.view.View
    public void onRtlPropertiesChanged(int i) {
        super.onRtlPropertiesChanged(i);
        updateResources();
    }

    private void updateResources() {
        updateFooterAnimator();
    }

    private void updateFooterAnimator() {
        this.mFooterAnimator = createFooterAnimator();
    }

    private TouchAnimator createFooterAnimator() {
        TouchAnimator.Builder builder = new TouchAnimator.Builder();
        builder.setStartDelay(0.84f);
        if (canShowEditIcon()) {
            builder.addFloat(this.mEdit, "alpha", 0.0f, 1.0f);
        }
        builder.addFloat(this.mMultiUserSwitch, "alpha", 0.0f, 1.0f);
        return builder.build();
    }

    @Override // com.android.systemui.qs.QSFooter
    public void setKeyguardShowing(boolean z) {
        setExpansion(this.mExpansionAmount);
    }

    @Override // com.android.systemui.qs.QSFooter
    public void setExpandClickListener(View.OnClickListener onClickListener) {
        this.mExpandClickListener = onClickListener;
    }

    @Override // com.android.systemui.qs.QSFooter
    public void setExpanded(boolean z) {
        if (this.mExpanded != z) {
            this.mExpanded = z;
            this.mCarrierText.setExpanded(z);
            updateEverything();
        }
    }

    @Override // com.android.systemui.qs.QSFooter
    public void setExpansion(float f) {
        this.mExpansionAmount = f;
        TouchAnimator touchAnimator = this.mSettingsCogAnimator;
        if (touchAnimator != null) {
            touchAnimator.setPosition(f);
        }
        TouchAnimator touchAnimator2 = this.mFooterAnimator;
        if (touchAnimator2 != null) {
            touchAnimator2.setPosition(f);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((FrameLayout) this).mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("development_settings_enabled"), false, this.mDeveloperSettingsObserver, -1);
    }

    @Override // android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        setListening(false);
        ((FrameLayout) this).mContext.getContentResolver().unregisterContentObserver(this.mDeveloperSettingsObserver);
        super.onDetachedFromWindow();
    }

    @Override // com.android.systemui.qs.QSFooter
    public void setListening(boolean z) {
        if (z != this.mListening) {
            this.mListening = z;
            updateListeners();
            updateEverything();
        }
    }

    @Override // android.view.View
    public boolean performAccessibilityAction(int i, Bundle bundle) {
        View.OnClickListener onClickListener;
        if (i != 262144 || (onClickListener = this.mExpandClickListener) == null) {
            return super.performAccessibilityAction(i, bundle);
        }
        onClickListener.onClick(null);
        return true;
    }

    @Override // android.view.View
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        accessibilityNodeInfo.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_EXPAND);
    }

    @Override // com.android.systemui.qs.QSFooter
    public void disable(int i, int i2, boolean z) {
        boolean z2 = true;
        if ((i2 & 1) == 0) {
            z2 = false;
        }
        if (z2 != this.mQsDisabled) {
            this.mQsDisabled = z2;
            updateEverything();
        }
    }

    public void updateEverything() {
        post(new Runnable() { // from class: com.android.systemui.qs.-$$Lambda$QSFooterImpl$FK1In3z-Y3ppRrcllMggnruYa_s
            @Override // java.lang.Runnable
            public final void run() {
                QSFooterImpl.this.lambda$updateEverything$3$QSFooterImpl();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$updateEverything$3 */
    public /* synthetic */ void lambda$updateEverything$3$QSFooterImpl() {
        updateVisibilities();
        updateClickabilities();
        setClickable(false);
    }

    private void updateClickabilities() {
        MultiUserSwitch multiUserSwitch = this.mMultiUserSwitch;
        boolean z = true;
        multiUserSwitch.setClickable(multiUserSwitch.getVisibility() == 0);
        View view = this.mEdit;
        view.setClickable(view.getVisibility() == 0);
        SettingsButton settingsButton = this.mSettingsButton;
        if (settingsButton.getVisibility() != 0) {
            z = false;
        }
        settingsButton.setClickable(z);
    }

    private void updateVisibilities() {
        int i = 0;
        this.mSettingsContainer.setVisibility(this.mQsDisabled ? 8 : 0);
        if (canShowEditIcon()) {
            this.mEdit.setVisibility(!this.mExpanded ? 4 : 0);
        } else {
            this.mEdit.setVisibility(8);
        }
        boolean isDeviceInDemoMode = UserManager.isDeviceInDemoMode(((FrameLayout) this).mContext);
        this.mMultiUserSwitch.setVisibility(showUserSwitcher() ? 0 : 4);
        SettingsButton settingsButton = this.mSettingsButton;
        if (isDeviceInDemoMode) {
            i = 4;
        }
        settingsButton.setVisibility(i);
    }

    private boolean showUserSwitcher() {
        boolean z = this.mExpanded && this.mMultiUserSwitch.isMultiUserEnabled();
        boolean isKeyguardDone = true ^ KeyguardUpdateMonitor.getInstance(((FrameLayout) this).mContext).isKeyguardDone();
        if (z && isKeyguardDone && !this.mMultiUserSwitch.hasMultipleUsers()) {
            z = false;
        }
        if (isLandscape()) {
            return false;
        }
        return z;
    }

    private void updateListeners() {
        if (this.mListening) {
            this.mUserInfoController.addCallback(this);
        } else {
            this.mUserInfoController.removeCallback(this);
        }
    }

    @Override // com.android.systemui.qs.QSFooter
    public void setQSPanel(QSPanel qSPanel) {
        this.mQsPanel = qSPanel;
        if (qSPanel != null) {
            this.mMultiUserSwitch.setQsPanel(qSPanel);
        }
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (view != this.mSettingsButton && view != this.mSettingsContainer) {
            return;
        }
        if (!this.mDeviceProvisionedController.isCurrentUserSetup()) {
            this.mActivityStarter.postQSRunnableDismissingKeyguard($$Lambda$QSFooterImpl$ORlOcuwnOcEc1bdhJcTagEFJfI4.INSTANCE);
            return;
        }
        MetricsLogger.action(((FrameLayout) this).mContext, this.mExpanded ? 406 : 490);
        OpMdmLogger.logQsPanel("click_settings");
        startSettingsActivity();
    }

    private void startSettingsActivity() {
        this.mActivityStarter.startActivity(new Intent("android.settings.SETTINGS"), true);
    }

    @Override // com.android.systemui.statusbar.policy.UserInfoController.OnUserInfoChangedListener
    public void onUserInfoChanged(String str, Drawable drawable, String str2) {
        this.mIsGuestUser = UserManager.get(((FrameLayout) this).mContext).isGuestUser(ActivityManager.getCurrentUser());
        updateResources();
        updateEverything();
        this.mMultiUserAvatar.setImageDrawable(drawable);
    }

    private boolean canShowEditIcon() {
        return !isLandscape() && !this.mIsGuestUser;
    }

    private boolean isLandscape() {
        return ((FrameLayout) this).mContext.getResources().getConfiguration().orientation == 2;
    }
}
