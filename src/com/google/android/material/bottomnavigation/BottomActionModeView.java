package com.google.android.material.bottomnavigation;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.material.R$attr;
import com.google.android.material.R$id;
import com.google.android.material.R$layout;
public class BottomActionModeView extends LinearLayout {
    public BottomActionModeView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, R$attr.bottomActionModeStyle);
    }

    public BottomActionModeView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        LayoutInflater.from(context).inflate(R$layout.control_bottom_actionmode_view, this);
        initView();
        setVisibility(8);
    }

    private void initView() {
        TextView textView = (TextView) findViewById(R$id.positive_button);
        TextView textView2 = (TextView) findViewById(R$id.negative_button);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.View, android.view.ViewGroup
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
    }
}
