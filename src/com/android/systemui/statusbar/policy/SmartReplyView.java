package com.android.systemui.statusbar.policy;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ContrastColorUtil;
import com.android.systemui.C0004R$color;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0011R$layout;
import com.android.systemui.C0015R$string;
import com.android.systemui.Dependency;
import com.android.systemui.R$styleable;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.statusbar.NotificationRemoteInputManager;
import com.android.systemui.statusbar.SmartReplyController;
import com.android.systemui.statusbar.notification.NotificationUtils;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.logging.NotificationLogger;
import com.android.systemui.statusbar.phone.KeyguardDismissUtil;
import com.android.systemui.statusbar.policy.SmartReplyView;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
public class SmartReplyView extends ViewGroup {
    private static final Comparator<View> DECREASING_MEASURED_WIDTH_WITHOUT_PADDING_COMPARATOR = $$Lambda$SmartReplyView$UA3QkbRzztEFRlbb86djKcGIV5E.INSTANCE;
    private static final int MEASURE_SPEC_ANY_LENGTH = View.MeasureSpec.makeMeasureSpec(0, 0);
    private ActivityStarter mActivityStarter;
    private final BreakIterator mBreakIterator;
    private PriorityQueue<Button> mCandidateButtonQueueForSqueezing;
    private final SmartReplyConstants mConstants = ((SmartReplyConstants) Dependency.get(SmartReplyConstants.class));
    private int mCurrentBackgroundColor;
    private final int mDefaultBackgroundColor;
    private final int mDefaultStrokeColor;
    private final int mDefaultTextColor;
    private final int mDefaultTextColorDarkBg;
    private final int mDoubleLineButtonPaddingHorizontal;
    private final int mHeightUpperLimit = NotificationUtils.getFontScaledHeight(((ViewGroup) this).mContext, C0005R$dimen.smart_reply_button_max_height);
    private final KeyguardDismissUtil mKeyguardDismissUtil = ((KeyguardDismissUtil) Dependency.get(KeyguardDismissUtil.class));
    private final double mMinStrokeContrast;
    private final NotificationRemoteInputManager mRemoteInputManager = ((NotificationRemoteInputManager) Dependency.get(NotificationRemoteInputManager.class));
    private final int mRippleColor;
    private final int mRippleColorDarkBg;
    private final int mSingleLineButtonPaddingHorizontal;
    private final int mSingleToDoubleLineButtonWidthIncrease;
    private boolean mSmartRepliesGeneratedByAssistant = false;
    private View mSmartReplyContainer;
    private final int mSpacing;
    private final int mStrokeWidth;

    /* access modifiers changed from: private */
    public enum SmartButtonType {
        REPLY,
        ACTION
    }

    static /* synthetic */ int lambda$static$0(View view, View view2) {
        return ((view2.getMeasuredWidth() - view2.getPaddingLeft()) - view2.getPaddingRight()) - ((view.getMeasuredWidth() - view.getPaddingLeft()) - view.getPaddingRight());
    }

    public SmartReplyView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        int color = context.getColor(C0004R$color.smart_reply_button_background);
        this.mCurrentBackgroundColor = color;
        this.mDefaultBackgroundColor = color;
        this.mDefaultTextColor = ((ViewGroup) this).mContext.getColor(C0004R$color.smart_reply_button_text);
        this.mDefaultTextColorDarkBg = ((ViewGroup) this).mContext.getColor(C0004R$color.smart_reply_button_text_dark_bg);
        this.mDefaultStrokeColor = ((ViewGroup) this).mContext.getColor(C0004R$color.smart_reply_button_stroke);
        int color2 = ((ViewGroup) this).mContext.getColor(C0004R$color.notification_ripple_untinted_color);
        this.mRippleColor = color2;
        this.mRippleColorDarkBg = Color.argb(Color.alpha(color2), 255, 255, 255);
        this.mMinStrokeContrast = ContrastColorUtil.calculateContrast(this.mDefaultStrokeColor, this.mDefaultBackgroundColor);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.SmartReplyView, 0, 0);
        int indexCount = obtainStyledAttributes.getIndexCount();
        int i = 0;
        int i2 = 0;
        int i3 = 0;
        int i4 = 0;
        for (int i5 = 0; i5 < indexCount; i5++) {
            int index = obtainStyledAttributes.getIndex(i5);
            if (index == R$styleable.SmartReplyView_spacing) {
                i2 = obtainStyledAttributes.getDimensionPixelSize(i5, 0);
            } else if (index == R$styleable.SmartReplyView_singleLineButtonPaddingHorizontal) {
                i3 = obtainStyledAttributes.getDimensionPixelSize(i5, 0);
            } else if (index == R$styleable.SmartReplyView_doubleLineButtonPaddingHorizontal) {
                i4 = obtainStyledAttributes.getDimensionPixelSize(i5, 0);
            } else if (index == R$styleable.SmartReplyView_buttonStrokeWidth) {
                i = obtainStyledAttributes.getDimensionPixelSize(i5, 0);
            }
        }
        obtainStyledAttributes.recycle();
        this.mStrokeWidth = i;
        this.mSpacing = i2;
        this.mSingleLineButtonPaddingHorizontal = i3;
        this.mDoubleLineButtonPaddingHorizontal = i4;
        this.mSingleToDoubleLineButtonWidthIncrease = (i4 - i3) * 2;
        this.mBreakIterator = BreakIterator.getLineInstance();
        reallocateCandidateButtonQueueForSqueezing();
    }

    public int getHeightUpperLimit() {
        return this.mHeightUpperLimit;
    }

    private void reallocateCandidateButtonQueueForSqueezing() {
        this.mCandidateButtonQueueForSqueezing = new PriorityQueue<>(Math.max(getChildCount(), 1), DECREASING_MEASURED_WIDTH_WITHOUT_PADDING_COMPARATOR);
    }

    public void resetSmartSuggestions(View view) {
        this.mSmartReplyContainer = view;
        removeAllViews();
        this.mCurrentBackgroundColor = this.mDefaultBackgroundColor;
    }

    public void addPreInflatedButtons(List<Button> list) {
        for (Button button : list) {
            addView(button);
        }
        reallocateCandidateButtonQueueForSqueezing();
    }

    public List<Button> inflateRepliesFromRemoteInput(SmartReplies smartReplies, SmartReplyController smartReplyController, NotificationEntry notificationEntry, boolean z) {
        ArrayList arrayList = new ArrayList();
        if (!(smartReplies.remoteInput == null || smartReplies.pendingIntent == null || smartReplies.choices == null)) {
            for (int i = 0; i < smartReplies.choices.size(); i++) {
                arrayList.add(inflateReplyButton(this, getContext(), i, smartReplies, smartReplyController, notificationEntry, z));
            }
            this.mSmartRepliesGeneratedByAssistant = smartReplies.fromAssistant;
        }
        return arrayList;
    }

    public List<Button> inflateSmartActions(Context context, SmartActions smartActions, SmartReplyController smartReplyController, NotificationEntry notificationEntry, HeadsUpManager headsUpManager, boolean z) {
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(context, ((ViewGroup) this).mContext.getTheme());
        ArrayList arrayList = new ArrayList();
        int size = smartActions.actions.size();
        for (int i = 0; i < size; i++) {
            if (smartActions.actions.get(i).actionIntent != null) {
                arrayList.add(inflateActionButton(this, getContext(), contextThemeWrapper, i, smartActions, smartReplyController, notificationEntry, headsUpManager, z));
            }
        }
        return arrayList;
    }

    public static SmartReplyView inflate(Context context) {
        return (SmartReplyView) LayoutInflater.from(context).inflate(C0011R$layout.smart_reply_view, (ViewGroup) null);
    }

    @VisibleForTesting
    static Button inflateReplyButton(SmartReplyView smartReplyView, Context context, int i, SmartReplies smartReplies, SmartReplyController smartReplyController, NotificationEntry notificationEntry, boolean z) {
        Button button = (Button) LayoutInflater.from(context).inflate(C0011R$layout.smart_reply_button, (ViewGroup) smartReplyView, false);
        CharSequence charSequence = smartReplies.choices.get(i);
        button.setText(charSequence);
        View.OnClickListener r0 = new View.OnClickListener(new ActivityStarter.OnDismissAction(smartReplies, charSequence, i, button, smartReplyController, notificationEntry, context) { // from class: com.android.systemui.statusbar.policy.-$$Lambda$SmartReplyView$rVuoX0krA-dMy7xAwdbzCHW8AzI
            public final /* synthetic */ SmartReplyView.SmartReplies f$1;
            public final /* synthetic */ CharSequence f$2;
            public final /* synthetic */ int f$3;
            public final /* synthetic */ Button f$4;
            public final /* synthetic */ SmartReplyController f$5;
            public final /* synthetic */ NotificationEntry f$6;
            public final /* synthetic */ Context f$7;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
                this.f$6 = r7;
                this.f$7 = r8;
            }

            @Override // com.android.systemui.plugins.ActivityStarter.OnDismissAction
            public final boolean onDismiss() {
                return SmartReplyView.lambda$inflateReplyButton$1(SmartReplyView.this, this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7);
            }
        }, notificationEntry) { // from class: com.android.systemui.statusbar.policy.-$$Lambda$SmartReplyView$zCSq2JAz-cY64WTEY4XQsF-yGXs
            public final /* synthetic */ ActivityStarter.OnDismissAction f$1;
            public final /* synthetic */ NotificationEntry f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                SmartReplyView smartReplyView2 = SmartReplyView.this;
                ActivityStarter.OnDismissAction onDismissAction = this.f$1;
                NotificationEntry notificationEntry2 = this.f$2;
                smartReplyView2.mKeyguardDismissUtil.executeWhenUnlocked(onDismissAction, !notificationEntry2.isRowPinned());
            }
        };
        if (z) {
            r0 = new DelayedOnClickListener(r0, smartReplyView.mConstants.getOnClickInitDelay());
        }
        button.setOnClickListener(r0);
        button.setAccessibilityDelegate(new View.AccessibilityDelegate() { // from class: com.android.systemui.statusbar.policy.SmartReplyView.1
            @Override // android.view.View.AccessibilityDelegate
            public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfo accessibilityNodeInfo) {
                super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfo);
                accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(16, SmartReplyView.this.getResources().getString(C0015R$string.accessibility_send_smart_reply)));
            }
        });
        setButtonColors(button, smartReplyView.mCurrentBackgroundColor, smartReplyView.mDefaultStrokeColor, smartReplyView.mDefaultTextColor, smartReplyView.mRippleColor, smartReplyView.mStrokeWidth);
        return button;
    }

    static /* synthetic */ boolean lambda$inflateReplyButton$1(SmartReplyView smartReplyView, SmartReplies smartReplies, CharSequence charSequence, int i, Button button, SmartReplyController smartReplyController, NotificationEntry notificationEntry, Context context) {
        if (smartReplyView.mConstants.getEffectiveEditChoicesBeforeSending(smartReplies.remoteInput.getEditChoicesBeforeSending())) {
            NotificationEntry.EditedSuggestionInfo editedSuggestionInfo = new NotificationEntry.EditedSuggestionInfo(charSequence, i);
            NotificationRemoteInputManager notificationRemoteInputManager = smartReplyView.mRemoteInputManager;
            RemoteInput remoteInput = smartReplies.remoteInput;
            notificationRemoteInputManager.activateRemoteInput(button, new RemoteInput[]{remoteInput}, remoteInput, smartReplies.pendingIntent, editedSuggestionInfo);
            return false;
        }
        smartReplyController.smartReplySent(notificationEntry, i, button.getText(), NotificationLogger.getNotificationLocation(notificationEntry).toMetricsEventEnum(), false);
        Bundle bundle = new Bundle();
        bundle.putString(smartReplies.remoteInput.getResultKey(), charSequence.toString());
        Intent addFlags = new Intent().addFlags(268435456);
        RemoteInput.addResultsToIntent(new RemoteInput[]{smartReplies.remoteInput}, addFlags, bundle);
        RemoteInput.setResultsSource(addFlags, 1);
        notificationEntry.setHasSentReply();
        try {
            smartReplies.pendingIntent.send(context, 0, addFlags);
        } catch (PendingIntent.CanceledException e) {
            Log.w("SmartReplyView", "Unable to send smart reply", e);
        }
        smartReplyView.mSmartReplyContainer.setVisibility(8);
        return false;
    }

    @VisibleForTesting
    static Button inflateActionButton(SmartReplyView smartReplyView, Context context, Context context2, int i, SmartActions smartActions, SmartReplyController smartReplyController, NotificationEntry notificationEntry, HeadsUpManager headsUpManager, boolean z) {
        Notification.Action action = smartActions.actions.get(i);
        Button button = (Button) LayoutInflater.from(context).inflate(C0011R$layout.smart_action_button, (ViewGroup) smartReplyView, false);
        button.setText(action.title);
        Drawable loadDrawable = action.getIcon().loadDrawable(context2);
        int dimensionPixelSize = context.getResources().getDimensionPixelSize(C0005R$dimen.smart_action_button_icon_size);
        loadDrawable.setBounds(0, 0, dimensionPixelSize, dimensionPixelSize);
        button.setCompoundDrawables(loadDrawable, null, null, null);
        View.OnClickListener r10 = new View.OnClickListener(action, smartReplyController, notificationEntry, i, smartActions, headsUpManager) { // from class: com.android.systemui.statusbar.policy.-$$Lambda$SmartReplyView$tct0o0Zp_9czv90IHtUOrdcaxl0
            public final /* synthetic */ Notification.Action f$1;
            public final /* synthetic */ SmartReplyController f$2;
            public final /* synthetic */ NotificationEntry f$3;
            public final /* synthetic */ int f$4;
            public final /* synthetic */ SmartReplyView.SmartActions f$5;
            public final /* synthetic */ HeadsUpManager f$6;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
                this.f$6 = r7;
            }

            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                Notification.Action action2;
                NotificationEntry notificationEntry2;
                SmartReplyView.this.getActivityStarter().startPendingIntentDismissingKeyguard(action2.actionIntent, new Runnable(notificationEntry2, this.f$4, this.f$1, this.f$5, this.f$6) { // from class: com.android.systemui.statusbar.policy.-$$Lambda$SmartReplyView$TA933H11Yl_oDGgX0f0ntr5xGgI
                    public final /* synthetic */ NotificationEntry f$1;
                    public final /* synthetic */ int f$2;
                    public final /* synthetic */ Notification.Action f$3;
                    public final /* synthetic */ SmartReplyView.SmartActions f$4;
                    public final /* synthetic */ HeadsUpManager f$5;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r4;
                        this.f$4 = r5;
                        this.f$5 = r6;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        SmartReplyView.lambda$inflateActionButton$3(SmartReplyController.this, this.f$1, this.f$2, this.f$3, this.f$4, this.f$5);
                    }
                }, this.f$3.getRow());
            }
        };
        if (z) {
            r10 = new DelayedOnClickListener(r10, smartReplyView.mConstants.getOnClickInitDelay());
        }
        button.setOnClickListener(r10);
        ((LayoutParams) button.getLayoutParams()).buttonType = SmartButtonType.ACTION;
        return button;
    }

    static /* synthetic */ void lambda$inflateActionButton$3(SmartReplyController smartReplyController, NotificationEntry notificationEntry, int i, Notification.Action action, SmartActions smartActions, HeadsUpManager headsUpManager) {
        smartReplyController.smartActionClicked(notificationEntry, i, action, smartActions.fromAssistant);
        headsUpManager.removeNotification(notificationEntry.getKey(), true);
    }

    @Override // android.view.ViewGroup
    public LayoutParams generateLayoutParams(AttributeSet attributeSet) {
        return new LayoutParams(((ViewGroup) this).mContext, attributeSet);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-2, -2);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return new LayoutParams(layoutParams.width, layoutParams.height);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onMeasure(int i, int i2) {
        int i3;
        Iterator it;
        int i4;
        int i5;
        if (View.MeasureSpec.getMode(i) == 0) {
            i3 = Integer.MAX_VALUE;
        } else {
            i3 = View.MeasureSpec.getSize(i);
        }
        resetButtonsLayoutParams();
        if (!this.mCandidateButtonQueueForSqueezing.isEmpty()) {
            Log.wtf("SmartReplyView", "Single line button queue leaked between onMeasure calls");
            this.mCandidateButtonQueueForSqueezing.clear();
        }
        SmartSuggestionMeasures smartSuggestionMeasures = new SmartSuggestionMeasures(((ViewGroup) this).mPaddingLeft + ((ViewGroup) this).mPaddingRight, 0, this.mSingleLineButtonPaddingHorizontal);
        List<View> filterActionsOrReplies = filterActionsOrReplies(SmartButtonType.ACTION);
        List<View> filterActionsOrReplies2 = filterActionsOrReplies(SmartButtonType.REPLY);
        ArrayList<View> arrayList = new ArrayList(filterActionsOrReplies);
        arrayList.addAll(filterActionsOrReplies2);
        ArrayList arrayList2 = new ArrayList();
        SmartSuggestionMeasures smartSuggestionMeasures2 = null;
        int maxNumActions = this.mConstants.getMaxNumActions();
        Iterator it2 = arrayList.iterator();
        int i6 = 0;
        int i7 = 0;
        while (it2.hasNext()) {
            View view = (View) it2.next();
            LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
            if (maxNumActions == -1 || layoutParams.buttonType != SmartButtonType.ACTION || i6 < maxNumActions) {
                i4 = maxNumActions;
                it = it2;
                view.setPadding(smartSuggestionMeasures.mButtonPaddingHorizontal, view.getPaddingTop(), smartSuggestionMeasures.mButtonPaddingHorizontal, view.getPaddingBottom());
                view.measure(MEASURE_SPEC_ANY_LENGTH, i2);
                arrayList2.add(view);
                Button button = (Button) view;
                int lineCount = button.getLineCount();
                if (lineCount >= 1 && lineCount <= 2) {
                    if (lineCount == 1) {
                        this.mCandidateButtonQueueForSqueezing.add(button);
                    }
                    SmartSuggestionMeasures clone = smartSuggestionMeasures.clone();
                    if (smartSuggestionMeasures2 == null && layoutParams.buttonType == SmartButtonType.REPLY) {
                        smartSuggestionMeasures2 = smartSuggestionMeasures.clone();
                    }
                    if (i7 == 0) {
                        i5 = 0;
                    } else {
                        i5 = this.mSpacing;
                    }
                    int measuredWidth = view.getMeasuredWidth();
                    int measuredHeight = view.getMeasuredHeight();
                    smartSuggestionMeasures.mMeasuredWidth += i5 + measuredWidth;
                    smartSuggestionMeasures.mMaxChildHeight = Math.max(smartSuggestionMeasures.mMaxChildHeight, measuredHeight);
                    if (smartSuggestionMeasures.mButtonPaddingHorizontal == this.mSingleLineButtonPaddingHorizontal && (lineCount == 2 || smartSuggestionMeasures.mMeasuredWidth > i3)) {
                        smartSuggestionMeasures.mMeasuredWidth += (i7 + 1) * this.mSingleToDoubleLineButtonWidthIncrease;
                        smartSuggestionMeasures.mButtonPaddingHorizontal = this.mDoubleLineButtonPaddingHorizontal;
                    }
                    if (smartSuggestionMeasures.mMeasuredWidth > i3) {
                        while (smartSuggestionMeasures.mMeasuredWidth > i3 && !this.mCandidateButtonQueueForSqueezing.isEmpty()) {
                            Button poll = this.mCandidateButtonQueueForSqueezing.poll();
                            int squeezeButton = squeezeButton(poll, i2);
                            if (squeezeButton != -1) {
                                smartSuggestionMeasures.mMaxChildHeight = Math.max(smartSuggestionMeasures.mMaxChildHeight, poll.getMeasuredHeight());
                                smartSuggestionMeasures.mMeasuredWidth -= squeezeButton;
                            }
                        }
                        if (smartSuggestionMeasures.mMeasuredWidth > i3) {
                            markButtonsWithPendingSqueezeStatusAs(3, arrayList2);
                            maxNumActions = i4;
                            it2 = it;
                            smartSuggestionMeasures = clone;
                        } else {
                            markButtonsWithPendingSqueezeStatusAs(2, arrayList2);
                        }
                    }
                    layoutParams.show = true;
                    i7++;
                    if (layoutParams.buttonType == SmartButtonType.ACTION) {
                        i6++;
                    }
                }
            } else {
                i4 = maxNumActions;
                it = it2;
            }
            maxNumActions = i4;
            it2 = it;
        }
        if (this.mSmartRepliesGeneratedByAssistant && !gotEnoughSmartReplies(filterActionsOrReplies2)) {
            for (View view2 : filterActionsOrReplies2) {
                ((LayoutParams) view2.getLayoutParams()).show = false;
            }
            smartSuggestionMeasures = smartSuggestionMeasures2;
        }
        this.mCandidateButtonQueueForSqueezing.clear();
        remeasureButtonsIfNecessary(smartSuggestionMeasures.mButtonPaddingHorizontal, smartSuggestionMeasures.mMaxChildHeight);
        int max = Math.max(getSuggestedMinimumHeight(), ((ViewGroup) this).mPaddingTop + smartSuggestionMeasures.mMaxChildHeight + ((ViewGroup) this).mPaddingBottom);
        for (View view3 : arrayList) {
            setCornerRadius((Button) view3, ((float) max) / 2.0f);
        }
        setMeasuredDimension(ViewGroup.resolveSize(Math.max(getSuggestedMinimumWidth(), smartSuggestionMeasures.mMeasuredWidth), i), ViewGroup.resolveSize(max, i2));
    }

    private static class SmartSuggestionMeasures {
        int mButtonPaddingHorizontal = -1;
        int mMaxChildHeight = -1;
        int mMeasuredWidth = -1;

        SmartSuggestionMeasures(int i, int i2, int i3) {
            this.mMeasuredWidth = i;
            this.mMaxChildHeight = i2;
            this.mButtonPaddingHorizontal = i3;
        }

        public SmartSuggestionMeasures clone() {
            return new SmartSuggestionMeasures(this.mMeasuredWidth, this.mMaxChildHeight, this.mButtonPaddingHorizontal);
        }
    }

    private boolean gotEnoughSmartReplies(List<View> list) {
        int i = 0;
        for (View view : list) {
            if (((LayoutParams) view.getLayoutParams()).show) {
                i++;
            }
        }
        if (i == 0 || i >= this.mConstants.getMinNumSystemGeneratedReplies()) {
            return true;
        }
        return false;
    }

    private List<View> filterActionsOrReplies(SmartButtonType smartButtonType) {
        ArrayList arrayList = new ArrayList();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = getChildAt(i);
            LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
            if (childAt.getVisibility() == 0 && (childAt instanceof Button) && layoutParams.buttonType == smartButtonType) {
                arrayList.add(childAt);
            }
        }
        return arrayList;
    }

    private void resetButtonsLayoutParams() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            LayoutParams layoutParams = (LayoutParams) getChildAt(i).getLayoutParams();
            layoutParams.show = false;
            layoutParams.squeezeStatus = 0;
        }
    }

    private int squeezeButton(Button button, int i) {
        int estimateOptimalSqueezedButtonTextWidth = estimateOptimalSqueezedButtonTextWidth(button);
        if (estimateOptimalSqueezedButtonTextWidth == -1) {
            return -1;
        }
        return squeezeButtonToTextWidth(button, i, estimateOptimalSqueezedButtonTextWidth);
    }

    /* JADX WARNING: Removed duplicated region for block: B:32:0x0090 A[LOOP:0: B:15:0x005c->B:32:0x0090, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x008e A[SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int estimateOptimalSqueezedButtonTextWidth(android.widget.Button r14) {
        /*
            r13 = this;
            java.lang.CharSequence r0 = r14.getText()
            java.lang.String r0 = r0.toString()
            android.text.method.TransformationMethod r1 = r14.getTransformationMethod()
            if (r1 != 0) goto L_0x000f
            goto L_0x0017
        L_0x000f:
            java.lang.CharSequence r0 = r1.getTransformation(r0, r14)
            java.lang.String r0 = r0.toString()
        L_0x0017:
            int r1 = r0.length()
            java.text.BreakIterator r2 = r13.mBreakIterator
            r2.setText(r0)
            java.text.BreakIterator r2 = r13.mBreakIterator
            int r3 = r1 / 2
            int r2 = r2.preceding(r3)
            r3 = -1
            if (r2 != r3) goto L_0x0034
            java.text.BreakIterator r2 = r13.mBreakIterator
            int r2 = r2.next()
            if (r2 != r3) goto L_0x0034
            return r3
        L_0x0034:
            android.text.TextPaint r14 = r14.getPaint()
            java.text.BreakIterator r2 = r13.mBreakIterator
            int r2 = r2.current()
            r4 = 0
            float r5 = android.text.Layout.getDesiredWidth(r0, r4, r2, r14)
            float r2 = android.text.Layout.getDesiredWidth(r0, r2, r1, r14)
            float r6 = java.lang.Math.max(r5, r2)
            int r2 = (r5 > r2 ? 1 : (r5 == r2 ? 0 : -1))
            if (r2 == 0) goto L_0x0094
            r5 = 1
            if (r2 <= 0) goto L_0x0054
            r2 = r5
            goto L_0x0055
        L_0x0054:
            r2 = r4
        L_0x0055:
            com.android.systemui.statusbar.policy.SmartReplyConstants r7 = r13.mConstants
            int r7 = r7.getMaxSqueezeRemeasureAttempts()
            r8 = r4
        L_0x005c:
            if (r8 >= r7) goto L_0x0094
            java.text.BreakIterator r9 = r13.mBreakIterator
            if (r2 == 0) goto L_0x0067
            int r9 = r9.previous()
            goto L_0x006b
        L_0x0067:
            int r9 = r9.next()
        L_0x006b:
            if (r9 != r3) goto L_0x006e
            goto L_0x0094
        L_0x006e:
            float r10 = android.text.Layout.getDesiredWidth(r0, r4, r9, r14)
            float r9 = android.text.Layout.getDesiredWidth(r0, r9, r1, r14)
            float r11 = java.lang.Math.max(r10, r9)
            int r12 = (r11 > r6 ? 1 : (r11 == r6 ? 0 : -1))
            if (r12 >= 0) goto L_0x0094
            if (r2 == 0) goto L_0x0085
            int r6 = (r10 > r9 ? 1 : (r10 == r9 ? 0 : -1))
            if (r6 > 0) goto L_0x008b
            goto L_0x0089
        L_0x0085:
            int r6 = (r10 > r9 ? 1 : (r10 == r9 ? 0 : -1))
            if (r6 < 0) goto L_0x008b
        L_0x0089:
            r6 = r5
            goto L_0x008c
        L_0x008b:
            r6 = r4
        L_0x008c:
            if (r6 == 0) goto L_0x0090
            r6 = r11
            goto L_0x0094
        L_0x0090:
            int r8 = r8 + 1
            r6 = r11
            goto L_0x005c
        L_0x0094:
            double r13 = (double) r6
            double r13 = java.lang.Math.ceil(r13)
            int r13 = (int) r13
            return r13
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.policy.SmartReplyView.estimateOptimalSqueezedButtonTextWidth(android.widget.Button):int");
    }

    private int getLeftCompoundDrawableWidthWithPadding(Button button) {
        Drawable drawable = button.getCompoundDrawables()[0];
        if (drawable == null) {
            return 0;
        }
        return drawable.getBounds().width() + button.getCompoundDrawablePadding();
    }

    private int squeezeButtonToTextWidth(Button button, int i, int i2) {
        int measuredWidth = button.getMeasuredWidth();
        if (button.getPaddingLeft() != this.mDoubleLineButtonPaddingHorizontal) {
            measuredWidth += this.mSingleToDoubleLineButtonWidthIncrease;
        }
        button.setPadding(this.mDoubleLineButtonPaddingHorizontal, button.getPaddingTop(), this.mDoubleLineButtonPaddingHorizontal, button.getPaddingBottom());
        button.measure(View.MeasureSpec.makeMeasureSpec((this.mDoubleLineButtonPaddingHorizontal * 2) + i2 + getLeftCompoundDrawableWidthWithPadding(button), Integer.MIN_VALUE), i);
        int measuredWidth2 = button.getMeasuredWidth();
        LayoutParams layoutParams = (LayoutParams) button.getLayoutParams();
        if (button.getLineCount() > 2 || measuredWidth2 >= measuredWidth) {
            layoutParams.squeezeStatus = 3;
            return -1;
        }
        layoutParams.squeezeStatus = 1;
        return measuredWidth - measuredWidth2;
    }

    private void remeasureButtonsIfNecessary(int i, int i2) {
        boolean z;
        int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(i2, 1073741824);
        int childCount = getChildCount();
        for (int i3 = 0; i3 < childCount; i3++) {
            View childAt = getChildAt(i3);
            LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
            if (layoutParams.show) {
                int measuredWidth = childAt.getMeasuredWidth();
                boolean z2 = true;
                if (layoutParams.squeezeStatus == 3) {
                    measuredWidth = Integer.MAX_VALUE;
                    z = true;
                } else {
                    z = false;
                }
                if (childAt.getPaddingLeft() != i) {
                    if (measuredWidth != Integer.MAX_VALUE) {
                        if (i == this.mSingleLineButtonPaddingHorizontal) {
                            measuredWidth -= this.mSingleToDoubleLineButtonWidthIncrease;
                        } else {
                            measuredWidth += this.mSingleToDoubleLineButtonWidthIncrease;
                        }
                    }
                    childAt.setPadding(i, childAt.getPaddingTop(), i, childAt.getPaddingBottom());
                    z = true;
                }
                if (childAt.getMeasuredHeight() == i2) {
                    z2 = z;
                }
                if (z2) {
                    childAt.measure(View.MeasureSpec.makeMeasureSpec(measuredWidth, Integer.MIN_VALUE), makeMeasureSpec);
                }
            }
        }
    }

    private void markButtonsWithPendingSqueezeStatusAs(int i, List<View> list) {
        for (View view : list) {
            LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
            if (layoutParams.squeezeStatus == 1) {
                layoutParams.squeezeStatus = i;
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup, android.view.View
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        boolean z2 = true;
        if (getLayoutDirection() != 1) {
            z2 = false;
        }
        int i5 = z2 ? (i3 - i) - ((ViewGroup) this).mPaddingRight : ((ViewGroup) this).mPaddingLeft;
        int childCount = getChildCount();
        for (int i6 = 0; i6 < childCount; i6++) {
            View childAt = getChildAt(i6);
            if (((LayoutParams) childAt.getLayoutParams()).show) {
                int measuredWidth = childAt.getMeasuredWidth();
                int measuredHeight = childAt.getMeasuredHeight();
                int i7 = z2 ? i5 - measuredWidth : i5;
                childAt.layout(i7, 0, i7 + measuredWidth, measuredHeight);
                int i8 = measuredWidth + this.mSpacing;
                i5 = z2 ? i5 - i8 : i5 + i8;
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public boolean drawChild(Canvas canvas, View view, long j) {
        return ((LayoutParams) view.getLayoutParams()).show && super.drawChild(canvas, view, j);
    }

    public void setBackgroundTintColor(int i) {
        if (i != this.mCurrentBackgroundColor) {
            this.mCurrentBackgroundColor = i;
            boolean z = !ContrastColorUtil.isColorLight(i);
            int i2 = -16777216 | i;
            int ensureTextContrast = ContrastColorUtil.ensureTextContrast(z ? this.mDefaultTextColorDarkBg : this.mDefaultTextColor, i2, z);
            int ensureContrast = ContrastColorUtil.ensureContrast(this.mDefaultStrokeColor, i2, z, this.mMinStrokeContrast);
            int i3 = z ? this.mRippleColorDarkBg : this.mRippleColor;
            int childCount = getChildCount();
            for (int i4 = 0; i4 < childCount; i4++) {
                setButtonColors((Button) getChildAt(i4), i, ensureContrast, ensureTextContrast, i3, this.mStrokeWidth);
            }
        }
    }

    private static void setButtonColors(Button button, int i, int i2, int i3, int i4, int i5) {
        Drawable background = button.getBackground();
        if (background instanceof RippleDrawable) {
            Drawable mutate = background.mutate();
            RippleDrawable rippleDrawable = (RippleDrawable) mutate;
            rippleDrawable.setColor(ColorStateList.valueOf(i4));
            Drawable drawable = rippleDrawable.getDrawable(0);
            if (drawable instanceof InsetDrawable) {
                Drawable drawable2 = ((InsetDrawable) drawable).getDrawable();
                if (drawable2 instanceof GradientDrawable) {
                    GradientDrawable gradientDrawable = (GradientDrawable) drawable2;
                    gradientDrawable.setColor(i);
                    gradientDrawable.setStroke(i5, i2);
                }
            }
            button.setBackground(mutate);
        }
        button.setTextColor(i3);
    }

    private void setCornerRadius(Button button, float f) {
        Drawable background = button.getBackground();
        if (background instanceof RippleDrawable) {
            Drawable drawable = ((RippleDrawable) background.mutate()).getDrawable(0);
            if (drawable instanceof InsetDrawable) {
                Drawable drawable2 = ((InsetDrawable) drawable).getDrawable();
                if (drawable2 instanceof GradientDrawable) {
                    ((GradientDrawable) drawable2).setCornerRadius(f);
                }
            }
        }
    }

    private ActivityStarter getActivityStarter() {
        if (this.mActivityStarter == null) {
            this.mActivityStarter = (ActivityStarter) Dependency.get(ActivityStarter.class);
        }
        return this.mActivityStarter;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public static class LayoutParams extends ViewGroup.LayoutParams {
        private SmartButtonType buttonType;
        private boolean show;
        private int squeezeStatus;

        private LayoutParams(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            this.show = false;
            this.squeezeStatus = 0;
            this.buttonType = SmartButtonType.REPLY;
        }

        private LayoutParams(int i, int i2) {
            super(i, i2);
            this.show = false;
            this.squeezeStatus = 0;
            this.buttonType = SmartButtonType.REPLY;
        }

        /* access modifiers changed from: package-private */
        @VisibleForTesting
        public boolean isShown() {
            return this.show;
        }
    }

    public static class SmartReplies {
        public final List<CharSequence> choices;
        public final boolean fromAssistant;
        public final PendingIntent pendingIntent;
        public final RemoteInput remoteInput;

        public SmartReplies(List<CharSequence> list, RemoteInput remoteInput, PendingIntent pendingIntent, boolean z) {
            this.choices = list;
            this.remoteInput = remoteInput;
            this.pendingIntent = pendingIntent;
            this.fromAssistant = z;
        }
    }

    public static class SmartActions {
        public final List<Notification.Action> actions;
        public final boolean fromAssistant;

        public SmartActions(List<Notification.Action> list, boolean z) {
            this.actions = list;
            this.fromAssistant = z;
        }
    }

    /* access modifiers changed from: private */
    public static class DelayedOnClickListener implements View.OnClickListener {
        private final View.OnClickListener mActualListener;
        private final long mInitDelayMs;
        private final long mInitTimeMs = SystemClock.elapsedRealtime();

        DelayedOnClickListener(View.OnClickListener onClickListener, long j) {
            this.mActualListener = onClickListener;
            this.mInitDelayMs = j;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            if (hasFinishedInitialization()) {
                this.mActualListener.onClick(view);
                return;
            }
            Log.i("SmartReplyView", "Accidental Smart Suggestion click registered, delay: " + this.mInitDelayMs);
        }

        private boolean hasFinishedInitialization() {
            return SystemClock.elapsedRealtime() >= this.mInitTimeMs + this.mInitDelayMs;
        }
    }
}
