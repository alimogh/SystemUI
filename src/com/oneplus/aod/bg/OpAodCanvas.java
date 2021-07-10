package com.oneplus.aod.bg;

import android.animation.Animator;
import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.Dependency;
import com.android.systemui.shared.system.OpContextWrapper;
import com.android.systemui.util.sensors.AsyncSensorManager;
import com.oneplus.aod.OpAodUtils;
import com.oneplus.aod.utils.OpAodBurnInProtectionHelper;
import com.oneplus.aod.utils.OpCanvasAodHelper;
import java.util.ArrayList;
import java.util.Collection;
public class OpAodCanvas extends View {
    private OpAodLowLightMask mAodMask;
    private IBgPaint mBgPaint;
    private boolean mChecking;
    private long mCurrentTime;
    private Handler mHandler;
    private int mHeight;
    private OpCanvasAodHelper mHelper;
    private OpCanvasAodHelper.OnBitmapHandleDoneListener mListener;
    private boolean mLowEnviroment;
    private AodBgObserver mObserver;
    private PowerManager mPm;
    private SensorEventListener mSensorEventListener;
    private AsyncSensorManager mSensorManager;
    private boolean mUserSwitching;
    private int mWidth;

    public OpAodCanvas(Context context) {
        this(context, null);
    }

    public OpAodCanvas(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public OpAodCanvas(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mSensorEventListener = new SensorEventListener() { // from class: com.oneplus.aod.bg.OpAodCanvas.1
            @Override // android.hardware.SensorEventListener
            public void onAccuracyChanged(Sensor sensor, int i2) {
            }

            @Override // android.hardware.SensorEventListener
            public void onSensorChanged(SensorEvent sensorEvent) {
                boolean z = OpAodCanvas.this.mLowEnviroment;
                int i2 = 0;
                OpAodCanvas.this.mLowEnviroment = sensorEvent.values[0] <= 20.0f;
                Log.d("OpAodCanvas", "check: light= " + z + " -> " + OpAodCanvas.this.mLowEnviroment);
                OpAodCanvas.this.mSensorManager.unregisterListener(OpAodCanvas.this.mSensorEventListener);
                OpAodCanvas.this.mChecking = false;
                if (OpAodCanvas.this.mPm != null && !OpAodCanvas.this.mPm.isInteractive()) {
                    if (OpAodCanvas.this.mAodMask != null) {
                        OpAodLowLightMask opAodLowLightMask = OpAodCanvas.this.mAodMask;
                        if (!OpAodCanvas.this.mLowEnviroment) {
                            i2 = 4;
                        }
                        opAodLowLightMask.setVisibility(i2);
                    }
                    if (OpAodCanvas.this.mBgPaint != null) {
                        OpAodCanvas.this.mBgPaint.userActivityInAlwaysOn();
                    }
                }
            }
        };
        this.mListener = new OpCanvasAodHelper.OnBitmapHandleDoneListener() { // from class: com.oneplus.aod.bg.OpAodCanvas.3
            @Override // com.oneplus.aod.utils.OpCanvasAodHelper.OnBitmapHandleDoneListener
            public void onBitmapHandleDone(boolean z, boolean z2, OpCanvasAodHelper.Data data) {
                StringBuilder sb = new StringBuilder();
                sb.append("onBitmapHandleDone: enabled= ");
                sb.append(z);
                sb.append(", contentChange= ");
                sb.append(z2);
                sb.append(", data= ");
                boolean z3 = true;
                sb.append(data != null);
                Log.i("OpAodCanvas", sb.toString());
                if (!z || data == null) {
                    z3 = false;
                }
                if (z3) {
                    if (z2) {
                        OpAodCanvas.this.shouldTurnOnAodGesture();
                    }
                    Settings.Secure.putIntForUser(((View) OpAodCanvas.this).mContext.getContentResolver(), "aod_clock_style", 0, KeyguardUpdateMonitor.getCurrentUser());
                }
                OpAodCanvas opAodCanvas = OpAodCanvas.this;
                opAodCanvas.setupPaint(opAodCanvas.choosePaint(z3), data);
            }
        };
        this.mHelper = new OpCanvasAodHelper(context);
        this.mLowEnviroment = false;
        this.mSensorManager = (AsyncSensorManager) Dependency.get(AsyncSensorManager.class);
        this.mPm = (PowerManager) context.getSystemService(PowerManager.class);
        this.mChecking = false;
    }

    public void setHandler(Handler handler) {
        this.mHandler = handler;
        AodBgObserver aodBgObserver = new AodBgObserver(handler);
        this.mObserver = aodBgObserver;
        aodBgObserver.register();
    }

    public void setAodMask(View view) {
        this.mAodMask = (OpAodLowLightMask) view;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onVisibilityChanged(View view, int i) {
        super.onVisibilityChanged(view, i);
        OpAodLowLightMask opAodLowLightMask = this.mAodMask;
        if (opAodLowLightMask != null) {
            opAodLowLightMask.setVisibility((this.mBgPaint == null || !this.mLowEnviroment || i != 0) ? 4 : 0);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        this.mWidth = i;
        this.mHeight = i2;
        IBgPaint iBgPaint = this.mBgPaint;
        if (iBgPaint != null) {
            iBgPaint.onSizeChanged(i, i2);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mCurrentTime = SystemClock.elapsedRealtime() / 1000;
        IBgPaint iBgPaint = this.mBgPaint;
        if (iBgPaint != null) {
            iBgPaint.onAttachedToWindow();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        IBgPaint iBgPaint = this.mBgPaint;
        if (iBgPaint != null) {
            iBgPaint.onDetachedFromWindow();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        IBgPaint iBgPaint = this.mBgPaint;
        if (iBgPaint != null) {
            iBgPaint.draw(canvas);
        }
    }

    public void reset() {
        Log.i("OpAodCanvas", "reset");
        setTranslationY(0.0f);
        setAlpha(1.0f);
        IBgPaint iBgPaint = this.mBgPaint;
        if (iBgPaint != null) {
            iBgPaint.reset();
        } else {
            Log.e("OpAodCanvas", "reset: bg paint is null!!!");
        }
        this.mObserver.register();
        clearLowEnvironmentChecking();
    }

    public void onTimeChanged() {
        long elapsedRealtime = SystemClock.elapsedRealtime() / 1000;
        if (elapsedRealtime - this.mCurrentTime > ((long) (OpAodBurnInProtectionHelper.getMoveDelay() * 59))) {
            burnInProtect();
            this.mCurrentTime = elapsedRealtime;
        }
    }

    public void burnInProtect() {
        OpAodLowLightMask opAodLowLightMask = this.mAodMask;
        if (opAodLowLightMask != null) {
            opAodLowLightMask.setVisibility(0);
        }
        IBgPaint iBgPaint = this.mBgPaint;
        if (iBgPaint != null) {
            iBgPaint.burnInProtect();
        }
    }

    public void recover() {
        Log.d("OpAodCanvas", "recover");
        this.mCurrentTime = SystemClock.elapsedRealtime() / 1000;
        IBgPaint iBgPaint = this.mBgPaint;
        if (iBgPaint != null) {
            iBgPaint.recover();
        }
        clearLowEnvironmentChecking();
    }

    public void userActivityInAlwaysOn(String str) {
        Log.d("OpAodCanvas", "userActivityInAlwaysOn: reason= " + str);
        this.mCurrentTime = SystemClock.elapsedRealtime() / 1000;
        checkEnvironment();
    }

    private void checkEnvironment() {
        if (!this.mChecking) {
            this.mSensorManager.registerListener(this.mSensorEventListener, this.mSensorManager.getDefaultSensor(5), 3, 0, this.mHandler);
            this.mChecking = true;
            return;
        }
        Log.d("OpAodCanvas", "checkEnvironment: already checking...");
    }

    public void release() {
        Log.d("OpAodCanvas", "release");
        this.mObserver.unregister();
        IBgPaint iBgPaint = this.mBgPaint;
        if (iBgPaint != null) {
            iBgPaint.release();
            this.mBgPaint = null;
        }
        OpAodLowLightMask opAodLowLightMask = this.mAodMask;
        if (opAodLowLightMask != null) {
            opAodLowLightMask.release();
            this.mAodMask = null;
        }
    }

    public void onUserUnlocked() {
        this.mHandler.post(new Runnable() { // from class: com.oneplus.aod.bg.OpAodCanvas.2
            @Override // java.lang.Runnable
            public void run() {
                OpAodCanvas.this.mObserver.register();
            }
        });
    }

    public void onUserSwitching(int i) {
        Log.i("OpAodCanvas", "onUserSwitching " + i);
        this.mUserSwitching = true;
    }

    public void onUserSwitchComplete(int i) {
        Log.i("OpAodCanvas", "onUserSwitchComplete " + i);
        this.mUserSwitching = false;
        this.mHelper.loadFromCache(this.mListener, this.mHandler);
    }

    public ArrayList<Animator> genAodDisappearAnimation() {
        IBgPaint iBgPaint = this.mBgPaint;
        if (iBgPaint != null) {
            return iBgPaint.genAodDisappearAnimation();
        }
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setupPaint(IBgPaint iBgPaint, OpCanvasAodHelper.Data data) {
        IBgPaint iBgPaint2 = this.mBgPaint;
        if (iBgPaint2 != null) {
            iBgPaint2.release();
        }
        this.mBgPaint = iBgPaint;
        if (Build.DEBUG_ONEPLUS) {
            Log.i("OpAodCanvas", "Paint class= " + this.mBgPaint + " attached= " + isAttachedToWindow());
        }
        if (iBgPaint != null) {
            this.mBgPaint.setup(this);
            IBgPaint iBgPaint3 = this.mBgPaint;
            if (iBgPaint3 instanceof OpSketchPaint) {
                ((OpSketchPaint) iBgPaint3).setupContour(data);
            }
            this.mBgPaint.onSizeChanged(this.mWidth, this.mHeight);
        }
        OpAodLowLightMask opAodLowLightMask = this.mAodMask;
        if (opAodLowLightMask != null) {
            if (iBgPaint != null) {
                opAodLowLightMask.createMask();
            } else {
                opAodLowLightMask.release();
            }
        }
        if (isAttachedToWindow()) {
            invalidate();
        }
    }

    public void stopDozing() {
        Log.d("OpAodCanvas", "stopDozing");
        clearLowEnvironmentChecking();
    }

    private void clearLowEnvironmentChecking() {
        this.mLowEnviroment = false;
        this.mChecking = false;
        this.mSensorManager.unregisterListener(this.mSensorEventListener);
    }

    public void disable() {
        Log.d("OpAodCanvas", "disable");
        this.mHelper.resetPrevClockStyle();
        onChange(null, true);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onChange(Bundle bundle, boolean z) {
        this.mHelper.saveCanvasAodParams(bundle, z, this.mListener, this.mHandler);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private IBgPaint choosePaint(boolean z) {
        if (z) {
            return new OpSketchPaint();
        }
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void shouldTurnOnAodGesture() {
        OpAodUtils.shouldTurnOnAodGesture(((View) this).mContext, KeyguardUpdateMonitor.getCurrentUser());
    }

    /* access modifiers changed from: private */
    public class AodBgObserver extends ContentObserver {
        private boolean mResigerSuccess;
        private final Uri mUri = Uri.parse("content://net.oneplus.launcher.canvas.CanvasProvider/canvasAODEnabled");

        public AodBgObserver(Handler handler) {
            super(handler);
        }

        public void register() {
            if (!this.mResigerSuccess) {
                try {
                    ((View) OpAodCanvas.this).mContext.getContentResolver().registerContentObserver(this.mUri, false, this, -1);
                    Log.d("OpAodCanvas", "register");
                    this.mResigerSuccess = true;
                } catch (Exception e) {
                    if (Build.DEBUG_ONEPLUS) {
                        Log.e("OpAodCanvas", "AodBgObserver: registerContentObserver failed.", e);
                    }
                }
                if (!this.mResigerSuccess || !onChangeInner(false)) {
                    OpAodCanvas.this.mHelper.loadFromCache(OpAodCanvas.this.mListener, OpAodCanvas.this.mHandler);
                }
            }
        }

        public void unregister() {
            if (this.mResigerSuccess) {
                ((View) OpAodCanvas.this).mContext.getContentResolver().unregisterContentObserver(this);
                this.mResigerSuccess = false;
            }
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri, int i) {
            if (this.mUri.equals(uri)) {
                Log.i("OpAodCanvas", "onChange called: userid= " + i);
                onChangeInner(true);
            }
        }

        public void onChange(boolean z, Collection<Uri> collection, int i, int i2) {
            onChange(z, collection, i2);
        }

        private boolean onChangeInner(boolean z) {
            try {
                Context currentUserContext = OpContextWrapper.getCurrentUserContext(((View) OpAodCanvas.this).mContext);
                if (currentUserContext != null) {
                    KeyguardUpdateMonitor keyguardUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
                    Log.i("OpAodCanvas", "onChangeInner: contentChange= " + z + ", userId= " + currentUserContext.getUserId() + ", isSwitchingUser= " + keyguardUpdateMonitor.isSwitchingUser() + ", mUserSwitching= " + OpAodCanvas.this.mUserSwitching);
                    if (!keyguardUpdateMonitor.isSwitchingUser()) {
                        if (!OpAodCanvas.this.mUserSwitching) {
                            Bundle call = currentUserContext.getContentResolver().call(this.mUri, "canvasAODEnabled", (String) null, (Bundle) null);
                            if (call == null) {
                                Log.e("OpAodCanvas", "call is null!!!");
                            } else {
                                OpAodCanvas.this.onChange(call, z);
                                return true;
                            }
                        }
                    }
                    Log.d("OpAodCanvas", "during switching user progress, return!");
                    return false;
                }
                Log.e("OpAodCanvas", "context is null!");
            } catch (Exception e) {
                Log.e("OpAodCanvas", "onChange occur error", e);
            }
            return false;
        }
    }
}
