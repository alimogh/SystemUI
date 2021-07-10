package com.android.settingslib.core.instrumentation;

import android.content.Context;
public class MetricsFeatureProvider {
    public abstract void hidden(Context context, int i, int i2);

    public abstract void visible(Context context, int i, int i2, int i3);
}
