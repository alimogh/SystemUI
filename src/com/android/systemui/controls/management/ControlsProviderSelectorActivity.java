package com.android.systemui.controls.management;

import android.app.ActivityOptions;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0011R$layout;
import com.android.systemui.C0015R$string;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.controls.controller.ControlsController;
import com.android.systemui.globalactions.GlobalActionsComponent;
import com.android.systemui.util.LifecycleActivity;
import java.util.concurrent.Executor;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KDeclarationContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: ControlsProviderSelectorActivity.kt */
public final class ControlsProviderSelectorActivity extends LifecycleActivity {
    private final Executor backExecutor;
    private final ControlsController controlsController;
    private final ControlsProviderSelectorActivity$currentUserTracker$1 currentUserTracker;
    private final Executor executor;
    private final GlobalActionsComponent globalActionsComponent;
    private final ControlsListingController listingController;
    private RecyclerView recyclerView;

    public static final /* synthetic */ RecyclerView access$getRecyclerView$p(ControlsProviderSelectorActivity controlsProviderSelectorActivity) {
        RecyclerView recyclerView = controlsProviderSelectorActivity.recyclerView;
        if (recyclerView != null) {
            return recyclerView;
        }
        Intrinsics.throwUninitializedPropertyAccessException("recyclerView");
        throw null;
    }

    public ControlsProviderSelectorActivity(@NotNull Executor executor, @NotNull Executor executor2, @NotNull ControlsListingController controlsListingController, @NotNull ControlsController controlsController, @NotNull GlobalActionsComponent globalActionsComponent, @NotNull BroadcastDispatcher broadcastDispatcher) {
        Intrinsics.checkParameterIsNotNull(executor, "executor");
        Intrinsics.checkParameterIsNotNull(executor2, "backExecutor");
        Intrinsics.checkParameterIsNotNull(controlsListingController, "listingController");
        Intrinsics.checkParameterIsNotNull(controlsController, "controlsController");
        Intrinsics.checkParameterIsNotNull(globalActionsComponent, "globalActionsComponent");
        Intrinsics.checkParameterIsNotNull(broadcastDispatcher, "broadcastDispatcher");
        this.executor = executor;
        this.backExecutor = executor2;
        this.listingController = controlsListingController;
        this.controlsController = controlsController;
        this.globalActionsComponent = globalActionsComponent;
        this.currentUserTracker = new ControlsProviderSelectorActivity$currentUserTracker$1(this, broadcastDispatcher, broadcastDispatcher);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.util.LifecycleActivity, android.app.Activity
    public void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        setContentView(C0011R$layout.controls_management);
        Lifecycle lifecycle = getLifecycle();
        ControlsAnimations controlsAnimations = ControlsAnimations.INSTANCE;
        View requireViewById = requireViewById(C0008R$id.controls_management_root);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById, "requireViewById<ViewGrou…controls_management_root)");
        Window window = getWindow();
        Intrinsics.checkExpressionValueIsNotNull(window, "window");
        Intent intent = getIntent();
        Intrinsics.checkExpressionValueIsNotNull(intent, "intent");
        lifecycle.addObserver(controlsAnimations.observerForAnimations((ViewGroup) requireViewById, window, intent));
        ViewStub viewStub = (ViewStub) requireViewById(C0008R$id.stub);
        viewStub.setLayoutResource(C0011R$layout.controls_management_apps);
        viewStub.inflate();
        View requireViewById2 = requireViewById(C0008R$id.list);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById2, "requireViewById(R.id.list)");
        RecyclerView recyclerView = (RecyclerView) requireViewById2;
        this.recyclerView = recyclerView;
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            TextView textView = (TextView) requireViewById(C0008R$id.title);
            textView.setText(textView.getResources().getText(C0015R$string.controls_providers_title));
            Button button = (Button) requireViewById(C0008R$id.other_apps);
            button.setVisibility(0);
            button.setText(17039360);
            button.setOnClickListener(new View.OnClickListener(this) { // from class: com.android.systemui.controls.management.ControlsProviderSelectorActivity$onCreate$$inlined$apply$lambda$1
                final /* synthetic */ ControlsProviderSelectorActivity this$0;

                {
                    this.this$0 = r1;
                }

                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    this.this$0.onBackPressed();
                }
            });
            View requireViewById3 = requireViewById(C0008R$id.done);
            Intrinsics.checkExpressionValueIsNotNull(requireViewById3, "requireViewById<View>(R.id.done)");
            requireViewById3.setVisibility(8);
            return;
        }
        Intrinsics.throwUninitializedPropertyAccessException("recyclerView");
        throw null;
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        this.globalActionsComponent.handleShowGlobalActionsMenu();
        animateExitAndFinish();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.util.LifecycleActivity, android.app.Activity
    public void onStart() {
        super.onStart();
        this.currentUserTracker.startTracking();
        RecyclerView recyclerView = this.recyclerView;
        if (recyclerView != null) {
            recyclerView.setAlpha(0.0f);
            RecyclerView recyclerView2 = this.recyclerView;
            if (recyclerView2 != null) {
                Executor executor = this.backExecutor;
                Executor executor2 = this.executor;
                Lifecycle lifecycle = getLifecycle();
                ControlsListingController controlsListingController = this.listingController;
                LayoutInflater from = LayoutInflater.from(this);
                Intrinsics.checkExpressionValueIsNotNull(from, "LayoutInflater.from(this)");
                ControlsProviderSelectorActivity$onStart$1 controlsProviderSelectorActivity$onStart$1 = new Function1<ComponentName, Unit>(this) { // from class: com.android.systemui.controls.management.ControlsProviderSelectorActivity$onStart$1
                    @Override // kotlin.jvm.internal.CallableReference
                    public final String getName() {
                        return "launchFavoritingActivity";
                    }

                    @Override // kotlin.jvm.internal.CallableReference
                    public final KDeclarationContainer getOwner() {
                        return Reflection.getOrCreateKotlinClass(ControlsProviderSelectorActivity.class);
                    }

                    @Override // kotlin.jvm.internal.CallableReference
                    public final String getSignature() {
                        return "launchFavoritingActivity(Landroid/content/ComponentName;)V";
                    }

                    /* Return type fixed from 'java.lang.Object' to match base method */
                    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
                    @Override // kotlin.jvm.functions.Function1
                    public /* bridge */ /* synthetic */ Unit invoke(ComponentName componentName) {
                        invoke(componentName);
                        return Unit.INSTANCE;
                    }

                    public final void invoke(@Nullable ComponentName componentName) {
                        ((ControlsProviderSelectorActivity) this.receiver).launchFavoritingActivity(componentName);
                    }
                };
                Resources resources = getResources();
                Intrinsics.checkExpressionValueIsNotNull(resources, "resources");
                FavoritesRenderer favoritesRenderer = new FavoritesRenderer(resources, new Function1<ComponentName, Integer>(this.controlsController) { // from class: com.android.systemui.controls.management.ControlsProviderSelectorActivity$onStart$2
                    @Override // kotlin.jvm.internal.CallableReference
                    public final String getName() {
                        return "countFavoritesForComponent";
                    }

                    @Override // kotlin.jvm.internal.CallableReference
                    public final KDeclarationContainer getOwner() {
                        return Reflection.getOrCreateKotlinClass(ControlsController.class);
                    }

                    @Override // kotlin.jvm.internal.CallableReference
                    public final String getSignature() {
                        return "countFavoritesForComponent(Landroid/content/ComponentName;)I";
                    }

                    /* Return type fixed from 'java.lang.Object' to match base method */
                    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
                    @Override // kotlin.jvm.functions.Function1
                    public /* bridge */ /* synthetic */ Integer invoke(ComponentName componentName) {
                        return Integer.valueOf(invoke(componentName));
                    }

                    public final int invoke(@NotNull ComponentName componentName) {
                        Intrinsics.checkParameterIsNotNull(componentName, "p1");
                        return ((ControlsController) this.receiver).countFavoritesForComponent(componentName);
                    }
                });
                Resources resources2 = getResources();
                Intrinsics.checkExpressionValueIsNotNull(resources2, "resources");
                AppAdapter appAdapter = new AppAdapter(executor, executor2, lifecycle, controlsListingController, from, controlsProviderSelectorActivity$onStart$1, favoritesRenderer, resources2);
                appAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver(this) { // from class: com.android.systemui.controls.management.ControlsProviderSelectorActivity$onStart$$inlined$apply$lambda$1
                    private boolean hasAnimated;
                    final /* synthetic */ ControlsProviderSelectorActivity this$0;

                    {
                        this.this$0 = r1;
                    }

                    @Override // androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
                    public void onChanged() {
                        if (!this.hasAnimated) {
                            this.hasAnimated = true;
                            ControlsAnimations.INSTANCE.enterAnimation(ControlsProviderSelectorActivity.access$getRecyclerView$p(this.this$0)).start();
                        }
                    }
                });
                recyclerView2.setAdapter(appAdapter);
                return;
            }
            Intrinsics.throwUninitializedPropertyAccessException("recyclerView");
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("recyclerView");
        throw null;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.util.LifecycleActivity, android.app.Activity
    public void onStop() {
        super.onStop();
        this.currentUserTracker.stopTracking();
    }

    public final void launchFavoritingActivity(@Nullable ComponentName componentName) {
        this.executor.execute(new Runnable(this, componentName) { // from class: com.android.systemui.controls.management.ControlsProviderSelectorActivity$launchFavoritingActivity$1
            final /* synthetic */ ComponentName $component;
            final /* synthetic */ ControlsProviderSelectorActivity this$0;

            {
                this.this$0 = r1;
                this.$component = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                ComponentName componentName2 = this.$component;
                if (componentName2 != null) {
                    Intent intent = new Intent(this.this$0.getApplicationContext(), ControlsFavoritingActivity.class);
                    intent.putExtra("extra_app_label", ControlsProviderSelectorActivity.access$getListingController$p(this.this$0).getAppLabel(componentName2));
                    intent.putExtra("android.intent.extra.COMPONENT_NAME", componentName2);
                    intent.putExtra("extra_from_provider_selector", true);
                    ControlsProviderSelectorActivity controlsProviderSelectorActivity = this.this$0;
                    controlsProviderSelectorActivity.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(controlsProviderSelectorActivity, new Pair[0]).toBundle());
                }
            }
        });
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.util.LifecycleActivity, android.app.Activity
    public void onDestroy() {
        this.currentUserTracker.stopTracking();
        super.onDestroy();
    }

    private final void animateExitAndFinish() {
        ViewGroup viewGroup = (ViewGroup) requireViewById(C0008R$id.controls_management_root);
        Intrinsics.checkExpressionValueIsNotNull(viewGroup, "rootView");
        ControlsAnimations.exitAnimation(viewGroup, new Runnable(this) { // from class: com.android.systemui.controls.management.ControlsProviderSelectorActivity$animateExitAndFinish$1
            final /* synthetic */ ControlsProviderSelectorActivity this$0;

            /* JADX WARN: Incorrect args count in method signature: ()V */
            {
                this.this$0 = r1;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.this$0.finish();
            }
        }).start();
    }
}
