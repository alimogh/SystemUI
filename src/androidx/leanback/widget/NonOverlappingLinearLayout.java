package androidx.leanback.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import java.util.ArrayList;
public class NonOverlappingLinearLayout extends LinearLayout {
    boolean mDeferFocusableViewAvailableInLayout;
    boolean mFocusableViewAvailableFixEnabled;
    final ArrayList<ArrayList<View>> mSortedAvailableViews;

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    public NonOverlappingLinearLayout(Context context) {
        this(context, null);
    }

    public NonOverlappingLinearLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public NonOverlappingLinearLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mFocusableViewAvailableFixEnabled = false;
        this.mSortedAvailableViews = new ArrayList<>();
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v0, types: [boolean] */
    /* JADX WARN: Type inference failed for: r0v1, types: [int] */
    /* JADX WARN: Type inference failed for: r0v7 */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Override // android.widget.LinearLayout, android.view.View, android.view.ViewGroup
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onLayout(boolean r5, int r6, int r7, int r8, int r9) {
        /*
            r4 = this;
            r0 = 0
            boolean r1 = r4.mFocusableViewAvailableFixEnabled     // Catch:{ all -> 0x009c }
            r2 = 1
            if (r1 == 0) goto L_0x0014
            int r1 = r4.getOrientation()     // Catch:{ all -> 0x009c }
            if (r1 != 0) goto L_0x0014
            int r1 = r4.getLayoutDirection()     // Catch:{ all -> 0x009c }
            if (r1 != r2) goto L_0x0014
            r1 = r2
            goto L_0x0015
        L_0x0014:
            r1 = r0
        L_0x0015:
            r4.mDeferFocusableViewAvailableInLayout = r1     // Catch:{ all -> 0x009c }
            if (r1 == 0) goto L_0x0049
        L_0x0019:
            java.util.ArrayList<java.util.ArrayList<android.view.View>> r1 = r4.mSortedAvailableViews     // Catch:{ all -> 0x009c }
            int r1 = r1.size()     // Catch:{ all -> 0x009c }
            int r3 = r4.getChildCount()     // Catch:{ all -> 0x009c }
            if (r1 <= r3) goto L_0x0032
            java.util.ArrayList<java.util.ArrayList<android.view.View>> r1 = r4.mSortedAvailableViews     // Catch:{ all -> 0x009c }
            java.util.ArrayList<java.util.ArrayList<android.view.View>> r3 = r4.mSortedAvailableViews     // Catch:{ all -> 0x009c }
            int r3 = r3.size()     // Catch:{ all -> 0x009c }
            int r3 = r3 - r2
            r1.remove(r3)     // Catch:{ all -> 0x009c }
            goto L_0x0019
        L_0x0032:
            java.util.ArrayList<java.util.ArrayList<android.view.View>> r1 = r4.mSortedAvailableViews     // Catch:{ all -> 0x009c }
            int r1 = r1.size()     // Catch:{ all -> 0x009c }
            int r2 = r4.getChildCount()     // Catch:{ all -> 0x009c }
            if (r1 >= r2) goto L_0x0049
            java.util.ArrayList<java.util.ArrayList<android.view.View>> r1 = r4.mSortedAvailableViews     // Catch:{ all -> 0x009c }
            java.util.ArrayList r2 = new java.util.ArrayList     // Catch:{ all -> 0x009c }
            r2.<init>()     // Catch:{ all -> 0x009c }
            r1.add(r2)     // Catch:{ all -> 0x009c }
            goto L_0x0032
        L_0x0049:
            super.onLayout(r5, r6, r7, r8, r9)     // Catch:{ all -> 0x009c }
            boolean r5 = r4.mDeferFocusableViewAvailableInLayout     // Catch:{ all -> 0x009c }
            if (r5 == 0) goto L_0x007f
            r5 = r0
        L_0x0051:
            java.util.ArrayList<java.util.ArrayList<android.view.View>> r6 = r4.mSortedAvailableViews     // Catch:{ all -> 0x009c }
            int r6 = r6.size()     // Catch:{ all -> 0x009c }
            if (r5 >= r6) goto L_0x007f
            r6 = r0
        L_0x005a:
            java.util.ArrayList<java.util.ArrayList<android.view.View>> r7 = r4.mSortedAvailableViews     // Catch:{ all -> 0x009c }
            java.lang.Object r7 = r7.get(r5)     // Catch:{ all -> 0x009c }
            java.util.ArrayList r7 = (java.util.ArrayList) r7     // Catch:{ all -> 0x009c }
            int r7 = r7.size()     // Catch:{ all -> 0x009c }
            if (r6 >= r7) goto L_0x007c
            java.util.ArrayList<java.util.ArrayList<android.view.View>> r7 = r4.mSortedAvailableViews     // Catch:{ all -> 0x009c }
            java.lang.Object r7 = r7.get(r5)     // Catch:{ all -> 0x009c }
            java.util.ArrayList r7 = (java.util.ArrayList) r7     // Catch:{ all -> 0x009c }
            java.lang.Object r7 = r7.get(r6)     // Catch:{ all -> 0x009c }
            android.view.View r7 = (android.view.View) r7     // Catch:{ all -> 0x009c }
            super.focusableViewAvailable(r7)     // Catch:{ all -> 0x009c }
            int r6 = r6 + 1
            goto L_0x005a
        L_0x007c:
            int r5 = r5 + 1
            goto L_0x0051
        L_0x007f:
            boolean r5 = r4.mDeferFocusableViewAvailableInLayout
            if (r5 == 0) goto L_0x009b
            r4.mDeferFocusableViewAvailableInLayout = r0
        L_0x0085:
            java.util.ArrayList<java.util.ArrayList<android.view.View>> r5 = r4.mSortedAvailableViews
            int r5 = r5.size()
            if (r0 >= r5) goto L_0x009b
            java.util.ArrayList<java.util.ArrayList<android.view.View>> r5 = r4.mSortedAvailableViews
            java.lang.Object r5 = r5.get(r0)
            java.util.ArrayList r5 = (java.util.ArrayList) r5
            r5.clear()
            int r0 = r0 + 1
            goto L_0x0085
        L_0x009b:
            return
        L_0x009c:
            r5 = move-exception
            boolean r6 = r4.mDeferFocusableViewAvailableInLayout
            if (r6 == 0) goto L_0x00b9
            r4.mDeferFocusableViewAvailableInLayout = r0
        L_0x00a3:
            java.util.ArrayList<java.util.ArrayList<android.view.View>> r6 = r4.mSortedAvailableViews
            int r6 = r6.size()
            if (r0 >= r6) goto L_0x00b9
            java.util.ArrayList<java.util.ArrayList<android.view.View>> r6 = r4.mSortedAvailableViews
            java.lang.Object r6 = r6.get(r0)
            java.util.ArrayList r6 = (java.util.ArrayList) r6
            r6.clear()
            int r0 = r0 + 1
            goto L_0x00a3
        L_0x00b9:
            throw r5
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.leanback.widget.NonOverlappingLinearLayout.onLayout(boolean, int, int, int, int):void");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001c, code lost:
        r0 = -1;
     */
    @Override // android.view.ViewParent, android.view.ViewGroup
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void focusableViewAvailable(android.view.View r4) {
        /*
            r3 = this;
            boolean r0 = r3.mDeferFocusableViewAvailableInLayout
            if (r0 == 0) goto L_0x002b
            r0 = r4
        L_0x0005:
            r1 = -1
            if (r0 == r3) goto L_0x001c
            if (r0 == 0) goto L_0x001c
            android.view.ViewParent r2 = r0.getParent()
            if (r2 != r3) goto L_0x0015
            int r0 = r3.indexOfChild(r0)
            goto L_0x001d
        L_0x0015:
            android.view.ViewParent r0 = r0.getParent()
            android.view.View r0 = (android.view.View) r0
            goto L_0x0005
        L_0x001c:
            r0 = r1
        L_0x001d:
            if (r0 == r1) goto L_0x002e
            java.util.ArrayList<java.util.ArrayList<android.view.View>> r3 = r3.mSortedAvailableViews
            java.lang.Object r3 = r3.get(r0)
            java.util.ArrayList r3 = (java.util.ArrayList) r3
            r3.add(r4)
            goto L_0x002e
        L_0x002b:
            super.focusableViewAvailable(r4)
        L_0x002e:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.leanback.widget.NonOverlappingLinearLayout.focusableViewAvailable(android.view.View):void");
    }
}
