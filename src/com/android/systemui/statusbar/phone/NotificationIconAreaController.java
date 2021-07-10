package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.collection.ArrayMap;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.internal.util.ContrastColorUtil;
import com.android.settingslib.Utils;
import com.android.systemui.C0002R$attr;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0011R$layout;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.NotificationListener;
import com.android.systemui.statusbar.NotificationMediaManager;
import com.android.systemui.statusbar.NotificationShelf;
import com.android.systemui.statusbar.StatusBarIconView;
import com.android.systemui.statusbar.notification.NotificationWakeUpCoordinator;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.oneplus.aod.OpAodWindowManager;
import com.oneplus.systemui.statusbar.phone.OpNotificationIconAreaController;
import com.oneplus.util.OpUtils;
import java.util.ArrayList;
import java.util.function.Function;
public class NotificationIconAreaController extends OpNotificationIconAreaController implements DarkIconDispatcher.DarkReceiver, StatusBarStateController.StateListener, NotificationWakeUpCoordinator.WakeUpListener {
    private boolean mAnimationsEnabled;
    private int mAodIconTint;
    private NotificationIconContainer mAodIcons;
    private final BubbleController mBubbleController;
    private final KeyguardBypassController mBypassController;
    private NotificationIconContainer mCenteredIcon;
    protected View mCenteredIconArea;
    private int mCenteredIconTint = -1;
    private StatusBarIconView mCenteredIconView;
    private Context mContext;
    private final ContrastColorUtil mContrastColorUtil;
    private final DozeParameters mDozeParameters;
    private int mIconHPadding;
    private int mIconSize;
    private int mIconTint = -1;
    private final NotificationMediaManager mMediaManager;
    protected View mNotificationIconArea;
    private NotificationIconContainer mNotificationIcons;
    private ViewGroup mNotificationScrollLayout;
    final NotificationListener.NotificationSettingsListener mSettingsListener = new NotificationListener.NotificationSettingsListener() { // from class: com.android.systemui.statusbar.phone.NotificationIconAreaController.1
        @Override // com.android.systemui.statusbar.NotificationListener.NotificationSettingsListener
        public void onStatusBarIconsBehaviorChanged(boolean z) {
            NotificationIconAreaController.this.mShowLowPriority = !z;
            if (NotificationIconAreaController.this.mNotificationScrollLayout != null) {
                NotificationIconAreaController.this.updateStatusBarIcons();
            }
        }
    };
    private NotificationIconContainer mShelfIcons;
    private boolean mShowLowPriority = true;
    private StatusBar mStatusBar;
    private final StatusBarStateController mStatusBarStateController;
    private final Rect mTintArea = new Rect();
    private final Runnable mUpdateStatusBarIcons = new Runnable() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$NWCrb8vzuopzf5kAygkNeXndtBo
        @Override // java.lang.Runnable
        public final void run() {
            NotificationIconAreaController.this.updateStatusBarIcons();
        }
    };
    private final NotificationWakeUpCoordinator mWakeUpCoordinator;

    public NotificationIconAreaController(Context context, StatusBar statusBar, StatusBarStateController statusBarStateController, NotificationWakeUpCoordinator notificationWakeUpCoordinator, KeyguardBypassController keyguardBypassController, NotificationMediaManager notificationMediaManager, NotificationListener notificationListener, DozeParameters dozeParameters, BubbleController bubbleController) {
        this.mStatusBar = statusBar;
        this.mContrastColorUtil = ContrastColorUtil.getInstance(context);
        this.mContext = context;
        this.mStatusBarStateController = statusBarStateController;
        statusBarStateController.addCallback(this);
        this.mMediaManager = notificationMediaManager;
        this.mDozeParameters = dozeParameters;
        this.mWakeUpCoordinator = notificationWakeUpCoordinator;
        notificationWakeUpCoordinator.addListener(this);
        this.mBypassController = keyguardBypassController;
        this.mBubbleController = bubbleController;
        notificationListener.addNotificationSettingsListener(this.mSettingsListener);
        initializeNotificationAreaViews(context);
        reloadAodColor();
    }

    /* access modifiers changed from: protected */
    public View inflateIconArea(LayoutInflater layoutInflater) {
        return layoutInflater.inflate(C0011R$layout.notification_icon_area, (ViewGroup) null);
    }

    /* access modifiers changed from: protected */
    public void initializeNotificationAreaViews(Context context) {
        reloadDimens(context);
        LayoutInflater from = LayoutInflater.from(context);
        View inflateIconArea = inflateIconArea(from);
        this.mNotificationIconArea = inflateIconArea;
        this.mNotificationIcons = (NotificationIconContainer) inflateIconArea.findViewById(C0008R$id.notificationIcons);
        this.mNotificationScrollLayout = this.mStatusBar.getNotificationScrollLayout();
        View inflate = from.inflate(C0011R$layout.center_icon_area, (ViewGroup) null);
        this.mCenteredIconArea = inflate;
        this.mCenteredIcon = (NotificationIconContainer) inflate.findViewById(C0008R$id.centeredIcon);
        initAodIcons();
    }

    public void initAodIcons() {
        boolean z = this.mAodIcons != null;
        if (z) {
            this.mAodIcons.setAnimationsEnabled(false);
            this.mAodIcons.removeAllViews();
        }
        NotificationIconContainer notificationIconContainer = (NotificationIconContainer) this.mStatusBar.getNotificationShadeWindowView().findViewById(C0008R$id.clock_notification_icon_container);
        this.mAodIcons = notificationIconContainer;
        notificationIconContainer.setOnLockScreen(true);
        updateAodIconsVisibility(false);
        updateAnimations();
        if (z) {
            updateAodNotificationIcons();
        }
    }

    public void setupShelf(NotificationShelf notificationShelf) {
        this.mShelfIcons = notificationShelf.getShelfIcons();
        notificationShelf.setCollapsedIcons(this.mNotificationIcons);
    }

    public void onDensityOrFontScaleChanged(Context context) {
        reloadDimens(context);
        FrameLayout.LayoutParams generateIconLayoutParams = generateIconLayoutParams();
        for (int i = 0; i < this.mNotificationIcons.getChildCount(); i++) {
            this.mNotificationIcons.getChildAt(i).setLayoutParams(generateIconLayoutParams);
        }
        for (int i2 = 0; i2 < this.mShelfIcons.getChildCount(); i2++) {
            this.mShelfIcons.getChildAt(i2).setLayoutParams(generateIconLayoutParams);
        }
        for (int i3 = 0; i3 < this.mCenteredIcon.getChildCount(); i3++) {
            this.mCenteredIcon.getChildAt(i3).setLayoutParams(generateIconLayoutParams);
        }
        for (int i4 = 0; i4 < this.mAodIcons.getChildCount(); i4++) {
            this.mAodIcons.getChildAt(i4).setLayoutParams(generateIconLayoutParams);
        }
    }

    private FrameLayout.LayoutParams generateIconLayoutParams() {
        return new FrameLayout.LayoutParams(this.mIconSize + (this.mIconHPadding * 2), getHeight());
    }

    private void reloadDimens(Context context) {
        Resources resources = context.getResources();
        this.mIconSize = resources.getDimensionPixelSize(17105484);
        this.mIconHPadding = resources.getDimensionPixelSize(C0005R$dimen.status_bar_icon_padding);
        resources.getDimensionPixelSize(C0005R$dimen.shelf_appear_translation);
    }

    public View getNotificationInnerAreaView() {
        return this.mNotificationIconArea;
    }

    public View getCenteredNotificationAreaView() {
        return this.mCenteredIconArea;
    }

    @Override // com.android.systemui.plugins.DarkIconDispatcher.DarkReceiver
    public void onDarkChanged(Rect rect, float f, int i) {
        if (rect == null) {
            this.mTintArea.setEmpty();
        } else {
            this.mTintArea.set(rect);
        }
        View view = this.mNotificationIconArea;
        if (view == null) {
            this.mIconTint = i;
        } else if (DarkIconDispatcher.isInArea(rect, view)) {
            this.mIconTint = i;
        }
        View view2 = this.mCenteredIconArea;
        if (view2 == null) {
            this.mCenteredIconTint = i;
        } else if (DarkIconDispatcher.isInArea(rect, view2)) {
            this.mCenteredIconTint = i;
        }
        applyNotificationIconsTint();
    }

    /* access modifiers changed from: protected */
    public int getHeight() {
        return this.mStatusBar.getStatusBarHeight();
    }

    public void updateNotificationIcons() {
        updateStatusBarIcons();
        updateShelfIcons();
        updateCenterIcon();
        updateAodNotificationIcons();
        applyNotificationIconsTint();
    }

    private void updateShelfIcons() {
        updateIconsForLayout($$Lambda$NotificationIconAreaController$afpYK1wAP1i0HTFHOa1jb1wzzAQ.INSTANCE, this.mShelfIcons, true, true, false, false, false, false, false, false);
    }

    public void updateStatusBarIcons() {
        updateIconsForLayout($$Lambda$NotificationIconAreaController$ujxUrqwlryo8PHBzga56kRshsA.INSTANCE, this.mNotificationIcons, false, this.mShowLowPriority, true, true, false, true, false, false);
    }

    private void updateCenterIcon() {
        updateIconsForLayout($$Lambda$NotificationIconAreaController$S6CJ2tXrA2ieNVmUpwBa8v9eeEY.INSTANCE, this.mCenteredIcon, false, true, false, false, false, false, false, true);
    }

    public void updateAodNotificationIcons() {
        updateIconsForLayout($$Lambda$NotificationIconAreaController$b7MkWJaTAeTosmR_aU3q7JZNLpI.INSTANCE, this.mAodIcons, false, true, true, true, true, true, this.mBypassController.getBypassEnabled(), false);
    }

    /* access modifiers changed from: package-private */
    public boolean shouldShouldLowPriorityIcons() {
        return this.mShowLowPriority;
    }

    private void updateIconsForLayout(Function<NotificationEntry, StatusBarIconView> function, final NotificationIconContainer notificationIconContainer, boolean z, boolean z2, boolean z3, boolean z4, boolean z5, boolean z6, boolean z7, boolean z8) {
        boolean z9;
        OpAodWindowManager aodWindowManager;
        int i;
        ArrayList arrayList;
        NotificationIconAreaController notificationIconAreaController = this;
        ArrayList arrayList2 = new ArrayList(notificationIconAreaController.mNotificationScrollLayout.getChildCount());
        boolean equals = notificationIconContainer.equals(notificationIconAreaController.mNotificationIcons);
        int i2 = 0;
        while (i2 < notificationIconAreaController.mNotificationScrollLayout.getChildCount()) {
            View childAt = notificationIconAreaController.mNotificationScrollLayout.getChildAt(i2);
            if (childAt instanceof ExpandableNotificationRow) {
                NotificationEntry entry = ((ExpandableNotificationRow) childAt).getEntry();
                i = i2;
                if (super.shouldShowNotificationIcon(entry, z, z2, z3, z4, z5, z6, z7, z8, equals, notificationIconAreaController.mMediaManager, notificationIconAreaController.mCenteredIconView, notificationIconAreaController.mBubbleController, notificationIconAreaController.mWakeUpCoordinator)) {
                    StatusBarIconView apply = function.apply(entry);
                    arrayList = arrayList2;
                    if (apply != null) {
                        arrayList.add(apply);
                    }
                } else {
                    arrayList = arrayList2;
                }
            } else {
                i = i2;
                arrayList = arrayList2;
            }
            i2 = i + 1;
            notificationIconAreaController = this;
            arrayList2 = arrayList;
        }
        ArrayMap<String, ArrayList<StatusBarIcon>> arrayMap = new ArrayMap<>();
        ArrayList arrayList3 = new ArrayList();
        for (int i3 = 0; i3 < notificationIconContainer.getChildCount(); i3++) {
            View childAt2 = notificationIconContainer.getChildAt(i3);
            if ((childAt2 instanceof StatusBarIconView) && !arrayList2.contains(childAt2)) {
                StatusBarIconView statusBarIconView = (StatusBarIconView) childAt2;
                String groupKey = statusBarIconView.getNotification().getGroupKey();
                int i4 = 0;
                boolean z10 = false;
                while (true) {
                    if (i4 >= arrayList2.size()) {
                        break;
                    }
                    StatusBarIconView statusBarIconView2 = (StatusBarIconView) arrayList2.get(i4);
                    if (statusBarIconView2.getSourceIcon().sameAs(statusBarIconView.getSourceIcon()) && statusBarIconView2.getNotification().getGroupKey().equals(groupKey)) {
                        if (z10) {
                            z10 = false;
                            break;
                        }
                        z10 = true;
                    }
                    i4++;
                }
                if (z10) {
                    ArrayList<StatusBarIcon> arrayList4 = arrayMap.get(groupKey);
                    if (arrayList4 == null) {
                        arrayList4 = new ArrayList<>();
                        arrayMap.put(groupKey, arrayList4);
                    }
                    arrayList4.add(statusBarIconView.getStatusBarIcon());
                }
                arrayList3.add(statusBarIconView);
            }
        }
        ArrayList arrayList5 = new ArrayList();
        for (String str : arrayMap.keySet()) {
            if (arrayMap.get(str).size() != 1) {
                arrayList5.add(str);
            }
        }
        arrayMap.removeAll(arrayList5);
        if (!equals || arrayMap.size() <= 0) {
            z9 = false;
        } else {
            if (Build.DEBUG_ONEPLUS) {
                Log.d("OpAodNotiIconArea", "has replacing icons");
            }
            z9 = true;
        }
        notificationIconContainer.setReplacingIcons(arrayMap);
        int size = arrayList3.size();
        for (int i5 = 0; i5 < size; i5++) {
            notificationIconContainer.removeView((View) arrayList3.get(i5));
        }
        if (equals && size > 0) {
            if (Build.DEBUG_ONEPLUS) {
                Log.d("OpAodNotiIconArea", "has icon to remove");
            }
            z9 = true;
        }
        ViewGroup.LayoutParams generateIconLayoutParams = generateIconLayoutParams();
        boolean z11 = z9;
        for (int i6 = 0; i6 < arrayList2.size(); i6++) {
            StatusBarIconView statusBarIconView3 = (StatusBarIconView) arrayList2.get(i6);
            notificationIconContainer.removeTransientView(statusBarIconView3);
            if (statusBarIconView3.getParent() == null) {
                if (z3) {
                    statusBarIconView3.setOnDismissListener(this.mUpdateStatusBarIcons);
                }
                if (equals) {
                    if (Build.DEBUG_ONEPLUS) {
                        Log.d("OpAodNotiIconArea", "add view");
                    }
                    z11 = true;
                }
                notificationIconContainer.addView(statusBarIconView3, i6, generateIconLayoutParams);
            }
        }
        notificationIconContainer.setChangingViewPositions(true);
        int childCount = notificationIconContainer.getChildCount();
        for (int i7 = 0; i7 < childCount; i7++) {
            View childAt3 = notificationIconContainer.getChildAt(i7);
            View view = (StatusBarIconView) arrayList2.get(i7);
            if (childAt3 != view) {
                notificationIconContainer.removeView(view);
                notificationIconContainer.addView(view, i7);
                if (equals) {
                    if (Build.DEBUG_ONEPLUS) {
                        Log.d("OpAodNotiIconArea", "re-sort icons");
                    }
                    z11 = true;
                }
            }
        }
        if (equals && ((this.mStatusBar.isDozing() || this.mStatusBar.isDozingCustom() || z11) && (aodWindowManager = this.mStatusBar.getAodWindowManager()) != null)) {
            aodWindowManager.getUIHandler().post(new Runnable() { // from class: com.android.systemui.statusbar.phone.NotificationIconAreaController.2
                @Override // java.lang.Runnable
                public void run() {
                    ((OpNotificationIconAreaController) NotificationIconAreaController.this).mAodNotificationIconCtrl.updateNotificationIcons(notificationIconContainer);
                }
            });
        }
        applyNotificationIconsTint();
        notificationIconContainer.setChangingViewPositions(false);
        notificationIconContainer.setReplacingIcons(null);
    }

    private void applyNotificationIconsTint() {
        for (int i = 0; i < this.mNotificationIcons.getChildCount(); i++) {
            StatusBarIconView statusBarIconView = (StatusBarIconView) this.mNotificationIcons.getChildAt(i);
            if (statusBarIconView.getWidth() != 0) {
                updateTintForIcon(statusBarIconView, this.mIconTint);
            } else {
                statusBarIconView.executeOnLayout(new Runnable(statusBarIconView) { // from class: com.android.systemui.statusbar.phone.-$$Lambda$NotificationIconAreaController$kEHcYKNlJqRNuom7zI__dD3YiUQ
                    public final /* synthetic */ StatusBarIconView f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        NotificationIconAreaController.this.lambda$applyNotificationIconsTint$4$NotificationIconAreaController(this.f$1);
                    }
                });
            }
        }
        for (int i2 = 0; i2 < this.mCenteredIcon.getChildCount(); i2++) {
            StatusBarIconView statusBarIconView2 = (StatusBarIconView) this.mCenteredIcon.getChildAt(i2);
            if (statusBarIconView2.getWidth() != 0) {
                updateTintForIcon(statusBarIconView2, this.mCenteredIconTint);
            } else {
                statusBarIconView2.executeOnLayout(new Runnable(statusBarIconView2) { // from class: com.android.systemui.statusbar.phone.-$$Lambda$NotificationIconAreaController$DNX7QrLi_n7I734CPybT_ZrNpwI
                    public final /* synthetic */ StatusBarIconView f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        NotificationIconAreaController.this.lambda$applyNotificationIconsTint$5$NotificationIconAreaController(this.f$1);
                    }
                });
            }
        }
        updateAodIconColors();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$applyNotificationIconsTint$4 */
    public /* synthetic */ void lambda$applyNotificationIconsTint$4$NotificationIconAreaController(StatusBarIconView statusBarIconView) {
        updateTintForIcon(statusBarIconView, this.mIconTint);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$applyNotificationIconsTint$5 */
    public /* synthetic */ void lambda$applyNotificationIconsTint$5$NotificationIconAreaController(StatusBarIconView statusBarIconView) {
        updateTintForIcon(statusBarIconView, this.mCenteredIconTint);
    }

    private void updateTintForIcon(StatusBarIconView statusBarIconView, int i) {
        updateTintForIconInternal(statusBarIconView, i, this.mContext, this.mContrastColorUtil, this.mTintArea);
    }

    public void showIconIsolated(StatusBarIconView statusBarIconView, boolean z) {
        this.mNotificationIcons.showIconIsolated(statusBarIconView, z);
    }

    public void setIsolatedIconLocation(Rect rect, boolean z) {
        this.mNotificationIcons.setIsolatedIconLocation(rect, z);
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onDozingChanged(boolean z) {
        this.mAodIcons.setDozing(z, this.mDozeParameters.getAlwaysOn() && !this.mDozeParameters.getDisplayNeedsBlanking(), 0);
    }

    public void setAnimationsEnabled(boolean z) {
        this.mAnimationsEnabled = z;
        updateAnimations();
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onStateChanged(int i) {
        updateAodIconsVisibility(false);
        updateAnimations();
    }

    private void updateAnimations() {
        boolean z = true;
        boolean z2 = this.mStatusBarStateController.getState() == 0;
        this.mAodIcons.setAnimationsEnabled(this.mAnimationsEnabled && !z2);
        this.mCenteredIcon.setAnimationsEnabled(this.mAnimationsEnabled && z2);
        NotificationIconContainer notificationIconContainer = this.mNotificationIcons;
        if (!this.mAnimationsEnabled || !z2) {
            z = false;
        }
        notificationIconContainer.setAnimationsEnabled(z);
    }

    public void onThemeChanged() {
        reloadAodColor();
        updateAodIconColors();
    }

    private void reloadAodColor() {
        this.mAodIconTint = Utils.getColorAttrDefaultColor(this.mContext, C0002R$attr.wallpaperTextColor);
    }

    private void updateAodIconColors() {
        for (int i = 0; i < this.mAodIcons.getChildCount(); i++) {
            StatusBarIconView statusBarIconView = (StatusBarIconView) this.mAodIcons.getChildAt(i);
            if (statusBarIconView.getWidth() != 0) {
                updateTintForIcon(statusBarIconView, this.mAodIconTint);
            } else {
                statusBarIconView.executeOnLayout(new Runnable(statusBarIconView) { // from class: com.android.systemui.statusbar.phone.-$$Lambda$NotificationIconAreaController$PUTDTipRCmrDLS4VQZByqHC4HFA
                    public final /* synthetic */ StatusBarIconView f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        NotificationIconAreaController.this.lambda$updateAodIconColors$6$NotificationIconAreaController(this.f$1);
                    }
                });
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$updateAodIconColors$6 */
    public /* synthetic */ void lambda$updateAodIconColors$6$NotificationIconAreaController(StatusBarIconView statusBarIconView) {
        updateTintForIcon(statusBarIconView, this.mAodIconTint);
    }

    @Override // com.android.systemui.statusbar.notification.NotificationWakeUpCoordinator.WakeUpListener
    public void onFullyHiddenChanged(boolean z) {
        boolean z2 = true;
        if (!this.mBypassController.getBypassEnabled()) {
            if (!this.mDozeParameters.getAlwaysOn() || this.mDozeParameters.getDisplayNeedsBlanking()) {
                z2 = false;
            }
            z2 &= z;
        }
        updateAodIconsVisibility(z2);
        updateAodNotificationIcons();
    }

    @Override // com.android.systemui.statusbar.notification.NotificationWakeUpCoordinator.WakeUpListener
    public void onPulseExpansionChanged(boolean z) {
        if (z) {
            updateAodIconsVisibility(true);
        }
    }

    private void updateAodIconsVisibility(boolean z) {
        if (OpUtils.DEBUG_ONEPLUS) {
            Log.d(NotificationIconAreaController.class.getSimpleName(), "for oneplus style, no updateAodIconsVisibility");
        }
    }
}
