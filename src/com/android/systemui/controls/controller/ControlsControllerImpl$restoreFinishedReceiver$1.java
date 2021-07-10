package com.android.systemui.controls.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: ControlsControllerImpl.kt */
public final class ControlsControllerImpl$restoreFinishedReceiver$1 extends BroadcastReceiver {
    final /* synthetic */ ControlsControllerImpl this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    ControlsControllerImpl$restoreFinishedReceiver$1(ControlsControllerImpl controlsControllerImpl) {
        this.this$0 = controlsControllerImpl;
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(@NotNull Context context, @NotNull Intent intent) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(intent, "intent");
        if (intent.getIntExtra("android.intent.extra.USER_ID", -10000) == this.this$0.getCurrentUserId()) {
            ControlsControllerImpl.access$getExecutor$p(this.this$0).execute(new Runnable(this) { // from class: com.android.systemui.controls.controller.ControlsControllerImpl$restoreFinishedReceiver$1$onReceive$1
                final /* synthetic */ ControlsControllerImpl$restoreFinishedReceiver$1 this$0;

                {
                    this.this$0 = r1;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    Log.d("ControlsControllerImpl", "Restore finished, storing auxiliary favorites");
                    this.this$0.this$0.getAuxiliaryPersistenceWrapper$packages__apps__OPSystemUI__android_common__OPSystemUI_core().initialize();
                    this.this$0.this$0.persistenceWrapper.storeFavorites(this.this$0.this$0.getAuxiliaryPersistenceWrapper$packages__apps__OPSystemUI__android_common__OPSystemUI_core().getFavorites());
                    ControlsControllerImpl controlsControllerImpl = this.this$0.this$0;
                    controlsControllerImpl.resetFavorites(controlsControllerImpl.getAvailable());
                }
            });
        }
    }
}
