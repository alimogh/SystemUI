package com.android.systemui.controls.controller;

import android.content.ComponentName;
import android.service.controls.Control;
import android.util.ArrayMap;
import android.util.Log;
import com.android.systemui.controls.controller.ControlsBindingController;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: ControlsControllerImpl.kt */
public final class ControlsControllerImpl$startSeeding$1 implements ControlsBindingController.LoadCallback {
    final /* synthetic */ Consumer $callback;
    final /* synthetic */ ComponentName $componentName;
    final /* synthetic */ boolean $didAnyFail;
    final /* synthetic */ List $remaining;
    final /* synthetic */ ControlsControllerImpl this$0;

    ControlsControllerImpl$startSeeding$1(ControlsControllerImpl controlsControllerImpl, ComponentName componentName, Consumer consumer, List list, boolean z) {
        this.this$0 = controlsControllerImpl;
        this.$componentName = componentName;
        this.$callback = consumer;
        this.$remaining = list;
        this.$didAnyFail = z;
    }

    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
    @Override // java.util.function.Consumer
    public /* bridge */ /* synthetic */ void accept(List<? extends Control> list) {
        accept((List<Control>) list);
    }

    public void accept(@NotNull List<Control> list) {
        Intrinsics.checkParameterIsNotNull(list, "controls");
        ControlsControllerImpl.access$getExecutor$p(this.this$0).execute(new Runnable(this, list) { // from class: com.android.systemui.controls.controller.ControlsControllerImpl$startSeeding$1$accept$1
            final /* synthetic */ List $controls;
            final /* synthetic */ ControlsControllerImpl$startSeeding$1 this$0;

            {
                this.this$0 = r1;
                this.$controls = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                ArrayMap arrayMap = new ArrayMap();
                for (Control control : this.$controls) {
                    CharSequence structure = control.getStructure();
                    if (structure == null) {
                        structure = "";
                    }
                    List list2 = (List) arrayMap.get(structure);
                    if (list2 == null) {
                        list2 = new ArrayList();
                    }
                    Intrinsics.checkExpressionValueIsNotNull(list2, "structureToControls.get(…ableListOf<ControlInfo>()");
                    if (list2.size() < 6) {
                        String controlId = control.getControlId();
                        Intrinsics.checkExpressionValueIsNotNull(controlId, "it.controlId");
                        CharSequence title = control.getTitle();
                        Intrinsics.checkExpressionValueIsNotNull(title, "it.title");
                        CharSequence subtitle = control.getSubtitle();
                        Intrinsics.checkExpressionValueIsNotNull(subtitle, "it.subtitle");
                        list2.add(new ControlInfo(controlId, title, subtitle, control.getDeviceType()));
                        arrayMap.put(structure, list2);
                    }
                }
                for (Map.Entry entry : arrayMap.entrySet()) {
                    CharSequence charSequence = (CharSequence) entry.getKey();
                    List list3 = (List) entry.getValue();
                    Favorites favorites = Favorites.INSTANCE;
                    ComponentName componentName = this.this$0.$componentName;
                    Intrinsics.checkExpressionValueIsNotNull(charSequence, "s");
                    Intrinsics.checkExpressionValueIsNotNull(list3, "cs");
                    favorites.replaceControls(new StructureInfo(componentName, charSequence, list3));
                }
                this.this$0.this$0.persistenceWrapper.storeFavorites(Favorites.INSTANCE.getAllStructures());
                ControlsControllerImpl$startSeeding$1 controlsControllerImpl$startSeeding$1 = this.this$0;
                Consumer consumer = controlsControllerImpl$startSeeding$1.$callback;
                String packageName = controlsControllerImpl$startSeeding$1.$componentName.getPackageName();
                Intrinsics.checkExpressionValueIsNotNull(packageName, "componentName.packageName");
                consumer.accept(new SeedResponse(packageName, true));
                ControlsControllerImpl$startSeeding$1 controlsControllerImpl$startSeeding$12 = this.this$0;
                controlsControllerImpl$startSeeding$12.this$0.startSeeding(controlsControllerImpl$startSeeding$12.$remaining, controlsControllerImpl$startSeeding$12.$callback, controlsControllerImpl$startSeeding$12.$didAnyFail);
            }
        });
    }

    @Override // com.android.systemui.controls.controller.ControlsBindingController.LoadCallback
    public void error(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "message");
        Log.e("ControlsControllerImpl", "Unable to seed favorites: " + str);
        ControlsControllerImpl.access$getExecutor$p(this.this$0).execute(new Runnable(this) { // from class: com.android.systemui.controls.controller.ControlsControllerImpl$startSeeding$1$error$1
            final /* synthetic */ ControlsControllerImpl$startSeeding$1 this$0;

            {
                this.this$0 = r1;
            }

            @Override // java.lang.Runnable
            public final void run() {
                ControlsControllerImpl$startSeeding$1 controlsControllerImpl$startSeeding$1 = this.this$0;
                Consumer consumer = controlsControllerImpl$startSeeding$1.$callback;
                String packageName = controlsControllerImpl$startSeeding$1.$componentName.getPackageName();
                Intrinsics.checkExpressionValueIsNotNull(packageName, "componentName.packageName");
                consumer.accept(new SeedResponse(packageName, false));
                ControlsControllerImpl$startSeeding$1 controlsControllerImpl$startSeeding$12 = this.this$0;
                controlsControllerImpl$startSeeding$12.this$0.startSeeding(controlsControllerImpl$startSeeding$12.$remaining, controlsControllerImpl$startSeeding$12.$callback, true);
            }
        });
    }
}
