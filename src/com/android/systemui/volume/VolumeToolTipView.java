package com.android.systemui.volume;

import android.content.Context;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.core.content.ContextCompat;
import com.android.systemui.C0004R$color;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0008R$id;
import com.android.systemui.recents.TriangleShape;
public class VolumeToolTipView extends LinearLayout {
    private Context mContext;

    public VolumeToolTipView(Context context) {
        super(context);
        this.mContext = context;
    }

    public VolumeToolTipView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mContext = context;
    }

    public VolumeToolTipView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mContext = context;
    }

    public VolumeToolTipView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mContext = context;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        drawArrow();
    }

    private void drawArrow() {
        View findViewById = findViewById(C0008R$id.arrow_left);
        View findViewById2 = findViewById(C0008R$id.arrow_right);
        if (isLandscape()) {
            findViewById.setVisibility(8);
            findViewById2.setVisibility(0);
            findViewById = findViewById2;
        } else {
            findViewById.setVisibility(0);
            findViewById2.setVisibility(8);
        }
        ViewGroup.LayoutParams layoutParams = findViewById.getLayoutParams();
        ShapeDrawable shapeDrawable = new ShapeDrawable(TriangleShape.createHorizontal((float) layoutParams.width, (float) layoutParams.height, !isLandscape()));
        Paint paint = shapeDrawable.getPaint();
        paint.setColor(ContextCompat.getColor(getContext(), C0004R$color.android_volume_panel_seekbar_color));
        paint.setPathEffect(new CornerPathEffect(getResources().getDimension(C0005R$dimen.volume_tool_tip_arrow_corner_radius)));
        findViewById.setBackground(shapeDrawable);
    }

    private boolean isLandscape() {
        return this.mContext.getResources().getConfiguration().orientation == 2;
    }
}
