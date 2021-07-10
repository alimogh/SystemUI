package com.android.systemui.screenshot;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.ActivityOptions;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Insets;
import android.graphics.Outline;
import android.graphics.Rect;
import android.graphics.Region;
import android.media.MediaActionSound;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.MathUtils;
import android.util.Slog;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.internal.logging.UiEventLogger;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0011R$layout;
import com.android.systemui.C0015R$string;
import com.android.systemui.screenshot.GlobalScreenshot;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.QuickStepContract;
import com.android.systemui.statusbar.phone.StatusBar;
import dagger.Lazy;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
public class GlobalScreenshot implements ViewTreeObserver.OnComputeInternalInsetsListener {
    private final Interpolator mAccelerateInterpolator = new AccelerateInterpolator();
    private HorizontalScrollView mActionsContainer;
    private ImageView mActionsContainerBackground;
    private ImageView mBackgroundProtection;
    private MediaActionSound mCameraSound;
    private final Context mContext;
    private Animator mDismissAnimation;
    private FrameLayout mDismissButton;
    private float mDismissDeltaY;
    private final Display mDisplay;
    private final DisplayMetrics mDisplayMetrics;
    private int mLeftInset;
    private int mNavMode;
    private Runnable mOnCompleteRunnable;
    private int mRightInset;
    private ImageView mScreenshotAnimatedView;
    private final Handler mScreenshotHandler = new Handler(Looper.getMainLooper()) { // from class: com.android.systemui.screenshot.GlobalScreenshot.1
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            if (message.what == 2) {
                GlobalScreenshot.this.mUiEventLogger.log(ScreenshotEvent.SCREENSHOT_INTERACTION_TIMEOUT);
                GlobalScreenshot.this.dismissScreenshot("timeout", false);
                GlobalScreenshot.this.mOnCompleteRunnable.run();
            }
        }
    };
    private View mScreenshotLayout;
    private ImageView mScreenshotPreview;
    private ScreenshotSelectorView mScreenshotSelectorView;
    private final UiEventLogger mUiEventLogger;
    private final WindowManager.LayoutParams mWindowLayoutParams;
    private final WindowManager mWindowManager;

    public GlobalScreenshot(Context context, Resources resources, ScreenshotNotificationsController screenshotNotificationsController, UiEventLogger uiEventLogger) {
        this.mContext = context;
        this.mUiEventLogger = uiEventLogger;
        reloadAssets();
        Configuration configuration = this.mContext.getResources().getConfiguration();
        configuration.isNightModeActive();
        configuration.getLayoutDirection();
        int i = configuration.orientation;
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-1, -1, 0, 0, 2036, 918816, -3);
        this.mWindowLayoutParams = layoutParams;
        layoutParams.setTitle("ScreenshotAnimation");
        WindowManager.LayoutParams layoutParams2 = this.mWindowLayoutParams;
        layoutParams2.layoutInDisplayCutoutMode = 3;
        layoutParams2.setFitInsetsTypes(0);
        WindowManager windowManager = (WindowManager) context.getSystemService("window");
        this.mWindowManager = windowManager;
        this.mDisplay = windowManager.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.mDisplayMetrics = displayMetrics;
        this.mDisplay.getRealMetrics(displayMetrics);
        resources.getDimensionPixelSize(C0005R$dimen.global_screenshot_x_scale);
        this.mDismissDeltaY = (float) resources.getDimensionPixelSize(C0005R$dimen.screenshot_dismissal_height_delta);
        AnimationUtils.loadInterpolator(this.mContext, 17563661);
        MediaActionSound mediaActionSound = new MediaActionSound();
        this.mCameraSound = mediaActionSound;
        mediaActionSound.load(0);
    }

    public void onComputeInternalInsets(ViewTreeObserver.InternalInsetsInfo internalInsetsInfo) {
        internalInsetsInfo.setTouchableInsets(3);
        Region region = new Region();
        Rect rect = new Rect();
        this.mScreenshotPreview.getBoundsOnScreen(rect);
        region.op(rect, Region.Op.UNION);
        Rect rect2 = new Rect();
        this.mActionsContainer.getBoundsOnScreen(rect2);
        region.op(rect2, Region.Op.UNION);
        Rect rect3 = new Rect();
        this.mDismissButton.getBoundsOnScreen(rect3);
        region.op(rect3, Region.Op.UNION);
        if (QuickStepContract.isGesturalMode(this.mNavMode)) {
            Rect rect4 = new Rect(0, 0, this.mLeftInset, this.mDisplayMetrics.heightPixels);
            region.op(rect4, Region.Op.UNION);
            DisplayMetrics displayMetrics = this.mDisplayMetrics;
            int i = displayMetrics.widthPixels;
            rect4.set(i - this.mRightInset, 0, i, displayMetrics.heightPixels);
            region.op(rect4, Region.Op.UNION);
        }
        internalInsetsInfo.touchableRegion.set(region);
    }

    private void reloadAssets() {
        View view = this.mScreenshotLayout;
        boolean z = view != null && view.isAttachedToWindow();
        if (z) {
            this.mWindowManager.removeView(this.mScreenshotLayout);
        }
        View inflate = LayoutInflater.from(this.mContext).inflate(C0011R$layout.global_screenshot, (ViewGroup) null);
        this.mScreenshotLayout = inflate;
        inflate.setOnTouchListener(new View.OnTouchListener() { // from class: com.android.systemui.screenshot.-$$Lambda$GlobalScreenshot$cjbBbqRWya3kStc4feynRVu5-_w
            @Override // android.view.View.OnTouchListener
            public final boolean onTouch(View view2, MotionEvent motionEvent) {
                return GlobalScreenshot.this.lambda$reloadAssets$0$GlobalScreenshot(view2, motionEvent);
            }
        });
        this.mScreenshotLayout.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() { // from class: com.android.systemui.screenshot.-$$Lambda$GlobalScreenshot$6btUb3pURbXlvq3U7gZEq6_gft0
            @Override // android.view.View.OnApplyWindowInsetsListener
            public final WindowInsets onApplyWindowInsets(View view2, WindowInsets windowInsets) {
                return GlobalScreenshot.this.lambda$reloadAssets$1$GlobalScreenshot(view2, windowInsets);
            }
        });
        this.mScreenshotLayout.setOnKeyListener(new View.OnKeyListener() { // from class: com.android.systemui.screenshot.GlobalScreenshot.2
            @Override // android.view.View.OnKeyListener
            public boolean onKey(View view2, int i, KeyEvent keyEvent) {
                if (i != 4) {
                    return false;
                }
                GlobalScreenshot.this.dismissScreenshot("back pressed", true);
                return true;
            }
        });
        this.mScreenshotLayout.setFocusableInTouchMode(true);
        this.mScreenshotLayout.requestFocus();
        ImageView imageView = (ImageView) this.mScreenshotLayout.findViewById(C0008R$id.global_screenshot_animated_view);
        this.mScreenshotAnimatedView = imageView;
        imageView.setClipToOutline(true);
        this.mScreenshotAnimatedView.setOutlineProvider(new ViewOutlineProvider(this) { // from class: com.android.systemui.screenshot.GlobalScreenshot.3
            @Override // android.view.ViewOutlineProvider
            public void getOutline(View view2, Outline outline) {
                outline.setRoundRect(new Rect(0, 0, view2.getWidth(), view2.getHeight()), ((float) view2.getWidth()) * 0.05f);
            }
        });
        ImageView imageView2 = (ImageView) this.mScreenshotLayout.findViewById(C0008R$id.global_screenshot_preview);
        this.mScreenshotPreview = imageView2;
        imageView2.setClipToOutline(true);
        this.mScreenshotPreview.setOutlineProvider(new ViewOutlineProvider(this) { // from class: com.android.systemui.screenshot.GlobalScreenshot.4
            @Override // android.view.ViewOutlineProvider
            public void getOutline(View view2, Outline outline) {
                outline.setRoundRect(new Rect(0, 0, view2.getWidth(), view2.getHeight()), ((float) view2.getWidth()) * 0.05f);
            }
        });
        this.mActionsContainerBackground = (ImageView) this.mScreenshotLayout.findViewById(C0008R$id.global_screenshot_actions_container_background);
        this.mActionsContainer = (HorizontalScrollView) this.mScreenshotLayout.findViewById(C0008R$id.global_screenshot_actions_container);
        LinearLayout linearLayout = (LinearLayout) this.mScreenshotLayout.findViewById(C0008R$id.global_screenshot_actions);
        this.mBackgroundProtection = (ImageView) this.mScreenshotLayout.findViewById(C0008R$id.global_screenshot_actions_background);
        FrameLayout frameLayout = (FrameLayout) this.mScreenshotLayout.findViewById(C0008R$id.global_screenshot_dismiss_button);
        this.mDismissButton = frameLayout;
        frameLayout.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.screenshot.-$$Lambda$GlobalScreenshot$ivNcVUrtovF5MBU69iA0tYfbicU
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) {
                GlobalScreenshot.this.lambda$reloadAssets$2$GlobalScreenshot(view2);
            }
        });
        ImageView imageView3 = (ImageView) this.mScreenshotLayout.findViewById(C0008R$id.global_screenshot_flash);
        this.mScreenshotSelectorView = (ScreenshotSelectorView) this.mScreenshotLayout.findViewById(C0008R$id.global_screenshot_selector);
        this.mScreenshotLayout.setFocusable(true);
        this.mScreenshotSelectorView.setFocusable(true);
        this.mScreenshotSelectorView.setFocusableInTouchMode(true);
        this.mScreenshotAnimatedView.setPivotX(0.0f);
        this.mScreenshotAnimatedView.setPivotY(0.0f);
        this.mActionsContainer.setScrollX(0);
        if (z) {
            this.mWindowManager.addView(this.mScreenshotLayout, this.mWindowLayoutParams);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$reloadAssets$0 */
    public /* synthetic */ boolean lambda$reloadAssets$0$GlobalScreenshot(View view, MotionEvent motionEvent) {
        if (motionEvent.getActionMasked() == 4) {
            setWindowFocusable(false);
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$reloadAssets$1 */
    public /* synthetic */ WindowInsets lambda$reloadAssets$1$GlobalScreenshot(View view, WindowInsets windowInsets) {
        if (QuickStepContract.isGesturalMode(this.mNavMode)) {
            Insets insets = windowInsets.getInsets(WindowInsets.Type.systemGestures());
            this.mLeftInset = insets.left;
            this.mRightInset = insets.right;
        } else {
            this.mRightInset = 0;
            this.mLeftInset = 0;
        }
        return this.mScreenshotLayout.onApplyWindowInsets(windowInsets);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$reloadAssets$2 */
    public /* synthetic */ void lambda$reloadAssets$2$GlobalScreenshot(View view) {
        this.mUiEventLogger.log(ScreenshotEvent.SCREENSHOT_EXPLICIT_DISMISSAL);
        dismissScreenshot("dismiss_button", false);
        this.mOnCompleteRunnable.run();
    }

    private void setWindowFocusable(boolean z) {
        if (z) {
            this.mWindowLayoutParams.flags &= -9;
        } else {
            this.mWindowLayoutParams.flags |= 8;
        }
        if (this.mScreenshotLayout.isAttachedToWindow()) {
            this.mWindowManager.updateViewLayout(this.mScreenshotLayout, this.mWindowLayoutParams);
        }
    }

    /* access modifiers changed from: package-private */
    public void stopScreenshot() {
        if (this.mScreenshotSelectorView.getSelectionRect() != null) {
            this.mWindowManager.removeView(this.mScreenshotLayout);
            this.mScreenshotSelectorView.stopSelection();
        }
    }

    /* access modifiers changed from: package-private */
    public void dismissScreenshot(String str, boolean z) {
        Log.v("GlobalScreenshot", "clearing screenshot: " + str);
        this.mScreenshotHandler.removeMessages(2);
        this.mScreenshotLayout.getViewTreeObserver().removeOnComputeInternalInsetsListener(this);
        if (!z) {
            AnimatorSet createScreenshotDismissAnimation = createScreenshotDismissAnimation();
            this.mDismissAnimation = createScreenshotDismissAnimation;
            createScreenshotDismissAnimation.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.screenshot.GlobalScreenshot.8
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    super.onAnimationEnd(animator);
                    GlobalScreenshot.this.clearScreenshot();
                }
            });
            this.mDismissAnimation.start();
            return;
        }
        clearScreenshot();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clearScreenshot() {
        if (this.mScreenshotLayout.isAttachedToWindow()) {
            this.mWindowManager.removeView(this.mScreenshotLayout);
        }
        this.mScreenshotPreview.setImageDrawable(null);
        this.mScreenshotAnimatedView.setImageDrawable(null);
        this.mScreenshotAnimatedView.setVisibility(8);
        this.mActionsContainerBackground.setVisibility(8);
        this.mActionsContainer.setVisibility(8);
        this.mBackgroundProtection.setAlpha(0.0f);
        this.mDismissButton.setVisibility(8);
        this.mScreenshotPreview.setVisibility(8);
        this.mScreenshotPreview.setLayerType(0, null);
        this.mScreenshotPreview.setContentDescription(this.mContext.getResources().getString(C0015R$string.screenshot_preview_description));
        this.mScreenshotLayout.setAlpha(1.0f);
        this.mDismissButton.setTranslationY(0.0f);
        this.mActionsContainer.setTranslationY(0.0f);
        this.mActionsContainerBackground.setTranslationY(0.0f);
        this.mScreenshotPreview.setTranslationY(0.0f);
    }

    private AnimatorSet createScreenshotDismissAnimation() {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        ofFloat.setStartDelay(50);
        ofFloat.setDuration(183L);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.screenshot.-$$Lambda$GlobalScreenshot$lwSCWVmpTO3-JMK1heDr17u172Q
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                GlobalScreenshot.this.lambda$createScreenshotDismissAnimation$16$GlobalScreenshot(valueAnimator);
            }
        });
        ValueAnimator ofFloat2 = ValueAnimator.ofFloat(0.0f, 1.0f);
        ofFloat2.setInterpolator(this.mAccelerateInterpolator);
        ofFloat2.setDuration(350L);
        ofFloat2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this.mScreenshotPreview.getTranslationY(), this.mDismissButton.getTranslationY()) { // from class: com.android.systemui.screenshot.-$$Lambda$GlobalScreenshot$2_tLLQ8ajKLz2LczKwL5qBWTPFQ
            public final /* synthetic */ float f$1;
            public final /* synthetic */ float f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                GlobalScreenshot.this.lambda$createScreenshotDismissAnimation$17$GlobalScreenshot(this.f$1, this.f$2, valueAnimator);
            }
        });
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(ofFloat2).with(ofFloat);
        return animatorSet;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$createScreenshotDismissAnimation$16 */
    public /* synthetic */ void lambda$createScreenshotDismissAnimation$16$GlobalScreenshot(ValueAnimator valueAnimator) {
        this.mScreenshotLayout.setAlpha(1.0f - valueAnimator.getAnimatedFraction());
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$createScreenshotDismissAnimation$17 */
    public /* synthetic */ void lambda$createScreenshotDismissAnimation$17$GlobalScreenshot(float f, float f2, ValueAnimator valueAnimator) {
        float lerp = MathUtils.lerp(0.0f, this.mDismissDeltaY, valueAnimator.getAnimatedFraction());
        this.mScreenshotPreview.setTranslationY(f + lerp);
        this.mDismissButton.setTranslationY(f2 + lerp);
        this.mActionsContainer.setTranslationY(lerp);
        this.mActionsContainerBackground.setTranslationY(lerp);
    }

    public static class ActionProxyReceiver extends BroadcastReceiver {
        private final StatusBar mStatusBar;

        public ActionProxyReceiver(Optional<Lazy<StatusBar>> optional) {
            StatusBar statusBar = null;
            Lazy<StatusBar> orElse = optional.orElse(null);
            this.mStatusBar = orElse != null ? orElse.get() : statusBar;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            $$Lambda$GlobalScreenshot$ActionProxyReceiver$tBhjeKzNYNKU1TanWTPaMXUfmOc r1 = new Runnable(intent, context) { // from class: com.android.systemui.screenshot.-$$Lambda$GlobalScreenshot$ActionProxyReceiver$tBhjeKzNYNKU1TanWTPaMXUfmOc
                public final /* synthetic */ Intent f$0;
                public final /* synthetic */ Context f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    GlobalScreenshot.ActionProxyReceiver.lambda$onReceive$0(this.f$0, this.f$1);
                }
            };
            StatusBar statusBar = this.mStatusBar;
            if (statusBar != null) {
                statusBar.executeRunnableDismissingKeyguard(r1, null, true, true, true);
            } else {
                r1.run();
            }
            if (intent.getBooleanExtra("android:smart_actions_enabled", false)) {
                ScreenshotSmartActions.notifyScreenshotAction(context, intent.getStringExtra("android:screenshot_id"), "android.intent.action.EDIT".equals(intent.getAction()) ? "Edit" : "Share", false);
            }
        }

        static /* synthetic */ void lambda$onReceive$0(Intent intent, Context context) {
            try {
                ActivityManagerWrapper.getInstance().closeSystemWindows("screenshot").get(3000, TimeUnit.MILLISECONDS);
                PendingIntent pendingIntent = (PendingIntent) intent.getParcelableExtra("android:screenshot_action_intent");
                if (intent.getBooleanExtra("android:screenshot_cancel_notification", false)) {
                    ScreenshotNotificationsController.cancelScreenshotNotification(context);
                }
                ActivityOptions makeBasic = ActivityOptions.makeBasic();
                makeBasic.setDisallowEnterPictureInPictureWhileLaunching(intent.getBooleanExtra("android:screenshot_disallow_enter_pip", false));
                try {
                    pendingIntent.send(context, 0, null, null, null, null, makeBasic.toBundle());
                } catch (PendingIntent.CanceledException e) {
                    Log.e("GlobalScreenshot", "Pending intent canceled", e);
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e2) {
                Slog.e("GlobalScreenshot", "Unable to share screenshot", e2);
            }
        }
    }

    public static class TargetChosenReceiver extends BroadcastReceiver {
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            ScreenshotNotificationsController.cancelScreenshotNotification(context);
        }
    }

    public static class DeleteScreenshotReceiver extends BroadcastReceiver {
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("android:screenshot_uri_id")) {
                ScreenshotNotificationsController.cancelScreenshotNotification(context);
                new DeleteImageInBackgroundTask(context).execute(Uri.parse(intent.getStringExtra("android:screenshot_uri_id")));
                if (intent.getBooleanExtra("android:smart_actions_enabled", false)) {
                    ScreenshotSmartActions.notifyScreenshotAction(context, intent.getStringExtra("android:screenshot_id"), "Delete", false);
                }
            }
        }
    }

    public static class SmartActionsReceiver extends BroadcastReceiver {
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            PendingIntent pendingIntent = (PendingIntent) intent.getParcelableExtra("android:screenshot_action_intent");
            String stringExtra = intent.getStringExtra("android:screenshot_action_type");
            Slog.d("GlobalScreenshot", "Executing smart action [" + stringExtra + "]:" + pendingIntent.getIntent());
            try {
                pendingIntent.send(context, 0, null, null, null, null, ActivityOptions.makeBasic().toBundle());
            } catch (PendingIntent.CanceledException e) {
                Log.e("GlobalScreenshot", "Pending intent canceled", e);
            }
            ScreenshotSmartActions.notifyScreenshotAction(context, intent.getStringExtra("android:screenshot_id"), stringExtra, true);
        }
    }
}
