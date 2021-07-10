package androidx.slice;

import android.content.Context;
import androidx.slice.core.SliceAction;
import androidx.slice.core.SliceActionImpl;
import androidx.slice.core.SliceQuery;
import androidx.slice.widget.ListContent;
import androidx.slice.widget.RowContent;
import java.util.ArrayList;
import java.util.List;
public class SliceMetadata {
    private Context mContext;
    private long mExpiry;
    private RowContent mHeaderContent;
    private long mLastUpdated;
    private ListContent mListContent;
    private Slice mSlice;
    private List<SliceAction> mSliceActions;

    public static SliceMetadata from(Context context, Slice slice) {
        return new SliceMetadata(context, slice);
    }

    private SliceMetadata(Context context, Slice slice) {
        RowContent rowContent;
        this.mSlice = slice;
        this.mContext = context;
        SliceItem find = SliceQuery.find(slice, "long", "ttl", (String) null);
        if (find != null) {
            this.mExpiry = find.getLong();
        }
        SliceItem find2 = SliceQuery.find(slice, "long", "last_updated", (String) null);
        if (find2 != null) {
            this.mLastUpdated = find2.getLong();
        }
        ListContent listContent = new ListContent(slice);
        this.mListContent = listContent;
        this.mHeaderContent = listContent.getHeader();
        this.mListContent.getHeaderTemplateType();
        this.mListContent.getShortcut(this.mContext);
        List<SliceAction> sliceActions = this.mListContent.getSliceActions();
        this.mSliceActions = sliceActions;
        if (sliceActions == null && (rowContent = this.mHeaderContent) != null && SliceQuery.hasHints(rowContent.getSliceItem(), "list_item")) {
            ArrayList<SliceItem> endItems = this.mHeaderContent.getEndItems();
            ArrayList arrayList = new ArrayList();
            for (int i = 0; i < endItems.size(); i++) {
                if (SliceQuery.find(endItems.get(i), "action") != null) {
                    arrayList.add(new SliceActionImpl(endItems.get(i)));
                }
            }
            if (arrayList.size() > 0) {
                this.mSliceActions = arrayList;
            }
        }
    }

    public List<SliceAction> getSliceActions() {
        return this.mSliceActions;
    }

    public int getLoadingState() {
        boolean z = SliceQuery.find(this.mSlice, null, "partial", null) != null;
        if (!this.mListContent.isValid()) {
            return 0;
        }
        if (z) {
            return 1;
        }
        return 2;
    }

    public long getLastUpdatedTime() {
        return this.mLastUpdated;
    }

    public boolean isPermissionSlice() {
        return this.mSlice.hasHint("permission_request");
    }

    public static List<SliceAction> getSliceActions(Slice slice) {
        SliceItem find = SliceQuery.find(slice, "slice", "actions", (String) null);
        List<SliceItem> findAll = find != null ? SliceQuery.findAll(find, "slice", new String[]{"actions", "shortcut"}, (String[]) null) : null;
        if (findAll == null) {
            return null;
        }
        ArrayList arrayList = new ArrayList(findAll.size());
        for (int i = 0; i < findAll.size(); i++) {
            arrayList.add(new SliceActionImpl(findAll.get(i)));
        }
        return arrayList;
    }

    public boolean isExpired() {
        long currentTimeMillis = System.currentTimeMillis();
        long j = this.mExpiry;
        return (j == 0 || j == -1 || currentTimeMillis <= j) ? false : true;
    }

    public boolean neverExpires() {
        return this.mExpiry == -1;
    }

    public long getTimeToExpiry() {
        long currentTimeMillis = System.currentTimeMillis();
        long j = this.mExpiry;
        if (j == 0 || j == -1 || currentTimeMillis > j) {
            return 0;
        }
        return j - currentTimeMillis;
    }

    public ListContent getListContent() {
        return this.mListContent;
    }
}
