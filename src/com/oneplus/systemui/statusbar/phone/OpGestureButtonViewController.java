package com.oneplus.systemui.statusbar.phone;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Slog;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;
import com.android.systemui.statusbar.phone.EdgeBackGestureHandler;
import com.oneplus.phone.OPEdgeEffect;
import com.oneplus.phone.OpSideGestureConfiguration;
public class OpGestureButtonViewController {
    private static final float[] EFFECT_DIST;
    private static float[] sEffectParams;
    private Context mContext;
    private Display mDisplay;
    private float mDownPointX;
    EdgeEffectView mEdgeEffectView;
    private int mEffectHeight = 64;
    private int mEffectStage = 0;
    private boolean mIsEnabled = false;
    OemGestureButtonHandler mOemGestureButtonAnimHandler;
    WindowManager.LayoutParams mRegionWindowParams;
    private int mRotation;
    private int mScreenHeight = -1;
    private int mScreenWidth = -1;
    private final WindowManager mWm;

    static /* synthetic */ int access$208(OpGestureButtonViewController opGestureButtonViewController) {
        int i = opGestureButtonViewController.mEffectStage;
        opGestureButtonViewController.mEffectStage = i + 1;
        return i;
    }

    static {
        float[] fArr = {0.2f, 0.5f, 0.7f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.9f, 0.8f, 0.6f, 0.6f, 0.4f, 0.3f, 0.0f};
        EFFECT_DIST = fArr;
        sEffectParams = fArr;
    }

    public OpGestureButtonViewController(Context context) {
        this.mContext = context;
        this.mWm = (WindowManager) context.getSystemService(WindowManager.class);
        HandlerThread handlerThread = new HandlerThread("OemGestureBtnAnimThread", -8);
        handlerThread.start();
        this.mOemGestureButtonAnimHandler = new OemGestureButtonHandler(handlerThread.getLooper());
        updateDisplaySize();
    }

    public void onBackAction(float f) {
        this.mDownPointX = f;
        this.mOemGestureButtonAnimHandler.sendEmptyMessage(1);
    }

    public void updateRegion(boolean z) {
        this.mIsEnabled = z;
        this.mOemGestureButtonAnimHandler.sendEmptyMessage(2);
    }

    public void updateDisplaySize() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        Display defaultDisplay = this.mWm.getDefaultDisplay();
        this.mDisplay = defaultDisplay;
        defaultDisplay.getRealMetrics(displayMetrics);
        this.mScreenHeight = Math.max(displayMetrics.widthPixels, displayMetrics.heightPixels);
        this.mScreenWidth = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
        this.mRotation = this.mDisplay.getRotation();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateGestureButtonRegion() {
        if (!this.mIsEnabled) {
            EdgeEffectView edgeEffectView = this.mEdgeEffectView;
            if (edgeEffectView != null) {
                if (edgeEffectView.getParent() != null) {
                    this.mWm.removeView(this.mEdgeEffectView);
                    if (Build.DEBUG_ONEPLUS) {
                        Log.d("OpGestureController", "removeView mEdgeEffectView ");
                    }
                }
                this.mEdgeEffectView = null;
                Slog.i("OpGestureController", "updateGestureButtonRegion: not enabled");
            }
        } else if (this.mEdgeEffectView == null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(2027);
            this.mRegionWindowParams = layoutParams;
            layoutParams.flags |= 280;
            layoutParams.privateFlags |= 16;
            layoutParams.layoutInDisplayCutoutMode = 3;
            layoutParams.setTitle("GestureButtonRegion");
            WindowManager.LayoutParams layoutParams2 = this.mRegionWindowParams;
            layoutParams2.format = -3;
            layoutParams2.windowAnimations = 16973828;
            updateWindowParams();
            EdgeEffectView edgeEffectView2 = new EdgeEffectView(this.mContext);
            this.mEdgeEffectView = edgeEffectView2;
            edgeEffectView2.setLayoutParams(this.mRegionWindowParams);
            this.mWm.addView(this.mEdgeEffectView, this.mRegionWindowParams);
            if (Build.DEBUG_ONEPLUS) {
                Log.d("OpGestureController", "addView mEdgeEffectView ");
            }
        } else {
            if (Build.DEBUG_ONEPLUS) {
                Log.d("OpGestureController", "update mEdgeEffectView layout");
            }
            updateWindowParams();
            this.mWm.updateViewLayout(this.mEdgeEffectView, this.mRegionWindowParams);
        }
    }

    /* access modifiers changed from: private */
    public class OemGestureButtonHandler extends Handler {
        public OemGestureButtonHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 1) {
                OpGestureButtonViewController.this.mEdgeEffectView.vibrate();
                if (OpGestureButtonViewController.this.isPortrait() && !EdgeBackGestureHandler.sSideGestureEnabled) {
                    OpGestureButtonViewController.this.mOemGestureButtonAnimHandler.sendEmptyMessage(3);
                }
            } else if (i == 2) {
                OpGestureButtonViewController.this.updateGestureButtonRegion();
            } else if (i == 3) {
                OpGestureButtonViewController opGestureButtonViewController = OpGestureButtonViewController.this;
                if (opGestureButtonViewController.mEdgeEffectView == null) {
                    return;
                }
                if (opGestureButtonViewController.mEffectStage <= OpGestureButtonViewController.sEffectParams.length - 1) {
                    OpGestureButtonViewController.this.mEdgeEffectView.setSize();
                    OpGestureButtonViewController.this.mEdgeEffectView.onPull(OpGestureButtonViewController.sEffectParams[OpGestureButtonViewController.this.mEffectStage]);
                    OpGestureButtonViewController.access$208(OpGestureButtonViewController.this);
                    OpGestureButtonViewController.this.mOemGestureButtonAnimHandler.sendEmptyMessageDelayed(3, 16);
                    return;
                }
                OpGestureButtonViewController.this.mEdgeEffectView.onRelease();
                OpGestureButtonViewController.this.mEffectStage = 0;
            }
        }
    }

    private void updateWindowParams() {
        if (this.mRegionWindowParams != null) {
            if (isPortrait()) {
                WindowManager.LayoutParams layoutParams = this.mRegionWindowParams;
                layoutParams.gravity = 80;
                layoutParams.width = -1;
                layoutParams.height = this.mEffectHeight;
            } else {
                this.mRegionWindowParams.gravity = this.mRotation == 1 ? 5 : 3;
                WindowManager.LayoutParams layoutParams2 = this.mRegionWindowParams;
                layoutParams2.width = this.mEffectHeight;
                layoutParams2.height = -1;
            }
            if (EdgeBackGestureHandler.sSideGestureEnabled) {
                WindowManager.LayoutParams layoutParams3 = this.mRegionWindowParams;
                layoutParams3.width = -1;
                layoutParams3.height = (int) (((float) (isPortrait() ? this.mScreenHeight : this.mScreenWidth)) * (1.0f - OpSideGestureConfiguration.PORTRAIT_NON_DETECT_SCALE));
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isPortrait() {
        int i = this.mRotation;
        return i == 0 || i == 2;
    }

    /* access modifiers changed from: private */
    public class EdgeEffectView extends ImageView {
        OPEdgeEffect mEffect;

        public EdgeEffectView(Context context) {
            super(context);
            this.mEffect = new OPEdgeEffect(context);
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.ImageView, android.view.View
        public void onDraw(Canvas canvas) {
            int i;
            super.onDraw(canvas);
            if (!this.mEffect.isFinished() && OpGestureButtonViewController.this.isPortrait()) {
                if (OpGestureButtonViewController.this.mDownPointX < ((float) OpGestureButtonViewController.this.mScreenWidth) / 3.0f) {
                    i = OpGestureButtonViewController.this.mScreenWidth / 3;
                } else {
                    i = OpGestureButtonViewController.this.mScreenWidth;
                }
                canvas.rotate((float) 180);
                canvas.translate((float) (-i), (float) (-(OpGestureButtonViewController.this.mScreenWidth / 18)));
                this.mEffect.draw(canvas);
            }
        }

        public void onPull(float f) {
            this.mEffect.onPull(f);
            postInvalidateOnAnimation();
        }

        public void setSize() {
            this.mEffect.setSize(OpGestureButtonViewController.this.mScreenWidth / 3, OpGestureButtonViewController.this.mScreenWidth / 3);
        }

        public void onRelease() {
            this.mEffect.onRelease();
            this.mEffect.finish();
            postInvalidateOnAnimation();
        }

        public void vibrate() {
            performHapticFeedback(1);
        }
    }
}
