package com.android.systemui.statusbar.phone;

import android.hardware.Sensor;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.Dumpable;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.util.Assert;
import com.android.systemui.util.sensors.AsyncSensorManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: KeyguardLiftController.kt */
public final class KeyguardLiftController extends KeyguardUpdateMonitorCallback implements StatusBarStateController.StateListener, Dumpable {
    private final AsyncSensorManager asyncSensorManager;
    private boolean bouncerVisible;
    private boolean isListening;
    private final KeyguardUpdateMonitor keyguardUpdateMonitor;
    private final TriggerEventListener listener = new TriggerEventListener(this) { // from class: com.android.systemui.statusbar.phone.KeyguardLiftController$listener$1
        final /* synthetic */ KeyguardLiftController this$0;

        /* JADX WARN: Incorrect args count in method signature: ()V */
        {
            this.this$0 = r1;
        }

        @Override // android.hardware.TriggerEventListener
        public void onTrigger(@Nullable TriggerEvent triggerEvent) {
            Assert.isMainThread();
            this.this$0.isListening = false;
            this.this$0.updateListeningState();
            this.this$0.keyguardUpdateMonitor.requestFaceAuth();
        }
    };
    private final Sensor pickupSensor;
    private final StatusBarStateController statusBarStateController;

    public KeyguardLiftController(@NotNull StatusBarStateController statusBarStateController, @NotNull AsyncSensorManager asyncSensorManager, @NotNull KeyguardUpdateMonitor keyguardUpdateMonitor, @NotNull DumpManager dumpManager) {
        Intrinsics.checkParameterIsNotNull(statusBarStateController, "statusBarStateController");
        Intrinsics.checkParameterIsNotNull(asyncSensorManager, "asyncSensorManager");
        Intrinsics.checkParameterIsNotNull(keyguardUpdateMonitor, "keyguardUpdateMonitor");
        Intrinsics.checkParameterIsNotNull(dumpManager, "dumpManager");
        this.statusBarStateController = statusBarStateController;
        this.asyncSensorManager = asyncSensorManager;
        this.keyguardUpdateMonitor = keyguardUpdateMonitor;
        this.pickupSensor = asyncSensorManager.getDefaultSensor(25);
        String name = KeyguardLiftController.class.getName();
        Intrinsics.checkExpressionValueIsNotNull(name, "javaClass.name");
        dumpManager.registerDumpable(name, this);
        this.statusBarStateController.addCallback(this);
        this.keyguardUpdateMonitor.registerCallback(this);
        updateListeningState();
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onDozingChanged(boolean z) {
        updateListeningState();
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onKeyguardBouncerChanged(boolean z) {
        this.bouncerVisible = z;
        updateListeningState();
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onKeyguardVisibilityChanged(boolean z) {
        updateListeningState();
    }

    @Override // com.android.systemui.Dumpable
    public void dump(@NotNull FileDescriptor fileDescriptor, @NotNull PrintWriter printWriter, @NotNull String[] strArr) {
        Intrinsics.checkParameterIsNotNull(fileDescriptor, "fd");
        Intrinsics.checkParameterIsNotNull(printWriter, "pw");
        Intrinsics.checkParameterIsNotNull(strArr, "args");
        printWriter.println("KeyguardLiftController:");
        printWriter.println("  pickupSensor: " + this.pickupSensor);
        printWriter.println("  isListening: " + this.isListening);
        printWriter.println("  bouncerVisible: " + this.bouncerVisible);
    }

    /* access modifiers changed from: private */
    public final void updateListeningState() {
        if (this.pickupSensor != null) {
            boolean z = true;
            if (!(this.keyguardUpdateMonitor.isKeyguardVisible() && !this.statusBarStateController.isDozing()) && !this.bouncerVisible) {
                z = false;
            }
            if (z != this.isListening) {
                this.isListening = z;
                if (z) {
                    this.asyncSensorManager.requestTriggerSensor(this.listener, this.pickupSensor);
                } else {
                    this.asyncSensorManager.cancelTriggerSensor(this.listener, this.pickupSensor);
                }
            }
        }
    }
}
