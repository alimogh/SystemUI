package com.android.systemui.globalactions;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;
import android.widget.ListView;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0006R$drawable;
public class GlobalActionsPopupMenu extends ListPopupWindow {
    private ListAdapter mAdapter;
    private Context mContext;
    private int mGlobalActionsSidePadding = 0;
    private boolean mIsDropDownMode;
    private int mMenuVerticalPadding = 0;
    private AdapterView.OnItemLongClickListener mOnItemLongClickListener;

    public GlobalActionsPopupMenu(Context context, boolean z) {
        super(context);
        this.mContext = context;
        Resources resources = context.getResources();
        setBackgroundDrawable(resources.getDrawable(C0006R$drawable.op_rounded_bg_full));
        this.mIsDropDownMode = z;
        setWindowLayoutType(2020);
        setInputMethodMode(2);
        setModal(true);
        this.mGlobalActionsSidePadding = resources.getDimensionPixelSize(C0005R$dimen.global_actions_side_margin);
        this.mMenuVerticalPadding = resources.getDimensionPixelSize(C0005R$dimen.op_control_margin_space2);
    }

    @Override // android.widget.ListPopupWindow
    public void setAdapter(ListAdapter listAdapter) {
        this.mAdapter = listAdapter;
        super.setAdapter(listAdapter);
    }

    @Override // android.widget.ListPopupWindow
    public void show() {
        super.show();
        if (this.mOnItemLongClickListener != null) {
            getListView().setOnItemLongClickListener(this.mOnItemLongClickListener);
        }
        ListView listView = getListView();
        this.mContext.getResources();
        setVerticalOffset((-getAnchorView().getHeight()) / 2);
        if (this.mIsDropDownMode) {
            listView.setDividerHeight(0);
            if (this.mAdapter != null) {
                int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec((int) (((double) Resources.getSystem().getDisplayMetrics().widthPixels) * 0.9d), Integer.MIN_VALUE);
                int i = 0;
                for (int i2 = 0; i2 < this.mAdapter.getCount(); i2++) {
                    View view = this.mAdapter.getView(i2, null, listView);
                    view.measure(makeMeasureSpec, 0);
                    i = Math.max(view.getMeasuredWidth(), i);
                }
                int i3 = this.mMenuVerticalPadding;
                listView.setPadding(0, i3, 0, i3);
                setWidth(i);
                if (getAnchorView().getLayoutDirection() == 1) {
                    setHorizontalOffset(getAnchorView().getWidth() - i);
                }
            } else {
                return;
            }
        } else if (this.mAdapter != null) {
            int makeMeasureSpec2 = View.MeasureSpec.makeMeasureSpec((int) (((double) Resources.getSystem().getDisplayMetrics().widthPixels) * 0.9d), Integer.MIN_VALUE);
            int i4 = 0;
            for (int i5 = 0; i5 < this.mAdapter.getCount(); i5++) {
                View view2 = this.mAdapter.getView(i5, null, listView);
                view2.measure(makeMeasureSpec2, 0);
                i4 = Math.max(view2.getMeasuredWidth(), i4);
            }
            int i6 = this.mMenuVerticalPadding;
            listView.setPadding(0, i6, 0, i6);
            setWidth(i4);
            if (getAnchorView().getLayoutDirection() == 0) {
                setHorizontalOffset((getAnchorView().getWidth() - this.mGlobalActionsSidePadding) - i4);
            } else {
                setHorizontalOffset(this.mGlobalActionsSidePadding);
            }
        } else {
            return;
        }
        super.show();
    }

    public void setOnItemLongClickListener(AdapterView.OnItemLongClickListener onItemLongClickListener) {
        this.mOnItemLongClickListener = onItemLongClickListener;
    }
}
