package com.android.systemui.qs.tileimpl;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.android.systemui.C0004R$color;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0008R$id;
import com.android.systemui.plugins.qs.QSIconView;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.AlphaControlledSignalTileView;
import java.util.Objects;
import java.util.function.Supplier;
public class QSIconViewImpl extends QSIconView {
    protected final String TAG = ("QSIconViewImpl." + getClass().getSimpleName());
    private boolean mAnimationEnabled = true;
    protected final View mIcon;
    protected final int mIconSizePx;
    private QSTile.Icon mLastIcon;
    private int mState = -1;
    private int mTint;

    /* access modifiers changed from: protected */
    public int getIconMeasureMode() {
        return 1073741824;
    }

    public QSIconViewImpl(Context context) {
        super(context);
        this.mIconSizePx = context.getResources().getDimensionPixelSize(C0005R$dimen.qs_tile_icon_size);
        View createIcon = createIcon();
        this.mIcon = createIcon;
        addView(createIcon);
    }

    @Override // com.android.systemui.plugins.qs.QSIconView
    public void disableAnimation() {
        this.mAnimationEnabled = false;
    }

    @Override // com.android.systemui.plugins.qs.QSIconView
    public View getIconView() {
        return this.mIcon;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onMeasure(int i, int i2) {
        int size = View.MeasureSpec.getSize(i);
        this.mIcon.measure(View.MeasureSpec.makeMeasureSpec(size, getIconMeasureMode()), exactly(this.mIconSizePx));
        setMeasuredDimension(size, this.mIcon.getMeasuredHeight());
    }

    @Override // android.view.View, java.lang.Object
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getSimpleName());
        sb.append('[');
        sb.append("state=" + this.mState);
        sb.append(", tint=" + this.mTint);
        if (this.mLastIcon != null) {
            sb.append(", lastIcon=" + this.mLastIcon.toString());
        }
        sb.append("]");
        return sb.toString();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup, android.view.View
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        layout(this.mIcon, (getMeasuredWidth() - this.mIcon.getMeasuredWidth()) / 2, 0);
    }

    @Override // com.android.systemui.plugins.qs.QSIconView
    public void setIcon(QSTile.State state, boolean z) {
        setIcon((ImageView) this.mIcon, state, z);
    }

    /* access modifiers changed from: protected */
    public void updateIcon(ImageView imageView, QSTile.State state, boolean z) {
        Drawable drawable;
        String str;
        Supplier<QSTile.Icon> supplier = state.iconSupplier;
        QSTile.Icon icon = supplier != null ? supplier.get() : state.icon;
        if (!Objects.equals(icon, imageView.getTag(C0008R$id.qs_icon_tag)) || this.mState != state.state || !Objects.equals(state.slash, imageView.getTag(C0008R$id.qs_slash_tag))) {
            boolean z2 = z && shouldAnimate(imageView);
            this.mLastIcon = icon;
            if (icon != null) {
                drawable = z2 ? icon.getDrawable(((ViewGroup) this).mContext) : icon.getInvisibleDrawable(((ViewGroup) this).mContext);
            } else {
                drawable = null;
            }
            int padding = icon != null ? icon.getPadding() : 0;
            if (drawable != null) {
                drawable.setAutoMirrored(false);
                drawable.setLayoutDirection(getLayoutDirection());
            }
            if (Build.DEBUG_ONEPLUS) {
                String str2 = this.TAG;
                StringBuilder sb = new StringBuilder();
                sb.append("updateIcon: label=");
                sb.append((Object) state.label);
                sb.append(", icon=");
                sb.append(icon);
                sb.append(", iconSupplier.get=");
                Supplier<QSTile.Icon> supplier2 = state.iconSupplier;
                sb.append(supplier2 != null ? supplier2.get() : null);
                sb.append(", state.icon=");
                sb.append(state.icon);
                sb.append(", shouldAnimate=");
                sb.append(z2);
                sb.append(", d=");
                if (drawable != null) {
                    str = drawable.toString();
                } else {
                    str = null;
                }
                sb.append(str);
                Log.d(str2, sb.toString());
            }
            if (imageView instanceof SlashImageView) {
                SlashImageView slashImageView = (SlashImageView) imageView;
                slashImageView.setAnimationEnabled(z2);
                slashImageView.setState(null, drawable);
            } else {
                imageView.setImageDrawable(drawable);
            }
            imageView.setTag(C0008R$id.qs_icon_tag, icon);
            imageView.setTag(C0008R$id.qs_slash_tag, state.slash);
            imageView.setPadding(0, padding, 0, padding);
            if (drawable instanceof Animatable2) {
                final Animatable2 animatable2 = (Animatable2) drawable;
                animatable2.start();
                if (state.isTransient) {
                    animatable2.registerAnimationCallback(new Animatable2.AnimationCallback(this) { // from class: com.android.systemui.qs.tileimpl.QSIconViewImpl.1
                        @Override // android.graphics.drawable.Animatable2.AnimationCallback
                        public void onAnimationEnd(Drawable drawable2) {
                            animatable2.start();
                        }
                    });
                }
            }
        }
    }

    private boolean shouldAnimate(ImageView imageView) {
        return this.mAnimationEnabled && imageView.isShown() && imageView.getDrawable() != null;
    }

    /* access modifiers changed from: protected */
    public void setIcon(ImageView imageView, QSTile.State state, boolean z) {
        if (state.disabledByPolicy) {
            imageView.setColorFilter(getContext().getColor(C0004R$color.qs_tile_disabled_color));
        } else {
            imageView.clearColorFilter();
        }
        int i = state.state;
        if (i != this.mState) {
            int color = getColor(i);
            this.mState = state.state;
            if (this.mTint == 0 || !z || !shouldAnimate(imageView)) {
                if (imageView instanceof AlphaControlledSignalTileView.AlphaControlledSlashImageView) {
                    ((AlphaControlledSignalTileView.AlphaControlledSlashImageView) imageView).setFinalImageTintList(ColorStateList.valueOf(color));
                } else {
                    setTint(imageView, color);
                }
                this.mTint = color;
                updateIcon(imageView, state, z);
                return;
            }
            setTint(imageView, color);
            updateIcon(imageView, state, z);
            this.mTint = color;
            return;
        }
        updateIcon(imageView, state, z);
    }

    /* access modifiers changed from: protected */
    public int getColor(int i) {
        return QSTileImpl.getColorForState(getContext(), i);
    }

    public static void setTint(ImageView imageView, int i) {
        imageView.setImageTintList(ColorStateList.valueOf(i));
    }

    /* access modifiers changed from: protected */
    public View createIcon() {
        SlashImageView slashImageView = new SlashImageView(((ViewGroup) this).mContext);
        slashImageView.setId(16908294);
        slashImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        return slashImageView;
    }

    /* access modifiers changed from: protected */
    public final int exactly(int i) {
        return View.MeasureSpec.makeMeasureSpec(i, 1073741824);
    }

    /* access modifiers changed from: protected */
    public final void layout(View view, int i, int i2) {
        view.layout(i, i2, view.getMeasuredWidth() + i, view.getMeasuredHeight() + i2);
    }
}
