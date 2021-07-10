package com.android.keyguard;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.android.systemui.C0002R$attr;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0016R$style;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.oneplus.keyguard.OpKeyguardMessageArea;
import com.oneplus.util.OpUtils;
import java.lang.ref.WeakReference;
public class KeyguardMessageArea extends OpKeyguardMessageArea implements SecurityMessageDisplay, ConfigurationController.ConfigurationListener {
    private static final Object ANNOUNCE_TOKEN = new Object();
    private boolean mBouncerVisible;
    private final ConfigurationController mConfigurationController;
    private ColorStateList mDefaultColorState;
    private final Handler mHandler;
    private KeyguardUpdateMonitorCallback mInfoCallback;
    private long mIsFacelockBouncerChangeToVisibleTime;
    private CharSequence mMessage;
    private ColorStateList mNextMessageColorState;
    private final Runnable mPostDelayToUpdateMessage;

    public KeyguardMessageArea(Context context) {
        super(context, null);
        this.mNextMessageColorState = ColorStateList.valueOf(-1);
        this.mIsFacelockBouncerChangeToVisibleTime = 0;
        this.mInfoCallback = new KeyguardUpdateMonitorCallback() { // from class: com.android.keyguard.KeyguardMessageArea.1
            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onFinishedGoingToSleep(int i) {
                KeyguardMessageArea.this.setSelected(false);
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onStartedWakingUp() {
                KeyguardMessageArea.this.setSelected(true);
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onKeyguardBouncerChanged(boolean z) {
                boolean isFacelockRecognizing = KeyguardUpdateMonitor.getInstance(((TextView) KeyguardMessageArea.this).mContext).isFacelockRecognizing();
                if (OpUtils.DEBUG_ONEPLUS) {
                    Log.i("KeyguardMessageArea", "onKeyguardBouncerChanged , bouncer:" + z + ", mBouncerVisible:" + KeyguardMessageArea.this.mBouncerVisible + ", isFacelockRecognizing:" + isFacelockRecognizing + ", facelockType:" + KeyguardUpdateMonitor.getInstance(((TextView) KeyguardMessageArea.this).mContext).getFacelockRunningType() + ", this:" + this);
                }
                if (isFacelockRecognizing && !KeyguardMessageArea.this.mBouncerVisible && z) {
                    KeyguardMessageArea.this.mIsFacelockBouncerChangeToVisibleTime = System.currentTimeMillis();
                }
                KeyguardMessageArea.this.mBouncerVisible = z;
                KeyguardMessageArea.this.update();
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onKeyguardVisibilityChanged(boolean z) {
                if (!z) {
                    KeyguardMessageArea.this.setSelected(false);
                }
            }
        };
        this.mPostDelayToUpdateMessage = new Runnable() { // from class: com.android.keyguard.KeyguardMessageArea.2
            @Override // java.lang.Runnable
            public void run() {
                Log.i("KeyguardMessageArea", "mPostDelayToUpdateMessage");
                KeyguardMessageArea.this.mIsFacelockBouncerChangeToVisibleTime = 0;
                KeyguardMessageArea.this.update();
            }
        };
        throw new IllegalStateException("This constructor should never be invoked");
    }

    public KeyguardMessageArea(Context context, AttributeSet attributeSet, ConfigurationController configurationController) {
        this(context, attributeSet, (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class), configurationController);
    }

    public KeyguardMessageArea(Context context, AttributeSet attributeSet, KeyguardUpdateMonitor keyguardUpdateMonitor, ConfigurationController configurationController) {
        super(context, attributeSet, keyguardUpdateMonitor, configurationController);
        this.mNextMessageColorState = ColorStateList.valueOf(-1);
        this.mIsFacelockBouncerChangeToVisibleTime = 0;
        this.mInfoCallback = new KeyguardUpdateMonitorCallback() { // from class: com.android.keyguard.KeyguardMessageArea.1
            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onFinishedGoingToSleep(int i) {
                KeyguardMessageArea.this.setSelected(false);
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onStartedWakingUp() {
                KeyguardMessageArea.this.setSelected(true);
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onKeyguardBouncerChanged(boolean z) {
                boolean isFacelockRecognizing = KeyguardUpdateMonitor.getInstance(((TextView) KeyguardMessageArea.this).mContext).isFacelockRecognizing();
                if (OpUtils.DEBUG_ONEPLUS) {
                    Log.i("KeyguardMessageArea", "onKeyguardBouncerChanged , bouncer:" + z + ", mBouncerVisible:" + KeyguardMessageArea.this.mBouncerVisible + ", isFacelockRecognizing:" + isFacelockRecognizing + ", facelockType:" + KeyguardUpdateMonitor.getInstance(((TextView) KeyguardMessageArea.this).mContext).getFacelockRunningType() + ", this:" + this);
                }
                if (isFacelockRecognizing && !KeyguardMessageArea.this.mBouncerVisible && z) {
                    KeyguardMessageArea.this.mIsFacelockBouncerChangeToVisibleTime = System.currentTimeMillis();
                }
                KeyguardMessageArea.this.mBouncerVisible = z;
                KeyguardMessageArea.this.update();
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onKeyguardVisibilityChanged(boolean z) {
                if (!z) {
                    KeyguardMessageArea.this.setSelected(false);
                }
            }
        };
        this.mPostDelayToUpdateMessage = new Runnable() { // from class: com.android.keyguard.KeyguardMessageArea.2
            @Override // java.lang.Runnable
            public void run() {
                Log.i("KeyguardMessageArea", "mPostDelayToUpdateMessage");
                KeyguardMessageArea.this.mIsFacelockBouncerChangeToVisibleTime = 0;
                KeyguardMessageArea.this.update();
            }
        };
        setLayerType(2, null);
        keyguardUpdateMonitor.registerCallback(this.mInfoCallback);
        this.mHandler = new Handler(Looper.myLooper());
        this.mConfigurationController = configurationController;
        onThemeChanged();
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.TextView, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mConfigurationController.addCallback(this);
        onThemeChanged();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mConfigurationController.removeCallback(this);
    }

    @Override // com.android.keyguard.SecurityMessageDisplay
    public void setNextMessageColor(ColorStateList colorStateList) {
        this.mNextMessageColorState = colorStateList;
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onThemeChanged() {
        TypedArray obtainStyledAttributes = ((TextView) this).mContext.obtainStyledAttributes(new int[]{C0002R$attr.wallpaperTextColor});
        ColorStateList valueOf = ColorStateList.valueOf(obtainStyledAttributes.getColor(0, -65536));
        obtainStyledAttributes.recycle();
        this.mDefaultColorState = valueOf;
        update();
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onDensityOrFontScaleChanged() {
        TypedArray obtainStyledAttributes = ((TextView) this).mContext.obtainStyledAttributes(C0016R$style.OpKeyguard_TextView, new int[]{16842901});
        setTextSize(0, (float) obtainStyledAttributes.getDimensionPixelSize(0, 0));
        obtainStyledAttributes.recycle();
    }

    @Override // com.android.keyguard.SecurityMessageDisplay
    public void setMessage(CharSequence charSequence) {
        if (!TextUtils.isEmpty(charSequence)) {
            securityMessageChanged(charSequence);
        } else {
            clearMessage();
        }
    }

    @Override // com.android.keyguard.SecurityMessageDisplay
    public void setMessage(int i) {
        setMessage(i != 0 ? getContext().getResources().getText(i) : null);
    }

    public static KeyguardMessageArea findSecurityMessageDisplay(View view) {
        KeyguardMessageArea keyguardMessageArea = (KeyguardMessageArea) view.findViewById(C0008R$id.keyguard_message_area);
        if (keyguardMessageArea == null) {
            keyguardMessageArea = (KeyguardMessageArea) view.getRootView().findViewById(C0008R$id.keyguard_message_area);
        }
        if (keyguardMessageArea != null) {
            return keyguardMessageArea;
        }
        throw new RuntimeException("Can't find keyguard_message_area in " + view.getClass());
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        setSelected(((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isDeviceInteractive());
    }

    private void securityMessageChanged(CharSequence charSequence) {
        Object obj = ANNOUNCE_TOKEN;
        this.mMessage = charSequence;
        update();
        this.mHandler.removeCallbacksAndMessages(obj);
        this.mHandler.postAtTime(new AnnounceRunnable(this, getText()), obj, SystemClock.uptimeMillis() + 250);
    }

    private void clearMessage() {
        this.mMessage = null;
        this.mMessageType = 0;
        update();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x007a  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x00c0  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x00c9  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void update() {
        /*
            r9 = this;
            java.lang.CharSequence r0 = r9.mMessage
            boolean r1 = r9.mBouncerVisible
            r2 = 0
            java.lang.String r3 = "KeyguardMessageArea"
            if (r1 != 0) goto L_0x0019
            java.lang.Class<com.android.keyguard.KeyguardUpdateMonitor> r1 = com.android.keyguard.KeyguardUpdateMonitor.class
            java.lang.Object r1 = com.android.systemui.Dependency.get(r1)
            com.android.keyguard.KeyguardUpdateMonitor r1 = (com.android.keyguard.KeyguardUpdateMonitor) r1
            boolean r1 = r1.isSimPinSecure()
            if (r1 != 0) goto L_0x0019
        L_0x0017:
            r0 = r2
            goto L_0x0071
        L_0x0019:
            long r4 = r9.mIsFacelockBouncerChangeToVisibleTime
            r6 = 0
            int r1 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1))
            if (r1 <= 0) goto L_0x0071
            long r4 = java.lang.System.currentTimeMillis()
            long r6 = r9.mIsFacelockBouncerChangeToVisibleTime
            long r6 = r4 - r6
            boolean r1 = com.oneplus.util.OpUtils.DEBUG_ONEPLUS
            if (r1 == 0) goto L_0x0053
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r8 = "now:"
            r1.append(r8)
            r1.append(r4)
            java.lang.String r4 = ", mIsFacelockBouncerChangeToVisibleTime:"
            r1.append(r4)
            long r4 = r9.mIsFacelockBouncerChangeToVisibleTime
            r1.append(r4)
            java.lang.String r4 = ", interval:"
            r1.append(r4)
            r1.append(r6)
            java.lang.String r1 = r1.toString()
            android.util.Log.i(r3, r1)
        L_0x0053:
            r4 = 500(0x1f4, double:2.47E-321)
            int r1 = (r6 > r4 ? 1 : (r6 == r4 ? 0 : -1))
            if (r1 >= 0) goto L_0x0071
            android.os.Handler r0 = r9.mHandler
            java.lang.Runnable r1 = r9.mPostDelayToUpdateMessage
            boolean r0 = r0.hasCallbacks(r1)
            if (r0 != 0) goto L_0x0017
            java.lang.String r0 = "postDelayed mPostDelayToUpdateMessage"
            android.util.Log.i(r3, r0)
            android.os.Handler r0 = r9.mHandler
            java.lang.Runnable r1 = r9.mPostDelayToUpdateMessage
            long r4 = r4 - r6
            r0.postDelayed(r1, r4)
            goto L_0x0017
        L_0x0071:
            boolean r1 = r9.mBouncerVisible
            r9.setTextWithAnim(r0, r1)
            boolean r1 = com.oneplus.util.OpUtils.DEBUG_ONEPLUS
            if (r1 == 0) goto L_0x0099
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "update, status:"
            r1.append(r2)
            r1.append(r0)
            java.lang.String r2 = ", bouncer:"
            r1.append(r2)
            boolean r2 = r9.mBouncerVisible
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            android.util.Log.i(r3, r1)
        L_0x0099:
            int r1 = r9.mMessageType
            r2 = 1
            if (r1 != r2) goto L_0x00b8
            if (r0 == 0) goto L_0x00b8
            int r1 = r0.length()
            if (r1 == 0) goto L_0x00b8
            java.lang.String r0 = r0.toString()
            java.lang.String r1 = " "
            boolean r0 = r1.equals(r0)
            if (r0 != 0) goto L_0x00b8
            r0 = 0
            r9.mMessageType = r0
            r9.animateErrorText(r9)
        L_0x00b8:
            android.content.res.ColorStateList r0 = r9.mDefaultColorState
            boolean r1 = com.oneplus.util.OpUtils.isCustomFingerprint()
            if (r1 == 0) goto L_0x00c9
            android.content.Context r0 = r9.mContext
            int r1 = com.android.systemui.C0002R$attr.wallpaperTextColor
            android.content.res.ColorStateList r0 = com.android.settingslib.Utils.getColorAttr(r0, r1)
            goto L_0x00da
        L_0x00c9:
            android.content.res.ColorStateList r1 = r9.mNextMessageColorState
            int r1 = r1.getDefaultColor()
            r2 = -1
            if (r1 == r2) goto L_0x00da
            android.content.res.ColorStateList r0 = r9.mNextMessageColorState
            android.content.res.ColorStateList r1 = android.content.res.ColorStateList.valueOf(r2)
            r9.mNextMessageColorState = r1
        L_0x00da:
            r9.setTextColor(r0)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.KeyguardMessageArea.update():void");
    }

    /* access modifiers changed from: private */
    public static class AnnounceRunnable implements Runnable {
        private final WeakReference<View> mHost;
        private final CharSequence mTextToAnnounce;

        AnnounceRunnable(View view, CharSequence charSequence) {
            this.mHost = new WeakReference<>(view);
            this.mTextToAnnounce = charSequence;
        }

        @Override // java.lang.Runnable
        public void run() {
            View view = this.mHost.get();
            if (view != null) {
                view.announceForAccessibility(this.mTextToAnnounce);
            }
        }
    }
}
