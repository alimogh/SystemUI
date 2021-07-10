package com.android.systemui.controls.controller;

import com.android.systemui.controls.ControlStatus;
import com.android.systemui.controls.controller.ControlsController;
import java.util.List;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: ControlsController.kt */
public final class ControlsControllerKt {
    public static /* synthetic */ ControlsController.LoadData createLoadDataObject$default(List list, List list2, boolean z, int i, Object obj) {
        if ((i & 4) != 0) {
            z = false;
        }
        return createLoadDataObject(list, list2, z);
    }

    @NotNull
    public static final ControlsController.LoadData createLoadDataObject(@NotNull List<ControlStatus> list, @NotNull List<String> list2, boolean z) {
        Intrinsics.checkParameterIsNotNull(list, "allControls");
        Intrinsics.checkParameterIsNotNull(list2, "favorites");
        return new ControlsController.LoadData(list, list2, z) { // from class: com.android.systemui.controls.controller.ControlsControllerKt$createLoadDataObject$1
            @NotNull
            private final List<ControlStatus> allControls;
            private final boolean errorOnLoad;
            @NotNull
            private final List<String> favoritesIds;

            {
                this.allControls = r1;
                this.favoritesIds = r2;
                this.errorOnLoad = r3;
            }

            @Override // com.android.systemui.controls.controller.ControlsController.LoadData
            @NotNull
            public List<ControlStatus> getAllControls() {
                return this.allControls;
            }

            @Override // com.android.systemui.controls.controller.ControlsController.LoadData
            @NotNull
            public List<String> getFavoritesIds() {
                return this.favoritesIds;
            }

            @Override // com.android.systemui.controls.controller.ControlsController.LoadData
            public boolean getErrorOnLoad() {
                return this.errorOnLoad;
            }
        };
    }
}
