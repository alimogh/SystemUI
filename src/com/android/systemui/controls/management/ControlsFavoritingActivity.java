package com.android.systemui.controls.management;

import android.app.ActivityOptions;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.viewpager2.widget.ViewPager2;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0011R$layout;
import com.android.systemui.C0015R$string;
import com.android.systemui.Prefs;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.controls.TooltipManager;
import com.android.systemui.controls.controller.ControlInfo;
import com.android.systemui.controls.controller.ControlsControllerImpl;
import com.android.systemui.controls.controller.StructureInfo;
import com.android.systemui.globalactions.GlobalActionsComponent;
import com.android.systemui.util.LifecycleActivity;
import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import kotlin.Unit;
import kotlin.collections.CollectionsKt__CollectionsKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: ControlsFavoritingActivity.kt */
public final class ControlsFavoritingActivity extends LifecycleActivity {
    private CharSequence appName;
    private Runnable cancelLoadRunnable;
    private Comparator<StructureContainer> comparator;
    private ComponentName component;
    private final ControlsControllerImpl controller;
    private final ControlsFavoritingActivity$controlsModelCallback$1 controlsModelCallback;
    private final ControlsFavoritingActivity$currentUserTracker$1 currentUserTracker;
    private View doneButton;
    private final Executor executor;
    private boolean fromProviderSelector;
    private final GlobalActionsComponent globalActionsComponent;
    private boolean isPagerLoaded;
    private List<StructureContainer> listOfStructures = CollectionsKt__CollectionsKt.emptyList();
    private final ControlsFavoritingActivity$listingCallback$1 listingCallback;
    private final ControlsListingController listingController;
    private TooltipManager mTooltipManager;
    private View otherAppsButton;
    private ManagementPageIndicator pageIndicator;
    private TextView statusText;
    private CharSequence structureExtra;
    private ViewPager2 structurePager;
    private TextView subtitleView;
    private TextView titleView;

    public static final /* synthetic */ Comparator access$getComparator$p(ControlsFavoritingActivity controlsFavoritingActivity) {
        Comparator<StructureContainer> comparator = controlsFavoritingActivity.comparator;
        if (comparator != null) {
            return comparator;
        }
        Intrinsics.throwUninitializedPropertyAccessException("comparator");
        throw null;
    }

    public static final /* synthetic */ View access$getDoneButton$p(ControlsFavoritingActivity controlsFavoritingActivity) {
        View view = controlsFavoritingActivity.doneButton;
        if (view != null) {
            return view;
        }
        Intrinsics.throwUninitializedPropertyAccessException("doneButton");
        throw null;
    }

    public static final /* synthetic */ View access$getOtherAppsButton$p(ControlsFavoritingActivity controlsFavoritingActivity) {
        View view = controlsFavoritingActivity.otherAppsButton;
        if (view != null) {
            return view;
        }
        Intrinsics.throwUninitializedPropertyAccessException("otherAppsButton");
        throw null;
    }

    public static final /* synthetic */ ManagementPageIndicator access$getPageIndicator$p(ControlsFavoritingActivity controlsFavoritingActivity) {
        ManagementPageIndicator managementPageIndicator = controlsFavoritingActivity.pageIndicator;
        if (managementPageIndicator != null) {
            return managementPageIndicator;
        }
        Intrinsics.throwUninitializedPropertyAccessException("pageIndicator");
        throw null;
    }

    public static final /* synthetic */ TextView access$getStatusText$p(ControlsFavoritingActivity controlsFavoritingActivity) {
        TextView textView = controlsFavoritingActivity.statusText;
        if (textView != null) {
            return textView;
        }
        Intrinsics.throwUninitializedPropertyAccessException("statusText");
        throw null;
    }

    public static final /* synthetic */ ViewPager2 access$getStructurePager$p(ControlsFavoritingActivity controlsFavoritingActivity) {
        ViewPager2 viewPager2 = controlsFavoritingActivity.structurePager;
        if (viewPager2 != null) {
            return viewPager2;
        }
        Intrinsics.throwUninitializedPropertyAccessException("structurePager");
        throw null;
    }

    public static final /* synthetic */ TextView access$getSubtitleView$p(ControlsFavoritingActivity controlsFavoritingActivity) {
        TextView textView = controlsFavoritingActivity.subtitleView;
        if (textView != null) {
            return textView;
        }
        Intrinsics.throwUninitializedPropertyAccessException("subtitleView");
        throw null;
    }

    public static final /* synthetic */ TextView access$getTitleView$p(ControlsFavoritingActivity controlsFavoritingActivity) {
        TextView textView = controlsFavoritingActivity.titleView;
        if (textView != null) {
            return textView;
        }
        Intrinsics.throwUninitializedPropertyAccessException("titleView");
        throw null;
    }

    public ControlsFavoritingActivity(@NotNull Executor executor, @NotNull ControlsControllerImpl controlsControllerImpl, @NotNull ControlsListingController controlsListingController, @NotNull BroadcastDispatcher broadcastDispatcher, @NotNull GlobalActionsComponent globalActionsComponent) {
        Intrinsics.checkParameterIsNotNull(executor, "executor");
        Intrinsics.checkParameterIsNotNull(controlsControllerImpl, "controller");
        Intrinsics.checkParameterIsNotNull(controlsListingController, "listingController");
        Intrinsics.checkParameterIsNotNull(broadcastDispatcher, "broadcastDispatcher");
        Intrinsics.checkParameterIsNotNull(globalActionsComponent, "globalActionsComponent");
        this.executor = executor;
        this.controller = controlsControllerImpl;
        this.listingController = controlsListingController;
        this.globalActionsComponent = globalActionsComponent;
        this.currentUserTracker = new ControlsFavoritingActivity$currentUserTracker$1(this, broadcastDispatcher, broadcastDispatcher);
        this.listingCallback = new ControlsFavoritingActivity$listingCallback$1(this);
        this.controlsModelCallback = new ControlsFavoritingActivity$controlsModelCallback$1(this);
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        if (!this.fromProviderSelector) {
            this.globalActionsComponent.handleShowGlobalActionsMenu();
        }
        animateExitAndFinish();
    }

    @Override // com.android.systemui.util.LifecycleActivity, android.app.Activity
    public void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        Resources resources = getResources();
        Intrinsics.checkExpressionValueIsNotNull(resources, "resources");
        Configuration configuration = resources.getConfiguration();
        Intrinsics.checkExpressionValueIsNotNull(configuration, "resources.configuration");
        Collator instance = Collator.getInstance(configuration.getLocales().get(0));
        Intrinsics.checkExpressionValueIsNotNull(instance, "collator");
        this.comparator = new Comparator<T>(instance) { // from class: com.android.systemui.controls.management.ControlsFavoritingActivity$onCreate$$inlined$compareBy$1
            final /* synthetic */ Comparator $comparator;

            {
                this.$comparator = r1;
            }

            /* JADX DEBUG: Multi-variable search result rejected for r0v1, resolved type: java.util.Comparator */
            /* JADX WARN: Multi-variable type inference failed */
            @Override // java.util.Comparator
            public final int compare(T t, T t2) {
                return this.$comparator.compare(t.getStructureName(), t2.getStructureName());
            }
        };
        this.appName = getIntent().getCharSequenceExtra("extra_app_label");
        this.structureExtra = getIntent().getCharSequenceExtra("extra_structure");
        this.component = (ComponentName) getIntent().getParcelableExtra("android.intent.extra.COMPONENT_NAME");
        this.fromProviderSelector = getIntent().getBooleanExtra("extra_from_provider_selector", false);
        bindViews();
    }

    private final void loadControls() {
        ComponentName componentName = this.component;
        if (componentName != null) {
            TextView textView = this.statusText;
            if (textView != null) {
                textView.setText(getResources().getText(17040441));
                this.controller.loadForComponent(componentName, new ControlsFavoritingActivity$loadControls$$inlined$let$lambda$1(getResources().getText(C0015R$string.controls_favorite_other_zone_header), this), new Consumer<Runnable>(this) { // from class: com.android.systemui.controls.management.ControlsFavoritingActivity$loadControls$$inlined$let$lambda$2
                    final /* synthetic */ ControlsFavoritingActivity this$0;

                    {
                        this.this$0 = r1;
                    }

                    public final void accept(@NotNull Runnable runnable) {
                        Intrinsics.checkParameterIsNotNull(runnable, "runnable");
                        ControlsFavoritingActivity.access$setCancelLoadRunnable$p(this.this$0, runnable);
                    }
                });
                return;
            }
            Intrinsics.throwUninitializedPropertyAccessException("statusText");
            throw null;
        }
    }

    private final void setUpPager() {
        ViewPager2 viewPager2 = this.structurePager;
        if (viewPager2 != null) {
            viewPager2.setAlpha(0.0f);
            ManagementPageIndicator managementPageIndicator = this.pageIndicator;
            if (managementPageIndicator != null) {
                managementPageIndicator.setAlpha(0.0f);
                ViewPager2 viewPager22 = this.structurePager;
                if (viewPager22 != null) {
                    viewPager22.setAdapter(new StructureAdapter(CollectionsKt__CollectionsKt.emptyList()));
                    viewPager22.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback(this) { // from class: com.android.systemui.controls.management.ControlsFavoritingActivity$setUpPager$$inlined$apply$lambda$1
                        final /* synthetic */ ControlsFavoritingActivity this$0;

                        {
                            this.this$0 = r1;
                        }

                        @Override // androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
                        public void onPageSelected(int i) {
                            super.onPageSelected(i);
                            CharSequence structureName = ((StructureContainer) this.this$0.listOfStructures.get(i)).getStructureName();
                            if (TextUtils.isEmpty(structureName)) {
                                structureName = this.this$0.appName;
                            }
                            ControlsFavoritingActivity.access$getTitleView$p(this.this$0).setText(structureName);
                            ControlsFavoritingActivity.access$getTitleView$p(this.this$0).requestFocus();
                        }

                        @Override // androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
                        public void onPageScrolled(int i, float f, int i2) {
                            super.onPageScrolled(i, f, i2);
                            ControlsFavoritingActivity.access$getPageIndicator$p(this.this$0).setLocation(((float) i) + f);
                        }
                    });
                    return;
                }
                Intrinsics.throwUninitializedPropertyAccessException("structurePager");
                throw null;
            }
            Intrinsics.throwUninitializedPropertyAccessException("pageIndicator");
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("structurePager");
        throw null;
    }

    private final void bindViews() {
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
        viewStub.setLayoutResource(C0011R$layout.controls_management_favorites);
        viewStub.inflate();
        View requireViewById2 = requireViewById(C0008R$id.status_message);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById2, "requireViewById(R.id.status_message)");
        this.statusText = (TextView) requireViewById2;
        if (shouldShowTooltip()) {
            TextView textView = this.statusText;
            if (textView != null) {
                Context context = textView.getContext();
                Intrinsics.checkExpressionValueIsNotNull(context, "statusText.context");
                TooltipManager tooltipManager = new TooltipManager(context, "ControlsStructureSwipeTooltipCount", 2, false, 8, null);
                this.mTooltipManager = tooltipManager;
                addContentView(tooltipManager != null ? tooltipManager.getLayout() : null, new FrameLayout.LayoutParams(-2, -2, 51));
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("statusText");
                throw null;
            }
        }
        View requireViewById3 = requireViewById(C0008R$id.structure_page_indicator);
        ManagementPageIndicator managementPageIndicator = (ManagementPageIndicator) requireViewById3;
        managementPageIndicator.setVisibilityListener(new Function1<Integer, Unit>(this) { // from class: com.android.systemui.controls.management.ControlsFavoritingActivity$bindViews$$inlined$apply$lambda$1
            final /* synthetic */ ControlsFavoritingActivity this$0;

            {
                this.this$0 = r1;
            }

            /* Return type fixed from 'java.lang.Object' to match base method */
            /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
            @Override // kotlin.jvm.functions.Function1
            public /* bridge */ /* synthetic */ Unit invoke(Integer num) {
                invoke(num.intValue());
                return Unit.INSTANCE;
            }

            public final void invoke(int i) {
                TooltipManager access$getMTooltipManager$p;
                if (i != 0 && (access$getMTooltipManager$p = ControlsFavoritingActivity.access$getMTooltipManager$p(this.this$0)) != null) {
                    access$getMTooltipManager$p.hide(true);
                }
            }
        });
        Intrinsics.checkExpressionValueIsNotNull(requireViewById3, "requireViewById<Manageme…}\n            }\n        }");
        this.pageIndicator = managementPageIndicator;
        CharSequence charSequence = this.structureExtra;
        if (charSequence == null && (charSequence = this.appName) == null) {
            charSequence = getResources().getText(C0015R$string.controls_favorite_default_title);
        }
        View requireViewById4 = requireViewById(C0008R$id.title);
        TextView textView2 = (TextView) requireViewById4;
        textView2.setText(charSequence);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById4, "requireViewById<TextView…   text = title\n        }");
        this.titleView = textView2;
        View requireViewById5 = requireViewById(C0008R$id.subtitle);
        TextView textView3 = (TextView) requireViewById5;
        textView3.setText(textView3.getResources().getText(C0015R$string.controls_favorite_subtitle));
        Intrinsics.checkExpressionValueIsNotNull(requireViewById5, "requireViewById<TextView…orite_subtitle)\n        }");
        this.subtitleView = textView3;
        View requireViewById6 = requireViewById(C0008R$id.structure_pager);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById6, "requireViewById<ViewPager2>(R.id.structure_pager)");
        ViewPager2 viewPager2 = (ViewPager2) requireViewById6;
        this.structurePager = viewPager2;
        if (viewPager2 != null) {
            viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback(this) { // from class: com.android.systemui.controls.management.ControlsFavoritingActivity$bindViews$5
                final /* synthetic */ ControlsFavoritingActivity this$0;

                /* JADX WARN: Incorrect args count in method signature: ()V */
                {
                    this.this$0 = r1;
                }

                @Override // androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
                public void onPageSelected(int i) {
                    super.onPageSelected(i);
                    TooltipManager access$getMTooltipManager$p = ControlsFavoritingActivity.access$getMTooltipManager$p(this.this$0);
                    if (access$getMTooltipManager$p != null) {
                        access$getMTooltipManager$p.hide(true);
                    }
                }
            });
            bindButtons();
            return;
        }
        Intrinsics.throwUninitializedPropertyAccessException("structurePager");
        throw null;
    }

    /* access modifiers changed from: public */
    private final void animateExitAndFinish() {
        ViewGroup viewGroup = (ViewGroup) requireViewById(C0008R$id.controls_management_root);
        Intrinsics.checkExpressionValueIsNotNull(viewGroup, "rootView");
        ControlsAnimations.exitAnimation(viewGroup, new Runnable(this) { // from class: com.android.systemui.controls.management.ControlsFavoritingActivity$animateExitAndFinish$1
            final /* synthetic */ ControlsFavoritingActivity this$0;

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

    private final void bindButtons() {
        View requireViewById = requireViewById(C0008R$id.other_apps);
        Button button = (Button) requireViewById;
        button.setOnClickListener(new View.OnClickListener(button, this) { // from class: com.android.systemui.controls.management.ControlsFavoritingActivity$bindButtons$$inlined$apply$lambda$1
            final /* synthetic */ Button $this_apply;
            final /* synthetic */ ControlsFavoritingActivity this$0;

            {
                this.$this_apply = r1;
                this.this$0 = r2;
            }

            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(this.$this_apply.getContext(), ControlsProviderSelectorActivity.class));
                if (ControlsFavoritingActivity.access$getDoneButton$p(this.this$0).isEnabled()) {
                    Toast.makeText(this.this$0.getApplicationContext(), C0015R$string.controls_favorite_toast_no_changes, 0).show();
                }
                ControlsFavoritingActivity controlsFavoritingActivity = this.this$0;
                controlsFavoritingActivity.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(controlsFavoritingActivity, new Pair[0]).toBundle());
                ControlsFavoritingActivity.access$animateExitAndFinish(this.this$0);
            }
        });
        Intrinsics.checkExpressionValueIsNotNull(requireViewById, "requireViewById<Button>(…)\n            }\n        }");
        this.otherAppsButton = requireViewById;
        View requireViewById2 = requireViewById(C0008R$id.done);
        Button button2 = (Button) requireViewById2;
        button2.setEnabled(false);
        button2.setOnClickListener(new View.OnClickListener(this) { // from class: com.android.systemui.controls.management.ControlsFavoritingActivity$bindButtons$$inlined$apply$lambda$2
            final /* synthetic */ ControlsFavoritingActivity this$0;

            {
                this.this$0 = r1;
            }

            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                if (ControlsFavoritingActivity.access$getComponent$p(this.this$0) != null) {
                    for (StructureContainer structureContainer : ControlsFavoritingActivity.access$getListOfStructures$p(this.this$0)) {
                        List<ControlInfo> favorites = structureContainer.getModel().getFavorites();
                        ControlsControllerImpl access$getController$p = ControlsFavoritingActivity.access$getController$p(this.this$0);
                        ComponentName access$getComponent$p = ControlsFavoritingActivity.access$getComponent$p(this.this$0);
                        if (access$getComponent$p != null) {
                            access$getController$p.replaceFavoritesForStructure(new StructureInfo(access$getComponent$p, structureContainer.getStructureName(), favorites));
                        } else {
                            Intrinsics.throwNpe();
                            throw null;
                        }
                    }
                    ControlsFavoritingActivity.access$animateExitAndFinish(this.this$0);
                    ControlsFavoritingActivity.access$getGlobalActionsComponent$p(this.this$0).handleShowGlobalActionsMenu();
                }
            }
        });
        Intrinsics.checkExpressionValueIsNotNull(requireViewById2, "requireViewById<Button>(…)\n            }\n        }");
        this.doneButton = requireViewById2;
    }

    @Override // com.android.systemui.util.LifecycleActivity, android.app.Activity
    public void onPause() {
        super.onPause();
        TooltipManager tooltipManager = this.mTooltipManager;
        if (tooltipManager != null) {
            tooltipManager.hide(false);
        }
    }

    @Override // com.android.systemui.util.LifecycleActivity, android.app.Activity
    public void onStart() {
        super.onStart();
        this.listingController.addCallback(this.listingCallback);
        this.currentUserTracker.startTracking();
    }

    @Override // com.android.systemui.util.LifecycleActivity, android.app.Activity
    public void onResume() {
        super.onResume();
        if (!this.isPagerLoaded) {
            setUpPager();
            loadControls();
            this.isPagerLoaded = true;
        }
    }

    @Override // com.android.systemui.util.LifecycleActivity, android.app.Activity
    public void onStop() {
        super.onStop();
        this.listingController.removeCallback(this.listingCallback);
        this.currentUserTracker.stopTracking();
    }

    @Override // android.app.Activity, android.content.ComponentCallbacks
    public void onConfigurationChanged(@NotNull Configuration configuration) {
        Intrinsics.checkParameterIsNotNull(configuration, "newConfig");
        super.onConfigurationChanged(configuration);
        TooltipManager tooltipManager = this.mTooltipManager;
        if (tooltipManager != null) {
            tooltipManager.hide(false);
        }
    }

    @Override // com.android.systemui.util.LifecycleActivity, android.app.Activity
    public void onDestroy() {
        Runnable runnable = this.cancelLoadRunnable;
        if (runnable != null) {
            runnable.run();
        }
        super.onDestroy();
    }

    private final boolean shouldShowTooltip() {
        return Prefs.getInt(getApplicationContext(), "ControlsStructureSwipeTooltipCount", 0) < 2;
    }
}
