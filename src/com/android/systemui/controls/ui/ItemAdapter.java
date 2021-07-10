package com.android.systemui.controls.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.systemui.C0008R$id;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: ControlsUiControllerImpl.kt */
final class ItemAdapter extends ArrayAdapter<SelectionItem> {
    private final LayoutInflater layoutInflater = LayoutInflater.from(getContext());
    private final int resource;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public ItemAdapter(@NotNull Context context, int i) {
        super(context, i);
        Intrinsics.checkParameterIsNotNull(context, "parentContext");
        this.resource = i;
    }

    @Override // android.widget.ArrayAdapter, android.widget.Adapter
    @NotNull
    public View getView(int i, @Nullable View view, @NotNull ViewGroup viewGroup) {
        Intrinsics.checkParameterIsNotNull(viewGroup, "parent");
        SelectionItem item = getItem(i);
        if (view == null) {
            view = this.layoutInflater.inflate(this.resource, viewGroup, false);
        }
        ((TextView) view.requireViewById(C0008R$id.controls_spinner_item)).setText(item.getTitle());
        ((ImageView) view.requireViewById(C0008R$id.app_icon)).setImageDrawable(item.getIcon());
        Intrinsics.checkExpressionValueIsNotNull(view, "view");
        return view;
    }
}
