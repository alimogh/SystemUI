package com.oneplus.keyguard.clock;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextClock;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
public class OpTextClock extends TextClock {
    private KeyguardUpdateMonitorCallback mMonitorCallback;
    private boolean mRegistered;

    public OpTextClock(Context context) {
        this(context, null, 0);
    }

    public OpTextClock(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public OpTextClock(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public OpTextClock(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mMonitorCallback = new KeyguardUpdateMonitorCallback() { // from class: com.oneplus.keyguard.clock.OpTextClock.1
            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onTimeChanged() {
                OpTextClock.this.refresh();
            }
        };
        disableClockTick();
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.TextClock, android.widget.TextView, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!this.mRegistered) {
            this.mRegistered = true;
            registerReceiver();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.TextClock, android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mRegistered) {
            unregisterReceiver();
            this.mRegistered = false;
        }
    }

    private void registerReceiver() {
        KeyguardUpdateMonitor.getInstance(getContext()).registerCallback(this.mMonitorCallback);
    }

    private void unregisterReceiver() {
        KeyguardUpdateMonitor.getInstance(getContext()).removeCallback(this.mMonitorCallback);
    }

    public void refresh() {
        super.refreshTime();
    }
}
