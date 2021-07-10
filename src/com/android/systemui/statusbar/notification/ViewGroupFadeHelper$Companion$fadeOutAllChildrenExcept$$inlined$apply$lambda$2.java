package com.android.systemui.statusbar.notification;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.ViewGroup;
import java.util.Set;
import org.jetbrains.annotations.Nullable;
/* compiled from: ViewGroupFadeHelper.kt */
public final class ViewGroupFadeHelper$Companion$fadeOutAllChildrenExcept$$inlined$apply$lambda$2 extends AnimatorListenerAdapter {
    final /* synthetic */ Runnable $endRunnable$inlined;

    ViewGroupFadeHelper$Companion$fadeOutAllChildrenExcept$$inlined$apply$lambda$2(long j, ViewGroup viewGroup, Set set, Runnable runnable) {
        this.$endRunnable$inlined = runnable;
    }

    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
    public void onAnimationEnd(@Nullable Animator animator) {
        Runnable runnable = this.$endRunnable$inlined;
        if (runnable != null) {
            runnable.run();
        }
    }
}
