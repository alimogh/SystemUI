package com.android.systemui.controls.ui;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import com.android.systemui.globalactions.GlobalActionsPopupMenu;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Ref$ObjectRef;
import org.jetbrains.annotations.NotNull;
/* compiled from: ControlsUiControllerImpl.kt */
public final class ControlsUiControllerImpl$createMenu$1 implements View.OnClickListener {
    final /* synthetic */ Ref$ObjectRef $adapter;
    final /* synthetic */ ImageView $anchor;
    final /* synthetic */ ControlsUiControllerImpl this$0;

    ControlsUiControllerImpl$createMenu$1(ControlsUiControllerImpl controlsUiControllerImpl, ImageView imageView, Ref$ObjectRef ref$ObjectRef) {
        this.this$0 = controlsUiControllerImpl;
        this.$anchor = imageView;
        this.$adapter = ref$ObjectRef;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(@NotNull View view) {
        Intrinsics.checkParameterIsNotNull(view, "v");
        ControlsUiControllerImpl controlsUiControllerImpl = this.this$0;
        GlobalActionsPopupMenu globalActionsPopupMenu = new GlobalActionsPopupMenu(this.this$0.popupThemedContext, false);
        globalActionsPopupMenu.setAnchorView(this.$anchor);
        globalActionsPopupMenu.setAdapter(this.$adapter.element);
        globalActionsPopupMenu.setOnItemClickListener(new AdapterView.OnItemClickListener(globalActionsPopupMenu, this) { // from class: com.android.systemui.controls.ui.ControlsUiControllerImpl$createMenu$1$onClick$$inlined$apply$lambda$1
            final /* synthetic */ GlobalActionsPopupMenu $this_apply;
            final /* synthetic */ ControlsUiControllerImpl$createMenu$1 this$0;

            {
                this.$this_apply = r1;
                this.this$0 = r2;
            }

            @Override // android.widget.AdapterView.OnItemClickListener
            public void onItemClick(@NotNull AdapterView<?> adapterView, @NotNull View view2, int i, long j) {
                Intrinsics.checkParameterIsNotNull(adapterView, "parent");
                Intrinsics.checkParameterIsNotNull(view2, "view");
                if (i == 0) {
                    ControlsUiControllerImpl controlsUiControllerImpl2 = this.this$0.this$0;
                    Context context = view2.getContext();
                    Intrinsics.checkExpressionValueIsNotNull(context, "view.context");
                    controlsUiControllerImpl2.startFavoritingActivity(context, this.this$0.this$0.selectedStructure);
                } else if (i == 1) {
                    ControlsUiControllerImpl controlsUiControllerImpl3 = this.this$0.this$0;
                    Context context2 = view2.getContext();
                    Intrinsics.checkExpressionValueIsNotNull(context2, "view.context");
                    controlsUiControllerImpl3.startEditingActivity(context2, this.this$0.this$0.selectedStructure);
                }
                this.$this_apply.dismiss();
            }
        });
        globalActionsPopupMenu.show();
        controlsUiControllerImpl.popup = globalActionsPopupMenu;
    }
}
