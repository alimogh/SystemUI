package com.google.android.material.edgeeffect;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
public class SpringRefreshLayout extends SwipeRefreshLayout {
    private View mChildwithOverScrolling = null;

    public SpringRefreshLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override // androidx.swiperefreshlayout.widget.SwipeRefreshLayout, android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        View view = this.mChildwithOverScrolling;
        if (view != null) {
            view.onTouchEvent(motionEvent);
        }
        return super.onTouchEvent(motionEvent);
    }
}
