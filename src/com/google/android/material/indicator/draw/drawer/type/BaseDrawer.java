package com.google.android.material.indicator.draw.drawer.type;

import android.graphics.Paint;
import com.google.android.material.indicator.draw.data.Indicator;
/* access modifiers changed from: package-private */
public class BaseDrawer {
    Indicator indicator;
    Paint paint;

    BaseDrawer(Paint paint, Indicator indicator) {
        this.paint = paint;
        this.indicator = indicator;
    }
}
