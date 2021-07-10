package com.android.systemui.util;

import android.view.View;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
public class SysuiLifecycle {
    public static LifecycleOwner viewAttachLifecycle(View view) {
        return new ViewLifecycle(view);
    }

    private static class ViewLifecycle implements LifecycleOwner, View.OnAttachStateChangeListener {
        private final LifecycleRegistry mLifecycle = new LifecycleRegistry(this);

        ViewLifecycle(View view) {
            view.addOnAttachStateChangeListener(this);
            if (view.isAttachedToWindow()) {
                this.mLifecycle.markState(Lifecycle.State.RESUMED);
            }
        }

        @Override // androidx.lifecycle.LifecycleOwner
        public Lifecycle getLifecycle() {
            return this.mLifecycle;
        }

        @Override // android.view.View.OnAttachStateChangeListener
        public void onViewAttachedToWindow(View view) {
            this.mLifecycle.markState(Lifecycle.State.RESUMED);
        }

        @Override // android.view.View.OnAttachStateChangeListener
        public void onViewDetachedFromWindow(View view) {
            this.mLifecycle.markState(Lifecycle.State.DESTROYED);
        }
    }
}
