package com.google.android.material.recyclerview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.animation.Interpolator;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.animation.AnimatorUtils;
import com.google.android.material.R$id;
import com.google.android.material.R$layout;
import com.google.android.material.R$styleable;
import com.oneplus.commonctrl.R$dimen;
import com.oneplus.commonctrl.R$integer;
public class AnimationGriditemView extends FrameLayout {
    private static final int ANIMATION_DURATION_RES = R$integer.op_control_time_225;
    private static final int RADIUS_RES = R$dimen.op_control_radius_r12;
    private static final RadiusMode[] sRadiusModeTypeArray = {RadiusMode.NONE, RadiusMode.RADIUS};
    private static final ImageView.ScaleType[] sScaleTypeArray = {ImageView.ScaleType.MATRIX, ImageView.ScaleType.FIT_XY, ImageView.ScaleType.FIT_START, ImageView.ScaleType.FIT_CENTER, ImageView.ScaleType.FIT_END, ImageView.ScaleType.CENTER, ImageView.ScaleType.CENTER_CROP, ImageView.ScaleType.CENTER_INSIDE};
    private ImageView mImage;
    private int mRadius;
    private RadiusMode mRadiusMode = RadiusMode.NONE;

    public enum RadiusMode {
        NONE(0),
        RADIUS(1);
        
        final int nativeInt;

        private RadiusMode(int i) {
            this.nativeInt = i;
        }
    }

    public AnimationGriditemView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        Interpolator interpolator = AnimatorUtils.GRID_ITEM_ANIMATION_INTERPOLATOR;
        init(attributeSet);
    }

    private void init(AttributeSet attributeSet) {
        LayoutInflater.from(getContext()).inflate(R$layout.op_animation_grid_list_item, (ViewGroup) this, true);
        this.mImage = (ImageView) findViewById(R$id.grid_item_img);
        findViewById(R$id.mantle);
        CheckBox checkBox = (CheckBox) findViewById(R$id.grid_item_checkbox);
        this.mRadius = getResources().getDimensionPixelOffset(RADIUS_RES);
        getResources().getInteger(ANIMATION_DURATION_RES);
        TypedArray obtainStyledAttributes = getContext().obtainStyledAttributes(attributeSet, R$styleable.AnimationGridItemView, 0, 0);
        Drawable drawable = obtainStyledAttributes.getDrawable(R$styleable.AnimationGridItemView_android_src);
        if (drawable != null) {
            setImageDrawable(drawable);
        }
        int i = obtainStyledAttributes.getInt(R$styleable.AnimationGridItemView_android_scaleType, 1);
        if (i >= 0) {
            this.mImage.setScaleType(sScaleTypeArray[i]);
        }
        int i2 = obtainStyledAttributes.getInt(R$styleable.AnimationGridItemView_radiusMode, -1);
        if (i2 >= 0) {
            setRadiusMode(sRadiusModeTypeArray[i2]);
        }
        obtainStyledAttributes.recycle();
    }

    public void setImageDrawable(Drawable drawable) {
        this.mImage.setImageDrawable(drawable);
    }

    public void setRadiusMode(RadiusMode radiusMode) {
        if (this.mRadiusMode != radiusMode) {
            this.mRadiusMode = radiusMode;
            scheduleRadiusChange();
        }
    }

    private void scheduleRadiusChange() {
        if (this.mRadiusMode == RadiusMode.RADIUS) {
            setOutlineProvider(new RoundRectOutlineProvider(this.mRadius));
            setClipToOutline(true);
            this.mImage.setOutlineProvider(new RoundRectOutlineProvider(this.mRadius));
            this.mImage.setClipToOutline(true);
        }
    }

    /* access modifiers changed from: private */
    public static class RoundRectOutlineProvider extends ViewOutlineProvider {
        private int mRadius;

        public RoundRectOutlineProvider(int i) {
            this.mRadius = i;
        }

        @Override // android.view.ViewOutlineProvider
        public void getOutline(View view, Outline outline) {
            outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), (float) this.mRadius);
        }
    }
}
