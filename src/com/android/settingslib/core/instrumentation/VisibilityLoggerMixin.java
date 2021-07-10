package com.android.settingslib.core.instrumentation;

import android.os.SystemClock;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.OnLifecycleEvent;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnAttach;
public class VisibilityLoggerMixin implements LifecycleObserver, OnAttach {
    private long mCreationTimestamp;
    private final int mMetricsCategory;
    private MetricsFeatureProvider mMetricsFeature;
    private int mSourceMetricsCategory;
    private long mVisibleTimestamp;

    @Override // com.android.settingslib.core.lifecycle.events.OnAttach
    public void onAttach() {
        this.mCreationTimestamp = SystemClock.elapsedRealtime();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume() {
        if (this.mMetricsFeature != null && this.mMetricsCategory != 0) {
            long elapsedRealtime = SystemClock.elapsedRealtime();
            this.mVisibleTimestamp = elapsedRealtime;
            long j = this.mCreationTimestamp;
            if (j != 0) {
                this.mMetricsFeature.visible(null, this.mSourceMetricsCategory, this.mMetricsCategory, (int) (elapsedRealtime - j));
                return;
            }
            this.mMetricsFeature.visible(null, this.mSourceMetricsCategory, this.mMetricsCategory, 0);
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPause() {
        this.mCreationTimestamp = 0;
        if (this.mMetricsFeature != null && this.mMetricsCategory != 0) {
            this.mMetricsFeature.hidden(null, this.mMetricsCategory, (int) (SystemClock.elapsedRealtime() - this.mVisibleTimestamp));
        }
    }
}
