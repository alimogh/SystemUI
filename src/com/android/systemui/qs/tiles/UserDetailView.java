package com.android.systemui.qs.tiles;

import android.content.Context;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.StatFs;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.UiEventLogger;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.drawable.CircleFramedDrawable;
import com.android.settingslib.drawable.UserIcons;
import com.android.systemui.C0004R$color;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0011R$layout;
import com.android.systemui.C0015R$string;
import com.android.systemui.SysUIToast;
import com.android.systemui.qs.PseudoGridView;
import com.android.systemui.qs.QSUserSwitcherEvent;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import com.oneplus.systemui.util.OpMdmLogger;
public class UserDetailView extends PseudoGridView {
    protected Adapter mAdapter;

    public UserDetailView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public static UserDetailView inflate(Context context, ViewGroup viewGroup, boolean z) {
        return (UserDetailView) LayoutInflater.from(context).inflate(C0011R$layout.qs_user_detail, viewGroup, z);
    }

    public void createAndSetAdapter(UserSwitcherController userSwitcherController, UiEventLogger uiEventLogger) {
        Adapter adapter = new Adapter(((ViewGroup) this).mContext, userSwitcherController, uiEventLogger);
        this.mAdapter = adapter;
        PseudoGridView.ViewGroupAdapterBridge.link(this, adapter);
    }

    public void refreshAdapter() {
        this.mAdapter.refresh();
    }

    public static class Adapter extends UserSwitcherController.BaseUserAdapter implements View.OnClickListener {
        private final Context mContext;
        protected UserSwitcherController mController;
        private View mCurrentUserView;
        private final UiEventLogger mUiEventLogger;

        public Adapter(Context context, UserSwitcherController userSwitcherController, UiEventLogger uiEventLogger) {
            super(userSwitcherController);
            this.mContext = context;
            this.mController = userSwitcherController;
            this.mUiEventLogger = uiEventLogger;
        }

        @Override // android.widget.Adapter
        public View getView(int i, View view, ViewGroup viewGroup) {
            return createUserDetailItemView(view, viewGroup, getItem(i));
        }

        public UserDetailItemView createUserDetailItemView(View view, ViewGroup viewGroup, UserSwitcherController.UserRecord userRecord) {
            UserDetailItemView convertOrInflate = UserDetailItemView.convertOrInflate(this.mContext, view, viewGroup);
            ColorFilter colorFilter = null;
            if (!userRecord.isCurrent || userRecord.isGuest) {
                convertOrInflate.setOnClickListener(this);
            } else {
                convertOrInflate.setOnClickListener(null);
            }
            String name = getName(this.mContext, userRecord);
            if (userRecord.picture == null) {
                convertOrInflate.bind(name, getDrawable(this.mContext, userRecord).mutate(), userRecord.resolveId());
            } else {
                CircleFramedDrawable circleFramedDrawable = new CircleFramedDrawable(userRecord.picture, (int) convertOrInflate.getResources().getDimension(C0005R$dimen.qs_framed_avatar_size));
                if (!userRecord.isSwitchToEnabled) {
                    colorFilter = UserSwitcherController.BaseUserAdapter.getDisabledUserAvatarColorFilter();
                }
                circleFramedDrawable.setColorFilter(colorFilter);
                convertOrInflate.bind(name, circleFramedDrawable, userRecord.info.id);
            }
            convertOrInflate.updateThemeColor(userRecord.isAddUser);
            convertOrInflate.setActivated(userRecord.isCurrent);
            convertOrInflate.setDisabledByAdmin(userRecord.isDisabledByAdmin);
            convertOrInflate.setEnabled(userRecord.isSwitchToEnabled);
            convertOrInflate.setAlpha(convertOrInflate.isEnabled() ? 1.0f : 0.38f);
            if (userRecord.isCurrent) {
                this.mCurrentUserView = convertOrInflate;
            }
            userRecord.isStorageInsufficient = false;
            if (!userRecord.isSwitchToEnabled) {
                convertOrInflate.setEnabled(false);
            } else if (userRecord.isAddUser || (userRecord.isGuest && userRecord.info == null)) {
                long availableInternalMemorySize = UserDetailView.getAvailableInternalMemorySize();
                Log.d("UserDetailView", "Available storage size=" + availableInternalMemorySize + " bytes");
                if (availableInternalMemorySize < -1149239296) {
                    Log.d("UserDetailView", "Storage size is too small, disable add user function");
                    convertOrInflate.setEnabled(false);
                    userRecord.isSwitchToEnabled = false;
                    userRecord.isStorageInsufficient = true;
                }
            }
            convertOrInflate.setTag(userRecord);
            return convertOrInflate;
        }

        private static Drawable getDrawable(Context context, UserSwitcherController.UserRecord userRecord) {
            Drawable drawable;
            int i;
            if (userRecord.isAddUser) {
                drawable = context.getDrawable(C0006R$drawable.ic_add_circle_qs);
            } else {
                drawable = UserIcons.getDefaultUserIcon(context.getResources(), userRecord.resolveId(), false);
            }
            if (userRecord.isCurrent) {
                i = C0004R$color.qs_user_switcher_selected_avatar_icon_color;
            } else if (!userRecord.isSwitchToEnabled) {
                i = C0004R$color.GM2_grey_600;
            } else {
                i = C0004R$color.qs_user_switcher_avatar_icon_color;
            }
            drawable.setTint(context.getResources().getColor(i, context.getTheme()));
            return drawable;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            UserSwitcherController.UserRecord userRecord = (UserSwitcherController.UserRecord) view.getTag();
            if (userRecord.isDisabledByAdmin) {
                this.mController.startActivity(RestrictedLockUtils.getShowAdminSupportDetailsIntent(this.mContext, userRecord.enforcedAdmin));
            } else if (userRecord.isSwitchToEnabled) {
                MetricsLogger.action(this.mContext, 156);
                this.mUiEventLogger.log(QSUserSwitcherEvent.QS_USER_SWITCH);
                if (!userRecord.isAddUser && !userRecord.isRestricted && !userRecord.isDisabledByAdmin) {
                    View view2 = this.mCurrentUserView;
                    if (view2 != null) {
                        view2.setActivated(false);
                    }
                    view.setActivated(true);
                }
                if (userRecord.isGuest) {
                    OpMdmLogger.log("quick_user", "guest", "1");
                } else if (!userRecord.isAddUser) {
                    OpMdmLogger.log("quick_user", "switch", "1");
                }
                switchTo(userRecord);
            } else if (userRecord.isStorageInsufficient) {
                Context context = this.mContext;
                SysUIToast.makeText(context, context.getString(C0015R$string.quick_settings_switch_user_storage_insufficient), 0).show();
            }
        }
    }

    public static long getAvailableInternalMemorySize() {
        StatFs statFs = new StatFs(Environment.getDataDirectory().getPath());
        return ((long) statFs.getAvailableBlocks()) * ((long) statFs.getBlockSize());
    }
}
