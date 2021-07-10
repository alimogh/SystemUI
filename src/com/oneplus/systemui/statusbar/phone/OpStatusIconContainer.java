package com.oneplus.systemui.statusbar.phone;

import android.content.Context;
import android.util.AttributeSet;
import com.android.keyguard.AlphaOptimizedLinearLayout;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.tuner.TunerService;
import com.oneplus.util.OpUtils;
public class OpStatusIconContainer extends AlphaOptimizedLinearLayout implements BatteryController.BatteryStateChangeCallback, TunerService.Tunable {
    protected static final int MAX_DOTS = OpUtils.getMaxDotsForStatusIconContainer();

    /* access modifiers changed from: protected */
    public int setUnderflowWidth(int i, int i2, int i3) {
        return 0;
    }

    public OpStatusIconContainer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "clock_seconds");
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((TunerService) Dependency.get(TunerService.class)).removeTunable(this);
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        if ("clock_seconds".equals(str)) {
            postDelayed(new Runnable() { // from class: com.oneplus.systemui.statusbar.phone.-$$Lambda$OpStatusIconContainer$t7R1h1AK4XR5Rd4ZPAhR22negYQ
                @Override // java.lang.Runnable
                public final void run() {
                    OpStatusIconContainer.this.lambda$onTuningChanged$0$OpStatusIconContainer();
                }
            }, 100);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onTuningChanged$0 */
    public /* synthetic */ void lambda$onTuningChanged$0$OpStatusIconContainer() {
        requestLayout();
    }
}
