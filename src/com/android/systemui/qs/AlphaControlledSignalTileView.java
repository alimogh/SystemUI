package com.android.systemui.qs;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import com.android.systemui.qs.tileimpl.SlashImageView;
public class AlphaControlledSignalTileView extends SignalTileView {
    public AlphaControlledSignalTileView(Context context) {
        super(context);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.SignalTileView
    public SlashImageView createSlashImageView(Context context) {
        return new AlphaControlledSlashImageView(context);
    }

    public static class AlphaControlledSlashImageView extends SlashImageView {
        public AlphaControlledSlashImageView(Context context) {
            super(context);
        }

        public void setFinalImageTintList(ColorStateList colorStateList) {
            super.setImageTintList(colorStateList);
            SlashDrawable slash = getSlash();
            if (slash != null) {
                ((AlphaControlledSlashDrawable) slash).setFinalTintList(colorStateList);
            }
        }

        /* access modifiers changed from: protected */
        @Override // com.android.systemui.qs.tileimpl.SlashImageView
        public void ensureSlashDrawable() {
            if (getSlash() == null) {
                AlphaControlledSlashDrawable alphaControlledSlashDrawable = new AlphaControlledSlashDrawable(getDrawable());
                setSlash(alphaControlledSlashDrawable);
                alphaControlledSlashDrawable.setAnimationEnabled(getAnimationEnabled());
                setImageViewDrawable(alphaControlledSlashDrawable);
            }
        }
    }

    public static class AlphaControlledSlashDrawable extends SlashDrawable {
        /* access modifiers changed from: protected */
        @Override // com.android.systemui.qs.SlashDrawable
        public void setDrawableTintList(ColorStateList colorStateList) {
        }

        AlphaControlledSlashDrawable(Drawable drawable) {
            super(drawable);
        }

        public void setFinalTintList(ColorStateList colorStateList) {
            super.setDrawableTintList(colorStateList);
        }
    }
}
