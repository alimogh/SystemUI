package androidx.core.view;
public interface NestedScrollingChild {
    @Override // androidx.core.view.NestedScrollingChild
    boolean isNestedScrollingEnabled();

    @Override // androidx.core.view.NestedScrollingChild
    void stopNestedScroll();
}
