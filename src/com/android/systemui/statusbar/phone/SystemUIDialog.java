package com.android.systemui.statusbar.phone;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.UserHandle;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import com.android.systemui.Dependency;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.statusbar.phone.SystemUIDialog;
import com.android.systemui.statusbar.policy.KeyguardStateController;
public class SystemUIDialog extends AlertDialog {
    private final Context mContext;
    private final DismissReceiver mDismissReceiver;

    /* JADX WARNING: Illegal instructions before constructor call */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public SystemUIDialog(android.content.Context r3) {
        /*
            r2 = this;
            int r0 = com.oneplus.util.ThemeColorUtils.getCurrentTheme()
            r1 = 1
            if (r0 != r1) goto L_0x000a
            int r0 = com.android.systemui.C0016R$style.oneplus_theme_dialog_dark
            goto L_0x000c
        L_0x000a:
            int r0 = com.android.systemui.C0016R$style.oneplus_theme_dialog_light
        L_0x000c:
            r2.<init>(r3, r0)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.SystemUIDialog.<init>(android.content.Context):void");
    }

    public SystemUIDialog(Context context, int i) {
        super(context, i);
        this.mContext = context;
        applyFlags(this);
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.setTitle(getClass().getSimpleName());
        getWindow().setAttributes(attributes);
        this.mDismissReceiver = new DismissReceiver(this);
    }

    /* access modifiers changed from: protected */
    @Override // android.app.Dialog
    public void onStart() {
        super.onStart();
        this.mDismissReceiver.register();
    }

    /* access modifiers changed from: protected */
    @Override // android.app.Dialog
    public void onStop() {
        super.onStop();
        this.mDismissReceiver.unregister();
    }

    public void setShowForAllUsers(boolean z) {
        setShowForAllUsers(this, z);
    }

    public void setMessage(int i) {
        setMessage(this.mContext.getString(i));
    }

    public void setPositiveButton(int i, DialogInterface.OnClickListener onClickListener) {
        setButton(-1, this.mContext.getString(i), onClickListener);
    }

    public void setNegativeButton(int i, DialogInterface.OnClickListener onClickListener) {
        setButton(-2, this.mContext.getString(i), onClickListener);
    }

    public static void setShowForAllUsers(Dialog dialog, boolean z) {
        if (z) {
            dialog.getWindow().getAttributes().privateFlags |= 16;
            return;
        }
        dialog.getWindow().getAttributes().privateFlags &= -17;
    }

    public static void setWindowOnTop(Dialog dialog) {
        Window window = dialog.getWindow();
        window.setType(2017);
        if (((KeyguardStateController) Dependency.get(KeyguardStateController.class)).isShowing()) {
            window.getAttributes().setFitInsetsTypes(window.getAttributes().getFitInsetsTypes() & (~WindowInsets.Type.statusBars()));
        }
    }

    public static AlertDialog applyFlags(AlertDialog alertDialog) {
        Window window = alertDialog.getWindow();
        window.setType(2017);
        window.addFlags(655360);
        window.getAttributes().setFitInsetsTypes(window.getAttributes().getFitInsetsTypes() & (~WindowInsets.Type.statusBars()));
        return alertDialog;
    }

    public static void registerDismissListener(Dialog dialog) {
        DismissReceiver dismissReceiver = new DismissReceiver(dialog);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$SystemUIDialog$aJwQFxZ3HhCkUHQluqH61p2yCMc
            @Override // android.content.DialogInterface.OnDismissListener
            public final void onDismiss(DialogInterface dialogInterface) {
                SystemUIDialog.DismissReceiver.this.unregister();
            }
        });
        dismissReceiver.register();
    }

    /* access modifiers changed from: private */
    public static class DismissReceiver extends BroadcastReceiver {
        private static final IntentFilter INTENT_FILTER;
        private final BroadcastDispatcher mBroadcastDispatcher = ((BroadcastDispatcher) Dependency.get(BroadcastDispatcher.class));
        private final Dialog mDialog;
        private boolean mRegistered;

        static {
            IntentFilter intentFilter = new IntentFilter();
            INTENT_FILTER = intentFilter;
            intentFilter.addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
            INTENT_FILTER.addAction("android.intent.action.SCREEN_OFF");
        }

        DismissReceiver(Dialog dialog) {
            this.mDialog = dialog;
        }

        /* access modifiers changed from: package-private */
        public void register() {
            this.mBroadcastDispatcher.registerReceiver(this, INTENT_FILTER, null, UserHandle.CURRENT);
            this.mRegistered = true;
        }

        /* access modifiers changed from: package-private */
        public void unregister() {
            if (this.mRegistered) {
                this.mBroadcastDispatcher.unregisterReceiver(this);
                this.mRegistered = false;
            }
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            this.mDialog.dismiss();
        }
    }
}
