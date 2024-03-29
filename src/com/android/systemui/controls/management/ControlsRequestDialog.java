package com.android.systemui.controls.management;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.service.controls.Control;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0011R$layout;
import com.android.systemui.C0015R$string;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.controls.controller.ControlInfo;
import com.android.systemui.controls.controller.ControlsController;
import com.android.systemui.controls.ui.RenderInfo;
import com.android.systemui.statusbar.phone.SystemUIDialog;
import com.android.systemui.util.LifecycleActivity;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: ControlsRequestDialog.kt */
public class ControlsRequestDialog extends LifecycleActivity implements DialogInterface.OnClickListener, DialogInterface.OnCancelListener {
    private final BroadcastDispatcher broadcastDispatcher;
    private final ControlsRequestDialog$callback$1 callback = new ControlsRequestDialog$callback$1();
    private Control control;
    private ComponentName controlComponent;
    private final ControlsController controller;
    private final ControlsListingController controlsListingController;
    private final ControlsRequestDialog$currentUserTracker$1 currentUserTracker = new ControlsRequestDialog$currentUserTracker$1(this, this.broadcastDispatcher);
    private Dialog dialog;

    public ControlsRequestDialog(@NotNull ControlsController controlsController, @NotNull BroadcastDispatcher broadcastDispatcher, @NotNull ControlsListingController controlsListingController) {
        Intrinsics.checkParameterIsNotNull(controlsController, "controller");
        Intrinsics.checkParameterIsNotNull(broadcastDispatcher, "broadcastDispatcher");
        Intrinsics.checkParameterIsNotNull(controlsListingController, "controlsListingController");
        this.controller = controlsController;
        this.broadcastDispatcher = broadcastDispatcher;
        this.controlsListingController = controlsListingController;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.util.LifecycleActivity, android.app.Activity
    public void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        if (!this.controller.getAvailable()) {
            Log.w("ControlsRequestDialog", "Quick Controls not available for this user ");
            finish();
        }
        this.currentUserTracker.startTracking();
        this.controlsListingController.addCallback(this.callback);
        int intExtra = getIntent().getIntExtra("android.intent.extra.USER_ID", -10000);
        int currentUserId = this.controller.getCurrentUserId();
        if (intExtra != currentUserId) {
            Log.w("ControlsRequestDialog", "Current user (" + currentUserId + ") different from request user (" + intExtra + ')');
            finish();
        }
        ComponentName componentName = (ComponentName) getIntent().getParcelableExtra("android.intent.extra.COMPONENT_NAME");
        if (componentName != null) {
            this.controlComponent = componentName;
            Control control = (Control) getIntent().getParcelableExtra("android.service.controls.extra.CONTROL");
            if (control != null) {
                this.control = control;
                return;
            }
            Log.e("ControlsRequestDialog", "Request did not contain control");
            finish();
            return;
        }
        Log.e("ControlsRequestDialog", "Request did not contain componentName");
        finish();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.util.LifecycleActivity, android.app.Activity
    public void onResume() {
        super.onResume();
        CharSequence verifyComponentAndGetLabel = verifyComponentAndGetLabel();
        if (verifyComponentAndGetLabel == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("The component specified (");
            ComponentName componentName = this.controlComponent;
            if (componentName != null) {
                sb.append(componentName.flattenToString());
                sb.append(' ');
                sb.append("is not a valid ControlsProviderService");
                Log.e("ControlsRequestDialog", sb.toString());
                finish();
                return;
            }
            Intrinsics.throwUninitializedPropertyAccessException("controlComponent");
            throw null;
        }
        if (isCurrentFavorite()) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("The control ");
            Control control = this.control;
            if (control != null) {
                sb2.append(control.getTitle());
                sb2.append(" is already a favorite");
                Log.w("ControlsRequestDialog", sb2.toString());
                finish();
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("control");
                throw null;
            }
        }
        Dialog createDialog = createDialog(verifyComponentAndGetLabel);
        this.dialog = createDialog;
        if (createDialog != null) {
            createDialog.show();
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.util.LifecycleActivity, android.app.Activity
    public void onDestroy() {
        Dialog dialog = this.dialog;
        if (dialog != null) {
            dialog.dismiss();
        }
        this.currentUserTracker.stopTracking();
        this.controlsListingController.removeCallback(this.callback);
        super.onDestroy();
    }

    private final CharSequence verifyComponentAndGetLabel() {
        ControlsListingController controlsListingController = this.controlsListingController;
        ComponentName componentName = this.controlComponent;
        if (componentName != null) {
            return controlsListingController.getAppLabel(componentName);
        }
        Intrinsics.throwUninitializedPropertyAccessException("controlComponent");
        throw null;
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x0065 A[SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final boolean isCurrentFavorite() {
        /*
            r7 = this;
            com.android.systemui.controls.controller.ControlsController r0 = r7.controller
            android.content.ComponentName r1 = r7.controlComponent
            r2 = 0
            if (r1 == 0) goto L_0x0066
            java.util.List r0 = r0.getFavoritesForComponent(r1)
            boolean r1 = r0 instanceof java.util.Collection
            r3 = 1
            r4 = 0
            if (r1 == 0) goto L_0x0019
            boolean r1 = r0.isEmpty()
            if (r1 == 0) goto L_0x0019
        L_0x0017:
            r3 = r4
            goto L_0x0065
        L_0x0019:
            java.util.Iterator r0 = r0.iterator()
        L_0x001d:
            boolean r1 = r0.hasNext()
            if (r1 == 0) goto L_0x0017
            java.lang.Object r1 = r0.next()
            com.android.systemui.controls.controller.StructureInfo r1 = (com.android.systemui.controls.controller.StructureInfo) r1
            java.util.List r1 = r1.getControls()
            boolean r5 = r1 instanceof java.util.Collection
            if (r5 == 0) goto L_0x0039
            boolean r5 = r1.isEmpty()
            if (r5 == 0) goto L_0x0039
        L_0x0037:
            r1 = r4
            goto L_0x0063
        L_0x0039:
            java.util.Iterator r1 = r1.iterator()
        L_0x003d:
            boolean r5 = r1.hasNext()
            if (r5 == 0) goto L_0x0037
            java.lang.Object r5 = r1.next()
            com.android.systemui.controls.controller.ControlInfo r5 = (com.android.systemui.controls.controller.ControlInfo) r5
            java.lang.String r5 = r5.getControlId()
            android.service.controls.Control r6 = r7.control
            if (r6 == 0) goto L_0x005d
            java.lang.String r6 = r6.getControlId()
            boolean r5 = kotlin.jvm.internal.Intrinsics.areEqual(r5, r6)
            if (r5 == 0) goto L_0x003d
            r1 = r3
            goto L_0x0063
        L_0x005d:
            java.lang.String r7 = "control"
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r7)
            throw r2
        L_0x0063:
            if (r1 == 0) goto L_0x001d
        L_0x0065:
            return r3
        L_0x0066:
            java.lang.String r7 = "controlComponent"
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r7)
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.controls.management.ControlsRequestDialog.isCurrentFavorite():boolean");
    }

    @NotNull
    public final Dialog createDialog(@NotNull CharSequence charSequence) {
        Intrinsics.checkParameterIsNotNull(charSequence, "label");
        RenderInfo.Companion companion = RenderInfo.Companion;
        ComponentName componentName = this.controlComponent;
        if (componentName != null) {
            Control control = this.control;
            if (control != null) {
                RenderInfo lookup$default = RenderInfo.Companion.lookup$default(companion, this, componentName, control.getDeviceType(), 0, 8, null);
                View inflate = LayoutInflater.from(this).inflate(C0011R$layout.controls_dialog, (ViewGroup) null);
                ImageView imageView = (ImageView) inflate.requireViewById(C0008R$id.icon);
                imageView.setImageDrawable(lookup$default.getIcon());
                Context context = imageView.getContext();
                Intrinsics.checkExpressionValueIsNotNull(context, "context");
                Resources resources = context.getResources();
                int foreground = lookup$default.getForeground();
                Context context2 = imageView.getContext();
                Intrinsics.checkExpressionValueIsNotNull(context2, "context");
                imageView.setImageTintList(resources.getColorStateList(foreground, context2.getTheme()));
                View requireViewById = inflate.requireViewById(C0008R$id.title);
                Intrinsics.checkExpressionValueIsNotNull(requireViewById, "requireViewById<TextView>(R.id.title)");
                TextView textView = (TextView) requireViewById;
                Control control2 = this.control;
                if (control2 != null) {
                    textView.setText(control2.getTitle());
                    View requireViewById2 = inflate.requireViewById(C0008R$id.subtitle);
                    Intrinsics.checkExpressionValueIsNotNull(requireViewById2, "requireViewById<TextView>(R.id.subtitle)");
                    TextView textView2 = (TextView) requireViewById2;
                    Control control3 = this.control;
                    if (control3 != null) {
                        textView2.setText(control3.getSubtitle());
                        View requireViewById3 = inflate.requireViewById(C0008R$id.control);
                        Intrinsics.checkExpressionValueIsNotNull(requireViewById3, "requireViewById<View>(R.id.control)");
                        requireViewById3.setElevation(inflate.getResources().getFloat(C0005R$dimen.control_card_elevation));
                        AlertDialog create = new AlertDialog.Builder(this).setTitle(getString(C0015R$string.controls_dialog_title)).setMessage(getString(C0015R$string.controls_dialog_message, new Object[]{charSequence})).setPositiveButton(C0015R$string.controls_dialog_ok, this).setNegativeButton(17039360, this).setOnCancelListener(this).setView(inflate).create();
                        SystemUIDialog.registerDismissListener(create);
                        create.setCanceledOnTouchOutside(true);
                        Intrinsics.checkExpressionValueIsNotNull(create, "dialog");
                        return create;
                    }
                    Intrinsics.throwUninitializedPropertyAccessException("control");
                    throw null;
                }
                Intrinsics.throwUninitializedPropertyAccessException("control");
                throw null;
            }
            Intrinsics.throwUninitializedPropertyAccessException("control");
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("controlComponent");
        throw null;
    }

    @Override // android.content.DialogInterface.OnCancelListener
    public void onCancel(@Nullable DialogInterface dialogInterface) {
        finish();
    }

    @Override // android.content.DialogInterface.OnClickListener
    public void onClick(@Nullable DialogInterface dialogInterface, int i) {
        if (i == -1) {
            ControlsController controlsController = this.controller;
            ComponentName componentName = this.controlComponent;
            if (componentName != null) {
                Control control = this.control;
                if (control != null) {
                    CharSequence structure = control.getStructure();
                    if (structure == null) {
                        structure = "";
                    }
                    Control control2 = this.control;
                    if (control2 != null) {
                        String controlId = control2.getControlId();
                        Intrinsics.checkExpressionValueIsNotNull(controlId, "control.controlId");
                        Control control3 = this.control;
                        if (control3 != null) {
                            CharSequence title = control3.getTitle();
                            Intrinsics.checkExpressionValueIsNotNull(title, "control.title");
                            Control control4 = this.control;
                            if (control4 != null) {
                                CharSequence subtitle = control4.getSubtitle();
                                Intrinsics.checkExpressionValueIsNotNull(subtitle, "control.subtitle");
                                Control control5 = this.control;
                                if (control5 != null) {
                                    controlsController.addFavorite(componentName, structure, new ControlInfo(controlId, title, subtitle, control5.getDeviceType()));
                                } else {
                                    Intrinsics.throwUninitializedPropertyAccessException("control");
                                    throw null;
                                }
                            } else {
                                Intrinsics.throwUninitializedPropertyAccessException("control");
                                throw null;
                            }
                        } else {
                            Intrinsics.throwUninitializedPropertyAccessException("control");
                            throw null;
                        }
                    } else {
                        Intrinsics.throwUninitializedPropertyAccessException("control");
                        throw null;
                    }
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("control");
                    throw null;
                }
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("controlComponent");
                throw null;
            }
        }
        finish();
    }
}
