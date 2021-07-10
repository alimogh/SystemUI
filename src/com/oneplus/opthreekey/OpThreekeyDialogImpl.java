package com.oneplus.opthreekey;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.hardware.display.DisplayManagerGlobal;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.systemui.C0004R$color;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0011R$layout;
import com.android.systemui.Dependency;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.android.systemui.plugins.Plugin;
import com.android.systemui.plugins.VolumeDialogController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.oneplus.opzenmode.OpZenModeController;
import com.oneplus.util.OpUtils;
import com.oneplus.util.ThemeColorUtils;
public class OpThreekeyDialogImpl implements Plugin, OpZenModeController.Callback, ConfigurationController.ConfigurationListener {
    private static boolean DEBUG = false;
    private static String TAG = "OpThreekeyDialogImpl";
    private int mAccentColor = 0;
    private Context mContext;
    private Dialog mDialog;
    private int mDialogPosition;
    private ViewGroup mDialogView;
    private final H mHandler = new H();
    private boolean mIsDeviceOn = false;
    private boolean mIsREDVersion = false;
    private boolean mIsThreekeyStateChangedWhenScreenOff = false;
    private OpThreekeyDialog$UserActivityListener mListener;
    private OpZenModeController mOpZenModeController;
    OrientationEventListener mOrientationListener;
    private int mOrientationType = 0;
    private boolean mShowing = false;
    private int mThemeBgColor = 0;
    private int mThemeColorMode = 0;
    private int mThemeIconColor = 0;
    private int mThemeTextColor = 0;
    private ImageView mThreeKeyIcon;
    private TextView mThreeKeyText;
    private int mThreeKeystate = -1;
    private final WakefulnessLifecycle.Observer mWakefulnessObserver = new WakefulnessLifecycle.Observer() { // from class: com.oneplus.opthreekey.OpThreekeyDialogImpl.1
        @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
        public void onFinishedWakingUp() {
            Log.d(OpThreekeyDialogImpl.TAG, "WakefulnessLifecycle, onFinishedWakingUp");
            OpThreekeyDialogImpl.this.mIsDeviceOn = true;
            if (OpThreekeyDialogImpl.this.mIsThreekeyStateChangedWhenScreenOff) {
                OpThreekeyDialogImpl.this.mIsThreekeyStateChangedWhenScreenOff = false;
                OpThreekeyDialogImpl.this.show(0);
            }
        }

        @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
        public void onFinishedGoingToSleep() {
            Log.d(OpThreekeyDialogImpl.TAG, "WakefulnessLifecycle, onFinishedGoingToSleep");
            OpThreekeyDialogImpl.this.mIsDeviceOn = false;
        }
    };
    private Window mWindow;
    private WindowManager.LayoutParams mWindowLayoutParams;
    private int mWindowType;

    private int computeTimeoutH() {
        return 3000;
    }

    static {
        DEBUG = Build.DEBUG_ONEPLUS;
    }

    public OpThreekeyDialogImpl(Context context) {
        this.mContext = context;
        this.mOpZenModeController = (OpZenModeController) Dependency.get(OpZenModeController.class);
        this.mOrientationListener = new OrientationEventListener(this.mContext, 3) { // from class: com.oneplus.opthreekey.OpThreekeyDialogImpl.2
            @Override // android.view.OrientationEventListener
            public void onOrientationChanged(int i) {
                OpThreekeyDialogImpl.this.checkOrientationType();
            }
        };
        VolumeDialogController volumeDialogController = (VolumeDialogController) Dependency.get(VolumeDialogController.class);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkOrientationType() {
        int rotation;
        Display realDisplay = DisplayManagerGlobal.getInstance().getRealDisplay(0);
        if (realDisplay != null && (rotation = realDisplay.getRotation()) != this.mOrientationType) {
            String str = TAG;
            Log.v(str, "Orientype to " + rotation);
            this.mOrientationType = rotation;
            updateThreekeyLayout();
        }
    }

    public void init(int i, OpThreekeyDialog$UserActivityListener opThreekeyDialog$UserActivityListener) {
        Log.v(TAG, "init");
        this.mWindowType = i;
        int i2 = this.mContext.getResources().getConfiguration().densityDpi;
        this.mOpZenModeController.addCallback(this);
        this.mListener = opThreekeyDialog$UserActivityListener;
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
        ((WakefulnessLifecycle) Dependency.get(WakefulnessLifecycle.class)).addObserver(this.mWakefulnessObserver);
        initDialog();
        Log.v(TAG, "in OpThreekeyDialog init mOpThreekeyNavigationDialog.getInstance");
        OpThreekeyNavigationDialog.getInstance(this.mContext);
    }

    public void destroy() {
        Log.v(TAG, "destroy");
        this.mOpZenModeController.removeCallback(this);
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).removeCallback(this);
        ((WakefulnessLifecycle) Dependency.get(WakefulnessLifecycle.class)).removeObserver(this.mWakefulnessObserver);
    }

    private void initDialog() {
        Dialog dialog = new Dialog(this.mContext);
        this.mDialog = dialog;
        this.mShowing = false;
        Window window = dialog.getWindow();
        this.mWindow = window;
        window.requestFeature(1);
        this.mWindow.setBackgroundDrawable(new ColorDrawable(0));
        this.mWindow.clearFlags(2);
        this.mWindow.addFlags(17563944);
        this.mDialog.setCanceledOnTouchOutside(false);
        WindowManager.LayoutParams attributes = this.mWindow.getAttributes();
        this.mWindowLayoutParams = attributes;
        attributes.type = this.mWindowType;
        attributes.format = -3;
        attributes.setTitle(OpThreekeyDialogImpl.class.getSimpleName());
        WindowManager.LayoutParams layoutParams = this.mWindowLayoutParams;
        layoutParams.gravity = 53;
        layoutParams.y = this.mDialogPosition;
        this.mWindow.setAttributes(layoutParams);
        this.mWindow.setSoftInputMode(48);
        this.mDialog.setContentView(C0011R$layout.op_threekey_dialog);
        this.mDialogView = (ViewGroup) this.mDialog.findViewById(C0008R$id.threekey_layout);
        this.mThreeKeyIcon = (ImageView) this.mDialog.findViewById(C0008R$id.threekey_icon);
        this.mThreeKeyText = (TextView) this.mDialog.findViewById(C0008R$id.threekey_text);
        updateTheme(true);
    }

    public void show(int i) {
        this.mHandler.obtainMessage(1, i, 0).sendToTarget();
    }

    private void registerOrientationListener(boolean z) {
        if (!this.mOrientationListener.canDetectOrientation() || !z) {
            Log.v(TAG, "Cannot detect orientation");
            this.mOrientationListener.disable();
            return;
        }
        Log.v(TAG, "Can detect orientation");
        this.mOrientationListener.enable();
    }

    /* JADX WARNING: Removed duplicated region for block: B:111:0x023e  */
    /* JADX WARNING: Removed duplicated region for block: B:113:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x018a  */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x018d  */
    /* JADX WARNING: Removed duplicated region for block: B:94:0x01cd  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateThreekeyLayout() {
        /*
        // Method dump skipped, instructions count: 623
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.opthreekey.OpThreekeyDialogImpl.updateThreekeyLayout():void");
    }

    private GradientDrawable getCornerGradientDrawable(int i, boolean z, int i2) {
        GradientDrawable gradientDrawable = (GradientDrawable) ((LayerDrawable) this.mContext.getResources().getDrawable(i)).getDrawable(0);
        gradientDrawable.setCornerRadii(getTheekeyCornerRadii(this.mContext, z, i2));
        gradientDrawable.setColor(this.mThemeBgColor);
        return gradientDrawable;
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0069  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private float getCuttingEdgeCornerRadiusValue(android.content.Context r6) {
        /*
            r5 = this;
            android.content.res.Resources r5 = r6.getResources()
            int r0 = com.android.systemui.C0005R$dimen.shape_corner_radius
            int r5 = r5.getDimensionPixelSize(r0)
            float r5 = (float) r5
            android.content.res.Resources r0 = r6.getResources()
            int r1 = com.android.systemui.C0005R$dimen.shape_corner_radius_for_check_roundedrect
            int r0 = r0.getDimensionPixelSize(r1)
            float r0 = (float) r0
            android.content.res.Resources r1 = r6.getResources()
            int r2 = com.android.systemui.C0005R$dimen.shape_corner_radius_for_check_teardrop
            int r1 = r1.getDimensionPixelSize(r2)
            float r1 = (float) r1
            android.content.res.Resources r2 = r6.getResources()
            int r3 = com.android.systemui.C0005R$dimen.shape_corner_radius_for_check_squircle
            int r2 = r2.getDimensionPixelSize(r3)
            float r2 = (float) r2
            android.content.res.Resources r3 = r6.getResources()
            int r4 = com.android.systemui.C0005R$dimen.shape_corner_radius_cutting_edge_circle
            int r3 = r3.getDimensionPixelSize(r4)
            float r3 = (float) r3
            int r0 = (r5 > r0 ? 1 : (r5 == r0 ? 0 : -1))
            if (r0 != 0) goto L_0x0047
            android.content.res.Resources r5 = r6.getResources()
            int r6 = com.android.systemui.C0005R$dimen.shape_corner_radius_cutting_edge_roundedrect
            int r5 = r5.getDimensionPixelSize(r6)
        L_0x0045:
            float r3 = (float) r5
            goto L_0x0065
        L_0x0047:
            int r0 = (r5 > r1 ? 1 : (r5 == r1 ? 0 : -1))
            if (r0 != 0) goto L_0x0056
            android.content.res.Resources r5 = r6.getResources()
            int r6 = com.android.systemui.C0005R$dimen.shape_corner_radius_cutting_edge_teardrop
            int r5 = r5.getDimensionPixelSize(r6)
            goto L_0x0045
        L_0x0056:
            int r5 = (r5 > r2 ? 1 : (r5 == r2 ? 0 : -1))
            if (r5 != 0) goto L_0x0065
            android.content.res.Resources r5 = r6.getResources()
            int r6 = com.android.systemui.C0005R$dimen.shape_corner_radius_cutting_edge_squircle
            int r5 = r5.getDimensionPixelSize(r6)
            goto L_0x0045
        L_0x0065:
            boolean r5 = com.oneplus.util.OpUtils.DEBUG_ONEPLUS
            if (r5 == 0) goto L_0x007f
            java.lang.String r5 = com.oneplus.opthreekey.OpThreekeyDialogImpl.TAG
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r0 = "getCornerRadiusValue, value:"
            r6.append(r0)
            r6.append(r3)
            java.lang.String r6 = r6.toString()
            android.util.Log.i(r5, r6)
        L_0x007f:
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.opthreekey.OpThreekeyDialogImpl.getCuttingEdgeCornerRadiusValue(android.content.Context):float");
    }

    /* JADX WARNING: Removed duplicated region for block: B:11:0x0064  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private float getMiddleCuttingEdgeCornerRadiusValue(android.content.Context r7) {
        /*
            r6 = this;
            android.content.res.Resources r6 = r7.getResources()
            int r0 = com.android.systemui.C0005R$dimen.shape_corner_radius
            int r6 = r6.getDimensionPixelSize(r0)
            float r6 = (float) r6
            android.content.res.Resources r0 = r7.getResources()
            int r1 = com.android.systemui.C0005R$dimen.shape_corner_radius_for_check_roundedrect
            int r0 = r0.getDimensionPixelSize(r1)
            float r0 = (float) r0
            android.content.res.Resources r1 = r7.getResources()
            int r2 = com.android.systemui.C0005R$dimen.shape_corner_radius_for_check_teardrop
            int r1 = r1.getDimensionPixelSize(r2)
            float r1 = (float) r1
            android.content.res.Resources r2 = r7.getResources()
            int r3 = com.android.systemui.C0005R$dimen.shape_corner_radius_for_check_squircle
            int r2 = r2.getDimensionPixelSize(r3)
            float r2 = (float) r2
            android.content.res.Resources r3 = r7.getResources()
            int r4 = com.android.systemui.C0005R$dimen.op_threekey_dialog_inner_height
            r5 = 1080(0x438, float:1.513E-42)
            int r3 = com.oneplus.util.OpUtils.getDimensionPixelSize(r3, r4, r5)
            float r3 = (float) r3
            r4 = 1073741824(0x40000000, float:2.0)
            float r3 = r3 / r4
            int r0 = (r6 > r0 ? 1 : (r6 == r0 ? 0 : -1))
            if (r0 != 0) goto L_0x004c
            android.content.res.Resources r6 = r7.getResources()
            int r7 = com.android.systemui.C0005R$dimen.shape_corner_radius_cutting_edge_roundedrect
            int r6 = r6.getDimensionPixelSize(r7)
        L_0x004a:
            float r3 = (float) r6
            goto L_0x0060
        L_0x004c:
            int r0 = (r6 > r1 ? 1 : (r6 == r1 ? 0 : -1))
            if (r0 != 0) goto L_0x0051
            goto L_0x0060
        L_0x0051:
            int r6 = (r6 > r2 ? 1 : (r6 == r2 ? 0 : -1))
            if (r6 != 0) goto L_0x0060
            android.content.res.Resources r6 = r7.getResources()
            int r7 = com.android.systemui.C0005R$dimen.shape_corner_radius_cutting_edge_squircle
            int r6 = r6.getDimensionPixelSize(r7)
            goto L_0x004a
        L_0x0060:
            boolean r6 = com.oneplus.util.OpUtils.DEBUG_ONEPLUS
            if (r6 == 0) goto L_0x007a
            java.lang.String r6 = com.oneplus.opthreekey.OpThreekeyDialogImpl.TAG
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r0 = "getMiddleCuttingEdgeCornerRadiusValue, value:"
            r7.append(r0)
            r7.append(r3)
            java.lang.String r7 = r7.toString()
            android.util.Log.i(r6, r7)
        L_0x007a:
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.opthreekey.OpThreekeyDialogImpl.getMiddleCuttingEdgeCornerRadiusValue(android.content.Context):float");
    }

    private float[] getTheekeyCornerRadii(Context context, boolean z, int i) {
        float dimensionPixelSize = ((float) OpUtils.getDimensionPixelSize(context.getResources(), C0005R$dimen.op_threekey_dialog_inner_height, 1080)) / 2.0f;
        int i2 = 0;
        float[] fArr = {dimensionPixelSize, dimensionPixelSize, dimensionPixelSize, dimensionPixelSize, dimensionPixelSize, dimensionPixelSize, dimensionPixelSize, dimensionPixelSize};
        float cuttingEdgeCornerRadiusValue = getCuttingEdgeCornerRadiusValue(context);
        float middleCuttingEdgeCornerRadiusValue = getMiddleCuttingEdgeCornerRadiusValue(context);
        if (OpUtils.DEBUG_ONEPLUS) {
            String str = TAG;
            Log.i(str, "getTheekeyCornerRadii, radius:" + dimensionPixelSize + " cuttingEdgeCornerRadii:" + cuttingEdgeCornerRadiusValue + " cuttingEdgeMiddleCornerRadii:" + middleCuttingEdgeCornerRadiusValue);
        }
        if (this.mOrientationType != 0) {
            while (i2 < 8) {
                fArr[i2] = middleCuttingEdgeCornerRadiusValue;
                i2++;
            }
        } else if (z) {
            if (i == 1) {
                fArr[4] = cuttingEdgeCornerRadiusValue;
                fArr[5] = cuttingEdgeCornerRadiusValue;
            } else if (i == 2) {
                while (i2 < 8) {
                    fArr[i2] = middleCuttingEdgeCornerRadiusValue;
                    i2++;
                }
            } else if (i == 3) {
                fArr[2] = cuttingEdgeCornerRadiusValue;
                fArr[3] = cuttingEdgeCornerRadiusValue;
            }
        } else if (i == 1) {
            fArr[6] = cuttingEdgeCornerRadiusValue;
            fArr[7] = cuttingEdgeCornerRadiusValue;
        } else if (i == 2) {
            while (i2 < 8) {
                fArr[i2] = middleCuttingEdgeCornerRadiusValue;
                i2++;
            }
        } else if (i == 3) {
            fArr[0] = cuttingEdgeCornerRadiusValue;
            fArr[1] = cuttingEdgeCornerRadiusValue;
        }
        return fArr;
    }

    @Override // com.oneplus.opzenmode.OpZenModeController.Callback
    public void onThreeKeyStatus(int i) {
        if (DEBUG) {
            String str = TAG;
            Log.d(str, "onThreeKeyStatus:" + i + ", mIsDeviceOn:" + this.mIsDeviceOn);
        }
        this.mHandler.obtainMessage(4, i, 0).sendToTarget();
        if (this.mThreeKeystate != -1) {
            if (!this.mIsDeviceOn) {
                this.mIsThreekeyStateChangedWhenScreenOff = true;
            }
            show(0);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showH(int i) {
        if (DEBUG) {
            Log.d(TAG, "showH r=");
        }
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(2);
        rescheduleTimeoutH();
        if (!this.mShowing) {
            registerOrientationListener(true);
            checkOrientationType();
            String str = TAG;
            Log.d(str, "showH mOrientationType=" + this.mOrientationType);
            this.mShowing = true;
            this.mDialog.show();
            OpThreekeyDialog$UserActivityListener opThreekeyDialog$UserActivityListener = this.mListener;
            if (opThreekeyDialog$UserActivityListener != null) {
                opThreekeyDialog$UserActivityListener.onThreekeyStateUserActivity();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dismissH(int i) {
        if (DEBUG) {
            Log.d(TAG, "dismissH r=");
        }
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(2);
        if (this.mShowing) {
            registerOrientationListener(false);
            this.mShowing = false;
            this.mDialog.dismiss();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stateChange(int i) {
        if (DEBUG) {
            String str = TAG;
            Log.d(str, "stateChange " + i);
        }
        if (i != this.mThreeKeystate) {
            this.mThreeKeystate = i;
            updateThreekeyLayout();
            OpThreekeyDialog$UserActivityListener opThreekeyDialog$UserActivityListener = this.mListener;
            if (opThreekeyDialog$UserActivityListener != null) {
                opThreekeyDialog$UserActivityListener.onThreekeyStateUserActivity();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void rescheduleTimeoutH() {
        this.mHandler.removeMessages(2);
        int computeTimeoutH = computeTimeoutH();
        H h = this.mHandler;
        h.sendMessageDelayed(h.obtainMessage(2, 3, 0), (long) computeTimeoutH);
        if (DEBUG) {
            String str = TAG;
            Log.d(str, "rescheduleTimeout " + computeTimeoutH);
        }
        OpThreekeyDialog$UserActivityListener opThreekeyDialog$UserActivityListener = this.mListener;
        if (opThreekeyDialog$UserActivityListener != null) {
            opThreekeyDialog$UserActivityListener.onThreekeyStateUserActivity();
        }
    }

    /* access modifiers changed from: private */
    public final class H extends Handler {
        public H() {
            super(Looper.getMainLooper());
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 1) {
                OpThreekeyDialogImpl.this.showH(message.arg1);
            } else if (i == 2) {
                OpThreekeyDialogImpl.this.dismissH(message.arg1);
            } else if (i == 3) {
                OpThreekeyDialogImpl.this.rescheduleTimeoutH();
            } else if (i == 4) {
                OpThreekeyDialogImpl.this.stateChange(message.arg1);
            }
        }
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onDensityOrFontScaleChanged() {
        dismissH(1);
        initDialog();
        updateThreekeyLayout();
    }

    public void applyTheme() {
        Resources resources = this.mContext.getResources();
        if (this.mThemeColorMode != 1) {
            this.mThemeIconColor = this.mAccentColor;
            this.mThemeTextColor = resources.getColor(C0004R$color.oneplus_contorl_text_color_primary_light);
            this.mThemeBgColor = resources.getColor(C0004R$color.op_control_bg_color_control);
        } else {
            this.mThemeIconColor = this.mAccentColor;
            this.mThemeTextColor = resources.getColor(C0004R$color.oneplus_contorl_text_color_primary_dark);
            this.mThemeBgColor = resources.getColor(C0004R$color.op_control_bg_color_control);
        }
        this.mThreeKeyText.setTextColor(this.mThemeTextColor);
        this.mThreeKeyIcon.setColorFilter(this.mThemeIconColor);
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onConfigChanged(Configuration configuration) {
        updateTheme(false);
        updateThreekeyLayout();
    }

    private void updateTheme(boolean z) {
        int themeColor = OpUtils.getThemeColor(this.mContext);
        int color = ThemeColorUtils.getColor(100);
        boolean isREDVersion = OpUtils.isREDVersion();
        boolean z2 = (this.mThemeColorMode == themeColor && this.mAccentColor == color && this.mIsREDVersion == isREDVersion) ? false : true;
        if (DEBUG) {
            String str = TAG;
            Log.i(str, "updateTheme change:" + z2 + " force:" + z + ", mIsREDVersion:" + this.mIsREDVersion + ", isREDVersion:" + isREDVersion);
        }
        if (z2 || z) {
            this.mIsREDVersion = isREDVersion;
            this.mThemeColorMode = themeColor;
            this.mAccentColor = color;
            applyTheme();
        }
    }
}
