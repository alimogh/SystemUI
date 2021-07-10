package com.android.systemui.controls.ui;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.service.controls.Control;
import android.service.controls.templates.ControlTemplate;
import android.service.controls.templates.TemperatureControlTemplate;
import android.view.View;
import com.android.systemui.C0008R$id;
import com.android.systemui.controls.ui.ControlViewHolder;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: TemperatureControlBehavior.kt */
public final class TemperatureControlBehavior implements Behavior {
    @NotNull
    public Drawable clipLayer;
    @NotNull
    public Control control;
    @NotNull
    public ControlViewHolder cvh;
    @Nullable
    private Behavior subBehavior;

    @NotNull
    public final Control getControl() {
        Control control = this.control;
        if (control != null) {
            return control;
        }
        Intrinsics.throwUninitializedPropertyAccessException("control");
        throw null;
    }

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
        Intrinsics.checkParameterIsNotNull(controlWithState, "cws");
        Control control = controlWithState.getControl();
        if (control != null) {
            this.control = control;
            ControlViewHolder controlViewHolder = this.cvh;
            if (controlViewHolder == null) {
                Intrinsics.throwUninitializedPropertyAccessException("cvh");
                throw null;
            } else if (control != null) {
                CharSequence statusText = control.getStatusText();
                Intrinsics.checkExpressionValueIsNotNull(statusText, "control.getStatusText()");
                int i2 = 0;
                ControlViewHolder.setStatusText$default(controlViewHolder, statusText, false, 2, null);
                ControlViewHolder controlViewHolder2 = this.cvh;
                if (controlViewHolder2 != null) {
                    Drawable background = controlViewHolder2.getLayout().getBackground();
                    if (background != null) {
                        Drawable findDrawableByLayerId = ((LayerDrawable) background).findDrawableByLayerId(C0008R$id.clip_layer);
                        Intrinsics.checkExpressionValueIsNotNull(findDrawableByLayerId, "ld.findDrawableByLayerId(R.id.clip_layer)");
                        this.clipLayer = findDrawableByLayerId;
                        Control control2 = this.control;
                        if (control2 != null) {
                            ControlTemplate controlTemplate = control2.getControlTemplate();
                            if (controlTemplate != null) {
                                TemperatureControlTemplate temperatureControlTemplate = (TemperatureControlTemplate) controlTemplate;
                                int currentActiveMode = temperatureControlTemplate.getCurrentActiveMode();
                                ControlTemplate template = temperatureControlTemplate.getTemplate();
                                if (Intrinsics.areEqual(template, ControlTemplate.getNoTemplateObject()) || Intrinsics.areEqual(template, ControlTemplate.getErrorTemplate())) {
                                    boolean z = (currentActiveMode == 0 || currentActiveMode == 1) ? false : true;
                                    Drawable drawable = this.clipLayer;
                                    if (drawable != null) {
                                        if (z) {
                                            i2 = 10000;
                                        }
                                        drawable.setLevel(i2);
                                        ControlViewHolder controlViewHolder3 = this.cvh;
                                        if (controlViewHolder3 != null) {
                                            ControlViewHolder.applyRenderInfo$packages__apps__OPSystemUI__android_common__OPSystemUI_core$default(controlViewHolder3, z, currentActiveMode, false, 4, null);
                                            ControlViewHolder controlViewHolder4 = this.cvh;
                                            if (controlViewHolder4 != null) {
                                                controlViewHolder4.getLayout().setOnClickListener(new View.OnClickListener(this, temperatureControlTemplate) { // from class: com.android.systemui.controls.ui.TemperatureControlBehavior$bind$1
                                                    final /* synthetic */ TemperatureControlTemplate $template;
                                                    final /* synthetic */ TemperatureControlBehavior this$0;

                                                    {
                                                        this.this$0 = r1;
                                                        this.$template = r2;
                                                    }

                                                    @Override // android.view.View.OnClickListener
                                                    public final void onClick(View view) {
                                                        ControlActionCoordinator controlActionCoordinator = this.this$0.getCvh().getControlActionCoordinator();
                                                        ControlViewHolder cvh = this.this$0.getCvh();
                                                        String templateId = this.$template.getTemplateId();
                                                        Intrinsics.checkExpressionValueIsNotNull(templateId, "template.getTemplateId()");
                                                        controlActionCoordinator.touch(cvh, templateId, this.this$0.getControl());
                                                    }
                                                });
                                            } else {
                                                Intrinsics.throwUninitializedPropertyAccessException("cvh");
                                                throw null;
                                            }
                                        } else {
                                            Intrinsics.throwUninitializedPropertyAccessException("cvh");
                                            throw null;
                                        }
                                    } else {
                                        Intrinsics.throwUninitializedPropertyAccessException("clipLayer");
                                        throw null;
                                    }
                                } else {
                                    ControlViewHolder controlViewHolder5 = this.cvh;
                                    if (controlViewHolder5 != null) {
                                        Behavior behavior = this.subBehavior;
                                        ControlViewHolder.Companion companion = ControlViewHolder.Companion;
                                        Control control3 = this.control;
                                        if (control3 != null) {
                                            int status = control3.getStatus();
                                            Intrinsics.checkExpressionValueIsNotNull(template, "subTemplate");
                                            Control control4 = this.control;
                                            if (control4 != null) {
                                                this.subBehavior = controlViewHolder5.bindBehavior(behavior, companion.findBehaviorClass(status, template, control4.getDeviceType()), currentActiveMode);
                                            } else {
                                                Intrinsics.throwUninitializedPropertyAccessException("control");
                                                throw null;
                                            }
                                        } else {
                                            Intrinsics.throwUninitializedPropertyAccessException("control");
                                            throw null;
                                        }
                                    } else {
                                        Intrinsics.throwUninitializedPropertyAccessException("cvh");
                                        throw null;
                                    }
                                }
                            } else {
                                throw new TypeCastException("null cannot be cast to non-null type android.service.controls.templates.TemperatureControlTemplate");
                            }
                        } else {
                            Intrinsics.throwUninitializedPropertyAccessException("control");
                            throw null;
                        }
                    } else {
                        throw new TypeCastException("null cannot be cast to non-null type android.graphics.drawable.LayerDrawable");
                    }
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("cvh");
                    throw null;
                }
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("control");
                throw null;
            }
        } else {
            Intrinsics.throwNpe();
            throw null;
        }
    }
}
