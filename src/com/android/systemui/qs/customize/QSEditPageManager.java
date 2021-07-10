package com.android.systemui.qs.customize;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0009R$integer;
import com.android.systemui.C0015R$string;
import com.android.systemui.qs.QSTileHost;
import com.android.systemui.qs.customize.TileQueryHelper;
import com.oneplus.util.OpUtils;
import java.util.ArrayList;
import java.util.List;
public class QSEditPageManager implements TileQueryHelper.TileStateListener {
    private List<TileQueryHelper.TileInfo> mAllTiles;
    private boolean mCanRemoveTile;
    private Context mContext;
    private List<String> mCurrentSpecs;
    TextView mDragLabel;
    private ItemLocations mLowerLocations;
    private QSEditViewPager mLowerPager;
    private List<TileQueryHelper.TileInfo> mLowerTiles = new ArrayList();
    RecyclerView mSource;
    RecyclerView mTarget;
    private ItemLocations mUpperLocations;
    private QSEditViewPager mUpperPager;
    private List<TileQueryHelper.TileInfo> mUpperTiles = new ArrayList();

    public void setHost(QSTileHost qSTileHost) {
    }

    public QSEditPageManager(Context context, QSEditViewPager qSEditViewPager, QSEditViewPager qSEditViewPager2, TextView textView) {
        boolean z = true;
        this.mCanRemoveTile = true;
        this.mSource = null;
        this.mTarget = null;
        this.mContext = context;
        this.mUpperPager = qSEditViewPager;
        this.mLowerPager = qSEditViewPager2;
        this.mDragLabel = textView;
        if (OpUtils.isSupportDoubleTapAlexa()) {
            OpUtils.setIsEditTileBefore(this.mContext, Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "sysui_qs_edited", 0, 0) != 1 ? false : z);
        }
        reloadResources();
    }

    private void reloadResources() {
        int dimensionPixelSize = this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.qs_edit_tile_width);
        int dimensionPixelSize2 = this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.qs_edit_tile_height);
        int dimensionPixelSize3 = this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.qs_edit_tile_margin);
        int integer = this.mContext.getResources().getInteger(C0009R$integer.quick_settings_num_columns);
        int integer2 = this.mContext.getResources().getInteger(C0009R$integer.quick_settings_max_rows);
        int integer3 = this.mContext.getResources().getInteger(C0009R$integer.quick_settings_num_rows_lower);
        Log.d("QSEditPageManager", "rows=" + integer2 + ", lower_rows=" + integer3);
        this.mUpperLocations = new ItemLocations(dimensionPixelSize, dimensionPixelSize2, dimensionPixelSize3, integer, integer2);
        this.mLowerLocations = new ItemLocations(dimensionPixelSize, dimensionPixelSize2, dimensionPixelSize3, integer, integer3);
    }

    public void recalcEditPage() {
        reloadResources();
        recalcSpecs();
    }

    public void setLayoutRTL(boolean z) {
        this.mUpperLocations.setLayoutRTL(z);
        this.mLowerLocations.setLayoutRTL(z);
    }

    private void setupRecyclerView(RecyclerView recyclerView, List<TileQueryHelper.TileInfo> list, ItemLocations itemLocations, QSEditViewPager qSEditViewPager) {
        QSEditTileAdapter qSEditTileAdapter = new QSEditTileAdapter(list, itemLocations, this);
        recyclerView.setLayoutManager(new GridLayoutManager(this.mContext, itemLocations.getColumns()) { // from class: com.android.systemui.qs.customize.QSEditPageManager.1
            @Override // androidx.recyclerview.widget.LinearLayoutManager, androidx.recyclerview.widget.RecyclerView.LayoutManager
            public boolean canScrollVertically() {
                return false;
            }

            @Override // androidx.recyclerview.widget.GridLayoutManager, androidx.recyclerview.widget.LinearLayoutManager, androidx.recyclerview.widget.RecyclerView.LayoutManager
            public RecyclerView.LayoutParams generateDefaultLayoutParams() {
                RecyclerView.LayoutParams generateDefaultLayoutParams = super.generateDefaultLayoutParams();
                ((ViewGroup.MarginLayoutParams) generateDefaultLayoutParams).bottomMargin = QSEditPageManager.this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.qs_tile_margin_row);
                ((ViewGroup.MarginLayoutParams) generateDefaultLayoutParams).rightMargin = QSEditPageManager.this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.qs_edit_tile_margin);
                ((ViewGroup.MarginLayoutParams) generateDefaultLayoutParams).leftMargin = QSEditPageManager.this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.qs_edit_tile_margin);
                return generateDefaultLayoutParams;
            }
        });
        recyclerView.setAdapter(qSEditTileAdapter);
        recyclerView.setOnDragListener(qSEditTileAdapter.getDragInstance());
        addPage(qSEditViewPager, recyclerView);
    }

    public void beginDragAndDrop(RecyclerView recyclerView) {
        boolean isInUpperPage = isInUpperPage(recyclerView);
        int itemCount = ((QSEditTileAdapter) recyclerView.getAdapter()).getItemCount();
        int count = this.mUpperPager.getAdapter().getCount();
        if (Build.DEBUG_ONEPLUS) {
            Log.d("QSEditPageManager", "beginDragAndDrop isUpperPage: " + isInUpperPage + "itemCount: " + itemCount + " pageCount: " + count);
        }
        if (!isInUpperPage || itemCount > 6 || count >= 2) {
            this.mCanRemoveTile = true;
        } else {
            this.mCanRemoveTile = false;
        }
        if (Build.DEBUG_ONEPLUS) {
            Log.d("QSEditPageManager", "beginDragAndDrop mCanRemoveTile: " + this.mCanRemoveTile);
        }
    }

    public boolean canRemoveTile() {
        return this.mCanRemoveTile;
    }

    public TextView getDragLabel() {
        return this.mDragLabel;
    }

    public void endDragAndDrop() {
        this.mDragLabel.setText(this.mContext.getResources().getString(C0015R$string.drag_to_add_tiles));
    }

    public boolean canScrollToNextPage() {
        if (!isPageEmpty(getPage(this.mUpperPager, this.mUpperPager.getCurrentItem() + 1))) {
            return true;
        }
        return false;
    }

    public void scrollNextPage() {
        int currentItem = this.mUpperPager.getCurrentItem();
        if (currentItem < this.mUpperPager.getAdapter().getCount()) {
            this.mUpperPager.setCurrentItem(currentItem + 1, true);
        }
    }

    public void scrollPrevPage() {
        int currentItem = this.mUpperPager.getCurrentItem();
        if (currentItem > 0) {
            this.mUpperPager.setCurrentItem(currentItem - 1, true);
        }
    }

    private void addPage(QSEditViewPager qSEditViewPager, RecyclerView recyclerView) {
        ((QSEditPageAdapter) qSEditViewPager.getAdapter()).addPage(recyclerView);
        qSEditViewPager.updateIndicator();
    }

    private void removePage(QSEditViewPager qSEditViewPager, int i) {
        ((QSEditPageAdapter) qSEditViewPager.getAdapter()).removePage(i);
        qSEditViewPager.updateIndicator();
    }

    private RecyclerView getPage(QSEditViewPager qSEditViewPager, int i) {
        return ((QSEditPageAdapter) qSEditViewPager.getAdapter()).getPage(i);
    }

    private QSEditViewPager getPager(RecyclerView recyclerView) {
        if (isInUpperPage(recyclerView)) {
            return this.mUpperPager;
        }
        return this.mLowerPager;
    }

    private int getPageCount(QSEditViewPager qSEditViewPager) {
        return qSEditViewPager.getAdapter().getCount();
    }

    public boolean isInUpperPage(RecyclerView recyclerView) {
        return ((QSEditPageAdapter) this.mUpperPager.getAdapter()).containsPage(recyclerView);
    }

    public boolean isInLowerPage(RecyclerView recyclerView) {
        return ((QSEditPageAdapter) this.mLowerPager.getAdapter()).containsPage(recyclerView);
    }

    public boolean isPageEmpty(RecyclerView recyclerView) {
        return ((QSEditTileAdapter) recyclerView.getAdapter()).isPageEmpty();
    }

    public boolean isPageFull(RecyclerView recyclerView) {
        return ((QSEditTileAdapter) recyclerView.getAdapter()).isPageFull();
    }

    public boolean isPageMoreThanFull(RecyclerView recyclerView) {
        return ((QSEditTileAdapter) recyclerView.getAdapter()).isPageMoreThanFull();
    }

    public ItemLocations getItemLocations(RecyclerView recyclerView) {
        return ((QSEditTileAdapter) recyclerView.getAdapter()).getItemLocations();
    }

    private void fillPager(QSEditViewPager qSEditViewPager, List<TileQueryHelper.TileInfo> list, ItemLocations itemLocations) {
        int maxItems = itemLocations.getMaxItems();
        int ceil = (int) Math.ceil(((double) list.size()) / ((double) maxItems));
        qSEditViewPager.setAdapter(new QSEditPageAdapter());
        for (int i = 0; i < ceil; i++) {
            RecyclerView recyclerView = new RecyclerView(this.mContext);
            recyclerView.setTag(Integer.valueOf(i));
            int i2 = i * maxItems;
            int i3 = i2 + maxItems;
            int min = Math.min(i3, list.size());
            setupRecyclerView(recyclerView, new ArrayList(list.subList(i2, min)), itemLocations, qSEditViewPager);
            if (i == ceil - 1 && min == i3) {
                RecyclerView recyclerView2 = new RecyclerView(this.mContext);
                recyclerView2.setTag(Integer.valueOf(ceil));
                setupRecyclerView(recyclerView2, null, itemLocations, qSEditViewPager);
            }
        }
        if (ceil == 0) {
            RecyclerView recyclerView3 = new RecyclerView(this.mContext);
            recyclerView3.setTag(0);
            setupRecyclerView(recyclerView3, new ArrayList(), itemLocations, qSEditViewPager);
        }
        qSEditViewPager.setCurrentItem(0);
    }

    public void onBeforeItemAdded(RecyclerView recyclerView) {
        QSEditViewPager pager = getPager(recyclerView);
        ItemLocations itemLocations = getItemLocations(recyclerView);
        Log.d("QSEditPageManager", "onBeforeItemAdded:items=" + ((QSEditTileAdapter) recyclerView.getAdapter()).getItemCount());
        if (isPageFull(recyclerView)) {
            Log.d("QSEditPageManager", "onBeforeItemAdded:page is full, move last item to the next page");
            int intValue = ((Integer) recyclerView.getTag()).intValue() + 1;
            for (int i = intValue; i < getPageCount(pager); i++) {
                RecyclerView page = getPage(pager, i);
                QSEditTileAdapter qSEditTileAdapter = (QSEditTileAdapter) getPage(pager, i - 1).getAdapter();
                TileQueryHelper.TileInfo tileInfo = null;
                if (i == intValue) {
                    tileInfo = qSEditTileAdapter.getItemList().remove(itemLocations.getMaxItems() - 1);
                    qSEditTileAdapter.notifyItemRemoved(itemLocations.getMaxItems() - 1);
                } else if (qSEditTileAdapter.getItemCount() > itemLocations.getMaxItems()) {
                    tileInfo = qSEditTileAdapter.getItemList().remove(itemLocations.getMaxItems());
                    qSEditTileAdapter.notifyItemRemoved(itemLocations.getMaxItems());
                }
                QSEditTileAdapter qSEditTileAdapter2 = (QSEditTileAdapter) page.getAdapter();
                if (tileInfo != null) {
                    qSEditTileAdapter2.getItemList().add(0, tileInfo);
                    qSEditTileAdapter2.notifyItemInserted(0);
                }
            }
        }
    }

    public void onBeforeItemRemoved(RecyclerView recyclerView) {
        QSEditViewPager pager = getPager(recyclerView);
        if (isPageFull(recyclerView) && (r6 = ((Integer) recyclerView.getTag()).intValue()) < getPageCount(pager) - 1) {
            while (true) {
                int intValue = intValue + 1;
                if (intValue < getPageCount(pager)) {
                    RecyclerView page = getPage(pager, intValue);
                    RecyclerView page2 = getPage(pager, intValue - 1);
                    QSEditTileAdapter qSEditTileAdapter = (QSEditTileAdapter) page.getAdapter();
                    QSEditTileAdapter qSEditTileAdapter2 = (QSEditTileAdapter) page2.getAdapter();
                    if (qSEditTileAdapter.getItemCount() > 0) {
                        qSEditTileAdapter.notifyItemRemoved(0);
                        qSEditTileAdapter2.getItemList().add(qSEditTileAdapter.getItemList().remove(0));
                        qSEditTileAdapter2.notifyItemInserted(qSEditTileAdapter2.getItemList().size() - 1);
                    }
                } else {
                    return;
                }
            }
        }
    }

    public void onAfterItemAdded(RecyclerView recyclerView) {
        QSEditViewPager pager = getPager(recyclerView);
        ItemLocations itemLocations = getItemLocations(recyclerView);
        RecyclerView page = getPage(pager, getPageCount(pager) - 1);
        Log.d("QSEditPageManager", "onAfterItemAdded:items=" + ((QSEditTileAdapter) page.getAdapter()).getItemCount());
        if (isPageFull(page)) {
            Log.d("QSEditPageManager", "onAfterItemAdded:page is full, add an empty page");
            RecyclerView recyclerView2 = new RecyclerView(this.mContext);
            recyclerView2.setTag(Integer.valueOf(getPageCount(pager)));
            setupRecyclerView(recyclerView2, null, itemLocations, pager);
            pager.getAdapter().notifyDataSetChanged();
        }
    }

    public void onAfterItemRemoved(RecyclerView recyclerView) {
        QSEditViewPager pager = getPager(recyclerView);
        if (getPageCount(pager) >= 2) {
            RecyclerView page = getPage(pager, getPageCount(pager) - 1);
            RecyclerView page2 = getPage(pager, getPageCount(pager) - 2);
            if (page.getAdapter().getItemCount() == 0 && !isPageFull(page2)) {
                Log.d("QSEditPageManager", "onAfterItemRemoved:remove the empty page");
                removePage(pager, getPageCount(pager) - 1);
                pager.getAdapter().notifyDataSetChanged();
            }
        }
    }

    public int rebuildPager(RecyclerView recyclerView) {
        QSEditViewPager pager = getPager(recyclerView);
        ItemLocations itemLocations = getItemLocations(recyclerView);
        int pageCount = getPageCount(pager);
        int i = 0;
        int i2 = 0;
        while (i < pageCount - 1) {
            RecyclerView page = getPage(pager, i);
            QSEditTileAdapter qSEditTileAdapter = (QSEditTileAdapter) page.getAdapter();
            i++;
            QSEditTileAdapter qSEditTileAdapter2 = (QSEditTileAdapter) getPage(pager, i).getAdapter();
            if (isPageMoreThanFull(page)) {
                qSEditTileAdapter.notifyItemRemoved(itemLocations.getMaxItems());
                qSEditTileAdapter2.getItemList().add(0, qSEditTileAdapter.getItemList().remove(itemLocations.getMaxItems()));
                qSEditTileAdapter2.notifyItemInserted(0);
                i2 = -1;
            } else if (!isPageFull(page)) {
                qSEditTileAdapter2.notifyItemRemoved(0);
                qSEditTileAdapter.getItemList().add(qSEditTileAdapter2.getItemList().remove(0));
                qSEditTileAdapter.notifyItemInserted(qSEditTileAdapter.getItemList().size() - 1);
                i2 = 1;
            }
        }
        return i2;
    }

    @Override // com.android.systemui.qs.customize.TileQueryHelper.TileStateListener
    public void onTilesChanged(List<TileQueryHelper.TileInfo> list) {
        this.mAllTiles = list;
    }

    public void saveSpecs(QSTileHost qSTileHost) {
        ArrayList arrayList = new ArrayList();
        QSEditViewPager qSEditViewPager = this.mUpperPager;
        if (!(qSEditViewPager == null || qSEditViewPager.getAdapter() == null)) {
            int pageCount = getPageCount(this.mUpperPager);
            for (int i = 0; i < pageCount; i++) {
                List<TileQueryHelper.TileInfo> itemList = ((QSEditTileAdapter) getPage(this.mUpperPager, i).getAdapter()).getItemList();
                int i2 = 0;
                while (i2 < itemList.size() && itemList.get(i2) != null) {
                    arrayList.add(itemList.get(i2).spec);
                    i2++;
                }
            }
            boolean isEditTileBefore = OpUtils.getIsEditTileBefore();
            Log.d("QSEditPageManager", "newSpecs=" + arrayList + ", " + isEditTileBefore);
            if (OpUtils.isSupportDoubleTapAlexa() && !isEditTileBefore) {
                OpUtils.setIsEditTileBefore(this.mContext, true);
            }
            qSTileHost.changeTiles(this.mCurrentSpecs, arrayList);
            this.mCurrentSpecs = arrayList;
        }
    }

    public void resetTileSpecs(QSTileHost qSTileHost, List<String> list) {
        Log.d("QSEditPageManager", "resetTileSpecs=" + list);
        qSTileHost.changeTiles(this.mCurrentSpecs, list);
        setTileSpecs(list);
    }

    public void setTileSpecs(List<String> list) {
        list.equals(this.mCurrentSpecs);
        this.mCurrentSpecs = list;
        recalcSpecs();
    }

    public void recalcSpecs() {
        if (!(this.mCurrentSpecs == null || this.mAllTiles == null)) {
            this.mLowerTiles = new ArrayList(this.mAllTiles);
            this.mUpperTiles.clear();
            for (int i = 0; i < this.mCurrentSpecs.size(); i++) {
                TileQueryHelper.TileInfo andRemoveLower = getAndRemoveLower(this.mCurrentSpecs.get(i));
                if (andRemoveLower != null) {
                    this.mUpperTiles.add(andRemoveLower);
                }
            }
            ArrayList arrayList = new ArrayList();
            for (int i2 = 0; i2 < this.mLowerTiles.size(); i2++) {
                TileQueryHelper.TileInfo tileInfo = this.mLowerTiles.get(i2);
                if (!tileInfo.isSystem) {
                    arrayList.add(tileInfo);
                }
            }
            this.mLowerTiles.removeAll(arrayList);
            this.mLowerTiles.addAll(arrayList);
            fillPager(this.mUpperPager, this.mUpperTiles, this.mUpperLocations);
            fillPager(this.mLowerPager, this.mLowerTiles, this.mLowerLocations);
        }
    }

    public void calculateItemLocation() {
        int[] iArr = new int[2];
        this.mUpperPager.getLocationOnScreen(iArr);
        this.mUpperLocations.setParentLocation(iArr[0], iArr[1], this.mUpperPager.getWidth());
        Log.d("QSEditPageManager", "mUpperPager=" + iArr[0] + ", " + iArr[1]);
        this.mLowerPager.getLocationOnScreen(iArr);
        this.mLowerLocations.setParentLocation(iArr[0], iArr[1], this.mLowerPager.getWidth());
        Log.d("QSEditPageManager", "mLowerPager=" + iArr[0] + ", " + iArr[1]);
    }

    private TileQueryHelper.TileInfo getAndRemoveLower(String str) {
        for (int i = 0; i < this.mLowerTiles.size(); i++) {
            try {
                if (this.mLowerTiles.get(i).spec.equals(str)) {
                    return this.mLowerTiles.remove(i);
                }
            } catch (NullPointerException e) {
                Log.d("QSEditPageManager", "getAndRemoveLower: number of tiles=" + this.mLowerTiles.size() + ", i=" + i);
                printTiles(this.mLowerTiles);
                printTiles(this.mAllTiles);
                throw e;
            }
        }
        return null;
    }

    private void printTiles(List<TileQueryHelper.TileInfo> list) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(":tile ");
            sb.append(i);
            sb.append(" = ");
            TileQueryHelper.TileInfo tileInfo = list.get(i);
            if (tileInfo != null) {
                sb.append(tileInfo.spec);
            } else {
                sb.append("NULL");
            }
        }
        Log.d("QSEditPageManager", "printTiles: tiles size=" + list.size() + " => " + sb.toString());
    }
}
