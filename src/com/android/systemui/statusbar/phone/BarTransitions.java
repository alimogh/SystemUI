package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import com.android.settingslib.Utils;
import com.android.systemui.C0004R$color;
import com.android.systemui.Interpolators;
import com.oneplus.onlineconfig.OpSystemUIGestureOnlineConfig;
import com.oneplus.plugin.OpLsState;
import com.oneplus.util.OpNavBarUtils;
import com.oneplus.util.OpUtils;
public class BarTransitions {
    private boolean mAlwaysOpaque = false;
    protected BarBackgroundDrawable mBarBackground;
    private int mMode;
    private final String mTag;
    private View mView;

    /* access modifiers changed from: protected */
    public boolean isLightsOut(int i) {
        return i == 3 || i == 6;
    }

    public BarTransitions(View view, int i) {
        this.mTag = "BarTransitions." + view.getClass().getSimpleName();
        this.mView = view;
        BarBackgroundDrawable barBackgroundDrawable = new BarBackgroundDrawable(this.mView.getContext(), i, view);
        this.mBarBackground = barBackgroundDrawable;
        View view2 = this.mView;
        if (view2 != null) {
            view2.setBackground(barBackgroundDrawable);
        }
    }

    public int getMode() {
        return this.mMode;
    }

    public boolean isAlwaysOpaque() {
        return this.mAlwaysOpaque;
    }

    public void transitionTo(int i, boolean z) {
        if (isAlwaysOpaque() && (i == 1 || i == 2 || i == 0)) {
            i = 4;
        }
        if (isAlwaysOpaque() && i == 6) {
            i = 3;
        }
        int i2 = this.mMode;
        if (i2 != i) {
            this.mMode = i;
            if (Build.DEBUG_ONEPLUS) {
                Log.d(this.mTag, String.format("%s -> %s animate=%s", modeToString(i2), modeToString(i), Boolean.valueOf(z)));
            }
            onTransition(i2, this.mMode, z);
        }
    }

    /* access modifiers changed from: protected */
    public void onTransition(int i, int i2, boolean z) {
        applyModeBackground(i, i2, z);
    }

    /* access modifiers changed from: protected */
    public void applyModeBackground(int i, int i2, boolean z) {
        if (Build.DEBUG_ONEPLUS) {
            Log.d(this.mTag, String.format("applyModeBackground oldMode=%s newMode=%s animate=%s", modeToString(i), modeToString(i2), Boolean.valueOf(z)));
        }
        BarBackgroundDrawable barBackgroundDrawable = this.mBarBackground;
        if (barBackgroundDrawable != null) {
            barBackgroundDrawable.applyModeBackground(i, i2, z);
        }
    }

    public static String modeToString(int i) {
        if (i == 4) {
            return "MODE_OPAQUE";
        }
        if (i == 1) {
            return "MODE_SEMI_TRANSPARENT";
        }
        if (i == 2) {
            return "MODE_TRANSLUCENT";
        }
        if (i == 3) {
            return "MODE_LIGHTS_OUT";
        }
        if (i == 0) {
            return "MODE_TRANSPARENT";
        }
        if (i == 5) {
            return "MODE_WARNING";
        }
        if (i == 6) {
            return "MODE_LIGHTS_OUT_TRANSPARENT";
        }
        if (i == 7) {
            return "MODE_HIGHLIGHT_HINT";
        }
        if (i == 11) {
            return "MODE_NOTCH_IGNORE";
        }
        Log.w("BarTransitions", "modeToString Unknown mode " + i);
        return "MODE_UNKNOWN";
    }

    public void finishAnimations() {
        BarBackgroundDrawable barBackgroundDrawable = this.mBarBackground;
        if (barBackgroundDrawable != null) {
            barBackgroundDrawable.finishAnimation();
        }
    }

    /* access modifiers changed from: protected */
    public static class BarBackgroundDrawable extends Drawable {
        private boolean mAnimating;
        private int mColor;
        private int mColorStart;
        private Context mContext = null;
        private long mEndTime;
        private Rect mFrame;
        private OpSystemUIGestureOnlineConfig mGestureOnlineConfig;
        private final Drawable mGradient;
        private int mGradientAlpha;
        private int mGradientAlphaStart;
        private int mHighlightColor;
        private int mMode = -1;
        private final int mNativeOpaque;
        private final int mOpaque;
        private Paint mPaint = new Paint();
        private final int mSemiTransparent;
        private long mStartTime;
        private PorterDuffColorFilter mTintFilter;
        private final int mTransparent;
        private View mView;
        private final int mWarning;

        @Override // android.graphics.drawable.Drawable
        public int getOpacity() {
            return -3;
        }

        @Override // android.graphics.drawable.Drawable
        public void setAlpha(int i) {
        }

        @Override // android.graphics.drawable.Drawable
        public void setColorFilter(ColorFilter colorFilter) {
        }

        public BarBackgroundDrawable(Context context, int i, View view) {
            this.mContext = context;
            this.mView = view;
            context.getResources();
            if (!OpNavBarUtils.isSupportCustomNavBar() || !view.getClass().getSimpleName().equals("NavigationBarView")) {
                this.mOpaque = context.getColor(C0004R$color.system_bar_background_opaque);
                this.mSemiTransparent = context.getColor(17170993);
            } else {
                this.mOpaque = context.getColor(C0004R$color.op_nav_bar_background_light);
                this.mSemiTransparent = context.getColor(C0004R$color.op_nav_bar_background_transparent);
            }
            this.mTransparent = context.getColor(C0004R$color.system_bar_background_transparent);
            this.mWarning = Utils.getColorAttrDefaultColor(context, 16844099);
            this.mNativeOpaque = context.getColor(C0004R$color.system_bar_background_opaque);
            this.mGestureOnlineConfig = OpSystemUIGestureOnlineConfig.getInstance();
            this.mGradient = context.getDrawable(i);
        }

        public void setFrame(Rect rect) {
            this.mFrame = rect;
        }

        @Override // android.graphics.drawable.Drawable
        public void setTint(int i) {
            PorterDuff.Mode mode;
            PorterDuffColorFilter porterDuffColorFilter = this.mTintFilter;
            if (porterDuffColorFilter == null) {
                mode = PorterDuff.Mode.SRC_IN;
            } else {
                mode = porterDuffColorFilter.getMode();
            }
            PorterDuffColorFilter porterDuffColorFilter2 = this.mTintFilter;
            if (porterDuffColorFilter2 == null || porterDuffColorFilter2.getColor() != i) {
                this.mTintFilter = new PorterDuffColorFilter(i, mode);
            }
            invalidateSelf();
        }

        @Override // android.graphics.drawable.Drawable
        public void setTintMode(PorterDuff.Mode mode) {
            PorterDuffColorFilter porterDuffColorFilter = this.mTintFilter;
            int color = porterDuffColorFilter == null ? 0 : porterDuffColorFilter.getColor();
            PorterDuffColorFilter porterDuffColorFilter2 = this.mTintFilter;
            if (porterDuffColorFilter2 == null || porterDuffColorFilter2.getMode() != mode) {
                this.mTintFilter = new PorterDuffColorFilter(color, mode);
            }
            invalidateSelf();
        }

        /* access modifiers changed from: protected */
        @Override // android.graphics.drawable.Drawable
        public void onBoundsChange(Rect rect) {
            super.onBoundsChange(rect);
            this.mGradient.setBounds(rect);
        }

        public void applyModeBackground(int i, int i2, boolean z) {
            this.mMode = i2;
            this.mAnimating = z;
            if (z) {
                long elapsedRealtime = SystemClock.elapsedRealtime();
                this.mStartTime = elapsedRealtime;
                this.mEndTime = elapsedRealtime + ((long) (this.mMode == 4 ? 0 : 200));
                this.mGradientAlphaStart = this.mGradientAlpha;
                this.mColorStart = this.mColor;
            }
            invalidateSelf();
        }

        public void finishAnimation() {
            if (this.mAnimating) {
                this.mAnimating = false;
                invalidateSelf();
            }
        }

        @Override // android.graphics.drawable.Drawable
        public void draw(Canvas canvas) {
            int i;
            int i2;
            Context context;
            int i3 = this.mMode;
            if (i3 == 5) {
                i = this.mWarning;
            } else if (i3 == 2) {
                i = this.mSemiTransparent;
            } else if (i3 == 1) {
                i = this.mSemiTransparent;
            } else if (i3 == 0 || i3 == 6) {
                i = this.mTransparent;
            } else if (i3 == 7) {
                i = this.mHighlightColor;
            } else if (i3 == 11) {
                i = this.mOpaque;
            } else if (OpNavBarUtils.isSupportCustomNavBar() && this.mContext != null && OpUtils.isScreenCompat()) {
                i = -16777216;
            } else if (!OpNavBarUtils.isSupportCustomNavBar() || (context = this.mContext) == null || !OpUtils.isNeedDarkNavBar(context)) {
                i = this.mOpaque;
            } else {
                i = this.mTransparent;
            }
            if (!this.mAnimating) {
                this.mColor = i;
                this.mGradientAlpha = 0;
            } else {
                long elapsedRealtime = SystemClock.elapsedRealtime();
                long j = this.mEndTime;
                if (elapsedRealtime >= j) {
                    this.mAnimating = false;
                    this.mColor = i;
                    this.mGradientAlpha = 0;
                } else {
                    long j2 = this.mStartTime;
                    float max = Math.max(0.0f, Math.min(Interpolators.LINEAR.getInterpolation(((float) (elapsedRealtime - j2)) / ((float) (j - j2))), 1.0f));
                    float f = 1.0f - max;
                    this.mGradientAlpha = (int) ((((float) 0) * max) + (((float) this.mGradientAlphaStart) * f));
                    if (!OpNavBarUtils.isSupportCustomNavBar() || i != this.mTransparent) {
                        this.mColor = Color.argb((int) ((((float) Color.alpha(i)) * max) + (((float) Color.alpha(this.mColorStart)) * f)), (int) ((((float) Color.red(i)) * max) + (((float) Color.red(this.mColorStart)) * f)), (int) ((((float) Color.green(i)) * max) + (((float) Color.green(this.mColorStart)) * f)), (int) ((max * ((float) Color.blue(i))) + (((float) Color.blue(this.mColorStart)) * f)));
                    } else {
                        this.mColor = Color.argb((int) ((max * ((float) Color.alpha(i))) + (((float) Color.alpha(this.mColorStart)) * f)), Color.red(this.mColorStart), Color.green(this.mColorStart), Color.blue(this.mColorStart));
                    }
                }
            }
            int i4 = this.mGradientAlpha;
            if (i4 > 0) {
                this.mGradient.setAlpha(i4);
                this.mGradient.draw(canvas);
            }
            if (Color.alpha(this.mColor) > 0) {
                this.mPaint.setColor(this.mColor);
                PorterDuffColorFilter porterDuffColorFilter = this.mTintFilter;
                if (porterDuffColorFilter != null) {
                    this.mPaint.setColorFilter(porterDuffColorFilter);
                }
                if (this.mFrame != null) {
                    if (this.mGestureOnlineConfig.isUseNativeOpaqueColor(OpUtils.getTopPackageName()) && this.mView.getClass().getSimpleName().equals("NavigationBarView") && ((i2 = this.mMode) == 4 || i2 == 0)) {
                        this.mPaint.setColor((OpLsState.getInstance().getPhoneStatusBar().getNavigationBarHiddenMode() == 1 || !EdgeBackGestureHandler.sSideGestureEnabled) ? this.mTransparent : this.mNativeOpaque);
                    }
                    canvas.drawRect(this.mFrame, this.mPaint);
                } else {
                    canvas.drawPaint(this.mPaint);
                }
            }
            if (this.mAnimating) {
                invalidateSelf();
            }
        }
    }
}
