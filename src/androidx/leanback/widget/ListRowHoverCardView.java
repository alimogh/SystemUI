package androidx.leanback.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.leanback.R$id;
import androidx.leanback.R$layout;
public final class ListRowHoverCardView extends LinearLayout {
    public ListRowHoverCardView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ListRowHoverCardView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        LayoutInflater.from(context).inflate(R$layout.lb_list_row_hovercard, this);
        TextView textView = (TextView) findViewById(R$id.title);
        TextView textView2 = (TextView) findViewById(R$id.description);
    }
}
