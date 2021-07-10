package com.android.systemui.assist;

import android.app.ActivityOptions;
import android.app.KeyguardManager;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.metrics.LogMaker;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import com.android.internal.app.AssistUtils;
import com.android.internal.app.IVoiceInteractionSessionListener;
import com.android.internal.app.IVoiceInteractionSessionShowCallback;
import com.android.internal.logging.MetricsLogger;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.settingslib.applications.InterestingConfigChanges;
import com.android.systemui.C0000R$anim;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0011R$layout;
import com.android.systemui.DejankUtils;
import com.android.systemui.Dependency;
import com.android.systemui.assist.ui.DefaultUiController;
import com.android.systemui.assist.ui.OpAssistNavigationDialog;
import com.android.systemui.model.SysUiState;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.recents.OverviewProxyService;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import dagger.Lazy;
import java.util.function.Supplier;
public class AssistManager {
    public static String EXTRA_ASSIST_MANAGER_LAUNCH_MODE = "assistant_launch_mode";
    private final AssistDisclosure mAssistDisclosure;
    protected final AssistLogger mAssistLogger;
    protected final AssistUtils mAssistUtils;
    private final CommandQueue mCommandQueue;
    private ConfigurationController.ConfigurationListener mConfigurationListener = new ConfigurationController.ConfigurationListener() { // from class: com.android.systemui.assist.AssistManager.3
        @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
        public void onConfigChanged(Configuration configuration) {
            boolean z;
            if (AssistManager.this.mInterestingConfigChanges.applyNewConfig(AssistManager.this.mContext.getResources())) {
                if (OpAssistNavigationDialog.getInstance(AssistManager.this.mContext).isShowing()) {
                    OpAssistNavigationDialog.getInstance(AssistManager.this.mContext).onConfigChanged(configuration);
                }
                if (AssistManager.this.mView != null) {
                    z = AssistManager.this.mView.isShowing();
                    AssistManager.this.mWindowManager.removeView(AssistManager.this.mView);
                } else {
                    z = false;
                }
                AssistManager assistManager = AssistManager.this;
                assistManager.mView = (AssistOrbContainer) LayoutInflater.from(assistManager.mContext).inflate(C0011R$layout.assist_orb, (ViewGroup) null);
                AssistManager.this.mView.setVisibility(8);
                AssistManager.this.mView.setSystemUiVisibility(1792);
                try {
                    AssistManager.this.mWindowManager.addView(AssistManager.this.mView, AssistManager.this.getLayoutParams());
                } catch (RuntimeException e) {
                    Log.e("AssistManager", e.getMessage());
                }
                if (z) {
                    AssistManager.this.mView.show(true, false);
                }
            }
        }

        @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
        public void onOverlayChanged() {
            if (AssistManager.this.mUiController != null) {
                AssistManager.this.mUiController.onOverlayChanged();
            }
        }

        @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
        public void onDensityOrFontScaleChanged() {
            Log.d("AssistManager", "onDensityOrFontScaleChanged");
            if (AssistManager.this.mUiController != null) {
                AssistManager.this.mUiController.onOverlayChanged();
            }
        }
    };
    protected final Context mContext;
    private final DeviceProvisionedController mDeviceProvisionedController;
    private final AssistHandleBehaviorController mHandleController;
    private Runnable mHideRunnable = new Runnable() { // from class: com.android.systemui.assist.AssistManager.2
        @Override // java.lang.Runnable
        public void run() {
            AssistManager.this.mView.removeCallbacks(this);
            AssistManager.this.mView.show(false, true);
        }
    };
    private final InterestingConfigChanges mInterestingConfigChanges;
    private boolean mIsPowerLongPressWithAssistant = false;
    private boolean mIsPowerLongPressWithGoogleAssistant = false;
    private final PhoneStateMonitor mPhoneStateMonitor;
    private final boolean mShouldEnableOrb;
    private IVoiceInteractionSessionShowCallback mShowCallback = new IVoiceInteractionSessionShowCallback.Stub() { // from class: com.android.systemui.assist.AssistManager.1
        public void onFailed() throws RemoteException {
            AssistManager.this.mView.post(AssistManager.this.mHideRunnable);
        }

        public void onShown() throws RemoteException {
            AssistManager.this.mView.post(AssistManager.this.mHideRunnable);
        }
    };
    protected final Lazy<SysUiState> mSysUiState;
    private final UiController mUiController;
    private AssistOrbContainer mView;
    private final WindowManager mWindowManager;

    public enum AssistManagerLaunchMode {
        UNKNOWN,
        DEFAULT
    }

    public interface UiController {
        void onGestureCompletion(float f);

        void onInvocationProgress(int i, float f);

        void onOverlayChanged();
    }

    /* access modifiers changed from: protected */
    public boolean shouldShowOrb() {
        return true;
    }

    public AssistManager(DeviceProvisionedController deviceProvisionedController, Context context, AssistUtils assistUtils, AssistHandleBehaviorController assistHandleBehaviorController, CommandQueue commandQueue, PhoneStateMonitor phoneStateMonitor, OverviewProxyService overviewProxyService, ConfigurationController configurationController, Lazy<SysUiState> lazy, DefaultUiController defaultUiController, AssistLogger assistLogger) {
        this.mContext = context;
        this.mDeviceProvisionedController = deviceProvisionedController;
        this.mCommandQueue = commandQueue;
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        this.mAssistUtils = assistUtils;
        this.mAssistDisclosure = new AssistDisclosure(context, new Handler());
        this.mPhoneStateMonitor = phoneStateMonitor;
        this.mHandleController = assistHandleBehaviorController;
        this.mAssistLogger = assistLogger;
        configurationController.addCallback(this.mConfigurationListener);
        registerVoiceInteractionSessionListener();
        this.mInterestingConfigChanges = new InterestingConfigChanges(-2147482748);
        this.mConfigurationListener.onConfigChanged(context.getResources().getConfiguration());
        this.mShouldEnableOrb = false;
        this.mUiController = defaultUiController;
        this.mSysUiState = lazy;
        overviewProxyService.addCallback((OverviewProxyService.OverviewProxyListener) new OverviewProxyService.OverviewProxyListener() { // from class: com.android.systemui.assist.AssistManager.4
            @Override // com.android.systemui.recents.OverviewProxyService.OverviewProxyListener
            public void onAssistantProgress(float f) {
                AssistManager.this.onInvocationProgress(1, f);
            }

            @Override // com.android.systemui.recents.OverviewProxyService.OverviewProxyListener
            public void onAssistantGestureCompletion(float f) {
                AssistManager.this.onGestureCompletion(f);
            }
        });
    }

    /* access modifiers changed from: protected */
    public void registerVoiceInteractionSessionListener() {
        this.mAssistUtils.registerVoiceInteractionSessionListener(new IVoiceInteractionSessionListener.Stub() { // from class: com.android.systemui.assist.AssistManager.5
            public void onVoiceSessionShown() throws RemoteException {
                AssistManager.this.mAssistLogger.reportAssistantSessionEvent(AssistantSessionEvent.ASSISTANT_SESSION_UPDATE);
            }

            public void onVoiceSessionHidden() throws RemoteException {
                AssistManager.this.mAssistLogger.reportAssistantSessionEvent(AssistantSessionEvent.ASSISTANT_SESSION_CLOSE);
            }

            public void onSetUiHints(Bundle bundle) {
                String string = bundle.getString("action");
                if ("show_assist_handles".equals(string)) {
                    AssistManager.this.requestAssistHandles();
                } else if ("set_assist_gesture_constrained".equals(string)) {
                    SysUiState sysUiState = AssistManager.this.mSysUiState.get();
                    sysUiState.setFlag(8192, bundle.getBoolean("should_constrain", false));
                    sysUiState.commitUpdate(0);
                }
            }
        });
    }

    public void startAssist(Bundle bundle) {
        String str;
        ComponentName assistInfo = getAssistInfo();
        if (assistInfo != null) {
            if (bundle != null) {
                this.mIsPowerLongPressWithAssistant = bundle.getBoolean("power_long_press_with_assistant_hint", false);
                this.mIsPowerLongPressWithGoogleAssistant = bundle.getBoolean("power_long_press_with_google_assistant_hint", false);
                Log.d("AssistManager", "LongPressWithAssistant: " + this.mIsPowerLongPressWithAssistant + " LongPressWithGoogleAssistant: " + this.mIsPowerLongPressWithGoogleAssistant);
                boolean z = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "op_assist_started", 0, -2) != 0;
                if ((this.mContext.getResources().getDimensionPixelOffset(C0005R$dimen.op_assist_nav_dialog_pow_pos) != 0) && !z && (this.mIsPowerLongPressWithAssistant || this.mIsPowerLongPressWithGoogleAssistant)) {
                    OpAssistNavigationDialog.getInstance(this.mContext);
                    OpAssistNavigationDialog.showDialog();
                    Settings.Secure.putIntForUser(this.mContext.getContentResolver(), "op_assist_started", 1, -2);
                    return;
                }
            } else {
                this.mIsPowerLongPressWithAssistant = false;
                this.mIsPowerLongPressWithGoogleAssistant = false;
            }
            boolean equals = assistInfo.equals(getVoiceInteractorComponentName());
            if (!equals || (!isVoiceSessionRunning() && shouldShowOrb())) {
                showOrb(assistInfo, equals);
                this.mView.postDelayed(this.mHideRunnable, equals ? 2500 : 1000);
            }
            if (bundle == null) {
                bundle = new Bundle();
            }
            int i = bundle.getInt("invocation_type", 0);
            if (i == 1) {
                this.mHandleController.onAssistantGesturePerformed();
            }
            int phoneState = this.mPhoneStateMonitor.getPhoneState();
            bundle.putInt("invocation_phone_state", phoneState);
            bundle.putLong("invocation_time_ms", SystemClock.elapsedRealtime());
            logStartAssistLegacy(i, phoneState);
            if (this.mIsPowerLongPressWithAssistant || this.mIsPowerLongPressWithGoogleAssistant) {
                String stringForUser = Settings.Secure.getStringForUser(this.mContext.getContentResolver(), "oneplus_default_voice_assist_picker_service", -2);
                if (TextUtils.isEmpty(stringForUser)) {
                    Log.d("AssistManager", "opAssistPicker is empty!");
                    return;
                }
                Log.d("AssistManager", "opAssistPicker: " + stringForUser);
                String[] split = stringForUser.split("/");
                if (split.length == 2) {
                    if (split[1].startsWith(".")) {
                        str = split[0] + split[1];
                    } else {
                        str = split[1];
                    }
                    ComponentName componentName = new ComponentName(split[0], str);
                    boolean equals2 = componentName.equals(getVoiceInteractorComponentName());
                    Log.d("AssistManager", "componentName: " + componentName + " opIsService: " + equals2);
                    bundle.putInt(EXTRA_ASSIST_MANAGER_LAUNCH_MODE, AssistManagerLaunchMode.DEFAULT.ordinal());
                    startAssistInternal(bundle, componentName, equals2, true);
                    return;
                }
            }
            if (bundle.getBoolean("com.heytap.speechassist", false)) {
                launchHeyTapVoiceAssist();
            } else {
                startAssistInternal(bundle, assistInfo, equals, false);
            }
        }
    }

    public void launchHeyTapVoiceAssist() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.heytap.speechassist", "com.heytap.speechassist.core.SpeechService"));
        intent.putExtra("caller_package", this.mContext.getPackageName());
        intent.putExtra("start_type", 1024);
        try {
            if (Build.VERSION.SDK_INT >= 26) {
                this.mContext.getApplicationContext().startForegroundService(intent);
            } else {
                this.mContext.getApplicationContext().startService(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onInvocationProgress(int i, float f) {
        this.mUiController.onInvocationProgress(i, f);
    }

    public void onGestureCompletion(float f) {
        this.mUiController.onGestureCompletion(f);
    }

    /* access modifiers changed from: protected */
    public void requestAssistHandles() {
        this.mHandleController.onAssistHandlesRequested();
    }

    public void hideAssist() {
        this.mAssistUtils.hideCurrentSession();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private WindowManager.LayoutParams getLayoutParams() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-1, this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.assist_orb_scrim_height), 2033, 280, -3);
        layoutParams.token = new Binder();
        layoutParams.gravity = 8388691;
        layoutParams.setTitle("AssistPreviewPanel");
        layoutParams.softInputMode = 49;
        return layoutParams;
    }

    private void showOrb(ComponentName componentName, boolean z) {
        maybeSwapSearchIcon(componentName, z);
        if (this.mShouldEnableOrb && !this.mIsPowerLongPressWithAssistant && !this.mIsPowerLongPressWithGoogleAssistant) {
            Log.v("AssistManager", "showOrb");
            this.mView.show(true, true);
        }
    }

    private void startAssistInternal(Bundle bundle, ComponentName componentName, boolean z, boolean z2) {
        if (z) {
            startVoiceInteractor(bundle);
            return;
        }
        KeyguardManager keyguardManager = (KeyguardManager) this.mContext.getSystemService("keyguard");
        if (!z2 || keyguardManager == null || (!keyguardManager.isDeviceLocked() && !keyguardManager.isKeyguardLocked())) {
            startAssistActivity(bundle, componentName, z2);
        } else {
            ((ActivityStarter) Dependency.get(ActivityStarter.class)).dismissKeyguardThenExecute(new ActivityStarter.OnDismissAction(z, bundle, componentName, z2) { // from class: com.android.systemui.assist.-$$Lambda$AssistManager$CMIbj1AIEeENlbznV0JUh10N9G4
                public final /* synthetic */ boolean f$1;
                public final /* synthetic */ Bundle f$2;
                public final /* synthetic */ ComponentName f$3;
                public final /* synthetic */ boolean f$4;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                }

                @Override // com.android.systemui.plugins.ActivityStarter.OnDismissAction
                public final boolean onDismiss() {
                    return AssistManager.this.lambda$startAssistInternal$0$AssistManager(this.f$1, this.f$2, this.f$3, this.f$4);
                }
            }, null, true);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startAssistInternal$0 */
    public /* synthetic */ boolean lambda$startAssistInternal$0$AssistManager(boolean z, Bundle bundle, ComponentName componentName, boolean z2) {
        if (z) {
            startVoiceInteractor(bundle);
            return true;
        }
        startAssistActivity(bundle, componentName, z2);
        return true;
    }

    private void startAssistActivity(Bundle bundle, ComponentName componentName, boolean z) {
        final Intent assistIntent;
        if (this.mDeviceProvisionedController.isDeviceProvisioned()) {
            boolean z2 = false;
            this.mCommandQueue.animateCollapsePanels(3, false);
            if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "assist_structure_enabled", 1, -2) != 0) {
                z2 = true;
            }
            SearchManager searchManager = (SearchManager) this.mContext.getSystemService("search");
            if (searchManager != null && (assistIntent = searchManager.getAssistIntent(z2)) != null) {
                assistIntent.setComponent(componentName);
                assistIntent.putExtras(bundle);
                if (z2 && AssistUtils.isDisclosureEnabled(this.mContext)) {
                    showDisclosure();
                }
                try {
                    final ActivityOptions makeCustomAnimation = ActivityOptions.makeCustomAnimation(this.mContext, C0000R$anim.search_launch_enter, C0000R$anim.search_launch_exit);
                    assistIntent.addFlags(268435456);
                    AsyncTask.execute(new Runnable() { // from class: com.android.systemui.assist.AssistManager.6
                        @Override // java.lang.Runnable
                        public void run() {
                            Log.d("AssistManager", "start Assist Activity: " + assistIntent);
                            AssistManager.this.mContext.startActivityAsUser(assistIntent, makeCustomAnimation.toBundle(), new UserHandle(-2));
                        }
                    });
                } catch (ActivityNotFoundException unused) {
                    Log.w("AssistManager", "Activity not found for " + assistIntent.getAction());
                }
            }
        }
    }

    private void startVoiceInteractor(Bundle bundle) {
        Log.d("AssistManager", "start VoiceInteractor");
        this.mAssistUtils.showSessionForActiveService(bundle, 4, this.mShowCallback, (IBinder) null);
    }

    public void launchVoiceAssistFromKeyguard() {
        this.mAssistUtils.launchVoiceAssistFromKeyguard();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$canVoiceAssistBeLaunchedFromKeyguard$1 */
    public /* synthetic */ Boolean lambda$canVoiceAssistBeLaunchedFromKeyguard$1$AssistManager() {
        return Boolean.valueOf(this.mAssistUtils.activeServiceSupportsLaunchFromKeyguard());
    }

    public boolean canVoiceAssistBeLaunchedFromKeyguard() {
        return ((Boolean) DejankUtils.whitelistIpcs(new Supplier() { // from class: com.android.systemui.assist.-$$Lambda$AssistManager$jug05Nabf2QR7m1yWlGRXdMVfIs
            @Override // java.util.function.Supplier
            public final Object get() {
                return AssistManager.this.lambda$canVoiceAssistBeLaunchedFromKeyguard$1$AssistManager();
            }
        })).booleanValue();
    }

    public ComponentName getVoiceInteractorComponentName() {
        return this.mAssistUtils.getActiveServiceComponentName();
    }

    private boolean isVoiceSessionRunning() {
        return this.mAssistUtils.isSessionRunning();
    }

    private void maybeSwapSearchIcon(ComponentName componentName, boolean z) {
        replaceDrawable(this.mView.getOrb().getLogo(), componentName, "com.android.systemui.action_assist_icon", z);
    }

    public void replaceDrawable(ImageView imageView, ComponentName componentName, String str, boolean z) {
        Bundle bundle;
        int i;
        if (componentName != null) {
            try {
                PackageManager packageManager = this.mContext.getPackageManager();
                if (z) {
                    bundle = packageManager.getServiceInfo(componentName, 128).metaData;
                } else {
                    bundle = packageManager.getActivityInfo(componentName, 128).metaData;
                }
                if (!(bundle == null || (i = bundle.getInt(str)) == 0)) {
                    imageView.setImageDrawable(packageManager.getResourcesForApplication(componentName.getPackageName()).getDrawable(i));
                    return;
                }
            } catch (PackageManager.NameNotFoundException unused) {
            } catch (Resources.NotFoundException e) {
                Log.w("AssistManager", "Failed to swap drawable from " + componentName.flattenToShortString(), e);
            }
        }
        imageView.setImageDrawable(null);
    }

    public ComponentName getAssistInfoForUser(int i) {
        return this.mAssistUtils.getAssistComponentForUser(i);
    }

    private ComponentName getAssistInfo() {
        return getAssistInfoForUser(KeyguardUpdateMonitor.getCurrentUser());
    }

    public void showDisclosure() {
        if (!this.mIsPowerLongPressWithAssistant && !this.mIsPowerLongPressWithGoogleAssistant) {
            Log.v("AssistManager", "showDisclosure");
            this.mAssistDisclosure.postShow();
        }
    }

    public void onLockscreenShown() {
        AsyncTask.execute(new Runnable() { // from class: com.android.systemui.assist.AssistManager.7
            @Override // java.lang.Runnable
            public void run() {
                AssistManager.this.mAssistUtils.onLockscreenShown();
            }
        });
    }

    public long getAssistHandleShowAndGoRemainingDurationMs() {
        return this.mHandleController.getShowAndGoRemainingTimeMs();
    }

    public int toLoggingSubType(int i) {
        return toLoggingSubType(i, this.mPhoneStateMonitor.getPhoneState());
    }

    /* access modifiers changed from: protected */
    public void logStartAssistLegacy(int i, int i2) {
        MetricsLogger.action(new LogMaker(1716).setType(1).setSubtype(toLoggingSubType(i, i2)));
    }

    /* access modifiers changed from: protected */
    public final int toLoggingSubType(int i, int i2) {
        return (!this.mHandleController.areHandlesShowing()) | (i << 1) | (i2 << 4);
    }
}
