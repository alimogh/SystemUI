package com.android.systemui.qs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.drawable.Animatable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.UiEventLogger;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0015R$string;
import com.android.systemui.Dependency;
import com.android.systemui.FontSizeUtils;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.qs.DetailAdapter;
import com.android.systemui.statusbar.CommandQueue;
import com.oneplus.systemui.util.OpMdmLogger;
import com.oneplus.util.OpUtils;
import com.oneplus.util.ThemeColorUtils;
import java.util.Objects;
public class QSDetail extends LinearLayout {
    private boolean mAnimatingOpen;
    private QSDetailClipper mClipper;
    private boolean mClosingDetail;
    private DetailAdapter mDetailAdapter;
    private ViewGroup mDetailContent;
    protected TextView mDetailDoneButton;
    protected TextView mDetailSettingsButton;
    private final SparseArray<View> mDetailViews = new SparseArray<>();
    private View mFooter;
    private boolean mFullyExpanded;
    private QuickStatusBarHeader mHeader;
    private final AnimatorListenerAdapter mHideGridContentWhenDone = new AnimatorListenerAdapter() { // from class: com.android.systemui.qs.QSDetail.4
        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationCancel(Animator animator) {
            animator.removeListener(this);
            QSDetail.this.mAnimatingOpen = false;
            QSDetail.this.checkPendingAnimations();
        }

        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animator) {
            if (QSDetail.this.mDetailAdapter != null) {
                QSDetail.this.mQsPanel.setGridContentVisibility(false);
                QSDetail.this.mHeader.setVisibility(4);
                QSDetail.this.mFooter.setVisibility(4);
            }
            QSDetail.this.mAnimatingOpen = false;
            QSDetail.this.checkPendingAnimations();
        }
    };
    private int mOpenX;
    private int mOpenY;
    protected View mQsDetailHeader;
    private ImageView mQsDetailHeaderBack;
    protected ImageView mQsDetailHeaderProgress;
    private Switch mQsDetailHeaderSwitch;
    private ViewStub mQsDetailHeaderSwitchStub;
    protected TextView mQsDetailHeaderTitle;
    private QSPanel mQsPanel;
    protected Callback mQsPanelCallback = new Callback() { // from class: com.android.systemui.qs.QSDetail.3
        @Override // com.android.systemui.qs.QSDetail.Callback
        public void onToggleStateChanged(final boolean z) {
            QSDetail.this.post(new Runnable() { // from class: com.android.systemui.qs.QSDetail.3.1
                @Override // java.lang.Runnable
                public void run() {
                    QSDetail qSDetail = QSDetail.this;
                    qSDetail.handleToggleStateChanged(z, qSDetail.mDetailAdapter != null && QSDetail.this.mDetailAdapter.getToggleEnabled());
                }
            });
        }

        @Override // com.android.systemui.qs.QSDetail.Callback
        public void onShowingDetail(final DetailAdapter detailAdapter, final int i, final int i2) {
            Log.d("QSDetail", "onShowingDetail: animatingOpen=" + QSDetail.this.mAnimatingOpen + ", closingDetail=" + QSDetail.this.mClosingDetail);
            if (QSDetail.this.mAnimatingOpen || QSDetail.this.mClosingDetail) {
                Log.d("QSDetail", "Still animating detail, skip this operation:mAnimatingOpen=" + QSDetail.this.mAnimatingOpen + ", mClosingDetail=" + QSDetail.this.mClosingDetail);
                return;
            }
            QSDetail.this.post(new Runnable() { // from class: com.android.systemui.qs.QSDetail.3.2
                @Override // java.lang.Runnable
                public void run() {
                    if (QSDetail.this.isAttachedToWindow()) {
                        QSDetail.this.handleShowingDetail(detailAdapter, i, i2, false);
                    }
                }
            });
        }

        @Override // com.android.systemui.qs.QSDetail.Callback
        public void onScanStateChanged(final boolean z) {
            QSDetail.this.post(new Runnable() { // from class: com.android.systemui.qs.QSDetail.3.3
                @Override // java.lang.Runnable
                public void run() {
                    QSDetail.this.handleScanStateChanged(z);
                }
            });
        }
    };
    private boolean mScanState;
    private boolean mSwitchState;
    private final AnimatorListenerAdapter mTeardownDetailWhenDone = new AnimatorListenerAdapter() { // from class: com.android.systemui.qs.QSDetail.5
        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animator) {
            QSDetail.this.mDetailContent.removeAllViews();
            QSDetail.this.setVisibility(4);
            QSDetail.this.mClosingDetail = false;
        }
    };
    private boolean mTriggeredExpand;
    private final UiEventLogger mUiEventLogger = QSEvents.INSTANCE.getQsUiEventsLogger();

    public interface Callback {
        void onScanStateChanged(boolean z);

        void onShowingDetail(DetailAdapter detailAdapter, int i, int i2);

        void onToggleStateChanged(boolean z);
    }

    public void setHost(QSTileHost qSTileHost) {
    }

    public QSDetail(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        FontSizeUtils.updateFontSize(this.mDetailDoneButton, C0005R$dimen.qs_detail_button_text_size);
        FontSizeUtils.updateFontSize(this.mDetailSettingsButton, C0005R$dimen.qs_detail_button_text_size);
        for (int i = 0; i < this.mDetailViews.size(); i++) {
            this.mDetailViews.valueAt(i).dispatchConfigurationChanged(configuration);
        }
    }

    @Override // android.widget.LinearLayout, android.view.View
    public void onRtlPropertiesChanged(int i) {
        super.onRtlPropertiesChanged(i);
        this.mQsDetailHeaderBack.setRotation(i == 1 ? 180.0f : 0.0f);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mDetailContent = (ViewGroup) findViewById(16908290);
        this.mDetailSettingsButton = (TextView) findViewById(16908314);
        this.mDetailDoneButton = (TextView) findViewById(16908313);
        View findViewById = findViewById(C0008R$id.qs_detail_header);
        this.mQsDetailHeader = findViewById;
        this.mQsDetailHeaderTitle = (TextView) findViewById.findViewById(16908310);
        this.mQsDetailHeaderSwitchStub = (ViewStub) this.mQsDetailHeader.findViewById(C0008R$id.toggle_stub);
        this.mQsDetailHeaderProgress = (ImageView) findViewById(C0008R$id.qs_detail_header_progress);
        updateDetailText();
        if (OpUtils.isREDVersion()) {
            setBackgroundResource(C0006R$drawable.op_qs_red_detail_background);
        }
        this.mClipper = new QSDetailClipper(this);
        AnonymousClass1 r0 = new View.OnClickListener() { // from class: com.android.systemui.qs.QSDetail.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                QSDetail qSDetail = QSDetail.this;
                qSDetail.announceForAccessibility(((LinearLayout) qSDetail).mContext.getString(C0015R$string.accessibility_desc_quick_settings));
                QSDetail.this.mQsPanel.closeDetail();
            }
        };
        this.mDetailDoneButton.setOnClickListener(r0);
        ImageView imageView = (ImageView) this.mQsDetailHeader.findViewById(16909604);
        this.mQsDetailHeaderBack = imageView;
        imageView.setOnClickListener(r0);
        updateThemeColor();
    }

    /* access modifiers changed from: protected */
    public void updateThemeColor() {
        ThemeColorUtils.getColor(100);
        int color = ThemeColorUtils.getColor(1);
        int color2 = ThemeColorUtils.getColor(10);
        int color3 = ThemeColorUtils.getColor(9);
        this.mDetailSettingsButton.setTextColor(color);
        this.mDetailDoneButton.setTextColor(color);
        this.mQsDetailHeaderTitle.setTextColor(color);
        this.mQsDetailHeaderBack.setImageTintList(ColorStateList.valueOf(color2));
        if (!OpUtils.isREDVersion()) {
            setBackgroundTintList(ColorStateList.valueOf(color3));
        }
    }

    public void setQsPanel(QSPanel qSPanel, QuickStatusBarHeader quickStatusBarHeader, View view) {
        this.mQsPanel = qSPanel;
        this.mHeader = quickStatusBarHeader;
        this.mFooter = view;
        quickStatusBarHeader.setCallback(this.mQsPanelCallback);
        this.mQsPanel.setCallback(this.mQsPanelCallback);
    }

    public boolean isShowingDetail() {
        return this.mDetailAdapter != null;
    }

    public void setFullyExpanded(boolean z) {
        if (this.mFullyExpanded != z) {
            Log.d("QSDetail", "setFullyExpanded: " + z);
        }
        this.mFullyExpanded = z;
    }

    public void setExpanded(boolean z) {
        if (!z) {
            this.mTriggeredExpand = false;
        }
    }

    private void updateDetailText() {
        this.mDetailDoneButton.setText(C0015R$string.quick_settings_done);
        this.mDetailSettingsButton.setText(C0015R$string.quick_settings_more_settings);
    }

    public boolean isClosingDetail() {
        return this.mClosingDetail;
    }

    public void handleShowingDetail(DetailAdapter detailAdapter, int i, int i2, boolean z) {
        AnimatorListenerAdapter animatorListenerAdapter;
        boolean z2 = detailAdapter != null;
        setClickable(z2);
        if (z2) {
            setupDetailHeader(detailAdapter);
            if (!z || this.mFullyExpanded) {
                this.mTriggeredExpand = false;
            } else {
                this.mTriggeredExpand = true;
                ((CommandQueue) Dependency.get(CommandQueue.class)).animateExpandSettingsPanel(null);
            }
            this.mOpenX = i;
            this.mOpenY = i2;
        } else {
            i = this.mOpenX;
            i2 = this.mOpenY;
            if (z && this.mTriggeredExpand) {
                ((CommandQueue) Dependency.get(CommandQueue.class)).animateCollapsePanels();
                this.mTriggeredExpand = false;
            }
        }
        boolean z3 = (this.mDetailAdapter != null) != (detailAdapter != null);
        if (z3 || this.mDetailAdapter != detailAdapter) {
            if (detailAdapter != null) {
                int metricsCategory = detailAdapter.getMetricsCategory();
                View createDetailView = detailAdapter.createDetailView(((LinearLayout) this).mContext, this.mDetailViews.get(metricsCategory), this.mDetailContent);
                if (createDetailView != null) {
                    setupDetailFooter(detailAdapter);
                    this.mDetailContent.removeAllViews();
                    this.mDetailContent.addView(createDetailView);
                    this.mDetailViews.put(metricsCategory, createDetailView);
                    ((MetricsLogger) Dependency.get(MetricsLogger.class)).visible(detailAdapter.getMetricsCategory());
                    this.mUiEventLogger.log(detailAdapter.openDetailEvent());
                    announceForAccessibility(((LinearLayout) this).mContext.getString(C0015R$string.accessibility_quick_settings_detail, detailAdapter.getTitle()));
                    this.mDetailAdapter = detailAdapter;
                    animatorListenerAdapter = this.mHideGridContentWhenDone;
                    setVisibility(0);
                } else {
                    throw new IllegalStateException("Must return detail view");
                }
            } else {
                if (this.mDetailAdapter != null) {
                    ((MetricsLogger) Dependency.get(MetricsLogger.class)).hidden(this.mDetailAdapter.getMetricsCategory());
                    this.mUiEventLogger.log(this.mDetailAdapter.closeDetailEvent());
                }
                this.mClosingDetail = true;
                this.mDetailAdapter = null;
                animatorListenerAdapter = this.mTeardownDetailWhenDone;
                this.mHeader.setVisibility(0);
                this.mFooter.setVisibility(0);
                this.mQsPanel.setGridContentVisibility(true);
                this.mQsPanelCallback.onScanStateChanged(false);
            }
            sendAccessibilityEvent(32);
            animateDetailVisibleDiff(i, i2, z3, animatorListenerAdapter);
        }
    }

    /* access modifiers changed from: protected */
    public void animateDetailVisibleDiff(int i, int i2, boolean z, Animator.AnimatorListener animatorListener) {
        if (z) {
            boolean z2 = true;
            this.mAnimatingOpen = this.mDetailAdapter != null;
            if (this.mFullyExpanded || this.mDetailAdapter != null) {
                setAlpha(1.0f);
                QSDetailClipper qSDetailClipper = this.mClipper;
                if (this.mDetailAdapter == null) {
                    z2 = false;
                }
                qSDetailClipper.animateCircularClip(i, i2, z2, animatorListener);
                return;
            }
            animate().alpha(0.0f).setDuration(300).setListener(animatorListener).start();
        }
    }

    /* access modifiers changed from: protected */
    public void setupDetailFooter(DetailAdapter detailAdapter) {
        Intent settingsIntent = detailAdapter.getSettingsIntent();
        this.mDetailSettingsButton.setVisibility(settingsIntent != null ? 0 : 8);
        this.mDetailSettingsButton.setOnClickListener(new View.OnClickListener(detailAdapter, settingsIntent) { // from class: com.android.systemui.qs.-$$Lambda$QSDetail$pzliEYo5cTSq7eIjXDX1Zngi5Yw
            public final /* synthetic */ DetailAdapter f$1;
            public final /* synthetic */ Intent f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                QSDetail.this.lambda$setupDetailFooter$0$QSDetail(this.f$1, this.f$2, view);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setupDetailFooter$0 */
    public /* synthetic */ void lambda$setupDetailFooter$0$QSDetail(DetailAdapter detailAdapter, Intent intent, View view) {
        ((MetricsLogger) Dependency.get(MetricsLogger.class)).action(929, detailAdapter.getMetricsCategory());
        this.mUiEventLogger.log(detailAdapter.moreSettingsEvent());
        ((ActivityStarter) Dependency.get(ActivityStarter.class)).postStartActivityDismissingKeyguard(intent, 0);
        if ("com.oneplus.intent.ACTION_LAUNCH_WLB".equals(intent.getAction())) {
            OpMdmLogger.log("qt_mode_change_menu", "qt_menu_settings", "1", "C22AG9UUDL");
        }
    }

    /* access modifiers changed from: protected */
    public void setupDetailHeader(final DetailAdapter detailAdapter) {
        this.mQsDetailHeaderTitle.setText(detailAdapter.getTitle());
        if (detailAdapter.getMetricsCategory() == 2006) {
            this.mDetailSettingsButton.setText(getContext().getString(C0015R$string.wlb_qs_detail_more_setting));
            this.mDetailSettingsButton.setTag("wlb_settings");
        } else {
            updateDetailText();
            this.mDetailSettingsButton.setTag("");
        }
        Boolean toggleState = detailAdapter.getToggleState();
        if (toggleState == null) {
            Switch r4 = this.mQsDetailHeaderSwitch;
            if (r4 != null) {
                r4.setVisibility(4);
            }
            this.mQsDetailHeader.setClickable(false);
            return;
        }
        if (this.mQsDetailHeaderSwitch == null) {
            this.mQsDetailHeaderSwitch = (Switch) this.mQsDetailHeaderSwitchStub.inflate();
        }
        this.mQsDetailHeaderSwitch.setVisibility(0);
        handleToggleStateChanged(toggleState.booleanValue(), detailAdapter.getToggleEnabled());
        this.mQsDetailHeader.setClickable(true);
        this.mQsDetailHeader.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.qs.QSDetail.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                boolean z = !QSDetail.this.mQsDetailHeaderSwitch.isChecked();
                QSDetail.this.mQsDetailHeaderSwitch.setChecked(z);
                detailAdapter.setToggleState(z);
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleToggleStateChanged(boolean z, boolean z2) {
        this.mSwitchState = z;
        if (!this.mAnimatingOpen) {
            Switch r0 = this.mQsDetailHeaderSwitch;
            if (r0 != null) {
                r0.setChecked(z);
            }
            this.mQsDetailHeader.setEnabled(z2);
            Switch r1 = this.mQsDetailHeaderSwitch;
            if (r1 != null) {
                r1.setEnabled(z2);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleScanStateChanged(boolean z) {
        if (this.mScanState != z) {
            this.mScanState = z;
            Animatable animatable = (Animatable) this.mQsDetailHeaderProgress.getDrawable();
            if (z) {
                this.mQsDetailHeaderProgress.animate().cancel();
                ViewPropertyAnimator alpha = this.mQsDetailHeaderProgress.animate().alpha(1.0f);
                Objects.requireNonNull(animatable);
                alpha.withEndAction(new Runnable(animatable) { // from class: com.android.systemui.qs.-$$Lambda$dWuG3P2xqsast1TFpf_9V5OJbdM
                    public final /* synthetic */ Animatable f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.start();
                    }
                }).start();
                return;
            }
            this.mQsDetailHeaderProgress.animate().cancel();
            ViewPropertyAnimator alpha2 = this.mQsDetailHeaderProgress.animate().alpha(0.0f);
            Objects.requireNonNull(animatable);
            alpha2.withEndAction(new Runnable(animatable) { // from class: com.android.systemui.qs.-$$Lambda$uWzoJtW0gRQtylxIzOBLYDei0eA
                public final /* synthetic */ Animatable f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.stop();
                }
            }).start();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkPendingAnimations() {
        boolean z = this.mSwitchState;
        DetailAdapter detailAdapter = this.mDetailAdapter;
        handleToggleStateChanged(z, detailAdapter != null && detailAdapter.getToggleEnabled());
    }
}
