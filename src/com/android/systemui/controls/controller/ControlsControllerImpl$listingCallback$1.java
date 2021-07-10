package com.android.systemui.controls.controller;

import android.content.ComponentName;
import android.content.SharedPreferences;
import android.util.Log;
import com.android.systemui.controls.ControlsServiceInfo;
import com.android.systemui.controls.management.ControlsListingController;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import kotlin.collections.CollectionsKt__IterablesKt;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: ControlsControllerImpl.kt */
public final class ControlsControllerImpl$listingCallback$1 implements ControlsListingController.ControlsListingCallback {
    final /* synthetic */ ControlsControllerImpl this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    ControlsControllerImpl$listingCallback$1(ControlsControllerImpl controlsControllerImpl) {
        this.this$0 = controlsControllerImpl;
    }

    @Override // com.android.systemui.controls.management.ControlsListingController.ControlsListingCallback
    public void onServicesUpdated(@NotNull List<ControlsServiceInfo> list) {
        Intrinsics.checkParameterIsNotNull(list, "serviceInfos");
        ControlsControllerImpl.access$getExecutor$p(this.this$0).execute(new Runnable(this, list) { // from class: com.android.systemui.controls.controller.ControlsControllerImpl$listingCallback$1$onServicesUpdated$1
            final /* synthetic */ List $serviceInfos;
            final /* synthetic */ ControlsControllerImpl$listingCallback$1 this$0;

            {
                this.this$0 = r1;
                this.$serviceInfos = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                List<ControlsServiceInfo> list2 = this.$serviceInfos;
                ArrayList arrayList = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(list2, 10));
                for (ControlsServiceInfo controlsServiceInfo : list2) {
                    arrayList.add(controlsServiceInfo.componentName);
                }
                Set<ComponentName> set = CollectionsKt___CollectionsKt.toSet(arrayList);
                List<StructureInfo> allStructures = Favorites.INSTANCE.getAllStructures();
                ArrayList arrayList2 = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(allStructures, 10));
                for (StructureInfo structureInfo : allStructures) {
                    arrayList2.add(structureInfo.getComponentName());
                }
                Set set2 = CollectionsKt___CollectionsKt.toSet(arrayList2);
                boolean z = false;
                SharedPreferences sharedPreferences = this.this$0.this$0.userStructure.getUserContext().getSharedPreferences("controls_prefs", 0);
                Set<String> stringSet = sharedPreferences.getStringSet("SeedingCompleted", new LinkedHashSet());
                ArrayList arrayList3 = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(set, 10));
                for (ComponentName componentName : set) {
                    Intrinsics.checkExpressionValueIsNotNull(componentName, "it");
                    arrayList3.add(componentName.getPackageName());
                }
                SharedPreferences.Editor edit = sharedPreferences.edit();
                Intrinsics.checkExpressionValueIsNotNull(stringSet, "completedSeedingPackageSet");
                edit.putStringSet("SeedingCompleted", CollectionsKt___CollectionsKt.intersect(stringSet, arrayList3)).apply();
                for (ComponentName componentName2 : CollectionsKt___CollectionsKt.subtract(set2, set)) {
                    Favorites favorites = Favorites.INSTANCE;
                    Intrinsics.checkExpressionValueIsNotNull(componentName2, "it");
                    favorites.removeStructures(componentName2);
                    this.this$0.this$0.bindingController.onComponentRemoved(componentName2);
                    z = true;
                }
                if (!this.this$0.this$0.getAuxiliaryPersistenceWrapper$packages__apps__OPSystemUI__android_common__OPSystemUI_core().getFavorites().isEmpty()) {
                    for (ComponentName componentName3 : CollectionsKt___CollectionsKt.subtract(set, set2)) {
                        AuxiliaryPersistenceWrapper auxiliaryPersistenceWrapper$packages__apps__OPSystemUI__android_common__OPSystemUI_core = this.this$0.this$0.getAuxiliaryPersistenceWrapper$packages__apps__OPSystemUI__android_common__OPSystemUI_core();
                        Intrinsics.checkExpressionValueIsNotNull(componentName3, "it");
                        List<StructureInfo> cachedFavoritesAndRemoveFor = auxiliaryPersistenceWrapper$packages__apps__OPSystemUI__android_common__OPSystemUI_core.getCachedFavoritesAndRemoveFor(componentName3);
                        if (!cachedFavoritesAndRemoveFor.isEmpty()) {
                            for (StructureInfo structureInfo2 : cachedFavoritesAndRemoveFor) {
                                Favorites.INSTANCE.replaceControls(structureInfo2);
                            }
                            z = true;
                        }
                    }
                    for (ComponentName componentName4 : CollectionsKt___CollectionsKt.intersect(set, set2)) {
                        AuxiliaryPersistenceWrapper auxiliaryPersistenceWrapper$packages__apps__OPSystemUI__android_common__OPSystemUI_core2 = this.this$0.this$0.getAuxiliaryPersistenceWrapper$packages__apps__OPSystemUI__android_common__OPSystemUI_core();
                        Intrinsics.checkExpressionValueIsNotNull(componentName4, "it");
                        auxiliaryPersistenceWrapper$packages__apps__OPSystemUI__android_common__OPSystemUI_core2.getCachedFavoritesAndRemoveFor(componentName4);
                    }
                }
                if (z) {
                    Log.d("ControlsControllerImpl", "Detected change in available services, storing updated favorites");
                    this.this$0.this$0.persistenceWrapper.storeFavorites(Favorites.INSTANCE.getAllStructures());
                }
            }
        });
    }
}
