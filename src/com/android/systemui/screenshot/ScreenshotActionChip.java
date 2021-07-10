package com.android.systemui.screenshot;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.systemui.C0004R$color;
import com.android.systemui.C0008R$id;
public class ScreenshotActionChip extends FrameLayout {
    public ScreenshotActionChip(Context context) {
        this(context, null);
    }

    public ScreenshotActionChip(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ScreenshotActionChip(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public ScreenshotActionChip(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        context.getColor(C0004R$color.global_screenshot_button_icon);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        ImageView imageView = (ImageView) findViewById(C0008R$id.screenshot_action_chip_icon);
        TextView textView = (TextView) findViewById(C0008R$id.screenshot_action_chip_text);
    }
}
