package com.oneplus.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.BoostFramework;
import android.util.Log;
import android.util.OpFeatures;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.PathInterpolator;
import com.android.internal.util.LatencyTracker;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0008R$id;
import com.android.systemui.Dependency;
import com.android.systemui.doze.DozeLog;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.statusbar.FlingAnimationUtils;
import com.android.systemui.statusbar.SysuiStatusBarStateController;
import com.android.systemui.statusbar.VibratorHelper;
import com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout;
import com.android.systemui.statusbar.phone.KeyguardClockPositionAlgorithm;
import com.android.systemui.statusbar.phone.NotificationPanelViewController;
import com.android.systemui.statusbar.phone.PanelView;
import com.android.systemui.statusbar.phone.PanelViewController;
import com.android.systemui.statusbar.phone.ScrimState;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.phone.StatusBarTouchableRegionManager;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.oneplus.plugin.OpLsState;
import com.oneplus.systemui.statusbar.phone.OpExpandButton;
import com.oneplus.systemui.statusbar.phone.OpStatusBar;
import com.oneplus.util.OpReflectionUtils;
public class OpPanelViewController extends PanelViewController implements GestureDetector.OnDoubleTapListener {
    private Context mContext;
    protected Handler mHandler;
    private int mHighlightHintVisualWidth;
    protected int mHighlightHintVisualX;
    private boolean mHightHintIntercepting;
    protected OpExpandButton mOpExpandButton;

    public abstract KeyguardClockPositionAlgorithm getKeyguardClockPositionAlgorithm();

    public abstract BoostFramework getPerf();

    @Override // android.view.GestureDetector.OnDoubleTapListener
    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
        return false;
    }

    @Override // android.view.GestureDetector.OnDoubleTapListener
    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        return false;
    }

    public OpPanelViewController(PanelView panelView, FalsingManager falsingManager, DozeLog dozeLog, KeyguardStateController keyguardStateController, SysuiStatusBarStateController sysuiStatusBarStateController, VibratorHelper vibratorHelper, LatencyTracker latencyTracker, FlingAnimationUtils.Builder builder, StatusBarTouchableRegionManager statusBarTouchableRegionManager) {
        super(panelView, falsingManager, dozeLog, keyguardStateController, sysuiStatusBarStateController, vibratorHelper, latencyTracker, builder, statusBarTouchableRegionManager);
        this.mContext = panelView.getContext();
        GestureDetector gestureDetector = new GestureDetector(this.mContext, new GestureDetector.SimpleOnGestureListener());
        this.mGestureDetector = gestureDetector;
        gestureDetector.setOnDoubleTapListener(this);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public void loadDimens() {
        super.loadDimens();
        if (((OpHighlightHintController) Dependency.get(OpHighlightHintController.class)).showOvalLayout()) {
            this.mHighlightHintVisualWidth = this.mResources.getDimensionPixelSize(C0005R$dimen.highlight_hint_icon_size_notch) + (this.mResources.getDimensionPixelSize(C0005R$dimen.highlight_hint_padding) * 2);
        }
    }

    public boolean onHightlightHintIntercept(MotionEvent motionEvent) {
        int findPointerIndex = motionEvent.findPointerIndex(getTrackingPointer());
        if (findPointerIndex < 0) {
            setTrackingPointer(motionEvent.getPointerId(0));
            findPointerIndex = 0;
        }
        float x = motionEvent.getX(findPointerIndex);
        float y = motionEvent.getY(findPointerIndex);
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked != 0) {
            if (actionMasked == 1) {
                Log.d(PanelViewController.TAG, "onHightlightHintIntercept / ACTION_UP / x:" + x + ", y:" + y + ", mHighlightHintVisualWidth:" + this.mHighlightHintVisualWidth + ", mHighlightHintVisualX:" + this.mHighlightHintVisualX + ", mOrientation:" + this.mOrientation);
                if (this.mHightHintIntercepting && shouldHightHintIntercept(x, y)) {
                    if (((OpHighlightHintController) Dependency.get(OpHighlightHintController.class)).isCarModeHighlightHintSHow()) {
                        ((OpHighlightHintController) Dependency.get(OpHighlightHintController.class)).launchCarModeAp(this.mContext);
                    } else {
                        this.mStatusBar.launchHighlightHintAp();
                    }
                }
                this.mHightHintIntercepting = false;
            } else if (actionMasked != 2) {
                if (actionMasked == 3) {
                    this.mHightHintIntercepting = false;
                }
            } else if (!shouldHightHintIntercept(x, y)) {
                this.mHightHintIntercepting = false;
            }
        } else if (shouldHightHintIntercept(x, y)) {
            this.mHightHintIntercepting = true;
        } else {
            this.mHightHintIntercepting = false;
        }
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0084, code lost:
        if (r10 <= r9) goto L_0x0086;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0099, code lost:
        if (r10 <= ((float) r1.getRight())) goto L_0x0086;
     */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x009e A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:40:? A[ADDED_TO_REGION, RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean shouldHightHintIntercept(float r10, float r11) {
        /*
            r9 = this;
            java.lang.Class<com.oneplus.systemui.statusbar.phone.OpHighlightHintController> r0 = com.oneplus.systemui.statusbar.phone.OpHighlightHintController.class
            com.android.systemui.statusbar.phone.StatusBar r1 = r9.mStatusBar
            android.view.View r1 = r1.getStatusBarView()
            int r2 = r1.getTop()
            float r2 = (float) r2
            int r2 = (r11 > r2 ? 1 : (r11 == r2 ? 0 : -1))
            r3 = 0
            r4 = 1
            if (r2 < 0) goto L_0x001e
            int r2 = r1.getBottom()
            float r2 = (float) r2
            int r11 = (r11 > r2 ? 1 : (r11 == r2 ? 0 : -1))
            if (r11 > 0) goto L_0x001e
            r11 = r4
            goto L_0x001f
        L_0x001e:
            r11 = r3
        L_0x001f:
            java.lang.Object r2 = com.android.systemui.Dependency.get(r0)
            com.oneplus.systemui.statusbar.phone.OpHighlightHintController r2 = (com.oneplus.systemui.statusbar.phone.OpHighlightHintController) r2
            boolean r2 = r2.showOvalLayout()
            if (r2 == 0) goto L_0x008a
            java.lang.Object r0 = com.android.systemui.Dependency.get(r0)
            com.oneplus.systemui.statusbar.phone.OpHighlightHintController r0 = (com.oneplus.systemui.statusbar.phone.OpHighlightHintController) r0
            boolean r0 = r0.isCarModeHighlightHintSHow()
            if (r0 != 0) goto L_0x008a
            android.view.ViewGroup r0 = r9.getView()
            boolean r0 = r0.isLayoutRtl()
            int r2 = r9.mOrientation
            if (r2 != r4) goto L_0x0045
            r2 = r4
            goto L_0x0046
        L_0x0045:
            r2 = r3
        L_0x0046:
            int r5 = r9.mHighlightHintVisualWidth
            float r5 = (float) r5
            r6 = 1073741824(0x40000000, float:2.0)
            float r5 = r5 / r6
            int r5 = (int) r5
            int r7 = r9.mHighlightHintVisualX
            int r7 = r7 - r5
            float r7 = (float) r7
            float r8 = r1.getX()
            int r8 = (r7 > r8 ? 1 : (r7 == r8 ? 0 : -1))
            if (r8 >= 0) goto L_0x005d
            float r7 = r1.getX()
        L_0x005d:
            int r8 = r9.mHighlightHintVisualX
            int r9 = r9.mHighlightHintVisualWidth
            int r8 = r8 + r9
            int r8 = r8 + r5
            int r9 = r1.getRight()
            float r9 = (float) r9
            float r9 = r9 / r6
            if (r2 == 0) goto L_0x0073
            if (r0 == 0) goto L_0x006f
            r7 = r9
            goto L_0x0073
        L_0x006f:
            float r7 = r1.getX()
        L_0x0073:
            if (r2 == 0) goto L_0x007d
            if (r0 == 0) goto L_0x007e
            int r9 = r1.getRight()
            float r9 = (float) r9
            goto L_0x007e
        L_0x007d:
            float r9 = (float) r8
        L_0x007e:
            int r0 = (r10 > r7 ? 1 : (r10 == r7 ? 0 : -1))
            if (r0 < 0) goto L_0x0088
            int r9 = (r10 > r9 ? 1 : (r10 == r9 ? 0 : -1))
            if (r9 > 0) goto L_0x0088
        L_0x0086:
            r9 = r4
            goto L_0x009c
        L_0x0088:
            r9 = r3
            goto L_0x009c
        L_0x008a:
            float r9 = r1.getX()
            int r9 = (r10 > r9 ? 1 : (r10 == r9 ? 0 : -1))
            if (r9 < 0) goto L_0x0088
            int r9 = r1.getRight()
            float r9 = (float) r9
            int r9 = (r10 > r9 ? 1 : (r10 == r9 ? 0 : -1))
            if (r9 > 0) goto L_0x0088
            goto L_0x0086
        L_0x009c:
            if (r9 == 0) goto L_0x00a1
            if (r11 == 0) goto L_0x00a1
            r3 = r4
        L_0x00a1:
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.systemui.statusbar.phone.OpPanelViewController.shouldHightHintIntercept(float, float):boolean");
    }

    public void setUpHighlightHintInfo() {
        Handler handler;
        OpHighlightHintController opHighlightHintController = (OpHighlightHintController) Dependency.get(OpHighlightHintController.class);
        if (opHighlightHintController.isHighLightHintShow() && opHighlightHintController.showOvalLayout() && (handler = this.mHandler) != null) {
            handler.postDelayed(new Runnable() { // from class: com.oneplus.systemui.statusbar.phone.-$$Lambda$OpPanelViewController$0l8g0MprKElc4TZRjhyoeov_yqY
                @Override // java.lang.Runnable
                public final void run() {
                    OpPanelViewController.this.run();
                }
            }, 500);
        }
    }

    /* access modifiers changed from: private */
    public void run() {
        StatusBar statusBar = this.mStatusBar;
        if (statusBar != null && statusBar.getOpStatusBarView() != null) {
            View findViewById = this.mStatusBar.getOpStatusBarView().findViewById(C0008R$id.highlight_hint_container);
            int[] iArr = new int[2];
            if (findViewById != null) {
                findViewById.getLocationOnScreen(iArr);
            }
            String str = PanelViewController.TAG;
            Log.i(str, "HighlightHintInfo target[0]:" + iArr[0]);
            this.mHighlightHintVisualX = iArr[0];
        }
    }

    private void setTrackingPointer(int i) {
        OpReflectionUtils.setValue(PanelViewController.class, this, "mTrackingPointer", Integer.valueOf(i));
    }

    private int getTrackingPointer() {
        return ((Integer) OpReflectionUtils.getValue(PanelViewController.class, this, "mTrackingPointer")).intValue();
    }

    private NotificationStackScrollLayout getNotificationStackScroller() {
        return (NotificationStackScrollLayout) OpReflectionUtils.getValue(NotificationPanelViewController.class, this, "mNotificationStackScroller");
    }

    private boolean isFingerprintAuthenticating() {
        return OpLsState.getInstance().getBiometricUnlockController().isFingerprintAuthenticating();
    }

    @Override // android.view.GestureDetector.OnDoubleTapListener
    public boolean onDoubleTap(MotionEvent motionEvent) {
        if (getNotificationStackScroller() == null || getNotificationStackScroller().getVisibility() != 0 || !this.mStatusBar.isFalsingThresholdNeeded() || this.mStatusBar.isBouncerShowing() || isFingerprintAuthenticating() || OpLsState.getInstance().getPreventModeCtrl().isPreventModeActive()) {
            return false;
        }
        Log.d(PanelViewController.TAG, "onDoubleTap to sleep");
        ((PowerManager) this.mContext.getSystemService("power")).goToSleep(SystemClock.uptimeMillis());
        return true;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public void opFlingToHeightAnimatorForBiometricUnlock() {
        ValueAnimator createHeightAnimatorForBiometricUnlock = createHeightAnimatorForBiometricUnlock();
        setAnimator(createHeightAnimatorForBiometricUnlock);
        createHeightAnimatorForBiometricUnlock.start();
    }

    /* access modifiers changed from: protected */
    public ValueAnimator createHeightAnimatorForBiometricUnlock() {
        float opGetMaxClockY = (float) getKeyguardClockPositionAlgorithm().opGetMaxClockY();
        float opGetClockY = (float) getKeyguardClockPositionAlgorithm().opGetClockY();
        final float expandedHeight = getExpandedHeight() - (opGetClockY - opGetMaxClockY);
        final float expandedHeight2 = getExpandedHeight() - (opGetClockY - (opGetMaxClockY - 100.0f));
        ValueAnimator ofFloat = ValueAnimator.ofFloat(expandedHeight, expandedHeight2);
        PathInterpolator pathInterpolator = new PathInterpolator(0.6f, 0.0f, 0.6f, 1.0f);
        boolean isPreventModeActive = OpLsState.getInstance().getPreventModeCtrl().isPreventModeActive();
        boolean isFacelockUnlocking = ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isFacelockUnlocking();
        if (isPreventModeActive) {
            ofFloat.setDuration(0L);
        } else {
            int i = 150;
            if ((!OpFeatures.isSupport(new int[]{91}) || !isFacelockUnlocking) && !((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isFingerprintAlreadyAuthenticated()) {
                i = 300;
            }
            if (SystemProperties.getBoolean("test.fling.enable", false)) {
                i = SystemProperties.getInt("test.fling.duration", 300);
            }
            ofFloat.setDuration((long) i);
        }
        Log.d(PanelViewController.TAG, "createHeightAnimatorForBiometricUnlock startHeight:" + expandedHeight + " endHeight:" + expandedHeight2 + " isPreventViewShow:" + isPreventModeActive + " isFacelockUnlocking:" + isFacelockUnlocking);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.systemui.statusbar.phone.OpPanelViewController.1
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                float f = expandedHeight2;
                float f2 = 1.0f - ((floatValue - f) / (expandedHeight - f));
                OpPanelViewController.this.getView().setAlpha(1.0f - f2);
                if (f2 == 1.0f) {
                    OpPanelViewController.this.setExpandedHeightInternal(0.0f);
                    return;
                }
                float expandedHeight3 = (OpPanelViewController.this.getExpandedHeight() - ((float) (OpPanelViewController.this.getKeyguardClockPositionAlgorithm().opGetClockY() - OpPanelViewController.this.getKeyguardClockPositionAlgorithm().opGetMaxClockY()))) - expandedHeight;
                int abs = (int) ((Math.abs(expandedHeight3) / expandedHeight) * 100.0f);
                String str = PanelViewController.TAG;
                Log.d(str, "createHeightAnimatorForBiometricUnlock percentage:" + abs + " diff:" + expandedHeight3);
                if (abs > 5) {
                    OpPanelViewController.this.setExpandedHeightInternal(((Float) valueAnimator.getAnimatedValue()).floatValue() + expandedHeight3);
                } else {
                    OpPanelViewController.this.setExpandedHeightInternal(((Float) valueAnimator.getAnimatedValue()).floatValue());
                }
            }
        });
        ofFloat.addListener(new AnimatorListenerAdapter() { // from class: com.oneplus.systemui.statusbar.phone.OpPanelViewController.2
            private boolean mCancelled;

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                Log.d(PanelViewController.TAG, "HeightAnimatorForBiometricUnlock start");
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                if (OpPanelViewController.this.getPerf() != null) {
                    OpPanelViewController.this.getPerf().perfLockRelease();
                }
                this.mCancelled = true;
                Log.d(PanelViewController.TAG, "HeightAnimatorForBiometricUnlock cancel");
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                String str = PanelViewController.TAG;
                Log.d(str, "HeightAnimatorForBiometricUnlock end, Cancelled:" + this.mCancelled);
                OpPanelViewController.this.getView().setAlpha(1.0f);
                if (OpPanelViewController.this.getPerf() != null) {
                    OpPanelViewController.this.getPerf().perfLockRelease();
                }
                OpPanelViewController.this.setAnimator(null);
                if (!this.mCancelled) {
                    OpPanelViewController.this.notifyExpandingFinished();
                }
                OpPanelViewController.this.notifyBarPanelExpansionChanged();
            }
        });
        ofFloat.setInterpolator(pathInterpolator);
        return ofFloat;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public void initExpandButton() {
        if (this.mOpExpandButton == null) {
            OpExpandButton opExpandButton = new OpExpandButton(this.mContext);
            this.mOpExpandButton = opExpandButton;
            opExpandButton.setmOnExpandButtonListener(new OpExpandButton.OnExpandButtonListener() { // from class: com.oneplus.systemui.statusbar.phone.OpPanelViewController.3
                @Override // com.oneplus.systemui.statusbar.phone.OpExpandButton.OnExpandButtonListener
                public void onExpandButtonClick() {
                    Log.d(PanelViewController.TAG, "onExpandButtonClick expand");
                    OpPanelViewController.this.expand(true);
                }

                @Override // com.oneplus.systemui.statusbar.phone.OpExpandButton.OnExpandButtonListener
                public void onOutSideClick(float f) {
                    OpExpandButton opExpandButton2;
                    String str = PanelViewController.TAG;
                    Log.d(str, "onOutSideClick position:" + f);
                    if (f == 0.0f && (opExpandButton2 = OpPanelViewController.this.mOpExpandButton) != null) {
                        opExpandButton2.dismiss();
                    }
                }
            });
        }
        StatusBar statusBar = this.mStatusBar;
        if (statusBar != null && statusBar.getmStatusBarCollapseListener() == null) {
            this.mStatusBar.setmStatusBarCollapseListener(new OpStatusBar.StatusBarCollapseListener() { // from class: com.oneplus.systemui.statusbar.phone.-$$Lambda$OpPanelViewController$ETHyd_pQqafCpVPaRSBqmkI8Ei0
                @Override // com.oneplus.systemui.statusbar.phone.OpStatusBar.StatusBarCollapseListener
                public final void statusBarCollapse() {
                    OpPanelViewController.this.lambda$initExpandButton$0$OpPanelViewController();
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$initExpandButton$0 */
    public /* synthetic */ void lambda$initExpandButton$0$OpPanelViewController() {
        OpExpandButton opExpandButton = this.mOpExpandButton;
        if (opExpandButton != null) {
            opExpandButton.dismiss();
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public void showExpandButton() {
        String str = PanelViewController.TAG;
        Log.d(str, "disable panel expandButton is show:" + this.mOpExpandButton.isShow());
        this.mOpExpandButton.show();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public boolean isWithinGameModeToolBoxRegion() {
        int i;
        Display display = this.mContext.getDisplay();
        if (display == null) {
            return false;
        }
        int rotation = display.getRotation();
        if (rotation == 0 || rotation == 2) {
            i = this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.op_game_mode_toolbox_region_width_port);
        } else {
            i = this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.op_game_mode_toolbox_region_width_land);
        }
        String str = PanelViewController.TAG;
        Log.d(str, "toolboxRegionWidth:" + i);
        if (this.mTouchActionDownY > ((float) this.mContext.getResources().getDimensionPixelSize(17105481))) {
            return false;
        }
        float f = this.mTouchActionDownX;
        if (f <= ((float) i) || f >= ((float) (display.getWidth() - i))) {
            return true;
        }
        return false;
    }

    public void updateScrimState(ScrimState scrimState) {
        NotificationStackScrollLayout notificationStackScroller = getNotificationStackScroller();
        if (notificationStackScroller != null) {
            notificationStackScroller.updateScrimState(scrimState);
        }
    }

    public void stopTrackingAfterUnlock() {
        if (isTracking()) {
            onTrackingStopped(true);
        }
    }
}
