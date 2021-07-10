package com.oneplus.systemui.biometrics;

import android.content.Context;
import android.hardware.fingerprint.IFingerprintService;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.Dependency;
import java.util.ArrayList;
import java.util.Iterator;
public class OpFodHelper {
    private static OpFodHelper sInstance;
    private boolean mActionEnroll;
    private boolean mBiometricPromptVisible;
    private int mCookie;
    private String mCurrentClient;
    private FingerprintState mFingerprintState = FingerprintState.STOP;
    private String mLastClient;
    private ArrayList<OnFingerprintStateChangeListener> mOnFingerprintStateChangeListeners = new ArrayList<>();
    private ArrayList<OpFodIconVisibilityChangeListener> mOnFodIconVisibilityChangeListeners = new ArrayList<>();
    private IFingerprintService mService = IFingerprintService.Stub.asInterface(ServiceManager.getService("fingerprint"));
    private KeyguardUpdateMonitor mUpdateMonitor = ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class));

    /* access modifiers changed from: package-private */
    public enum FingerprintState {
        RUNNING,
        STOP,
        SUSPEND,
        LOCKOUT
    }

    public interface OnFingerprintStateChangeListener {
        void onFingerprintStateChanged();
    }

    public interface OpFodIconVisibilityChangeListener {
        void onFodIconVisibilityChange(boolean z);
    }

    public void setFodIconViewController(OpFodIconViewController opFodIconViewController) {
    }

    private OpFodHelper(Context context) {
    }

    public static void init(Context context) {
        synchronized (OpFodHelper.class) {
            if (sInstance == null) {
                sInstance = new OpFodHelper(context);
            } else {
                Log.w("OpFodHelper", "already init.");
            }
        }
    }

    public static OpFodHelper getInstance() {
        OpFodHelper opFodHelper = sInstance;
        if (opFodHelper != null) {
            return opFodHelper;
        }
        Log.e("OpFodHelper", "not initial yet, call init before.");
        throw new RuntimeException("not initial yet, call init before.");
    }

    public static boolean isSystemUI(String str) {
        return !TextUtils.isEmpty(str) && "com.android.systemui".equals(str);
    }

    public static boolean isForceShow(String str) {
        return !TextUtils.isEmpty(str) && "forceShow-keyguard".equals(str);
    }

    public static boolean isAppLocker(String str) {
        return !TextUtils.isEmpty(str) && "com.oneplus.applocker".equals(str);
    }

    public static boolean isSettings(String str) {
        return !TextUtils.isEmpty(str) && "com.android.settings".equals(str);
    }

    public static boolean isFileManager(String str) {
        return !TextUtils.isEmpty(str) && "com.oneplus.filemanager".equals(str);
    }

    public static boolean isKeyguard(String str) {
        return isSystemUI(str) || isForceShow(str);
    }

    public static boolean isSystemApp() {
        OpFodHelper opFodHelper = sInstance;
        if (opFodHelper == null) {
            return false;
        }
        String currentOwner = opFodHelper.getCurrentOwner();
        if (isKeyguard(currentOwner) || isSettings(currentOwner) || isAppLocker(currentOwner) || sInstance.isFromBiometricPrompt() || isFileManager(currentOwner)) {
            return true;
        }
        return false;
    }

    public void addFingerprintStateChangeListener(OnFingerprintStateChangeListener onFingerprintStateChangeListener) {
        synchronized (this.mOnFingerprintStateChangeListeners) {
            if (!this.mOnFingerprintStateChangeListeners.contains(onFingerprintStateChangeListener)) {
                this.mOnFingerprintStateChangeListeners.add(onFingerprintStateChangeListener);
            }
        }
    }

    public void handleQSExpandChanged(boolean z) {
        Log.d("OpFodHelper", "handleQSExpandChanged " + z);
        pauseOrResumeInner(z);
    }

    public void handleShutdownDialogVisibilityChanged(boolean z) {
        Log.d("OpFodHelper", "handleShutdownDialogVisibilityChanged " + z);
        pauseOrResumeInner(z);
    }

    private void pauseOrResumeInner(boolean z) {
        IFingerprintService iFingerprintService = this.mService;
        if (iFingerprintService != null) {
            try {
                String authenticatedPackage = iFingerprintService.getAuthenticatedPackage();
                if (TextUtils.isEmpty(authenticatedPackage)) {
                    Log.d("OpFodHelper", "empty client, do not handle it.");
                } else if (isKeyguard(authenticatedPackage)) {
                    Log.d("OpFodHelper", "keyguard client. do not handle it.");
                } else {
                    this.mService.updateStatus(z ? 12 : 11);
                }
            } catch (RemoteException e) {
                Log.e("OpFodHelper", "updateStatus occur remote exception", e);
            }
        } else {
            Log.d("OpFodHelper", "pauseOrResumeInner null pointer");
        }
    }

    public void updateOwner(Bundle bundle) {
        boolean z;
        boolean z2;
        boolean z3;
        int i;
        String str;
        boolean z4 = false;
        if (bundle != null) {
            str = bundle.getString("key_fingerprint_package_name", "");
            i = bundle.getInt("key_cookie", 0);
            z3 = bundle.getBoolean("key_resume", false);
            z2 = bundle.getBoolean("key_suspend", false);
            z = bundle.getBoolean("key_enroll", false);
        } else {
            str = null;
            z = false;
            i = 0;
            z3 = false;
            z2 = false;
        }
        this.mLastClient = this.mCurrentClient;
        this.mCurrentClient = str;
        this.mCookie = i;
        this.mBiometricPromptVisible = false;
        this.mActionEnroll = z;
        if (z3 && i != 0) {
            this.mBiometricPromptVisible = true;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("updateOwner: current= ");
        sb.append(this.mCurrentClient);
        sb.append(", last= ");
        sb.append(this.mLastClient);
        sb.append(", from BiometricPrompt? ");
        if (i != 0) {
            z4 = true;
        }
        sb.append(z4);
        sb.append(", resume= ");
        sb.append(z3);
        sb.append(", enroll= ");
        sb.append(this.mActionEnroll);
        Log.d("OpFodHelper", sb.toString());
        boolean equals = true ^ TextUtils.equals(str, this.mLastClient);
        if (TextUtils.isEmpty(str)) {
            changeState(FingerprintState.STOP, equals);
            return;
        }
        if (this.mUpdateMonitor == null) {
            this.mUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        }
        if (!this.mUpdateMonitor.isUnlockingWithBiometricAllowed() && !this.mActionEnroll) {
            changeState(FingerprintState.LOCKOUT, equals);
        } else if (z2) {
            changeState(FingerprintState.SUSPEND, equals);
        } else {
            changeState(FingerprintState.RUNNING, equals);
        }
    }

    public boolean updateBiometricPromptReady(int i) {
        if (this.mCookie != i) {
            Log.d("OpFodHelper", "updateBiometricPromptReady: cookie not matched");
            return false;
        }
        this.mBiometricPromptVisible = true;
        return true;
    }

    public boolean isFromBiometricPrompt() {
        return this.mCookie != 0;
    }

    public boolean isBiometricPromptReadyToShow() {
        return isFromBiometricPrompt() && this.mBiometricPromptVisible;
    }

    public String getCurrentOwner() {
        return this.mCurrentClient;
    }

    public String getLastOwner() {
        return this.mLastClient;
    }

    public boolean isEmptyClient() {
        return TextUtils.isEmpty(this.mCurrentClient);
    }

    public boolean isForceShowClient() {
        return isForceShow(this.mCurrentClient);
    }

    public boolean isKeyguardClient() {
        return isKeyguard(this.mCurrentClient);
    }

    public boolean isKeyguardAuthenticating() {
        return isKeyguard(this.mCurrentClient);
    }

    public boolean isKeyguardUnlocked() {
        return isKeyguard(this.mLastClient);
    }

    public boolean isFingerprintDetecting() {
        return this.mFingerprintState == FingerprintState.RUNNING;
    }

    public boolean isFingerprintSuspended() {
        return this.mFingerprintState == FingerprintState.SUSPEND;
    }

    public boolean isDoingEnroll() {
        return this.mActionEnroll;
    }

    public boolean isFingerprintStopped() {
        return this.mFingerprintState == FingerprintState.STOP;
    }

    public boolean isFingerprintLockout() {
        return this.mFingerprintState == FingerprintState.LOCKOUT;
    }

    private void changeState(FingerprintState fingerprintState, boolean z) {
        if (this.mFingerprintState != fingerprintState || z) {
            Log.d("OpFodHelper", "changeState ( " + this.mFingerprintState + " -> " + fingerprintState + " ) , force? " + z);
            this.mFingerprintState = fingerprintState;
            synchronized (this.mOnFingerprintStateChangeListeners) {
                Iterator<OnFingerprintStateChangeListener> it = this.mOnFingerprintStateChangeListeners.iterator();
                while (it.hasNext()) {
                    OnFingerprintStateChangeListener next = it.next();
                    if (next != null) {
                        next.onFingerprintStateChanged();
                    }
                }
            }
        }
    }

    public void changeState(FingerprintState fingerprintState) {
        changeState(fingerprintState, false);
    }

    public void notifyFodIconChanged(boolean z) {
        synchronized (this.mOnFodIconVisibilityChangeListeners) {
            Iterator<OpFodIconVisibilityChangeListener> it = this.mOnFodIconVisibilityChangeListeners.iterator();
            while (it.hasNext()) {
                OpFodIconVisibilityChangeListener next = it.next();
                if (next != null) {
                    next.onFodIconVisibilityChange(z);
                }
            }
        }
    }
}
