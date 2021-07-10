package com.android.systemui.qs.customize;

import android.content.ClipData;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import com.android.systemui.C0009R$integer;
import com.android.systemui.C0015R$string;
import com.android.systemui.qs.customize.QSEditTileAdapter;
import com.android.systemui.qs.customize.TileQueryHelper;
import com.android.systemui.qs.tileimpl.QSIconViewImpl;
import java.util.ArrayList;
import java.util.List;
public class QSEditTileAdapter extends RecyclerView.Adapter<TileViewHolder> {
    private static int mGoToPage = 0;
    private static long mLastPageTime = 0;
    private static int mPositionSource = -1;
    private static int mPositionTarget = -1;
    private static TileQueryHelper.TileInfo mSelectedItem;
    private Handler mHandler = new Handler();
    private ItemLocations mItemLocations = null;
    private QSEditPageManager mManager = null;
    private View.DragShadowBuilder mShadowBuilder = null;
    private List<TileQueryHelper.TileInfo> mTiles;

    public QSEditTileAdapter(List<TileQueryHelper.TileInfo> list, ItemLocations itemLocations, QSEditPageManager qSEditPageManager) {
        this.mTiles = list;
        if (list == null) {
            this.mTiles = new ArrayList();
        }
        this.mItemLocations = itemLocations;
        this.mManager = qSEditPageManager;
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public TileViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        return new TileViewHolder(this, new CustomizeTileView(context, new QSIconViewImpl(context)));
    }

    public void onBindViewHolder(TileViewHolder tileViewHolder, int i) {
        TileQueryHelper.TileInfo tileInfo = this.mTiles.get(i);
        tileViewHolder.mTileView.onStateChanged(tileInfo.state);
        tileViewHolder.mTileView.setShowAppLabel(!tileInfo.isSystem);
        tileViewHolder.mTileView.setTag(tileViewHolder);
        tileViewHolder.setData(tileInfo);
        tileViewHolder.mTileView.setOnLongClickListener(new View.OnLongClickListener() { // from class: com.android.systemui.qs.customize.QSEditTileAdapter.1
            @Override // android.view.View.OnLongClickListener
            public boolean onLongClick(View view) {
                String str;
                ClipData newPlainText = ClipData.newPlainText("", "");
                QSEditTileAdapter.this.mShadowBuilder = new View.DragShadowBuilder(view);
                view.startDrag(newPlainText, QSEditTileAdapter.this.mShadowBuilder, view, 0);
                TileViewHolder tileViewHolder2 = (TileViewHolder) view.getTag();
                tileViewHolder2.setVisible(false);
                TileQueryHelper.TileInfo unused = QSEditTileAdapter.mSelectedItem = tileViewHolder2.mData;
                QSEditTileAdapter.this.mManager.mSource = (RecyclerView) view.getParent();
                int unused2 = QSEditTileAdapter.mPositionSource = ((TileViewHolder) view.getTag()).getAdapterPosition();
                QSEditTileAdapter.this.mManager.beginDragAndDrop(QSEditTileAdapter.this.mManager.mSource);
                Resources resources = view.getContext().getResources();
                int integer = resources.getInteger(C0009R$integer.quick_settings_min_num_tiles);
                if (QSEditTileAdapter.this.mManager.canRemoveTile()) {
                    str = resources.getString(C0015R$string.drag_to_remove_tiles);
                } else {
                    str = resources.getString(C0015R$string.drag_to_remove_disabled, Integer.valueOf(integer));
                }
                QSEditTileAdapter.this.mManager.getDragLabel().setText(str);
                return true;
            }
        });
        tileViewHolder.mTileView.setOnDragListener(getDragInstance());
    }

    public ItemLocations getItemLocations() {
        return this.mItemLocations;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getIndexInPage(View view, DragEvent dragEvent) {
        Point touchPositionFromDragEvent = getTouchPositionFromDragEvent(view, dragEvent);
        int positionIndex = this.mItemLocations.getPositionIndex(touchPositionFromDragEvent.x, touchPositionFromDragEvent.y);
        if (positionIndex >= 0) {
            return Math.min(positionIndex, getItemCount());
        }
        return getItemCount() - 1;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getTargetPage(View view, DragEvent dragEvent) {
        Point touchPositionFromDragEvent = getTouchPositionFromDragEvent(view, dragEvent);
        if (this.mItemLocations.isGoingToNextPage(touchPositionFromDragEvent.x)) {
            return 1;
        }
        return this.mItemLocations.isGoingToPrevPage(touchPositionFromDragEvent.x) ? 2 : 0;
    }

    public boolean isPageEmpty() {
        return getItemCount() == 0;
    }

    public boolean isPageFull() {
        return getItemCount() == this.mItemLocations.getMaxItems();
    }

    public boolean isPageMoreThanFull() {
        return getItemCount() > this.mItemLocations.getMaxItems();
    }

    private Point getTouchPositionFromDragEvent(View view, DragEvent dragEvent) {
        Rect rect = new Rect();
        view.getGlobalVisibleRect(rect);
        return new Point(rect.left + Math.round(dragEvent.getX()), rect.top + Math.round(dragEvent.getY()));
    }

    public DragListener getDragInstance() {
        return new DragListener();
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        return this.mTiles.size();
    }

    public List<TileQueryHelper.TileInfo> getItemList() {
        return this.mTiles;
    }

    public class TileViewHolder extends RecyclerView.ViewHolder {
        private TileQueryHelper.TileInfo mData = null;
        private CustomizeTileView mTileView;

        public TileViewHolder(QSEditTileAdapter qSEditTileAdapter, View view) {
            super(view);
            if (view instanceof CustomizeTileView) {
                CustomizeTileView customizeTileView = (CustomizeTileView) view;
                this.mTileView = customizeTileView;
                customizeTileView.setBackground(null);
                this.mTileView.getIcon().disableAnimation();
            }
        }

        public void setVisible(boolean z) {
            TileQueryHelper.TileInfo tileInfo = this.mData;
            if (tileInfo != null) {
                tileInfo.isVisible = z;
                if (z) {
                    this.mTileView.setVisibility(0);
                } else {
                    this.mTileView.setVisibility(4);
                }
            }
        }

        public void setData(TileQueryHelper.TileInfo tileInfo) {
            this.mData = tileInfo;
            setVisible(tileInfo.isVisible);
        }
    }

    public class DragListener implements View.OnDragListener {
        private final Runnable mScrollWorker = new Runnable() { // from class: com.android.systemui.qs.customize.QSEditTileAdapter.DragListener.1
            @Override // java.lang.Runnable
            public void run() {
                DragListener.this.scrollPage(QSEditTileAdapter.mGoToPage);
            }
        };

        public DragListener() {
        }

        private void moveItem() {
            List<TileQueryHelper.TileInfo> itemList = ((QSEditTileAdapter) QSEditTileAdapter.this.mManager.mSource.getAdapter()).getItemList();
            itemList.add(QSEditTileAdapter.mPositionTarget, itemList.remove(QSEditTileAdapter.mPositionSource));
            QSEditTileAdapter.this.notifyItemMoved(QSEditTileAdapter.mPositionSource, QSEditTileAdapter.mPositionTarget);
            int unused = QSEditTileAdapter.mPositionSource = QSEditTileAdapter.mPositionTarget;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void scrollPage(int i) {
            long unused = QSEditTileAdapter.mLastPageTime = 0;
            if (i == 1) {
                QSEditTileAdapter.this.mManager.scrollNextPage();
            } else {
                QSEditTileAdapter.this.mManager.scrollPrevPage();
            }
        }

        private void checkIfDragToPage(int i) {
            if (i == 0) {
                long unused = QSEditTileAdapter.mLastPageTime = 0;
                QSEditTileAdapter.this.mHandler.removeCallbacks(this.mScrollWorker);
                return;
            }
            boolean z = true;
            if ((i != 1 || !QSEditTileAdapter.this.mManager.canScrollToNextPage()) && i != 2) {
                z = false;
            }
            if (QSEditTileAdapter.mLastPageTime == 0 && z) {
                long unused2 = QSEditTileAdapter.mLastPageTime = SystemClock.uptimeMillis();
                int unused3 = QSEditTileAdapter.mGoToPage = i;
                QSEditTileAdapter.this.mHandler.removeCallbacks(this.mScrollWorker);
                QSEditTileAdapter.this.mHandler.postDelayed(this.mScrollWorker, 1000);
            }
        }

        @Override // android.view.View.OnDragListener
        public boolean onDrag(View view, DragEvent dragEvent) {
            int action = dragEvent.getAction();
            View view2 = (View) dragEvent.getLocalState();
            if (view instanceof RecyclerView) {
                QSEditTileAdapter.this.mManager.mTarget = (RecyclerView) view;
                if (QSEditTileAdapter.this.mManager.mSource == QSEditTileAdapter.this.mManager.mTarget || QSEditTileAdapter.this.mManager.isPageFull(QSEditTileAdapter.this.mManager.mTarget)) {
                    if (action == 2) {
                        checkIfDragToPage(QSEditTileAdapter.this.getTargetPage(view, dragEvent));
                    }
                    return true;
                }
                int unused = QSEditTileAdapter.mPositionTarget = QSEditTileAdapter.this.getIndexInPage(view, dragEvent);
            } else {
                QSEditTileAdapter.this.mManager.mTarget = (RecyclerView) view.getParent();
                int unused2 = QSEditTileAdapter.mPositionTarget = ((TileViewHolder) view.getTag()).getAdapterPosition();
            }
            if (QSEditTileAdapter.this.mManager.isInLowerPage(QSEditTileAdapter.this.mManager.mSource) && QSEditTileAdapter.this.mManager.isInLowerPage(QSEditTileAdapter.this.mManager.mTarget)) {
                Log.d("QSEditTileAdapter", "from lower to lower, skip it");
                return true;
            } else if (!QSEditTileAdapter.this.mManager.canRemoveTile() && QSEditTileAdapter.this.mManager.mSource != QSEditTileAdapter.this.mManager.mTarget) {
                return true;
            } else {
                if (action != 2) {
                    if (action == 3 || action == 4) {
                        if (!QSEditTileAdapter.mSelectedItem.isVisible) {
                            QSEditTileAdapter.mSelectedItem.isVisible = true;
                        }
                        long unused3 = QSEditTileAdapter.mLastPageTime = 0;
                        QSEditTileAdapter.this.mHandler.removeCallbacks(this.mScrollWorker);
                        QSEditTileAdapter.this.mManager.endDragAndDrop();
                        QSEditTileAdapter.this.mHandler.post(new Runnable() { // from class: com.android.systemui.qs.customize.-$$Lambda$QSEditTileAdapter$DragListener$8rZsnprmTFL0WsIM-QCuZjcWhFw
                            @Override // java.lang.Runnable
                            public final void run() {
                                QSEditTileAdapter.DragListener.this.lambda$onDrag$1$QSEditTileAdapter$DragListener();
                            }
                        });
                    } else if (action == 5 && QSEditTileAdapter.this.mManager.mSource == QSEditTileAdapter.this.mManager.mTarget && QSEditTileAdapter.this.mManager.isInUpperPage(QSEditTileAdapter.this.mManager.mSource)) {
                        int indexInPage = QSEditTileAdapter.this.getIndexInPage(view, dragEvent);
                        if (!(QSEditTileAdapter.mPositionSource == -1 || QSEditTileAdapter.mPositionTarget == -1 || QSEditTileAdapter.mPositionSource == indexInPage || QSEditTileAdapter.mPositionTarget == QSEditTileAdapter.mPositionSource)) {
                            moveItem();
                        }
                    }
                } else if (QSEditTileAdapter.this.mManager.mSource == QSEditTileAdapter.this.mManager.mTarget) {
                    if (QSEditTileAdapter.this.mManager.isInUpperPage(QSEditTileAdapter.this.mManager.mSource)) {
                        checkIfDragToPage(QSEditTileAdapter.this.getTargetPage(view, dragEvent));
                        if (!(QSEditTileAdapter.mPositionSource == QSEditTileAdapter.this.getIndexInPage(view, dragEvent) || QSEditTileAdapter.mPositionTarget == QSEditTileAdapter.mPositionSource || QSEditTileAdapter.mPositionSource == -1 || QSEditTileAdapter.mPositionTarget == -1)) {
                            moveItem();
                        }
                    }
                } else if (!(QSEditTileAdapter.mPositionSource == -1 || QSEditTileAdapter.mPositionTarget == -1)) {
                    boolean z = QSEditTileAdapter.this.mManager.isInUpperPage(QSEditTileAdapter.this.mManager.mSource) != QSEditTileAdapter.this.mManager.isInUpperPage(QSEditTileAdapter.this.mManager.mTarget);
                    if (z || !QSEditTileAdapter.this.mManager.isPageFull(QSEditTileAdapter.this.mManager.mSource) || !QSEditTileAdapter.this.mManager.isPageEmpty(QSEditTileAdapter.this.mManager.mTarget)) {
                        QSEditTileAdapter qSEditTileAdapter = (QSEditTileAdapter) QSEditTileAdapter.this.mManager.mSource.getAdapter();
                        QSEditTileAdapter qSEditTileAdapter2 = (QSEditTileAdapter) QSEditTileAdapter.this.mManager.mTarget.getAdapter();
                        if (z) {
                            QSEditTileAdapter.this.mManager.onBeforeItemAdded(QSEditTileAdapter.this.mManager.mTarget);
                            QSEditTileAdapter.this.mManager.onBeforeItemRemoved(QSEditTileAdapter.this.mManager.mSource);
                        }
                        TileQueryHelper.TileInfo remove = qSEditTileAdapter.getItemList().remove(QSEditTileAdapter.mPositionSource);
                        qSEditTileAdapter.notifyItemRemoved(QSEditTileAdapter.mPositionSource);
                        List<TileQueryHelper.TileInfo> itemList = qSEditTileAdapter2.getItemList();
                        if (QSEditTileAdapter.mPositionTarget > itemList.size()) {
                            int unused4 = QSEditTileAdapter.mPositionTarget = itemList.size();
                        }
                        itemList.add(QSEditTileAdapter.mPositionTarget, remove);
                        qSEditTileAdapter2.notifyItemInserted(QSEditTileAdapter.mPositionTarget);
                        if (z) {
                            QSEditTileAdapter.this.mHandler.post(new Runnable(QSEditTileAdapter.this.mManager.mSource) { // from class: com.android.systemui.qs.customize.-$$Lambda$QSEditTileAdapter$DragListener$_3ph1ImMFmqk5BeISw3amC74Zvo
                                public final /* synthetic */ RecyclerView f$1;

                                {
                                    this.f$1 = r2;
                                }

                                @Override // java.lang.Runnable
                                public final void run() {
                                    QSEditTileAdapter.DragListener.this.lambda$onDrag$0$QSEditTileAdapter$DragListener(this.f$1);
                                }
                            });
                            int unused5 = QSEditTileAdapter.mPositionSource = QSEditTileAdapter.mPositionTarget;
                        } else if (QSEditTileAdapter.this.mManager.rebuildPager(QSEditTileAdapter.this.mManager.mTarget) == 1) {
                            int unused6 = QSEditTileAdapter.mPositionSource = QSEditTileAdapter.mPositionTarget - 1;
                        } else {
                            int unused7 = QSEditTileAdapter.mPositionSource = QSEditTileAdapter.mPositionTarget;
                        }
                        QSEditTileAdapter.this.mManager.mSource = QSEditTileAdapter.this.mManager.mTarget;
                    } else {
                        Log.d("QSEditTileAdapter", "Moving item in fulled page to empty page, skip it.");
                    }
                }
                return true;
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onDrag$0 */
        public /* synthetic */ void lambda$onDrag$0$QSEditTileAdapter$DragListener(RecyclerView recyclerView) {
            QSEditTileAdapter.this.mManager.onAfterItemRemoved(recyclerView);
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onDrag$1 */
        public /* synthetic */ void lambda$onDrag$1$QSEditTileAdapter$DragListener() {
            Log.d("QSEditTileAdapter", "post view changing event");
            QSEditTileAdapter.this.mManager.onAfterItemAdded(QSEditTileAdapter.this.mManager.mSource);
            ((QSEditTileAdapter) QSEditTileAdapter.this.mManager.mSource.getAdapter()).notifyItemChanged(QSEditTileAdapter.mPositionSource);
        }
    }
}
