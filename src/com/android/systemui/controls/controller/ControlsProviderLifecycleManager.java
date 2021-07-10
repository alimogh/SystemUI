package com.android.systemui.controls.controller;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.UserHandle;
import android.service.controls.IControlsActionCallback;
import android.service.controls.IControlsSubscriber;
import android.service.controls.IControlsSubscription;
import android.service.controls.actions.ControlAction;
import android.util.ArraySet;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import com.android.systemui.util.concurrency.DelayableExecutor;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: ControlsProviderLifecycleManager.kt */
public final class ControlsProviderLifecycleManager implements IBinder.DeathRecipient {
    private static final int BIND_FLAGS = 67108865;
    private final String TAG = ControlsProviderLifecycleManager.class.getSimpleName();
    private final IControlsActionCallback.Stub actionCallbackService;
    private int bindTryCount;
    @NotNull
    private final ComponentName componentName;
    private final Context context;
    private final DelayableExecutor executor;
    private final Intent intent;
    private Runnable onLoadCanceller;
    @GuardedBy({"queuedServiceMethods"})
    private final Set<ServiceMethod> queuedServiceMethods = new ArraySet();
    private boolean requiresBound;
    private final ControlsProviderLifecycleManager$serviceConnection$1 serviceConnection;
    @NotNull
    private final IBinder token = new Binder();
    @NotNull
    private final UserHandle user;
    private ServiceWrapper wrapper;

    public ControlsProviderLifecycleManager(@NotNull Context context, @NotNull DelayableExecutor delayableExecutor, @NotNull IControlsActionCallback.Stub stub, @NotNull UserHandle userHandle, @NotNull ComponentName componentName) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(delayableExecutor, "executor");
        Intrinsics.checkParameterIsNotNull(stub, "actionCallbackService");
        Intrinsics.checkParameterIsNotNull(userHandle, "user");
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        this.context = context;
        this.executor = delayableExecutor;
        this.actionCallbackService = stub;
        this.user = userHandle;
        this.componentName = componentName;
        Intent intent = new Intent();
        intent.setComponent(this.componentName);
        Bundle bundle = new Bundle();
        bundle.putBinder("CALLBACK_TOKEN", this.token);
        intent.putExtra("CALLBACK_BUNDLE", bundle);
        this.intent = intent;
        this.serviceConnection = new ControlsProviderLifecycleManager$serviceConnection$1(this);
    }

    @NotNull
    public final UserHandle getUser() {
        return this.user;
    }

    @NotNull
    public final ComponentName getComponentName() {
        return this.componentName;
    }

    @NotNull
    public final IBinder getToken() {
        return this.token;
    }

    /* access modifiers changed from: private */
    public final void bindService(boolean z) {
        this.executor.execute(new Runnable(this, z) { // from class: com.android.systemui.controls.controller.ControlsProviderLifecycleManager$bindService$1
            final /* synthetic */ boolean $bind;
            final /* synthetic */ ControlsProviderLifecycleManager this$0;

            {
                this.this$0 = r1;
                this.$bind = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                this.this$0.requiresBound = this.$bind;
                if (!this.$bind) {
                    String str = this.this$0.TAG;
                    Log.d(str, "Unbinding service " + this.this$0.intent);
                    this.this$0.bindTryCount = 0;
                    if (this.this$0.wrapper != null) {
                        this.this$0.context.unbindService(this.this$0.serviceConnection);
                    }
                    this.this$0.wrapper = null;
                } else if (this.this$0.bindTryCount != 5) {
                    String str2 = this.this$0.TAG;
                    Log.d(str2, "Binding service " + this.this$0.intent);
                    ControlsProviderLifecycleManager controlsProviderLifecycleManager = this.this$0;
                    controlsProviderLifecycleManager.bindTryCount = controlsProviderLifecycleManager.bindTryCount + 1;
                    try {
                        this.this$0.context.bindServiceAsUser(this.this$0.intent, this.this$0.serviceConnection, ControlsProviderLifecycleManager.BIND_FLAGS, this.this$0.getUser());
                    } catch (SecurityException e) {
                        Log.e(this.this$0.TAG, "Failed to bind to service", e);
                    }
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public final void handlePendingServiceMethods() {
        ArraySet<ServiceMethod> arraySet;
        synchronized (this.queuedServiceMethods) {
            arraySet = new ArraySet(this.queuedServiceMethods);
            this.queuedServiceMethods.clear();
        }
        for (ServiceMethod serviceMethod : arraySet) {
            serviceMethod.run();
        }
    }

    @Override // android.os.IBinder.DeathRecipient
    public void binderDied() {
        if (this.wrapper != null) {
            this.wrapper = null;
            if (this.requiresBound) {
                Log.d(this.TAG, "binderDied");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void queueServiceMethod(ServiceMethod serviceMethod) {
        synchronized (this.queuedServiceMethods) {
            this.queuedServiceMethods.add(serviceMethod);
        }
    }

    private final void invokeOrQueue(ServiceMethod serviceMethod) {
        if (this.wrapper != null) {
            serviceMethod.run();
            return;
        }
        queueServiceMethod(serviceMethod);
        bindService(true);
    }

    public final void maybeBindAndLoad(@NotNull IControlsSubscriber.Stub stub) {
        Intrinsics.checkParameterIsNotNull(stub, "subscriber");
        this.onLoadCanceller = this.executor.executeDelayed(new Runnable(this, stub) { // from class: com.android.systemui.controls.controller.ControlsProviderLifecycleManager$maybeBindAndLoad$1
            final /* synthetic */ IControlsSubscriber.Stub $subscriber;
            final /* synthetic */ ControlsProviderLifecycleManager this$0;

            {
                this.this$0 = r1;
                this.$subscriber = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                String str = this.this$0.TAG;
                Log.d(str, "Timeout waiting onLoad for " + this.this$0.getComponentName());
                this.$subscriber.onError(this.this$0.getToken(), "Timeout waiting onLoad");
                this.this$0.unbindService();
            }
        }, 20, TimeUnit.SECONDS);
        invokeOrQueue(new Load(this, stub));
    }

    public final void maybeBindAndLoadSuggested(@NotNull IControlsSubscriber.Stub stub) {
        Intrinsics.checkParameterIsNotNull(stub, "subscriber");
        this.onLoadCanceller = this.executor.executeDelayed(new Runnable(this, stub) { // from class: com.android.systemui.controls.controller.ControlsProviderLifecycleManager$maybeBindAndLoadSuggested$1
            final /* synthetic */ IControlsSubscriber.Stub $subscriber;
            final /* synthetic */ ControlsProviderLifecycleManager this$0;

            {
                this.this$0 = r1;
                this.$subscriber = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                String str = this.this$0.TAG;
                Log.d(str, "Timeout waiting onLoadSuggested for " + this.this$0.getComponentName());
                this.$subscriber.onError(this.this$0.getToken(), "Timeout waiting onLoadSuggested");
                this.this$0.unbindService();
            }
        }, 20, TimeUnit.SECONDS);
        invokeOrQueue(new Suggest(this, stub));
    }

    public final void cancelLoadTimeout() {
        Runnable runnable = this.onLoadCanceller;
        if (runnable != null) {
            runnable.run();
        }
        this.onLoadCanceller = null;
    }

    public final void maybeBindAndSubscribe(@NotNull List<String> list, @NotNull IControlsSubscriber iControlsSubscriber) {
        Intrinsics.checkParameterIsNotNull(list, "controlIds");
        Intrinsics.checkParameterIsNotNull(iControlsSubscriber, "subscriber");
        invokeOrQueue(new Subscribe(this, list, iControlsSubscriber));
    }

    public final void maybeBindAndSendAction(@NotNull String str, @NotNull ControlAction controlAction) {
        Intrinsics.checkParameterIsNotNull(str, "controlId");
        Intrinsics.checkParameterIsNotNull(controlAction, "action");
        invokeOrQueue(new Action(this, str, controlAction));
    }

    public final void startSubscription(@NotNull IControlsSubscription iControlsSubscription, long j) {
        Intrinsics.checkParameterIsNotNull(iControlsSubscription, "subscription");
        String str = this.TAG;
        Log.d(str, "startSubscription: " + iControlsSubscription);
        ServiceWrapper serviceWrapper = this.wrapper;
        if (serviceWrapper != null) {
            serviceWrapper.request(iControlsSubscription, j);
        }
    }

    public final void cancelSubscription(@NotNull IControlsSubscription iControlsSubscription) {
        Intrinsics.checkParameterIsNotNull(iControlsSubscription, "subscription");
        String str = this.TAG;
        Log.d(str, "cancelSubscription: " + iControlsSubscription);
        ServiceWrapper serviceWrapper = this.wrapper;
        if (serviceWrapper != null) {
            serviceWrapper.cancel(iControlsSubscription);
        }
    }

    public final void unbindService() {
        Runnable runnable = this.onLoadCanceller;
        if (runnable != null) {
            runnable.run();
        }
        this.onLoadCanceller = null;
        bindService(false);
    }

    @Override // java.lang.Object
    @NotNull
    public String toString() {
        StringBuilder sb = new StringBuilder("ControlsProviderLifecycleManager(");
        sb.append("component=" + this.componentName);
        sb.append(", user=" + this.user);
        sb.append(")");
        String sb2 = sb.toString();
        Intrinsics.checkExpressionValueIsNotNull(sb2, "StringBuilder(\"ControlsP…\")\")\n        }.toString()");
        return sb2;
    }

    /* compiled from: ControlsProviderLifecycleManager.kt */
    public abstract class ServiceMethod {
        public abstract boolean callWrapper$packages__apps__OPSystemUI__android_common__OPSystemUI_core();

        /* JADX WARN: Incorrect args count in method signature: ()V */
        public ServiceMethod() {
        }

        public final void run() {
            if (!callWrapper$packages__apps__OPSystemUI__android_common__OPSystemUI_core()) {
                ControlsProviderLifecycleManager.this.queueServiceMethod(this);
                ControlsProviderLifecycleManager.this.binderDied();
            }
        }
    }

    /* compiled from: ControlsProviderLifecycleManager.kt */
    public final class Load extends ServiceMethod {
        @NotNull
        private final IControlsSubscriber.Stub subscriber;
        final /* synthetic */ ControlsProviderLifecycleManager this$0;

        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        public Load(@NotNull ControlsProviderLifecycleManager controlsProviderLifecycleManager, IControlsSubscriber.Stub stub) {
            super();
            Intrinsics.checkParameterIsNotNull(stub, "subscriber");
            this.this$0 = controlsProviderLifecycleManager;
            this.subscriber = stub;
        }

        @Override // com.android.systemui.controls.controller.ControlsProviderLifecycleManager.ServiceMethod
        public boolean callWrapper$packages__apps__OPSystemUI__android_common__OPSystemUI_core() {
            String str = this.this$0.TAG;
            Log.d(str, "load " + this.this$0.getComponentName());
            ServiceWrapper serviceWrapper = this.this$0.wrapper;
            if (serviceWrapper != null) {
                return serviceWrapper.load(this.subscriber);
            }
            return false;
        }
    }

    /* compiled from: ControlsProviderLifecycleManager.kt */
    public final class Suggest extends ServiceMethod {
        @NotNull
        private final IControlsSubscriber.Stub subscriber;
        final /* synthetic */ ControlsProviderLifecycleManager this$0;

        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        public Suggest(@NotNull ControlsProviderLifecycleManager controlsProviderLifecycleManager, IControlsSubscriber.Stub stub) {
            super();
            Intrinsics.checkParameterIsNotNull(stub, "subscriber");
            this.this$0 = controlsProviderLifecycleManager;
            this.subscriber = stub;
        }

        @Override // com.android.systemui.controls.controller.ControlsProviderLifecycleManager.ServiceMethod
        public boolean callWrapper$packages__apps__OPSystemUI__android_common__OPSystemUI_core() {
            String str = this.this$0.TAG;
            Log.d(str, "suggest " + this.this$0.getComponentName());
            ServiceWrapper serviceWrapper = this.this$0.wrapper;
            if (serviceWrapper != null) {
                return serviceWrapper.loadSuggested(this.subscriber);
            }
            return false;
        }
    }

    /* compiled from: ControlsProviderLifecycleManager.kt */
    public final class Subscribe extends ServiceMethod {
        @NotNull
        private final List<String> list;
        @NotNull
        private final IControlsSubscriber subscriber;
        final /* synthetic */ ControlsProviderLifecycleManager this$0;

        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        public Subscribe(@NotNull ControlsProviderLifecycleManager controlsProviderLifecycleManager, @NotNull List<String> list, IControlsSubscriber iControlsSubscriber) {
            super();
            Intrinsics.checkParameterIsNotNull(list, "list");
            Intrinsics.checkParameterIsNotNull(iControlsSubscriber, "subscriber");
            this.this$0 = controlsProviderLifecycleManager;
            this.list = list;
            this.subscriber = iControlsSubscriber;
        }

        @Override // com.android.systemui.controls.controller.ControlsProviderLifecycleManager.ServiceMethod
        public boolean callWrapper$packages__apps__OPSystemUI__android_common__OPSystemUI_core() {
            String str = this.this$0.TAG;
            Log.d(str, "subscribe " + this.this$0.getComponentName() + " - " + this.list);
            ServiceWrapper serviceWrapper = this.this$0.wrapper;
            if (serviceWrapper != null) {
                return serviceWrapper.subscribe(this.list, this.subscriber);
            }
            return false;
        }
    }

    /* compiled from: ControlsProviderLifecycleManager.kt */
    public final class Action extends ServiceMethod {
        @NotNull
        private final ControlAction action;
        @NotNull
        private final String id;
        final /* synthetic */ ControlsProviderLifecycleManager this$0;

        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        public Action(@NotNull ControlsProviderLifecycleManager controlsProviderLifecycleManager, @NotNull String str, ControlAction controlAction) {
            super();
            Intrinsics.checkParameterIsNotNull(str, "id");
            Intrinsics.checkParameterIsNotNull(controlAction, "action");
            this.this$0 = controlsProviderLifecycleManager;
            this.id = str;
            this.action = controlAction;
        }

        @Override // com.android.systemui.controls.controller.ControlsProviderLifecycleManager.ServiceMethod
        public boolean callWrapper$packages__apps__OPSystemUI__android_common__OPSystemUI_core() {
            String str = this.this$0.TAG;
            Log.d(str, "onAction " + this.this$0.getComponentName() + " - " + this.id);
            ServiceWrapper serviceWrapper = this.this$0.wrapper;
            if (serviceWrapper != null) {
                return serviceWrapper.action(this.id, this.action, this.this$0.actionCallbackService);
            }
            return false;
        }
    }
}
