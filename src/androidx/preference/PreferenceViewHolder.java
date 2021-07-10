package androidx.preference;

import android.util.SparseArray;
import android.view.View;
import androidx.recyclerview.widget.RecyclerView;
public class PreferenceViewHolder extends RecyclerView.ViewHolder {
    private final SparseArray<View> mCachedViews;
    private boolean mDividerAllowedAbove;
    private boolean mDividerAllowedBelow;

    PreferenceViewHolder(View view) {
        super(view);
        SparseArray<View> sparseArray = new SparseArray<>(4);
        this.mCachedViews = sparseArray;
        sparseArray.put(16908310, view.findViewById(16908310));
        this.mCachedViews.put(16908304, view.findViewById(16908304));
        this.mCachedViews.put(16908294, view.findViewById(16908294));
        SparseArray<View> sparseArray2 = this.mCachedViews;
        int i = R$id.icon_frame;
        sparseArray2.put(i, view.findViewById(i));
        this.mCachedViews.put(16908350, view.findViewById(16908350));
    }

    public View findViewById(int i) {
        View view = this.mCachedViews.get(i);
        if (view != null) {
            return view;
        }
        View findViewById = this.itemView.findViewById(i);
        if (findViewById != null) {
            this.mCachedViews.put(i, findViewById);
        }
        return findViewById;
    }

    public boolean isDividerAllowedAbove() {
        return this.mDividerAllowedAbove;
    }

    public void setDividerAllowedAbove(boolean z) {
        this.mDividerAllowedAbove = z;
    }

    public boolean isDividerAllowedBelow() {
        return this.mDividerAllowedBelow;
    }

    public void setDividerAllowedBelow(boolean z) {
        this.mDividerAllowedBelow = z;
    }
}