package com.oneplus.aod.utils.bitmoji.triggers;

import android.content.Context;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.util.Log;
import com.android.internal.util.IndentingPrintWriter;
import com.oneplus.aod.utils.bitmoji.OpBitmojiManager;
import com.oneplus.aod.utils.bitmoji.triggers.base.DelayTrigger;
import com.oneplus.aod.utils.bitmoji.triggers.base.Trigger;
import java.io.FileDescriptor;
public class CameraTrigger extends DelayTrigger {
    private CameraManager.AvailabilityCallback mCameraCallback = new CameraManager.AvailabilityCallback() { // from class: com.oneplus.aod.utils.bitmoji.triggers.CameraTrigger.1
        public void onCameraOpened(String str, String str2) {
            if (Build.DEBUG_ONEPLUS) {
                String str3 = ((Trigger) CameraTrigger.this).mTag;
                Log.d(str3, "onCameraOpened: cameraId= " + str + ", packageId= " + str2);
            }
            boolean isFrontCamera = CameraTrigger.this.isFrontCamera(str);
            if (!isFrontCamera) {
                CameraTrigger.this.mCameraOpenId = 0;
                CameraTrigger.this.mCameraOpenBack = true;
            } else {
                CameraTrigger.this.mCameraOpenBack = false;
            }
            CameraTrigger.this.checkTrigger(isFrontCamera);
        }

        public void onCameraClosed(String str) {
            if (Build.DEBUG_ONEPLUS) {
                String str2 = ((Trigger) CameraTrigger.this).mTag;
                Log.d(str2, "onCameraClosed: cameraId= " + str);
            }
            boolean isFrontCamera = CameraTrigger.this.isFrontCamera(str);
            if (!isFrontCamera) {
                CameraTrigger.this.mCameraOpenBack = false;
            }
            CameraTrigger.this.checkTrigger(isFrontCamera);
        }
    };
    private CameraManager mCameraManager;
    private boolean mCameraOpenBack;
    private boolean mCameraOpenFront;
    private int mCameraOpenId;

    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.Trigger
    public String getMdmLabel() {
        return "camera";
    }

    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.CategoryTrigger, com.oneplus.aod.utils.bitmoji.triggers.base.Trigger
    public int getPriority() {
        return 3;
    }

    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.DelayTrigger
    public String getTriggerId() {
        return "camera";
    }

    public CameraTrigger(Context context, OpBitmojiManager opBitmojiManager) {
        super(context, opBitmojiManager);
        this.mCameraManager = (CameraManager) context.getSystemService("camera");
    }

    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.DelayTrigger, com.oneplus.aod.utils.bitmoji.triggers.base.Trigger
    public void init() {
        super.init();
        this.mCameraManager.registerAvailabilityCallback(this.mCameraCallback, this.mBitmojiManager.getHandler());
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.CategoryTrigger
    public String[] getCategories() {
        return new String[]{"camera"};
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.CategoryTrigger
    public String getCurrentCategory() {
        if (this.mCameraOpenBack) {
            return "camera";
        }
        if (this.mIsDelayed && this.mCameraOpenId == 0) {
            return "camera";
        }
        if (this.mCameraOpenFront) {
            return "front camera";
        }
        if (!this.mIsDelayed || this.mCameraOpenId != 1) {
            return null;
        }
        return "front camera";
    }

    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.DelayTrigger
    public boolean isActiveInner() {
        return this.mCameraOpenBack || this.mCameraOpenFront;
    }

    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.DelayTrigger, com.oneplus.aod.utils.bitmoji.triggers.base.CategoryTrigger, com.oneplus.aod.utils.bitmoji.triggers.base.Trigger
    public void dumpDetail(FileDescriptor fileDescriptor, IndentingPrintWriter indentingPrintWriter, String[] strArr) {
        super.dumpDetail(fileDescriptor, indentingPrintWriter, strArr);
        indentingPrintWriter.println("cameraOpenBack=" + this.mCameraOpenBack);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isFrontCamera(String str) {
        try {
            if (((Integer) this.mCameraManager.getCameraCharacteristics(str).get(CameraCharacteristics.LENS_FACING)).intValue() == 0) {
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.w(this.mTag, "isFrontCamera occur error", e);
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkTrigger(boolean z) {
        if (this.mCameraOpenBack && !z) {
            onTriggerChanged("camera", true);
        } else if (!this.mCameraOpenBack) {
            onTriggerChanged("camera", false);
        }
    }
}
