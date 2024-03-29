package com.android.systemui.volume;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.SystemProperties;
import android.util.Log;
import android.view.KeyEvent;
import com.android.systemui.statusbar.phone.SystemUIDialog;
public abstract class SafetyWarningDialog extends SystemUIDialog implements DialogInterface.OnDismissListener, DialogInterface.OnClickListener {
    private static final String TAG = Util.logTag(SafetyWarningDialog.class);
    private final AudioManager mAudioManager;
    private final Context mContext;
    private boolean mDisableOnVolumeUp;
    private boolean mNewVolumeUp;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() { // from class: com.android.systemui.volume.SafetyWarningDialog.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(intent.getAction())) {
                if (D.BUG) {
                    Log.d(SafetyWarningDialog.TAG, "Received ACTION_CLOSE_SYSTEM_DIALOGS");
                }
                SafetyWarningDialog.this.cancel();
                SafetyWarningDialog.this.cleanUp();
            }
        }
    };
    private long mShowTime;

    /* access modifiers changed from: protected */
    public abstract void cleanUp();

    public SafetyWarningDialog(Context context, AudioManager audioManager) {
        super(context);
        Log.i(TAG, "SafetyWarningDialog init");
        this.mContext = context;
        this.mAudioManager = audioManager;
        try {
            this.mDisableOnVolumeUp = context.getResources().getBoolean(17891518);
        } catch (Resources.NotFoundException unused) {
            this.mDisableOnVolumeUp = true;
        }
        getWindow().setType(2010);
        setShowForAllUsers(true);
        setMessage(this.mContext.getString(17041195));
        setButton(-1, this.mContext.getString(17039379), this);
        setButton(-2, this.mContext.getString(17039369), (DialogInterface.OnClickListener) null);
        setOnDismissListener(this);
        context.registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
    }

    @Override // android.app.AlertDialog, android.view.KeyEvent.Callback, android.app.Dialog
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        if (this.mDisableOnVolumeUp && i == 24 && keyEvent.getRepeatCount() == 0) {
            this.mNewVolumeUp = true;
        }
        return super.onKeyDown(i, keyEvent);
    }

    @Override // android.app.AlertDialog, android.view.KeyEvent.Callback, android.app.Dialog
    public boolean onKeyUp(int i, KeyEvent keyEvent) {
        if (i == 24 && this.mNewVolumeUp && System.currentTimeMillis() - this.mShowTime > 1000) {
            boolean isEUVersion = isEUVersion();
            if (D.BUG) {
                Log.d(TAG, "Confirmed warning via VOLUME_UP");
            }
            String str = TAG;
            Log.i(str, "SafetyWarningDialog onKeyUp:" + System.currentTimeMillis() + " mShowTime:" + this.mShowTime + " KEY_CONFIRM_ALLOWED_AFTER:1000isEUVersion:" + isEUVersion);
            if (!isEUVersion) {
                this.mAudioManager.disableSafeMediaVolume();
            }
            dismiss();
        }
        return super.onKeyUp(i, keyEvent);
    }

    @Override // android.content.DialogInterface.OnClickListener
    public void onClick(DialogInterface dialogInterface, int i) {
        Log.i(TAG, "SafetyWarningDialog onClick");
        this.mAudioManager.disableSafeMediaVolume();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.SystemUIDialog, android.app.Dialog
    public void onStart() {
        super.onStart();
        this.mShowTime = System.currentTimeMillis();
    }

    @Override // android.content.DialogInterface.OnDismissListener
    public void onDismiss(DialogInterface dialogInterface) {
        Log.i(TAG, "SafetyWarningDialog onDismiss");
        this.mContext.unregisterReceiver(this.mReceiver);
        cleanUp();
    }

    public static boolean isEUVersion() {
        return "true".equals(SystemProperties.get("ro.build.eu", "false"));
    }
}
