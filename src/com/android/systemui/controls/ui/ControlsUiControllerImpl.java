package com.android.systemui.controls.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.service.controls.Control;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.Space;
import android.widget.TextView;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0009R$integer;
import com.android.systemui.C0011R$layout;
import com.android.systemui.C0015R$string;
import com.android.systemui.C0016R$style;
import com.android.systemui.controls.controller.ControlInfo;
import com.android.systemui.controls.controller.ControlsController;
import com.android.systemui.controls.controller.StructureInfo;
import com.android.systemui.controls.management.ControlsEditingActivity;
import com.android.systemui.controls.management.ControlsFavoritingActivity;
import com.android.systemui.controls.management.ControlsListingController;
import com.android.systemui.controls.management.ControlsProviderSelectorActivity;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.statusbar.phone.ShadeController;
import com.android.systemui.util.concurrency.DelayableExecutor;
import dagger.Lazy;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import kotlin.TypeCastException;
import kotlin.Unit;
import kotlin.collections.CollectionsKt__IterablesKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KDeclarationContainer;
import org.jetbrains.annotations.NotNull;
/* compiled from: ControlsUiControllerImpl.kt */
public final class ControlsUiControllerImpl implements ControlsUiController {
    private static final ComponentName EMPTY_COMPONENT;
    private static final StructureInfo EMPTY_STRUCTURE;
    private final ActivityStarter activityStarter;
    private List<StructureInfo> allStructures;
    @NotNull
    private final DelayableExecutor bgExecutor;
    private final Collator collator;
    @NotNull
    private final Context context;
    @NotNull
    private final ControlActionCoordinator controlActionCoordinator;
    private final Map<ControlKey, ControlViewHolder> controlViewsById = new LinkedHashMap();
    private final Map<ControlKey, ControlWithState> controlsById = new LinkedHashMap();
    @NotNull
    private final Lazy<ControlsController> controlsController;
    @NotNull
    private final Lazy<ControlsListingController> controlsListingController;
    private Runnable dismissGlobalActions;
    private boolean hidden = true;
    private ControlsListingController.ControlsListingCallback listingCallback;
    private final Comparator<SelectionItem> localeComparator;
    private final Consumer<Boolean> onSeedingComplete;
    private ViewGroup parent;
    private ListPopupWindow popup;
    private final ContextThemeWrapper popupThemedContext = new ContextThemeWrapper(this.context, C0016R$style.Control_ListPopupWindow);
    private StructureInfo selectedStructure = EMPTY_STRUCTURE;
    private final ShadeController shadeController;
    @NotNull
    private final SharedPreferences sharedPreferences;
    @NotNull
    private final DelayableExecutor uiExecutor;

    public ControlsUiControllerImpl(@NotNull Lazy<ControlsController> lazy, @NotNull Context context, @NotNull DelayableExecutor delayableExecutor, @NotNull DelayableExecutor delayableExecutor2, @NotNull Lazy<ControlsListingController> lazy2, @NotNull SharedPreferences sharedPreferences, @NotNull ControlActionCoordinator controlActionCoordinator, @NotNull ActivityStarter activityStarter, @NotNull ShadeController shadeController) {
        Intrinsics.checkParameterIsNotNull(lazy, "controlsController");
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(delayableExecutor, "uiExecutor");
        Intrinsics.checkParameterIsNotNull(delayableExecutor2, "bgExecutor");
        Intrinsics.checkParameterIsNotNull(lazy2, "controlsListingController");
        Intrinsics.checkParameterIsNotNull(sharedPreferences, "sharedPreferences");
        Intrinsics.checkParameterIsNotNull(controlActionCoordinator, "controlActionCoordinator");
        Intrinsics.checkParameterIsNotNull(activityStarter, "activityStarter");
        Intrinsics.checkParameterIsNotNull(shadeController, "shadeController");
        this.controlsController = lazy;
        this.context = context;
        this.uiExecutor = delayableExecutor;
        this.bgExecutor = delayableExecutor2;
        this.controlsListingController = lazy2;
        this.sharedPreferences = sharedPreferences;
        this.controlActionCoordinator = controlActionCoordinator;
        this.activityStarter = activityStarter;
        this.shadeController = shadeController;
        Resources resources = this.context.getResources();
        Intrinsics.checkExpressionValueIsNotNull(resources, "context.resources");
        Configuration configuration = resources.getConfiguration();
        Intrinsics.checkExpressionValueIsNotNull(configuration, "context.resources.configuration");
        Collator instance = Collator.getInstance(configuration.getLocales().get(0));
        this.collator = instance;
        Intrinsics.checkExpressionValueIsNotNull(instance, "collator");
        this.localeComparator = new Comparator<T>(instance) { // from class: com.android.systemui.controls.ui.ControlsUiControllerImpl$$special$$inlined$compareBy$1
            final /* synthetic */ Comparator $comparator;

            {
                this.$comparator = r1;
            }

            /* JADX DEBUG: Multi-variable search result rejected for r0v1, resolved type: java.util.Comparator */
            /* JADX WARN: Multi-variable type inference failed */
            @Override // java.util.Comparator
            public final int compare(T t, T t2) {
                return this.$comparator.compare(t.getTitle(), t2.getTitle());
            }
        };
        this.onSeedingComplete = new Consumer<Boolean>(this) { // from class: com.android.systemui.controls.ui.ControlsUiControllerImpl$onSeedingComplete$1
            final /* synthetic */ ControlsUiControllerImpl this$0;

            {
                this.this$0 = r1;
            }

            /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
            @Override // java.util.function.Consumer
            public /* bridge */ /* synthetic */ void accept(Boolean bool) {
                accept(bool.booleanValue());
            }

            public final void accept(boolean z) {
                T t;
                if (z) {
                    ControlsUiControllerImpl controlsUiControllerImpl = this.this$0;
                    Iterator<T> it = controlsUiControllerImpl.getControlsController().get().getFavorites().iterator();
                    if (!it.hasNext()) {
                        t = null;
                    } else {
                        T next = it.next();
                        if (it.hasNext()) {
                            int size = next.getControls().size();
                            do {
                                T next2 = it.next();
                                int size2 = next2.getControls().size();
                                if (size < size2) {
                                    next = next2;
                                    size = size2;
                                }
                            } while (it.hasNext());
                        }
                        t = next;
                    }
                    T t2 = t;
                    if (t2 == null) {
                        t2 = ControlsUiControllerImpl.EMPTY_STRUCTURE;
                    }
                    controlsUiControllerImpl.selectedStructure = t2;
                    ControlsUiControllerImpl controlsUiControllerImpl2 = this.this$0;
                    controlsUiControllerImpl2.updatePreferences(controlsUiControllerImpl2.selectedStructure);
                }
                ControlsUiControllerImpl controlsUiControllerImpl3 = this.this$0;
                controlsUiControllerImpl3.reload(ControlsUiControllerImpl.access$getParent$p(controlsUiControllerImpl3));
            }
        };
    }

    public static final /* synthetic */ Runnable access$getDismissGlobalActions$p(ControlsUiControllerImpl controlsUiControllerImpl) {
        Runnable runnable = controlsUiControllerImpl.dismissGlobalActions;
        if (runnable != null) {
            return runnable;
        }
        Intrinsics.throwUninitializedPropertyAccessException("dismissGlobalActions");
        throw null;
    }

    public static final /* synthetic */ ViewGroup access$getParent$p(ControlsUiControllerImpl controlsUiControllerImpl) {
        ViewGroup viewGroup = controlsUiControllerImpl.parent;
        if (viewGroup != null) {
            return viewGroup;
        }
        Intrinsics.throwUninitializedPropertyAccessException("parent");
        throw null;
    }

    @NotNull
    public final Lazy<ControlsController> getControlsController() {
        return this.controlsController;
    }

    @NotNull
    public final DelayableExecutor getUiExecutor() {
        return this.uiExecutor;
    }

    static {
        ComponentName componentName = new ComponentName("", "");
        EMPTY_COMPONENT = componentName;
        EMPTY_STRUCTURE = new StructureInfo(componentName, "", new ArrayList());
    }

    @Override // com.android.systemui.controls.ui.ControlsUiController
    public boolean getAvailable() {
        return this.controlsController.get().getAvailable();
    }

    private final ControlsListingController.ControlsListingCallback createCallback(Function1<? super List<SelectionItem>, Unit> function1) {
        return new ControlsUiControllerImpl$createCallback$1(this, function1);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r6v20, resolved type: java.util.Map<com.android.systemui.controls.ui.ControlKey, com.android.systemui.controls.ui.ControlWithState> */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.android.systemui.controls.ui.ControlsUiController
    public void show(@NotNull ViewGroup viewGroup, @NotNull Runnable runnable) {
        Intrinsics.checkParameterIsNotNull(viewGroup, "parent");
        Intrinsics.checkParameterIsNotNull(runnable, "dismissGlobalActions");
        Log.d("ControlsUiController", "show()");
        this.parent = viewGroup;
        this.dismissGlobalActions = runnable;
        this.hidden = false;
        List<StructureInfo> favorites = this.controlsController.get().getFavorites();
        this.allStructures = favorites;
        if (favorites != null) {
            this.selectedStructure = loadPreference(favorites);
            if (this.controlsController.get().addSeedingFavoritesCallback(this.onSeedingComplete)) {
                this.listingCallback = createCallback(new Function1<List<? extends SelectionItem>, Unit>(this) { // from class: com.android.systemui.controls.ui.ControlsUiControllerImpl$show$1
                    @Override // kotlin.jvm.internal.CallableReference
                    public final String getName() {
                        return "showSeedingView";
                    }

                    @Override // kotlin.jvm.internal.CallableReference
                    public final KDeclarationContainer getOwner() {
                        return Reflection.getOrCreateKotlinClass(ControlsUiControllerImpl.class);
                    }

                    @Override // kotlin.jvm.internal.CallableReference
                    public final String getSignature() {
                        return "showSeedingView(Ljava/util/List;)V";
                    }

                    /* Return type fixed from 'java.lang.Object' to match base method */
                    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
                    @Override // kotlin.jvm.functions.Function1
                    public /* bridge */ /* synthetic */ Unit invoke(List<? extends SelectionItem> list) {
                        invoke((List<SelectionItem>) list);
                        return Unit.INSTANCE;
                    }

                    public final void invoke(@NotNull List<SelectionItem> list) {
                        Intrinsics.checkParameterIsNotNull(list, "p1");
                        ((ControlsUiControllerImpl) this.receiver).showSeedingView(list);
                    }
                });
            } else {
                if (this.selectedStructure.getControls().isEmpty()) {
                    List<StructureInfo> list = this.allStructures;
                    if (list == null) {
                        Intrinsics.throwUninitializedPropertyAccessException("allStructures");
                        throw null;
                    } else if (list.size() <= 1) {
                        this.listingCallback = createCallback(new Function1<List<? extends SelectionItem>, Unit>(this) { // from class: com.android.systemui.controls.ui.ControlsUiControllerImpl$show$2
                            @Override // kotlin.jvm.internal.CallableReference
                            public final String getName() {
                                return "showInitialSetupView";
                            }

                            @Override // kotlin.jvm.internal.CallableReference
                            public final KDeclarationContainer getOwner() {
                                return Reflection.getOrCreateKotlinClass(ControlsUiControllerImpl.class);
                            }

                            @Override // kotlin.jvm.internal.CallableReference
                            public final String getSignature() {
                                return "showInitialSetupView(Ljava/util/List;)V";
                            }

                            /* Return type fixed from 'java.lang.Object' to match base method */
                            /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
                            @Override // kotlin.jvm.functions.Function1
                            public /* bridge */ /* synthetic */ Unit invoke(List<? extends SelectionItem> list2) {
                                invoke((List<SelectionItem>) list2);
                                return Unit.INSTANCE;
                            }

                            public final void invoke(@NotNull List<SelectionItem> list2) {
                                Intrinsics.checkParameterIsNotNull(list2, "p1");
                                ((ControlsUiControllerImpl) this.receiver).showInitialSetupView(list2);
                            }
                        });
                    }
                }
                List<ControlInfo> controls = this.selectedStructure.getControls();
                ArrayList arrayList = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(controls, 10));
                for (ControlInfo controlInfo : controls) {
                    arrayList.add(new ControlWithState(this.selectedStructure.getComponentName(), controlInfo, null));
                }
                Map<ControlKey, ControlWithState> map = this.controlsById;
                for (Object obj : arrayList) {
                    map.put(new ControlKey(this.selectedStructure.getComponentName(), ((ControlWithState) obj).getCi().getControlId()), obj);
                }
                this.listingCallback = createCallback(new Function1<List<? extends SelectionItem>, Unit>(this) { // from class: com.android.systemui.controls.ui.ControlsUiControllerImpl$show$5
                    @Override // kotlin.jvm.internal.CallableReference
                    public final String getName() {
                        return "showControlsView";
                    }

                    @Override // kotlin.jvm.internal.CallableReference
                    public final KDeclarationContainer getOwner() {
                        return Reflection.getOrCreateKotlinClass(ControlsUiControllerImpl.class);
                    }

                    @Override // kotlin.jvm.internal.CallableReference
                    public final String getSignature() {
                        return "showControlsView(Ljava/util/List;)V";
                    }

                    /* Return type fixed from 'java.lang.Object' to match base method */
                    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
                    @Override // kotlin.jvm.functions.Function1
                    public /* bridge */ /* synthetic */ Unit invoke(List<? extends SelectionItem> list2) {
                        invoke((List<SelectionItem>) list2);
                        return Unit.INSTANCE;
                    }

                    public final void invoke(@NotNull List<SelectionItem> list2) {
                        Intrinsics.checkParameterIsNotNull(list2, "p1");
                        ((ControlsUiControllerImpl) this.receiver).showControlsView(list2);
                    }
                });
                this.controlsController.get().subscribeToFavorites(this.selectedStructure);
            }
            ControlsListingController controlsListingController = this.controlsListingController.get();
            ControlsListingController.ControlsListingCallback controlsListingCallback = this.listingCallback;
            if (controlsListingCallback != null) {
                controlsListingController.addCallback(controlsListingCallback);
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("listingCallback");
                throw null;
            }
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("allStructures");
            throw null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void reload(ViewGroup viewGroup) {
        if (!this.hidden) {
            ControlsListingController controlsListingController = this.controlsListingController.get();
            ControlsListingController.ControlsListingCallback controlsListingCallback = this.listingCallback;
            if (controlsListingCallback != null) {
                controlsListingController.removeCallback(controlsListingCallback);
                this.controlsController.get().unsubscribe();
                ObjectAnimator ofFloat = ObjectAnimator.ofFloat(viewGroup, "alpha", 1.0f, 0.0f);
                ofFloat.setInterpolator(new AccelerateInterpolator(1.0f));
                ofFloat.setDuration(200L);
                ofFloat.addListener(new AnimatorListenerAdapter(this, viewGroup) { // from class: com.android.systemui.controls.ui.ControlsUiControllerImpl$reload$1
                    final /* synthetic */ ViewGroup $parent;
                    final /* synthetic */ ControlsUiControllerImpl this$0;

                    {
                        this.this$0 = r1;
                        this.$parent = r2;
                    }

                    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                    public void onAnimationEnd(@NotNull Animator animator) {
                        Intrinsics.checkParameterIsNotNull(animator, "animation");
                        this.this$0.controlViewsById.clear();
                        this.this$0.controlsById.clear();
                        ControlsUiControllerImpl controlsUiControllerImpl = this.this$0;
                        controlsUiControllerImpl.show(this.$parent, ControlsUiControllerImpl.access$getDismissGlobalActions$p(controlsUiControllerImpl));
                        ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(this.$parent, "alpha", 0.0f, 1.0f);
                        ofFloat2.setInterpolator(new DecelerateInterpolator(1.0f));
                        ofFloat2.setDuration(200L);
                        ofFloat2.start();
                    }
                });
                ofFloat.start();
                return;
            }
            Intrinsics.throwUninitializedPropertyAccessException("listingCallback");
            throw null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void showSeedingView(List<SelectionItem> list) {
        LayoutInflater from = LayoutInflater.from(this.context);
        int i = C0011R$layout.controls_no_favorites;
        ViewGroup viewGroup = this.parent;
        if (viewGroup != null) {
            from.inflate(i, viewGroup, true);
            ViewGroup viewGroup2 = this.parent;
            if (viewGroup2 != null) {
                ((TextView) viewGroup2.requireViewById(C0008R$id.controls_subtitle)).setText(this.context.getResources().getString(C0015R$string.controls_seeding_in_progress));
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("parent");
                throw null;
            }
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("parent");
            throw null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void showInitialSetupView(List<SelectionItem> list) {
        LayoutInflater from = LayoutInflater.from(this.context);
        int i = C0011R$layout.controls_no_favorites;
        ViewGroup viewGroup = this.parent;
        if (viewGroup != null) {
            from.inflate(i, viewGroup, true);
            ViewGroup viewGroup2 = this.parent;
            if (viewGroup2 != null) {
                View requireViewById = viewGroup2.requireViewById(C0008R$id.controls_no_favorites_group);
                if (requireViewById != null) {
                    ViewGroup viewGroup3 = (ViewGroup) requireViewById;
                    viewGroup3.setOnClickListener(new View.OnClickListener(this) { // from class: com.android.systemui.controls.ui.ControlsUiControllerImpl$showInitialSetupView$1
                        final /* synthetic */ ControlsUiControllerImpl this$0;

                        {
                            this.this$0 = r1;
                        }

                        @Override // android.view.View.OnClickListener
                        public final void onClick(@NotNull View view) {
                            Intrinsics.checkParameterIsNotNull(view, "v");
                            ControlsUiControllerImpl controlsUiControllerImpl = this.this$0;
                            Context context = view.getContext();
                            Intrinsics.checkExpressionValueIsNotNull(context, "v.context");
                            controlsUiControllerImpl.startProviderSelectorActivity(context);
                        }
                    });
                    ViewGroup viewGroup4 = this.parent;
                    if (viewGroup4 != null) {
                        ((TextView) viewGroup4.requireViewById(C0008R$id.controls_subtitle)).setText(this.context.getResources().getString(C0015R$string.quick_controls_subtitle));
                        ViewGroup viewGroup5 = this.parent;
                        if (viewGroup5 != null) {
                            View requireViewById2 = viewGroup5.requireViewById(C0008R$id.controls_icon_row);
                            if (requireViewById2 != null) {
                                ViewGroup viewGroup6 = (ViewGroup) requireViewById2;
                                for (SelectionItem selectionItem : list) {
                                    View inflate = from.inflate(C0011R$layout.controls_icon, viewGroup3, false);
                                    if (inflate != null) {
                                        ImageView imageView = (ImageView) inflate;
                                        imageView.setContentDescription(selectionItem.getTitle());
                                        imageView.setImageDrawable(selectionItem.getIcon());
                                        viewGroup6.addView(imageView);
                                    } else {
                                        throw new TypeCastException("null cannot be cast to non-null type android.widget.ImageView");
                                    }
                                }
                                return;
                            }
                            throw new TypeCastException("null cannot be cast to non-null type android.view.ViewGroup");
                        }
                        Intrinsics.throwUninitializedPropertyAccessException("parent");
                        throw null;
                    }
                    Intrinsics.throwUninitializedPropertyAccessException("parent");
                    throw null;
                }
                throw new TypeCastException("null cannot be cast to non-null type android.view.ViewGroup");
            }
            Intrinsics.throwUninitializedPropertyAccessException("parent");
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("parent");
        throw null;
    }

    /* access modifiers changed from: private */
    public final void startFavoritingActivity(Context context, StructureInfo structureInfo) {
        startTargetedActivity(context, structureInfo, ControlsFavoritingActivity.class);
    }

    /* access modifiers changed from: private */
    public final void startEditingActivity(Context context, StructureInfo structureInfo) {
        startTargetedActivity(context, structureInfo, ControlsEditingActivity.class);
    }

    private final void startTargetedActivity(Context context, StructureInfo structureInfo, Class<?> cls) {
        Intent intent = new Intent(context, cls);
        intent.addFlags(335544320);
        putIntentExtras(intent, structureInfo);
        startActivity(context, intent);
    }

    private final SelectionItem findSelectionItem(StructureInfo structureInfo, List<SelectionItem> list) {
        Object obj;
        boolean z;
        Iterator<T> it = list.iterator();
        while (true) {
            if (!it.hasNext()) {
                obj = null;
                break;
            }
            obj = it.next();
            SelectionItem selectionItem = (SelectionItem) obj;
            if (!Intrinsics.areEqual(selectionItem.getComponentName(), structureInfo.getComponentName()) || !Intrinsics.areEqual(selectionItem.getStructure(), structureInfo.getStructure())) {
                z = false;
                continue;
            } else {
                z = true;
                continue;
            }
            if (z) {
                break;
            }
        }
        return (SelectionItem) obj;
    }

    private final void putIntentExtras(Intent intent, StructureInfo structureInfo) {
        intent.putExtra("extra_app_label", this.controlsListingController.get().getAppLabel(structureInfo.getComponentName()));
        intent.putExtra("extra_structure", structureInfo.getStructure());
        intent.putExtra("android.intent.extra.COMPONENT_NAME", structureInfo.getComponentName());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void startProviderSelectorActivity(Context context) {
        Intent intent = new Intent(context, ControlsProviderSelectorActivity.class);
        intent.addFlags(335544320);
        startActivity(context, intent);
    }

    private final void startActivity(Context context, Intent intent) {
        intent.putExtra("extra_animate", true);
        Runnable runnable = this.dismissGlobalActions;
        if (runnable != null) {
            runnable.run();
            this.activityStarter.dismissKeyguardThenExecute(new ActivityStarter.OnDismissAction(this, context, intent) { // from class: com.android.systemui.controls.ui.ControlsUiControllerImpl$startActivity$1
                final /* synthetic */ Context $context;
                final /* synthetic */ Intent $intent;
                final /* synthetic */ ControlsUiControllerImpl this$0;

                {
                    this.this$0 = r1;
                    this.$context = r2;
                    this.$intent = r3;
                }

                @Override // com.android.systemui.plugins.ActivityStarter.OnDismissAction
                public final boolean onDismiss() {
                    this.this$0.shadeController.collapsePanel(false);
                    this.$context.startActivity(this.$intent);
                    return true;
                }
            }, null, true);
            return;
        }
        Intrinsics.throwUninitializedPropertyAccessException("dismissGlobalActions");
        throw null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void showControlsView(List<SelectionItem> list) {
        this.controlViewsById.clear();
        createListView();
        createDropDown(list);
        createMenu();
    }

    /* JADX WARN: Type inference failed for: r2v4, types: [android.widget.ArrayAdapter, T] */
    /* JADX WARNING: Unknown variable types count: 1 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final void createMenu() {
        /*
            r5 = this;
            r0 = 2
            java.lang.String[] r0 = new java.lang.String[r0]
            android.content.Context r1 = r5.context
            android.content.res.Resources r1 = r1.getResources()
            int r2 = com.android.systemui.C0015R$string.controls_menu_add
            java.lang.String r1 = r1.getString(r2)
            r2 = 0
            r0[r2] = r1
            android.content.Context r1 = r5.context
            android.content.res.Resources r1 = r1.getResources()
            int r2 = com.android.systemui.C0015R$string.controls_menu_edit
            java.lang.String r1 = r1.getString(r2)
            r2 = 1
            r0[r2] = r1
            kotlin.jvm.internal.Ref$ObjectRef r1 = new kotlin.jvm.internal.Ref$ObjectRef
            r1.<init>()
            android.widget.ArrayAdapter r2 = new android.widget.ArrayAdapter
            android.content.Context r3 = r5.context
            int r4 = com.android.systemui.C0011R$layout.controls_more_item
            r2.<init>(r3, r4, r0)
            r1.element = r2
            android.view.ViewGroup r0 = r5.parent
            if (r0 == 0) goto L_0x0046
            int r2 = com.android.systemui.C0008R$id.controls_more
            android.view.View r0 = r0.requireViewById(r2)
            android.widget.ImageView r0 = (android.widget.ImageView) r0
            com.android.systemui.controls.ui.ControlsUiControllerImpl$createMenu$1 r2 = new com.android.systemui.controls.ui.ControlsUiControllerImpl$createMenu$1
            r2.<init>(r5, r0, r1)
            r0.setOnClickListener(r2)
            return
        L_0x0046:
            java.lang.String r5 = "parent"
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r5)
            r5 = 0
            throw r5
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.controls.ui.ControlsUiControllerImpl.createMenu():void");
    }

    private final void createListView() {
        LayoutInflater from = LayoutInflater.from(this.context);
        int i = C0011R$layout.controls_with_favorites;
        ViewGroup viewGroup = this.parent;
        if (viewGroup != null) {
            from.inflate(i, viewGroup, true);
            int findMaxColumns = findMaxColumns();
            ViewGroup viewGroup2 = this.parent;
            if (viewGroup2 != null) {
                View requireViewById = viewGroup2.requireViewById(C0008R$id.global_actions_controls_list);
                if (requireViewById != null) {
                    ViewGroup viewGroup3 = (ViewGroup) requireViewById;
                    Intrinsics.checkExpressionValueIsNotNull(from, "inflater");
                    ViewGroup createRow = createRow(from, viewGroup3);
                    for (ControlInfo controlInfo : this.selectedStructure.getControls()) {
                        ControlKey controlKey = new ControlKey(this.selectedStructure.getComponentName(), controlInfo.getControlId());
                        ControlWithState controlWithState = this.controlsById.get(controlKey);
                        if (controlWithState != null) {
                            if (createRow.getChildCount() == findMaxColumns) {
                                createRow = createRow(from, viewGroup3);
                            }
                            View inflate = from.inflate(C0011R$layout.controls_base_item, createRow, false);
                            if (inflate != null) {
                                ViewGroup viewGroup4 = (ViewGroup) inflate;
                                if (createRow.getChildCount() + 1 == findMaxColumns) {
                                    ViewGroup.LayoutParams layoutParams = viewGroup4.getLayoutParams();
                                    if (layoutParams != null) {
                                        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) layoutParams;
                                        marginLayoutParams.rightMargin = 0;
                                        viewGroup4.setLayoutParams(marginLayoutParams);
                                    } else {
                                        throw new TypeCastException("null cannot be cast to non-null type android.view.ViewGroup.MarginLayoutParams");
                                    }
                                }
                                createRow.addView(viewGroup4);
                                ControlsController controlsController = this.controlsController.get();
                                Intrinsics.checkExpressionValueIsNotNull(controlsController, "controlsController.get()");
                                ControlViewHolder controlViewHolder = new ControlViewHolder(viewGroup4, controlsController, this.uiExecutor, this.bgExecutor, this.controlActionCoordinator);
                                controlViewHolder.bindData(controlWithState);
                                this.controlViewsById.put(controlKey, controlViewHolder);
                            } else {
                                throw new TypeCastException("null cannot be cast to non-null type android.view.ViewGroup");
                            }
                        }
                    }
                    int size = this.selectedStructure.getControls().size() % findMaxColumns;
                    for (int i2 = size == 0 ? 0 : findMaxColumns - size; i2 > 0; i2--) {
                        createRow.addView(new Space(this.context), new LinearLayout.LayoutParams(0, 0, 1.0f));
                    }
                    return;
                }
                throw new TypeCastException("null cannot be cast to non-null type android.view.ViewGroup");
            }
            Intrinsics.throwUninitializedPropertyAccessException("parent");
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("parent");
        throw null;
    }

    private final int findMaxColumns() {
        int i;
        Resources resources = this.context.getResources();
        int integer = resources.getInteger(C0009R$integer.controls_max_columns);
        int integer2 = resources.getInteger(C0009R$integer.controls_max_columns_adjust_below_width_dp);
        TypedValue typedValue = new TypedValue();
        boolean z = true;
        resources.getValue(C0005R$dimen.controls_max_columns_adjust_above_font_scale, typedValue, true);
        float f = typedValue.getFloat();
        Intrinsics.checkExpressionValueIsNotNull(resources, "res");
        Configuration configuration = resources.getConfiguration();
        if (configuration.orientation != 1) {
            z = false;
        }
        return (!z || (i = configuration.screenWidthDp) == 0 || i > integer2 || configuration.fontScale < f) ? integer : integer - 1;
    }

    private final StructureInfo loadPreference(List<StructureInfo> list) {
        ComponentName componentName;
        boolean z;
        if (list.isEmpty()) {
            return EMPTY_STRUCTURE;
        }
        Object obj = null;
        String string = this.sharedPreferences.getString("controls_component", null);
        if (string == null || (componentName = ComponentName.unflattenFromString(string)) == null) {
            componentName = EMPTY_COMPONENT;
        }
        String string2 = this.sharedPreferences.getString("controls_structure", "");
        Iterator<T> it = list.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            Object next = it.next();
            StructureInfo structureInfo = (StructureInfo) next;
            if (!Intrinsics.areEqual(componentName, structureInfo.getComponentName()) || !Intrinsics.areEqual(string2, structureInfo.getStructure())) {
                z = false;
                continue;
            } else {
                z = true;
                continue;
            }
            if (z) {
                obj = next;
                break;
            }
        }
        StructureInfo structureInfo2 = (StructureInfo) obj;
        return structureInfo2 != null ? structureInfo2 : list.get(0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void updatePreferences(StructureInfo structureInfo) {
        if (!Intrinsics.areEqual(structureInfo, EMPTY_STRUCTURE)) {
            this.sharedPreferences.edit().putString("controls_component", structureInfo.getComponentName().flattenToString()).putString("controls_structure", structureInfo.getStructure().toString()).commit();
        }
    }

    /* access modifiers changed from: private */
    public final void switchAppOrStructure(SelectionItem selectionItem) {
        boolean z;
        List<StructureInfo> list = this.allStructures;
        if (list != null) {
            for (StructureInfo structureInfo : list) {
                if (!Intrinsics.areEqual(structureInfo.getStructure(), selectionItem.getStructure()) || !Intrinsics.areEqual(structureInfo.getComponentName(), selectionItem.getComponentName())) {
                    z = false;
                    continue;
                } else {
                    z = true;
                    continue;
                }
                if (z) {
                    if (!Intrinsics.areEqual(structureInfo, this.selectedStructure)) {
                        this.selectedStructure = structureInfo;
                        updatePreferences(structureInfo);
                        ViewGroup viewGroup = this.parent;
                        if (viewGroup != null) {
                            reload(viewGroup);
                            return;
                        } else {
                            Intrinsics.throwUninitializedPropertyAccessException("parent");
                            throw null;
                        }
                    } else {
                        return;
                    }
                }
            }
            throw new NoSuchElementException("Collection contains no element matching the predicate.");
        }
        Intrinsics.throwUninitializedPropertyAccessException("allStructures");
        throw null;
    }

    @Override // com.android.systemui.controls.ui.ControlsUiController
    public void closeDialogs(boolean z) {
        if (z) {
            ListPopupWindow listPopupWindow = this.popup;
            if (listPopupWindow != null) {
                listPopupWindow.dismissImmediate();
            }
        } else {
            ListPopupWindow listPopupWindow2 = this.popup;
            if (listPopupWindow2 != null) {
                listPopupWindow2.dismiss();
            }
        }
        this.popup = null;
        for (Map.Entry<ControlKey, ControlViewHolder> entry : this.controlViewsById.entrySet()) {
            entry.getValue().dismiss();
        }
        this.controlActionCoordinator.closeDialogs();
    }

    @Override // com.android.systemui.controls.ui.ControlsUiController
    public void hide() {
        this.hidden = true;
        closeDialogs(true);
        this.controlsController.get().unsubscribe();
        ViewGroup viewGroup = this.parent;
        if (viewGroup != null) {
            viewGroup.removeAllViews();
            this.controlsById.clear();
            this.controlViewsById.clear();
            ControlsListingController controlsListingController = this.controlsListingController.get();
            ControlsListingController.ControlsListingCallback controlsListingCallback = this.listingCallback;
            if (controlsListingCallback != null) {
                controlsListingController.removeCallback(controlsListingCallback);
                RenderInfo.Companion.clearCache();
                return;
            }
            Intrinsics.throwUninitializedPropertyAccessException("listingCallback");
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("parent");
        throw null;
    }

    @Override // com.android.systemui.controls.ui.ControlsUiController
    public void onActionResponse(@NotNull ComponentName componentName, @NotNull String str, int i) {
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        Intrinsics.checkParameterIsNotNull(str, "controlId");
        this.uiExecutor.execute(new Runnable(this, new ControlKey(componentName, str), i) { // from class: com.android.systemui.controls.ui.ControlsUiControllerImpl$onActionResponse$1
            final /* synthetic */ ControlKey $key;
            final /* synthetic */ int $response;
            final /* synthetic */ ControlsUiControllerImpl this$0;

            {
                this.this$0 = r1;
                this.$key = r2;
                this.$response = r3;
            }

            @Override // java.lang.Runnable
            public final void run() {
                ControlViewHolder controlViewHolder = (ControlViewHolder) this.this$0.controlViewsById.get(this.$key);
                if (controlViewHolder != null) {
                    controlViewHolder.actionResponse(this.$response);
                }
            }
        });
    }

    private final ViewGroup createRow(LayoutInflater layoutInflater, ViewGroup viewGroup) {
        View inflate = layoutInflater.inflate(C0011R$layout.controls_row, viewGroup, false);
        if (inflate != null) {
            ViewGroup viewGroup2 = (ViewGroup) inflate;
            viewGroup.addView(viewGroup2);
            return viewGroup2;
        }
        throw new TypeCastException("null cannot be cast to non-null type android.view.ViewGroup");
    }

    /* JADX WARN: Type inference failed for: r4v1, types: [com.android.systemui.controls.ui.ItemAdapter, android.widget.ArrayAdapter, T] */
    /* JADX WARNING: Unknown variable types count: 1 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final void createDropDown(java.util.List<com.android.systemui.controls.ui.SelectionItem> r14) {
        /*
        // Method dump skipped, instructions count: 287
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.controls.ui.ControlsUiControllerImpl.createDropDown(java.util.List):void");
    }

    @Override // com.android.systemui.controls.ui.ControlsUiController
    public void onRefreshState(@NotNull ComponentName componentName, @NotNull List<Control> list) {
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        Intrinsics.checkParameterIsNotNull(list, "controls");
        for (Control control : list) {
            Map<ControlKey, ControlWithState> map = this.controlsById;
            String controlId = control.getControlId();
            Intrinsics.checkExpressionValueIsNotNull(controlId, "c.getControlId()");
            ControlWithState controlWithState = map.get(new ControlKey(componentName, controlId));
            if (controlWithState != null) {
                Log.d("ControlsUiController", "onRefreshState() for id: " + control.getControlId());
                ControlWithState controlWithState2 = new ControlWithState(componentName, controlWithState.getCi(), control);
                String controlId2 = control.getControlId();
                Intrinsics.checkExpressionValueIsNotNull(controlId2, "c.getControlId()");
                ControlKey controlKey = new ControlKey(componentName, controlId2);
                this.controlsById.put(controlKey, controlWithState2);
                this.uiExecutor.execute(new Runnable(controlKey, controlWithState2, control, this, componentName) { // from class: com.android.systemui.controls.ui.ControlsUiControllerImpl$onRefreshState$$inlined$forEach$lambda$1
                    final /* synthetic */ ControlWithState $cws;
                    final /* synthetic */ ControlKey $key;
                    final /* synthetic */ ControlsUiControllerImpl this$0;

                    {
                        this.$key = r1;
                        this.$cws = r2;
                        this.this$0 = r4;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        ControlViewHolder controlViewHolder = (ControlViewHolder) this.this$0.controlViewsById.get(this.$key);
                        if (controlViewHolder != null) {
                            controlViewHolder.bindData(this.$cws);
                        }
                    }
                });
            }
        }
    }
}
