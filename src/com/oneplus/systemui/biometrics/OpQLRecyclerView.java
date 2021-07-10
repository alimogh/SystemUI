package com.oneplus.systemui.biometrics;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.ActivityOptions;
import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.android.systemui.Interpolators;
import com.oneplus.config.ConfigObserver;
import com.oneplus.systemui.OpSystemUIInjector;
import com.oneplus.systemui.biometrics.OpQLAdapter;
import com.oneplus.util.OpUtils;
import com.oneplus.util.VibratorSceneUtils;
import java.util.ArrayList;
import org.json.JSONArray;
public class OpQLRecyclerView extends RecyclerView {
    private static final AudioAttributes VIBRATION_ATTRIBUTES = new AudioAttributes.Builder().setContentType(4).setUsage(13).build();
    protected OpQLAdapter mAdapter;
    private boolean mAnimate;
    ArrayList<OpQLAdapter.ActionInfo> mAppMap;
    private int mBarPosition;
    private Runnable mCancelFalseRunnable;
    private Runnable mCheckNextScroll;
    private Context mContext;
    private ValueAnimator mEnterAnimator;
    private AnimationViewData mFocusedViewData;
    private OpQLHelper mHelper;
    private boolean mInitialized;
    private boolean mIsCancel;
    private boolean mIsQLExit;
    private TextView mLabel;
    private long mLastScrollTime;
    QLLayoutManager mLayoutManager;
    private ValueAnimator mLeaveAnimator;
    private int mPadding;
    private int mPosition;
    private ConfigObserver mQuickPayConfigObserver;
    private float mScrollSpeed;
    private Runnable mScrollToPosition;
    private int mVibTime;
    private RecyclerView mView;

    private int checkBarPosition(float f) {
        for (int i = 6; i >= 0; i--) {
            if (f > ((float) i)) {
                return i;
            }
        }
        return 0;
    }

    public OpQLRecyclerView(Context context) {
        this(context, null);
    }

    public OpQLRecyclerView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mAppMap = new ArrayList<>();
        this.mFocusedViewData = new AnimationViewData(this);
        this.mPosition = 0;
        this.mScrollSpeed = 150.0f;
        this.mIsCancel = false;
        this.mInitialized = false;
        this.mBarPosition = 3;
        this.mPadding = 0;
        this.mAnimate = false;
        this.mVibTime = 0;
        this.mLastScrollTime = 0;
        this.mHelper = null;
        this.mIsQLExit = false;
        new ContentObserver(new Handler()) { // from class: com.oneplus.systemui.biometrics.OpQLRecyclerView.1
            @Override // android.database.ContentObserver
            public void onChange(boolean z) {
                OpQLRecyclerView opQLRecyclerView = OpQLRecyclerView.this;
                opQLRecyclerView.mVibTime = Settings.System.getInt(opQLRecyclerView.mContext.getContentResolver(), "ql_vib_time", 20);
                if (OpQLRecyclerView.this.mVibTime < 0) {
                    OpQLRecyclerView.this.mVibTime = 0;
                } else if (OpQLRecyclerView.this.mVibTime > 100) {
                    OpQLRecyclerView.this.mVibTime = 100;
                }
                if (Build.DEBUG_ONEPLUS) {
                    Log.d("QuickLaunch.QLRecyclerView", "ql_vib_time " + OpQLRecyclerView.this.mVibTime);
                }
            }
        };
        this.mCheckNextScroll = new Runnable() { // from class: com.oneplus.systemui.biometrics.OpQLRecyclerView.2
            @Override // java.lang.Runnable
            public void run() {
                if (!OpQLRecyclerView.this.mIsQLExit) {
                    int i = OpQLRecyclerView.this.mPosition;
                    if (OpQLRecyclerView.this.mBarPosition >= 6 && i > 0) {
                        i--;
                    } else if (OpQLRecyclerView.this.mBarPosition == 0 && i < OpQLRecyclerView.this.mAppMap.size() - 1 && (i = i + 1) >= OpQLRecyclerView.this.mAppMap.size()) {
                        i = OpQLRecyclerView.this.mAppMap.size() - 1;
                    }
                    if (i != OpQLRecyclerView.this.mPosition) {
                        OpQLRecyclerView.this.mPosition = i;
                        OpQLRecyclerView.this.lambda$new$1();
                        OpQLRecyclerView opQLRecyclerView = OpQLRecyclerView.this;
                        opQLRecyclerView.postDelayed(opQLRecyclerView.mCheckNextScroll, 500);
                    }
                }
            }
        };
        this.mScrollToPosition = new Runnable() { // from class: com.oneplus.systemui.biometrics.-$$Lambda$OpQLRecyclerView$wbTw-T-uK67VMzbDKJUkv0L2hdc
            @Override // java.lang.Runnable
            public final void run() {
                OpQLRecyclerView.this.lambda$new$1$OpQLRecyclerView();
            }
        };
        this.mCancelFalseRunnable = new Runnable() { // from class: com.oneplus.systemui.biometrics.-$$Lambda$OpQLRecyclerView$j9NxWBPkTr_qjJfr1ooqs-rzI14
            @Override // java.lang.Runnable
            public final void run() {
                OpQLRecyclerView.this.lambda$new$2$OpQLRecyclerView();
            }
        };
        this.mContext = context;
        QLLayoutManager qLLayoutManager = new QLLayoutManager(context, 0, false);
        this.mLayoutManager = qLLayoutManager;
        setLayoutManager(qLLayoutManager);
        OpQLHelper opQLHelper = new OpQLHelper(context);
        this.mHelper = opQLHelper;
        this.mAppMap = opQLHelper.getQLApps();
        setHasFixedSize(true);
        setItemViewCacheSize(6);
        setDrawingCacheEnabled(true);
        setDrawingCacheQuality(1048576);
        setNestedScrollingEnabled(false);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        this.mView = this;
        this.mPadding = getPaddingRight();
        postDelayed(new Runnable() { // from class: com.oneplus.systemui.biometrics.-$$Lambda$OpQLRecyclerView$Ini__TyRJAsREWpcbiNMmgGeNcg
            @Override // java.lang.Runnable
            public final void run() {
                OpQLRecyclerView.this.lambda$onFinishInflate$0$OpQLRecyclerView();
            }
        }, 200);
        ConfigObserver configObserver = new ConfigObserver(this.mContext, getHandler(), new QuickPayConfigUpdater(), "QuickPay_APPS_Config");
        this.mQuickPayConfigObserver = configObserver;
        configObserver.register();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onFinishInflate$0 */
    public /* synthetic */ void lambda$onFinishInflate$0$OpQLRecyclerView() {
        View findViewByPosition = this.mLayoutManager.findViewByPosition(this.mPosition);
        if (findViewByPosition != null) {
            OrientationHelper createHorizontalHelper = OrientationHelper.createHorizontalHelper(this.mLayoutManager);
            int end = (createHorizontalHelper.getEnd() / 2) - (createHorizontalHelper.getDecoratedStart(findViewByPosition) + (createHorizontalHelper.getDecoratedMeasurement(findViewByPosition) / 2));
            if (end != 0) {
                this.mLayoutManager.scrollToPositionWithOffset(this.mPosition, end);
            }
            findViewByPosition.animate().scaleX(1.0f).scaleY(1.0f).alpha(1.0f).setDuration(100).start();
        }
        this.mInitialized = true;
    }

    public void onQLExit() {
        this.mIsQLExit = true;
        removeCallbacks(this.mScrollToPosition);
        removeCallbacks(this.mCheckNextScroll);
        this.mQuickPayConfigObserver.unregister();
        this.mQuickPayConfigObserver = null;
        setAdapter(null);
        OpQLAdapter opQLAdapter = this.mAdapter;
        if (opQLAdapter != null) {
            opQLAdapter.onQLExit();
            this.mAdapter = null;
        }
        this.mView = null;
        this.mAppMap = null;
        this.mHelper = null;
        this.mContext = null;
        this.mFocusedViewData.view = null;
        this.mFocusedViewData = null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* renamed from: scrollToPosition */
    public void lambda$new$1() {
        this.mLayoutManager.smoothScrollToPosition(this, new RecyclerView.State(), this.mPosition);
    }

    public synchronized void onScrollProgress(float f) {
        if (this.mInitialized) {
            int checkBarPosition = checkBarPosition(f);
            if (isLayoutRtl()) {
                checkBarPosition = 7 - checkBarPosition;
            }
            if (checkBarPosition != this.mBarPosition) {
                int i = checkBarPosition - this.mBarPosition;
                this.mBarPosition = checkBarPosition;
                if (!this.mIsCancel) {
                    int i2 = this.mPosition - i;
                    if (i2 < 0) {
                        i2 = 0;
                    } else if (i2 >= this.mAppMap.size()) {
                        i2 = this.mAppMap.size() - 1;
                    }
                    if (Build.DEBUG_ONEPLUS) {
                        Log.d("QuickLaunch.QLRecyclerView", "onScrollProgress mBarPosition " + this.mBarPosition + " mPosition " + this.mPosition + " position " + i2);
                    }
                    removeCallbacks(this.mCheckNextScroll);
                    if (this.mPosition != i2) {
                        this.mPosition = i2;
                        long currentTimeMillis = System.currentTimeMillis() - this.mLastScrollTime;
                        if (currentTimeMillis < 50) {
                            removeCallbacks(this.mScrollToPosition);
                            postDelayed(this.mScrollToPosition, 50 - currentTimeMillis);
                        } else {
                            lambda$new$1();
                        }
                        this.mLastScrollTime = System.currentTimeMillis();
                    }
                    postDelayed(this.mCheckNextScroll, 500);
                }
            }
        }
    }

    public void launch() {
        View findViewByPosition;
        int i;
        int i2;
        removeCallbacks(this.mCheckNextScroll);
        if (!this.mIsCancel && (findViewByPosition = this.mLayoutManager.findViewByPosition(this.mPosition)) != null) {
            OpQLAdapter.ActionInfo actionInfo = (OpQLAdapter.ActionInfo) findViewByPosition.getTag();
            OpSystemUIInjector.addAppLockerPassedPackage(actionInfo.mPackageName + "0");
            int i3 = 0;
            if (actionInfo.mActionName.equals("OpenApp")) {
                int measuredWidth = findViewByPosition.getMeasuredWidth();
                int measuredHeight = findViewByPosition.getMeasuredHeight();
                Drawable drawable = ((ImageView) findViewByPosition).getDrawable();
                if (drawable != null) {
                    Rect bounds = drawable.getBounds();
                    i2 = findViewByPosition.getPaddingTop();
                    int width = bounds.width();
                    int height = bounds.height();
                    i3 = (measuredWidth - bounds.width()) / 2;
                    measuredWidth = width;
                    i = height;
                } else {
                    i = measuredHeight;
                    i2 = 0;
                }
                this.mHelper.startApp(actionInfo.mPackageName, ActivityOptions.makeClipRevealAnimation(findViewByPosition, i3, i2, measuredWidth, i), actionInfo.mUid);
            } else if (actionInfo.mActionName.equals("OpenShortcut")) {
                this.mHelper.startShortcut(actionInfo.mPackageName, actionInfo.mShortcutId, actionInfo.mUid, false);
            } else if (actionInfo.mActionName.equals("OpenQuickPay")) {
                this.mHelper.startQuickPay(actionInfo.mPaymentWhich, actionInfo.mUid);
            } else if (actionInfo.mActionName.equals("OpenWxMiniProgram")) {
                this.mHelper.startWxMiniProgram(actionInfo.mWxMiniProgramWhich);
            }
        }
    }

    public void setLabelView(TextView textView) {
        this.mLabel = textView;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateViewScale() {
        float width = ((float) getWidth()) / 2.0f;
        float f = 0.25f * width;
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            float min = ((-0.32999998f * (Math.min(f, Math.abs(width - (((float) (this.mLayoutManager.getDecoratedRight(childAt) + this.mLayoutManager.getDecoratedLeft(childAt))) / 2.0f))) - 0.0f)) / (f - 0.0f)) + 1.0f;
            if (Build.DEBUG_ONEPLUS) {
                Log.d("QuickLaunch.QLRecyclerView", "updateViewScale mIsCancel " + this.mIsCancel);
            }
            if (!this.mIsCancel) {
                childAt.setScaleX(min);
                childAt.setScaleY(min);
                if (min > 0.9f) {
                    childAt.setAlpha(1.0f);
                } else {
                    childAt.setAlpha(0.5f);
                }
            } else {
                childAt.animate().scaleX(0.67f).scaleY(0.67f).alpha(0.5f).start();
            }
        }
    }

    public void onVelocityChanged(float f) {
        if (f <= 250.0f) {
            f = 250.0f;
        }
        float f2 = (250.0f / f) * 250.0f * 1.2f;
        this.mScrollSpeed = f2;
        if (f2 < 50.0f) {
            this.mScrollSpeed = 50.0f;
        } else if (f2 > 470.0f) {
            this.mScrollSpeed = 470.0f;
        }
    }

    public class QLLayoutManager extends LinearLayoutManager {
        public QLLayoutManager(Context context, int i, boolean z) {
            super(context, i, z);
        }

        @Override // androidx.recyclerview.widget.LinearLayoutManager, androidx.recyclerview.widget.RecyclerView.LayoutManager
        public int scrollHorizontallyBy(int i, RecyclerView.Recycler recycler, RecyclerView.State state) {
            int scrollHorizontallyBy = super.scrollHorizontallyBy(i, recycler, state);
            OpQLRecyclerView.this.updateViewScale();
            OpQLRecyclerView.this.updateLabel();
            return scrollHorizontallyBy;
        }

        @Override // androidx.recyclerview.widget.LinearLayoutManager, androidx.recyclerview.widget.RecyclerView.LayoutManager
        public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int i) {
            OpQLRecyclerView.this.stopScroll();
            if (!OpQLRecyclerView.this.mIsQLExit) {
                AnonymousClass1 r2 = new LinearSmoothScroller(recyclerView.getContext()) { // from class: com.oneplus.systemui.biometrics.OpQLRecyclerView.QLLayoutManager.1
                    /* access modifiers changed from: protected */
                    @Override // androidx.recyclerview.widget.LinearSmoothScroller
                    public float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                        return OpQLRecyclerView.this.mScrollSpeed / ((float) displayMetrics.densityDpi);
                    }

                    /* access modifiers changed from: protected */
                    @Override // androidx.recyclerview.widget.LinearSmoothScroller, androidx.recyclerview.widget.RecyclerView.SmoothScroller
                    public void onTargetFound(View view, RecyclerView.State state2, RecyclerView.SmoothScroller.Action action) {
                        int calculateDistanceToCenter = calculateDistanceToCenter(view, OrientationHelper.createHorizontalHelper(OpQLRecyclerView.this.mLayoutManager));
                        int calculateTimeForDeceleration = calculateTimeForDeceleration(calculateDistanceToCenter);
                        if (calculateTimeForDeceleration > 0) {
                            action.update(calculateDistanceToCenter, 0, calculateTimeForDeceleration, this.mDecelerateInterpolator);
                        }
                    }

                    private int calculateDistanceToCenter(View view, OrientationHelper orientationHelper) {
                        return (orientationHelper.getDecoratedStart(view) + (orientationHelper.getDecoratedMeasurement(view) / 2)) - (orientationHelper.getEnd() / 2);
                    }
                };
                r2.setTargetPosition(i);
                startSmoothScroll(r2);
                OpQLRecyclerView.this.vibrate();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void vibrate() {
        Vibrator vibrator = (Vibrator) this.mContext.getSystemService("vibrator");
        int i = this.mVibTime;
        VibrationEffect createOneShot = VibrationEffect.createOneShot(i != 0 ? (long) i : 20, -1);
        if (OpUtils.isSupportLinearVibration()) {
            VibratorSceneUtils.doVibrateWithSceneIfNeeded(this.mContext, vibrator, 1011);
        } else if (OpUtils.isSupportZVibrationMotor()) {
            vibrator.vibrate(VibrationEffect.get(0), VIBRATION_ATTRIBUTES);
        } else {
            vibrator.vibrate(createOneShot, VIBRATION_ATTRIBUTES);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateLabel() {
        String str = this.mAppMap.get(this.mPosition).mLabel;
        TextView textView = this.mLabel;
        if (textView != null && str != null) {
            textView.setText(str);
        }
    }

    public int getItemCount() {
        return this.mAppMap.size();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void cancelAnimation(float f, boolean z) {
        float iconPadding = ((float) this.mAdapter.getIconPadding()) - (((float) (this.mAdapter.getIconPadding() - 10)) * (1.0f - f));
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) childAt.getLayoutParams();
            int i2 = (int) iconPadding;
            marginLayoutParams.leftMargin = i2;
            marginLayoutParams.rightMargin = i2;
            childAt.setLayoutParams(marginLayoutParams);
            if (childAt.equals(this.mLayoutManager.findViewByPosition(this.mPosition))) {
                float f2 = f * 0.5f;
                float f3 = f2 + 1.0f;
                childAt.setScaleX(f3);
                childAt.setScaleY(f3);
                childAt.setAlpha(f2 + 0.5f);
            }
        }
        if (!z) {
            AnimationViewData animationViewData = this.mFocusedViewData;
            float f4 = animationViewData.xAfter;
            this.mLayoutManager.scrollHorizontallyBy((int) (animationViewData.view.getX() - (f4 + ((animationViewData.xBefore - f4) * f))), new RecyclerView.Recycler(), new RecyclerView.State());
        }
    }

    public synchronized void onEnterCancelView() {
        if (Build.DEBUG_ONEPLUS) {
            Log.d("QuickLaunch.QLRecyclerView", "onEnterCancelView");
        }
        removeCallbacks(this.mCancelFalseRunnable);
        this.mIsCancel = true;
        removeCallbacks(this.mCheckNextScroll);
        if (this.mAnimate) {
            this.mFocusedViewData.view = this.mLayoutManager.findViewByPosition(this.mPosition);
            this.mFocusedViewData.xBefore = this.mFocusedViewData.view.getX();
            if (this.mLeaveAnimator != null && this.mLeaveAnimator.isRunning()) {
                this.mLeaveAnimator.end();
            }
            if (this.mEnterAnimator != null) {
                this.mEnterAnimator.cancel();
            }
            ValueAnimator ofInt = ValueAnimator.ofInt(this.mPadding, 0);
            this.mEnterAnimator = ofInt;
            ofInt.setInterpolator(Interpolators.ACCELERATE);
            this.mEnterAnimator.setDuration(300L);
            this.mEnterAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.systemui.biometrics.OpQLRecyclerView.3
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    OpQLRecyclerView.this.mView.setPadding(((Integer) valueAnimator.getAnimatedValue()).intValue(), 40, ((Integer) valueAnimator.getAnimatedValue()).intValue(), 40);
                    OpQLRecyclerView.this.cancelAnimation(((float) ((Integer) valueAnimator.getAnimatedValue()).intValue()) / ((float) OpQLRecyclerView.this.mPadding), true);
                }
            });
            this.mEnterAnimator.addListener(new AnimatorListenerAdapter() { // from class: com.oneplus.systemui.biometrics.OpQLRecyclerView.4
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    if (Build.DEBUG_ONEPLUS) {
                        Log.d("QuickLaunch.QLRecyclerView", "mEnterAnimator end");
                    }
                    OpQLRecyclerView.this.cancelAnimation(0.0f, true);
                    synchronized (OpQLRecyclerView.this) {
                        OpQLRecyclerView.this.mEnterAnimator = null;
                    }
                }
            });
            this.mEnterAnimator.start();
        } else {
            for (int i = 0; i < getChildCount(); i++) {
                View childAt = getChildAt(i);
                if (childAt.getScaleX() > 0.67f) {
                    childAt.animate().scaleX(0.67f).scaleY(0.67f).alpha(0.5f).setDuration(100).start();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$2 */
    public /* synthetic */ void lambda$new$2$OpQLRecyclerView() {
        if (Build.DEBUG_ONEPLUS) {
            Log.d("QuickLaunch.QLRecyclerView", "mCancelFalseRunnable");
        }
        this.mIsCancel = false;
    }

    public synchronized void onLeaveCancelView() {
        if (Build.DEBUG_ONEPLUS) {
            Log.d("QuickLaunch.QLRecyclerView", "onLeaveCancelView");
        }
        setOnScrollListener(null);
        if (this.mAnimate) {
            this.mFocusedViewData.xAfter = this.mFocusedViewData.view.getX();
            if (this.mEnterAnimator != null && this.mEnterAnimator.isRunning()) {
                this.mEnterAnimator.end();
            }
            if (this.mLeaveAnimator != null) {
                this.mLeaveAnimator.cancel();
            }
            ValueAnimator ofInt = ValueAnimator.ofInt(0, this.mPadding);
            this.mLeaveAnimator = ofInt;
            ofInt.setInterpolator(Interpolators.ACCELERATE);
            this.mLeaveAnimator.setDuration(300L);
            this.mLeaveAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.systemui.biometrics.OpQLRecyclerView.5
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float intValue = ((float) ((Integer) valueAnimator.getAnimatedValue()).intValue()) / ((float) OpQLRecyclerView.this.mPadding);
                    OpQLRecyclerView.this.mView.setPadding(((Integer) valueAnimator.getAnimatedValue()).intValue(), 40, ((Integer) valueAnimator.getAnimatedValue()).intValue(), 40);
                    OpQLRecyclerView.this.cancelAnimation(intValue, false);
                }
            });
            this.mLeaveAnimator.addListener(new AnimatorListenerAdapter() { // from class: com.oneplus.systemui.biometrics.OpQLRecyclerView.6
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    if (Build.DEBUG_ONEPLUS) {
                        Log.d("QuickLaunch.QLRecyclerView", "mLeaveAnimator end");
                    }
                    OpQLRecyclerView.this.mIsCancel = false;
                    OpQLRecyclerView.this.cancelAnimation(1.0f, false);
                    synchronized (OpQLRecyclerView.this) {
                        OpQLRecyclerView.this.mLeaveAnimator = null;
                    }
                    OpQLRecyclerView.this.updateLabel();
                }
            });
            this.mLeaveAnimator.start();
        } else {
            this.mLayoutManager.findViewByPosition(this.mPosition).animate().scaleX(1.0f).scaleY(1.0f).alpha(1.0f).setDuration(100).start();
            postDelayed(this.mCancelFalseRunnable, 100);
            updateLabel();
        }
        postDelayed(this.mCheckNextScroll, 500);
    }

    /* access modifiers changed from: package-private */
    public class AnimationViewData {
        View view;
        float xAfter;
        float xBefore;

        AnimationViewData(OpQLRecyclerView opQLRecyclerView) {
        }
    }

    public void setQLConfig(String str) {
        if (str != null && !str.equals("")) {
            this.mHelper.parseQLConfig(str);
            OpQLAdapter opQLAdapter = new OpQLAdapter(this.mContext, this.mAppMap);
            this.mAdapter = opQLAdapter;
            setAdapter(opQLAdapter);
            if (this.mAdapter.getItemCount() > 0) {
                int itemCount = this.mAdapter.getItemCount() - 1;
                this.mPosition = itemCount;
                this.mLayoutManager.scrollToPosition(itemCount);
                updateLabel();
            }
        }
    }

    class QuickPayConfigUpdater implements ConfigObserver.ConfigUpdater {
        QuickPayConfigUpdater() {
        }

        public void updateConfig(JSONArray jSONArray) {
            OpQLRecyclerView.this.mHelper.resolveQuickPayConfigFromJSON(jSONArray);
        }
    }
}
