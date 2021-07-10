package com.android.systemui.statusbar.policy;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.hardware.input.InputManager;
import android.media.AudioManager;
import android.metrics.LogMaker;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.UiEventLogger;
import com.android.internal.logging.UiEventLoggerImpl;
import com.android.systemui.C0008R$id;
import com.android.systemui.Dependency;
import com.android.systemui.R$styleable;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.recents.OverviewProxyService;
import com.android.systemui.shared.system.QuickStepContract;
import com.android.systemui.statusbar.phone.ButtonInterface;
import com.android.systemui.statusbar.policy.KeyButtonRipple;
import com.oneplus.util.OpNavBarUtils;
public class KeyButtonView extends ImageView implements ButtonInterface {
    private static final boolean DEBUG = Build.DEBUG_ONEPLUS;
    private static final String TAG = KeyButtonView.class.getSimpleName();
    private AudioManager mAudioManager;
    private final Runnable mCheckLongPress;
    private int mCode;
    private int mContentDescriptionRes;
    private float mDarkIntensity;
    private long mDownTime;
    private boolean mGestureAborted;
    private boolean mHasOvalBg;
    private final InputManager mInputManager;
    private int mKey;
    @VisibleForTesting
    boolean mLongClicked;
    private final MetricsLogger mMetricsLogger;
    private View.OnClickListener mOnClickListener;
    private final Paint mOvalBgPaint;
    private final OverviewProxyService mOverviewProxyService;
    private final boolean mPlaySounds;
    private final KeyButtonRipple mRipple;
    private int mThemeColor;
    private int mTouchDownX;
    private int mTouchDownY;
    private final UiEventLogger mUiEventLogger;

    @Override // com.android.systemui.statusbar.phone.ButtonInterface
    public void setVertical(boolean z) {
    }

    @VisibleForTesting
    public enum NavBarButtonEvent implements UiEventLogger.UiEventEnum {
        NAVBAR_HOME_BUTTON_TAP(533),
        NAVBAR_BACK_BUTTON_TAP(534),
        NAVBAR_OVERVIEW_BUTTON_TAP(535),
        NAVBAR_HOME_BUTTON_LONGPRESS(536),
        NAVBAR_BACK_BUTTON_LONGPRESS(537),
        NAVBAR_OVERVIEW_BUTTON_LONGPRESS(538),
        NONE(0);
        
        private final int mId;

        private NavBarButtonEvent(int i) {
            this.mId = i;
        }

        public int getId() {
            return this.mId;
        }
    }

    public KeyButtonView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public KeyButtonView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, InputManager.getInstance(), new UiEventLoggerImpl());
    }

    @VisibleForTesting
    public KeyButtonView(Context context, AttributeSet attributeSet, int i, InputManager inputManager, UiEventLogger uiEventLogger) {
        super(context, attributeSet);
        this.mMetricsLogger = (MetricsLogger) Dependency.get(MetricsLogger.class);
        this.mOvalBgPaint = new Paint(3);
        this.mHasOvalBg = false;
        this.mKey = 3;
        this.mThemeColor = 0;
        this.mCheckLongPress = new Runnable() { // from class: com.android.systemui.statusbar.policy.KeyButtonView.1
            @Override // java.lang.Runnable
            public void run() {
                if (!KeyButtonView.this.isPressed()) {
                    return;
                }
                if (KeyButtonView.this.isLongClickable()) {
                    KeyButtonView.this.performLongClick();
                    KeyButtonView.this.mLongClicked = true;
                    return;
                }
                if (KeyButtonView.DEBUG) {
                    String str = KeyButtonView.TAG;
                    Log.i(str, "Logpress mCode: " + KeyButtonView.this.mCode);
                }
                KeyButtonView.this.sendEvent(0, 128);
                KeyButtonView.this.sendAccessibilityEvent(2);
                KeyButtonView.this.mLongClicked = true;
            }
        };
        this.mUiEventLogger = uiEventLogger;
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.KeyButtonView, i, 0);
        this.mCode = obtainStyledAttributes.getInteger(R$styleable.KeyButtonView_keyCode, 0);
        this.mPlaySounds = obtainStyledAttributes.getBoolean(R$styleable.KeyButtonView_playSound, true);
        TypedValue typedValue = new TypedValue();
        if (obtainStyledAttributes.getValue(R$styleable.KeyButtonView_android_contentDescription, typedValue)) {
            this.mContentDescriptionRes = typedValue.resourceId;
        }
        obtainStyledAttributes.recycle();
        setClickable(true);
        this.mAudioManager = (AudioManager) context.getSystemService("audio");
        this.mRipple = new KeyButtonRipple(context, this);
        this.mOverviewProxyService = (OverviewProxyService) Dependency.get(OverviewProxyService.class);
        this.mInputManager = inputManager;
        setBackground(this.mRipple);
        setWillNotDraw(false);
        forceHasOverlappingRendering(false);
        this.mKey = determineKey();
        if (!OpNavBarUtils.isSupportCustomKeys() && this.mCode == 187) {
            this.mCode = 0;
        }
    }

    @Override // android.view.View
    public boolean isClickable() {
        return this.mCode != 0 || super.isClickable();
    }

    public void setCode(int i) {
        this.mCode = i;
    }

    @Override // android.view.View
    public void setOnClickListener(View.OnClickListener onClickListener) {
        super.setOnClickListener(onClickListener);
        this.mOnClickListener = onClickListener;
    }

    public void loadAsync(Icon icon) {
        new AsyncTask<Icon, Void, Drawable>() { // from class: com.android.systemui.statusbar.policy.KeyButtonView.2
            /* access modifiers changed from: protected */
            public Drawable doInBackground(Icon... iconArr) {
                return iconArr[0].loadDrawable(((ImageView) KeyButtonView.this).mContext);
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(Drawable drawable) {
                KeyButtonView.this.setImageDrawable(drawable);
            }
        }.execute(icon);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        int i = this.mContentDescriptionRes;
        if (i != 0) {
            setContentDescription(((ImageView) this).mContext.getString(i));
        }
    }

    @Override // android.view.View
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        if (this.mCode != 0) {
            accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(16, null));
            if (isLongClickable()) {
                accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(32, null));
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onWindowVisibilityChanged(int i) {
        super.onWindowVisibilityChanged(i);
        if (i != 0) {
            jumpDrawablesToCurrentState();
        }
    }

    public boolean performAccessibilityActionInternal(int i, Bundle bundle) {
        if (i == 16 && this.mCode != 0) {
            sendEvent(0, 0, SystemClock.uptimeMillis());
            sendEvent(1, 0);
            sendAccessibilityEvent(1);
            playSoundEffect(0);
            return true;
        } else if (i != 32 || this.mCode == 0) {
            return super.performAccessibilityActionInternal(i, bundle);
        } else {
            sendEvent(0, 128);
            sendEvent(1, 0);
            sendAccessibilityEvent(2);
            return true;
        }
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        View.OnClickListener onClickListener;
        String str = TAG;
        boolean shouldShowSwipeUpUI = this.mOverviewProxyService.shouldShowSwipeUpUI();
        int action = motionEvent.getAction();
        if (action == 0) {
            this.mGestureAborted = false;
        }
        if (this.mGestureAborted) {
            setPressed(false);
            return false;
        }
        long uptimeMillis = SystemClock.uptimeMillis();
        if (action == 0) {
            this.mDownTime = SystemClock.uptimeMillis();
            this.mLongClicked = false;
            setPressed(true);
            this.mTouchDownX = (int) motionEvent.getRawX();
            this.mTouchDownY = (int) motionEvent.getRawY();
            if (this.mCode != 0) {
                if (DEBUG) {
                    Log.i(str, "ACTION_DOWN mCode: " + this.mCode);
                }
                sendEvent(0, 0, this.mDownTime);
            } else {
                performHapticFeedback(1);
            }
            if (!shouldShowSwipeUpUI) {
                playSoundEffect(0);
            }
            removeCallbacks(this.mCheckLongPress);
            postDelayed(this.mCheckLongPress, (long) ViewConfiguration.getLongPressTimeout());
        } else if (action == 1) {
            boolean z = isPressed() && !this.mLongClicked;
            setPressed(false);
            boolean z2 = SystemClock.uptimeMillis() - this.mDownTime > 150;
            if (shouldShowSwipeUpUI) {
                if (z) {
                    performHapticFeedback(1);
                    playSoundEffect(0);
                }
            } else if (z2 && !this.mLongClicked) {
                performHapticFeedback(8);
            }
            if (this.mCode != 0) {
                if (z) {
                    if (DEBUG) {
                        Log.i(str, "ACTION_UP mCode: " + this.mCode);
                    }
                    sendEvent(1, 0);
                    sendAccessibilityEvent(1);
                } else {
                    sendEvent(1, 32);
                }
            } else if (z && (onClickListener = this.mOnClickListener) != null) {
                onClickListener.onClick(this);
                sendAccessibilityEvent(1);
            }
            removeCallbacks(this.mCheckLongPress);
        } else if (action == 2) {
            int rawY = (int) motionEvent.getRawY();
            float quickStepTouchSlopPx = QuickStepContract.getQuickStepTouchSlopPx(getContext());
            if (((float) Math.abs(((int) motionEvent.getRawX()) - this.mTouchDownX)) > quickStepTouchSlopPx || ((float) Math.abs(rawY - this.mTouchDownY)) > quickStepTouchSlopPx) {
                setPressed(false);
                removeCallbacks(this.mCheckLongPress);
            }
        } else if (action == 3) {
            setPressed(false);
            if (this.mCode != 0) {
                sendEvent(1, 32);
            }
            removeCallbacks(this.mCheckLongPress);
        }
        long uptimeMillis2 = SystemClock.uptimeMillis() - uptimeMillis;
        if (uptimeMillis2 > 1000 && this.mKey != 3) {
            Log.d(str, "process event spent a long time. KEY:" + this.mKey + ", action:" + action + ", costTime:" + uptimeMillis2);
        }
        return true;
    }

    @Override // android.widget.ImageView, com.android.systemui.statusbar.phone.ButtonInterface
    public void setImageDrawable(Drawable drawable) {
        KeyButtonRipple.Type type;
        super.setImageDrawable(drawable);
        if (drawable != null && (drawable instanceof KeyButtonDrawable)) {
            KeyButtonDrawable keyButtonDrawable = (KeyButtonDrawable) drawable;
            keyButtonDrawable.setDarkIntensity(this.mDarkIntensity);
            boolean hasOvalBg = keyButtonDrawable.hasOvalBg();
            this.mHasOvalBg = hasOvalBg;
            if (hasOvalBg) {
                this.mOvalBgPaint.setColor(keyButtonDrawable.getDrawableBackgroundColor());
            }
            KeyButtonRipple keyButtonRipple = this.mRipple;
            if (keyButtonDrawable.hasOvalBg()) {
                type = KeyButtonRipple.Type.OVAL;
            } else {
                type = KeyButtonRipple.Type.ROUNDED_RECT;
            }
            keyButtonRipple.setType(type);
            if (OpNavBarUtils.isSupportCustomNavBar()) {
                updateThemeColorInternal();
            }
        }
    }

    @Override // android.view.View
    public void playSoundEffect(int i) {
        if (this.mPlaySounds) {
            this.mAudioManager.playSoundEffect(i, ActivityManager.getCurrentUser());
        }
    }

    public void sendEvent(int i, int i2) {
        sendEvent(i, i2, SystemClock.uptimeMillis());
    }

    /* JADX WARNING: Removed duplicated region for block: B:34:0x0049  */
    /* JADX WARNING: Removed duplicated region for block: B:37:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void logSomePresses(int r4, int r5) {
        /*
            r3 = this;
            r0 = r5 & 128(0x80, float:1.794E-43)
            r1 = 1
            if (r0 == 0) goto L_0x0007
            r0 = r1
            goto L_0x0008
        L_0x0007:
            r0 = 0
        L_0x0008:
            com.android.systemui.statusbar.policy.KeyButtonView$NavBarButtonEvent r2 = com.android.systemui.statusbar.policy.KeyButtonView.NavBarButtonEvent.NONE
            if (r4 != r1) goto L_0x0011
            boolean r1 = r3.mLongClicked
            if (r1 == 0) goto L_0x0011
            return
        L_0x0011:
            if (r4 != 0) goto L_0x0016
            if (r0 != 0) goto L_0x0016
            return
        L_0x0016:
            r4 = r5 & 32
            if (r4 != 0) goto L_0x004e
            r4 = r5 & 256(0x100, float:3.59E-43)
            if (r4 == 0) goto L_0x001f
            goto L_0x004e
        L_0x001f:
            int r4 = r3.mCode
            r5 = 3
            if (r4 == r5) goto L_0x003d
            r5 = 4
            if (r4 == r5) goto L_0x0035
            r5 = 187(0xbb, float:2.62E-43)
            if (r4 == r5) goto L_0x002c
            goto L_0x0045
        L_0x002c:
            if (r0 == 0) goto L_0x0031
            com.android.systemui.statusbar.policy.KeyButtonView$NavBarButtonEvent r4 = com.android.systemui.statusbar.policy.KeyButtonView.NavBarButtonEvent.NAVBAR_OVERVIEW_BUTTON_LONGPRESS
            goto L_0x0033
        L_0x0031:
            com.android.systemui.statusbar.policy.KeyButtonView$NavBarButtonEvent r4 = com.android.systemui.statusbar.policy.KeyButtonView.NavBarButtonEvent.NAVBAR_OVERVIEW_BUTTON_TAP
        L_0x0033:
            r2 = r4
            goto L_0x0045
        L_0x0035:
            if (r0 == 0) goto L_0x003a
            com.android.systemui.statusbar.policy.KeyButtonView$NavBarButtonEvent r4 = com.android.systemui.statusbar.policy.KeyButtonView.NavBarButtonEvent.NAVBAR_BACK_BUTTON_LONGPRESS
            goto L_0x0033
        L_0x003a:
            com.android.systemui.statusbar.policy.KeyButtonView$NavBarButtonEvent r4 = com.android.systemui.statusbar.policy.KeyButtonView.NavBarButtonEvent.NAVBAR_BACK_BUTTON_TAP
            goto L_0x0033
        L_0x003d:
            if (r0 == 0) goto L_0x0042
            com.android.systemui.statusbar.policy.KeyButtonView$NavBarButtonEvent r4 = com.android.systemui.statusbar.policy.KeyButtonView.NavBarButtonEvent.NAVBAR_HOME_BUTTON_LONGPRESS
            goto L_0x0033
        L_0x0042:
            com.android.systemui.statusbar.policy.KeyButtonView$NavBarButtonEvent r4 = com.android.systemui.statusbar.policy.KeyButtonView.NavBarButtonEvent.NAVBAR_HOME_BUTTON_TAP
            goto L_0x0033
        L_0x0045:
            com.android.systemui.statusbar.policy.KeyButtonView$NavBarButtonEvent r4 = com.android.systemui.statusbar.policy.KeyButtonView.NavBarButtonEvent.NONE
            if (r2 == r4) goto L_0x004e
            com.android.internal.logging.UiEventLogger r3 = r3.mUiEventLogger
            r3.log(r2)
        L_0x004e:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.policy.KeyButtonView.logSomePresses(int, int):void");
    }

    private void sendEvent(int i, int i2, long j) {
        this.mMetricsLogger.write(new LogMaker(931).setType(4).setSubtype(this.mCode).addTaggedData(933, Integer.valueOf(i)).addTaggedData(932, Integer.valueOf(i2)));
        logSomePresses(i, i2);
        if (this.mCode == 4 && i2 != 128) {
            String str = TAG;
            Log.i(str, "Back button event: " + KeyEvent.actionToString(i));
            if (i == 1) {
                this.mOverviewProxyService.notifyBackAction((i2 & 32) == 0, -1, -1, true, false);
            }
        }
        KeyEvent keyEvent = new KeyEvent(this.mDownTime, j, i, this.mCode, (i2 & 128) != 0 ? 1 : 0, 0, -1, 0, i2 | 8 | 64, 257);
        int displayId = getDisplay() != null ? getDisplay().getDisplayId() : -1;
        int expandedDisplayId = ((BubbleController) Dependency.get(BubbleController.class)).getExpandedDisplayId(((ImageView) this).mContext);
        if (this.mCode == 4 && expandedDisplayId != -1) {
            displayId = expandedDisplayId;
        }
        if (displayId != -1) {
            keyEvent.setDisplayId(displayId);
        }
        this.mInputManager.injectInputEvent(keyEvent, 0);
    }

    public void setRippleColor(int i) {
        KeyButtonRipple keyButtonRipple = this.mRipple;
        if (keyButtonRipple != null) {
            keyButtonRipple.setColor(i);
        }
    }

    public void updateThemeColor(int i) {
        this.mThemeColor = i;
        updateThemeColorInternal();
    }

    private void updateThemeColorInternal() {
        Drawable drawable = getDrawable();
        if (drawable != null && (drawable instanceof KeyButtonDrawable)) {
            ((KeyButtonDrawable) drawable).updateThemeColor(this.mThemeColor);
        }
        postInvalidate();
    }

    @Override // com.android.systemui.statusbar.phone.ButtonInterface
    public void abortCurrentGesture() {
        Log.d("b/63783866", "KeyButtonView.abortCurrentGesture");
        setPressed(false);
        this.mRipple.abortDelayedRipple();
        this.mGestureAborted = true;
    }

    @Override // com.android.systemui.statusbar.phone.ButtonInterface
    public void setDarkIntensity(float f) {
        this.mDarkIntensity = f;
        Drawable drawable = getDrawable();
        if (drawable != null && (drawable instanceof KeyButtonDrawable)) {
            ((KeyButtonDrawable) drawable).setDarkIntensity(f);
            invalidate();
        }
        this.mRipple.setDarkIntensity(f);
    }

    @Override // com.android.systemui.statusbar.phone.ButtonInterface
    public void setDelayTouchFeedback(boolean z) {
        this.mRipple.setDelayTouchFeedback(z);
    }

    @Override // android.view.View
    public void draw(Canvas canvas) {
        if (this.mHasOvalBg) {
            canvas.save();
            canvas.translate((float) ((getLeft() + getRight()) / 2), (float) ((getTop() + getBottom()) / 2));
            int min = Math.min(getWidth(), getHeight()) / 2;
            float f = (float) (-min);
            float f2 = (float) min;
            canvas.drawOval(f, f, f2, f2, this.mOvalBgPaint);
            canvas.restore();
        }
        super.draw(canvas);
    }

    @Override // android.view.View
    public boolean isLongClickable() {
        if (!OpNavBarUtils.isSupportCustomKeys() || this.mKey == 3) {
            return super.isLongClickable();
        }
        return false;
    }

    public int determineKey() {
        int id = getId();
        if (id == C0008R$id.back) {
            return 0;
        }
        if (id == C0008R$id.home) {
            return 1;
        }
        return id == C0008R$id.recent_apps ? 2 : 3;
    }
}
