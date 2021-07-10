package com.android.systemui.controls.ui;

import android.content.ComponentName;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: ControlsUiControllerImpl.kt */
/* access modifiers changed from: package-private */
public final class ControlKey {
    @NotNull
    private final ComponentName componentName;
    @NotNull
    private final String controlId;

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ControlKey)) {
            return false;
        }
        ControlKey controlKey = (ControlKey) obj;
        return Intrinsics.areEqual(this.componentName, controlKey.componentName) && Intrinsics.areEqual(this.controlId, controlKey.controlId);
    }

    public int hashCode() {
        ComponentName componentName = this.componentName;
        int i = 0;
        int hashCode = (componentName != null ? componentName.hashCode() : 0) * 31;
        String str = this.controlId;
        if (str != null) {
            i = str.hashCode();
        }
        return hashCode + i;
    }

    @NotNull
    public String toString() {
        return "ControlKey(componentName=" + this.componentName + ", controlId=" + this.controlId + ")";
    }

    public ControlKey(@NotNull ComponentName componentName, @NotNull String str) {
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        Intrinsics.checkParameterIsNotNull(str, "controlId");
        this.componentName = componentName;
        this.controlId = str;
    }
}
