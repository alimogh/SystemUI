package com.android.systemui.media;

import android.graphics.Rect;
import android.util.ArraySet;
import android.view.View;
import com.android.systemui.statusbar.NotificationMediaManager;
import com.android.systemui.util.animation.DisappearParameters;
import com.android.systemui.util.animation.MeasurementInput;
import com.android.systemui.util.animation.MeasurementOutput;
import com.android.systemui.util.animation.UniqueObjectHostView;
import java.util.Iterator;
import java.util.Objects;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: MediaHost.kt */
public final class MediaHost implements MediaHostState {
    @NotNull
    private final Rect currentBounds = new Rect();
    @NotNull
    public UniqueObjectHostView hostView;
    private final MediaHost$listener$1 listener = new MediaHost$listener$1(this);
    private int location = -1;
    private final MediaDataFilter mediaDataFilter;
    private final MediaHierarchyManager mediaHierarchyManager;
    private final MediaHostStatesManager mediaHostStatesManager;
    private final NotificationMediaManager notificationMediaManager;
    private final MediaHostStateHolder state;
    private final int[] tmpLocationOnScreen = {0, 0};
    private ArraySet<Function1<Boolean, Unit>> visibleChangedListeners = new ArraySet<>();

    @Override // com.android.systemui.media.MediaHostState
    @NotNull
    public MediaHostState copy() {
        return this.state.copy();
    }

    @Override // com.android.systemui.media.MediaHostState
    @NotNull
    public DisappearParameters getDisappearParameters() {
        return this.state.getDisappearParameters();
    }

    @Override // com.android.systemui.media.MediaHostState
    public float getExpansion() {
        return this.state.getExpansion();
    }

    @Override // com.android.systemui.media.MediaHostState
    public boolean getFalsingProtectionNeeded() {
        return this.state.getFalsingProtectionNeeded();
    }

    @Override // com.android.systemui.media.MediaHostState
    @Nullable
    public MeasurementInput getMeasurementInput() {
        return this.state.getMeasurementInput();
    }

    @Override // com.android.systemui.media.MediaHostState
    public boolean getShowsOnlyActiveMedia() {
        return this.state.getShowsOnlyActiveMedia();
    }

    @Override // com.android.systemui.media.MediaHostState
    public boolean getVisible() {
        return this.state.getVisible();
    }

    public void setDisappearParameters(@NotNull DisappearParameters disappearParameters) {
        Intrinsics.checkParameterIsNotNull(disappearParameters, "<set-?>");
        this.state.setDisappearParameters(disappearParameters);
    }

    @Override // com.android.systemui.media.MediaHostState
    public void setExpansion(float f) {
        this.state.setExpansion(f);
    }

    public void setFalsingProtectionNeeded(boolean z) {
        this.state.setFalsingProtectionNeeded(z);
    }

    public void setShowsOnlyActiveMedia(boolean z) {
        this.state.setShowsOnlyActiveMedia(z);
    }

    public void setVisible(boolean z) {
        this.state.setVisible(z);
    }

    public MediaHost(@NotNull MediaHostStateHolder mediaHostStateHolder, @NotNull MediaHierarchyManager mediaHierarchyManager, @NotNull MediaDataFilter mediaDataFilter, @NotNull MediaHostStatesManager mediaHostStatesManager, @NotNull NotificationMediaManager notificationMediaManager) {
        Intrinsics.checkParameterIsNotNull(mediaHostStateHolder, "state");
        Intrinsics.checkParameterIsNotNull(mediaHierarchyManager, "mediaHierarchyManager");
        Intrinsics.checkParameterIsNotNull(mediaDataFilter, "mediaDataFilter");
        Intrinsics.checkParameterIsNotNull(mediaHostStatesManager, "mediaHostStatesManager");
        Intrinsics.checkParameterIsNotNull(notificationMediaManager, "notificationMediaManager");
        this.state = mediaHostStateHolder;
        this.mediaHierarchyManager = mediaHierarchyManager;
        this.mediaDataFilter = mediaDataFilter;
        this.mediaHostStatesManager = mediaHostStatesManager;
        this.notificationMediaManager = notificationMediaManager;
    }

    @NotNull
    public final UniqueObjectHostView getHostView() {
        UniqueObjectHostView uniqueObjectHostView = this.hostView;
        if (uniqueObjectHostView != null) {
            return uniqueObjectHostView;
        }
        Intrinsics.throwUninitializedPropertyAccessException("hostView");
        throw null;
    }

    public final void setHostView(@NotNull UniqueObjectHostView uniqueObjectHostView) {
        Intrinsics.checkParameterIsNotNull(uniqueObjectHostView, "<set-?>");
        this.hostView = uniqueObjectHostView;
    }

    public final int getLocation() {
        return this.location;
    }

    @NotNull
    public final Rect getCurrentBounds() {
        UniqueObjectHostView uniqueObjectHostView = this.hostView;
        if (uniqueObjectHostView != null) {
            uniqueObjectHostView.getLocationOnScreen(this.tmpLocationOnScreen);
            int i = 0;
            int i2 = this.tmpLocationOnScreen[0];
            UniqueObjectHostView uniqueObjectHostView2 = this.hostView;
            if (uniqueObjectHostView2 != null) {
                int paddingLeft = i2 + uniqueObjectHostView2.getPaddingLeft();
                int i3 = this.tmpLocationOnScreen[1];
                UniqueObjectHostView uniqueObjectHostView3 = this.hostView;
                if (uniqueObjectHostView3 != null) {
                    int paddingTop = i3 + uniqueObjectHostView3.getPaddingTop();
                    int i4 = this.tmpLocationOnScreen[0];
                    UniqueObjectHostView uniqueObjectHostView4 = this.hostView;
                    if (uniqueObjectHostView4 != null) {
                        int width = i4 + uniqueObjectHostView4.getWidth();
                        UniqueObjectHostView uniqueObjectHostView5 = this.hostView;
                        if (uniqueObjectHostView5 != null) {
                            int paddingRight = width - uniqueObjectHostView5.getPaddingRight();
                            int i5 = this.tmpLocationOnScreen[1];
                            UniqueObjectHostView uniqueObjectHostView6 = this.hostView;
                            if (uniqueObjectHostView6 != null) {
                                int height = i5 + uniqueObjectHostView6.getHeight();
                                UniqueObjectHostView uniqueObjectHostView7 = this.hostView;
                                if (uniqueObjectHostView7 != null) {
                                    int paddingBottom = height - uniqueObjectHostView7.getPaddingBottom();
                                    if (paddingRight < paddingLeft) {
                                        paddingLeft = 0;
                                        paddingRight = 0;
                                    }
                                    if (paddingBottom < paddingTop) {
                                        paddingBottom = 0;
                                    } else {
                                        i = paddingTop;
                                    }
                                    this.currentBounds.set(paddingLeft, i, paddingRight, paddingBottom);
                                    return this.currentBounds;
                                }
                                Intrinsics.throwUninitializedPropertyAccessException("hostView");
                                throw null;
                            }
                            Intrinsics.throwUninitializedPropertyAccessException("hostView");
                            throw null;
                        }
                        Intrinsics.throwUninitializedPropertyAccessException("hostView");
                        throw null;
                    }
                    Intrinsics.throwUninitializedPropertyAccessException("hostView");
                    throw null;
                }
                Intrinsics.throwUninitializedPropertyAccessException("hostView");
                throw null;
            }
            Intrinsics.throwUninitializedPropertyAccessException("hostView");
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("hostView");
        throw null;
    }

    public final void addVisibilityChangeListener(@NotNull Function1<? super Boolean, Unit> function1) {
        Intrinsics.checkParameterIsNotNull(function1, "listener");
        this.visibleChangedListeners.add(function1);
    }

    public final void init(int i) {
        this.location = i;
        UniqueObjectHostView register = this.mediaHierarchyManager.register(this);
        this.hostView = register;
        if (register != null) {
            register.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener(this) { // from class: com.android.systemui.media.MediaHost$init$1
                final /* synthetic */ MediaHost this$0;

                /* JADX WARN: Incorrect args count in method signature: ()V */
                {
                    this.this$0 = r1;
                }

                @Override // android.view.View.OnAttachStateChangeListener
                public void onViewAttachedToWindow(@Nullable View view) {
                    this.this$0.mediaDataFilter.addListener(this.this$0.listener);
                    this.this$0.updateViewVisibility();
                }

                @Override // android.view.View.OnAttachStateChangeListener
                public void onViewDetachedFromWindow(@Nullable View view) {
                    this.this$0.mediaDataFilter.removeListener(this.this$0.listener);
                }
            });
            UniqueObjectHostView uniqueObjectHostView = this.hostView;
            if (uniqueObjectHostView != null) {
                uniqueObjectHostView.setMeasurementManager(new UniqueObjectHostView.MeasurementManager(this, i) { // from class: com.android.systemui.media.MediaHost$init$2
                    final /* synthetic */ int $location;
                    final /* synthetic */ MediaHost this$0;

                    {
                        this.this$0 = r1;
                        this.$location = r2;
                    }

                    @Override // com.android.systemui.util.animation.UniqueObjectHostView.MeasurementManager
                    @NotNull
                    public MeasurementOutput onMeasure(@NotNull MeasurementInput measurementInput) {
                        Intrinsics.checkParameterIsNotNull(measurementInput, "input");
                        if (View.MeasureSpec.getMode(measurementInput.getWidthMeasureSpec()) == Integer.MIN_VALUE) {
                            measurementInput.setWidthMeasureSpec(View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(measurementInput.getWidthMeasureSpec()), 1073741824));
                        }
                        this.this$0.state.setMeasurementInput(measurementInput);
                        return this.this$0.mediaHostStatesManager.updateCarouselDimensions(this.$location, this.this$0.state);
                    }
                });
                this.state.setChangedListener(new Function0<Unit>(this, i) { // from class: com.android.systemui.media.MediaHost$init$3
                    final /* synthetic */ int $location;
                    final /* synthetic */ MediaHost this$0;

                    {
                        this.this$0 = r1;
                        this.$location = r2;
                    }

                    @Override // kotlin.jvm.functions.Function0
                    public final void invoke() {
                        this.this$0.mediaHostStatesManager.updateHostState(this.$location, this.this$0.state);
                    }
                });
                updateViewVisibility();
                return;
            }
            Intrinsics.throwUninitializedPropertyAccessException("hostView");
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("hostView");
        throw null;
    }

    /* access modifiers changed from: private */
    public final void updateViewVisibility() {
        boolean z;
        if (getShowsOnlyActiveMedia()) {
            z = this.mediaDataFilter.hasActiveMedia();
        } else {
            z = this.mediaDataFilter.hasAnyMedia();
        }
        setVisible(z);
        int i = getVisible() ? 0 : 8;
        this.notificationMediaManager.onMediaHostVisibilityChanged(i);
        UniqueObjectHostView uniqueObjectHostView = this.hostView;
        if (uniqueObjectHostView == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hostView");
            throw null;
        } else if (i != uniqueObjectHostView.getVisibility()) {
            UniqueObjectHostView uniqueObjectHostView2 = this.hostView;
            if (uniqueObjectHostView2 != null) {
                uniqueObjectHostView2.setVisibility(i);
                Iterator<T> it = this.visibleChangedListeners.iterator();
                while (it.hasNext()) {
                    ((Function1) it.next()).invoke(Boolean.valueOf(getVisible()));
                }
                return;
            }
            Intrinsics.throwUninitializedPropertyAccessException("hostView");
            throw null;
        }
    }

    /* compiled from: MediaHost.kt */
    public static final class MediaHostStateHolder implements MediaHostState {
        @Nullable
        private Function0<Unit> changedListener;
        @NotNull
        private DisappearParameters disappearParameters = new DisappearParameters();
        private float expansion;
        private boolean falsingProtectionNeeded;
        private int lastDisappearHash = getDisappearParameters().hashCode();
        @Nullable
        private MeasurementInput measurementInput;
        private boolean showsOnlyActiveMedia;
        private boolean visible = true;

        @Override // com.android.systemui.media.MediaHostState
        @Nullable
        public MeasurementInput getMeasurementInput() {
            return this.measurementInput;
        }

        public void setMeasurementInput(@Nullable MeasurementInput measurementInput) {
            if (measurementInput == null || !measurementInput.equals(this.measurementInput)) {
                this.measurementInput = measurementInput;
                Function0<Unit> function0 = this.changedListener;
                if (function0 != null) {
                    function0.invoke();
                }
            }
        }

        @Override // com.android.systemui.media.MediaHostState
        public float getExpansion() {
            return this.expansion;
        }

        @Override // com.android.systemui.media.MediaHostState
        public void setExpansion(float f) {
            if (!Float.valueOf(f).equals(Float.valueOf(this.expansion))) {
                this.expansion = f;
                Function0<Unit> function0 = this.changedListener;
                if (function0 != null) {
                    function0.invoke();
                }
            }
        }

        @Override // com.android.systemui.media.MediaHostState
        public boolean getShowsOnlyActiveMedia() {
            return this.showsOnlyActiveMedia;
        }

        public void setShowsOnlyActiveMedia(boolean z) {
            if (!Boolean.valueOf(z).equals(Boolean.valueOf(this.showsOnlyActiveMedia))) {
                this.showsOnlyActiveMedia = z;
                Function0<Unit> function0 = this.changedListener;
                if (function0 != null) {
                    function0.invoke();
                }
            }
        }

        @Override // com.android.systemui.media.MediaHostState
        public boolean getVisible() {
            return this.visible;
        }

        public void setVisible(boolean z) {
            if (this.visible != z) {
                this.visible = z;
                Function0<Unit> function0 = this.changedListener;
                if (function0 != null) {
                    function0.invoke();
                }
            }
        }

        @Override // com.android.systemui.media.MediaHostState
        public boolean getFalsingProtectionNeeded() {
            return this.falsingProtectionNeeded;
        }

        public void setFalsingProtectionNeeded(boolean z) {
            if (this.falsingProtectionNeeded != z) {
                this.falsingProtectionNeeded = z;
                Function0<Unit> function0 = this.changedListener;
                if (function0 != null) {
                    function0.invoke();
                }
            }
        }

        @Override // com.android.systemui.media.MediaHostState
        @NotNull
        public DisappearParameters getDisappearParameters() {
            return this.disappearParameters;
        }

        public void setDisappearParameters(@NotNull DisappearParameters disappearParameters) {
            Intrinsics.checkParameterIsNotNull(disappearParameters, "value");
            int hashCode = disappearParameters.hashCode();
            if (!Integer.valueOf(this.lastDisappearHash).equals(Integer.valueOf(hashCode))) {
                this.disappearParameters = disappearParameters;
                this.lastDisappearHash = hashCode;
                Function0<Unit> function0 = this.changedListener;
                if (function0 != null) {
                    function0.invoke();
                }
            }
        }

        public final void setChangedListener(@Nullable Function0<Unit> function0) {
            this.changedListener = function0;
        }

        @Override // com.android.systemui.media.MediaHostState
        @NotNull
        public MediaHostState copy() {
            MediaHostStateHolder mediaHostStateHolder = new MediaHostStateHolder();
            mediaHostStateHolder.setExpansion(getExpansion());
            mediaHostStateHolder.setShowsOnlyActiveMedia(getShowsOnlyActiveMedia());
            MeasurementInput measurementInput = getMeasurementInput();
            MeasurementInput measurementInput2 = null;
            if (measurementInput != null) {
                measurementInput2 = MeasurementInput.copy$default(measurementInput, 0, 0, 3, null);
            }
            mediaHostStateHolder.setMeasurementInput(measurementInput2);
            mediaHostStateHolder.setVisible(getVisible());
            mediaHostStateHolder.setDisappearParameters(getDisappearParameters().deepCopy());
            mediaHostStateHolder.setFalsingProtectionNeeded(getFalsingProtectionNeeded());
            return mediaHostStateHolder;
        }

        public boolean equals(@Nullable Object obj) {
            if (!(obj instanceof MediaHostState)) {
                return false;
            }
            MediaHostState mediaHostState = (MediaHostState) obj;
            if (Objects.equals(getMeasurementInput(), mediaHostState.getMeasurementInput()) && getExpansion() == mediaHostState.getExpansion() && getShowsOnlyActiveMedia() == mediaHostState.getShowsOnlyActiveMedia() && getVisible() == mediaHostState.getVisible() && getFalsingProtectionNeeded() == mediaHostState.getFalsingProtectionNeeded() && getDisappearParameters().equals(mediaHostState.getDisappearParameters())) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            MeasurementInput measurementInput = getMeasurementInput();
            return ((((((((((measurementInput != null ? measurementInput.hashCode() : 0) * 31) + Float.hashCode(getExpansion())) * 31) + Boolean.hashCode(getFalsingProtectionNeeded())) * 31) + Boolean.hashCode(getShowsOnlyActiveMedia())) * 31) + (getVisible() ? 1 : 2)) * 31) + getDisappearParameters().hashCode();
        }
    }
}
