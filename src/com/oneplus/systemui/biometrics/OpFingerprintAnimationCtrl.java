package com.oneplus.systemui.biometrics;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.ViewGroup;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0009R$integer;
import com.oneplus.systemui.biometrics.OpFrameAnimationHelper;
import com.oneplus.util.OpUtils;
public class OpFingerprintAnimationCtrl {
    private static final boolean DEBUG = Build.DEBUG_ONEPLUS;
    private long mAnimPostDelayTime = 0;
    private long mAnimPostDelayTimeOnAod = 0;
    private int mAnimationState = 0;
    private Context mContext;
    private int mCurAnimationType = 0;
    private long mCustAnimPostDelayTime = 0;
    private long mCustAnimPostDelayTimeOnAod = 0;
    private int mDownAnimFrameNum = 0;
    private int mDownAnimStartIndex = 0;
    private OpFrameAnimationHelper mDownAnimationHelper;
    private OpFingerprintAnimationView mDownAnimationView;
    private Handler mHandler = new Handler();
    private boolean mIsInteractive = false;
    private OpFrameAnimationHelper mOnGoingAnimationHelper;
    private OpFingerprintAnimationView mOnGoingAnimationView;
    ContentObserver mSettingsObserver = new ContentObserver(this.mHandler) { // from class: com.oneplus.systemui.biometrics.OpFingerprintAnimationCtrl.1
        @Override // android.database.ContentObserver
        public void onChange(boolean z) {
            super.onChange(z);
            OpFingerprintAnimationCtrl.this.checkAnimationValueValid();
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri) {
            super.onChange(z, uri);
            OpFingerprintAnimationCtrl.this.checkAnimationValueValid();
        }
    };
    private int mUpAnimFrameNum = 0;
    private int mUpAnimStartIndex = 0;
    private OpFrameAnimationHelper mUpAnimationHelper;
    private OpFingerprintAnimationView mUpAnimationView;

    /* access modifiers changed from: protected */
    public void checkAnimationValueValid() {
        int currentUser = KeyguardUpdateMonitor.getCurrentUser();
        int intForUser = Settings.System.getIntForUser(this.mContext.getContentResolver(), "op_custom_unlock_animation_style", 0, currentUser);
        Log.d("FingerprintAnimationCtrl", " checkAnimationValueValid: current: " + this.mCurAnimationType + " new: " + intForUser);
        if (!OpUtils.isMCLVersion()) {
            if (intForUser == 3 || intForUser == 10) {
                Settings.System.putIntForUser(this.mContext.getContentResolver(), "op_custom_unlock_animation_style", this.mCurAnimationType, currentUser);
                return;
            }
        } else if (intForUser == 11 && OpFingerprintAnimationResHelper.getDownEndFrameIndex(this.mContext, this.mCurAnimationType) == 0) {
            Settings.System.putIntForUser(this.mContext.getContentResolver(), "op_custom_unlock_animation_style", this.mCurAnimationType, currentUser);
            return;
        }
        this.mCurAnimationType = intForUser;
        this.mDownAnimStartIndex = OpFingerprintAnimationResHelper.getDownStartFrameIndex(this.mContext, intForUser);
        this.mDownAnimFrameNum = OpFingerprintAnimationResHelper.getDownPlayFrameNum(this.mContext, this.mCurAnimationType);
        this.mUpAnimStartIndex = OpFingerprintAnimationResHelper.getUpStartFrameIndex(this.mContext, this.mCurAnimationType);
        this.mUpAnimFrameNum = OpFingerprintAnimationResHelper.getUpPlayFrameNum(this.mContext, this.mCurAnimationType);
        if (!(this.mDownAnimationHelper == null && this.mUpAnimationHelper == null)) {
            this.mDownAnimationHelper = null;
            this.mUpAnimationHelper = null;
        }
        updateAnimationRes(this.mIsInteractive);
    }

    OpFingerprintAnimationCtrl(ViewGroup viewGroup, Context context) {
        this.mContext = context;
        this.mDownAnimationView = (OpFingerprintAnimationView) viewGroup.findViewById(C0008R$id.op_fingerprint_animation_view_1);
        this.mUpAnimationView = (OpFingerprintAnimationView) viewGroup.findViewById(C0008R$id.op_fingerprint_animation_view_3);
        try {
            this.mAnimPostDelayTime = (long) this.mContext.getResources().getInteger(C0009R$integer.fingerprint_animation_post_delay_time);
            this.mAnimPostDelayTimeOnAod = (long) this.mContext.getResources().getInteger(C0009R$integer.fingerprint_animation_post_delay_time_on_aod);
            this.mCustAnimPostDelayTime = (long) this.mContext.getResources().getInteger(C0009R$integer.fingerprint_cust_animation_post_delay_time);
            this.mCustAnimPostDelayTimeOnAod = (long) this.mContext.getResources().getInteger(C0009R$integer.fingerprint_cust_animation_post_delay_time_on_aod);
        } catch (Exception unused) {
            Log.e("FingerprintAnimationCtrl", "Parse fingerprint animation post delay time error");
            this.mAnimPostDelayTime = 0;
            this.mAnimPostDelayTimeOnAod = 0;
        }
        this.mSettingsObserver.onChange(true);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("op_custom_unlock_animation_style"), true, this.mSettingsObserver, -1);
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x006a  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0082  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0089  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x009f  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateAnimationRes(boolean r10) {
        /*
            r9 = this;
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = " updateanimationRes to "
            r0.append(r1)
            int r1 = r9.mCurAnimationType
            r0.append(r1)
            java.lang.String r1 = ", isInteractive = "
            r0.append(r1)
            r0.append(r10)
            java.lang.String r0 = r0.toString()
            java.lang.String r1 = "FingerprintAnimationCtrl"
            android.util.Log.d(r1, r0)
            r9.mIsInteractive = r10
            boolean r0 = com.oneplus.systemui.biometrics.OpFingerprintAnimationCtrl.DEBUG
            if (r0 == 0) goto L_0x0046
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r2 = "mDownAnimationHelper = "
            r0.append(r2)
            com.oneplus.systemui.biometrics.OpFrameAnimationHelper r2 = r9.mDownAnimationHelper
            r0.append(r2)
            java.lang.String r2 = ", mUpAnimationHelper = "
            r0.append(r2)
            com.oneplus.systemui.biometrics.OpFrameAnimationHelper r2 = r9.mUpAnimationHelper
            r0.append(r2)
            java.lang.String r0 = r0.toString()
            android.util.Log.d(r1, r0)
        L_0x0046:
            int r0 = r9.mCurAnimationType
            r1 = 11
            if (r0 != r1) goto L_0x005f
            long r0 = r9.mCustAnimPostDelayTime
            r2 = 0
            int r4 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1))
            if (r4 == 0) goto L_0x005f
            long r4 = r9.mCustAnimPostDelayTimeOnAod
            int r2 = (r4 > r2 ? 1 : (r4 == r2 ? 0 : -1))
            if (r2 == 0) goto L_0x005f
            if (r10 == 0) goto L_0x005d
            goto L_0x0066
        L_0x005d:
            r0 = r4
            goto L_0x0066
        L_0x005f:
            if (r10 == 0) goto L_0x0064
            long r0 = r9.mAnimPostDelayTime
            goto L_0x0066
        L_0x0064:
            long r0 = r9.mAnimPostDelayTimeOnAod
        L_0x0066:
            com.oneplus.systemui.biometrics.OpFrameAnimationHelper r10 = r9.mDownAnimationHelper
            if (r10 != 0) goto L_0x0082
            com.oneplus.systemui.biometrics.OpFrameAnimationHelper r10 = new com.oneplus.systemui.biometrics.OpFrameAnimationHelper
            com.oneplus.systemui.biometrics.OpFingerprintAnimationView r3 = r9.mDownAnimationView
            android.content.Context r2 = r9.mContext
            int r4 = r9.mCurAnimationType
            int[] r4 = com.oneplus.systemui.biometrics.OpFingerprintAnimationResHelper.getDownAnimationRes(r2, r4)
            int r7 = r9.mDownAnimStartIndex
            int r8 = r9.mDownAnimFrameNum
            r2 = r10
            r5 = r0
            r2.<init>(r3, r4, r5, r7, r8)
            r9.mDownAnimationHelper = r10
            goto L_0x0085
        L_0x0082:
            r10.updateAnimPostDelayTime(r0)
        L_0x0085:
            com.oneplus.systemui.biometrics.OpFrameAnimationHelper r10 = r9.mUpAnimationHelper
            if (r10 != 0) goto L_0x009f
            com.oneplus.systemui.biometrics.OpFrameAnimationHelper r10 = new com.oneplus.systemui.biometrics.OpFrameAnimationHelper
            com.oneplus.systemui.biometrics.OpFingerprintAnimationView r3 = r9.mUpAnimationView
            int r2 = r9.mCurAnimationType
            int[] r4 = com.oneplus.systemui.biometrics.OpFingerprintAnimationResHelper.getUpAnimationRes(r2)
            int r7 = r9.mUpAnimStartIndex
            int r8 = r9.mUpAnimFrameNum
            r2 = r10
            r5 = r0
            r2.<init>(r3, r4, r5, r7, r8)
            r9.mUpAnimationHelper = r10
            goto L_0x00a2
        L_0x009f:
            r10.updateAnimPostDelayTime(r0)
        L_0x00a2:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.systemui.biometrics.OpFingerprintAnimationCtrl.updateAnimationRes(boolean):void");
    }

    public void playAnimation(int i) {
        if (DEBUG) {
            Log.d("FingerprintAnimationCtrl", "playAnimation: type = " + i + ", current state = " + this.mAnimationState);
        }
        int i2 = this.mAnimationState;
        if (i == i2) {
            if (DEBUG) {
                Log.d("FingerprintAnimationCtrl", "playAnimation: type no change");
            }
        } else if (i2 != 0 || i == 1) {
            stopAnimation(i);
            if (i == 1) {
                OpFrameAnimationHelper opFrameAnimationHelper = this.mDownAnimationHelper;
                if (opFrameAnimationHelper != null) {
                    opFrameAnimationHelper.start(true);
                    this.mOnGoingAnimationView = this.mDownAnimationView;
                    this.mOnGoingAnimationHelper = this.mDownAnimationHelper;
                }
            } else if (i == 2 && this.mUpAnimationHelper != null) {
                this.mOnGoingAnimationView = this.mUpAnimationView;
                this.mDownAnimationHelper.stop();
                this.mUpAnimationHelper.start(false);
                this.mOnGoingAnimationHelper = this.mUpAnimationHelper;
            }
        } else if (DEBUG) {
            Log.d("FingerprintAnimationCtrl", "playAnimation: type none or not touch down");
        }
    }

    public void stopAnimation(int i) {
        OpFingerprintAnimationView opFingerprintAnimationView;
        OpFrameAnimationHelper opFrameAnimationHelper;
        Log.d("FingerprintAnimationCtrl", "stopAnimation: current state = " + this.mAnimationState + ", mOnGoingAnimationView = " + this.mOnGoingAnimationView);
        if (!(this.mAnimationState == 0 || (opFingerprintAnimationView = this.mOnGoingAnimationView) == null)) {
            if (opFingerprintAnimationView != null) {
                if (!opFingerprintAnimationView.equals(this.mDownAnimationView) || (opFrameAnimationHelper = this.mDownAnimationHelper) == null) {
                    OpFrameAnimationHelper opFrameAnimationHelper2 = this.mUpAnimationHelper;
                    if (opFrameAnimationHelper2 != null) {
                        opFrameAnimationHelper2.stop();
                    }
                } else {
                    opFrameAnimationHelper.stop();
                }
                this.mOnGoingAnimationView = null;
                this.mOnGoingAnimationHelper = null;
            } else {
                return;
            }
        }
        this.mAnimationState = i;
    }

    public void updateAnimationDelayTime(boolean z) {
        long j = z ? this.mAnimPostDelayTime : this.mAnimPostDelayTimeOnAod;
        OpFrameAnimationHelper opFrameAnimationHelper = this.mDownAnimationHelper;
        if (opFrameAnimationHelper != null) {
            opFrameAnimationHelper.updateAnimPostDelayTime(j);
        }
        OpFrameAnimationHelper opFrameAnimationHelper2 = this.mUpAnimationHelper;
        if (opFrameAnimationHelper2 != null) {
            opFrameAnimationHelper2.updateAnimPostDelayTime(j);
        }
    }

    public void resetState() {
        Log.d("FingerprintAnimationCtrl", "resetState");
        OpFrameAnimationHelper opFrameAnimationHelper = this.mDownAnimationHelper;
        if (opFrameAnimationHelper != null) {
            opFrameAnimationHelper.stop();
            this.mDownAnimationHelper.resetResource();
        }
        OpFrameAnimationHelper opFrameAnimationHelper2 = this.mUpAnimationHelper;
        if (opFrameAnimationHelper2 != null) {
            opFrameAnimationHelper2.stop();
            this.mUpAnimationHelper.resetResource();
        }
        this.mDownAnimationHelper = null;
        this.mUpAnimationHelper = null;
    }

    public void updateLayoutDimension(boolean z) {
        int dimension = (int) this.mContext.getResources().getDimension(z ? C0005R$dimen.fp_animation_width_2k : C0005R$dimen.fp_animation_width_1080p);
        int dimension2 = (int) this.mContext.getResources().getDimension(z ? C0005R$dimen.fp_animation_height_2k : C0005R$dimen.fp_animation_height_1080p);
        ViewGroup.LayoutParams layoutParams = this.mDownAnimationView.getLayoutParams();
        layoutParams.width = dimension;
        layoutParams.height = dimension2;
        this.mDownAnimationView.setLayoutParams(layoutParams);
        ViewGroup.LayoutParams layoutParams2 = this.mUpAnimationView.getLayoutParams();
        layoutParams2.width = dimension;
        layoutParams2.height = dimension2;
        this.mUpAnimationView.setLayoutParams(layoutParams2);
    }

    public void waitAnimationFinished(OpFrameAnimationHelper.Callbacks callbacks) {
        OpFrameAnimationHelper opFrameAnimationHelper;
        OpFrameAnimationHelper opFrameAnimationHelper2;
        if (DEBUG) {
            Log.i("FingerprintAnimationCtrl", "register fp animation's callback = " + callbacks + ", animationState = " + this.mAnimationState);
        }
        int i = this.mAnimationState;
        if (i == 0) {
            Log.e("FingerprintAnimationCtrl", "It shouldn't go into the state.");
        } else if (i == 1 && (opFrameAnimationHelper2 = this.mDownAnimationHelper) != null) {
            opFrameAnimationHelper2.waitAnimationFinished(callbacks);
        } else if (this.mAnimationState == 2 && (opFrameAnimationHelper = this.mUpAnimationHelper) != null) {
            opFrameAnimationHelper.waitAnimationFinished(callbacks);
        }
    }

    public boolean isPlayingAnimation() {
        OpFrameAnimationHelper opFrameAnimationHelper = this.mOnGoingAnimationHelper;
        if (opFrameAnimationHelper != null) {
            return opFrameAnimationHelper.isAnimationRunning();
        }
        return false;
    }
}
