package com.oneplus.systemui.keyguard;

import android.content.Context;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.os.Trace;
import android.provider.Settings;
import android.util.Log;
import android.util.Slog;
import com.android.internal.policy.IKeyguardStateCallback;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardViewController;
import com.android.systemui.Dependency;
import com.android.systemui.SystemUI;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.statusbar.phone.StatusBar;
import com.oneplus.onlineconfig.OpFingerprintConfig;
import com.oneplus.plugin.OpLsState;
import com.oneplus.util.OpReflectionUtils;
import com.oneplus.util.OpUtils;
import dagger.Lazy;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
public class OpKeyguardViewMediator extends SystemUI {
    public static int AUTHENTICATE_FACEUNLOCK = 2;
    public static int AUTHENTICATE_FINGERPRINT = 1;
    public static int AUTHENTICATE_IGNORE = 0;
    public static boolean DEBUG_THREAD = false;
    protected static final boolean IS_CUSTOM_FINGERPRINT = OpUtils.isCustomFingerprint();
    private int mAuthenticatingType = AUTHENTICATE_IGNORE;
    protected OpKeyguardUnlockCounter mKeyguardUnlockCounter;
    private int mLastAlpha = 1;
    private OpFingerprintConfig mOpFingerprintConfig;
    protected StatusBar mStatusBar;
    private KeyguardUpdateMonitor mUpdateMonitor;

    /* access modifiers changed from: protected */
    public class OpHandler extends Handler {
        public OpHandler(OpKeyguardViewMediator opKeyguardViewMediator, Looper looper, Handler.Callback callback, boolean z) {
            super(looper, callback, z);
        }
    }

    public OpKeyguardViewMediator(Context context) {
        super(context);
    }

    @Override // com.android.systemui.SystemUI
    public void start() {
        this.mUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        this.mStatusBar = (StatusBar) Dependency.get(StatusBar.class);
        this.mKeyguardUnlockCounter = new OpKeyguardUnlockCounter(this.mContext);
        this.mOpFingerprintConfig = new OpFingerprintConfig(this.mContext);
    }

    public void notifyScreenOffAuthenticate(boolean z, int i) {
        notifyScreenOffAuthenticate(z, i, 4);
    }

    public void notifyScreenOffAuthenticate(boolean z, int i, int i2) {
        int i3;
        Log.d("OpKeyguardViewMediator", "notifyAuthenticate Change: " + z + ", type:" + i + ", currentType:" + this.mAuthenticatingType + ", result:" + i2);
        if (!z) {
            if (IS_CUSTOM_FINGERPRINT && this.mAuthenticatingType == (i3 = AUTHENTICATE_FACEUNLOCK) && i != i3 && i2 == 5) {
                Log.d("OpKeyguardViewMediator", "not handle another Authenticate");
            }
            this.mAuthenticatingType = AUTHENTICATE_IGNORE;
        } else if (this.mAuthenticatingType != AUTHENTICATE_IGNORE) {
            Log.d("OpKeyguardViewMediator", "not handle another Authenticate");
            return;
        } else {
            this.mAuthenticatingType = i;
        }
        if (i2 != 7) {
            for (int size = getKeyguardStateCallback().size() - 1; size >= 0; size--) {
                try {
                    getKeyguardStateCallback().get(size).onFingerprintStateChange(z, i, i2, 0);
                } catch (RemoteException e) {
                    Slog.w("OpKeyguardViewMediator", "Failed to call onFingerprintStateChange", e);
                    if (e instanceof DeadObjectException) {
                        getKeyguardStateCallback().remove(size);
                    }
                }
            }
        }
        if (OpUtils.isCustomFingerprint()) {
            this.mUpdateMonitor.dispatchAuthenticateChanged(z, i, i2, 0);
        }
    }

    public boolean isScreenOffAuthenticating() {
        return this.mAuthenticatingType != 0;
    }

    public void changePanelAlpha(int i, int i2) {
        StatusBar phoneStatusBar = OpLsState.getInstance().getPhoneStatusBar();
        if (phoneStatusBar == null) {
            Log.d("OpKeyguardViewMediator", "mStatusBar is null");
        } else if (i > 0 && getKeyguardViewController().isUnlockWithWallpaper() && !isScreenOffAuthenticating()) {
            Log.d("OpKeyguardViewMediator", "not set backdrop alpha");
        } else if (i2 == AUTHENTICATE_IGNORE || !isScreenOffAuthenticating() || i2 == this.mAuthenticatingType) {
            Log.d("OpKeyguardViewMediator", "changePanelAlpha to " + i + ", type:" + i2 + ", currentType:" + this.mAuthenticatingType);
            float f = (float) i;
            phoneStatusBar.setPanelViewAlpha(f, false, this.mAuthenticatingType);
            phoneStatusBar.setWallpaperAlpha(f);
            this.mLastAlpha = i;
        } else {
            Log.d("OpKeyguardViewMediator", "return set alpha");
        }
    }

    public void notifyBarHeightChange(boolean z) {
        if (this.mLastAlpha == 0) {
            StatusBar phoneStatusBar = OpLsState.getInstance().getPhoneStatusBar();
            Log.d("OpKeyguardViewMediator", "recover alpha");
            if (phoneStatusBar != null) {
                phoneStatusBar.setWallpaperAlpha(1.0f);
                phoneStatusBar.setPanelViewAlpha(1.0f, false, -1);
            }
            this.mLastAlpha = 1;
        }
    }

    public void notifyPreventModeChange(boolean z) {
        for (int size = getKeyguardStateCallback().size() - 1; size >= 0; size--) {
            try {
                getKeyguardStateCallback().get(size).onPocketModeActiveChanged(z);
            } catch (RemoteException e) {
                Log.w("OpKeyguardViewMediator", "Failed to call onPocketModeActiveChanged", e);
                if (e instanceof DeadObjectException) {
                    getKeyguardStateCallback().remove(size);
                }
            }
        }
    }

    public void onWakeAndUnlocking(boolean z) {
        onWakeAndUnlocking();
        if (IS_CUSTOM_FINGERPRINT && z && !this.mUpdateMonitor.isFacelockUnlocking()) {
            changePanelAlpha(0, AUTHENTICATE_FINGERPRINT);
            StatusBar phoneStatusBar = OpLsState.getInstance().getPhoneStatusBar();
            if (phoneStatusBar != null) {
                phoneStatusBar.onWakingAndUnlocking();
            }
        }
    }

    public void onFinishedWakingUp() {
        Trace.beginSection("KeyguardViewMediator#onFinishedWakingUp");
        Log.d("OpKeyguardViewMediator", "onFinishedWakingUp");
        this.mUpdateMonitor.dispatchFinishedWakingUp();
        Trace.endSection();
    }

    /* access modifiers changed from: protected */
    public void opDump(PrintWriter printWriter, String[] strArr) {
        OpFingerprintConfig opFingerprintConfig = this.mOpFingerprintConfig;
        if (!(opFingerprintConfig == null || opFingerprintConfig.getAppUnsupportAccelerateList() == null)) {
            ArrayList appUnsupportAccelerateList = this.mOpFingerprintConfig.getAppUnsupportAccelerateList();
            printWriter.println("UnsupportAccelerateList:");
            Iterator it = appUnsupportAccelerateList.iterator();
            while (it.hasNext()) {
                printWriter.println((String) it.next());
            }
        }
        String str = "";
        for (String str2 : strArr) {
            str = str + " " + str2;
        }
        printWriter.println("# opDump #  args : " + str);
        Slog.v("OpKeyguardViewMediator", "# responseLogDump #  args : " + str);
        int i = 0;
        while (i < strArr.length) {
            String str3 = strArr[i];
            i++;
            if ("log".equals(str3)) {
                printWriter.println("# log #");
                if (i < strArr.length) {
                    String[] strArr2 = new String[(strArr.length - i)];
                    if (strArr.length > 2) {
                        System.arraycopy(strArr, i, strArr2, 0, strArr.length - i);
                    }
                    opResponseLogDump(printWriter, strArr2);
                } else {
                    return;
                }
            }
        }
    }

    private void opResponseLogDump(PrintWriter printWriter, String[] strArr) {
        String str = "";
        for (String str2 : strArr) {
            str = str + " " + str2;
        }
        printWriter.println("# opResponseLogDump #  args : " + str);
        Slog.v("OpKeyguardViewMediator", "# opResponseLogDump #  args : " + str);
        if (strArr.length != 1) {
            printWriter.println("# help #\n" + ("dumpsys activity service systemui com.android.systemui.keyguard.keyguardviewmediator --log on(true/false)\n"));
            return;
        }
        boolean equals = "true".equals(strArr[0]);
        Log.d("OpKeyguardViewMediator", " opResponseLogDump on:" + equals);
        DEBUG_THREAD = equals;
    }

    /* access modifiers changed from: protected */
    public boolean isSupportAccelerate() {
        OpFingerprintConfig opFingerprintConfig = this.mOpFingerprintConfig;
        if (opFingerprintConfig != null) {
            return opFingerprintConfig.isAppSupportAccelerate(OpUtils.getTopPackageName());
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void updateKeyguardDisable(boolean z) {
        Log.d("OpKeyguardViewMediator", "updateKeyguardDisable:" + z + " mUpdateMonitor.getCurrentUser():" + KeyguardUpdateMonitor.getCurrentUser());
        Settings.System.putIntForUser(this.mContext.getContentResolver(), "keyguard_disable", z ? 1 : 0, KeyguardUpdateMonitor.getCurrentUser());
    }

    private void onWakeAndUnlocking() {
        OpReflectionUtils.methodInvokeVoid(KeyguardViewMediator.class, this, "onWakeAndUnlocking", new Object[0]);
    }

    private ArrayList<IKeyguardStateCallback> getKeyguardStateCallback() {
        return (ArrayList) OpReflectionUtils.getValue(KeyguardViewMediator.class, this, "mKeyguardStateCallbacks");
    }

    private KeyguardViewController getKeyguardViewController() {
        return (KeyguardViewController) ((Lazy) OpReflectionUtils.getValue(KeyguardViewMediator.class, this, "mKeyguardViewControllerLazy")).get();
    }

    public OpKeyguardUnlockCounter getKeyguardUnlockCounter() {
        return this.mKeyguardUnlockCounter;
    }
}
