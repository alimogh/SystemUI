package com.android.systemui.qs.customize;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import java.util.HashMap;
public class RtlViewPager extends ViewPager {
    private int mLayoutDirection = 0;
    private HashMap<ViewPager.OnPageChangeListener, ReversingOnPageChangeListener> mPageChangeListeners = new HashMap<>();

    public RtlViewPager(Context context) {
        super(context);
    }

    public RtlViewPager(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override // android.view.View
    public void onRtlPropertiesChanged(int i) {
        super.onRtlPropertiesChanged(i);
        int i2 = 0;
        int i3 = 1;
        if (i != 1) {
            i3 = 0;
        }
        if (i3 != this.mLayoutDirection) {
            PagerAdapter adapter = super.getAdapter();
            if (adapter != null) {
                i2 = getCurrentItem();
            }
            this.mLayoutDirection = i3;
            if (adapter != null) {
                adapter.notifyDataSetChanged();
                setCurrentItem(i2);
            }
        }
    }

    @Override // androidx.viewpager.widget.ViewPager
    public void setAdapter(PagerAdapter pagerAdapter) {
        if (pagerAdapter != null) {
            pagerAdapter = new ReversingAdapter(pagerAdapter);
        }
        super.setAdapter(pagerAdapter);
        setCurrentItem(0);
    }

    @Override // androidx.viewpager.widget.ViewPager
    public PagerAdapter getAdapter() {
        ReversingAdapter reversingAdapter = (ReversingAdapter) super.getAdapter();
        if (reversingAdapter == null) {
            return null;
        }
        return reversingAdapter.getDelegate();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isRtl() {
        return this.mLayoutDirection == 1;
    }

    @Override // androidx.viewpager.widget.ViewPager
    public int getCurrentItem() {
        int currentItem = super.getCurrentItem();
        PagerAdapter adapter = super.getAdapter();
        return (adapter == null || !isRtl()) ? currentItem : (adapter.getCount() - currentItem) - 1;
    }

    @Override // androidx.viewpager.widget.ViewPager
    public void setCurrentItem(int i, boolean z) {
        PagerAdapter adapter = super.getAdapter();
        if (adapter != null && isRtl()) {
            i = (adapter.getCount() - i) - 1;
        }
        super.setCurrentItem(i, z);
    }

    @Override // androidx.viewpager.widget.ViewPager
    public void setCurrentItem(int i) {
        PagerAdapter adapter = super.getAdapter();
        if (adapter != null && isRtl()) {
            i = (adapter.getCount() - i) - 1;
        }
        super.setCurrentItem(i);
    }

    public static class SavedState implements Parcelable {
        private final int mLayoutDirection;
        private final Parcelable mViewPagerSavedState;

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        private SavedState(Parcelable parcelable, int i) {
            this.mViewPagerSavedState = parcelable;
            this.mLayoutDirection = i;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeParcelable(this.mViewPagerSavedState, i);
            parcel.writeInt(this.mLayoutDirection);
        }
    }

    @Override // androidx.viewpager.widget.ViewPager, android.view.View
    public Parcelable onSaveInstanceState() {
        return new SavedState(super.onSaveInstanceState(), this.mLayoutDirection);
    }

    @Override // androidx.viewpager.widget.ViewPager, android.view.View
    public void onRestoreInstanceState(Parcelable parcelable) {
        if (!(parcelable instanceof SavedState)) {
            super.onRestoreInstanceState(parcelable);
            return;
        }
        SavedState savedState = (SavedState) parcelable;
        this.mLayoutDirection = savedState.mLayoutDirection;
        super.onRestoreInstanceState(savedState.mViewPagerSavedState);
    }

    @Override // androidx.viewpager.widget.ViewPager
    public void setOnPageChangeListener(ViewPager.OnPageChangeListener onPageChangeListener) {
        super.setOnPageChangeListener(new ReversingOnPageChangeListener(onPageChangeListener));
    }

    @Override // androidx.viewpager.widget.ViewPager
    public void addOnPageChangeListener(ViewPager.OnPageChangeListener onPageChangeListener) {
        ReversingOnPageChangeListener reversingOnPageChangeListener = new ReversingOnPageChangeListener(onPageChangeListener);
        this.mPageChangeListeners.put(onPageChangeListener, reversingOnPageChangeListener);
        super.addOnPageChangeListener(reversingOnPageChangeListener);
    }

    @Override // androidx.viewpager.widget.ViewPager
    public void removeOnPageChangeListener(ViewPager.OnPageChangeListener onPageChangeListener) {
        super.removeOnPageChangeListener(this.mPageChangeListeners.get(onPageChangeListener));
    }

    /* access modifiers changed from: protected */
    @Override // androidx.viewpager.widget.ViewPager, android.view.View
    public void onMeasure(int i, int i2) {
        if (View.MeasureSpec.getMode(i2) == 0) {
            int i3 = 0;
            for (int i4 = 0; i4 < getChildCount(); i4++) {
                View childAt = getChildAt(i4);
                childAt.measure(i, View.MeasureSpec.makeMeasureSpec(0, 0));
                int measuredHeight = childAt.getMeasuredHeight();
                if (measuredHeight > i3) {
                    i3 = measuredHeight;
                }
            }
            i2 = View.MeasureSpec.makeMeasureSpec(i3, 1073741824);
        }
        super.onMeasure(i, i2);
    }

    /* access modifiers changed from: private */
    public class ReversingOnPageChangeListener implements ViewPager.OnPageChangeListener {
        private final ViewPager.OnPageChangeListener mListener;

        public ReversingOnPageChangeListener(ViewPager.OnPageChangeListener onPageChangeListener) {
            this.mListener = onPageChangeListener;
        }

        @Override // androidx.viewpager.widget.ViewPager.OnPageChangeListener
        public void onPageScrolled(int i, float f, int i2) {
            int width = RtlViewPager.this.getWidth();
            PagerAdapter adapter = RtlViewPager.super.getAdapter();
            if (RtlViewPager.this.isRtl() && adapter != null) {
                int count = adapter.getCount();
                float f2 = (float) width;
                int pageWidth = ((int) ((1.0f - adapter.getPageWidth(i)) * f2)) + i2;
                while (i < count && pageWidth > 0) {
                    i++;
                    pageWidth -= (int) (adapter.getPageWidth(i) * f2);
                }
                i = (count - i) - 1;
                i2 = -pageWidth;
                f = ((float) i2) / (f2 * adapter.getPageWidth(i));
            }
            this.mListener.onPageScrolled(i, f, i2);
        }

        @Override // androidx.viewpager.widget.ViewPager.OnPageChangeListener
        public void onPageSelected(int i) {
            PagerAdapter adapter = RtlViewPager.super.getAdapter();
            if (RtlViewPager.this.isRtl() && adapter != null) {
                i = (adapter.getCount() - i) - 1;
            }
            this.mListener.onPageSelected(i);
        }

        @Override // androidx.viewpager.widget.ViewPager.OnPageChangeListener
        public void onPageScrollStateChanged(int i) {
            this.mListener.onPageScrollStateChanged(i);
        }
    }

    /* access modifiers changed from: private */
    public class ReversingAdapter extends DelegatingPagerAdapter {
        public ReversingAdapter(PagerAdapter pagerAdapter) {
            super(pagerAdapter);
        }

        @Override // com.android.systemui.qs.customize.DelegatingPagerAdapter, androidx.viewpager.widget.PagerAdapter
        public void destroyItem(ViewGroup viewGroup, int i, Object obj) {
            if (RtlViewPager.this.isRtl()) {
                i = (getCount() - i) - 1;
            }
            super.destroyItem(viewGroup, i, obj);
        }

        @Override // com.android.systemui.qs.customize.DelegatingPagerAdapter, androidx.viewpager.widget.PagerAdapter
        public void destroyItem(View view, int i, Object obj) {
            if (RtlViewPager.this.isRtl()) {
                i = (getCount() - i) - 1;
            }
            super.destroyItem(view, i, obj);
        }

        @Override // com.android.systemui.qs.customize.DelegatingPagerAdapter, androidx.viewpager.widget.PagerAdapter
        public int getItemPosition(Object obj) {
            int itemPosition = super.getItemPosition(obj);
            if (!RtlViewPager.this.isRtl()) {
                return itemPosition;
            }
            if (itemPosition == -1 || itemPosition == -2) {
                return -2;
            }
            return (getCount() - itemPosition) - 1;
        }

        @Override // com.android.systemui.qs.customize.DelegatingPagerAdapter, androidx.viewpager.widget.PagerAdapter
        public CharSequence getPageTitle(int i) {
            if (RtlViewPager.this.isRtl()) {
                i = (getCount() - i) - 1;
            }
            return super.getPageTitle(i);
        }

        @Override // com.android.systemui.qs.customize.DelegatingPagerAdapter, androidx.viewpager.widget.PagerAdapter
        public float getPageWidth(int i) {
            if (RtlViewPager.this.isRtl()) {
                i = (getCount() - i) - 1;
            }
            return super.getPageWidth(i);
        }

        @Override // com.android.systemui.qs.customize.DelegatingPagerAdapter, androidx.viewpager.widget.PagerAdapter
        public Object instantiateItem(ViewGroup viewGroup, int i) {
            if (RtlViewPager.this.isRtl()) {
                i = (getCount() - i) - 1;
            }
            return super.instantiateItem(viewGroup, i);
        }

        @Override // com.android.systemui.qs.customize.DelegatingPagerAdapter, androidx.viewpager.widget.PagerAdapter
        public Object instantiateItem(View view, int i) {
            if (RtlViewPager.this.isRtl()) {
                i = (getCount() - i) - 1;
            }
            return super.instantiateItem(view, i);
        }

        @Override // com.android.systemui.qs.customize.DelegatingPagerAdapter, androidx.viewpager.widget.PagerAdapter
        public void setPrimaryItem(View view, int i, Object obj) {
            if (RtlViewPager.this.isRtl()) {
                i = (getCount() - i) - 1;
            }
            super.setPrimaryItem(view, i, obj);
        }

        @Override // com.android.systemui.qs.customize.DelegatingPagerAdapter, androidx.viewpager.widget.PagerAdapter
        public void setPrimaryItem(ViewGroup viewGroup, int i, Object obj) {
            if (RtlViewPager.this.isRtl()) {
                i = (getCount() - i) - 1;
            }
            super.setPrimaryItem(viewGroup, i, obj);
        }
    }
}
