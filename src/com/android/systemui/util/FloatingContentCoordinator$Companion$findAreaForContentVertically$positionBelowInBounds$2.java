package com.android.systemui.util;

import android.graphics.Rect;
import kotlin.Lazy;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Lambda;
import kotlin.reflect.KProperty;
/* compiled from: FloatingContentCoordinator.kt */
/* access modifiers changed from: package-private */
public final class FloatingContentCoordinator$Companion$findAreaForContentVertically$positionBelowInBounds$2 extends Lambda implements Function0<Boolean> {
    final /* synthetic */ Rect $allowedBounds;
    final /* synthetic */ Lazy $newContentBoundsBelow;
    final /* synthetic */ KProperty $newContentBoundsBelow$metadata;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    FloatingContentCoordinator$Companion$findAreaForContentVertically$positionBelowInBounds$2(Rect rect, Lazy lazy, KProperty kProperty) {
        super(0);
        this.$allowedBounds = rect;
        this.$newContentBoundsBelow = lazy;
        this.$newContentBoundsBelow$metadata = kProperty;
    }

    /* Return type fixed from 'boolean' to match base method */
    /* JADX WARN: Type inference failed for: r1v4, types: [boolean, java.lang.Boolean] */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Override // kotlin.jvm.functions.Function0
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final java.lang.Boolean invoke() {
        /*
            r1 = this;
            android.graphics.Rect r0 = r1.$allowedBounds
            kotlin.Lazy r1 = r1.$newContentBoundsBelow
            java.lang.Object r1 = r1.getValue()
            android.graphics.Rect r1 = (android.graphics.Rect) r1
            boolean r1 = r0.contains(r1)
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.util.FloatingContentCoordinator$Companion$findAreaForContentVertically$positionBelowInBounds$2.invoke():boolean");
    }
}
