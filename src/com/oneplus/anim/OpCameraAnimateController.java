package com.oneplus.anim;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManager;
import com.android.systemui.C0003R$bool;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.oneplus.util.OpUtils;
public class OpCameraAnimateController implements ConfigurationController.ConfigurationListener {
    private CameraManager mCameraManager;
    private ContentObserver mContentObserver = new ContentObserver(new Handler()) { // from class: com.oneplus.anim.OpCameraAnimateController.2
        @Override // android.database.ContentObserver
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z) {
            super.onChange(z);
            OpCameraAnimateController.this.updateCameraAnim();
        }
    };
    private ContentResolver mContentResolver;
    private Context mContext;
    private OpGraphLight mGraphLight;
    private boolean mIsCutoutHide = false;
    private boolean mIsLastFrontCameraAnimStateOn;
    private boolean mIsOneHandMode = false;
    private Uri mOneHandSettings = Settings.Global.getUriFor("one_hand_mode_status");

    public OpCameraAnimateController(Context context) {
        this.mContext = context;
    }

    public void init() {
        boolean z = this.mContext.getResources().getBoolean(C0003R$bool.config_should_show_front_camera_animation);
        int i = SystemProperties.getInt("debug.frontcamera.control", 0);
        Log.i("OpCameraAnimateController", "init shouldShow: " + z + ", controlAnim:" + i);
        if (i == 1) {
            z = true;
        } else if (i == 2) {
            z = false;
        }
        if (z) {
            this.mGraphLight = new OpGraphLight((WindowManager) this.mContext.getSystemService(WindowManager.class), this.mContext, new Handler());
            this.mContentResolver = this.mContext.getContentResolver();
            Log.d("OpCameraAnimateController", " isSupportHolePunchFrontCam " + OpUtils.isSupportHolePunchFrontCam());
            if (OpUtils.isSupportHolePunchFrontCam()) {
                CameraManager cameraManager = (CameraManager) this.mContext.getSystemService("camera");
                this.mCameraManager = cameraManager;
                cameraManager.registerAvailabilityCallback(new CameraManager.AvailabilityCallback() { // from class: com.oneplus.anim.OpCameraAnimateController.1
                    @Override // android.hardware.camera2.CameraManager.AvailabilityCallback
                    public void onCameraAvailable(String str) {
                        super.onCameraAvailable(str);
                        boolean isFrontCamera = OpCameraAnimateController.this.isFrontCamera(str);
                        Log.i("OpCameraAnimateController", "onCameraAvailable:" + str + " isFrontCamera " + isFrontCamera);
                        if (isFrontCamera) {
                            OpCameraAnimateController.this.mIsLastFrontCameraAnimStateOn = false;
                            OpCameraAnimateController.this.mGraphLight.stop();
                            ((ConfigurationController) Dependency.get(ConfigurationController.class)).removeCallback(OpCameraAnimateController.this);
                            OpCameraAnimateController.this.mContentResolver.unregisterContentObserver(OpCameraAnimateController.this.mContentObserver);
                        }
                    }

                    @Override // android.hardware.camera2.CameraManager.AvailabilityCallback
                    public void onCameraUnavailable(String str) {
                        super.onCameraUnavailable(str);
                        OpCameraAnimateController opCameraAnimateController = OpCameraAnimateController.this;
                        opCameraAnimateController.mIsCutoutHide = OpUtils.isCutoutHide(opCameraAnimateController.mContext);
                        OpCameraAnimateController opCameraAnimateController2 = OpCameraAnimateController.this;
                        opCameraAnimateController2.mIsOneHandMode = Settings.Global.getInt(opCameraAnimateController2.mContext.getContentResolver(), "one_hand_mode_status", 0) == 1;
                        boolean isFrontCamera = OpCameraAnimateController.this.isFrontCamera(str);
                        Log.i("OpCameraAnimateController", "onCameraUnavailable:" + str + " isFrontCamera " + isFrontCamera + ", OpUtils.isCutoutHide(mContext):" + OpCameraAnimateController.this.mIsCutoutHide + ", oneHandMode:" + OpCameraAnimateController.this.mIsOneHandMode);
                        if (isFrontCamera && !OpCameraAnimateController.this.mIsCutoutHide && !OpCameraAnimateController.this.mIsOneHandMode) {
                            OpCameraAnimateController.this.mIsLastFrontCameraAnimStateOn = true;
                            OpCameraAnimateController.this.mGraphLight.postShow();
                            ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(OpCameraAnimateController.this);
                            OpCameraAnimateController.this.mContentResolver.registerContentObserver(OpCameraAnimateController.this.mOneHandSettings, false, OpCameraAnimateController.this.mContentObserver);
                        }
                    }
                }, new Handler());
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isFrontCamera(String str) {
        try {
            if (this.mCameraManager == null) {
                return false;
            }
            Log.i("OpCameraAnimateController", "isFrontCamera " + str);
            if (((Integer) this.mCameraManager.getCameraCharacteristics(str).get(CameraCharacteristics.LENS_FACING)).intValue() == 0) {
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.d("OpCameraAnimateController", e.toString());
            return false;
        }
    }

    public boolean isFrontCameraAnimOn() {
        return this.mIsLastFrontCameraAnimStateOn;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateCameraAnim() {
        if (this.mIsLastFrontCameraAnimStateOn && this.mGraphLight != null) {
            boolean isCutoutHide = OpUtils.isCutoutHide(this.mContext);
            boolean z = false;
            if (Settings.Global.getInt(this.mContext.getContentResolver(), "one_hand_mode_status", 0) == 1) {
                z = true;
            }
            if (this.mIsCutoutHide != isCutoutHide || this.mIsOneHandMode != z) {
                this.mIsOneHandMode = z;
                this.mIsCutoutHide = isCutoutHide;
                if (OpUtils.DEBUG_ONEPLUS) {
                    Log.d("OpCameraAnimateController", "updateCameraAnim isCutoutHide:" + isCutoutHide + " , isOneHandMode: " + z);
                }
                if (this.mIsOneHandMode || this.mIsCutoutHide) {
                    this.mGraphLight.forceStop();
                } else {
                    this.mGraphLight.postShow();
                }
            }
        }
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onConfigChanged(Configuration configuration) {
        OpGraphLight opGraphLight;
        if (this.mIsLastFrontCameraAnimStateOn && (opGraphLight = this.mGraphLight) != null) {
            opGraphLight.onConfigChanged(configuration);
        }
        updateCameraAnim();
    }
}
