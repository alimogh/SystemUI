package com.android.systemui.controls.management;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ServiceInfo;
import android.os.UserHandle;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settingslib.applications.ServiceListing;
import com.android.systemui.controls.ControlsServiceInfo;
import com.android.systemui.controls.management.ControlsListingController;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import kotlin.collections.CollectionsKt__CollectionsKt;
import kotlin.collections.CollectionsKt__IterablesKt;
import kotlin.collections.SetsKt__SetsKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: ControlsListingControllerImpl.kt */
public final class ControlsListingControllerImpl implements ControlsListingController {
    private Set<ComponentName> availableComponents;
    private List<? extends ServiceInfo> availableServices;
    private final Executor backgroundExecutor;
    private final Set<ControlsListingController.ControlsListingCallback> callbacks;
    private final Context context;
    private int currentUserId;
    private ServiceListing serviceListing;
    private final Function1<Context, ServiceListing> serviceListingBuilder;
    private final ServiceListing.Callback serviceListingCallback;
    private AtomicInteger userChangeInProgress;

    /* JADX DEBUG: Multi-variable search result rejected for r4v0, resolved type: kotlin.jvm.functions.Function1<? super android.content.Context, ? extends com.android.settingslib.applications.ServiceListing> */
    /* JADX WARN: Multi-variable type inference failed */
    @VisibleForTesting
    public ControlsListingControllerImpl(@NotNull Context context, @NotNull Executor executor, @NotNull Function1<? super Context, ? extends ServiceListing> function1) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(executor, "backgroundExecutor");
        Intrinsics.checkParameterIsNotNull(function1, "serviceListingBuilder");
        this.context = context;
        this.backgroundExecutor = executor;
        this.serviceListingBuilder = function1;
        this.serviceListing = (ServiceListing) function1.invoke(context);
        this.callbacks = new LinkedHashSet();
        this.availableComponents = SetsKt__SetsKt.emptySet();
        this.availableServices = CollectionsKt__CollectionsKt.emptyList();
        this.userChangeInProgress = new AtomicInteger(0);
        this.currentUserId = ActivityManager.getCurrentUser();
        this.serviceListingCallback = new ControlsListingControllerImpl$serviceListingCallback$1(this);
        Log.d("ControlsListingControllerImpl", "Initializing");
        this.serviceListing.addCallback(this.serviceListingCallback);
        this.serviceListing.setListening(true);
        this.serviceListing.reload();
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public ControlsListingControllerImpl(@NotNull Context context, @NotNull Executor executor) {
        this(context, executor, AnonymousClass1.INSTANCE);
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(executor, "executor");
    }

    @Override // com.android.systemui.util.UserAwareController
    public int getCurrentUserId() {
        return this.currentUserId;
    }

    @Override // com.android.systemui.util.UserAwareController
    public void changeUser(@NotNull UserHandle userHandle) {
        Intrinsics.checkParameterIsNotNull(userHandle, "newUser");
        this.userChangeInProgress.incrementAndGet();
        this.serviceListing.setListening(false);
        this.backgroundExecutor.execute(new Runnable(this, userHandle) { // from class: com.android.systemui.controls.management.ControlsListingControllerImpl$changeUser$1
            final /* synthetic */ UserHandle $newUser;
            final /* synthetic */ ControlsListingControllerImpl this$0;

            {
                this.this$0 = r1;
                this.$newUser = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                if (ControlsListingControllerImpl.access$getUserChangeInProgress$p(this.this$0).decrementAndGet() == 0) {
                    ControlsListingControllerImpl.access$setCurrentUserId$p(this.this$0, this.$newUser.getIdentifier());
                    Context createContextAsUser = ControlsListingControllerImpl.access$getContext$p(this.this$0).createContextAsUser(this.$newUser, 0);
                    ControlsListingControllerImpl controlsListingControllerImpl = this.this$0;
                    Function1 access$getServiceListingBuilder$p = ControlsListingControllerImpl.access$getServiceListingBuilder$p(controlsListingControllerImpl);
                    Intrinsics.checkExpressionValueIsNotNull(createContextAsUser, "contextForUser");
                    ControlsListingControllerImpl.access$setServiceListing$p(controlsListingControllerImpl, (ServiceListing) access$getServiceListingBuilder$p.invoke(createContextAsUser));
                    ControlsListingControllerImpl.access$getServiceListing$p(this.this$0).addCallback(ControlsListingControllerImpl.access$getServiceListingCallback$p(this.this$0));
                    ControlsListingControllerImpl.access$getServiceListing$p(this.this$0).setListening(true);
                    ControlsListingControllerImpl.access$getServiceListing$p(this.this$0).reload();
                }
            }
        });
    }

    public void addCallback(@NotNull ControlsListingController.ControlsListingCallback controlsListingCallback) {
        Intrinsics.checkParameterIsNotNull(controlsListingCallback, "listener");
        this.backgroundExecutor.execute(new Runnable(this, controlsListingCallback) { // from class: com.android.systemui.controls.management.ControlsListingControllerImpl$addCallback$1
            final /* synthetic */ ControlsListingController.ControlsListingCallback $listener;
            final /* synthetic */ ControlsListingControllerImpl this$0;

            {
                this.this$0 = r1;
                this.$listener = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                if (ControlsListingControllerImpl.access$getUserChangeInProgress$p(this.this$0).get() > 0) {
                    this.this$0.addCallback(this.$listener);
                    return;
                }
                List<ControlsServiceInfo> currentServices = this.this$0.getCurrentServices();
                Log.d("ControlsListingControllerImpl", "Subscribing callback, service count: " + currentServices.size());
                ControlsListingControllerImpl.access$getCallbacks$p(this.this$0).add(this.$listener);
                this.$listener.onServicesUpdated(currentServices);
            }
        });
    }

    public void removeCallback(@NotNull ControlsListingController.ControlsListingCallback controlsListingCallback) {
        Intrinsics.checkParameterIsNotNull(controlsListingCallback, "listener");
        this.backgroundExecutor.execute(new Runnable(this, controlsListingCallback) { // from class: com.android.systemui.controls.management.ControlsListingControllerImpl$removeCallback$1
            final /* synthetic */ ControlsListingController.ControlsListingCallback $listener;
            final /* synthetic */ ControlsListingControllerImpl this$0;

            {
                this.this$0 = r1;
                this.$listener = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                Log.d("ControlsListingControllerImpl", "Unsubscribing callback");
                ControlsListingControllerImpl.access$getCallbacks$p(this.this$0).remove(this.$listener);
            }
        });
    }

    @NotNull
    public List<ControlsServiceInfo> getCurrentServices() {
        List<? extends ServiceInfo> list = this.availableServices;
        ArrayList arrayList = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(list, 10));
        for (ServiceInfo serviceInfo : list) {
            arrayList.add(new ControlsServiceInfo(this.context, serviceInfo));
        }
        return arrayList;
    }

    @Override // com.android.systemui.controls.management.ControlsListingController
    @Nullable
    public CharSequence getAppLabel(@NotNull ComponentName componentName) {
        Object obj;
        Intrinsics.checkParameterIsNotNull(componentName, "name");
        Iterator<T> it = getCurrentServices().iterator();
        while (true) {
            if (!it.hasNext()) {
                obj = null;
                break;
            }
            obj = it.next();
            if (Intrinsics.areEqual(((ControlsServiceInfo) obj).componentName, componentName)) {
                break;
            }
        }
        ControlsServiceInfo controlsServiceInfo = (ControlsServiceInfo) obj;
        if (controlsServiceInfo != null) {
            return controlsServiceInfo.loadLabel();
        }
        return null;
    }
}
