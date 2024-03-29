package com.android.systemui.media;

import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.SeekBar;
import androidx.core.view.GestureDetectorCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.android.systemui.media.SeekBarViewModel;
import com.android.systemui.util.concurrency.RepeatableExecutor;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: SeekBarViewModel.kt */
public final class SeekBarViewModel {
    private Progress _data = new Progress(false, false, null, null);
    private final MutableLiveData<Progress> _progress;
    private final RepeatableExecutor bgExecutor;
    private SeekBarViewModel$callback$1 callback;
    private Runnable cancel;
    private MediaController controller;
    private boolean isFalseSeek;
    private boolean listening;
    private PlaybackState playbackState;
    private boolean scrubbing;

    public SeekBarViewModel(@NotNull RepeatableExecutor repeatableExecutor) {
        Intrinsics.checkParameterIsNotNull(repeatableExecutor, "bgExecutor");
        this.bgExecutor = repeatableExecutor;
        MutableLiveData<Progress> mutableLiveData = new MutableLiveData<>();
        mutableLiveData.postValue(this._data);
        this._progress = mutableLiveData;
        this.callback = new SeekBarViewModel$callback$1(this);
        this.listening = true;
    }

    /* access modifiers changed from: private */
    public final void set_data(Progress progress) {
        this._data = progress;
        this._progress.postValue(progress);
    }

    @NotNull
    public final LiveData<Progress> getProgress() {
        return this._progress;
    }

    /* access modifiers changed from: private */
    public final void setController(MediaController mediaController) {
        MediaController mediaController2 = this.controller;
        MediaSession.Token token = null;
        MediaSession.Token sessionToken = mediaController2 != null ? mediaController2.getSessionToken() : null;
        if (mediaController != null) {
            token = mediaController.getSessionToken();
        }
        if (!Intrinsics.areEqual(sessionToken, token)) {
            MediaController mediaController3 = this.controller;
            if (mediaController3 != null) {
                mediaController3.unregisterCallback(this.callback);
            }
            if (mediaController != null) {
                mediaController.registerCallback(this.callback);
            }
            this.controller = mediaController;
        }
    }

    public final void setListening(boolean z) {
        this.bgExecutor.execute(new Runnable(this, z) { // from class: com.android.systemui.media.SeekBarViewModel$listening$1
            final /* synthetic */ boolean $value;
            final /* synthetic */ SeekBarViewModel this$0;

            {
                this.this$0 = r1;
                this.$value = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                boolean z2 = this.this$0.listening;
                boolean z3 = this.$value;
                if (z2 != z3) {
                    this.this$0.listening = z3;
                    this.this$0.checkIfPollingNeeded();
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public final void setScrubbing(boolean z) {
        if (this.scrubbing != z) {
            this.scrubbing = z;
            checkIfPollingNeeded();
        }
    }

    public final void onSeekStarting() {
        this.bgExecutor.execute(new Runnable(this) { // from class: com.android.systemui.media.SeekBarViewModel$onSeekStarting$1
            final /* synthetic */ SeekBarViewModel this$0;

            {
                this.this$0 = r1;
            }

            @Override // java.lang.Runnable
            public final void run() {
                this.this$0.setScrubbing(true);
                this.this$0.isFalseSeek = false;
            }
        });
    }

    public final void onSeekProgress(long j) {
        this.bgExecutor.execute(new Runnable(this, j) { // from class: com.android.systemui.media.SeekBarViewModel$onSeekProgress$1
            final /* synthetic */ long $position;
            final /* synthetic */ SeekBarViewModel this$0;

            {
                this.this$0 = r1;
                this.$position = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                if (this.this$0.scrubbing) {
                    SeekBarViewModel seekBarViewModel = this.this$0;
                    seekBarViewModel.set_data(SeekBarViewModel.Progress.copy$default(seekBarViewModel._data, false, false, Integer.valueOf((int) this.$position), null, 11, null));
                }
            }
        });
    }

    public final void onSeekFalse() {
        this.bgExecutor.execute(new Runnable(this) { // from class: com.android.systemui.media.SeekBarViewModel$onSeekFalse$1
            final /* synthetic */ SeekBarViewModel this$0;

            {
                this.this$0 = r1;
            }

            @Override // java.lang.Runnable
            public final void run() {
                if (this.this$0.scrubbing) {
                    this.this$0.isFalseSeek = true;
                }
            }
        });
    }

    public final void onSeek(long j) {
        this.bgExecutor.execute(new Runnable(this, j) { // from class: com.android.systemui.media.SeekBarViewModel$onSeek$1
            final /* synthetic */ long $position;
            final /* synthetic */ SeekBarViewModel this$0;

            {
                this.this$0 = r1;
                this.$position = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                MediaController.TransportControls transportControls;
                if (this.this$0.isFalseSeek) {
                    this.this$0.setScrubbing(false);
                    this.this$0.checkPlaybackPosition();
                    return;
                }
                MediaController mediaController = this.this$0.controller;
                if (!(mediaController == null || (transportControls = mediaController.getTransportControls()) == null)) {
                    transportControls.seekTo(this.$position);
                }
                this.this$0.playbackState = null;
                this.this$0.setScrubbing(false);
            }
        });
    }

    public final void updateController(@Nullable MediaController mediaController) {
        setController(mediaController);
        MediaController mediaController2 = this.controller;
        Integer num = null;
        this.playbackState = mediaController2 != null ? mediaController2.getPlaybackState() : null;
        MediaController mediaController3 = this.controller;
        MediaMetadata metadata = mediaController3 != null ? mediaController3.getMetadata() : null;
        PlaybackState playbackState = this.playbackState;
        boolean z = true;
        boolean z2 = ((playbackState != null ? playbackState.getActions() : 0) & 256) != 0;
        PlaybackState playbackState2 = this.playbackState;
        Integer valueOf = playbackState2 != null ? Integer.valueOf((int) playbackState2.getPosition()) : null;
        if (metadata != null) {
            num = Integer.valueOf((int) metadata.getLong("android.media.metadata.DURATION"));
        }
        PlaybackState playbackState3 = this.playbackState;
        if (playbackState3 == null || ((playbackState3 != null && playbackState3.getState() == 0) || (num != null && num.intValue() <= 0))) {
            z = false;
        }
        set_data(new Progress(z, z2, valueOf, num));
        checkIfPollingNeeded();
    }

    public final void clearController() {
        this.bgExecutor.execute(new Runnable(this) { // from class: com.android.systemui.media.SeekBarViewModel$clearController$1
            final /* synthetic */ SeekBarViewModel this$0;

            {
                this.this$0 = r1;
            }

            @Override // java.lang.Runnable
            public final void run() {
                this.this$0.setController(null);
                this.this$0.playbackState = null;
                Runnable runnable = this.this$0.cancel;
                if (runnable != null) {
                    runnable.run();
                }
                this.this$0.cancel = null;
                SeekBarViewModel seekBarViewModel = this.this$0;
                seekBarViewModel.set_data(SeekBarViewModel.Progress.copy$default(seekBarViewModel._data, false, false, null, null, 14, null));
            }
        });
    }

    public final void onDestroy() {
        this.bgExecutor.execute(new Runnable(this) { // from class: com.android.systemui.media.SeekBarViewModel$onDestroy$1
            final /* synthetic */ SeekBarViewModel this$0;

            {
                this.this$0 = r1;
            }

            @Override // java.lang.Runnable
            public final void run() {
                this.this$0.setController(null);
                this.this$0.playbackState = null;
                Runnable runnable = this.this$0.cancel;
                if (runnable != null) {
                    runnable.run();
                }
                this.this$0.cancel = null;
            }
        });
    }

    /* access modifiers changed from: private */
    public final void checkPlaybackPosition() {
        Integer duration = this._data.getDuration();
        int intValue = duration != null ? duration.intValue() : -1;
        PlaybackState playbackState = this.playbackState;
        Integer valueOf = playbackState != null ? Integer.valueOf((int) SeekBarViewModelKt.access$computePosition(playbackState, (long) intValue)) : null;
        if (valueOf != null && (!Intrinsics.areEqual(this._data.getElapsedTime(), valueOf))) {
            set_data(Progress.copy$default(this._data, false, false, valueOf, null, 11, null));
        }
    }

    /* access modifiers changed from: private */
    public final void checkIfPollingNeeded() {
        boolean z = false;
        if (this.listening && !this.scrubbing) {
            PlaybackState playbackState = this.playbackState;
            if (playbackState != null ? SeekBarViewModelKt.access$isInMotion(playbackState) : false) {
                z = true;
            }
        }
        if (!z) {
            Runnable runnable = this.cancel;
            if (runnable != null) {
                runnable.run();
            }
            this.cancel = null;
        } else if (this.cancel == null) {
            this.cancel = this.bgExecutor.executeRepeatedly(new Runnable() { // from class: com.android.systemui.media.SeekBarViewModelKt$sam$java_lang_Runnable$0
                @Override // java.lang.Runnable
                public final /* synthetic */ void run() {
                    Intrinsics.checkExpressionValueIsNotNull(Function0.this.invoke(), "invoke(...)");
                }
            }, 0, 100);
        }
    }

    @NotNull
    public final SeekBar.OnSeekBarChangeListener getSeekBarListener() {
        return new SeekBarChangeListener(this);
    }

    public final void attachTouchHandlers(@NotNull SeekBar seekBar) {
        Intrinsics.checkParameterIsNotNull(seekBar, "bar");
        seekBar.setOnSeekBarChangeListener(getSeekBarListener());
        seekBar.setOnTouchListener(new SeekBarTouchListener(this, seekBar));
    }

    /* compiled from: SeekBarViewModel.kt */
    /* access modifiers changed from: private */
    public static final class SeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        @NotNull
        private final SeekBarViewModel viewModel;

        public SeekBarChangeListener(@NotNull SeekBarViewModel seekBarViewModel) {
            Intrinsics.checkParameterIsNotNull(seekBarViewModel, "viewModel");
            this.viewModel = seekBarViewModel;
        }

        @Override // android.widget.SeekBar.OnSeekBarChangeListener
        public void onProgressChanged(@NotNull SeekBar seekBar, int i, boolean z) {
            Intrinsics.checkParameterIsNotNull(seekBar, "bar");
            if (z) {
                this.viewModel.onSeekProgress((long) i);
            }
        }

        @Override // android.widget.SeekBar.OnSeekBarChangeListener
        public void onStartTrackingTouch(@NotNull SeekBar seekBar) {
            Intrinsics.checkParameterIsNotNull(seekBar, "bar");
            this.viewModel.onSeekStarting();
        }

        @Override // android.widget.SeekBar.OnSeekBarChangeListener
        public void onStopTrackingTouch(@NotNull SeekBar seekBar) {
            Intrinsics.checkParameterIsNotNull(seekBar, "bar");
            this.viewModel.onSeek((long) seekBar.getProgress());
        }
    }

    /* compiled from: SeekBarViewModel.kt */
    /* access modifiers changed from: private */
    public static final class SeekBarTouchListener implements View.OnTouchListener, GestureDetector.OnGestureListener {
        private final SeekBar bar;
        private final GestureDetectorCompat detector;
        private final int flingVelocity = (ViewConfiguration.get(this.bar.getContext()).getScaledMinimumFlingVelocity() * 10);
        private boolean shouldGoToSeekBar;
        private final SeekBarViewModel viewModel;

        @Override // android.view.GestureDetector.OnGestureListener
        public void onLongPress(@NotNull MotionEvent motionEvent) {
            Intrinsics.checkParameterIsNotNull(motionEvent, "event");
        }

        @Override // android.view.GestureDetector.OnGestureListener
        public void onShowPress(@NotNull MotionEvent motionEvent) {
            Intrinsics.checkParameterIsNotNull(motionEvent, "event");
        }

        public SeekBarTouchListener(@NotNull SeekBarViewModel seekBarViewModel, @NotNull SeekBar seekBar) {
            Intrinsics.checkParameterIsNotNull(seekBarViewModel, "viewModel");
            Intrinsics.checkParameterIsNotNull(seekBar, "bar");
            this.viewModel = seekBarViewModel;
            this.bar = seekBar;
            this.detector = new GestureDetectorCompat(seekBar.getContext(), this);
        }

        @Override // android.view.View.OnTouchListener
        public boolean onTouch(@NotNull View view, @NotNull MotionEvent motionEvent) {
            Intrinsics.checkParameterIsNotNull(view, "view");
            Intrinsics.checkParameterIsNotNull(motionEvent, "event");
            if (!Intrinsics.areEqual(view, this.bar)) {
                return false;
            }
            this.detector.onTouchEvent(motionEvent);
            return !this.shouldGoToSeekBar;
        }

        @Override // android.view.GestureDetector.OnGestureListener
        public boolean onDown(@NotNull MotionEvent motionEvent) {
            double d;
            double d2;
            ViewParent parent;
            Intrinsics.checkParameterIsNotNull(motionEvent, "event");
            int paddingLeft = this.bar.getPaddingLeft();
            int paddingRight = this.bar.getPaddingRight();
            int progress = this.bar.getProgress();
            int max = this.bar.getMax() - this.bar.getMin();
            double min = max > 0 ? ((double) (progress - this.bar.getMin())) / ((double) max) : 0.0d;
            int width = (this.bar.getWidth() - paddingLeft) - paddingRight;
            if (this.bar.isLayoutRtl()) {
                d2 = (double) paddingLeft;
                d = ((double) width) * (((double) 1) - min);
            } else {
                d2 = (double) paddingLeft;
                d = ((double) width) * min;
            }
            double d3 = d2 + d;
            long height = (long) (this.bar.getHeight() / 2);
            int round = (int) (Math.round(d3) - height);
            int round2 = (int) (Math.round(d3) + height);
            int round3 = Math.round(motionEvent.getX());
            boolean z = round3 >= round && round3 <= round2;
            this.shouldGoToSeekBar = z;
            if (z && (parent = this.bar.getParent()) != null) {
                parent.requestDisallowInterceptTouchEvent(true);
            }
            return this.shouldGoToSeekBar;
        }

        @Override // android.view.GestureDetector.OnGestureListener
        public boolean onSingleTapUp(@NotNull MotionEvent motionEvent) {
            Intrinsics.checkParameterIsNotNull(motionEvent, "event");
            this.shouldGoToSeekBar = true;
            return true;
        }

        @Override // android.view.GestureDetector.OnGestureListener
        public boolean onScroll(@NotNull MotionEvent motionEvent, @NotNull MotionEvent motionEvent2, float f, float f2) {
            Intrinsics.checkParameterIsNotNull(motionEvent, "eventStart");
            Intrinsics.checkParameterIsNotNull(motionEvent2, "event");
            return this.shouldGoToSeekBar;
        }

        @Override // android.view.GestureDetector.OnGestureListener
        public boolean onFling(@NotNull MotionEvent motionEvent, @NotNull MotionEvent motionEvent2, float f, float f2) {
            Intrinsics.checkParameterIsNotNull(motionEvent, "eventStart");
            Intrinsics.checkParameterIsNotNull(motionEvent2, "event");
            if (Math.abs(f) > ((float) this.flingVelocity) || Math.abs(f2) > ((float) this.flingVelocity)) {
                this.viewModel.onSeekFalse();
            }
            return this.shouldGoToSeekBar;
        }
    }

    /* compiled from: SeekBarViewModel.kt */
    public static final class Progress {
        @Nullable
        private final Integer duration;
        @Nullable
        private final Integer elapsedTime;
        private final boolean enabled;
        private final boolean seekAvailable;

        public static /* synthetic */ Progress copy$default(Progress progress, boolean z, boolean z2, Integer num, Integer num2, int i, Object obj) {
            if ((i & 1) != 0) {
                z = progress.enabled;
            }
            if ((i & 2) != 0) {
                z2 = progress.seekAvailable;
            }
            if ((i & 4) != 0) {
                num = progress.elapsedTime;
            }
            if ((i & 8) != 0) {
                num2 = progress.duration;
            }
            return progress.copy(z, z2, num, num2);
        }

        @NotNull
        public final Progress copy(boolean z, boolean z2, @Nullable Integer num, @Nullable Integer num2) {
            return new Progress(z, z2, num, num2);
        }

        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Progress)) {
                return false;
            }
            Progress progress = (Progress) obj;
            return this.enabled == progress.enabled && this.seekAvailable == progress.seekAvailable && Intrinsics.areEqual(this.elapsedTime, progress.elapsedTime) && Intrinsics.areEqual(this.duration, progress.duration);
        }

        public int hashCode() {
            boolean z = this.enabled;
            int i = 1;
            if (z) {
                z = true;
            }
            int i2 = z ? 1 : 0;
            int i3 = z ? 1 : 0;
            int i4 = z ? 1 : 0;
            int i5 = i2 * 31;
            boolean z2 = this.seekAvailable;
            if (!z2) {
                i = z2 ? 1 : 0;
            }
            int i6 = (i5 + i) * 31;
            Integer num = this.elapsedTime;
            int i7 = 0;
            int hashCode = (i6 + (num != null ? num.hashCode() : 0)) * 31;
            Integer num2 = this.duration;
            if (num2 != null) {
                i7 = num2.hashCode();
            }
            return hashCode + i7;
        }

        @NotNull
        public String toString() {
            return "Progress(enabled=" + this.enabled + ", seekAvailable=" + this.seekAvailable + ", elapsedTime=" + this.elapsedTime + ", duration=" + this.duration + ")";
        }

        public Progress(boolean z, boolean z2, @Nullable Integer num, @Nullable Integer num2) {
            this.enabled = z;
            this.seekAvailable = z2;
            this.elapsedTime = num;
            this.duration = num2;
        }

        public final boolean getEnabled() {
            return this.enabled;
        }

        public final boolean getSeekAvailable() {
            return this.seekAvailable;
        }

        @Nullable
        public final Integer getElapsedTime() {
            return this.elapsedTime;
        }

        @Nullable
        public final Integer getDuration() {
            return this.duration;
        }
    }
}
