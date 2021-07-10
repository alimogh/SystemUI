package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.LocaleList;
import android.util.Log;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.oneplus.systemui.biometrics.OpFodHelper;
import com.oneplus.util.OpUtils;
import java.util.ArrayList;
import java.util.List;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: ConfigurationControllerImpl.kt */
public final class ConfigurationControllerImpl implements ConfigurationController {
    private final Context context;
    private int density;
    private float fontScale;
    private final boolean inCarMode;
    private final Configuration lastConfig = new Configuration();
    private final List<ConfigurationController.ConfigurationListener> listeners = new ArrayList();
    private LocaleList localeList;
    private boolean mIsSpecialTheme;
    private int mOrientation;
    private int mSmallestScreenWidthDp;
    private int uiMode;

    public ConfigurationControllerImpl(@NotNull Context context) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Resources resources = context.getResources();
        Intrinsics.checkExpressionValueIsNotNull(resources, "context.resources");
        Configuration configuration = resources.getConfiguration();
        this.context = context;
        this.fontScale = configuration.fontScale;
        this.density = configuration.densityDpi;
        this.inCarMode = (configuration.uiMode & 15) == 3;
        this.uiMode = configuration.uiMode & 48;
        Intrinsics.checkExpressionValueIsNotNull(configuration, "currentConfig");
        this.localeList = configuration.getLocales();
        this.mSmallestScreenWidthDp = configuration.smallestScreenWidthDp;
        this.mOrientation = configuration.orientation;
        OpFodHelper.init(this.context);
        this.mIsSpecialTheme = OpUtils.isSpecialTheme(this.context);
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController
    public void notifyThemeChanged() {
        for (ConfigurationController.ConfigurationListener configurationListener : new ArrayList(this.listeners)) {
            if (this.listeners.contains(configurationListener)) {
                configurationListener.onThemeChanged();
            }
        }
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController
    public void onConfigurationChanged(@NotNull Configuration configuration) {
        Intrinsics.checkParameterIsNotNull(configuration, "newConfig");
        ArrayList<ConfigurationController.ConfigurationListener> arrayList = new ArrayList(this.listeners);
        for (ConfigurationController.ConfigurationListener configurationListener : arrayList) {
            if (this.listeners.contains(configurationListener)) {
                configurationListener.onConfigChanged(configuration);
            }
        }
        float f = configuration.fontScale;
        int i = configuration.densityDpi;
        int i2 = configuration.uiMode & 48;
        boolean isSpecialTheme = OpUtils.isSpecialTheme(this.context);
        boolean z = (i2 == this.uiMode && isSpecialTheme == this.mIsSpecialTheme) ? false : true;
        int i3 = configuration.orientation;
        if (OpUtils.isCustomFingerprint()) {
            OpFodHelper instance = OpFodHelper.getInstance();
            KeyguardUpdateMonitor keyguardUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
            Log.i("ConfigurationControllerImpl", "old: " + this.mOrientation + ", new: " + i3);
            if (!(keyguardUpdateMonitor == null || instance == null || !instance.isFingerprintDetecting() || i3 == this.mOrientation)) {
                keyguardUpdateMonitor.updateFodIconVisibility();
            }
        }
        this.mOrientation = i3;
        Log.d("ConfigurationController", "onConfigurationChanged, oldUiMode: " + this.uiMode + ", newUiMode: " + i2 + ", oldSpecialTheme = " + this.mIsSpecialTheme + ", newSpecialTheme = " + isSpecialTheme);
        if (!(i == this.density && f == this.fontScale && (!(this.inCarMode && z) && this.mSmallestScreenWidthDp == configuration.smallestScreenWidthDp))) {
            OpUtils.updateDensityDpi(i);
            OpUtils.updateScreenResolutionManually(this.context);
            for (ConfigurationController.ConfigurationListener configurationListener2 : arrayList) {
                if (this.listeners.contains(configurationListener2)) {
                    configurationListener2.onDensityOrFontScaleChanged();
                }
            }
            this.density = i;
            this.fontScale = f;
            this.mSmallestScreenWidthDp = configuration.smallestScreenWidthDp;
        }
        LocaleList locales = configuration.getLocales();
        if (!Intrinsics.areEqual(locales, this.localeList)) {
            this.localeList = locales;
            for (ConfigurationController.ConfigurationListener configurationListener3 : arrayList) {
                if (this.listeners.contains(configurationListener3) && configurationListener3 != null) {
                    configurationListener3.onLocaleListChanged();
                }
            }
        }
        if (z) {
            Log.d("ConfigurationController", "onConfigurationChanged, trigger onUiModeChanged for listeners");
            this.context.getTheme().applyStyle(this.context.getThemeResId(), true);
            this.uiMode = i2;
            this.mIsSpecialTheme = isSpecialTheme;
            for (ConfigurationController.ConfigurationListener configurationListener4 : arrayList) {
                if (this.listeners.contains(configurationListener4)) {
                    configurationListener4.onUiModeChanged();
                }
            }
        }
        if ((this.lastConfig.updateFrom(configuration) & Integer.MIN_VALUE) != 0) {
            for (ConfigurationController.ConfigurationListener configurationListener5 : arrayList) {
                if (this.listeners.contains(configurationListener5)) {
                    configurationListener5.onOverlayChanged();
                }
            }
        }
    }

    public void addCallback(@NotNull ConfigurationController.ConfigurationListener configurationListener) {
        Intrinsics.checkParameterIsNotNull(configurationListener, "listener");
        this.listeners.add(configurationListener);
        configurationListener.onDensityOrFontScaleChanged();
    }

    public void removeCallback(@NotNull ConfigurationController.ConfigurationListener configurationListener) {
        Intrinsics.checkParameterIsNotNull(configurationListener, "listener");
        this.listeners.remove(configurationListener);
    }
}
