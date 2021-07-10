package com.oneplus.keyguard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.R$styleable;
import java.util.ArrayList;
import java.util.Stack;
public class OpPasswordTextViewForPin extends View {
    private Interpolator mAppearInterpolator;
    private int mCharPadding;
    private Stack<CharState> mCharPool;
    private Interpolator mDisappearInterpolator;
    private int mDotSize;
    private final Paint mDrawPaint;
    private onTextChangedListerner mOnTextChangeListerner;
    private PowerManager mPM;
    private int mPinPasswordLength;
    private boolean mShowPassword;
    private String mText;
    private ArrayList<CharState> mTextChars;
    private final int mTextHeightRaw;
    private UserActivityListener mUserActivityListener;

    public interface UserActivityListener {
        void onCheckPasswordAndUnlock();

        void onUserActivity();
    }

    public interface onTextChangedListerner {
        void onTextChanged(String str);
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    public OpPasswordTextViewForPin(Context context) {
        this(context, null);
    }

    public OpPasswordTextViewForPin(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public OpPasswordTextViewForPin(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    /* JADX INFO: finally extract failed */
    public OpPasswordTextViewForPin(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mTextChars = new ArrayList<>();
        this.mText = "";
        this.mCharPool = new Stack<>();
        this.mDrawPaint = new Paint();
        new Paint();
        new Paint();
        new Paint();
        new Paint();
        new Paint();
        boolean z = false;
        this.mPinPasswordLength = 0;
        setFocusableInTouchMode(true);
        setFocusable(true);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.PasswordTextView);
        try {
            this.mTextHeightRaw = obtainStyledAttributes.getInt(R$styleable.PasswordTextView_scaledTextSize, 0);
            obtainStyledAttributes.getInt(R$styleable.PasswordTextView_android_gravity, 17);
            this.mDotSize = obtainStyledAttributes.getDimensionPixelSize(R$styleable.PasswordTextView_dotSize, getContext().getResources().getDimensionPixelSize(C0005R$dimen.password_dot_size));
            this.mCharPadding = obtainStyledAttributes.getDimensionPixelSize(R$styleable.PasswordTextView_charPadding, getContext().getResources().getDimensionPixelSize(C0005R$dimen.password_char_padding));
            this.mDrawPaint.setColor(obtainStyledAttributes.getColor(R$styleable.PasswordTextView_android_textColor, -1));
            obtainStyledAttributes.recycle();
            this.mDrawPaint.setFlags(129);
            this.mDrawPaint.setTextAlign(Paint.Align.CENTER);
            this.mShowPassword = Settings.System.getInt(((View) this).mContext.getContentResolver(), "show_password", 1) == 1 ? true : z;
            this.mAppearInterpolator = AnimationUtils.loadInterpolator(((View) this).mContext, 17563662);
            this.mDisappearInterpolator = AnimationUtils.loadInterpolator(((View) this).mContext, 17563663);
            AnimationUtils.loadInterpolator(((View) this).mContext, 17563661);
            this.mPM = (PowerManager) ((View) this).mContext.getSystemService("power");
            this.mPinPasswordLength = KeyguardUpdateMonitor.getInstance(context).keyguardPinPasswordLength();
        } catch (Throwable th) {
            obtainStyledAttributes.recycle();
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        float width = ((float) (getWidth() / 2)) - (getDrawingWidth() / 2.0f);
        int size = this.mTextChars.size();
        Rect charBounds = getCharBounds();
        int i = charBounds.bottom - charBounds.top;
        float height = ((float) getHeight()) / 2.0f;
        float f = (float) (charBounds.right - charBounds.left);
        for (int i2 = 0; i2 < size; i2++) {
            width += this.mTextChars.get(i2).draw(canvas, width, i, height, f);
        }
    }

    private Rect getCharBounds() {
        this.mDrawPaint.setTextSize(((float) this.mTextHeightRaw) * getResources().getDisplayMetrics().scaledDensity);
        Rect rect = new Rect();
        this.mDrawPaint.getTextBounds("0", 0, 1, rect);
        return rect;
    }

    private float getDrawingWidth() {
        int size = this.mTextChars.size();
        Rect charBounds = getCharBounds();
        int i = charBounds.right - charBounds.left;
        int i2 = 0;
        for (int i3 = 0; i3 < size; i3++) {
            CharState charState = this.mTextChars.get(i3);
            if (i3 != 0) {
                i2 = (int) (((float) i2) + (((float) this.mCharPadding) * charState.currentWidthFactor));
            }
            i2 = (int) (((float) i2) + (((float) i) * charState.currentWidthFactor));
        }
        return (float) i2;
    }

    public void append(char c) {
        CharState charState;
        int size = this.mTextChars.size();
        String str = this.mText;
        String str2 = this.mText + c;
        this.mText = str2;
        int length = str2.length();
        if (length > size) {
            charState = obtainCharState(c);
            this.mTextChars.add(charState);
        } else {
            CharState charState2 = this.mTextChars.get(length - 1);
            charState2.whichChar = c;
            charState = charState2;
        }
        charState.startAppearAnimation();
        if (length > 1) {
            CharState charState3 = this.mTextChars.get(length - 2);
            if (charState3.isDotSwapPending) {
                charState3.swapToDotWhenAppearFinished();
            }
        }
        int i = this.mPinPasswordLength;
        boolean z = i != 0 && length == i;
        if (length == 16 || z) {
            new Handler().post(new Runnable() { // from class: com.oneplus.keyguard.OpPasswordTextViewForPin.1
                @Override // java.lang.Runnable
                public void run() {
                    OpPasswordTextViewForPin.this.mUserActivityListener.onCheckPasswordAndUnlock();
                }
            });
        }
        onTextChangedListerner ontextchangedlisterner = this.mOnTextChangeListerner;
        if (ontextchangedlisterner != null) {
            ontextchangedlisterner.onTextChanged(this.mText);
        }
        userActivity();
        sendAccessibilityEventTypeViewTextChanged(str, str.length(), 0, 1);
    }

    public void setUserActivityListener(UserActivityListener userActivityListener) {
        this.mUserActivityListener = userActivityListener;
    }

    private void userActivity() {
        this.mPM.userActivity(SystemClock.uptimeMillis(), false);
        UserActivityListener userActivityListener = this.mUserActivityListener;
        if (userActivityListener != null) {
            userActivityListener.onUserActivity();
        }
    }

    public void deleteLastChar() {
        int length = this.mText.length();
        String str = this.mText;
        if (length > 0) {
            int i = length - 1;
            this.mText = str.substring(0, i);
            this.mTextChars.get(i).startRemoveAnimation(0, 0);
        }
        onTextChangedListerner ontextchangedlisterner = this.mOnTextChangeListerner;
        if (ontextchangedlisterner != null) {
            ontextchangedlisterner.onTextChanged(this.mText);
        }
        userActivity();
        sendAccessibilityEventTypeViewTextChanged(str, str.length() - 1, 1, 0);
    }

    public String getText() {
        return this.mText;
    }

    private CharState obtainCharState(char c) {
        CharState charState;
        if (this.mCharPool.isEmpty()) {
            charState = new CharState();
        } else {
            charState = this.mCharPool.pop();
            charState.reset();
        }
        charState.whichChar = c;
        return charState;
    }

    public void reset(boolean z, boolean z2) {
        String str = this.mText;
        this.mText = "";
        int size = this.mTextChars.size();
        int i = size - 1;
        int i2 = i / 2;
        int i3 = 0;
        while (i3 < size) {
            CharState charState = this.mTextChars.get(i3);
            if (z) {
                charState.startRemoveAnimation(Math.min(((long) (i3 <= i2 ? i3 * 2 : i - (((i3 - i2) - 1) * 2))) * 40, 200L), Math.min(40 * ((long) i), 200L) + 160);
                charState.removeDotSwapCallbacks();
            } else {
                this.mCharPool.push(charState);
            }
            i3++;
        }
        if (!z) {
            this.mTextChars.clear();
        }
        onTextChangedListerner ontextchangedlisterner = this.mOnTextChangeListerner;
        if (ontextchangedlisterner != null) {
            ontextchangedlisterner.onTextChanged(this.mText);
        }
        if (z2) {
            sendAccessibilityEventTypeViewTextChanged(str, 0, str.length(), 0);
        }
    }

    /* access modifiers changed from: package-private */
    public void sendAccessibilityEventTypeViewTextChanged(String str, int i, int i2, int i3) {
        if (!AccessibilityManager.getInstance(((View) this).mContext).isEnabled()) {
            return;
        }
        if (isFocused() || (isSelected() && isShown())) {
            if (!shouldSpeakPasswordsForAccessibility()) {
                str = null;
            }
            AccessibilityEvent obtain = AccessibilityEvent.obtain(16);
            obtain.setFromIndex(i);
            obtain.setRemovedCount(i2);
            obtain.setAddedCount(i3);
            obtain.setBeforeText(str);
            obtain.setPassword(true);
            sendAccessibilityEventUnchecked(obtain);
        }
    }

    @Override // android.view.View
    public void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        super.onInitializeAccessibilityEvent(accessibilityEvent);
        accessibilityEvent.setClassName(OpPasswordTextViewForPin.class.getName());
        accessibilityEvent.setPassword(true);
    }

    @Override // android.view.View
    public void onPopulateAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        super.onPopulateAccessibilityEvent(accessibilityEvent);
        if (shouldSpeakPasswordsForAccessibility()) {
            String str = this.mText;
            if (!TextUtils.isEmpty(str)) {
                accessibilityEvent.getText().add(str);
            }
        }
    }

    @Override // android.view.View
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        accessibilityNodeInfo.setClassName(OpPasswordTextViewForPin.class.getName());
        accessibilityNodeInfo.setPassword(true);
        if (shouldSpeakPasswordsForAccessibility()) {
            accessibilityNodeInfo.setText(this.mText);
        }
        accessibilityNodeInfo.setEditable(true);
        accessibilityNodeInfo.setInputType(16);
    }

    private boolean shouldSpeakPasswordsForAccessibility() {
        return Settings.Secure.getIntForUser(((View) this).mContext.getContentResolver(), "speak_password", 0, -3) == 1;
    }

    /* access modifiers changed from: private */
    public class CharState {
        float currentDotSizeFactor;
        float currentTextSizeFactor;
        float currentTextTranslationY;
        float currentWidthFactor;
        boolean dotAnimationIsGrowing;
        Animator dotAnimator;
        Animator.AnimatorListener dotFinishListener;
        private ValueAnimator.AnimatorUpdateListener dotSizeUpdater;
        private Runnable dotSwapperRunnable;
        boolean isDotSwapPending;
        Animator.AnimatorListener removeEndListener;
        boolean textAnimationIsGrowing;
        ValueAnimator textAnimator;
        Animator.AnimatorListener textFinishListener;
        private ValueAnimator.AnimatorUpdateListener textSizeUpdater;
        ValueAnimator textTranslateAnimator;
        Animator.AnimatorListener textTranslateFinishListener;
        private ValueAnimator.AnimatorUpdateListener textTranslationUpdater;
        char whichChar;
        boolean widthAnimationIsGrowing;
        ValueAnimator widthAnimator;
        Animator.AnimatorListener widthFinishListener;
        private ValueAnimator.AnimatorUpdateListener widthUpdater;

        private CharState() {
            this.currentTextTranslationY = 1.0f;
            this.removeEndListener = new AnimatorListenerAdapter() { // from class: com.oneplus.keyguard.OpPasswordTextViewForPin.CharState.1
                private boolean mCancelled;

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationCancel(Animator animator) {
                    this.mCancelled = true;
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    if (!this.mCancelled) {
                        OpPasswordTextViewForPin.this.mTextChars.remove(CharState.this);
                        OpPasswordTextViewForPin.this.mCharPool.push(CharState.this);
                        CharState.this.reset();
                        CharState charState = CharState.this;
                        charState.cancelAnimator(charState.textTranslateAnimator);
                        CharState.this.textTranslateAnimator = null;
                    }
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationStart(Animator animator) {
                    this.mCancelled = false;
                }
            };
            this.dotFinishListener = new AnimatorListenerAdapter() { // from class: com.oneplus.keyguard.OpPasswordTextViewForPin.CharState.2
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    CharState.this.dotAnimator = null;
                }
            };
            this.textFinishListener = new AnimatorListenerAdapter() { // from class: com.oneplus.keyguard.OpPasswordTextViewForPin.CharState.3
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    CharState.this.textAnimator = null;
                }
            };
            this.textTranslateFinishListener = new AnimatorListenerAdapter() { // from class: com.oneplus.keyguard.OpPasswordTextViewForPin.CharState.4
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    CharState.this.textTranslateAnimator = null;
                }
            };
            this.widthFinishListener = new AnimatorListenerAdapter() { // from class: com.oneplus.keyguard.OpPasswordTextViewForPin.CharState.5
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    CharState.this.widthAnimator = null;
                }
            };
            this.dotSizeUpdater = new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.keyguard.OpPasswordTextViewForPin.CharState.6
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    CharState.this.currentDotSizeFactor = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                    OpPasswordTextViewForPin.this.invalidate();
                }
            };
            this.textSizeUpdater = new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.keyguard.OpPasswordTextViewForPin.CharState.7
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    CharState.this.currentTextSizeFactor = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                    OpPasswordTextViewForPin.this.invalidate();
                }
            };
            this.textTranslationUpdater = new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.keyguard.OpPasswordTextViewForPin.CharState.8
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    CharState.this.currentTextTranslationY = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                    OpPasswordTextViewForPin.this.invalidate();
                }
            };
            this.widthUpdater = new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.keyguard.OpPasswordTextViewForPin.CharState.9
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    CharState.this.currentWidthFactor = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                    OpPasswordTextViewForPin.this.invalidate();
                }
            };
            this.dotSwapperRunnable = new Runnable() { // from class: com.oneplus.keyguard.OpPasswordTextViewForPin.CharState.10
                @Override // java.lang.Runnable
                public void run() {
                    CharState.this.performSwap();
                    CharState.this.isDotSwapPending = false;
                }
            };
        }

        /* access modifiers changed from: package-private */
        public void reset() {
            this.whichChar = 0;
            this.currentTextSizeFactor = 0.0f;
            this.currentDotSizeFactor = 0.0f;
            this.currentWidthFactor = 0.0f;
            cancelAnimator(this.textAnimator);
            this.textAnimator = null;
            cancelAnimator(this.dotAnimator);
            this.dotAnimator = null;
            cancelAnimator(this.widthAnimator);
            this.widthAnimator = null;
            this.currentTextTranslationY = 1.0f;
            removeDotSwapCallbacks();
        }

        /* access modifiers changed from: package-private */
        public void startRemoveAnimation(long j, long j2) {
            boolean z = true;
            boolean z2 = (this.currentDotSizeFactor > 0.0f && this.dotAnimator == null) || (this.dotAnimator != null && this.dotAnimationIsGrowing);
            boolean z3 = (this.currentTextSizeFactor > 0.0f && this.textAnimator == null) || (this.textAnimator != null && this.textAnimationIsGrowing);
            if ((this.currentWidthFactor <= 0.0f || this.widthAnimator != null) && (this.widthAnimator == null || !this.widthAnimationIsGrowing)) {
                z = false;
            }
            if (z2) {
                startDotDisappearAnimation(j);
            }
            if (z3) {
                startTextDisappearAnimation(j);
            }
            if (z) {
                startWidthDisappearAnimation(j2);
            }
        }

        /* access modifiers changed from: package-private */
        public void startAppearAnimation() {
            boolean z = true;
            boolean z2 = !OpPasswordTextViewForPin.this.mShowPassword && (this.dotAnimator == null || !this.dotAnimationIsGrowing);
            boolean z3 = OpPasswordTextViewForPin.this.mShowPassword && (this.textAnimator == null || !this.textAnimationIsGrowing);
            if (this.widthAnimator != null && this.widthAnimationIsGrowing) {
                z = false;
            }
            if (z2) {
                startDotAppearAnimation(0);
            }
            if (z3) {
                startTextAppearAnimation();
            }
            if (z) {
                startWidthAppearAnimation();
            }
            if (OpPasswordTextViewForPin.this.mShowPassword) {
                postDotSwap(250);
            }
        }

        private void postDotSwap(long j) {
            removeDotSwapCallbacks();
            OpPasswordTextViewForPin.this.postDelayed(this.dotSwapperRunnable, j);
            this.isDotSwapPending = true;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void removeDotSwapCallbacks() {
            OpPasswordTextViewForPin.this.removeCallbacks(this.dotSwapperRunnable);
            this.isDotSwapPending = false;
        }

        /* access modifiers changed from: package-private */
        public void swapToDotWhenAppearFinished() {
            removeDotSwapCallbacks();
            ValueAnimator valueAnimator = this.textAnimator;
            if (valueAnimator != null) {
                postDotSwap((valueAnimator.getDuration() - this.textAnimator.getCurrentPlayTime()) + 100);
            } else {
                performSwap();
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void performSwap() {
            startTextDisappearAnimation(0);
            startDotAppearAnimation(30);
        }

        private void startWidthDisappearAnimation(long j) {
            cancelAnimator(this.widthAnimator);
            ValueAnimator ofFloat = ValueAnimator.ofFloat(this.currentWidthFactor, 0.0f);
            this.widthAnimator = ofFloat;
            ofFloat.addUpdateListener(this.widthUpdater);
            this.widthAnimator.addListener(this.widthFinishListener);
            this.widthAnimator.addListener(this.removeEndListener);
            this.widthAnimator.setDuration((long) (this.currentWidthFactor * 160.0f));
            this.widthAnimator.setStartDelay(j);
            this.widthAnimator.start();
            this.widthAnimationIsGrowing = false;
        }

        private void startTextDisappearAnimation(long j) {
            cancelAnimator(this.textAnimator);
            ValueAnimator ofFloat = ValueAnimator.ofFloat(this.currentTextSizeFactor, 0.0f);
            this.textAnimator = ofFloat;
            ofFloat.addUpdateListener(this.textSizeUpdater);
            this.textAnimator.addListener(this.textFinishListener);
            this.textAnimator.setInterpolator(OpPasswordTextViewForPin.this.mDisappearInterpolator);
            this.textAnimator.setDuration((long) (this.currentTextSizeFactor * 160.0f));
            this.textAnimator.setStartDelay(j);
            this.textAnimator.start();
            this.textAnimationIsGrowing = false;
        }

        private void startDotDisappearAnimation(long j) {
            cancelAnimator(this.dotAnimator);
            ValueAnimator ofFloat = ValueAnimator.ofFloat(this.currentDotSizeFactor, 0.0f);
            ofFloat.addUpdateListener(this.dotSizeUpdater);
            ofFloat.addListener(this.dotFinishListener);
            ofFloat.setInterpolator(OpPasswordTextViewForPin.this.mDisappearInterpolator);
            ofFloat.setDuration((long) (Math.min(this.currentDotSizeFactor, 1.0f) * 160.0f));
            ofFloat.setStartDelay(j);
            ofFloat.start();
            this.dotAnimator = ofFloat;
            this.dotAnimationIsGrowing = false;
        }

        private void startWidthAppearAnimation() {
            cancelAnimator(this.widthAnimator);
            ValueAnimator ofFloat = ValueAnimator.ofFloat(this.currentWidthFactor, 1.0f);
            this.widthAnimator = ofFloat;
            ofFloat.addUpdateListener(this.widthUpdater);
            this.widthAnimator.addListener(this.widthFinishListener);
            this.widthAnimator.setDuration((long) ((1.0f - this.currentWidthFactor) * 160.0f));
            this.widthAnimator.start();
            this.widthAnimationIsGrowing = true;
        }

        private void startTextAppearAnimation() {
            cancelAnimator(this.textAnimator);
            ValueAnimator ofFloat = ValueAnimator.ofFloat(this.currentTextSizeFactor, 1.0f);
            this.textAnimator = ofFloat;
            ofFloat.addUpdateListener(this.textSizeUpdater);
            this.textAnimator.addListener(this.textFinishListener);
            this.textAnimator.setInterpolator(OpPasswordTextViewForPin.this.mAppearInterpolator);
            this.textAnimator.setDuration((long) ((1.0f - this.currentTextSizeFactor) * 160.0f));
            this.textAnimator.start();
            this.textAnimationIsGrowing = true;
            if (this.textTranslateAnimator == null) {
                ValueAnimator ofFloat2 = ValueAnimator.ofFloat(1.0f, 0.0f);
                this.textTranslateAnimator = ofFloat2;
                ofFloat2.addUpdateListener(this.textTranslationUpdater);
                this.textTranslateAnimator.addListener(this.textTranslateFinishListener);
                this.textTranslateAnimator.setInterpolator(OpPasswordTextViewForPin.this.mAppearInterpolator);
                this.textTranslateAnimator.setDuration(160L);
                this.textTranslateAnimator.start();
            }
        }

        private void startDotAppearAnimation(long j) {
            cancelAnimator(this.dotAnimator);
            if (!OpPasswordTextViewForPin.this.mShowPassword) {
                ValueAnimator ofFloat = ValueAnimator.ofFloat(this.currentDotSizeFactor, 1.5f);
                ofFloat.addUpdateListener(this.dotSizeUpdater);
                ofFloat.setInterpolator(OpPasswordTextViewForPin.this.mAppearInterpolator);
                ofFloat.setDuration(160L);
                ValueAnimator ofFloat2 = ValueAnimator.ofFloat(1.5f, 1.0f);
                ofFloat2.addUpdateListener(this.dotSizeUpdater);
                ofFloat2.setDuration(160L);
                ofFloat2.addListener(this.dotFinishListener);
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playSequentially(ofFloat, ofFloat2);
                animatorSet.setStartDelay(j);
                animatorSet.start();
                this.dotAnimator = animatorSet;
            } else {
                ValueAnimator ofFloat3 = ValueAnimator.ofFloat(this.currentDotSizeFactor, 1.0f);
                ofFloat3.addUpdateListener(this.dotSizeUpdater);
                ofFloat3.setDuration((long) ((1.0f - this.currentDotSizeFactor) * 160.0f));
                ofFloat3.addListener(this.dotFinishListener);
                ofFloat3.setStartDelay(j);
                ofFloat3.start();
                this.dotAnimator = ofFloat3;
            }
            this.dotAnimationIsGrowing = true;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void cancelAnimator(Animator animator) {
            if (animator != null) {
                animator.cancel();
            }
        }

        public float draw(Canvas canvas, float f, int i, float f2, float f3) {
            boolean z = true;
            boolean z2 = this.currentTextSizeFactor > 0.0f;
            if (this.currentDotSizeFactor <= 0.0f) {
                z = false;
            }
            float f4 = f3 * this.currentWidthFactor;
            if (z2) {
                float f5 = (float) i;
                float f6 = ((f5 / 2.0f) * this.currentTextSizeFactor) + f2 + (f5 * this.currentTextTranslationY * 0.8f);
                canvas.save();
                canvas.translate((f4 / 2.0f) + f, f6);
                float f7 = this.currentTextSizeFactor;
                canvas.scale(f7, f7);
                canvas.drawText(Character.toString(this.whichChar), 0.0f, 0.0f, OpPasswordTextViewForPin.this.mDrawPaint);
                canvas.restore();
            }
            if (z) {
                canvas.save();
                canvas.translate(f + (f4 / 2.0f), f2);
                canvas.drawCircle(0.0f, 0.0f, ((float) (OpPasswordTextViewForPin.this.mDotSize / 2)) * this.currentDotSizeFactor, OpPasswordTextViewForPin.this.mDrawPaint);
                canvas.restore();
            }
            return f4 + (((float) OpPasswordTextViewForPin.this.mCharPadding) * this.currentWidthFactor);
        }
    }

    public void setTextChangeListener(onTextChangedListerner ontextchangedlisterner) {
        this.mOnTextChangeListerner = ontextchangedlisterner;
    }
}
