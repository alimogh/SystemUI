package com.android.systemui.qs.carrier;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.ActivityStarter;
public class QSCarrierGroup extends LinearLayout implements View.OnClickListener {
    public QSCarrierGroup(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.qs.carrier.-$$Lambda$5PGGtCuNaapATBUmRqqxoqpC1BE
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                QSCarrierGroup.this.onClick(view);
            }
        });
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (view.isVisibleToUser() && KeyguardUpdateMonitor.getCurrentUser() == 0) {
            ((ActivityStarter) Dependency.get(ActivityStarter.class)).postStartActivityDismissingKeyguard(new Intent("oneplus.intent.action.SIM_AND_NETWORK_SETTINGS"), 0);
        }
    }
}
