package com.android.systemui.controls.ui;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import com.android.systemui.globalactions.GlobalActionsPopupMenu;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Ref$ObjectRef;
import org.jetbrains.annotations.NotNull;
/* compiled from: ControlsUiControllerImpl.kt */
public final class ControlsUiControllerImpl$createDropDown$3 implements View.OnClickListener {
    final /* synthetic */ Ref$ObjectRef $adapter;
    final /* synthetic */ ViewGroup $anchor;
    final /* synthetic */ ControlsUiControllerImpl this$0;

    ControlsUiControllerImpl$createDropDown$3(ControlsUiControllerImpl controlsUiControllerImpl, ViewGroup viewGroup, Ref$ObjectRef ref$ObjectRef) {
        this.this$0 = controlsUiControllerImpl;
        this.$anchor = viewGroup;
        this.$adapter = ref$ObjectRef;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(@NotNull View view) {
        Intrinsics.checkParameterIsNotNull(view, "v");
        ControlsUiControllerImpl controlsUiControllerImpl = this.this$0;
        GlobalActionsPopupMenu globalActionsPopupMenu = new GlobalActionsPopupMenu(this.this$0.popupThemedContext, true);
        globalActionsPopupMenu.setAnchorView(this.$anchor);
        globalActionsPopupMenu.setAdapter(this.$adapter.element);
        globalActionsPopupMenu.setOnItemClickListener(new AdapterView.OnItemClickListener(globalActionsPopupMenu, this) { // from class: com.android.systemui.controls.ui.ControlsUiControllerImpl$createDropDown$3$onClick$$inlined$apply$lambda$1
            final /* synthetic */ GlobalActionsPopupMenu $this_apply;
            final /* synthetic */ ControlsUiControllerImpl$createDropDown$3 this$0;

            {
                this.$this_apply = r1;
                this.this$0 = r2;
            }

            @Override // android.widget.AdapterView.OnItemClickListener
            public void onItemClick(@NotNull AdapterView<?> adapterView, @NotNull View view2, int i, long j) {
                Intrinsics.checkParameterIsNotNull(adapterView, "parent");
                Intrinsics.checkParameterIsNotNull(view2, "view");
                Object itemAtPosition = adapterView.getItemAtPosition(i);
                if (itemAtPosition != null) {
                    this.this$0.this$0.switchAppOrStructure((SelectionItem) itemAtPosition);
                    this.$this_apply.dismiss();
                    return;
                }
                throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.controls.ui.SelectionItem");
            }
        });
        globalActionsPopupMenu.show();
        controlsUiControllerImpl.popup = globalActionsPopupMenu;
    }
}
