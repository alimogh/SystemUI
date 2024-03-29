package com.google.android.material.edgeeffect;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.EdgeEffect;
import android.widget.GridView;
import android.widget.ListAdapter;
import com.google.android.material.edgeeffect.SpringRelativeLayout;
public class SpringGridView extends GridView {
    private EdgeEffect mBottomGlow;
    private int mDispatchScrollCounter;
    private SpringRelativeLayout.SEdgeEffectFactory mEdgeEffectFactory;
    AbsListView.OnScrollListener mGivenOnScrollListener;
    boolean mGlowing = false;
    private int mInitialTouchY;
    private int mLastTouchY;
    private float mLastX;
    private float mLastY;
    private float mLastYVel = 0.0f;
    private int mMaxFlingVelocity;
    private int[] mNestedOffsets;
    OnScrollListenerWrapper mOnScrollListenerWrapper = new OnScrollListenerWrapper();
    boolean mOverScrollNested = false;
    float mPullGrowBottom = 0.9f;
    float mPullGrowTop = 0.1f;
    private int[] mScrollOffset;
    private int mScrollPointerId;
    private int mScrollState;
    int[] mScrollStepConsumed;
    SpringRelativeLayout mSpringLayout = null;
    private EdgeEffect mTopGlow;
    private int mTouchSlop;
    private VelocityTracker mVelocityTracker;

    public void onScrolled(int i, int i2) {
    }

    public SpringGridView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    private void init() {
        ViewConfiguration viewConfiguration = ViewConfiguration.get(getContext());
        this.mTouchSlop = viewConfiguration.getScaledTouchSlop();
        this.mMaxFlingVelocity = viewConfiguration.getScaledMaximumFlingVelocity();
        this.mScrollStepConsumed = new int[2];
        this.mScrollOffset = new int[2];
        this.mNestedOffsets = new int[2];
        setOnScrollListener(this.mOnScrollListenerWrapper);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public boolean overScrollBy(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, boolean z) {
        return super.overScrollBy(0, i2, 0, i4, 0, i6, 0, 0, z);
    }

    @Override // android.widget.AbsListView, android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        this.mVelocityTracker.addMovement(motionEvent);
        int actionMasked = motionEvent.getActionMasked();
        int actionIndex = motionEvent.getActionIndex();
        boolean z = false;
        if (actionMasked == 0) {
            int pointerId = motionEvent.getPointerId(0);
            this.mScrollPointerId = pointerId;
            int findPointerIndex = motionEvent.findPointerIndex(pointerId);
            if (findPointerIndex < 0) {
                return false;
            }
            if (!isReadyToOverScroll(!(getLastVisiblePosition() == getAdapter().getCount() - 1), (int) (motionEvent.getX(findPointerIndex) + 0.5f), (int) (motionEvent.getY(findPointerIndex) + 0.5f), 0)) {
                return super.onInterceptTouchEvent(motionEvent);
            }
            int y = (int) (motionEvent.getY() + 0.5f);
            this.mLastTouchY = y;
            this.mInitialTouchY = y;
            if (this.mScrollState == 2) {
                getParent().requestDisallowInterceptTouchEvent(true);
                setScrollState(1);
            }
            int[] iArr = this.mNestedOffsets;
            iArr[1] = 0;
            iArr[0] = 0;
            startNestedScroll(2);
        } else if (actionMasked == 1) {
            this.mVelocityTracker.clear();
            stopNestedScroll();
        } else if (actionMasked == 2) {
            int findPointerIndex2 = motionEvent.findPointerIndex(this.mScrollPointerId);
            if (findPointerIndex2 < 0) {
                return false;
            }
            motionEvent.getX(findPointerIndex2);
            int y2 = (int) (motionEvent.getY(findPointerIndex2) + 0.5f);
            if (this.mScrollState != 1) {
                if (Math.abs(y2 - this.mInitialTouchY) > this.mTouchSlop) {
                    this.mLastTouchY = y2;
                    z = true;
                }
                if (z) {
                    setScrollState(1);
                }
            }
        } else if (actionMasked == 3) {
            cancelTouch();
        } else if (actionMasked == 5) {
            this.mScrollPointerId = motionEvent.getPointerId(actionIndex);
            int y3 = (int) (motionEvent.getY(actionIndex) + 0.5f);
            this.mLastTouchY = y3;
            this.mInitialTouchY = y3;
        } else if (actionMasked == 6) {
            onPointerUp(motionEvent);
        }
        this.mLastX = motionEvent.getX();
        this.mLastY = motionEvent.getY();
        return super.onInterceptTouchEvent(motionEvent);
    }

    @Override // android.widget.AbsListView, android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean z;
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        MotionEvent obtain = MotionEvent.obtain(motionEvent);
        int actionMasked = motionEvent.getActionMasked();
        int actionIndex = motionEvent.getActionIndex();
        boolean z2 = false;
        if (actionMasked == 0) {
            int[] iArr = this.mNestedOffsets;
            iArr[1] = 0;
            iArr[0] = 0;
        }
        int[] iArr2 = this.mNestedOffsets;
        obtain.offsetLocation((float) iArr2[0], (float) iArr2[1]);
        if (actionMasked == 0) {
            this.mScrollPointerId = motionEvent.getPointerId(0);
            int y = (int) (motionEvent.getY() + 0.5f);
            this.mLastTouchY = y;
            this.mInitialTouchY = y;
            int childCount = getChildCount();
            if (childCount > 0) {
                getChildAt(childCount - 1).getBottom();
            }
            startNestedScroll(2);
        } else if (actionMasked == 1) {
            this.mVelocityTracker.addMovement(obtain);
            this.mVelocityTracker.computeCurrentVelocity(1000, (float) this.mMaxFlingVelocity);
            float f = -this.mVelocityTracker.getYVelocity(this.mScrollPointerId);
            if (f == 0.0f) {
                setScrollState(0);
            } else {
                this.mLastYVel = f;
            }
            resetTouch();
            z2 = true;
        } else if (actionMasked == 2) {
            int findPointerIndex = motionEvent.findPointerIndex(this.mScrollPointerId);
            if (findPointerIndex < 0) {
                Log.e("SpringListView", "Error processing scroll; pointer index for id " + this.mScrollPointerId + " not found. Did any MotionEvents get skipped?");
                obtain.recycle();
                return false;
            }
            int x = (int) (motionEvent.getX(findPointerIndex) + 0.5f);
            int y2 = (int) (motionEvent.getY(findPointerIndex) + 0.5f);
            int i = this.mLastTouchY - y2;
            if (this.mScrollState != 1) {
                int abs = Math.abs(i);
                int i2 = this.mTouchSlop;
                if (abs > i2) {
                    i = i > 0 ? i - i2 : i + i2;
                    z = true;
                } else {
                    z = false;
                }
                if (z) {
                    setScrollState(1);
                }
            }
            if (this.mScrollState == 1) {
                this.mLastTouchY = y2 - this.mScrollOffset[1];
                if (scrollByInternal(x, y2, i, obtain)) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
            }
        } else if (actionMasked == 3) {
            cancelTouch();
        } else if (actionMasked == 5) {
            this.mScrollPointerId = motionEvent.getPointerId(actionIndex);
            int y3 = (int) (motionEvent.getY(actionIndex) + 0.5f);
            this.mLastTouchY = y3;
            this.mInitialTouchY = y3;
        } else if (actionMasked == 6) {
            onPointerUp(motionEvent);
        }
        if (!z2) {
            this.mVelocityTracker.addMovement(obtain);
        }
        obtain.recycle();
        this.mLastX = motionEvent.getX();
        this.mLastY = motionEvent.getY();
        return super.onTouchEvent(motionEvent);
    }

    /* access modifiers changed from: package-private */
    public void ensureTopGlow() {
        SpringRelativeLayout.SEdgeEffectFactory sEdgeEffectFactory = this.mEdgeEffectFactory;
        if (sEdgeEffectFactory == null) {
            Log.e("SpringGridView", "setEdgeEffectFactory first, please!");
        } else if (this.mTopGlow == null) {
            this.mTopGlow = sEdgeEffectFactory.createEdgeEffect(this, 1);
            if (getClipToPadding()) {
                this.mTopGlow.setSize((getMeasuredWidth() - getPaddingLeft()) - getPaddingRight(), (getMeasuredHeight() - getPaddingTop()) - getPaddingBottom());
            } else {
                this.mTopGlow.setSize(getMeasuredWidth(), getMeasuredHeight());
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void ensureBottomGlow() {
        SpringRelativeLayout.SEdgeEffectFactory sEdgeEffectFactory = this.mEdgeEffectFactory;
        if (sEdgeEffectFactory == null) {
            Log.e("SpringGridView", "setEdgeEffectFactory first, please!");
        } else if (this.mBottomGlow == null) {
            this.mBottomGlow = sEdgeEffectFactory.createEdgeEffect(this, 3);
            if (getClipToPadding()) {
                this.mBottomGlow.setSize((getMeasuredWidth() - getPaddingLeft()) - getPaddingRight(), (getMeasuredHeight() - getPaddingTop()) - getPaddingBottom());
            } else {
                this.mBottomGlow.setSize(getMeasuredWidth(), getMeasuredHeight());
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0070  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void pullGlows(float r6, float r7, float r8, float r9) {
        /*
            r5 = this;
            int r0 = r5.getHeight()
            float r0 = (float) r0
            int r0 = (r8 > r0 ? 1 : (r8 == r0 ? 0 : -1))
            if (r0 > 0) goto L_0x007b
            r0 = 0
            int r1 = (r8 > r0 ? 1 : (r8 == r0 ? 0 : -1))
            if (r1 >= 0) goto L_0x000f
            goto L_0x007b
        L_0x000f:
            int r1 = r5.getHeight()
            float r1 = (float) r1
            float r8 = r8 / r1
            int r1 = (r9 > r0 ? 1 : (r9 == r0 ? 0 : -1))
            r2 = 1
            if (r1 >= 0) goto L_0x0040
            float r1 = r5.mPullGrowBottom
            int r1 = (r8 > r1 ? 1 : (r8 == r1 ? 0 : -1))
            if (r1 >= 0) goto L_0x0040
            float r1 = r5.mPullGrowTop
            int r1 = (r8 > r1 ? 1 : (r8 == r1 ? 0 : -1))
            if (r1 <= 0) goto L_0x0040
            r5.ensureTopGlow()
            android.widget.EdgeEffect r8 = r5.mTopGlow
            if (r8 == 0) goto L_0x006d
            float r1 = -r9
            int r3 = r5.getHeight()
            float r3 = (float) r3
            float r1 = r1 / r3
            int r3 = r5.getWidth()
            float r3 = (float) r3
            float r6 = r6 / r3
            r8.onPull(r1, r6)
            r5.mGlowing = r2
            goto L_0x006e
        L_0x0040:
            int r1 = (r9 > r0 ? 1 : (r9 == r0 ? 0 : -1))
            if (r1 <= 0) goto L_0x006d
            float r1 = r5.mPullGrowTop
            int r1 = (r8 > r1 ? 1 : (r8 == r1 ? 0 : -1))
            if (r1 <= 0) goto L_0x006d
            float r1 = r5.mPullGrowBottom
            int r8 = (r8 > r1 ? 1 : (r8 == r1 ? 0 : -1))
            if (r8 >= 0) goto L_0x006d
            r5.ensureBottomGlow()
            android.widget.EdgeEffect r8 = r5.mBottomGlow
            if (r8 == 0) goto L_0x006d
            int r1 = r5.getHeight()
            float r1 = (float) r1
            float r1 = r9 / r1
            r3 = 1065353216(0x3f800000, float:1.0)
            int r4 = r5.getWidth()
            float r4 = (float) r4
            float r6 = r6 / r4
            float r3 = r3 - r6
            r8.onPull(r1, r3)
            r5.mGlowing = r2
            goto L_0x006e
        L_0x006d:
            r2 = 0
        L_0x006e:
            if (r2 != 0) goto L_0x0078
            int r6 = (r7 > r0 ? 1 : (r7 == r0 ? 0 : -1))
            if (r6 != 0) goto L_0x0078
            int r6 = (r9 > r0 ? 1 : (r9 == r0 ? 0 : -1))
            if (r6 == 0) goto L_0x007b
        L_0x0078:
            r5.postInvalidateOnAnimation()
        L_0x007b:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.material.edgeeffect.SpringGridView.pullGlows(float, float, float, float):void");
    }

    /* access modifiers changed from: package-private */
    public void setScrollState(int i) {
        if (i != this.mScrollState) {
            this.mScrollState = i;
        }
    }

    private void resetTouch() {
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.clear();
        }
        releaseGlows();
    }

    private void releaseGlows() {
        boolean z;
        EdgeEffect edgeEffect = this.mTopGlow;
        if (edgeEffect != null) {
            edgeEffect.onRelease();
            this.mGlowing = false;
            z = this.mTopGlow.isFinished() | false;
        } else {
            z = false;
        }
        EdgeEffect edgeEffect2 = this.mBottomGlow;
        if (edgeEffect2 != null) {
            edgeEffect2.onRelease();
            this.mGlowing = false;
            z |= this.mBottomGlow.isFinished();
        }
        if (z) {
            postInvalidateOnAnimation();
        }
    }

    private void cancelTouch() {
        resetTouch();
        setScrollState(0);
    }

    private void onPointerUp(MotionEvent motionEvent) {
        int actionIndex = motionEvent.getActionIndex();
        if (motionEvent.getPointerId(actionIndex) == this.mScrollPointerId) {
            int i = actionIndex == 0 ? 1 : 0;
            this.mScrollPointerId = motionEvent.getPointerId(i);
            int y = (int) (motionEvent.getY(i) + 0.5f);
            this.mLastTouchY = y;
            this.mInitialTouchY = y;
        }
    }

    /* access modifiers changed from: package-private */
    public void dispatchOnScrolled(int i, int i2) {
        this.mDispatchScrollCounter++;
        int scrollX = getScrollX();
        int scrollY = getScrollY();
        onScrollChanged(scrollX, scrollY, scrollX, scrollY);
        onScrolled(i, i2);
        this.mDispatchScrollCounter--;
    }

    /* access modifiers changed from: package-private */
    public boolean scrollByInternal(int i, int i2, int i3, MotionEvent motionEvent) {
        int i4;
        int i5;
        int i6;
        int i7;
        if (!isReadyToOverScroll(i3 < 0, i, i2, i3)) {
            return false;
        }
        if (getAdapter() != null) {
            scrollStep(i, i3, this.mScrollStepConsumed);
            int[] iArr = this.mScrollStepConsumed;
            i7 = iArr[0];
            i4 = iArr[1];
            i6 = i - i7;
            i5 = i3 - i4;
        } else {
            i4 = 0;
            i7 = 0;
            i6 = 0;
            i5 = 0;
        }
        invalidate();
        if (getOverScrollMode() != 2) {
            if (motionEvent != null && !motionEvent.isFromSource(8194)) {
                pullGlows(motionEvent.getX(), (float) i6, motionEvent.getY(), (float) i5);
            }
            considerReleasingGlowsOnScroll(i, i3);
        }
        if (!(i7 == 0 && i4 == 0)) {
            dispatchOnScrolled(i7, i4);
        }
        if (!awakenScrollBars()) {
            invalidate();
        }
        return (i7 == 0 && i4 == 0) ? false : true;
    }

    /* access modifiers changed from: package-private */
    public void scrollStep(int i, int i2, int[] iArr) {
        if (iArr != null) {
            iArr[1] = 0;
        }
    }

    private boolean isReadyToOverScroll(boolean z, int i, int i2, int i3) {
        View childAt;
        ListAdapter adapter = getAdapter();
        if (adapter == null || adapter.isEmpty()) {
            return false;
        }
        if (z && getFirstVisiblePosition() == 0) {
            View childAt2 = getChildAt(0);
            if (childAt2 == null || childAt2.getTop() < getListPaddingTop()) {
                return false;
            }
            return true;
        } else if (z || adapter == null || getLastVisiblePosition() != adapter.getCount() - 1 || (childAt = getChildAt(getChildCount() - 1)) == null || childAt.getBottom() > getHeight() - getListPaddingBottom()) {
            return false;
        } else {
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public void considerReleasingGlowsOnScroll(int i, int i2) {
        EdgeEffect edgeEffect = this.mTopGlow;
        boolean z = false;
        if (edgeEffect != null && !edgeEffect.isFinished() && i2 > 0) {
            this.mTopGlow.onRelease();
            z = false | this.mTopGlow.isFinished();
        }
        EdgeEffect edgeEffect2 = this.mBottomGlow;
        if (edgeEffect2 != null && !edgeEffect2.isFinished() && i2 < 0) {
            this.mBottomGlow.onRelease();
            z |= this.mBottomGlow.isFinished();
        }
        if (z) {
            postInvalidateOnAnimation();
        }
    }

    /* access modifiers changed from: package-private */
    public class OnScrollListenerWrapper implements AbsListView.OnScrollListener {
        int state = 0;

        OnScrollListenerWrapper() {
        }

        @Override // android.widget.AbsListView.OnScrollListener
        public void onScrollStateChanged(AbsListView absListView, int i) {
            this.state = i;
            AbsListView.OnScrollListener onScrollListener = SpringGridView.this.mGivenOnScrollListener;
            if (onScrollListener != null) {
                onScrollListener.onScrollStateChanged(absListView, i);
            }
        }

        @Override // android.widget.AbsListView.OnScrollListener
        public void onScroll(AbsListView absListView, int i, int i2, int i3) {
            AbsListView.OnScrollListener onScrollListener = SpringGridView.this.mGivenOnScrollListener;
            if (onScrollListener != null) {
                onScrollListener.onScroll(absListView, i, i2, i3);
            }
            if (this.state == 1) {
                SpringGridView springGridView = SpringGridView.this;
                if (springGridView.mSpringLayout == null) {
                    ViewGroup viewGroup = (ViewGroup) springGridView.getParent();
                    if (viewGroup instanceof SpringRelativeLayout) {
                        SpringGridView.this.mSpringLayout = (SpringRelativeLayout) viewGroup;
                    }
                }
                SpringRelativeLayout springRelativeLayout = SpringGridView.this.mSpringLayout;
                if (springRelativeLayout != null) {
                    springRelativeLayout.onRecyclerViewScrolled();
                }
            }
            boolean z = false;
            boolean z2 = this.state != 2;
            if (this.state != 1) {
                z = true;
            }
            if (!z2 || !z) {
                if (!SpringGridView.this.canScrollVertically(-1)) {
                    SpringGridView springGridView2 = SpringGridView.this;
                    if (!springGridView2.mGlowing) {
                        float f = springGridView2.mLastYVel;
                        if (f >= 0.0f) {
                            f = SpringGridView.this.computeVelocity();
                        }
                        SpringGridView springGridView3 = SpringGridView.this;
                        float f2 = f / 20.0f;
                        springGridView3.pullGlows(springGridView3.mLastX, 0.0f, SpringGridView.this.mLastY, f2);
                        if (SpringGridView.this.mTopGlow != null) {
                            SpringGridView.this.mTopGlow.onAbsorb((int) f2);
                        }
                    }
                }
                if (!SpringGridView.this.canScrollVertically(1)) {
                    SpringGridView springGridView4 = SpringGridView.this;
                    if (!springGridView4.mGlowing) {
                        float f3 = springGridView4.mLastYVel;
                        if (f3 <= 0.0f) {
                            f3 = SpringGridView.this.computeVelocity();
                        }
                        SpringGridView springGridView5 = SpringGridView.this;
                        float f4 = f3 / 20.0f;
                        springGridView5.pullGlows(springGridView5.mLastX, 0.0f, SpringGridView.this.mLastY, f4);
                        if (SpringGridView.this.mBottomGlow != null) {
                            SpringGridView.this.mBottomGlow.onAbsorb((int) f4);
                        }
                    }
                }
            }
        }
    }

    @Override // android.widget.AbsListView
    public void setOnScrollListener(AbsListView.OnScrollListener onScrollListener) {
        if (isUserOnScrollListener(onScrollListener)) {
            this.mGivenOnScrollListener = onScrollListener;
        } else {
            super.setOnScrollListener(onScrollListener);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isUserOnScrollListener(AbsListView.OnScrollListener onScrollListener) {
        return onScrollListener != this.mOnScrollListenerWrapper;
    }

    /* access modifiers changed from: package-private */
    public float computeVelocity() {
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        this.mVelocityTracker.computeCurrentVelocity(1000, (float) this.mMaxFlingVelocity);
        return -this.mVelocityTracker.getYVelocity(this.mScrollPointerId);
    }
}
