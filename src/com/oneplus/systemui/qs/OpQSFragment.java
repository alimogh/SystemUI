package com.oneplus.systemui.qs;

import android.os.Build;
import android.util.Log;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.Dependency;
import com.android.systemui.util.LifecycleFragment;
import com.oneplus.util.OpUtils;
public class OpQSFragment extends LifecycleFragment {
    private static final boolean DEBUG = Build.DEBUG_ONEPLUS;
    private boolean mLastIsCurrentQsExpand = false;
    private boolean mLastIsKeyguardVisible = false;
    private boolean mLastVisible = false;
    private boolean mVisible;

    public void setExpansionHight(float f) {
        KeyguardUpdateMonitor keyguardUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        boolean z = f != 0.0f;
        boolean isQSExpanded = keyguardUpdateMonitor.isQSExpanded();
        boolean isKeyguardVisible = keyguardUpdateMonitor.isKeyguardVisible();
        if (DEBUG && !(this.mLastVisible == z && this.mLastIsCurrentQsExpand == isQSExpanded && this.mLastIsKeyguardVisible == isKeyguardVisible)) {
            Log.d("OpQS", "setExpansionHeight: mVisible= " + this.mLastVisible + "->" + z + ", isCurrentQsExpand= " + this.mLastIsCurrentQsExpand + "->" + isQSExpanded + ", isKeyguardVisible= " + this.mLastIsKeyguardVisible + "->" + isKeyguardVisible);
        }
        this.mLastVisible = z;
        this.mLastIsCurrentQsExpand = isQSExpanded;
        this.mLastIsKeyguardVisible = isKeyguardVisible;
        if (isKeyguardVisible) {
            if (this.mVisible != z || z != isQSExpanded) {
                this.mVisible = z;
                if (OpUtils.isCustomFingerprint()) {
                    keyguardUpdateMonitor.setQSExpanded(this.mVisible);
                }
            }
        }
    }
}
