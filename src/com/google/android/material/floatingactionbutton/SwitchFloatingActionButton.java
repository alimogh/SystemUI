package com.google.android.material.floatingactionbutton;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.animation.PathInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.google.android.material.R$attr;
import com.google.android.material.R$dimen;
import com.google.android.material.R$drawable;
import com.google.android.material.R$id;
import com.google.android.material.R$layout;
import com.google.android.material.R$style;
import com.google.android.material.R$styleable;
public class SwitchFloatingActionButton extends RelativeLayout {
    ViewOutlineProvider mCardViewOutlineProvider;
    private ImageView mNormalImageView;

    static {
        new PathInterpolator(0.0f, 0.0f, 0.4f, 1.0f);
    }

    public SwitchFloatingActionButton(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, R$attr.switchFloatingActionButtonStyle);
    }

    public SwitchFloatingActionButton(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mCardViewOutlineProvider = new ViewOutlineProvider(this) { // from class: com.google.android.material.floatingactionbutton.SwitchFloatingActionButton.1
            @Override // android.view.ViewOutlineProvider
            public void getOutline(View view, Outline outline) {
                outline.setAlpha(0.9f);
                outline.setOval(0, 0, view.getWidth(), view.getHeight());
            }
        };
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.switchFloatingActionButton, i, R$style.Widget_Design_SwitchFloatingActionButton);
        float dimension = getResources().getDimension(R$dimen.op_float_action_button_shadow_z8);
        ColorStateList colorStateList = obtainStyledAttributes.getColorStateList(R$styleable.switchFloatingActionButton_tintColor);
        Drawable mutate = getResources().getDrawable(R$drawable.switch_floating_action_button).mutate();
        mutate.setTintList(colorStateList);
        setBackground(new RippleDrawable(ColorStateList.valueOf(getResources().getColor(17170443)), mutate, null));
        setElevation(dimension);
        setOutlineProvider(this.mCardViewOutlineProvider);
        ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R$layout.float_switch_button, this);
        this.mNormalImageView = (ImageView) findViewById(R$id.normal_imageview);
        this.mNormalImageView.setImageDrawable(obtainStyledAttributes.getDrawable(R$styleable.switchFloatingActionButton_image));
        ImageView imageView = (ImageView) findViewById(R$id.switch_imageview);
        obtainStyledAttributes.recycle();
    }
}
