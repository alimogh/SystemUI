package com.android.systemui.globalactions;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.OpFeatures;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.internal.colorextraction.drawable.ScrimDrawable;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0016R$style;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.GlobalActions;
import com.android.systemui.plugins.GlobalActionsPanelPlugin;
import com.android.systemui.statusbar.BlurUtils;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.statusbar.policy.ExtensionController;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.oneplus.util.OpUtils;
import dagger.Lazy;
public class GlobalActionsImpl implements GlobalActions, CommandQueue.Callbacks {
    private final BlurUtils mBlurUtils;
    private final CommandQueue mCommandQueue;
    private final Context mContext;
    private final DeviceProvisionedController mDeviceProvisionedController = ((DeviceProvisionedController) Dependency.get(DeviceProvisionedController.class));
    private boolean mDisabled;
    private GlobalActionsDialog mGlobalActionsDialog;
    private final Lazy<GlobalActionsDialog> mGlobalActionsDialogLazy;
    private final KeyguardStateController mKeyguardStateController = ((KeyguardStateController) Dependency.get(KeyguardStateController.class));
    private final ExtensionController.Extension<GlobalActionsPanelPlugin> mWalletPluginProvider;

    public GlobalActionsImpl(Context context, CommandQueue commandQueue, Lazy<GlobalActionsDialog> lazy, BlurUtils blurUtils) {
        this.mContext = context;
        this.mGlobalActionsDialogLazy = lazy;
        this.mCommandQueue = commandQueue;
        this.mBlurUtils = blurUtils;
        commandQueue.addCallback((CommandQueue.Callbacks) this);
        ExtensionController.ExtensionBuilder newExtension = ((ExtensionController) Dependency.get(ExtensionController.class)).newExtension(GlobalActionsPanelPlugin.class);
        newExtension.withPlugin(GlobalActionsPanelPlugin.class);
        this.mWalletPluginProvider = newExtension.build();
    }

    @Override // com.android.systemui.plugins.GlobalActions
    public void destroy() {
        this.mCommandQueue.removeCallback((CommandQueue.Callbacks) this);
        GlobalActionsDialog globalActionsDialog = this.mGlobalActionsDialog;
        if (globalActionsDialog != null) {
            globalActionsDialog.destroy();
            this.mGlobalActionsDialog = null;
        }
    }

    @Override // com.android.systemui.plugins.GlobalActions
    public void showGlobalActions(GlobalActions.GlobalActionsManager globalActionsManager) {
        if (!this.mDisabled) {
            GlobalActionsDialog globalActionsDialog = this.mGlobalActionsDialogLazy.get();
            this.mGlobalActionsDialog = globalActionsDialog;
            globalActionsDialog.showOrHideDialog(this.mKeyguardStateController.isShowing(), this.mDeviceProvisionedController.isDeviceProvisioned(), this.mWalletPluginProvider.get());
            ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).requestFaceAuth();
        }
    }

    @Override // com.android.systemui.plugins.GlobalActions
    public void showShutdownUi(boolean z, String str) {
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).notifyShutDownOrReboot();
        if (!hasCustomizedShutdownAnim()) {
            Drawable scrimDrawable = new ScrimDrawable();
            Dialog dialog = new Dialog(this.mContext, C0016R$style.Theme_SystemUI_Dialog_GlobalActions);
            dialog.setOnShowListener(new DialogInterface.OnShowListener(scrimDrawable, dialog) { // from class: com.android.systemui.globalactions.-$$Lambda$GlobalActionsImpl$FGsviRHv4VNB5P1rRHu2A1JPouY
                public final /* synthetic */ ScrimDrawable f$1;
                public final /* synthetic */ Dialog f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // android.content.DialogInterface.OnShowListener
                public final void onShow(DialogInterface dialogInterface) {
                    GlobalActionsImpl.this.lambda$showShutdownUi$0$GlobalActionsImpl(this.f$1, this.f$2, dialogInterface);
                }
            });
            Window window = dialog.getWindow();
            window.requestFeature(1);
            window.getAttributes().systemUiVisibility |= 1792;
            window.getDecorView();
            window.getAttributes().width = -1;
            window.getAttributes().height = -1;
            window.getAttributes().layoutInDisplayCutoutMode = 3;
            window.setType(2020);
            window.getAttributes().setFitInsetsTypes(0);
            window.clearFlags(2);
            window.addFlags(17629472);
            window.setBackgroundDrawable(scrimDrawable);
            window.setWindowAnimations(C0016R$style.Animation_ShutdownUi);
            dialog.setContentView(17367302);
            dialog.setCancelable(false);
            ProgressBar progressBar = (ProgressBar) dialog.findViewById(16908301);
            if (progressBar != null) {
                progressBar.getIndeterminateDrawable().setTint(-1);
            }
            TextView textView = (TextView) dialog.findViewById(16908308);
            TextView textView2 = (TextView) dialog.findViewById(16908309);
            textView.setTextColor(-1);
            textView2.setTextColor(-1);
            textView2.setText(getRebootMessage(z, str));
            String reasonMessage = getReasonMessage(str);
            if (reasonMessage != null) {
                textView.setVisibility(0);
                textView.setText(reasonMessage);
            }
            dialog.show();
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$showShutdownUi$0 */
    public /* synthetic */ void lambda$showShutdownUi$0$GlobalActionsImpl(ScrimDrawable scrimDrawable, Dialog dialog, DialogInterface dialogInterface) {
        if (this.mBlurUtils.supportsBlursOnWindows()) {
            scrimDrawable.setAlpha(137);
            this.mBlurUtils.applyBlur(dialog.getWindow().getDecorView().getViewRootImpl(), this.mBlurUtils.blurRadiusOfRatio(1.0f));
            return;
        }
        scrimDrawable.setAlpha((int) (this.mContext.getResources().getFloat(C0005R$dimen.op_shutdown_scrim_behind_alpha) * 255.0f));
    }

    private int getRebootMessage(boolean z, String str) {
        if (str != null && str.startsWith("recovery-update")) {
            return 17041120;
        }
        if ((str == null || !str.equals("recovery")) && !z) {
            return 17041262;
        }
        return 17041116;
    }

    private String getReasonMessage(String str) {
        if (str == null || !str.startsWith("recovery-update")) {
            return null;
        }
        return this.mContext.getString(17041121);
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void disable(int i, int i2, int i3, boolean z) {
        GlobalActionsDialog globalActionsDialog;
        boolean z2 = (i3 & 8) != 0;
        if (i == this.mContext.getDisplayId() && z2 != this.mDisabled) {
            this.mDisabled = z2;
            if (z2 && (globalActionsDialog = this.mGlobalActionsDialog) != null) {
                globalActionsDialog.dismissDialog();
            }
        }
    }

    private boolean hasCustomizedShutdownAnim() {
        boolean isSupport = OpFeatures.isSupport(new int[]{62});
        Log.d("GlobalActionsImpl", "hasCusShutdownAnim=" + isSupport);
        if ((!OpFeatures.isSupport(new int[]{128}) && !OpFeatures.isSupport(new int[]{183})) || !isSupport) {
            return isSupport;
        }
        Log.i("GlobalActionsImpl", "show CRA enableShut=" + OpUtils.isEnableCustShutdownAnim(this.mContext));
        return OpUtils.isEnableCustShutdownAnim(this.mContext);
    }
}
