package com.google.android.libraries.assistant.oemsmartspace.lib;

import com.google.android.libraries.assistant.oemsmartspace.lib.SmartspaceContainerController;
final /* synthetic */ class SmartspaceContainerController$DeviceStateChangeReceiver$$Lambda$0 implements Runnable {
    private final SmartspaceContainerController.DeviceStateChangeReceiver arg$1;

    SmartspaceContainerController$DeviceStateChangeReceiver$$Lambda$0(SmartspaceContainerController.DeviceStateChangeReceiver deviceStateChangeReceiver) {
        this.arg$1 = deviceStateChangeReceiver;
    }

    @Override // java.lang.Runnable
    public void run() {
        this.arg$1.lambda$onReceive$0$SmartspaceContainerController$DeviceStateChangeReceiver();
    }
}
