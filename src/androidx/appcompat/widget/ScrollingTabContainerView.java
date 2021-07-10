package androidx.appcompat.widget;

import android.widget.AdapterView;
import android.widget.HorizontalScrollView;
public class ScrollingTabContainerView extends HorizontalScrollView implements AdapterView.OnItemSelectedListener {
    public abstract void setAllowCollapse(boolean z);
}
