package com.android.systemui.qs.external;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.metrics.LogMaker;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.service.quicksettings.IQSTileService;
import android.service.quicksettings.Tile;
import android.util.Log;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;
import android.widget.Switch;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.external.TileLifecycleManager;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import java.util.Objects;
import java.util.function.Supplier;
public class CustomTile extends QSTileImpl<QSTile.State> implements TileLifecycleManager.TileChangeListener {
    private static final boolean DEBUG = Build.DEBUG_ONEPLUS;
    private final ComponentName mComponent;
    private Icon mDefaultIcon;
    private CharSequence mDefaultLabel;
    private boolean mIsShowingDialog;
    private boolean mIsTokenGranted;
    private boolean mListening;
    private final IQSTileService mService;
    private final TileServiceManager mServiceManager;
    private final Tile mTile;
    private final IBinder mToken = new Binder();
    private final int mUser;
    private final Context mUserContext;
    private final IWindowManager mWindowManager = WindowManagerGlobal.getWindowManagerService();

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return 268;
    }

    private CustomTile(QSHost qSHost, String str, Context context) {
        super(qSHost);
        this.mComponent = ComponentName.unflattenFromString(str);
        this.mTile = new Tile();
        this.mUserContext = context;
        this.mUser = context.getUserId();
        updateDefaultTileAndIcon();
        TileServiceManager tileWrapper = qSHost.getTileServices().getTileWrapper(this);
        this.mServiceManager = tileWrapper;
        if (tileWrapper.isToggleableTile()) {
            resetStates();
        }
        this.mService = this.mServiceManager.getTileService();
        this.mServiceManager.setTileChangeListener(this);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public long getStaleTimeout() {
        return (((long) this.mHost.indexOf(getTileSpec())) * 60000) + 3600000;
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0041 A[Catch:{ NameNotFoundException -> 0x00d8 }] */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x004c A[Catch:{ NameNotFoundException -> 0x00d8 }] */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0053 A[Catch:{ NameNotFoundException -> 0x00d8 }] */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x00ac A[Catch:{ NameNotFoundException -> 0x00d8 }] */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00d2 A[Catch:{ NameNotFoundException -> 0x00d8 }] */
    /* JADX WARNING: Removed duplicated region for block: B:39:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateDefaultTileAndIcon() {
        /*
            r10 = this;
            r0 = 0
            android.content.Context r1 = r10.mUserContext     // Catch:{ NameNotFoundException -> 0x00d8 }
            android.content.pm.PackageManager r1 = r1.getPackageManager()     // Catch:{ NameNotFoundException -> 0x00d8 }
            r2 = 786432(0xc0000, float:1.102026E-39)
            boolean r3 = r10.isSystemApp(r1)     // Catch:{ NameNotFoundException -> 0x00d8 }
            if (r3 == 0) goto L_0x0012
            r2 = 786944(0xc0200, float:1.102743E-39)
        L_0x0012:
            android.content.ComponentName r3 = r10.mComponent     // Catch:{ NameNotFoundException -> 0x00d8 }
            android.content.pm.ServiceInfo r2 = r1.getServiceInfo(r3, r2)     // Catch:{ NameNotFoundException -> 0x00d8 }
            int r3 = r2.icon     // Catch:{ NameNotFoundException -> 0x00d8 }
            if (r3 == 0) goto L_0x001f
            int r3 = r2.icon     // Catch:{ NameNotFoundException -> 0x00d8 }
            goto L_0x0023
        L_0x001f:
            android.content.pm.ApplicationInfo r3 = r2.applicationInfo     // Catch:{ NameNotFoundException -> 0x00d8 }
            int r3 = r3.icon     // Catch:{ NameNotFoundException -> 0x00d8 }
        L_0x0023:
            android.service.quicksettings.Tile r4 = r10.mTile     // Catch:{ NameNotFoundException -> 0x00d8 }
            android.graphics.drawable.Icon r4 = r4.getIcon()     // Catch:{ NameNotFoundException -> 0x00d8 }
            r5 = 0
            r6 = 1
            if (r4 == 0) goto L_0x003e
            android.service.quicksettings.Tile r4 = r10.mTile     // Catch:{ NameNotFoundException -> 0x00d8 }
            android.graphics.drawable.Icon r4 = r4.getIcon()     // Catch:{ NameNotFoundException -> 0x00d8 }
            android.graphics.drawable.Icon r7 = r10.mDefaultIcon     // Catch:{ NameNotFoundException -> 0x00d8 }
            boolean r4 = r10.iconEquals(r4, r7)     // Catch:{ NameNotFoundException -> 0x00d8 }
            if (r4 == 0) goto L_0x003c
            goto L_0x003e
        L_0x003c:
            r4 = r5
            goto L_0x003f
        L_0x003e:
            r4 = r6
        L_0x003f:
            if (r3 == 0) goto L_0x004c
            android.content.ComponentName r7 = r10.mComponent     // Catch:{ NameNotFoundException -> 0x00d8 }
            java.lang.String r7 = r7.getPackageName()     // Catch:{ NameNotFoundException -> 0x00d8 }
            android.graphics.drawable.Icon r7 = android.graphics.drawable.Icon.createWithResource(r7, r3)     // Catch:{ NameNotFoundException -> 0x00d8 }
            goto L_0x004d
        L_0x004c:
            r7 = r0
        L_0x004d:
            r10.mDefaultIcon = r7     // Catch:{ NameNotFoundException -> 0x00d8 }
            boolean r7 = com.android.systemui.qs.external.CustomTile.DEBUG     // Catch:{ NameNotFoundException -> 0x00d8 }
            if (r7 == 0) goto L_0x00aa
            java.lang.String r7 = r10.TAG     // Catch:{ NameNotFoundException -> 0x00d8 }
            java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch:{ NameNotFoundException -> 0x00d8 }
            r8.<init>()     // Catch:{ NameNotFoundException -> 0x00d8 }
            java.lang.String r9 = "updateDefaultTileAndIcon: label="
            r8.append(r9)     // Catch:{ NameNotFoundException -> 0x00d8 }
            android.service.quicksettings.Tile r9 = r10.mTile     // Catch:{ NameNotFoundException -> 0x00d8 }
            java.lang.CharSequence r9 = r9.getLabel()     // Catch:{ NameNotFoundException -> 0x00d8 }
            r8.append(r9)     // Catch:{ NameNotFoundException -> 0x00d8 }
            java.lang.String r9 = ", icon="
            r8.append(r9)     // Catch:{ NameNotFoundException -> 0x00d8 }
            r8.append(r3)     // Catch:{ NameNotFoundException -> 0x00d8 }
            java.lang.String r3 = ", mDefaultIcon="
            r8.append(r3)     // Catch:{ NameNotFoundException -> 0x00d8 }
            android.graphics.drawable.Icon r3 = r10.mDefaultIcon     // Catch:{ NameNotFoundException -> 0x00d8 }
            r8.append(r3)     // Catch:{ NameNotFoundException -> 0x00d8 }
            java.lang.String r3 = ", info.icon="
            r8.append(r3)     // Catch:{ NameNotFoundException -> 0x00d8 }
            int r3 = r2.icon     // Catch:{ NameNotFoundException -> 0x00d8 }
            r8.append(r3)     // Catch:{ NameNotFoundException -> 0x00d8 }
            java.lang.String r3 = ", info.applicationInfo.icon="
            r8.append(r3)     // Catch:{ NameNotFoundException -> 0x00d8 }
            android.content.pm.ApplicationInfo r3 = r2.applicationInfo     // Catch:{ NameNotFoundException -> 0x00d8 }
            if (r3 == 0) goto L_0x0097
            android.content.pm.ApplicationInfo r3 = r2.applicationInfo     // Catch:{ NameNotFoundException -> 0x00d8 }
            int r3 = r3.icon     // Catch:{ NameNotFoundException -> 0x00d8 }
            java.lang.Integer r3 = java.lang.Integer.valueOf(r3)     // Catch:{ NameNotFoundException -> 0x00d8 }
            goto L_0x0098
        L_0x0097:
            r3 = r0
        L_0x0098:
            r8.append(r3)     // Catch:{ NameNotFoundException -> 0x00d8 }
            java.lang.String r3 = ", updateIcon="
            r8.append(r3)     // Catch:{ NameNotFoundException -> 0x00d8 }
            r8.append(r4)     // Catch:{ NameNotFoundException -> 0x00d8 }
            java.lang.String r3 = r8.toString()     // Catch:{ NameNotFoundException -> 0x00d8 }
            android.util.Log.d(r7, r3)     // Catch:{ NameNotFoundException -> 0x00d8 }
        L_0x00aa:
            if (r4 == 0) goto L_0x00b3
            android.service.quicksettings.Tile r3 = r10.mTile     // Catch:{ NameNotFoundException -> 0x00d8 }
            android.graphics.drawable.Icon r4 = r10.mDefaultIcon     // Catch:{ NameNotFoundException -> 0x00d8 }
            r3.setIcon(r4)     // Catch:{ NameNotFoundException -> 0x00d8 }
        L_0x00b3:
            android.service.quicksettings.Tile r3 = r10.mTile     // Catch:{ NameNotFoundException -> 0x00d8 }
            java.lang.CharSequence r3 = r3.getLabel()     // Catch:{ NameNotFoundException -> 0x00d8 }
            if (r3 == 0) goto L_0x00c9
            android.service.quicksettings.Tile r3 = r10.mTile     // Catch:{ NameNotFoundException -> 0x00d8 }
            java.lang.CharSequence r3 = r3.getLabel()     // Catch:{ NameNotFoundException -> 0x00d8 }
            java.lang.CharSequence r4 = r10.mDefaultLabel     // Catch:{ NameNotFoundException -> 0x00d8 }
            boolean r3 = android.text.TextUtils.equals(r3, r4)     // Catch:{ NameNotFoundException -> 0x00d8 }
            if (r3 == 0) goto L_0x00ca
        L_0x00c9:
            r5 = r6
        L_0x00ca:
            java.lang.CharSequence r1 = r2.loadLabel(r1)     // Catch:{ NameNotFoundException -> 0x00d8 }
            r10.mDefaultLabel = r1     // Catch:{ NameNotFoundException -> 0x00d8 }
            if (r5 == 0) goto L_0x00dc
            android.service.quicksettings.Tile r2 = r10.mTile     // Catch:{ NameNotFoundException -> 0x00d8 }
            r2.setLabel(r1)     // Catch:{ NameNotFoundException -> 0x00d8 }
            goto L_0x00dc
        L_0x00d8:
            r10.mDefaultIcon = r0
            r10.mDefaultLabel = r0
        L_0x00dc:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.qs.external.CustomTile.updateDefaultTileAndIcon():void");
    }

    private boolean isSystemApp(PackageManager packageManager) throws PackageManager.NameNotFoundException {
        return packageManager.getApplicationInfo(this.mComponent.getPackageName(), 0).isSystemApp();
    }

    private boolean iconEquals(Icon icon, Icon icon2) {
        if (icon == icon2) {
            return true;
        }
        return icon != null && icon2 != null && icon.getType() == 2 && icon2.getType() == 2 && icon.getResId() == icon2.getResId() && Objects.equals(icon.getResPackage(), icon2.getResPackage());
    }

    @Override // com.android.systemui.qs.external.TileLifecycleManager.TileChangeListener
    public void onTileChanged(ComponentName componentName) {
        updateDefaultTileAndIcon();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public boolean isAvailable() {
        return this.mDefaultIcon != null;
    }

    public int getUser() {
        return this.mUser;
    }

    public ComponentName getComponent() {
        return this.mComponent;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public LogMaker populate(LogMaker logMaker) {
        return super.populate(logMaker).setComponentName(this.mComponent);
    }

    public Tile getQsTile() {
        updateDefaultTileAndIcon();
        return this.mTile;
    }

    public void updateState(Tile tile) {
        if (DEBUG) {
            String str = this.TAG;
            Log.d(str, "updateState label=" + ((Object) tile.getLabel()) + " state=" + tile.getState() + " icon=" + tile.getIcon());
        }
        this.mTile.setIcon(tile.getIcon());
        this.mTile.setLabel(tile.getLabel());
        this.mTile.setSubtitle(tile.getSubtitle());
        this.mTile.setContentDescription(tile.getContentDescription());
        this.mTile.setStateDescription(tile.getStateDescription());
        this.mTile.setState(tile.getState());
        refreshState();
    }

    public void onDialogShown() {
        this.mIsShowingDialog = true;
    }

    public void onDialogHidden() {
        this.mIsShowingDialog = false;
        try {
            if (DEBUG) {
                Log.d(this.TAG, "Removing token");
            }
            this.mWindowManager.removeWindowToken(this.mToken, 0);
        } catch (RemoteException unused) {
        }
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
        super.handleSetListening(z);
        if (this.mListening != z) {
            this.mListening = z;
            if (z) {
                try {
                    updateDefaultTileAndIcon();
                    refreshState();
                    if (!this.mServiceManager.isActiveTile()) {
                        this.mServiceManager.setBindRequested(true);
                        this.mService.onStartListening();
                    }
                } catch (RemoteException unused) {
                }
            } else {
                this.mService.onStopListening();
                if (this.mIsTokenGranted && !this.mIsShowingDialog) {
                    try {
                        if (DEBUG) {
                            Log.d(this.TAG, "Removing token");
                        }
                        this.mWindowManager.removeWindowToken(this.mToken, 0);
                    } catch (RemoteException unused2) {
                    }
                    this.mIsTokenGranted = false;
                }
                this.mIsShowingDialog = false;
                this.mServiceManager.setBindRequested(false);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleDestroy() {
        super.handleDestroy();
        if (this.mIsTokenGranted) {
            try {
                if (DEBUG) {
                    Log.d(this.TAG, "Removing token");
                }
                this.mWindowManager.removeWindowToken(this.mToken, 0);
            } catch (RemoteException unused) {
            }
        }
        this.mHost.getTileServices().freeService(this, this.mServiceManager);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.State newTileState() {
        TileServiceManager tileServiceManager = this.mServiceManager;
        if (tileServiceManager == null || !tileServiceManager.isToggleableTile()) {
            return new QSTile.State();
        }
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        Intent intent = new Intent("android.service.quicksettings.action.QS_TILE_PREFERENCES");
        intent.setPackage(this.mComponent.getPackageName());
        Intent resolveIntent = resolveIntent(intent);
        if (resolveIntent == null) {
            return new Intent("android.settings.APPLICATION_DETAILS_SETTINGS").setData(Uri.fromParts("package", this.mComponent.getPackageName(), null));
        }
        resolveIntent.putExtra("android.intent.extra.COMPONENT_NAME", this.mComponent);
        resolveIntent.putExtra("state", this.mTile.getState());
        return resolveIntent;
    }

    private Intent resolveIntent(Intent intent) {
        ResolveInfo resolveActivityAsUser = this.mContext.getPackageManager().resolveActivityAsUser(intent, 0, ActivityManager.getCurrentUser());
        if (resolveActivityAsUser == null) {
            return null;
        }
        Intent intent2 = new Intent("android.service.quicksettings.action.QS_TILE_PREFERENCES");
        ActivityInfo activityInfo = resolveActivityAsUser.activityInfo;
        return intent2.setClassName(activityInfo.packageName, activityInfo.name);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        if (this.mTile.getState() != 0) {
            try {
                if (DEBUG) {
                    Log.d(this.TAG, "Adding token");
                }
                this.mWindowManager.addWindowToken(this.mToken, 2035, 0);
                this.mIsTokenGranted = true;
            } catch (RemoteException unused) {
            }
            try {
                if (DEBUG) {
                    Log.d(this.TAG, "isActiveTile");
                }
                if (this.mServiceManager.isActiveTile()) {
                    if (DEBUG) {
                        Log.d(this.TAG, "setBindRequested");
                    }
                    this.mServiceManager.setBindRequested(true);
                    if (DEBUG) {
                        Log.d(this.TAG, "onStartListening");
                    }
                    this.mService.onStartListening();
                }
                if (DEBUG) {
                    Log.d(this.TAG, "onClick");
                }
                this.mService.onClick(this.mToken);
            } catch (RemoteException unused2) {
            }
        }
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return getState().label;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleUpdateState(QSTile.State state, Object obj) {
        Drawable drawable;
        int state2 = this.mTile.getState();
        if (this.mServiceManager.hasPendingBind() && Build.DEBUG_ONEPLUS) {
            String str = this.TAG;
            Log.d(str, "has pending bind. tileState=" + state2);
        }
        state.state = state2;
        boolean z = false;
        try {
            drawable = this.mTile.getIcon().loadDrawable(this.mUserContext);
        } catch (Exception unused) {
            Log.w(this.TAG, "Invalid icon, forcing into unavailable state");
            state.state = 0;
            drawable = this.mDefaultIcon.loadDrawable(this.mUserContext);
        }
        state.iconSupplier = new Supplier(drawable) { // from class: com.android.systemui.qs.external.-$$Lambda$CustomTile$Oh-NzDEMM2yCWnVYbU2_DKTzaqo
            public final /* synthetic */ Drawable f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Supplier
            public final Object get() {
                return CustomTile.lambda$handleUpdateState$0(this.f$0);
            }
        };
        state.label = this.mTile.getLabel();
        CharSequence subtitle = this.mTile.getSubtitle();
        if (subtitle == null || subtitle.length() <= 0) {
            state.secondaryLabel = null;
        } else {
            state.secondaryLabel = subtitle;
        }
        if (this.mTile.getContentDescription() != null) {
            state.contentDescription = this.mTile.getContentDescription();
        } else {
            state.contentDescription = state.label;
        }
        if (this.mTile.getStateDescription() != null) {
            state.stateDescription = this.mTile.getStateDescription();
        } else {
            state.stateDescription = null;
        }
        if (state instanceof QSTile.BooleanState) {
            state.expandedAccessibilityClassName = Switch.class.getName();
            QSTile.BooleanState booleanState = (QSTile.BooleanState) state;
            if (state.state == 2) {
                z = true;
            }
            booleanState.value = z;
        }
    }

    static /* synthetic */ QSTile.Icon lambda$handleUpdateState$0(Drawable drawable) {
        Drawable.ConstantState constantState;
        if (drawable == null || (constantState = drawable.getConstantState()) == null) {
            return null;
        }
        return new QSTileImpl.DrawableIcon(constantState.newDrawable());
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public final String getMetricsSpec() {
        return this.mComponent.getPackageName();
    }

    public void startUnlockAndRun() {
        ((ActivityStarter) Dependency.get(ActivityStarter.class)).postQSRunnableDismissingKeyguard(new Runnable() { // from class: com.android.systemui.qs.external.-$$Lambda$CustomTile$q1MKWZaaapZOjYFe9CyeyabLR0Q
            @Override // java.lang.Runnable
            public final void run() {
                CustomTile.this.lambda$startUnlockAndRun$1$CustomTile();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startUnlockAndRun$1 */
    public /* synthetic */ void lambda$startUnlockAndRun$1$CustomTile() {
        try {
            this.mService.onUnlockComplete();
        } catch (RemoteException unused) {
        }
    }

    public static String toSpec(ComponentName componentName) {
        return "custom(" + componentName.flattenToShortString() + ")";
    }

    public static ComponentName getComponentFromSpec(String str) {
        String substring = str.substring(7, str.length() - 1);
        if (!substring.isEmpty()) {
            return ComponentName.unflattenFromString(substring);
        }
        throw new IllegalArgumentException("Empty custom tile spec action");
    }

    public static CustomTile create(QSHost qSHost, String str, Context context) {
        if (str == null || !str.startsWith("custom(") || !str.endsWith(")")) {
            throw new IllegalArgumentException("Bad custom tile spec: " + str);
        }
        String substring = str.substring(7, str.length() - 1);
        if (!substring.isEmpty()) {
            return new CustomTile(qSHost, substring, context);
        }
        throw new IllegalArgumentException("Empty custom tile spec action");
    }
}
