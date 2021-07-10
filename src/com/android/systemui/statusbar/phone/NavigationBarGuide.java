package com.android.systemui.statusbar.phone;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Region;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Slog;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0011R$layout;
import com.android.systemui.C0015R$string;
import com.android.systemui.C0016R$style;
public class NavigationBarGuide {
    private static final boolean DEBUG = Build.DEBUG_ONEPLUS;
    private final Runnable mConfirm = new Runnable() { // from class: com.android.systemui.statusbar.phone.NavigationBarGuide.1
        @Override // java.lang.Runnable
        public void run() {
            if (NavigationBarGuide.DEBUG) {
                Slog.d("NavigationBarGuide", "mConfirm.run()");
            }
            if (!NavigationBarGuide.this.mConfirmed) {
                NavigationBarGuide.this.mConfirmed = true;
                NavigationBarGuide.this.saveSetting();
            }
            NavigationBarGuide.this.handleHide();
        }
    };
    private boolean mConfirmed;
    private ContentWindowView mContentWindow;
    private final Context mContext;
    private final H mHandler;
    private int mRotation;
    private StatusBar mStatusBar;
    private WindowManager mWindowManager;
    private final IBinder mWindowToken = new Binder();

    public NavigationBarGuide(Context context, StatusBar statusBar) {
        this.mContext = context;
        this.mHandler = new H();
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        this.mConfirmed = isConfirmed();
        this.mStatusBar = statusBar;
    }

    private boolean isConfirmed() {
        boolean z = false;
        try {
            z = "confirmed".equals(Settings.Secure.getString(this.mContext.getContentResolver(), "navigation_bar_guide_confirmation"));
            if (DEBUG) {
                Slog.d("NavigationBarGuide", "Loaded confirmed=" + z);
            }
        } catch (Throwable th) {
            Slog.w("NavigationBarGuide", "Error loading confirmations, value=" + ((String) null), th);
        }
        return z;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void saveSetting() {
        if (DEBUG) {
            Slog.d("NavigationBarGuide", "saveSetting()");
        }
        try {
            String str = this.mConfirmed ? "confirmed" : null;
            Settings.Secure.putString(this.mContext.getContentResolver(), "navigation_bar_guide_confirmation", str);
            if (DEBUG) {
                Slog.d("NavigationBarGuide", "Saved value=" + str);
            }
        } catch (Throwable th) {
            Slog.w("NavigationBarGuide", "Error saving confirmations, mConfirmed=" + this.mConfirmed, th);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleHide() {
        if (this.mContentWindow != null) {
            if (DEBUG) {
                Slog.d("NavigationBarGuide", "Hiding navigation bar guide confirmation");
            }
            this.mWindowManager.removeView(this.mContentWindow);
            this.mContentWindow = null;
        }
    }

    private WindowManager.LayoutParams getContentWindowLayoutParams() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-1, -1, 2014, 16777504, -3);
        layoutParams.setTitle("NavigationBarGuide");
        layoutParams.windowAnimations = C0016R$style.Animation_NavigationBarGuide;
        layoutParams.token = getWindowToken();
        return layoutParams;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private FrameLayout.LayoutParams getBubbleLayoutParams() {
        int i;
        int i2 = this.mRotation;
        int i3 = -1;
        int i4 = 3;
        if (i2 == 1 || i2 == 3) {
            i3 = (int) (((float) this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.nav_bar_guide_width_land)) * this.mContext.getResources().getConfiguration().fontScale);
            i = -1;
        } else {
            i = -2;
        }
        int i5 = this.mRotation;
        if (i5 == 1) {
            i4 = 5;
        } else if (i5 != 3) {
            i4 = 80;
        }
        return new FrameLayout.LayoutParams(i3, i, i4);
    }

    public void show() {
        if (DEBUG) {
            Slog.d("NavigationBarGuide", "show DEBUG_SHOW_EVERY_TIME=false, mConfirmed=" + this.mConfirmed);
        }
        if (!this.mConfirmed) {
            this.mHandler.removeMessages(1);
            this.mHandler.sendEmptyMessageDelayed(1, 750);
        }
    }

    public IBinder getWindowToken() {
        return this.mWindowToken;
    }

    /* access modifiers changed from: private */
    public class ContentWindowView extends FrameLayout {
        private final ColorDrawable mColor = new ColorDrawable(0);
        private ValueAnimator mColorAnim;
        private final Runnable mConfirm;
        private ViewGroup mContentLayout;
        private ViewTreeObserver.OnComputeInternalInsetsListener mInsetsListener = new ViewTreeObserver.OnComputeInternalInsetsListener() { // from class: com.android.systemui.statusbar.phone.NavigationBarGuide.ContentWindowView.2
            private final int[] mTmpInt2 = new int[2];

            public void onComputeInternalInsets(ViewTreeObserver.InternalInsetsInfo internalInsetsInfo) {
                ContentWindowView.this.mContentLayout.getLocationInWindow(this.mTmpInt2);
                internalInsetsInfo.setTouchableInsets(3);
                Region region = internalInsetsInfo.touchableRegion;
                int[] iArr = this.mTmpInt2;
                region.set(iArr[0], iArr[1], iArr[0] + ContentWindowView.this.mContentLayout.getWidth(), this.mTmpInt2[1] + ContentWindowView.this.mContentLayout.getHeight());
            }
        };
        private final Interpolator mInterpolator;
        private MyOrientationEventListener mOrientationListener;
        private Runnable mUpdateLayoutRunnable = new Runnable() { // from class: com.android.systemui.statusbar.phone.NavigationBarGuide.ContentWindowView.1
            @Override // java.lang.Runnable
            public void run() {
                if (ContentWindowView.this.mContentLayout != null && ContentWindowView.this.mContentLayout.getParent() != null) {
                    ContentWindowView.this.initViews();
                }
            }
        };

        @Override // android.view.View
        public boolean onTouchEvent(MotionEvent motionEvent) {
            return true;
        }

        private class MyOrientationEventListener extends OrientationEventListener {
            public MyOrientationEventListener(Context context, int i) {
                super(context, i);
            }

            @Override // android.view.OrientationEventListener
            public void onOrientationChanged(int i) {
                if (i != -1 && NavigationBarGuide.this.mRotation != NavigationBarGuide.this.mWindowManager.getDefaultDisplay().getRotation()) {
                    ContentWindowView contentWindowView = ContentWindowView.this;
                    contentWindowView.post(contentWindowView.mUpdateLayoutRunnable);
                }
            }
        }

        public ContentWindowView(Context context, Runnable runnable) {
            super(context);
            this.mConfirm = runnable;
            setBackground(this.mColor);
            setImportantForAccessibility(2);
            this.mInterpolator = AnimationUtils.loadInterpolator(((FrameLayout) this).mContext, 17563662);
        }

        @Override // android.view.View, android.view.ViewGroup
        public void onAttachedToWindow() {
            super.onAttachedToWindow();
            MyOrientationEventListener myOrientationEventListener = new MyOrientationEventListener(((FrameLayout) this).mContext, 3);
            this.mOrientationListener = myOrientationEventListener;
            myOrientationEventListener.enable();
            getViewTreeObserver().addOnComputeInternalInsetsListener(this.mInsetsListener);
            initViews();
        }

        @Override // android.view.View, android.view.ViewGroup
        public void onDetachedFromWindow() {
            this.mOrientationListener.disable();
            NavigationBarGuide.this.mHandler.postDelayed(new Runnable() { // from class: com.android.systemui.statusbar.phone.NavigationBarGuide.ContentWindowView.3
                @Override // java.lang.Runnable
                public void run() {
                    if (NavigationBarGuide.this.mStatusBar.getNavigationBarView() != null) {
                        NavigationBarGuide.this.mStatusBar.getNavigationBarView().setVisibility(0);
                    }
                }
            }, 250);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void initViews() {
            int i;
            int i2;
            int i3;
            removeAllViews();
            NavigationBarGuide navigationBarGuide = NavigationBarGuide.this;
            navigationBarGuide.mRotation = navigationBarGuide.mWindowManager.getDefaultDisplay().getRotation();
            if (NavigationBarGuide.this.mRotation == 1) {
                i = C0011R$layout.navigation_bar_guide_rot90;
            } else if (NavigationBarGuide.this.mRotation == 3) {
                i = C0011R$layout.navigation_bar_guide_rot270;
            } else {
                i = C0011R$layout.navigation_bar_guide;
            }
            ViewGroup viewGroup = (ViewGroup) View.inflate(getContext(), i, null);
            this.mContentLayout = viewGroup;
            TextView textView = (TextView) viewGroup.findViewById(C0008R$id.nav_bar_guide_title);
            if (textView != null) {
                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(getResources().getString(C0015R$string.nav_bar_guide_title));
                int length = spannableStringBuilder.length();
                spannableStringBuilder.append((CharSequence) "pin_off");
                Drawable drawable = getResources().getDrawable(C0006R$drawable.ic_nav_bar_guide_pin_off);
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                spannableStringBuilder.setSpan(new ImageSpan(drawable, 1), length, spannableStringBuilder.length(), 33);
                textView.setText(spannableStringBuilder);
            }
            ((Button) this.mContentLayout.findViewById(C0008R$id.ok)).setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.statusbar.phone.NavigationBarGuide.ContentWindowView.4
                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    ContentWindowView.this.mConfirm.run();
                }
            });
            addView(this.mContentLayout, NavigationBarGuide.this.getBubbleLayoutParams());
            if (ActivityManager.isHighEndGfx()) {
                final ViewGroup viewGroup2 = this.mContentLayout;
                if (NavigationBarGuide.this.mRotation == 1 || NavigationBarGuide.this.mRotation == 3) {
                    i3 = getResources().getDimensionPixelSize(C0005R$dimen.nav_bar_guide_anim_offsetX_land);
                    i2 = getResources().getDimensionPixelSize(C0005R$dimen.nav_bar_guide_anim_offsetY_land);
                } else {
                    i3 = getResources().getDimensionPixelSize(C0005R$dimen.nav_bar_guide_anim_offsetX);
                    i2 = getResources().getDimensionPixelSize(C0005R$dimen.nav_bar_guide_anim_offsetY);
                }
                if (NavigationBarGuide.this.mRotation == 3) {
                    i3 = 0 - i3;
                }
                viewGroup2.setAlpha(0.0f);
                viewGroup2.setTranslationX((float) i3);
                viewGroup2.setTranslationY((float) i2);
                postOnAnimation(new Runnable() { // from class: com.android.systemui.statusbar.phone.NavigationBarGuide.ContentWindowView.5
                    @Override // java.lang.Runnable
                    public void run() {
                        viewGroup2.animate().alpha(1.0f).translationX(0.0f).translationY(0.0f).setDuration(250).setInterpolator(ContentWindowView.this.mInterpolator).withLayer().start();
                        ContentWindowView.this.mColorAnim = ValueAnimator.ofObject(new ArgbEvaluator(), 0, Integer.MIN_VALUE);
                        ContentWindowView.this.mColorAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.statusbar.phone.NavigationBarGuide.ContentWindowView.5.1
                            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                ContentWindowView.this.mColor.setColor(((Integer) valueAnimator.getAnimatedValue()).intValue());
                            }
                        });
                        ContentWindowView.this.mColorAnim.setDuration(250L);
                        ContentWindowView.this.mColorAnim.setInterpolator(ContentWindowView.this.mInterpolator);
                        ContentWindowView.this.mColorAnim.start();
                    }
                });
                return;
            }
            this.mColor.setColor(Integer.MIN_VALUE);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleShow() {
        if (DEBUG) {
            Slog.d("NavigationBarGuide", "Showing navigation bar guide confirmation");
        }
        ContentWindowView contentWindowView = new ContentWindowView(this.mContext, this.mConfirm);
        this.mContentWindow = contentWindowView;
        contentWindowView.setSystemUiVisibility(768);
        this.mWindowManager.addView(this.mContentWindow, getContentWindowLayoutParams());
        this.mStatusBar.getNavigationBarView().setVisibility(8);
    }

    /* access modifiers changed from: private */
    public final class H extends Handler {
        private H() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 1) {
                NavigationBarGuide.this.handleShow();
            } else if (i == 2) {
                NavigationBarGuide.this.handleHide();
            }
        }
    }
}
