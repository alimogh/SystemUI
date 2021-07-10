package com.android.systemui.qs.customize;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import android.widget.Button;
import com.android.systemui.C0015R$string;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSTileHost;
import com.android.systemui.qs.external.CustomTile;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;
public class TileQueryHelper {
    private final boolean DEBUG = Build.DEBUG_ONEPLUS;
    private ArrayList<QSTile> mAllTiles = new ArrayList<>();
    private final Executor mBgExecutor;
    private final Context mContext;
    private boolean mFinished;
    private TileStateListener mListener;
    private final Executor mMainExecutor;
    private final ArraySet<String> mSpecs = new ArraySet<>();
    private final ArrayList<TileInfo> mTiles = new ArrayList<>();

    public static class TileInfo {
        public boolean isSystem;
        public boolean isVisible = true;
        public String spec;
        public QSTile.State state;
    }

    public interface TileStateListener {
        void onTilesChanged(List<TileInfo> list);
    }

    public TileQueryHelper(Context context, Executor executor, Executor executor2) {
        if (this.DEBUG) {
            Log.i("TileQueryHelper", "new TileQueryHelper");
        }
        this.mContext = context;
        this.mMainExecutor = executor;
        this.mBgExecutor = executor2;
    }

    public void setListener(TileStateListener tileStateListener) {
        this.mListener = tileStateListener;
    }

    public void queryTiles(QSTileHost qSTileHost) {
        this.mTiles.clear();
        this.mSpecs.clear();
        this.mAllTiles.clear();
        this.mFinished = false;
        addCurrentAndStockTiles(qSTileHost);
        addPackageTiles(qSTileHost);
    }

    public boolean isFinished() {
        return this.mFinished;
    }

    private void addCurrentAndStockTiles(QSTileHost qSTileHost) {
        QSTile createTile;
        String string = this.mContext.getString(C0015R$string.quick_settings_tiles_stock);
        String string2 = Settings.Secure.getString(this.mContext.getContentResolver(), "sysui_qs_tiles");
        ArrayList arrayList = new ArrayList();
        if (string2 != null) {
            arrayList.addAll(Arrays.asList(string2.split(",")));
        } else {
            string2 = "";
        }
        String[] split = string.split(",");
        for (String str : split) {
            boolean z = false;
            for (String str2 : string2.split(",")) {
                if (str2.equals(str)) {
                    z = true;
                }
            }
            if (!z) {
                arrayList.add(str);
            }
        }
        if (Build.IS_DEBUGGABLE && !string2.contains("dbg:mem")) {
            arrayList.add("dbg:mem");
        }
        ArrayList arrayList2 = new ArrayList();
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            String str3 = (String) it.next();
            if (!str3.startsWith("custom(") && (createTile = qSTileHost.createTile(str3)) != null) {
                if (!createTile.isAvailable()) {
                    createTile.setTileSpec(str3);
                    createTile.destroy();
                } else if (!qSTileHost.isNeedToHide(str3)) {
                    createTile.setListening(this, true);
                    createTile.refreshState();
                    createTile.setListening(this, false);
                    createTile.setTileSpec(str3);
                    arrayList2.add(createTile);
                    this.mAllTiles.add(createTile);
                }
            }
        }
        this.mBgExecutor.execute(new Runnable(arrayList2) { // from class: com.android.systemui.qs.customize.-$$Lambda$TileQueryHelper$sMzDfkcNEMwHLLe95kLdEn4WPkc
            public final /* synthetic */ ArrayList f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                TileQueryHelper.this.lambda$addCurrentAndStockTiles$0$TileQueryHelper(this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$addCurrentAndStockTiles$0 */
    public /* synthetic */ void lambda$addCurrentAndStockTiles$0$TileQueryHelper(ArrayList arrayList) {
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            QSTile qSTile = (QSTile) it.next();
            QSTile.State copy = qSTile.getState().copy();
            copy.label = qSTile.getTileLabel();
            qSTile.destroy();
            addTile(qSTile.getTileSpec(), null, copy, true);
        }
        notifyTilesChanged(false);
    }

    private void addPackageTiles(QSTileHost qSTileHost) {
        this.mBgExecutor.execute(new Runnable(qSTileHost) { // from class: com.android.systemui.qs.customize.-$$Lambda$TileQueryHelper$-7aqDrq4N73id-i9gI_WE72bklw
            public final /* synthetic */ QSTileHost f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                TileQueryHelper.this.lambda$addPackageTiles$1$TileQueryHelper(this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$addPackageTiles$1 */
    public /* synthetic */ void lambda$addPackageTiles$1$TileQueryHelper(QSTileHost qSTileHost) {
        Collection<QSTile> tiles = qSTileHost.getTiles();
        PackageManager packageManager = this.mContext.getPackageManager();
        List<ResolveInfo> queryIntentServicesAsUser = packageManager.queryIntentServicesAsUser(new Intent("android.service.quicksettings.action.QS_TILE"), 0, ActivityManager.getCurrentUser());
        String string = this.mContext.getString(C0015R$string.quick_settings_tiles_stock);
        for (ResolveInfo resolveInfo : queryIntentServicesAsUser) {
            ComponentName componentName = new ComponentName(resolveInfo.serviceInfo.packageName, resolveInfo.serviceInfo.name);
            if (!string.contains(componentName.flattenToString())) {
                CharSequence loadLabel = resolveInfo.serviceInfo.applicationInfo.loadLabel(packageManager);
                String spec = CustomTile.toSpec(componentName);
                QSTile.State state = getState(tiles, spec);
                if (!qSTileHost.isNeedToHide(spec)) {
                    if (state != null) {
                        if (Build.DEBUG_ONEPLUS) {
                            Log.d("TileQueryHelper", "spec=" + spec + ", addPkgTile");
                        }
                        addTile(spec, loadLabel, state, false);
                    } else {
                        ServiceInfo serviceInfo = resolveInfo.serviceInfo;
                        if (serviceInfo.icon != 0 || serviceInfo.applicationInfo.icon != 0) {
                            if (Build.DEBUG_ONEPLUS) {
                                Log.d("TileQueryHelper", "spec=" + spec + ", serviceIcon=" + resolveInfo.serviceInfo.icon + ", appIcon=" + resolveInfo.serviceInfo.applicationInfo.icon);
                            }
                            Drawable loadIcon = resolveInfo.serviceInfo.loadIcon(packageManager);
                            if ("android.permission.BIND_QUICK_SETTINGS_TILE".equals(resolveInfo.serviceInfo.permission)) {
                                if (loadIcon != null) {
                                    loadIcon.mutate();
                                    loadIcon.setTint(this.mContext.getColor(17170443));
                                    CharSequence loadLabel2 = resolveInfo.serviceInfo.loadLabel(packageManager);
                                    if (Build.DEBUG_ONEPLUS) {
                                        Log.d("TileQueryHelper", "spec=" + spec + ", calling create. icon=" + loadIcon);
                                    }
                                    createStateAndAddTile(spec, loadIcon, loadLabel2 != null ? loadLabel2.toString() : "null", loadLabel);
                                } else if (Build.DEBUG_ONEPLUS) {
                                    Log.d("TileQueryHelper", "spec=" + spec + ", icon is null, skip.");
                                }
                            }
                        } else if (Build.DEBUG_ONEPLUS) {
                            Log.d("TileQueryHelper", "spec=" + spec + ", icons are 0. skip.");
                        }
                    }
                }
            }
        }
        notifyTilesChanged(true);
    }

    private void notifyTilesChanged(boolean z) {
        this.mMainExecutor.execute(new Runnable(new ArrayList(this.mTiles), z) { // from class: com.android.systemui.qs.customize.-$$Lambda$TileQueryHelper$td1yVFso44MefBPUi6jpDHx3Yoc
            public final /* synthetic */ ArrayList f$1;
            public final /* synthetic */ boolean f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.lang.Runnable
            public final void run() {
                TileQueryHelper.this.lambda$notifyTilesChanged$2$TileQueryHelper(this.f$1, this.f$2);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$notifyTilesChanged$2 */
    public /* synthetic */ void lambda$notifyTilesChanged$2$TileQueryHelper(ArrayList arrayList, boolean z) {
        TileStateListener tileStateListener = this.mListener;
        if (tileStateListener != null) {
            tileStateListener.onTilesChanged(arrayList);
        }
        this.mFinished = z;
        if (z) {
            TileStateListener tileStateListener2 = this.mListener;
            if (tileStateListener2 instanceof QSEditPageManager) {
                ((QSEditPageManager) tileStateListener2).recalcSpecs();
            }
        }
    }

    private QSTile.State getState(Collection<QSTile> collection, String str) {
        for (QSTile qSTile : collection) {
            if (str.equals(qSTile.getTileSpec())) {
                return qSTile.getState().copy();
            }
        }
        return null;
    }

    private void addTile(String str, CharSequence charSequence, QSTile.State state, boolean z) {
        if (!this.mSpecs.contains(str)) {
            if (Build.DEBUG_ONEPLUS) {
                Log.d("TileQueryHelper", "addTile: spec=" + str + ", isSystem=" + z);
            }
            TileInfo tileInfo = new TileInfo();
            tileInfo.state = state;
            state.dualTarget = false;
            state.expandedAccessibilityClassName = Button.class.getName();
            tileInfo.spec = str;
            QSTile.State state2 = tileInfo.state;
            if (z || TextUtils.equals(state.label, charSequence)) {
                charSequence = null;
            }
            state2.secondaryLabel = charSequence;
            tileInfo.isSystem = z;
            this.mTiles.add(tileInfo);
            this.mSpecs.add(str);
        }
    }

    private void createStateAndAddTile(String str, Drawable drawable, CharSequence charSequence, CharSequence charSequence2) {
        QSTile.State state = new QSTile.State();
        state.state = 1;
        state.label = charSequence;
        state.contentDescription = charSequence;
        state.icon = new QSTileImpl.DrawableIcon(drawable);
        addTile(str, charSequence2, state, false);
    }

    public void destroyTiles() {
        ArrayList<QSTile> arrayList = this.mAllTiles;
        if (arrayList != null) {
            Iterator<QSTile> it = arrayList.iterator();
            while (it.hasNext()) {
                it.next().destroy();
            }
            this.mAllTiles.clear();
        }
    }

    public void recalcEditPage() {
        TileStateListener tileStateListener = this.mListener;
        if (tileStateListener != null && this.mFinished && (tileStateListener instanceof QSEditPageManager)) {
            ((QSEditPageManager) tileStateListener).recalcEditPage();
        }
    }
}
