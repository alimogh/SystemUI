package com.android.systemui.controls.controller;

import android.util.Log;
import com.android.systemui.controls.controller.ControlsBindingControllerImpl;
import kotlin.jvm.functions.Function0;
/* compiled from: ControlsBindingControllerImpl.kt */
final class ControlsBindingControllerImpl$LoadSubscriber$loadCancel$1 implements Runnable {
    final /* synthetic */ ControlsBindingControllerImpl.LoadSubscriber this$0;

    ControlsBindingControllerImpl$LoadSubscriber$loadCancel$1(ControlsBindingControllerImpl.LoadSubscriber loadSubscriber) {
        this.this$0 = loadSubscriber;
    }

    @Override // java.lang.Runnable
    public final void run() {
        Function0 function0 = this.this$0._loadCancelInternal;
        if (function0 != null) {
            Log.d("ControlsBindingControllerImpl", "Canceling loadSubscribtion");
            function0.invoke();
        }
    }
}
