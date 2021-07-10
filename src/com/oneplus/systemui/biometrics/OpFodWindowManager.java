package com.oneplus.systemui.biometrics;

import android.content.Context;
import android.content.res.Resources;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import com.android.systemui.C0005R$dimen;
import com.oneplus.systemui.biometrics.OpBiometricDialogImpl;
import com.oneplus.systemui.biometrics.OpFingerprintDialogView;
import com.oneplus.util.OpUtils;
public class OpFodWindowManager {
    private static final boolean DEBUG_ONEPLUS = Build.DEBUG_ONEPLUS;
    private Context mContext;
    private boolean mCustHideCutout;
    private OpFingerprintDialogView mDialogView;
    private WindowManager.LayoutParams mFpLayoutParams;
    private View mHighlightView;
    private WindowManager.LayoutParams mHighlightViewParams;
    private boolean mIs2KDisplay;
    private boolean mTransparentIconExpand;
    private WindowManager.LayoutParams mTransparentIconParams;
    private int mTransparentIconSize;
    private View mTransparentIconView;
    private WindowManager mWindowManager;
    private final IBinder mWindowToken = new Binder();

    public OpFodWindowManager(Context context, OpBiometricDialogImpl opBiometricDialogImpl, OpFingerprintDialogView opFingerprintDialogView) {
        boolean z = false;
        this.mIs2KDisplay = false;
        this.mContext = context;
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        this.mDialogView = opFingerprintDialogView;
        this.mFpLayoutParams = getFpLayoutParams();
        this.mHighlightViewParams = getHighlightViewLayoutParams();
        this.mTransparentIconParams = getIconLayoutParams();
        this.mDialogView.setFodWindowManager(this);
        this.mCustHideCutout = OpUtils.isCutoutHide(this.mContext);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);
        Log.d("OpFodWindowManager", "metrics.width = " + displayMetrics.widthPixels);
        this.mIs2KDisplay = displayMetrics.widthPixels == 1440 ? true : z;
    }

    public void addFpViewToWindow() {
        OpFingerprintDialogView opFingerprintDialogView = this.mDialogView;
        if (opFingerprintDialogView == null || opFingerprintDialogView.isAttachedToWindow()) {
            Log.d("OpFodWindowManager", "can't add fp view window.");
        } else {
            this.mWindowManager.addView(this.mDialogView, getFpLayoutParams());
        }
    }

    public void addHighlightViewToWindow(View view) {
        if (view != null) {
            Log.d("OpFodWindowManager", "addHighlightViewToWindow");
            this.mHighlightView = view;
            this.mWindowManager.addView(view, getHighlightViewLayoutParams());
        }
    }

    public void removeHighlightView() {
        if (this.mHighlightView.isAttachedToWindow()) {
            Log.d("OpFodWindowManager", "removeHighlightView");
            this.mWindowManager.removeViewImmediate(this.mHighlightView);
        }
    }

    public void addTransparentIconViewToWindow(View view) {
        if (view != null) {
            Log.d("OpFodWindowManager", "addTransparentIconViewToWindow");
            this.mTransparentIconView = view;
            this.mWindowManager.addView(view, this.mTransparentIconParams);
        }
    }

    public void removeTransparentIconView() {
        if (this.mTransparentIconView.isAttachedToWindow()) {
            Log.d("OpFodWindowManager", "removeTransparentIconView");
            this.mWindowManager.removeViewImmediate(this.mTransparentIconView);
        }
    }

    public void addView(View view) {
        if (view == null) {
            Log.d("OpFodWindowManager", "addView view is null return.");
        } else if (this.mHighlightView == view || (view instanceof OpFingerprintDialogView.OpFingerprintHighlightView)) {
            addHighlightViewToWindow(view);
        } else if (this.mTransparentIconView == view || (view instanceof OpBiometricDialogImpl.OpFingerprintBlockTouchView)) {
            addTransparentIconViewToWindow(view);
        } else {
            Log.d("OpFodWindowManager", "unmatch view added. " + view.getClass().getSimpleName());
        }
    }

    public void removeView(View view) {
        if (view == null) {
            Log.d("OpFodWindowManager", "removeView view is null return.");
        } else if (this.mHighlightView == view || (view instanceof OpFingerprintDialogView.OpFingerprintHighlightView)) {
            removeHighlightView();
        } else if (this.mTransparentIconView == view || (view instanceof OpBiometricDialogImpl.OpFingerprintBlockTouchView)) {
            removeTransparentIconView();
        } else {
            Log.d("OpFodWindowManager", "unmatch view removed. " + view.getClass().getSimpleName());
        }
    }

    public WindowManager.LayoutParams getFpLayoutParams() {
        return getCustomLayoutParams("OPFingerprintView");
    }

    private WindowManager.LayoutParams getHighlightViewLayoutParams() {
        return getCustomLayoutParams("OPFingerprintVDpressed");
    }

    private WindowManager.LayoutParams getCustomLayoutParams(String str) {
        int i;
        int i2;
        int i3;
        WindowManager.LayoutParams layoutParams;
        WindowManager.LayoutParams layoutParams2;
        boolean isKeyguardAuthenticating = OpFodHelper.getInstance().isKeyguardAuthenticating();
        String currentOwner = OpFodHelper.getInstance().getCurrentOwner();
        int i4 = 1;
        if (str.equals("OPFingerprintView") && (layoutParams2 = this.mFpLayoutParams) != null) {
            if (isKeyguardAuthenticating || "com.oneplus.applocker".equals(currentOwner)) {
                i4 = -1;
            }
            layoutParams2.screenOrientation = i4;
            if (DEBUG_ONEPLUS) {
                Log.d("OpFodWindowManager", "getCustomLayoutParams: fpLayoutParams screenOrientation= " + this.mFpLayoutParams.screenOrientation);
            }
            return this.mFpLayoutParams;
        } else if (!str.equals("OPFingerprintVDpressed") || (layoutParams = this.mHighlightViewParams) == null) {
            WindowManager.LayoutParams layoutParams3 = new WindowManager.LayoutParams();
            if (str.equals("OPFingerprintView")) {
                layoutParams3.type = 2305;
            } else if (str.equals("OPFingerprintVDpressed")) {
                layoutParams3.type = 2306;
                layoutParams3.privateFlags |= 1048576;
            }
            layoutParams3.privateFlags |= 16;
            layoutParams3.layoutInDisplayCutoutMode = 1;
            boolean isSupportCustomFingerprintType2 = OpUtils.isSupportCustomFingerprintType2();
            layoutParams3.flags = 16778520;
            layoutParams3.format = -2;
            layoutParams3.width = -1;
            layoutParams3.height = -1;
            layoutParams3.gravity = 17;
            if (OpUtils.isSupportResolutionSwitch(this.mContext)) {
                boolean is2KResolution = OpUtils.is2KResolution();
                if (str.equals("OPFingerprintView")) {
                    layoutParams3.width = -1;
                    layoutParams3.height = this.mContext.getResources().getDimensionPixelSize(is2KResolution ? C0005R$dimen.fp_animation_height_2k : C0005R$dimen.fp_animation_height_1080p);
                    Resources resources = this.mContext.getResources();
                    if (isSupportCustomFingerprintType2) {
                        i3 = C0005R$dimen.op_biometric_animation_view_ss_y;
                    } else {
                        i3 = is2KResolution ? C0005R$dimen.op_biometric_animation_view_y_2k : C0005R$dimen.op_biometric_animation_view_y_1080p;
                    }
                    layoutParams3.y = resources.getDimensionPixelSize(i3);
                    layoutParams3.gravity = 48;
                } else if (str.equals("OPFingerprintVDpressed")) {
                    Resources resources2 = this.mContext.getResources();
                    if (isSupportCustomFingerprintType2) {
                        i = C0005R$dimen.op_biometric_icon_flash_ss_width;
                    } else {
                        i = is2KResolution ? C0005R$dimen.op_biometric_icon_flash_width_2k : C0005R$dimen.op_biometric_icon_flash_width_1080p;
                    }
                    int dimensionPixelSize = resources2.getDimensionPixelSize(i);
                    layoutParams3.height = dimensionPixelSize;
                    layoutParams3.width = dimensionPixelSize;
                    Resources resources3 = this.mContext.getResources();
                    if (isSupportCustomFingerprintType2) {
                        i2 = C0005R$dimen.op_biometric_icon_flash_ss_location_y;
                    } else {
                        i2 = is2KResolution ? C0005R$dimen.op_biometric_icon_flash_location_y_2k : C0005R$dimen.op_biometric_icon_flash_location_y_1080p;
                    }
                    layoutParams3.y = resources3.getDimensionPixelSize(i2);
                    layoutParams3.gravity = 48;
                }
            } else if (str.equals("OPFingerprintView")) {
                layoutParams3.width = -1;
                layoutParams3.height = this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.fp_animation_height);
                layoutParams3.y = this.mContext.getResources().getDimensionPixelSize(isSupportCustomFingerprintType2 ? C0005R$dimen.op_biometric_animation_view_ss_y : C0005R$dimen.op_biometric_animation_view_y);
                layoutParams3.gravity = 48;
            } else if (str.equals("OPFingerprintVDpressed")) {
                int dimensionPixelSize2 = this.mContext.getResources().getDimensionPixelSize(isSupportCustomFingerprintType2 ? C0005R$dimen.op_biometric_icon_flash_ss_width : C0005R$dimen.op_biometric_icon_flash_width);
                layoutParams3.height = dimensionPixelSize2;
                layoutParams3.width = dimensionPixelSize2;
                layoutParams3.y = this.mContext.getResources().getDimensionPixelSize(isSupportCustomFingerprintType2 ? C0005R$dimen.op_biometric_icon_flash_ss_location_y : C0005R$dimen.op_biometric_icon_flash_location_y);
                layoutParams3.gravity = 48;
            }
            if (OpUtils.isCutoutHide(this.mContext)) {
                layoutParams3.y -= OpUtils.getCutoutPathdataHeight(this.mContext);
            }
            if (isKeyguardAuthenticating || "com.oneplus.applocker".equals(currentOwner)) {
                i4 = -1;
            }
            layoutParams3.screenOrientation = i4;
            layoutParams3.windowAnimations = 84934692;
            layoutParams3.setTitle(str);
            layoutParams3.token = this.mWindowToken;
            Log.i("OpFodWindowManager", "getCustomLayoutParams owner:" + currentOwner + " title:" + str);
            this.mDialogView.setSystemUiVisibility(this.mDialogView.getSystemUiVisibility() | 1026);
            return layoutParams3;
        } else {
            if (isKeyguardAuthenticating || "com.oneplus.applocker".equals(currentOwner)) {
                i4 = -1;
            }
            layoutParams.screenOrientation = i4;
            if (DEBUG_ONEPLUS) {
                Log.d("OpFodWindowManager", "getCustomLayoutParams: highlightLayoutParams screenOrientation= " + this.mHighlightViewParams.screenOrientation);
            }
            return this.mHighlightViewParams;
        }
    }

    private WindowManager.LayoutParams getIconLayoutParams() {
        int i;
        if (OpUtils.isSupportResolutionSwitch(this.mContext)) {
            this.mTransparentIconSize = this.mContext.getResources().getDimensionPixelSize(OpUtils.is2KResolution() ? C0005R$dimen.op_biometric_transparent_icon_size_2k : C0005R$dimen.op_biometric_transparent_icon_size_1080p);
        } else {
            this.mTransparentIconSize = this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.op_biometric_transparent_icon_size);
        }
        int i2 = this.mTransparentIconSize;
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(i2, i2, 2305, 16777480, -3);
        layoutParams.privateFlags |= 16;
        layoutParams.setTitle("FingerprintTransparentIcon");
        layoutParams.gravity = 49;
        layoutParams.windowAnimations = 0;
        layoutParams.layoutInDisplayCutoutMode = 1;
        boolean isSupportCustomFingerprintType2 = OpUtils.isSupportCustomFingerprintType2();
        layoutParams.y = this.mContext.getResources().getDimensionPixelSize(isSupportCustomFingerprintType2 ? C0005R$dimen.op_biometric_transparent_icon_ss_location_y : C0005R$dimen.op_biometric_transparent_icon_location_y);
        if (OpUtils.isSupportResolutionSwitch(this.mContext)) {
            Resources resources = this.mContext.getResources();
            if (isSupportCustomFingerprintType2) {
                i = C0005R$dimen.op_biometric_transparent_icon_ss_location_y;
            } else {
                i = OpUtils.is2KResolution() ? C0005R$dimen.op_biometric_transparent_icon_location_y_2k : C0005R$dimen.op_biometric_transparent_icon_location_y_1080p;
            }
            layoutParams.y = resources.getDimensionPixelSize(i);
        } else {
            layoutParams.y = this.mContext.getResources().getDimensionPixelSize(isSupportCustomFingerprintType2 ? C0005R$dimen.op_biometric_transparent_icon_ss_location_y : C0005R$dimen.op_biometric_transparent_icon_location_y);
        }
        if (OpUtils.isCutoutHide(this.mContext)) {
            layoutParams.y -= OpUtils.getCutoutPathdataHeight(this.mContext);
        }
        layoutParams.screenOrientation = -1;
        return layoutParams;
    }

    public void handleConfigurationChange() {
        this.mFpLayoutParams = null;
        this.mFpLayoutParams = getCustomLayoutParams("OPFingerprintView");
        Log.d("OpFodWindowManager", "mViewLayoutParams height " + this.mFpLayoutParams.height);
        OpFingerprintDialogView opFingerprintDialogView = this.mDialogView;
        if (opFingerprintDialogView != null && opFingerprintDialogView.isAttachedToWindow()) {
            this.mWindowManager.updateViewLayout(this.mDialogView, this.mFpLayoutParams);
        }
        this.mHighlightViewParams = null;
        this.mHighlightViewParams = getCustomLayoutParams("OPFingerprintVDpressed");
        View view = this.mHighlightView;
        if (view != null && view.isAttachedToWindow()) {
            this.mWindowManager.updateViewLayout(this.mHighlightView, this.mHighlightViewParams);
        }
        this.mTransparentIconParams = null;
        this.mTransparentIconParams = getIconLayoutParams();
        View view2 = this.mTransparentIconView;
        if (view2 != null && view2.isAttachedToWindow() && !this.mTransparentIconExpand) {
            this.mWindowManager.updateViewLayout(this.mTransparentIconView, this.mTransparentIconParams);
        }
    }

    public void handleUpdateTransparentIconLayoutParams(boolean z) {
        View view = this.mTransparentIconView;
        if (view != null && view.isAttachedToWindow()) {
            Log.d("OpFodWindowManager", "updateTransparentIconLayoutParams: " + z);
            WindowManager.LayoutParams iconLayoutParams = getIconLayoutParams();
            this.mTransparentIconExpand = z;
            if (z) {
                iconLayoutParams.width = -1;
                iconLayoutParams.height = -1;
                iconLayoutParams.y = 0;
                if ("com.oneplus.applocker".equals(OpFodHelper.getInstance().getCurrentOwner())) {
                    iconLayoutParams.screenOrientation = -1;
                } else {
                    iconLayoutParams.screenOrientation = 1;
                }
            }
            this.mWindowManager.updateViewLayout(this.mTransparentIconView, iconLayoutParams);
        }
    }

    public void onOverlayChanged() {
        Log.i("OpFodWindowManager", "onOverlayChanged be trigger in OpBiometricDialogImpl, mCustHideCutout:" + this.mCustHideCutout + ", OpUtils.isCutoutHide(mContext):" + OpUtils.isCutoutHide(this.mContext));
        if (this.mCustHideCutout != OpUtils.isCutoutHide(this.mContext)) {
            this.mCustHideCutout = OpUtils.isCutoutHide(this.mContext);
            DisplayMetrics displayMetrics = new DisplayMetrics();
            this.mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);
            boolean z = true;
            boolean z2 = this.mContext.getResources().getConfiguration().orientation == 2;
            if (z2) {
                if (displayMetrics.heightPixels != 1440) {
                    z = false;
                }
                this.mIs2KDisplay = z;
            } else {
                if (displayMetrics.widthPixels != 1440) {
                    z = false;
                }
                this.mIs2KDisplay = z;
            }
            Log.d("OpFodWindowManager", "onOverlayChanged, metrics.width = " + displayMetrics.widthPixels + ", metrics.height = " + displayMetrics.heightPixels + ", isLandscape = " + z2 + ", is2KDisplay = " + this.mIs2KDisplay);
            OpFingerprintDialogView opFingerprintDialogView = this.mDialogView;
            if (opFingerprintDialogView != null) {
                opFingerprintDialogView.updateLayoutDimension(this.mIs2KDisplay, (float) displayMetrics.widthPixels);
            }
        }
    }

    public void onDensityOrFontScaleChanged() {
        Log.d("OpFodWindowManager", "onDensityOrFontScaleChanged");
        if (this.mDialogView == null) {
            Log.d("OpFodWindowManager", "onDensityOrFontScaleChanged mDialogView doesn't init yet");
        } else if (OpUtils.isSupportResolutionSwitch(this.mContext) && this.mDialogView != null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            this.mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);
            boolean z = true;
            boolean z2 = this.mContext.getResources().getConfiguration().orientation == 2;
            if (z2) {
                if (displayMetrics.heightPixels != 1440) {
                    z = false;
                }
                this.mIs2KDisplay = z;
            } else {
                if (displayMetrics.widthPixels != 1440) {
                    z = false;
                }
                this.mIs2KDisplay = z;
            }
            Log.d("OpFodWindowManager", "metrics.width = " + displayMetrics.widthPixels + ", metrics.height = " + displayMetrics.heightPixels + ", isLandscape = " + z2 + ", is2KDisplay = " + this.mIs2KDisplay);
            this.mDialogView.updateLayoutDimension(this.mIs2KDisplay, (float) displayMetrics.widthPixels);
        }
    }

    public boolean isTransparentViewExpanded() {
        return this.mTransparentIconExpand;
    }
}
