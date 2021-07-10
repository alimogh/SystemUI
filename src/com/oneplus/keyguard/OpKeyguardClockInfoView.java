package com.oneplus.keyguard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.keyguard.KeyguardAssistantView;
import com.android.systemui.C0004R$color;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0011R$layout;
import com.android.systemui.C0016R$style;
import com.android.systemui.Dependency;
import com.android.systemui.R$styleable;
import com.android.systemui.assist.ui.DisplayUtils;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.settings.CurrentUserTracker;
import com.android.systemui.statusbar.phone.StatusBar;
import com.oneplus.aod.OpClockViewCtrl;
import com.oneplus.aod.utils.OpAodSettings;
import com.oneplus.aod.views.IOpAodClock;
import com.oneplus.util.OpUtils;
import java.util.Locale;
public class OpKeyguardClockInfoView extends LinearLayout implements IOpAodClock {
    private boolean mAllowShowSensitiveData;
    private ViewGroup mAodSliceViewContainer;
    private ContentObserver mContentObserver;
    private Context mContext;
    private boolean[] mDebugRaiseCrashRate;
    private int mDensityDpi;
    private boolean mHasWindowFocus;
    private ImageView mIcon;
    private LinearLayout mInnerPanel;
    private boolean mIsAodSliceOn;
    private boolean mIsFormat12Hour;
    private ContentObserver mIsFormat12HourObserver;
    private KeyguardAssistantView mKeyguardAssistantView;
    public KeyguardAssistantView.Callback mKeyguardAssistantViewCallback;
    private TextView mPrimary;
    private final BroadcastReceiver mReceiver;
    private TextView mRemark;
    private TextView mSecondary;
    private TextView mTextViewDateLineThree;
    private OpKeyguardOneplusTextView mTextViewDateOfWeekLineOne;
    private OpKeyguardOneplusTextView mTextViewTimeLineTwo;
    private Handler mUiHandler;
    private int mUser;
    private CurrentUserTracker mUserTracker;
    private ViewTypeEnum mViewType;

    public enum ViewTypeEnum {
        keyguardOne,
        keyguardTwo,
        aod,
        qs;
        
        private static ViewTypeEnum[] allValues = values();

        public static ViewTypeEnum fromOrdinal(int i) {
            return allValues[i];
        }
    }

    public OpKeyguardClockInfoView(Context context) {
        super(context);
        this.mIsAodSliceOn = false;
        this.mAllowShowSensitiveData = true;
        this.mIsFormat12Hour = false;
        this.mDebugRaiseCrashRate = new boolean[]{false, false, false, false};
        this.mContentObserver = new ContentObserver(new Handler()) { // from class: com.oneplus.keyguard.OpKeyguardClockInfoView.1
            @Override // android.database.ContentObserver
            public void onChange(boolean z) {
                super.onChange(z);
                OpKeyguardClockInfoView opKeyguardClockInfoView = OpKeyguardClockInfoView.this;
                boolean z2 = false;
                if (1 == Settings.Secure.getIntForUser(opKeyguardClockInfoView.mContext.getContentResolver(), "lock_screen_allow_private_notifications", 0, -2)) {
                    z2 = true;
                }
                opKeyguardClockInfoView.mAllowShowSensitiveData = z2;
                Log.i("OpKeyguardClockInfoView", "mContentObserver selfChange:" + z + ", mAllowShowSensitiveData:" + OpKeyguardClockInfoView.this.mAllowShowSensitiveData);
                if (OpKeyguardClockInfoView.this.mKeyguardAssistantView != null) {
                    OpKeyguardClockInfoView.this.mKeyguardAssistantView.setHideSensitiveData(!OpKeyguardClockInfoView.this.mAllowShowSensitiveData);
                }
            }
        };
        this.mIsFormat12HourObserver = new ContentObserver(new Handler()) { // from class: com.oneplus.keyguard.OpKeyguardClockInfoView.2
            @Override // android.database.ContentObserver
            public void onChange(boolean z) {
                super.onChange(z);
                Log.i("OpKeyguardClockInfoView", "receive mIsFormat12HourObserver changed");
                OpKeyguardClockInfoView opKeyguardClockInfoView = OpKeyguardClockInfoView.this;
                opKeyguardClockInfoView.mIsFormat12Hour = "12".equals(Settings.System.getStringForUser(opKeyguardClockInfoView.mContext.getContentResolver(), "time_12_24", -2));
                OpKeyguardClockInfoView.this.updateTime();
            }
        };
        this.mKeyguardAssistantViewCallback = new KeyguardAssistantView.Callback() { // from class: com.oneplus.keyguard.OpKeyguardClockInfoView.3
            @Override // com.android.keyguard.KeyguardAssistantView.Callback
            public void onCardShownChanged(boolean z) {
                Log.i("OpKeyguardClockInfoView", "receive onCardShownChanged value:" + z);
                OpKeyguardClockInfoView.this.updateView();
            }
        };
        this.mReceiver = new BroadcastReceiver() { // from class: com.oneplus.keyguard.OpKeyguardClockInfoView.5
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                String action = intent.getAction();
                if (action.equals("android.intent.action.TIME_TICK")) {
                    if (OpUtils.DEBUG_ONEPLUS) {
                        Log.i("OpKeyguardClockInfoView", "ACTION_TIME_TICK");
                    }
                    OpKeyguardClockInfoView.this.updateTime();
                } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                    Log.i("OpKeyguardClockInfoView", "ACTION_SCREEN_OFF");
                    OpKeyguardClockInfoView.this.updateView();
                    OpKeyguardClockInfoView.this.updateTime();
                }
            }
        };
    }

    /* JADX INFO: finally extract failed */
    public OpKeyguardClockInfoView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mIsAodSliceOn = false;
        boolean z = true;
        this.mAllowShowSensitiveData = true;
        this.mIsFormat12Hour = false;
        this.mDebugRaiseCrashRate = new boolean[]{false, false, false, false};
        this.mContentObserver = new ContentObserver(new Handler()) { // from class: com.oneplus.keyguard.OpKeyguardClockInfoView.1
            @Override // android.database.ContentObserver
            public void onChange(boolean z) {
                super.onChange(z);
                OpKeyguardClockInfoView opKeyguardClockInfoView = OpKeyguardClockInfoView.this;
                boolean z2 = false;
                if (1 == Settings.Secure.getIntForUser(opKeyguardClockInfoView.mContext.getContentResolver(), "lock_screen_allow_private_notifications", 0, -2)) {
                    z2 = true;
                }
                opKeyguardClockInfoView.mAllowShowSensitiveData = z2;
                Log.i("OpKeyguardClockInfoView", "mContentObserver selfChange:" + z + ", mAllowShowSensitiveData:" + OpKeyguardClockInfoView.this.mAllowShowSensitiveData);
                if (OpKeyguardClockInfoView.this.mKeyguardAssistantView != null) {
                    OpKeyguardClockInfoView.this.mKeyguardAssistantView.setHideSensitiveData(!OpKeyguardClockInfoView.this.mAllowShowSensitiveData);
                }
            }
        };
        this.mIsFormat12HourObserver = new ContentObserver(new Handler()) { // from class: com.oneplus.keyguard.OpKeyguardClockInfoView.2
            @Override // android.database.ContentObserver
            public void onChange(boolean z) {
                super.onChange(z);
                Log.i("OpKeyguardClockInfoView", "receive mIsFormat12HourObserver changed");
                OpKeyguardClockInfoView opKeyguardClockInfoView = OpKeyguardClockInfoView.this;
                opKeyguardClockInfoView.mIsFormat12Hour = "12".equals(Settings.System.getStringForUser(opKeyguardClockInfoView.mContext.getContentResolver(), "time_12_24", -2));
                OpKeyguardClockInfoView.this.updateTime();
            }
        };
        this.mKeyguardAssistantViewCallback = new KeyguardAssistantView.Callback() { // from class: com.oneplus.keyguard.OpKeyguardClockInfoView.3
            @Override // com.android.keyguard.KeyguardAssistantView.Callback
            public void onCardShownChanged(boolean z) {
                Log.i("OpKeyguardClockInfoView", "receive onCardShownChanged value:" + z);
                OpKeyguardClockInfoView.this.updateView();
            }
        };
        this.mReceiver = new BroadcastReceiver() { // from class: com.oneplus.keyguard.OpKeyguardClockInfoView.5
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                String action = intent.getAction();
                if (action.equals("android.intent.action.TIME_TICK")) {
                    if (OpUtils.DEBUG_ONEPLUS) {
                        Log.i("OpKeyguardClockInfoView", "ACTION_TIME_TICK");
                    }
                    OpKeyguardClockInfoView.this.updateTime();
                } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                    Log.i("OpKeyguardClockInfoView", "ACTION_SCREEN_OFF");
                    OpKeyguardClockInfoView.this.updateView();
                    OpKeyguardClockInfoView.this.updateTime();
                }
            }
        };
        Log.i("OpKeyguardClockInfoView", "OpKeyguardClockInfoView constructer, callers= " + Debug.getCallers(20));
        this.mDebugRaiseCrashRate[0] = SystemProperties.getInt("persist.debug.raise.crashrate.type1init", 0) == 1;
        this.mDebugRaiseCrashRate[1] = SystemProperties.getInt("persist.debug.raise.crashrate.type1release", 0) == 1;
        this.mDebugRaiseCrashRate[2] = SystemProperties.getInt("persist.debug.raise.crashrate.type2", 0) == 1;
        this.mDebugRaiseCrashRate[3] = SystemProperties.getInt("persist.debug.raise.crashrate.type2every", 0) != 1 ? false : z;
        this.mContext = context;
        this.mUiHandler = new Handler(Looper.getMainLooper());
        TypedArray obtainStyledAttributes = context.getTheme().obtainStyledAttributes(attributeSet, R$styleable.OpKeyguardClockInfoView, 0, 0);
        try {
            this.mViewType = ViewTypeEnum.fromOrdinal(obtainStyledAttributes.getInteger(R$styleable.OpKeyguardClockInfoView_inputType, 0));
            Log.i("OpKeyguardClockInfoView", "mViewType:" + this.mViewType);
            obtainStyledAttributes.recycle();
            this.mDensityDpi = this.mContext.getResources().getConfiguration().densityDpi;
            init();
            if (this.mViewType == ViewTypeEnum.aod) {
                Log.i("OpKeyguardClockInfoView", "new LinearLayout.LayoutParams");
                setLayoutParams(new FrameLayout.LayoutParams(context, attributeSet));
            }
            this.mUserTracker = new CurrentUserTracker((BroadcastDispatcher) Dependency.get(BroadcastDispatcher.class)) { // from class: com.oneplus.keyguard.OpKeyguardClockInfoView.4
                @Override // com.android.systemui.settings.CurrentUserTracker
                public void onUserSwitched(int i) {
                    OpKeyguardClockInfoView.this.mUser = i;
                    OpKeyguardClockInfoView.this.mContext.getContentResolver().unregisterContentObserver(OpKeyguardClockInfoView.this.mContentObserver);
                    OpKeyguardClockInfoView.this.mContext.getContentResolver().unregisterContentObserver(OpKeyguardClockInfoView.this.mIsFormat12HourObserver);
                    boolean z2 = false;
                    OpKeyguardClockInfoView.this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("lock_screen_allow_private_notifications"), false, OpKeyguardClockInfoView.this.mContentObserver, -2);
                    OpKeyguardClockInfoView.this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("time_12_24"), false, OpKeyguardClockInfoView.this.mIsFormat12HourObserver, -2);
                    OpKeyguardClockInfoView opKeyguardClockInfoView = OpKeyguardClockInfoView.this;
                    opKeyguardClockInfoView.mIsFormat12Hour = "12".equals(Settings.System.getStringForUser(opKeyguardClockInfoView.mContext.getContentResolver(), "time_12_24", -2));
                    OpKeyguardClockInfoView opKeyguardClockInfoView2 = OpKeyguardClockInfoView.this;
                    if (1 == Settings.Secure.getIntForUser(opKeyguardClockInfoView2.mContext.getContentResolver(), "lock_screen_allow_private_notifications", 0, -2)) {
                        z2 = true;
                    }
                    opKeyguardClockInfoView2.mAllowShowSensitiveData = z2;
                    if (OpKeyguardClockInfoView.this.mKeyguardAssistantView != null) {
                        OpKeyguardClockInfoView.this.mKeyguardAssistantView.setHideSensitiveData(!OpKeyguardClockInfoView.this.mAllowShowSensitiveData);
                    }
                    OpKeyguardClockInfoView.this.updateTime();
                }
            };
        } catch (Throwable th) {
            obtainStyledAttributes.recycle();
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    public void init() {
        Log.i("OpKeyguardClockInfoView", "init, callers= " + Debug.getCallers(20));
        ViewGroup.LayoutParams layoutParams = new WindowManager.LayoutParams(-1, -1, 0, 0, 2014, 16777224, -3);
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(this.mContext).inflate(C0011R$layout.op_keyguard_info_view, (ViewGroup) null);
        this.mInnerPanel = linearLayout;
        addView(linearLayout, layoutParams);
        this.mTextViewDateOfWeekLineOne = (OpKeyguardOneplusTextView) this.mInnerPanel.findViewById(C0008R$id.day_of_week);
        this.mTextViewTimeLineTwo = (OpKeyguardOneplusTextView) this.mInnerPanel.findViewById(C0008R$id.time_clock);
        this.mTextViewDateLineThree = (TextView) this.mInnerPanel.findViewById(C0008R$id.day_of_month);
        this.mTextViewTimeLineTwo.updataClockView(true);
        this.mAodSliceViewContainer = (ViewGroup) this.mInnerPanel.findViewById(C0008R$id.aod_slice_container);
        this.mIcon = (ImageView) findViewById(C0008R$id.slice_icon);
        this.mPrimary = (TextView) findViewById(C0008R$id.slice_primary);
        this.mRemark = (TextView) findViewById(C0008R$id.slice_remark);
        this.mSecondary = (TextView) findViewById(C0008R$id.slice_secondary);
        setInputType(this.mViewType.ordinal());
        updateLayout();
        updateTime();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        Log.i("OpKeyguardClockInfoView", "onFinishInflate, callers= " + Debug.getCallers(3));
    }

    private void updateLayout() {
        updateLayout(false);
    }

    private void updateLayout(boolean z) {
        Log.i("OpKeyguardClockInfoView", "updateLayout, stack: " + Debug.getCallers(2));
        this.mUiHandler.post(new Runnable(z) { // from class: com.oneplus.keyguard.-$$Lambda$OpKeyguardClockInfoView$u60fm-CI5wh7vITPqFIY5QjKpjU
            public final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                OpKeyguardClockInfoView.this.lambda$updateLayout$0$OpKeyguardClockInfoView(this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$updateLayout$0 */
    public /* synthetic */ void lambda$updateLayout$0$OpKeyguardClockInfoView(boolean z) {
        int i;
        int i2;
        boolean[] zArr = this.mDebugRaiseCrashRate;
        if (zArr[2] || zArr[3]) {
            Log.i("OpKeyguardClockInfoView", "persist.debug.raise.crashrate.type2, force:" + z + ", onlyForce" + this.mDebugRaiseCrashRate[2] + ", every:" + this.mDebugRaiseCrashRate[3]);
            if (z || this.mDebugRaiseCrashRate[3]) {
                release();
                initAssitantView();
            }
        }
        int width = DisplayUtils.getWidth(this.mContext);
        if (width > 1080) {
            i = OpUtils.convertPxByResolutionProportion((float) this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.op_keyguard_clock_info_view_smart_space_height), 1080);
        } else {
            i = this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.op_keyguard_clock_info_view_smart_space_height);
        }
        if (width > 1080) {
            i2 = OpUtils.convertPxByResolutionProportion((float) this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.op_keyguard_clock_info_view_content_padding), 1080);
        } else {
            i2 = this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.op_keyguard_clock_info_view_content_padding);
        }
        ViewGroup.LayoutParams layoutParams = this.mTextViewDateOfWeekLineOne.getLayoutParams();
        layoutParams.height = i;
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) this.mTextViewDateOfWeekLineOne.getLayoutParams();
        marginLayoutParams.leftMargin = i2;
        marginLayoutParams.rightMargin = i2;
        this.mTextViewDateOfWeekLineOne.setLayoutParams(layoutParams);
        this.mTextViewDateOfWeekLineOne.setLayoutParams(marginLayoutParams);
        ViewGroup.LayoutParams layoutParams2 = this.mTextViewTimeLineTwo.getLayoutParams();
        layoutParams2.height = i;
        ViewGroup.MarginLayoutParams marginLayoutParams2 = (ViewGroup.MarginLayoutParams) this.mTextViewTimeLineTwo.getLayoutParams();
        marginLayoutParams2.leftMargin = i2;
        marginLayoutParams2.rightMargin = i2;
        this.mTextViewTimeLineTwo.setLayoutParams(layoutParams2);
        this.mTextViewTimeLineTwo.setLayoutParams(marginLayoutParams2);
        this.mTextViewTimeLineTwo.updataClockView();
        ViewGroup.LayoutParams layoutParams3 = this.mAodSliceViewContainer.getLayoutParams();
        layoutParams3.height = i;
        ViewGroup.MarginLayoutParams marginLayoutParams3 = (ViewGroup.MarginLayoutParams) this.mAodSliceViewContainer.getLayoutParams();
        marginLayoutParams3.leftMargin = i2;
        marginLayoutParams3.rightMargin = i2;
        this.mAodSliceViewContainer.setLayoutParams(layoutParams3);
        this.mAodSliceViewContainer.setLayoutParams(marginLayoutParams3);
        ViewGroup.MarginLayoutParams marginLayoutParams4 = (ViewGroup.MarginLayoutParams) this.mTextViewDateLineThree.getLayoutParams();
        marginLayoutParams4.leftMargin = i2;
        marginLayoutParams4.rightMargin = i2;
        this.mTextViewDateLineThree.setLayoutParams(marginLayoutParams4);
        this.mTextViewDateLineThree.setTextSize(0, (float) OpUtils.convertDpToFixedPx2(getResources().getDimension(C0005R$dimen.op_keyguard_clock_info_view_day_of_month_textsize)));
        updateLayoutColor();
        KeyguardAssistantView keyguardAssistantView = this.mKeyguardAssistantView;
        if (keyguardAssistantView != null) {
            keyguardAssistantView.refresh();
        }
        updateOpKeyguardOneplusTextViewState();
        updateSliceLayout();
        updateSliceTextSize();
        requestLayout();
    }

    private void updateOpKeyguardOneplusTextViewState() {
        OpKeyguardOneplusTextView opKeyguardOneplusTextView = this.mTextViewTimeLineTwo;
        if (opKeyguardOneplusTextView != null) {
            if (this.mViewType == ViewTypeEnum.aod) {
                opKeyguardOneplusTextView.setIsAOD(true);
            }
            this.mTextViewTimeLineTwo.setIsClockTimeLineTwo(true);
        }
        OpKeyguardOneplusTextView opKeyguardOneplusTextView2 = this.mTextViewDateOfWeekLineOne;
        if (opKeyguardOneplusTextView2 != null && this.mViewType == ViewTypeEnum.aod) {
            opKeyguardOneplusTextView2.setIsAOD(true);
        }
    }

    private boolean isREDVersion() {
        boolean z = this.mViewType == ViewTypeEnum.aod;
        boolean z2 = OpClockViewCtrl.getClockStyle() == 50;
        if (!OpUtils.isREDVersion() || z) {
            return z && z2;
        }
        return true;
    }

    private void updateLayoutColor() {
        this.mUiHandler.post(new Runnable() { // from class: com.oneplus.keyguard.-$$Lambda$OpKeyguardClockInfoView$Zuomt3H6DzQICU0EGTI7PFW2zvY
            @Override // java.lang.Runnable
            public final void run() {
                OpKeyguardClockInfoView.this.lambda$updateLayoutColor$1$OpKeyguardClockInfoView();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$updateLayoutColor$1 */
    public /* synthetic */ void lambda$updateLayoutColor$1$OpKeyguardClockInfoView() {
        boolean z = this.mContext.getThemeResId() == C0016R$style.Theme_SystemUI_Light;
        int color = this.mContext.getResources().getColor((!z || this.mViewType == ViewTypeEnum.aod) ? C0004R$color.op_control_text_color_primary_dark : C0004R$color.op_control_text_color_primary_light);
        Log.i("OpKeyguardClockInfoView", "lightWpTheme:" + z + ", targetTextColor:" + color);
        OpKeyguardOneplusTextView opKeyguardOneplusTextView = this.mTextViewDateOfWeekLineOne;
        if (opKeyguardOneplusTextView != null) {
            opKeyguardOneplusTextView.setTextColor(color);
        }
        OpKeyguardOneplusTextView opKeyguardOneplusTextView2 = this.mTextViewTimeLineTwo;
        if (opKeyguardOneplusTextView2 != null) {
            opKeyguardOneplusTextView2.setTextColor(color);
        }
        if (this.mTextViewDateLineThree != null) {
            if (isREDVersion()) {
                color = this.mContext.getColor(C0004R$color.op_turquoise);
                this.mTextViewDateLineThree.setShadowLayer(this.mContext.getResources().getFloat(C0005R$dimen.op_cb_lockscreen_clock_date_shadow_radius), this.mContext.getResources().getFloat(C0005R$dimen.op_cb_lockscreen_clock_date_shadow_dx), this.mContext.getResources().getFloat(C0005R$dimen.op_cb_lockscreen_clock_date_shadow_dy), this.mContext.getColor(C0004R$color.op_turquoise));
            } else {
                this.mTextViewDateLineThree.getPaint().clearShadowLayer();
            }
            this.mTextViewDateLineThree.setTextColor(color);
        }
        KeyguardAssistantView keyguardAssistantView = this.mKeyguardAssistantView;
        if (keyguardAssistantView != null) {
            keyguardAssistantView.updateTextColor(this.mViewType);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        if (OpUtils.DEBUG_ONEPLUS) {
            Log.i("OpKeyguardClockInfoView", "onConfigurationChanged, newConfig:" + configuration + ", mDensityDpi:" + this.mDensityDpi + ", newConfig.densityDpi:" + configuration.densityDpi);
        }
        boolean z = false;
        int i = this.mDensityDpi;
        int i2 = configuration.densityDpi;
        if (i != i2) {
            this.mDensityDpi = i2;
            z = true;
        }
        updateLayout(z);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (OpUtils.DEBUG_ONEPLUS) {
            Log.i("OpKeyguardClockInfoView", "onAttachedToWindow");
        }
        boolean z = false;
        if (this.mDebugRaiseCrashRate[0]) {
            Log.i("OpKeyguardClockInfoView", "persist.debug.raise.crashrate.type1init 1");
            release();
        }
        this.mContext.getResources().getConfiguration().getLayoutDirection();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.TIME_TICK");
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        this.mContext.registerReceiver(this.mReceiver, intentFilter);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("lock_screen_allow_private_notifications"), false, this.mContentObserver, -2);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("time_12_24"), false, this.mIsFormat12HourObserver, -2);
        this.mIsFormat12Hour = "12".equals(Settings.System.getStringForUser(this.mContext.getContentResolver(), "time_12_24", -2));
        if (1 == Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "lock_screen_allow_private_notifications", 0, -2)) {
            z = true;
        }
        this.mAllowShowSensitiveData = z;
        KeyguardAssistantView keyguardAssistantView = this.mKeyguardAssistantView;
        if (keyguardAssistantView != null) {
            keyguardAssistantView.setHideSensitiveData(true ^ z);
        }
        this.mUserTracker.startTracking();
        updateLayout();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (OpUtils.DEBUG_ONEPLUS) {
            Log.i("OpKeyguardClockInfoView", "onDetachedFromWindow");
        }
        this.mContext.unregisterReceiver(this.mReceiver);
        this.mContext.getContentResolver().unregisterContentObserver(this.mContentObserver);
        this.mContext.getContentResolver().unregisterContentObserver(this.mIsFormat12HourObserver);
        this.mUserTracker.stopTracking();
        if (this.mViewType != ViewTypeEnum.aod) {
            release();
        }
        if (this.mDebugRaiseCrashRate[1]) {
            Log.i("OpKeyguardClockInfoView", "persist.debug.raise.crashrate.type1release 1");
            initAssitantView();
        }
    }

    @Override // android.view.View
    public void onWindowFocusChanged(boolean z) {
        super.onWindowFocusChanged(z);
        if (this.mHasWindowFocus != z) {
            this.mHasWindowFocus = z;
            if (z) {
                Log.i("OpKeyguardClockInfoView", "focus false to true");
                updateView();
                updateTime();
                this.mUiHandler.post(new Runnable() { // from class: com.oneplus.keyguard.-$$Lambda$OpKeyguardClockInfoView$A4NxJH1ep6bbCOvImDLbIcrsN4M
                    @Override // java.lang.Runnable
                    public final void run() {
                        OpKeyguardClockInfoView.this.lambda$onWindowFocusChanged$2$OpKeyguardClockInfoView();
                    }
                });
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onWindowFocusChanged$2 */
    public /* synthetic */ void lambda$onWindowFocusChanged$2$OpKeyguardClockInfoView() {
        KeyguardAssistantView keyguardAssistantView = this.mKeyguardAssistantView;
        if (keyguardAssistantView != null) {
            keyguardAssistantView.refresh();
        }
    }

    public void setInputType(int i) {
        Log.i("OpKeyguardClockInfoView", "InputType:" + i + ", Looper.myLooper()" + Looper.myLooper() + ", Looper.getMainLooper():" + Looper.getMainLooper());
        if (i == 0) {
            initAssitantView();
        } else if (i == 2) {
            this.mUiHandler = ((StatusBar) Dependency.get(StatusBar.class)).getAodWindowManager().getUIHandler();
            initAssitantView();
        }
        updateView();
    }

    @Override // com.oneplus.aod.views.IOpAodClock
    public void release() {
        KeyguardAssistantView keyguardAssistantView = this.mKeyguardAssistantView;
        if (keyguardAssistantView != null) {
            keyguardAssistantView.removeCallback(this.mKeyguardAssistantViewCallback);
            this.mKeyguardAssistantView.release();
            this.mKeyguardAssistantView = null;
        }
        this.mContext.getContentResolver().unregisterContentObserver(this.mContentObserver);
        this.mContext.getContentResolver().unregisterContentObserver(this.mIsFormat12HourObserver);
    }

    private void initAssitantView() {
        if (this.mKeyguardAssistantView == null) {
            this.mKeyguardAssistantView = new KeyguardAssistantView(this, this.mContext, this.mUiHandler);
        }
        KeyguardAssistantView keyguardAssistantView = this.mKeyguardAssistantView;
        if (keyguardAssistantView != null) {
            keyguardAssistantView.addCallback(this.mKeyguardAssistantViewCallback);
            this.mKeyguardAssistantView.inflateIndicatorContainer();
            this.mKeyguardAssistantView.setHideSensitiveData(!this.mAllowShowSensitiveData);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateView() {
        StringBuilder sb = new StringBuilder();
        sb.append("updateView, mIsAodSliceOn:");
        sb.append(this.mIsAodSliceOn);
        sb.append(", mTextViewDateOfWeekLineOne:");
        sb.append(this.mTextViewDateOfWeekLineOne);
        sb.append(", mKeyguardAssistantView.hasHeader():");
        KeyguardAssistantView keyguardAssistantView = this.mKeyguardAssistantView;
        sb.append(keyguardAssistantView != null ? Boolean.valueOf(keyguardAssistantView.hasHeader()) : null);
        sb.append(", stack:");
        sb.append(Debug.getCallers(2));
        Log.i("OpKeyguardClockInfoView", sb.toString());
        this.mUiHandler.post(new Runnable() { // from class: com.oneplus.keyguard.-$$Lambda$OpKeyguardClockInfoView$9ZPNDFnqCrsqwFl9rZoa6t70jug
            @Override // java.lang.Runnable
            public final void run() {
                OpKeyguardClockInfoView.this.lambda$updateView$3$OpKeyguardClockInfoView();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$updateView$3 */
    public /* synthetic */ void lambda$updateView$3$OpKeyguardClockInfoView() {
        OpKeyguardOneplusTextView opKeyguardOneplusTextView;
        KeyguardAssistantView keyguardAssistantView = this.mKeyguardAssistantView;
        if (keyguardAssistantView != null && keyguardAssistantView.hasHeader()) {
            OpKeyguardOneplusTextView opKeyguardOneplusTextView2 = this.mTextViewDateOfWeekLineOne;
            if (opKeyguardOneplusTextView2 != null) {
                opKeyguardOneplusTextView2.setVisibility(8);
                this.mKeyguardAssistantView.setVisibility(0);
                this.mAodSliceViewContainer.setVisibility(8);
                updateAodSliceView();
            }
        } else if (!this.mIsAodSliceOn || (opKeyguardOneplusTextView = this.mTextViewDateOfWeekLineOne) == null) {
            OpKeyguardOneplusTextView opKeyguardOneplusTextView3 = this.mTextViewDateOfWeekLineOne;
            if (opKeyguardOneplusTextView3 != null) {
                opKeyguardOneplusTextView3.setVisibility(0);
                KeyguardAssistantView keyguardAssistantView2 = this.mKeyguardAssistantView;
                if (keyguardAssistantView2 != null) {
                    keyguardAssistantView2.setVisibility(8);
                }
                this.mAodSliceViewContainer.setVisibility(8);
            }
        } else {
            opKeyguardOneplusTextView.setVisibility(8);
            KeyguardAssistantView keyguardAssistantView3 = this.mKeyguardAssistantView;
            if (keyguardAssistantView3 != null) {
                keyguardAssistantView3.setVisibility(8);
            }
            this.mAodSliceViewContainer.setVisibility(0);
        }
        updateAodSliceView();
    }

    private void updateAodSliceView() {
        this.mUiHandler.post(new Runnable() { // from class: com.oneplus.keyguard.-$$Lambda$OpKeyguardClockInfoView$DPBizKI2erV0BuuVhBhM1NBKLn8
            @Override // java.lang.Runnable
            public final void run() {
                OpKeyguardClockInfoView.this.lambda$updateAodSliceView$4$OpKeyguardClockInfoView();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$updateAodSliceView$4 */
    public /* synthetic */ void lambda$updateAodSliceView$4$OpKeyguardClockInfoView() {
        KeyguardAssistantView keyguardAssistantView;
        if (!this.mIsAodSliceOn || (keyguardAssistantView = this.mKeyguardAssistantView) == null || keyguardAssistantView.hasHeader()) {
            this.mIcon.setVisibility(8);
            this.mPrimary.setVisibility(8);
            this.mSecondary.setVisibility(8);
            this.mRemark.setVisibility(8);
            this.mAodSliceViewContainer.setVisibility(8);
            return;
        }
        this.mIcon.setVisibility(0);
        this.mPrimary.setVisibility(0);
        this.mSecondary.setVisibility(0);
        if (this.mRemark.getText().toString().length() == 0) {
            this.mRemark.setVisibility(8);
        } else {
            this.mRemark.setVisibility(0);
        }
        this.mAodSliceViewContainer.setVisibility(0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateTime() {
        this.mUiHandler.post(new Runnable() { // from class: com.oneplus.keyguard.-$$Lambda$OpKeyguardClockInfoView$jBVEr0PDLC8cO_n131H8PkRyoBg
            @Override // java.lang.Runnable
            public final void run() {
                OpKeyguardClockInfoView.this.lambda$updateTime$5$OpKeyguardClockInfoView();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: updateTimeInner */
    public void lambda$updateTime$5() {
        Calendar instance = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE");
        SimpleDateFormat simpleDateFormat2 = this.mIsFormat12Hour ? new SimpleDateFormat("h'꞉'mm") : new SimpleDateFormat("HH'꞉'mm");
        Locale locale = Locale.getDefault();
        SimpleDateFormat simpleDateFormat3 = new SimpleDateFormat(DateFormat.getBestDateTimePattern(locale, "MMMMd").toString(), locale);
        OpKeyguardOneplusTextView opKeyguardOneplusTextView = this.mTextViewDateOfWeekLineOne;
        if (opKeyguardOneplusTextView != null) {
            opKeyguardOneplusTextView.setText(simpleDateFormat.format(instance));
        }
        OpKeyguardOneplusTextView opKeyguardOneplusTextView2 = this.mTextViewTimeLineTwo;
        if (opKeyguardOneplusTextView2 != null) {
            opKeyguardOneplusTextView2.setText(simpleDateFormat2.format(instance), TextView.BufferType.SPANNABLE);
        }
        TextView textView = this.mTextViewDateLineThree;
        if (textView != null) {
            textView.setText(simpleDateFormat3.format(instance));
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.View, android.view.ViewGroup
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.View
    public void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
    }

    @Override // com.oneplus.aod.views.IOpAodClock
    public void applyLayoutParams(OpAodSettings.OpViewInfo opViewInfo) {
        FrameLayout.LayoutParams layoutParams;
        getLayoutParams();
        if (this.mViewType == ViewTypeEnum.aod && (layoutParams = (FrameLayout.LayoutParams) getLayoutParams()) != null && opViewInfo != null) {
            layoutParams.setMarginStart(opViewInfo.getMarginStart(this.mContext));
            layoutParams.setMarginEnd(opViewInfo.getMarginEnd(this.mContext));
            layoutParams.topMargin = opViewInfo.getMarginTop(this.mContext);
            layoutParams.bottomMargin = opViewInfo.getMarginBottom(this.mContext);
        }
    }

    @Override // com.oneplus.aod.views.IOpAodClock
    public void onTimeChanged(java.util.Calendar calendar) {
        lambda$updateTime$5();
    }

    public void updateSliceView(boolean z, int i, String str, String str2, String str3) {
        Log.i("OpKeyguardClockInfoView", "updateSliceView, status:" + z + ", primary:" + str + ", second:" + str2 + ", remark:" + str3);
        this.mIsAodSliceOn = z;
        this.mUiHandler.post(new Runnable(i, str, str2, str3) { // from class: com.oneplus.keyguard.-$$Lambda$OpKeyguardClockInfoView$Cd4J8qvrKdlQ5OPk0Q5STNSjfJ8
            public final /* synthetic */ int f$1;
            public final /* synthetic */ String f$2;
            public final /* synthetic */ String f$3;
            public final /* synthetic */ String f$4;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
            }

            @Override // java.lang.Runnable
            public final void run() {
                OpKeyguardClockInfoView.this.lambda$updateSliceView$6$OpKeyguardClockInfoView(this.f$1, this.f$2, this.f$3, this.f$4);
            }
        });
        updateView();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$updateSliceView$6 */
    public /* synthetic */ void lambda$updateSliceView$6$OpKeyguardClockInfoView(int i, String str, String str2, String str3) {
        this.mIcon.setImageResource(i);
        this.mPrimary.setText(str);
        this.mSecondary.setText(str2);
        this.mRemark.setText(str3);
        if (str3 == null || str3.length() == 0) {
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.mPrimary.getLayoutParams();
            layoutParams.width = 0;
            layoutParams.weight = 1.0f;
            this.mPrimary.setLayoutParams(layoutParams);
            return;
        }
        LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams) this.mPrimary.getLayoutParams();
        layoutParams2.width = -2;
        layoutParams2.weight = 0.0f;
        this.mPrimary.setLayoutParams(layoutParams2);
    }

    private void updateSliceLayout() {
        View findViewById = findViewById(C0008R$id.slice_primary_container);
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) findViewById.getLayoutParams();
        marginLayoutParams.bottomMargin = OpUtils.convertDpToFixedPx(getResources().getDimension(C0005R$dimen.aod_slice_layout_primary_margin_bottom));
        findViewById.setLayoutParams(marginLayoutParams);
        int convertDpToFixedPx = OpUtils.convertDpToFixedPx(getResources().getDimension(C0005R$dimen.op_control_icon_size_list));
        ViewGroup.MarginLayoutParams marginLayoutParams2 = (ViewGroup.MarginLayoutParams) this.mIcon.getLayoutParams();
        marginLayoutParams2.width = convertDpToFixedPx;
        marginLayoutParams2.height = convertDpToFixedPx;
        this.mIcon.setLayoutParams(marginLayoutParams2);
        ViewGroup.MarginLayoutParams marginLayoutParams3 = (ViewGroup.MarginLayoutParams) this.mPrimary.getLayoutParams();
        marginLayoutParams3.setMarginStart(OpUtils.convertDpToFixedPx(getResources().getDimension(C0005R$dimen.op_control_margin_space1)));
        TextView textView = this.mPrimary;
        textView.setPaddingRelative(textView.getPaddingStart(), this.mPrimary.getPaddingTop(), OpUtils.convertDpToFixedPx(getResources().getDimension(C0005R$dimen.aod_slice_view_primary_padding_end)), this.mPrimary.getPaddingBottom());
        this.mPrimary.setLayoutParams(marginLayoutParams3);
        ViewGroup.MarginLayoutParams marginLayoutParams4 = (ViewGroup.MarginLayoutParams) this.mRemark.getLayoutParams();
        marginLayoutParams4.setMarginStart(OpUtils.convertDpToFixedPx(getResources().getDimension(C0005R$dimen.op_control_margin_space1)));
        this.mRemark.setLayoutParams(marginLayoutParams4);
    }

    private void updateSliceTextSize() {
        float convertDpToFixedPx2 = (float) OpUtils.convertDpToFixedPx2(getResources().getDimension(C0005R$dimen.op_keyguard_clock_info_view_slice_primary_textsize));
        this.mPrimary.setTextSize(0, convertDpToFixedPx2);
        this.mRemark.setTextSize(0, convertDpToFixedPx2);
        this.mSecondary.setTextSize(0, (float) OpUtils.convertDpToFixedPx2(getResources().getDimension(C0005R$dimen.aod_slice_text_size_secondary)));
    }
}
