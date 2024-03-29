package com.android.systemui.doze;

import android.content.Context;
import android.os.Build;
import android.os.PowerManager;
import android.os.SystemClock;
import android.service.dreams.DreamService;
import com.android.systemui.doze.DozeMachine;
import com.android.systemui.plugins.DozeServicePlugin;
import com.android.systemui.plugins.PluginListener;
import com.android.systemui.shared.plugins.PluginManager;
import com.android.systemui.statusbar.phone.StatusBar;
import com.oneplus.plugin.OpLsState;
import java.io.FileDescriptor;
import java.io.PrintWriter;
public class DozeService extends DreamService implements DozeMachine.Service, DozeServicePlugin.RequestDoze, PluginListener<DozeServicePlugin> {
    static final boolean DEBUG = Build.DEBUG_ONEPLUS;
    private final DozeFactory mDozeFactory;
    private DozeMachine mDozeMachine;
    private DozeServicePlugin mDozePlugin;
    private PluginManager mPluginManager;

    public DozeService(DozeFactory dozeFactory, PluginManager pluginManager) {
        setDebug(DEBUG);
        this.mDozeFactory = dozeFactory;
        this.mPluginManager = pluginManager;
    }

    @Override // android.service.dreams.DreamService, android.app.Service
    public void onCreate() {
        super.onCreate();
        setWindowless(true);
        this.mPluginManager.addPluginListener((PluginListener) this, DozeServicePlugin.class, false);
        this.mDozeMachine = this.mDozeFactory.assembleMachine(this);
    }

    @Override // android.service.dreams.DreamService, android.app.Service
    public void onDestroy() {
        PluginManager pluginManager = this.mPluginManager;
        if (pluginManager != null) {
            pluginManager.removePluginListener(this);
        }
        super.onDestroy();
        this.mDozeMachine.destroy();
        this.mDozeMachine = null;
    }

    public void onPluginConnected(DozeServicePlugin dozeServicePlugin, Context context) {
        this.mDozePlugin = dozeServicePlugin;
        dozeServicePlugin.setDozeRequester(this);
    }

    public void onPluginDisconnected(DozeServicePlugin dozeServicePlugin) {
        DozeServicePlugin dozeServicePlugin2 = this.mDozePlugin;
        if (dozeServicePlugin2 != null) {
            dozeServicePlugin2.onDreamingStopped();
            this.mDozePlugin = null;
        }
    }

    @Override // android.service.dreams.DreamService
    public void onDreamingStarted() {
        super.onDreamingStarted();
        DozeMachine dozeMachine = this.mDozeMachine;
        if (dozeMachine == null) {
            finish();
            return;
        }
        dozeMachine.requestState(DozeMachine.State.INITIALIZED);
        startDozing();
        DozeServicePlugin dozeServicePlugin = this.mDozePlugin;
        if (dozeServicePlugin != null) {
            dozeServicePlugin.onDreamingStarted();
        }
    }

    @Override // android.service.dreams.DreamService
    public void onDreamingStopped() {
        super.onDreamingStopped();
        this.mDozeMachine.requestState(DozeMachine.State.FINISH);
        DozeServicePlugin dozeServicePlugin = this.mDozePlugin;
        if (dozeServicePlugin != null) {
            dozeServicePlugin.onDreamingStopped();
        }
    }

    /* access modifiers changed from: protected */
    public void dumpOnHandler(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        super.dumpOnHandler(fileDescriptor, printWriter, strArr);
        DozeMachine dozeMachine = this.mDozeMachine;
        if (dozeMachine != null) {
            dozeMachine.dump(printWriter);
        }
    }

    @Override // com.android.systemui.doze.DozeMachine.Service
    public void requestWakeUp() {
        ((PowerManager) getSystemService(PowerManager.class)).wakeUp(SystemClock.uptimeMillis(), 4, "com.android.systemui:NODOZE");
    }

    @Override // com.android.systemui.plugins.DozeServicePlugin.RequestDoze
    public void onRequestShowDoze() {
        DozeMachine dozeMachine = this.mDozeMachine;
        if (dozeMachine != null) {
            dozeMachine.requestState(DozeMachine.State.DOZE_AOD);
        }
    }

    @Override // com.android.systemui.plugins.DozeServicePlugin.RequestDoze
    public void onRequestHideDoze() {
        DozeMachine dozeMachine = this.mDozeMachine;
        if (dozeMachine != null) {
            dozeMachine.requestState(DozeMachine.State.DOZE);
        }
    }

    public void onSingleTap() {
        StatusBar phoneStatusBar = OpLsState.getInstance().getPhoneStatusBar();
        if (phoneStatusBar != null) {
            phoneStatusBar.onSingleTap();
        }
    }
}
