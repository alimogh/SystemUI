package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0008R$id;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.StatusIconDisplayable;
import com.android.systemui.statusbar.notification.stack.AnimationFilter;
import com.android.systemui.statusbar.notification.stack.AnimationProperties;
import com.android.systemui.statusbar.notification.stack.ViewState;
import com.android.systemui.statusbar.policy.BatteryController;
import com.oneplus.systemui.statusbar.phone.OpStatusIconContainer;
import java.util.ArrayList;
import java.util.List;
public class StatusIconContainer extends OpStatusIconContainer {
    private static final AnimationProperties ADD_ICON_PROPERTIES;
    private static final boolean DEBUG_OVERFLOW = SystemProperties.getBoolean("debug.status_icon_outline", false);
    private static final AnimationProperties X_ANIMATION_PROPERTIES;
    private boolean mBatteryChange;
    private BatteryController mBatteryController;
    private boolean mBatteryPercentShow;
    private int mClockWidth;
    private boolean mClockWidthChanged;
    private int mDotPadding;
    private final Handler mHandler;
    private int mIconDotFrameWidth;
    private int mIconSpacing;
    private ArrayList<String> mIgnoredSlots;
    private ArrayList<StatusIconState> mLayoutStates;
    private ArrayList<View> mMeasureViews;
    private boolean mNeedsUnderflow;
    private String mOpTag;
    private Runnable mReRequestLayout;
    private int mReRequestLayoutTimes;
    private boolean mShouldRestrictIcons;
    private int mStaticDotDiameter;
    private StatusBar mStatusBar;
    private int mUnderflowStart;
    private int mUnderflowWidth;

    static /* synthetic */ int access$208(StatusIconContainer statusIconContainer) {
        int i = statusIconContainer.mReRequestLayoutTimes;
        statusIconContainer.mReRequestLayoutTimes = i + 1;
        return i;
    }

    static {
        AnonymousClass2 r0 = new AnimationProperties() { // from class: com.android.systemui.statusbar.phone.StatusIconContainer.2
            private AnimationFilter mAnimationFilter;

            {
                AnimationFilter animationFilter = new AnimationFilter();
                animationFilter.animateAlpha();
                this.mAnimationFilter = animationFilter;
            }

            @Override // com.android.systemui.statusbar.notification.stack.AnimationProperties
            public AnimationFilter getAnimationFilter() {
                return this.mAnimationFilter;
            }
        };
        r0.setDuration(200);
        r0.setDelay(50);
        ADD_ICON_PROPERTIES = r0;
        AnonymousClass3 r02 = new AnimationProperties() { // from class: com.android.systemui.statusbar.phone.StatusIconContainer.3
            private AnimationFilter mAnimationFilter;

            {
                AnimationFilter animationFilter = new AnimationFilter();
                animationFilter.animateX();
                this.mAnimationFilter = animationFilter;
            }

            @Override // com.android.systemui.statusbar.notification.stack.AnimationProperties
            public AnimationFilter getAnimationFilter() {
                return this.mAnimationFilter;
            }
        };
        r02.setDuration(200);
        X_ANIMATION_PROPERTIES = r02;
        new AnimationProperties() { // from class: com.android.systemui.statusbar.phone.StatusIconContainer.4
            private AnimationFilter mAnimationFilter;

            {
                AnimationFilter animationFilter = new AnimationFilter();
                animationFilter.animateX();
                animationFilter.animateY();
                animationFilter.animateAlpha();
                animationFilter.animateScale();
                this.mAnimationFilter = animationFilter;
            }

            @Override // com.android.systemui.statusbar.notification.stack.AnimationProperties
            public AnimationFilter getAnimationFilter() {
                return this.mAnimationFilter;
            }
        }.setDuration(200);
    }

    public void setOpTag(String str) {
        this.mOpTag = str;
    }

    public StatusIconContainer(Context context) {
        this(context, null);
    }

    public StatusIconContainer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mReRequestLayoutTimes = 0;
        this.mUnderflowStart = 0;
        this.mShouldRestrictIcons = true;
        this.mLayoutStates = new ArrayList<>();
        this.mMeasureViews = new ArrayList<>();
        this.mIgnoredSlots = new ArrayList<>();
        this.mOpTag = "";
        this.mClockWidth = 0;
        this.mClockWidthChanged = false;
        this.mReRequestLayout = new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusIconContainer.1
            @Override // java.lang.Runnable
            public void run() {
                StatusIconContainer.this.mHandler.removeCallbacks(StatusIconContainer.this.mReRequestLayout);
                StatusIconContainer.access$208(StatusIconContainer.this);
                boolean z = StatusIconContainer.this.mReRequestLayoutTimes >= 2;
                Log.i("StatusIconContainer", "mReRequestLayout, mReRequestLayoutTimes:" + StatusIconContainer.this.mReRequestLayoutTimes + ", timeout:" + z);
                if (!StatusIconContainer.this.isLayoutRequested() || z) {
                    StatusIconContainer.this.mReRequestLayoutTimes = 0;
                    StatusIconContainer.this.requestLayout();
                    return;
                }
                StatusIconContainer.this.mHandler.postDelayed(StatusIconContainer.this.mReRequestLayout, 100);
            }
        };
        this.mBatteryPercentShow = false;
        this.mHandler = new Handler();
        this.mBatteryController = (BatteryController) Dependency.get(BatteryController.class);
        initDimens();
        setWillNotDraw(!DEBUG_OVERFLOW);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
    }

    public void setShouldRestrictIcons(boolean z) {
        this.mShouldRestrictIcons = z;
    }

    public boolean isRestrictingIcons() {
        return this.mShouldRestrictIcons;
    }

    private void initDimens() {
        this.mIconDotFrameWidth = getResources().getDimensionPixelSize(17105484);
        this.mDotPadding = getResources().getDimensionPixelSize(C0005R$dimen.overflow_icon_dot_padding);
        this.mIconSpacing = getResources().getDimensionPixelSize(C0005R$dimen.status_bar_system_icon_spacing);
        int dimensionPixelSize = getResources().getDimensionPixelSize(C0005R$dimen.overflow_dot_radius) * 2;
        this.mStaticDotDiameter = dimensionPixelSize;
        this.mUnderflowWidth = setUnderflowWidth(this.mIconDotFrameWidth, dimensionPixelSize, this.mDotPadding);
    }

    private int getOpWidth() {
        return getOpMaxWidth(getWidth());
    }

    private int getOpMaxWidth(int i) {
        if (this.mOpTag.isEmpty()) {
            return i;
        }
        if (!"status_icon_container".equals(this.mOpTag) && !"demo_status_icon_container".equals(this.mOpTag)) {
            return i;
        }
        if (this.mStatusBar == null) {
            this.mStatusBar = (StatusBar) Dependency.get(StatusBar.class);
        }
        int systemIconAreaMaxWidth = this.mStatusBar.getSystemIconAreaMaxWidth(i);
        return (systemIconAreaMaxWidth <= 0 || i <= systemIconAreaMaxWidth) ? i : systemIconAreaMaxWidth;
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.View, android.view.ViewGroup
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        float height = ((float) getHeight()) / 2.0f;
        for (int i5 = 0; i5 < getChildCount(); i5++) {
            View childAt = getChildAt(i5);
            int measuredWidth = childAt.getMeasuredWidth();
            int measuredHeight = childAt.getMeasuredHeight();
            int i6 = (int) (height - (((float) measuredHeight) / 2.0f));
            childAt.layout(0, i6, measuredWidth, measuredHeight + i6);
        }
        resetViewStates();
        calculateIconTranslations();
        applyIconStates();
        if (this.mStatusBar == null) {
            this.mStatusBar = (StatusBar) Dependency.get(StatusBar.class);
        }
        int minWidthOfClock = this.mStatusBar.getMinWidthOfClock();
        if (this.mClockWidth != minWidthOfClock) {
            this.mClockWidth = minWidthOfClock;
            this.mClockWidthChanged = true;
        }
        if (getWidth() > getOpWidth() || this.mBatteryChange || this.mBatteryPercentShow || this.mClockWidthChanged) {
            StringBuilder sb = new StringBuilder();
            sb.append("onLayout, last, getWidth() > getOpWidth(),  getWidth():");
            sb.append(getWidth());
            sb.append(", getMeasuredWidth:");
            sb.append(getMeasuredWidth());
            sb.append(", getOpWidth():");
            sb.append(getOpWidth());
            sb.append(", getParent().getParent():");
            sb.append(getParent() != null ? getParent().getParent() : "Null");
            Log.i("StatusIconContainer", sb.toString());
            this.mHandler.postDelayed(this.mReRequestLayout, 100);
            this.mBatteryChange = false;
            this.mBatteryPercentShow = false;
            this.mClockWidthChanged = false;
        }
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
    public void onBatteryStyleChanged(int i) {
        this.mBatteryChange = true;
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
    public void onBatteryPercentShowChange(boolean z) {
        this.mBatteryPercentShow = true;
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.View
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (DEBUG_OVERFLOW) {
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(-65536);
            canvas.drawRect((float) getPaddingStart(), 0.0f, (float) (getWidth() - getPaddingEnd()), (float) getHeight(), paint);
            paint.setColor(-16711936);
            int i = this.mUnderflowStart;
            canvas.drawRect((float) i, 0.0f, (float) (i + this.mUnderflowWidth), (float) getHeight(), paint);
            paint.setColor(-16776961);
            int childCount = getChildCount();
            for (int i2 = 0; i2 < childCount; i2++) {
                View childAt = getChildAt(i2);
                StatusIconState viewStateFromChild = getViewStateFromChild(childAt);
                canvas.drawRect(viewStateFromChild.xTranslation, (float) childAt.getTop(), ((float) childAt.getWidth()) + viewStateFromChild.xTranslation, (float) childAt.getBottom(), paint);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.View
    public void onMeasure(int i, int i2) {
        int i3;
        this.mMeasureViews.clear();
        int mode = View.MeasureSpec.getMode(i);
        int opMaxWidth = getOpMaxWidth(View.MeasureSpec.getSize(i));
        int childCount = getChildCount();
        for (int i4 = 0; i4 < childCount; i4++) {
            StatusIconDisplayable statusIconDisplayable = (StatusIconDisplayable) getChildAt(i4);
            if (statusIconDisplayable.isIconVisible() && !statusIconDisplayable.isIconBlocked() && !this.mIgnoredSlots.contains(statusIconDisplayable.getSlot())) {
                this.mMeasureViews.add((View) statusIconDisplayable);
            }
        }
        int size = this.mMeasureViews.size();
        int i5 = size <= 70 ? 70 : 69;
        int i6 = ((LinearLayout) this).mPaddingLeft + ((LinearLayout) this).mPaddingRight;
        int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(opMaxWidth, 0);
        this.mNeedsUnderflow = this.mShouldRestrictIcons && size > 70;
        boolean z = true;
        for (int i7 = 0; i7 < size; i7++) {
            View view = this.mMeasureViews.get((size - i7) - 1);
            measureChild(view, makeMeasureSpec, i2);
            int i8 = i7 == size - 1 ? 0 : this.mIconSpacing;
            if (!this.mShouldRestrictIcons) {
                i3 = getViewTotalMeasuredWidth(view);
            } else if (i7 >= i5 || !z) {
                if (z) {
                    i6 += this.mUnderflowWidth;
                    z = false;
                }
            } else {
                i3 = getViewTotalMeasuredWidth(view);
            }
            i6 += i3 + i8;
        }
        if (mode == 1073741824) {
            if (!this.mNeedsUnderflow && i6 > opMaxWidth) {
                this.mNeedsUnderflow = true;
            }
            setMeasuredDimension(opMaxWidth, View.MeasureSpec.getSize(i2));
            return;
        }
        if (mode != Integer.MIN_VALUE || i6 <= opMaxWidth) {
            opMaxWidth = i6;
        } else {
            this.mNeedsUnderflow = true;
        }
        setMeasuredDimension(opMaxWidth, View.MeasureSpec.getSize(i2));
    }

    @Override // android.view.ViewGroup
    public void onViewAdded(View view) {
        super.onViewAdded(view);
        StatusIconState statusIconState = new StatusIconState();
        statusIconState.justAdded = true;
        view.setTag(C0008R$id.status_bar_view_state_tag, statusIconState);
    }

    @Override // android.view.ViewGroup
    public void onViewRemoved(View view) {
        super.onViewRemoved(view);
        view.setTag(C0008R$id.status_bar_view_state_tag, null);
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.systemui.statusbar.phone.OpStatusIconContainer, android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mBatteryController.addCallback(this);
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.systemui.statusbar.phone.OpStatusIconContainer, android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mBatteryController.removeCallback(this);
    }

    public void addIgnoredSlots(List<String> list) {
        for (String str : list) {
            addIgnoredSlotInternal(str);
        }
        requestLayout();
    }

    private void addIgnoredSlotInternal(String str) {
        if (!this.mIgnoredSlots.contains(str)) {
            this.mIgnoredSlots.add(str);
        }
    }

    private void calculateIconTranslations() {
        int i;
        this.mLayoutStates.clear();
        float opWidth = (float) getOpWidth();
        float paddingEnd = opWidth - ((float) getPaddingEnd());
        float paddingStart = (float) getPaddingStart();
        int childCount = getChildCount();
        int i2 = childCount - 1;
        while (true) {
            if (i2 < 0) {
                break;
            }
            View childAt = getChildAt(i2);
            StatusIconDisplayable statusIconDisplayable = (StatusIconDisplayable) childAt;
            StatusIconState viewStateFromChild = getViewStateFromChild(childAt);
            if (!statusIconDisplayable.isIconVisible() || statusIconDisplayable.isIconBlocked() || this.mIgnoredSlots.contains(statusIconDisplayable.getSlot())) {
                viewStateFromChild.visibleState = 2;
            } else {
                float viewTotalWidth = paddingEnd - ((float) getViewTotalWidth(childAt));
                viewStateFromChild.visibleState = 0;
                viewStateFromChild.xTranslation = viewTotalWidth;
                this.mLayoutStates.add(0, viewStateFromChild);
                paddingEnd = viewTotalWidth - ((float) this.mIconSpacing);
            }
            i2--;
        }
        int size = this.mLayoutStates.size();
        int i3 = 70;
        if (size > 70) {
            i3 = 69;
        }
        this.mUnderflowStart = 0;
        int i4 = size - 1;
        int i5 = 0;
        while (true) {
            if (i4 < 0) {
                i4 = -1;
                break;
            }
            StatusIconState statusIconState = this.mLayoutStates.get(i4);
            if ((this.mNeedsUnderflow && statusIconState.xTranslation < ((float) this.mUnderflowWidth) + paddingStart) || (this.mShouldRestrictIcons && i5 >= i3)) {
                break;
            }
            this.mUnderflowStart = (int) Math.max(paddingStart, (statusIconState.xTranslation - ((float) this.mUnderflowWidth)) - ((float) this.mIconSpacing));
            i5++;
            i4--;
        }
        if (i4 != -1) {
            int i6 = this.mStaticDotDiameter + this.mDotPadding;
            int i7 = (this.mUnderflowStart + this.mUnderflowWidth) - this.mIconDotFrameWidth;
            int i8 = 0;
            while (i4 >= 0) {
                StatusIconState statusIconState2 = this.mLayoutStates.get(i4);
                if (i8 < OpStatusIconContainer.MAX_DOTS) {
                    statusIconState2.xTranslation = (float) i7;
                    statusIconState2.visibleState = 1;
                    i7 -= i6;
                    i8++;
                } else {
                    statusIconState2.visibleState = 2;
                }
                i4--;
            }
        }
        if (isLayoutRtl()) {
            for (i = 0; i < childCount; i++) {
                View childAt2 = getChildAt(i);
                StatusIconState viewStateFromChild2 = getViewStateFromChild(childAt2);
                viewStateFromChild2.xTranslation = (opWidth - viewStateFromChild2.xTranslation) - ((float) childAt2.getWidth());
            }
        }
    }

    private void applyIconStates() {
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            StatusIconState viewStateFromChild = getViewStateFromChild(childAt);
            if (viewStateFromChild != null) {
                viewStateFromChild.applyToView(childAt);
            }
        }
    }

    private void resetViewStates() {
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            StatusIconState viewStateFromChild = getViewStateFromChild(childAt);
            if (viewStateFromChild != null) {
                viewStateFromChild.initFrom(childAt);
                viewStateFromChild.alpha = 1.0f;
                viewStateFromChild.hidden = false;
            }
        }
    }

    private static StatusIconState getViewStateFromChild(View view) {
        return (StatusIconState) view.getTag(C0008R$id.status_bar_view_state_tag);
    }

    private static int getViewTotalMeasuredWidth(View view) {
        return view.getMeasuredWidth() + view.getPaddingStart() + view.getPaddingEnd();
    }

    private static int getViewTotalWidth(View view) {
        return view.getWidth() + view.getPaddingStart() + view.getPaddingEnd();
    }

    public static class StatusIconState extends ViewState {
        float distanceToViewEnd = -1.0f;
        public boolean justAdded = true;
        public int visibleState = 0;

        @Override // com.android.systemui.statusbar.notification.stack.ViewState
        public void applyToView(View view) {
            float width = (view.getParent() instanceof View ? (float) ((View) view.getParent()).getWidth() : 0.0f) - this.xTranslation;
            if (view instanceof StatusIconDisplayable) {
                StatusIconDisplayable statusIconDisplayable = (StatusIconDisplayable) view;
                AnimationProperties animationProperties = null;
                if (this.justAdded || (statusIconDisplayable.getVisibleState() == 2 && this.visibleState == 0)) {
                    super.applyToView(view);
                    animationProperties = StatusIconContainer.ADD_ICON_PROPERTIES;
                } else if (!(this.visibleState == 2 || this.distanceToViewEnd == width)) {
                    animationProperties = StatusIconContainer.X_ANIMATION_PROPERTIES;
                }
                statusIconDisplayable.setVisibleState(this.visibleState, true);
                if (animationProperties != null) {
                    animateTo(view, animationProperties);
                } else {
                    super.applyToView(view);
                }
                this.justAdded = false;
                this.distanceToViewEnd = width;
            }
        }
    }
}
