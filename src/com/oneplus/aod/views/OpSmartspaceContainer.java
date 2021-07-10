package com.oneplus.aod.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import java.util.ArrayList;
public class OpSmartspaceContainer extends FrameLayout {
    private ArrayList<View.OnAttachStateChangeListener> mCacheListeners;

    public OpSmartspaceContainer(Context context) {
        this(context, null);
    }

    public OpSmartspaceContainer(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public OpSmartspaceContainer(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mCacheListeners = new ArrayList<>();
    }

    @Override // android.view.View
    public void addOnAttachStateChangeListener(View.OnAttachStateChangeListener onAttachStateChangeListener) {
        this.mCacheListeners.add(onAttachStateChangeListener);
        super.addOnAttachStateChangeListener(onAttachStateChangeListener);
    }

    @Override // android.view.View
    public void removeOnAttachStateChangeListener(View.OnAttachStateChangeListener onAttachStateChangeListener) {
        this.mCacheListeners.remove(onAttachStateChangeListener);
        super.removeOnAttachStateChangeListener(onAttachStateChangeListener);
    }
}
