package com.android.systemui.qs.customize;

import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import java.util.ArrayList;
import java.util.List;
public class QSEditPageAdapter extends PagerAdapter {
    private List<RecyclerView> mListViews = new ArrayList();

    @Override // androidx.viewpager.widget.PagerAdapter
    public boolean isViewFromObject(View view, Object obj) {
        return view == obj;
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    public void destroyItem(ViewGroup viewGroup, int i, Object obj) {
        viewGroup.removeView((View) obj);
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    public Object instantiateItem(ViewGroup viewGroup, int i) {
        RecyclerView recyclerView = this.mListViews.get(i);
        viewGroup.addView(recyclerView);
        return recyclerView;
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    public int getCount() {
        return this.mListViews.size();
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    public int getItemPosition(Object obj) {
        return this.mListViews.contains(obj) ? -1 : -2;
    }

    public void addPage(RecyclerView recyclerView) {
        this.mListViews.add(recyclerView);
        notifyDataSetChanged();
    }

    public void removePage(int i) {
        this.mListViews.remove(i);
        notifyDataSetChanged();
    }

    public RecyclerView getPage(int i) {
        if (i >= this.mListViews.size() || i < 0) {
            i = Math.max(this.mListViews.size() - 1, 0);
        }
        return this.mListViews.get(i);
    }

    public boolean containsPage(RecyclerView recyclerView) {
        return this.mListViews.contains(recyclerView);
    }
}
