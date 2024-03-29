package com.android.settingslib.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.core.content.res.TypedArrayUtils;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
public class LayoutPreference extends Preference {
    private boolean mAllowDividerAbove;
    private boolean mAllowDividerBelow;
    private final View.OnClickListener mClickListener = new View.OnClickListener() { // from class: com.android.settingslib.widget.-$$Lambda$LayoutPreference$A_OWgARxS1B51rTsCCoDBOGYAP0
        @Override // android.view.View.OnClickListener
        public final void onClick(View view) {
            LayoutPreference.this.lambda$new$0$LayoutPreference(view);
        }
    };
    private View mRootView;

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$LayoutPreference(View view) {
        performClick(view);
    }

    public LayoutPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context, attributeSet, 0);
    }

    private void init(Context context, AttributeSet attributeSet, int i) {
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.Preference);
        int i2 = R$styleable.Preference_allowDividerAbove;
        this.mAllowDividerAbove = TypedArrayUtils.getBoolean(obtainStyledAttributes, i2, i2, false);
        int i3 = R$styleable.Preference_allowDividerBelow;
        this.mAllowDividerBelow = TypedArrayUtils.getBoolean(obtainStyledAttributes, i3, i3, false);
        obtainStyledAttributes.recycle();
        TypedArray obtainStyledAttributes2 = context.obtainStyledAttributes(attributeSet, R$styleable.Preference, i, 0);
        int resourceId = obtainStyledAttributes2.getResourceId(R$styleable.Preference_android_layout, 0);
        if (resourceId != 0) {
            obtainStyledAttributes2.recycle();
            setView(LayoutInflater.from(getContext()).inflate(resourceId, (ViewGroup) null, false));
            return;
        }
        throw new IllegalArgumentException("LayoutPreference requires a layout to be defined");
    }

    private void setView(View view) {
        setLayoutResource(R$layout.layout_preference_frame);
        this.mRootView = view;
        setShouldDisableView(false);
    }

    @Override // androidx.preference.Preference
    public void onBindViewHolder(PreferenceViewHolder preferenceViewHolder) {
        preferenceViewHolder.itemView.setOnClickListener(this.mClickListener);
        boolean isSelectable = isSelectable();
        preferenceViewHolder.itemView.setFocusable(isSelectable);
        preferenceViewHolder.itemView.setClickable(isSelectable);
        preferenceViewHolder.setDividerAllowedAbove(this.mAllowDividerAbove);
        preferenceViewHolder.setDividerAllowedBelow(this.mAllowDividerBelow);
        FrameLayout frameLayout = (FrameLayout) preferenceViewHolder.itemView;
        frameLayout.removeAllViews();
        ViewGroup viewGroup = (ViewGroup) this.mRootView.getParent();
        if (viewGroup != null) {
            viewGroup.removeView(this.mRootView);
        }
        frameLayout.addView(this.mRootView);
    }
}
