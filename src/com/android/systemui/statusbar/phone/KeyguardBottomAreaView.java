package com.android.systemui.statusbar.phone;

import android.app.ActivityOptions;
import android.app.ActivityTaskManager;
import android.app.IApplicationThread;
import android.app.ProfilerInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.UserHandle;
import android.telecom.TelecomManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.EmergencyCarrierArea;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.ActivityIntentHelper;
import com.android.systemui.C0003R$bool;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0015R$string;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.doze.util.BurnInHelperKt;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.IntentButtonProvider;
import com.android.systemui.recents.OverviewProxyService;
import com.android.systemui.shared.system.QuickStepContract;
import com.android.systemui.statusbar.KeyguardAffordanceView;
import com.android.systemui.statusbar.policy.AccessibilityController;
import com.android.systemui.statusbar.policy.ExtensionController;
import com.android.systemui.statusbar.policy.FlashlightController;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.statusbar.policy.PreviewInflater;
import com.android.systemui.tuner.LockscreenFragment;
import com.android.systemui.tuner.TunerService;
import com.oneplus.core.oimc.OIMCServiceManager;
import com.oneplus.systemui.util.OpMdmLogger;
import com.oneplus.util.OpUtils;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.oneplus.odm.OpDeviceManagerInjector;
public class KeyguardBottomAreaView extends FrameLayout implements View.OnClickListener, KeyguardStateController.Callback, AccessibilityController.AccessibilityStateChangedCallback {
    public static final Intent INSECURE_CAMERA_INTENT = new Intent("android.media.action.STILL_IMAGE_CAMERA");
    private static final Intent PHONE_INTENT = new Intent("android.intent.action.DIAL");
    private static final Intent SECURE_CAMERA_INTENT = new Intent("android.media.action.STILL_IMAGE_CAMERA_SECURE").addFlags(8388608);
    private AccessibilityController mAccessibilityController;
    private View.AccessibilityDelegate mAccessibilityDelegate;
    private ActivityIntentHelper mActivityIntentHelper;
    private ActivityStarter mActivityStarter;
    private KeyguardAffordanceHelper mAffordanceHelper;
    private int mBurnInXOffset;
    private int mBurnInYOffset;
    private View mCameraPreview;
    private float mDarkAmount;
    private final BroadcastReceiver mDevicePolicyReceiver;
    protected Display mDisplay;
    private final DisplayMetrics mDisplayMetrics;
    private boolean mDozing;
    private EmergencyCarrierArea mEmergencyCarrierArea;
    private TextView mEnterpriseDisclosure;
    private Handler mHandler;
    private ViewGroup mIndicationArea;
    private int mIndicationBottomMargin;
    private TextView mIndicationText;
    private KeyguardStateController mKeyguardStateController;
    private KeyguardAffordanceView mLeftAffordanceView;
    private Drawable mLeftAssistIcon;
    private IntentButtonProvider.IntentButton mLeftButton;
    private String mLeftButtonStr;
    private ExtensionController.Extension<IntentButtonProvider.IntentButton> mLeftExtension;
    private boolean mLeftIsVoiceAssist;
    private View mLeftPreview;
    private boolean mNeedShowOTAWizard;
    private final OIMCServiceManager mOIMCServiceManager;
    private ViewGroup mOverlayContainer;
    private final OverviewProxyService mOverviewProxyService;
    private ViewGroup mPreviewContainer;
    private PreviewInflater mPreviewInflater;
    private boolean mPrewarmBound;
    private final ServiceConnection mPrewarmConnection;
    private Messenger mPrewarmMessenger;
    private Point mRealDisplaySize;
    private KeyguardAffordanceView mRightAffordanceView;
    private IntentButtonProvider.IntentButton mRightButton;
    private String mRightButtonStr;
    private ExtensionController.Extension<IntentButtonProvider.IntentButton> mRightExtension;
    private final boolean mShowCameraAffordance;
    private StatusBar mStatusBar;
    private final KeyguardUpdateMonitorCallback mUpdateMonitorCallback;
    private boolean mUserSetupComplete;
    protected WindowManager mWindowManager;

    /* access modifiers changed from: private */
    public static boolean isSuccessfulLaunch(int i) {
        return i == 0 || i == 3 || i == 2;
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    public KeyguardBottomAreaView(Context context) {
        this(context, null);
    }

    public KeyguardBottomAreaView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public KeyguardBottomAreaView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public KeyguardBottomAreaView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mPrewarmConnection = new ServiceConnection() { // from class: com.android.systemui.statusbar.phone.KeyguardBottomAreaView.1
            @Override // android.content.ServiceConnection
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                KeyguardBottomAreaView.this.mPrewarmMessenger = new Messenger(iBinder);
            }

            @Override // android.content.ServiceConnection
            public void onServiceDisconnected(ComponentName componentName) {
                KeyguardBottomAreaView.this.mPrewarmMessenger = null;
            }
        };
        this.mRightButton = new DefaultRightButton();
        this.mLeftButton = new DefaultLeftButton();
        this.mHandler = null;
        this.mDisplayMetrics = (DisplayMetrics) Dependency.get(DisplayMetrics.class);
        this.mRealDisplaySize = new Point();
        this.mAccessibilityDelegate = new View.AccessibilityDelegate() { // from class: com.android.systemui.statusbar.phone.KeyguardBottomAreaView.2
            @Override // android.view.View.AccessibilityDelegate
            public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfo accessibilityNodeInfo) {
                String str;
                super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfo);
                if (view == KeyguardBottomAreaView.this.mRightAffordanceView) {
                    str = KeyguardBottomAreaView.this.getResources().getString(C0015R$string.camera_label);
                } else if (view == KeyguardBottomAreaView.this.mLeftAffordanceView) {
                    str = KeyguardBottomAreaView.this.mLeftIsVoiceAssist ? KeyguardBottomAreaView.this.getResources().getString(C0015R$string.voice_assist_label) : KeyguardBottomAreaView.this.getResources().getString(C0015R$string.phone_label);
                } else {
                    str = null;
                }
                accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(16, str));
            }

            @Override // android.view.View.AccessibilityDelegate
            public boolean performAccessibilityAction(View view, int i3, Bundle bundle) {
                if (i3 == 16) {
                    if (view == KeyguardBottomAreaView.this.mRightAffordanceView) {
                        KeyguardBottomAreaView.this.launchCamera("lockscreen_affordance");
                        return true;
                    } else if (view == KeyguardBottomAreaView.this.mLeftAffordanceView) {
                        KeyguardBottomAreaView.this.launchLeftAffordance();
                        return true;
                    }
                }
                return super.performAccessibilityAction(view, i3, bundle);
            }
        };
        this.mDevicePolicyReceiver = new BroadcastReceiver() { // from class: com.android.systemui.statusbar.phone.KeyguardBottomAreaView.11
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                KeyguardBottomAreaView.this.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.KeyguardBottomAreaView.11.1
                    @Override // java.lang.Runnable
                    public void run() {
                        KeyguardBottomAreaView.this.updateCameraVisibility();
                    }
                });
            }
        };
        this.mUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() { // from class: com.android.systemui.statusbar.phone.KeyguardBottomAreaView.12
            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onUserSwitchComplete(int i3) {
                KeyguardBottomAreaView.this.updateCameraVisibility();
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onUserUnlocked() {
                KeyguardBottomAreaView.this.inflateCameraPreview();
                KeyguardBottomAreaView.this.updateCameraVisibility();
                KeyguardBottomAreaView.this.updateLeftAffordance();
            }
        };
        this.mNeedShowOTAWizard = false;
        getResources().getBoolean(C0003R$bool.oneplus_config_keyguardShowLeftAffordance);
        this.mShowCameraAffordance = getResources().getBoolean(C0003R$bool.oneplus_config_keyguardShowCameraAffordance);
        initHandler();
        WindowManager windowManager = (WindowManager) ((FrameLayout) this).mContext.getSystemService("window");
        this.mWindowManager = windowManager;
        this.mDisplay = windowManager.getDefaultDisplay();
        this.mOIMCServiceManager = new OIMCServiceManager();
        this.mOverviewProxyService = (OverviewProxyService) Dependency.get(OverviewProxyService.class);
    }

    public void initFrom(KeyguardBottomAreaView keyguardBottomAreaView) {
        setStatusBar(keyguardBottomAreaView.mStatusBar);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mPreviewInflater = new PreviewInflater(((FrameLayout) this).mContext, new LockPatternUtils(((FrameLayout) this).mContext), new ActivityIntentHelper(((FrameLayout) this).mContext));
        this.mPreviewContainer = (ViewGroup) findViewById(C0008R$id.preview_container);
        this.mEmergencyCarrierArea = (EmergencyCarrierArea) findViewById(C0008R$id.keyguard_selector_fade_container);
        this.mOverlayContainer = (ViewGroup) findViewById(C0008R$id.overlay_container);
        this.mRightAffordanceView = (KeyguardAffordanceView) findViewById(C0008R$id.camera_button);
        this.mLeftAffordanceView = (KeyguardAffordanceView) findViewById(C0008R$id.left_button);
        this.mIndicationArea = (ViewGroup) findViewById(C0008R$id.keyguard_indication_area);
        this.mEnterpriseDisclosure = (TextView) findViewById(C0008R$id.keyguard_indication_enterprise_disclosure);
        this.mIndicationText = (TextView) findViewById(C0008R$id.keyguard_indication_text);
        calculateIndicationBottomMargin();
        this.mBurnInYOffset = getResources().getDimensionPixelSize(C0005R$dimen.default_burn_in_prevention_offset);
        ImageView imageView = (ImageView) findViewById(C0008R$id.charging_dash);
        KeyguardStateController keyguardStateController = (KeyguardStateController) Dependency.get(KeyguardStateController.class);
        this.mKeyguardStateController = keyguardStateController;
        keyguardStateController.addCallback(this);
        updateCameraVisibility();
        setClipChildren(false);
        setClipToPadding(false);
        inflateCameraPreview();
        this.mRightAffordanceView.setOnClickListener(this);
        this.mLeftAffordanceView.setOnClickListener(this);
        initAccessibility();
        this.mActivityStarter = (ActivityStarter) Dependency.get(ActivityStarter.class);
        FlashlightController flashlightController = (FlashlightController) Dependency.get(FlashlightController.class);
        this.mAccessibilityController = (AccessibilityController) Dependency.get(AccessibilityController.class);
        this.mActivityIntentHelper = new ActivityIntentHelper(getContext());
        updateLeftAffordance();
        PHONE_INTENT.addCategory("android.intent.category.DEFAULT");
        setLayoutDirection(getResources().getConfiguration().getLayoutDirection());
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mAccessibilityController.addStateChangedCallback(this);
        ExtensionController.ExtensionBuilder newExtension = ((ExtensionController) Dependency.get(ExtensionController.class)).newExtension(IntentButtonProvider.IntentButton.class);
        newExtension.withPlugin(IntentButtonProvider.class, "com.android.systemui.action.PLUGIN_LOCKSCREEN_RIGHT_BUTTON", $$Lambda$KeyguardBottomAreaView$g4KaNPI9kzVsHrOlMYmA_f9J2Y.INSTANCE);
        newExtension.withTunerFactory(new LockscreenFragment.LockButtonFactory(((FrameLayout) this).mContext, "sysui_keyguard_right"));
        newExtension.withDefault(new Supplier() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$KeyguardBottomAreaView$41MKD52m3LHIf9RRtKFf6LfUif0
            @Override // java.util.function.Supplier
            public final Object get() {
                return KeyguardBottomAreaView.this.lambda$onAttachedToWindow$1$KeyguardBottomAreaView();
            }
        });
        newExtension.withCallback(new Consumer() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$KeyguardBottomAreaView$Z_R5g5wpXUcfPYLHCfZHekG4xK0
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                KeyguardBottomAreaView.this.lambda$onAttachedToWindow$2$KeyguardBottomAreaView((IntentButtonProvider.IntentButton) obj);
            }
        });
        this.mRightExtension = newExtension.build();
        ExtensionController.ExtensionBuilder newExtension2 = ((ExtensionController) Dependency.get(ExtensionController.class)).newExtension(IntentButtonProvider.IntentButton.class);
        newExtension2.withPlugin(IntentButtonProvider.class, "com.android.systemui.action.PLUGIN_LOCKSCREEN_LEFT_BUTTON", $$Lambda$KeyguardBottomAreaView$Eh9_ou4HbbT4H4ZFilpDDtanY4k.INSTANCE);
        newExtension2.withTunerFactory(new LockscreenFragment.LockButtonFactory(((FrameLayout) this).mContext, "sysui_keyguard_left"));
        newExtension2.withDefault(new Supplier() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$KeyguardBottomAreaView$W-hTEBW5YZVW2MsKtz0LzBCynHY
            @Override // java.util.function.Supplier
            public final Object get() {
                return KeyguardBottomAreaView.this.lambda$onAttachedToWindow$4$KeyguardBottomAreaView();
            }
        });
        newExtension2.withCallback(new Consumer() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$KeyguardBottomAreaView$owXxFBBnubMOAUdfyf5a48bf-Zo
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                KeyguardBottomAreaView.this.lambda$onAttachedToWindow$5$KeyguardBottomAreaView((IntentButtonProvider.IntentButton) obj);
            }
        });
        this.mLeftExtension = newExtension2.build();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED");
        getContext().registerReceiverAsUser(this.mDevicePolicyReceiver, UserHandle.ALL, intentFilter, null, null);
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).registerCallback(this.mUpdateMonitorCallback);
        this.mKeyguardStateController.addCallback(this);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onAttachedToWindow$1 */
    public /* synthetic */ IntentButtonProvider.IntentButton lambda$onAttachedToWindow$1$KeyguardBottomAreaView() {
        return new DefaultRightButton();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onAttachedToWindow$4 */
    public /* synthetic */ IntentButtonProvider.IntentButton lambda$onAttachedToWindow$4$KeyguardBottomAreaView() {
        return new DefaultLeftButton();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mKeyguardStateController.removeCallback(this);
        this.mAccessibilityController.removeStateChangedCallback(this);
        this.mRightExtension.destroy();
        this.mLeftExtension.destroy();
        getContext().unregisterReceiver(this.mDevicePolicyReceiver);
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).removeCallback(this.mUpdateMonitorCallback);
    }

    private void initAccessibility() {
        this.mLeftAffordanceView.setAccessibilityDelegate(this.mAccessibilityDelegate);
        this.mRightAffordanceView.setAccessibilityDelegate(this.mAccessibilityDelegate);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        updateIndicationArea();
        this.mBurnInYOffset = getResources().getDimensionPixelSize(C0005R$dimen.default_burn_in_prevention_offset);
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) this.mIndicationArea.getLayoutParams();
        int i = marginLayoutParams.bottomMargin;
        int i2 = this.mIndicationBottomMargin;
        if (i != i2) {
            marginLayoutParams.bottomMargin = i2;
            this.mIndicationArea.setLayoutParams(marginLayoutParams);
        }
        this.mEnterpriseDisclosure.setTextSize(0, (float) getResources().getDimensionPixelSize(17105509));
        this.mIndicationText.setTextSize(0, (float) getResources().getDimensionPixelSize(C0005R$dimen.oneplus_contorl_text_size_body1));
        ViewGroup.LayoutParams layoutParams = this.mRightAffordanceView.getLayoutParams();
        layoutParams.width = getResources().getDimensionPixelSize(C0005R$dimen.keyguard_affordance_width);
        layoutParams.height = getResources().getDimensionPixelSize(C0005R$dimen.keyguard_affordance_height);
        this.mRightAffordanceView.setLayoutParams(layoutParams);
        updateRightAffordanceIcon();
        ViewGroup.LayoutParams layoutParams2 = this.mLeftAffordanceView.getLayoutParams();
        layoutParams2.width = getResources().getDimensionPixelSize(C0005R$dimen.keyguard_affordance_width);
        layoutParams2.height = getResources().getDimensionPixelSize(C0005R$dimen.keyguard_affordance_height);
        this.mLeftAffordanceView.setLayoutParams(layoutParams2);
        updateLeftAffordanceIcon();
    }

    private void calculateIndicationBottomMargin() {
        if (OpUtils.isCustomFingerprint()) {
            boolean isUnlockWithFingerprintPossible = KeyguardUpdateMonitor.getInstance(((FrameLayout) this).mContext).isUnlockWithFingerprintPossible(KeyguardUpdateMonitor.getCurrentUser());
            this.mDisplay.getMetrics(this.mDisplayMetrics);
            this.mDisplay.getRealSize(this.mRealDisplaySize);
            int i = this.mRealDisplaySize.y;
            boolean z = this.mDisplayMetrics.widthPixels == 1440;
            int dimensionPixelSize = ((FrameLayout) this).mContext.getResources().getDimensionPixelSize(z ? C0005R$dimen.op_biometric_icon_normal_location_y_2k : C0005R$dimen.op_biometric_icon_normal_location_y_1080p);
            if (OpUtils.isCutoutHide(((FrameLayout) this).mContext)) {
                dimensionPixelSize -= OpUtils.getCutoutPathdataHeight(((FrameLayout) this).mContext);
            }
            int dimensionPixelSize2 = (i - dimensionPixelSize) + ((FrameLayout) this).mContext.getResources().getDimensionPixelSize(z ? C0005R$dimen.op_keyguard_indication_padding_bottom_2k : C0005R$dimen.op_keyguard_indication_padding_bottom_1080p);
            if (!isUnlockWithFingerprintPossible) {
                dimensionPixelSize2 = getResources().getDimensionPixelSize(C0005R$dimen.margin_top1) + getResources().getDimensionPixelSize(C0005R$dimen.keyguard_affordance_height);
            }
            this.mIndicationBottomMargin = dimensionPixelSize2;
            return;
        }
        this.mIndicationBottomMargin = getResources().getDimensionPixelSize(C0005R$dimen.keyguard_indication_margin_bottom);
    }

    public void updateIndicationArea() {
        calculateIndicationBottomMargin();
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) this.mIndicationArea.getLayoutParams();
        int i = marginLayoutParams.bottomMargin;
        int i2 = this.mIndicationBottomMargin;
        if (i != i2) {
            marginLayoutParams.bottomMargin = i2;
            this.mIndicationArea.setLayoutParams(marginLayoutParams);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0080, code lost:
        if (r2 > 0) goto L_0x0082;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x008e, code lost:
        if (r2 > 0) goto L_0x0082;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateRightAffordanceIcon() {
        /*
            r7 = this;
            com.android.systemui.plugins.IntentButtonProvider$IntentButton r0 = r7.mRightButton
            com.android.systemui.plugins.IntentButtonProvider$IntentButton$IconState r0 = r0.getIcon()
            com.android.systemui.statusbar.KeyguardAffordanceView r1 = r7.mRightAffordanceView
            boolean r2 = r7.mDozing
            r3 = 0
            if (r2 != 0) goto L_0x0013
            boolean r2 = r0.isVisible
            if (r2 == 0) goto L_0x0013
            r2 = r3
            goto L_0x0015
        L_0x0013:
            r2 = 8
        L_0x0015:
            r1.setVisibility(r2)
            android.graphics.drawable.Drawable r1 = r0.drawable
            com.android.systemui.statusbar.KeyguardAffordanceView r2 = r7.mRightAffordanceView
            android.graphics.drawable.Drawable r2 = r2.getDrawable()
            if (r1 != r2) goto L_0x002c
            boolean r1 = r0.tint
            com.android.systemui.statusbar.KeyguardAffordanceView r2 = r7.mRightAffordanceView
            boolean r2 = r2.shouldTint()
            if (r1 == r2) goto L_0x0035
        L_0x002c:
            com.android.systemui.statusbar.KeyguardAffordanceView r1 = r7.mRightAffordanceView
            android.graphics.drawable.Drawable r2 = r0.drawable
            boolean r4 = r0.tint
            r1.setImageDrawable(r2, r4)
        L_0x0035:
            com.android.systemui.statusbar.KeyguardAffordanceView r1 = r7.mRightAffordanceView
            java.lang.CharSequence r2 = r0.contentDescription
            r1.setContentDescription(r2)
            boolean r1 = com.oneplus.util.OpUtils.isCustomFingerprint()
            if (r1 == 0) goto L_0x0096
            android.content.res.Resources r1 = r7.getResources()
            int r2 = com.android.systemui.C0005R$dimen.op_keyguard_affordance_view_padding
            int r1 = r1.getDimensionPixelSize(r2)
            com.android.systemui.statusbar.KeyguardAffordanceView r2 = r7.mRightAffordanceView
            r2.setPaddingRelative(r3, r3, r1, r1)
            android.content.res.Resources r2 = r7.getResources()
            int r4 = com.android.systemui.C0005R$dimen.keyguard_affordance_height
            int r2 = r2.getDimensionPixelSize(r4)
            android.content.res.Resources r4 = r7.getResources()
            int r5 = com.android.systemui.C0005R$dimen.keyguard_affordance_width
            int r4 = r4.getDimensionPixelSize(r5)
            android.graphics.drawable.Drawable r0 = r0.drawable
            if (r0 == 0) goto L_0x006e
            int r0 = r0.getIntrinsicWidth()
            goto L_0x006f
        L_0x006e:
            r0 = r3
        L_0x006f:
            int r5 = r7.getLayoutDirection()
            r6 = 1
            if (r5 != r6) goto L_0x0077
            goto L_0x0078
        L_0x0077:
            r6 = r3
        L_0x0078:
            if (r6 == 0) goto L_0x0084
            int r0 = r0 / 2
            int r4 = r1 + r0
            int r2 = r2 - r1
            int r2 = r2 - r0
            if (r2 <= 0) goto L_0x0091
        L_0x0082:
            r3 = r2
            goto L_0x0091
        L_0x0084:
            int r4 = r4 - r1
            int r0 = r0 / 2
            int r4 = r4 - r0
            if (r4 <= 0) goto L_0x008b
            goto L_0x008c
        L_0x008b:
            r4 = r3
        L_0x008c:
            int r2 = r2 - r1
            int r2 = r2 - r0
            if (r2 <= 0) goto L_0x0091
            goto L_0x0082
        L_0x0091:
            com.android.systemui.statusbar.KeyguardAffordanceView r7 = r7.mRightAffordanceView
            r7.setCenter(r4, r3)
        L_0x0096:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.KeyguardBottomAreaView.updateRightAffordanceIcon():void");
    }

    public void setStatusBar(StatusBar statusBar) {
        this.mStatusBar = statusBar;
        updateCameraVisibility();
    }

    public void setAffordanceHelper(KeyguardAffordanceHelper keyguardAffordanceHelper) {
        this.mAffordanceHelper = keyguardAffordanceHelper;
    }

    public void setUserSetupComplete(boolean z) {
        this.mUserSetupComplete = z;
        updateCameraVisibility();
        updateLeftAffordanceIcon();
    }

    private Intent getCameraIntent() {
        return this.mRightButton.getIntent();
    }

    public ResolveInfo resolveCameraIntent() {
        return ((FrameLayout) this).mContext.getPackageManager().resolveActivityAsUser(getCameraIntent(), 65536, KeyguardUpdateMonitor.getCurrentUser());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateCameraVisibility() {
        KeyguardAffordanceView keyguardAffordanceView = this.mRightAffordanceView;
        if (keyguardAffordanceView != null) {
            keyguardAffordanceView.setVisibility((this.mDozing || !this.mShowCameraAffordance || !this.mRightButton.getIcon().isVisible) ? 8 : 0);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0080, code lost:
        if (r2 > 0) goto L_0x0082;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x008a, code lost:
        if (r2 > 0) goto L_0x0082;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateLeftAffordanceIcon() {
        /*
            r7 = this;
            com.android.systemui.plugins.IntentButtonProvider$IntentButton r0 = r7.mLeftButton
            com.android.systemui.plugins.IntentButtonProvider$IntentButton$IconState r0 = r0.getIcon()
            com.android.systemui.statusbar.KeyguardAffordanceView r1 = r7.mLeftAffordanceView
            boolean r2 = r0.isVisible
            r3 = 0
            if (r2 == 0) goto L_0x000f
            r2 = r3
            goto L_0x0011
        L_0x000f:
            r2 = 8
        L_0x0011:
            r1.setVisibility(r2)
            android.graphics.drawable.Drawable r1 = r0.drawable
            com.android.systemui.statusbar.KeyguardAffordanceView r2 = r7.mLeftAffordanceView
            android.graphics.drawable.Drawable r2 = r2.getDrawable()
            if (r1 != r2) goto L_0x0028
            boolean r1 = r0.tint
            com.android.systemui.statusbar.KeyguardAffordanceView r2 = r7.mLeftAffordanceView
            boolean r2 = r2.shouldTint()
            if (r1 == r2) goto L_0x0031
        L_0x0028:
            com.android.systemui.statusbar.KeyguardAffordanceView r1 = r7.mLeftAffordanceView
            android.graphics.drawable.Drawable r2 = r0.drawable
            boolean r4 = r0.tint
            r1.setImageDrawable(r2, r4)
        L_0x0031:
            com.android.systemui.statusbar.KeyguardAffordanceView r1 = r7.mLeftAffordanceView
            java.lang.CharSequence r2 = r0.contentDescription
            r1.setContentDescription(r2)
            boolean r1 = com.oneplus.util.OpUtils.isCustomFingerprint()
            if (r1 == 0) goto L_0x0092
            android.content.res.Resources r1 = r7.getResources()
            int r2 = com.android.systemui.C0005R$dimen.op_keyguard_affordance_view_padding
            int r1 = r1.getDimensionPixelSize(r2)
            com.android.systemui.statusbar.KeyguardAffordanceView r2 = r7.mLeftAffordanceView
            r2.setPaddingRelative(r1, r3, r3, r1)
            android.content.res.Resources r2 = r7.getResources()
            int r4 = com.android.systemui.C0005R$dimen.keyguard_affordance_height
            int r2 = r2.getDimensionPixelSize(r4)
            android.content.res.Resources r4 = r7.getResources()
            int r5 = com.android.systemui.C0005R$dimen.keyguard_affordance_width
            int r4 = r4.getDimensionPixelSize(r5)
            android.graphics.drawable.Drawable r0 = r0.drawable
            if (r0 == 0) goto L_0x006a
            int r0 = r0.getIntrinsicWidth()
            goto L_0x006b
        L_0x006a:
            r0 = r3
        L_0x006b:
            int r5 = r7.getLayoutDirection()
            r6 = 1
            if (r5 != r6) goto L_0x0073
            goto L_0x0074
        L_0x0073:
            r6 = r3
        L_0x0074:
            if (r6 == 0) goto L_0x0084
            int r4 = r4 - r1
            int r0 = r0 / 2
            int r4 = r4 - r0
            if (r4 <= 0) goto L_0x007d
            goto L_0x007e
        L_0x007d:
            r4 = r3
        L_0x007e:
            int r2 = r2 - r1
            int r2 = r2 - r0
            if (r2 <= 0) goto L_0x008d
        L_0x0082:
            r3 = r2
            goto L_0x008d
        L_0x0084:
            int r0 = r0 / 2
            int r4 = r1 + r0
            int r2 = r2 - r1
            int r2 = r2 - r0
            if (r2 <= 0) goto L_0x008d
            goto L_0x0082
        L_0x008d:
            com.android.systemui.statusbar.KeyguardAffordanceView r7 = r7.mLeftAffordanceView
            r7.setCenter(r4, r3)
        L_0x0092:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.KeyguardBottomAreaView.updateLeftAffordanceIcon():void");
    }

    public boolean isLeftVoiceAssist() {
        return this.mLeftIsVoiceAssist;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isPhoneVisible() {
        PackageManager packageManager = ((FrameLayout) this).mContext.getPackageManager();
        if (!packageManager.hasSystemFeature("android.hardware.telephony") || packageManager.resolveActivity(PHONE_INTENT, 0) == null) {
            return false;
        }
        return true;
    }

    @Override // com.android.systemui.statusbar.policy.AccessibilityController.AccessibilityStateChangedCallback
    public void onStateChanged(boolean z, boolean z2) {
        this.mRightAffordanceView.setClickable(z2);
        this.mLeftAffordanceView.setClickable(z2);
        this.mRightAffordanceView.setFocusable(z);
        this.mLeftAffordanceView.setFocusable(z);
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (view == this.mRightAffordanceView) {
            launchCamera("lockscreen_affordance");
        } else if (view == this.mLeftAffordanceView) {
            launchLeftAffordance();
        }
    }

    public void bindCameraPrewarmService() {
        Bundle bundle;
        String string;
        ActivityInfo targetActivityInfo = this.mActivityIntentHelper.getTargetActivityInfo(getCameraIntent(), KeyguardUpdateMonitor.getCurrentUser(), true);
        if (targetActivityInfo != null && (bundle = targetActivityInfo.metaData) != null && (string = bundle.getString("android.media.still_image_camera_preview_service")) != null) {
            Intent intent = new Intent();
            intent.setClassName(targetActivityInfo.packageName, string);
            intent.setAction("android.service.media.CameraPrewarmService.ACTION_PREWARM");
            try {
                if (getContext().bindServiceAsUser(intent, this.mPrewarmConnection, 67108865, new UserHandle(-2))) {
                    this.mPrewarmBound = true;
                }
            } catch (SecurityException e) {
                Log.w("StatusBar/KeyguardBottomAreaView", "Unable to bind to prewarm service package=" + targetActivityInfo.packageName + " class=" + string, e);
            }
        }
    }

    public void unbindCameraPrewarmService(boolean z) {
        if (this.mPrewarmBound) {
            Messenger messenger = this.mPrewarmMessenger;
            if (messenger != null && z) {
                try {
                    messenger.send(Message.obtain((Handler) null, 1));
                } catch (RemoteException e) {
                    Log.w("StatusBar/KeyguardBottomAreaView", "Error sending camera fired message", e);
                }
            }
            ((FrameLayout) this).mContext.unbindService(this.mPrewarmConnection);
            this.mPrewarmBound = false;
        }
    }

    public void launchCamera(String str) {
        launchOpCamera(str);
    }

    private void launchOpCamera(String str) {
        final Intent doubleTapPowerOpAppIntent;
        if (str != "power_double_tap" || (doubleTapPowerOpAppIntent = this.mStatusBar.getDoubleTapPowerOpAppIntent(1)) == null) {
            if (this.mStatusBar.notifyCameraLaunching(str, str == "power_double_tap")) {
                Log.d("StatusBar/KeyguardBottomAreaView", "pending launchCamera, " + str);
                return;
            }
            final Intent cameraIntent = getCameraIntent();
            Log.d("StatusBar/KeyguardBottomAreaView", "launchCamera, " + str + ", intent:" + cameraIntent);
            try {
                ((FrameLayout) this).mContext.getPackageManager().getPackageInfo("com.oneplus.camera", 1);
                if (cameraIntent == SECURE_CAMERA_INTENT) {
                    cameraIntent.setComponent(new ComponentName("com.oneplus.camera", "com.oneplus.camera.OPSecureCameraActivity"));
                } else {
                    cameraIntent.setComponent(new ComponentName("com.oneplus.camera", "com.oneplus.camera.OPCameraActivity"));
                }
            } catch (PackageManager.NameNotFoundException unused) {
                Log.i("StatusBar/KeyguardBottomAreaView", "no op camera");
                cameraIntent.setComponent(new ComponentName("com.android.camera2", "com.android.camera.CameraActivity"));
            }
            cameraIntent.putExtra("com.android.systemui.camera_launch_source", str);
            cameraIntent.putExtra("com.android.systemui.camera_launch_source_gesture", NotificationPanelViewController.mLastCameraGestureLaunchSource);
            NotificationPanelViewController.mLastCameraGestureLaunchSource = 0;
            boolean wouldLaunchResolverActivity = this.mActivityIntentHelper.wouldLaunchResolverActivity(cameraIntent, KeyguardUpdateMonitor.getCurrentUser());
            if (cameraIntent != SECURE_CAMERA_INTENT || wouldLaunchResolverActivity) {
                this.mActivityStarter.startActivity(cameraIntent, false, (ActivityStarter.Callback) new ActivityStarter.Callback() { // from class: com.android.systemui.statusbar.phone.KeyguardBottomAreaView.7
                    @Override // com.android.systemui.plugins.ActivityStarter.Callback
                    public void onActivityStarted(int i) {
                        KeyguardBottomAreaView.this.unbindCameraPrewarmService(KeyguardBottomAreaView.isSuccessfulLaunch(i));
                    }
                });
            } else {
                AsyncTask.execute(new Runnable() { // from class: com.android.systemui.statusbar.phone.KeyguardBottomAreaView.6
                    @Override // java.lang.Runnable
                    public void run() {
                        int i;
                        ActivityOptions makeBasic = ActivityOptions.makeBasic();
                        makeBasic.setDisallowEnterPictureInPictureWhileLaunching(true);
                        makeBasic.setRotationAnimationHint(3);
                        try {
                            i = ActivityTaskManager.getService().startActivityAsUser((IApplicationThread) null, KeyguardBottomAreaView.this.getContext().getBasePackageName(), KeyguardBottomAreaView.this.getContext().getAttributionTag(), cameraIntent, cameraIntent.resolveTypeIfNeeded(KeyguardBottomAreaView.this.getContext().getContentResolver()), (IBinder) null, (String) null, 0, 268435456, (ProfilerInfo) null, makeBasic.toBundle(), UserHandle.CURRENT.getIdentifier());
                        } catch (RemoteException e) {
                            Log.w("StatusBar/KeyguardBottomAreaView", "Unable to start camera activity", e);
                            i = -96;
                        }
                        final boolean isSuccessfulLaunch = KeyguardBottomAreaView.isSuccessfulLaunch(i);
                        KeyguardBottomAreaView.this.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.KeyguardBottomAreaView.6.1
                            @Override // java.lang.Runnable
                            public void run() {
                                KeyguardBottomAreaView.this.unbindCameraPrewarmService(isSuccessfulLaunch);
                            }
                        });
                    }
                });
            }
        } else {
            boolean booleanExtra = doubleTapPowerOpAppIntent.getBooleanExtra("isAlexa", false);
            Log.d("StatusBar/KeyguardBottomAreaView", "DoubleTapPower: launching " + doubleTapPowerOpAppIntent + ", " + booleanExtra);
            if (booleanExtra) {
                KeyguardUpdateMonitor.getInstance(((FrameLayout) this).mContext).updateLaunchingCameraState(false);
                this.mActivityStarter.startActivity(doubleTapPowerOpAppIntent, false);
                return;
            }
            AsyncTask.execute(new Runnable() { // from class: com.android.systemui.statusbar.phone.KeyguardBottomAreaView.5
                @Override // java.lang.Runnable
                public void run() {
                    int i;
                    ActivityOptions makeBasic = ActivityOptions.makeBasic();
                    makeBasic.setDisallowEnterPictureInPictureWhileLaunching(true);
                    makeBasic.setRotationAnimationHint(3);
                    try {
                        i = ActivityTaskManager.getService().startActivityAsUser((IApplicationThread) null, KeyguardBottomAreaView.this.getContext().getBasePackageName(), KeyguardBottomAreaView.this.getContext().getAttributionTag(), doubleTapPowerOpAppIntent, doubleTapPowerOpAppIntent.resolveTypeIfNeeded(KeyguardBottomAreaView.this.getContext().getContentResolver()), (IBinder) null, (String) null, 0, 268435456, (ProfilerInfo) null, makeBasic.toBundle(), UserHandle.CURRENT.getIdentifier());
                    } catch (RemoteException e) {
                        Log.w("StatusBar/KeyguardBottomAreaView", "DoubleTapPower: Unable to start activity", e);
                        i = -96;
                    }
                    Log.i("StatusBar/KeyguardBottomAreaView", "DoubleTapPower: launching " + doubleTapPowerOpAppIntent + " result=" + i);
                }
            });
        }
    }

    public void setDarkAmount(float f) {
        if (f != this.mDarkAmount) {
            this.mDarkAmount = f;
            dozeTimeTick();
        }
    }

    public void launchLeftAffordance() {
        if (this.mLeftIsVoiceAssist) {
            launchVoiceAssist();
            OpMdmLogger.log("lock_voice", "", "1");
            return;
        }
        launchPhone();
        OpMdmLogger.log("lock_phone", "", "1");
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void launchVoiceAssist() {
        AnonymousClass8 r1 = new Runnable(this) { // from class: com.android.systemui.statusbar.phone.KeyguardBottomAreaView.8
            @Override // java.lang.Runnable
            public void run() {
                ((AssistManager) Dependency.get(AssistManager.class)).launchVoiceAssistFromKeyguard();
                if (Build.DEBUG_ONEPLUS) {
                    Log.d("StatusBar/KeyguardBottomAreaView", "launchVoiceAssist run");
                }
            }
        };
        if (!this.mKeyguardStateController.canDismissLockScreen()) {
            if (Build.DEBUG_ONEPLUS) {
                Log.d("StatusBar/KeyguardBottomAreaView", "launchVoiceAssist start");
            }
            ((Executor) Dependency.get(Dependency.BACKGROUND_EXECUTOR)).execute(r1);
        } else {
            this.mStatusBar.executeRunnableDismissingKeyguard(r1, null, !TextUtils.isEmpty(this.mRightButtonStr) && ((TunerService) Dependency.get(TunerService.class)).getValue("sysui_keyguard_right_unlock", 1) != 0, false, true);
        }
        OverviewProxyService overviewProxyService = this.mOverviewProxyService;
        if (overviewProxyService != null && !QuickStepContract.isGesturalMode(overviewProxyService.getNavBarMode())) {
            getWindowInsetsController().show(WindowInsets.Type.navigationBars());
        }
        collectOpenAssistantEvent();
    }

    private void collectOpenAssistantEvent() {
        this.mHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.KeyguardBottomAreaView.9
            @Override // java.lang.Runnable
            public void run() {
                OpDeviceManagerInjector.getInstance().preserveAssistantData(((FrameLayout) KeyguardBottomAreaView.this).mContext, 9);
            }
        });
    }

    private void initHandler() {
        HandlerThread handlerThread = new HandlerThread("thread");
        handlerThread.start();
        this.mHandler = new Handler(handlerThread.getLooper());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean canLaunchVoiceAssist() {
        return ((AssistManager) Dependency.get(AssistManager.class)).canVoiceAssistBeLaunchedFromKeyguard();
    }

    private void launchPhone() {
        final TelecomManager from = TelecomManager.from(((FrameLayout) this).mContext);
        if (from.isInCall()) {
            AsyncTask.execute(new Runnable(this) { // from class: com.android.systemui.statusbar.phone.KeyguardBottomAreaView.10
                @Override // java.lang.Runnable
                public void run() {
                    from.showInCallScreen(false);
                }
            });
            return;
        }
        boolean z = true;
        if (TextUtils.isEmpty(this.mLeftButtonStr) || ((TunerService) Dependency.get(TunerService.class)).getValue("sysui_keyguard_left_unlock", 1) == 0) {
            z = false;
        }
        this.mActivityStarter.startActivity(this.mLeftButton.getIntent(), z);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onVisibilityChanged(View view, int i) {
        super.onVisibilityChanged(view, i);
        if (view == this && i == 0) {
            updateCameraVisibility();
        }
    }

    public KeyguardAffordanceView getLeftView() {
        return this.mLeftAffordanceView;
    }

    public KeyguardAffordanceView getRightView() {
        return this.mRightAffordanceView;
    }

    public View getLeftPreview() {
        return this.mLeftPreview;
    }

    public View getRightPreview() {
        return this.mCameraPreview;
    }

    public View getIndicationArea() {
        return this.mIndicationArea;
    }

    @Override // com.android.systemui.statusbar.policy.KeyguardStateController.Callback
    public void onUnlockedChanged() {
        updateCameraVisibility();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0033  */
    /* JADX WARNING: Removed duplicated region for block: B:16:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:8:0x0021  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void inflateCameraPreview() {
        /*
            r4 = this;
            android.view.View r0 = r4.mCameraPreview
            r1 = 0
            if (r0 == 0) goto L_0x0012
            android.view.ViewGroup r2 = r4.mPreviewContainer
            r2.removeView(r0)
            int r0 = r0.getVisibility()
            if (r0 != 0) goto L_0x0012
            r0 = 1
            goto L_0x0013
        L_0x0012:
            r0 = r1
        L_0x0013:
            com.android.systemui.statusbar.policy.PreviewInflater r2 = r4.mPreviewInflater
            android.content.Intent r3 = r4.getCameraIntent()
            android.view.View r2 = r2.inflatePreview(r3)
            r4.mCameraPreview = r2
            if (r2 == 0) goto L_0x002f
            android.view.ViewGroup r3 = r4.mPreviewContainer
            r3.addView(r2)
            android.view.View r2 = r4.mCameraPreview
            if (r0 == 0) goto L_0x002b
            goto L_0x002c
        L_0x002b:
            r1 = 4
        L_0x002c:
            r2.setVisibility(r1)
        L_0x002f:
            com.android.systemui.statusbar.phone.KeyguardAffordanceHelper r4 = r4.mAffordanceHelper
            if (r4 == 0) goto L_0x0036
            r4.updatePreviews()
        L_0x0036:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.KeyguardBottomAreaView.inflateCameraPreview():void");
    }

    private void updateLeftPreview() {
        View view = this.mLeftPreview;
        if (view != null) {
            this.mPreviewContainer.removeView(view);
        }
        if (!this.mLeftIsVoiceAssist) {
            this.mLeftPreview = this.mPreviewInflater.inflatePreview(this.mLeftButton.getIntent());
        } else if (((AssistManager) Dependency.get(AssistManager.class)).getVoiceInteractorComponentName() != null) {
            this.mLeftPreview = this.mPreviewInflater.inflatePreviewFromService(((AssistManager) Dependency.get(AssistManager.class)).getVoiceInteractorComponentName());
        }
        View view2 = this.mLeftPreview;
        if (view2 != null) {
            this.mPreviewContainer.addView(view2);
            this.mLeftPreview.setVisibility(4);
        }
        KeyguardAffordanceHelper keyguardAffordanceHelper = this.mAffordanceHelper;
        if (keyguardAffordanceHelper != null) {
            keyguardAffordanceHelper.updatePreviews();
        }
    }

    public void startFinishDozeAnimation() {
        long j = 0;
        if (this.mLeftAffordanceView.getVisibility() == 0) {
            startFinishDozeAnimationElement(this.mLeftAffordanceView, 0);
            j = 48;
        }
        if (this.mRightAffordanceView.getVisibility() == 0) {
            startFinishDozeAnimationElement(this.mRightAffordanceView, j);
        }
    }

    private void startFinishDozeAnimationElement(View view, long j) {
        view.setAlpha(0.0f);
        view.setTranslationY((float) (view.getHeight() / 2));
        view.animate().alpha(1.0f).translationY(0.0f).setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN).setStartDelay(j).setDuration(250);
    }

    public void updateLeftAffordance() {
        updateLeftAffordanceIcon();
        updateLeftPreview();
    }

    /* access modifiers changed from: private */
    /* renamed from: setRightButton */
    public void lambda$onAttachedToWindow$2(IntentButtonProvider.IntentButton intentButton) {
        this.mRightButton = intentButton;
        updateRightAffordanceIcon();
        updateCameraVisibility();
        inflateCameraPreview();
    }

    /* access modifiers changed from: private */
    /* renamed from: setLeftButton */
    public void lambda$onAttachedToWindow$5(IntentButtonProvider.IntentButton intentButton) {
        this.mLeftButton = intentButton;
        if (!(intentButton instanceof DefaultLeftButton)) {
            this.mLeftIsVoiceAssist = false;
        }
        Log.i("StatusBar/KeyguardBottomAreaView", "setLeftButton mLeftIsVoiceAssist:" + this.mLeftIsVoiceAssist);
        updateLeftAffordance();
    }

    public void setDozing(boolean z, boolean z2) {
        this.mDozing = z;
        updateCameraVisibility();
        updateLeftAffordanceIcon();
        if (z) {
            this.mOverlayContainer.setVisibility(4);
            return;
        }
        this.mOverlayContainer.setVisibility(0);
        if (z2) {
            startFinishDozeAnimation();
        }
    }

    public void dozeTimeTick() {
        this.mIndicationArea.setTranslationY(((float) (BurnInHelperKt.getBurnInOffset(this.mBurnInYOffset * 2, false) - this.mBurnInYOffset)) * this.mDarkAmount);
    }

    public void setAntiBurnInOffsetX(int i) {
        if (this.mBurnInXOffset != i) {
            this.mBurnInXOffset = i;
            this.mIndicationArea.setTranslationX((float) i);
        }
    }

    public void setAffordanceAlpha(float f) {
        this.mLeftAffordanceView.setAlpha(f);
        this.mRightAffordanceView.setAlpha(f);
        this.mIndicationArea.setAlpha(f);
        this.mEmergencyCarrierArea.setAlpha(f);
    }

    /* access modifiers changed from: private */
    public class DefaultLeftButton implements IntentButtonProvider.IntentButton {
        private IntentButtonProvider.IntentButton.IconState mIconState;

        private DefaultLeftButton() {
            this.mIconState = new IntentButtonProvider.IntentButton.IconState();
        }

        @Override // com.android.systemui.plugins.IntentButtonProvider.IntentButton
        public IntentButtonProvider.IntentButton.IconState getIcon() {
            KeyguardBottomAreaView keyguardBottomAreaView = KeyguardBottomAreaView.this;
            keyguardBottomAreaView.mLeftIsVoiceAssist = keyguardBottomAreaView.canLaunchVoiceAssist();
            Log.i("StatusBar/KeyguardBottomAreaView", "DefaultLeftButton mLeftIsVoiceAssist:" + KeyguardBottomAreaView.this.mLeftIsVoiceAssist);
            boolean z = KeyguardBottomAreaView.this.getResources().getBoolean(C0003R$bool.oneplus_config_keyguardShowLeftAffordance);
            boolean z2 = false;
            if (KeyguardBottomAreaView.this.mLeftIsVoiceAssist) {
                IntentButtonProvider.IntentButton.IconState iconState = this.mIconState;
                if (KeyguardBottomAreaView.this.mUserSetupComplete && z && !KeyguardBottomAreaView.this.isNeedHideLockIcon()) {
                    z2 = true;
                }
                iconState.isVisible = z2;
                if (KeyguardBottomAreaView.this.mLeftAssistIcon != null) {
                    this.mIconState.drawable = KeyguardBottomAreaView.this.mLeftAssistIcon;
                } else if (OpUtils.isCustomFingerprint()) {
                    this.mIconState.drawable = ((FrameLayout) KeyguardBottomAreaView.this).mContext.getDrawable(C0006R$drawable.ic_mic_fod);
                } else {
                    this.mIconState.drawable = ((FrameLayout) KeyguardBottomAreaView.this).mContext.getDrawable(C0006R$drawable.ic_mic_26dp);
                }
                this.mIconState.contentDescription = ((FrameLayout) KeyguardBottomAreaView.this).mContext.getString(C0015R$string.accessibility_voice_assist_button);
            } else {
                boolean z3 = KeyguardBottomAreaView.this.mOIMCServiceManager.getRemoteFuncStatus("HeadsUpNotificationZen") == 1;
                IntentButtonProvider.IntentButton.IconState iconState2 = this.mIconState;
                if (KeyguardBottomAreaView.this.mUserSetupComplete && z && KeyguardBottomAreaView.this.isPhoneVisible() && !KeyguardBottomAreaView.this.isNeedHideLockIcon() && !z3) {
                    z2 = true;
                }
                iconState2.isVisible = z2;
                this.mIconState.drawable = ((FrameLayout) KeyguardBottomAreaView.this).mContext.getDrawable(C0006R$drawable.ic_phone_fod);
                this.mIconState.contentDescription = ((FrameLayout) KeyguardBottomAreaView.this).mContext.getString(C0015R$string.accessibility_phone_button);
            }
            Log.i("StatusBar/KeyguardBottomAreaView", "DefaultLeftButton , mIconState.isVisible:" + this.mIconState.isVisible + ", mUserSetupComplete:" + KeyguardBottomAreaView.this.mUserSetupComplete + ", showAffordance:" + z + ", isPhoneVisible():" + KeyguardBottomAreaView.this.isPhoneVisible() + ", isNeedHideLockIcon():" + KeyguardBottomAreaView.this.isNeedHideLockIcon());
            return this.mIconState;
        }

        @Override // com.android.systemui.plugins.IntentButtonProvider.IntentButton
        public Intent getIntent() {
            return KeyguardBottomAreaView.PHONE_INTENT;
        }
    }

    /* access modifiers changed from: private */
    public class DefaultRightButton implements IntentButtonProvider.IntentButton {
        private IntentButtonProvider.IntentButton.IconState mIconState;

        private DefaultRightButton() {
            this.mIconState = new IntentButtonProvider.IntentButton.IconState();
        }

        @Override // com.android.systemui.plugins.IntentButtonProvider.IntentButton
        public IntentButtonProvider.IntentButton.IconState getIcon() {
            boolean z = true;
            boolean z2 = KeyguardBottomAreaView.this.mStatusBar != null && !KeyguardBottomAreaView.this.mStatusBar.isCameraAllowedByAdmin();
            IntentButtonProvider.IntentButton.IconState iconState = this.mIconState;
            if (z2 || KeyguardBottomAreaView.this.resolveCameraIntent() == null || !KeyguardBottomAreaView.this.getResources().getBoolean(C0003R$bool.oneplus_config_keyguardShowCameraAffordance) || !KeyguardBottomAreaView.this.mUserSetupComplete || KeyguardBottomAreaView.this.isNeedHideLockIcon()) {
                z = false;
            }
            iconState.isVisible = z;
            Log.i("StatusBar/KeyguardBottomAreaView", "DefaultRightButton , mIconState.isVisible:" + this.mIconState.isVisible + ", isCameraDisabled:" + z2 + ", resolveCameraIntent():" + KeyguardBottomAreaView.this.resolveCameraIntent() + ", oneplus_config_keyguardShowCameraAffordance:" + KeyguardBottomAreaView.this.getResources().getBoolean(C0003R$bool.oneplus_config_keyguardShowCameraAffordance) + ", mUserSetupComplete:" + KeyguardBottomAreaView.this.mUserSetupComplete + ", isNeedHideLockIcon():" + KeyguardBottomAreaView.this.isNeedHideLockIcon());
            this.mIconState.drawable = ((FrameLayout) KeyguardBottomAreaView.this).mContext.getDrawable(C0006R$drawable.ic_camera_fod);
            this.mIconState.contentDescription = ((FrameLayout) KeyguardBottomAreaView.this).mContext.getString(C0015R$string.accessibility_camera_button);
            return this.mIconState;
        }

        @Override // com.android.systemui.plugins.IntentButtonProvider.IntentButton
        public Intent getIntent() {
            if (KeyguardBottomAreaView.this.mKeyguardStateController == null) {
                Log.i("StatusBar/KeyguardBottomAreaView", "mKeyguardStateController == null return INSECURE_CAMERA_INTENT");
                return KeyguardBottomAreaView.INSECURE_CAMERA_INTENT;
            }
            boolean canDismissLockScreen = KeyguardBottomAreaView.this.mKeyguardStateController.canDismissLockScreen();
            boolean isMethodSecure = KeyguardBottomAreaView.this.mKeyguardStateController.isMethodSecure();
            boolean userCanSkipBouncer = KeyguardUpdateMonitor.getInstance(((FrameLayout) KeyguardBottomAreaView.this).mContext).getUserCanSkipBouncer(KeyguardUpdateMonitor.getCurrentUser());
            if (KeyguardBottomAreaView.this.mStatusBar == null || !isMethodSecure || userCanSkipBouncer || KeyguardBottomAreaView.this.mStatusBar.isKeyguardShowing()) {
                return (!isMethodSecure || canDismissLockScreen) ? KeyguardBottomAreaView.INSECURE_CAMERA_INTENT : KeyguardBottomAreaView.SECURE_CAMERA_INTENT;
            }
            Log.d("StatusBar/KeyguardBottomAreaView", "launchCamera to INSECURE");
            return KeyguardBottomAreaView.INSECURE_CAMERA_INTENT;
        }
    }

    @Override // android.view.View
    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        int safeInsetBottom = windowInsets.getDisplayCutout() != null ? windowInsets.getDisplayCutout().getSafeInsetBottom() : 0;
        if (isPaddingRelative()) {
            setPaddingRelative(getPaddingStart(), getPaddingTop(), getPaddingEnd(), safeInsetBottom);
        } else {
            setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), safeInsetBottom);
        }
        return windowInsets;
    }

    public void setShowOTAWizard(boolean z) {
        this.mNeedShowOTAWizard = z;
        updateCameraVisibility();
        updateLeftAffordanceIcon();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isNeedHideLockIcon() {
        if (!this.mNeedShowOTAWizard || KeyguardUpdateMonitor.getInstance(((FrameLayout) this).mContext).isUserUnlocked()) {
            return false;
        }
        return this.mNeedShowOTAWizard;
    }
}
