package com.google.android.material.floatingactionbutton;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.google.android.material.R$attr;
import com.google.android.material.R$drawable;
import com.google.android.material.R$id;
import com.google.android.material.R$layout;
import com.google.android.material.R$style;
import com.google.android.material.R$styleable;
public class RectangleFloatingActionButton extends RelativeLayout {
    private ImageView mNormalImageView;

    public RectangleFloatingActionButton(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, R$attr.rectangleFloatingActionButtonStyle);
    }

    public RectangleFloatingActionButton(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.rectangleFloatingActionButton, i, R$style.Widget_Design_RectangleFloatingActionButton);
        ColorStateList colorStateList = obtainStyledAttributes.getColorStateList(R$styleable.rectangleFloatingActionButton_tintColor);
        Drawable mutate = getResources().getDrawable(R$drawable.rectangle_floating_action_button).mutate();
        mutate.setTintList(colorStateList);
        setBackground(new RippleDrawable(ColorStateList.valueOf(getResources().getColor(17170443)), mutate, null));
        ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R$layout.float_switch_button, this);
        this.mNormalImageView = (ImageView) findViewById(R$id.normal_imageview);
        this.mNormalImageView.setImageDrawable(obtainStyledAttributes.getDrawable(R$styleable.rectangleFloatingActionButton_image));
        ImageView imageView = (ImageView) findViewById(R$id.switch_imageview);
        obtainStyledAttributes.recycle();
    }
}
