package com.android.systemui.media;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Log;
import android.util.MathUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.android.systemui.C0004R$color;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0011R$layout;
import com.android.systemui.media.MediaDataManager;
import com.android.systemui.media.MediaHostStatesManager;
import com.android.systemui.media.PlayerViewHolder;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.qs.PageIndicator;
import com.android.systemui.statusbar.notification.VisualStabilityManager;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.util.Utils;
import com.android.systemui.util.animation.MeasurementInput;
import com.android.systemui.util.animation.TransitionLayout;
import com.android.systemui.util.animation.UniqueObjectHostView;
import com.android.systemui.util.animation.UniqueObjectHostViewKt;
import com.android.systemui.util.concurrency.DelayableExecutor;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import javax.inject.Provider;
import kotlin.TypeCastException;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Reflection;
import kotlin.jvm.internal.TypeIntrinsics;
import kotlin.reflect.KDeclarationContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: MediaCarouselController.kt */
public final class MediaCarouselController {
    private final ActivityStarter activityStarter;
    private int carouselMeasureHeight;
    private int carouselMeasureWidth;
    private final MediaCarouselController$configListener$1 configListener = new ConfigurationController.ConfigurationListener(this) { // from class: com.android.systemui.media.MediaCarouselController$configListener$1
        final /* synthetic */ MediaCarouselController this$0;

        /* JADX WARN: Incorrect args count in method signature: ()V */
        {
            this.this$0 = r1;
        }

        @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
        public void onDensityOrFontScaleChanged() {
            this.this$0.recreatePlayers();
            this.this$0.inflateSettingsButton();
        }

        @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
        public void onOverlayChanged() {
            this.this$0.inflateSettingsButton();
        }

        @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
        public void onConfigChanged(@Nullable Configuration configuration) {
            if (configuration != null) {
                MediaCarouselController mediaCarouselController = this.this$0;
                boolean z = true;
                if (configuration.getLayoutDirection() != 1) {
                    z = false;
                }
                mediaCarouselController.setRtl(z);
            }
        }
    };
    private final Context context;
    private int currentCarouselHeight;
    private int currentCarouselWidth;
    private int currentEndLocation = -1;
    private int currentStartLocation = -1;
    private float currentTransitionProgress = 1.0f;
    private boolean currentlyExpanded = true;
    private boolean currentlyShowingOnlyActive;
    private MediaHostState desiredHostState;
    private int desiredLocation = -1;
    private boolean isRtl;
    private final MediaScrollView mediaCarousel;
    private final MediaCarouselScrollHandler mediaCarouselScrollHandler;
    private final ViewGroup mediaContent;
    private final Provider<MediaControlPanel> mediaControlPanelFactory;
    private final Map<String, MediaData> mediaData = new LinkedHashMap();
    @NotNull
    private final ViewGroup mediaFrame;
    private final MediaHostStatesManager mediaHostStatesManager;
    @NotNull
    private final Map<String, MediaControlPanel> mediaPlayers = new LinkedHashMap();
    private boolean needsReordering;
    private final PageIndicator pageIndicator;
    private boolean playersVisible;
    private View settingsButton;
    private final VisualStabilityManager.Callback visualStabilityCallback;
    private final VisualStabilityManager visualStabilityManager;

    public MediaCarouselController(@NotNull Context context, @NotNull Provider<MediaControlPanel> provider, @NotNull VisualStabilityManager visualStabilityManager, @NotNull MediaHostStatesManager mediaHostStatesManager, @NotNull ActivityStarter activityStarter, @NotNull DelayableExecutor delayableExecutor, @NotNull MediaDataFilter mediaDataFilter, @NotNull ConfigurationController configurationController, @NotNull FalsingManager falsingManager) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(provider, "mediaControlPanelFactory");
        Intrinsics.checkParameterIsNotNull(visualStabilityManager, "visualStabilityManager");
        Intrinsics.checkParameterIsNotNull(mediaHostStatesManager, "mediaHostStatesManager");
        Intrinsics.checkParameterIsNotNull(activityStarter, "activityStarter");
        Intrinsics.checkParameterIsNotNull(delayableExecutor, "executor");
        Intrinsics.checkParameterIsNotNull(mediaDataFilter, "mediaManager");
        Intrinsics.checkParameterIsNotNull(configurationController, "configurationController");
        Intrinsics.checkParameterIsNotNull(falsingManager, "falsingManager");
        this.context = context;
        this.mediaControlPanelFactory = provider;
        this.visualStabilityManager = visualStabilityManager;
        this.mediaHostStatesManager = mediaHostStatesManager;
        this.activityStarter = activityStarter;
        ViewGroup inflateMediaCarousel = inflateMediaCarousel();
        this.mediaFrame = inflateMediaCarousel;
        View requireViewById = inflateMediaCarousel.requireViewById(C0008R$id.media_carousel_scroller);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById, "mediaFrame.requireViewBy….media_carousel_scroller)");
        this.mediaCarousel = (MediaScrollView) requireViewById;
        View requireViewById2 = this.mediaFrame.requireViewById(C0008R$id.media_page_indicator);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById2, "mediaFrame.requireViewBy….id.media_page_indicator)");
        PageIndicator pageIndicator = (PageIndicator) requireViewById2;
        this.pageIndicator = pageIndicator;
        pageIndicator.setColor(this.context.getResources().getColor(C0004R$color.op_control_text_color_primary_dark));
        this.mediaCarouselScrollHandler = new MediaCarouselScrollHandler(this.mediaCarousel, this.pageIndicator, delayableExecutor, new Function0<Unit>(mediaDataFilter) { // from class: com.android.systemui.media.MediaCarouselController.1
            @Override // kotlin.jvm.internal.CallableReference
            public final String getName() {
                return "onSwipeToDismiss";
            }

            @Override // kotlin.jvm.internal.CallableReference
            public final KDeclarationContainer getOwner() {
                return Reflection.getOrCreateKotlinClass(MediaDataFilter.class);
            }

            @Override // kotlin.jvm.internal.CallableReference
            public final String getSignature() {
                return "onSwipeToDismiss()V";
            }

            @Override // kotlin.jvm.functions.Function0
            public final void invoke() {
                ((MediaDataFilter) this.receiver).onSwipeToDismiss();
            }
        }, new Function0<Unit>(this) { // from class: com.android.systemui.media.MediaCarouselController.2
            @Override // kotlin.jvm.internal.CallableReference
            public final String getName() {
                return "updatePageIndicatorLocation";
            }

            @Override // kotlin.jvm.internal.CallableReference
            public final KDeclarationContainer getOwner() {
                return Reflection.getOrCreateKotlinClass(MediaCarouselController.class);
            }

            @Override // kotlin.jvm.internal.CallableReference
            public final String getSignature() {
                return "updatePageIndicatorLocation()V";
            }

            @Override // kotlin.jvm.functions.Function0
            public final void invoke() {
                ((MediaCarouselController) this.receiver).updatePageIndicatorLocation();
            }
        }, falsingManager);
        Resources resources = this.context.getResources();
        Intrinsics.checkExpressionValueIsNotNull(resources, "context.resources");
        Configuration configuration = resources.getConfiguration();
        Intrinsics.checkExpressionValueIsNotNull(configuration, "context.resources.configuration");
        setRtl(configuration.getLayoutDirection() == 1);
        inflateSettingsButton();
        View requireViewById3 = this.mediaCarousel.requireViewById(C0008R$id.media_carousel);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById3, "mediaCarousel.requireViewById(R.id.media_carousel)");
        this.mediaContent = (ViewGroup) requireViewById3;
        configurationController.addCallback(this.configListener);
        AnonymousClass3 r2 = new VisualStabilityManager.Callback(this) { // from class: com.android.systemui.media.MediaCarouselController.3
            final /* synthetic */ MediaCarouselController this$0;

            {
                this.this$0 = r1;
            }

            @Override // com.android.systemui.statusbar.notification.VisualStabilityManager.Callback
            public final void onChangeAllowed() {
                if (this.this$0.needsReordering) {
                    this.this$0.needsReordering = false;
                    this.this$0.reorderAllPlayers();
                }
                this.this$0.mediaCarouselScrollHandler.scrollToStart();
            }
        };
        this.visualStabilityCallback = r2;
        this.visualStabilityManager.addReorderingAllowedCallback(r2, true);
        mediaDataFilter.addListener(new MediaDataManager.Listener(this) { // from class: com.android.systemui.media.MediaCarouselController.4
            final /* synthetic */ MediaCarouselController this$0;

            /* JADX WARN: Incorrect args count in method signature: ()V */
            {
                this.this$0 = r1;
            }

            @Override // com.android.systemui.media.MediaDataManager.Listener
            public void onMediaDataLoaded(@NotNull String str, @Nullable String str2, @NotNull MediaData mediaData) {
                Intrinsics.checkParameterIsNotNull(str, "key");
                Intrinsics.checkParameterIsNotNull(mediaData, "data");
                if (str2 != null) {
                    MediaData mediaData2 = (MediaData) this.this$0.mediaData.remove(str2);
                }
                if (mediaData.getActive() || Utils.useMediaResumption(this.this$0.context)) {
                    this.this$0.mediaData.put(str, mediaData);
                    this.this$0.addOrUpdatePlayer(str, str2, mediaData);
                    return;
                }
                onMediaDataRemoved(str);
            }

            @Override // com.android.systemui.media.MediaDataManager.Listener
            public void onMediaDataRemoved(@NotNull String str) {
                Intrinsics.checkParameterIsNotNull(str, "key");
                this.this$0.mediaData.remove(str);
                this.this$0.removePlayer(str);
            }
        });
        this.mediaFrame.addOnLayoutChangeListener(new View.OnLayoutChangeListener(this) { // from class: com.android.systemui.media.MediaCarouselController.5
            final /* synthetic */ MediaCarouselController this$0;

            {
                this.this$0 = r1;
            }

            @Override // android.view.View.OnLayoutChangeListener
            public final void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                this.this$0.updatePageIndicatorLocation();
            }
        });
        this.mediaHostStatesManager.addCallback(new MediaHostStatesManager.Callback(this) { // from class: com.android.systemui.media.MediaCarouselController.6
            final /* synthetic */ MediaCarouselController this$0;

            /* JADX WARN: Incorrect args count in method signature: ()V */
            {
                this.this$0 = r1;
            }

            @Override // com.android.systemui.media.MediaHostStatesManager.Callback
            public void onHostStateChanged(int i, @NotNull MediaHostState mediaHostState) {
                Intrinsics.checkParameterIsNotNull(mediaHostState, "mediaHostState");
                if (i == this.this$0.desiredLocation) {
                    MediaCarouselController mediaCarouselController = this.this$0;
                    MediaCarouselController.onDesiredLocationChanged$default(mediaCarouselController, mediaCarouselController.desiredLocation, mediaHostState, false, 0, 0, 24, null);
                }
            }
        });
    }

    @NotNull
    public final ViewGroup getMediaFrame() {
        return this.mediaFrame;
    }

    /* access modifiers changed from: private */
    public final void setRtl(boolean z) {
        if (z != this.isRtl) {
            this.isRtl = z;
            this.mediaFrame.setLayoutDirection(z ? 1 : 0);
            this.mediaCarouselScrollHandler.scrollToStart();
        }
    }

    private final void setCurrentlyExpanded(boolean z) {
        if (this.currentlyExpanded != z) {
            this.currentlyExpanded = z;
            for (MediaControlPanel mediaControlPanel : this.mediaPlayers.values()) {
                mediaControlPanel.setListening(this.currentlyExpanded);
            }
        }
    }

    /* access modifiers changed from: private */
    public final void inflateSettingsButton() {
        View inflate = LayoutInflater.from(this.context).inflate(C0011R$layout.media_carousel_settings_button, this.mediaFrame, false);
        if (inflate != null) {
            View view = this.settingsButton;
            if (view != null) {
                ViewGroup viewGroup = this.mediaFrame;
                if (view != null) {
                    viewGroup.removeView(view);
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("settingsButton");
                    throw null;
                }
            }
            this.settingsButton = inflate;
            ViewGroup viewGroup2 = this.mediaFrame;
            if (inflate != null) {
                viewGroup2.addView(inflate);
                this.mediaCarouselScrollHandler.onSettingsButtonUpdated(inflate);
                View view2 = this.settingsButton;
                if (view2 != null) {
                    view2.setOnClickListener(new View.OnClickListener(this) { // from class: com.android.systemui.media.MediaCarouselController$inflateSettingsButton$2
                        final /* synthetic */ MediaCarouselController this$0;

                        {
                            this.this$0 = r1;
                        }

                        @Override // android.view.View.OnClickListener
                        public final void onClick(View view3) {
                            this.this$0.activityStarter.startActivity(MediaCarouselControllerKt.access$getSettingsIntent$p(), true);
                        }
                    });
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("settingsButton");
                    throw null;
                }
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("settingsButton");
                throw null;
            }
        } else {
            throw new TypeCastException("null cannot be cast to non-null type android.view.View");
        }
    }

    private final ViewGroup inflateMediaCarousel() {
        View inflate = LayoutInflater.from(this.context).inflate(C0011R$layout.media_carousel, (ViewGroup) new UniqueObjectHostView(this.context), false);
        if (inflate != null) {
            ViewGroup viewGroup = (ViewGroup) inflate;
            viewGroup.setLayoutDirection(3);
            return viewGroup;
        }
        throw new TypeCastException("null cannot be cast to non-null type android.view.ViewGroup");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void reorderAllPlayers() {
        for (MediaControlPanel mediaControlPanel : this.mediaPlayers.values()) {
            PlayerViewHolder view = mediaControlPanel.getView();
            TransitionLayout player = view != null ? view.getPlayer() : null;
            if (mediaControlPanel.isPlaying() && this.mediaContent.indexOfChild(player) != 0) {
                this.mediaContent.removeView(player);
                this.mediaContent.addView(player, 0);
            }
        }
        this.mediaCarouselScrollHandler.onPlayersChanged();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void addOrUpdatePlayer(String str, String str2, MediaData mediaData) {
        TransitionLayout player;
        TransitionLayout transitionLayout = null;
        if (this.mediaPlayers.get(str2) != null) {
            Map<String, MediaControlPanel> map = this.mediaPlayers;
            if (map != null) {
                MediaControlPanel mediaControlPanel = (MediaControlPanel) TypeIntrinsics.asMutableMap(map).remove(str2);
                Map<String, MediaControlPanel> map2 = this.mediaPlayers;
                if (mediaControlPanel == null) {
                    Intrinsics.throwNpe();
                    throw null;
                } else if (map2.put(str, mediaControlPanel) != null) {
                    Log.wtf("MediaCarouselController", "new key " + str + " already exists when migrating from " + str2);
                }
            } else {
                throw new TypeCastException("null cannot be cast to non-null type kotlin.collections.MutableMap<K, V>");
            }
        }
        MediaControlPanel mediaControlPanel2 = this.mediaPlayers.get(str);
        if (mediaControlPanel2 == null) {
            MediaControlPanel mediaControlPanel3 = this.mediaControlPanelFactory.get();
            PlayerViewHolder.Companion companion = PlayerViewHolder.Companion;
            LayoutInflater from = LayoutInflater.from(this.context);
            Intrinsics.checkExpressionValueIsNotNull(from, "LayoutInflater.from(context)");
            mediaControlPanel3.attach(companion.create(from, this.mediaContent));
            Intrinsics.checkExpressionValueIsNotNull(mediaControlPanel3, "existingPlayer");
            mediaControlPanel3.getMediaViewController().setSizeChangedListener(new Function0<Unit>(this) { // from class: com.android.systemui.media.MediaCarouselController$addOrUpdatePlayer$2
                @Override // kotlin.jvm.internal.CallableReference
                public final String getName() {
                    return "updateCarouselDimensions";
                }

                @Override // kotlin.jvm.internal.CallableReference
                public final KDeclarationContainer getOwner() {
                    return Reflection.getOrCreateKotlinClass(MediaCarouselController.class);
                }

                @Override // kotlin.jvm.internal.CallableReference
                public final String getSignature() {
                    return "updateCarouselDimensions()V";
                }

                @Override // kotlin.jvm.functions.Function0
                public final void invoke() {
                    ((MediaCarouselController) this.receiver).updateCarouselDimensions();
                }
            });
            this.mediaPlayers.put(str, mediaControlPanel3);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
            PlayerViewHolder view = mediaControlPanel3.getView();
            if (!(view == null || (player = view.getPlayer()) == null)) {
                player.setLayoutParams(layoutParams);
            }
            mediaControlPanel3.bind(mediaData);
            mediaControlPanel3.setListening(this.currentlyExpanded);
            updatePlayerToState(mediaControlPanel3, true);
            if (mediaControlPanel3.isPlaying()) {
                ViewGroup viewGroup = this.mediaContent;
                PlayerViewHolder view2 = mediaControlPanel3.getView();
                if (view2 != null) {
                    transitionLayout = view2.getPlayer();
                }
                viewGroup.addView(transitionLayout, 0);
            } else {
                ViewGroup viewGroup2 = this.mediaContent;
                PlayerViewHolder view3 = mediaControlPanel3.getView();
                if (view3 != null) {
                    transitionLayout = view3.getPlayer();
                }
                viewGroup2.addView(transitionLayout);
            }
        } else {
            mediaControlPanel2.bind(mediaData);
            if (mediaControlPanel2.isPlaying()) {
                ViewGroup viewGroup3 = this.mediaContent;
                PlayerViewHolder view4 = mediaControlPanel2.getView();
                if (viewGroup3.indexOfChild(view4 != null ? view4.getPlayer() : null) != 0) {
                    if (this.visualStabilityManager.isReorderingAllowed()) {
                        ViewGroup viewGroup4 = this.mediaContent;
                        PlayerViewHolder view5 = mediaControlPanel2.getView();
                        viewGroup4.removeView(view5 != null ? view5.getPlayer() : null);
                        ViewGroup viewGroup5 = this.mediaContent;
                        PlayerViewHolder view6 = mediaControlPanel2.getView();
                        if (view6 != null) {
                            transitionLayout = view6.getPlayer();
                        }
                        viewGroup5.addView(transitionLayout, 0);
                    } else {
                        this.needsReordering = true;
                    }
                }
            }
        }
        updatePageIndicator();
        this.mediaCarouselScrollHandler.onPlayersChanged();
        UniqueObjectHostViewKt.setRequiresRemeasuring(this.mediaCarousel, true);
        if (this.mediaPlayers.size() != this.mediaContent.getChildCount()) {
            Log.wtf("MediaCarouselController", "Size of players list and number of views in carousel are out of sync");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void removePlayer(String str) {
        MediaControlPanel remove = this.mediaPlayers.remove(str);
        if (remove != null) {
            this.mediaCarouselScrollHandler.onPrePlayerRemoved(remove);
            ViewGroup viewGroup = this.mediaContent;
            PlayerViewHolder view = remove.getView();
            viewGroup.removeView(view != null ? view.getPlayer() : null);
            remove.onDestroy();
            this.mediaCarouselScrollHandler.onPlayersChanged();
            updatePageIndicator();
        }
    }

    /* access modifiers changed from: private */
    public final void recreatePlayers() {
        this.mediaData.forEach(new BiConsumer<String, MediaData>(this) { // from class: com.android.systemui.media.MediaCarouselController$recreatePlayers$1
            final /* synthetic */ MediaCarouselController this$0;

            {
                this.this$0 = r1;
            }

            public final void accept(@NotNull String str, @NotNull MediaData mediaData) {
                Intrinsics.checkParameterIsNotNull(str, "key");
                Intrinsics.checkParameterIsNotNull(mediaData, "data");
                this.this$0.removePlayer(str);
                this.this$0.addOrUpdatePlayer(str, null, mediaData);
            }
        });
    }

    private final void updatePageIndicator() {
        int childCount = this.mediaContent.getChildCount();
        this.pageIndicator.setNumPages(childCount, -1);
        if (childCount == 1) {
            this.pageIndicator.setLocation(0.0f);
        }
        updatePageIndicatorAlpha();
    }

    public final void setCurrentState(int i, int i2, float f, boolean z) {
        if (!(i == this.currentStartLocation && i2 == this.currentEndLocation && f == this.currentTransitionProgress && !z)) {
            this.currentStartLocation = i;
            this.currentEndLocation = i2;
            this.currentTransitionProgress = f;
            for (MediaControlPanel mediaControlPanel : this.mediaPlayers.values()) {
                updatePlayerToState(mediaControlPanel, z);
            }
            maybeResetSettingsCog();
            updatePageIndicatorAlpha();
        }
    }

    private final void updatePageIndicatorAlpha() {
        Map<Integer, MediaHostState> mediaHostStates = this.mediaHostStatesManager.getMediaHostStates();
        MediaHostState mediaHostState = mediaHostStates.get(Integer.valueOf(this.currentEndLocation));
        boolean z = false;
        boolean visible = mediaHostState != null ? mediaHostState.getVisible() : false;
        MediaHostState mediaHostState2 = mediaHostStates.get(Integer.valueOf(this.currentStartLocation));
        if (mediaHostState2 != null) {
            z = mediaHostState2.getVisible();
        }
        float f = 1.0f;
        float f2 = z ? 1.0f : 0.0f;
        float f3 = visible ? 1.0f : 0.0f;
        if (!visible || !z) {
            float f4 = this.currentTransitionProgress;
            if (!visible) {
                f4 = 1.0f - f4;
            }
            f = MathUtils.lerp(f2, f3, MathUtils.constrain(MathUtils.map(0.95f, 1.0f, 0.0f, 1.0f, f4), 0.0f, 1.0f));
        }
        this.pageIndicator.setAlpha(f);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void updatePageIndicatorLocation() {
        int i;
        int i2;
        if (this.isRtl) {
            i2 = this.pageIndicator.getWidth();
            i = this.currentCarouselWidth;
        } else {
            i2 = this.currentCarouselWidth;
            i = this.pageIndicator.getWidth();
        }
        this.pageIndicator.setTranslationX((((float) (i2 - i)) / 2.0f) + this.mediaCarouselScrollHandler.getContentTranslation());
        ViewGroup.LayoutParams layoutParams = this.pageIndicator.getLayoutParams();
        if (layoutParams != null) {
            PageIndicator pageIndicator = this.pageIndicator;
            pageIndicator.setTranslationY((float) ((this.currentCarouselHeight - pageIndicator.getHeight()) - ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin));
            return;
        }
        throw new TypeCastException("null cannot be cast to non-null type android.view.ViewGroup.MarginLayoutParams");
    }

    /* access modifiers changed from: private */
    public final void updateCarouselDimensions() {
        int i = 0;
        int i2 = 0;
        for (MediaControlPanel mediaControlPanel : this.mediaPlayers.values()) {
            MediaViewController mediaViewController = mediaControlPanel.getMediaViewController();
            Intrinsics.checkExpressionValueIsNotNull(mediaViewController, "mediaPlayer.mediaViewController");
            i = Math.max(i, mediaViewController.getCurrentWidth() + ((int) mediaViewController.getTranslationX()));
            i2 = Math.max(i2, mediaViewController.getCurrentHeight() + ((int) mediaViewController.getTranslationY()));
        }
        if (!(i == this.currentCarouselWidth && i2 == this.currentCarouselHeight)) {
            this.currentCarouselWidth = i;
            this.currentCarouselHeight = i2;
            this.mediaCarouselScrollHandler.setCarouselBounds(i, i2);
            updatePageIndicatorLocation();
        }
    }

    private final void maybeResetSettingsCog() {
        Map<Integer, MediaHostState> mediaHostStates = this.mediaHostStatesManager.getMediaHostStates();
        MediaHostState mediaHostState = mediaHostStates.get(Integer.valueOf(this.currentEndLocation));
        boolean showsOnlyActiveMedia = mediaHostState != null ? mediaHostState.getShowsOnlyActiveMedia() : true;
        MediaHostState mediaHostState2 = mediaHostStates.get(Integer.valueOf(this.currentStartLocation));
        boolean showsOnlyActiveMedia2 = mediaHostState2 != null ? mediaHostState2.getShowsOnlyActiveMedia() : showsOnlyActiveMedia;
        if (this.currentlyShowingOnlyActive == showsOnlyActiveMedia) {
            float f = this.currentTransitionProgress;
            if (f == 1.0f || f == 0.0f || showsOnlyActiveMedia2 == showsOnlyActiveMedia) {
                return;
            }
        }
        this.currentlyShowingOnlyActive = showsOnlyActiveMedia;
        this.mediaCarouselScrollHandler.resetTranslation(true);
    }

    private final void updatePlayerToState(MediaControlPanel mediaControlPanel, boolean z) {
        mediaControlPanel.getMediaViewController().setCurrentState(this.currentStartLocation, this.currentEndLocation, this.currentTransitionProgress, z);
    }

    public static /* synthetic */ void onDesiredLocationChanged$default(MediaCarouselController mediaCarouselController, int i, MediaHostState mediaHostState, boolean z, long j, long j2, int i2, Object obj) {
        mediaCarouselController.onDesiredLocationChanged(i, mediaHostState, z, (i2 & 8) != 0 ? 200 : j, (i2 & 16) != 0 ? 0 : j2);
    }

    public final void onDesiredLocationChanged(int i, @Nullable MediaHostState mediaHostState, boolean z, long j, long j2) {
        if (mediaHostState != null) {
            this.desiredLocation = i;
            this.desiredHostState = mediaHostState;
            setCurrentlyExpanded(mediaHostState.getExpansion() > ((float) 0));
            for (MediaControlPanel mediaControlPanel : this.mediaPlayers.values()) {
                if (z) {
                    mediaControlPanel.getMediaViewController().animatePendingStateChange(j, j2);
                }
                mediaControlPanel.getMediaViewController().onLocationPreChange(i);
            }
            this.mediaCarouselScrollHandler.setShowsSettingsButton(!mediaHostState.getShowsOnlyActiveMedia());
            this.mediaCarouselScrollHandler.setFalsingProtectionNeeded(mediaHostState.getFalsingProtectionNeeded());
            boolean visible = mediaHostState.getVisible();
            if (visible != this.playersVisible) {
                this.playersVisible = visible;
                if (visible) {
                    MediaCarouselScrollHandler.resetTranslation$default(this.mediaCarouselScrollHandler, false, 1, null);
                }
            }
            updateCarouselSize();
        }
    }

    private final void updateCarouselSize() {
        MeasurementInput measurementInput;
        MeasurementInput measurementInput2;
        MeasurementInput measurementInput3;
        MeasurementInput measurementInput4;
        MediaHostState mediaHostState = this.desiredHostState;
        int width = (mediaHostState == null || (measurementInput4 = mediaHostState.getMeasurementInput()) == null) ? 0 : measurementInput4.getWidth();
        MediaHostState mediaHostState2 = this.desiredHostState;
        int height = (mediaHostState2 == null || (measurementInput3 = mediaHostState2.getMeasurementInput()) == null) ? 0 : measurementInput3.getHeight();
        if ((width != this.carouselMeasureWidth && width != 0) || (height != this.carouselMeasureHeight && height != 0)) {
            this.carouselMeasureWidth = width;
            this.carouselMeasureHeight = height;
            int dimensionPixelSize = this.context.getResources().getDimensionPixelSize(C0005R$dimen.qs_media_padding) + width;
            MediaHostState mediaHostState3 = this.desiredHostState;
            int widthMeasureSpec = (mediaHostState3 == null || (measurementInput2 = mediaHostState3.getMeasurementInput()) == null) ? 0 : measurementInput2.getWidthMeasureSpec();
            MediaHostState mediaHostState4 = this.desiredHostState;
            this.mediaCarousel.measure(widthMeasureSpec, (mediaHostState4 == null || (measurementInput = mediaHostState4.getMeasurementInput()) == null) ? 0 : measurementInput.getHeightMeasureSpec());
            MediaScrollView mediaScrollView = this.mediaCarousel;
            mediaScrollView.layout(0, 0, width, mediaScrollView.getMeasuredHeight());
            this.mediaCarouselScrollHandler.setPlayerWidthPlusPadding(dimensionPixelSize);
        }
    }
}
