package com.android.systemui;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.VectorDrawable;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerExecutor;
import android.os.HandlerThread;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.DisplayCutout;
import android.view.DisplayInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.internal.util.Preconditions;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.RegionInterceptingFrameLayout;
import com.android.systemui.ScreenDecorations;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.qs.SecureSetting;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.util.leak.RotationUtils;
import com.oneplus.util.OpUtils;
import com.oneplus.util.SystemSetting;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
public class ScreenDecorations extends SystemUI implements TunerService.Tunable {
    private static final boolean DEBUG = OpUtils.DEBUG_ONEPLUS;
    private static final boolean DEBUG_COLOR = DEBUG_SCREENSHOT_ROUNDED_CORNERS;
    private static final boolean DEBUG_CUTOUT_CIRCLE_DISPLAY = SystemProperties.getBoolean("debug.cutout.display.circle.enable", false);
    private static final boolean DEBUG_CUTOUT_DISPLAY = SystemProperties.getBoolean("debug.cutout.display.enable", false);
    private static final boolean DEBUG_SCREENSHOT_ROUNDED_CORNERS = SystemProperties.getBoolean("debug.screenshot_rounded_corners", false);
    private static final boolean DEBUG_SCREEN_DECORATIONS = SystemProperties.getBoolean("debug.screen_decorations", false);
    private static int mDisableRoundedCorner = SystemProperties.getInt("vendor.display.disable_rounded_corner", 0);
    private static int mOpCustRegionRight;
    private static int mOpCustRegionleft;
    private int MAX_BLOCK_INTERVAL = 1200;
    private final BroadcastDispatcher mBroadcastDispatcher;
    private SecureSetting mColorInversionSetting;
    private DisplayCutoutView[] mCutoutViews;
    private DisplayManager.DisplayListener mDisplayListener;
    private DisplayManager mDisplayManager;
    private Handler mHandler;
    private boolean mHasRoundedCorner = false;
    protected boolean mIsRegistered;
    private boolean mIsRoundedCornerMultipleRadius;
    private int mLastSizeBottom;
    private int mLastSizeTop;
    private final Handler mMainHandler;
    private boolean mOpPendingRotationChange = false;
    private boolean mOpPendingRotationChangeBottom = false;
    protected View[] mOverlays;
    private boolean mPendingRotationChange;
    float mResizeRatio = 0.75f;
    private int mRotation;
    protected int mRoundedDefault;
    protected int mRoundedDefaultBottom;
    int mRoundedDefaultBottomResize;
    protected int mRoundedDefaultBottomWidth;
    int mRoundedDefaultBottomWidthResize;
    protected int mRoundedDefaultTop;
    int mRoundedDefaultTopResize;
    protected int mRoundedDefaultTopWidth;
    int mRoundedDefaultTopWidthResize;
    int mScreenResolution = 0;
    private Runnable mShowRunnable = new Runnable() { // from class: com.android.systemui.ScreenDecorations.8
        @Override // java.lang.Runnable
        public void run() {
            int rotation = ScreenDecorations.this.mContext.getDisplay().getRotation();
            if (rotation != ScreenDecorations.this.mRotation) {
                Log.i("ScreenDecorations", "Attention, rotation changed at postDelay interval, mRotation:" + ScreenDecorations.this.mRotation + ", newRotation:" + rotation);
                ScreenDecorations.this.mRotation = rotation;
            }
            if (ScreenDecorations.this.isRectangleTop() || ScreenDecorations.this.isRectangleBottom()) {
                ScreenDecorations.this.updateDecorSize();
            }
            ScreenDecorations.this.updateLayoutParams();
            if (OpUtils.DEBUG_ONEPLUS) {
                Log.i("ScreenDecorations", "mShowRunnable, mOpPendingRotationChange:" + ScreenDecorations.this.mOpPendingRotationChange + ", mOpPendingRotationChangeBottom:" + ScreenDecorations.this.mOpPendingRotationChangeBottom + ", mRotation:" + ScreenDecorations.this.mRotation);
            }
            ScreenDecorations.this.mOpPendingRotationChange = false;
            ScreenDecorations.this.mOpPendingRotationChangeBottom = false;
            if (ScreenDecorations.this.mOverlays != null) {
                for (int i = 0; i < 4; i++) {
                    ScreenDecorations screenDecorations = ScreenDecorations.this;
                    if (screenDecorations.mOverlays[i] != null) {
                        screenDecorations.updateView(i);
                        ScreenDecorations.this.mOverlays[i].setVisibility(0);
                    }
                }
            }
        }
    };
    private final BroadcastReceiver mShutDownBroadcastReceiver = new BroadcastReceiver() { // from class: com.android.systemui.ScreenDecorations.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.ACTION_SHUTDOWN".equals(intent.getAction())) {
                Log.d("ScreenDecorations", "Shutdown & remove all overlays");
                ScreenDecorations.this.removeAllOverlays();
            }
        }
    };
    private SystemSetting mTempColorInversionDisableSetting;
    private boolean mTempDisableInversion = false;
    private final TunerService mTunerService;
    private final BroadcastReceiver mUserSwitchIntentReceiver = new BroadcastReceiver() { // from class: com.android.systemui.ScreenDecorations.7
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            int currentUser = ActivityManager.getCurrentUser();
            if (ScreenDecorations.DEBUG) {
                Log.d("ScreenDecorations", "UserSwitched newUserId=" + currentUser);
            }
            ScreenDecorations.this.mColorInversionSetting.setUserId(currentUser);
            if (ScreenDecorations.this.mTempColorInversionDisableSetting != null) {
                ScreenDecorations.this.mTempColorInversionDisableSetting.onUserSwitched();
            }
            ScreenDecorations screenDecorations = ScreenDecorations.this;
            screenDecorations.updateColorInversion(screenDecorations.mColorInversionSetting.getValue());
        }
    };
    private WindowManager mWindowManager;

    public static int getBoundPositionFromRotation(int i, int i2) {
        int i3 = i - i2;
        return i3 < 0 ? i3 + 4 : i3;
    }

    private int getRoundedCornerRotationX(int i, boolean z) {
        if (z) {
            return 0;
        }
        return (i == 0 || i == 2) ? 180 : 0;
    }

    private int getRoundedCornerRotationY(int i, boolean z) {
        if (z) {
            return 0;
        }
        return (i == 1 || i == 3) ? 180 : 0;
    }

    public static Region rectsToRegion(List<Rect> list) {
        Region obtain = Region.obtain();
        if (list != null) {
            for (Rect rect : list) {
                if (rect != null && !rect.isEmpty()) {
                    obtain.op(rect, Region.Op.UNION);
                }
            }
        }
        return obtain;
    }

    public ScreenDecorations(Context context, Handler handler, BroadcastDispatcher broadcastDispatcher, TunerService tunerService) {
        super(context);
        this.mMainHandler = handler;
        this.mBroadcastDispatcher = broadcastDispatcher;
        this.mTunerService = tunerService;
    }

    @Override // com.android.systemui.SystemUI
    public void start() {
        Handler startHandlerThread = startHandlerThread();
        this.mHandler = startHandlerThread;
        startHandlerThread.post(new Runnable() { // from class: com.android.systemui.-$$Lambda$ScreenDecorations$IfAux2ksmJXT9o9i38WaSEQXJTQ
            @Override // java.lang.Runnable
            public final void run() {
                ScreenDecorations.lambda$IfAux2ksmJXT9o9i38WaSEQXJTQ(ScreenDecorations.this);
            }
        });
        if (OpUtils.isUST()) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.ACTION_SHUTDOWN");
            this.mBroadcastDispatcher.registerReceiver(this.mShutDownBroadcastReceiver, intentFilter, new HandlerExecutor(this.mHandler), UserHandle.ALL);
        }
    }

    public Handler startHandlerThread() {
        HandlerThread handlerThread = new HandlerThread("ScreenDecorations");
        handlerThread.start();
        return handlerThread.getThreadHandler();
    }

    /* access modifiers changed from: public */
    private void startOnScreenDecorationsThread() {
        this.mRotation = this.mContext.getDisplay().getRotation();
        this.mWindowManager = (WindowManager) this.mContext.getSystemService(WindowManager.class);
        this.mDisplayManager = (DisplayManager) this.mContext.getSystemService(DisplayManager.class);
        this.mIsRoundedCornerMultipleRadius = this.mContext.getResources().getBoolean(C0003R$bool.config_roundedCornerMultipleRadius);
        updateRoundedCornerRadii();
        this.mHasRoundedCorner = true;
        mOpCustRegionleft = this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.op_cust_statusbar_cutout_show_region_left);
        mOpCustRegionRight = this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.op_cust_statusbar_cutout_show_region_right);
        new SettingsObserver();
        this.mScreenResolution = Settings.Global.getInt(this.mContext.getContentResolver(), "oneplus_screen_resolution_adjust", 2);
        setupDecorations();
        setupCameraListener();
        AnonymousClass3 r0 = new DisplayManager.DisplayListener() { // from class: com.android.systemui.ScreenDecorations.3
            @Override // android.hardware.display.DisplayManager.DisplayListener
            public void onDisplayAdded(int i) {
            }

            @Override // android.hardware.display.DisplayManager.DisplayListener
            public void onDisplayRemoved(int i) {
            }

            @Override // android.hardware.display.DisplayManager.DisplayListener
            public void onDisplayChanged(int i) {
                int rotation = ScreenDecorations.this.mContext.getDisplay().getRotation();
                boolean z = OpUtils.DEBUG_ONEPLUS;
                Log.i("ScreenDecorations", "onDisplayChanged, displayId:" + i + ", now Rotation:" + ScreenDecorations.this.mRotation + ", newRotation:" + rotation);
                ScreenDecorations.this.mOpPendingRotationChange = false;
                ScreenDecorations.this.mOpPendingRotationChangeBottom = false;
                ScreenDecorations.this.updateOrientation();
            }
        };
        this.mDisplayListener = r0;
        this.mDisplayManager.registerDisplayListener(r0, this.mHandler);
        updateOrientation();
    }

    private void setupDecorations() {
        Rect[] rectArr;
        if (hasRoundedCorners() || shouldDrawCutout()) {
            DisplayCutout cutout = getCutout();
            if (cutout == null) {
                rectArr = null;
            } else {
                rectArr = cutout.getBoundingRectsAll();
            }
            for (int i = 0; i < 4; i++) {
                int boundPositionFromRotation = getBoundPositionFromRotation(i, this.mRotation);
                if ((rectArr == null || rectArr[boundPositionFromRotation].isEmpty()) && !shouldShowRoundedCorner(i)) {
                    removeOverlay(i);
                } else {
                    createOverlay(i);
                }
            }
            initRoundCornerView();
            if (DEBUG_SCREEN_DECORATIONS) {
                updateColorInversion(-256);
            }
        } else {
            removeAllOverlays();
        }
        if (!hasOverlays()) {
            this.mMainHandler.post(new Runnable() { // from class: com.android.systemui.-$$Lambda$ScreenDecorations$CTk_RNSSvwUoNV8CfAa6W3y0c0A
                @Override // java.lang.Runnable
                public final void run() {
                    ScreenDecorations.this.lambda$setupDecorations$1$ScreenDecorations();
                }
            });
            SecureSetting secureSetting = this.mColorInversionSetting;
            if (secureSetting != null) {
                secureSetting.setListening(false);
            }
            this.mBroadcastDispatcher.unregisterReceiver(this.mUserSwitchIntentReceiver);
            this.mIsRegistered = false;
        } else if (!this.mIsRegistered) {
            this.mDisplayManager.getDisplay(0).getMetrics(new DisplayMetrics());
            this.mMainHandler.post(new Runnable() { // from class: com.android.systemui.-$$Lambda$ScreenDecorations$ItnW8ZEHeCqCHue6f8abcXewifU
                @Override // java.lang.Runnable
                public final void run() {
                    ScreenDecorations.this.lambda$setupDecorations$0$ScreenDecorations();
                }
            });
            if (this.mColorInversionSetting == null) {
                AnonymousClass4 r0 = new SecureSetting(this.mContext, this.mHandler, "accessibility_display_inversion_enabled") { // from class: com.android.systemui.ScreenDecorations.4
                    @Override // com.android.systemui.qs.SecureSetting
                    public void handleValueChanged(int i2, boolean z) {
                        ScreenDecorations.this.updateColorInversion(i2);
                    }
                };
                this.mColorInversionSetting = r0;
                r0.setListening(true);
                this.mColorInversionSetting.onChange(false);
            }
            AnonymousClass5 r02 = new SystemSetting(this.mContext, this.mHandler, "temp_disable_inversion", true) { // from class: com.android.systemui.ScreenDecorations.5
                @Override // com.oneplus.util.SystemSetting
                public void handleValueChanged(int i2, boolean z) {
                    ScreenDecorations.this.mTempDisableInversion = i2 != 0;
                    ScreenDecorations screenDecorations = ScreenDecorations.this;
                    screenDecorations.updateColorInversion(screenDecorations.mColorInversionSetting.getValue());
                }
            };
            this.mTempColorInversionDisableSetting = r02;
            r02.setListening(true);
            this.mTempColorInversionDisableSetting.onChange(false);
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.USER_SWITCHED");
            this.mBroadcastDispatcher.registerReceiver(this.mUserSwitchIntentReceiver, intentFilter, new HandlerExecutor(this.mHandler), UserHandle.ALL);
            this.mIsRegistered = true;
        }
    }

    /* access modifiers changed from: public */
    /* renamed from: lambda$setupDecorations$0 */
    private /* synthetic */ void lambda$setupDecorations$0$ScreenDecorations() {
        this.mTunerService.addTunable(this, "sysui_rounded_size");
    }

    /* access modifiers changed from: public */
    /* renamed from: lambda$setupDecorations$1 */
    private /* synthetic */ void lambda$setupDecorations$1$ScreenDecorations() {
        this.mTunerService.removeTunable(this);
    }

    public DisplayCutout getCutout() {
        return this.mContext.getDisplay().getCutout();
    }

    public boolean hasOverlays() {
        if (this.mOverlays == null) {
            return false;
        }
        for (int i = 0; i < 4; i++) {
            if (this.mOverlays[i] != null) {
                return true;
            }
        }
        this.mOverlays = null;
        return false;
    }

    private void removeAllOverlays() {
        if (this.mOverlays != null) {
            for (int i = 0; i < 4; i++) {
                if (this.mOverlays[i] != null) {
                    removeOverlay(i);
                }
            }
            this.mOverlays = null;
        }
    }

    private void removeOverlay(int i) {
        View[] viewArr = this.mOverlays;
        if (viewArr != null && viewArr[i] != null) {
            this.mWindowManager.removeViewImmediate(viewArr[i]);
            this.mOverlays[i] = null;
        }
    }

    private void createOverlay(final int i) {
        if (this.mOverlays == null) {
            this.mOverlays = new View[4];
        }
        if (this.mCutoutViews == null) {
            this.mCutoutViews = new DisplayCutoutView[4];
        }
        View[] viewArr = this.mOverlays;
        if (viewArr[i] == null) {
            viewArr[i] = LayoutInflater.from(this.mContext).inflate(C0011R$layout.rounded_corners, (ViewGroup) null);
            this.mCutoutViews[i] = new DisplayCutoutView(this.mContext, i, this);
            ((ViewGroup) this.mOverlays[i]).addView(this.mCutoutViews[i]);
            this.mOverlays[i].setSystemUiVisibility(256);
            this.mOverlays[i].setAlpha(0.0f);
            this.mOverlays[i].setForceDarkAllowed(false);
            updateView(i);
            this.mWindowManager.addView(this.mOverlays[i], getWindowLayoutParams(i));
            this.mOverlays[i].addOnLayoutChangeListener(new View.OnLayoutChangeListener() { // from class: com.android.systemui.ScreenDecorations.6
                @Override // android.view.View.OnLayoutChangeListener
                public void onLayoutChange(View view, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9) {
                    ScreenDecorations.this.mOverlays[i].removeOnLayoutChangeListener(this);
                    ScreenDecorations.this.mOverlays[i].animate().alpha(1.0f).setDuration(1000).start();
                }
            });
            this.mOverlays[i].getViewTreeObserver().addOnPreDrawListener(new ValidatingPreDrawListener(this.mOverlays[i], i));
        }
    }

    private void updateView(int i) {
        View[] viewArr = this.mOverlays;
        if (viewArr != null && viewArr[i] != null) {
            if ((i != 1 || !isRectangleTop()) && (i != 3 || !isRectangleBottom())) {
                updateRoundedCornerView(i, C0008R$id.left);
                updateRoundedCornerView(i, C0008R$id.right);
            } else {
                updateViewByBitmap(i, C0008R$id.left);
                updateViewByBitmap(i, C0008R$id.right);
            }
            updateDecorSize();
            DisplayCutoutView[] displayCutoutViewArr = this.mCutoutViews;
            if (displayCutoutViewArr != null && displayCutoutViewArr[i] != null) {
                displayCutoutViewArr[i].setRotation(this.mRotation);
            }
        }
    }

    public WindowManager.LayoutParams getWindowLayoutParams(int i) {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(getWidthLayoutParamByPos(i), getHeightLayoutParamByPos(i), 2024, 545259816, -3);
        int i2 = layoutParams.privateFlags | 80;
        layoutParams.privateFlags = i2;
        if (!DEBUG_SCREENSHOT_ROUNDED_CORNERS) {
            layoutParams.privateFlags = i2 | 1048576;
        }
        layoutParams.setTitle(getWindowTitleByPos(i));
        layoutParams.gravity = getOverlayWindowGravity(i);
        layoutParams.layoutInDisplayCutoutMode = 3;
        layoutParams.setFitInsetsTypes(0);
        layoutParams.privateFlags |= 16777216;
        return layoutParams;
    }

    private int getWidthLayoutParamByPos(int i) {
        int boundPositionFromRotation = getBoundPositionFromRotation(i, this.mRotation);
        return (boundPositionFromRotation == 1 || boundPositionFromRotation == 3) ? -1 : -2;
    }

    private int getHeightLayoutParamByPos(int i) {
        int boundPositionFromRotation = getBoundPositionFromRotation(i, this.mRotation);
        return (boundPositionFromRotation == 1 || boundPositionFromRotation == 3) ? -2 : -1;
    }

    public static String getWindowTitleByPos(int i) {
        if (i == 0) {
            return "ScreenDecorOverlayLeft";
        }
        if (i == 1) {
            return "ScreenDecorOverlay";
        }
        if (i == 2) {
            return "ScreenDecorOverlayRight";
        }
        if (i == 3) {
            return "ScreenDecorOverlayBottom";
        }
        throw new IllegalArgumentException("unknown bound position: " + i);
    }

    private int getOverlayWindowGravity(int i) {
        int boundPositionFromRotation = getBoundPositionFromRotation(i, this.mRotation);
        if (boundPositionFromRotation == 0) {
            return 3;
        }
        if (boundPositionFromRotation == 1) {
            return 48;
        }
        if (boundPositionFromRotation == 2) {
            return 5;
        }
        if (boundPositionFromRotation == 3) {
            return 80;
        }
        throw new IllegalArgumentException("unknown bound position: " + i);
    }

    private void setupCameraListener() {
        this.mContext.getResources().getBoolean(C0003R$bool.config_enableDisplayCutoutProtection);
    }

    private void updateColorInversion(int i) {
        int i2 = -16777216;
        int i3 = i != 0 ? -1 : -16777216;
        if (DEBUG_COLOR) {
            i3 = -65536;
        }
        if (!this.mTempDisableInversion) {
            i2 = i3;
        }
        if (DEBUG_SCREEN_DECORATIONS) {
            i2 = -256;
        }
        ColorStateList valueOf = ColorStateList.valueOf(i2);
        if (this.mOverlays != null) {
            for (int i4 = 0; i4 < 4; i4++) {
                View[] viewArr = this.mOverlays;
                if (viewArr[i4] != null) {
                    int childCount = ((ViewGroup) viewArr[i4]).getChildCount();
                    for (int i5 = 0; i5 < childCount; i5++) {
                        View childAt = ((ViewGroup) this.mOverlays[i4]).getChildAt(i5);
                        if (childAt instanceof ImageView) {
                            ((ImageView) childAt).setImageTintList(valueOf);
                        } else if (childAt instanceof DisplayCutoutView) {
                            ((DisplayCutoutView) childAt).setColor(i2);
                        }
                    }
                }
            }
        }
    }

    @Override // com.android.systemui.SystemUI
    public void onConfigurationChanged(Configuration configuration) {
        this.mHandler.post(new Runnable(configuration) { // from class: com.android.systemui.-$$Lambda$ScreenDecorations$NfhIKJZ6L4jkc7cEhc50RJtdE1g
            public final /* synthetic */ Configuration f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                ScreenDecorations.this.lambda$onConfigurationChanged$2$ScreenDecorations(this.f$1);
            }
        });
    }

    /* access modifiers changed from: public */
    /* renamed from: lambda$onConfigurationChanged$2 */
    private /* synthetic */ void lambda$onConfigurationChanged$2$ScreenDecorations(Configuration configuration) {
        boolean z = OpUtils.DEBUG_ONEPLUS;
        Log.i("ScreenDecorations", "receive onConfigurationChanged, newConfig.orientation:" + configuration.orientation + ", getRotation:" + RotationUtils.getExactRotation(this.mContext));
        if (OpUtils.isSupportResolutionSwitch(this.mContext) || isRectangleTop() || isRectangleBottom()) {
            this.mScreenResolution = Settings.Global.getInt(this.mContext.getContentResolver(), "oneplus_screen_resolution_adjust", 2);
            updateDecorSize();
        }
        int i = this.mRotation;
        this.mPendingRotationChange = false;
        updateOrientation();
        updateRoundedCornerRadii();
        if (DEBUG) {
            Log.i("ScreenDecorations", "onConfigChanged from rot " + i + " to " + this.mRotation);
        }
        setupDecorations();
        if (this.mOverlays != null) {
            updateLayoutParams();
        }
    }

    private void updateOrientation() {
        boolean z = this.mHandler.getLooper().getThread() == Thread.currentThread();
        Preconditions.checkState(z, "must call on " + this.mHandler.getLooper().getThread() + ", but was " + Thread.currentThread());
        if (!this.mPendingRotationChange) {
            int rotation = this.mContext.getDisplay().getRotation();
            if (rotation != this.mRotation || !isOverlaysVisibility()) {
                this.mRotation = rotation;
                if (this.mOverlays != null) {
                    updateLayoutParams();
                    for (int i = 0; i < 4; i++) {
                        View[] viewArr = this.mOverlays;
                        if (viewArr[i] != null) {
                            viewArr[i].setVisibility(8);
                        }
                    }
                    postShow();
                }
            }
        }
    }

    private boolean isOverlaysVisibility() {
        if (this.mOverlays != null) {
            for (int i = 0; i < 4; i++) {
                View[] viewArr = this.mOverlays;
                if (viewArr[i] != null && viewArr[i].getVisibility() == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private void postShow() {
        this.mHandler.removeCallbacks(this.mShowRunnable);
        this.mHandler.postDelayed(this.mShowRunnable, 400);
    }

    private void updateRoundedCornerRadii() {
        int dimensionPixelSize = this.mContext.getResources().getDimensionPixelSize(17105455);
        boolean z = false;
        if (mDisableRoundedCorner == 1) {
            dimensionPixelSize = 0;
        }
        int dimensionPixelSize2 = this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.rounded_corner_radius_top);
        int dimensionPixelSize3 = this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.rounded_corner_radius_bottom);
        int dimensionPixelSize4 = this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.rounded_corner_radius_top_width);
        int dimensionPixelSize5 = this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.rounded_corner_radius_bottom_width);
        if (DEBUG_SCREEN_DECORATIONS) {
            Log.i("ScreenDecorations", "newRoundedDefaultTop:" + dimensionPixelSize2 + "newRoundedDefaultBottom:" + dimensionPixelSize3 + ", newRoundedDefaultBottomWidth:" + dimensionPixelSize5);
        }
        if (!(this.mRoundedDefault == dimensionPixelSize && this.mRoundedDefaultBottom == dimensionPixelSize3 && this.mRoundedDefaultTop == dimensionPixelSize2)) {
            z = true;
        }
        if (z) {
            if (this.mIsRoundedCornerMultipleRadius) {
                VectorDrawable vectorDrawable = (VectorDrawable) this.mContext.getDrawable(C0006R$drawable.rounded);
                int max = Math.max(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
                this.mRoundedDefault = max;
                this.mRoundedDefaultBottom = max;
                this.mRoundedDefaultTop = max;
            } else {
                this.mRoundedDefault = dimensionPixelSize;
                this.mRoundedDefaultTop = dimensionPixelSize2;
                this.mRoundedDefaultBottom = dimensionPixelSize3;
            }
            this.mRoundedDefaultTopWidth = dimensionPixelSize4;
            this.mRoundedDefaultBottomWidth = dimensionPixelSize5;
            onTuningChanged("sysui_rounded_size", null);
        }
    }

    private void updateRoundedCornerView(int i, int i2) {
        View findViewById = this.mOverlays[i].findViewById(i2);
        if (findViewById != null) {
            findViewById.setVisibility(8);
            if (shouldShowRoundedCorner(i)) {
                int boundPositionFromRotation = getBoundPositionFromRotation(i, this.mRotation);
                boolean z = true;
                ((FrameLayout.LayoutParams) findViewById.getLayoutParams()).gravity = getOpRoundedCornerGravity(i, boundPositionFromRotation, i2 == C0008R$id.left);
                findViewById.setRotation((float) getOpRoundedCornerRotation(boundPositionFromRotation, i));
                findViewById.setRotationX((float) getRoundedCornerRotationX(boundPositionFromRotation, i2 == C0008R$id.left));
                if (i2 != C0008R$id.left) {
                    z = false;
                }
                findViewById.setRotationY((float) getRoundedCornerRotationY(boundPositionFromRotation, z));
                findViewById.setVisibility(0);
            }
        }
    }

    private void updateViewByBitmap(int i, int i2) {
        ImageView imageView = (ImageView) this.mOverlays[i].findViewById(i2);
        int boundPositionFromRotation = getBoundPositionFromRotation(i, this.mRotation);
        boolean z = false;
        int opRoundedCornerGravity = getOpRoundedCornerGravity(i, boundPositionFromRotation, i2 == C0008R$id.left);
        int opRoundedCornerRotation = getOpRoundedCornerRotation(boundPositionFromRotation, i);
        ((FrameLayout.LayoutParams) imageView.getLayoutParams()).gravity = opRoundedCornerGravity;
        try {
            InputStream openRawResource = this.mContext.getResources().openRawResource(i == 1 ? C0006R$drawable.rounded_top : C0006R$drawable.rounded_bottom);
            if (openRawResource != null) {
                Bitmap decodeStream = BitmapFactory.decodeStream(openRawResource);
                openRawResource.close();
                if (decodeStream == null) {
                    Log.d("ScreenDecorations", "Bitmap is null");
                    return;
                }
                imageView.setImageBitmap(rotateBitmap(opRoundedCornerRotation, decodeStream));
                imageView.setRotationX((float) getRoundedCornerRotationX(boundPositionFromRotation, i2 == C0008R$id.left));
                if (i2 == C0008R$id.left) {
                    z = true;
                }
                imageView.setRotationY((float) getRoundedCornerRotationY(boundPositionFromRotation, z));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getOpRoundedCornerGravity(int i, int i2, boolean z) {
        if (i2 == 0) {
            return i == 1 ? !z ? 51 : 83 : z ? 51 : 83;
        }
        if (i2 == 1) {
            return i == 1 ? z ? 51 : 53 : !z ? 51 : 53;
        }
        if (i2 == 2) {
            return i == 1 ? z ? 53 : 85 : !z ? 53 : 85;
        }
        if (i2 == 3) {
            return i == 1 ? !z ? 83 : 85 : z ? 83 : 85;
        }
        throw new IllegalArgumentException("Incorrect position: " + i2);
    }

    private int getOpRoundedCornerRotation(int i, int i2) {
        if (i == 0) {
            return i2 == 1 ? 270 : 90;
        }
        if (i == 1) {
            return i2 == 1 ? 0 : 180;
        }
        if (i == 2) {
            return i2 == 1 ? 90 : 270;
        }
        if (i == 3) {
            return i2 == 1 ? 180 : 0;
        }
        throw new IllegalArgumentException("Incorrect position: " + i);
    }

    private boolean hasRoundedCorners() {
        return this.mHasRoundedCorner;
    }

    private boolean shouldShowRoundedCorner(int i) {
        if (!hasRoundedCorners()) {
            return false;
        }
        DisplayCutout cutout = getCutout();
        boolean z = cutout == null || cutout.isBoundsEmpty();
        int boundPositionFromRotation = getBoundPositionFromRotation(1, this.mRotation);
        int boundPositionFromRotation2 = getBoundPositionFromRotation(3, this.mRotation);
        if (z || !cutout.getBoundingRectsAll()[boundPositionFromRotation].isEmpty() || !cutout.getBoundingRectsAll()[boundPositionFromRotation2].isEmpty()) {
            if (i == 1 || i == 3) {
                return true;
            }
            return false;
        } else if (i == 0 || i == 2) {
            return true;
        } else {
            return false;
        }
    }

    private boolean shouldDrawCutout() {
        return shouldDrawCutout(this.mContext);
    }

    static boolean shouldDrawCutout(Context context) {
        if (mDisableRoundedCorner == 1) {
            return false;
        }
        return context.getResources().getBoolean(17891463);
    }

    private void updateLayoutParams() {
        if (this.mOverlays != null) {
            for (int i = 0; i < 4; i++) {
                View[] viewArr = this.mOverlays;
                if (viewArr[i] != null) {
                    this.mWindowManager.updateViewLayout(viewArr[i], getWindowLayoutParams(i));
                }
            }
        }
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        this.mHandler.post(new Runnable(str) { // from class: com.android.systemui.-$$Lambda$ScreenDecorations$SUGiySfRKK3_sOKVTApzHsRl4l4
            public final /* synthetic */ String f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                ScreenDecorations.this.lambda$onTuningChanged$3$ScreenDecorations(this.f$1);
            }
        });
    }

    /* access modifiers changed from: public */
    /* renamed from: lambda$onTuningChanged$3 */
    private /* synthetic */ void lambda$onTuningChanged$3$ScreenDecorations(String str) {
        if (this.mOverlays != null) {
            "sysui_rounded_size".equals(str);
        }
    }

    public void setSize(View view, int i) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = i;
        layoutParams.height = i;
        view.setLayoutParams(layoutParams);
    }

    private void setSize(View view, int i, int i2) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = i2;
        layoutParams.height = i;
        view.setLayoutParams(layoutParams);
    }

    public static class DisplayCutoutView extends View implements DisplayManager.DisplayListener, RegionInterceptingFrameLayout.RegionInterceptableView {
        private final Path mBoundingPath = new Path();
        private final Rect mBoundingRect = new Rect();
        private final List<Rect> mBounds = new ArrayList();
        private float mCameraProtectionProgress = 0.5f;
        private float mCircleR;
        private float mCircleX;
        private float mCircleY;
        private int mColor = -16777216;
        private Context mContext;
        private final Path mDebugPath = new Path();
        private final ScreenDecorations mDecorations;
        private boolean mDrawCutoutManually;
        private final DisplayInfo mInfo = new DisplayInfo();
        private int mInitialPosition;
        private final int[] mLocation = new int[2];
        private final Path mOpCutoutPath = new Path();
        private final Paint mPaint = new Paint();
        private int mPosition;
        private Path mProtectionPath;
        private Path mProtectionPathOrig;
        private RectF mProtectionRect;
        private RectF mProtectionRectOrig;
        private int mRotation;
        private boolean mShowProtection = false;
        private Rect mTotalBounds = new Rect();

        @Override // android.hardware.display.DisplayManager.DisplayListener
        public void onDisplayAdded(int i) {
        }

        @Override // android.hardware.display.DisplayManager.DisplayListener
        public void onDisplayRemoved(int i) {
        }

        public void setRotation(int i) {
        }

        @Override // com.android.systemui.RegionInterceptingFrameLayout.RegionInterceptableView
        public boolean shouldInterceptTouch() {
            return false;
        }

        public DisplayCutoutView(Context context, int i, ScreenDecorations screenDecorations) {
            super(context);
            this.mInitialPosition = i;
            this.mDecorations = screenDecorations;
            setId(C0008R$id.display_cutout);
            if (ScreenDecorations.DEBUG) {
                getViewTreeObserver().addOnDrawListener(new ViewTreeObserver.OnDrawListener(i) { // from class: com.android.systemui.-$$Lambda$ScreenDecorations$DisplayCutoutView$2GNvax__nx7i-PkAOEvsPY3xIqs
                    public final /* synthetic */ int f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // android.view.ViewTreeObserver.OnDrawListener
                    public final void onDraw() {
                        ScreenDecorations.DisplayCutoutView.this.lambda$new$0$ScreenDecorations$DisplayCutoutView(this.f$1);
                    }
                });
            }
            this.mContext = context;
            if (ScreenDecorations.DEBUG_SCREEN_DECORATIONS) {
                this.mColor = -256;
            }
            boolean z = context.getResources().getBoolean(C0003R$bool.config_draw_cutout_manually);
            this.mDrawCutoutManually = z;
            if (z) {
                this.mCircleX = context.getResources().getDimension(C0005R$dimen.op_draw_cutout_circle_x);
                this.mCircleY = context.getResources().getDimension(C0005R$dimen.op_draw_cutout_circle_y);
                this.mCircleR = context.getResources().getDimension(C0005R$dimen.op_draw_cutout_circle_r);
            }
        }

        /* access modifiers changed from: public */
        /* renamed from: lambda$new$0 */
        private /* synthetic */ void lambda$new$0$ScreenDecorations$DisplayCutoutView(int i) {
            Log.i("ScreenDecorations", ScreenDecorations.getWindowTitleByPos(i) + " drawn in rot " + this.mRotation);
        }

        public void setColor(int i) {
            this.mColor = i;
            if (ScreenDecorations.DEBUG_SCREEN_DECORATIONS) {
                this.mColor = -256;
            }
            invalidate();
        }

        @Override // android.view.View
        public void onAttachedToWindow() {
            super.onAttachedToWindow();
            ((DisplayManager) this.mContext.getSystemService(DisplayManager.class)).registerDisplayListener(this, getHandler());
            update();
        }

        @Override // android.view.View
        public void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            ((DisplayManager) this.mContext.getSystemService(DisplayManager.class)).unregisterDisplayListener(this);
        }

        @Override // android.view.View
        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            getLocationOnScreen(this.mLocation);
            int[] iArr = this.mLocation;
            canvas.translate((float) (-iArr[0]), (float) (-iArr[1]));
            if (ScreenDecorations.DEBUG_CUTOUT_DISPLAY) {
                updateDebugPath();
                if (!this.mDebugPath.isEmpty()) {
                    this.mPaint.setColor(-256);
                    this.mPaint.setStyle(Paint.Style.FILL);
                    this.mPaint.setAntiAlias(true);
                    canvas.drawPath(this.mDebugPath, this.mPaint);
                    return;
                }
                return;
            }
            if (ScreenDecorations.DEBUG_CUTOUT_CIRCLE_DISPLAY) {
                String str = SystemProperties.get("debug.cutout.display.circle.data", "");
                if (!str.isEmpty()) {
                    Log.d("ScreenDecorations", "DEBUG_CUTOUT_CIRCLE_DISPLAY:" + str);
                    String[] split = str.split(",");
                    opUpdateCutoutPath(Float.valueOf(split[0]).floatValue(), Float.valueOf(split[1]).floatValue(), Float.valueOf(split[2]).floatValue());
                    if (!this.mOpCutoutPath.isEmpty()) {
                        if (Integer.valueOf(split[3]).intValue() == 0) {
                            this.mPaint.setColor(-256);
                        } else {
                            this.mPaint.setColor(this.mColor);
                        }
                        this.mPaint.setStyle(Paint.Style.FILL);
                        this.mPaint.setAntiAlias(true);
                        canvas.drawPath(this.mOpCutoutPath, this.mPaint);
                        return;
                    }
                    return;
                }
            } else if (this.mDrawCutoutManually && !OpUtils.isCutoutEmulationEnabled()) {
                opUpdateCutoutPath(this.mCircleX, this.mCircleY, this.mCircleR);
                if (!this.mOpCutoutPath.isEmpty()) {
                    this.mPaint.setColor(this.mColor);
                    this.mPaint.setStyle(Paint.Style.FILL);
                    this.mPaint.setAntiAlias(true);
                    canvas.drawPath(this.mOpCutoutPath, this.mPaint);
                    return;
                }
                return;
            }
            if (!this.mBoundingPath.isEmpty()) {
                this.mPaint.setColor(this.mColor);
                this.mPaint.setStyle(Paint.Style.FILL);
                this.mPaint.setAntiAlias(true);
                canvas.drawPath(this.mBoundingPath, this.mPaint);
            }
            if (this.mCameraProtectionProgress > 0.5f && !this.mProtectionRect.isEmpty()) {
                float f = this.mCameraProtectionProgress;
                canvas.scale(f, f, this.mProtectionRect.centerX(), this.mProtectionRect.centerY());
                canvas.drawPath(this.mProtectionPath, this.mPaint);
            }
        }

        private void updateDebugPath() {
            DisplayInfo displayInfo = this.mInfo;
            int i = displayInfo.logicalWidth;
            int i2 = displayInfo.logicalHeight;
            int i3 = displayInfo.rotation;
            if (!(i3 == 1 || i3 == 3)) {
            }
            String str = SystemProperties.get("debug.cutout.display.pathdata", "");
            Log.d("ScreenDecorations", "updateDebugPath: " + str);
            this.mDebugPath.reset();
        }

        private void opUpdateCutoutPath(float f, float f2, float f3) {
            Context context;
            DisplayInfo displayInfo = this.mInfo;
            int i = displayInfo.logicalWidth;
            int i2 = displayInfo.logicalHeight;
            int i3 = displayInfo.rotation;
            boolean z = true;
            if (!(i3 == 1 || i3 == 3)) {
                z = false;
            }
            int i4 = z ? i2 : i;
            if (!z) {
                i = i2;
            }
            if (!ScreenDecorations.DEBUG_CUTOUT_CIRCLE_DISPLAY && (context = this.mContext) != null && OpUtils.isSupportResolutionSwitch(context)) {
                OpUtils.updateScreenResolutionManually(this.mContext);
                f = OpUtils.convertPxByResolutionProportionWithoutRound(f, 1440, i4);
                f2 = OpUtils.convertPxByResolutionProportionWithoutRound(f2, 1440, i4);
                f3 = OpUtils.convertPxByResolutionProportionWithoutRound(f3, 1440, i4);
            }
            if (ScreenDecorations.DEBUG) {
                Log.d("ScreenDecorations", "Draw cutout manually circle x: " + f + " y: " + f2 + " r: " + f3);
            }
            this.mOpCutoutPath.reset();
            this.mOpCutoutPath.addCircle(f, f2, f3, Path.Direction.CCW);
            Matrix matrix = new Matrix();
            transformPhysicalToLogicalCoordinates(this.mInfo.rotation, i4, i, matrix);
            this.mOpCutoutPath.transform(matrix);
        }

        @Override // android.hardware.display.DisplayManager.DisplayListener
        public void onDisplayChanged(final int i) {
            int i2 = ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isFacelockRecognizing() ? 500 : 400;
            if (OpUtils.isSupportHolePunchFrontCam() && !OpUtils.isCutoutEmulationEnabled()) {
                i2 = 100;
            }
            postDelayed(new Runnable() { // from class: com.android.systemui.ScreenDecorations.DisplayCutoutView.1
                @Override // java.lang.Runnable
                public void run() {
                    if (DisplayCutoutView.this.getDisplay() != null && i == DisplayCutoutView.this.getDisplay().getDisplayId()) {
                        DisplayCutoutView.this.update();
                    }
                }
            }, (long) i2);
        }

        private void update() {
            int i;
            if (isAttachedToWindow() && !this.mDecorations.mPendingRotationChange) {
                int rotation = this.mContext.getDisplay().getRotation();
                this.mRotation = rotation;
                this.mPosition = ScreenDecorations.getBoundPositionFromRotation(this.mInitialPosition, rotation);
                requestLayout();
                getDisplay().getDisplayInfo(this.mInfo);
                this.mBounds.clear();
                this.mBoundingRect.setEmpty();
                this.mBoundingPath.reset();
                if (!ScreenDecorations.shouldDrawCutout(getContext()) || !hasCutout()) {
                    i = 8;
                } else {
                    this.mBounds.addAll(this.mInfo.displayCutout.getBoundingRects());
                    localBounds(this.mBoundingRect);
                    updateGravity();
                    updateBoundingPath();
                    invalidate();
                    i = 0;
                }
                if (i != getVisibility()) {
                    setVisibility(i);
                }
            }
        }

        private void updateBoundingPath() {
            DisplayInfo displayInfo = this.mInfo;
            int i = displayInfo.logicalWidth;
            int i2 = displayInfo.logicalHeight;
            int i3 = displayInfo.rotation;
            boolean z = true;
            if (!(i3 == 1 || i3 == 3)) {
                z = false;
            }
            int i4 = z ? i2 : i;
            if (!z) {
                i = i2;
            }
            Path pathFromResources = DisplayCutout.pathFromResources(getResources(), i4, i);
            if (pathFromResources != null) {
                this.mBoundingPath.set(pathFromResources);
            } else {
                this.mBoundingPath.reset();
            }
            Matrix matrix = new Matrix();
            transformPhysicalToLogicalCoordinates(this.mInfo.rotation, i4, i, matrix);
            this.mBoundingPath.transform(matrix);
            Path path = this.mProtectionPathOrig;
            if (path != null) {
                this.mProtectionPath.set(path);
                this.mProtectionPath.transform(matrix);
                matrix.mapRect(this.mProtectionRect, this.mProtectionRectOrig);
            }
        }

        private static void transformPhysicalToLogicalCoordinates(int i, int i2, int i3, Matrix matrix) {
            if (i == 0) {
                matrix.reset();
            } else if (i == 1) {
                matrix.setRotate(270.0f);
                matrix.postTranslate(0.0f, (float) i2);
            } else if (i == 2) {
                matrix.setRotate(180.0f);
                matrix.postTranslate((float) i2, (float) i3);
            } else if (i == 3) {
                matrix.setRotate(90.0f);
                matrix.postTranslate((float) i3, 0.0f);
            } else {
                throw new IllegalArgumentException("Unknown rotation: " + i);
            }
        }

        private void updateGravity() {
            ViewGroup.LayoutParams layoutParams = getLayoutParams();
            if (layoutParams instanceof FrameLayout.LayoutParams) {
                FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) layoutParams;
                int gravity = getGravity(this.mInfo.displayCutout);
                if (layoutParams2.gravity != gravity) {
                    layoutParams2.gravity = gravity;
                    setLayoutParams(layoutParams2);
                }
            }
        }

        private boolean hasCutout() {
            DisplayCutout displayCutout = this.mInfo.displayCutout;
            if (displayCutout == null) {
                return false;
            }
            int i = this.mPosition;
            if (i == 0) {
                return !displayCutout.getBoundingRectLeft().isEmpty();
            }
            if (i == 1) {
                return !displayCutout.getBoundingRectTop().isEmpty();
            }
            if (i == 3) {
                return !displayCutout.getBoundingRectBottom().isEmpty();
            }
            if (i == 2) {
                return !displayCutout.getBoundingRectRight().isEmpty();
            }
            return false;
        }

        @Override // android.view.View
        public void onMeasure(int i, int i2) {
            if (this.mBounds.isEmpty()) {
                super.onMeasure(i, i2);
            } else if (this.mShowProtection) {
                this.mTotalBounds.union(this.mBoundingRect);
                Rect rect = this.mTotalBounds;
                RectF rectF = this.mProtectionRect;
                rect.union((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom);
                setMeasuredDimension(View.resolveSizeAndState(this.mTotalBounds.width(), i, 0), View.resolveSizeAndState(this.mTotalBounds.height(), i2, 0));
            } else {
                setMeasuredDimension(View.resolveSizeAndState(this.mBoundingRect.width(), i, 0), View.resolveSizeAndState(this.mBoundingRect.height(), i2, 0));
            }
        }

        public static void boundsFromDirection(DisplayCutout displayCutout, int i, Rect rect) {
            boundsFromDirection(null, displayCutout, i, rect);
        }

        public static void boundsFromDirection(Context context, DisplayCutout displayCutout, int i, Rect rect) {
            if (displayCutout != null) {
                if (i == 3) {
                    rect.set(displayCutout.getBoundingRectLeft());
                } else if (i == 5) {
                    rect.set(displayCutout.getBoundingRectRight());
                } else if (i == 48) {
                    rect.set(displayCutout.getBoundingRectTop());
                    if (context == null) {
                        return;
                    }
                    if (!(ScreenDecorations.mOpCustRegionleft == 0 && ScreenDecorations.mOpCustRegionRight == 0) && !OpUtils.isCutoutEmulationEnabled()) {
                        rect.left = ScreenDecorations.mOpCustRegionleft;
                        rect.right = ScreenDecorations.mOpCustRegionRight;
                    }
                } else if (i != 80) {
                    rect.setEmpty();
                } else {
                    rect.set(displayCutout.getBoundingRectBottom());
                }
            }
        }

        private void localBounds(Rect rect) {
            DisplayCutout displayCutout = this.mInfo.displayCutout;
            boundsFromDirection(this.mContext, displayCutout, getGravity(displayCutout), rect);
            if (OpUtils.isSupportHolePunchFrontCam() && rect.left != 0) {
                DisplayInfo displayInfo = this.mInfo;
                int i = displayInfo.logicalWidth;
                int i2 = displayInfo.logicalHeight;
                int i3 = displayInfo.rotation;
                boolean z = true;
                if (!(i3 == 1 || i3 == 3)) {
                    z = false;
                }
                int i4 = z ? i2 : i;
                if (!z) {
                    i = i2;
                }
                boundsFromDirection(this.mContext, DisplayCutout.fromResourcesRectApproximation(this.mContext.getResources(), i4, i), 48, rect);
                Log.d("ScreenDecorations", "localBounds height:" + rect.height());
            }
        }

        private int getGravity(DisplayCutout displayCutout) {
            int i = this.mPosition;
            if (i == 0) {
                if (!displayCutout.getBoundingRectLeft().isEmpty()) {
                    return 3;
                }
                return 0;
            } else if (i == 1) {
                if (!displayCutout.getBoundingRectTop().isEmpty()) {
                    return 48;
                }
                return 0;
            } else if (i != 3) {
                return (i != 2 || displayCutout.getBoundingRectRight().isEmpty()) ? 0 : 5;
            } else {
                if (!displayCutout.getBoundingRectBottom().isEmpty()) {
                    return 80;
                }
                return 0;
            }
        }

        @Override // com.android.systemui.RegionInterceptingFrameLayout.RegionInterceptableView
        public Region getInterceptRegion() {
            if (this.mInfo.displayCutout == null) {
                return null;
            }
            View rootView = getRootView();
            Region rectsToRegion = ScreenDecorations.rectsToRegion(this.mInfo.displayCutout.getBoundingRects());
            rootView.getLocationOnScreen(this.mLocation);
            int[] iArr = this.mLocation;
            rectsToRegion.translate(-iArr[0], -iArr[1]);
            rectsToRegion.op(rootView.getLeft(), rootView.getTop(), rootView.getRight(), rootView.getBottom(), Region.Op.INTERSECT);
            return rectsToRegion;
        }
    }

    public class ValidatingPreDrawListener implements ViewTreeObserver.OnPreDrawListener {
        private final View mView;

        public ValidatingPreDrawListener(View view, int i) {
            ScreenDecorations.this = r1;
            this.mView = view;
        }

        @Override // android.view.ViewTreeObserver.OnPreDrawListener
        public boolean onPreDraw() {
            int rotation = ScreenDecorations.this.mContext.getDisplay().getRotation();
            if (rotation == ScreenDecorations.this.mRotation || ScreenDecorations.this.mPendingRotationChange) {
                return true;
            }
            if (ScreenDecorations.DEBUG) {
                Log.i("ScreenDecorations", "Drawing rot " + ScreenDecorations.this.mRotation + ", but display is at rot " + rotation + ". Restarting draw");
            }
            this.mView.invalidate();
            return false;
        }
    }

    private void initRoundCornerView() {
        View[] viewArr = this.mOverlays;
        boolean z = true;
        if (viewArr[1] == null || viewArr[3] == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("initRoundCornerView failed, top null: ");
            sb.append(this.mOverlays[1] == null);
            sb.append(" bottom null: ");
            if (this.mOverlays[3] != null) {
                z = false;
            }
            sb.append(z);
            Log.d("ScreenDecorations", sb.toString());
            return;
        }
        ((ImageView) viewArr[1].findViewById(C0008R$id.left)).setImageResource(C0006R$drawable.rounded_top);
        ((ImageView) this.mOverlays[1].findViewById(C0008R$id.right)).setImageResource(C0006R$drawable.rounded_top);
        ((ImageView) this.mOverlays[3].findViewById(C0008R$id.left)).setImageResource(C0006R$drawable.rounded_bottom);
        ((ImageView) this.mOverlays[3].findViewById(C0008R$id.right)).setImageResource(C0006R$drawable.rounded_bottom);
        if (OpUtils.isSupportResolutionSwitch(this.mContext)) {
            float f = this.mResizeRatio;
            this.mRoundedDefaultTopResize = (int) (((float) this.mRoundedDefaultTop) * f);
            this.mRoundedDefaultBottomResize = (int) (((float) this.mRoundedDefaultBottom) * f);
            this.mRoundedDefaultTopWidthResize = (int) (((float) this.mRoundedDefaultTopWidth) * f);
            this.mRoundedDefaultBottomWidthResize = (int) (((float) this.mRoundedDefaultBottomWidth) * f);
        }
        updateDecorSize();
    }

    public void updateDecorSize() {
        int i;
        int i2;
        int i3;
        int i4;
        View[] viewArr = this.mOverlays;
        View view = viewArr[1];
        View view2 = viewArr[3];
        if (view != null && view2 != null) {
            if (OpUtils.isSupportResolutionSwitch(this.mContext)) {
                int i5 = this.mScreenResolution;
                if (i5 == 0 || i5 == 2) {
                    i4 = this.mRoundedDefaultTop;
                    i3 = this.mRoundedDefaultBottom;
                    i2 = this.mRoundedDefaultTopWidth;
                    i = this.mRoundedDefaultBottomWidth;
                } else {
                    i4 = this.mRoundedDefaultTopResize;
                    i3 = this.mRoundedDefaultBottomResize;
                    i2 = this.mRoundedDefaultTopWidthResize;
                    i = this.mRoundedDefaultBottomWidthResize;
                }
            } else {
                i4 = this.mRoundedDefaultTop;
                i3 = this.mRoundedDefaultBottom;
                i2 = this.mRoundedDefaultTopWidth;
                i = this.mRoundedDefaultBottomWidth;
            }
            if (isRectangleTop() || isRectangleBottom() || this.mLastSizeTop != i4 || this.mLastSizeBottom != i3) {
                this.mLastSizeTop = i4;
                this.mLastSizeBottom = i3;
                Log.d("ScreenDecorations", "updateDecorSize top " + this.mLastSizeTop + " bottom " + this.mLastSizeBottom + " mRotation " + this.mRotation);
                if (i2 != 0) {
                    int i6 = this.mRotation;
                    if (i6 == 1 || i6 == 3) {
                        i2 = i4;
                        i4 = i2;
                    }
                    setSize(view.findViewById(C0008R$id.left), i4, i2);
                    setSize(view.findViewById(C0008R$id.right), i4, i2);
                } else {
                    setSize(view.findViewById(C0008R$id.left), i4);
                    setSize(view.findViewById(C0008R$id.right), i4);
                }
                if (i != 0) {
                    int i7 = this.mRotation;
                    if (i7 == 1 || i7 == 3) {
                        i = i3;
                        i3 = i;
                    }
                    setSize(view2.findViewById(C0008R$id.left), i3, i);
                    setSize(view2.findViewById(C0008R$id.right), i3, i);
                    return;
                }
                setSize(view2.findViewById(C0008R$id.left), i3);
                setSize(view2.findViewById(C0008R$id.right), i3);
            }
        }
    }

    public final class SettingsObserver extends ContentObserver {
        private final Uri mOPScreenResolutionUri = Settings.Global.getUriFor("oneplus_screen_resolution_adjust");

        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        public SettingsObserver() {
            super(new Handler());
            ScreenDecorations.this = r4;
            r4.mContext.getContentResolver().registerContentObserver(this.mOPScreenResolutionUri, false, this, -1);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri) {
            if (uri != null) {
                ScreenDecorations screenDecorations = ScreenDecorations.this;
                screenDecorations.mScreenResolution = Settings.Global.getInt(screenDecorations.mContext.getContentResolver(), "oneplus_screen_resolution_adjust", 2);
            }
        }
    }

    private static Bitmap rotateBitmap(int i, Bitmap bitmap) {
        if (bitmap == null) {
            Log.d("ScreenDecorations", "bitmap is null");
            return null;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate((float) i);
        Bitmap createBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if (!(createBitmap == bitmap || bitmap == null || bitmap.isRecycled())) {
            bitmap.recycle();
        }
        return createBitmap;
    }

    private boolean isRectangleTop() {
        int i = this.mRoundedDefaultTopWidth;
        return (i == 0 || this.mRoundedDefaultTop == i) ? false : true;
    }

    private boolean isRectangleBottom() {
        int i = this.mRoundedDefaultBottomWidth;
        return (i == 0 || this.mRoundedDefaultBottom == i) ? false : true;
    }
}
