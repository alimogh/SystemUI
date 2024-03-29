package com.android.systemui.usb;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.debug.IAdbManager;
import android.os.Bundle;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
import com.android.systemui.C0015R$string;
import com.android.systemui.C0016R$style;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.oneplus.util.ThemeColorUtils;
public class UsbDebuggingActivity extends AlertActivity implements DialogInterface.OnClickListener {
    private CheckBox mAlwaysAllow;
    private final BroadcastDispatcher mBroadcastDispatcher;
    private UsbDisconnectedReceiver mDisconnectedReceiver;
    private String mKey;
    private boolean mServiceNotified;

    public UsbDebuggingActivity(BroadcastDispatcher broadcastDispatcher) {
        this.mBroadcastDispatcher = broadcastDispatcher;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r6v0, resolved type: com.android.systemui.usb.UsbDebuggingActivity */
    /* JADX WARN: Multi-variable type inference failed */
    public void onCreate(Bundle bundle) {
        int i;
        Window window = getWindow();
        window.addSystemFlags(524288);
        window.setType(2008);
        UsbDebuggingActivity.super.onCreate(bundle);
        if (SystemProperties.getInt("service.adb.tcp.port", 0) == 0) {
            this.mDisconnectedReceiver = new UsbDisconnectedReceiver(this);
        }
        Intent intent = getIntent();
        String stringExtra = intent.getStringExtra("fingerprints");
        String stringExtra2 = intent.getStringExtra("key");
        this.mKey = stringExtra2;
        if (stringExtra == null || stringExtra2 == null) {
            finish();
            return;
        }
        AlertController.AlertParams alertParams = ((AlertActivity) this).mAlertParams;
        alertParams.mTitle = getString(C0015R$string.usb_debugging_title);
        alertParams.mMessage = getString(C0015R$string.usb_debugging_message, new Object[]{stringExtra});
        alertParams.mPositiveButtonText = getString(C0015R$string.usb_debugging_allow);
        alertParams.mNegativeButtonText = getString(17039360);
        alertParams.mPositiveButtonListener = this;
        alertParams.mNegativeButtonListener = this;
        if (ThemeColorUtils.getCurrentTheme() == 1) {
            i = C0016R$style.Oneplus_SystemUI_Dialog_Alert_Dark;
        } else {
            i = C0016R$style.Oneplus_SystemUI_Dialog_Alert_Light;
        }
        alertParams.mContext.setTheme(i);
        View inflate = LayoutInflater.from(alertParams.mContext).inflate(17367092, (ViewGroup) null);
        CheckBox checkBox = (CheckBox) inflate.findViewById(16908751);
        this.mAlwaysAllow = checkBox;
        checkBox.setText(getString(C0015R$string.usb_debugging_always));
        alertParams.mView = inflate;
        window.setCloseOnTouchOutside(false);
        setupAlert();
    }

    public void onWindowAttributesChanged(WindowManager.LayoutParams layoutParams) {
        UsbDebuggingActivity.super.onWindowAttributesChanged(layoutParams);
    }

    private class UsbDisconnectedReceiver extends BroadcastReceiver {
        private final Activity mActivity;

        UsbDisconnectedReceiver(Activity activity) {
            this.mActivity = activity;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if ("android.hardware.usb.action.USB_STATE".equals(intent.getAction()) && !intent.getBooleanExtra("connected", false)) {
                UsbDebuggingActivity.this.notifyService(false);
                this.mActivity.finish();
            }
        }
    }

    public void onStart() {
        UsbDebuggingActivity.super.onStart();
        if (this.mDisconnectedReceiver != null) {
            this.mBroadcastDispatcher.registerReceiver(this.mDisconnectedReceiver, new IntentFilter("android.hardware.usb.action.USB_STATE"));
        }
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        UsbDisconnectedReceiver usbDisconnectedReceiver = this.mDisconnectedReceiver;
        if (usbDisconnectedReceiver != null) {
            this.mBroadcastDispatcher.unregisterReceiver(usbDisconnectedReceiver);
        }
        UsbDebuggingActivity.super.onStop();
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        if (!this.mServiceNotified) {
            notifyService(false);
        }
        UsbDebuggingActivity.super.onDestroy();
    }

    @Override // android.content.DialogInterface.OnClickListener
    public void onClick(DialogInterface dialogInterface, int i) {
        boolean z = true;
        boolean z2 = i == -1;
        if (!z2 || !this.mAlwaysAllow.isChecked()) {
            z = false;
        }
        notifyService(z2, z);
        finish();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyService(boolean z) {
        notifyService(z, false);
    }

    private void notifyService(boolean z, boolean z2) {
        try {
            IAdbManager asInterface = IAdbManager.Stub.asInterface(ServiceManager.getService("adb"));
            if (z) {
                asInterface.allowDebugging(z2, this.mKey);
            } else {
                asInterface.denyDebugging();
            }
            this.mServiceNotified = true;
        } catch (Exception e) {
            Log.e("UsbDebuggingActivity", "Unable to notify Usb service", e);
        }
    }
}
