package androidx.appcompat.app;

import android.content.Context;
import android.content.res.Configuration;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPresenter;
import androidx.appcompat.widget.DecorToolbar;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.R$styleable;
import androidx.core.view.ViewCompat;
import java.util.ArrayList;
/* access modifiers changed from: package-private */
public class ToolbarActionBar extends ActionBar {
    DecorToolbar mDecorToolbar;
    private boolean mLastMenuVisibility;
    private boolean mMenuCallbackSet;
    private final Runnable mMenuInvalidator;
    private ArrayList<ActionBar.OnMenuVisibilityListener> mMenuVisibilityListeners;
    private Toolbar mToolBar;
    Window.Callback mWindowCallback;

    @Override // androidx.appcompat.app.ActionBar
    public void setDefaultDisplayHomeAsUpEnabled(boolean z) {
    }

    @Override // androidx.appcompat.app.ActionBar
    public void setShowHideAnimationEnabled(boolean z) {
    }

    public Toolbar getToolBar() {
        return this.mToolBar;
    }

    @Override // androidx.appcompat.app.ActionBar
    public Context getThemedContext() {
        return this.mDecorToolbar.getContext();
    }

    @Override // androidx.appcompat.app.ActionBar
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
    }

    @Override // androidx.appcompat.app.ActionBar
    public void setWindowTitle(CharSequence charSequence) {
        this.mDecorToolbar.setWindowTitle(charSequence);
    }

    @Override // androidx.appcompat.app.ActionBar
    public int getDisplayOptions() {
        return this.mDecorToolbar.getDisplayOptions();
    }

    @Override // androidx.appcompat.app.ActionBar
    public boolean openOptionsMenu() {
        return this.mDecorToolbar.showOverflowMenu();
    }

    @Override // androidx.appcompat.app.ActionBar
    public boolean closeOptionsMenu() {
        return this.mDecorToolbar.hideOverflowMenu();
    }

    @Override // androidx.appcompat.app.ActionBar
    public boolean invalidateOptionsMenu() {
        this.mDecorToolbar.getViewGroup().removeCallbacks(this.mMenuInvalidator);
        ViewCompat.postOnAnimation(this.mDecorToolbar.getViewGroup(), this.mMenuInvalidator);
        return true;
    }

    @Override // androidx.appcompat.app.ActionBar
    public boolean collapseActionView() {
        if (!this.mDecorToolbar.hasExpandedActionView()) {
            return false;
        }
        this.mDecorToolbar.collapseActionView();
        return true;
    }

    @Override // androidx.appcompat.app.ActionBar
    public boolean onMenuKeyEvent(KeyEvent keyEvent) {
        if (keyEvent.getAction() == 1) {
            openOptionsMenu();
        }
        return true;
    }

    @Override // androidx.appcompat.app.ActionBar
    public boolean onKeyShortcut(int i, KeyEvent keyEvent) {
        Menu menu = getMenu();
        if (menu == null) {
            return false;
        }
        boolean z = true;
        if (KeyCharacterMap.load(keyEvent != null ? keyEvent.getDeviceId() : -1).getKeyboardType() == 1) {
            z = false;
        }
        menu.setQwertyMode(z);
        return menu.performShortcut(i, keyEvent, 0);
    }

    /* access modifiers changed from: package-private */
    @Override // androidx.appcompat.app.ActionBar
    public void onDestroy() {
        this.mDecorToolbar.getViewGroup().removeCallbacks(this.mMenuInvalidator);
    }

    @Override // androidx.appcompat.app.ActionBar
    public void dispatchMenuVisibilityChanged(boolean z) {
        if (z != this.mLastMenuVisibility) {
            this.mLastMenuVisibility = z;
            int size = this.mMenuVisibilityListeners.size();
            for (int i = 0; i < size; i++) {
                this.mMenuVisibilityListeners.get(i).onMenuVisibilityChanged(z);
            }
        }
    }

    private Menu getMenu() {
        if (!this.mMenuCallbackSet) {
            this.mDecorToolbar.setMenuCallbacks(new ActionMenuPresenterCallback(), new MenuBuilderCallback());
            this.mMenuCallbackSet = true;
        }
        return this.mDecorToolbar.getMenu();
    }

    /* access modifiers changed from: private */
    public final class ActionMenuPresenterCallback implements MenuPresenter.Callback {
        private boolean mClosingActionMenu;

        ActionMenuPresenterCallback() {
        }

        @Override // androidx.appcompat.view.menu.MenuPresenter.Callback
        public boolean onOpenSubMenu(MenuBuilder menuBuilder) {
            Window.Callback callback = ToolbarActionBar.this.mWindowCallback;
            if (callback == null) {
                return false;
            }
            callback.onMenuOpened(R$styleable.Constraint_transitionEasing, menuBuilder);
            return true;
        }

        @Override // androidx.appcompat.view.menu.MenuPresenter.Callback
        public void onCloseMenu(MenuBuilder menuBuilder, boolean z) {
            if (!this.mClosingActionMenu) {
                this.mClosingActionMenu = true;
                ToolbarActionBar.this.mDecorToolbar.dismissPopupMenus();
                Window.Callback callback = ToolbarActionBar.this.mWindowCallback;
                if (callback != null) {
                    callback.onPanelClosed(R$styleable.Constraint_transitionEasing, menuBuilder);
                }
                this.mClosingActionMenu = false;
            }
        }
    }

    /* access modifiers changed from: private */
    public final class MenuBuilderCallback implements MenuBuilder.Callback {
        @Override // androidx.appcompat.view.menu.MenuBuilder.Callback
        public boolean onMenuItemSelected(MenuBuilder menuBuilder, MenuItem menuItem) {
            return false;
        }

        MenuBuilderCallback() {
        }

        @Override // androidx.appcompat.view.menu.MenuBuilder.Callback
        public void onMenuModeChange(MenuBuilder menuBuilder) {
            ToolbarActionBar toolbarActionBar = ToolbarActionBar.this;
            if (toolbarActionBar.mWindowCallback == null) {
                return;
            }
            if (toolbarActionBar.mDecorToolbar.isOverflowMenuShowing()) {
                ToolbarActionBar.this.mWindowCallback.onPanelClosed(R$styleable.Constraint_transitionEasing, menuBuilder);
            } else if (ToolbarActionBar.this.mWindowCallback.onPreparePanel(0, null, menuBuilder)) {
                ToolbarActionBar.this.mWindowCallback.onMenuOpened(R$styleable.Constraint_transitionEasing, menuBuilder);
            }
        }
    }
}
