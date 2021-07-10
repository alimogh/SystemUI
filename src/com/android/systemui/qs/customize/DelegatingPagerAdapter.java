package com.android.systemui.qs.customize;

import android.database.DataSetObserver;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import androidx.viewpager.widget.PagerAdapter;
public class DelegatingPagerAdapter extends PagerAdapter {
    private final PagerAdapter mDelegate;

    public DelegatingPagerAdapter(PagerAdapter pagerAdapter) {
        this.mDelegate = pagerAdapter;
        pagerAdapter.registerDataSetObserver(new MyDataSetObserver());
    }

    public PagerAdapter getDelegate() {
        return this.mDelegate;
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    public int getCount() {
        return this.mDelegate.getCount();
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    public void startUpdate(ViewGroup viewGroup) {
        this.mDelegate.startUpdate(viewGroup);
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    public Object instantiateItem(ViewGroup viewGroup, int i) {
        return this.mDelegate.instantiateItem(viewGroup, i);
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    public void destroyItem(ViewGroup viewGroup, int i, Object obj) {
        this.mDelegate.destroyItem(viewGroup, i, obj);
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    public void setPrimaryItem(ViewGroup viewGroup, int i, Object obj) {
        this.mDelegate.setPrimaryItem(viewGroup, i, obj);
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    public void finishUpdate(ViewGroup viewGroup) {
        this.mDelegate.finishUpdate(viewGroup);
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    @Deprecated
    public void startUpdate(View view) {
        this.mDelegate.startUpdate(view);
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    @Deprecated
    public Object instantiateItem(View view, int i) {
        return this.mDelegate.instantiateItem(view, i);
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    @Deprecated
    public void destroyItem(View view, int i, Object obj) {
        this.mDelegate.destroyItem(view, i, obj);
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    @Deprecated
    public void setPrimaryItem(View view, int i, Object obj) {
        this.mDelegate.setPrimaryItem(view, i, obj);
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    @Deprecated
    public void finishUpdate(View view) {
        this.mDelegate.finishUpdate(view);
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    public boolean isViewFromObject(View view, Object obj) {
        return this.mDelegate.isViewFromObject(view, obj);
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    public Parcelable saveState() {
        return this.mDelegate.saveState();
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    public void restoreState(Parcelable parcelable, ClassLoader classLoader) {
        this.mDelegate.restoreState(parcelable, classLoader);
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    public int getItemPosition(Object obj) {
        return this.mDelegate.getItemPosition(obj);
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    public void notifyDataSetChanged() {
        this.mDelegate.notifyDataSetChanged();
    }

    /* access modifiers changed from: package-private */
    public void superNotifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {
        this.mDelegate.registerDataSetObserver(dataSetObserver);
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
        this.mDelegate.unregisterDataSetObserver(dataSetObserver);
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    public CharSequence getPageTitle(int i) {
        return this.mDelegate.getPageTitle(i);
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    public float getPageWidth(int i) {
        return this.mDelegate.getPageWidth(i);
    }

    private static class MyDataSetObserver extends DataSetObserver {
        final DelegatingPagerAdapter mParent;

        private MyDataSetObserver(DelegatingPagerAdapter delegatingPagerAdapter) {
            this.mParent = delegatingPagerAdapter;
        }

        @Override // android.database.DataSetObserver
        public void onChanged() {
            DelegatingPagerAdapter delegatingPagerAdapter = this.mParent;
            if (delegatingPagerAdapter != null) {
                delegatingPagerAdapter.superNotifyDataSetChanged();
            }
        }

        @Override // android.database.DataSetObserver
        public void onInvalidated() {
            onChanged();
        }
    }
}
