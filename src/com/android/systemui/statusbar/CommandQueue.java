package com.android.systemui.statusbar;

import android.app.ITransientNotificationCallback;
import android.content.ComponentName;
import android.content.Context;
import android.hardware.biometrics.IBiometricServiceReceiverInternal;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import com.android.internal.os.SomeArgs;
import com.android.internal.statusbar.IStatusBar;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.internal.view.AppearanceRegion;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.CallbackController;
import com.android.systemui.tracing.ProtoTracer;
import java.util.ArrayList;
import java.util.Iterator;
public class CommandQueue extends IStatusBar.Stub implements CallbackController<Callbacks>, DisplayManager.DisplayListener {
    private ArrayList<Callbacks> mCallbacks = new ArrayList<>();
    private SparseArray<Pair<Integer, Integer>> mDisplayDisabled = new SparseArray<>();
    private Handler mHandler = new H(Looper.getMainLooper());
    private int mLastUpdatedImeDisplayId = -1;
    private final Object mLock = new Object();
    private ProtoTracer mProtoTracer;

    public interface Callbacks {
        default void abortTransient(int i, int[] iArr) {
        }

        default void addQsTile(ComponentName componentName) {
        }

        default void animateCollapsePanels(int i, boolean z) {
        }

        default void animateExpandNotificationsPanel() {
        }

        default void animateExpandSettingsPanel(String str) {
        }

        default void appTransitionCancelled(int i) {
        }

        default void appTransitionFinished(int i) {
        }

        default void appTransitionPending(int i, boolean z) {
        }

        default void appTransitionStarting(int i, long j, long j2, boolean z) {
        }

        default void cancelPreloadRecentApps() {
        }

        default void clickTile(ComponentName componentName) {
        }

        default void disable(int i, int i2, int i3, boolean z) {
        }

        default void dismissInattentiveSleepWarning(boolean z) {
        }

        default void dismissKeyboardShortcutsMenu() {
        }

        default void handleShowGlobalActionsMenu() {
        }

        default void handleShowShutdownUi(boolean z, String str) {
        }

        default void handleSystemKey(int i) {
        }

        default void hideAuthenticationDialog() {
        }

        default void hideFodDialog(Bundle bundle, String str) {
        }

        default void hideRecentApps(boolean z, boolean z2) {
        }

        default void hideToast(String str, IBinder iBinder) {
        }

        default void notifyImeWindowVisibleStatus(int i, IBinder iBinder, int i2, int i3, boolean z) {
        }

        default void notifyNavBarColorChanged(int i, String str, String str2) {
        }

        default void onBiometricAuthenticated() {
        }

        default void onBiometricError(int i, int i2, int i3) {
        }

        default void onBiometricHelp(String str) {
        }

        default void onCameraLaunchGestureDetected(int i) {
        }

        default void onDisplayReady(int i) {
        }

        default void onDisplayRemoved(int i) {
        }

        default void onFingerprintAcquired(int i, int i2) {
        }

        default void onFingerprintAuthenticatedFail() {
        }

        default void onFingerprintAuthenticatedSuccess() {
        }

        default void onFingerprintEnrollResult(int i) {
        }

        default void onFingerprintError(int i) {
        }

        default void onRecentsAnimationStateChanged(boolean z) {
        }

        default void onRotationProposal(int i, boolean z) {
        }

        default void onSystemBarAppearanceChanged(int i, int i2, AppearanceRegion[] appearanceRegionArr, boolean z) {
        }

        default void onTracingStateChanged(boolean z) {
        }

        default void onVideoChanged(String str, boolean z) {
        }

        default void passSystemUIEvent(int i) {
        }

        default void preloadRecentApps() {
        }

        default void remQsTile(ComponentName componentName) {
        }

        default void removeIcon(String str) {
        }

        @Override // com.android.systemui.statusbar.phone.StatusBarIconController
        default void setIcon(String str, StatusBarIcon statusBarIcon) {
        }

        default void setImeWindowStatus(int i, IBinder iBinder, int i2, int i3, boolean z) {
        }

        default void setTopAppHidesStatusBar(boolean z) {
        }

        default void setWindowState(int i, int i2, int i3) {
        }

        default void showAssistDisclosure() {
        }

        default void showAuthenticationDialog(Bundle bundle, IBiometricServiceReceiverInternal iBiometricServiceReceiverInternal, int i, boolean z, int i2, String str, long j, int i3) {
        }

        default void showFodDialog(Bundle bundle, String str) {
        }

        default void showInattentiveSleepWarning() {
        }

        default void showPictureInPictureMenu() {
        }

        default void showPinningEnterExitToast(boolean z) {
        }

        default void showPinningEscapeToast() {
        }

        default void showRecentApps(boolean z) {
        }

        default void showScreenPinningRequest(int i) {
        }

        default void showToast(int i, String str, IBinder iBinder, CharSequence charSequence, IBinder iBinder2, int i2, ITransientNotificationCallback iTransientNotificationCallback) {
        }

        default void showTransient(int i, int[] iArr) {
        }

        default void showWirelessChargingAnimation(int i) {
        }

        default void startAssist(Bundle bundle) {
        }

        default void suppressAmbientDisplay(boolean z) {
        }

        default void toggleKeyboardShortcutsMenu(int i) {
        }

        default void togglePanel() {
        }

        default void toggleRecentApps() {
        }

        default void toggleSplitScreen() {
        }

        default void toggleWxBus() {
        }

        default void topAppWindowChanged(int i, boolean z, boolean z2) {
        }

        default void updateDisplayPowerStatus(int i) {
        }
    }

    @Override // android.hardware.display.DisplayManager.DisplayListener
    public void onDisplayAdded(int i) {
    }

    @Override // android.hardware.display.DisplayManager.DisplayListener
    public void onDisplayChanged(int i) {
    }

    public CommandQueue(Context context, ProtoTracer protoTracer) {
        this.mProtoTracer = protoTracer;
        ((DisplayManager) context.getSystemService(DisplayManager.class)).registerDisplayListener(this, this.mHandler);
        setDisabled(0, 0, 0);
    }

    @Override // android.hardware.display.DisplayManager.DisplayListener
    public void onDisplayRemoved(int i) {
        synchronized (this.mLock) {
            this.mDisplayDisabled.remove(i);
        }
        for (int size = this.mCallbacks.size() - 1; size >= 0; size--) {
            this.mCallbacks.get(size).onDisplayRemoved(i);
        }
    }

    public boolean panelsEnabled() {
        int disabled1 = getDisabled1(0);
        int disabled2 = getDisabled2(0);
        if ((disabled1 & 65536) == 0 && (disabled2 & 4) == 0 && !StatusBar.ONLY_CORE_APPS) {
            return true;
        }
        return false;
    }

    public void addCallback(Callbacks callbacks) {
        this.mCallbacks.add(callbacks);
        for (int i = 0; i < this.mDisplayDisabled.size(); i++) {
            int keyAt = this.mDisplayDisabled.keyAt(i);
            callbacks.disable(keyAt, getDisabled1(keyAt), getDisabled2(keyAt), false);
        }
    }

    public void removeCallback(Callbacks callbacks) {
        this.mCallbacks.remove(callbacks);
    }

    public void setIcon(String str, StatusBarIcon statusBarIcon) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(65536, 1, 0, new Pair(str, statusBarIcon)).sendToTarget();
        }
    }

    public void removeIcon(String str) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(65536, 2, 0, str).sendToTarget();
        }
    }

    public void disable(int i, int i2, int i3, boolean z) {
        synchronized (this.mLock) {
            if (Build.DEBUG_ONEPLUS) {
                int disabled1 = getDisabled1(i);
                int disabled2 = getDisabled2(i);
                if (!(disabled1 == i2 && disabled2 == i3)) {
                    Log.d("CommandQueue", "disable: s1=0x" + Integer.toHexString(disabled1) + "->0x" + Integer.toHexString(i2) + ", s2=0x" + Integer.toHexString(disabled2) + "->0x" + Integer.toHexString(i3));
                }
            }
            setDisabled(i, i2, i3);
            this.mHandler.removeMessages(131072);
            SomeArgs obtain = SomeArgs.obtain();
            obtain.argi1 = i;
            obtain.argi2 = i2;
            obtain.argi3 = i3;
            obtain.argi4 = z ? 1 : 0;
            Message obtainMessage = this.mHandler.obtainMessage(131072, obtain);
            if (Looper.myLooper() == this.mHandler.getLooper()) {
                this.mHandler.handleMessage(obtainMessage);
                obtainMessage.recycle();
            } else {
                obtainMessage.sendToTarget();
            }
        }
    }

    public void disable(int i, int i2, int i3) {
        disable(i, i2, i3, true);
    }

    public void recomputeDisableFlags(int i, boolean z) {
        synchronized (this.mLock) {
            disable(i, getDisabled1(i), getDisabled2(i), z);
        }
    }

    private void setDisabled(int i, int i2, int i3) {
        this.mDisplayDisabled.put(i, new Pair<>(Integer.valueOf(i2), Integer.valueOf(i3)));
    }

    private int getDisabled1(int i) {
        return ((Integer) getDisabled(i).first).intValue();
    }

    private int getDisabled2(int i) {
        return ((Integer) getDisabled(i).second).intValue();
    }

    private Pair<Integer, Integer> getDisabled(int i) {
        Pair<Integer, Integer> pair = this.mDisplayDisabled.get(i);
        if (pair != null) {
            return pair;
        }
        Pair<Integer, Integer> pair2 = new Pair<>(0, 0);
        this.mDisplayDisabled.put(i, pair2);
        return pair2;
    }

    public void animateExpandNotificationsPanel() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(196608);
            this.mHandler.sendEmptyMessage(196608);
        }
    }

    public void animateCollapsePanels() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(262144);
            this.mHandler.obtainMessage(262144, 0, 0).sendToTarget();
        }
    }

    public void animateCollapsePanels(int i, boolean z) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(262144);
            this.mHandler.obtainMessage(262144, i, z ? 1 : 0).sendToTarget();
        }
    }

    public void togglePanel() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(2293760);
            this.mHandler.obtainMessage(2293760, 0, 0).sendToTarget();
        }
    }

    public void animateExpandSettingsPanel(String str) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(327680);
            this.mHandler.obtainMessage(327680, str).sendToTarget();
        }
    }

    public void topAppWindowChanged(int i, boolean z, boolean z2) {
        synchronized (this.mLock) {
            SomeArgs obtain = SomeArgs.obtain();
            obtain.argi1 = i;
            int i2 = 1;
            obtain.argi2 = z ? 1 : 0;
            if (!z2) {
                i2 = 0;
            }
            obtain.argi3 = i2;
            this.mHandler.obtainMessage(3276800, obtain).sendToTarget();
        }
    }

    public void setImeWindowStatus(int i, IBinder iBinder, int i2, int i3, boolean z, boolean z2) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(524288);
            SomeArgs obtain = SomeArgs.obtain();
            obtain.argi1 = i;
            obtain.argi2 = i2;
            obtain.argi3 = i3;
            int i4 = 1;
            obtain.argi4 = z ? 1 : 0;
            if (!z2) {
                i4 = 0;
            }
            obtain.argi5 = i4;
            obtain.arg1 = iBinder;
            this.mHandler.obtainMessage(524288, obtain).sendToTarget();
        }
    }

    public void showRecentApps(boolean z) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(851968);
            this.mHandler.obtainMessage(851968, z ? 1 : 0, 0, null).sendToTarget();
        }
    }

    public void hideRecentApps(boolean z, boolean z2) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(917504);
            this.mHandler.obtainMessage(917504, z ? 1 : 0, z2 ? 1 : 0, null).sendToTarget();
        }
    }

    public void toggleSplitScreen() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(1966080);
            this.mHandler.obtainMessage(1966080, 0, 0, null).sendToTarget();
        }
    }

    public void toggleRecentApps() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(589824);
            Message obtainMessage = this.mHandler.obtainMessage(589824, 0, 0, null);
            obtainMessage.setAsynchronous(true);
            obtainMessage.sendToTarget();
        }
    }

    public void preloadRecentApps() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(655360);
            this.mHandler.obtainMessage(655360, 0, 0, null).sendToTarget();
        }
    }

    public void cancelPreloadRecentApps() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(720896);
            this.mHandler.obtainMessage(720896, 0, 0, null).sendToTarget();
        }
    }

    public void dismissKeyboardShortcutsMenu() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(2097152);
            this.mHandler.obtainMessage(2097152).sendToTarget();
        }
    }

    public void toggleKeyboardShortcutsMenu(int i) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(1638400);
            this.mHandler.obtainMessage(1638400, i, 0).sendToTarget();
        }
    }

    public void showPictureInPictureMenu() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(1703936);
            this.mHandler.obtainMessage(1703936).sendToTarget();
        }
    }

    public void setWindowState(int i, int i2, int i3) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(786432, i, i2, Integer.valueOf(i3)).sendToTarget();
        }
    }

    public void showScreenPinningRequest(int i) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(1179648, i, 0, null).sendToTarget();
        }
    }

    public void appTransitionPending(int i) {
        appTransitionPending(i, false);
    }

    public void appTransitionPending(int i, boolean z) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(1245184, i, z ? 1 : 0).sendToTarget();
        }
    }

    public void appTransitionCancelled(int i) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(1310720, i, 0).sendToTarget();
        }
    }

    public void appTransitionStarting(int i, long j, long j2) {
        appTransitionStarting(i, j, j2, false);
    }

    public void appTransitionStarting(int i, long j, long j2, boolean z) {
        synchronized (this.mLock) {
            SomeArgs obtain = SomeArgs.obtain();
            obtain.argi1 = i;
            obtain.argi2 = z ? 1 : 0;
            obtain.arg1 = Long.valueOf(j);
            obtain.arg2 = Long.valueOf(j2);
            this.mHandler.obtainMessage(1376256, obtain).sendToTarget();
        }
    }

    public void appTransitionFinished(int i) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(2031616, i, 0).sendToTarget();
        }
    }

    public void showAssistDisclosure() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(1441792);
            this.mHandler.obtainMessage(1441792).sendToTarget();
        }
    }

    public void startAssist(Bundle bundle) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(1507328);
            this.mHandler.obtainMessage(1507328, bundle).sendToTarget();
        }
    }

    public void onCameraLaunchGestureDetected(int i) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(1572864);
            this.mHandler.obtainMessage(1572864, i, 0).sendToTarget();
        }
    }

    public void addQsTile(ComponentName componentName) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(1769472, componentName).sendToTarget();
        }
    }

    public void remQsTile(ComponentName componentName) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(1835008, componentName).sendToTarget();
        }
    }

    public void clickQsTile(ComponentName componentName) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(1900544, componentName).sendToTarget();
        }
    }

    public void handleSystemKey(int i) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(2162688, i, 0).sendToTarget();
        }
    }

    public void showPinningEnterExitToast(boolean z) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(2949120, Boolean.valueOf(z)).sendToTarget();
        }
    }

    public void showPinningEscapeToast() {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(3014656).sendToTarget();
        }
    }

    public void showGlobalActionsMenu() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(2228224);
            this.mHandler.obtainMessage(2228224).sendToTarget();
        }
    }

    public void setTopAppHidesStatusBar(boolean z) {
        this.mHandler.removeMessages(2424832);
        this.mHandler.obtainMessage(2424832, z ? 1 : 0, 0).sendToTarget();
    }

    public void showShutdownUi(boolean z, String str) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(2359296);
            this.mHandler.obtainMessage(2359296, z ? 1 : 0, 0, str).sendToTarget();
        }
    }

    public void showWirelessChargingAnimation(int i) {
        this.mHandler.removeMessages(2883584);
        this.mHandler.obtainMessage(2883584, i, 0).sendToTarget();
    }

    public void onProposedRotationChanged(int i, boolean z) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(2490368);
            this.mHandler.obtainMessage(2490368, i, z ? 1 : 0, null).sendToTarget();
        }
    }

    public void showAuthenticationDialog(Bundle bundle, IBiometricServiceReceiverInternal iBiometricServiceReceiverInternal, int i, boolean z, int i2, String str, long j, int i3) {
        synchronized (this.mLock) {
            SomeArgs obtain = SomeArgs.obtain();
            obtain.arg1 = bundle;
            obtain.arg2 = iBiometricServiceReceiverInternal;
            obtain.argi1 = i;
            obtain.arg3 = Boolean.valueOf(z);
            obtain.argi2 = i2;
            obtain.arg4 = str;
            obtain.arg5 = Long.valueOf(j);
            obtain.argi3 = i3;
            this.mHandler.obtainMessage(2555904, obtain).sendToTarget();
        }
    }

    public void showToast(int i, String str, IBinder iBinder, CharSequence charSequence, IBinder iBinder2, int i2, ITransientNotificationCallback iTransientNotificationCallback) {
        synchronized (this.mLock) {
            SomeArgs obtain = SomeArgs.obtain();
            obtain.arg1 = str;
            obtain.arg2 = iBinder;
            obtain.arg3 = charSequence;
            obtain.arg4 = iBinder2;
            obtain.arg5 = iTransientNotificationCallback;
            obtain.argi1 = i;
            obtain.argi2 = i2;
            this.mHandler.obtainMessage(3473408, obtain).sendToTarget();
        }
    }

    public void hideToast(String str, IBinder iBinder) {
        synchronized (this.mLock) {
            SomeArgs obtain = SomeArgs.obtain();
            obtain.arg1 = str;
            obtain.arg2 = iBinder;
            this.mHandler.obtainMessage(3538944, obtain).sendToTarget();
        }
    }

    public void onBiometricAuthenticated() {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(2621440).sendToTarget();
        }
    }

    public void onBiometricHelp(String str) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(2686976, str).sendToTarget();
        }
    }

    public void onBiometricError(int i, int i2, int i3) {
        synchronized (this.mLock) {
            SomeArgs obtain = SomeArgs.obtain();
            obtain.argi1 = i;
            obtain.argi2 = i2;
            obtain.argi3 = i3;
            this.mHandler.obtainMessage(2752512, obtain).sendToTarget();
        }
    }

    public void hideAuthenticationDialog() {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(2818048).sendToTarget();
        }
    }

    public void onDisplayReady(int i) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(458752, i, 0).sendToTarget();
        }
    }

    public void onFingerprintAcquired(int i, int i2) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(6750208, i, i2).sendToTarget();
        }
    }

    public void onFingerprintEnrollResult(int i) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(6815744, i, 0).sendToTarget();
        }
    }

    public void onFingerprintAuthenticatedFailed() {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(6881280).sendToTarget();
        }
    }

    public void onRecentsAnimationStateChanged(boolean z) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(3080192, z ? 1 : 0, 0).sendToTarget();
        }
    }

    public void showInattentiveSleepWarning() {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(3342336).sendToTarget();
        }
    }

    public void dismissInattentiveSleepWarning(boolean z) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(3407872, Boolean.valueOf(z)).sendToTarget();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleShowImeButton(int i, IBinder iBinder, int i2, int i3, boolean z, boolean z2) {
        int i4;
        if (i != -1) {
            if (!(z2 || (i4 = this.mLastUpdatedImeDisplayId) == i || i4 == -1)) {
                sendImeInvisibleStatusForPrevNavBar();
            }
            for (int i5 = 0; i5 < this.mCallbacks.size(); i5++) {
                this.mCallbacks.get(i5).notifyImeWindowVisibleStatus(i, iBinder, i2, i3, z);
                this.mCallbacks.get(i5).setImeWindowStatus(i, iBinder, i2, i3, z);
            }
            this.mLastUpdatedImeDisplayId = i;
        }
    }

    private void sendImeInvisibleStatusForPrevNavBar() {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            this.mCallbacks.get(i).setImeWindowStatus(this.mLastUpdatedImeDisplayId, null, 4, 0, false);
        }
    }

    public void notifyNavBarColorChanged(int i, String str, String str2) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(6553600);
            this.mHandler.obtainMessage(6553600, i, 0, new String[]{str, str2}).sendToTarget();
        }
    }

    public void showFodDialog(Bundle bundle, String str) {
        synchronized (this.mLock) {
            SomeArgs obtain = SomeArgs.obtain();
            obtain.arg1 = bundle;
            obtain.arg2 = str;
            this.mHandler.obtainMessage(7077888, obtain).sendToTarget();
        }
    }

    public void hideFodDialog(Bundle bundle, String str) {
        synchronized (this.mLock) {
            SomeArgs obtain = SomeArgs.obtain();
            obtain.arg1 = bundle;
            obtain.arg2 = str;
            this.mHandler.obtainMessage(7143424, obtain).sendToTarget();
        }
    }

    public void updateDisplayPowerStatus(int i) {
        synchronized (this.mLock) {
            SomeArgs obtain = SomeArgs.obtain();
            obtain.argi1 = i;
            this.mHandler.obtainMessage(7405568, obtain).sendToTarget();
        }
    }

    public void onFingerprintAuthenticatedSuccess() {
        synchronized (this.mLock) {
            this.mHandler.sendEmptyMessage(7208960);
        }
    }

    public void onFingerprintError(int i) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(7274496, i, 0).sendToTarget();
        }
    }

    public void passSystemUIEvent(int i) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(6946816);
            this.mHandler.obtainMessage(6946816, i, 0).sendToTarget();
        }
    }

    public void onVideoChanged(String str, boolean z) {
        synchronized (this.mLock) {
            SomeArgs obtain = SomeArgs.obtain();
            obtain.arg1 = str;
            obtain.argi1 = z ? 1 : 0;
            this.mHandler.obtainMessage(7471104, obtain).sendToTarget();
        }
    }

    public void onSystemBarAppearanceChanged(int i, int i2, AppearanceRegion[] appearanceRegionArr, boolean z) {
        synchronized (this.mLock) {
            SomeArgs obtain = SomeArgs.obtain();
            obtain.argi1 = i;
            obtain.argi2 = i2;
            obtain.argi3 = z ? 1 : 0;
            obtain.arg1 = appearanceRegionArr;
            this.mHandler.obtainMessage(393216, obtain).sendToTarget();
        }
    }

    public void showTransient(int i, int[] iArr) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(3145728, i, 0, iArr).sendToTarget();
        }
    }

    public void abortTransient(int i, int[] iArr) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(3211264, i, 0, iArr).sendToTarget();
        }
    }

    public void startTracing() {
        synchronized (this.mLock) {
            if (this.mProtoTracer != null) {
                this.mProtoTracer.start();
            }
            this.mHandler.obtainMessage(3604480, Boolean.TRUE).sendToTarget();
        }
    }

    public void stopTracing() {
        synchronized (this.mLock) {
            if (this.mProtoTracer != null) {
                this.mProtoTracer.stop();
            }
            this.mHandler.obtainMessage(3604480, Boolean.FALSE).sendToTarget();
        }
    }

    public void suppressAmbientDisplay(boolean z) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(3670016, Boolean.valueOf(z)).sendToTarget();
        }
    }

    private final class H extends Handler {
        private H(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            boolean z = false;
            switch (message.what & -65536) {
                case 65536:
                    int i = message.arg1;
                    if (i == 1) {
                        Pair pair = (Pair) message.obj;
                        for (int i2 = 0; i2 < CommandQueue.this.mCallbacks.size(); i2++) {
                            ((Callbacks) CommandQueue.this.mCallbacks.get(i2)).setIcon((String) pair.first, (StatusBarIcon) pair.second);
                        }
                        return;
                    } else if (i == 2) {
                        for (int i3 = 0; i3 < CommandQueue.this.mCallbacks.size(); i3++) {
                            ((Callbacks) CommandQueue.this.mCallbacks.get(i3)).removeIcon((String) message.obj);
                        }
                        return;
                    } else {
                        return;
                    }
                case 131072:
                    SomeArgs someArgs = (SomeArgs) message.obj;
                    for (int i4 = 0; i4 < CommandQueue.this.mCallbacks.size(); i4++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i4)).disable(someArgs.argi1, someArgs.argi2, someArgs.argi3, someArgs.argi4 != 0);
                    }
                    return;
                case 196608:
                    for (int i5 = 0; i5 < CommandQueue.this.mCallbacks.size(); i5++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i5)).animateExpandNotificationsPanel();
                    }
                    return;
                case 262144:
                    for (int i6 = 0; i6 < CommandQueue.this.mCallbacks.size(); i6++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i6)).animateCollapsePanels(message.arg1, message.arg2 != 0);
                    }
                    return;
                case 327680:
                    for (int i7 = 0; i7 < CommandQueue.this.mCallbacks.size(); i7++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i7)).animateExpandSettingsPanel((String) message.obj);
                    }
                    return;
                case 393216:
                    SomeArgs someArgs2 = (SomeArgs) message.obj;
                    for (int i8 = 0; i8 < CommandQueue.this.mCallbacks.size(); i8++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i8)).onSystemBarAppearanceChanged(someArgs2.argi1, someArgs2.argi2, (AppearanceRegion[]) someArgs2.arg1, someArgs2.argi3 == 1);
                    }
                    someArgs2.recycle();
                    return;
                case 458752:
                    for (int i9 = 0; i9 < CommandQueue.this.mCallbacks.size(); i9++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i9)).onDisplayReady(message.arg1);
                    }
                    return;
                case 524288:
                    SomeArgs someArgs3 = (SomeArgs) message.obj;
                    CommandQueue.this.handleShowImeButton(someArgs3.argi1, (IBinder) someArgs3.arg1, someArgs3.argi2, someArgs3.argi3, someArgs3.argi4 != 0, someArgs3.argi5 != 0);
                    return;
                case 589824:
                    for (int i10 = 0; i10 < CommandQueue.this.mCallbacks.size(); i10++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i10)).toggleRecentApps();
                    }
                    return;
                case 655360:
                    for (int i11 = 0; i11 < CommandQueue.this.mCallbacks.size(); i11++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i11)).preloadRecentApps();
                    }
                    return;
                case 720896:
                    for (int i12 = 0; i12 < CommandQueue.this.mCallbacks.size(); i12++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i12)).cancelPreloadRecentApps();
                    }
                    return;
                case 786432:
                    for (int i13 = 0; i13 < CommandQueue.this.mCallbacks.size(); i13++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i13)).setWindowState(message.arg1, message.arg2, ((Integer) message.obj).intValue());
                    }
                    return;
                case 851968:
                    for (int i14 = 0; i14 < CommandQueue.this.mCallbacks.size(); i14++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i14)).showRecentApps(message.arg1 != 0);
                    }
                    return;
                case 917504:
                    for (int i15 = 0; i15 < CommandQueue.this.mCallbacks.size(); i15++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i15)).hideRecentApps(message.arg1 != 0, message.arg2 != 0);
                    }
                    return;
                case 1179648:
                    for (int i16 = 0; i16 < CommandQueue.this.mCallbacks.size(); i16++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i16)).showScreenPinningRequest(message.arg1);
                    }
                    return;
                case 1245184:
                    for (int i17 = 0; i17 < CommandQueue.this.mCallbacks.size(); i17++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i17)).appTransitionPending(message.arg1, message.arg2 != 0);
                    }
                    return;
                case 1310720:
                    for (int i18 = 0; i18 < CommandQueue.this.mCallbacks.size(); i18++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i18)).appTransitionCancelled(message.arg1);
                    }
                    return;
                case 1376256:
                    SomeArgs someArgs4 = (SomeArgs) message.obj;
                    for (int i19 = 0; i19 < CommandQueue.this.mCallbacks.size(); i19++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i19)).appTransitionStarting(someArgs4.argi1, ((Long) someArgs4.arg1).longValue(), ((Long) someArgs4.arg2).longValue(), someArgs4.argi2 != 0);
                    }
                    return;
                case 1441792:
                    for (int i20 = 0; i20 < CommandQueue.this.mCallbacks.size(); i20++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i20)).showAssistDisclosure();
                    }
                    return;
                case 1507328:
                    for (int i21 = 0; i21 < CommandQueue.this.mCallbacks.size(); i21++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i21)).startAssist((Bundle) message.obj);
                    }
                    return;
                case 1572864:
                    for (int i22 = 0; i22 < CommandQueue.this.mCallbacks.size(); i22++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i22)).onCameraLaunchGestureDetected(message.arg1);
                    }
                    return;
                case 1638400:
                    for (int i23 = 0; i23 < CommandQueue.this.mCallbacks.size(); i23++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i23)).toggleKeyboardShortcutsMenu(message.arg1);
                    }
                    return;
                case 1703936:
                    for (int i24 = 0; i24 < CommandQueue.this.mCallbacks.size(); i24++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i24)).showPictureInPictureMenu();
                    }
                    return;
                case 1769472:
                    for (int i25 = 0; i25 < CommandQueue.this.mCallbacks.size(); i25++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i25)).addQsTile((ComponentName) message.obj);
                    }
                    return;
                case 1835008:
                    for (int i26 = 0; i26 < CommandQueue.this.mCallbacks.size(); i26++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i26)).remQsTile((ComponentName) message.obj);
                    }
                    return;
                case 1900544:
                    for (int i27 = 0; i27 < CommandQueue.this.mCallbacks.size(); i27++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i27)).clickTile((ComponentName) message.obj);
                    }
                    return;
                case 1966080:
                    for (int i28 = 0; i28 < CommandQueue.this.mCallbacks.size(); i28++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i28)).toggleSplitScreen();
                    }
                    return;
                case 2031616:
                    for (int i29 = 0; i29 < CommandQueue.this.mCallbacks.size(); i29++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i29)).appTransitionFinished(message.arg1);
                    }
                    return;
                case 2097152:
                    for (int i30 = 0; i30 < CommandQueue.this.mCallbacks.size(); i30++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i30)).dismissKeyboardShortcutsMenu();
                    }
                    return;
                case 2162688:
                    for (int i31 = 0; i31 < CommandQueue.this.mCallbacks.size(); i31++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i31)).handleSystemKey(message.arg1);
                    }
                    return;
                case 2228224:
                    for (int i32 = 0; i32 < CommandQueue.this.mCallbacks.size(); i32++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i32)).handleShowGlobalActionsMenu();
                    }
                    return;
                case 2293760:
                    for (int i33 = 0; i33 < CommandQueue.this.mCallbacks.size(); i33++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i33)).togglePanel();
                    }
                    return;
                case 2359296:
                    for (int i34 = 0; i34 < CommandQueue.this.mCallbacks.size(); i34++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i34)).handleShowShutdownUi(message.arg1 != 0, (String) message.obj);
                    }
                    return;
                case 2424832:
                    for (int i35 = 0; i35 < CommandQueue.this.mCallbacks.size(); i35++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i35)).setTopAppHidesStatusBar(message.arg1 != 0);
                    }
                    return;
                case 2490368:
                    for (int i36 = 0; i36 < CommandQueue.this.mCallbacks.size(); i36++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i36)).onRotationProposal(message.arg1, message.arg2 != 0);
                    }
                    return;
                case 2555904:
                    CommandQueue.this.mHandler.removeMessages(2752512);
                    CommandQueue.this.mHandler.removeMessages(2686976);
                    CommandQueue.this.mHandler.removeMessages(2621440);
                    SomeArgs someArgs5 = (SomeArgs) message.obj;
                    for (int i37 = 0; i37 < CommandQueue.this.mCallbacks.size(); i37++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i37)).showAuthenticationDialog((Bundle) someArgs5.arg1, (IBiometricServiceReceiverInternal) someArgs5.arg2, someArgs5.argi1, ((Boolean) someArgs5.arg3).booleanValue(), someArgs5.argi2, (String) someArgs5.arg4, ((Long) someArgs5.arg5).longValue(), someArgs5.argi3);
                    }
                    someArgs5.recycle();
                    return;
                case 2621440:
                    for (int i38 = 0; i38 < CommandQueue.this.mCallbacks.size(); i38++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i38)).onBiometricAuthenticated();
                    }
                    return;
                case 2686976:
                    for (int i39 = 0; i39 < CommandQueue.this.mCallbacks.size(); i39++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i39)).onBiometricHelp((String) message.obj);
                    }
                    return;
                case 2752512:
                    SomeArgs someArgs6 = (SomeArgs) message.obj;
                    for (int i40 = 0; i40 < CommandQueue.this.mCallbacks.size(); i40++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i40)).onBiometricError(someArgs6.argi1, someArgs6.argi2, someArgs6.argi3);
                    }
                    someArgs6.recycle();
                    return;
                case 2818048:
                    for (int i41 = 0; i41 < CommandQueue.this.mCallbacks.size(); i41++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i41)).hideAuthenticationDialog();
                    }
                    return;
                case 2883584:
                    for (int i42 = 0; i42 < CommandQueue.this.mCallbacks.size(); i42++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i42)).showWirelessChargingAnimation(message.arg1);
                    }
                    return;
                case 2949120:
                    for (int i43 = 0; i43 < CommandQueue.this.mCallbacks.size(); i43++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i43)).showPinningEnterExitToast(((Boolean) message.obj).booleanValue());
                    }
                    return;
                case 3014656:
                    for (int i44 = 0; i44 < CommandQueue.this.mCallbacks.size(); i44++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i44)).showPinningEscapeToast();
                    }
                    return;
                case 3080192:
                    for (int i45 = 0; i45 < CommandQueue.this.mCallbacks.size(); i45++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i45)).onRecentsAnimationStateChanged(message.arg1 > 0);
                    }
                    return;
                case 3145728:
                    int i46 = message.arg1;
                    int[] iArr = (int[]) message.obj;
                    for (int i47 = 0; i47 < CommandQueue.this.mCallbacks.size(); i47++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i47)).showTransient(i46, iArr);
                    }
                    return;
                case 3211264:
                    int i48 = message.arg1;
                    int[] iArr2 = (int[]) message.obj;
                    for (int i49 = 0; i49 < CommandQueue.this.mCallbacks.size(); i49++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i49)).abortTransient(i48, iArr2);
                    }
                    return;
                case 3276800:
                    SomeArgs someArgs7 = (SomeArgs) message.obj;
                    for (int i50 = 0; i50 < CommandQueue.this.mCallbacks.size(); i50++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i50)).topAppWindowChanged(someArgs7.argi1, someArgs7.argi2 != 0, someArgs7.argi3 != 0);
                    }
                    someArgs7.recycle();
                    return;
                case 3342336:
                    for (int i51 = 0; i51 < CommandQueue.this.mCallbacks.size(); i51++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i51)).showInattentiveSleepWarning();
                    }
                    return;
                case 3407872:
                    for (int i52 = 0; i52 < CommandQueue.this.mCallbacks.size(); i52++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i52)).dismissInattentiveSleepWarning(((Boolean) message.obj).booleanValue());
                    }
                    return;
                case 3473408:
                    SomeArgs someArgs8 = (SomeArgs) message.obj;
                    String str = (String) someArgs8.arg1;
                    IBinder iBinder = (IBinder) someArgs8.arg2;
                    CharSequence charSequence = (CharSequence) someArgs8.arg3;
                    IBinder iBinder2 = (IBinder) someArgs8.arg4;
                    ITransientNotificationCallback iTransientNotificationCallback = (ITransientNotificationCallback) someArgs8.arg5;
                    int i53 = someArgs8.argi1;
                    int i54 = someArgs8.argi2;
                    Iterator it = CommandQueue.this.mCallbacks.iterator();
                    while (it.hasNext()) {
                        ((Callbacks) it.next()).showToast(i53, str, iBinder, charSequence, iBinder2, i54, iTransientNotificationCallback);
                    }
                    return;
                case 3538944:
                    SomeArgs someArgs9 = (SomeArgs) message.obj;
                    String str2 = (String) someArgs9.arg1;
                    IBinder iBinder3 = (IBinder) someArgs9.arg2;
                    Iterator it2 = CommandQueue.this.mCallbacks.iterator();
                    while (it2.hasNext()) {
                        ((Callbacks) it2.next()).hideToast(str2, iBinder3);
                    }
                    return;
                case 3604480:
                    for (int i55 = 0; i55 < CommandQueue.this.mCallbacks.size(); i55++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i55)).onTracingStateChanged(((Boolean) message.obj).booleanValue());
                    }
                    return;
                case 3670016:
                    Iterator it3 = CommandQueue.this.mCallbacks.iterator();
                    while (it3.hasNext()) {
                        ((Callbacks) it3.next()).suppressAmbientDisplay(((Boolean) message.obj).booleanValue());
                    }
                    return;
                case 6553600:
                    String[] strArr = (String[]) message.obj;
                    Iterator it4 = CommandQueue.this.mCallbacks.iterator();
                    while (it4.hasNext()) {
                        ((Callbacks) it4.next()).notifyNavBarColorChanged(message.arg1, strArr[0], strArr[1]);
                    }
                    return;
                case 6750208:
                    Iterator it5 = CommandQueue.this.mCallbacks.iterator();
                    while (it5.hasNext()) {
                        ((Callbacks) it5.next()).onFingerprintAcquired(message.arg1, message.arg2);
                    }
                    return;
                case 6815744:
                    Iterator it6 = CommandQueue.this.mCallbacks.iterator();
                    while (it6.hasNext()) {
                        ((Callbacks) it6.next()).onFingerprintEnrollResult(message.arg1);
                    }
                    return;
                case 6881280:
                    Iterator it7 = CommandQueue.this.mCallbacks.iterator();
                    while (it7.hasNext()) {
                        ((Callbacks) it7.next()).onFingerprintAuthenticatedFail();
                    }
                    return;
                case 6946816:
                    Iterator it8 = CommandQueue.this.mCallbacks.iterator();
                    while (it8.hasNext()) {
                        ((Callbacks) it8.next()).passSystemUIEvent(message.arg1);
                    }
                    return;
                case 7077888:
                    CommandQueue.this.mHandler.removeMessages(7274496);
                    CommandQueue.this.mHandler.removeMessages(7208960);
                    SomeArgs someArgs10 = (SomeArgs) message.obj;
                    Iterator it9 = CommandQueue.this.mCallbacks.iterator();
                    while (it9.hasNext()) {
                        ((Callbacks) it9.next()).showFodDialog((Bundle) someArgs10.arg1, (String) someArgs10.arg2);
                    }
                    return;
                case 7143424:
                    SomeArgs someArgs11 = (SomeArgs) message.obj;
                    Iterator it10 = CommandQueue.this.mCallbacks.iterator();
                    while (it10.hasNext()) {
                        ((Callbacks) it10.next()).hideFodDialog((Bundle) someArgs11.arg1, (String) someArgs11.arg2);
                    }
                    return;
                case 7208960:
                    Iterator it11 = CommandQueue.this.mCallbacks.iterator();
                    while (it11.hasNext()) {
                        ((Callbacks) it11.next()).onFingerprintAuthenticatedSuccess();
                    }
                    return;
                case 7274496:
                    Iterator it12 = CommandQueue.this.mCallbacks.iterator();
                    while (it12.hasNext()) {
                        ((Callbacks) it12.next()).onFingerprintError(message.arg1);
                    }
                    return;
                case 7340032:
                    for (int i56 = 0; i56 < CommandQueue.this.mCallbacks.size(); i56++) {
                        ((Callbacks) CommandQueue.this.mCallbacks.get(i56)).toggleWxBus();
                    }
                    return;
                case 7405568:
                    int i57 = ((SomeArgs) message.obj).argi1;
                    Iterator it13 = CommandQueue.this.mCallbacks.iterator();
                    while (it13.hasNext()) {
                        ((Callbacks) it13.next()).updateDisplayPowerStatus(i57);
                    }
                    return;
                case 7471104:
                    SomeArgs someArgs12 = (SomeArgs) message.obj;
                    String str3 = (String) someArgs12.arg1;
                    if (someArgs12.argi1 == 1) {
                        z = true;
                    }
                    Iterator it14 = CommandQueue.this.mCallbacks.iterator();
                    while (it14.hasNext()) {
                        ((Callbacks) it14.next()).onVideoChanged(str3, z);
                    }
                    return;
                default:
                    return;
            }
        }
    }
}
