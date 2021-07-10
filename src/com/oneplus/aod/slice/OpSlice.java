package com.oneplus.aod.slice;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.android.systemui.Dependency;
import com.oneplus.aod.slice.OpSliceManager;
import java.io.PrintWriter;
public abstract class OpSlice {
    protected static final boolean DEBUG = Build.DEBUG_ONEPLUS;
    protected OpSliceManager.Callback mCallback;
    protected H mHandler = new H((Looper) Dependency.get(Dependency.BG_LOOPER));
    protected int mIcon = 0;
    private boolean mIsActive = false;
    private boolean mIsEnabled = false;
    protected String mPrimary;
    protected String mRemark;
    protected String mSecondary;
    protected String mTag = ("OpSlice." + getClass().getSimpleName());

    public OpSlice(OpSliceManager.Callback callback) {
        this.mCallback = callback;
    }

    /* access modifiers changed from: protected */
    public void setListening(boolean z) {
        this.mHandler.obtainMessage(1, z ? 1 : 0, 0).sendToTarget();
    }

    /* access modifiers changed from: protected */
    public void setEnabled(boolean z) {
        if (this.mIsEnabled != z) {
            this.mHandler.obtainMessage(2, z ? 1 : 0, 0).sendToTarget();
            this.mIsEnabled = z;
        }
    }

    public int getIcon() {
        return this.mIcon;
    }

    public String getPrimaryString() {
        return this.mPrimary;
    }

    public String getRemark() {
        return this.mRemark;
    }

    public String getSecondaryString() {
        return this.mSecondary;
    }

    public void onTimeChanged() {
        this.mHandler.obtainMessage(3).sendToTarget();
    }

    /* access modifiers changed from: protected */
    public void updateUI() {
        OpSliceManager.Callback callback = this.mCallback;
        if (callback != null) {
            callback.updateUI();
        }
    }

    /* access modifiers changed from: protected */
    public void setActive(boolean z) {
        this.mIsActive = z;
    }

    /* access modifiers changed from: protected */
    public boolean isActive() {
        return this.mIsActive;
    }

    /* access modifiers changed from: protected */
    public boolean isEnabled() {
        return this.mIsEnabled;
    }

    /* access modifiers changed from: protected */
    public void handleSetListening(boolean z) {
        if (DEBUG) {
            String str = this.mTag;
            Log.i(str, "handleSetListening: " + z);
        }
    }

    /* access modifiers changed from: protected */
    public void handleSetEnabled(boolean z) {
        if (DEBUG) {
            String str = this.mTag;
            Log.d(str, "handleSetEnabled: " + z);
        }
    }

    /* access modifiers changed from: protected */
    public void handleTimeChanged() {
        if (DEBUG) {
            Log.d(this.mTag, "handleTimeChanged");
        }
    }

    /* access modifiers changed from: protected */
    public final class H extends Handler {
        protected H(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            int i = message.what;
            boolean z = false;
            if (i == 1) {
                OpSlice opSlice = OpSlice.this;
                if (message.arg1 == 1) {
                    z = true;
                }
                opSlice.handleSetListening(z);
            } else if (i == 2) {
                OpSlice opSlice2 = OpSlice.this;
                if (message.arg1 == 1) {
                    z = true;
                }
                opSlice2.handleSetEnabled(z);
            } else if (i == 3) {
                OpSlice.this.handleTimeChanged();
            }
        }
    }

    public void dump(PrintWriter printWriter) {
        printWriter.print(this.mTag);
        printWriter.println(":");
        printWriter.print("  mIsEnabled=");
        printWriter.print(this.mIsEnabled);
        printWriter.print(" mIsActive=");
        printWriter.println(this.mIsActive);
    }
}
