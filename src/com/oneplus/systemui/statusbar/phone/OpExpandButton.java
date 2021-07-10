package com.oneplus.systemui.statusbar.phone;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.DisplayCutout;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0011R$layout;
import com.android.systemui.C0016R$style;
import com.android.systemui.assist.ui.DisplayUtils;
import com.oneplus.systemui.statusbar.phone.OpExpandButtonLayout;
import com.oneplus.util.OpUtils;
import java.util.Iterator;
import java.util.List;
public class OpExpandButton {
    private int mButtonHeight;
    private int mButtonWidth;
    private Context mContext;
    private GameModeObserver mGameModeObserver;
    private final Handler mHandler = new Handler() { // from class: com.oneplus.systemui.statusbar.phone.OpExpandButton.1
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            if (message.what == 1000) {
                OpExpandButton.this.dismiss();
            }
        }
    };
    private boolean mIsShow;
    private OnExpandButtonListener mOnExpandButtonListener;
    private ScreenBroadcastReceiver mScreenReceiver;
    private View mView;
    protected WindowManager mWindowManager;

    public interface OnExpandButtonListener {
        void onExpandButtonClick();

        void onOutSideClick(float f);
    }

    public OpExpandButton(Context context) {
        this.mContext = context;
        this.mButtonHeight = context.getResources().getDimensionPixelSize(C0005R$dimen.expand_button_height);
        this.mButtonWidth = context.getResources().getDimensionPixelSize(C0005R$dimen.expand_button_width);
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        this.mGameModeObserver = new GameModeObserver();
        this.mView = initContentView();
    }

    /* access modifiers changed from: protected */
    public View initContentView() {
        View inflate = LayoutInflater.from(this.mContext).inflate(C0011R$layout.expand_button_layout, (ViewGroup) null);
        OpExpandButtonLayout opExpandButtonLayout = (OpExpandButtonLayout) inflate.findViewById(C0008R$id.expand_button_layout_container);
        opExpandButtonLayout.setOnConfigurationChangeListener(new OpExpandButtonLayout.OnConfigurationChangeListener() { // from class: com.oneplus.systemui.statusbar.phone.-$$Lambda$OpExpandButton$8EePPRB4CXv_EDcTM-nBxsLCk-A
            @Override // com.oneplus.systemui.statusbar.phone.OpExpandButtonLayout.OnConfigurationChangeListener
            public final void onConfigurationChanged(Configuration configuration) {
                OpExpandButton.this.lambda$initContentView$0$OpExpandButton(configuration);
            }
        });
        opExpandButtonLayout.setOnClickListener(new View.OnClickListener() { // from class: com.oneplus.systemui.statusbar.phone.-$$Lambda$OpExpandButton$POgYuTcrqhVlayFWiXkFoZ-wuwU
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                OpExpandButton.this.lambda$initContentView$1$OpExpandButton(view);
            }
        });
        inflate.setOnTouchListener(new View.OnTouchListener() { // from class: com.oneplus.systemui.statusbar.phone.-$$Lambda$OpExpandButton$Pu_P-U0dD5_FwzjeR75IF5wPo_c
            @Override // android.view.View.OnTouchListener
            public final boolean onTouch(View view, MotionEvent motionEvent) {
                return OpExpandButton.this.lambda$initContentView$2$OpExpandButton(view, motionEvent);
            }
        });
        return inflate;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$initContentView$0 */
    public /* synthetic */ void lambda$initContentView$0$OpExpandButton(Configuration configuration) {
        dismiss();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$initContentView$1 */
    public /* synthetic */ void lambda$initContentView$1$OpExpandButton(View view) {
        dismiss();
        OnExpandButtonListener onExpandButtonListener = this.mOnExpandButtonListener;
        if (onExpandButtonListener != null) {
            onExpandButtonListener.onExpandButtonClick();
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$initContentView$2 */
    public /* synthetic */ boolean lambda$initContentView$2$OpExpandButton(View view, MotionEvent motionEvent) {
        OnExpandButtonListener onExpandButtonListener;
        if (motionEvent.getAction() != 4 || (onExpandButtonListener = this.mOnExpandButtonListener) == null) {
            return false;
        }
        onExpandButtonListener.onOutSideClick(motionEvent.getY());
        return false;
    }

    /* access modifiers changed from: protected */
    public void addViewToWindow(View view) {
        int i;
        List<Rect> boundingRects;
        Log.d("ExpandButton", "addViewToWindow width：" + this.mContext.getResources().getDimensionPixelOffset(C0005R$dimen.expand_button_width) + ",height:" + this.mContext.getResources().getDimensionPixelOffset(C0005R$dimen.expand_button_height));
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-2, -2, 2018, 262920, -2);
        int dimensionPixelSize = this.mContext.getResources().getDimensionPixelSize(17105481);
        int i2 = this.mButtonHeight;
        int i3 = (dimensionPixelSize - i2) >> 1;
        int i4 = this.mButtonWidth;
        int width = (DisplayUtils.getWidth(this.mContext) >> 1) - (i4 >> 1);
        Rect rect = new Rect(width, i3, i4 + width, i2 + i3);
        DisplayCutout cutout = this.mWindowManager.getDefaultDisplay().getCutout();
        if (cutout != null && (boundingRects = cutout.getBoundingRects()) != null && boundingRects.size() > 0) {
            Iterator<Rect> it = boundingRects.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                Rect next = it.next();
                boolean intersects = Rect.intersects(next, rect);
                Log.d("ExpandButton", "intersects:" + intersects + ", cutoutRect:" + next + ", buttonRect:" + rect);
                if (intersects) {
                    i = cutout.getSafeInsetTop();
                    break;
                }
            }
        }
        i = 0;
        layoutParams.gravity = 49;
        layoutParams.privateFlags |= 18;
        layoutParams.y = i3 + i;
        layoutParams.windowAnimations = C0016R$style.ExpandButtonAnimation;
        registerScreenReceiver();
        this.mWindowManager.addView(view, layoutParams);
        this.mIsShow = true;
    }

    private void removeViewFromWindow() {
        WindowManager windowManager;
        Log.d("ExpandButton", "removeViewFromWindow");
        View view = this.mView;
        if (!(view == null || (windowManager = this.mWindowManager) == null)) {
            windowManager.removeViewImmediate(view);
        }
        this.mIsShow = false;
        this.mGameModeObserver.register(false);
        unregisterScreenReceiver();
    }

    public void show() {
        if (!this.mIsShow) {
            if (this.mView == null) {
                this.mView = initContentView();
            }
            this.mGameModeObserver.register(true);
            addViewToWindow(this.mView);
        } else {
            this.mHandler.removeMessages(1000);
        }
        this.mHandler.sendEmptyMessageDelayed(1000, 5000);
    }

    public void dismiss() {
        if (this.mIsShow) {
            this.mHandler.removeMessages(1000);
            removeViewFromWindow();
        }
    }

    public boolean isShow() {
        return this.mIsShow;
    }

    public void setmOnExpandButtonListener(OnExpandButtonListener onExpandButtonListener) {
        this.mOnExpandButtonListener = onExpandButtonListener;
    }

    /* access modifiers changed from: private */
    public final class GameModeObserver extends ContentObserver {
        private final Uri gameModeUri = Settings.System.getUriFor("game_mode_status");

        public GameModeObserver() {
            super(new Handler(Looper.getMainLooper()));
        }

        public void register(boolean z) {
            ContentResolver contentResolver = OpExpandButton.this.mContext.getContentResolver();
            if (z) {
                contentResolver.registerContentObserver(this.gameModeUri, false, this);
            } else {
                contentResolver.unregisterContentObserver(this);
            }
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri) {
            super.onChange(z, uri);
            boolean isGameModeOn = OpUtils.isGameModeOn(OpExpandButton.this.mContext);
            Log.d("ExpandButton", "onChange game mode on ? " + isGameModeOn);
            if (!isGameModeOn) {
                OpExpandButton.this.dismiss();
            }
        }
    }

    /* access modifiers changed from: private */
    public class ScreenBroadcastReceiver extends BroadcastReceiver {
        private ScreenBroadcastReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i("ExpandButton", "onReceive action:" + action);
            if ("android.intent.action.SCREEN_OFF".equals(action)) {
                OpExpandButton.this.dismiss();
            }
        }
    }

    private void registerScreenReceiver() {
        if (this.mScreenReceiver == null) {
            this.mScreenReceiver = new ScreenBroadcastReceiver();
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        this.mContext.registerReceiver(this.mScreenReceiver, intentFilter);
    }

    private void unregisterScreenReceiver() {
        ScreenBroadcastReceiver screenBroadcastReceiver = this.mScreenReceiver;
        if (screenBroadcastReceiver != null) {
            try {
                this.mContext.unregisterReceiver(screenBroadcastReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.mScreenReceiver = null;
        }
    }
}
