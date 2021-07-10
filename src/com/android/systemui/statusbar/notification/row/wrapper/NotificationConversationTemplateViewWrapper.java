package com.android.systemui.statusbar.notification.row.wrapper;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.widget.CachingIconView;
import com.android.internal.widget.ConversationLayout;
import com.android.internal.widget.MessagingLinearLayout;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.statusbar.TransformableView;
import com.android.systemui.statusbar.ViewTransformationHelper;
import com.android.systemui.statusbar.notification.NotificationUtils;
import com.android.systemui.statusbar.notification.TransformState;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.HybridNotificationView;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: NotificationConversationTemplateViewWrapper.kt */
public final class NotificationConversationTemplateViewWrapper extends NotificationTemplateViewWrapper {
    private View appName;
    private View conversationBadgeBg;
    private CachingIconView conversationIconView;
    private final ConversationLayout conversationLayout;
    private View conversationTitleView;
    private View expandButton;
    private View expandButtonContainer;
    private View expandButtonInnerContainer;
    private View facePileBottom;
    private View facePileBottomBg;
    private View facePileTop;
    private ViewGroup imageMessageContainer;
    private View importanceRing;
    private ViewGroup mActions;
    private MessagingLinearLayout messagingLinearLayout;
    private final int minHeightWithActions;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public NotificationConversationTemplateViewWrapper(@NotNull Context context, @NotNull View view, @NotNull ExpandableNotificationRow expandableNotificationRow) {
        super(context, view, expandableNotificationRow);
        Intrinsics.checkParameterIsNotNull(context, "ctx");
        Intrinsics.checkParameterIsNotNull(view, "view");
        Intrinsics.checkParameterIsNotNull(expandableNotificationRow, "row");
        this.minHeightWithActions = NotificationUtils.getFontScaledHeight(context, C0005R$dimen.notification_messaging_actions_min_height);
        this.conversationLayout = (ConversationLayout) view;
    }

    private final void resolveViews() {
        MessagingLinearLayout messagingLinearLayout = this.conversationLayout.getMessagingLinearLayout();
        Intrinsics.checkExpressionValueIsNotNull(messagingLinearLayout, "conversationLayout.messagingLinearLayout");
        this.messagingLinearLayout = messagingLinearLayout;
        ViewGroup imageMessageContainer = this.conversationLayout.getImageMessageContainer();
        Intrinsics.checkExpressionValueIsNotNull(imageMessageContainer, "conversationLayout.imageMessageContainer");
        this.imageMessageContainer = imageMessageContainer;
        ConversationLayout conversationLayout = this.conversationLayout;
        CachingIconView requireViewById = conversationLayout.requireViewById(16908888);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById, "requireViewById(com.andr…l.R.id.conversation_icon)");
        this.conversationIconView = requireViewById;
        View requireViewById2 = conversationLayout.requireViewById(16908890);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById2, "requireViewById(com.andr…nversation_icon_badge_bg)");
        this.conversationBadgeBg = requireViewById2;
        View requireViewById3 = conversationLayout.requireViewById(16908949);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById3, "requireViewById(com.andr…ernal.R.id.expand_button)");
        this.expandButton = requireViewById3;
        View requireViewById4 = conversationLayout.requireViewById(16908951);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById4, "requireViewById(com.andr….expand_button_container)");
        this.expandButtonContainer = requireViewById4;
        View requireViewById5 = conversationLayout.requireViewById(16908952);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById5, "requireViewById(com.andr…d_button_inner_container)");
        this.expandButtonInnerContainer = requireViewById5;
        View requireViewById6 = conversationLayout.requireViewById(16908891);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById6, "requireViewById(com.andr…ersation_icon_badge_ring)");
        this.importanceRing = requireViewById6;
        View requireViewById7 = conversationLayout.requireViewById(16908761);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById7, "requireViewById(com.andr…ernal.R.id.app_name_text)");
        this.appName = requireViewById7;
        View requireViewById8 = conversationLayout.requireViewById(16908894);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById8, "requireViewById(com.andr…l.R.id.conversation_text)");
        this.conversationTitleView = requireViewById8;
        this.facePileTop = conversationLayout.findViewById(16908886);
        this.facePileBottom = conversationLayout.findViewById(16908884);
        this.facePileBottomBg = conversationLayout.findViewById(16908885);
        View requireViewById9 = conversationLayout.requireViewById(16908721);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById9, "requireViewById(com.android.internal.R.id.actions)");
        ViewGroup viewGroup = (ViewGroup) requireViewById9;
        this.mActions = viewGroup;
        if (viewGroup == null) {
            Intrinsics.throwUninitializedPropertyAccessException("mActions");
            throw null;
        } else if (viewGroup == null) {
        } else {
            if (viewGroup != null) {
                View childAt = viewGroup.getChildAt(0);
                if (childAt != null) {
                    ViewGroup.LayoutParams layoutParams = childAt.getLayoutParams();
                    if (layoutParams != null) {
                        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) layoutParams;
                        ViewGroup viewGroup2 = this.mActions;
                        if (viewGroup2 == null) {
                            Intrinsics.throwUninitializedPropertyAccessException("mActions");
                            throw null;
                        } else if (viewGroup2 != null) {
                            int paddingTop = viewGroup2.getPaddingTop();
                            ViewGroup viewGroup3 = this.mActions;
                            if (viewGroup3 != null) {
                                int paddingEnd = viewGroup3.getPaddingEnd();
                                ViewGroup viewGroup4 = this.mActions;
                                if (viewGroup4 != null) {
                                    viewGroup2.setPaddingRelative(0, paddingTop, paddingEnd, viewGroup4.getPaddingBottom());
                                } else {
                                    Intrinsics.throwUninitializedPropertyAccessException("mActions");
                                    throw null;
                                }
                            } else {
                                Intrinsics.throwUninitializedPropertyAccessException("mActions");
                                throw null;
                            }
                        } else {
                            Intrinsics.throwUninitializedPropertyAccessException("mActions");
                            throw null;
                        }
                    } else {
                        throw new TypeCastException("null cannot be cast to non-null type android.view.ViewGroup.MarginLayoutParams");
                    }
                }
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("mActions");
                throw null;
            }
        }
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationTemplateViewWrapper, com.android.systemui.statusbar.notification.row.wrapper.NotificationHeaderViewWrapper, com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    public void onContentUpdated(@NotNull ExpandableNotificationRow expandableNotificationRow) {
        Intrinsics.checkParameterIsNotNull(expandableNotificationRow, "row");
        resolveViews();
        super.onContentUpdated(expandableNotificationRow);
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x000c: APUT  (r1v0 android.view.View[]), (0 ??[int, short, byte, char]), (r2v0 com.android.internal.widget.MessagingLinearLayout) */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0038: APUT  (r1v3 android.view.View[]), (0 ??[int, short, byte, char]), (r2v4 com.android.internal.widget.CachingIconView) */
    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationTemplateViewWrapper, com.android.systemui.statusbar.notification.row.wrapper.NotificationHeaderViewWrapper
    public void updateTransformedTypes() {
        super.updateTransformedTypes();
        View[] viewArr = new View[3];
        MessagingLinearLayout messagingLinearLayout = this.messagingLinearLayout;
        if (messagingLinearLayout != null) {
            viewArr[0] = messagingLinearLayout;
            View view = this.appName;
            if (view != null) {
                viewArr[1] = view;
                View view2 = this.conversationTitleView;
                if (view2 != null) {
                    viewArr[2] = view2;
                    addTransformedViews(viewArr);
                    ViewTransformationHelper viewTransformationHelper = this.mTransformationHelper;
                    NotificationConversationTemplateViewWrapper$updateTransformedTypes$1 notificationConversationTemplateViewWrapper$updateTransformedTypes$1 = new ViewTransformationHelper.CustomTransformation() { // from class: com.android.systemui.statusbar.notification.row.wrapper.NotificationConversationTemplateViewWrapper$updateTransformedTypes$1
                        @Override // com.android.systemui.statusbar.ViewTransformationHelper.CustomTransformation
                        public boolean transformTo(@NotNull TransformState transformState, @NotNull TransformableView transformableView, float f) {
                            Intrinsics.checkParameterIsNotNull(transformState, "ownState");
                            Intrinsics.checkParameterIsNotNull(transformableView, "otherView");
                            if (transformableView instanceof HybridNotificationView) {
                                return false;
                            }
                            transformState.ensureVisible();
                            return true;
                        }

                        @Override // com.android.systemui.statusbar.ViewTransformationHelper.CustomTransformation
                        public boolean transformFrom(@NotNull TransformState transformState, @NotNull TransformableView transformableView, float f) {
                            Intrinsics.checkParameterIsNotNull(transformState, "ownState");
                            Intrinsics.checkParameterIsNotNull(transformableView, "otherView");
                            return transformTo(transformState, transformableView, f);
                        }
                    };
                    ViewGroup viewGroup = this.imageMessageContainer;
                    if (viewGroup != null) {
                        viewTransformationHelper.setCustomTransformation(notificationConversationTemplateViewWrapper$updateTransformedTypes$1, viewGroup.getId());
                        View[] viewArr2 = new View[7];
                        CachingIconView cachingIconView = this.conversationIconView;
                        if (cachingIconView != null) {
                            viewArr2[0] = cachingIconView;
                            View view3 = this.conversationBadgeBg;
                            if (view3 != null) {
                                viewArr2[1] = view3;
                                View view4 = this.expandButton;
                                if (view4 != null) {
                                    viewArr2[2] = view4;
                                    View view5 = this.importanceRing;
                                    if (view5 != null) {
                                        viewArr2[3] = view5;
                                        viewArr2[4] = this.facePileTop;
                                        viewArr2[5] = this.facePileBottom;
                                        viewArr2[6] = this.facePileBottomBg;
                                        addViewsTransformingToSimilar(viewArr2);
                                        return;
                                    }
                                    Intrinsics.throwUninitializedPropertyAccessException("importanceRing");
                                    throw null;
                                }
                                Intrinsics.throwUninitializedPropertyAccessException("expandButton");
                                throw null;
                            }
                            Intrinsics.throwUninitializedPropertyAccessException("conversationBadgeBg");
                            throw null;
                        }
                        Intrinsics.throwUninitializedPropertyAccessException("conversationIconView");
                        throw null;
                    }
                    Intrinsics.throwUninitializedPropertyAccessException("imageMessageContainer");
                    throw null;
                }
                Intrinsics.throwUninitializedPropertyAccessException("conversationTitleView");
                throw null;
            }
            Intrinsics.throwUninitializedPropertyAccessException("appName");
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("messagingLinearLayout");
        throw null;
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationHeaderViewWrapper, com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    @NotNull
    public View getExpandButton() {
        View view = this.expandButtonInnerContainer;
        if (view != null) {
            return view;
        }
        Intrinsics.throwUninitializedPropertyAccessException("expandButtonInnerContainer");
        throw null;
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationHeaderViewWrapper, com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    public void setShelfIconVisible(boolean z) {
        if (this.conversationLayout.isImportantConversation()) {
            CachingIconView cachingIconView = this.conversationIconView;
            if (cachingIconView == null) {
                Intrinsics.throwUninitializedPropertyAccessException("conversationIconView");
                throw null;
            } else if (cachingIconView.getVisibility() != 8) {
                CachingIconView cachingIconView2 = this.conversationIconView;
                if (cachingIconView2 != null) {
                    cachingIconView2.setForceHidden(z);
                    return;
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("conversationIconView");
                    throw null;
                }
            }
        }
        super.setShelfIconVisible(z);
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationHeaderViewWrapper, com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    @Nullable
    public View getShelfTransformationTarget() {
        if (!this.conversationLayout.isImportantConversation()) {
            return super.getShelfTransformationTarget();
        }
        CachingIconView cachingIconView = this.conversationIconView;
        if (cachingIconView == null) {
            Intrinsics.throwUninitializedPropertyAccessException("conversationIconView");
            throw null;
        } else if (cachingIconView.getVisibility() == 8) {
            return super.getShelfTransformationTarget();
        } else {
            CachingIconView cachingIconView2 = this.conversationIconView;
            if (cachingIconView2 != null) {
                return cachingIconView2;
            }
            Intrinsics.throwUninitializedPropertyAccessException("conversationIconView");
            throw null;
        }
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    public void setRemoteInputVisible(boolean z) {
        this.conversationLayout.showHistoricMessages(z);
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationHeaderViewWrapper, com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    public void updateExpandability(boolean z, @Nullable View.OnClickListener onClickListener) {
        this.conversationLayout.updateExpandability(z, onClickListener);
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationTemplateViewWrapper, com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    public boolean disallowSingleClick(float f, float f2) {
        boolean z;
        View view = this.expandButtonContainer;
        if (view != null) {
            if (view.getVisibility() == 0) {
                View view2 = this.expandButtonContainer;
                if (view2 == null) {
                    Intrinsics.throwUninitializedPropertyAccessException("expandButtonContainer");
                    throw null;
                } else if (isOnView(view2, f, f2)) {
                    z = true;
                    return z || super.disallowSingleClick(f, f2);
                }
            }
            z = false;
            if (z) {
                return true;
            }
        }
        Intrinsics.throwUninitializedPropertyAccessException("expandButtonContainer");
        throw null;
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    public int getMinLayoutHeight() {
        View view = this.mActionsContainer;
        if (view != null) {
            Intrinsics.checkExpressionValueIsNotNull(view, "mActionsContainer");
            if (view.getVisibility() != 8) {
                return this.minHeightWithActions;
            }
        }
        return super.getMinLayoutHeight();
    }

    private final void addTransformedViews(View... viewArr) {
        for (View view : viewArr) {
            if (view != null) {
                this.mTransformationHelper.addTransformedView(view);
            }
        }
    }

    private final void addViewsTransformingToSimilar(View... viewArr) {
        for (View view : viewArr) {
            if (view != null) {
                this.mTransformationHelper.addViewTransformingToSimilar(view);
            }
        }
    }
}
