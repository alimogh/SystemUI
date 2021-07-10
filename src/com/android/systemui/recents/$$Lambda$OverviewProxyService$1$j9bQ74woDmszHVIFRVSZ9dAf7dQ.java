package com.android.systemui.recents;

import com.android.systemui.stackdivider.Divider;
import java.util.function.Function;
/* compiled from: lambda */
/* renamed from: com.android.systemui.recents.-$$Lambda$OverviewProxyService$1$j9bQ74woDmszHVIFRVSZ9dAf7dQ  reason: invalid class name */
public final /* synthetic */ class $$Lambda$OverviewProxyService$1$j9bQ74woDmszHVIFRVSZ9dAf7dQ implements Function {
    public static final /* synthetic */ $$Lambda$OverviewProxyService$1$j9bQ74woDmszHVIFRVSZ9dAf7dQ INSTANCE = new $$Lambda$OverviewProxyService$1$j9bQ74woDmszHVIFRVSZ9dAf7dQ();

    private /* synthetic */ $$Lambda$OverviewProxyService$1$j9bQ74woDmszHVIFRVSZ9dAf7dQ() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return ((Divider) obj).getView().getNonMinimizedSplitScreenSecondaryBounds();
    }
}
