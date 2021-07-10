package com.android.systemui.qs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import androidx.lifecycle.Lifecycle;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0011R$layout;
import com.android.systemui.C0016R$style;
import com.android.systemui.Interpolators;
import com.android.systemui.media.MediaHost;
import com.android.systemui.plugins.qs.QS;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.qs.QSContainerImplController;
import com.android.systemui.qs.customize.QSCustomizer;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.phone.NotificationsQuickSettingsContainer;
import com.android.systemui.statusbar.policy.RemoteInputQuickSettingsDisabler;
import com.android.systemui.util.InjectionInflationController;
import com.android.systemui.util.Utils;
import com.android.systemui.util.animation.UniqueObjectHostView;
import com.oneplus.systemui.qs.OpQSFragment;
public class QSFragment extends OpQSFragment implements QS, CommandQueue.Callbacks, StatusBarStateController.StateListener {
    private final Animator.AnimatorListener mAnimateHeaderSlidingInListener = new AnimatorListenerAdapter() { // from class: com.android.systemui.qs.QSFragment.3
        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animator) {
            QSFragment.this.mHeaderAnimating = false;
            QSFragment.this.updateQsState();
        }
    };
    private QSContainerImpl mContainer;
    private long mDelay;
    private QSFooter mFooter;
    protected QuickStatusBarHeader mHeader;
    private boolean mHeaderAnimating;
    private final QSTileHost mHost;
    private final InjectionInflationController mInjectionInflater;
    private boolean mLastKeyguardAndExpanded;
    private float mLastQSExpansion = -1.0f;
    private int mLastViewHeight;
    private int mLayoutDirection;
    private boolean mListening;
    private QS.HeightListener mPanelView;
    private QSAnimator mQSAnimator;
    private QSContainerImplController mQSContainerImplController;
    private final QSContainerImplController.Builder mQSContainerImplControllerBuilder;
    private QSCustomizer mQSCustomizer;
    private QSDetail mQSDetail;
    protected QSPanel mQSPanel;
    protected NonInterceptingScrollView mQSPanelScrollView;
    private final Rect mQsBounds = new Rect();
    private boolean mQsDisabled;
    private boolean mQsExpanded;
    private final RemoteInputQuickSettingsDisabler mRemoteInputQuickSettingsDisabler;
    private boolean mShowCollapsedOnKeyguard;
    private boolean mStackScrollerOverscrolling;
    private final ViewTreeObserver.OnPreDrawListener mStartHeaderSlidingIn = new ViewTreeObserver.OnPreDrawListener() { // from class: com.android.systemui.qs.QSFragment.2
        @Override // android.view.ViewTreeObserver.OnPreDrawListener
        public boolean onPreDraw() {
            if (QSFragment.this.getView() == null) {
                return true;
            }
            QSFragment.this.getView().getViewTreeObserver().removeOnPreDrawListener(this);
            QSFragment.this.getView().animate().translationY(0.0f).setStartDelay(QSFragment.this.mDelay).setDuration(448).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).setListener(QSFragment.this.mAnimateHeaderSlidingInListener).start();
            return true;
        }
    };
    private int mState;
    private final StatusBarStateController mStatusBarStateController;
    private int[] mTmpLocation = new int[2];

    @Override // com.android.systemui.plugins.qs.QS
    public void setHasNotifications(boolean z) {
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setHeaderClickable(boolean z) {
    }

    public QSFragment(RemoteInputQuickSettingsDisabler remoteInputQuickSettingsDisabler, InjectionInflationController injectionInflationController, QSTileHost qSTileHost, StatusBarStateController statusBarStateController, CommandQueue commandQueue, QSContainerImplController.Builder builder) {
        this.mRemoteInputQuickSettingsDisabler = remoteInputQuickSettingsDisabler;
        this.mInjectionInflater = injectionInflationController;
        this.mQSContainerImplControllerBuilder = builder;
        commandQueue.observe(getLifecycle(), (Lifecycle) this);
        this.mHost = qSTileHost;
        this.mStatusBarStateController = statusBarStateController;
    }

    @Override // android.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return this.mInjectionInflater.injectable(layoutInflater.cloneInContext(new ContextThemeWrapper(getContext(), C0016R$style.qs_theme))).inflate(C0011R$layout.qs_panel, viewGroup, false);
    }

    @Override // android.app.Fragment
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        this.mQSPanel = (QSPanel) view.findViewById(C0008R$id.quick_settings_panel);
        NonInterceptingScrollView nonInterceptingScrollView = (NonInterceptingScrollView) view.findViewById(C0008R$id.expanded_qs_scroll_view);
        this.mQSPanelScrollView = nonInterceptingScrollView;
        nonInterceptingScrollView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() { // from class: com.android.systemui.qs.-$$Lambda$QSFragment$2XSLuGneMm7PezTcR5XlC3hGadQ
            @Override // android.view.View.OnLayoutChangeListener
            public final void onLayoutChange(View view2, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                QSFragment.this.lambda$onViewCreated$0$QSFragment(view2, i, i2, i3, i4, i5, i6, i7, i8);
            }
        });
        this.mQSPanelScrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() { // from class: com.android.systemui.qs.-$$Lambda$QSFragment$D2SSstlg0NEg-LdXPc7QqvH01NE
            @Override // android.view.View.OnScrollChangeListener
            public final void onScrollChange(View view2, int i, int i2, int i3, int i4) {
                QSFragment.this.lambda$onViewCreated$1$QSFragment(view2, i, i2, i3, i4);
            }
        });
        this.mQSDetail = (QSDetail) view.findViewById(C0008R$id.qs_detail);
        this.mHeader = (QuickStatusBarHeader) view.findViewById(C0008R$id.header);
        this.mQSPanel.setHeaderContainer((ViewGroup) view.findViewById(C0008R$id.header_text_container));
        this.mFooter = (QSFooter) view.findViewById(C0008R$id.qs_footer);
        this.mContainer = (QSContainerImpl) view.findViewById(C0008R$id.quick_settings_container);
        QSContainerImplController.Builder builder = this.mQSContainerImplControllerBuilder;
        builder.setQSContainerImpl((QSContainerImpl) view);
        this.mQSContainerImplController = builder.build();
        this.mQSDetail.setQsPanel(this.mQSPanel, this.mHeader, (View) this.mFooter);
        this.mQSAnimator = new QSAnimator(this, (QuickQSPanel) this.mHeader.findViewById(C0008R$id.quick_qs_panel), this.mQSPanel);
        QSCustomizer qSCustomizer = (QSCustomizer) view.findViewById(C0008R$id.qs_customize);
        this.mQSCustomizer = qSCustomizer;
        qSCustomizer.setQs(this);
        if (bundle != null) {
            setExpanded(bundle.getBoolean("expanded"));
            setListening(bundle.getBoolean("listening"));
            setEditLocation(view);
            this.mQSCustomizer.restoreInstanceState(bundle);
            if (this.mQsExpanded) {
                this.mQSPanel.getTileLayout().restoreInstanceState(bundle);
            }
        }
        setHost(this.mHost);
        this.mStatusBarStateController.addCallback(this);
        onStateChanged(this.mStatusBarStateController.getState());
        view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() { // from class: com.android.systemui.qs.-$$Lambda$QSFragment$O2Q4y8liaaT1BCWBXINGcury9NY
            @Override // android.view.View.OnLayoutChangeListener
            public final void onLayoutChange(View view2, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                QSFragment.this.lambda$onViewCreated$2$QSFragment(view2, i, i2, i3, i4, i5, i6, i7, i8);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onViewCreated$0 */
    public /* synthetic */ void lambda$onViewCreated$0$QSFragment(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        updateQsBounds();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onViewCreated$1 */
    public /* synthetic */ void lambda$onViewCreated$1$QSFragment(View view, int i, int i2, int i3, int i4) {
        this.mQSAnimator.onQsScrollingChanged();
        this.mHeader.setExpandedScrollAmount(i2);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onViewCreated$2 */
    public /* synthetic */ void lambda$onViewCreated$2$QSFragment(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        if (i6 - i8 != i2 - i4) {
            float f = this.mLastQSExpansion;
            setQsExpansion(f, f);
        }
    }

    @Override // com.android.systemui.util.LifecycleFragment, android.app.Fragment
    public void onDestroy() {
        super.onDestroy();
        this.mQSPanel.setBrightnessListening(false);
        this.mStatusBarStateController.removeCallback(this);
        if (this.mListening) {
            setListening(false);
        }
        this.mQSCustomizer.setQs(null);
    }

    @Override // android.app.Fragment
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putBoolean("expanded", this.mQsExpanded);
        bundle.putBoolean("listening", this.mListening);
        this.mQSCustomizer.saveInstanceState(bundle);
        if (this.mQsExpanded) {
            this.mQSPanel.getTileLayout().saveInstanceState(bundle);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isListening() {
        return this.mListening;
    }

    /* access modifiers changed from: package-private */
    public boolean isExpanded() {
        return this.mQsExpanded;
    }

    @Override // com.android.systemui.plugins.qs.QS
    public View getHeader() {
        return this.mHeader;
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setPanelView(QS.HeightListener heightListener) {
        this.mPanelView = heightListener;
    }

    @Override // android.app.Fragment, android.content.ComponentCallbacks
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        setEditLocation(getView());
        if (configuration.getLayoutDirection() != this.mLayoutDirection) {
            this.mLayoutDirection = configuration.getLayoutDirection();
            QSAnimator qSAnimator = this.mQSAnimator;
            if (qSAnimator != null) {
                qSAnimator.onRtlChanged();
            }
        }
        QSContainerImpl qSContainerImpl = this.mContainer;
        if (qSContainerImpl != null) {
            qSContainerImpl.updateResources(configuration);
        }
    }

    private void setEditLocation(View view) {
        View findViewById = view.findViewById(16908291);
        int[] locationOnScreen = findViewById.getLocationOnScreen();
        this.mQSCustomizer.setEditLocation(locationOnScreen[0] + (findViewById.getWidth() / 2), locationOnScreen[1] + (findViewById.getHeight() / 2));
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setContainer(ViewGroup viewGroup) {
        if (viewGroup instanceof NotificationsQuickSettingsContainer) {
            this.mQSCustomizer.setContainer((NotificationsQuickSettingsContainer) viewGroup);
        }
    }

    @Override // com.android.systemui.plugins.qs.QS
    public boolean isCustomizing() {
        return this.mQSCustomizer.isCustomizing();
    }

    public void setHost(QSTileHost qSTileHost) {
        this.mQSPanel.setHost(qSTileHost, this.mQSCustomizer);
        this.mHeader.setQSPanel(this.mQSPanel);
        this.mFooter.setQSPanel(this.mQSPanel);
        this.mQSDetail.setHost(qSTileHost);
        QSAnimator qSAnimator = this.mQSAnimator;
        if (qSAnimator != null) {
            qSAnimator.setHost(qSTileHost);
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void disable(int i, int i2, int i3, boolean z) {
        if (i == getContext().getDisplayId()) {
            int adjustDisableFlags = this.mRemoteInputQuickSettingsDisabler.adjustDisableFlags(i3);
            boolean z2 = (adjustDisableFlags & 1) != 0;
            if (z2 != this.mQsDisabled) {
                this.mQsDisabled = z2;
                this.mContainer.disable(i2, adjustDisableFlags, z);
                this.mHeader.disable(i2, adjustDisableFlags, z);
                this.mFooter.disable(i2, adjustDisableFlags, z);
                updateQsState();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateQsState() {
        boolean z = true;
        int i = 0;
        boolean z2 = this.mQsExpanded || this.mStackScrollerOverscrolling || this.mHeaderAnimating;
        this.mQSPanel.setExpanded(this.mQsExpanded);
        this.mQSDetail.setExpanded(this.mQsExpanded);
        boolean isKeyguardShowing = isKeyguardShowing();
        this.mHeader.setVisibility((this.mQsExpanded || !isKeyguardShowing || this.mHeaderAnimating || this.mShowCollapsedOnKeyguard) ? 0 : 4);
        this.mHeader.setExpanded((isKeyguardShowing && !this.mHeaderAnimating && !this.mShowCollapsedOnKeyguard) || (this.mQsExpanded && !this.mStackScrollerOverscrolling));
        this.mFooter.setVisibility((this.mQsDisabled || (!this.mQsExpanded && isKeyguardShowing && !this.mHeaderAnimating && !this.mShowCollapsedOnKeyguard)) ? 4 : 0);
        QSFooter qSFooter = this.mFooter;
        if ((!isKeyguardShowing || this.mHeaderAnimating || this.mShowCollapsedOnKeyguard) && (!this.mQsExpanded || this.mStackScrollerOverscrolling)) {
            z = false;
        }
        qSFooter.setExpanded(z);
        QSPanel qSPanel = this.mQSPanel;
        if (this.mQsDisabled || !z2) {
            i = 4;
        }
        qSPanel.setVisibility(i);
    }

    private boolean isKeyguardShowing() {
        return this.mStatusBarStateController.getState() == 1;
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setShowCollapsedOnKeyguard(boolean z) {
        if (z != this.mShowCollapsedOnKeyguard) {
            this.mShowCollapsedOnKeyguard = z;
            updateQsState();
            QSAnimator qSAnimator = this.mQSAnimator;
            if (qSAnimator != null) {
                qSAnimator.setShowCollapsedOnKeyguard(z);
            }
            if (!z && isKeyguardShowing()) {
                setQsExpansion(this.mLastQSExpansion, 0.0f);
            }
        }
    }

    public QSPanel getQsPanel() {
        return this.mQSPanel;
    }

    @Override // com.android.systemui.plugins.qs.QS
    public boolean isShowingDetail() {
        return this.mQSPanel.isShowingCustomize() || this.mQSDetail.isShowingDetail() || this.mQSCustomizer.isShown();
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setExpanded(boolean z) {
        this.mQsExpanded = z;
        this.mQSPanel.setListening(this.mListening, z);
        updateQsState();
    }

    private void setKeyguardShowing(boolean z) {
        this.mLastQSExpansion = -1.0f;
        QSAnimator qSAnimator = this.mQSAnimator;
        if (qSAnimator != null) {
            qSAnimator.setOnKeyguard(z);
        }
        this.mFooter.setKeyguardShowing(z);
        updateQsState();
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setOverscrolling(boolean z) {
        this.mStackScrollerOverscrolling = z;
        updateQsState();
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setListening(boolean z) {
        this.mListening = z;
        this.mQSContainerImplController.setListening(z);
        this.mHeader.setListening(z);
        this.mFooter.setListening(z);
        this.mQSPanel.setListening(this.mListening, this.mQsExpanded);
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setHeaderListening(boolean z) {
        this.mHeader.setListening(z);
        this.mFooter.setListening(z);
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setQsExpansion(float f, float f2) {
        this.mContainer.setExpansion(f);
        float f3 = f - 1.0f;
        boolean z = true;
        boolean z2 = isKeyguardShowing() && !this.mShowCollapsedOnKeyguard;
        if (!this.mHeaderAnimating && !headerWillBeAnimating()) {
            View view = getView();
            if (z2) {
                f2 = ((float) this.mHeader.getHeight()) * f3;
            }
            view.setTranslationY(f2);
        }
        int height = getView().getHeight();
        if (f != this.mLastQSExpansion || this.mLastKeyguardAndExpanded != z2 || this.mLastViewHeight != height) {
            this.mLastQSExpansion = f;
            this.mLastKeyguardAndExpanded = z2;
            this.mLastViewHeight = height;
            boolean z3 = f == 1.0f;
            boolean z4 = f == 0.0f;
            float bottom = f3 * ((float) ((((this.mQSPanelScrollView.getBottom() - this.mHeader.getBottom()) + this.mHeader.getPaddingBottom()) - (getContext().getResources().getConfiguration().orientation == 1 ? getExpandedMediaHeight() - getQuickMediaHeight() : 0)) - getSecurityViewHeight()));
            this.mHeader.setExpansion(z2, f, bottom);
            this.mFooter.setExpansion(z2 ? 1.0f : f);
            this.mQSPanel.getQsTileRevealController().setExpansion(f);
            this.mQSPanel.getTileLayout().setExpansion(f);
            this.mQSPanelScrollView.setTranslationY(bottom);
            if (z4) {
                this.mQSPanelScrollView.setScrollY(0);
            }
            this.mQSDetail.setFullyExpanded(z3);
            QSPanel qSPanel = this.mQSPanel;
            if (0.0f >= f || f >= 1.0f) {
                z = false;
            }
            qSPanel.setIsExpanding(z);
            if (!z3) {
                this.mQsBounds.top = (int) (-this.mQSPanelScrollView.getTranslationY());
                this.mQsBounds.right = this.mQSPanelScrollView.getWidth();
                this.mQsBounds.bottom = this.mQSPanelScrollView.getHeight();
            }
            updateQsBounds();
            QSAnimator qSAnimator = this.mQSAnimator;
            if (qSAnimator != null) {
                qSAnimator.setPosition(f);
            }
            updateMediaPositions();
        }
    }

    private void updateQsBounds() {
        if (this.mLastQSExpansion == 1.0f) {
            this.mQsBounds.set(0, 0, this.mQSPanelScrollView.getWidth(), this.mQSPanelScrollView.getHeight());
        }
        this.mQSPanelScrollView.setClipBounds(this.mQsBounds);
    }

    private void updateMediaPositions() {
        if (Utils.useQsMediaPlayer(getContext())) {
            this.mContainer.getLocationOnScreen(this.mTmpLocation);
            float height = (float) (this.mTmpLocation[1] + this.mContainer.getHeight());
            pinToBottom((height - ((float) this.mQSPanelScrollView.getScrollY())) + ((float) this.mQSPanelScrollView.getScrollRange()), this.mQSPanel.getMediaHost(), true);
            pinToBottom(height, this.mHeader.getHeaderQsPanel().getMediaHost(), false);
        }
    }

    private void pinToBottom(float f, MediaHost mediaHost, boolean z) {
        float f2;
        UniqueObjectHostView hostView = mediaHost.getHostView();
        if (this.mLastQSExpansion > 0.0f) {
            float totalBottomMargin = ((f - getTotalBottomMargin(hostView)) - ((float) hostView.getHeight())) - (((float) mediaHost.getCurrentBounds().top) - hostView.getTranslationY());
            if (z) {
                f2 = Math.min(totalBottomMargin, 0.0f);
            } else {
                f2 = Math.max(totalBottomMargin, 0.0f);
            }
            hostView.setTranslationY(f2);
            return;
        }
        hostView.setTranslationY(0.0f);
    }

    private float getTotalBottomMargin(View view) {
        View view2 = (View) view.getParent();
        int i = 0;
        while (true) {
            view = view2;
            if ((view instanceof QSContainerImpl) || view == null) {
                break;
            }
            i += view.getHeight() - view.getBottom();
            view2 = (View) view.getParent();
        }
        return (float) i;
    }

    private boolean headerWillBeAnimating() {
        if (this.mState != 1 || !this.mShowCollapsedOnKeyguard || isKeyguardShowing()) {
            return false;
        }
        return true;
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void animateHeaderSlidingIn(long j) {
        if (!this.mQsExpanded && getView().getTranslationY() != 0.0f) {
            this.mHeaderAnimating = true;
            this.mDelay = j;
            getView().getViewTreeObserver().addOnPreDrawListener(this.mStartHeaderSlidingIn);
        }
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void animateHeaderSlidingOut() {
        if (getView().getY() != ((float) (-this.mHeader.getHeight()))) {
            this.mHeaderAnimating = true;
            getView().animate().y((float) (-this.mHeader.getHeight())).setStartDelay(0).setDuration(360).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).setListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.qs.QSFragment.1
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    if (QSFragment.this.getView() != null) {
                        QSFragment.this.getView().animate().setListener(null);
                    }
                    QSFragment.this.mHeaderAnimating = false;
                    QSFragment.this.updateQsState();
                }
            }).start();
        }
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setExpandClickListener(View.OnClickListener onClickListener) {
        this.mFooter.setExpandClickListener(onClickListener);
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void closeDetail() {
        this.mQSPanel.closeDetail();
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void notifyCustomizeChanged() {
        this.mContainer.updateExpansion();
        int i = 0;
        this.mQSPanelScrollView.setVisibility(!this.mQSCustomizer.isCustomizing() ? 0 : 4);
        QSFooter qSFooter = this.mFooter;
        if (this.mQSCustomizer.isCustomizing()) {
            i = 4;
        }
        qSFooter.setVisibility(i);
        this.mPanelView.onQsHeightChanged();
    }

    @Override // com.android.systemui.plugins.qs.QS
    public int getDesiredHeight() {
        if (this.mQSCustomizer.isCustomizing()) {
            return getView().getHeight();
        }
        if (!this.mQSDetail.isClosingDetail()) {
            return getView().getMeasuredHeight();
        }
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.mQSPanelScrollView.getLayoutParams();
        return layoutParams.topMargin + layoutParams.bottomMargin + this.mQSPanelScrollView.getMeasuredHeight() + getView().getPaddingBottom();
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setHeightOverride(int i) {
        this.mContainer.setHeightOverride(i);
    }

    @Override // com.android.systemui.plugins.qs.QS
    public int getQsMinExpansionHeight() {
        return this.mHeader.getHeight();
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void hideImmediately() {
        getView().animate().cancel();
        getView().setY((float) (-this.mHeader.getHeight()));
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onStateChanged(int i) {
        this.mState = i;
        boolean z = true;
        if (i != 1) {
            z = false;
        }
        setKeyguardShowing(z);
    }

    public QuickQSPanel getQuickQsPanel() {
        return this.mHeader.getHeaderQsPanel();
    }

    private int getQuickMediaHeight() {
        try {
            if (this.mHeader.getHeaderQsPanel().getMediaHost().getVisible()) {
                return this.mHeader.getHeaderQsPanel().getMediaHost().getHostView().getMeasuredHeight();
            }
            return 0;
        } catch (Exception e) {
            Log.d(QS.TAG, "getQuickMediaHeight: exception caught. exit quietly.", e);
            return 0;
        }
    }

    private int getExpandedMediaHeight() {
        try {
            if (this.mQSPanel.getMediaHost().getVisible()) {
                return this.mQSPanel.getMediaHost().getHostView().getMeasuredHeight();
            }
            return 0;
        } catch (Exception e) {
            Log.d(QS.TAG, "getExpandedMediaHeight: exception caught. exit quietly.", e);
            return 0;
        }
    }

    private int getSecurityViewHeight() {
        try {
            QSSecurityFooter securityFooter = this.mQSPanel.getSecurityFooter();
            if (securityFooter == null || securityFooter.getView().getVisibility() != 0) {
                return 0;
            }
            return securityFooter.getView().getHeight();
        } catch (Exception e) {
            Log.d(QS.TAG, "getSecurityViewHeight: exception caught. exit quietly.", e);
            return 0;
        }
    }
}
