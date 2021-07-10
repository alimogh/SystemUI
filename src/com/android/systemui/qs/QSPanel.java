package com.android.systemui.qs;

import android.content.ComponentName;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.metrics.LogMaker;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.UiEventLogger;
import com.android.internal.widget.RemeasuringLinearLayout;
import com.android.systemui.C0004R$color;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0009R$integer;
import com.android.systemui.C0011R$layout;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.media.MediaHost;
import com.android.systemui.plugins.qs.DetailAdapter;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.plugins.qs.QSTileView;
import com.android.systemui.qs.PagedTileLayout;
import com.android.systemui.qs.QSDetail;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.customize.QSCustomizer;
import com.android.systemui.qs.external.CustomTile;
import com.android.systemui.qs.logging.QSLogger;
import com.android.systemui.settings.BrightnessController;
import com.android.systemui.settings.ToggleSeekBar;
import com.android.systemui.settings.ToggleSlider;
import com.android.systemui.settings.ToggleSliderView;
import com.android.systemui.statusbar.policy.BrightnessMirrorController;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.util.Utils;
import com.android.systemui.util.animation.DisappearParameters;
import com.android.systemui.util.animation.UniqueObjectHostView;
import com.oneplus.systemui.qs.OpQSDateTimePanelLayout;
import com.oneplus.systemui.qs.OpQSWidgetAdapter;
import com.oneplus.systemui.util.OpMdmLogger;
import com.oneplus.util.OpUtils;
import com.oneplus.util.ThemeColorUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
public class QSPanel extends LinearLayout implements TunerService.Tunable, QSHost.Callback, BrightnessMirrorController.BrightnessMirrorListener, Dumpable {
    private BrightnessController mBrightnessController;
    private View mBrightnessMirror;
    private BrightnessMirrorController mBrightnessMirrorController;
    protected View mBrightnessView;
    private final BroadcastDispatcher mBroadcastDispatcher;
    private String mCachedSpecs = "";
    private QSDetail.Callback mCallback;
    private int mContentMarginEnd;
    private int mContentMarginStart;
    protected final Context mContext;
    private QSCustomizer mCustomizePanel;
    protected OpQSDateTimePanelLayout mDatePanel;
    private Record mDetailRecord;
    protected View mDivider;
    private final DumpManager mDumpManager;
    protected boolean mExpanded;
    protected View mFooter;
    private int mFooterMarginStartHorizontal;
    private PageIndicator mFooterPageIndicator;
    private boolean mGridContentVisible = true;
    private final H mHandler = new H();
    private ViewGroup mHeaderContainer;
    private LinearLayout mHorizontalContentContainer;
    private LinearLayout mHorizontalLinearLayout;
    private QSTileLayout mHorizontalTileLayout;
    protected QSTileHost mHost;
    private int mLastOrientation = -1;
    protected boolean mListening;
    protected final MediaHost mMediaHost;
    private boolean mMediaLastVisible = false;
    private int mMediaTotalBottomMargin;
    private Consumer<Boolean> mMediaVisibilityChangedListener;
    private final MetricsLogger mMetricsLogger = ((MetricsLogger) Dependency.get(MetricsLogger.class));
    private final int mMovableContentStartIndex;
    private PageIndicator mPanelPageIndicator;
    private final QSLogger mQSLogger;
    private QSTileRevealController mQsTileRevealController;
    protected final ArrayList<TileRecord> mRecords = new ArrayList<>();
    protected QSTileLayout mRegularTileLayout;
    protected QSSecurityFooter mSecurityFooter;
    protected QSTileLayout mTileLayout;
    protected final UiEventLogger mUiEventLogger;
    private boolean mUsingHorizontalLayout;
    protected boolean mUsingMediaPlayer;
    private int mVisualMarginEnd;
    private int mVisualMarginStart;
    private OpQSWidgetAdapter mWidgetAdapter;
    protected View mWidgetLayout;
    private RecyclerView mWidgetListView;

    public interface QSTileLayout {
        void addTile(TileRecord tileRecord);

        int getNumVisibleTiles();

        int getOffsetTop(TileRecord tileRecord);

        void removeTile(TileRecord tileRecord);

        default void restoreInstanceState(Bundle bundle) {
        }

        default void saveInstanceState(Bundle bundle) {
        }

        default void setExpansion(float f) {
        }

        void setListening(boolean z);

        default boolean setMaxColumns(int i) {
            return false;
        }

        default boolean setMinRows(int i) {
            return false;
        }

        boolean updateResources();
    }

    public static final class TileRecord extends Record {
        public QSTile.Callback callback;
        public boolean scanState;
        public QSTile tile;
        public QSTileView tileView;
    }

    /* access modifiers changed from: protected */
    public boolean displayMediaMarginsOnMedia() {
        return true;
    }

    /* access modifiers changed from: protected */
    public String getDumpableTag() {
        return "QSPanel";
    }

    /* access modifiers changed from: protected */
    public boolean needsDynamicRowsAndColumns() {
        return true;
    }

    public QSPanel(Context context, AttributeSet attributeSet, DumpManager dumpManager, BroadcastDispatcher broadcastDispatcher, QSLogger qSLogger, MediaHost mediaHost, UiEventLogger uiEventLogger) {
        super(context, attributeSet);
        this.mUsingMediaPlayer = Utils.useQsMediaPlayer(context);
        this.mMediaTotalBottomMargin = getResources().getDimensionPixelSize(C0005R$dimen.quick_settings_bottom_margin_media);
        this.mMediaHost = mediaHost;
        mediaHost.addVisibilityChangeListener(new Function1() { // from class: com.android.systemui.qs.-$$Lambda$QSPanel$eQ8pVxxhUsNJKcJOLQN4uzlXkuA
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return QSPanel.this.lambda$new$0$QSPanel((Boolean) obj);
            }
        });
        this.mContext = context;
        this.mQSLogger = qSLogger;
        this.mDumpManager = dumpManager;
        this.mBroadcastDispatcher = broadcastDispatcher;
        this.mUiEventLogger = uiEventLogger;
        setOrientation(1);
        addViewsAboveTiles();
        this.mMovableContentStartIndex = getChildCount();
        this.mRegularTileLayout = createRegularTileLayout();
        if (this.mUsingMediaPlayer) {
            RemeasuringLinearLayout remeasuringLinearLayout = new RemeasuringLinearLayout(this.mContext);
            this.mHorizontalLinearLayout = remeasuringLinearLayout;
            remeasuringLinearLayout.setOrientation(0);
            this.mHorizontalLinearLayout.setClipChildren(false);
            this.mHorizontalLinearLayout.setClipToPadding(false);
            RemeasuringLinearLayout remeasuringLinearLayout2 = new RemeasuringLinearLayout(this.mContext);
            this.mHorizontalContentContainer = remeasuringLinearLayout2;
            remeasuringLinearLayout2.setOrientation(1);
            this.mHorizontalContentContainer.setClipChildren(false);
            this.mHorizontalContentContainer.setClipToPadding(false);
            this.mHorizontalTileLayout = createHorizontalTileLayout();
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, -2, 1.0f);
            layoutParams.setMarginStart(0);
            layoutParams.setMarginEnd((int) this.mContext.getResources().getDimension(C0005R$dimen.qqs_media_spacing));
            layoutParams.gravity = 16;
            this.mHorizontalLinearLayout.addView(this.mHorizontalContentContainer, layoutParams);
            addView(this.mHorizontalLinearLayout, new LinearLayout.LayoutParams(-1, 0, 1.0f));
            initMediaHostState();
        }
        PageIndicator pageIndicator = (PageIndicator) LayoutInflater.from(context).inflate(C0011R$layout.qs_page_indicator, (ViewGroup) this, false);
        this.mPanelPageIndicator = pageIndicator;
        ((ViewGroup.MarginLayoutParams) pageIndicator.getLayoutParams()).bottomMargin = getResources().getDimensionPixelSize(C0005R$dimen.op_control_margin_space1);
        addView(this.mPanelPageIndicator);
        addBrightnessView();
        addSecurityFooter();
        QSTileLayout qSTileLayout = this.mRegularTileLayout;
        if (qSTileLayout instanceof PagedTileLayout) {
            this.mQsTileRevealController = new QSTileRevealController(this.mContext, this, (PagedTileLayout) qSTileLayout);
        }
        this.mQSLogger.logAllTilesChangeListening(this.mListening, getDumpableTag(), this.mCachedSpecs);
        updateResources();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ Unit lambda$new$0$QSPanel(Boolean bool) {
        onMediaVisibilityChanged(bool);
        return null;
    }

    /* access modifiers changed from: protected */
    public void onMediaVisibilityChanged(Boolean bool) {
        switchTileLayout();
        Consumer<Boolean> consumer = this.mMediaVisibilityChangedListener;
        if (consumer != null) {
            consumer.accept(bool);
        }
    }

    /* access modifiers changed from: protected */
    public void addSecurityFooter() {
        QSSecurityFooter qSSecurityFooter = new QSSecurityFooter(this, this.mContext);
        this.mSecurityFooter = qSSecurityFooter;
        OpQSWidgetAdapter opQSWidgetAdapter = this.mWidgetAdapter;
        if (opQSWidgetAdapter != null) {
            qSSecurityFooter.setOpQSWidgetAdapter(opQSWidgetAdapter);
        }
    }

    /* access modifiers changed from: protected */
    public void addViewsAboveTiles() {
        OpQSDateTimePanelLayout opQSDateTimePanelLayout = (OpQSDateTimePanelLayout) LayoutInflater.from(this.mContext).inflate(C0011R$layout.op_qs_date_time_panel_layout, (ViewGroup) this, false);
        this.mDatePanel = opQSDateTimePanelLayout;
        addView(opQSDateTimePanelLayout);
        View inflate = LayoutInflater.from(this.mContext).inflate(C0011R$layout.op_qs_widget_layout, (ViewGroup) this, false);
        this.mWidgetLayout = inflate;
        RecyclerView recyclerView = (RecyclerView) inflate.findViewById(C0008R$id.op_qs_widget_list);
        this.mWidgetListView = recyclerView;
        recyclerView.setHasFixedSize(true);
        OpQSWidgetAdapter opQSWidgetAdapter = new OpQSWidgetAdapter(this.mContext);
        this.mWidgetAdapter = opQSWidgetAdapter;
        this.mWidgetListView.setAdapter(opQSWidgetAdapter);
        addView(this.mWidgetLayout);
    }

    /* access modifiers changed from: protected */
    public void addBrightnessView() {
        this.mBrightnessView = LayoutInflater.from(this.mContext).inflate(C0011R$layout.quick_settings_brightness_dialog, (ViewGroup) this, false);
        updateBrightnessViewParams();
        addView(this.mBrightnessView);
        ((ToggleSeekBar) this.mBrightnessView.findViewById(C0008R$id.slider)).setProgressBackgroundTintMode(PorterDuff.Mode.SRC);
        this.mBrightnessController = new BrightnessController(getContext(), (ImageView) findViewById(C0008R$id.brightness_level), (ImageView) findViewById(C0008R$id.brightness_icon), (ToggleSlider) findViewById(C0008R$id.brightness_slider), this.mBroadcastDispatcher);
    }

    /* access modifiers changed from: protected */
    public QSTileLayout createRegularTileLayout() {
        if (this.mRegularTileLayout == null) {
            this.mRegularTileLayout = (QSTileLayout) LayoutInflater.from(this.mContext).inflate(C0011R$layout.qs_paged_tile_layout, (ViewGroup) this, false);
        }
        return this.mRegularTileLayout;
    }

    /* access modifiers changed from: protected */
    public QSTileLayout createHorizontalTileLayout() {
        return createRegularTileLayout();
    }

    /* access modifiers changed from: protected */
    public void initMediaHostState() {
        this.mMediaHost.setExpansion(1.0f);
        this.mMediaHost.setShowsOnlyActiveMedia(false);
        updateMediaDisappearParameters();
        this.mMediaHost.init(0);
    }

    private void updateMediaDisappearParameters() {
        if (this.mUsingMediaPlayer) {
            DisappearParameters disappearParameters = this.mMediaHost.getDisappearParameters();
            if (this.mUsingHorizontalLayout) {
                disappearParameters.getDisappearSize().set(0.0f, 0.4f);
                disappearParameters.getGonePivot().set(1.0f, 1.0f);
                disappearParameters.getContentTranslationFraction().set(0.25f, 1.0f);
                disappearParameters.setDisappearEnd(0.6f);
            } else {
                disappearParameters.getDisappearSize().set(1.0f, 0.0f);
                disappearParameters.getGonePivot().set(0.0f, 1.0f);
                disappearParameters.getContentTranslationFraction().set(0.0f, 1.05f);
                disappearParameters.setDisappearEnd(0.95f);
            }
            disappearParameters.setFadeStartPosition(0.95f);
            disappearParameters.setDisappearStart(0.0f);
            this.mMediaHost.setDisappearParameters(disappearParameters);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.View
    public void onMeasure(int i, int i2) {
        QSTileLayout qSTileLayout = this.mTileLayout;
        if (qSTileLayout instanceof PagedTileLayout) {
            PageIndicator pageIndicator = this.mFooterPageIndicator;
            if (pageIndicator != null) {
                pageIndicator.setNumPages(((PagedTileLayout) qSTileLayout).getNumPages());
            }
            PageIndicator pageIndicator2 = this.mPanelPageIndicator;
            if (pageIndicator2 != null) {
                pageIndicator2.setNumPages(((PagedTileLayout) this.mTileLayout).getNumPages());
            }
            int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(10000, 1073741824);
            ((PagedTileLayout) this.mTileLayout).setExcessHeight(10000 - View.MeasureSpec.getSize(i2));
            i2 = makeMeasureSpec;
        }
        super.onMeasure(i, i2);
        int paddingBottom = getPaddingBottom() + getPaddingTop();
        int childCount = getChildCount();
        for (int i3 = 0; i3 < childCount; i3++) {
            View childAt = getChildAt(i3);
            if (childAt.getVisibility() != 8) {
                ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) childAt.getLayoutParams();
                paddingBottom = paddingBottom + childAt.getMeasuredHeight() + marginLayoutParams.topMargin + marginLayoutParams.bottomMargin;
            }
        }
        setMeasuredDimension(getMeasuredWidth(), paddingBottom);
    }

    public View getPageIndicator() {
        return this.mPanelPageIndicator;
    }

    public QSTileRevealController getQsTileRevealController() {
        return this.mQsTileRevealController;
    }

    public boolean isShowingCustomize() {
        QSCustomizer qSCustomizer = this.mCustomizePanel;
        return qSCustomizer != null && qSCustomizer.isCustomizing();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "qs_show_brightness");
        QSTileHost qSTileHost = this.mHost;
        if (qSTileHost != null) {
            setTiles(qSTileHost.getTiles());
        }
        BrightnessMirrorController brightnessMirrorController = this.mBrightnessMirrorController;
        if (brightnessMirrorController != null) {
            brightnessMirrorController.addCallback((BrightnessMirrorController.BrightnessMirrorListener) this);
        }
        this.mDumpManager.registerDumpable(getDumpableTag(), this);
        OpQSWidgetAdapter opQSWidgetAdapter = this.mWidgetAdapter;
        if (opQSWidgetAdapter != null) {
            opQSWidgetAdapter.setListening(true);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        ((TunerService) Dependency.get(TunerService.class)).removeTunable(this);
        QSTileHost qSTileHost = this.mHost;
        if (qSTileHost != null) {
            qSTileHost.removeCallback(this);
        }
        QSTileLayout qSTileLayout = this.mTileLayout;
        if (qSTileLayout != null) {
            qSTileLayout.setListening(false);
        }
        Iterator<TileRecord> it = this.mRecords.iterator();
        while (it.hasNext()) {
            it.next().tile.removeCallbacks();
        }
        this.mRecords.clear();
        BrightnessMirrorController brightnessMirrorController = this.mBrightnessMirrorController;
        if (brightnessMirrorController != null) {
            brightnessMirrorController.removeCallback((BrightnessMirrorController.BrightnessMirrorListener) this);
        }
        this.mDumpManager.unregisterDumpable(getDumpableTag());
        OpQSWidgetAdapter opQSWidgetAdapter = this.mWidgetAdapter;
        if (opQSWidgetAdapter != null) {
            opQSWidgetAdapter.setListening(false);
        }
        super.onDetachedFromWindow();
    }

    @Override // com.android.systemui.qs.QSHost.Callback
    public void onTilesChanged() {
        setTiles(this.mHost.getTiles());
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        View view;
        if ("qs_show_brightness".equals(str) && (view = this.mBrightnessView) != null) {
            updateViewVisibilityForTuningValue(view, str2);
        }
    }

    private void updateViewVisibilityForTuningValue(View view, String str) {
        view.setVisibility(TunerService.parseIntegerSwitch(str, true) ? 0 : 8);
    }

    public void openDetails(String str) {
        QSTile tile = getTile(str);
        if (tile != null) {
            showDetailAdapter(true, tile.getDetailAdapter(), new int[]{getWidth() / 2, 0});
        }
    }

    private QSTile getTile(String str) {
        for (int i = 0; i < this.mRecords.size(); i++) {
            if (str.equals(this.mRecords.get(i).tile.getTileSpec())) {
                return this.mRecords.get(i).tile;
            }
        }
        return this.mHost.createTile(str);
    }

    public void setBrightnessMirror(BrightnessMirrorController brightnessMirrorController) {
        BrightnessMirrorController brightnessMirrorController2 = this.mBrightnessMirrorController;
        if (brightnessMirrorController2 != null) {
            brightnessMirrorController2.removeCallback((BrightnessMirrorController.BrightnessMirrorListener) this);
        }
        this.mBrightnessMirrorController = brightnessMirrorController;
        if (brightnessMirrorController != null) {
            brightnessMirrorController.addCallback((BrightnessMirrorController.BrightnessMirrorListener) this);
        }
        updateBrightnessMirror();
    }

    @Override // com.android.systemui.statusbar.policy.BrightnessMirrorController.BrightnessMirrorListener
    public void onBrightnessMirrorReinflated(View view) {
        updateBrightnessMirror();
    }

    public void updateThemeColor() {
        int i;
        if (ThemeColorUtils.getCurrentTheme() == 2) {
            i = -1;
        } else {
            i = ThemeColorUtils.getColor(100);
        }
        int color = ThemeColorUtils.getColor(13);
        int color2 = ThemeColorUtils.getColor(10);
        int color3 = ThemeColorUtils.getColor(9);
        int thumbBackground = ThemeColorUtils.getThumbBackground();
        int color4 = ThemeColorUtils.getColor(11);
        if (OpUtils.isREDVersion()) {
            color2 = getContext().getColor(C0004R$color.op_turquoise);
            color4 = color2;
        }
        View view = this.mBrightnessView;
        if (view != null) {
            ToggleSeekBar toggleSeekBar = (ToggleSeekBar) view.findViewById(C0008R$id.slider);
            toggleSeekBar.setThumbTintList(ColorStateList.valueOf(i));
            toggleSeekBar.setProgressTintList(ColorStateList.valueOf(i));
            toggleSeekBar.setProgressBackgroundTintList(ColorStateList.valueOf(color));
            toggleSeekBar.setBackgroundDrawable(null);
            ((ImageView) this.mBrightnessView.findViewById(C0008R$id.brightness_level)).setImageTintList(ColorStateList.valueOf(color4));
            ((ImageView) this.mBrightnessView.findViewById(C0008R$id.brightness_icon)).setImageTintList(ColorStateList.valueOf(color2));
            View view2 = this.mBrightnessMirror;
            if (view2 != null) {
                ToggleSeekBar toggleSeekBar2 = (ToggleSeekBar) view2.findViewById(C0008R$id.slider);
                ((FrameLayout) this.mBrightnessMirror.findViewById(C0008R$id.brightness_mirror_frame)).setBackgroundTintList(ColorStateList.valueOf(color3));
                toggleSeekBar2.setThumbTintList(ColorStateList.valueOf(i));
                toggleSeekBar2.setProgressTintList(ColorStateList.valueOf(i));
                toggleSeekBar2.setProgressBackgroundTintList(ColorStateList.valueOf(color));
                toggleSeekBar2.setBackgroundDrawable(this.mContext.getResources().getDrawable(thumbBackground));
                ((ImageView) this.mBrightnessMirror.findViewById(C0008R$id.brightness_level)).setImageTintList(ColorStateList.valueOf(color4));
                ((ImageView) this.mBrightnessMirror.findViewById(C0008R$id.brightness_icon)).setImageTintList(ColorStateList.valueOf(color2));
            }
        }
        QSSecurityFooter qSSecurityFooter = this.mSecurityFooter;
        if (qSSecurityFooter != null) {
            qSSecurityFooter.updateThemeColor();
        }
    }

    /* access modifiers changed from: package-private */
    public View getBrightnessView() {
        return this.mBrightnessView;
    }

    public void setCallback(QSDetail.Callback callback) {
        this.mCallback = callback;
    }

    public void setHost(QSTileHost qSTileHost, QSCustomizer qSCustomizer) {
        this.mHost = qSTileHost;
        qSTileHost.addCallback(this);
        setTiles(this.mHost.getTiles());
        QSSecurityFooter qSSecurityFooter = this.mSecurityFooter;
        if (qSSecurityFooter != null) {
            qSSecurityFooter.setHostEnvironment(qSTileHost);
        }
        this.mCustomizePanel = qSCustomizer;
        if (qSCustomizer != null) {
            qSCustomizer.setHost(this.mHost);
        }
    }

    private void updatePageIndicator() {
        PageIndicator pageIndicator;
        if ((this.mRegularTileLayout instanceof PagedTileLayout) && (pageIndicator = this.mFooterPageIndicator) != null) {
            pageIndicator.setVisibility(8);
            ((PagedTileLayout) this.mRegularTileLayout).setPageIndicator(this.mPanelPageIndicator);
        }
    }

    public QSTileHost getHost() {
        return this.mHost;
    }

    public void updateResources() {
        Log.d("QSPanel", "updateResources");
        getResources().getDimensionPixelSize(C0005R$dimen.qs_quick_tile_size);
        getResources().getDimensionPixelSize(C0005R$dimen.qs_tile_background_size);
        this.mFooterMarginStartHorizontal = getResources().getDimensionPixelSize(C0005R$dimen.qs_footer_horizontal_margin);
        updatePadding();
        updatePageIndicator();
        int i = 0;
        boolean z = getResources().getConfiguration().orientation == 2;
        OpQSDateTimePanelLayout opQSDateTimePanelLayout = this.mDatePanel;
        if (opQSDateTimePanelLayout != null) {
            opQSDateTimePanelLayout.setVisibility((z || !OpUtils.needLargeQSClock(this.mContext)) ? 8 : 0);
        }
        View view = this.mWidgetLayout;
        if (view != null) {
            if (z) {
                i = 8;
            }
            view.setVisibility(i);
        }
        refreshAllTiles();
        QSTileLayout qSTileLayout = this.mTileLayout;
        if (qSTileLayout != null) {
            qSTileLayout.updateResources();
        }
    }

    /* access modifiers changed from: protected */
    public void updatePadding() {
        Resources resources = this.mContext.getResources();
        int dimensionPixelSize = resources.getDimensionPixelSize(C0005R$dimen.qs_panel_padding_top);
        if (this.mUsingHorizontalLayout) {
            dimensionPixelSize = (int) (((float) dimensionPixelSize) * 0.6f);
            if (resources.getDisplayMetrics().densityDpi >= 510) {
                ((View) this.mTileLayout).setPadding(0, 0, 0, 0);
            }
        }
        setPaddingRelative(getPaddingStart(), dimensionPixelSize, getPaddingEnd(), resources.getDimensionPixelSize(C0005R$dimen.qs_panel_padding_bottom));
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        QSSecurityFooter qSSecurityFooter = this.mSecurityFooter;
        if (qSSecurityFooter != null) {
            qSSecurityFooter.onConfigurationChanged();
        }
        updateResources();
        updateBrightnessMirror();
        int i = configuration.orientation;
        if (i != this.mLastOrientation) {
            this.mLastOrientation = i;
            switchTileLayout(true);
        }
        updateBrightnessViewParams();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mFooter = findViewById(C0008R$id.qs_footer);
        this.mDivider = findViewById(C0008R$id.divider);
        switchTileLayout(true);
    }

    /* access modifiers changed from: package-private */
    public boolean switchTileLayout() {
        return switchTileLayout(false);
    }

    private boolean switchTileLayout(boolean z) {
        QSTileLayout qSTileLayout;
        boolean shouldUseHorizontalLayout = shouldUseHorizontalLayout();
        if (this.mDivider != null) {
            if (shouldUseHorizontalLayout || !this.mUsingMediaPlayer || !this.mMediaHost.getVisible()) {
                this.mDivider.setVisibility(8);
            } else {
                this.mDivider.setVisibility(0);
            }
        }
        boolean visible = this.mMediaHost.getVisible();
        if (shouldUseHorizontalLayout == this.mUsingHorizontalLayout && !z && this.mMediaLastVisible == visible) {
            return false;
        }
        if (OpUtils.DEBUG_ONEPLUS) {
            Log.d("QSPanel", "switchTileLayout: horizontal=" + this.mUsingHorizontalLayout + "->" + shouldUseHorizontalLayout + ", force=" + z + ", mediaVisible=" + this.mMediaLastVisible + "->" + visible);
        }
        this.mMediaLastVisible = visible;
        this.mUsingHorizontalLayout = shouldUseHorizontalLayout;
        View view = shouldUseHorizontalLayout ? this.mHorizontalLinearLayout : (View) this.mRegularTileLayout;
        View view2 = shouldUseHorizontalLayout ? (View) this.mRegularTileLayout : this.mHorizontalLinearLayout;
        LinearLayout linearLayout = shouldUseHorizontalLayout ? this.mHorizontalContentContainer : this;
        QSTileLayout qSTileLayout2 = shouldUseHorizontalLayout ? this.mHorizontalTileLayout : this.mRegularTileLayout;
        if (!(view2 == null || ((qSTileLayout = this.mRegularTileLayout) == this.mHorizontalTileLayout && view2 == qSTileLayout))) {
            view2.setVisibility(8);
        }
        view.setVisibility(0);
        switchAllContentToParent(linearLayout, qSTileLayout2);
        reAttachMediaHost();
        QSTileLayout qSTileLayout3 = this.mTileLayout;
        if (qSTileLayout3 != null) {
            qSTileLayout3.setListening(false);
            Iterator<TileRecord> it = this.mRecords.iterator();
            while (it.hasNext()) {
                TileRecord next = it.next();
                this.mTileLayout.removeTile(next);
                next.tile.removeCallback(next.callback);
            }
        }
        this.mTileLayout = qSTileLayout2;
        if (qSTileLayout2 instanceof PagedTileLayout) {
            ((PagedTileLayout) qSTileLayout2).setPageIndicator(this.mPanelPageIndicator);
            ((PagedTileLayout) this.mTileLayout).setIsUseMediaLayout(shouldUseHorizontalLayout);
        }
        QSTileHost qSTileHost = this.mHost;
        if (qSTileHost != null) {
            setTiles(qSTileHost.getTiles());
        }
        qSTileLayout2.setListening(this.mListening);
        if (needsDynamicRowsAndColumns()) {
            if (shouldUseHorizontalLayout || !this.mUsingMediaPlayer || visible) {
                qSTileLayout2.setMinRows(shouldUseHorizontalLayout ? 2 : 1);
            } else {
                qSTileLayout2.setMinRows(this.mContext.getResources().getInteger(C0009R$integer.quick_settings_max_rows));
            }
            qSTileLayout2.setMaxColumns(shouldUseHorizontalLayout ? 3 : 100);
        }
        updateTileLayoutMargins();
        updateFooterMargin();
        updateDividerMargin();
        updateMediaDisappearParameters();
        updateMediaHostContentMargins();
        updateHorizontalLinearLayoutMargins();
        updatePadding();
        return true;
    }

    private void updateHorizontalLinearLayoutMargins() {
        if (this.mHorizontalLinearLayout != null && !displayMediaMarginsOnMedia()) {
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.mHorizontalLinearLayout.getLayoutParams();
            layoutParams.bottomMargin = this.mMediaTotalBottomMargin - getPaddingBottom();
            this.mHorizontalLinearLayout.setLayoutParams(layoutParams);
        }
    }

    private void switchAllContentToParent(ViewGroup viewGroup, QSTileLayout qSTileLayout) {
        ViewGroup viewGroup2;
        int i = viewGroup == this ? this.mMovableContentStartIndex : 0;
        if (viewGroup instanceof QuickQSPanel) {
            i = 1;
        }
        switchToParent((View) qSTileLayout, viewGroup, i);
        int i2 = i + 1;
        QSSecurityFooter qSSecurityFooter = this.mSecurityFooter;
        if (qSSecurityFooter != null) {
            View view = qSSecurityFooter.getView();
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
            if (!this.mUsingHorizontalLayout || (viewGroup2 = this.mHeaderContainer) == null) {
                if (getResources().getConfiguration().orientation == 1) {
                    layoutParams.width = -1;
                    i2 += 3;
                } else {
                    layoutParams.width = -2;
                }
                layoutParams.weight = 0.0f;
                switchToParent(view, viewGroup, i2);
                i2++;
            } else {
                layoutParams.width = 0;
                layoutParams.weight = 1.6f;
                switchToParent(view, viewGroup2, 1);
            }
            view.setLayoutParams(layoutParams);
        }
        View view2 = this.mFooter;
        if (view2 != null) {
            switchToParent(view2, viewGroup, i2);
        }
    }

    private void switchToParent(View view, ViewGroup viewGroup, int i) {
        ViewGroup viewGroup2 = (ViewGroup) view.getParent();
        if (viewGroup2 != viewGroup || viewGroup2.indexOfChild(view) != i) {
            if (viewGroup2 != null) {
                viewGroup2.removeView(view);
            }
            try {
                viewGroup.addView(view, i);
            } catch (Exception e) {
                Log.d("QSPanel", "switchToParent: exception caught.", e);
                viewGroup.addView(view);
            }
        }
    }

    private boolean shouldUseHorizontalLayout() {
        return this.mUsingMediaPlayer && this.mMediaHost.getVisible() && getResources().getConfiguration().orientation == 2;
    }

    /* access modifiers changed from: protected */
    public void reAttachMediaHost() {
        if (this.mUsingMediaPlayer) {
            boolean shouldUseHorizontalLayout = shouldUseHorizontalLayout();
            UniqueObjectHostView hostView = this.mMediaHost.getHostView();
            LinearLayout linearLayout = shouldUseHorizontalLayout ? this.mHorizontalLinearLayout : this;
            ViewGroup viewGroup = (ViewGroup) hostView.getParent();
            if (viewGroup != linearLayout) {
                if (viewGroup != null) {
                    viewGroup.removeView(hostView);
                }
                linearLayout.addView(hostView);
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) hostView.getLayoutParams();
                layoutParams.height = -2;
                int i = 0;
                layoutParams.width = shouldUseHorizontalLayout ? 0 : -1;
                layoutParams.weight = shouldUseHorizontalLayout ? 1.4f : 0.0f;
                if (!shouldUseHorizontalLayout || displayMediaMarginsOnMedia()) {
                    i = this.mMediaTotalBottomMargin - getPaddingBottom();
                }
                layoutParams.bottomMargin = i;
            }
        }
    }

    public void updateBrightnessMirror() {
        if (this.mBrightnessMirrorController != null) {
            ToggleSliderView toggleSliderView = (ToggleSliderView) findViewById(C0008R$id.brightness_slider);
            toggleSliderView.setMirror((ToggleSliderView) this.mBrightnessMirrorController.getMirror().findViewById(C0008R$id.brightness_slider));
            toggleSliderView.setMirrorController(this.mBrightnessMirrorController);
            if (this.mBrightnessController != null) {
                View mirror = this.mBrightnessMirrorController.getMirror();
                this.mBrightnessMirror = mirror;
                this.mBrightnessController.setMirrorView(mirror);
                ((ToggleSeekBar) this.mBrightnessMirror.findViewById(C0008R$id.slider)).setProgressBackgroundTintMode(PorterDuff.Mode.SRC);
            }
        }
        updateThemeColor();
    }

    public void setExpanded(boolean z) {
        if (this.mExpanded != z) {
            Log.d("QSPanel", "setExpanded: " + this.mExpanded + "->" + z);
            this.mQSLogger.logPanelExpanded(z, getDumpableTag());
            this.mExpanded = z;
            if (!z) {
                QSTileLayout qSTileLayout = this.mTileLayout;
                if (qSTileLayout instanceof PagedTileLayout) {
                    ((PagedTileLayout) qSTileLayout).setCurrentItem(0, false);
                }
            }
            this.mMetricsLogger.visibility(111, this.mExpanded);
            if (!this.mExpanded) {
                this.mUiEventLogger.log(closePanelEvent());
                QSCustomizer qSCustomizer = this.mCustomizePanel;
                if (qSCustomizer == null || !qSCustomizer.isShown()) {
                    showDetail(false, this.mDetailRecord);
                } else {
                    this.mCustomizePanel.hideNoAnimation();
                }
            } else {
                this.mUiEventLogger.log(openPanelEvent());
                logTiles();
            }
        }
    }

    public void setPageListener(PagedTileLayout.PageListener pageListener) {
        QSTileLayout qSTileLayout = this.mTileLayout;
        if (qSTileLayout instanceof PagedTileLayout) {
            ((PagedTileLayout) qSTileLayout).setPageListener(pageListener);
        }
    }

    public boolean isExpanded() {
        return this.mExpanded;
    }

    public void setListening(boolean z) {
        if (this.mListening != z) {
            Log.d("QSPanel", "setListening: " + this.mListening + "->" + z);
            this.mListening = z;
            if (this.mTileLayout != null) {
                this.mQSLogger.logAllTilesChangeListening(z, getDumpableTag(), this.mCachedSpecs);
                this.mTileLayout.setListening(z);
            }
            if (this.mListening) {
                refreshAllTiles();
            }
        }
    }

    private String getTilesSpecs() {
        return (String) this.mRecords.stream().map($$Lambda$QSPanel$EbHBtJlVwGzmqefWXJDEYuyGlcQ.INSTANCE).collect(Collectors.joining(","));
    }

    public void setListening(boolean z, boolean z2) {
        setListening(z && z2);
        switchTileLayout();
        QSSecurityFooter qSSecurityFooter = this.mSecurityFooter;
        if (qSSecurityFooter != null) {
            qSSecurityFooter.setListening(z);
        }
        setBrightnessListening(z);
    }

    public void setBrightnessListening(boolean z) {
        if (OpUtils.DEBUG_ONEPLUS) {
            Log.d("QSPanel", "setListeningBrightness: " + z);
        }
        BrightnessController brightnessController = this.mBrightnessController;
        if (brightnessController != null) {
            if (z) {
                brightnessController.registerCallbacks();
            } else {
                brightnessController.unregisterCallbacks();
            }
        }
    }

    public void refreshAllTiles() {
        Log.d("QSPanel", "refreshAllTiles");
        BrightnessController brightnessController = this.mBrightnessController;
        if (brightnessController != null) {
            brightnessController.checkRestrictionAndSetEnabled();
        }
        Iterator<TileRecord> it = this.mRecords.iterator();
        while (it.hasNext()) {
            it.next().tile.refreshState();
        }
        QSSecurityFooter qSSecurityFooter = this.mSecurityFooter;
        if (qSSecurityFooter != null) {
            qSSecurityFooter.refreshState();
        }
    }

    public void showDetailAdapter(boolean z, DetailAdapter detailAdapter, int[] iArr) {
        int i = iArr[0];
        int i2 = iArr[1];
        ((View) getParent()).getLocationInWindow(iArr);
        Record record = new Record();
        record.detailAdapter = detailAdapter;
        record.x = i - iArr[0];
        record.y = i2 - iArr[1];
        iArr[0] = i;
        iArr[1] = i2;
        showDetail(z, record);
    }

    /* access modifiers changed from: protected */
    public void showDetail(boolean z, Record record) {
        this.mHandler.obtainMessage(1, z ? 1 : 0, 0, record).sendToTarget();
    }

    public void setTiles(Collection<QSTile> collection) {
        setTiles(collection, false);
    }

    public void setTiles(Collection<QSTile> collection, boolean z) {
        if (!z) {
            this.mQsTileRevealController.updateRevealedTiles(collection);
        }
        Iterator<TileRecord> it = this.mRecords.iterator();
        while (it.hasNext()) {
            TileRecord next = it.next();
            this.mTileLayout.removeTile(next);
            next.tile.removeCallback(next.callback);
        }
        this.mRecords.clear();
        this.mCachedSpecs = "";
        for (QSTile qSTile : collection) {
            addTile(qSTile, z);
        }
    }

    /* access modifiers changed from: protected */
    public void drawTile(TileRecord tileRecord, QSTile.State state) {
        tileRecord.tileView.onStateChanged(state);
    }

    /* access modifiers changed from: protected */
    public QSTileView createTileView(QSTile qSTile, boolean z) {
        return this.mHost.createTileView(qSTile, z);
    }

    /* access modifiers changed from: protected */
    public QSEvent openPanelEvent() {
        return QSEvent.QS_PANEL_EXPANDED;
    }

    /* access modifiers changed from: protected */
    public QSEvent closePanelEvent() {
        return QSEvent.QS_PANEL_COLLAPSED;
    }

    /* access modifiers changed from: protected */
    public boolean shouldShowDetail() {
        return this.mExpanded;
    }

    /* access modifiers changed from: protected */
    public TileRecord addTile(QSTile qSTile, boolean z) {
        final TileRecord tileRecord = new TileRecord();
        tileRecord.tile = qSTile;
        tileRecord.tileView = createTileView(qSTile, z);
        AnonymousClass1 r2 = new QSTile.Callback() { // from class: com.android.systemui.qs.QSPanel.1
            @Override // com.android.systemui.plugins.qs.QSTile.Callback
            public void onStateChanged(QSTile.State state) {
                QSPanel.this.drawTile(tileRecord, state);
            }

            @Override // com.android.systemui.plugins.qs.QSTile.Callback
            public void onShowDetail(boolean z2) {
                if (QSPanel.this.shouldShowDetail()) {
                    QSPanel.this.showDetail(z2, tileRecord);
                }
            }

            @Override // com.android.systemui.plugins.qs.QSTile.Callback
            public void onToggleStateChanged(boolean z2) {
                if (QSPanel.this.mDetailRecord == tileRecord) {
                    QSPanel.this.fireToggleStateChanged(z2);
                }
            }

            @Override // com.android.systemui.plugins.qs.QSTile.Callback
            public void onScanStateChanged(boolean z2) {
                tileRecord.scanState = z2;
                Record record = QSPanel.this.mDetailRecord;
                TileRecord tileRecord2 = tileRecord;
                if (record == tileRecord2) {
                    QSPanel.this.fireScanStateChanged(tileRecord2.scanState);
                }
            }

            @Override // com.android.systemui.plugins.qs.QSTile.Callback
            public void onAnnouncementRequested(CharSequence charSequence) {
                if (charSequence != null) {
                    QSPanel.this.mHandler.obtainMessage(3, charSequence).sendToTarget();
                }
            }
        };
        tileRecord.tile.addCallback(r2);
        tileRecord.callback = r2;
        tileRecord.tileView.init(tileRecord.tile);
        tileRecord.tile.refreshState();
        this.mRecords.add(tileRecord);
        this.mCachedSpecs = getTilesSpecs();
        QSTileLayout qSTileLayout = this.mTileLayout;
        if (qSTileLayout != null) {
            qSTileLayout.addTile(tileRecord);
        }
        return tileRecord;
    }

    public void showEdit(final View view) {
        OpMdmLogger.log("quick_edit", "", "1");
        view.post(new Runnable() { // from class: com.android.systemui.qs.QSPanel.2
            @Override // java.lang.Runnable
            public void run() {
                if (QSPanel.this.mCustomizePanel != null && !QSPanel.this.mCustomizePanel.isCustomizing()) {
                    int[] locationOnScreen = view.getLocationOnScreen();
                    QSPanel.this.mCustomizePanel.show(locationOnScreen[0] + (view.getWidth() / 2), locationOnScreen[1] + (view.getHeight() / 2));
                }
            }
        });
    }

    public void closeDetail() {
        QSCustomizer qSCustomizer = this.mCustomizePanel;
        if (qSCustomizer == null || !qSCustomizer.isShown()) {
            showDetail(false, this.mDetailRecord);
        } else {
            this.mCustomizePanel.hide();
        }
    }

    /* access modifiers changed from: protected */
    public void handleShowDetail(Record record, boolean z) {
        int i;
        if (record instanceof TileRecord) {
            handleShowDetailTile((TileRecord) record, z);
            return;
        }
        int i2 = 0;
        if (record != null) {
            i2 = record.x;
            i = record.y;
        } else {
            i = 0;
        }
        handleShowDetailImpl(record, z, i2, i);
    }

    private void handleShowDetailTile(TileRecord tileRecord, boolean z) {
        if ((this.mDetailRecord != null) != z || this.mDetailRecord != tileRecord) {
            if (z) {
                DetailAdapter detailAdapter = tileRecord.tile.getDetailAdapter();
                tileRecord.detailAdapter = detailAdapter;
                if (detailAdapter == null) {
                    return;
                }
            }
            tileRecord.tile.setDetailListening(z);
            handleShowDetailImpl(tileRecord, z, tileRecord.tileView.getLeft() + (tileRecord.tileView.getWidth() / 2), tileRecord.tileView.getDetailY() + this.mTileLayout.getOffsetTop(tileRecord) + getTop());
        }
    }

    private void handleShowDetailImpl(Record record, boolean z, int i, int i2) {
        DetailAdapter detailAdapter = null;
        setDetailRecord(z ? record : null);
        if (z) {
            detailAdapter = record.detailAdapter;
        }
        fireShowingDetail(detailAdapter, i, i2);
    }

    /* access modifiers changed from: protected */
    public void setDetailRecord(Record record) {
        if (record != this.mDetailRecord) {
            this.mDetailRecord = record;
            fireScanStateChanged((record instanceof TileRecord) && ((TileRecord) record).scanState);
        }
    }

    /* access modifiers changed from: package-private */
    public void setGridContentVisibility(boolean z) {
        int i = z ? 0 : 4;
        setVisibility(i);
        if (this.mGridContentVisible != z) {
            this.mMetricsLogger.visibility(111, i);
        }
        this.mGridContentVisible = z;
    }

    private void logTiles() {
        for (int i = 0; i < this.mRecords.size(); i++) {
            QSTile qSTile = this.mRecords.get(i).tile;
            this.mMetricsLogger.write(qSTile.populate(new LogMaker(qSTile.getMetricsCategory()).setType(1)));
        }
    }

    private void fireShowingDetail(DetailAdapter detailAdapter, int i, int i2) {
        QSDetail.Callback callback = this.mCallback;
        if (callback != null) {
            callback.onShowingDetail(detailAdapter, i, i2);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void fireToggleStateChanged(boolean z) {
        QSDetail.Callback callback = this.mCallback;
        if (callback != null) {
            callback.onToggleStateChanged(z);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void fireScanStateChanged(boolean z) {
        QSDetail.Callback callback = this.mCallback;
        if (callback != null) {
            callback.onScanStateChanged(z);
        }
    }

    public void clickTile(ComponentName componentName) {
        String spec = CustomTile.toSpec(componentName);
        int size = this.mRecords.size();
        for (int i = 0; i < size; i++) {
            if (this.mRecords.get(i).tile.getTileSpec().equals(spec)) {
                this.mRecords.get(i).tile.click();
                return;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public QSTileLayout getTileLayout() {
        return this.mTileLayout;
    }

    /* access modifiers changed from: package-private */
    public QSTileView getTileView(QSTile qSTile) {
        Iterator<TileRecord> it = this.mRecords.iterator();
        while (it.hasNext()) {
            TileRecord next = it.next();
            if (next.tile == qSTile) {
                return next.tileView;
            }
        }
        return null;
    }

    public QSSecurityFooter getSecurityFooter() {
        return this.mSecurityFooter;
    }

    public View getDivider() {
        return this.mDivider;
    }

    public void showDeviceMonitoringDialog() {
        QSSecurityFooter qSSecurityFooter = this.mSecurityFooter;
        if (qSSecurityFooter != null) {
            qSSecurityFooter.showDeviceMonitoringDialog();
        }
    }

    public void setContentMargins(int i, int i2) {
        this.mContentMarginStart = 0;
        this.mContentMarginEnd = 0;
        updateTileLayoutMargins(0, 0);
        updateMediaHostContentMargins();
        updateFooterMargin();
        updateDividerMargin();
    }

    private void updateFooterMargin() {
        if (this.mFooter != null) {
            updateMargins(this.mFooter, this.mUsingHorizontalLayout ? this.mFooterMarginStartHorizontal : 0, 0);
        }
    }

    private void updateTileLayoutMargins(int i, int i2) {
        this.mVisualMarginStart = i;
        this.mVisualMarginEnd = i2;
        updateTileLayoutMargins();
    }

    private void updateTileLayoutMargins() {
        int i = this.mVisualMarginEnd;
        if (this.mUsingHorizontalLayout) {
            i = 0;
            if (getResources().getDisplayMetrics().densityDpi >= 510) {
                updateMargins((View) this.mTileLayout, this.mVisualMarginStart, getResources().getDimensionPixelSize(C0005R$dimen.op_qs_paged_tile_layout_margin_top), 0, 0);
            }
        }
        updateMargins((View) this.mTileLayout, this.mVisualMarginStart, i);
    }

    /* access modifiers changed from: protected */
    public void updateMargins(View view, int i, int i2, int i3, int i4) {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
        layoutParams.setMargins(i, i2, i3, i4);
        view.setLayoutParams(layoutParams);
    }

    private void updateDividerMargin() {
        View view = this.mDivider;
        if (view != null) {
            updateMargins(view, this.mContentMarginStart, this.mContentMarginEnd);
        }
    }

    /* access modifiers changed from: protected */
    public void updateMediaHostContentMargins() {
        if (this.mUsingMediaPlayer) {
            int dimensionPixelSize = getResources().getDimensionPixelSize(C0005R$dimen.op_control_margin_space4);
            int dimensionPixelSize2 = getResources().getDimensionPixelSize(C0005R$dimen.op_control_margin_space4);
            if (this.mUsingHorizontalLayout) {
                dimensionPixelSize = 0;
                dimensionPixelSize2 = getResources().getDimensionPixelSize(C0005R$dimen.qs_tile_layout_margin_side);
            }
            updateMargins(this.mMediaHost.getHostView(), dimensionPixelSize, dimensionPixelSize2);
        }
    }

    /* access modifiers changed from: protected */
    public void updateMargins(View view, int i, int i2) {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
        layoutParams.setMarginStart(i);
        layoutParams.setMarginEnd(i2);
        view.setLayoutParams(layoutParams);
    }

    public MediaHost getMediaHost() {
        return this.mMediaHost;
    }

    public void setHeaderContainer(ViewGroup viewGroup) {
        this.mHeaderContainer = viewGroup;
    }

    public void setMediaVisibilityChangedListener(Consumer<Boolean> consumer) {
        this.mMediaVisibilityChangedListener = consumer;
    }

    /* access modifiers changed from: private */
    public class H extends Handler {
        private H() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            int i = message.what;
            boolean z = true;
            if (i == 1) {
                QSPanel qSPanel = QSPanel.this;
                Record record = (Record) message.obj;
                if (message.arg1 == 0) {
                    z = false;
                }
                qSPanel.handleShowDetail(record, z);
            } else if (i == 3) {
                QSPanel.this.announceForAccessibility((CharSequence) message.obj);
            }
        }
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println(getClass().getSimpleName() + ":");
        printWriter.println("  Tile records:");
        Iterator<TileRecord> it = this.mRecords.iterator();
        while (it.hasNext()) {
            TileRecord next = it.next();
            if (next.tile instanceof Dumpable) {
                printWriter.print("    ");
                ((Dumpable) next.tile).dump(fileDescriptor, printWriter, strArr);
                printWriter.print("    ");
                printWriter.println(next.tileView.toString());
            }
        }
    }

    /* access modifiers changed from: protected */
    public static class Record {
        DetailAdapter detailAdapter;
        int x;
        int y;

        protected Record() {
        }
    }

    public void setIsExpanding(boolean z) {
        QSSecurityFooter qSSecurityFooter = this.mSecurityFooter;
        if (qSSecurityFooter != null) {
            qSSecurityFooter.setIsExpanding(z);
        }
    }

    public OpQSWidgetAdapter getOpQSWidgetAdapter() {
        return this.mWidgetAdapter;
    }

    private void updateBrightnessViewParams() {
        if (this.mBrightnessView != null) {
            int dimensionPixelSize = getResources().getDimensionPixelSize(C0005R$dimen.brightness_mirror_height);
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) this.mBrightnessView.getLayoutParams();
            marginLayoutParams.height = dimensionPixelSize;
            this.mBrightnessView.setLayoutParams(marginLayoutParams);
            forceLayout();
        }
    }
}
