package com.android.systemui.qs;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import com.android.internal.logging.InstanceId;
import com.android.internal.logging.InstanceIdSequence;
import com.android.internal.logging.UiEventLogger;
import com.android.systemui.C0015R$string;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.plugins.PluginListener;
import com.android.systemui.plugins.qs.QSFactory;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.plugins.qs.QSTileView;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.external.CustomTile;
import com.android.systemui.qs.external.TileLifecycleManager;
import com.android.systemui.qs.external.TileServices;
import com.android.systemui.qs.logging.QSLogger;
import com.android.systemui.shared.plugins.PluginManager;
import com.android.systemui.statusbar.phone.AutoTileManager;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.policy.HotspotController;
import com.android.systemui.tuner.TunerService;
import com.oneplus.util.OpUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.inject.Provider;
public class QSTileHost implements QSHost, TunerService.Tunable, PluginListener<QSFactory>, Dumpable {
    private static final boolean DEBUG = Log.isLoggable("QSTileHost", 3);
    private static final boolean DEBUG_ONEPLUS = Build.DEBUG_ONEPLUS;
    private AutoTileManager mAutoTiles;
    private final BroadcastDispatcher mBroadcastDispatcher;
    private final List<QSHost.Callback> mCallbacks = new ArrayList();
    private final Context mContext;
    private int mCurrentUser;
    private final DumpManager mDumpManager;
    private final StatusBarIconController mIconController;
    private final InstanceIdSequence mInstanceIdSequence;
    private final OperatorCustom mOperatorCustom;
    private final QSLogger mQSLogger;
    private final ArrayList<QSFactory> mQsFactories = new ArrayList<>();
    private final TileServices mServices;
    private final Optional<StatusBar> mStatusBarOptional;
    protected final ArrayList<String> mTileSpecs = new ArrayList<>();
    private final LinkedHashMap<String, QSTile> mTiles = new LinkedHashMap<>();
    private final TunerService mTunerService;
    private final UiEventLogger mUiEventLogger;
    private Context mUserContext;

    @Override // com.android.systemui.qs.QSHost
    public void warn(String str, Throwable th) {
    }

    public QSTileHost(Context context, StatusBarIconController statusBarIconController, QSFactory qSFactory, Handler handler, Looper looper, PluginManager pluginManager, TunerService tunerService, Provider<AutoTileManager> provider, DumpManager dumpManager, BroadcastDispatcher broadcastDispatcher, Optional<StatusBar> optional, QSLogger qSLogger, UiEventLogger uiEventLogger) {
        this.mIconController = statusBarIconController;
        this.mContext = context;
        this.mUserContext = context;
        this.mTunerService = tunerService;
        this.mDumpManager = dumpManager;
        this.mQSLogger = qSLogger;
        this.mUiEventLogger = uiEventLogger;
        this.mBroadcastDispatcher = broadcastDispatcher;
        this.mInstanceIdSequence = new InstanceIdSequence(1048576);
        this.mServices = new TileServices(this, looper, this.mBroadcastDispatcher);
        this.mStatusBarOptional = optional;
        this.mQsFactories.add(qSFactory);
        pluginManager.addPluginListener((PluginListener) this, QSFactory.class, true);
        this.mDumpManager.registerDumpable("QSTileHost", this);
        handler.post(new Runnable(tunerService, provider) { // from class: com.android.systemui.qs.-$$Lambda$QSTileHost$8OyZkY1GXlSGEY9CusSz83dAxOw
            public final /* synthetic */ TunerService f$1;
            public final /* synthetic */ Provider f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.lang.Runnable
            public final void run() {
                QSTileHost.this.lambda$new$0$QSTileHost(this.f$1, this.f$2);
            }
        });
        OperatorCustom operatorCustom = new OperatorCustom();
        this.mOperatorCustom = operatorCustom;
        operatorCustom.init();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$QSTileHost(TunerService tunerService, Provider provider) {
        tunerService.addTunable(this, "sysui_qs_tiles");
        this.mAutoTiles = (AutoTileManager) provider.get();
    }

    public StatusBarIconController getIconController() {
        return this.mIconController;
    }

    @Override // com.android.systemui.qs.QSHost
    public InstanceId getNewInstanceId() {
        return this.mInstanceIdSequence.newInstanceId();
    }

    public void onPluginConnected(QSFactory qSFactory, Context context) {
        this.mQsFactories.add(0, qSFactory);
        String value = this.mTunerService.getValue("sysui_qs_tiles");
        onTuningChanged("sysui_qs_tiles", "");
        onTuningChanged("sysui_qs_tiles", value);
    }

    public void onPluginDisconnected(QSFactory qSFactory) {
        this.mQsFactories.remove(qSFactory);
        String value = this.mTunerService.getValue("sysui_qs_tiles");
        onTuningChanged("sysui_qs_tiles", "");
        onTuningChanged("sysui_qs_tiles", value);
    }

    @Override // com.android.systemui.qs.QSHost
    public QSLogger getQSLogger() {
        return this.mQSLogger;
    }

    @Override // com.android.systemui.qs.QSHost
    public UiEventLogger getUiEventLogger() {
        return this.mUiEventLogger;
    }

    public void addCallback(QSHost.Callback callback) {
        this.mCallbacks.add(callback);
    }

    public void removeCallback(QSHost.Callback callback) {
        this.mCallbacks.remove(callback);
    }

    public Collection<QSTile> getTiles() {
        return this.mTiles.values();
    }

    @Override // com.android.systemui.qs.QSHost
    public void collapsePanels() {
        this.mStatusBarOptional.ifPresent($$Lambda$4RRpk2g2DG1jxcebU4uq2xyjwbI.INSTANCE);
    }

    public void forceCollapsePanels() {
        this.mStatusBarOptional.ifPresent($$Lambda$mg7HvLF2bK625f51dPBSLbws.INSTANCE);
    }

    @Override // com.android.systemui.qs.QSHost
    public void openPanels() {
        this.mStatusBarOptional.ifPresent($$Lambda$dlfb7Xnz27iJwNxSQU2fGCzuI2E.INSTANCE);
    }

    @Override // com.android.systemui.qs.QSHost
    public Context getContext() {
        return this.mContext;
    }

    @Override // com.android.systemui.qs.QSHost
    public Context getUserContext() {
        return this.mUserContext;
    }

    @Override // com.android.systemui.qs.QSHost
    public TileServices getTileServices() {
        return this.mServices;
    }

    @Override // com.android.systemui.qs.QSHost
    public int indexOf(String str) {
        return this.mTileSpecs.indexOf(str);
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        boolean z;
        if ("sysui_qs_tiles".equals(str)) {
            Log.d("QSTileHost", "Recreating tiles");
            if (str2 == null && UserManager.isDeviceInDemoMode(this.mContext)) {
                str2 = this.mContext.getResources().getString(C0015R$string.quick_settings_tiles_retail_mode);
            }
            List<String> loadTileSpecs = loadTileSpecs(this.mContext, str2);
            int currentUser = ActivityManager.getCurrentUser();
            if (currentUser != this.mCurrentUser) {
                this.mUserContext = this.mContext.createContextAsUser(UserHandle.of(currentUser), 0);
                AutoTileManager autoTileManager = this.mAutoTiles;
                if (autoTileManager != null) {
                    autoTileManager.lambda$changeUser$0(UserHandle.of(currentUser));
                }
            }
            if (!loadTileSpecs.equals(this.mTileSpecs) || currentUser != this.mCurrentUser) {
                this.mTiles.entrySet().stream().filter(new Predicate(loadTileSpecs) { // from class: com.android.systemui.qs.-$$Lambda$QSTileHost$tL3GWCpuev-DvXg1noj_yj7fk3Y
                    public final /* synthetic */ List f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // java.util.function.Predicate
                    public final boolean test(Object obj) {
                        return QSTileHost.lambda$onTuningChanged$2(this.f$0, (Map.Entry) obj);
                    }
                }).forEach(new Consumer() { // from class: com.android.systemui.qs.-$$Lambda$QSTileHost$nV3a9GzHlwmibkt4wOBaCI5DZk8
                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        QSTileHost.this.lambda$onTuningChanged$3$QSTileHost((Map.Entry) obj);
                    }
                });
                LinkedHashMap linkedHashMap = new LinkedHashMap();
                Iterator<String> it = loadTileSpecs.iterator();
                while (it.hasNext()) {
                    String next = it.next();
                    QSTile qSTile = this.mTiles.get(next);
                    if (!next.startsWith("custom(")) {
                        next = next.toLowerCase();
                    }
                    if (qSTile == null || (((z = qSTile instanceof CustomTile)) && ((CustomTile) qSTile).getUser() != currentUser)) {
                        if (qSTile != null) {
                            qSTile.destroy();
                            Log.d("QSTileHost", "Destroying tile for wrong user: " + next);
                            this.mQSLogger.logTileDestroyed(next, "Tile for wrong user");
                        }
                        Log.d("QSTileHost", "Creating tile: " + next);
                        try {
                            QSTile createTile = createTile(next);
                            if (createTile != null) {
                                createTile.setTileSpec(next);
                                if (createTile.isAvailable()) {
                                    linkedHashMap.put(next, createTile);
                                    this.mQSLogger.logTileAdded(next);
                                } else {
                                    createTile.destroy();
                                    Log.d("QSTileHost", "Destroying not available tile: " + next);
                                    this.mQSLogger.logTileDestroyed(next, "Tile not available");
                                }
                            }
                        } catch (Throwable th) {
                            Log.w("QSTileHost", "Error creating tile for spec: " + next, th);
                        }
                    } else if (qSTile.isAvailable()) {
                        if (DEBUG) {
                            Log.d("QSTileHost", "Adding " + qSTile);
                        }
                        qSTile.removeCallbacks();
                        if (!z && this.mCurrentUser != currentUser) {
                            qSTile.userSwitch(currentUser);
                        }
                        linkedHashMap.put(next, qSTile);
                        this.mQSLogger.logTileAdded(next);
                    } else {
                        qSTile.destroy();
                        Log.d("QSTileHost", "Destroying not available tile: " + next);
                        this.mQSLogger.logTileDestroyed(next, "Tile not available");
                    }
                }
                this.mCurrentUser = currentUser;
                ArrayList arrayList = new ArrayList(this.mTileSpecs);
                this.mTileSpecs.clear();
                this.mTileSpecs.addAll(loadTileSpecs);
                this.mTiles.clear();
                this.mTiles.putAll(linkedHashMap);
                if (!linkedHashMap.isEmpty() || loadTileSpecs.isEmpty()) {
                    for (int i = 0; i < this.mCallbacks.size(); i++) {
                        this.mCallbacks.get(i).onTilesChanged();
                    }
                    return;
                }
                Log.d("QSTileHost", "No valid tiles on tuning changed. Setting to default.");
                changeTiles(arrayList, loadTileSpecs(this.mContext, ""));
            }
        }
    }

    static /* synthetic */ boolean lambda$onTuningChanged$2(List list, Map.Entry entry) {
        return !list.contains(entry.getKey());
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onTuningChanged$3 */
    public /* synthetic */ void lambda$onTuningChanged$3$QSTileHost(Map.Entry entry) {
        Log.d("QSTileHost", "Destroying tile: " + ((String) entry.getKey()));
        this.mQSLogger.logTileDestroyed((String) entry.getKey(), "Tile removed");
        ((QSTile) entry.getValue()).destroy();
    }

    @Override // com.android.systemui.qs.QSHost
    public void removeTile(String str) {
        if (str != null) {
            changeTileSpecs(new Predicate(str) { // from class: com.android.systemui.qs.-$$Lambda$QSTileHost$lvnGvThFo7-HeGkbFqhwU9KCtaQ
                public final /* synthetic */ String f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return ((List) obj).remove(this.f$0);
                }
            });
        }
    }

    @Override // com.android.systemui.qs.QSHost
    public void unmarkTileAsAutoAdded(String str) {
        AutoTileManager autoTileManager = this.mAutoTiles;
        if (autoTileManager != null) {
            autoTileManager.unmarkTileAsAutoAdded(str);
        }
    }

    static /* synthetic */ boolean lambda$addTile$5(String str, List list) {
        return !list.contains(str) && list.add(str);
    }

    public void addTile(String str) {
        changeTileSpecs(new Predicate(str) { // from class: com.android.systemui.qs.-$$Lambda$QSTileHost$iiTl64od8Xx0qaz8exmdhzyHaWg
            public final /* synthetic */ String f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return QSTileHost.lambda$addTile$5(this.f$0, (List) obj);
            }
        });
    }

    private void saveTilesToSettings(List<String> list) {
        Settings.Secure.putStringForUser(this.mContext.getContentResolver(), "sysui_qs_tiles", TextUtils.join(",", list), null, false, this.mCurrentUser, true);
    }

    private void changeTileSpecs(Predicate<List<String>> predicate) {
        List<String> loadTileSpecs = loadTileSpecs(this.mContext, Settings.Secure.getStringForUser(this.mContext.getContentResolver(), "sysui_qs_tiles", this.mCurrentUser));
        if (predicate.test(loadTileSpecs)) {
            saveTilesToSettings(loadTileSpecs);
        }
    }

    public void addTile(ComponentName componentName) {
        addTile(componentName, false);
    }

    public void addTile(ComponentName componentName, boolean z) {
        String spec = CustomTile.toSpec(componentName);
        if (!this.mTileSpecs.contains(spec)) {
            ArrayList arrayList = new ArrayList(this.mTileSpecs);
            if (z) {
                arrayList.add(spec);
            } else {
                arrayList.add(0, spec);
            }
            changeTiles(this.mTileSpecs, arrayList);
        }
    }

    public void removeTile(ComponentName componentName) {
        ArrayList arrayList = new ArrayList(this.mTileSpecs);
        arrayList.remove(CustomTile.toSpec(componentName));
        changeTiles(this.mTileSpecs, arrayList);
    }

    public void changeTiles(List<String> list, List<String> list2) {
        if (list == null || !list.equals(list2)) {
            ArrayList arrayList = new ArrayList(list);
            int size = arrayList.size();
            for (int i = 0; i < size; i++) {
                String str = (String) arrayList.get(i);
                if (str.startsWith("custom(") && !list2.contains(str)) {
                    ComponentName componentFromSpec = CustomTile.getComponentFromSpec(str);
                    TileLifecycleManager tileLifecycleManager = new TileLifecycleManager(new Handler(), this.mContext, this.mServices, new Tile(), new Intent().setComponent(componentFromSpec), new UserHandle(this.mCurrentUser), this.mBroadcastDispatcher);
                    tileLifecycleManager.onStopListening();
                    tileLifecycleManager.onTileRemoved();
                    TileLifecycleManager.setTileAdded(this.mContext, componentFromSpec, false);
                    tileLifecycleManager.flushMessagesAndUnbind();
                }
            }
            if (DEBUG) {
                Log.d("QSTileHost", "saveCurrentTiles " + list2);
            }
            saveTilesToSettings(list2);
        } else if (DEBUG) {
            Log.d("QSTileHost", "changeTiles: no change skip. tiles=" + list);
        }
    }

    public QSTile createTile(String str) {
        for (int i = 0; i < this.mQsFactories.size(); i++) {
            QSTile createTile = this.mQsFactories.get(i).createTile(str);
            if (createTile != null) {
                return createTile;
            }
        }
        return null;
    }

    public QSTileView createTileView(QSTile qSTile, boolean z) {
        for (int i = 0; i < this.mQsFactories.size(); i++) {
            QSTileView createTileView = this.mQsFactories.get(i).createTileView(qSTile, z);
            if (createTileView != null) {
                return createTileView;
            }
        }
        throw new RuntimeException("Default factory didn't create view for " + qSTile.getTileSpec());
    }

    protected static List<String> loadTileSpecs(Context context, String str) {
        Resources resources = context.getResources();
        if (OpUtils.isCurrentGuest(context)) {
            str = resources.getString(C0015R$string.quick_settings_tiles_guest);
            if (DEBUG) {
                Log.d("QSTileHost", "Loaded tile specs of guest from config: " + str);
            }
        }
        if (TextUtils.isEmpty(str)) {
            str = resources.getString(C0015R$string.quick_settings_tiles);
            if (DEBUG) {
                Log.d("QSTileHost", "Loaded tile specs from config: " + str);
            }
        } else if (DEBUG) {
            Log.d("QSTileHost", "Loaded tile specs from setting: " + str);
        }
        ArrayList arrayList = new ArrayList();
        ArraySet arraySet = new ArraySet();
        boolean z = false;
        for (String str2 : str.split(",")) {
            String trim = str2.trim();
            if (!trim.isEmpty()) {
                if (trim.equals("default")) {
                    if (!z) {
                        for (String str3 : getDefaultSpecs(context)) {
                            if (!arraySet.contains(str3)) {
                                arrayList.add(str3);
                                arraySet.add(str3);
                            }
                        }
                        z = true;
                    }
                } else if (!arraySet.contains(trim)) {
                    if (trim.equals("opdnd")) {
                        arrayList.add("dnd");
                    } else if (trim.equals("powersaving")) {
                        arrayList.add("battery");
                    } else {
                        arrayList.add(trim);
                        arraySet.add(trim);
                    }
                }
            }
        }
        return arrayList;
    }

    public static List<String> getDefaultSpecs(Context context) {
        ArrayList arrayList = new ArrayList();
        arrayList.addAll(Arrays.asList(context.getResources().getString(C0015R$string.quick_settings_tiles_default).split(",")));
        if (Build.IS_DEBUGGABLE) {
            arrayList.add("dbg:mem");
        }
        return arrayList;
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("QSTileHost:");
        this.mTiles.values().stream().filter($$Lambda$QSTileHost$w0YHlhMwIm7qnoeEO7kRZCq47o8.INSTANCE).forEach(new Consumer(fileDescriptor, printWriter, strArr) { // from class: com.android.systemui.qs.-$$Lambda$QSTileHost$8dGA3dPDXgH8k-YhV5jUASLKyAo
            public final /* synthetic */ FileDescriptor f$0;
            public final /* synthetic */ PrintWriter f$1;
            public final /* synthetic */ String[] f$2;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ((Dumpable) ((QSTile) obj)).dump(this.f$0, this.f$1, this.f$2);
            }
        });
    }

    static /* synthetic */ boolean lambda$dump$6(QSTile qSTile) {
        return qSTile instanceof Dumpable;
    }

    public boolean isNeedToHide(String str) {
        OperatorCustom operatorCustom = this.mOperatorCustom;
        if (operatorCustom != null && operatorCustom.mHideList != null && this.mOperatorCustom.mHideList.size() > 0 && !TextUtils.isEmpty(str) && this.mOperatorCustom.mHideList.contains(str)) {
            return true;
        }
        return false;
    }

    private class OperatorCustom {
        private List<String> mHideList;
        private HideTileBroadcastReceiver mHideTileReceiver;
        private boolean mHospotDisableByOperator;
        private final HotspotController.Callback mHotspotCallback;
        private int recordPosition;

        private OperatorCustom() {
            this.mHospotDisableByOperator = false;
            this.recordPosition = -2;
            this.mHideList = new ArrayList();
            this.mHotspotCallback = new HotspotController.Callback() { // from class: com.android.systemui.qs.QSTileHost.OperatorCustom.1
                @Override // com.android.systemui.statusbar.policy.HotspotController.Callback
                public void onHotspotChanged(boolean z, int i) {
                }

                @Override // com.android.systemui.statusbar.policy.HotspotController.Callback
                public void onOperatorHotspotChanged(boolean z) {
                    if (QSTileHost.DEBUG_ONEPLUS) {
                        Log.i("QSTileHost", "onOperatorHotspotChanged / disableByOperator:" + z + " / recordPosition:" + OperatorCustom.this.recordPosition);
                    }
                    if (OperatorCustom.this.recordPosition == -2) {
                        OperatorCustom operatorCustom = OperatorCustom.this;
                        operatorCustom.recordPosition = operatorCustom.existTile("hotspot");
                    }
                    OperatorCustom.this.mHospotDisableByOperator = z;
                    if (OperatorCustom.this.mHospotDisableByOperator) {
                        OperatorCustom operatorCustom2 = OperatorCustom.this;
                        operatorCustom2.recordPosition = operatorCustom2.existTile("hotspot");
                        QSTileHost.this.removeTile("hotspot");
                    } else if (OperatorCustom.this.mHospotDisableByOperator || OperatorCustom.this.recordPosition < 0) {
                        Log.i("QSTileHost", "onOperatorHotspotChanged / else / disableByOperator:" + z + " / existTile(HOTSPOT):" + OperatorCustom.this.existTile("hotspot"));
                    } else {
                        OperatorCustom operatorCustom3 = OperatorCustom.this;
                        operatorCustom3.addTileWithPosition("hotspot", operatorCustom3.recordPosition, false);
                    }
                }
            };
        }

        public void init() {
            ((HotspotController) Dependency.get(HotspotController.class)).addCallback(this.mHotspotCallback);
            String stringForUser = Settings.Secure.getStringForUser(QSTileHost.this.mContext.getContentResolver(), "op_sysui_qs_tiles_hide", ActivityManager.getCurrentUser());
            if (QSTileHost.DEBUG_ONEPLUS) {
                Log.d("QSTileHost", "hideTiles=" + stringForUser);
            }
            this.mHideList.clear();
            if (!TextUtils.isEmpty(stringForUser)) {
                String[] split = stringForUser.split(",");
                for (String str : split) {
                    if (!TextUtils.isEmpty(str)) {
                        String trim = str.trim();
                        this.mHideList.add(trim);
                        hideTile(trim, true, -1);
                    }
                }
            }
            this.mHideTileReceiver = new HideTileBroadcastReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("com.oneplus.systemui.qs.hide_tile");
            QSTileHost.this.mContext.registerReceiver(this.mHideTileReceiver, intentFilter, "android.permission.WRITE_SECURE_SETTINGS", null);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int existTile(String str) {
            return QSTileHost.loadTileSpecs(QSTileHost.this.mContext, Settings.Secure.getStringForUser(QSTileHost.this.mContext.getContentResolver(), "sysui_qs_tiles", ActivityManager.getCurrentUser())).indexOf(str);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void addTileWithPosition(String str, int i, boolean z) {
            List<String> loadTileSpecs = QSTileHost.loadTileSpecs(QSTileHost.this.mContext, Settings.Secure.getStringForUser(QSTileHost.this.mContext.getContentResolver(), "sysui_qs_tiles", ActivityManager.getCurrentUser()));
            if (z || !loadTileSpecs.contains(str)) {
                loadTileSpecs.remove(str);
                if (QSTileHost.DEBUG_ONEPLUS) {
                    Log.i("QSTileHost", "addTileWithPosition / position:" + i + " / tileSpecs.size():" + loadTileSpecs.size());
                }
                if (i >= 0 && i < loadTileSpecs.size()) {
                    loadTileSpecs.add(i, str);
                } else if (i >= loadTileSpecs.size()) {
                    loadTileSpecs.add(str);
                }
                Settings.Secure.putStringForUser(QSTileHost.this.mContext.getContentResolver(), "sysui_qs_tiles", TextUtils.join(",", loadTileSpecs), ActivityManager.getCurrentUser());
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void hideTile(String str, boolean z, int i) {
            if (z) {
                if (QSTileHost.this.mTileSpecs.contains(str)) {
                    ArrayList arrayList = new ArrayList(QSTileHost.this.mTileSpecs);
                    arrayList.remove(str);
                    QSTileHost qSTileHost = QSTileHost.this;
                    qSTileHost.changeTiles(qSTileHost.mTileSpecs, arrayList);
                }
            } else if (QSTileHost.this.mTileSpecs.size() <= 0) {
                addTileWithPosition(str, 0, true);
            } else {
                addTileWithPosition(str, i, true);
            }
        }

        public class HideTileBroadcastReceiver extends BroadcastReceiver {
            public HideTileBroadcastReceiver() {
            }

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (intent != null && "com.oneplus.systemui.qs.hide_tile".equals(intent.getAction())) {
                    String stringExtra = intent.getStringExtra("tile");
                    boolean booleanExtra = intent.getBooleanExtra("hide", false);
                    int intExtra = intent.getIntExtra("position", 100);
                    if (!TextUtils.isEmpty(stringExtra)) {
                        String trim = stringExtra.trim();
                        String stringForUser = Settings.Secure.getStringForUser(QSTileHost.this.mContext.getContentResolver(), "sysui_qs_tiles", ActivityManager.getCurrentUser());
                        List<String> loadTileSpecs = QSTileHost.loadTileSpecs(QSTileHost.this.mContext, stringForUser);
                        if (QSTileHost.DEBUG_ONEPLUS) {
                            Log.d("QSTileHost", "HideTileReceiver: setting=" + stringForUser);
                            Log.d("QSTileHost", "HideTileReceiver: tile=" + trim + ", hide=" + booleanExtra + ", pos=" + intExtra);
                            StringBuilder sb = new StringBuilder();
                            sb.append("HideTileReceiver: list=");
                            sb.append(QSTileHost.this.mTileSpecs);
                            Log.d("QSTileHost", sb.toString());
                            Log.d("QSTileHost", "HideTileReceiver: hide=" + OperatorCustom.this.mHideList);
                        }
                        if ("custom(com.amazon.dee.app/com.amazon.alexa.handsfree.settings.quicksettings.AlexaQuickSettingService)".equals(trim)) {
                            Log.d("QSTileHost", "edited: " + OpUtils.getIsEditTileBefore());
                            if (!OpUtils.isSupportDoubleTapAlexa()) {
                                Log.d("QSTileHost", "not support to add alexa tile");
                                return;
                            } else if (stringForUser == null || !OpUtils.getIsEditTileBefore()) {
                                OpUtils.setIsEditTileBefore(QSTileHost.this.mContext, true);
                            } else {
                                Log.d("QSTileHost", "not allow to add alexa tile");
                                return;
                            }
                        }
                        if (booleanExtra) {
                            OperatorCustom.this.mHideList.add(trim);
                            if (loadTileSpecs.contains(trim)) {
                                OperatorCustom.this.hideTile(trim, true, intExtra);
                            }
                        } else {
                            if (OperatorCustom.this.mHideList.contains(trim)) {
                                OperatorCustom.this.mHideList.remove(trim);
                            }
                            OperatorCustom.this.hideTile(trim, false, intExtra);
                        }
                        Settings.Secure.putStringForUser(QSTileHost.this.mContext.getContentResolver(), "op_sysui_qs_tiles_hide", TextUtils.join(",", OperatorCustom.this.mHideList), ActivityManager.getCurrentUser());
                    }
                }
            }
        }
    }
}
