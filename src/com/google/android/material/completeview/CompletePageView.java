package com.google.android.material.completeview;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.graphics.drawable.DrawableCompat;
import com.google.android.material.R$attr;
import com.google.android.material.R$drawable;
import com.google.android.material.R$id;
import com.google.android.material.R$layout;
import com.google.android.material.R$styleable;
import com.oneplus.common.AppUtils;
import com.oneplus.common.NavigationButtonUtils;
public class CompletePageView extends LinearLayout {
    private Button mBottomFixActionButton;
    private Button mBottomLeftActionButton;
    private Button mBottomMediumActionButton;
    private Button mBottomRightActionButton;
    private LinearLayout mButtonLayout;
    private TextView mContentText;
    private ColorStateList mIconColor;
    private Drawable mIconDrawable;
    private ImageView mIconView;
    private Button mMiddleBottomActionButton;
    private Button mMiddleTopActionButton;
    private int mPreBottomMargin;
    private int mStatus;
    private TextView mSubHeadingText;

    public CompletePageView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, R$attr.completePageStyle);
    }

    public CompletePageView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public CompletePageView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mStatus = 0;
        this.mPreBottomMargin = 0;
        LayoutInflater.from(context).inflate(R$layout.op_complete_layout, this);
        initView();
        initArrayTyped(context, attributeSet, i, i2);
    }

    private void initArrayTyped(Context context, AttributeSet attributeSet, int i, int i2) {
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.CompletePageView, i, i2);
        Drawable drawable = obtainStyledAttributes.getDrawable(R$styleable.CompletePageView_completeDrawable);
        String string = obtainStyledAttributes.getString(R$styleable.CompletePageView_completeTitle);
        String string2 = obtainStyledAttributes.getString(R$styleable.CompletePageView_completeSubHeading);
        String string3 = obtainStyledAttributes.getString(R$styleable.CompletePageView_completeMiddleTopActionText);
        String string4 = obtainStyledAttributes.getString(R$styleable.CompletePageView_completeMiddleBottomActionText);
        String string5 = obtainStyledAttributes.getString(R$styleable.CompletePageView_completeBottomFixActionText);
        String string6 = obtainStyledAttributes.getString(R$styleable.CompletePageView_completeBottomMediumActionText);
        String string7 = obtainStyledAttributes.getString(R$styleable.CompletePageView_completeBottomLeftActionText);
        String string8 = obtainStyledAttributes.getString(R$styleable.CompletePageView_completeBottomRightActionText);
        setCompleteTitle(string);
        setCompleteSubHeading(string2);
        setActionText(this.mMiddleTopActionButton, string3);
        setActionText(this.mMiddleBottomActionButton, string4);
        setActionText(this.mBottomFixActionButton, string5);
        setActionText(this.mBottomMediumActionButton, string6);
        setActionText(this.mBottomLeftActionButton, string7);
        setActionText(this.mBottomRightActionButton, string8);
        if (drawable == null) {
            checkStatus();
        } else {
            this.mIconDrawable = drawable;
        }
        Drawable wrap = DrawableCompat.wrap(this.mIconDrawable);
        if (obtainStyledAttributes.hasValue(R$styleable.CompletePageView_completeIconColor)) {
            ColorStateList colorStateList = obtainStyledAttributes.getColorStateList(R$styleable.CompletePageView_completeIconColor);
            this.mIconColor = colorStateList;
            DrawableCompat.setTintList(wrap, colorStateList);
        }
        setIcon(wrap);
        obtainStyledAttributes.recycle();
    }

    private void initView() {
        this.mIconView = (ImageView) findViewById(R$id.complete_icon);
        this.mContentText = (TextView) findViewById(R$id.complete_title);
        this.mSubHeadingText = (TextView) findViewById(R$id.complete_subheading_title);
        this.mMiddleTopActionButton = (Button) findViewById(R$id.complete_middle_top_action);
        this.mMiddleBottomActionButton = (Button) findViewById(R$id.complete_middle_bottom_action);
        this.mBottomMediumActionButton = (Button) findViewById(R$id.complete_bottom_single_medium_action);
        this.mBottomFixActionButton = (Button) findViewById(R$id.complete_bottom_single_fix_action);
        this.mBottomLeftActionButton = (Button) findViewById(R$id.complete_bottom_left_action);
        this.mBottomRightActionButton = (Button) findViewById(R$id.complete_bottom_right_action);
        this.mButtonLayout = (LinearLayout) findViewById(R$id.complete_button_layout);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.View, android.view.ViewGroup
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        View findViewById;
        super.onLayout(z, i, i2, i3, i4);
        if (NavigationButtonUtils.isGestureNavigationBar(getContext())) {
            int i5 = NavigationButtonUtils.isGestureButtonShowOnCreen(getContext()) ? 26 : 42;
            if (i5 != this.mPreBottomMargin && (findViewById = findViewById(R$id.complete_buttons)) != null) {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) findViewById.getLayoutParams();
                layoutParams.bottomMargin = (int) AppUtils.dpToPx(getContext(), i5);
                this.mPreBottomMargin = i5;
                findViewById.setLayoutParams(layoutParams);
            }
        }
    }

    private void checkStatus() {
        int i = this.mStatus;
        if (i == 0) {
            this.mIconDrawable = getContext().getDrawable(R$drawable.ic_success_icon);
        } else if (i != 1) {
            this.mIconDrawable = getContext().getDrawable(R$drawable.ic_success_icon);
        } else {
            this.mIconDrawable = getContext().getDrawable(R$drawable.ic_error_icon);
        }
    }

    private void setActionText(Button button, CharSequence charSequence) {
        if (button != null) {
            if (TextUtils.isEmpty(charSequence)) {
                button.setVisibility(8);
            } else {
                LinearLayout linearLayout = this.mButtonLayout;
                if (linearLayout != null) {
                    linearLayout.setVisibility(0);
                }
                button.setVisibility(0);
            }
            button.setText(charSequence);
        }
    }

    public void setIcon(Drawable drawable) {
        ImageView imageView = this.mIconView;
        if (imageView != null) {
            imageView.setImageDrawable(drawable);
            this.mIconDrawable = drawable;
        }
    }

    public void setCompleteTitle(CharSequence charSequence) {
        if (this.mContentText != null) {
            if (TextUtils.isEmpty(charSequence)) {
                this.mContentText.setVisibility(8);
            } else {
                this.mContentText.setVisibility(0);
            }
            this.mContentText.setText(charSequence);
        }
    }

    public void setCompleteSubHeading(CharSequence charSequence) {
        if (this.mSubHeadingText != null) {
            if (TextUtils.isEmpty(charSequence)) {
                this.mSubHeadingText.setVisibility(8);
            } else {
                this.mSubHeadingText.setVisibility(0);
            }
            this.mSubHeadingText.setText(charSequence);
        }
    }
}
