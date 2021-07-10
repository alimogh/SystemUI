package com.android.systemui.util;

import android.graphics.Rect;
import android.util.Log;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.PropertyReference0Impl;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KProperty;
import org.jetbrains.annotations.NotNull;
/* compiled from: FloatingContentCoordinator.kt */
public final class FloatingContentCoordinator {
    public static final Companion Companion = new Companion(null);
    private final Map<FloatingContent, Rect> allContentBounds = new HashMap();
    private boolean currentlyResolvingConflicts;

    /* compiled from: FloatingContentCoordinator.kt */
    public interface FloatingContent {
        @NotNull
        Rect getAllowedFloatingBoundsRegion();

        @NotNull
        Rect getFloatingBoundsOnScreen();

        void moveToBounds(@NotNull Rect rect);

        @NotNull
        default Rect calculateNewBoundsOnOverlap(@NotNull Rect rect, @NotNull List<Rect> list) {
            Intrinsics.checkParameterIsNotNull(rect, "overlappingContentBounds");
            Intrinsics.checkParameterIsNotNull(list, "otherContentBounds");
            return FloatingContentCoordinator.Companion.findAreaForContentVertically(getFloatingBoundsOnScreen(), rect, list, getAllowedFloatingBoundsRegion());
        }
    }

    public final void onContentAdded(@NotNull FloatingContent floatingContent) {
        Intrinsics.checkParameterIsNotNull(floatingContent, "newContent");
        updateContentBounds();
        this.allContentBounds.put(floatingContent, floatingContent.getFloatingBoundsOnScreen());
        maybeMoveConflictingContent(floatingContent);
    }

    public final void onContentMoved(@NotNull FloatingContent floatingContent) {
        Intrinsics.checkParameterIsNotNull(floatingContent, "content");
        if (!this.currentlyResolvingConflicts) {
            if (!this.allContentBounds.containsKey(floatingContent)) {
                Log.wtf("FloatingCoordinator", "Received onContentMoved call before onContentAdded! This should never happen.");
                return;
            }
            updateContentBounds();
            maybeMoveConflictingContent(floatingContent);
        }
    }

    public final void onContentRemoved(@NotNull FloatingContent floatingContent) {
        Intrinsics.checkParameterIsNotNull(floatingContent, "removedContent");
        this.allContentBounds.remove(floatingContent);
    }

    private final void maybeMoveConflictingContent(FloatingContent floatingContent) {
        this.currentlyResolvingConflicts = true;
        Rect rect = this.allContentBounds.get(floatingContent);
        if (rect != null) {
            Rect rect2 = rect;
            Map<FloatingContent, Rect> map = this.allContentBounds;
            LinkedHashMap linkedHashMap = new LinkedHashMap();
            Iterator<Map.Entry<FloatingContent, Rect>> it = map.entrySet().iterator();
            while (true) {
                boolean z = false;
                if (!it.hasNext()) {
                    break;
                }
                Map.Entry<FloatingContent, Rect> next = it.next();
                Rect value = next.getValue();
                if ((!Intrinsics.areEqual(next.getKey(), floatingContent)) && Rect.intersects(rect2, value)) {
                    z = true;
                }
                if (z) {
                    linkedHashMap.put(next.getKey(), next.getValue());
                }
            }
            for (Map.Entry entry : linkedHashMap.entrySet()) {
                FloatingContent floatingContent2 = (FloatingContent) entry.getKey();
                Rect calculateNewBoundsOnOverlap = floatingContent2.calculateNewBoundsOnOverlap(rect2, CollectionsKt___CollectionsKt.minus(CollectionsKt___CollectionsKt.minus(this.allContentBounds.values(), (Rect) entry.getValue()), rect2));
                if (!calculateNewBoundsOnOverlap.isEmpty()) {
                    floatingContent2.moveToBounds(calculateNewBoundsOnOverlap);
                    this.allContentBounds.put(floatingContent2, floatingContent2.getFloatingBoundsOnScreen());
                }
            }
            this.currentlyResolvingConflicts = false;
            return;
        }
        Intrinsics.throwNpe();
        throw null;
    }

    private final void updateContentBounds() {
        for (FloatingContent floatingContent : this.allContentBounds.keySet()) {
            this.allContentBounds.put(floatingContent, floatingContent.getFloatingBoundsOnScreen());
        }
    }

    /* compiled from: FloatingContentCoordinator.kt */
    public static final class Companion {
        static final /* synthetic */ KProperty[] $$delegatedProperties;

        static {
            PropertyReference0Impl propertyReference0Impl = new PropertyReference0Impl(Reflection.getOrCreateKotlinClass(Companion.class), "newContentBoundsAbove", "<v#0>");
            Reflection.property0(propertyReference0Impl);
            PropertyReference0Impl propertyReference0Impl2 = new PropertyReference0Impl(Reflection.getOrCreateKotlinClass(Companion.class), "newContentBoundsBelow", "<v#1>");
            Reflection.property0(propertyReference0Impl2);
            PropertyReference0Impl propertyReference0Impl3 = new PropertyReference0Impl(Reflection.getOrCreateKotlinClass(Companion.class), "positionAboveInBounds", "<v#2>");
            Reflection.property0(propertyReference0Impl3);
            PropertyReference0Impl propertyReference0Impl4 = new PropertyReference0Impl(Reflection.getOrCreateKotlinClass(Companion.class), "positionBelowInBounds", "<v#3>");
            Reflection.property0(propertyReference0Impl4);
            $$delegatedProperties = new KProperty[]{propertyReference0Impl, propertyReference0Impl2, propertyReference0Impl3, propertyReference0Impl4};
        }

        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        /* JADX WARN: Type inference failed for: r12v4, types: [java.util.List, T] */
        /* JADX WARN: Type inference failed for: r12v6, types: [java.util.List, T] */
        /* JADX WARNING: Unknown variable types count: 2 */
        @org.jetbrains.annotations.NotNull
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public final android.graphics.Rect findAreaForContentVertically(@org.jetbrains.annotations.NotNull android.graphics.Rect r10, @org.jetbrains.annotations.NotNull android.graphics.Rect r11, @org.jetbrains.annotations.NotNull java.util.Collection<android.graphics.Rect> r12, @org.jetbrains.annotations.NotNull android.graphics.Rect r13) {
            /*
            // Method dump skipped, instructions count: 256
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.util.FloatingContentCoordinator.Companion.findAreaForContentVertically(android.graphics.Rect, android.graphics.Rect, java.util.Collection, android.graphics.Rect):android.graphics.Rect");
        }

        private final boolean rectsIntersectVertically(Rect rect, Rect rect2) {
            int i;
            int i2 = rect.left;
            return (i2 >= rect2.left && i2 <= rect2.right) || ((i = rect.right) <= rect2.right && i >= rect2.left);
        }

        @NotNull
        public final Rect findAreaForContentAboveOrBelow(@NotNull Rect rect, @NotNull Collection<Rect> collection, boolean z) {
            Intrinsics.checkParameterIsNotNull(rect, "contentRect");
            Intrinsics.checkParameterIsNotNull(collection, "exclusionRects");
            List<Rect> list = CollectionsKt___CollectionsKt.sortedWith(collection, new FloatingContentCoordinator$Companion$findAreaForContentAboveOrBelow$$inlined$sortedBy$1(z));
            Rect rect2 = new Rect(rect);
            for (Rect rect3 : list) {
                if (!Rect.intersects(rect2, rect3)) {
                    break;
                }
                rect2.offsetTo(rect2.left, rect3.top + (z ? -rect.height() : rect3.height()));
            }
            return rect2;
        }
    }
}
