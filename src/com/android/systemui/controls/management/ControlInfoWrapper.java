package com.android.systemui.controls.management;

import android.content.ComponentName;
import android.graphics.drawable.Icon;
import com.android.systemui.controls.ControlInterface;
import com.android.systemui.controls.controller.ControlInfo;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: ControlsModel.kt */
public final class ControlInfoWrapper extends ElementWrapper implements ControlInterface {
    @NotNull
    private final ComponentName component;
    @NotNull
    private final ControlInfo controlInfo;
    private boolean favorite;

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ControlInfoWrapper)) {
            return false;
        }
        ControlInfoWrapper controlInfoWrapper = (ControlInfoWrapper) obj;
        return Intrinsics.areEqual(getComponent(), controlInfoWrapper.getComponent()) && Intrinsics.areEqual(this.controlInfo, controlInfoWrapper.controlInfo) && getFavorite() == controlInfoWrapper.getFavorite();
    }

    @Override // com.android.systemui.controls.ControlInterface
    @Nullable
    public Icon getCustomIcon() {
        return null;
    }

    public int hashCode() {
        ComponentName component = getComponent();
        int i = 0;
        int hashCode = (component != null ? component.hashCode() : 0) * 31;
        ControlInfo controlInfo = this.controlInfo;
        if (controlInfo != null) {
            i = controlInfo.hashCode();
        }
        int i2 = (hashCode + i) * 31;
        boolean favorite = getFavorite();
        if (favorite) {
            favorite = true;
        }
        int i3 = favorite ? 1 : 0;
        int i4 = favorite ? 1 : 0;
        int i5 = favorite ? 1 : 0;
        return i2 + i3;
    }

    @NotNull
    public String toString() {
        return "ControlInfoWrapper(component=" + getComponent() + ", controlInfo=" + this.controlInfo + ", favorite=" + getFavorite() + ")";
    }

    @Override // com.android.systemui.controls.ControlInterface
    public boolean getRemoved() {
        return ControlInterface.DefaultImpls.getRemoved(this);
    }

    @Override // com.android.systemui.controls.ControlInterface
    @NotNull
    public ComponentName getComponent() {
        return this.component;
    }

    @NotNull
    public final ControlInfo getControlInfo() {
        return this.controlInfo;
    }

    @Override // com.android.systemui.controls.ControlInterface
    public boolean getFavorite() {
        return this.favorite;
    }

    public void setFavorite(boolean z) {
        this.favorite = z;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public ControlInfoWrapper(@NotNull ComponentName componentName, @NotNull ControlInfo controlInfo, boolean z) {
        super(null);
        Intrinsics.checkParameterIsNotNull(componentName, "component");
        Intrinsics.checkParameterIsNotNull(controlInfo, "controlInfo");
        this.component = componentName;
        this.controlInfo = controlInfo;
        this.favorite = z;
    }

    @Override // com.android.systemui.controls.ControlInterface
    @NotNull
    public String getControlId() {
        return this.controlInfo.getControlId();
    }

    @Override // com.android.systemui.controls.ControlInterface
    @NotNull
    public CharSequence getTitle() {
        return this.controlInfo.getControlTitle();
    }

    @Override // com.android.systemui.controls.ControlInterface
    @NotNull
    public CharSequence getSubtitle() {
        return this.controlInfo.getControlSubtitle();
    }

    @Override // com.android.systemui.controls.ControlInterface
    public int getDeviceType() {
        return this.controlInfo.getDeviceType();
    }
}
