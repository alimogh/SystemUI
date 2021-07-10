package com.oneplus.systemui.statusbar.phone;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.UserHandle;
import android.text.Html;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import androidx.appcompat.app.AlertDialog;
import com.android.systemui.C0016R$style;
import com.android.systemui.Dependency;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.oneplus.util.ThemeColorUtils;
public class OpSystemUIDialog extends AlertDialog {
    private final Context mContext;
    private final DismissReceiver mDismissReceiver;

    public OpSystemUIDialog(Context context) {
        this(context, C0016R$style.OnePlus_ThemeOverlay_AppCompat_Dialog_Alert);
    }

    public OpSystemUIDialog(Context context, int i) {
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
    @Override // androidx.appcompat.app.AppCompatDialog, android.app.Dialog
    public void onStop() {
        super.onStop();
        this.mDismissReceiver.unregister();
    }

    public void setShowForAllUsers(boolean z) {
        setShowForAllUsers(this, z);
    }

    @Override // androidx.appcompat.app.AppCompatDialog, android.app.Dialog
    public void setTitle(int i) {
        setTitle(this.mContext.getString(i));
    }

    @Override // androidx.appcompat.app.AlertDialog, androidx.appcompat.app.AppCompatDialog, android.app.Dialog
    public void setTitle(CharSequence charSequence) {
        if (ThemeColorUtils.getCurrentTheme() == 1) {
            super.setTitle(Html.fromHtml("<font color='#D8D8D8'>" + charSequence.toString().replace("\n", "<br />") + "</font>"));
            return;
        }
        super.setTitle(Html.fromHtml("<b>" + ((Object) charSequence) + "</b>"));
    }

    @Override // androidx.appcompat.app.AlertDialog
    public void setMessage(CharSequence charSequence) {
        if (ThemeColorUtils.getCurrentTheme() == 1) {
            super.setMessage(Html.fromHtml("<font color='#8D8D8D'>" + charSequence.toString().replace("\n", "<br />") + "</font>"));
            return;
        }
        super.setMessage(charSequence);
    }

    public void setPositiveButton(int i, DialogInterface.OnClickListener onClickListener) {
        setButton(-1, this.mContext.getString(i), onClickListener);
    }

    public void setNegativeButton(int i, DialogInterface.OnClickListener onClickListener) {
        setButton(-2, this.mContext.getString(i), onClickListener);
    }

    @Override // androidx.appcompat.app.AlertDialog
    public void setButton(int i, CharSequence charSequence, DialogInterface.OnClickListener onClickListener) {
        super.setButton(i, Html.fromHtml("<b>" + ((Object) charSequence) + "</b>"), onClickListener);
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

    private static class DismissReceiver extends BroadcastReceiver {
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
