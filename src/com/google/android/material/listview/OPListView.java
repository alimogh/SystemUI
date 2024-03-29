package com.google.android.material.listview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ListView;
import com.google.android.material.R$attr;
import com.google.android.material.R$dimen;
import com.google.android.material.R$styleable;
import java.util.ArrayList;
public class OPListView extends ListView {
    private boolean mAnimRunning;
    private ArrayList<ObjectAnimator> mAnimatorList;
    ValueAnimator.AnimatorUpdateListener mAnimatorUpdateListener;
    private DecelerateInterpolator mDecelerateInterpolator;
    AnimatorSet mDelAniSet;
    private boolean mDelAnimationFlag;
    private ArrayList<Integer> mDelOriViewTopList;
    private ArrayList<Integer> mDelPosList;
    private ArrayList<View> mDelViewList;
    private DeleteAnimationListener mDeleteAnimationListener;
    private boolean mDisableTouchEvent;
    private Drawable mDivider;
    private IDividerController mDividerController;
    private int mDividerHeight;
    private boolean mFooterDividersEnabled;
    private boolean mHeaderDividersEnabled;
    private boolean mInDeleteAnimation;
    private boolean mIsClipToPadding;
    private ArrayList<View> mNowViewList;
    private int mOriBelowLeftCount;
    private int mOriCurDeleteCount;
    private int mOriCurLeftCount;
    private boolean mOriLastPage;
    private int mOriUpperDeleteCount;
    Rect mTempRect;

    public interface DeleteAnimationListener {
        void onAnimationEnd();
    }

    public OPListView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 16842868);
    }

    public OPListView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public OPListView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mDividerHeight = 1;
        this.mDelViewList = null;
        this.mDelPosList = null;
        this.mNowViewList = null;
        this.mDelOriViewTopList = null;
        this.mDelAniSet = null;
        this.mDecelerateInterpolator = new DecelerateInterpolator(1.2f);
        this.mAnimatorList = new ArrayList<>();
        this.mTempRect = new Rect();
        this.mHeaderDividersEnabled = true;
        this.mFooterDividersEnabled = true;
        this.mIsClipToPadding = true;
        this.mDividerController = null;
        this.mAnimatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() { // from class: com.google.android.material.listview.OPListView.1
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                OPListView.this.invalidate();
            }
        };
        init(context, attributeSet, i, i2);
    }

    private void init(Context context, AttributeSet attributeSet, int i, int i2) {
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.OPListView, R$attr.OPListViewStyle, 0);
        Drawable drawable = obtainStyledAttributes.getDrawable(R$styleable.OPListView_android_divider);
        Drawable drawable2 = obtainStyledAttributes.getDrawable(R$styleable.OPListView_android_background);
        if (drawable != null) {
            setDivider(drawable);
        }
        if (drawable2 != null) {
            setBackground(drawable2);
        }
        this.mDividerHeight = getResources().getDimensionPixelSize(R$dimen.listview_divider_height);
        setOverScrollMode(0);
        super.setDivider(null);
        setDividerHeight(this.mDividerHeight);
        setFooterDividersEnabled(false);
        obtainStyledAttributes.recycle();
    }

    @Override // android.widget.ListView
    public void setHeaderDividersEnabled(boolean z) {
        this.mHeaderDividersEnabled = z;
    }

    @Override // android.widget.ListView
    public void setFooterDividersEnabled(boolean z) {
        this.mFooterDividersEnabled = z;
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.ListView, android.widget.AbsListView, android.view.ViewGroup, android.view.View
    public void dispatchDraw(Canvas canvas) {
        int i;
        int i2;
        int i3;
        super.dispatchDraw(canvas);
        Drawable overscrollHeader = getOverscrollHeader();
        Drawable overscrollFooter = getOverscrollFooter();
        boolean z = false;
        int i4 = overscrollHeader != null ? 1 : 0;
        boolean z2 = overscrollFooter != null;
        boolean z3 = getDivider() != null;
        if (z3 || i4 != 0 || z2) {
            Rect rect = this.mTempRect;
            rect.left = getPaddingLeft();
            rect.right = (getRight() - getLeft()) - getPaddingRight();
            int childCount = getChildCount();
            int headerViewsCount = getHeaderViewsCount();
            int count = getCount() - getFooterViewsCount();
            boolean z4 = this.mHeaderDividersEnabled;
            boolean z5 = this.mFooterDividersEnabled;
            int firstVisiblePosition = getFirstVisiblePosition();
            getAdapter();
            if (isClipToPadding()) {
                i2 = getListPaddingTop();
                i = getListPaddingBottom();
            } else {
                i2 = 0;
                i = 0;
            }
            int bottom = ((getBottom() - getTop()) - i) + getScrollY();
            if (!isStackFromBottom()) {
                int scrollY = getScrollY();
                if (childCount > 0 && scrollY < 0 && z3) {
                    rect.bottom = 0;
                    rect.top = -getDividerHeight();
                    drawDivider(canvas, rect, -1);
                }
                int i5 = 0;
                while (i5 < childCount) {
                    int i6 = firstVisiblePosition + i5;
                    boolean z6 = i6 < headerViewsCount ? true : z;
                    boolean z7 = i6 >= count ? true : z;
                    if ((z4 || !z6) && (z5 || !z7)) {
                        View childAt = getChildAt(i5);
                        int bottom2 = childAt.getBottom();
                        i3 = firstVisiblePosition;
                        boolean z8 = i5 == childCount + -1;
                        if (z3 && shouldDrawDivider(i5) && childAt.getHeight() > 0 && bottom2 < bottom && (!z2 || !z8)) {
                            int i7 = i6 + 1;
                            if ((z4 || (!z6 && i7 >= headerViewsCount)) && (z8 || z5 || (!z7 && i7 < count))) {
                                int translationY = (int) childAt.getTranslationY();
                                rect.top = bottom2 + translationY;
                                rect.bottom = bottom2 + getDividerHeight() + translationY;
                                drawDivider(canvas, rect, i5);
                            }
                        }
                    } else {
                        i3 = firstVisiblePosition;
                    }
                    i5++;
                    firstVisiblePosition = i3;
                    z = false;
                }
            } else {
                int scrollY2 = getScrollY();
                int i8 = i4;
                while (i8 < childCount) {
                    int i9 = firstVisiblePosition + i8;
                    boolean z9 = i9 < headerViewsCount;
                    boolean z10 = i9 >= count;
                    if ((z4 || !z9) && (z5 || !z10)) {
                        int top = getChildAt(i8).getTop();
                        if (z3 && shouldDrawDivider(i8) && top > i2) {
                            boolean z11 = i8 == i4;
                            int i10 = i9 - 1;
                            if ((z4 || (!z9 && i10 >= headerViewsCount)) && (z11 || z5 || (!z10 && i10 < count))) {
                                rect.top = top - getDividerHeight();
                                rect.bottom = top;
                                drawDivider(canvas, rect, i8 - 1);
                            }
                        }
                    }
                    i8++;
                }
                if (childCount > 0 && scrollY2 > 0 && z3) {
                    rect.top = bottom;
                    rect.bottom = bottom + getDividerHeight();
                    drawDivider(canvas, rect, -1);
                }
            }
        }
        if (this.mDelAnimationFlag) {
            this.mDelAnimationFlag = false;
            startDelDropAnimation();
        }
    }

    @Override // android.widget.ListView
    public Drawable getDivider() {
        return this.mDivider;
    }

    @Override // android.widget.ListView
    public void setDivider(Drawable drawable) {
        this.mDivider = drawable;
        requestLayout();
        invalidate();
    }

    @Override // android.widget.ListView
    public int getDividerHeight() {
        return this.mDividerHeight;
    }

    private boolean isClipToPadding() {
        return this.mIsClipToPadding;
    }

    @Override // android.view.ViewGroup
    public void setClipToPadding(boolean z) {
        super.setClipToPadding(z);
        this.mIsClipToPadding = z;
    }

    /* access modifiers changed from: package-private */
    public void drawDivider(Canvas canvas, Rect rect, int i) {
        Drawable divider = getDivider();
        int dividerType = getDividerType(i + getFirstVisiblePosition());
        if (this.mDividerController != null) {
            if (dividerType == 1) {
                rect.left = 0;
                rect.right = getWidth();
            } else if (dividerType == 2) {
                rect.left = 100;
                rect.right = getWidth() - 32;
            }
        }
        divider.setBounds(rect);
        divider.draw(canvas);
    }

    private int getDividerType(int i) {
        IDividerController iDividerController = this.mDividerController;
        if (iDividerController == null) {
            return -1;
        }
        return iDividerController.getDividerType(i);
    }

    private boolean shouldDrawDivider(int i) {
        int dividerType = getDividerType(i + getFirstVisiblePosition());
        IDividerController iDividerController = this.mDividerController;
        return iDividerController == null || (iDividerController != null && dividerType > 0);
    }

    private ObjectAnimator getAnimator(int i, View view, float f) {
        if (i >= this.mAnimatorList.size()) {
            ObjectAnimator ofPropertyValuesHolder = ObjectAnimator.ofPropertyValuesHolder(view, PropertyValuesHolder.ofFloat("y", f, (float) view.getTop()));
            this.mAnimatorList.add(ofPropertyValuesHolder);
            return ofPropertyValuesHolder;
        }
        ObjectAnimator objectAnimator = this.mAnimatorList.get(i);
        objectAnimator.getValues()[0].setFloatValues(f, (float) view.getTop());
        objectAnimator.setTarget(view);
        return objectAnimator;
    }

    private void startDelDropAnimation() {
        this.mDelAniSet = new AnimatorSet();
        setDelViewLocation();
        for (int i = 0; i < this.mNowViewList.size(); i++) {
            ObjectAnimator animator = getAnimator(i, this.mNowViewList.get(i), (float) this.mDelOriViewTopList.get(i).intValue());
            animator.setDuration((long) 200);
            animator.setInterpolator(this.mDecelerateInterpolator);
            animator.addUpdateListener(this.mAnimatorUpdateListener);
            this.mDelAniSet.playTogether(animator);
        }
        this.mDelAniSet.addListener(new AnimatorListenerAdapter() { // from class: com.google.android.material.listview.OPListView.3
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator2) {
                OPListView.this.mAnimRunning = false;
                OPListView.this.mInDeleteAnimation = false;
                OPListView.this.mDisableTouchEvent = false;
                OPListView.this.mDelPosList.clear();
                OPListView.this.mDelOriViewTopList.clear();
                OPListView.this.mDelViewList.clear();
                OPListView.this.mNowViewList.clear();
                OPListView.this.invalidate();
                if (OPListView.this.mDeleteAnimationListener != null) {
                    OPListView.this.mDeleteAnimationListener.onAnimationEnd();
                }
            }
        });
        this.mDelAniSet.start();
    }

    /* JADX DEBUG: Multi-variable search result rejected for r6v3, resolved type: java.util.ArrayList<android.view.View> */
    /* JADX DEBUG: Multi-variable search result rejected for r6v4, resolved type: java.util.ArrayList<java.lang.Integer> */
    /* JADX WARN: Multi-variable type inference failed */
    private void setDelViewLocation() {
        int firstVisiblePosition = getFirstVisiblePosition();
        int childCount = getChildCount();
        int i = 0;
        boolean z = getLastVisiblePosition() == getAdapter().getCount() - 1;
        boolean z2 = firstVisiblePosition == 0;
        getTop();
        int bottom = getBottom();
        int childCount2 = getChildCount();
        ArrayList<View> arrayList = this.mNowViewList;
        if (arrayList == null) {
            this.mNowViewList = new ArrayList<>();
        } else {
            arrayList.clear();
        }
        int i2 = 0;
        for (int i3 = 0; i3 < childCount2; i3++) {
            View childAt = getChildAt(i3);
            this.mNowViewList.add(childAt);
            if (i3 == 0 && childAt != null) {
                i2 = childAt.getHeight();
            }
        }
        if (this.mOriLastPage) {
            int i4 = this.mOriUpperDeleteCount;
            if (i4 == 0) {
                if (this.mOriCurDeleteCount != 0) {
                    Log.d("OPListView", "DeleteAnimation Case 14 ");
                }
            } else if (this.mOriCurDeleteCount == 0) {
                if (i4 >= this.mOriCurLeftCount) {
                    Log.d("OPListView", "DeleteAnimation Case 12 ");
                    this.mDelOriViewTopList.clear();
                } else {
                    Log.d("OPListView", "DeleteAnimation Case 13 ");
                    for (int i5 = 0; i5 < this.mOriUpperDeleteCount; i5++) {
                        this.mDelOriViewTopList.remove(0);
                    }
                }
            } else if (z2) {
                Log.d("OPListView", "DeleteAnimation Case 17 ");
            } else if (i4 >= this.mOriCurLeftCount) {
                Log.d("OPListView", "DeleteAnimation Case 15 ");
            } else {
                Log.d("OPListView", "DeleteAnimation Case 16 ");
            }
            int i6 = 1;
            while (childCount > this.mDelOriViewTopList.size()) {
                this.mDelOriViewTopList.add(0, Integer.valueOf((-i2) * i6));
                i6++;
            }
        } else if (!z) {
            int i7 = this.mOriUpperDeleteCount;
            if (i7 == 0) {
                Log.d("OPListView", "DeleteAnimation Case 1");
            } else if (i7 >= this.mOriCurLeftCount) {
                Log.d("OPListView", "DeleteAnimation Case 3 ");
                this.mDelOriViewTopList.clear();
            } else {
                Log.d("OPListView", "DeleteAnimation Case 2 ");
                for (int i8 = 0; i8 < this.mOriUpperDeleteCount; i8++) {
                    this.mDelOriViewTopList.remove(0);
                }
            }
        } else {
            if (!z2) {
                int i9 = this.mOriUpperDeleteCount;
                if (i9 == 0) {
                    Log.d("OPListView", "DeleteAnimation Case 4 ");
                } else if (this.mOriCurDeleteCount == 0) {
                    if (i9 >= this.mOriCurLeftCount) {
                        Log.d("OPListView", "DeleteAnimation Case 9 ");
                    } else {
                        Log.d("OPListView", "DeleteAnimation Case 10 ");
                    }
                } else if (i9 >= this.mOriCurLeftCount) {
                    Log.d("OPListView", "DeleteAnimation Case 5 ");
                } else {
                    Log.d("OPListView", "DeleteAnimation Case 6 ");
                }
            } else if (this.mOriCurDeleteCount == 0) {
                Log.d("OPListView", "DeleteAnimation Case 11 ");
            } else if (this.mOriUpperDeleteCount >= this.mOriCurLeftCount) {
                Log.d("OPListView", "DeleteAnimation Case 7 ");
            } else {
                Log.d("OPListView", "DeleteAnimation Case 8 ");
            }
            int i10 = 0;
            while (i10 < this.mOriBelowLeftCount) {
                i10++;
                this.mDelOriViewTopList.add(Integer.valueOf((i10 * i2) + bottom));
            }
            int size = this.mDelOriViewTopList.size() - childCount;
            for (int i11 = 0; i11 < size; i11++) {
                this.mDelOriViewTopList.remove(0);
            }
            int i12 = 1;
            while (childCount > this.mDelOriViewTopList.size()) {
                this.mDelOriViewTopList.add(0, Integer.valueOf((-i2) * i12));
                i12++;
            }
        }
        int size2 = this.mNowViewList.size() - this.mDelOriViewTopList.size();
        int i13 = 0;
        while (i13 < size2) {
            i13++;
            this.mDelOriViewTopList.add(Integer.valueOf((i2 * i13) + bottom));
        }
        int i14 = 0;
        for (int i15 = childCount2 - 1; i15 >= 0; i15--) {
            if (this.mNowViewList.get(i15).getTop() == this.mDelOriViewTopList.get(i15).intValue()) {
                this.mNowViewList.remove(i15);
                this.mDelOriViewTopList.remove(i15);
            } else if (this.mDelOriViewTopList.get(i15).intValue() < this.mNowViewList.get(i15).getTop()) {
                i14++;
            }
        }
        if (i14 > 1) {
            ArrayList arrayList2 = (ArrayList) this.mNowViewList.clone();
            ArrayList arrayList3 = (ArrayList) this.mDelOriViewTopList.clone();
            this.mNowViewList.clear();
            this.mDelOriViewTopList.clear();
            while (i < arrayList2.size()) {
                int i16 = i < i14 ? (i14 - i) - 1 : i;
                this.mNowViewList.add(arrayList2.get(i16));
                this.mDelOriViewTopList.add(arrayList3.get(i16));
                i++;
            }
        }
    }
}
