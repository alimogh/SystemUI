package com.oneplus.aod.utils.bitmoji.triggers.base;

import android.content.Context;
import android.os.Build;
import android.os.Debug;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Log;
import com.android.internal.util.IndentingPrintWriter;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.oneplus.aod.utils.bitmoji.OpBitmojiManager;
import java.io.FileDescriptor;
public abstract class DelayTrigger extends CategoryTrigger {
    private static final int SYS_DELAY_TIME = SystemProperties.getInt("sys.aod.bitmoji.delaytime", 600000);
    protected KeyguardUpdateMonitorCallback mCallback;
    protected long mInActiveTime = -1;
    protected boolean mIsDelayed;
    private Runnable mResetAndShow = new Runnable() { // from class: com.oneplus.aod.utils.bitmoji.triggers.base.-$$Lambda$DelayTrigger$Gal1il_whCr5i8wA70Byx5LmFKc
        @Override // java.lang.Runnable
        public final void run() {
            DelayTrigger.this.shouldResetAndShowAfterScreenOff();
        }
    };
    protected boolean mTriggerActive;

    public boolean enableDelay() {
        return true;
    }

    public abstract String getTriggerId();

    /* access modifiers changed from: protected */
    public abstract boolean isActiveInner();

    public DelayTrigger(Context context, OpBitmojiManager opBitmojiManager) {
        super(context, opBitmojiManager);
    }

    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.Trigger
    public void init() {
        super.init();
        if (enableDelay()) {
            getKeyguardUpdateMonitor().registerCallback(getUpdateMonitorCallback());
        }
    }

    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.Trigger
    public boolean isActive() {
        if (enableDelay() && this.mIsDelayed) {
            if (SystemClock.elapsedRealtime() - this.mStartTime < ((long) Trigger.IMAGE_PER_TIME) * ((long) Trigger.IMAGES_PER_SET)) {
                return true;
            }
            Log.w(this.mTag, "isActive: delay flag is still true but duration is exceed.");
        }
        return isActiveInner();
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.Trigger
    public void onCompleted() {
        this.mIsDelayed = false;
    }

    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.CategoryTrigger, com.oneplus.aod.utils.bitmoji.triggers.base.Trigger
    public void dumpDetail(FileDescriptor fileDescriptor, IndentingPrintWriter indentingPrintWriter, String[] strArr) {
        indentingPrintWriter.println("enableDelay=" + enableDelay());
        indentingPrintWriter.println("isDelayed=" + this.mIsDelayed);
        indentingPrintWriter.println("inActiveTime=" + this.mInActiveTime);
    }

    public void reset() {
        reset(false);
    }

    private void reset(boolean z) {
        if (!z) {
            if (this.mInActiveTime != -1) {
                if (Build.DEBUG_ONEPLUS) {
                    String str = this.mTag;
                    Log.d(str, "reset: callers= " + Debug.getCallers(1));
                }
                this.mInActiveTime = -1;
            }
            if (!isActiveInner()) {
                this.mTriggerActive = false;
            }
        }
        this.mIsDelayed = false;
    }

    /* access modifiers changed from: protected */
    public KeyguardUpdateMonitorCallback getUpdateMonitorCallback() {
        if (this.mCallback == null) {
            this.mCallback = new TriggerUpdateMonitorCallback();
        }
        return this.mCallback;
    }

    /* access modifiers changed from: protected */
    public void onTriggerChanged(String str, boolean z) {
        if (this.mTriggerActive != z) {
            this.mTriggerActive = z;
            if (!z) {
                if (this.mIsDelayed) {
                    Log.i(this.mTag, "we are in delay progress, block it!");
                    return;
                } else {
                    this.mInActiveTime = SystemClock.elapsedRealtime();
                    reset(true);
                }
            }
            this.mBitmojiManager.onTriggerChanged(str, z);
        }
    }

    private boolean hasTimeToDelayShow() {
        if (this.mInActiveTime != -1 && SystemClock.elapsedRealtime() - this.mInActiveTime < ((long) SYS_DELAY_TIME)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void shouldResetAndShowAfterScreenOff() {
        boolean isActiveInner = isActiveInner();
        boolean hasTimeToDelayShow = hasTimeToDelayShow();
        if ((isActiveInner || hasTimeToDelayShow) && !this.mIsDelayed) {
            String str = this.mTag;
            Log.d(str, "shouldResetAndShowAfterScreenOff: isActiveInner= " + isActiveInner + ", hasTimeToDelayShow= " + hasTimeToDelayShow);
            this.mIsDelayed = true;
            onTriggerChanged(getTriggerId(), true);
        }
    }

    /* access modifiers changed from: protected */
    public class TriggerUpdateMonitorCallback extends KeyguardUpdateMonitorCallback {
        protected TriggerUpdateMonitorCallback() {
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onStartedGoingToSleep(int i) {
            if (DelayTrigger.this.getHandler() != null) {
                DelayTrigger.this.getHandler().post(DelayTrigger.this.mResetAndShow);
            } else {
                Log.e(DelayTrigger.this.mTag, "onStartedGoingToSleep: handler is null");
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onStartedWakingUp() {
            if (DelayTrigger.this.getHandler() != null) {
                DelayTrigger.this.getHandler().removeCallbacks(DelayTrigger.this.mResetAndShow);
            } else {
                Log.e(DelayTrigger.this.mTag, "onStartedWakingUp: handler is null");
            }
        }
    }
}
