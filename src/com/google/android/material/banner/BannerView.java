package com.google.android.material.banner;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.material.R$attr;
import com.google.android.material.R$id;
import com.google.android.material.R$layout;
public class BannerView extends FrameLayout {
    public BannerView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, R$attr.bannerViewStyle);
    }

    public BannerView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        LayoutInflater.from(context).inflate(R$layout.control_banner_view, this);
        initView();
        setVisibility(4);
    }

    private void initView() {
        ImageView imageView = (ImageView) findViewById(R$id.banner_icon);
        TextView textView = (TextView) findViewById(R$id.banner_title);
        Button button = (Button) findViewById(R$id.single_action_button);
        Button button2 = (Button) findViewById(R$id.multi_action_button_left);
        Button button3 = (Button) findViewById(R$id.multi_action_button_right);
        LinearLayout linearLayout = (LinearLayout) findViewById(R$id.banner_vertical_button_layout);
        LinearLayout linearLayout2 = (LinearLayout) findViewById(R$id.banner_text_layout);
        LinearLayout linearLayout3 = (LinearLayout) findViewById(R$id.banner_layout);
    }
}
