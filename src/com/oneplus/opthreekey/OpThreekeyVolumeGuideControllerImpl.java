package com.oneplus.opthreekey;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import androidx.appcompat.app.AlertDialog;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.C0004R$color;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0015R$string;
import com.android.systemui.C0016R$style;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.oneplus.opthreekey.OpThreekeyVolumeGuideController;
import com.oneplus.opzenmode.OpZenModeController;
import com.oneplus.util.OpUtils;
import com.oneplus.util.ThemeColorUtils;
import java.util.ArrayList;
import java.util.List;
public class OpThreekeyVolumeGuideControllerImpl implements OpThreekeyVolumeGuideController, OpZenModeController.Callback, ConfigurationController.ConfigurationListener {
    private static boolean DEBUG = Build.DEBUG_ONEPLUS;
    private int mAccentColor = 0;
    private AlertDialog mAlertDialog;
    private Context mContext;
    private final H mHandler = new H();
    private boolean mIsLandscape = false;
    private boolean mIsREDVersion = false;
    private final List<ItemEntity> mItemEntities = new ArrayList();
    private final ArrayList<OpThreekeyVolumeGuideController.IOpThreekeyVolumeGuideControllerCallBack> mOpThreekeyVolumeGuideCallBack = new ArrayList<>();
    private int[] mShowingType = {-1, -1, -1, -1};
    private int mThemeColorMode = 0;
    private int mThreeKeystate = -1;
    private int mType = 0;
    KeyguardUpdateMonitorCallback mUpdateMonitorCallback = new KeyguardUpdateMonitorCallback(this) { // from class: com.oneplus.opthreekey.OpThreekeyVolumeGuideControllerImpl.1
        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onScreenTurnedOn() {
            if (OpThreekeyVolumeGuideControllerImpl.DEBUG) {
                Log.d("OpThreekeyVolumeGuideControllerImpl", "onScreenTurnedOn");
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onScreenTurnedOff() {
            if (OpThreekeyVolumeGuideControllerImpl.DEBUG) {
                Log.d("OpThreekeyVolumeGuideControllerImpl", "onScreenTurnedOff");
            }
        }
    };
    private int mWindowType;

    private int computeTimeoutH() {
        return 15000;
    }

    private int convertThreekeyToType(int i) {
        return i - 1;
    }

    /* access modifiers changed from: private */
    public final class H extends Handler {
        public H() {
            super(Looper.getMainLooper());
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            int i = message.what;
            boolean z = true;
            if (i == 1) {
                OpThreekeyVolumeGuideControllerImpl opThreekeyVolumeGuideControllerImpl = OpThreekeyVolumeGuideControllerImpl.this;
                int i2 = message.arg1;
                if (message.arg2 != 1) {
                    z = false;
                }
                opThreekeyVolumeGuideControllerImpl.showH(i2, z);
            } else if (i == 2) {
                OpThreekeyVolumeGuideControllerImpl.this.dismissH(message.arg1);
            } else if (i == 3) {
                OpThreekeyVolumeGuideControllerImpl.this.rescheduleTimeoutH();
            } else if (i == 4) {
                OpThreekeyVolumeGuideControllerImpl.this.stateChangeH(message.arg1);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showH(int i, boolean z) {
        if (DEBUG) {
            Log.d("OpThreekeyVolumeGuideControllerImpl", "showH r=" + i + ", mShowingType[type]:" + this.mShowingType[i]);
        }
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(2);
        if (this.mShowingType[i] == -1 && i > -1) {
            for (int i2 = 0; i2 < this.mShowingType.length; i2++) {
                dismissH(i2);
            }
            if (isThreekeyGuideEverShow(i) && !z) {
                return;
            }
            if (i != 2 || z) {
                updateShowingType(i, 1);
                showInfoDialog(this.mItemEntities.get(i));
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dismissH(int i) {
        if (DEBUG) {
            Log.d("OpThreekeyVolumeGuideControllerImpl", "dismissH r=" + i + ", mShowingType[type]:" + this.mShowingType[i]);
        }
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(2);
        updateShowingType(i, -1);
        AlertDialog alertDialog = this.mAlertDialog;
        if (alertDialog != null) {
            alertDialog.dismiss();
            this.mAlertDialog = null;
        }
    }

    /* access modifiers changed from: protected */
    public void rescheduleTimeoutH() {
        this.mHandler.removeMessages(2);
        int computeTimeoutH = computeTimeoutH();
        H h = this.mHandler;
        h.sendMessageDelayed(h.obtainMessage(2, 3, 0), (long) computeTimeoutH);
        if (DEBUG) {
            Log.d("OpThreekeyVolumeGuideControllerImpl", "rescheduleTimeout " + computeTimeoutH);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stateChangeH(int i) {
        int i2 = this.mThreeKeystate;
        if (DEBUG) {
            Log.d("OpThreekeyVolumeGuideControllerImpl", "stateChangeH " + i + ", lastThreeKeystate:" + i2);
        }
        if (i != this.mThreeKeystate) {
            this.mThreeKeystate = i;
        }
        if (i2 != -1) {
            showH(convertThreekeyToType(this.mThreeKeystate), false);
        }
    }

    private void updateShowingType(int i, int i2) {
        this.mShowingType[i] = i2;
    }

    public OpThreekeyVolumeGuideControllerImpl(Context context) {
        Log.i("OpThreekeyVolumeGuideControllerImpl", "OpThreekeyVolumeGuideControllerImpl" + Debug.getCallers(15));
        this.mContext = context;
    }

    @Override // com.oneplus.opthreekey.OpThreekeyVolumeGuideController
    public void init(int i, OpThreekeyVolumeGuideController.UserActivityListener userActivityListener) {
        Log.v("OpThreekeyVolumeGuideControllerImpl", "init");
        this.mWindowType = i;
        int i2 = this.mContext.getResources().getConfiguration().densityDpi;
        this.mIsLandscape = isLandscape();
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
        ((OpZenModeController) Dependency.get(OpZenModeController.class)).addCallback(this);
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).registerCallback(this.mUpdateMonitorCallback);
        initDialogData();
    }

    private void initDialogData() {
        ItemEntity itemEntity = new ItemEntity(C0015R$string.threekey_volume_guide_dialog_silent_title, C0015R$string.threekey_volume_guide_dialog_silent_msg, C0006R$drawable.threekey_volume_silent_guide_light_pic, C0006R$drawable.threekey_volume_silent_guide_dark_pic, 0);
        ItemEntity itemEntity2 = new ItemEntity(C0015R$string.threekey_volume_guide_dialog_vibrate_title, C0015R$string.threekey_volume_guide_dialog_vibrate_msg, C0006R$drawable.threekey_volume_vibrate_guide_light_pic, C0006R$drawable.threekey_volume_vibrate_guide_dark_pic, 1);
        ItemEntity itemEntity3 = new ItemEntity(C0015R$string.threekey_volume_guide_dialog_ring_title, C0015R$string.threekey_volume_guide_dialog_ring_msg, C0006R$drawable.threekey_volume_ring_guide_light_pic, C0006R$drawable.threekey_volume_ring_guide_dark_pic, 2);
        ItemEntity itemEntity4 = new ItemEntity(C0015R$string.threekey_volume_guide_dialog_mutering_title, C0015R$string.threekey_volume_guide_dialog_mutering_msg, C0006R$drawable.threekey_volume_mutering_guide_light_pic, C0006R$drawable.threekey_volume_mutering_guide_dark_pic, 3);
        this.mItemEntities.add(itemEntity);
        this.mItemEntities.add(itemEntity2);
        this.mItemEntities.add(itemEntity3);
        this.mItemEntities.add(itemEntity4);
    }

    private void showInfoDialog(ItemEntity itemEntity) {
        showInfoDialog(itemEntity, null);
    }

    private void showInfoDialog(ItemEntity itemEntity, Runnable runnable) {
        int i;
        int i2;
        int i3;
        if (itemEntity != null) {
            int i4 = itemEntity.type;
            int i5 = -1;
            if (i4 == 0 || i4 == 1 || i4 == 2 || i4 == 3) {
                int i6 = itemEntity.nameId;
                int i7 = itemEntity.msgId;
                i = C0015R$string.threekey_volume_guide_dialog_ok;
                i2 = i7;
                i3 = i6;
                i5 = this.mThemeColorMode == 1 ? itemEntity.darkResId : itemEntity.resId;
            } else {
                i3 = -1;
                i2 = -1;
                i = -1;
            }
            showInfoDialog(i5, i3, i2, i, i4, runnable);
        }
    }

    private void showInfoDialog(int i, int i2, int i3, int i4, final int i5, final Runnable runnable) {
        int i6;
        if (i2 == -1 && i3 == -1 && i4 == -1 && i == -1) {
            Log.i("OpThreekeyVolumeGuideControllerImpl", "showInfoDialog, nothing to show");
            return;
        }
        Context context = this.mContext;
        if (ThemeColorUtils.getCurrentTheme() == 1) {
            i6 = C0016R$style.oneplus_theme_dialog_dark;
        } else {
            i6 = C0016R$style.oneplus_theme_dialog_light;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context, i6));
        if (i2 != -1) {
            builder.setTitle(i2);
        }
        if (i3 != -1) {
            builder.setMessage(i3);
        }
        if (i != -1) {
            if (this.mIsLandscape) {
                String str = this.mContext.getResources().getResourceEntryName(i) + "_land";
                Log.i("OpThreekeyVolumeGuideControllerImpl", "showInfoDialog, resLandName:" + str + ", before:" + i);
                i = getResourceId(this.mContext, str);
                Log.i("OpThreekeyVolumeGuideControllerImpl", "showInfoDialog, resLandName:" + str + ", after:" + i);
            }
            builder.setCustomImage(i);
        }
        if (i4 != -1) {
            builder.setNegativeButton(i4, new DialogInterface.OnClickListener(i5) { // from class: com.oneplus.opthreekey.-$$Lambda$OpThreekeyVolumeGuideControllerImpl$AXdii1Cjht4N0sLBNN7ptwe34zQ
                public final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                @Override // android.content.DialogInterface.OnClickListener
                public final void onClick(DialogInterface dialogInterface, int i7) {
                    OpThreekeyVolumeGuideControllerImpl.this.lambda$showInfoDialog$0$OpThreekeyVolumeGuideControllerImpl(this.f$1, dialogInterface, i7);
                }
            });
        }
        AlertDialog create = builder.create();
        this.mAlertDialog = create;
        if (runnable != null) {
            create.setOnShowListener(new DialogInterface.OnShowListener(this) { // from class: com.oneplus.opthreekey.OpThreekeyVolumeGuideControllerImpl.2
                @Override // android.content.DialogInterface.OnShowListener
                public void onShow(DialogInterface dialogInterface) {
                    Log.i("OpThreekeyVolumeGuideControllerImpl", "showInfoDialog, onShow");
                    runnable.run();
                }
            });
        }
        this.mAlertDialog.setOnDismissListener(new DialogInterface.OnDismissListener(this) { // from class: com.oneplus.opthreekey.OpThreekeyVolumeGuideControllerImpl.3
            @Override // android.content.DialogInterface.OnDismissListener
            public void onDismiss(DialogInterface dialogInterface) {
                Log.i("OpThreekeyVolumeGuideControllerImpl", "showInfoDialog, onDismiss");
            }
        });
        this.mAlertDialog.setCanceledOnTouchOutside(false);
        this.mAlertDialog.setCancelable(true);
        this.mAlertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() { // from class: com.oneplus.opthreekey.OpThreekeyVolumeGuideControllerImpl.4
            @Override // android.content.DialogInterface.OnKeyListener
            public boolean onKey(DialogInterface dialogInterface, int i7, KeyEvent keyEvent) {
                Log.i("OpThreekeyVolumeGuideControllerImpl", "onKey, onDismiss, keyCode:" + i7);
                if (i7 != 4) {
                    return true;
                }
                OpThreekeyVolumeGuideControllerImpl.this.dismiss(i5);
                return true;
            }
        });
        Window window = this.mAlertDialog.getWindow();
        window.addFlags(17563904);
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.type = this.mWindowType;
        attributes.setTitle(OpThreekeyDialogImpl.class.getSimpleName());
        window.setAttributes(attributes);
        window.setSoftInputMode(48);
        this.mAlertDialog.show();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$showInfoDialog$0 */
    public /* synthetic */ void lambda$showInfoDialog$0$OpThreekeyVolumeGuideControllerImpl(int i, DialogInterface dialogInterface, int i2) {
        Log.i("OpThreekeyVolumeGuideControllerImpl", "showInfoDialog, setNegativeButton");
        updateShowingType(i, -1);
        setThreekeyGuideEverShow(i);
    }

    public void show(int i, boolean z) {
        Log.i("OpThreekeyVolumeGuideControllerImpl", "show, reason:" + i);
        this.mHandler.obtainMessage(1, i, z ? 1 : 0, 0).sendToTarget();
    }

    public void dismiss(int i) {
        Log.i("OpThreekeyVolumeGuideControllerImpl", "dismiss, reason:" + i);
        this.mHandler.obtainMessage(2, i, 0).sendToTarget();
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onConfigChanged(Configuration configuration) {
        Log.i("OpThreekeyVolumeGuideControllerImpl", "onConfigChanged, newConfig.orientation:" + configuration.orientation);
        updateTheme(false);
        boolean z = configuration.orientation == 2;
        if (this.mIsLandscape != z) {
            this.mIsLandscape = z;
            for (int i = 0; i < this.mShowingType.length; i++) {
                updateShowingType(i, -1);
                dismiss(i);
            }
        }
    }

    @Override // com.oneplus.opzenmode.OpZenModeController.Callback
    public void onThreeKeyStatus(int i) {
        if (DEBUG) {
            Log.d("OpThreekeyVolumeGuideControllerImpl", "onThreeKeyStatus:" + i);
        }
        this.mHandler.obtainMessage(4, i, 150).sendToTarget();
    }

    private void updateTheme(boolean z) {
        int themeColor = OpUtils.getThemeColor(this.mContext);
        int color = ThemeColorUtils.getColor(100);
        boolean isREDVersion = OpUtils.isREDVersion();
        boolean z2 = (this.mThemeColorMode == themeColor && this.mAccentColor == color && this.mIsREDVersion == isREDVersion) ? false : true;
        if (DEBUG) {
            Log.i("OpThreekeyVolumeGuideControllerImpl", "updateTheme change:" + z2 + " force:" + z + ", mIsREDVersion:" + this.mIsREDVersion + ", isREDVersion:" + isREDVersion);
        }
        if (z2 || z) {
            this.mIsREDVersion = isREDVersion;
            this.mThemeColorMode = themeColor;
            this.mAccentColor = color;
            applyTheme();
        }
    }

    public void applyTheme() {
        Resources resources = this.mContext.getResources();
        if (this.mThemeColorMode != 1) {
            resources.getColor(C0004R$color.oneplus_contorl_text_color_primary_light);
            resources.getColor(C0004R$color.op_control_bg_color_control);
            return;
        }
        resources.getColor(C0004R$color.oneplus_contorl_text_color_primary_dark);
        resources.getColor(C0004R$color.op_control_bg_color_control);
    }

    @Override // com.oneplus.opthreekey.OpThreekeyVolumeGuideController
    public int isNeedToShowGuideUi(int i, boolean z) {
        Log.i("OpThreekeyVolumeGuideControllerImpl", "isNeedToShowGuideUi, type:" + i + ", force:" + z);
        show(i, z);
        return 0;
    }

    public void addCallback(OpThreekeyVolumeGuideController.IOpThreekeyVolumeGuideControllerCallBack iOpThreekeyVolumeGuideControllerCallBack) {
        synchronized (this) {
            this.mOpThreekeyVolumeGuideCallBack.add(iOpThreekeyVolumeGuideControllerCallBack);
            try {
                iOpThreekeyVolumeGuideControllerCallBack.onThreekeyVolumeGuideUiStateChanged(this.mType);
            } catch (Exception e) {
                Log.w("OpThreekeyVolumeGuideControllerImpl", "Failed to call to IOpThreekeyVolumeGuideControllerCallBack" + e);
            }
        }
    }

    public void removeCallback(OpThreekeyVolumeGuideController.IOpThreekeyVolumeGuideControllerCallBack iOpThreekeyVolumeGuideControllerCallBack) {
        synchronized (this) {
            this.mOpThreekeyVolumeGuideCallBack.remove(iOpThreekeyVolumeGuideControllerCallBack);
        }
    }

    /* access modifiers changed from: package-private */
    public static class ItemEntity {
        int darkResId;
        int msgId;
        int nameId;
        int resId;
        int type;

        public ItemEntity(int i, int i2, int i3, int i4, int i5) {
            this.nameId = i;
            this.msgId = i2;
            this.resId = i3;
            this.darkResId = i4;
            this.type = i5;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0037, code lost:
        if (android.provider.Settings.Secure.getIntForUser(r4.mContext.getContentResolver(), "threekey_ring_guide_dialog", 0, -2) != 0) goto L_0x0015;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0049, code lost:
        if (android.provider.Settings.Secure.getIntForUser(r4.mContext.getContentResolver(), "threekey_mute_ring_guide_dialog", 0, -2) != 0) goto L_0x0015;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:3:0x0011, code lost:
        if (android.provider.Settings.Secure.getIntForUser(r4.mContext.getContentResolver(), "threekey_silent_guide_dialog", 0, -2) != 0) goto L_0x0015;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0025, code lost:
        if (android.provider.Settings.Secure.getIntForUser(r4.mContext.getContentResolver(), "threekey_vibrate_guide_dialog", 0, -2) != 0) goto L_0x0015;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isThreekeyGuideEverShow(int r5) {
        /*
            r4 = this;
            r0 = -2
            r1 = 1
            r2 = 0
            if (r5 != 0) goto L_0x0017
            android.content.Context r4 = r4.mContext
            android.content.ContentResolver r4 = r4.getContentResolver()
            java.lang.String r5 = "threekey_silent_guide_dialog"
            int r4 = android.provider.Settings.Secure.getIntForUser(r4, r5, r2, r0)
            if (r4 == 0) goto L_0x0014
            goto L_0x0015
        L_0x0014:
            r1 = r2
        L_0x0015:
            r2 = r1
            goto L_0x004c
        L_0x0017:
            if (r5 != r1) goto L_0x0028
            android.content.Context r4 = r4.mContext
            android.content.ContentResolver r4 = r4.getContentResolver()
            java.lang.String r5 = "threekey_vibrate_guide_dialog"
            int r4 = android.provider.Settings.Secure.getIntForUser(r4, r5, r2, r0)
            if (r4 == 0) goto L_0x0014
            goto L_0x0015
        L_0x0028:
            r3 = 2
            if (r5 != r3) goto L_0x003a
            android.content.Context r4 = r4.mContext
            android.content.ContentResolver r4 = r4.getContentResolver()
            java.lang.String r5 = "threekey_ring_guide_dialog"
            int r4 = android.provider.Settings.Secure.getIntForUser(r4, r5, r2, r0)
            if (r4 == 0) goto L_0x0014
            goto L_0x0015
        L_0x003a:
            r3 = 3
            if (r5 != r3) goto L_0x004c
            android.content.Context r4 = r4.mContext
            android.content.ContentResolver r4 = r4.getContentResolver()
            java.lang.String r5 = "threekey_mute_ring_guide_dialog"
            int r4 = android.provider.Settings.Secure.getIntForUser(r4, r5, r2, r0)
            if (r4 == 0) goto L_0x0014
            goto L_0x0015
        L_0x004c:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.opthreekey.OpThreekeyVolumeGuideControllerImpl.isThreekeyGuideEverShow(int):boolean");
    }

    private boolean setThreekeyGuideEverShow(int i) {
        if (i == 0) {
            return Settings.Secure.putIntForUser(this.mContext.getContentResolver(), "threekey_silent_guide_dialog", 1, -2);
        }
        if (i == 1) {
            return Settings.Secure.putIntForUser(this.mContext.getContentResolver(), "threekey_vibrate_guide_dialog", 1, -2);
        }
        if (i == 2) {
            return Settings.Secure.putIntForUser(this.mContext.getContentResolver(), "threekey_ring_guide_dialog", 1, -2);
        }
        if (i == 3) {
            return Settings.Secure.putIntForUser(this.mContext.getContentResolver(), "threekey_mute_ring_guide_dialog", 1, -2);
        }
        return false;
    }

    private boolean isLandscape() {
        return this.mContext.getResources().getConfiguration().orientation == 2;
    }

    public static int getResourceId(Context context, String str) {
        return context.getResources().getIdentifier(str, "drawable", context.getPackageName());
    }
}
