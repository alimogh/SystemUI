package com.android.systemui.qs.customize;

import android.content.Context;
import android.util.AttributeSet;
import androidx.viewpager.widget.ViewPager;
import com.android.systemui.qs.PageIndicator;
public class QSEditViewPager extends RtlViewPager {
    private PageIndicator mPageIndicator;

    public QSEditViewPager(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setOnPageChangeListener(new ViewPager.OnPageChangeListener() { // from class: com.android.systemui.qs.customize.QSEditViewPager.1
            @Override // androidx.viewpager.widget.ViewPager.OnPageChangeListener
            public void onPageScrollStateChanged(int i) {
            }

            @Override // androidx.viewpager.widget.ViewPager.OnPageChangeListener
            public void onPageSelected(int i) {
            }

            @Override // androidx.viewpager.widget.ViewPager.OnPageChangeListener
            public void onPageScrolled(int i, float f, int i2) {
                if (QSEditViewPager.this.mPageIndicator != null) {
                    QSEditViewPager.this.mPageIndicator.setLocation(((float) i) + f);
                }
            }
        });
    }

    public void setPageIndicator(PageIndicator pageIndicator) {
        this.mPageIndicator = pageIndicator;
    }

    public void updateIndicator() {
        int count = getAdapter().getCount();
        this.mPageIndicator.setNumPages(count);
        this.mPageIndicator.setVisibility(count > 1 ? 0 : 4);
    }
}
