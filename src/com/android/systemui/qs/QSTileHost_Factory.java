package com.android.systemui.qs;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.android.internal.logging.UiEventLogger;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.plugins.qs.QSFactory;
import com.android.systemui.qs.logging.QSLogger;
import com.android.systemui.shared.plugins.PluginManager;
import com.android.systemui.statusbar.phone.AutoTileManager;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.tuner.TunerService;
import dagger.internal.Factory;
import java.util.Optional;
import javax.inject.Provider;
public final class QSTileHost_Factory implements Factory<QSTileHost> {
    private final Provider<AutoTileManager> autoTilesProvider;
    private final Provider<Looper> bgLooperProvider;
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<Context> contextProvider;
    private final Provider<QSFactory> defaultFactoryProvider;
    private final Provider<DumpManager> dumpManagerProvider;
    private final Provider<StatusBarIconController> iconControllerProvider;
    private final Provider<Handler> mainHandlerProvider;
    private final Provider<PluginManager> pluginManagerProvider;
    private final Provider<QSLogger> qsLoggerProvider;
    private final Provider<Optional<StatusBar>> statusBarOptionalProvider;
    private final Provider<TunerService> tunerServiceProvider;
    private final Provider<UiEventLogger> uiEventLoggerProvider;

    public QSTileHost_Factory(Provider<Context> provider, Provider<StatusBarIconController> provider2, Provider<QSFactory> provider3, Provider<Handler> provider4, Provider<Looper> provider5, Provider<PluginManager> provider6, Provider<TunerService> provider7, Provider<AutoTileManager> provider8, Provider<DumpManager> provider9, Provider<BroadcastDispatcher> provider10, Provider<Optional<StatusBar>> provider11, Provider<QSLogger> provider12, Provider<UiEventLogger> provider13) {
        this.contextProvider = provider;
        this.iconControllerProvider = provider2;
        this.defaultFactoryProvider = provider3;
        this.mainHandlerProvider = provider4;
        this.bgLooperProvider = provider5;
        this.pluginManagerProvider = provider6;
        this.tunerServiceProvider = provider7;
        this.autoTilesProvider = provider8;
        this.dumpManagerProvider = provider9;
        this.broadcastDispatcherProvider = provider10;
        this.statusBarOptionalProvider = provider11;
        this.qsLoggerProvider = provider12;
        this.uiEventLoggerProvider = provider13;
    }

    @Override // javax.inject.Provider
    public QSTileHost get() {
        return provideInstance(this.contextProvider, this.iconControllerProvider, this.defaultFactoryProvider, this.mainHandlerProvider, this.bgLooperProvider, this.pluginManagerProvider, this.tunerServiceProvider, this.autoTilesProvider, this.dumpManagerProvider, this.broadcastDispatcherProvider, this.statusBarOptionalProvider, this.qsLoggerProvider, this.uiEventLoggerProvider);
    }

    public static QSTileHost provideInstance(Provider<Context> provider, Provider<StatusBarIconController> provider2, Provider<QSFactory> provider3, Provider<Handler> provider4, Provider<Looper> provider5, Provider<PluginManager> provider6, Provider<TunerService> provider7, Provider<AutoTileManager> provider8, Provider<DumpManager> provider9, Provider<BroadcastDispatcher> provider10, Provider<Optional<StatusBar>> provider11, Provider<QSLogger> provider12, Provider<UiEventLogger> provider13) {
        return new QSTileHost(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get(), provider8, provider9.get(), provider10.get(), provider11.get(), provider12.get(), provider13.get());
    }

    public static QSTileHost_Factory create(Provider<Context> provider, Provider<StatusBarIconController> provider2, Provider<QSFactory> provider3, Provider<Handler> provider4, Provider<Looper> provider5, Provider<PluginManager> provider6, Provider<TunerService> provider7, Provider<AutoTileManager> provider8, Provider<DumpManager> provider9, Provider<BroadcastDispatcher> provider10, Provider<Optional<StatusBar>> provider11, Provider<QSLogger> provider12, Provider<UiEventLogger> provider13) {
        return new QSTileHost_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9, provider10, provider11, provider12, provider13);
    }
}
