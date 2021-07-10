package com.android.systemui.controls.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.service.controls.Control;
import android.service.controls.actions.BooleanAction;
import android.service.controls.actions.CommandAction;
import android.service.controls.actions.FloatAction;
import android.util.Log;
import com.android.systemui.controls.ui.ControlActionCoordinatorImpl;
import com.android.systemui.globalactions.GlobalActionsComponent;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.util.concurrency.DelayableExecutor;
import java.util.LinkedHashSet;
import java.util.Set;
import kotlin.TypeCastException;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Ref$BooleanRef;
import org.jetbrains.annotations.NotNull;
/* compiled from: ControlActionCoordinatorImpl.kt */
public final class ControlActionCoordinatorImpl implements ControlActionCoordinator {
    private Set<String> actionsInProgress;
    private final ActivityStarter activityStarter;
    private final DelayableExecutor bgExecutor;
    private final Context context;
    private Dialog dialog;
    private final GlobalActionsComponent globalActionsComponent;
    private final KeyguardStateController keyguardStateController;
    private Action pendingAction;
    private final DelayableExecutor uiExecutor;
    private final Vibrator vibrator;

    public ControlActionCoordinatorImpl(@NotNull Context context, @NotNull DelayableExecutor delayableExecutor, @NotNull DelayableExecutor delayableExecutor2, @NotNull ActivityStarter activityStarter, @NotNull KeyguardStateController keyguardStateController, @NotNull GlobalActionsComponent globalActionsComponent) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(delayableExecutor, "bgExecutor");
        Intrinsics.checkParameterIsNotNull(delayableExecutor2, "uiExecutor");
        Intrinsics.checkParameterIsNotNull(activityStarter, "activityStarter");
        Intrinsics.checkParameterIsNotNull(keyguardStateController, "keyguardStateController");
        Intrinsics.checkParameterIsNotNull(globalActionsComponent, "globalActionsComponent");
        this.context = context;
        this.bgExecutor = delayableExecutor;
        this.uiExecutor = delayableExecutor2;
        this.activityStarter = activityStarter;
        this.keyguardStateController = keyguardStateController;
        this.globalActionsComponent = globalActionsComponent;
        Object systemService = context.getSystemService("vibrator");
        if (systemService != null) {
            this.vibrator = (Vibrator) systemService;
            this.actionsInProgress = new LinkedHashSet();
            return;
        }
        throw new TypeCastException("null cannot be cast to non-null type android.os.Vibrator");
    }

    @Override // com.android.systemui.controls.ui.ControlActionCoordinator
    public void closeDialogs() {
        Dialog dialog = this.dialog;
        if (dialog != null) {
            dialog.dismiss();
        }
        this.dialog = null;
    }

    @Override // com.android.systemui.controls.ui.ControlActionCoordinator
    public void toggle(@NotNull ControlViewHolder controlViewHolder, @NotNull String str, boolean z) {
        Intrinsics.checkParameterIsNotNull(controlViewHolder, "cvh");
        Intrinsics.checkParameterIsNotNull(str, "templateId");
        bouncerOrRun(new Action(this, controlViewHolder.getCws().getCi().getControlId(), new Function0<Unit>(controlViewHolder, str, z) { // from class: com.android.systemui.controls.ui.ControlActionCoordinatorImpl$toggle$1
            final /* synthetic */ ControlViewHolder $cvh;
            final /* synthetic */ boolean $isChecked;
            final /* synthetic */ String $templateId;

            {
                this.$cvh = r1;
                this.$templateId = r2;
                this.$isChecked = r3;
            }

            @Override // kotlin.jvm.functions.Function0
            public final void invoke() {
                this.$cvh.getLayout().performHapticFeedback(6);
                this.$cvh.action(new BooleanAction(this.$templateId, !this.$isChecked));
            }
        }, true));
    }

    @Override // com.android.systemui.controls.ui.ControlActionCoordinator
    public void touch(@NotNull ControlViewHolder controlViewHolder, @NotNull String str, @NotNull Control control) {
        Intrinsics.checkParameterIsNotNull(controlViewHolder, "cvh");
        Intrinsics.checkParameterIsNotNull(str, "templateId");
        Intrinsics.checkParameterIsNotNull(control, "control");
        bouncerOrRun(new Action(this, controlViewHolder.getCws().getCi().getControlId(), new Function0<Unit>(this, controlViewHolder, control, str) { // from class: com.android.systemui.controls.ui.ControlActionCoordinatorImpl$touch$1
            final /* synthetic */ Control $control;
            final /* synthetic */ ControlViewHolder $cvh;
            final /* synthetic */ String $templateId;
            final /* synthetic */ ControlActionCoordinatorImpl this$0;

            {
                this.this$0 = r1;
                this.$cvh = r2;
                this.$control = r3;
                this.$templateId = r4;
            }

            @Override // kotlin.jvm.functions.Function0
            public final void invoke() {
                this.$cvh.getLayout().performHapticFeedback(6);
                if (this.$cvh.usePanel()) {
                    ControlActionCoordinatorImpl controlActionCoordinatorImpl = this.this$0;
                    ControlViewHolder controlViewHolder2 = this.$cvh;
                    Intent intent = this.$control.getAppIntent().getIntent();
                    Intrinsics.checkExpressionValueIsNotNull(intent, "control.getAppIntent().getIntent()");
                    controlActionCoordinatorImpl.showDialog(controlViewHolder2, intent);
                    return;
                }
                this.$cvh.action(new CommandAction(this.$templateId));
            }
        }, controlViewHolder.usePanel()));
    }

    @Override // com.android.systemui.controls.ui.ControlActionCoordinator
    public void drag(boolean z) {
        if (z) {
            vibrate(Vibrations.INSTANCE.getRangeEdgeEffect());
        } else {
            vibrate(Vibrations.INSTANCE.getRangeMiddleEffect());
        }
    }

    @Override // com.android.systemui.controls.ui.ControlActionCoordinator
    public void setValue(@NotNull ControlViewHolder controlViewHolder, @NotNull String str, float f) {
        Intrinsics.checkParameterIsNotNull(controlViewHolder, "cvh");
        Intrinsics.checkParameterIsNotNull(str, "templateId");
        bouncerOrRun(new Action(this, controlViewHolder.getCws().getCi().getControlId(), new Function0<Unit>(controlViewHolder, str, f) { // from class: com.android.systemui.controls.ui.ControlActionCoordinatorImpl$setValue$1
            final /* synthetic */ ControlViewHolder $cvh;
            final /* synthetic */ float $newValue;
            final /* synthetic */ String $templateId;

            {
                this.$cvh = r1;
                this.$templateId = r2;
                this.$newValue = r3;
            }

            @Override // kotlin.jvm.functions.Function0
            public final void invoke() {
                this.$cvh.action(new FloatAction(this.$templateId, this.$newValue));
            }
        }, true));
    }

    @Override // com.android.systemui.controls.ui.ControlActionCoordinator
    public void longPress(@NotNull ControlViewHolder controlViewHolder) {
        Intrinsics.checkParameterIsNotNull(controlViewHolder, "cvh");
        bouncerOrRun(new Action(this, controlViewHolder.getCws().getCi().getControlId(), new Function0<Unit>(this, controlViewHolder) { // from class: com.android.systemui.controls.ui.ControlActionCoordinatorImpl$longPress$1
            final /* synthetic */ ControlViewHolder $cvh;
            final /* synthetic */ ControlActionCoordinatorImpl this$0;

            {
                this.this$0 = r1;
                this.$cvh = r2;
            }

            @Override // kotlin.jvm.functions.Function0
            public final void invoke() {
                Control control = this.$cvh.getCws().getControl();
                if (control != null) {
                    this.$cvh.getLayout().performHapticFeedback(0);
                    ControlActionCoordinatorImpl controlActionCoordinatorImpl = this.this$0;
                    ControlViewHolder controlViewHolder2 = this.$cvh;
                    Intent intent = control.getAppIntent().getIntent();
                    Intrinsics.checkExpressionValueIsNotNull(intent, "it.getAppIntent().getIntent()");
                    ControlActionCoordinatorImpl.access$showDialog(controlActionCoordinatorImpl, controlViewHolder2, intent);
                }
            }
        }, false));
    }

    @Override // com.android.systemui.controls.ui.ControlActionCoordinator
    public void runPendingAction(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "controlId");
        Action action = this.pendingAction;
        if (Intrinsics.areEqual(action != null ? action.getControlId() : null, str)) {
            Action action2 = this.pendingAction;
            if (action2 != null) {
                action2.invoke();
            }
            this.pendingAction = null;
        }
    }

    @Override // com.android.systemui.controls.ui.ControlActionCoordinator
    public void enableActionOnTouch(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "controlId");
        this.actionsInProgress.remove(str);
    }

    private final boolean shouldRunAction(String str) {
        if (!this.actionsInProgress.add(str)) {
            return false;
        }
        this.uiExecutor.executeDelayed(new Runnable(this, str) { // from class: com.android.systemui.controls.ui.ControlActionCoordinatorImpl$shouldRunAction$1
            final /* synthetic */ String $controlId;
            final /* synthetic */ ControlActionCoordinatorImpl this$0;

            {
                this.this$0 = r1;
                this.$controlId = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                this.this$0.actionsInProgress.remove(this.$controlId);
            }
        }, 3000);
        return true;
    }

    private final void bouncerOrRun(Action action) {
        if (this.keyguardStateController.isShowing()) {
            Ref$BooleanRef ref$BooleanRef = new Ref$BooleanRef();
            boolean z = !this.keyguardStateController.isUnlocked();
            ref$BooleanRef.element = z;
            if (z) {
                this.context.sendBroadcast(new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
                this.pendingAction = action;
            }
            this.activityStarter.dismissKeyguardThenExecute(new ActivityStarter.OnDismissAction(this, ref$BooleanRef, action) { // from class: com.android.systemui.controls.ui.ControlActionCoordinatorImpl$bouncerOrRun$1
                final /* synthetic */ ControlActionCoordinatorImpl.Action $action;
                final /* synthetic */ Ref$BooleanRef $closeGlobalActions;
                final /* synthetic */ ControlActionCoordinatorImpl this$0;

                {
                    this.this$0 = r1;
                    this.$closeGlobalActions = r2;
                    this.$action = r3;
                }

                @Override // com.android.systemui.plugins.ActivityStarter.OnDismissAction
                public final boolean onDismiss() {
                    Log.d("ControlsUiController", "Device unlocked, invoking controls action");
                    if (this.$closeGlobalActions.element) {
                        ControlActionCoordinatorImpl.access$getGlobalActionsComponent$p(this.this$0).handleShowGlobalActionsMenu();
                        return true;
                    }
                    this.$action.invoke();
                    return true;
                }
            }, new Runnable(this) { // from class: com.android.systemui.controls.ui.ControlActionCoordinatorImpl$bouncerOrRun$2
                final /* synthetic */ ControlActionCoordinatorImpl this$0;

                {
                    this.this$0 = r1;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    ControlActionCoordinatorImpl.access$setPendingAction$p(this.this$0, null);
                }
            }, true);
            return;
        }
        action.invoke();
    }

    private final void vibrate(VibrationEffect vibrationEffect) {
        this.bgExecutor.execute(new Runnable(this, vibrationEffect) { // from class: com.android.systemui.controls.ui.ControlActionCoordinatorImpl$vibrate$1
            final /* synthetic */ VibrationEffect $effect;
            final /* synthetic */ ControlActionCoordinatorImpl this$0;

            {
                this.this$0 = r1;
                this.$effect = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                this.this$0.vibrator.vibrate(this.$effect);
            }
        });
    }

    /* access modifiers changed from: public */
    private final void showDialog(ControlViewHolder controlViewHolder, Intent intent) {
        this.bgExecutor.execute(new ControlActionCoordinatorImpl$showDialog$1(this, controlViewHolder, intent));
    }

    /* compiled from: ControlActionCoordinatorImpl.kt */
    public final class Action {
        private final boolean blockable;
        @NotNull
        private final String controlId;
        @NotNull
        private final Function0<Unit> f;
        final /* synthetic */ ControlActionCoordinatorImpl this$0;

        public Action(@NotNull ControlActionCoordinatorImpl controlActionCoordinatorImpl, @NotNull String str, Function0<Unit> function0, boolean z) {
            Intrinsics.checkParameterIsNotNull(str, "controlId");
            Intrinsics.checkParameterIsNotNull(function0, "f");
            this.this$0 = controlActionCoordinatorImpl;
            this.controlId = str;
            this.f = function0;
            this.blockable = z;
        }

        @NotNull
        public final String getControlId() {
            return this.controlId;
        }

        public final void invoke() {
            if (!this.blockable || this.this$0.shouldRunAction(this.controlId)) {
                this.f.invoke();
            }
        }
    }
}
