package com.android.systemui.qs;

import android.util.Log;
import android.view.View;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.qs.QS;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.plugins.qs.QSTileView;
import com.android.systemui.qs.PagedTileLayout;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.QSPanel;
import com.android.systemui.qs.TouchAnimator;
import com.android.systemui.tuner.TunerService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
public class QSAnimator implements QSHost.Callback, PagedTileLayout.PageListener, TouchAnimator.Listener, View.OnLayoutChangeListener, View.OnAttachStateChangeListener, TunerService.Tunable {
    private TouchAnimator mAllPagesDelayedAnimator;
    private final ArrayList<View> mAllViews = new ArrayList<>();
    private boolean mAllowFancy;
    private int mBottom;
    private TouchAnimator mFirstPageAnimator;
    private TouchAnimator mFirstPageDelayedAnimator;
    private boolean mFullRows;
    private QSTileHost mHost;
    private float mLastPosition;
    private int mLeft;
    private boolean mNeedsAnimatorUpdate = false;
    private final TouchAnimator.Listener mNonFirstPageListener = new TouchAnimator.ListenerAdapter() { // from class: com.android.systemui.qs.QSAnimator.1
        @Override // com.android.systemui.qs.TouchAnimator.Listener
        public void onAnimationAtEnd() {
            QSAnimator.this.mQuickQsPanel.setVisibility(4);
        }

        @Override // com.android.systemui.qs.TouchAnimator.Listener
        public void onAnimationStarted() {
            QSAnimator.this.mQuickQsPanel.setVisibility(0);
        }
    };
    private TouchAnimator mNonfirstPageAnimator;
    private TouchAnimator mNonfirstPageDelayedAnimator;
    private int mOldBottom;
    private int mOldLeft;
    private int mOldRight;
    private int mOldTop;
    private boolean mOnFirstPage = true;
    private boolean mOnKeyguard;
    private TouchAnimator mPageIndicatorAnimator;
    private final QS mQs;
    private final QSPanel mQsPanel;
    private final QuickQSPanel mQuickQsPanel;
    private final ArrayList<View> mQuickQsViews = new ArrayList<>();
    private int mRight;
    private boolean mShowCollapsedOnKeyguard;
    private int mTop;
    private TouchAnimator mTranslationXAnimator;
    private TouchAnimator mTranslationYAnimator;
    private Runnable mUpdateAnimators = new Runnable() { // from class: com.android.systemui.qs.QSAnimator.2
        @Override // java.lang.Runnable
        public void run() {
            QSAnimator.this.updateAnimators();
            QSAnimator.this.setCurrentPosition();
        }
    };

    public QSAnimator(QS qs, QuickQSPanel quickQSPanel, QSPanel qSPanel) {
        this.mQs = qs;
        this.mQuickQsPanel = quickQSPanel;
        this.mQsPanel = qSPanel;
        qSPanel.addOnAttachStateChangeListener(this);
        qs.getView().addOnLayoutChangeListener(this);
        if (this.mQsPanel.isAttachedToWindow()) {
            onViewAttachedToWindow(null);
        }
        QSPanel.QSTileLayout tileLayout = this.mQsPanel.getTileLayout();
        if (tileLayout instanceof PagedTileLayout) {
            PagedTileLayout pagedTileLayout = (PagedTileLayout) tileLayout;
        } else {
            Log.w("QSAnimator", "QS Not using page layout");
        }
        qSPanel.setPageListener(this);
    }

    public void onRtlChanged() {
        updateAnimators();
    }

    public void onQsScrollingChanged() {
        this.mNeedsAnimatorUpdate = true;
    }

    public void setOnKeyguard(boolean z) {
        this.mOnKeyguard = z;
        updateQQSVisibility();
        if (this.mOnKeyguard) {
            clearAnimationState();
        }
    }

    /* access modifiers changed from: package-private */
    public void setShowCollapsedOnKeyguard(boolean z) {
        this.mShowCollapsedOnKeyguard = z;
        updateQQSVisibility();
        setCurrentPosition();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setCurrentPosition() {
        setPosition(this.mLastPosition);
    }

    private void updateQQSVisibility() {
        this.mQuickQsPanel.setVisibility((!this.mOnKeyguard || this.mShowCollapsedOnKeyguard) ? 0 : 4);
    }

    public void setHost(QSTileHost qSTileHost) {
        this.mHost = qSTileHost;
        qSTileHost.addCallback(this);
        updateAnimators();
    }

    @Override // android.view.View.OnAttachStateChangeListener
    public void onViewAttachedToWindow(View view) {
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "sysui_qs_fancy_anim", "sysui_qs_move_whole_rows", "sysui_qqs_count");
    }

    @Override // android.view.View.OnAttachStateChangeListener
    public void onViewDetachedFromWindow(View view) {
        QSTileHost qSTileHost = this.mHost;
        if (qSTileHost != null) {
            qSTileHost.removeCallback(this);
        }
        ((TunerService) Dependency.get(TunerService.class)).removeTunable(this);
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        if ("sysui_qs_fancy_anim".equals(str)) {
            boolean parseIntegerSwitch = TunerService.parseIntegerSwitch(str2, true);
            this.mAllowFancy = parseIntegerSwitch;
            if (!parseIntegerSwitch) {
                clearAnimationState();
            }
        } else if ("sysui_qs_move_whole_rows".equals(str)) {
            this.mFullRows = TunerService.parseIntegerSwitch(str2, true);
        } else if ("sysui_qqs_count".equals(str)) {
            QuickQSPanel.parseNumTiles(str2);
            clearAnimationState();
        }
        updateAnimators();
    }

    @Override // com.android.systemui.qs.PagedTileLayout.PageListener
    public void onPageChanged(boolean z) {
        if (this.mOnFirstPage != z) {
            if (!z) {
                clearAnimationState();
            } else {
                updateAnimators();
            }
            this.mOnFirstPage = z;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateAnimators() {
        int i;
        float f;
        Collection<QSTile> collection;
        int i2;
        int i3;
        float f2;
        int[] iArr;
        boolean z;
        boolean z2;
        int[] iArr2;
        this.mNeedsAnimatorUpdate = false;
        TouchAnimator.Builder builder = new TouchAnimator.Builder();
        TouchAnimator.Builder builder2 = new TouchAnimator.Builder();
        TouchAnimator.Builder builder3 = new TouchAnimator.Builder();
        if (this.mQsPanel.getHost() != null) {
            Collection<QSTile> tiles = this.mQsPanel.getHost().getTiles();
            int[] iArr3 = new int[2];
            int[] iArr4 = new int[2];
            clearAnimationState();
            this.mAllViews.clear();
            this.mQuickQsViews.clear();
            QSPanel.QSTileLayout tileLayout = this.mQsPanel.getTileLayout();
            this.mAllViews.add((View) tileLayout);
            int measuredHeight = this.mQs.getView() != null ? this.mQs.getView().getMeasuredHeight() : 0;
            int measuredWidth = this.mQs.getView() != null ? this.mQs.getView().getMeasuredWidth() : 0;
            int bottom = (measuredHeight - this.mQs.getHeader().getBottom()) + this.mQs.getHeader().getPaddingBottom();
            float f3 = (float) bottom;
            builder.addFloat(tileLayout, "translationY", f3, 0.0f);
            Iterator<QSTile> it = tiles.iterator();
            int i4 = 0;
            int i5 = 0;
            while (it.hasNext()) {
                QSTile next = it.next();
                QSTileView tileView = this.mQsPanel.getTileView(next);
                if (tileView == null) {
                    Log.e("QSAnimator", "tileView is null " + next.getTileSpec());
                    collection = tiles;
                    i2 = bottom;
                    i3 = measuredWidth;
                    f2 = f3;
                } else {
                    collection = tiles;
                    View iconView = tileView.getIcon().getIconView();
                    i2 = bottom;
                    View view = this.mQs.getView();
                    f2 = f3;
                    i3 = measuredWidth;
                    if (i4 < this.mQuickQsPanel.getTileLayout().getNumVisibleTiles() && this.mAllowFancy) {
                        QSTileView tileView2 = this.mQuickQsPanel.getTileView(next);
                        if (tileView2 != null) {
                            int i6 = iArr3[0];
                            getRelativePosition(iArr3, tileView2.getIcon().getIconView(), view);
                            getRelativePosition(iArr4, iconView, view);
                            int i7 = iArr4[0] - iArr3[0];
                            int i8 = iArr4[1] - iArr3[1];
                            i5 = iArr3[0] - i6;
                            if (i4 < tileLayout.getNumVisibleTiles()) {
                                builder2.addFloat(tileView2, "translationX", 0.0f, (float) i7);
                                builder3.addFloat(tileView2, "translationY", 0.0f, (float) i8);
                                builder2.addFloat(tileView, "translationX", (float) (-i7), 0.0f);
                                builder3.addFloat(tileView, "translationY", (float) (-i8), 0.0f);
                                iArr2 = iArr4;
                            } else {
                                iArr2 = iArr4;
                                builder.addFloat(tileView2, "alpha", 1.0f, 0.0f);
                                builder3.addFloat(tileView2, "translationY", 0.0f, (float) i8);
                                builder2.addFloat(tileView2, "translationX", 0.0f, (float) (this.mQsPanel.isLayoutRtl() ? i7 - i3 : i7 + i3));
                            }
                            this.mQuickQsViews.add(tileView.getIconWithBackground());
                            this.mAllViews.add(tileView.getIcon());
                            this.mAllViews.add(tileView2);
                            iArr = iArr3;
                            bottom = i2;
                            iArr4 = iArr2;
                        }
                    } else if (!this.mFullRows || i4 >= tileLayout.getNumVisibleTiles()) {
                        iArr = iArr3;
                        iArr4 = iArr4;
                        builder.addFloat(tileView, "alpha", 0.0f, 1.0f);
                        bottom = i2;
                        z2 = false;
                        z = true;
                        builder.addFloat(tileView, "translationY", (float) (-bottom), 0.0f);
                        this.mAllViews.add(tileView);
                        i4++;
                        it = it;
                        tiles = collection;
                        f3 = f2;
                        measuredWidth = i3;
                        iArr3 = iArr;
                    } else {
                        iArr3[0] = iArr3[0] + i5;
                        iArr4 = iArr4;
                        getRelativePosition(iArr4, iconView, view);
                        iArr = iArr3;
                        builder.addFloat(tileView, "translationY", f2, 0.0f);
                        builder2.addFloat(tileView, "translationX", (float) (-(iArr4[0] - iArr3[0])), 0.0f);
                        builder3.addFloat(tileView, "translationY", (float) (-(iArr4[1] - iArr3[1])), 0.0f);
                        this.mAllViews.add(iconView);
                        bottom = i2;
                    }
                    z2 = false;
                    z = true;
                    this.mAllViews.add(tileView);
                    i4++;
                    it = it;
                    tiles = collection;
                    f3 = f2;
                    measuredWidth = i3;
                    iArr3 = iArr;
                }
                it = it;
                bottom = i2;
                tiles = collection;
                f3 = f2;
                measuredWidth = i3;
            }
            if (this.mAllowFancy) {
                View brightnessView = this.mQsPanel.getBrightnessView();
                if (brightnessView != null) {
                    TouchAnimator.Builder builder4 = new TouchAnimator.Builder();
                    builder4.setStartDelay(0.9f);
                    builder4.addFloat(brightnessView, "alpha", 0.0f, 1.0f);
                    builder4.build();
                    this.mAllViews.add(brightnessView);
                }
                builder.setListener(this);
                this.mFirstPageAnimator = builder.build();
                TouchAnimator.Builder builder5 = new TouchAnimator.Builder();
                builder5.setStartDelay(0.84f);
                builder5.addFloat(tileLayout, "alpha", 0.0f, 1.0f);
                this.mFirstPageDelayedAnimator = builder5.build();
                TouchAnimator.Builder builder6 = new TouchAnimator.Builder();
                builder6.setStartDelay(0.84f);
                if (this.mQsPanel.getSecurityFooter() != null) {
                    i = 2;
                    builder6.addFloat(this.mQsPanel.getSecurityFooter().getView(), "alpha", 0.0f, 1.0f);
                } else {
                    i = 2;
                }
                if (this.mQsPanel.getDivider() != null) {
                    float[] fArr = new float[i];
                    // fill-array-data instruction
                    fArr[0] = 0.0f;
                    fArr[1] = 1.0f;
                    builder6.addFloat(this.mQsPanel.getDivider(), "alpha", fArr);
                }
                this.mAllPagesDelayedAnimator = builder6.build();
                if (this.mQsPanel.getSecurityFooter() != null) {
                    this.mAllViews.add(this.mQsPanel.getSecurityFooter().getView());
                }
                if (this.mQsPanel.getDivider() != null) {
                    this.mAllViews.add(this.mQsPanel.getDivider());
                }
                TouchAnimator.Builder builder7 = new TouchAnimator.Builder();
                builder7.setStartDelay(0.9f);
                builder7.addFloat(this.mQsPanel.getPageIndicator(), "alpha", 0.0f, 1.0f);
                this.mPageIndicatorAnimator = builder7.build();
                this.mAllViews.add(this.mQsPanel.getPageIndicator());
                if (tiles.size() <= 3) {
                    f = 1.0f;
                } else {
                    f = tiles.size() <= 6 ? 0.4f : 0.0f;
                }
                PathInterpolatorBuilder pathInterpolatorBuilder = new PathInterpolatorBuilder(0.0f, 0.0f, f, 1.0f);
                builder2.setInterpolator(pathInterpolatorBuilder.getXInterpolator());
                builder3.setInterpolator(pathInterpolatorBuilder.getYInterpolator());
                this.mTranslationXAnimator = builder2.build();
                this.mTranslationYAnimator = builder3.build();
            }
            TouchAnimator.Builder builder8 = new TouchAnimator.Builder();
            builder8.addFloat(this.mQuickQsPanel, "alpha", 1.0f, 0.0f);
            builder8.addFloat(this.mQsPanel.getPageIndicator(), "alpha", 0.0f, 1.0f);
            builder8.setListener(this.mNonFirstPageListener);
            builder8.setEndDelay(0.5f);
            this.mNonfirstPageAnimator = builder8.build();
            TouchAnimator.Builder builder9 = new TouchAnimator.Builder();
            builder9.setStartDelay(0.14f);
            builder9.addFloat(tileLayout, "alpha", 0.0f, 1.0f);
            this.mNonfirstPageDelayedAnimator = builder9.build();
        }
    }

    private void getRelativePosition(int[] iArr, View view, View view2) {
        iArr[0] = (view.getWidth() / 2) + 0;
        iArr[1] = 0;
        getRelativePositionInt(iArr, view, view2);
    }

    private void getRelativePositionInt(int[] iArr, View view, View view2) {
        if (view != view2 && view != null) {
            if (!(view instanceof PagedTileLayout.TilePage)) {
                iArr[0] = iArr[0] + view.getLeft();
                iArr[1] = iArr[1] + view.getTop();
            }
            if (!(view instanceof PagedTileLayout)) {
                iArr[0] = iArr[0] - view.getScrollX();
                iArr[1] = iArr[1] - view.getScrollY();
            }
            getRelativePositionInt(iArr, (View) view.getParent(), view2);
        }
    }

    public void setPosition(float f) {
        if (this.mNeedsAnimatorUpdate) {
            updateAnimators();
        }
        if (this.mFirstPageAnimator != null) {
            if (this.mOnKeyguard) {
                f = this.mShowCollapsedOnKeyguard ? 0.0f : 1.0f;
            }
            this.mLastPosition = f;
            if (this.mAllowFancy) {
                this.mAllPagesDelayedAnimator.setPosition(f);
            }
            if (this.mAllowFancy) {
                if (this.mOnFirstPage) {
                    this.mQuickQsPanel.setAlpha(1.0f);
                    this.mFirstPageAnimator.setPosition(f);
                    this.mFirstPageDelayedAnimator.setPosition(f);
                    this.mTranslationXAnimator.setPosition(f);
                    this.mTranslationYAnimator.setPosition(f);
                } else {
                    this.mNonfirstPageAnimator.setPosition(f);
                    this.mNonfirstPageDelayedAnimator.setPosition(f);
                }
                TouchAnimator touchAnimator = this.mPageIndicatorAnimator;
                if (touchAnimator != null) {
                    touchAnimator.setPosition(f);
                }
            }
            if (f == 0.0f) {
                this.mQuickQsPanel.getBrightnessView().setVisibility(0);
            } else {
                this.mQuickQsPanel.getBrightnessView().setVisibility(4);
            }
        }
    }

    @Override // com.android.systemui.qs.TouchAnimator.Listener
    public void onAnimationAtStart() {
        this.mQuickQsPanel.setVisibility(0);
    }

    @Override // com.android.systemui.qs.TouchAnimator.Listener
    public void onAnimationAtEnd() {
        this.mQuickQsPanel.setVisibility(4);
        int size = this.mQuickQsViews.size();
        for (int i = 0; i < size; i++) {
            this.mQuickQsViews.get(i).setVisibility(0);
        }
    }

    @Override // com.android.systemui.qs.TouchAnimator.Listener
    public void onAnimationStarted() {
        updateQQSVisibility();
        if (this.mOnFirstPage) {
            int size = this.mQuickQsViews.size();
            for (int i = 0; i < size; i++) {
                this.mQuickQsViews.get(i).setVisibility(4);
            }
        }
    }

    private void clearAnimationState() {
        int size = this.mAllViews.size();
        this.mQuickQsPanel.setAlpha(0.0f);
        for (int i = 0; i < size; i++) {
            View view = this.mAllViews.get(i);
            view.setAlpha(1.0f);
            view.setTranslationX(0.0f);
            view.setTranslationY(0.0f);
        }
        int size2 = this.mQuickQsViews.size();
        for (int i2 = 0; i2 < size2; i2++) {
            this.mQuickQsViews.get(i2).setVisibility(0);
        }
    }

    @Override // android.view.View.OnLayoutChangeListener
    public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        if (this.mLeft != i || this.mTop != i2 || this.mRight != i3 || this.mBottom != i4 || this.mOldLeft != i5 || this.mOldTop != i6 || this.mOldRight != i7 || this.mOldBottom != i8) {
            this.mLeft = i;
            this.mTop = i2;
            this.mRight = i3;
            this.mBottom = i4;
            this.mOldLeft = i5;
            this.mOldTop = i6;
            this.mOldRight = i7;
            this.mOldBottom = i8;
            this.mQsPanel.post(this.mUpdateAnimators);
        }
    }

    @Override // com.android.systemui.qs.QSHost.Callback
    public void onTilesChanged() {
        this.mQsPanel.post(this.mUpdateAnimators);
    }
}
