package com.android.systemui.controls.controller;

import android.content.ComponentName;
import android.service.controls.Control;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import kotlin.Pair;
import kotlin.collections.CollectionsKt__CollectionsKt;
import kotlin.collections.CollectionsKt__IterablesKt;
import kotlin.collections.CollectionsKt__MutableCollectionsKt;
import kotlin.collections.MapsKt__MapsKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.ranges.RangesKt___RangesKt;
import org.jetbrains.annotations.NotNull;
/* compiled from: ControlsControllerImpl.kt */
/* access modifiers changed from: package-private */
public final class Favorites {
    public static final Favorites INSTANCE = new Favorites();
    private static Map<ComponentName, ? extends List<StructureInfo>> favMap = MapsKt__MapsKt.emptyMap();

    private Favorites() {
    }

    @NotNull
    public final List<StructureInfo> getAllStructures() {
        Map<ComponentName, ? extends List<StructureInfo>> map = favMap;
        ArrayList arrayList = new ArrayList();
        for (Map.Entry<ComponentName, ? extends List<StructureInfo>> entry : map.entrySet()) {
            boolean unused = CollectionsKt__MutableCollectionsKt.addAll(arrayList, (List) entry.getValue());
        }
        return arrayList;
    }

    @NotNull
    public final List<StructureInfo> getStructuresForComponent(@NotNull ComponentName componentName) {
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        List<StructureInfo> list = (List) favMap.get(componentName);
        return list != null ? list : CollectionsKt__CollectionsKt.emptyList();
    }

    @NotNull
    public final List<ControlInfo> getControlsForStructure(@NotNull StructureInfo structureInfo) {
        Object obj;
        List<ControlInfo> controls;
        Intrinsics.checkParameterIsNotNull(structureInfo, "structure");
        Iterator<T> it = getStructuresForComponent(structureInfo.getComponentName()).iterator();
        while (true) {
            if (!it.hasNext()) {
                obj = null;
                break;
            }
            obj = it.next();
            if (Intrinsics.areEqual(((StructureInfo) obj).getStructure(), structureInfo.getStructure())) {
                break;
            }
        }
        StructureInfo structureInfo2 = (StructureInfo) obj;
        return (structureInfo2 == null || (controls = structureInfo2.getControls()) == null) ? CollectionsKt__CollectionsKt.emptyList() : controls;
    }

    @NotNull
    public final List<ControlInfo> getControlsForComponent(@NotNull ComponentName componentName) {
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        List<StructureInfo> structuresForComponent = getStructuresForComponent(componentName);
        ArrayList arrayList = new ArrayList();
        for (StructureInfo structureInfo : structuresForComponent) {
            boolean unused = CollectionsKt__MutableCollectionsKt.addAll(arrayList, structureInfo.getControls());
        }
        return arrayList;
    }

    public final void removeStructures(@NotNull ComponentName componentName) {
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        Map<ComponentName, ? extends List<StructureInfo>> map = MapsKt__MapsKt.toMutableMap(favMap);
        map.remove(componentName);
        favMap = map;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x006d, code lost:
        if (r1 != null) goto L_0x0079;
     */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0043 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0044  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final boolean addFavorite(@org.jetbrains.annotations.NotNull android.content.ComponentName r10, @org.jetbrains.annotations.NotNull java.lang.CharSequence r11, @org.jetbrains.annotations.NotNull com.android.systemui.controls.controller.ControlInfo r12) {
        /*
            r9 = this;
            java.lang.String r0 = "componentName"
            kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(r10, r0)
            java.lang.String r0 = "structureName"
            kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(r11, r0)
            java.lang.String r0 = "controlInfo"
            kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(r12, r0)
            java.util.List r0 = r9.getControlsForComponent(r10)
            boolean r1 = r0 instanceof java.util.Collection
            r2 = 1
            r3 = 0
            if (r1 == 0) goto L_0x0022
            boolean r1 = r0.isEmpty()
            if (r1 == 0) goto L_0x0022
        L_0x0020:
            r0 = r3
            goto L_0x0041
        L_0x0022:
            java.util.Iterator r0 = r0.iterator()
        L_0x0026:
            boolean r1 = r0.hasNext()
            if (r1 == 0) goto L_0x0020
            java.lang.Object r1 = r0.next()
            com.android.systemui.controls.controller.ControlInfo r1 = (com.android.systemui.controls.controller.ControlInfo) r1
            java.lang.String r1 = r1.getControlId()
            java.lang.String r4 = r12.getControlId()
            boolean r1 = kotlin.jvm.internal.Intrinsics.areEqual(r1, r4)
            if (r1 == 0) goto L_0x0026
            r0 = r2
        L_0x0041:
            if (r0 == 0) goto L_0x0044
            return r3
        L_0x0044:
            java.util.Map<android.content.ComponentName, ? extends java.util.List<com.android.systemui.controls.controller.StructureInfo>> r0 = com.android.systemui.controls.controller.Favorites.favMap
            java.lang.Object r0 = r0.get(r10)
            java.util.List r0 = (java.util.List) r0
            if (r0 == 0) goto L_0x0070
            java.util.Iterator r0 = r0.iterator()
        L_0x0052:
            boolean r1 = r0.hasNext()
            if (r1 == 0) goto L_0x006a
            java.lang.Object r1 = r0.next()
            r3 = r1
            com.android.systemui.controls.controller.StructureInfo r3 = (com.android.systemui.controls.controller.StructureInfo) r3
            java.lang.CharSequence r3 = r3.getStructure()
            boolean r3 = kotlin.jvm.internal.Intrinsics.areEqual(r3, r11)
            if (r3 == 0) goto L_0x0052
            goto L_0x006b
        L_0x006a:
            r1 = 0
        L_0x006b:
            com.android.systemui.controls.controller.StructureInfo r1 = (com.android.systemui.controls.controller.StructureInfo) r1
            if (r1 == 0) goto L_0x0070
            goto L_0x0079
        L_0x0070:
            com.android.systemui.controls.controller.StructureInfo r1 = new com.android.systemui.controls.controller.StructureInfo
            java.util.List r0 = kotlin.collections.CollectionsKt.emptyList()
            r1.<init>(r10, r11, r0)
        L_0x0079:
            r3 = r1
            r4 = 0
            r5 = 0
            java.util.List r10 = r3.getControls()
            java.util.List r6 = kotlin.collections.CollectionsKt.plus(r10, r12)
            r7 = 3
            r8 = 0
            com.android.systemui.controls.controller.StructureInfo r10 = com.android.systemui.controls.controller.StructureInfo.copy$default(r3, r4, r5, r6, r7, r8)
            r9.replaceControls(r10)
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.controls.controller.Favorites.addFavorite(android.content.ComponentName, java.lang.CharSequence, com.android.systemui.controls.controller.ControlInfo):boolean");
    }

    public final void replaceControls(@NotNull StructureInfo structureInfo) {
        Intrinsics.checkParameterIsNotNull(structureInfo, "updatedStructure");
        Map<ComponentName, ? extends List<StructureInfo>> map = MapsKt__MapsKt.toMutableMap(favMap);
        ArrayList arrayList = new ArrayList();
        ComponentName componentName = structureInfo.getComponentName();
        boolean z = false;
        for (StructureInfo structureInfo2 : getStructuresForComponent(componentName)) {
            if (Intrinsics.areEqual(structureInfo2.getStructure(), structureInfo.getStructure())) {
                z = true;
                structureInfo2 = structureInfo;
            }
            if (!structureInfo2.getControls().isEmpty()) {
                arrayList.add(structureInfo2);
            }
        }
        if (!z && !structureInfo.getControls().isEmpty()) {
            arrayList.add(structureInfo);
        }
        map.put(componentName, arrayList);
        favMap = map;
    }

    public final void clear() {
        favMap = MapsKt__MapsKt.emptyMap();
    }

    public final boolean updateControls(@NotNull ComponentName componentName, @NotNull List<Control> list) {
        Pair pair;
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        Intrinsics.checkParameterIsNotNull(list, "controls");
        LinkedHashMap linkedHashMap = new LinkedHashMap(RangesKt___RangesKt.coerceAtLeast(MapsKt__MapsKt.mapCapacity(CollectionsKt__IterablesKt.collectionSizeOrDefault(list, 10)), 16));
        for (Object obj : list) {
            linkedHashMap.put(((Control) obj).getControlId(), obj);
        }
        LinkedHashMap linkedHashMap2 = new LinkedHashMap();
        boolean z = false;
        for (StructureInfo structureInfo : getStructuresForComponent(componentName)) {
            for (ControlInfo controlInfo : structureInfo.getControls()) {
                Control control = (Control) linkedHashMap.get(controlInfo.getControlId());
                if (control != null) {
                    if ((!Intrinsics.areEqual(control.getTitle(), controlInfo.getControlTitle())) || (!Intrinsics.areEqual(control.getSubtitle(), controlInfo.getControlSubtitle())) || control.getDeviceType() != controlInfo.getDeviceType()) {
                        CharSequence title = control.getTitle();
                        Intrinsics.checkExpressionValueIsNotNull(title, "updatedControl.title");
                        CharSequence subtitle = control.getSubtitle();
                        Intrinsics.checkExpressionValueIsNotNull(subtitle, "updatedControl.subtitle");
                        controlInfo = ControlInfo.copy$default(controlInfo, null, title, subtitle, control.getDeviceType(), 1, null);
                        z = true;
                    }
                    CharSequence structure = control.getStructure();
                    if (structure == null) {
                        structure = "";
                    }
                    if (!Intrinsics.areEqual(structureInfo.getStructure(), structure)) {
                        z = true;
                    }
                    pair = new Pair(structure, controlInfo);
                } else {
                    pair = new Pair(structureInfo.getStructure(), controlInfo);
                }
                CharSequence charSequence = (CharSequence) pair.component1();
                ControlInfo controlInfo2 = (ControlInfo) pair.component2();
                Object obj2 = linkedHashMap2.get(charSequence);
                if (obj2 == null) {
                    obj2 = new ArrayList();
                    linkedHashMap2.put(charSequence, obj2);
                }
                ((List) obj2).add(controlInfo2);
            }
        }
        if (!z) {
            return false;
        }
        ArrayList arrayList = new ArrayList(linkedHashMap2.size());
        for (Map.Entry entry : linkedHashMap2.entrySet()) {
            arrayList.add(new StructureInfo(componentName, (CharSequence) entry.getKey(), (List) entry.getValue()));
        }
        Map<ComponentName, ? extends List<StructureInfo>> map = MapsKt__MapsKt.toMutableMap(favMap);
        map.put(componentName, arrayList);
        favMap = map;
        return true;
    }

    public final void load(@NotNull List<StructureInfo> list) {
        Intrinsics.checkParameterIsNotNull(list, "structures");
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        for (Object obj : list) {
            ComponentName componentName = ((StructureInfo) obj).getComponentName();
            Object obj2 = linkedHashMap.get(componentName);
            if (obj2 == null) {
                obj2 = new ArrayList();
                linkedHashMap.put(componentName, obj2);
            }
            ((List) obj2).add(obj);
        }
        favMap = linkedHashMap;
    }
}
