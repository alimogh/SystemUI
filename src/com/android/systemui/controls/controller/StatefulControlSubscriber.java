package com.android.systemui.controls.controller;

import android.os.IBinder;
import android.service.controls.Control;
import android.service.controls.IControlsSubscriber;
import android.service.controls.IControlsSubscription;
import android.util.Log;
import com.android.systemui.util.concurrency.DelayableExecutor;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: StatefulControlSubscriber.kt */
public final class StatefulControlSubscriber extends IControlsSubscriber.Stub {
    private final DelayableExecutor bgExecutor;
    private final ControlsController controller;
    private final ControlsProviderLifecycleManager provider;
    private final long requestLimit;
    private IControlsSubscription subscription;
    private boolean subscriptionOpen;

    public StatefulControlSubscriber(@NotNull ControlsController controlsController, @NotNull ControlsProviderLifecycleManager controlsProviderLifecycleManager, @NotNull DelayableExecutor delayableExecutor, long j) {
        Intrinsics.checkParameterIsNotNull(controlsController, "controller");
        Intrinsics.checkParameterIsNotNull(controlsProviderLifecycleManager, "provider");
        Intrinsics.checkParameterIsNotNull(delayableExecutor, "bgExecutor");
        this.controller = controlsController;
        this.provider = controlsProviderLifecycleManager;
        this.bgExecutor = delayableExecutor;
        this.requestLimit = j;
    }

    private final void run(IBinder iBinder, Function0<Unit> function0) {
        if (Intrinsics.areEqual(this.provider.getToken(), iBinder)) {
            this.bgExecutor.execute(new Runnable(function0) { // from class: com.android.systemui.controls.controller.StatefulControlSubscriber$run$1
                final /* synthetic */ Function0 $f;

                {
                    this.$f = r1;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    this.$f.invoke();
                }
            });
        }
    }

    public void onSubscribe(@NotNull IBinder iBinder, @NotNull IControlsSubscription iControlsSubscription) {
        Intrinsics.checkParameterIsNotNull(iBinder, "token");
        Intrinsics.checkParameterIsNotNull(iControlsSubscription, "subs");
        run(iBinder, new Function0<Unit>(this, iControlsSubscription) { // from class: com.android.systemui.controls.controller.StatefulControlSubscriber$onSubscribe$1
            final /* synthetic */ IControlsSubscription $subs;
            final /* synthetic */ StatefulControlSubscriber this$0;

            {
                this.this$0 = r1;
                this.$subs = r2;
            }

            @Override // kotlin.jvm.functions.Function0
            public final void invoke() {
                this.this$0.subscriptionOpen = true;
                this.this$0.subscription = this.$subs;
                this.this$0.provider.startSubscription(this.$subs, this.this$0.requestLimit);
            }
        });
    }

    public void onNext(@NotNull IBinder iBinder, @NotNull Control control) {
        Intrinsics.checkParameterIsNotNull(iBinder, "token");
        Intrinsics.checkParameterIsNotNull(control, "control");
        run(iBinder, new Function0<Unit>(this, iBinder, control) { // from class: com.android.systemui.controls.controller.StatefulControlSubscriber$onNext$1
            final /* synthetic */ Control $control;
            final /* synthetic */ IBinder $token;
            final /* synthetic */ StatefulControlSubscriber this$0;

            {
                this.this$0 = r1;
                this.$token = r2;
                this.$control = r3;
            }

            @Override // kotlin.jvm.functions.Function0
            public final void invoke() {
                if (!(this.this$0.subscriptionOpen)) {
                    Log.w("StatefulControlSubscriber", "Refresh outside of window for token:" + this.$token);
                    return;
                }
                this.this$0.controller.refreshStatus(this.this$0.provider.getComponentName(), this.$control);
            }
        });
    }

    public void onError(@NotNull IBinder iBinder, @NotNull String str) {
        Intrinsics.checkParameterIsNotNull(iBinder, "token");
        Intrinsics.checkParameterIsNotNull(str, "error");
        run(iBinder, new Function0<Unit>(this, str) { // from class: com.android.systemui.controls.controller.StatefulControlSubscriber$onError$1
            final /* synthetic */ String $error;
            final /* synthetic */ StatefulControlSubscriber this$0;

            {
                this.this$0 = r1;
                this.$error = r2;
            }

            @Override // kotlin.jvm.functions.Function0
            public final void invoke() {
                if (this.this$0.subscriptionOpen) {
                    this.this$0.subscriptionOpen = false;
                    Log.e("StatefulControlSubscriber", "onError receive from '" + this.this$0.provider.getComponentName() + "': " + this.$error);
                }
            }
        });
    }

    public void onComplete(@NotNull IBinder iBinder) {
        Intrinsics.checkParameterIsNotNull(iBinder, "token");
        run(iBinder, new Function0<Unit>(this) { // from class: com.android.systemui.controls.controller.StatefulControlSubscriber$onComplete$1
            final /* synthetic */ StatefulControlSubscriber this$0;

            {
                this.this$0 = r1;
            }

            @Override // kotlin.jvm.functions.Function0
            public final void invoke() {
                if (this.this$0.subscriptionOpen) {
                    this.this$0.subscriptionOpen = false;
                    Log.i("StatefulControlSubscriber", "onComplete receive from '" + this.this$0.provider.getComponentName() + '\'');
                }
            }
        });
    }

    public final void cancel() {
        if (this.subscriptionOpen) {
            this.bgExecutor.execute(new Runnable(this) { // from class: com.android.systemui.controls.controller.StatefulControlSubscriber$cancel$1
                final /* synthetic */ StatefulControlSubscriber this$0;

                {
                    this.this$0 = r1;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    if (this.this$0.subscriptionOpen) {
                        this.this$0.subscriptionOpen = false;
                        IControlsSubscription iControlsSubscription = this.this$0.subscription;
                        if (iControlsSubscription != null) {
                            this.this$0.provider.cancelSubscription(iControlsSubscription);
                        }
                        this.this$0.subscription = null;
                    }
                }
            });
        }
    }
}
