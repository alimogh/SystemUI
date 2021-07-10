package com.oneplus.systemui.statusbar.phone;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import com.android.systemui.model.SysUiState;
import com.android.systemui.recents.OverviewProxyService;
import com.android.systemui.shared.plugins.PluginManager;
import com.android.systemui.statusbar.phone.EdgeBackGestureHandler;
public class OpEdgeBackGestureHandler extends EdgeBackGestureHandler {
    public OpEdgeBackGestureHandler(Context context, OverviewProxyService overviewProxyService, SysUiState sysUiState, PluginManager pluginManager) {
        super(context, overviewProxyService, sysUiState, pluginManager, new Runnable() { // from class: com.oneplus.systemui.statusbar.phone.OpEdgeBackGestureHandler.1
            @Override // java.lang.Runnable
            public void run() {
            }
        });
    }

    public void onNavigationBarHidden() {
        Log.d("OpEdgeBackGestureHandler", "onNavigationBarHidden");
        this.mIsHidden = true;
        onNavBarAttached();
    }

    public void onNavigationBarShow() {
        Log.d("OpEdgeBackGestureHandler", "onNavigationBarShow");
        this.mIsHidden = false;
        onNavBarDetached();
    }

    @Override // com.android.systemui.statusbar.phone.EdgeBackGestureHandler
    public void onConfigurationChanged(int i) {
        if (Build.DEBUG_ONEPLUS) {
            Log.d("OpEdgeBackGestureHandler", "OpEdgeBackGestureHandler onConfigurationChanged");
        }
        super.onConfigurationChanged(i);
    }
}
