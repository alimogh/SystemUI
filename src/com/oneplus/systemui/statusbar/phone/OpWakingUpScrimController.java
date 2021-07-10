package com.oneplus.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowManager;
import com.android.internal.os.SomeArgs;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.C0011R$layout;
import com.android.systemui.Dependency;
import com.oneplus.aod.OpWakingUpScrim;
import com.oneplus.util.OpUtils;
public class OpWakingUpScrimController {
    private Context mContext;
    private boolean mIsAnimationStarted;
    private KeyguardUpdateMonitor mKeyguardUpdateMonitor = ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class));
    private boolean mRequestShow;
    private Animator mScrimAnimator;
    private OpWakingUpScrim mScrimView;
    private Handler mUIHandler;
    private HandlerThread mUIHandlerThread;
    private final WindowManager mWindowManager;

    public void initHandler(Looper looper) {
        this.mUIHandler = new Handler(looper) { // from class: com.oneplus.systemui.statusbar.phone.OpWakingUpScrimController.1
            @Override // android.os.Handler
            public void handleMessage(Message message) {
                int i = message.what;
                if (i == 1) {
                    SomeArgs someArgs = (SomeArgs) message.obj;
                    OpWakingUpScrimController.this.handleStartAnimation(((Boolean) someArgs.arg1).booleanValue(), ((Boolean) someArgs.arg2).booleanValue());
                } else if (i == 2) {
                    OpWakingUpScrimController.this.handleAddToWindow(((Boolean) message.obj).booleanValue());
                } else if (i == 3) {
                    OpWakingUpScrimController.this.handleRemoveFromWindow();
                } else if (i == 4) {
                    OpWakingUpScrimController.this.handleFingerprintAuthenticated();
                }
            }
        };
    }

    public OpWakingUpScrimController(Context context) {
        new Handler(Looper.getMainLooper());
        this.mContext = context;
        this.mScrimView = (OpWakingUpScrim) LayoutInflater.from(context).inflate(C0011R$layout.op_wakingup_scrim_view, (ViewGroup) null);
        HandlerThread handlerThread = new HandlerThread("WakingUpScrimUI", -8);
        this.mUIHandlerThread = handlerThread;
        handlerThread.start();
        initHandler(this.mUIHandlerThread.getLooper());
        this.mWindowManager = (WindowManager) context.getSystemService("window");
    }

    public void prepare() {
        if (OpUtils.DEBUG_ONEPLUS) {
            Log.d("OpWakingUpScrimController", "AddToWindow");
        }
        boolean z = true;
        this.mRequestShow = true;
        if (this.mIsAnimationStarted && this.mScrimAnimator != null) {
            this.mUIHandler.post(new Runnable() { // from class: com.oneplus.systemui.statusbar.phone.OpWakingUpScrimController.2
                @Override // java.lang.Runnable
                public void run() {
                    OpWakingUpScrimController.this.mScrimAnimator.cancel();
                }
            });
        }
        if (!this.mUIHandler.hasMessages(3) && !this.mScrimView.isAttachedToWindow()) {
            z = false;
        }
        this.mUIHandler.removeMessages(3);
        this.mUIHandler.removeMessages(2);
        this.mUIHandler.obtainMessage(2, Boolean.valueOf(z)).sendToTarget();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleAddToWindow(boolean z) {
        if (OpUtils.DEBUG_ONEPLUS) {
            Log.d("OpWakingUpScrimController", "handleAddToWindow:" + this.mScrimView.isAttachedToWindow() + " justResetState:" + z + " mRequestShow: " + this.mRequestShow);
        }
        if (this.mScrimView.isAttachedToWindow() || !this.mRequestShow) {
            this.mScrimView.reset();
            return;
        }
        try {
            this.mWindowManager.addView(this.mScrimView, getWindowLayoutParams());
        } catch (IllegalStateException e) {
            Log.e("OpWakingUpScrimController", "addView occur exception. isAttachedToWindow= " + this.mScrimView.isAttachedToWindow(), e);
        }
        this.mIsAnimationStarted = false;
        this.mScrimView.reset();
        this.mScrimView.setVisibility(0);
        this.mScrimView.setSystemUiVisibility(this.mScrimView.getSystemUiVisibility() | 1792);
        this.mScrimView.getWindowInsetsController().hide(WindowInsets.Type.navigationBars());
    }

    public void onFingerprintAuthenticated() {
        this.mUIHandler.sendEmptyMessage(4);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFingerprintAuthenticated() {
        OpWakingUpScrim opWakingUpScrim = this.mScrimView;
        if (opWakingUpScrim != null && opWakingUpScrim.getWindowInsetsController() != null) {
            this.mScrimView.getWindowInsetsController().show(WindowInsets.Type.navigationBars());
        }
    }

    public void startAnimation(boolean z, boolean z2) {
        if (!this.mScrimView.isAttachedToWindow()) {
            Log.w("OpWakingUpScrimController", "stop startAnimation window desn't attached");
        } else if (this.mIsAnimationStarted) {
            Log.w("OpWakingUpScrimController", "stop startAnimation since it's started");
        } else {
            if (OpUtils.DEBUG_ONEPLUS) {
                Log.d("OpWakingUpScrimController", "startAnimation:" + z + ", " + z2);
            }
            this.mRequestShow = false;
            SomeArgs obtain = SomeArgs.obtain();
            obtain.arg1 = Boolean.valueOf(z);
            obtain.arg2 = Boolean.valueOf(z2);
            this.mUIHandler.removeMessages(1);
            this.mUIHandler.obtainMessage(1, obtain).sendToTarget();
            this.mIsAnimationStarted = true;
        }
    }

    public void handleStartAnimation(boolean z, boolean z2) {
        Animator animator;
        Animator animator2 = this.mScrimAnimator;
        if (animator2 == null || !animator2.isRunning()) {
            if (this.mIsAnimationStarted && (animator = this.mScrimAnimator) != null) {
                animator.cancel();
            }
            if (z) {
                this.mScrimAnimator = this.mScrimView.getDisappearAnimationWithDelay();
            } else {
                this.mScrimAnimator = this.mScrimView.getDisappearAnimationWithoutDelay(z2);
            }
            Log.w("OpWakingUpScrimController", "handleStartAnimation withDelay:" + z);
            this.mScrimAnimator.addListener(new AnimatorListenerAdapter() { // from class: com.oneplus.systemui.statusbar.phone.OpWakingUpScrimController.3
                boolean mCancelled;

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationStart(Animator animator3) {
                    this.mCancelled = false;
                    Log.i("OpWakingUpScrimController", "WakingUpScrimView onAnimationStart");
                    if (OpWakingUpScrimController.this.mKeyguardUpdateMonitor != null) {
                        OpWakingUpScrimController.this.mKeyguardUpdateMonitor.onWakingUpScrimAnimationStart(System.currentTimeMillis());
                    }
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationCancel(Animator animator3) {
                    Log.i("OpWakingUpScrimController", "WakingUpScrimView onAnimationCancel");
                    this.mCancelled = true;
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator3) {
                    Log.i("OpWakingUpScrimController", "WakingUpScrimView onAnimationEnd: cance " + this.mCancelled);
                    if (!this.mCancelled) {
                        OpWakingUpScrimController.this.selfRemoveFromWindow(false);
                    }
                    OpWakingUpScrimController.this.mIsAnimationStarted = false;
                }
            });
            this.mScrimAnimator.start();
            return;
        }
        Log.w("OpWakingUpScrimController", "animation running");
    }

    public void removeFromWindow(boolean z) {
        Log.d("OpWakingUpScrimController", "removeFromWindow, remove: " + z);
        this.mRequestShow = false;
        selfRemoveFromWindow(z);
    }

    public void removeFromWindowForCameraLaunched() {
        Log.d("OpWakingUpScrimController", "removeFromWindowForCameraLaunched");
        this.mRequestShow = false;
        int i = 325;
        if (OpUtils.DEBUG_ONEPLUS) {
            i = SystemProperties.getInt("debug.remove.window.camera.launched", 325);
            Log.i("OpWakingUpScrimController", "removeFromWindowForCameraLaunched, DEBUG, delay:" + i);
        }
        selfRemoveFromWindow(false, i);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void selfRemoveFromWindow(boolean z) {
        selfRemoveFromWindow(z, 200);
    }

    private void selfRemoveFromWindow(boolean z, int i) {
        Log.d("OpWakingUpScrimController", "selfRemoveFromWindow, remove: " + z + ", delay: " + i);
        this.mUIHandler.removeMessages(3);
        if (z) {
            this.mUIHandler.obtainMessage(3).sendToTarget();
            return;
        }
        Handler handler = this.mUIHandler;
        handler.sendMessageDelayed(handler.obtainMessage(3), (long) i);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleRemoveFromWindow() {
        Animator animator = this.mScrimAnimator;
        if (animator != null && animator.isRunning()) {
            Log.d("OpWakingUpScrimController", "animation still play, remove window until animation end");
        } else if (this.mScrimView == null) {
            Log.d("OpWakingUpScrimController", "scrim view is null");
        } else {
            Log.d("OpWakingUpScrimController", "handleRemoveFromWindow, attached: " + this.mScrimView.isAttachedToWindow() + ", mRequestShow: " + this.mRequestShow);
            if (this.mScrimView.isAttachedToWindow() && !this.mRequestShow) {
                try {
                    if (!(this.mScrimView.getWindowInsetsController() == null || this.mKeyguardUpdateMonitor == null || !this.mKeyguardUpdateMonitor.isFingerprintAlreadyAuthenticated())) {
                        this.mScrimView.getWindowInsetsController().show(WindowInsets.Type.navigationBars());
                    }
                    this.mWindowManager.removeViewImmediate(this.mScrimView);
                } catch (IllegalStateException e) {
                    Log.e("OpWakingUpScrimController", "removeViewImmediate occur exception. isAttachedToWindow= " + this.mScrimView.isAttachedToWindow(), e);
                }
            }
        }
    }

    private WindowManager.LayoutParams getWindowLayoutParams() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.type = 2307;
        layoutParams.privateFlags = 16;
        layoutParams.layoutInDisplayCutoutMode = 3;
        layoutParams.flags = 1304;
        layoutParams.format = -2;
        layoutParams.width = -1;
        layoutParams.height = -1;
        layoutParams.gravity = 17;
        layoutParams.screenOrientation = 1;
        layoutParams.setTitle("OpWakingUpScrim");
        layoutParams.softInputMode = 3;
        return layoutParams;
    }
}
