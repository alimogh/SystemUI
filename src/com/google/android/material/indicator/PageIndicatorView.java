package com.google.android.material.indicator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import androidx.core.text.TextUtilsCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.R$color;
import com.google.android.material.R$dimen;
import com.google.android.material.indicator.IndicatorManager;
import com.google.android.material.indicator.animation.type.AnimationType;
import com.google.android.material.indicator.draw.data.Indicator;
import com.google.android.material.indicator.draw.data.Orientation;
import com.google.android.material.indicator.draw.data.PositionSavedState;
import com.google.android.material.indicator.draw.data.RtlMode;
import com.google.android.material.indicator.utils.CoordinatesUtils;
public class PageIndicatorView extends View implements ViewPager.OnPageChangeListener, IndicatorManager.Listener, ViewPager.OnAdapterChangeListener, View.OnTouchListener {
    private static final Handler HANDLER = new Handler(Looper.getMainLooper());
    private Runnable idleRunnable = new Runnable() { // from class: com.google.android.material.indicator.PageIndicatorView.2
        @Override // java.lang.Runnable
        public void run() {
            PageIndicatorView.this.manager.indicator().setIdle(true);
            PageIndicatorView.this.hideWithAnimation();
        }
    };
    private boolean isInteractionEnabled;
    private IndicatorManager manager;
    private DataSetObserver setObserver;
    private ViewPager viewPager;

    public PageIndicatorView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDetachedFromWindow() {
        unRegisterSetObserver();
        super.onDetachedFromWindow();
    }

    @Override // android.view.View
    public Parcelable onSaveInstanceState() {
        Indicator indicator = this.manager.indicator();
        PositionSavedState positionSavedState = new PositionSavedState(super.onSaveInstanceState());
        positionSavedState.setSelectedPosition(indicator.getSelectedPosition());
        positionSavedState.setSelectingPosition(indicator.getSelectingPosition());
        positionSavedState.setLastSelectedPosition(indicator.getLastSelectedPosition());
        return positionSavedState;
    }

    @Override // android.view.View
    public void onRestoreInstanceState(Parcelable parcelable) {
        if (parcelable instanceof PositionSavedState) {
            Indicator indicator = this.manager.indicator();
            PositionSavedState positionSavedState = (PositionSavedState) parcelable;
            indicator.setSelectedPosition(positionSavedState.getSelectedPosition());
            indicator.setSelectingPosition(positionSavedState.getSelectingPosition());
            indicator.setLastSelectedPosition(positionSavedState.getLastSelectedPosition());
            super.onRestoreInstanceState(positionSavedState.getSuperState());
            return;
        }
        super.onRestoreInstanceState(parcelable);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onMeasure(int i, int i2) {
        Pair<Integer, Integer> measureViewSize = this.manager.drawer().measureViewSize(i, i2);
        setMeasuredDimension(((Integer) measureViewSize.first).intValue(), ((Integer) measureViewSize.second).intValue());
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        this.manager.drawer().draw(canvas);
    }

    @Override // android.view.View
    @SuppressLint({"ClickableViewAccessibility"})
    public boolean onTouchEvent(MotionEvent motionEvent) {
        this.manager.drawer().touch(motionEvent);
        return true;
    }

    @Override // android.view.View.OnTouchListener
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (!this.manager.indicator().isFadeOnIdle()) {
            return false;
        }
        int action = motionEvent.getAction();
        if (action == 0) {
            stopIdleRunnable();
        } else if (action == 1) {
            startIdleRunnable();
        }
        return false;
    }

    @Override // com.google.android.material.indicator.IndicatorManager.Listener
    public void onIndicatorUpdated() {
        invalidate();
    }

    @Override // androidx.viewpager.widget.ViewPager.OnPageChangeListener
    public void onPageScrolled(int i, float f, int i2) {
        onPageScroll(i, f);
    }

    @Override // androidx.viewpager.widget.ViewPager.OnPageChangeListener
    public void onPageSelected(int i) {
        onPageSelect(i);
    }

    @Override // androidx.viewpager.widget.ViewPager.OnPageChangeListener
    public void onPageScrollStateChanged(int i) {
        if (i == 0) {
            this.manager.indicator().setInteractiveAnimation(this.isInteractionEnabled);
        }
    }

    @Override // androidx.viewpager.widget.ViewPager.OnAdapterChangeListener
    public void onAdapterChanged(ViewPager viewPager, PagerAdapter pagerAdapter, PagerAdapter pagerAdapter2) {
        DataSetObserver dataSetObserver;
        if (this.manager.indicator().isDynamicCount()) {
            if (!(pagerAdapter == null || (dataSetObserver = this.setObserver) == null)) {
                pagerAdapter.unregisterDataSetObserver(dataSetObserver);
                this.setObserver = null;
            }
            registerSetObserver();
        }
        updateState();
    }

    public void setSelection(int i) {
        Indicator indicator = this.manager.indicator();
        int adjustPosition = adjustPosition(i);
        if (adjustPosition != indicator.getSelectedPosition() && adjustPosition != indicator.getSelectingPosition()) {
            indicator.setInteractiveAnimation(false);
            indicator.setLastSelectedPosition(indicator.getSelectedPosition());
            indicator.setSelectingPosition(adjustPosition);
            indicator.setSelectedPosition(adjustPosition);
            this.manager.animate().basic();
        }
    }

    public void setProgress(int i, float f) {
        Indicator indicator = this.manager.indicator();
        if (indicator.isInteractiveAnimation()) {
            int count = indicator.getCount();
            if (count <= 0 || i < 0) {
                i = 0;
            } else {
                int i2 = count - 1;
                if (i > i2) {
                    i = i2;
                }
            }
            if (f < 0.0f) {
                f = 0.0f;
            } else if (f > 1.0f) {
                f = 1.0f;
            }
            if (f == 1.0f) {
                indicator.setLastSelectedPosition(indicator.getSelectedPosition());
                indicator.setSelectedPosition(i);
            }
            indicator.setSelectingPosition(i);
            this.manager.animate().interactive(f);
        }
    }

    private void init(Context context, AttributeSet attributeSet) {
        initIndicatorManager(context, attributeSet);
        if (this.manager.indicator().isFadeOnIdle()) {
            startIdleRunnable();
        }
    }

    private void initIndicatorManager(Context context, AttributeSet attributeSet) {
        IndicatorManager indicatorManager = new IndicatorManager(this);
        this.manager = indicatorManager;
        Indicator indicator = indicatorManager.indicator();
        indicator.setPaddingLeft(getPaddingLeft());
        indicator.setPaddingTop(getPaddingTop());
        indicator.setPaddingRight(getPaddingRight());
        indicator.setPaddingBottom(getPaddingBottom());
        indicator.setInteractiveAnimation(false);
        indicator.setAutoVisibility(true);
        indicator.setDynamicCount(false);
        indicator.setAnimationType(AnimationType.WORM);
        indicator.setRtlMode(RtlMode.Auto);
        indicator.setFadeOnIdle(false);
        indicator.setIdleDuration(3000);
        indicator.setRadius(context.getResources().getDimensionPixelOffset(R$dimen.qs_page_indicator_height) / 2);
        indicator.setOrientation(Orientation.HORIZONTAL);
        indicator.setPadding(context.getResources().getDimensionPixelOffset(R$dimen.op_control_margin_space2));
        indicator.setAnimationDuration(125);
        indicator.setSelectedColor(context.getColor(R$color.op_control_icon_color_active_default));
        indicator.setUnselectedColor(context.getColor(R$color.op_control_icon_color_disable_default));
        this.isInteractionEnabled = indicator.isInteractiveAnimation();
    }

    private void registerSetObserver() {
        ViewPager viewPager;
        if (this.setObserver == null && (viewPager = this.viewPager) != null && viewPager.getAdapter() != null) {
            this.setObserver = new DataSetObserver() { // from class: com.google.android.material.indicator.PageIndicatorView.1
                @Override // android.database.DataSetObserver
                public void onChanged() {
                    PageIndicatorView.this.updateState();
                }
            };
            try {
                this.viewPager.getAdapter().registerDataSetObserver(this.setObserver);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    private void unRegisterSetObserver() {
        ViewPager viewPager;
        if (this.setObserver != null && (viewPager = this.viewPager) != null && viewPager.getAdapter() != null) {
            try {
                this.viewPager.getAdapter().unregisterDataSetObserver(this.setObserver);
                this.setObserver = null;
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateState() {
        ViewPager viewPager = this.viewPager;
        if (viewPager != null && viewPager.getAdapter() != null) {
            int count = this.viewPager.getAdapter().getCount();
            int currentItem = isRtl() ? (count - 1) - this.viewPager.getCurrentItem() : this.viewPager.getCurrentItem();
            this.manager.indicator().setSelectedPosition(currentItem);
            this.manager.indicator().setSelectingPosition(currentItem);
            this.manager.indicator().setLastSelectedPosition(currentItem);
            this.manager.indicator().setCount(count);
            this.manager.animate().end();
            updateVisibility();
            requestLayout();
        }
    }

    private void updateVisibility() {
        if (this.manager.indicator().isAutoVisibility()) {
            int count = this.manager.indicator().getCount();
            int visibility = getVisibility();
            if (visibility != 0 && count > 1) {
                setVisibility(0);
            } else if (visibility != 4 && count <= 1) {
                setVisibility(4);
            }
        }
    }

    private void onPageSelect(int i) {
        Indicator indicator = this.manager.indicator();
        boolean isViewMeasured = isViewMeasured();
        int count = indicator.getCount();
        if (isViewMeasured) {
            if (isRtl()) {
                i = (count - 1) - i;
            }
            setSelection(i);
        }
    }

    private void onPageScroll(int i, float f) {
        Indicator indicator = this.manager.indicator();
        if (isViewMeasured() && indicator.isInteractiveAnimation() && indicator.getAnimationType() != AnimationType.NONE) {
            Pair<Integer, Float> progress = CoordinatesUtils.getProgress(indicator, i, f, isRtl());
            setProgress(((Integer) progress.first).intValue(), ((Float) progress.second).floatValue());
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.google.android.material.indicator.PageIndicatorView$3  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass3 {
        static final /* synthetic */ int[] $SwitchMap$com$google$android$material$indicator$draw$data$RtlMode;

        static {
            int[] iArr = new int[RtlMode.values().length];
            $SwitchMap$com$google$android$material$indicator$draw$data$RtlMode = iArr;
            try {
                iArr[RtlMode.On.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$google$android$material$indicator$draw$data$RtlMode[RtlMode.Off.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$google$android$material$indicator$draw$data$RtlMode[RtlMode.Auto.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }

    private boolean isRtl() {
        int i = AnonymousClass3.$SwitchMap$com$google$android$material$indicator$draw$data$RtlMode[this.manager.indicator().getRtlMode().ordinal()];
        if (i == 1) {
            return true;
        }
        if (i != 3) {
            return false;
        }
        return TextUtilsCompat.getLayoutDirectionFromLocale(getContext().getResources().getConfiguration().locale) == 1;
    }

    private boolean isViewMeasured() {
        return (getMeasuredHeight() == 0 && getMeasuredWidth() == 0) ? false : true;
    }

    private int adjustPosition(int i) {
        int count = this.manager.indicator().getCount() - 1;
        if (i < 0) {
            return 0;
        }
        return i > count ? count : i;
    }

    private void displayWithAnimation() {
        animate().cancel();
        animate().alpha(1.0f).setDuration(250);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void hideWithAnimation() {
        animate().cancel();
        animate().alpha(0.0f).setDuration(250);
    }

    private void startIdleRunnable() {
        HANDLER.removeCallbacks(this.idleRunnable);
        HANDLER.postDelayed(this.idleRunnable, this.manager.indicator().getIdleDuration());
    }

    private void stopIdleRunnable() {
        HANDLER.removeCallbacks(this.idleRunnable);
        displayWithAnimation();
    }
}
