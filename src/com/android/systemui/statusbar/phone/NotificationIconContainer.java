package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Icon;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Property;
import android.view.View;
import androidx.collection.ArrayMap;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.statusbar.StatusBarIconView;
import com.android.systemui.statusbar.notification.stack.AnimationFilter;
import com.android.systemui.statusbar.notification.stack.AnimationProperties;
import com.android.systemui.statusbar.notification.stack.ViewState;
import com.android.systemui.statusbar.phone.NotificationIconContainer;
import com.oneplus.systemui.statusbar.phone.OpNotificationIconContainer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
public class NotificationIconContainer extends OpNotificationIconContainer {
    private static final AnimationProperties ADD_ICON_PROPERTIES;
    private static final AnimationProperties DOT_ANIMATION_PROPERTIES;
    private static final AnimationProperties ICON_ANIMATION_PROPERTIES;
    private static final AnimationProperties UNISOLATION_PROPERTY;
    private static final AnimationProperties UNISOLATION_PROPERTY_OTHERS;
    private static final AnimationProperties sTempProperties = new AnimationProperties() { // from class: com.android.systemui.statusbar.phone.NotificationIconContainer.3
        private AnimationFilter mAnimationFilter = new AnimationFilter();

        @Override // com.android.systemui.statusbar.notification.stack.AnimationProperties
        public AnimationFilter getAnimationFilter() {
            return this.mAnimationFilter;
        }
    };
    private int[] mAbsolutePosition = new int[2];
    private int mActualLayoutWidth = Integer.MIN_VALUE;
    private float mActualPaddingEnd = -2.14748365E9f;
    private float mActualPaddingStart = -2.14748365E9f;
    private int mAddAnimationStartIndex = -1;
    private boolean mAnimationsEnabled = true;
    private int mCannedAnimationStartIndex = -1;
    private boolean mChangingViewPositions;
    private boolean mDisallowNextAnimation;
    private int mDotPadding;
    private boolean mDozing;
    private IconState mFirstVisibleIconState;
    private int mIconSize;
    private final HashMap<View, IconState> mIconStates = new HashMap<>();
    private boolean mIsStaticLayout = true;
    private StatusBarIconView mIsolatedIcon;
    private View mIsolatedIconForAnimation;
    private Rect mIsolatedIconLocation;
    private IconState mLastVisibleIconState;
    private int mNumDots;
    private boolean mOnLockScreen;
    private float mOpenedAmount = 0.0f;
    private int mOverflowWidth;
    private ArrayMap<String, ArrayList<StatusBarIcon>> mReplacingIcons;
    private int mSpeedBumpIndex = -1;
    private int mStaticDotDiameter;
    private int mStaticDotRadius;
    private float mVisualOverflowStart;

    static {
        AnonymousClass1 r0 = new AnimationProperties() { // from class: com.android.systemui.statusbar.phone.NotificationIconContainer.1
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
        r0.setDuration(200);
        DOT_ANIMATION_PROPERTIES = r0;
        AnonymousClass2 r02 = new AnimationProperties() { // from class: com.android.systemui.statusbar.phone.NotificationIconContainer.2
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
        };
        r02.setDuration(100);
        ICON_ANIMATION_PROPERTIES = r02;
        AnonymousClass4 r03 = new AnimationProperties() { // from class: com.android.systemui.statusbar.phone.NotificationIconContainer.4
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
        r03.setDuration(200);
        r03.setDelay(50);
        ADD_ICON_PROPERTIES = r03;
        AnonymousClass5 r04 = new AnimationProperties() { // from class: com.android.systemui.statusbar.phone.NotificationIconContainer.5
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
        r04.setDuration(110);
        UNISOLATION_PROPERTY_OTHERS = r04;
        AnonymousClass6 r05 = new AnimationProperties() { // from class: com.android.systemui.statusbar.phone.NotificationIconContainer.6
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
        r05.setDuration(110);
        UNISOLATION_PROPERTY = r05;
    }

    public NotificationIconContainer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        initDimens();
        setWillNotDraw(true);
    }

    private void initDimens() {
        this.mDotPadding = getResources().getDimensionPixelSize(C0005R$dimen.overflow_icon_dot_padding);
        int dimensionPixelSize = getResources().getDimensionPixelSize(C0005R$dimen.overflow_dot_radius);
        this.mStaticDotRadius = dimensionPixelSize;
        this.mStaticDotDiameter = dimensionPixelSize * 2;
        initDimensInternal();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setColor(-65536);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(getActualPaddingStart(), 0.0f, getLayoutEnd(), (float) getHeight(), paint);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        initDimens();
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.FrameLayout, android.view.View, android.view.ViewGroup
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        float height = ((float) getHeight()) / 2.0f;
        this.mIconSize = 0;
        for (int i5 = 0; i5 < getChildCount(); i5++) {
            View childAt = getChildAt(i5);
            int measuredWidth = childAt.getMeasuredWidth();
            int measuredHeight = childAt.getMeasuredHeight();
            int i6 = (int) (height - (((float) measuredHeight) / 2.0f));
            childAt.layout(0, i6, measuredWidth, measuredHeight + i6);
            if (i5 == 0) {
                setIconSize(childAt.getWidth());
            }
        }
        getLocationOnScreen(this.mAbsolutePosition);
        if (this.mIsStaticLayout) {
            updateState();
        }
    }

    private void setIconSize(int i) {
        this.mIconSize = i;
        this.mOverflowWidth = setOverflowWidth(i, this.mStaticDotDiameter, this.mDotPadding);
    }

    private void updateState() {
        resetViewStates();
        calculateIconTranslations();
        applyIconStates();
    }

    public void applyIconStates() {
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            IconState iconState = this.mIconStates.get(childAt);
            if (iconState != null) {
                iconState.applyToView(childAt);
            }
        }
        this.mAddAnimationStartIndex = -1;
        this.mCannedAnimationStartIndex = -1;
        this.mDisallowNextAnimation = false;
        this.mIsolatedIconForAnimation = null;
    }

    @Override // android.view.ViewGroup
    public void onViewAdded(View view) {
        super.onViewAdded(view);
        boolean isReplacingIcon = isReplacingIcon(view);
        if (!this.mChangingViewPositions) {
            IconState iconState = new IconState(view);
            if (isReplacingIcon) {
                iconState.justAdded = false;
                iconState.justReplaced = true;
            }
            this.mIconStates.put(view, iconState);
        }
        int indexOfChild = indexOfChild(view);
        if (indexOfChild < getChildCount() - 1 && !isReplacingIcon && this.mIconStates.get(getChildAt(indexOfChild + 1)).iconAppearAmount > 0.0f) {
            int i = this.mAddAnimationStartIndex;
            if (i < 0) {
                this.mAddAnimationStartIndex = indexOfChild;
            } else {
                this.mAddAnimationStartIndex = Math.min(i, indexOfChild);
            }
        }
        if (view instanceof StatusBarIconView) {
            ((StatusBarIconView) view).setDozing(this.mDozing, false, 0);
        }
    }

    private boolean isReplacingIcon(View view) {
        if (this.mReplacingIcons == null || !(view instanceof StatusBarIconView)) {
            return false;
        }
        StatusBarIconView statusBarIconView = (StatusBarIconView) view;
        Icon sourceIcon = statusBarIconView.getSourceIcon();
        ArrayList<StatusBarIcon> arrayList = this.mReplacingIcons.get(statusBarIconView.getNotification().getGroupKey());
        if (arrayList == null || !sourceIcon.sameAs(arrayList.get(0).icon)) {
            return false;
        }
        return true;
    }

    @Override // android.view.ViewGroup
    public void onViewRemoved(View view) {
        super.onViewRemoved(view);
        if (view instanceof StatusBarIconView) {
            boolean isReplacingIcon = isReplacingIcon(view);
            StatusBarIconView statusBarIconView = (StatusBarIconView) view;
            if (areAnimationsEnabled(statusBarIconView) && statusBarIconView.getVisibleState() != 2 && view.getVisibility() == 0 && isReplacingIcon) {
                int findFirstViewIndexAfter = findFirstViewIndexAfter(statusBarIconView.getTranslationX());
                int i = this.mAddAnimationStartIndex;
                if (i < 0) {
                    this.mAddAnimationStartIndex = findFirstViewIndexAfter;
                } else {
                    this.mAddAnimationStartIndex = Math.min(i, findFirstViewIndexAfter);
                }
            }
            if (!this.mChangingViewPositions) {
                this.mIconStates.remove(view);
                if (areAnimationsEnabled(statusBarIconView) && !isReplacingIcon) {
                    addTransientView(statusBarIconView, 0);
                    boolean z = true;
                    boolean z2 = view == this.mIsolatedIcon;
                    boolean onKeyguard = onKeyguard();
                    if (this.mRemoveWithoutAnimation && onKeyguard) {
                        z = false;
                    }
                    Log.d("NotificationIconContainer", "onViewRemoved: needsAnimation= " + z + ", onKeyguard= " + onKeyguard + ", mRemoveWithoutAnimation= " + this.mRemoveWithoutAnimation);
                    statusBarIconView.setVisibleState(2, z, new Runnable(statusBarIconView) { // from class: com.android.systemui.statusbar.phone.-$$Lambda$NotificationIconContainer$sYOppFQ4vSNRi0SYdFbv716CxNY
                        public final /* synthetic */ StatusBarIconView f$1;

                        {
                            this.f$1 = r2;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            NotificationIconContainer.this.lambda$onViewRemoved$0$NotificationIconContainer(this.f$1);
                        }
                    }, z2 ? 110 : 0);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onViewRemoved$0 */
    public /* synthetic */ void lambda$onViewRemoved$0$NotificationIconContainer(StatusBarIconView statusBarIconView) {
        removeTransientView(statusBarIconView);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean areAnimationsEnabled(StatusBarIconView statusBarIconView) {
        return this.mAnimationsEnabled || statusBarIconView == this.mIsolatedIcon;
    }

    private int findFirstViewIndexAfter(float f) {
        for (int i = 0; i < getChildCount(); i++) {
            if (getChildAt(i).getTranslationX() > f) {
                return i;
            }
        }
        return getChildCount();
    }

    public void resetViewStates() {
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            IconState iconState = this.mIconStates.get(childAt);
            iconState.initFrom(childAt);
            StatusBarIconView statusBarIconView = this.mIsolatedIcon;
            iconState.alpha = (statusBarIconView == null || childAt == statusBarIconView) ? 1.0f : 0.0f;
            iconState.hidden = false;
        }
    }

    public void calculateIconTranslations() {
        int i;
        IconState iconState;
        float f;
        float actualPaddingStart = getActualPaddingStart();
        int childCount = getChildCount();
        if (this.mOnLockScreen) {
            i = 5;
        } else {
            i = this.mIsStaticLayout ? 40 : childCount;
        }
        float layoutEnd = getLayoutEnd();
        float maxOverflowStart = getMaxOverflowStart();
        float f2 = 0.0f;
        this.mVisualOverflowStart = 0.0f;
        this.mFirstVisibleIconState = null;
        int i2 = this.mSpeedBumpIndex;
        int i3 = -1;
        boolean z = i2 != -1 && i2 < getChildCount();
        int maxDots = getMaxDots();
        int i4 = -1;
        int i5 = 0;
        while (i5 < childCount) {
            View childAt = getChildAt(i5);
            IconState iconState2 = this.mIconStates.get(childAt);
            if (iconState2.iconAppearAmount == 1.0f) {
                iconState2.xTranslation = actualPaddingStart;
            }
            if (this.mFirstVisibleIconState == null) {
                this.mFirstVisibleIconState = iconState2;
            }
            int i6 = this.mSpeedBumpIndex;
            boolean z2 = (i6 != i3 && i5 >= i6 && iconState2.iconAppearAmount > f2) || i5 >= i;
            int i7 = childCount - 1;
            if (maxDots <= 0) {
                i7 = childCount;
            }
            boolean z3 = i5 == i7;
            float iconScaleIncreased = (!this.mOnLockScreen || !(childAt instanceof StatusBarIconView)) ? 1.0f : ((StatusBarIconView) childAt).getIconScaleIncreased();
            if (this.mOpenedAmount != f2) {
                z3 = z3 && !z && !z2;
            }
            iconState2.visibleState = 0;
            if (z3) {
                f = layoutEnd - ((float) this.mIconSize);
            } else {
                f = maxOverflowStart - ((float) this.mIconSize);
            }
            boolean z4 = actualPaddingStart > f;
            if (i4 == -1 && (z2 || z4)) {
                i4 = (!z3 || z2) ? i5 : i5 - 1;
                this.mVisualOverflowStart = layoutEnd - ((float) this.mOverflowWidth);
                if (z2 || this.mIsStaticLayout) {
                    this.mVisualOverflowStart = Math.min(actualPaddingStart, this.mVisualOverflowStart);
                }
            }
            actualPaddingStart = actualPaddingStart + (iconState2.iconAppearAmount * ((float) childAt.getWidth()) * iconScaleIncreased) + ((float) this.mIconPadding);
            i5++;
            f2 = 0.0f;
            i3 = -1;
        }
        this.mNumDots = 0;
        if (i4 != -1) {
            actualPaddingStart = this.mVisualOverflowStart;
            for (int i8 = i4; i8 < childCount; i8++) {
                IconState iconState3 = this.mIconStates.get(getChildAt(i8));
                int i9 = this.mStaticDotDiameter + this.mDotPadding;
                iconState3.xTranslation = actualPaddingStart;
                int i10 = this.mNumDots;
                if (i10 < maxDots) {
                    if (i10 != 0 || iconState3.iconAppearAmount >= 0.8f) {
                        iconState3.visibleState = 1;
                        this.mNumDots++;
                    } else {
                        iconState3.visibleState = 0;
                    }
                    if (this.mNumDots == maxDots) {
                        i9 *= maxDots;
                    }
                    actualPaddingStart += ((float) i9) * iconState3.iconAppearAmount;
                    this.mLastVisibleIconState = iconState3;
                } else {
                    iconState3.visibleState = 2;
                }
            }
        } else if (childCount > 0) {
            this.mLastVisibleIconState = this.mIconStates.get(getChildAt(childCount - 1));
            this.mFirstVisibleIconState = this.mIconStates.get(getChildAt(0));
        }
        if (this.mOnLockScreen && actualPaddingStart < getLayoutEnd()) {
            IconState iconState4 = this.mFirstVisibleIconState;
            float f3 = iconState4 == null ? 0.0f : iconState4.xTranslation;
            IconState iconState5 = this.mLastVisibleIconState;
            float layoutEnd2 = ((getLayoutEnd() - getActualPaddingStart()) - (iconState5 != null ? Math.min((float) getWidth(), iconState5.xTranslation + ((float) this.mIconSize)) - f3 : 0.0f)) / 2.0f;
            if (i4 != -1) {
                layoutEnd2 = (((getLayoutEnd() - this.mVisualOverflowStart) / 2.0f) + layoutEnd2) / 2.0f;
            }
            for (int i11 = 0; i11 < childCount; i11++) {
                this.mIconStates.get(getChildAt(i11)).xTranslation += layoutEnd2;
            }
        }
        if (isLayoutRtl()) {
            for (int i12 = 0; i12 < childCount; i12++) {
                View childAt2 = getChildAt(i12);
                IconState iconState6 = this.mIconStates.get(childAt2);
                iconState6.xTranslation = (((float) getWidth()) - iconState6.xTranslation) - ((float) childAt2.getWidth());
            }
        }
        StatusBarIconView statusBarIconView = this.mIsolatedIcon;
        if (!(statusBarIconView == null || (iconState = this.mIconStates.get(statusBarIconView)) == null)) {
            iconState.xTranslation = ((float) (this.mIsolatedIconLocation.left - this.mAbsolutePosition[0])) - (((1.0f - this.mIsolatedIcon.getIconScale()) * ((float) this.mIsolatedIcon.getWidth())) / 2.0f);
            iconState.visibleState = 0;
        }
    }

    private float getLayoutEnd() {
        return ((float) getActualWidth()) - getActualPaddingEnd();
    }

    private float getActualPaddingEnd() {
        float f = this.mActualPaddingEnd;
        return f == -2.14748365E9f ? (float) getPaddingEnd() : f;
    }

    public float getActualPaddingStart() {
        float f = this.mActualPaddingStart;
        return f == -2.14748365E9f ? (float) getPaddingStart() : f;
    }

    public void setIsStaticLayout(boolean z) {
        this.mIsStaticLayout = z;
    }

    public void setActualLayoutWidth(int i) {
        this.mActualLayoutWidth = i;
    }

    public void setActualPaddingEnd(float f) {
        this.mActualPaddingEnd = f;
    }

    public void setActualPaddingStart(float f) {
        this.mActualPaddingStart = f;
    }

    public int getActualWidth() {
        int i = this.mActualLayoutWidth;
        return i == Integer.MIN_VALUE ? getWidth() : i;
    }

    public int getFinalTranslationX() {
        float f;
        if (this.mLastVisibleIconState == null) {
            return 0;
        }
        if (isLayoutRtl()) {
            f = ((float) getWidth()) - this.mLastVisibleIconState.xTranslation;
        } else {
            f = this.mLastVisibleIconState.xTranslation + ((float) this.mIconSize);
        }
        return Math.min(getWidth(), (int) f);
    }

    private float getMaxOverflowStart() {
        return getLayoutEnd() - ((float) this.mOverflowWidth);
    }

    public void setChangingViewPositions(boolean z) {
        this.mChangingViewPositions = z;
    }

    public void setDozing(boolean z, boolean z2, long j) {
        this.mDozing = z;
        this.mDisallowNextAnimation |= !z2;
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            if (childAt instanceof StatusBarIconView) {
                ((StatusBarIconView) childAt).setDozing(z, z2, j);
            }
        }
    }

    public IconState getIconState(StatusBarIconView statusBarIconView) {
        return this.mIconStates.get(statusBarIconView);
    }

    public void setSpeedBumpIndex(int i) {
        this.mSpeedBumpIndex = i;
    }

    public void setOpenedAmount(float f) {
        this.mOpenedAmount = f;
    }

    public boolean hasOverflow() {
        return this.mNumDots > 0;
    }

    public boolean hasPartialOverflow() {
        int i = this.mNumDots;
        return i > 0 && i < getMaxDots();
    }

    public int getPartialOverflowExtraPadding() {
        if (!hasPartialOverflow()) {
            return 0;
        }
        int maxDots = (getMaxDots() - this.mNumDots) * (this.mStaticDotDiameter + this.mDotPadding);
        return getFinalTranslationX() + maxDots > getWidth() ? getWidth() - getFinalTranslationX() : maxDots;
    }

    public int getNoOverflowExtraPadding() {
        if (this.mNumDots != 0) {
            return 0;
        }
        int i = this.mOverflowWidth;
        return getFinalTranslationX() + i > getWidth() ? getWidth() - getFinalTranslationX() : i;
    }

    public void setAnimationsEnabled(boolean z) {
        if (!z && this.mAnimationsEnabled) {
            for (int i = 0; i < getChildCount(); i++) {
                View childAt = getChildAt(i);
                IconState iconState = this.mIconStates.get(childAt);
                if (iconState != null) {
                    iconState.cancelAnimations(childAt);
                    iconState.applyToView(childAt);
                }
            }
        }
        this.mAnimationsEnabled = z;
    }

    public void setReplacingIcons(ArrayMap<String, ArrayList<StatusBarIcon>> arrayMap) {
        this.mReplacingIcons = arrayMap;
    }

    public void showIconIsolated(StatusBarIconView statusBarIconView, boolean z) {
        if (z) {
            this.mIsolatedIconForAnimation = statusBarIconView != null ? statusBarIconView : this.mIsolatedIcon;
        }
        this.mIsolatedIcon = statusBarIconView;
        updateState();
    }

    public void setIsolatedIconLocation(Rect rect, boolean z) {
        this.mIsolatedIconLocation = rect;
        if (z) {
            updateState();
        }
    }

    public void setOnLockScreen(boolean z) {
        this.mOnLockScreen = z;
    }

    public class IconState extends ViewState {
        public float clampedAppearAmount = 1.0f;
        public int customTransformHeight = Integer.MIN_VALUE;
        public float iconAppearAmount = 1.0f;
        public int iconColor = 0;
        public boolean isLastExpandIcon;
        public boolean justAdded = true;
        private boolean justReplaced;
        private final Consumer<Property> mCannedAnimationEndListener;
        private final View mView;
        public boolean needsCannedAnimation;
        public boolean noAnimations;
        public boolean translateContent;
        public boolean useFullTransitionAmount;
        public boolean useLinearTransitionAmount;
        public int visibleState;

        public IconState(View view) {
            this.mView = view;
            this.mCannedAnimationEndListener = new Consumer() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$NotificationIconContainer$IconState$017TXxTv7ZZuJUNh-HxPGZnMwVo
                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    NotificationIconContainer.IconState.this.lambda$new$0$NotificationIconContainer$IconState((Property) obj);
                }
            };
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$0 */
        public /* synthetic */ void lambda$new$0$NotificationIconContainer$IconState(Property property) {
            if (property == View.TRANSLATION_Y && this.iconAppearAmount == 0.0f && this.mView.getVisibility() == 0) {
                this.mView.setVisibility(4);
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:37:0x0082  */
        /* JADX WARNING: Removed duplicated region for block: B:57:0x012f  */
        @Override // com.android.systemui.statusbar.notification.stack.ViewState
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void applyToView(android.view.View r13) {
            /*
            // Method dump skipped, instructions count: 399
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.NotificationIconContainer.IconState.applyToView(android.view.View):void");
        }

        public boolean hasCustomTransformHeight() {
            return this.isLastExpandIcon && this.customTransformHeight != Integer.MIN_VALUE;
        }

        @Override // com.android.systemui.statusbar.notification.stack.ViewState
        public void initFrom(View view) {
            super.initFrom(view);
            if (view instanceof StatusBarIconView) {
                this.iconColor = ((StatusBarIconView) view).getStaticDrawableColor();
            }
        }
    }
}
