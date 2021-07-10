package com.android.systemui.controls.ui;

import android.content.ComponentName;
import android.graphics.drawable.Drawable;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: ControlsUiControllerImpl.kt */
/* access modifiers changed from: package-private */
public final class SelectionItem {
    @NotNull
    private final CharSequence appName;
    @NotNull
    private final ComponentName componentName;
    @NotNull
    private final Drawable icon;
    @NotNull
    private final CharSequence structure;

    public static /* synthetic */ SelectionItem copy$default(SelectionItem selectionItem, CharSequence charSequence, CharSequence charSequence2, Drawable drawable, ComponentName componentName, int i, Object obj) {
        if ((i & 1) != 0) {
            charSequence = selectionItem.appName;
        }
        if ((i & 2) != 0) {
            charSequence2 = selectionItem.structure;
        }
        if ((i & 4) != 0) {
            drawable = selectionItem.icon;
        }
        if ((i & 8) != 0) {
            componentName = selectionItem.componentName;
        }
        return selectionItem.copy(charSequence, charSequence2, drawable, componentName);
    }

    @NotNull
    public final SelectionItem copy(@NotNull CharSequence charSequence, @NotNull CharSequence charSequence2, @NotNull Drawable drawable, @NotNull ComponentName componentName) {
        Intrinsics.checkParameterIsNotNull(charSequence, "appName");
        Intrinsics.checkParameterIsNotNull(charSequence2, "structure");
        Intrinsics.checkParameterIsNotNull(drawable, "icon");
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        return new SelectionItem(charSequence, charSequence2, drawable, componentName);
    }

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SelectionItem)) {
            return false;
        }
        SelectionItem selectionItem = (SelectionItem) obj;
        return Intrinsics.areEqual(this.appName, selectionItem.appName) && Intrinsics.areEqual(this.structure, selectionItem.structure) && Intrinsics.areEqual(this.icon, selectionItem.icon) && Intrinsics.areEqual(this.componentName, selectionItem.componentName);
    }

    public int hashCode() {
        CharSequence charSequence = this.appName;
        int i = 0;
        int hashCode = (charSequence != null ? charSequence.hashCode() : 0) * 31;
        CharSequence charSequence2 = this.structure;
        int hashCode2 = (hashCode + (charSequence2 != null ? charSequence2.hashCode() : 0)) * 31;
        Drawable drawable = this.icon;
        int hashCode3 = (hashCode2 + (drawable != null ? drawable.hashCode() : 0)) * 31;
        ComponentName componentName = this.componentName;
        if (componentName != null) {
            i = componentName.hashCode();
        }
        return hashCode3 + i;
    }

    @NotNull
    public String toString() {
        return "SelectionItem(appName=" + this.appName + ", structure=" + this.structure + ", icon=" + this.icon + ", componentName=" + this.componentName + ")";
    }

    public SelectionItem(@NotNull CharSequence charSequence, @NotNull CharSequence charSequence2, @NotNull Drawable drawable, @NotNull ComponentName componentName) {
        Intrinsics.checkParameterIsNotNull(charSequence, "appName");
        Intrinsics.checkParameterIsNotNull(charSequence2, "structure");
        Intrinsics.checkParameterIsNotNull(drawable, "icon");
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        this.appName = charSequence;
        this.structure = charSequence2;
        this.icon = drawable;
        this.componentName = componentName;
    }

    @NotNull
    public final CharSequence getStructure() {
        return this.structure;
    }

    @NotNull
    public final Drawable getIcon() {
        return this.icon;
    }

    @NotNull
    public final ComponentName getComponentName() {
        return this.componentName;
    }

    @NotNull
    public final CharSequence getTitle() {
        return this.structure.length() == 0 ? this.appName : this.structure;
    }
}
