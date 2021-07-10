package com.android.systemui.qs.customize;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.UiEventLogger;
import com.android.internal.logging.UiEventLoggerImpl;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0011R$layout;
import com.android.systemui.Dependency;
import com.android.systemui.keyguard.ScreenLifecycle;
import com.android.systemui.plugins.qs.QS;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.PageIndicator;
import com.android.systemui.qs.QSDetailClipper;
import com.android.systemui.qs.QSEditEvent;
import com.android.systemui.qs.QSTileHost;
import com.android.systemui.recents.OverviewProxyService;
import com.android.systemui.shared.system.QuickStepContract;
import com.android.systemui.statusbar.phone.LightBarController;
import com.android.systemui.statusbar.phone.NotificationsQuickSettingsContainer;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.oneplus.util.ThemeColorUtils;
import java.util.ArrayList;
public class QSCustomizer extends LinearLayout implements Toolbar.OnMenuItemClickListener {
    private boolean isShown;
    private final QSDetailClipper mClipper;
    private final Animator.AnimatorListener mCollapseAnimationListener = new AnimatorListenerAdapter() { // from class: com.android.systemui.qs.customize.QSCustomizer.4
        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animator) {
            if (!QSCustomizer.this.isShown) {
                QSCustomizer.this.setVisibility(8);
            }
            if (QSCustomizer.this.mNotifQsContainer != null) {
                QSCustomizer.this.mNotifQsContainer.setCustomizerAnimating(false);
            }
            QSCustomizer.this.setCustomizing(false);
        }

        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationCancel(Animator animator) {
            if (!QSCustomizer.this.isShown) {
                QSCustomizer.this.setVisibility(8);
            }
            if (QSCustomizer.this.mNotifQsContainer != null) {
                QSCustomizer.this.mNotifQsContainer.setCustomizerAnimating(false);
            }
            QSCustomizer.this.setCustomizing(false);
        }
    };
    private View mContainer;
    private boolean mCustomizing;
    private View mDivider;
    private TextView mDragLabel;
    private final Animator.AnimatorListener mExpandAnimationListener = new AnimatorListenerAdapter() { // from class: com.android.systemui.qs.customize.QSCustomizer.3
        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animator) {
            if (QSCustomizer.this.isShown) {
                QSCustomizer.this.setCustomizing(true);
            }
            QSCustomizer.this.mOpening = false;
            if (QSCustomizer.this.mNotifQsContainer != null) {
                QSCustomizer.this.mNotifQsContainer.setCustomizerAnimating(false);
            }
            QSCustomizer.this.mPageManager.calculateItemLocation();
        }

        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationCancel(Animator animator) {
            QSCustomizer.this.mOpening = false;
            if (QSCustomizer.this.mNotifQsContainer != null) {
                QSCustomizer.this.mNotifQsContainer.setCustomizerAnimating(false);
            }
            QSCustomizer.this.mPageManager.calculateItemLocation();
        }
    };
    private QSTileHost mHost;
    private boolean mIsShowingNavBackdrop;
    private final KeyguardStateController.Callback mKeyguardCallback = new KeyguardStateController.Callback() { // from class: com.android.systemui.qs.customize.QSCustomizer.2
        @Override // com.android.systemui.statusbar.policy.KeyguardStateController.Callback
        public void onKeyguardShowingChanged() {
            if (QSCustomizer.this.isAttachedToWindow() && QSCustomizer.this.mKeyguardStateController.isShowing() && !QSCustomizer.this.mOpening) {
                QSCustomizer.this.hideNoAnimation();
            }
        }
    };
    private KeyguardStateController mKeyguardStateController;
    private final LightBarController mLightBarController;
    private QSEditViewPager mLowerPages;
    private NotificationsQuickSettingsContainer mNotifQsContainer;
    private boolean mOpening;
    private QSEditPageManager mPageManager;
    private QS mQs;
    private final ScreenLifecycle mScreenLifecycle;
    private final TileQueryHelper mTileQueryHelper;
    private Toolbar mToolbar;
    private UiEventLogger mUiEventLogger = new UiEventLoggerImpl();
    private LinearLayout mUpperIndicatorContainer;
    private QSEditViewPager mUpperPages;
    private int mX;
    private int mY;

    public QSCustomizer(Context context, AttributeSet attributeSet, LightBarController lightBarController, KeyguardStateController keyguardStateController, ScreenLifecycle screenLifecycle, TileQueryHelper tileQueryHelper, UiEventLogger uiEventLogger) {
        super(new ContextThemeWrapper(context, ThemeColorUtils.getEditTheme()), attributeSet);
        LayoutInflater.from(getContext()).inflate(C0011R$layout.qs_customize_panel_content2, this);
        this.mContainer = findViewById(C0008R$id.customize_container);
        this.mClipper = new QSDetailClipper(findViewById(C0008R$id.customize_container));
        Toolbar toolbar = (Toolbar) findViewById(C0008R$id.op_qs_edit_appbar);
        this.mToolbar = toolbar;
        toolbar.setOnMenuItemClickListener(this);
        this.mToolbar.setNavigationOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.qs.customize.-$$Lambda$QSCustomizer$UrjF3azOC-uGdbn9DsJsA7X1eQo
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                QSCustomizer.this.lambda$new$0$QSCustomizer(view);
            }
        });
        this.mUpperPages = (QSEditViewPager) findViewById(C0008R$id.upperPages);
        this.mLowerPages = (QSEditViewPager) findViewById(C0008R$id.lowerPages);
        this.mUpperIndicatorContainer = (LinearLayout) findViewById(C0008R$id.upperIndicatorContainer);
        this.mUpperPages.setPageIndicator((PageIndicator) findViewById(C0008R$id.upperIndicator));
        this.mLowerPages.setPageIndicator((PageIndicator) findViewById(C0008R$id.lowerIndicator));
        TextView textView = (TextView) findViewById(C0008R$id.dragLabel);
        this.mDragLabel = textView;
        QSEditPageManager qSEditPageManager = new QSEditPageManager(context, this.mUpperPages, this.mLowerPages, textView);
        this.mPageManager = qSEditPageManager;
        qSEditPageManager.setLayoutRTL(isLayoutRtl());
        this.mTileQueryHelper = tileQueryHelper;
        tileQueryHelper.setListener(this.mPageManager);
        this.mLightBarController = lightBarController;
        this.mKeyguardStateController = keyguardStateController;
        this.mScreenLifecycle = screenLifecycle;
        updateNavBackDrop(getResources().getConfiguration());
        this.mDivider = findViewById(C0008R$id.divider);
        this.mDragLabel.setOnClickListener(new EmptyClickListener(this));
        setOnClickListener(new EmptyClickListener(this));
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$QSCustomizer(View view) {
        hide();
    }

    public class EmptyClickListener implements View.OnClickListener {
        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
        }

        public EmptyClickListener(QSCustomizer qSCustomizer) {
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mKeyguardStateController.addCallback(this.mKeyguardCallback);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        this.mNotifQsContainer = null;
        this.mKeyguardStateController.removeCallback(this.mKeyguardCallback);
        this.mTileQueryHelper.destroyTiles();
        super.onDetachedFromWindow();
    }

    @Override // android.widget.LinearLayout, android.view.View
    public void onRtlPropertiesChanged(int i) {
        super.onRtlPropertiesChanged(i);
        this.mPageManager.setLayoutRTL(isLayoutRtl());
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        updateThemeColor();
        this.mToolbar.getMenu().add(0, 1, 0, ((LinearLayout) this).mContext.getString(17041143));
    }

    /* access modifiers changed from: protected */
    public void updateThemeColor() {
        int color = ThemeColorUtils.getColor(9);
        ThemeColorUtils.getColor(10);
        ThemeColorUtils.getColor(1);
        int color2 = ThemeColorUtils.getColor(2);
        int color3 = ThemeColorUtils.getColor(15);
        if (ThemeColorUtils.getCurrentTheme() != 2) {
            ThemeColorUtils.getColor(100);
        }
        int color4 = ThemeColorUtils.getColor(14);
        findViewById(C0008R$id.op_qs_edit_appbar).setBackgroundColor(color);
        this.mUpperIndicatorContainer.setBackgroundColor(color);
        setBackgroundTintList(ColorStateList.valueOf(color4));
        this.mContainer.setBackgroundTintList(ColorStateList.valueOf(color4));
        this.mLowerPages.setBackgroundColor(color4);
        this.mUpperPages.setBackgroundColor(color);
        findViewById(C0008R$id.toolbar_panel).setBackgroundColor(color);
        this.mDragLabel.setBackgroundColor(color4);
        this.mDragLabel.setTextColor(color2);
        this.mDivider.setBackgroundColor(color3);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        if (configuration.orientation == 2) {
            hide();
        }
        updateNavBackDrop(configuration);
        this.mTileQueryHelper.recalcEditPage();
    }

    private void updateNavBackDrop(Configuration configuration) {
        View findViewById = findViewById(C0008R$id.nav_bar_background);
        int i = 0;
        boolean z = (configuration.smallestScreenWidthDp >= 600 || configuration.orientation != 2) && !QuickStepContract.isGesturalMode(((OverviewProxyService) Dependency.get(OverviewProxyService.class)).getNavBarMode());
        this.mIsShowingNavBackdrop = z;
        if (findViewById != null) {
            if (!z) {
                i = 8;
            }
            findViewById.setVisibility(i);
        }
        updateNavColors();
    }

    private void updateNavColors() {
        this.mLightBarController.setQsCustomizing(this.mIsShowingNavBackdrop && this.isShown);
    }

    public void setHost(QSTileHost qSTileHost) {
        this.mHost = qSTileHost;
        this.mPageManager.setHost(qSTileHost);
        queryTiles();
    }

    public void setContainer(NotificationsQuickSettingsContainer notificationsQuickSettingsContainer) {
        this.mNotifQsContainer = notificationsQuickSettingsContainer;
    }

    public void setQs(QS qs) {
        this.mQs = qs;
    }

    public void show(int i, int i2) {
        if (!this.isShown) {
            Log.d("QSCustomizer", "show edit UI");
            int[] locationOnScreen = findViewById(C0008R$id.customize_container).getLocationOnScreen();
            this.mX = i - locationOnScreen[0];
            this.mY = i2 - locationOnScreen[1];
            this.mUiEventLogger.log(QSEditEvent.QS_EDIT_OPEN);
            this.isShown = true;
            this.mOpening = true;
            setTileSpecs();
            setVisibility(0);
            this.mClipper.animateCircularClip(this.mX, this.mY, true, this.mExpandAnimationListener);
            queryTiles();
            this.mNotifQsContainer.setCustomizerAnimating(true);
            this.mNotifQsContainer.setCustomizerShowing(true);
            updateNavColors();
        }
    }

    public void showImmediately() {
        if (!this.isShown) {
            setVisibility(0);
            this.mClipper.cancelAnimator();
            this.mClipper.showBackground();
            this.isShown = true;
            setTileSpecs();
            setCustomizing(true);
            queryTiles();
            this.mNotifQsContainer.setCustomizerAnimating(false);
            this.mNotifQsContainer.setCustomizerShowing(true);
            updateNavColors();
        }
    }

    private void queryTiles() {
        this.mTileQueryHelper.queryTiles(this.mHost);
    }

    public void hide() {
        boolean z = this.mScreenLifecycle.getScreenState() != 0;
        if (this.isShown) {
            Log.d("QSCustomizer", "hide edit UI");
            this.mUiEventLogger.log(QSEditEvent.QS_EDIT_CLOSED);
            this.isShown = false;
            this.mToolbar.dismissPopupMenus();
            this.mClipper.cancelAnimator();
            this.mOpening = false;
            setCustomizing(false);
            save();
            if (z) {
                this.mClipper.animateCircularClip(this.mX, this.mY, false, this.mCollapseAnimationListener);
            } else {
                setVisibility(8);
            }
            this.mNotifQsContainer.setCustomizerAnimating(z);
            this.mNotifQsContainer.setCustomizerShowing(false);
            updateNavColors();
            this.mTileQueryHelper.destroyTiles();
        }
    }

    public void hideNoAnimation() {
        MetricsLogger.hidden(getContext(), 358);
        this.isShown = false;
        this.mToolbar.dismissPopupMenus();
        setCustomizing(false);
        save();
        setVisibility(8);
        this.mNotifQsContainer.setCustomizerShowing(false);
        this.mTileQueryHelper.destroyTiles();
    }

    @Override // android.view.View
    public boolean isShown() {
        return this.isShown;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setCustomizing(boolean z) {
        Log.d("QSCustomizer", "setCustomizing=" + z);
        this.mCustomizing = z;
        QS qs = this.mQs;
        if (qs != null) {
            qs.notifyCustomizeChanged();
        }
    }

    public boolean isCustomizing() {
        return this.mCustomizing;
    }

    @Override // androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
    public boolean onMenuItemClick(MenuItem menuItem) {
        if (menuItem.getItemId() != 1) {
            return false;
        }
        this.mUiEventLogger.log(QSEditEvent.QS_EDIT_RESET);
        reset();
        return false;
    }

    private void reset() {
        this.mPageManager.resetTileSpecs(this.mHost, QSTileHost.getDefaultSpecs(((LinearLayout) this).mContext));
    }

    private void setTileSpecs() {
        ArrayList arrayList = new ArrayList();
        for (QSTile qSTile : this.mHost.getTiles()) {
            arrayList.add(qSTile.getTileSpec());
        }
        this.mPageManager.setTileSpecs(arrayList);
    }

    private void save() {
        if (this.mTileQueryHelper.isFinished()) {
            this.mPageManager.saveSpecs(this.mHost);
        }
    }

    public void saveInstanceState(Bundle bundle) {
        if (this.isShown) {
            this.mKeyguardStateController.removeCallback(this.mKeyguardCallback);
        }
        bundle.putBoolean("qs_customizing", this.mCustomizing);
    }

    public void restoreInstanceState(Bundle bundle) {
        boolean z = bundle.getBoolean("qs_customizing");
        if (z != this.mCustomizing) {
            Log.d("QSCustomizer", "customizing " + z + " mCustomizing " + this.mCustomizing);
        } else if (z) {
            setVisibility(0);
            addOnLayoutChangeListener(new View.OnLayoutChangeListener() { // from class: com.android.systemui.qs.customize.QSCustomizer.1
                @Override // android.view.View.OnLayoutChangeListener
                public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                    QSCustomizer.this.removeOnLayoutChangeListener(this);
                    QSCustomizer.this.showImmediately();
                }
            });
        }
    }

    public void setEditLocation(int i, int i2) {
        int[] locationOnScreen = findViewById(C0008R$id.customize_container).getLocationOnScreen();
        this.mX = i - locationOnScreen[0];
        this.mY = i2 - locationOnScreen[1];
    }
}
