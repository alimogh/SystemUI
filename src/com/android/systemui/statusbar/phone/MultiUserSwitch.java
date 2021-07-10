package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.os.UserManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.FrameLayout;
import com.android.systemui.C0003R$bool;
import com.android.systemui.C0015R$string;
import com.android.systemui.DejankUtils;
import com.android.systemui.Dependency;
import com.android.systemui.Prefs;
import com.android.systemui.plugins.qs.DetailAdapter;
import com.android.systemui.qs.QSPanel;
import com.android.systemui.statusbar.policy.KeyguardUserSwitcher;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import com.oneplus.systemui.util.OpMdmLogger;
import java.util.function.Supplier;
public class MultiUserSwitch extends FrameLayout implements View.OnClickListener {
    private boolean mKeyguardMode;
    private KeyguardUserSwitcher mKeyguardUserSwitcher;
    protected QSPanel mQsPanel;
    private final int[] mTmpInt2 = new int[2];
    private UserSwitcherController.BaseUserAdapter mUserListener;
    final UserManager mUserManager = UserManager.get(getContext());
    protected UserSwitcherController mUserSwitcherController;

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    public MultiUserSwitch(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        setOnClickListener(this);
        refreshContentDescription();
    }

    public void setQsPanel(QSPanel qSPanel) {
        this.mQsPanel = qSPanel;
        setUserSwitcherController((UserSwitcherController) Dependency.get(UserSwitcherController.class));
    }

    public boolean hasMultipleUsers() {
        UserSwitcherController.BaseUserAdapter baseUserAdapter = this.mUserListener;
        if (baseUserAdapter == null || baseUserAdapter.getUserCount() == 0 || !Prefs.getBoolean(getContext(), "HasSeenMultiUser", false)) {
            return false;
        }
        return true;
    }

    public void setUserSwitcherController(UserSwitcherController userSwitcherController) {
        this.mUserSwitcherController = userSwitcherController;
        registerListener();
        refreshContentDescription();
    }

    public void setKeyguardMode(boolean z) {
        this.mKeyguardMode = z;
        registerListener();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$isMultiUserEnabled$0 */
    public /* synthetic */ Boolean lambda$isMultiUserEnabled$0$MultiUserSwitch() {
        return Boolean.valueOf(this.mUserManager.isUserSwitcherEnabled(((FrameLayout) this).mContext.getResources().getBoolean(C0003R$bool.qs_show_user_switcher_for_single_user)));
    }

    public boolean isMultiUserEnabled() {
        return ((Boolean) DejankUtils.whitelistIpcs(new Supplier() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$MultiUserSwitch$xTCgN3mrazovqBP0eNshKQ-KXu4
            @Override // java.util.function.Supplier
            public final Object get() {
                return MultiUserSwitch.this.lambda$isMultiUserEnabled$0$MultiUserSwitch();
            }
        })).booleanValue();
    }

    private void registerListener() {
        UserSwitcherController userSwitcherController;
        if (this.mUserManager.isUserSwitcherEnabled() && this.mUserListener == null && (userSwitcherController = this.mUserSwitcherController) != null) {
            this.mUserListener = new UserSwitcherController.BaseUserAdapter(userSwitcherController) { // from class: com.android.systemui.statusbar.phone.MultiUserSwitch.1
                @Override // android.widget.Adapter
                public View getView(int i, View view, ViewGroup viewGroup) {
                    return null;
                }

                @Override // android.widget.BaseAdapter
                public void notifyDataSetChanged() {
                    MultiUserSwitch.this.refreshContentDescription();
                }
            };
            refreshContentDescription();
        }
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        OpMdmLogger.log("quick_user", "icon", "1");
        if (this.mKeyguardMode) {
            KeyguardUserSwitcher keyguardUserSwitcher = this.mKeyguardUserSwitcher;
            if (keyguardUserSwitcher != null) {
                keyguardUserSwitcher.show(true);
            }
        } else if (this.mQsPanel != null && this.mUserSwitcherController != null) {
            View childAt = getChildCount() > 0 ? getChildAt(0) : this;
            childAt.getLocationInWindow(this.mTmpInt2);
            int[] iArr = this.mTmpInt2;
            iArr[0] = iArr[0] + (childAt.getWidth() / 2);
            int[] iArr2 = this.mTmpInt2;
            iArr2[1] = iArr2[1] + (childAt.getHeight() / 2);
            this.mQsPanel.showDetailAdapter(true, getUserDetailAdapter(), this.mTmpInt2);
        }
    }

    @Override // android.view.View
    public void setClickable(boolean z) {
        super.setClickable(z);
        refreshContentDescription();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$refreshContentDescription$1 */
    public /* synthetic */ Boolean lambda$refreshContentDescription$1$MultiUserSwitch() {
        return Boolean.valueOf(this.mUserManager.isUserSwitcherEnabled());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void refreshContentDescription() {
        UserSwitcherController userSwitcherController;
        String str = null;
        String currentUserName = (!((Boolean) DejankUtils.whitelistIpcs(new Supplier() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$MultiUserSwitch$Rk51aHgdEumbLebIN0bdVUl-aWQ
            @Override // java.util.function.Supplier
            public final Object get() {
                return MultiUserSwitch.this.lambda$refreshContentDescription$1$MultiUserSwitch();
            }
        })).booleanValue() || (userSwitcherController = this.mUserSwitcherController) == null) ? null : userSwitcherController.getCurrentUserName(((FrameLayout) this).mContext);
        if (!TextUtils.isEmpty(currentUserName)) {
            str = ((FrameLayout) this).mContext.getString(C0015R$string.accessibility_quick_settings_user, currentUserName);
        }
        if (!TextUtils.equals(getContentDescription(), str)) {
            setContentDescription(str);
        }
    }

    @Override // android.view.View
    public void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        super.onInitializeAccessibilityEvent(accessibilityEvent);
        accessibilityEvent.setClassName(Button.class.getName());
    }

    @Override // android.view.View
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        accessibilityNodeInfo.setClassName(Button.class.getName());
    }

    /* access modifiers changed from: protected */
    public DetailAdapter getUserDetailAdapter() {
        return this.mUserSwitcherController.userDetailAdapter;
    }
}
