package com.android.keyguard;

import android.app.ActivityOptions;
import android.app.ActivityTaskManager;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.telecom.TelecomManager;
import android.telephony.CellInfo;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Slog;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.util.EmergencyAffordanceManager;
import com.android.internal.widget.LockPatternUtils;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.DejankUtils;
import com.android.systemui.Dependency;
import com.oneplus.util.OpUtils;
import java.util.List;
public class EmergencyButton extends Button {
    Drawable mBgDrawble;
    private boolean mDebounce;
    private int mDownX;
    private int mDownY;
    private final EmergencyAffordanceManager mEmergencyAffordanceManager;
    private EmergencyButtonCallback mEmergencyButtonCallback;
    private boolean[] mEmergencyCapable;
    private final boolean mEnableEmergencyCallWhileSimLocked;
    private Handler mHandler;
    KeyguardUpdateMonitorCallback mInfoCallback;
    private boolean mIsOOS;
    private final boolean mIsVoiceCapable;
    private LockPatternUtils mLockPatternUtils;
    private boolean mLongPressWasDragged;
    private PowerManager mPowerManager;

    public interface EmergencyButtonCallback {
        boolean isShowEmergencyPanel();

        void onEmergencyButtonClickedWhenInCall();

        void onLaunchEmergencyPanel();
    }

    public EmergencyButton(Context context) {
        this(context, null);
    }

    public EmergencyButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mDebounce = false;
        this.mHandler = new Handler(Looper.getMainLooper()) { // from class: com.android.keyguard.EmergencyButton.1
            @Override // android.os.Handler
            public void handleMessage(Message message) {
                if (message.what == 100) {
                    EmergencyButton.this.mDebounce = false;
                }
            }
        };
        this.mIsOOS = false;
        this.mInfoCallback = new KeyguardUpdateMonitorCallback() { // from class: com.android.keyguard.EmergencyButton.2
            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onSimStateChanged(int i, int i2, int i3) {
                EmergencyButton.this.requestCellInfoUpdate();
                EmergencyButton.this.updateEmergencyCallButton();
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onPhoneStateChanged(int i) {
                EmergencyButton.this.requestCellInfoUpdate();
                EmergencyButton.this.updateEmergencyCallButton();
            }

            @Override // com.oneplus.keyguard.OpKeyguardUpdateMonitorCallback
            public void onBootCompleted() {
                Log.d("EmergencyButton", "onBootCompleted");
                EmergencyButton.this.requestCellInfoUpdate();
                EmergencyButton.this.updateEmergencyCallButton();
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onServiceStateChanged(int i, ServiceState serviceState) {
                EmergencyButton.this.requestCellInfoUpdate();
                EmergencyButton.this.updateEmergencyCallButton();
            }
        };
        this.mEmergencyCapable = new boolean[TelephonyManager.getDefault().getPhoneCount()];
        this.mIsVoiceCapable = getTelephonyManager().isVoiceCapable();
        this.mEnableEmergencyCallWhileSimLocked = ((Button) this).mContext.getResources().getBoolean(17891459);
        this.mEmergencyAffordanceManager = new EmergencyAffordanceManager(context);
        if (OpUtils.isCustomFingerprint()) {
            setPaintFlags(getPaintFlags() | 8);
        }
    }

    private TelephonyManager getTelephonyManager() {
        return (TelephonyManager) ((Button) this).mContext.getSystemService("phone");
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.TextView, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).registerCallback(this.mInfoCallback);
        this.mHandler.removeMessages(100);
        this.mDebounce = false;
        requestCellInfoUpdate();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).removeCallback(this.mInfoCallback);
        this.mHandler.removeMessages(100);
        this.mDebounce = false;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mLockPatternUtils = new LockPatternUtils(((Button) this).mContext);
        this.mPowerManager = (PowerManager) ((Button) this).mContext.getSystemService("power");
        this.mBgDrawble = getResources().getDrawable(C0006R$drawable.layout_emergency_botton_bg);
        setOnClickListener(new View.OnClickListener() { // from class: com.android.keyguard.-$$Lambda$EmergencyButton$KTHEYrkUJc7xBxT3_mk1U-fqYZ8
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                EmergencyButton.this.lambda$onFinishInflate$0$EmergencyButton(view);
            }
        });
        if (this.mEmergencyAffordanceManager.needsEmergencyAffordance()) {
            setOnLongClickListener(new View.OnLongClickListener() { // from class: com.android.keyguard.-$$Lambda$EmergencyButton$lDso_ObwUd3nlVNy8pLMJXmgJO0
                @Override // android.view.View.OnLongClickListener
                public final boolean onLongClick(View view) {
                    return EmergencyButton.this.lambda$onFinishInflate$1$EmergencyButton(view);
                }
            });
        }
        requestCellInfoUpdate();
        DejankUtils.whitelistIpcs(new Runnable() { // from class: com.android.keyguard.-$$Lambda$7IHJ89G67Qw9GERRIAzsEiEp-U8
            @Override // java.lang.Runnable
            public final void run() {
                EmergencyButton.this.updateEmergencyCallButton();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onFinishInflate$0 */
    public /* synthetic */ void lambda$onFinishInflate$0$EmergencyButton(View view) {
        EmergencyButtonCallback emergencyButtonCallback;
        EmergencyButtonCallback emergencyButtonCallback2 = this.mEmergencyButtonCallback;
        if (emergencyButtonCallback2 == null) {
            takeEmergencyCallAction();
            return;
        }
        boolean isShowEmergencyPanel = emergencyButtonCallback2.isShowEmergencyPanel();
        boolean isInCall = isInCall();
        if (Build.DEBUG_ONEPLUS) {
            Log.i("EmergencyButton", "isShowEmergencyPanel: isShowEmergencyPanel= " + isShowEmergencyPanel + ", isInCall= " + isInCall);
        }
        if (!isShowEmergencyPanel || isInCall) {
            if (!((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isKeyguardVisible() && (emergencyButtonCallback = this.mEmergencyButtonCallback) != null) {
                emergencyButtonCallback.onEmergencyButtonClickedWhenInCall();
            }
            takeEmergencyCallAction();
            return;
        }
        this.mEmergencyButtonCallback.onLaunchEmergencyPanel();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onFinishInflate$1 */
    public /* synthetic */ boolean lambda$onFinishInflate$1$EmergencyButton(View view) {
        if (this.mLongPressWasDragged || !this.mEmergencyAffordanceManager.needsEmergencyAffordance()) {
            return false;
        }
        this.mEmergencyAffordanceManager.performEmergencyCall();
        return true;
    }

    @Override // android.widget.TextView, android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        int x = (int) motionEvent.getX();
        int y = (int) motionEvent.getY();
        if (motionEvent.getActionMasked() == 0) {
            this.mDownX = x;
            this.mDownY = y;
            this.mLongPressWasDragged = false;
        } else {
            int abs = Math.abs(x - this.mDownX);
            int abs2 = Math.abs(y - this.mDownY);
            int scaledTouchSlop = ViewConfiguration.get(((Button) this).mContext).getScaledTouchSlop();
            if (Math.abs(abs2) > scaledTouchSlop || Math.abs(abs) > scaledTouchSlop) {
                this.mLongPressWasDragged = true;
            }
        }
        return super.onTouchEvent(motionEvent);
    }

    @Override // android.widget.TextView, android.view.View
    public boolean performLongClick() {
        return super.performLongClick();
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.TextView, android.view.View
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        requestCellInfoUpdate();
        updateEmergencyCallButton();
    }

    public void takeEmergencyCallAction() {
        if (!this.mDebounce) {
            this.mDebounce = true;
            this.mHandler.removeMessages(100);
            Handler handler = this.mHandler;
            handler.sendMessageDelayed(handler.obtainMessage(100), 500);
            if (Build.DEBUG_ONEPLUS) {
                Log.d("EmergencyButton", "takeEmergencyCallAction, isInCall():" + isInCall() + ", stack:" + Debug.getCallers(3));
            }
            MetricsLogger.action(((Button) this).mContext, 200);
            PowerManager powerManager = this.mPowerManager;
            if (powerManager != null) {
                powerManager.userActivity(SystemClock.uptimeMillis(), true);
            }
            try {
                ActivityTaskManager.getService().stopSystemLockTaskMode();
            } catch (RemoteException unused) {
                Slog.w("EmergencyButton", "Failed to stop app pinning");
            }
            if (isInCall()) {
                resumeCall();
                EmergencyButtonCallback emergencyButtonCallback = this.mEmergencyButtonCallback;
                if (emergencyButtonCallback != null) {
                    emergencyButtonCallback.onEmergencyButtonClickedWhenInCall();
                    return;
                }
                return;
            }
            KeyguardUpdateMonitor keyguardUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
            if (keyguardUpdateMonitor != null) {
                keyguardUpdateMonitor.reportEmergencyCallAction(true);
            } else {
                Log.w("EmergencyButton", "KeyguardUpdateMonitor was null, launching intent anyway.");
            }
            TelecomManager telecommManager = getTelecommManager();
            if (telecommManager == null) {
                Log.wtf("EmergencyButton", "TelecomManager was null, cannot launch emergency dialer");
                return;
            }
            getContext().startActivityAsUser(telecommManager.createLaunchEmergencyDialerIntent(null).setFlags(343932928).putExtra("com.android.phone.EmergencyDialer.extra.ENTRY_TYPE", 1), ActivityOptions.makeCustomAnimation(getContext(), 0, 0).toBundle(), new UserHandle(KeyguardUpdateMonitor.getCurrentUser()));
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0053, code lost:
        if (r4 == false) goto L_0x0010;
     */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0096  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x00b4  */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x014f  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0171  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateEmergencyCallButton() {
        /*
        // Method dump skipped, instructions count: 394
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.EmergencyButton.updateEmergencyCallButton():void");
    }

    public void setCallback(EmergencyButtonCallback emergencyButtonCallback) {
        this.mEmergencyButtonCallback = emergencyButtonCallback;
    }

    private void resumeCall() {
        if (getTelecommManager() == null) {
            Log.d("EmergencyButton", " resumeCall: getTelecommManager is null");
        } else {
            getTelecommManager().showInCallScreen(false);
        }
    }

    private boolean isInCall() {
        if (getTelecommManager() == null) {
            Log.d("EmergencyButton", " isInCall: getTelecommManager is null");
            return false;
        } else if (((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).hasBootCompleted()) {
            return getTelecommManager().isInCall();
        } else {
            Log.d("EmergencyButton", "return isInCall before BootCompleted");
            return false;
        }
    }

    private TelecomManager getTelecommManager() {
        return (TelecomManager) ((Button) this).mContext.getSystemService("telecom");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void requestCellInfoUpdate() {
        List<SubscriptionInfo> subscriptionInfo = ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).getSubscriptionInfo(true);
        if (subscriptionInfo.size() > 0) {
            for (SubscriptionInfo subscriptionInfo2 : subscriptionInfo) {
                requestCellInfoUpdate(subscriptionInfo2.getSubscriptionId(), subscriptionInfo2.getSimSlotIndex());
            }
            return;
        }
        requestCellInfoUpdate(-1, 0);
    }

    private void requestCellInfoUpdate(int i, final int i2) {
        getTelephonyManager().createForSubscriptionId(i).requestCellInfoUpdate(((Button) this).mContext.getMainExecutor(), new TelephonyManager.CellInfoCallback() { // from class: com.android.keyguard.EmergencyButton.3
            @Override // android.telephony.TelephonyManager.CellInfoCallback
            public void onCellInfo(List<CellInfo> list) {
                if (list == null || list.isEmpty()) {
                    Log.d("EmergencyButton", "requestCellInfoUpdate.onCellInfo is null or empty on phone" + i2);
                    EmergencyButton.this.mEmergencyCapable[i2] = false;
                } else {
                    EmergencyButton.this.mEmergencyCapable[i2] = true;
                }
                EmergencyButton.this.updateEmergencyCallButton();
            }
        });
    }

    private boolean isEmergencyCapable() {
        int i = 0;
        while (true) {
            boolean[] zArr = this.mEmergencyCapable;
            if (i >= zArr.length) {
                return false;
            }
            if (zArr[i]) {
                return true;
            }
            i++;
        }
    }
}
