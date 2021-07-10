package com.android.systemui.controls.controller;

import android.content.ComponentName;
import android.service.controls.Control;
import com.android.systemui.controls.ControlStatus;
import com.android.systemui.controls.controller.ControlsBindingController;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import kotlin.collections.CollectionsKt__IterablesKt;
import kotlin.collections.CollectionsKt__MutableCollectionsKt;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: ControlsControllerImpl.kt */
public final class ControlsControllerImpl$loadForComponent$2 implements ControlsBindingController.LoadCallback {
    final /* synthetic */ ComponentName $componentName;
    final /* synthetic */ Consumer $dataCallback;
    final /* synthetic */ ControlsControllerImpl this$0;

    ControlsControllerImpl$loadForComponent$2(ControlsControllerImpl controlsControllerImpl, ComponentName componentName, Consumer consumer) {
        this.this$0 = controlsControllerImpl;
        this.$componentName = componentName;
        this.$dataCallback = consumer;
    }

    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
    @Override // java.util.function.Consumer
    public /* bridge */ /* synthetic */ void accept(List<? extends Control> list) {
        accept((List<Control>) list);
    }

    public void accept(@NotNull List<Control> list) {
        Intrinsics.checkParameterIsNotNull(list, "controls");
        ControlsControllerImpl.access$getExecutor$p(this.this$0).execute(new Runnable(this, list) { // from class: com.android.systemui.controls.controller.ControlsControllerImpl$loadForComponent$2$accept$1
            final /* synthetic */ List $controls;
            final /* synthetic */ ControlsControllerImpl$loadForComponent$2 this$0;

            {
                this.this$0 = r1;
                this.$controls = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                List<ControlInfo> controlsForComponent = Favorites.INSTANCE.getControlsForComponent(this.this$0.$componentName);
                ArrayList arrayList = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(controlsForComponent, 10));
                for (ControlInfo controlInfo : controlsForComponent) {
                    arrayList.add(controlInfo.getControlId());
                }
                if (Favorites.INSTANCE.updateControls(this.this$0.$componentName, this.$controls)) {
                    this.this$0.this$0.persistenceWrapper.storeFavorites(Favorites.INSTANCE.getAllStructures());
                }
                Set set = this.this$0.this$0.findRemoved(CollectionsKt___CollectionsKt.toSet(arrayList), this.$controls);
                List<Control> list2 = this.$controls;
                ArrayList arrayList2 = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(list2, 10));
                for (Control control : list2) {
                    arrayList2.add(new ControlStatus(control, this.this$0.$componentName, arrayList.contains(control.getControlId()), false, 8, null));
                }
                ArrayList arrayList3 = new ArrayList();
                for (StructureInfo structureInfo : Favorites.INSTANCE.getStructuresForComponent(this.this$0.$componentName)) {
                    for (ControlInfo controlInfo2 : structureInfo.getControls()) {
                        if (set.contains(controlInfo2.getControlId())) {
                            ControlsControllerImpl$loadForComponent$2 controlsControllerImpl$loadForComponent$2 = this.this$0;
                            arrayList3.add(ControlsControllerImpl.createRemovedStatus$default(controlsControllerImpl$loadForComponent$2.this$0, controlsControllerImpl$loadForComponent$2.$componentName, controlInfo2, structureInfo.getStructure(), false, 8, null));
                        }
                    }
                }
                this.this$0.$dataCallback.accept(ControlsControllerKt.createLoadDataObject$default(CollectionsKt___CollectionsKt.plus((Collection) arrayList3, (Iterable) arrayList2), arrayList, false, 4, null));
            }
        });
    }

    @Override // com.android.systemui.controls.controller.ControlsBindingController.LoadCallback
    public void error(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "message");
        ControlsControllerImpl.access$getExecutor$p(this.this$0).execute(new Runnable(this) { // from class: com.android.systemui.controls.controller.ControlsControllerImpl$loadForComponent$2$error$1
            final /* synthetic */ ControlsControllerImpl$loadForComponent$2 this$0;

            {
                this.this$0 = r1;
            }

            @Override // java.lang.Runnable
            public final void run() {
                List<StructureInfo> structuresForComponent = Favorites.INSTANCE.getStructuresForComponent(this.this$0.$componentName);
                ArrayList<ControlStatus> arrayList = new ArrayList();
                for (StructureInfo structureInfo : structuresForComponent) {
                    List<ControlInfo> controls = structureInfo.getControls();
                    ArrayList arrayList2 = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(controls, 10));
                    for (ControlInfo controlInfo : controls) {
                        ControlsControllerImpl$loadForComponent$2 controlsControllerImpl$loadForComponent$2 = this.this$0;
                        arrayList2.add(controlsControllerImpl$loadForComponent$2.this$0.createRemovedStatus(controlsControllerImpl$loadForComponent$2.$componentName, controlInfo, structureInfo.getStructure(), false));
                    }
                    boolean unused = CollectionsKt__MutableCollectionsKt.addAll(arrayList, arrayList2);
                }
                ArrayList arrayList3 = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(arrayList, 10));
                for (ControlStatus controlStatus : arrayList) {
                    arrayList3.add(controlStatus.getControl().getControlId());
                }
                this.this$0.$dataCallback.accept(ControlsControllerKt.createLoadDataObject(arrayList, arrayList3, true));
            }
        });
    }
}
