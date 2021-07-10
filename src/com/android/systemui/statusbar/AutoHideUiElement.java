package com.android.systemui.statusbar;
public interface AutoHideUiElement {
    void hide();

    boolean isHideNavBar();

    boolean isVisible();

    void refreshLayout(int i);

    default boolean shouldHideOnTouch() {
        return true;
    }

    void synchronizeState();
}
