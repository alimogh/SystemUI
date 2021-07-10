package com.google.android.material.appbar;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.OverScroller;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.appbar.AppBarLayout;
import java.lang.reflect.Field;
public class AppBarLayoutBehavior extends AppBarLayout.Behavior {
    private boolean shouldBlockNestedScroll;

    public AppBarLayoutBehavior(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    private void stopAppbarLayoutFling(AppBarLayout appBarLayout) {
        try {
            Class<? super Object> superclass = getClass().getSuperclass().getSuperclass();
            Field declaredField = superclass.getDeclaredField("mFlingRunnable");
            Field declaredField2 = superclass.getDeclaredField("scroller");
            declaredField.setAccessible(true);
            declaredField2.setAccessible(true);
            Runnable runnable = (Runnable) declaredField.get(this);
            OverScroller overScroller = (OverScroller) declaredField2.get(this);
            if (runnable != null) {
                Log.d("chenhb", "存在flingRunnable");
                appBarLayout.removeCallbacks(runnable);
                declaredField.set(this, null);
            }
            if (overScroller != null && !overScroller.isFinished()) {
                overScroller.abortAnimation();
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
        }
    }

    @Override // com.google.android.material.appbar.AppBarLayout.BaseBehavior
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, AppBarLayout appBarLayout, View view, View view2, int i, int i2) {
        stopAppbarLayoutFling(appBarLayout);
        return super.onStartNestedScroll(coordinatorLayout, (CoordinatorLayout) appBarLayout, view, view2, i, i2);
    }

    @Override // com.google.android.material.appbar.AppBarLayout.BaseBehavior
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, AppBarLayout appBarLayout, View view, int i, int i2, int[] iArr, int i3) {
        if (this.shouldBlockNestedScroll) {
            return;
        }
        if (i2 < -30) {
            super.onNestedPreScroll(coordinatorLayout, (CoordinatorLayout) appBarLayout, view, i, -30, iArr, i3);
        } else {
            super.onNestedPreScroll(coordinatorLayout, (CoordinatorLayout) appBarLayout, view, i, i2, iArr, i3);
        }
    }

    public void onNestedScroll(CoordinatorLayout coordinatorLayout, AppBarLayout appBarLayout, View view, int i, int i2, int i3, int i4, int i5) {
        if (!this.shouldBlockNestedScroll) {
            super.onNestedScroll(coordinatorLayout, (CoordinatorLayout) appBarLayout, view, i, i2, i3, i4, i5);
        }
    }

    public boolean onInterceptTouchEvent(CoordinatorLayout coordinatorLayout, AppBarLayout appBarLayout, MotionEvent motionEvent) {
        Object superSuperField;
        if (motionEvent.getAction() == 0 && (superSuperField = getSuperSuperField(this, "scroller")) != null && (superSuperField instanceof OverScroller)) {
            ((OverScroller) superSuperField).abortAnimation();
        }
        return super.onInterceptTouchEvent(coordinatorLayout, (CoordinatorLayout) appBarLayout, motionEvent);
    }

    private Object getSuperSuperField(Object obj, String str) {
        try {
            Field declaredField = obj.getClass().getSuperclass().getSuperclass().getSuperclass().getDeclaredField(str);
            declaredField.setAccessible(true);
            return declaredField.get(obj);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override // com.google.android.material.appbar.AppBarLayout.BaseBehavior
    public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, AppBarLayout appBarLayout, View view, int i) {
        super.onStopNestedScroll(coordinatorLayout, (CoordinatorLayout) appBarLayout, view, i);
        this.shouldBlockNestedScroll = false;
    }
}
