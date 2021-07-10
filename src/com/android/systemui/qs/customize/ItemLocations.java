package com.android.systemui.qs.customize;

import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
public class ItemLocations {
    private static int FLIP_PAGE_WIDTH = 80;
    private static int SCREEN_WIDTH = 1080;
    private int mColumns;
    private boolean mIsLayoutRTL = false;
    private int mItemHeight;
    private Rect[] mItemLocations;
    private int mItemWidth;
    private int mItems = 0;
    private int mMargin;
    private Point mParentLocation;
    private int mRows;

    public ItemLocations(int i, int i2, int i3, int i4, int i5) {
        SCREEN_WIDTH = Resources.getSystem().getDisplayMetrics().widthPixels;
        Log.d("ItemLocations", "SCREEN_WIDTH=" + SCREEN_WIDTH);
        this.mParentLocation = new Point(0, 0);
        this.mItemWidth = i;
        this.mItemHeight = i2;
        this.mMargin = i3;
        this.mColumns = i4;
        this.mRows = i5;
        int i6 = i4 * i5;
        this.mItems = i6;
        this.mItemLocations = new Rect[i6];
        initLocationItems();
    }

    public void setParentLocation(int i, int i2, int i3) {
        this.mParentLocation.set(i, i2);
        this.mItemWidth = i3 / this.mColumns;
        initLocationItems();
    }

    public int getMaxItems() {
        return this.mItems;
    }

    public int getColumns() {
        return this.mColumns;
    }

    private void initLocationItems() {
        int i = this.mItemHeight;
        int i2 = this.mMargin;
        int i3 = i + (i2 * 2);
        int i4 = this.mItemWidth + (i2 * 2);
        Log.d("ItemLocations", "mItemWidth=" + this.mItemWidth + ", mItemHeight=" + this.mItemHeight);
        Point point = this.mParentLocation;
        int i5 = point.x;
        int i6 = point.y;
        int i7 = 0;
        int i8 = 0;
        while (true) {
            int i9 = this.mRows;
            if (i7 < i9) {
                if (i7 == i9 - 1) {
                    i3 += 10;
                }
                int i10 = i6 + 0;
                for (int i11 = 0; i11 < this.mColumns; i11++) {
                    int i12 = i5 + 0;
                    int i13 = i12 + i4;
                    this.mItemLocations[i8] = new Rect(i12, i10, i13, i10 + i3);
                    Log.d("ItemLocations", "Location=" + this.mItemLocations[i8]);
                    i8++;
                    i5 = i13 + 0;
                }
                i5 = this.mParentLocation.x;
                i6 = i10 + i3 + 0;
                i7++;
            } else {
                return;
            }
        }
    }

    public int getPositionIndex(int i, int i2) {
        for (int i3 = 0; i3 < this.mItems; i3++) {
            if (this.mItemLocations[i3].contains(i, i2)) {
                if (!isLayoutRTL()) {
                    return i3;
                } else {
                    int i4 = this.mColumns;
                    return ((((i3 / i4) * i4) + i4) - (i3 % i4)) - 1;
                }
            }
        }
        return -1;
    }

    public void setLayoutRTL(boolean z) {
        this.mIsLayoutRTL = z;
    }

    public boolean isLayoutRTL() {
        return this.mIsLayoutRTL;
    }

    public boolean isGoingToNextPage(int i) {
        return isLayoutRTL() ? i < FLIP_PAGE_WIDTH : i > SCREEN_WIDTH - FLIP_PAGE_WIDTH;
    }

    public boolean isGoingToPrevPage(int i) {
        return isLayoutRTL() ? i > SCREEN_WIDTH - FLIP_PAGE_WIDTH : i < FLIP_PAGE_WIDTH;
    }
}
