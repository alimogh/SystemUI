package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Build;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import android.view.IWindowManager;
import android.view.MotionEvent;
import com.android.systemui.statusbar.AutoHideUiElement;
public class AutoHideController {
    protected static final boolean DEBUG_ONEPLUS = Build.DEBUG_ONEPLUS;
    private final Runnable mAutoHide = new Runnable() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$AutoHideController$sJYAhc6qJ_sO_ZdtlpSd2BPK504
        @Override // java.lang.Runnable
        public final void run() {
            AutoHideController.this.lambda$new$0$AutoHideController();
        }
    };
    private boolean mAutoHideSuspended;
    private Context mContext;
    private int mDisplayId;
    private final Handler mHandler;
    private AutoHideUiElement mNavigationBar;
    private AutoHideUiElement mStatusBar;
    private boolean mSwapNavKeys = false;
    private ContentObserver mSwapNavKeysObserver;
    private final IWindowManager mWindowManagerService;

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$AutoHideController() {
        if (isAnyTransientBarShown()) {
            hideTransientBars();
        }
    }

    public AutoHideController(Context context, Handler handler, IWindowManager iWindowManager) {
        this.mHandler = handler;
        this.mWindowManagerService = iWindowManager;
        this.mDisplayId = context.getDisplayId();
        this.mContext = context;
        ContentObserver swapNavObserver = getSwapNavObserver(this.mHandler);
        this.mSwapNavKeysObserver = swapNavObserver;
        swapNavObserver.onChange(true);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("oem_acc_key_define"), true, this.mSwapNavKeysObserver, -1);
    }

    public void setStatusBar(AutoHideUiElement autoHideUiElement) {
        this.mStatusBar = autoHideUiElement;
    }

    public void setNavigationBar(AutoHideUiElement autoHideUiElement) {
        this.mNavigationBar = autoHideUiElement;
    }

    private void hideTransientBars() {
        try {
            this.mWindowManagerService.hideTransientBars(this.mDisplayId);
        } catch (RemoteException unused) {
            Log.w("AutoHideController", "Cannot get WindowManager");
        }
        AutoHideUiElement autoHideUiElement = this.mStatusBar;
        if (autoHideUiElement != null) {
            autoHideUiElement.hide();
        }
        AutoHideUiElement autoHideUiElement2 = this.mNavigationBar;
        if (autoHideUiElement2 != null) {
            autoHideUiElement2.hide();
        }
    }

    /* access modifiers changed from: package-private */
    public void resumeSuspendedAutoHide() {
        if (this.mAutoHideSuspended) {
            scheduleAutoHide();
            Runnable checkBarModesRunnable = getCheckBarModesRunnable();
            if (checkBarModesRunnable != null) {
                this.mHandler.postDelayed(checkBarModesRunnable, 500);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void suspendAutoHide() {
        this.mHandler.removeCallbacks(this.mAutoHide);
        Runnable checkBarModesRunnable = getCheckBarModesRunnable();
        if (checkBarModesRunnable != null) {
            this.mHandler.removeCallbacks(checkBarModesRunnable);
        }
        AutoHideUiElement autoHideUiElement = this.mStatusBar;
        if (autoHideUiElement == null || !autoHideUiElement.isHideNavBar()) {
            this.mAutoHideSuspended = isAnyTransientBarShown();
            return;
        }
        this.mHandler.post(this.mAutoHide);
        this.mAutoHideSuspended = false;
    }

    public void touchAutoHide() {
        if (isAnyTransientBarShown()) {
            scheduleAutoHide();
        } else {
            cancelAutoHide();
        }
    }

    private Runnable getCheckBarModesRunnable() {
        if (this.mStatusBar != null) {
            return new Runnable() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$AutoHideController$Dw54NegELGCFcbvVgChoOa9gkLA
                @Override // java.lang.Runnable
                public final void run() {
                    AutoHideController.this.lambda$getCheckBarModesRunnable$1$AutoHideController();
                }
            };
        }
        if (this.mNavigationBar != null) {
            return new Runnable() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$AutoHideController$FON87SM6b4--2jIBTAjBTcUbKIM
                @Override // java.lang.Runnable
                public final void run() {
                    AutoHideController.this.lambda$getCheckBarModesRunnable$2$AutoHideController();
                }
            };
        }
        return null;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$getCheckBarModesRunnable$1 */
    public /* synthetic */ void lambda$getCheckBarModesRunnable$1$AutoHideController() {
        this.mStatusBar.synchronizeState();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$getCheckBarModesRunnable$2 */
    public /* synthetic */ void lambda$getCheckBarModesRunnable$2$AutoHideController() {
        this.mNavigationBar.synchronizeState();
    }

    private void cancelAutoHide() {
        this.mAutoHideSuspended = false;
        this.mHandler.removeCallbacks(this.mAutoHide);
    }

    private void scheduleAutoHide() {
        cancelAutoHide();
        this.mHandler.postDelayed(this.mAutoHide, 2250);
    }

    /* access modifiers changed from: package-private */
    public void checkUserAutoHide(MotionEvent motionEvent) {
        boolean z = isAnyTransientBarShown() && motionEvent.getAction() == 4 && motionEvent.getX() == 0.0f && motionEvent.getY() == 0.0f;
        AutoHideUiElement autoHideUiElement = this.mStatusBar;
        if (autoHideUiElement != null) {
            z &= autoHideUiElement.shouldHideOnTouch();
        }
        AutoHideUiElement autoHideUiElement2 = this.mNavigationBar;
        if (autoHideUiElement2 != null) {
            z &= autoHideUiElement2.shouldHideOnTouch();
        }
        if (z) {
            userAutoHide();
        }
    }

    private void userAutoHide() {
        cancelAutoHide();
        if (DEBUG_ONEPLUS) {
            Log.i("AutoHideController", " userAutohide");
        }
        this.mHandler.postDelayed(this.mAutoHide, 350);
    }

    private boolean isAnyTransientBarShown() {
        AutoHideUiElement autoHideUiElement = this.mStatusBar;
        if (autoHideUiElement != null && autoHideUiElement.isVisible()) {
            return true;
        }
        AutoHideUiElement autoHideUiElement2 = this.mNavigationBar;
        if (autoHideUiElement2 == null || !autoHideUiElement2.isVisible()) {
            return false;
        }
        return true;
    }

    private ContentObserver getSwapNavObserver(Handler handler) {
        return new ContentObserver(handler) { // from class: com.android.systemui.statusbar.phone.AutoHideController.1
            @Override // android.database.ContentObserver
            public void onChange(boolean z) {
                boolean z2 = true;
                if (Settings.System.getInt(AutoHideController.this.mContext.getContentResolver(), "oem_acc_key_define", 0) != 1) {
                    z2 = false;
                }
                Log.d("AutoHideController", "swap key from " + AutoHideController.this.mSwapNavKeys + " to " + z2);
                if (AutoHideController.this.mSwapNavKeys != z2) {
                    AutoHideController.this.mSwapNavKeys = z2;
                    if (AutoHideController.this.mNavigationBar != null) {
                        AutoHideController.this.mNavigationBar.refreshLayout(0);
                    }
                }
            }
        };
    }
}
