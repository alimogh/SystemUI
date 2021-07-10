package com.android.keyguard;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import com.android.settingslib.Utils;
import com.android.systemui.C0002R$attr;
import com.android.systemui.C0004R$color;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0008R$id;
import com.google.android.libraries.assistant.oemsmartspace.lib.SmartspaceContainerController;
import com.google.android.libraries.assistant.oemsmartspace.shared.SmartspaceUpdateListener;
import com.oneplus.aod.views.OpSmartspaceContainer;
import com.oneplus.keyguard.OpKeyguardClockInfoView;
import com.oneplus.util.OpUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
public class KeyguardAssistantView {
    private static final boolean mIsNoCustGoogleView = SystemProperties.getBoolean("debug.no.smartspace.custgoogleview", true);
    private String TAG = "KeyguardAssistantView";
    private boolean mAllowShowNotification;
    private OpSmartspaceContainer mAmbientIndicationContainer;
    private View.OnAttachStateChangeListener mAttachStateListener;
    private final List<Callback> mCallbacks;
    private Runnable mContentChangeListener;
    private Context mContext;
    private SmartspaceContainerController mController;
    private float mDarkAmount;
    private final Runnable mDelayForSetGoogleSmartspaceChildViewHeight;
    private final boolean mEnableSmartSpace;
    private Handler mHandler;
    private boolean mHasHeader;
    private View mKeyguardStatusView;
    private KeyguardViewUpdateListener mKeyguardViewUpdateListener;
    final Runnable mNotifyHasHeaderRunner;
    private int mRetryTimes;
    private final SettingObserver mSettingObserver;

    public interface Callback {
        void onCardShownChanged(boolean z);
    }

    static /* synthetic */ int access$608(KeyguardAssistantView keyguardAssistantView) {
        int i = keyguardAssistantView.mRetryTimes;
        keyguardAssistantView.mRetryTimes = i + 1;
        return i;
    }

    public KeyguardAssistantView(View view, Context context, Handler handler) {
        boolean z = false;
        this.mHasHeader = false;
        this.mDarkAmount = 0.0f;
        this.mCallbacks = new ArrayList();
        this.mEnableSmartSpace = SystemProperties.getBoolean("persist.enable.smartspace.lib", true);
        this.mSettingObserver = new SettingObserver();
        this.mAllowShowNotification = true;
        this.mNotifyHasHeaderRunner = new Runnable() { // from class: com.android.keyguard.KeyguardAssistantView.1
            @Override // java.lang.Runnable
            public void run() {
                KeyguardAssistantView.this.notifyHasHeader();
            }
        };
        this.mRetryTimes = 0;
        this.mDelayForSetGoogleSmartspaceChildViewHeight = new Runnable() { // from class: com.android.keyguard.KeyguardAssistantView.2
            @Override // java.lang.Runnable
            public void run() {
                if (KeyguardAssistantView.this.setGoogleSmartspaceChildViewHeightInternal() || KeyguardAssistantView.this.mRetryTimes >= 3) {
                    if (KeyguardAssistantView.this.mRetryTimes >= 3) {
                        String str = KeyguardAssistantView.this.TAG;
                        Log.i(str, "RetryTimes >= MAX_RETRY_TIMES, retryTimes:" + KeyguardAssistantView.this.mRetryTimes);
                    }
                    Log.i(KeyguardAssistantView.this.TAG, "setGoogleSmartspaceChildViewHeightInternal complete");
                    KeyguardAssistantView.this.mRetryTimes = 0;
                    KeyguardAssistantView.this.setHasHeader(true);
                    return;
                }
                KeyguardAssistantView.access$608(KeyguardAssistantView.this);
                KeyguardAssistantView.this.mHandler.removeCallbacks(KeyguardAssistantView.this.mDelayForSetGoogleSmartspaceChildViewHeight);
                KeyguardAssistantView.this.mHandler.postDelayed(KeyguardAssistantView.this.mDelayForSetGoogleSmartspaceChildViewHeight, 250);
            }
        };
        this.mAttachStateListener = new View.OnAttachStateChangeListener() { // from class: com.android.keyguard.KeyguardAssistantView.4
            @Override // android.view.View.OnAttachStateChangeListener
            public void onViewAttachedToWindow(View view2) {
                KeyguardAssistantView.this.TAG = getHistoryParentSimpleName(view2);
                Log.i(KeyguardAssistantView.this.TAG, "onViewAttachedToWindow");
            }

            @Override // android.view.View.OnAttachStateChangeListener
            public void onViewDetachedFromWindow(View view2) {
                KeyguardAssistantView.this.TAG = KeyguardAssistantView.class.getSimpleName();
                Log.i(KeyguardAssistantView.this.TAG, "onViewDetachedFromWindow");
            }

            private String getHistoryParentSimpleName(View view2) {
                if (view2 == null) {
                    return "";
                }
                int i = 0;
                String simpleName = KeyguardAssistantView.class.getSimpleName();
                while (view2.getParent() != null && i < 5) {
                    String simpleName2 = view2.getParent().getClass().getSimpleName();
                    if ("FrameLayout".equals(simpleName2)) {
                        simpleName2 = "F";
                    } else if ("LinearLayout".equals(simpleName2)) {
                        simpleName2 = "L";
                    } else if ("RelativeLayout".equals(simpleName2)) {
                        simpleName2 = "R";
                    }
                    simpleName = simpleName2 + "/" + simpleName;
                    view2 = (View) view2.getParent();
                    i++;
                }
                return simpleName;
            }
        };
        String str = this.TAG;
        Log.i(str, "KeyguardAssistantView constructor, callers= " + Debug.getCallers(5));
        this.mContext = context;
        if (handler == null) {
            Log.i(this.TAG, "KeyguardAssistantView handler == null, new one");
            this.mHandler = new Handler();
        } else {
            this.mHandler = handler;
        }
        this.mKeyguardStatusView = view;
        OpSmartspaceContainer opSmartspaceContainer = (OpSmartspaceContainer) view.findViewById(C0008R$id.ambient_assistant_container);
        this.mAmbientIndicationContainer = opSmartspaceContainer;
        opSmartspaceContainer.addOnAttachStateChangeListener(this.mAttachStateListener);
        KeyguardViewUpdateListener keyguardViewUpdateListener = new KeyguardViewUpdateListener();
        this.mKeyguardViewUpdateListener = keyguardViewUpdateListener;
        keyguardViewUpdateListener.setKeyguardAssistantView(this);
        this.mAllowShowNotification = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "lock_screen_show_notifications", 1, -2) != 0 ? true : z;
        this.mSettingObserver.observe();
    }

    public void release() {
        Log.d(this.TAG, "release");
        this.mAmbientIndicationContainer.removeOnAttachStateChangeListener(this.mAttachStateListener);
        SmartspaceContainerController smartspaceContainerController = this.mController;
        if (smartspaceContainerController != null) {
            smartspaceContainerController.unsetView();
            this.mController = null;
        }
        this.mSettingObserver.unObserve();
    }

    public void inflateIndicatorContainer() {
        if (!this.mEnableSmartSpace) {
            Log.i(this.TAG, "not enable smartspace");
        } else if (this.mAmbientIndicationContainer != null) {
            Log.d(this.TAG, "Adding ambient contents");
            this.mController = SmartspaceContainerController.create(this.mContext, this.mHandler);
            Utils.getColorAttrDefaultColor(this.mContext, C0002R$attr.wallpaperTextColor);
            Bundle bundle = new Bundle();
            bundle.putString("com.google.android.apps.oemsmartspace.SMARTSPACE_TYPE_KEY", "com.google.android.apps.oemsmartspace.SMARTSPACE_FOR_SYSUI");
            bundle.putInt("com.google.android.apps.oemsmartspace.TEXT_COLOR_KEY", this.mContext.getColor(C0004R$color.op_nav_bar_background_light));
            bundle.putString("com.google.android.apps.oemsmartspace.SMARTSPACE_RESOURCE_PACKAGE", "net.oneplus.launcher");
            bundle.putBoolean("com.google.android.apps.oemsmartspace.SMARTSPACE_ENABLE_DATE_KEY", true);
            bundle.putBoolean("com.google.android.apps.oemsmartspace.SMARTSPACE_SET_LEFT_ALIGNED_KEY", true);
            bundle.putFloat("com.google.android.apps.oemsmartspace.SMARTSPACE_TEXT_SIZE_FACTOR_KEY", Float.parseFloat("0.8"));
            this.mController.setView(this.mAmbientIndicationContainer, bundle, this.mKeyguardViewUpdateListener);
        } else {
            String str = this.TAG;
            Log.d(str, "Failed to add ambient contents" + this.mAmbientIndicationContainer);
        }
    }

    public boolean hasHeader() {
        return this.mHasHeader;
    }

    public void refresh() {
        if (this.mController != null) {
            Log.i(this.TAG, "refresh");
            this.mController.refresh();
        }
    }

    public void updateTextColor(OpKeyguardClockInfoView.ViewTypeEnum viewTypeEnum) {
        int i;
        if (this.mController != null) {
            if (viewTypeEnum == OpKeyguardClockInfoView.ViewTypeEnum.aod) {
                i = this.mContext.getColor(C0004R$color.op_nav_bar_background_light);
            } else {
                i = getTextColor();
            }
            String str = this.TAG;
            Log.d(str, "updateTextColor, textColor:" + Integer.toHexString(i) + ", viewTypeEnum:" + viewTypeEnum);
            Bundle bundle = new Bundle();
            bundle.putInt("com.google.android.apps.oemsmartspace.TEXT_COLOR_KEY", i);
            this.mController.setStyle(bundle);
        }
    }

    private int getTextColor() {
        return Utils.getColorAttrDefaultColor(this.mContext, C0002R$attr.wallpaperTextColor);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setHasHeader(boolean z) {
        if (!this.mAllowShowNotification) {
            z = false;
        }
        this.mHasHeader = z;
        this.mHandler.removeCallbacks(this.mNotifyHasHeaderRunner);
        this.mHandler.postDelayed(this.mNotifyHasHeaderRunner, 1000);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyHasHeader() {
        Runnable runnable = this.mContentChangeListener;
        if (runnable != null) {
            runnable.run();
        }
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            this.mCallbacks.get(i).onCardShownChanged(this.mHasHeader);
        }
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("KeyguardAssistantView:");
        printWriter.println(" mDarkAmount: " + this.mDarkAmount);
        printWriter.println(" mHasHeader: " + this.mHasHeader);
        printWriter.println(" mAllowShowNotification: " + this.mAllowShowNotification);
    }

    /* access modifiers changed from: private */
    public static class KeyguardViewUpdateListener implements SmartspaceUpdateListener {
        private KeyguardAssistantView mKeyguardAssistantView;

        private KeyguardViewUpdateListener() {
        }

        public void setKeyguardAssistantView(KeyguardAssistantView keyguardAssistantView) {
            this.mKeyguardAssistantView = keyguardAssistantView;
        }

        private void setKeyguardShowingHeader(boolean z) {
            Log.i("KeyguardViewUpdateListener", "setKeyguardShowingHeader:" + z);
            KeyguardAssistantView keyguardAssistantView = this.mKeyguardAssistantView;
            if (keyguardAssistantView != null) {
                keyguardAssistantView.setHasHeader(z);
            }
        }

        @Override // com.google.android.libraries.assistant.oemsmartspace.shared.SmartspaceUpdateListener
        public void onNoCardAndChipShown(int i) {
            Log.i("KeyguardViewUpdateListener", "onNoCardAndChipShown:" + Debug.getCallers(1));
            setKeyguardShowingHeader(false);
        }

        @Override // com.google.android.libraries.assistant.oemsmartspace.shared.SmartspaceUpdateListener
        public void onCardShown(int i) {
            Log.i("KeyguardViewUpdateListener", "onCardShown:" + Debug.getCallers(1));
            if (KeyguardAssistantView.mIsNoCustGoogleView) {
                setKeyguardShowingHeader(true);
            } else {
                this.mKeyguardAssistantView.setGoogleSmartspaceChildViewHeight();
            }
        }

        @Override // com.google.android.libraries.assistant.oemsmartspace.shared.SmartspaceUpdateListener
        public void onChipShown(int i) {
            Log.i("KeyguardViewUpdateListener", "onChipShown:" + Debug.getCallers(1));
            if (KeyguardAssistantView.mIsNoCustGoogleView) {
                setKeyguardShowingHeader(true);
            } else {
                this.mKeyguardAssistantView.setGoogleSmartspaceChildViewHeight();
            }
        }

        @Override // com.google.android.libraries.assistant.oemsmartspace.shared.SmartspaceUpdateListener
        public void onBothCardAndChipShown(int i) {
            Log.i("KeyguardViewUpdateListener", "onBothCardAndChipShown:" + Debug.getCallers(1));
            if (KeyguardAssistantView.mIsNoCustGoogleView) {
                setKeyguardShowingHeader(true);
            } else {
                this.mKeyguardAssistantView.setGoogleSmartspaceChildViewHeight();
            }
        }
    }

    public void addCallback(Callback callback) {
        callback.onCardShownChanged(hasHeader());
        this.mCallbacks.add(callback);
    }

    public void removeCallback(Callback callback) {
        this.mCallbacks.remove(callback);
    }

    public void setHideSensitiveData(boolean z) {
        if (this.mController != null) {
            String str = this.TAG;
            Log.d(str, "setHideSensitiveData:" + z + ", callers= " + Debug.getCallers(1));
            this.mController.setHideSensitiveData(z);
            return;
        }
        Log.d(this.TAG, "setHideSensitiveData warnning, mController null");
    }

    public void setVisibility(int i) {
        OpSmartspaceContainer opSmartspaceContainer = this.mAmbientIndicationContainer;
        if (opSmartspaceContainer != null) {
            opSmartspaceContainer.setVisibility(i);
        } else {
            Log.d(this.TAG, "setVisibility warnning, mAmbientIndicationContainer null");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean setGoogleSmartspaceChildViewHeightInternal() {
        TextView textView = (TextView) this.mAmbientIndicationContainer.findViewById(C0008R$id.title_leading_truncatable);
        if (textView != null) {
            ViewGroup.LayoutParams layoutParams = textView.getLayoutParams();
            layoutParams.height = Math.round(((float) this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.op_keyguard_clock_info_view_smart_space_height)) / 2.5f);
            textView.setLayoutParams(layoutParams);
            TextView textView2 = (TextView) this.mAmbientIndicationContainer.findViewById(C0008R$id.title_trailing_truncatable);
            if (textView2 != null) {
                ViewGroup.LayoutParams layoutParams2 = textView2.getLayoutParams();
                layoutParams2.height = Math.round(((float) this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.op_keyguard_clock_info_view_smart_space_height)) / 2.5f);
                textView2.setLayoutParams(layoutParams2);
                final ViewGroup viewGroup = (ViewGroup) this.mAmbientIndicationContainer.findViewById(C0008R$id.subtitle_line);
                if (viewGroup != null) {
                    viewGroup.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener(this) { // from class: com.android.keyguard.KeyguardAssistantView.3
                        @Override // android.view.ViewTreeObserver.OnPreDrawListener
                        public boolean onPreDraw() {
                            viewGroup.getViewTreeObserver().removeOnPreDrawListener(this);
                            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) viewGroup.getLayoutParams();
                            if (marginLayoutParams.bottomMargin == 8) {
                                return true;
                            }
                            marginLayoutParams.bottomMargin = 8;
                            viewGroup.setLayoutParams(marginLayoutParams);
                            return true;
                        }
                    });
                    return true;
                }
                Log.i(this.TAG, "viewGroup_subtitle_line params null");
                return false;
            }
            Log.i(this.TAG, "title_trailing_truncatable params null");
            return false;
        }
        Log.i(this.TAG, "title_leading_truncatable params null");
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setGoogleSmartspaceChildViewHeight() {
        String str = this.TAG;
        Log.i(str, "setGoogleSmartspaceChildViewHeight, callers= " + Debug.getCallers(5));
        this.mHandler.removeCallbacks(this.mDelayForSetGoogleSmartspaceChildViewHeight);
        this.mHandler.post(this.mDelayForSetGoogleSmartspaceChildViewHeight);
    }

    /* access modifiers changed from: private */
    public final class SettingObserver extends ContentObserver {
        public SettingObserver() {
            super(new Handler());
        }

        /* access modifiers changed from: package-private */
        public void observe() {
            KeyguardAssistantView.this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("lock_screen_show_notifications"), false, KeyguardAssistantView.this.mSettingObserver, -2);
        }

        /* access modifiers changed from: package-private */
        public void unObserve() {
            KeyguardAssistantView.this.mContext.getContentResolver().unregisterContentObserver(KeyguardAssistantView.this.mSettingObserver);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri) {
            super.onChange(z, uri);
            if (Settings.Secure.getUriFor("lock_screen_show_notifications").equals(uri)) {
                boolean z2 = true;
                if (Settings.Secure.getIntForUser(KeyguardAssistantView.this.mContext.getContentResolver(), "lock_screen_show_notifications", 1, -2) == 0) {
                    z2 = false;
                }
                if (OpUtils.DEBUG_ONEPLUS) {
                    String str = KeyguardAssistantView.this.TAG;
                    Log.i(str, "onChange LOCK_SCREEN_SHOW_NOTIFICATIONS, show:" + z2);
                }
                KeyguardAssistantView.this.mAllowShowNotification = z2;
            }
        }
    }
}
