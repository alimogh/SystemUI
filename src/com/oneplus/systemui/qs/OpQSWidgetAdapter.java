package com.oneplus.systemui.qs;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0011R$layout;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.ActivityStarter;
import com.oneplus.systemui.qs.OpQSWidgetAdapter;
import com.oneplus.util.OpUtils;
import com.oneplus.util.ThemeColorUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.function.Predicate;
public class OpQSWidgetAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final LayoutInflater mInflater;
    private boolean mListening = false;
    private final ArrayList<OpWidgetInfo> mWidgets = new ArrayList<>();

    public OpQSWidgetAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
        ActivityStarter activityStarter = (ActivityStarter) Dependency.get(ActivityStarter.class);
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new WidgetViewHolder(this, this.mInflater.inflate(C0011R$layout.op_qs_widget_view, viewGroup, false), i);
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        ((WidgetViewHolder) viewHolder).setInfo(this.mWidgets.get(i));
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        ArrayList<OpWidgetInfo> arrayList = this.mWidgets;
        if (arrayList != null) {
            return arrayList.size();
        }
        return 0;
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemViewType(int i) {
        ArrayList<OpWidgetInfo> arrayList = this.mWidgets;
        if (arrayList != null) {
            return arrayList.get(i).mWidgetType;
        }
        return 0;
    }

    public void setListening(boolean z) {
        if (this.mListening != z) {
            this.mListening = z;
        }
    }

    public void addItem(OpWidgetInfo opWidgetInfo) {
        int i = opWidgetInfo.mWidgetType;
        boolean z = false;
        if (i == 1 || i == 2 || i == 3) {
            Iterator<OpWidgetInfo> it = this.mWidgets.iterator();
            while (it.hasNext()) {
                OpWidgetInfo next = it.next();
                if (next.mWidgetType == opWidgetInfo.mWidgetType) {
                    next.mIconRes = opWidgetInfo.mIconRes;
                    next.mText = opWidgetInfo.mText;
                    z = true;
                }
            }
        }
        if (!z) {
            this.mWidgets.add(opWidgetInfo);
            Collections.sort(this.mWidgets);
        }
        notifyDataSetChanged();
    }

    static /* synthetic */ boolean lambda$removeItem$0(int i, OpWidgetInfo opWidgetInfo) {
        return opWidgetInfo.mWidgetType == i;
    }

    public void removeItem(int i) {
        this.mWidgets.removeIf(new Predicate(i) { // from class: com.oneplus.systemui.qs.-$$Lambda$OpQSWidgetAdapter$8KQ2rPpS2lkZafqga_VMFt8_CxM
            public final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return OpQSWidgetAdapter.lambda$removeItem$0(this.f$0, (OpQSWidgetAdapter.OpWidgetInfo) obj);
            }
        });
        notifyDataSetChanged();
    }

    private class WidgetViewHolder extends RecyclerView.ViewHolder {
        private final ImageView mIcon;
        private final View mRootView;
        private final TextView mText;

        public WidgetViewHolder(OpQSWidgetAdapter opQSWidgetAdapter, View view, int i) {
            super(view);
            int i2;
            this.mRootView = view;
            this.mIcon = (ImageView) view.findViewById(C0008R$id.op_qs_widget_icon);
            this.mText = (TextView) view.findViewById(C0008R$id.op_qs_widget_text);
            this.mIcon.setImageTintList(ColorStateList.valueOf(ThemeColorUtils.getColor(18)));
            this.mText.setTextColor(ThemeColorUtils.getColor(19));
            View view2 = this.mRootView;
            if (ThemeColorUtils.getCurrentTheme() == 0) {
                i2 = C0006R$drawable.op_qs_widget_bg_light;
            } else {
                i2 = OpUtils.isREDVersion() ? C0006R$drawable.op_qs_red_all : C0006R$drawable.op_qs_widget_bg_dark;
            }
            view2.setBackgroundResource(i2);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setInfo(OpWidgetInfo opWidgetInfo) {
            this.mIcon.setImageResource(opWidgetInfo.mIconRes);
            this.mText.setText(opWidgetInfo.mText);
            this.mRootView.setOnClickListener(opWidgetInfo.mClickListener);
        }
    }

    public static class OpWidgetInfo implements Comparable<OpWidgetInfo> {
        private View.OnClickListener mClickListener;
        private int mIconRes;
        private String mText;
        private int mWidgetType;

        /* JADX INFO: this call moved to the top of the method (can break code semantics) */
        public OpWidgetInfo(int i, int i2, CharSequence charSequence, View.OnClickListener onClickListener) {
            this(i, i2, charSequence != null ? charSequence.toString() : "", onClickListener);
        }

        public OpWidgetInfo(int i, int i2, String str, View.OnClickListener onClickListener) {
            this.mWidgetType = i;
            this.mIconRes = i2;
            this.mText = str == null ? "" : str;
            this.mClickListener = onClickListener;
        }

        public int compareTo(OpWidgetInfo opWidgetInfo) {
            return opWidgetInfo.mWidgetType - this.mWidgetType;
        }
    }
}
