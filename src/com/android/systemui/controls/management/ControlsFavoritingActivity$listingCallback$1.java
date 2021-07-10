package com.android.systemui.controls.management;

import com.android.systemui.controls.ControlsServiceInfo;
import com.android.systemui.controls.management.ControlsListingController;
import java.util.List;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: ControlsFavoritingActivity.kt */
public final class ControlsFavoritingActivity$listingCallback$1 implements ControlsListingController.ControlsListingCallback {
    final /* synthetic */ ControlsFavoritingActivity this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    ControlsFavoritingActivity$listingCallback$1(ControlsFavoritingActivity controlsFavoritingActivity) {
        this.this$0 = controlsFavoritingActivity;
    }

    @Override // com.android.systemui.controls.management.ControlsListingController.ControlsListingCallback
    public void onServicesUpdated(@NotNull List<ControlsServiceInfo> list) {
        Intrinsics.checkParameterIsNotNull(list, "serviceInfos");
        if (list.size() > 1) {
            ControlsFavoritingActivity.access$getOtherAppsButton$p(this.this$0).post(new Runnable(this) { // from class: com.android.systemui.controls.management.ControlsFavoritingActivity$listingCallback$1$onServicesUpdated$1
                final /* synthetic */ ControlsFavoritingActivity$listingCallback$1 this$0;

                {
                    this.this$0 = r1;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    ControlsFavoritingActivity.access$getOtherAppsButton$p(this.this$0.this$0).setVisibility(0);
                }
            });
        }
    }
}
