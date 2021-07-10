package com.google.android.material.tooltipview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.material.R$id;
import com.google.android.material.R$layout;
public class BubbleView extends LinearLayout {
    public BubbleView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        LayoutInflater.from(context).inflate(R$layout.control_bubble_view, this);
        initView();
        setWillNotDraw(false);
    }

    private void initView() {
        TextView textView = (TextView) findViewById(R$id.title);
        TextView textView2 = (TextView) findViewById(R$id.message);
        Button button = (Button) findViewById(R$id.button1);
        Button button2 = (Button) findViewById(R$id.button2);
        ImageView imageView = (ImageView) findViewById(R$id.bubble_image);
    }
}
