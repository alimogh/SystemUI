package androidx.leanback.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.leanback.R$attr;
import androidx.leanback.R$id;
import androidx.leanback.R$layout;
public class TitleView extends FrameLayout {
    public TitleView(Context context) {
        this(context, null);
    }

    public TitleView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, R$attr.browseTitleViewStyle);
    }

    public TitleView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        View inflate = LayoutInflater.from(context).inflate(R$layout.lb_title_view, this);
        ImageView imageView = (ImageView) inflate.findViewById(R$id.title_badge);
        TextView textView = (TextView) inflate.findViewById(R$id.title_text);
        SearchOrbView searchOrbView = (SearchOrbView) inflate.findViewById(R$id.title_orb);
        setClipToPadding(false);
        setClipChildren(false);
    }
}
