package com.oneplus.systemui.statusbar.policy;

import android.animation.Animator;
import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.statusbar.policy.KeyButtonView;
import com.oneplus.util.OpUtils;
public class OpHomeButton extends KeyButtonView {
    private AnimDirection mAnimDirection = AnimDirection.NONE;
    private Display mDisplay;
    private int mDownX;
    private int mDownY;
    private float mOriginTranX;
    private float mOriginTranY;
    private int mScreenWidth;
    private Animator mStartAnim = null;
    private boolean mStartAnimPlayed = false;
    private float mStartAnimTargetTranX = 0.0f;
    private float mStartAnimTargetTranY = 0.0f;
    private int mTranslationLimit;
    private int mTranslationRestore;
    private int mTranslationReverse;

    public enum AnimDirection {
        NONE,
        UP,
        DOWN,
        LEFT,
        RIGHT
    }

    public OpHomeButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        Resources resources = context.getResources();
        this.mTranslationLimit = resources.getDimensionPixelSize(C0005R$dimen.op_nav_home_handle_translation_limit);
        this.mTranslationReverse = resources.getDimensionPixelSize(C0005R$dimen.op_nav_home_handle_anim_reverse);
        this.mTranslationRestore = resources.getDimensionPixelSize(C0005R$dimen.op_nav_home_handle_anim_restore);
        this.mOriginTranX = getTranslationX();
        this.mOriginTranY = getTranslationY();
        this.mDisplay = ((WindowManager) context.getSystemService(WindowManager.class)).getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.mDisplay.getRealMetrics(displayMetrics);
        this.mScreenWidth = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
    }

    public OpHomeButton(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public void handleTouch(MotionEvent motionEvent) {
        int x = (int) motionEvent.getX();
        int y = (int) motionEvent.getY();
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked != 0) {
            if (actionMasked != 1) {
                if (actionMasked == 2) {
                    int abs = Math.abs(x - this.mDownX);
                    int abs2 = Math.abs(y - this.mDownY);
                    if (!this.mStartAnimPlayed && (abs >= 40 || abs2 >= 40)) {
                        if (this.mAnimDirection == AnimDirection.NONE && abs <= abs2 && abs < abs2) {
                            this.mAnimDirection = y - this.mDownY > 0 ? AnimDirection.DOWN : AnimDirection.UP;
                        }
                        doStartAnim();
                        return;
                    } else if (OpUtils.DEBUG_ONEPLUS) {
                        Log.d("OpHomeButton", "Animation played " + this.mStartAnimPlayed + ", xDiff " + abs + ", yDiff " + abs2);
                        return;
                    } else {
                        return;
                    }
                } else if (actionMasked != 3) {
                    return;
                }
            }
            if (this.mAnimDirection == AnimDirection.NONE) {
                reset();
            } else {
                doEndAnim();
            }
        } else {
            this.mAnimDirection = AnimDirection.NONE;
            this.mDownX = x;
            this.mDownY = y;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0031, code lost:
        if (r6.mDisplay.getRotation() == 2) goto L_0x0037;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void doStartAnim() {
        /*
            r6 = this;
            com.oneplus.systemui.statusbar.policy.OpHomeButton$AnimDirection r0 = r6.mAnimDirection
            com.oneplus.systemui.statusbar.policy.OpHomeButton$AnimDirection r1 = com.oneplus.systemui.statusbar.policy.OpHomeButton.AnimDirection.DOWN
            r2 = 2
            if (r0 != r1) goto L_0x0037
            boolean r0 = com.oneplus.systemui.statusbar.phone.OpEdgeNavGestureHandler.isOneHandedEnable()
            if (r0 == 0) goto L_0x0033
            int r0 = r6.mDownX
            float r1 = (float) r0
            int r3 = r6.mScreenWidth
            float r4 = (float) r3
            r5 = 1077936128(0x40400000, float:3.0)
            float r4 = r4 / r5
            int r1 = (r1 > r4 ? 1 : (r1 == r4 ? 0 : -1))
            if (r1 < 0) goto L_0x0033
            float r0 = (float) r0
            float r1 = (float) r3
            float r3 = (float) r3
            float r3 = r3 / r5
            float r1 = r1 - r3
            int r0 = (r0 > r1 ? 1 : (r0 == r1 ? 0 : -1))
            if (r0 > 0) goto L_0x0033
            android.view.Display r0 = r6.mDisplay
            int r0 = r0.getRotation()
            if (r0 == 0) goto L_0x0037
            android.view.Display r0 = r6.mDisplay
            int r0 = r0.getRotation()
            if (r0 == r2) goto L_0x0037
        L_0x0033:
            r6.reset()
            return
        L_0x0037:
            int[] r0 = com.oneplus.systemui.statusbar.policy.OpHomeButton.AnonymousClass3.$SwitchMap$com$oneplus$systemui$statusbar$policy$OpHomeButton$AnimDirection
            com.oneplus.systemui.statusbar.policy.OpHomeButton$AnimDirection r1 = r6.mAnimDirection
            int r1 = r1.ordinal()
            r0 = r0[r1]
            r1 = 0
            r3 = 1
            if (r0 == r3) goto L_0x0072
            if (r0 == r2) goto L_0x0072
            r4 = 3
            if (r0 == r4) goto L_0x004e
            r4 = 4
            if (r0 == r4) goto L_0x004e
            goto L_0x0095
        L_0x004e:
            float r0 = r6.mOriginTranY
            com.oneplus.systemui.statusbar.policy.OpHomeButton$AnimDirection r4 = r6.mAnimDirection
            com.oneplus.systemui.statusbar.policy.OpHomeButton$AnimDirection r5 = com.oneplus.systemui.statusbar.policy.OpHomeButton.AnimDirection.UP
            if (r4 != r5) goto L_0x005b
            int r4 = r6.mTranslationLimit
            int r4 = 0 - r4
            goto L_0x005d
        L_0x005b:
            int r4 = r6.mTranslationLimit
        L_0x005d:
            float r4 = (float) r4
            float r0 = r0 + r4
            r6.mStartAnimTargetTranY = r0
            float[] r2 = new float[r2]
            float r4 = r6.mOriginTranY
            r2[r1] = r4
            r2[r3] = r0
            java.lang.String r0 = "translationY"
            android.animation.ObjectAnimator r0 = android.animation.ObjectAnimator.ofFloat(r6, r0, r2)
            r6.mStartAnim = r0
            goto L_0x0095
        L_0x0072:
            float r0 = r6.mOriginTranX
            com.oneplus.systemui.statusbar.policy.OpHomeButton$AnimDirection r4 = r6.mAnimDirection
            com.oneplus.systemui.statusbar.policy.OpHomeButton$AnimDirection r5 = com.oneplus.systemui.statusbar.policy.OpHomeButton.AnimDirection.LEFT
            if (r4 != r5) goto L_0x007f
            int r4 = r6.mTranslationLimit
            int r4 = 0 - r4
            goto L_0x0081
        L_0x007f:
            int r4 = r6.mTranslationLimit
        L_0x0081:
            float r4 = (float) r4
            float r0 = r0 + r4
            r6.mStartAnimTargetTranX = r0
            float[] r2 = new float[r2]
            float r4 = r6.mOriginTranX
            r2[r1] = r4
            r2[r3] = r0
            java.lang.String r0 = "translationX"
            android.animation.ObjectAnimator r0 = android.animation.ObjectAnimator.ofFloat(r6, r0, r2)
            r6.mStartAnim = r0
        L_0x0095:
            android.animation.Animator r0 = r6.mStartAnim
            if (r0 == 0) goto L_0x00ac
            r6.mStartAnimPlayed = r3
            r1 = 150(0x96, double:7.4E-322)
            r0.setDuration(r1)
            android.animation.Animator r0 = r6.mStartAnim
            android.view.animation.Interpolator r1 = com.android.systemui.Interpolators.CUSTOM_40_40
            r0.setInterpolator(r1)
            android.animation.Animator r6 = r6.mStartAnim
            r6.start()
        L_0x00ac:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.systemui.statusbar.policy.OpHomeButton.doStartAnim():void");
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.oneplus.systemui.statusbar.policy.OpHomeButton$3  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass3 {
        static final /* synthetic */ int[] $SwitchMap$com$oneplus$systemui$statusbar$policy$OpHomeButton$AnimDirection;

        static {
            int[] iArr = new int[AnimDirection.values().length];
            $SwitchMap$com$oneplus$systemui$statusbar$policy$OpHomeButton$AnimDirection = iArr;
            try {
                iArr[AnimDirection.LEFT.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$oneplus$systemui$statusbar$policy$OpHomeButton$AnimDirection[AnimDirection.RIGHT.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$oneplus$systemui$statusbar$policy$OpHomeButton$AnimDirection[AnimDirection.DOWN.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$oneplus$systemui$statusbar$policy$OpHomeButton$AnimDirection[AnimDirection.UP.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$com$oneplus$systemui$statusbar$policy$OpHomeButton$AnimDirection[AnimDirection.NONE.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0031, code lost:
        if (r8.mDisplay.getRotation() == 2) goto L_0x0037;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void doEndAnim() {
        /*
        // Method dump skipped, instructions count: 268
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.systemui.statusbar.policy.OpHomeButton.doEndAnim():void");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reset() {
        this.mAnimDirection = AnimDirection.NONE;
        this.mStartAnimPlayed = false;
        this.mStartAnim = null;
        this.mStartAnimTargetTranX = 0.0f;
        this.mStartAnimTargetTranY = 0.0f;
    }
}
