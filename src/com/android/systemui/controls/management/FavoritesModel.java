package com.android.systemui.controls.management;

import android.content.ComponentName;
import android.util.Log;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.android.systemui.controls.ControlInterface;
import com.android.systemui.controls.controller.ControlInfo;
import com.android.systemui.controls.management.ControlsModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import kotlin.TypeCastException;
import kotlin.collections.CollectionsKt__IterablesKt;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: FavoritesModel.kt */
public final class FavoritesModel implements ControlsModel {
    private RecyclerView.Adapter<?> adapter;
    private final ComponentName componentName;
    private int dividerPosition;
    @NotNull
    private final List<ElementWrapper> elements;
    private final FavoritesModelCallback favoritesModelCallback;
    @NotNull
    private final ItemTouchHelper.SimpleCallback itemTouchHelperCallback;
    private boolean modified;
    @NotNull
    private final ControlsModel.MoveHelper moveHelper = new ControlsModel.MoveHelper(this) { // from class: com.android.systemui.controls.management.FavoritesModel$moveHelper$1
        final /* synthetic */ FavoritesModel this$0;

        /* JADX WARN: Incorrect args count in method signature: ()V */
        {
            this.this$0 = r1;
        }

        @Override // com.android.systemui.controls.management.ControlsModel.MoveHelper
        public boolean canMoveBefore(int i) {
            return i > 0 && i < this.this$0.dividerPosition;
        }

        @Override // com.android.systemui.controls.management.ControlsModel.MoveHelper
        public boolean canMoveAfter(int i) {
            return i >= 0 && i < this.this$0.dividerPosition - 1;
        }

        @Override // com.android.systemui.controls.management.ControlsModel.MoveHelper
        public void moveBefore(int i) {
            if (!canMoveBefore(i)) {
                Log.w("FavoritesModel", "Cannot move position " + i + " before");
                return;
            }
            this.this$0.onMoveItem(i, i - 1);
        }

        @Override // com.android.systemui.controls.management.ControlsModel.MoveHelper
        public void moveAfter(int i) {
            if (!canMoveAfter(i)) {
                Log.w("FavoritesModel", "Cannot move position " + i + " after");
                return;
            }
            this.this$0.onMoveItem(i, i + 1);
        }
    };

    /* compiled from: FavoritesModel.kt */
    public interface FavoritesModelCallback extends ControlsModel.ControlsModelCallback {
        void onNoneChanged(boolean z);
    }

    public FavoritesModel(@NotNull ComponentName componentName, @NotNull List<ControlInfo> list, @NotNull FavoritesModelCallback favoritesModelCallback) {
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        Intrinsics.checkParameterIsNotNull(list, "favorites");
        Intrinsics.checkParameterIsNotNull(favoritesModelCallback, "favoritesModelCallback");
        this.componentName = componentName;
        this.favoritesModelCallback = favoritesModelCallback;
        ArrayList arrayList = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(list, 10));
        for (ControlInfo controlInfo : list) {
            arrayList.add(new ControlInfoWrapper(this.componentName, controlInfo, true));
        }
        this.elements = CollectionsKt___CollectionsKt.plus((Collection) arrayList, (Object) new DividerWrapper(false, false, 3, null));
        this.dividerPosition = getElements().size() - 1;
        this.itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(this, 0, 0) { // from class: com.android.systemui.controls.management.FavoritesModel$itemTouchHelperCallback$1
            private final int MOVEMENT = 15;
            final /* synthetic */ FavoritesModel this$0;

            @Override // androidx.recyclerview.widget.ItemTouchHelper.Callback
            public boolean isItemViewSwipeEnabled() {
                return false;
            }

            @Override // androidx.recyclerview.widget.ItemTouchHelper.Callback
            public void onSwiped(@NotNull RecyclerView.ViewHolder viewHolder, int i) {
                Intrinsics.checkParameterIsNotNull(viewHolder, "viewHolder");
            }

            {
                this.this$0 = r1;
            }

            @Override // androidx.recyclerview.widget.ItemTouchHelper.Callback
            public boolean onMove(@NotNull RecyclerView recyclerView, @NotNull RecyclerView.ViewHolder viewHolder, @NotNull RecyclerView.ViewHolder viewHolder2) {
                Intrinsics.checkParameterIsNotNull(recyclerView, "recyclerView");
                Intrinsics.checkParameterIsNotNull(viewHolder, "viewHolder");
                Intrinsics.checkParameterIsNotNull(viewHolder2, "target");
                this.this$0.onMoveItem(viewHolder.getAdapterPosition(), viewHolder2.getAdapterPosition());
                return true;
            }

            @Override // androidx.recyclerview.widget.ItemTouchHelper.Callback
            public int getMovementFlags(@NotNull RecyclerView recyclerView, @NotNull RecyclerView.ViewHolder viewHolder) {
                Intrinsics.checkParameterIsNotNull(recyclerView, "recyclerView");
                Intrinsics.checkParameterIsNotNull(viewHolder, "viewHolder");
                if (viewHolder.getAdapterPosition() < this.this$0.dividerPosition) {
                    return ItemTouchHelper.Callback.makeMovementFlags(this.MOVEMENT, 0);
                }
                return ItemTouchHelper.Callback.makeMovementFlags(0, 0);
            }

            @Override // androidx.recyclerview.widget.ItemTouchHelper.Callback
            public boolean canDropOver(@NotNull RecyclerView recyclerView, @NotNull RecyclerView.ViewHolder viewHolder, @NotNull RecyclerView.ViewHolder viewHolder2) {
                Intrinsics.checkParameterIsNotNull(recyclerView, "recyclerView");
                Intrinsics.checkParameterIsNotNull(viewHolder, "current");
                Intrinsics.checkParameterIsNotNull(viewHolder2, "target");
                return viewHolder2.getAdapterPosition() < this.this$0.dividerPosition;
            }
        };
    }

    @Override // com.android.systemui.controls.management.ControlsModel
    @NotNull
    public ControlsModel.MoveHelper getMoveHelper() {
        return this.moveHelper;
    }

    public void attachAdapter(@NotNull RecyclerView.Adapter<?> adapter) {
        Intrinsics.checkParameterIsNotNull(adapter, "adapter");
        this.adapter = adapter;
    }

    @Override // com.android.systemui.controls.management.ControlsModel
    @NotNull
    public List<ControlInfo> getFavorites() {
        List<ElementWrapper> list = CollectionsKt___CollectionsKt.take(getElements(), this.dividerPosition);
        ArrayList arrayList = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(list, 10));
        for (ElementWrapper elementWrapper : list) {
            if (elementWrapper != null) {
                arrayList.add(((ControlInfoWrapper) elementWrapper).getControlInfo());
            } else {
                throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.controls.management.ControlInfoWrapper");
            }
        }
        return arrayList;
    }

    @Override // com.android.systemui.controls.management.ControlsModel
    @NotNull
    public List<ElementWrapper> getElements() {
        return this.elements;
    }

    @Override // com.android.systemui.controls.management.ControlsModel
    public void changeFavoriteStatus(@NotNull String str, boolean z) {
        Intrinsics.checkParameterIsNotNull(str, "controlId");
        Iterator<ElementWrapper> it = getElements().iterator();
        int i = 0;
        while (true) {
            if (!it.hasNext()) {
                i = -1;
                break;
            }
            ElementWrapper next = it.next();
            if ((next instanceof ControlInterface) && Intrinsics.areEqual(((ControlInterface) next).getControlId(), str)) {
                break;
            }
            i++;
        }
        if (i != -1) {
            if (i < this.dividerPosition && z) {
                return;
            }
            if (i > this.dividerPosition && !z) {
                return;
            }
            if (z) {
                onMoveItemInternal(i, this.dividerPosition);
            } else {
                onMoveItemInternal(i, getElements().size() - 1);
            }
        }
    }

    public void onMoveItem(int i, int i2) {
        onMoveItemInternal(i, i2);
    }

    private final void updateDividerNone(int i, boolean z) {
        ElementWrapper elementWrapper = getElements().get(i);
        if (elementWrapper != null) {
            ((DividerWrapper) elementWrapper).setShowNone(z);
            this.favoritesModelCallback.onNoneChanged(z);
            return;
        }
        throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.controls.management.DividerWrapper");
    }

    private final void updateDividerShow(int i, boolean z) {
        ElementWrapper elementWrapper = getElements().get(i);
        if (elementWrapper != null) {
            ((DividerWrapper) elementWrapper).setShowDivider(z);
            return;
        }
        throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.controls.management.DividerWrapper");
    }

    private final void onMoveItemInternal(int i, int i2) {
        RecyclerView.Adapter<?> adapter;
        int i3;
        int i4 = this.dividerPosition;
        if (i != i4) {
            boolean z = false;
            if ((i < i4 && i2 >= i4) || (i > (i3 = this.dividerPosition) && i2 <= i3)) {
                int i5 = this.dividerPosition;
                if (i >= i5 || i2 < i5) {
                    int i6 = this.dividerPosition;
                    if (i > i6 && i2 <= i6) {
                        ElementWrapper elementWrapper = getElements().get(i);
                        if (elementWrapper != null) {
                            ((ControlInfoWrapper) elementWrapper).setFavorite(true);
                        } else {
                            throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.controls.management.ControlInfoWrapper");
                        }
                    }
                } else {
                    ElementWrapper elementWrapper2 = getElements().get(i);
                    if (elementWrapper2 != null) {
                        ((ControlInfoWrapper) elementWrapper2).setFavorite(false);
                    } else {
                        throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.controls.management.ControlInfoWrapper");
                    }
                }
                updateDivider(i, i2);
                z = true;
            }
            moveElement(i, i2);
            RecyclerView.Adapter<?> adapter2 = this.adapter;
            if (adapter2 != null) {
                adapter2.notifyItemMoved(i, i2);
            }
            if (z && (adapter = this.adapter) != null) {
                adapter.notifyItemChanged(i2, new Object());
            }
            if (!this.modified) {
                this.modified = true;
                this.favoritesModelCallback.onFirstChange();
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x004a  */
    /* JADX WARNING: Removed duplicated region for block: B:26:? A[ADDED_TO_REGION, RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final void updateDivider(int r5, int r6) {
        /*
            r4 = this;
            int r0 = r4.dividerPosition
            r1 = 0
            r2 = 1
            if (r5 >= r0) goto L_0x0025
            if (r6 < r0) goto L_0x0025
            int r5 = r0 + -1
            r4.dividerPosition = r5
            if (r5 != 0) goto L_0x0012
            r4.updateDividerNone(r0, r2)
            r1 = r2
        L_0x0012:
            int r5 = r4.dividerPosition
            java.util.List r6 = r4.getElements()
            int r6 = r6.size()
            int r6 = r6 + -2
            if (r5 != r6) goto L_0x0048
            r4.updateDividerShow(r0, r2)
        L_0x0023:
            r1 = r2
            goto L_0x0048
        L_0x0025:
            int r3 = r4.dividerPosition
            if (r5 <= r3) goto L_0x0048
            if (r6 > r3) goto L_0x0048
            int r3 = r3 + r2
            r4.dividerPosition = r3
            if (r3 != r2) goto L_0x0035
            r4.updateDividerNone(r0, r1)
            r5 = r2
            goto L_0x0036
        L_0x0035:
            r5 = r1
        L_0x0036:
            int r6 = r4.dividerPosition
            java.util.List r3 = r4.getElements()
            int r3 = r3.size()
            int r3 = r3 - r2
            if (r6 != r3) goto L_0x0047
            r4.updateDividerShow(r0, r1)
            goto L_0x0023
        L_0x0047:
            r1 = r5
        L_0x0048:
            if (r1 == 0) goto L_0x0051
            androidx.recyclerview.widget.RecyclerView$Adapter<?> r4 = r4.adapter
            if (r4 == 0) goto L_0x0051
            r4.notifyItemChanged(r0)
        L_0x0051:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.controls.management.FavoritesModel.updateDivider(int, int):void");
    }

    private final void moveElement(int i, int i2) {
        if (i < i2) {
            while (i < i2) {
                int i3 = i + 1;
                Collections.swap(getElements(), i, i3);
                i = i3;
            }
            return;
        }
        int i4 = i2 + 1;
        if (i >= i4) {
            while (true) {
                Collections.swap(getElements(), i, i - 1);
                if (i != i4) {
                    i--;
                } else {
                    return;
                }
            }
        }
    }

    @NotNull
    public final ItemTouchHelper.SimpleCallback getItemTouchHelperCallback() {
        return this.itemTouchHelperCallback;
    }
}
