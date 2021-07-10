package com.android.systemui.recents;

import android.app.ActivityTaskManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Insets;
import android.graphics.Rect;
import android.graphics.Region;
import android.hardware.fingerprint.IFingerprintService;
import android.hardware.input.InputManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityManager;
import com.android.internal.accessibility.dialog.AccessibilityButtonChooserActivity;
import com.android.internal.policy.ScreenDecorationsUtils;
import com.android.internal.util.ScreenshotHelper;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.model.SysUiState;
import com.android.systemui.pip.PipUI;
import com.android.systemui.recents.OverviewProxyService;
import com.android.systemui.settings.CurrentUserTracker;
import com.android.systemui.shared.recents.IOverviewProxy;
import com.android.systemui.shared.recents.IPinnedStackAnimationListener;
import com.android.systemui.shared.recents.ISystemUiProxy;
import com.android.systemui.shared.recents.model.Task$TaskKey;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.QuickStepContract;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.NavigationBarController;
import com.android.systemui.statusbar.phone.EdgeBackGestureHandler;
import com.android.systemui.statusbar.phone.NavigationBarFragment;
import com.android.systemui.statusbar.phone.NavigationBarView;
import com.android.systemui.statusbar.phone.NavigationModeController;
import com.android.systemui.statusbar.phone.NotificationShadeWindowController;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.phone.StatusBarWindowCallback;
import com.android.systemui.statusbar.policy.CallbackController;
import com.oneplus.plugin.OpLsState;
import com.oneplus.systemui.OpSystemUIInjector;
import com.oneplus.util.OpUtils;
import dagger.Lazy;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
public class OverviewProxyService extends CurrentUserTracker implements CallbackController<OverviewProxyListener>, NavigationModeController.ModeChangedListener, Dumpable {
    private Region mActiveNavBarRegion;
    private final ContentObserver mAssistContentObserver = new ContentObserver(new Handler(Looper.getMainLooper())) { // from class: com.android.systemui.recents.OverviewProxyService.6
        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri) {
            if (OpUtils.DEBUG_ONEPLUS) {
                Log.d("OverviewProxyService", "send assistant availability internal to launcher when receive assist state change.");
            }
            OverviewProxyService.this.sendAssistantAvailabilityInternal();
        }
    };
    private String mAuthenticatingPackage = null;
    private boolean mBound;
    private int mConnectionBackoffAttempts;
    private final List<OverviewProxyListener> mConnectionCallbacks = new ArrayList();
    private final Runnable mConnectionRunnable = new Runnable() { // from class: com.android.systemui.recents.-$$Lambda$OverviewProxyService$2FrwSEVJnaHX9GGsAnD2I96htxU
        @Override // java.lang.Runnable
        public final void run() {
            OverviewProxyService.this.internalConnectToCurrentUser();
        }
    };
    private ContentResolver mContentResolver;
    private final Context mContext;
    private int mCurrentBoundedUserId = -1;
    private final Runnable mDeferredConnectionCallback = new Runnable() { // from class: com.android.systemui.recents.-$$Lambda$OverviewProxyService$53s1j2vSUNo_EjM7u2nSTJl32gM
        @Override // java.lang.Runnable
        public final void run() {
            OverviewProxyService.this.lambda$new$0$OverviewProxyService();
        }
    };
    private final Optional<Divider> mDividerOptional;
    private final Handler mHandler;
    private boolean mInputFocusTransferStarted;
    private boolean mIsEnabled;
    private final BroadcastReceiver mLauncherStateChangedReceiver = new BroadcastReceiver() { // from class: com.android.systemui.recents.OverviewProxyService.2
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String[] stringArrayExtra = intent.getStringArrayExtra("android.intent.extra.changed_component_name_list");
            if (stringArrayExtra != null) {
                for (String str : stringArrayExtra) {
                    if (str.equals("net.oneplus.launcher.wallpaper.DummyWallpaper")) {
                        Log.d("OverviewProxyService", "Ignore dummy wallpaper package change");
                        return;
                    }
                }
            }
            OverviewProxyService.this.updateEnabledState();
            OverviewProxyService.this.startConnectionToCurrentUser();
        }
    };
    private float mNavBarButtonAlpha;
    private final NavigationBarController mNavBarController;
    private int mNavBarMode = 0;
    private OverviewProxyOneHandedListener mOneHandListener;
    private IOverviewProxy mOverviewProxy;
    private final ServiceConnection mOverviewServiceConnection = new ServiceConnection() { // from class: com.android.systemui.recents.OverviewProxyService.3
        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            OverviewProxyService.this.mConnectionBackoffAttempts = 0;
            OverviewProxyService.this.mHandler.removeCallbacks(OverviewProxyService.this.mDeferredConnectionCallback);
            try {
                iBinder.linkToDeath(OverviewProxyService.this.mOverviewServiceDeathRcpt, 0);
                OverviewProxyService overviewProxyService = OverviewProxyService.this;
                overviewProxyService.mCurrentBoundedUserId = overviewProxyService.getCurrentUserId();
                OverviewProxyService.this.mOverviewProxy = IOverviewProxy.Stub.asInterface(iBinder);
                Bundle bundle = new Bundle();
                bundle.putBinder("extra_sysui_proxy", OverviewProxyService.this.mSysUiProxy.asBinder());
                bundle.putFloat("extra_window_corner_radius", OverviewProxyService.this.mWindowCornerRadius);
                bundle.putBoolean("extra_supports_window_corners", OverviewProxyService.this.mSupportsRoundedCornersOnWindows);
                try {
                    OverviewProxyService.this.mOverviewProxy.onInitialize(bundle);
                } catch (RemoteException e) {
                    OverviewProxyService.this.mCurrentBoundedUserId = -1;
                    Log.e("OverviewProxyService", "Failed to call onInitialize()", e);
                }
                OverviewProxyService.this.dispatchNavButtonBounds();
                OverviewProxyService.this.updateSystemUiStateFlags();
                OverviewProxyService overviewProxyService2 = OverviewProxyService.this;
                overviewProxyService2.notifySystemUiStateFlags(overviewProxyService2.mSysUiState.getFlags());
                OverviewProxyService.this.notifyConnectionChanged();
                if (OpUtils.DEBUG_ONEPLUS) {
                    Log.d("OverviewProxyService", "send assistant availability internal to launcher when overview proxy service connected.");
                }
                OverviewProxyService.this.sendAssistantAvailabilityInternal();
                OverviewProxyService.this.mContentResolver.registerContentObserver(Settings.Secure.getUriFor("assistant"), false, OverviewProxyService.this.mAssistContentObserver, -1);
                OpUtils.updateSupportAssistantGestureState(OverviewProxyService.this.mContext);
            } catch (RemoteException e2) {
                Log.e("OverviewProxyService", "Lost connection to launcher service", e2);
                OverviewProxyService.this.disconnectFromLauncherService();
                OverviewProxyService.this.retryConnectionWithBackoff();
            }
        }

        @Override // android.content.ServiceConnection
        public void onNullBinding(ComponentName componentName) {
            Log.w("OverviewProxyService", "Null binding of '" + componentName + "', try reconnecting");
            OverviewProxyService.this.mCurrentBoundedUserId = -1;
            OverviewProxyService.this.retryConnectionWithBackoff();
        }

        @Override // android.content.ServiceConnection
        public void onBindingDied(ComponentName componentName) {
            Log.w("OverviewProxyService", "Binding died of '" + componentName + "', try reconnecting");
            OverviewProxyService.this.mCurrentBoundedUserId = -1;
            OverviewProxyService.this.retryConnectionWithBackoff();
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            OverviewProxyService.this.mCurrentBoundedUserId = -1;
        }
    };
    private final IBinder.DeathRecipient mOverviewServiceDeathRcpt = new IBinder.DeathRecipient() { // from class: com.android.systemui.recents.-$$Lambda$FF1twVzMKp_FAsQO2IsbqUbCb-s
        @Override // android.os.IBinder.DeathRecipient
        public final void binderDied() {
            OverviewProxyService.this.cleanupAfterDeath();
        }
    };
    private final PipUI mPipUI;
    private final Intent mQuickStepIntent;
    private final ComponentName mRecentsComponentName;
    private final ScreenshotHelper mScreenshotHelper;
    private StatusBar mStatusBar;
    private MotionEvent mStatusBarGestureDownEvent;
    private final Optional<Lazy<StatusBar>> mStatusBarOptionalLazy;
    private final NotificationShadeWindowController mStatusBarWinController;
    private final StatusBarWindowCallback mStatusBarWindowCallback = new StatusBarWindowCallback() { // from class: com.android.systemui.recents.-$$Lambda$OverviewProxyService$b7uhSpdl46tRQQQT8ZW7Bieyg6A
        @Override // com.android.systemui.statusbar.phone.StatusBarWindowCallback
        public final void onStateChanged(boolean z, boolean z2, boolean z3) {
            OverviewProxyService.this.onStatusBarStateChanged(z, z2, z3);
        }
    };
    private boolean mSupportsRoundedCornersOnWindows;
    private ISystemUiProxy mSysUiProxy = new ISystemUiProxy.Stub() { // from class: com.android.systemui.recents.OverviewProxyService.1
        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void handleImageAsScreenshot(Bitmap bitmap, Rect rect, Insets insets, int i) {
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void startScreenPinning(int i) {
            if (verifyCaller("startScreenPinning")) {
                long clearCallingIdentity = Binder.clearCallingIdentity();
                try {
                    OverviewProxyService.this.mHandler.post(new Runnable(i) { // from class: com.android.systemui.recents.-$$Lambda$OverviewProxyService$1$4SXWj0CMroT_CN5f-JJLswjoG60
                        public final /* synthetic */ int f$1;

                        {
                            this.f$1 = r2;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            OverviewProxyService.AnonymousClass1.this.lambda$startScreenPinning$1$OverviewProxyService$1(this.f$1);
                        }
                    });
                } finally {
                    Binder.restoreCallingIdentity(clearCallingIdentity);
                }
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$startScreenPinning$1 */
        public /* synthetic */ void lambda$startScreenPinning$1$OverviewProxyService$1(int i) {
            OverviewProxyService.this.mStatusBarOptionalLazy.ifPresent(new Consumer(i) { // from class: com.android.systemui.recents.-$$Lambda$OverviewProxyService$1$L6GammKdHWnk5GdqkgMLveN3ScI
                public final /* synthetic */ int f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    ((StatusBar) ((Lazy) obj).get()).showScreenPinningRequest(this.f$0, false);
                }
            });
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void stopScreenPinning() {
            if (verifyCaller("stopScreenPinning")) {
                long clearCallingIdentity = Binder.clearCallingIdentity();
                try {
                    OverviewProxyService.this.mHandler.post($$Lambda$OverviewProxyService$1$9uERjvGI5cZ0Wh2SqRhoEXg8wYk.INSTANCE);
                } finally {
                    Binder.restoreCallingIdentity(clearCallingIdentity);
                }
            }
        }

        static /* synthetic */ void lambda$stopScreenPinning$2() {
            try {
                ActivityTaskManager.getService().stopSystemLockTaskMode();
            } catch (RemoteException unused) {
                Log.e("OverviewProxyService", "Failed to stop screen pinning");
            }
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void onStatusBarMotionEvent(MotionEvent motionEvent) {
            if (verifyCaller("onStatusBarMotionEvent")) {
                long clearCallingIdentity = Binder.clearCallingIdentity();
                try {
                    OverviewProxyService.this.mTouchHandler.obtainMessage(1, motionEvent).sendToTarget();
                } finally {
                    Binder.restoreCallingIdentity(clearCallingIdentity);
                }
            }
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void onSplitScreenInvoked() {
            if (verifyCaller("onSplitScreenInvoked")) {
                long clearCallingIdentity = Binder.clearCallingIdentity();
                try {
                    OverviewProxyService.this.mDividerOptional.ifPresent($$Lambda$xuXEcdh0HmTmuN4e7qU9mBkM36M.INSTANCE);
                } finally {
                    Binder.restoreCallingIdentity(clearCallingIdentity);
                }
            }
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void onOverviewShown(boolean z) {
            if (verifyCaller("onOverviewShown")) {
                long clearCallingIdentity = Binder.clearCallingIdentity();
                try {
                    OverviewProxyService.this.mHandler.post(new Runnable(z) { // from class: com.android.systemui.recents.-$$Lambda$OverviewProxyService$1$o_Nvl9rNrEnvxnQlEkJ_hCsmmfI
                        public final /* synthetic */ boolean f$1;

                        {
                            this.f$1 = r2;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            OverviewProxyService.AnonymousClass1.this.lambda$onOverviewShown$3$OverviewProxyService$1(this.f$1);
                        }
                    });
                } finally {
                    Binder.restoreCallingIdentity(clearCallingIdentity);
                }
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onOverviewShown$3 */
        public /* synthetic */ void lambda$onOverviewShown$3$OverviewProxyService$1(boolean z) {
            for (int size = OverviewProxyService.this.mConnectionCallbacks.size() - 1; size >= 0; size--) {
                ((OverviewProxyListener) OverviewProxyService.this.mConnectionCallbacks.get(size)).onOverviewShown(z);
            }
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public Rect getNonMinimizedSplitScreenSecondaryBounds() {
            if (!verifyCaller("getNonMinimizedSplitScreenSecondaryBounds")) {
                return null;
            }
            long clearCallingIdentity = Binder.clearCallingIdentity();
            try {
                return (Rect) OverviewProxyService.this.mDividerOptional.map($$Lambda$OverviewProxyService$1$j9bQ74woDmszHVIFRVSZ9dAf7dQ.INSTANCE).orElse(null);
            } finally {
                Binder.restoreCallingIdentity(clearCallingIdentity);
            }
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void setNavBarButtonAlpha(float f, boolean z) {
            if (verifyCaller("setNavBarButtonAlpha")) {
                long clearCallingIdentity = Binder.clearCallingIdentity();
                try {
                    OverviewProxyService.this.mNavBarButtonAlpha = f;
                    OverviewProxyService.this.mHandler.post(new Runnable(f, z) { // from class: com.android.systemui.recents.-$$Lambda$OverviewProxyService$1$ponbkRY3xkHBd5y3BY2Qppv8kjo
                        public final /* synthetic */ float f$1;
                        public final /* synthetic */ boolean f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            OverviewProxyService.AnonymousClass1.this.lambda$setNavBarButtonAlpha$5$OverviewProxyService$1(this.f$1, this.f$2);
                        }
                    });
                } finally {
                    Binder.restoreCallingIdentity(clearCallingIdentity);
                }
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$setNavBarButtonAlpha$5 */
        public /* synthetic */ void lambda$setNavBarButtonAlpha$5$OverviewProxyService$1(float f, boolean z) {
            OverviewProxyService.this.notifyNavBarButtonAlphaChanged(f, z);
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void setBackButtonAlpha(float f, boolean z) {
            setNavBarButtonAlpha(f, z);
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void onAssistantProgress(float f) {
            if (verifyCaller("onAssistantProgress")) {
                long clearCallingIdentity = Binder.clearCallingIdentity();
                try {
                    OverviewProxyService.this.mHandler.post(new Runnable(f) { // from class: com.android.systemui.recents.-$$Lambda$OverviewProxyService$1$ThcRnqU7jG-XhWGA-EMyxMRPnp8
                        public final /* synthetic */ float f$1;

                        {
                            this.f$1 = r2;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            OverviewProxyService.AnonymousClass1.this.lambda$onAssistantProgress$6$OverviewProxyService$1(this.f$1);
                        }
                    });
                } finally {
                    Binder.restoreCallingIdentity(clearCallingIdentity);
                }
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onAssistantProgress$6 */
        public /* synthetic */ void lambda$onAssistantProgress$6$OverviewProxyService$1(float f) {
            OverviewProxyService.this.notifyAssistantProgress(f);
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void onAssistantGestureCompletion(float f) {
            if (verifyCaller("onAssistantGestureCompletion")) {
                long clearCallingIdentity = Binder.clearCallingIdentity();
                try {
                    OverviewProxyService.this.mHandler.post(new Runnable(f) { // from class: com.android.systemui.recents.-$$Lambda$OverviewProxyService$1$HAkY0KGHUHzLhPvJ8yVUB3VnkYU
                        public final /* synthetic */ float f$1;

                        {
                            this.f$1 = r2;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            OverviewProxyService.AnonymousClass1.this.lambda$onAssistantGestureCompletion$7$OverviewProxyService$1(this.f$1);
                        }
                    });
                } finally {
                    Binder.restoreCallingIdentity(clearCallingIdentity);
                }
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onAssistantGestureCompletion$7 */
        public /* synthetic */ void lambda$onAssistantGestureCompletion$7$OverviewProxyService$1(float f) {
            OverviewProxyService.this.notifyAssistantGestureCompletion(f);
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void startAssistant(Bundle bundle) {
            if (verifyCaller("startAssistant")) {
                StatusBar phoneStatusBar = OpLsState.getInstance().getPhoneStatusBar();
                if (phoneStatusBar == null || !phoneStatusBar.checkGestureStartAssist(bundle)) {
                    long clearCallingIdentity = Binder.clearCallingIdentity();
                    try {
                        OverviewProxyService.this.mHandler.post(new Runnable(bundle) { // from class: com.android.systemui.recents.-$$Lambda$OverviewProxyService$1$KKlukCn2icvgSOzmHsTu-W3lzwM
                            public final /* synthetic */ Bundle f$1;

                            {
                                this.f$1 = r2;
                            }

                            @Override // java.lang.Runnable
                            public final void run() {
                                OverviewProxyService.AnonymousClass1.this.lambda$startAssistant$8$OverviewProxyService$1(this.f$1);
                            }
                        });
                    } finally {
                        Binder.restoreCallingIdentity(clearCallingIdentity);
                    }
                }
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$startAssistant$8 */
        public /* synthetic */ void lambda$startAssistant$8$OverviewProxyService$1(Bundle bundle) {
            OverviewProxyService.this.notifyStartAssistant(bundle);
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public Bundle monitorGestureInput(String str, int i) {
            if (!verifyCaller("monitorGestureInput")) {
                return null;
            }
            long clearCallingIdentity = Binder.clearCallingIdentity();
            try {
                Parcelable monitorGestureInput = InputManager.getInstance().monitorGestureInput(str, i);
                Bundle bundle = new Bundle();
                bundle.putParcelable("extra_input_monitor", monitorGestureInput);
                return bundle;
            } finally {
                Binder.restoreCallingIdentity(clearCallingIdentity);
            }
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void notifyAccessibilityButtonClicked(int i) {
            if (verifyCaller("notifyAccessibilityButtonClicked")) {
                long clearCallingIdentity = Binder.clearCallingIdentity();
                try {
                    AccessibilityManager.getInstance(OverviewProxyService.this.mContext).notifyAccessibilityButtonClicked(i);
                } finally {
                    Binder.restoreCallingIdentity(clearCallingIdentity);
                }
            }
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void notifyAccessibilityButtonLongClicked() {
            if (verifyCaller("notifyAccessibilityButtonLongClicked")) {
                long clearCallingIdentity = Binder.clearCallingIdentity();
                try {
                    Intent intent = new Intent("com.android.internal.intent.action.CHOOSE_ACCESSIBILITY_BUTTON");
                    intent.setClassName("android", AccessibilityButtonChooserActivity.class.getName());
                    intent.addFlags(268468224);
                    OverviewProxyService.this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
                } finally {
                    Binder.restoreCallingIdentity(clearCallingIdentity);
                }
            }
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void setShelfHeight(boolean z, int i) {
            if (verifyCaller("setShelfHeight")) {
                long clearCallingIdentity = Binder.clearCallingIdentity();
                try {
                    OverviewProxyService.this.mPipUI.setShelfHeight(z, i);
                } finally {
                    Binder.restoreCallingIdentity(clearCallingIdentity);
                }
            }
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void setSplitScreenMinimized(boolean z) {
            Divider divider = (Divider) OverviewProxyService.this.mDividerOptional.get();
            if (divider != null) {
                divider.setMinimized(z);
            }
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void notifySwipeToHomeFinished() {
            if (verifyCaller("notifySwipeToHomeFinished")) {
                long clearCallingIdentity = Binder.clearCallingIdentity();
                try {
                    OverviewProxyService.this.mPipUI.setPinnedStackAnimationType(1);
                } finally {
                    Binder.restoreCallingIdentity(clearCallingIdentity);
                }
            }
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void setPinnedStackAnimationListener(IPinnedStackAnimationListener iPinnedStackAnimationListener) {
            if (verifyCaller("setPinnedStackAnimationListener")) {
                long clearCallingIdentity = Binder.clearCallingIdentity();
                try {
                    OverviewProxyService.this.mPipUI.setPinnedStackAnimationListener(iPinnedStackAnimationListener);
                } finally {
                    Binder.restoreCallingIdentity(clearCallingIdentity);
                }
            }
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void notifyGestureStarted() {
            Log.d("OverviewProxyService", "notifyGestureStarted");
            if (verifyCaller("notifyGestureStarted")) {
                long clearCallingIdentity = Binder.clearCallingIdentity();
                try {
                    OverviewProxyService.this.mHandler.post(new Runnable() { // from class: com.android.systemui.recents.-$$Lambda$OverviewProxyService$1$xSGAkQ2x1NF4-4ZNWR0YkXgmedw
                        @Override // java.lang.Runnable
                        public final void run() {
                            OverviewProxyService.AnonymousClass1.this.lambda$notifyGestureStarted$9$OverviewProxyService$1();
                        }
                    });
                } finally {
                    Binder.restoreCallingIdentity(clearCallingIdentity);
                }
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$notifyGestureStarted$9 */
        public /* synthetic */ void lambda$notifyGestureStarted$9$OverviewProxyService$1() {
            IFingerprintService asInterface = IFingerprintService.Stub.asInterface(ServiceManager.getService("fingerprint"));
            if (asInterface != null) {
                try {
                    String authenticatedPackage = asInterface.getAuthenticatedPackage();
                    Log.d("OverviewProxyService", "authenticatingPkg = " + authenticatedPackage);
                    if (!TextUtils.isEmpty(authenticatedPackage) && !"com.android.systemui".equals(authenticatedPackage)) {
                        OverviewProxyService.this.mAuthenticatingPackage = authenticatedPackage;
                        asInterface.updateStatus(12);
                    }
                } catch (RemoteException e) {
                    Log.w("OverviewProxyService", "notifyGestureStarted" + e);
                }
            } else {
                Log.w("OverviewProxyService", "notifyGestureStarted: ifp is null");
            }
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void notifyGestureEnded(int i) {
            Log.d("OverviewProxyService", "notifyGestureEnded: action = " + i + ", mAuthenticatingPackage = " + OverviewProxyService.this.mAuthenticatingPackage);
            if (verifyCaller("notifyGestureEnded")) {
                long clearCallingIdentity = Binder.clearCallingIdentity();
                try {
                    OverviewProxyService.this.mHandler.post(new Runnable(i) { // from class: com.android.systemui.recents.-$$Lambda$OverviewProxyService$1$uJ5MW04d5AkZdh5cPGG_oLQkx2E
                        public final /* synthetic */ int f$1;

                        {
                            this.f$1 = r2;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            OverviewProxyService.AnonymousClass1.this.lambda$notifyGestureEnded$10$OverviewProxyService$1(this.f$1);
                        }
                    });
                } finally {
                    Binder.restoreCallingIdentity(clearCallingIdentity);
                }
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$notifyGestureEnded$10 */
        public /* synthetic */ void lambda$notifyGestureEnded$10$OverviewProxyService$1(int i) {
            IFingerprintService asInterface;
            try {
                if (OverviewProxyService.this.mAuthenticatingPackage != null && !"com.android.systemui".equals(OverviewProxyService.this.mAuthenticatingPackage) && ((i == 3 || i == 50) && (asInterface = IFingerprintService.Stub.asInterface(ServiceManager.getService("fingerprint"))) != null)) {
                    asInterface.updateStatus(11);
                }
                OverviewProxyService.this.mAuthenticatingPackage = null;
            } catch (RemoteException e) {
                Log.w("OverviewProxyService", "notifyGestureEnded" + e);
            }
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void notifyLeaveOneHandMode() {
            if (verifyCaller("notifyLeaveOneHandMode")) {
                long clearCallingIdentity = Binder.clearCallingIdentity();
                try {
                    OverviewProxyService.this.mHandler.post(new Runnable() { // from class: com.android.systemui.recents.-$$Lambda$OverviewProxyService$1$JDlTwe6K_b4ivU-L9x4RUBmfj8Y
                        @Override // java.lang.Runnable
                        public final void run() {
                            OverviewProxyService.AnonymousClass1.this.lambda$notifyLeaveOneHandMode$11$OverviewProxyService$1();
                        }
                    });
                } finally {
                    Binder.restoreCallingIdentity(clearCallingIdentity);
                }
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$notifyLeaveOneHandMode$11 */
        public /* synthetic */ void lambda$notifyLeaveOneHandMode$11$OverviewProxyService$1() {
            OverviewProxyService.this.notifyLeaveOneHand();
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public int getFullScreenNavBarInset(String str) {
            int i = 0;
            if (!verifyCaller("getFullScreenNavBarInset")) {
                return 0;
            }
            long clearCallingIdentity = Binder.clearCallingIdentity();
            try {
                if (OpSystemUIInjector.isInNavGestureFullscreenList(str)) {
                    i = OverviewProxyService.this.mContext.getResources().getDimensionPixelSize(17105327);
                }
                return i;
            } finally {
                Binder.restoreCallingIdentity(clearCallingIdentity);
            }
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void onQuickSwitchToNewTask(int i) {
            if (verifyCaller("onQuickSwitchToNewTask")) {
                long clearCallingIdentity = Binder.clearCallingIdentity();
                try {
                    OverviewProxyService.this.mHandler.post(new Runnable(i) { // from class: com.android.systemui.recents.-$$Lambda$OverviewProxyService$1$REjY1PmxQXr3V_EXXZFFaE_W1eM
                        public final /* synthetic */ int f$1;

                        {
                            this.f$1 = r2;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            OverviewProxyService.AnonymousClass1.this.lambda$onQuickSwitchToNewTask$12$OverviewProxyService$1(this.f$1);
                        }
                    });
                } finally {
                    Binder.restoreCallingIdentity(clearCallingIdentity);
                }
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onQuickSwitchToNewTask$12 */
        public /* synthetic */ void lambda$onQuickSwitchToNewTask$12$OverviewProxyService$1(int i) {
            OverviewProxyService.this.notifyQuickSwitchToNewTask(i);
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void handleImageBundleAsScreenshot(Bundle bundle, Rect rect, Insets insets, Task$TaskKey task$TaskKey) {
            OverviewProxyService.this.mScreenshotHelper.provideScreenshot(bundle, rect, insets, task$TaskKey.id, task$TaskKey.userId, task$TaskKey.sourceComponent, 3, OverviewProxyService.this.mHandler, (Consumer) null);
        }

        private boolean verifyCaller(String str) {
            int identifier = Binder.getCallingUserHandle().getIdentifier();
            if (identifier == OverviewProxyService.this.mCurrentBoundedUserId) {
                return true;
            }
            Log.w("OverviewProxyService", "Launcher called sysui with invalid user: " + identifier + ", reason: " + str);
            return false;
        }
    };
    private SysUiState mSysUiState;
    private Handler mTouchHandler = new Handler(Looper.myLooper(), null, true) { // from class: com.android.systemui.recents.OverviewProxyService.5
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 1) {
                OverviewProxyService.this.dispatchLauncherTouch((MotionEvent) message.obj);
            } else if (i == 2) {
                OverviewProxyService.this.dispatchCancelTouch();
            }
        }
    };
    private float mWindowCornerRadius;

    public interface OverviewProxyListener {
        default void onAssistantGestureCompletion(float f) {
        }

        default void onAssistantProgress(float f) {
        }

        default void onConnectionChanged(boolean z) {
        }

        default void onNavBarButtonAlphaChanged(float f, boolean z) {
        }

        default void onOverviewShown(boolean z) {
        }

        default void onQuickSwitchToNewTask(int i) {
        }

        default void onToggleRecentApps() {
        }

        default void startAssistant(Bundle bundle) {
        }
    }

    public interface OverviewProxyOneHandedListener {
        default void leaveOneHand() {
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$OverviewProxyService() {
        Log.w("OverviewProxyService", "Binder supposed established connection but actual connection to service timed out, trying again");
        retryConnectionWithBackoff();
    }

    public OverviewProxyService(Context context, CommandQueue commandQueue, NavigationBarController navigationBarController, NavigationModeController navigationModeController, NotificationShadeWindowController notificationShadeWindowController, SysUiState sysUiState, PipUI pipUI, Optional<Divider> optional, Optional<Lazy<StatusBar>> optional2, BroadcastDispatcher broadcastDispatcher) {
        super(broadcastDispatcher);
        this.mContext = context;
        this.mPipUI = pipUI;
        this.mStatusBarOptionalLazy = optional2;
        this.mHandler = new Handler();
        this.mNavBarController = navigationBarController;
        this.mStatusBarWinController = notificationShadeWindowController;
        this.mConnectionBackoffAttempts = 0;
        this.mDividerOptional = optional;
        this.mRecentsComponentName = ComponentName.unflattenFromString(context.getString(17039953));
        this.mQuickStepIntent = new Intent("android.intent.action.QUICKSTEP_SERVICE").setPackage(this.mRecentsComponentName.getPackageName());
        this.mWindowCornerRadius = ScreenDecorationsUtils.getWindowCornerRadius(this.mContext.getResources());
        this.mSupportsRoundedCornersOnWindows = ScreenDecorationsUtils.supportsRoundedCornersOnWindows(this.mContext.getResources());
        this.mSysUiState = sysUiState;
        sysUiState.addCallback(new SysUiState.SysUiStateCallback() { // from class: com.android.systemui.recents.-$$Lambda$OverviewProxyService$UsZDbsgQ2Qpz6L03F4-TRLuFj_w
            @Override // com.android.systemui.model.SysUiState.SysUiStateCallback
            public final void onSystemUiStateChanged(int i) {
                OverviewProxyService.this.notifySystemUiStateFlags(i);
            }
        });
        this.mNavBarButtonAlpha = 1.0f;
        this.mNavBarMode = navigationModeController.addListener(this);
        IntentFilter intentFilter = new IntentFilter("android.intent.action.PACKAGE_ADDED");
        intentFilter.addDataScheme("package");
        intentFilter.addDataSchemeSpecificPart(this.mRecentsComponentName.getPackageName(), 0);
        intentFilter.addAction("android.intent.action.PACKAGE_CHANGED");
        this.mContext.registerReceiver(this.mLauncherStateChangedReceiver, intentFilter);
        notificationShadeWindowController.registerCallback(this.mStatusBarWindowCallback);
        this.mScreenshotHelper = new ScreenshotHelper(context);
        commandQueue.addCallback((CommandQueue.Callbacks) new CommandQueue.Callbacks() { // from class: com.android.systemui.recents.OverviewProxyService.4
            @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
            public void onTracingStateChanged(boolean z) {
                SysUiState sysUiState2 = OverviewProxyService.this.mSysUiState;
                sysUiState2.setFlag(4096, z);
                sysUiState2.commitUpdate(OverviewProxyService.this.mContext.getDisplayId());
            }
        });
        startTracking();
        updateEnabledState();
        startConnectionToCurrentUser();
        this.mContentResolver = this.mContext.getContentResolver();
    }

    @Override // com.android.systemui.settings.CurrentUserTracker
    public void onUserSwitched(int i) {
        this.mConnectionBackoffAttempts = 0;
        internalConnectToCurrentUser();
    }

    public void notifyBackAction(boolean z, int i, int i2, boolean z2, boolean z3) {
        try {
            if (this.mOverviewProxy != null) {
                this.mOverviewProxy.onBackAction(z, i, i2, z2, z3);
            }
        } catch (RemoteException e) {
            Log.e("OverviewProxyService", "Failed to notify back action", e);
        }
    }

    public void updateSystemUiStateFlags() {
        NavigationBarFragment defaultNavigationBarFragment = this.mNavBarController.getDefaultNavigationBarFragment();
        NavigationBarView navigationBarView = this.mNavBarController.getNavigationBarView(this.mContext.getDisplayId());
        if (defaultNavigationBarFragment != null) {
            defaultNavigationBarFragment.updateSystemUiStateFlags(-1);
        }
        if (navigationBarView != null) {
            navigationBarView.updatePanelSystemUiStateFlags();
            navigationBarView.updateDisabledSystemUiStateFlags();
        }
        NotificationShadeWindowController notificationShadeWindowController = this.mStatusBarWinController;
        if (notificationShadeWindowController != null) {
            notificationShadeWindowController.notifyStateChangedCallbacks();
        }
        notifySystemUiStateFlags(this.mSysUiState.getFlags());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    public void notifySystemUiStateFlags(int i) {
        StatusBar phoneStatusBar = OpLsState.getInstance().getPhoneStatusBar();
        if (phoneStatusBar != null && phoneStatusBar.getStatusBarWindowState() == 2) {
            i |= QuickStepContract.SYSUI_STATE_STATUS_BAR_HIDDEN;
        }
        try {
            if (this.mOverviewProxy != null) {
                Log.d("OverviewProxyService", "SystemUi flags: " + Integer.toHexString(i));
                this.mOverviewProxy.onSystemUiStateChanged(i);
            }
        } catch (RemoteException e) {
            Log.e("OverviewProxyService", "Failed to notify sysui state change", e);
        }
    }

    public void notifyKeyguardDone() {
        try {
            if (this.mOverviewProxy != null) {
                this.mOverviewProxy.onKeyguardDone();
                Log.d("OverviewProxyService", "notifyKeyguardDone");
            }
        } catch (RemoteException e) {
            Log.e("OverviewProxyService", "Failed to notify keyguard done", e);
        }
    }

    /* access modifiers changed from: private */
    public void onStatusBarStateChanged(boolean z, boolean z2, boolean z3) {
        SysUiState sysUiState = this.mSysUiState;
        boolean z4 = true;
        sysUiState.setFlag(64, z && !z2);
        if (!z || !z2) {
            z4 = false;
        }
        sysUiState.setFlag(512, z4);
        sysUiState.setFlag(8, z3);
        sysUiState.commitUpdate(this.mContext.getDisplayId());
    }

    public void onActiveNavBarRegionChanges(Region region) {
        this.mActiveNavBarRegion = region;
        dispatchNavButtonBounds();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchNavButtonBounds() {
        Region region;
        IOverviewProxy iOverviewProxy = this.mOverviewProxy;
        if (iOverviewProxy != null && (region = this.mActiveNavBarRegion) != null) {
            try {
                iOverviewProxy.onActiveNavBarRegionChanges(region);
            } catch (RemoteException e) {
                Log.e("OverviewProxyService", "Failed to call onActiveNavBarRegionChanges()", e);
            }
        }
    }

    public void cleanupAfterDeath() {
        if (this.mInputFocusTransferStarted) {
            this.mHandler.post(new Runnable() { // from class: com.android.systemui.recents.-$$Lambda$OverviewProxyService$PSR8w04DgkmYl0QS7DaTBJbM_iU
                @Override // java.lang.Runnable
                public final void run() {
                    OverviewProxyService.this.lambda$cleanupAfterDeath$2$OverviewProxyService();
                }
            });
        }
        if (this.mStatusBarGestureDownEvent != null) {
            this.mHandler.post(new Runnable() { // from class: com.android.systemui.recents.-$$Lambda$OverviewProxyService$VRFcZ95NRiYdJpC4k7VGsDGQNaE
                @Override // java.lang.Runnable
                public final void run() {
                    OverviewProxyService.this.lambda$cleanupAfterDeath$4$OverviewProxyService();
                }
            });
        }
        startConnectionToCurrentUser();
        if (this.mDividerOptional.get() != null) {
            Log.d("OverviewProxyService", "cleanupAfterDeath");
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$cleanupAfterDeath$2 */
    public /* synthetic */ void lambda$cleanupAfterDeath$2$OverviewProxyService() {
        this.mStatusBarOptionalLazy.ifPresent(new Consumer() { // from class: com.android.systemui.recents.-$$Lambda$OverviewProxyService$r1ukwXYi8j1mxwBUvifNk9B4ue4
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                OverviewProxyService.this.lambda$cleanupAfterDeath$1$OverviewProxyService((Lazy) obj);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$cleanupAfterDeath$1 */
    public /* synthetic */ void lambda$cleanupAfterDeath$1$OverviewProxyService(Lazy lazy) {
        this.mInputFocusTransferStarted = false;
        ((StatusBar) lazy.get()).onInputFocusTransfer(false, true, 0.0f);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$cleanupAfterDeath$4 */
    public /* synthetic */ void lambda$cleanupAfterDeath$4$OverviewProxyService() {
        this.mStatusBarOptionalLazy.ifPresent(new Consumer() { // from class: com.android.systemui.recents.-$$Lambda$OverviewProxyService$LIT7PecpH_WUNVrTU-kqXthWWwk
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                OverviewProxyService.this.lambda$cleanupAfterDeath$3$OverviewProxyService((Lazy) obj);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$cleanupAfterDeath$3 */
    public /* synthetic */ void lambda$cleanupAfterDeath$3$OverviewProxyService(Lazy lazy) {
        StatusBar statusBar = (StatusBar) lazy.get();
        if (statusBar != null) {
            try {
                this.mStatusBarGestureDownEvent.setAction(3);
                statusBar.dispatchNotificationsPanelTouchEvent(this.mStatusBarGestureDownEvent);
                this.mStatusBarGestureDownEvent.recycle();
                this.mStatusBarGestureDownEvent = null;
            } catch (NullPointerException e) {
                Log.e("OverviewProxyService", "StatusBarGestureDownEvent is null ", e);
            }
        }
    }

    public void startConnectionToCurrentUser() {
        if (this.mHandler.getLooper() != Looper.myLooper()) {
            this.mHandler.post(this.mConnectionRunnable);
        } else {
            internalConnectToCurrentUser();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchLauncherTouch(MotionEvent motionEvent) {
        this.mStatusBarOptionalLazy.ifPresent(new Consumer(motionEvent) { // from class: com.android.systemui.recents.-$$Lambda$OverviewProxyService$FWXo0MqS9edx71sMZ4zb9k8Offc
            public final /* synthetic */ MotionEvent f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                OverviewProxyService.this.lambda$dispatchLauncherTouch$5$OverviewProxyService(this.f$1, (Lazy) obj);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$dispatchLauncherTouch$5 */
    public /* synthetic */ void lambda$dispatchLauncherTouch$5$OverviewProxyService(MotionEvent motionEvent, Lazy lazy) {
        StatusBar statusBar = (StatusBar) lazy.get();
        if (statusBar != null && motionEvent != null && statusBar != null && motionEvent != null) {
            statusBar.dispatchNotificationsPanelTouchEvent(motionEvent);
            int actionMasked = motionEvent.getActionMasked();
            if (actionMasked == 0) {
                this.mStatusBarGestureDownEvent = MotionEvent.obtain(motionEvent);
            }
            boolean z = true;
            if (actionMasked == 1 || actionMasked == 3) {
                StringBuilder sb = new StringBuilder();
                sb.append("dispatchLauncherTouch, event:");
                if (this.mStatusBarGestureDownEvent == null) {
                    z = false;
                }
                sb.append(z);
                sb.append(", ");
                sb.append(actionMasked);
                Log.d("OverviewProxyService", sb.toString());
                MotionEvent motionEvent2 = this.mStatusBarGestureDownEvent;
                if (motionEvent2 != null) {
                    motionEvent2.recycle();
                    this.mStatusBarGestureDownEvent = null;
                }
            }
            motionEvent.recycle();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchCancelTouch() {
        this.mStatusBarOptionalLazy.ifPresent(new Consumer() { // from class: com.android.systemui.recents.-$$Lambda$OverviewProxyService$WTIImBC7StgC-546kSb0MM6WeV8
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                OverviewProxyService.this.lambda$dispatchCancelTouch$6$OverviewProxyService((Lazy) obj);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$dispatchCancelTouch$6 */
    public /* synthetic */ void lambda$dispatchCancelTouch$6$OverviewProxyService(Lazy lazy) {
        StatusBar statusBar = (StatusBar) lazy.get();
        StringBuilder sb = new StringBuilder();
        sb.append("dispatchCancelTouch, event:");
        sb.append(this.mStatusBarGestureDownEvent != null);
        Log.d("OverviewProxyService", sb.toString());
        if (statusBar != null && this.mStatusBarGestureDownEvent != null) {
            System.out.println("MERONG dispatchNotificationPanelTouchEvent");
            this.mStatusBarGestureDownEvent.setAction(3);
            statusBar.dispatchNotificationsPanelTouchEvent(this.mStatusBarGestureDownEvent);
            this.mStatusBarGestureDownEvent.recycle();
            this.mStatusBarGestureDownEvent = null;
        }
    }

    /* access modifiers changed from: private */
    public void internalConnectToCurrentUser() {
        disconnectFromLauncherService();
        if (!isEnabled()) {
            Log.v("OverviewProxyService", "Cannot attempt connection, is enabled " + isEnabled());
            return;
        }
        this.mHandler.removeCallbacks(this.mConnectionRunnable);
        try {
            this.mBound = this.mContext.bindServiceAsUser(new Intent("android.intent.action.QUICKSTEP_SERVICE").setPackage(this.mRecentsComponentName.getPackageName()), this.mOverviewServiceConnection, 33554433, UserHandle.of(getCurrentUserId()));
        } catch (SecurityException e) {
            Log.e("OverviewProxyService", "Unable to bind because of security error", e);
        }
        if (this.mBound) {
            this.mHandler.postDelayed(this.mDeferredConnectionCallback, 5000);
        } else {
            retryConnectionWithBackoff();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void retryConnectionWithBackoff() {
        if (!this.mHandler.hasCallbacks(this.mConnectionRunnable)) {
            long min = (long) Math.min(Math.scalb(1000.0f, this.mConnectionBackoffAttempts), 600000.0f);
            this.mHandler.postDelayed(this.mConnectionRunnable, min);
            this.mConnectionBackoffAttempts++;
            Log.w("OverviewProxyService", "Failed to connect on attempt " + this.mConnectionBackoffAttempts + " will try again in " + min + "ms");
        }
    }

    public void addCallback(OverviewProxyListener overviewProxyListener) {
        this.mConnectionCallbacks.add(overviewProxyListener);
        overviewProxyListener.onConnectionChanged(this.mOverviewProxy != null);
        overviewProxyListener.onNavBarButtonAlphaChanged(this.mNavBarButtonAlpha, false);
    }

    public void removeCallback(OverviewProxyListener overviewProxyListener) {
        this.mConnectionCallbacks.remove(overviewProxyListener);
    }

    public boolean shouldShowSwipeUpUI() {
        return isEnabled() && !QuickStepContract.isLegacyMode(this.mNavBarMode);
    }

    public boolean isEnabled() {
        return this.mIsEnabled;
    }

    public IOverviewProxy getProxy() {
        return this.mOverviewProxy;
    }

    public ISystemUiProxy getSysUIProxy() {
        return this.mSysUiProxy;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void disconnectFromLauncherService() {
        if (this.mBound) {
            this.mContext.unbindService(this.mOverviewServiceConnection);
            this.mBound = false;
        }
        IOverviewProxy iOverviewProxy = this.mOverviewProxy;
        if (iOverviewProxy != null) {
            iOverviewProxy.asBinder().unlinkToDeath(this.mOverviewServiceDeathRcpt, 0);
            this.mOverviewProxy = null;
            notifyNavBarButtonAlphaChanged(1.0f, false);
            notifyConnectionChanged();
        }
    }

    public void notifyNavBarButtonAlphaChanged(float f, boolean z) {
        for (int size = this.mConnectionCallbacks.size() - 1; size >= 0; size--) {
            this.mConnectionCallbacks.get(size).onNavBarButtonAlphaChanged(f, z);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyConnectionChanged() {
        for (int size = this.mConnectionCallbacks.size() - 1; size >= 0; size--) {
            this.mConnectionCallbacks.get(size).onConnectionChanged(this.mOverviewProxy != null);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyQuickSwitchToNewTask(int i) {
        for (int size = this.mConnectionCallbacks.size() - 1; size >= 0; size--) {
            this.mConnectionCallbacks.get(size).onQuickSwitchToNewTask(i);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyAssistantProgress(float f) {
        for (int size = this.mConnectionCallbacks.size() - 1; size >= 0; size--) {
            this.mConnectionCallbacks.get(size).onAssistantProgress(f);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyAssistantGestureCompletion(float f) {
        for (int size = this.mConnectionCallbacks.size() - 1; size >= 0; size--) {
            this.mConnectionCallbacks.get(size).onAssistantGestureCompletion(f);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyStartAssistant(Bundle bundle) {
        for (int size = this.mConnectionCallbacks.size() - 1; size >= 0; size--) {
            this.mConnectionCallbacks.get(size).startAssistant(bundle);
        }
    }

    public void notifySplitScreenBoundsChanged(Rect rect, Rect rect2) {
        try {
            if (this.mOverviewProxy != null) {
                this.mOverviewProxy.onSplitScreenSecondaryBoundsChanged(rect, rect2);
            } else {
                Log.e("OverviewProxyService", "Failed to get overview proxy for split screen bounds.");
            }
        } catch (RemoteException e) {
            Log.e("OverviewProxyService", "Failed to call onSplitScreenSecondaryBoundsChanged()", e);
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyToggleRecentApps() {
        for (int size = this.mConnectionCallbacks.size() - 1; size >= 0; size--) {
            this.mConnectionCallbacks.get(size).onToggleRecentApps();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateEnabledState() {
        this.mIsEnabled = this.mContext.getPackageManager().resolveServiceAsUser(this.mQuickStepIntent, 1048576, ActivityManagerWrapper.getInstance().getCurrentUserId()) != null;
    }

    @Override // com.android.systemui.statusbar.phone.NavigationModeController.ModeChangedListener
    public void onNavigationModeChanged(int i) {
        this.mNavBarMode = i;
    }

    public int getNavBarMode() {
        return this.mNavBarMode;
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("OverviewProxyService state:");
        printWriter.print("  recentsComponentName=");
        printWriter.println(this.mRecentsComponentName);
        printWriter.print("  isConnected=");
        printWriter.println(this.mOverviewProxy != null);
        printWriter.print("  connectionBackoffAttempts=");
        printWriter.println(this.mConnectionBackoffAttempts);
        printWriter.print("  quickStepIntent=");
        printWriter.println(this.mQuickStepIntent);
        printWriter.print("  quickStepIntentResolved=");
        printWriter.println(isEnabled());
        this.mSysUiState.dump(fileDescriptor, printWriter, strArr);
        printWriter.print(" mInputFocusTransferStarted=");
        printWriter.println(this.mInputFocusTransferStarted);
    }

    public void addOneHandListener(OverviewProxyOneHandedListener overviewProxyOneHandedListener) {
        Log.d("OverviewProxyService", "Overviewproxy addOneHandListener");
        this.mOneHandListener = overviewProxyOneHandedListener;
    }

    public void updateSystemUIStateFlagsInternal() {
        NavigationBarView navigationBarView = this.mNavBarController.getNavigationBarView(this.mContext.getDisplayId());
        StatusBar phoneStatusBar = OpLsState.getInstance().getPhoneStatusBar();
        this.mStatusBar = phoneStatusBar;
        if (navigationBarView == null && phoneStatusBar != null) {
            int displayId = this.mContext.getDisplayId();
            if (OpUtils.DEBUG_ONEPLUS) {
                Log.d("OverviewProxyService", "Update system ui flag state to launcher when navigation bar is hidden");
            }
            int disableFlag = this.mStatusBar.getDisableFlag();
            SysUiState sysUiState = this.mSysUiState;
            boolean z = false;
            sysUiState.setFlag(128, (16777216 & disableFlag) != 0);
            sysUiState.commitUpdate(displayId);
            SysUiState sysUiState2 = this.mSysUiState;
            sysUiState2.setFlag(256, (disableFlag & 2097152) != 0);
            sysUiState2.commitUpdate(displayId);
            NavigationBarFragment defaultNavigationBarFragment = this.mNavBarController.getDefaultNavigationBarFragment();
            boolean isNavBarWindowVisible = defaultNavigationBarFragment != null ? defaultNavigationBarFragment.isNavBarWindowVisible() : true;
            boolean z2 = (this.mStatusBar.getNavigationBarHiddenMode() == 1 || !EdgeBackGestureHandler.sSideGestureEnabled) && QuickStepContract.isGesturalMode(this.mNavBarMode);
            if (OpUtils.DEBUG_ONEPLUS) {
                Log.d("OverviewProxyService", "Update nav bar is hidden, navBarWindowVidsble:" + isNavBarWindowVisible + ", navBarHidde:" + z2);
            }
            SysUiState sysUiState3 = this.mSysUiState;
            if (!isNavBarWindowVisible && !z2) {
                z = true;
            }
            sysUiState3.setFlag(2, z);
            sysUiState3.commitUpdate(displayId);
            if (this.mStatusBar.getPanelController() != null) {
                SysUiState sysUiState4 = this.mSysUiState;
                sysUiState4.setFlag(4, this.mStatusBar.getPanelController().isFullyExpanded());
                sysUiState4.commitUpdate(displayId);
            }
            notifySystemUiStateFlags(this.mSysUiState.getFlags());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyLeaveOneHand() {
        StringBuilder sb = new StringBuilder();
        sb.append("Overviewproxy notifyLeaveOneHand  mOneHandListener is null ");
        sb.append(this.mOneHandListener == null);
        Log.d("OverviewProxyService", sb.toString());
        OverviewProxyOneHandedListener overviewProxyOneHandedListener = this.mOneHandListener;
        if (overviewProxyOneHandedListener != null) {
            overviewProxyOneHandedListener.leaveOneHand();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendAssistantAvailabilityInternal() {
        boolean z = true;
        boolean z2 = ((AssistManager) Dependency.get(AssistManager.class)).getAssistInfoForUser(-2) != null;
        if (getProxy() != null) {
            try {
                Log.d("OverviewProxyService", "Send assistant availability data to launcher " + z2);
                IOverviewProxy proxy = getProxy();
                if (!z2 || !QuickStepContract.isGesturalMode(this.mNavBarMode)) {
                    z = false;
                }
                proxy.onAssistantAvailable(z);
            } catch (RemoteException unused) {
                Log.w("OverviewProxyService", "Unable to send assistant availability data to launcher");
            }
        }
    }
}
