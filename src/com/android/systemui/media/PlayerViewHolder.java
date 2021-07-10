package com.android.systemui.media;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0011R$layout;
import com.android.systemui.util.animation.TransitionLayout;
import kotlin.TypeCastException;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: PlayerViewHolder.kt */
public final class PlayerViewHolder {
    public static final Companion Companion = new Companion(null);
    private final ImageButton action0;
    private final ImageButton action1;
    private final ImageButton action2;
    private final ImageButton action3;
    private final ImageButton action4;
    private final ImageView albumView;
    private final ImageView appIcon;
    private final TextView appName;
    private final TextView artistText;
    private final TextView elapsedTimeView;
    @NotNull
    private final TransitionLayout player;
    private final ViewGroup progressTimes;
    private final ViewGroup seamless;
    private final ImageView seamlessFallback;
    private final ImageView seamlessIcon;
    private final TextView seamlessText;
    private final SeekBar seekBar;
    private final TextView titleText;
    private final TextView totalTimeView;

    private PlayerViewHolder(View view) {
        if (view != null) {
            this.player = (TransitionLayout) view;
            this.appIcon = (ImageView) view.requireViewById(C0008R$id.icon);
            this.appName = (TextView) view.requireViewById(C0008R$id.app_name);
            this.albumView = (ImageView) view.requireViewById(C0008R$id.album_art);
            this.titleText = (TextView) view.requireViewById(C0008R$id.header_title);
            this.artistText = (TextView) view.requireViewById(C0008R$id.header_artist);
            this.seamless = (ViewGroup) view.requireViewById(C0008R$id.media_seamless);
            this.seamlessIcon = (ImageView) view.requireViewById(C0008R$id.media_seamless_image);
            this.seamlessText = (TextView) view.requireViewById(C0008R$id.media_seamless_text);
            this.seamlessFallback = (ImageView) view.requireViewById(C0008R$id.media_seamless_fallback);
            this.seekBar = (SeekBar) view.requireViewById(C0008R$id.media_progress_bar);
            this.progressTimes = (ViewGroup) view.requireViewById(C0008R$id.notification_media_progress_time);
            this.elapsedTimeView = (TextView) view.requireViewById(C0008R$id.media_elapsed_time);
            this.totalTimeView = (TextView) view.requireViewById(C0008R$id.media_total_time);
            this.action0 = (ImageButton) view.requireViewById(C0008R$id.action0);
            this.action1 = (ImageButton) view.requireViewById(C0008R$id.action1);
            this.action2 = (ImageButton) view.requireViewById(C0008R$id.action2);
            this.action3 = (ImageButton) view.requireViewById(C0008R$id.action3);
            this.action4 = (ImageButton) view.requireViewById(C0008R$id.action4);
            Drawable background = this.player.getBackground();
            if (background != null) {
                IlluminationDrawable illuminationDrawable = (IlluminationDrawable) background;
                ViewGroup viewGroup = this.seamless;
                Intrinsics.checkExpressionValueIsNotNull(viewGroup, "seamless");
                illuminationDrawable.registerLightSource(viewGroup);
                ImageButton imageButton = this.action0;
                Intrinsics.checkExpressionValueIsNotNull(imageButton, "action0");
                illuminationDrawable.registerLightSource(imageButton);
                ImageButton imageButton2 = this.action1;
                Intrinsics.checkExpressionValueIsNotNull(imageButton2, "action1");
                illuminationDrawable.registerLightSource(imageButton2);
                ImageButton imageButton3 = this.action2;
                Intrinsics.checkExpressionValueIsNotNull(imageButton3, "action2");
                illuminationDrawable.registerLightSource(imageButton3);
                ImageButton imageButton4 = this.action3;
                Intrinsics.checkExpressionValueIsNotNull(imageButton4, "action3");
                illuminationDrawable.registerLightSource(imageButton4);
                ImageButton imageButton5 = this.action4;
                Intrinsics.checkExpressionValueIsNotNull(imageButton5, "action4");
                illuminationDrawable.registerLightSource(imageButton5);
                view.requireViewById(C0008R$id.qs_media_controls_options);
                return;
            }
            throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.media.IlluminationDrawable");
        }
        throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.util.animation.TransitionLayout");
    }

    public /* synthetic */ PlayerViewHolder(View view, DefaultConstructorMarker defaultConstructorMarker) {
        this(view);
    }

    @NotNull
    public final TransitionLayout getPlayer() {
        return this.player;
    }

    public final ImageView getAppIcon() {
        return this.appIcon;
    }

    public final TextView getAppName() {
        return this.appName;
    }

    public final ImageView getAlbumView() {
        return this.albumView;
    }

    public final TextView getTitleText() {
        return this.titleText;
    }

    public final TextView getArtistText() {
        return this.artistText;
    }

    public final ViewGroup getSeamless() {
        return this.seamless;
    }

    public final ImageView getSeamlessIcon() {
        return this.seamlessIcon;
    }

    public final TextView getSeamlessText() {
        return this.seamlessText;
    }

    public final ImageView getSeamlessFallback() {
        return this.seamlessFallback;
    }

    public final SeekBar getSeekBar() {
        return this.seekBar;
    }

    public final ViewGroup getProgressTimes() {
        return this.progressTimes;
    }

    public final TextView getElapsedTimeView() {
        return this.elapsedTimeView;
    }

    public final TextView getTotalTimeView() {
        return this.totalTimeView;
    }

    @NotNull
    public final ImageButton getAction(int i) {
        if (i == C0008R$id.action0) {
            ImageButton imageButton = this.action0;
            Intrinsics.checkExpressionValueIsNotNull(imageButton, "action0");
            return imageButton;
        } else if (i == C0008R$id.action1) {
            ImageButton imageButton2 = this.action1;
            Intrinsics.checkExpressionValueIsNotNull(imageButton2, "action1");
            return imageButton2;
        } else if (i == C0008R$id.action2) {
            ImageButton imageButton3 = this.action2;
            Intrinsics.checkExpressionValueIsNotNull(imageButton3, "action2");
            return imageButton3;
        } else if (i == C0008R$id.action3) {
            ImageButton imageButton4 = this.action3;
            Intrinsics.checkExpressionValueIsNotNull(imageButton4, "action3");
            return imageButton4;
        } else if (i == C0008R$id.action4) {
            ImageButton imageButton5 = this.action4;
            Intrinsics.checkExpressionValueIsNotNull(imageButton5, "action4");
            return imageButton5;
        } else {
            throw new IllegalArgumentException();
        }
    }

    /* compiled from: PlayerViewHolder.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        @NotNull
        public final PlayerViewHolder create(@NotNull LayoutInflater layoutInflater, @NotNull ViewGroup viewGroup) {
            Intrinsics.checkParameterIsNotNull(layoutInflater, "inflater");
            Intrinsics.checkParameterIsNotNull(viewGroup, "parent");
            View inflate = layoutInflater.inflate(C0011R$layout.media_view, viewGroup, false);
            Intrinsics.checkExpressionValueIsNotNull(inflate, "mediaView");
            inflate.setLayoutDirection(3);
            PlayerViewHolder playerViewHolder = new PlayerViewHolder(inflate, null);
            SeekBar seekBar = playerViewHolder.getSeekBar();
            Intrinsics.checkExpressionValueIsNotNull(seekBar, "seekBar");
            seekBar.setLayoutDirection(0);
            ViewGroup progressTimes = playerViewHolder.getProgressTimes();
            Intrinsics.checkExpressionValueIsNotNull(progressTimes, "progressTimes");
            progressTimes.setLayoutDirection(0);
            return playerViewHolder;
        }
    }
}
