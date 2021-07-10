package com.oneplus.systemui.qs;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import com.android.systemui.C0004R$color;
import com.android.systemui.C0008R$id;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.statusbar.policy.Clock;
import com.android.systemui.statusbar.policy.DateView;
import com.oneplus.util.OpUtils;
import com.oneplus.util.ThemeColorUtils;
public class OpQSDateTimePanelLayout extends FrameLayout implements View.OnClickListener {
    private final ActivityStarter mActivityStarter = ((ActivityStarter) Dependency.get(ActivityStarter.class));
    private Clock mClockView;
    private DateView mDateView;

    public OpQSDateTimePanelLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        Clock clock = (Clock) findViewById(C0008R$id.op_qs_clock);
        this.mClockView = clock;
        clock.setOnClickListener(this);
        DateView dateView = (DateView) findViewById(C0008R$id.date);
        this.mDateView = dateView;
        dateView.setOnClickListener(this);
        updateThemeColor();
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (view == this.mClockView || view == this.mDateView) {
            this.mActivityStarter.postStartActivityDismissingKeyguard(new Intent("android.intent.action.SHOW_ALARMS"), 0);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        this.mClockView.useWallpaperTextColor(false);
    }

    private void updateThemeColor() {
        Context context;
        int color = ThemeColorUtils.getColor(1);
        if (OpUtils.isREDVersion() && (context = ((FrameLayout) this).mContext) != null) {
            color = context.getColor(C0004R$color.op_turquoise);
        }
        this.mDateView.setTextColor(color);
    }
}
