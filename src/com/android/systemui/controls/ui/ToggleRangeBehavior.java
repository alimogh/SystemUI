package com.android.systemui.controls.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.service.controls.Control;
import android.service.controls.templates.ControlTemplate;
import android.service.controls.templates.RangeTemplate;
import android.service.controls.templates.TemperatureControlTemplate;
import android.service.controls.templates.ToggleRangeTemplate;
import android.util.Log;
import android.util.MathUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0008R$id;
import com.android.systemui.Interpolators;
import com.android.systemui.controls.ui.ToggleRangeBehavior;
import java.util.Arrays;
import java.util.IllegalFormatException;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.StringCompanionObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: ToggleRangeBehavior.kt */
public final class ToggleRangeBehavior implements Behavior {
    @NotNull
    public Drawable clipLayer;
    private int colorOffset;
    @NotNull
    public Context context;
    @NotNull
    public Control control;
    @NotNull
    private String currentRangeValue = "";
    @NotNull
    private CharSequence currentStatusText = "";
    @NotNull
    public ControlViewHolder cvh;
    private boolean isChecked;
    private boolean isToggleable;
    private ValueAnimator rangeAnimator;
    @NotNull
    public RangeTemplate rangeTemplate;
    @NotNull
    public String templateId;

    @NotNull
    public final Drawable getClipLayer() {
        Drawable drawable = this.clipLayer;
        if (drawable != null) {
            return drawable;
        }
        Intrinsics.throwUninitializedPropertyAccessException("clipLayer");
        throw null;
    }

    @NotNull
    public final String getTemplateId() {
        String str = this.templateId;
        if (str != null) {
            return str;
        }
        Intrinsics.throwUninitializedPropertyAccessException("templateId");
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

    @NotNull
    public final RangeTemplate getRangeTemplate() {
        RangeTemplate rangeTemplate = this.rangeTemplate;
        if (rangeTemplate != null) {
            return rangeTemplate;
        }
        Intrinsics.throwUninitializedPropertyAccessException("rangeTemplate");
        throw null;
    }

    public final boolean isChecked() {
        return this.isChecked;
    }

    public final boolean isToggleable() {
        return this.isToggleable;
    }

    @Override // com.android.systemui.controls.ui.Behavior
    public void initialize(@NotNull ControlViewHolder controlViewHolder) {
        Intrinsics.checkParameterIsNotNull(controlViewHolder, "cvh");
        this.cvh = controlViewHolder;
        this.context = controlViewHolder.getContext();
        ToggleRangeGestureListener toggleRangeGestureListener = new ToggleRangeGestureListener(this, controlViewHolder.getLayout());
        Context context = this.context;
        if (context != null) {
            controlViewHolder.getLayout().setOnTouchListener(new View.OnTouchListener(this, new GestureDetector(context, toggleRangeGestureListener), toggleRangeGestureListener) { // from class: com.android.systemui.controls.ui.ToggleRangeBehavior$initialize$1
                final /* synthetic */ GestureDetector $gestureDetector;
                final /* synthetic */ ToggleRangeBehavior.ToggleRangeGestureListener $gestureListener;
                final /* synthetic */ ToggleRangeBehavior this$0;

                {
                    this.this$0 = r1;
                    this.$gestureDetector = r2;
                    this.$gestureListener = r3;
                }

                @Override // android.view.View.OnTouchListener
                public final boolean onTouch(@NotNull View view, @NotNull MotionEvent motionEvent) {
                    Intrinsics.checkParameterIsNotNull(view, "v");
                    Intrinsics.checkParameterIsNotNull(motionEvent, "e");
                    if (!this.$gestureDetector.onTouchEvent(motionEvent) && motionEvent.getAction() == 1 && this.$gestureListener.isDragging()) {
                        view.getParent().requestDisallowInterceptTouchEvent(false);
                        this.$gestureListener.setDragging(false);
                        this.this$0.endUpdateRange();
                    }
                    return false;
                }
            });
            return;
        }
        Intrinsics.throwUninitializedPropertyAccessException("context");
        throw null;
    }

    private final void setup(ToggleRangeTemplate toggleRangeTemplate) {
        RangeTemplate range = toggleRangeTemplate.getRange();
        Intrinsics.checkExpressionValueIsNotNull(range, "template.getRange()");
        this.rangeTemplate = range;
        this.isToggleable = true;
        this.isChecked = toggleRangeTemplate.isChecked();
    }

    private final void setup(RangeTemplate rangeTemplate) {
        this.rangeTemplate = rangeTemplate;
        if (rangeTemplate != null) {
            float currentValue = rangeTemplate.getCurrentValue();
            RangeTemplate rangeTemplate2 = this.rangeTemplate;
            if (rangeTemplate2 != null) {
                this.isChecked = currentValue != rangeTemplate2.getMinValue();
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("rangeTemplate");
                throw null;
            }
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("rangeTemplate");
            throw null;
        }
    }

    private final boolean setupTemplate(ControlTemplate controlTemplate) {
        if (controlTemplate instanceof ToggleRangeTemplate) {
            setup((ToggleRangeTemplate) controlTemplate);
            return true;
        } else if (controlTemplate instanceof RangeTemplate) {
            setup((RangeTemplate) controlTemplate);
            return true;
        } else if (controlTemplate instanceof TemperatureControlTemplate) {
            ControlTemplate template = ((TemperatureControlTemplate) controlTemplate).getTemplate();
            Intrinsics.checkExpressionValueIsNotNull(template, "template.getTemplate()");
            return setupTemplate(template);
        } else {
            Log.e("ControlsUiController", "Unsupported template type: " + controlTemplate);
            return false;
        }
    }

    @Override // com.android.systemui.controls.ui.Behavior
    public void bind(@NotNull ControlWithState controlWithState, int i) {
        Intrinsics.checkParameterIsNotNull(controlWithState, "cws");
        Control control = controlWithState.getControl();
        if (control != null) {
            this.control = control;
            this.colorOffset = i;
            if (control != null) {
                CharSequence statusText = control.getStatusText();
                Intrinsics.checkExpressionValueIsNotNull(statusText, "control.getStatusText()");
                this.currentStatusText = statusText;
                ControlViewHolder controlViewHolder = this.cvh;
                if (controlViewHolder != null) {
                    controlViewHolder.getLayout().setOnLongClickListener(null);
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
                                Intrinsics.checkExpressionValueIsNotNull(controlTemplate, "template");
                                if (setupTemplate(controlTemplate)) {
                                    String templateId = controlTemplate.getTemplateId();
                                    Intrinsics.checkExpressionValueIsNotNull(templateId, "template.getTemplateId()");
                                    this.templateId = templateId;
                                    RangeTemplate rangeTemplate = this.rangeTemplate;
                                    if (rangeTemplate != null) {
                                        updateRange(rangeToLevelValue(rangeTemplate.getCurrentValue()), this.isChecked, false);
                                        ControlViewHolder controlViewHolder3 = this.cvh;
                                        if (controlViewHolder3 != null) {
                                            ControlViewHolder.applyRenderInfo$packages__apps__OPSystemUI__android_common__OPSystemUI_core$default(controlViewHolder3, this.isChecked, i, false, 4, null);
                                            ControlViewHolder controlViewHolder4 = this.cvh;
                                            if (controlViewHolder4 != null) {
                                                controlViewHolder4.getLayout().setAccessibilityDelegate(new View.AccessibilityDelegate(this) { // from class: com.android.systemui.controls.ui.ToggleRangeBehavior$bind$1
                                                    final /* synthetic */ ToggleRangeBehavior this$0;

                                                    @Override // android.view.View.AccessibilityDelegate
                                                    public boolean onRequestSendAccessibilityEvent(@NotNull ViewGroup viewGroup, @NotNull View view, @NotNull AccessibilityEvent accessibilityEvent) {
                                                        Intrinsics.checkParameterIsNotNull(viewGroup, "host");
                                                        Intrinsics.checkParameterIsNotNull(view, "child");
                                                        Intrinsics.checkParameterIsNotNull(accessibilityEvent, "event");
                                                        return true;
                                                    }

                                                    /* JADX WARN: Incorrect args count in method signature: ()V */
                                                    {
                                                        this.this$0 = r1;
                                                    }

                                                    @Override // android.view.View.AccessibilityDelegate
                                                    public void onInitializeAccessibilityNodeInfo(@NotNull View view, @NotNull AccessibilityNodeInfo accessibilityNodeInfo) {
                                                        Intrinsics.checkParameterIsNotNull(view, "host");
                                                        Intrinsics.checkParameterIsNotNull(accessibilityNodeInfo, "info");
                                                        super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfo);
                                                        int i2 = 0;
                                                        float f = this.this$0.levelToRangeValue(0);
                                                        ToggleRangeBehavior toggleRangeBehavior = this.this$0;
                                                        float f2 = toggleRangeBehavior.levelToRangeValue(toggleRangeBehavior.getClipLayer().getLevel());
                                                        float f3 = this.this$0.levelToRangeValue(10000);
                                                        double stepValue = (double) this.this$0.getRangeTemplate().getStepValue();
                                                        if (stepValue != Math.floor(stepValue)) {
                                                            i2 = 1;
                                                        }
                                                        if (this.this$0.isChecked()) {
                                                            accessibilityNodeInfo.setRangeInfo(AccessibilityNodeInfo.RangeInfo.obtain(i2, f, f3, f2));
                                                        }
                                                        accessibilityNodeInfo.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SET_PROGRESS);
                                                    }

                                                    /* JADX WARNING: Removed duplicated region for block: B:17:0x007c  */
                                                    @Override // android.view.View.AccessibilityDelegate
                                                    /* Code decompiled incorrectly, please refer to instructions dump. */
                                                    public boolean performAccessibilityAction(@org.jetbrains.annotations.NotNull android.view.View r7, int r8, @org.jetbrains.annotations.Nullable android.os.Bundle r9) {
                                                        /*
                                                            r6 = this;
                                                            java.lang.String r0 = "host"
                                                            kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(r7, r0)
                                                            r0 = 0
                                                            r1 = 1
                                                            r2 = 16
                                                            if (r8 != r2) goto L_0x0035
                                                            com.android.systemui.controls.ui.ToggleRangeBehavior r2 = r6.this$0
                                                            boolean r2 = r2.isToggleable()
                                                            if (r2 != 0) goto L_0x0015
                                                        L_0x0013:
                                                            r2 = r0
                                                            goto L_0x007a
                                                        L_0x0015:
                                                            com.android.systemui.controls.ui.ToggleRangeBehavior r2 = r6.this$0
                                                            com.android.systemui.controls.ui.ControlViewHolder r2 = r2.getCvh()
                                                            com.android.systemui.controls.ui.ControlActionCoordinator r2 = r2.getControlActionCoordinator()
                                                            com.android.systemui.controls.ui.ToggleRangeBehavior r3 = r6.this$0
                                                            com.android.systemui.controls.ui.ControlViewHolder r3 = r3.getCvh()
                                                            com.android.systemui.controls.ui.ToggleRangeBehavior r4 = r6.this$0
                                                            java.lang.String r4 = r4.getTemplateId()
                                                            com.android.systemui.controls.ui.ToggleRangeBehavior r5 = r6.this$0
                                                            boolean r5 = r5.isChecked()
                                                            r2.toggle(r3, r4, r5)
                                                            goto L_0x004c
                                                        L_0x0035:
                                                            r2 = 32
                                                            if (r8 != r2) goto L_0x004e
                                                            com.android.systemui.controls.ui.ToggleRangeBehavior r2 = r6.this$0
                                                            com.android.systemui.controls.ui.ControlViewHolder r2 = r2.getCvh()
                                                            com.android.systemui.controls.ui.ControlActionCoordinator r2 = r2.getControlActionCoordinator()
                                                            com.android.systemui.controls.ui.ToggleRangeBehavior r3 = r6.this$0
                                                            com.android.systemui.controls.ui.ControlViewHolder r3 = r3.getCvh()
                                                            r2.longPress(r3)
                                                        L_0x004c:
                                                            r2 = r1
                                                            goto L_0x007a
                                                        L_0x004e:
                                                            android.view.accessibility.AccessibilityNodeInfo$AccessibilityAction r2 = android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction.ACTION_SET_PROGRESS
                                                            int r2 = r2.getId()
                                                            if (r8 != r2) goto L_0x0013
                                                            if (r9 == 0) goto L_0x0013
                                                            java.lang.String r2 = "android.view.accessibility.action.ARGUMENT_PROGRESS_VALUE"
                                                            boolean r3 = r9.containsKey(r2)
                                                            if (r3 != 0) goto L_0x0061
                                                            goto L_0x0013
                                                        L_0x0061:
                                                            float r2 = r9.getFloat(r2)
                                                            com.android.systemui.controls.ui.ToggleRangeBehavior r3 = r6.this$0
                                                            int r2 = com.android.systemui.controls.ui.ToggleRangeBehavior.access$rangeToLevelValue(r3, r2)
                                                            com.android.systemui.controls.ui.ToggleRangeBehavior r3 = r6.this$0
                                                            boolean r4 = r3.isChecked()
                                                            r3.updateRange(r2, r4, r1)
                                                            com.android.systemui.controls.ui.ToggleRangeBehavior r2 = r6.this$0
                                                            r2.endUpdateRange()
                                                            goto L_0x004c
                                                        L_0x007a:
                                                            if (r2 != 0) goto L_0x0082
                                                            boolean r6 = super.performAccessibilityAction(r7, r8, r9)
                                                            if (r6 == 0) goto L_0x0083
                                                        L_0x0082:
                                                            r0 = r1
                                                        L_0x0083:
                                                            return r0
                                                        */
                                                        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.controls.ui.ToggleRangeBehavior$bind$1.performAccessibilityAction(android.view.View, int, android.os.Bundle):boolean");
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
                                        Intrinsics.throwUninitializedPropertyAccessException("rangeTemplate");
                                        throw null;
                                    }
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

    public final void beginUpdateRange() {
        ControlViewHolder controlViewHolder = this.cvh;
        if (controlViewHolder != null) {
            controlViewHolder.setUserInteractionInProgress(true);
            ControlViewHolder controlViewHolder2 = this.cvh;
            if (controlViewHolder2 != null) {
                Context context = this.context;
                if (context != null) {
                    controlViewHolder2.setStatusTextSize((float) context.getResources().getDimensionPixelSize(C0005R$dimen.control_status_expanded));
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("context");
                    throw null;
                }
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("cvh");
                throw null;
            }
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("cvh");
            throw null;
        }
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x007e: APUT  
      (r0v4 int[])
      (0 ??[int, short, byte, char])
      (wrap: int : 0x007a: INVOKE  (r3v3 int) = 
      (wrap: android.graphics.drawable.ClipDrawable : 0x0076: INVOKE  (r3v2 android.graphics.drawable.ClipDrawable) = (r3v1 com.android.systemui.controls.ui.ControlViewHolder) type: VIRTUAL call: com.android.systemui.controls.ui.ControlViewHolder.getClipLayer():android.graphics.drawable.ClipDrawable)
     type: VIRTUAL call: android.graphics.drawable.ClipDrawable.getLevel():int)
     */
    public final void updateRange(int i, boolean z, boolean z2) {
        int max = Math.max(0, Math.min(10000, i));
        Drawable drawable = this.clipLayer;
        if (drawable != null) {
            if (drawable.getLevel() == 0 && max > 0) {
                ControlViewHolder controlViewHolder = this.cvh;
                if (controlViewHolder != null) {
                    controlViewHolder.applyRenderInfo$packages__apps__OPSystemUI__android_common__OPSystemUI_core(z, this.colorOffset, false);
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("cvh");
                    throw null;
                }
            }
            ValueAnimator valueAnimator = this.rangeAnimator;
            if (valueAnimator != null) {
                valueAnimator.cancel();
            }
            if (z2) {
                boolean z3 = max == 0 || max == 10000;
                Drawable drawable2 = this.clipLayer;
                if (drawable2 == null) {
                    Intrinsics.throwUninitializedPropertyAccessException("clipLayer");
                    throw null;
                } else if (drawable2.getLevel() != max) {
                    ControlViewHolder controlViewHolder2 = this.cvh;
                    if (controlViewHolder2 != null) {
                        controlViewHolder2.getControlActionCoordinator().drag(z3);
                        Drawable drawable3 = this.clipLayer;
                        if (drawable3 != null) {
                            drawable3.setLevel(max);
                        } else {
                            Intrinsics.throwUninitializedPropertyAccessException("clipLayer");
                            throw null;
                        }
                    } else {
                        Intrinsics.throwUninitializedPropertyAccessException("cvh");
                        throw null;
                    }
                }
            } else {
                Drawable drawable4 = this.clipLayer;
                if (drawable4 == null) {
                    Intrinsics.throwUninitializedPropertyAccessException("clipLayer");
                    throw null;
                } else if (max != drawable4.getLevel()) {
                    int[] iArr = new int[2];
                    ControlViewHolder controlViewHolder3 = this.cvh;
                    if (controlViewHolder3 != null) {
                        iArr[0] = controlViewHolder3.getClipLayer().getLevel();
                        iArr[1] = max;
                        ValueAnimator ofInt = ValueAnimator.ofInt(iArr);
                        ofInt.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this) { // from class: com.android.systemui.controls.ui.ToggleRangeBehavior$updateRange$$inlined$apply$lambda$1
                            final /* synthetic */ ToggleRangeBehavior this$0;

                            {
                                this.this$0 = r1;
                            }

                            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                            public final void onAnimationUpdate(ValueAnimator valueAnimator2) {
                                ClipDrawable clipLayer = this.this$0.getCvh().getClipLayer();
                                Intrinsics.checkExpressionValueIsNotNull(valueAnimator2, "it");
                                Object animatedValue = valueAnimator2.getAnimatedValue();
                                if (animatedValue != null) {
                                    clipLayer.setLevel(((Integer) animatedValue).intValue());
                                    return;
                                }
                                throw new TypeCastException("null cannot be cast to non-null type kotlin.Int");
                            }
                        });
                        ofInt.addListener(new AnimatorListenerAdapter(this) { // from class: com.android.systemui.controls.ui.ToggleRangeBehavior$updateRange$$inlined$apply$lambda$2
                            final /* synthetic */ ToggleRangeBehavior this$0;

                            {
                                this.this$0 = r1;
                            }

                            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                            public void onAnimationEnd(@Nullable Animator animator) {
                                this.this$0.rangeAnimator = null;
                            }
                        });
                        ofInt.setDuration(700L);
                        ofInt.setInterpolator(Interpolators.CONTROL_STATE);
                        ofInt.start();
                        this.rangeAnimator = ofInt;
                    } else {
                        Intrinsics.throwUninitializedPropertyAccessException("cvh");
                        throw null;
                    }
                }
            }
            if (z) {
                float levelToRangeValue = levelToRangeValue(max);
                RangeTemplate rangeTemplate = this.rangeTemplate;
                if (rangeTemplate != null) {
                    String format = format(rangeTemplate.getFormatString().toString(), "%.1f", levelToRangeValue);
                    this.currentRangeValue = format;
                    if (z2) {
                        ControlViewHolder controlViewHolder4 = this.cvh;
                        if (controlViewHolder4 != null) {
                            controlViewHolder4.setStatusText(format, true);
                        } else {
                            Intrinsics.throwUninitializedPropertyAccessException("cvh");
                            throw null;
                        }
                    } else {
                        ControlViewHolder controlViewHolder5 = this.cvh;
                        if (controlViewHolder5 != null) {
                            ControlViewHolder.setStatusText$default(controlViewHolder5, this.currentStatusText + ' ' + this.currentRangeValue, false, 2, null);
                            return;
                        }
                        Intrinsics.throwUninitializedPropertyAccessException("cvh");
                        throw null;
                    }
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("rangeTemplate");
                    throw null;
                }
            } else {
                ControlViewHolder controlViewHolder6 = this.cvh;
                if (controlViewHolder6 != null) {
                    ControlViewHolder.setStatusText$default(controlViewHolder6, this.currentStatusText, false, 2, null);
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("cvh");
                    throw null;
                }
            }
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("clipLayer");
            throw null;
        }
    }

    private final String format(String str, String str2, float f) {
        try {
            StringCompanionObject stringCompanionObject = StringCompanionObject.INSTANCE;
            String format = String.format(str, Arrays.copyOf(new Object[]{Float.valueOf(findNearestStep(f))}, 1));
            Intrinsics.checkExpressionValueIsNotNull(format, "java.lang.String.format(format, *args)");
            return format;
        } catch (IllegalFormatException e) {
            Log.w("ControlsUiController", "Illegal format in range template", e);
            if (Intrinsics.areEqual(str2, "")) {
                return "";
            }
            return format(str2, "", f);
        }
    }

    /* access modifiers changed from: private */
    public final float levelToRangeValue(int i) {
        RangeTemplate rangeTemplate = this.rangeTemplate;
        if (rangeTemplate != null) {
            float minValue = rangeTemplate.getMinValue();
            RangeTemplate rangeTemplate2 = this.rangeTemplate;
            if (rangeTemplate2 != null) {
                return MathUtils.constrainedMap(minValue, rangeTemplate2.getMaxValue(), (float) 0, (float) 10000, (float) i);
            }
            Intrinsics.throwUninitializedPropertyAccessException("rangeTemplate");
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("rangeTemplate");
        throw null;
    }

    /* access modifiers changed from: private */
    public final int rangeToLevelValue(float f) {
        float f2 = (float) 0;
        float f3 = (float) 10000;
        RangeTemplate rangeTemplate = this.rangeTemplate;
        if (rangeTemplate != null) {
            float minValue = rangeTemplate.getMinValue();
            RangeTemplate rangeTemplate2 = this.rangeTemplate;
            if (rangeTemplate2 != null) {
                return (int) MathUtils.constrainedMap(f2, f3, minValue, rangeTemplate2.getMaxValue(), f);
            }
            Intrinsics.throwUninitializedPropertyAccessException("rangeTemplate");
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("rangeTemplate");
        throw null;
    }

    public final void endUpdateRange() {
        ControlViewHolder controlViewHolder = this.cvh;
        if (controlViewHolder != null) {
            Context context = this.context;
            if (context != null) {
                controlViewHolder.setStatusTextSize((float) context.getResources().getDimensionPixelSize(C0005R$dimen.control_status_normal));
                ControlViewHolder controlViewHolder2 = this.cvh;
                if (controlViewHolder2 != null) {
                    controlViewHolder2.setStatusText(this.currentStatusText + ' ' + this.currentRangeValue, true);
                    ControlViewHolder controlViewHolder3 = this.cvh;
                    if (controlViewHolder3 != null) {
                        ControlActionCoordinator controlActionCoordinator = controlViewHolder3.getControlActionCoordinator();
                        ControlViewHolder controlViewHolder4 = this.cvh;
                        if (controlViewHolder4 != null) {
                            RangeTemplate rangeTemplate = this.rangeTemplate;
                            if (rangeTemplate != null) {
                                String templateId = rangeTemplate.getTemplateId();
                                Intrinsics.checkExpressionValueIsNotNull(templateId, "rangeTemplate.getTemplateId()");
                                Drawable drawable = this.clipLayer;
                                if (drawable != null) {
                                    controlActionCoordinator.setValue(controlViewHolder4, templateId, findNearestStep(levelToRangeValue(drawable.getLevel())));
                                    ControlViewHolder controlViewHolder5 = this.cvh;
                                    if (controlViewHolder5 != null) {
                                        controlViewHolder5.setUserInteractionInProgress(false);
                                    } else {
                                        Intrinsics.throwUninitializedPropertyAccessException("cvh");
                                        throw null;
                                    }
                                } else {
                                    Intrinsics.throwUninitializedPropertyAccessException("clipLayer");
                                    throw null;
                                }
                            } else {
                                Intrinsics.throwUninitializedPropertyAccessException("rangeTemplate");
                                throw null;
                            }
                        } else {
                            Intrinsics.throwUninitializedPropertyAccessException("cvh");
                            throw null;
                        }
                    } else {
                        Intrinsics.throwUninitializedPropertyAccessException("cvh");
                        throw null;
                    }
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("cvh");
                    throw null;
                }
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("context");
                throw null;
            }
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("cvh");
            throw null;
        }
    }

    public final float findNearestStep(float f) {
        RangeTemplate rangeTemplate = this.rangeTemplate;
        if (rangeTemplate != null) {
            float minValue = rangeTemplate.getMinValue();
            float f2 = 1000.0f;
            while (true) {
                RangeTemplate rangeTemplate2 = this.rangeTemplate;
                if (rangeTemplate2 == null) {
                    Intrinsics.throwUninitializedPropertyAccessException("rangeTemplate");
                    throw null;
                } else if (minValue <= rangeTemplate2.getMaxValue()) {
                    float abs = Math.abs(f - minValue);
                    if (abs < f2) {
                        RangeTemplate rangeTemplate3 = this.rangeTemplate;
                        if (rangeTemplate3 != null) {
                            minValue += rangeTemplate3.getStepValue();
                            f2 = abs;
                        } else {
                            Intrinsics.throwUninitializedPropertyAccessException("rangeTemplate");
                            throw null;
                        }
                    } else {
                        RangeTemplate rangeTemplate4 = this.rangeTemplate;
                        if (rangeTemplate4 != null) {
                            return minValue - rangeTemplate4.getStepValue();
                        }
                        Intrinsics.throwUninitializedPropertyAccessException("rangeTemplate");
                        throw null;
                    }
                } else {
                    RangeTemplate rangeTemplate5 = this.rangeTemplate;
                    if (rangeTemplate5 != null) {
                        return rangeTemplate5.getMaxValue();
                    }
                    Intrinsics.throwUninitializedPropertyAccessException("rangeTemplate");
                    throw null;
                }
            }
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("rangeTemplate");
            throw null;
        }
    }

    /* compiled from: ToggleRangeBehavior.kt */
    public final class ToggleRangeGestureListener extends GestureDetector.SimpleOnGestureListener {
        private boolean isDragging;
        final /* synthetic */ ToggleRangeBehavior this$0;
        @NotNull
        private final View v;

        @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnGestureListener
        public boolean onDown(@NotNull MotionEvent motionEvent) {
            Intrinsics.checkParameterIsNotNull(motionEvent, "e");
            return true;
        }

        public ToggleRangeGestureListener(@NotNull ToggleRangeBehavior toggleRangeBehavior, View view) {
            Intrinsics.checkParameterIsNotNull(view, "v");
            this.this$0 = toggleRangeBehavior;
            this.v = view;
        }

        public final boolean isDragging() {
            return this.isDragging;
        }

        public final void setDragging(boolean z) {
            this.isDragging = z;
        }

        @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnGestureListener
        public void onLongPress(@NotNull MotionEvent motionEvent) {
            Intrinsics.checkParameterIsNotNull(motionEvent, "e");
            if (!this.isDragging) {
                this.this$0.getCvh().getControlActionCoordinator().longPress(this.this$0.getCvh());
            }
        }

        @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnGestureListener
        public boolean onScroll(@NotNull MotionEvent motionEvent, @NotNull MotionEvent motionEvent2, float f, float f2) {
            Intrinsics.checkParameterIsNotNull(motionEvent, "e1");
            Intrinsics.checkParameterIsNotNull(motionEvent2, "e2");
            if (!this.isDragging) {
                this.v.getParent().requestDisallowInterceptTouchEvent(true);
                this.this$0.beginUpdateRange();
                this.isDragging = true;
            }
            ToggleRangeBehavior toggleRangeBehavior = this.this$0;
            toggleRangeBehavior.updateRange(toggleRangeBehavior.getClipLayer().getLevel() + ((int) (((float) 10000) * ((-f) / ((float) this.v.getWidth())))), true, true);
            return true;
        }

        @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnGestureListener
        public boolean onSingleTapUp(@NotNull MotionEvent motionEvent) {
            Intrinsics.checkParameterIsNotNull(motionEvent, "e");
            if (!this.this$0.isToggleable()) {
                return false;
            }
            this.this$0.getCvh().getControlActionCoordinator().toggle(this.this$0.getCvh(), this.this$0.getTemplateId(), this.this$0.isChecked());
            return true;
        }
    }
}
