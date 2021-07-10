package com.android.systemui.controls.ui;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.service.controls.Control;
import android.view.View;
import com.android.systemui.C0015R$string;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: StatusBehavior.kt */
public final class StatusBehavior implements Behavior {
    @NotNull
    public ControlViewHolder cvh;

    @NotNull
    public final ControlViewHolder getCvh() {
        ControlViewHolder controlViewHolder = this.cvh;
        if (controlViewHolder != null) {
            return controlViewHolder;
        }
        Intrinsics.throwUninitializedPropertyAccessException("cvh");
        throw null;
    }

    @Override // com.android.systemui.controls.ui.Behavior
    public void initialize(@NotNull ControlViewHolder controlViewHolder) {
        Intrinsics.checkParameterIsNotNull(controlViewHolder, "cvh");
        this.cvh = controlViewHolder;
    }

    @Override // com.android.systemui.controls.ui.Behavior
    public void bind(@NotNull ControlWithState controlWithState, int i) {
        int i2;
        Intrinsics.checkParameterIsNotNull(controlWithState, "cws");
        Control control = controlWithState.getControl();
        int status = control != null ? control.getStatus() : 0;
        if (status == 2) {
            ControlViewHolder controlViewHolder = this.cvh;
            if (controlViewHolder != null) {
                controlViewHolder.getLayout().setOnClickListener(new View.OnClickListener(this, controlWithState) { // from class: com.android.systemui.controls.ui.StatusBehavior$bind$msg$1
                    final /* synthetic */ ControlWithState $cws;
                    final /* synthetic */ StatusBehavior this$0;

                    {
                        this.this$0 = r1;
                        this.$cws = r2;
                    }

                    @Override // android.view.View.OnClickListener
                    public final void onClick(View view) {
                        StatusBehavior statusBehavior = this.this$0;
                        statusBehavior.showNotFoundDialog(statusBehavior.getCvh(), this.$cws);
                    }
                });
                ControlViewHolder controlViewHolder2 = this.cvh;
                if (controlViewHolder2 != null) {
                    controlViewHolder2.getLayout().setOnLongClickListener(new View.OnLongClickListener(this, controlWithState) { // from class: com.android.systemui.controls.ui.StatusBehavior$bind$msg$2
                        final /* synthetic */ ControlWithState $cws;
                        final /* synthetic */ StatusBehavior this$0;

                        {
                            this.this$0 = r1;
                            this.$cws = r2;
                        }

                        @Override // android.view.View.OnLongClickListener
                        public final boolean onLongClick(View view) {
                            StatusBehavior statusBehavior = this.this$0;
                            statusBehavior.showNotFoundDialog(statusBehavior.getCvh(), this.$cws);
                            return true;
                        }
                    });
                    i2 = C0015R$string.controls_error_removed;
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("cvh");
                    throw null;
                }
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("cvh");
                throw null;
            }
        } else if (status == 3) {
            i2 = C0015R$string.controls_error_generic;
        } else if (status != 4) {
            ControlViewHolder controlViewHolder3 = this.cvh;
            if (controlViewHolder3 != null) {
                controlViewHolder3.setLoading(true);
                i2 = 17040441;
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("cvh");
                throw null;
            }
        } else {
            i2 = C0015R$string.controls_error_timeout;
        }
        ControlViewHolder controlViewHolder4 = this.cvh;
        if (controlViewHolder4 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("cvh");
            throw null;
        } else if (controlViewHolder4 != null) {
            String string = controlViewHolder4.getContext().getString(i2);
            Intrinsics.checkExpressionValueIsNotNull(string, "cvh.context.getString(msg)");
            ControlViewHolder.setStatusText$default(controlViewHolder4, string, false, 2, null);
            ControlViewHolder controlViewHolder5 = this.cvh;
            if (controlViewHolder5 != null) {
                ControlViewHolder.applyRenderInfo$packages__apps__OPSystemUI__android_common__OPSystemUI_core$default(controlViewHolder5, false, i, false, 4, null);
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("cvh");
                throw null;
            }
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("cvh");
            throw null;
        }
    }

    /* access modifiers changed from: private */
    public final void showNotFoundDialog(ControlViewHolder controlViewHolder, ControlWithState controlWithState) {
        PackageManager packageManager = controlViewHolder.getContext().getPackageManager();
        CharSequence applicationLabel = packageManager.getApplicationLabel(packageManager.getApplicationInfo(controlWithState.getComponentName().getPackageName(), 128));
        AlertDialog.Builder builder = new AlertDialog.Builder(controlViewHolder.getContext(), 16974545);
        Resources resources = controlViewHolder.getContext().getResources();
        builder.setTitle(resources.getString(C0015R$string.controls_error_removed_title));
        builder.setMessage(resources.getString(C0015R$string.controls_error_removed_message, controlViewHolder.getTitle().getText(), applicationLabel));
        builder.setPositiveButton(C0015R$string.controls_open_app, new DialogInterface.OnClickListener(builder, controlViewHolder, applicationLabel, controlWithState) { // from class: com.android.systemui.controls.ui.StatusBehavior$showNotFoundDialog$$inlined$apply$lambda$1
            final /* synthetic */ ControlViewHolder $cvh$inlined;
            final /* synthetic */ ControlWithState $cws$inlined;
            final /* synthetic */ AlertDialog.Builder $this_apply;

            {
                this.$this_apply = r1;
                this.$cvh$inlined = r2;
                this.$cws$inlined = r4;
            }

            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                PendingIntent appIntent;
                try {
                    Control control = this.$cws$inlined.getControl();
                    if (!(control == null || (appIntent = control.getAppIntent()) == null)) {
                        appIntent.send();
                    }
                    this.$this_apply.getContext().sendBroadcast(new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
                } catch (PendingIntent.CanceledException unused) {
                    this.$cvh$inlined.setErrorStatus();
                }
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton(17039360, StatusBehavior$showNotFoundDialog$builder$1$2.INSTANCE);
        AlertDialog create = builder.create();
        create.getWindow().setType(2020);
        create.show();
        controlViewHolder.setVisibleDialog(create);
    }
}
