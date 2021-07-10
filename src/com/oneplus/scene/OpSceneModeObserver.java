package com.oneplus.scene;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.NavigationBarController;
import com.android.systemui.util.Utils;
import com.oneplus.plugin.OpLsState;
import java.util.ArrayList;
public class OpSceneModeObserver {
    private final ArrayList<Callback> mCallbacks = new ArrayList<>();
    private Context mContext = null;
    private boolean mIsInBrickMode = false;
    private NavigationBarController mNavigationBarController;
    private SettingsObserver mSettingsObserver;

    public interface Callback {
        void onBrickModeChanged();
    }

    public OpSceneModeObserver(Context context) {
        this.mContext = context;
        this.mNavigationBarController = (NavigationBarController) Dependency.get(NavigationBarController.class);
        SettingsObserver settingsObserver = new SettingsObserver(new Handler());
        this.mSettingsObserver = settingsObserver;
        settingsObserver.observe();
    }

    /* access modifiers changed from: private */
    public final class SettingsObserver extends ContentObserver {
        private final Uri mOpBrickModeStatusUri = Settings.Secure.getUriFor("op_breath_mode_status");

        SettingsObserver(Handler handler) {
            super(handler);
        }

        /* access modifiers changed from: package-private */
        public void observe() {
            OpSceneModeObserver.this.mContext.getContentResolver().registerContentObserver(this.mOpBrickModeStatusUri, false, this, -1);
            update(null);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri) {
            update(uri);
        }

        public void update(Uri uri) {
            boolean equals;
            ContentResolver contentResolver = OpSceneModeObserver.this.mContext.getContentResolver();
            if ((uri == null || this.mOpBrickModeStatusUri.equals(uri)) && OpSceneModeObserver.this.mIsInBrickMode != (equals = "1".equals(Settings.Secure.getStringForUser(contentResolver, "op_breath_mode_status", -2)))) {
                OpSceneModeObserver.this.mIsInBrickMode = equals;
                if (OpLsState.getInstance().getPhoneStatusBar() != null) {
                    OpLsState.getInstance().getPhoneStatusBar().onBrickModeChanged(equals);
                }
                if (!(OpSceneModeObserver.this.mNavigationBarController == null || OpSceneModeObserver.this.mNavigationBarController.getDefaultNavigationBarFragment() == null)) {
                    OpSceneModeObserver.this.mNavigationBarController.getDefaultNavigationBarFragment().onBrickModeChanged(equals);
                }
                Utils.safeForeach(OpSceneModeObserver.this.mCallbacks, $$Lambda$OpSceneModeObserver$SettingsObserver$TUm0lcGRNGDHGc16Oivsv0ulDQ.INSTANCE);
            }
            Log.d("OpSceneModeObserver", "update uri: " + uri + " mIsInBrickMode: " + OpSceneModeObserver.this.mIsInBrickMode);
        }
    }

    public boolean isInBrickMode() {
        return this.mIsInBrickMode;
    }

    public void addCallback(Callback callback) {
        this.mCallbacks.add(callback);
    }

    public void removeCallback(Callback callback) {
        this.mCallbacks.remove(callback);
    }
}
