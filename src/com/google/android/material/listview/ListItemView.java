package com.google.android.material.listview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.material.R$attr;
import com.google.android.material.R$dimen;
import com.google.android.material.R$id;
import com.google.android.material.R$layout;
import com.google.android.material.R$styleable;
import com.google.android.material.checkbox.SelectedAvatarView;
import java.util.Locale;
public class ListItemView extends LinearLayout {
    private Drawable mAvataIconDrawable;
    private SelectedAvatarView mAvataIconView;
    private LinearLayout mCustomViewLayout;
    private Drawable mDrawable;
    private ImageView mIconView;
    private LinearLayout mImageFrame;
    private TextView mListSummaryView;
    private TextView mListTitleView;
    private LinearLayout mRootLayout;

    public ListItemView(Context context) {
        this(context, null);
    }

    public ListItemView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        LayoutInflater.from(context).inflate(R$layout.control_listitem_view, this);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.ListItemView, R$attr.ListItemViewStyle, 0);
        initView();
        if (obtainStyledAttributes.hasValue(R$styleable.ListItemView_itemIcon)) {
            setIcon(obtainStyledAttributes.getDrawable(R$styleable.ListItemView_itemIcon));
        }
        setTitle(obtainStyledAttributes.getString(R$styleable.ListItemView_title));
        setSummary(obtainStyledAttributes.getString(R$styleable.ListItemView_summary));
        obtainStyledAttributes.recycle();
    }

    private void initView() {
        this.mIconView = (ImageView) findViewById(R$id.icon);
        this.mAvataIconView = (SelectedAvatarView) findViewById(R$id.avatar_icon);
        this.mListTitleView = (TextView) findViewById(R$id.list_title);
        this.mListSummaryView = (TextView) findViewById(R$id.list_summary);
        findViewById(R$id.divider);
        this.mCustomViewLayout = (LinearLayout) findViewById(R$id.list_widget_frame);
        this.mRootLayout = (LinearLayout) findViewById(R$id.root_layout);
        this.mImageFrame = (LinearLayout) findViewById(R$id.icon_frame);
    }

    public void setIcon(Drawable drawable) {
        ImageView imageView;
        if (drawable != null && (imageView = this.mIconView) != null && this.mDrawable != drawable) {
            this.mDrawable = drawable;
            imageView.setImageDrawable(drawable);
            LinearLayout linearLayout = this.mImageFrame;
            if (linearLayout != null) {
                linearLayout.setVisibility(0);
            }
            SelectedAvatarView selectedAvatarView = this.mAvataIconView;
            if (selectedAvatarView != null) {
                selectedAvatarView.setVisibility(8);
            }
        }
    }

    public void setTitle(CharSequence charSequence) {
        TextView textView = this.mListTitleView;
        if (textView != null) {
            textView.setText(charSequence);
        }
    }

    public void setSummary(CharSequence charSequence) {
        TextView textView = this.mListSummaryView;
        if (textView != null) {
            textView.setText(charSequence);
        }
    }

    public void addCustomView(View view) {
        LinearLayout linearLayout = this.mCustomViewLayout;
        if (linearLayout != null) {
            linearLayout.removeAllViews();
            this.mCustomViewLayout.setVisibility(0);
            this.mCustomViewLayout.addView(view);
            LinearLayout linearLayout2 = this.mRootLayout;
            linearLayout2.setPadding(linearLayout2.getPaddingLeft(), this.mRootLayout.getPaddingTop(), getResources().getDimensionPixelOffset(R$dimen.op_control_margin_space2), this.mRootLayout.getPaddingBottom());
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        LinearLayout linearLayout;
        super.onAttachedToWindow();
        boolean z = true;
        if (TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) != 1) {
            z = false;
        }
        if (z && (linearLayout = this.mRootLayout) != null && this.mCustomViewLayout != null) {
            linearLayout.setPadding(getResources().getDimensionPixelOffset(R$dimen.op_control_margin_space2), this.mRootLayout.getPaddingTop(), this.mRootLayout.getPaddingRight(), this.mRootLayout.getPaddingBottom());
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.View, android.view.ViewGroup
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        if (this.mListSummaryView != null) {
            CharSequence summary = getSummary();
            if (!TextUtils.isEmpty(summary)) {
                this.mListSummaryView.setText(summary);
                this.mListSummaryView.setVisibility(0);
                this.mListSummaryView.getCurrentTextColor();
            } else {
                this.mListSummaryView.setVisibility(8);
            }
        }
        if (this.mListTitleView != null) {
            CharSequence title = getTitle();
            if (!TextUtils.isEmpty(title)) {
                this.mListTitleView.setText(title);
                this.mListTitleView.setVisibility(0);
            } else {
                this.mListTitleView.setVisibility(8);
            }
        }
        ImageView imageView = this.mIconView;
        if (imageView != null) {
            Drawable drawable = this.mDrawable;
            if (drawable != null) {
                imageView.setImageDrawable(drawable);
            }
            if (this.mDrawable != null) {
                this.mIconView.setVisibility(0);
            } else {
                this.mIconView.setVisibility(8);
            }
        }
        SelectedAvatarView selectedAvatarView = this.mAvataIconView;
        if (selectedAvatarView != null) {
            Drawable drawable2 = this.mAvataIconDrawable;
            if (drawable2 != null) {
                selectedAvatarView.setImageDrawable(drawable2);
            }
            if (this.mAvataIconDrawable != null) {
                this.mAvataIconView.setVisibility(0);
            } else {
                this.mAvataIconView.setVisibility(8);
            }
        }
        boolean z2 = true;
        if (this.mCustomViewLayout.getVisibility() == 0) {
            if (TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) != 1) {
                z2 = false;
            }
            ViewGroup.LayoutParams layoutParams = getLayoutParams();
            if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                ((ViewGroup.MarginLayoutParams) layoutParams).rightMargin = 0;
                setLayoutParams(layoutParams);
            }
            if (z2) {
                LinearLayout linearLayout = this.mRootLayout;
                linearLayout.setPadding(0, linearLayout.getPaddingTop(), 0, this.mRootLayout.getPaddingBottom());
            } else {
                LinearLayout linearLayout2 = this.mRootLayout;
                linearLayout2.setPadding(linearLayout2.getPaddingLeft(), this.mRootLayout.getPaddingTop(), getResources().getDimensionPixelOffset(R$dimen.op_control_margin_space2), this.mRootLayout.getPaddingBottom());
            }
        } else {
            if (TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) != 1) {
                z2 = false;
            }
            if (!z2) {
                LinearLayout linearLayout3 = this.mRootLayout;
                linearLayout3.setPadding(linearLayout3.getPaddingLeft(), this.mRootLayout.getPaddingTop(), getResources().getDimensionPixelOffset(R$dimen.op_control_margin_screen_right2), this.mRootLayout.getPaddingBottom());
            } else {
                this.mRootLayout.setPadding(getResources().getDimensionPixelOffset(R$dimen.op_control_margin_space2), this.mRootLayout.getPaddingTop(), this.mRootLayout.getPaddingRight(), this.mRootLayout.getPaddingBottom());
            }
            if (!TextUtils.isEmpty(this.mListSummaryView.getText()) && this.mListSummaryView.getText().length() > 30) {
                LinearLayout linearLayout4 = this.mRootLayout;
                linearLayout4.setPadding(linearLayout4.getPaddingLeft(), this.mRootLayout.getPaddingTop(), 0, this.mRootLayout.getPaddingBottom());
                ViewGroup.LayoutParams layoutParams2 = getLayoutParams();
                if (layoutParams2 instanceof ViewGroup.MarginLayoutParams) {
                    ((ViewGroup.MarginLayoutParams) layoutParams2).rightMargin = getResources().getDimensionPixelOffset(R$dimen.op_control_margin_screen_right2);
                    setLayoutParams(layoutParams2);
                }
            }
            if (!TextUtils.isEmpty(this.mListTitleView.getText()) && this.mListTitleView.getMeasuredWidth() > getResources().getDisplayMetrics().widthPixels - 400) {
                LinearLayout linearLayout5 = this.mRootLayout;
                linearLayout5.setPadding(linearLayout5.getPaddingLeft(), this.mRootLayout.getPaddingTop(), 0, this.mRootLayout.getPaddingBottom());
                ViewGroup.LayoutParams layoutParams3 = getLayoutParams();
                if (layoutParams3 instanceof ViewGroup.MarginLayoutParams) {
                    ((ViewGroup.MarginLayoutParams) layoutParams3).rightMargin = getResources().getDimensionPixelOffset(R$dimen.op_control_margin_screen_right2);
                    setLayoutParams(layoutParams3);
                }
            }
        }
        View findViewById = findViewById(R$id.text_layout);
        if (findViewById != null) {
            LinearLayout.LayoutParams layoutParams4 = (LinearLayout.LayoutParams) findViewById.getLayoutParams();
            if (!(findViewById == null || layoutParams4 == null)) {
                if (!isSummaryEmpty()) {
                    layoutParams4.topMargin = getResources().getDimensionPixelSize(R$dimen.op_control_margin_list_top2);
                    layoutParams4.bottomMargin = getResources().getDimensionPixelSize(R$dimen.op_control_margin_list_bottom2);
                } else {
                    layoutParams4.topMargin = getResources().getDimensionPixelSize(R$dimen.op_control_margin_list_top4);
                    layoutParams4.bottomMargin = getResources().getDimensionPixelSize(R$dimen.op_control_margin_list_bottom4);
                }
                findViewById.setLayoutParams(layoutParams4);
            }
        }
        LinearLayout linearLayout6 = this.mImageFrame;
        if (linearLayout6 != null) {
            LinearLayout.LayoutParams layoutParams5 = (LinearLayout.LayoutParams) linearLayout6.getLayoutParams();
            if (!(findViewById == null || layoutParams5 == null)) {
                if (!isSummaryEmpty()) {
                    layoutParams5.gravity = 8388659;
                    layoutParams5.topMargin = getResources().getDimensionPixelSize(R$dimen.op_control_margin_list_top3);
                } else {
                    layoutParams5.gravity = 16;
                    layoutParams5.topMargin = 0;
                }
                this.mImageFrame.setLayoutParams(layoutParams5);
            }
        }
        super.onLayout(z, i, i2, i3, i4);
    }

    private boolean isSummaryEmpty() {
        return this.mListSummaryView == null || TextUtils.isEmpty(getSummary());
    }

    private CharSequence getTitle() {
        TextView textView = this.mListTitleView;
        if (textView != null) {
            return textView.getText();
        }
        return null;
    }

    private CharSequence getSummary() {
        TextView textView = this.mListSummaryView;
        if (textView != null) {
            return textView.getText();
        }
        return null;
    }
}
