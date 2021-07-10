package com.oneplus.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.widget.LinearLayout;
public class OpExpandButtonLayout extends LinearLayout {
    private OnConfigurationChangeListener mOnConfigurationChangeListener;

    public interface OnConfigurationChangeListener {
        void onConfigurationChanged(Configuration configuration);
    }

    public OpExpandButtonLayout(Context context) {
        super(context);
    }

    public OpExpandButtonLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public OpExpandButtonLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        OnConfigurationChangeListener onConfigurationChangeListener = this.mOnConfigurationChangeListener;
        if (onConfigurationChangeListener != null) {
            onConfigurationChangeListener.onConfigurationChanged(configuration);
        }
    }

    public void setOnConfigurationChangeListener(OnConfigurationChangeListener onConfigurationChangeListener) {
        this.mOnConfigurationChangeListener = onConfigurationChangeListener;
    }
}
