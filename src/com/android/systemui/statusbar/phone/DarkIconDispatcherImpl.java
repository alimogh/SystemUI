package com.android.systemui.statusbar.phone;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.os.Handler;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.R$styleable;
import com.android.systemui.C0004R$color;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.phone.LightBarTransitionsController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
public class DarkIconDispatcherImpl implements SysuiDarkIconDispatcher, LightBarTransitionsController.DarkIntensityApplier {
    private final String TAG = DarkIconDispatcherImpl.class.getSimpleName();
    private float mDarkIntensity;
    private int mDarkModeIconColorSingleTone;
    private final Handler mHandler;
    private int mIconTint = -1;
    private int mLightModeIconColorSingleTone;
    private Runnable mReApplyIconTint = new Runnable() { // from class: com.android.systemui.statusbar.phone.DarkIconDispatcherImpl.1
        @Override // java.lang.Runnable
        public void run() {
            DarkIconDispatcherImpl.access$008(DarkIconDispatcherImpl.this);
            DarkIconDispatcherImpl.this.applyIconTint();
        }
    };
    private final ArrayMap<Object, DarkIconDispatcher.DarkReceiver> mReceivers = new ArrayMap<>();
    private int mSetTintRetryTimes = 0;
    private final Rect mTintArea = new Rect();
    private View mTraceView;
    private final LightBarTransitionsController mTransitionsController;

    @Override // com.android.systemui.statusbar.phone.LightBarTransitionsController.DarkIntensityApplier
    public int getTintAnimationDuration() {
        return R$styleable.AppCompatTheme_windowFixedHeightMajor;
    }

    static /* synthetic */ int access$008(DarkIconDispatcherImpl darkIconDispatcherImpl) {
        int i = darkIconDispatcherImpl.mSetTintRetryTimes;
        darkIconDispatcherImpl.mSetTintRetryTimes = i + 1;
        return i;
    }

    public DarkIconDispatcherImpl(Context context, CommandQueue commandQueue) {
        this.mDarkModeIconColorSingleTone = context.getColor(C0004R$color.dark_mode_icon_color_single_tone);
        this.mLightModeIconColorSingleTone = context.getColor(C0004R$color.light_mode_icon_color_single_tone);
        this.mTransitionsController = new LightBarTransitionsController(context, this, commandQueue);
        this.mHandler = new Handler();
    }

    @Override // com.android.systemui.statusbar.phone.SysuiDarkIconDispatcher
    public LightBarTransitionsController getTransitionsController() {
        return this.mTransitionsController;
    }

    @Override // com.android.systemui.plugins.DarkIconDispatcher
    public void addDarkReceiver(DarkIconDispatcher.DarkReceiver darkReceiver) {
        this.mReceivers.put(darkReceiver, darkReceiver);
        darkReceiver.onDarkChanged(this.mTintArea, this.mDarkIntensity, this.mIconTint);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$addDarkReceiver$0 */
    public /* synthetic */ void lambda$addDarkReceiver$0$DarkIconDispatcherImpl(ImageView imageView, Rect rect, float f, int i) {
        imageView.setImageTintList(ColorStateList.valueOf(DarkIconDispatcher.getTint(this.mTintArea, imageView, this.mIconTint)));
    }

    @Override // com.android.systemui.plugins.DarkIconDispatcher
    public void addDarkReceiver(ImageView imageView) {
        $$Lambda$DarkIconDispatcherImpl$ok51JmL9mmr4FNW4V8J0PDfHR6I r0 = new DarkIconDispatcher.DarkReceiver(imageView) { // from class: com.android.systemui.statusbar.phone.-$$Lambda$DarkIconDispatcherImpl$ok51JmL9mmr4FNW4V8J0PDfHR6I
            public final /* synthetic */ ImageView f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.systemui.plugins.DarkIconDispatcher.DarkReceiver
            public final void onDarkChanged(Rect rect, float f, int i) {
                DarkIconDispatcherImpl.this.lambda$addDarkReceiver$0$DarkIconDispatcherImpl(this.f$1, rect, f, i);
            }
        };
        this.mReceivers.put(imageView, r0);
        r0.onDarkChanged(this.mTintArea, this.mDarkIntensity, this.mIconTint);
    }

    @Override // com.android.systemui.plugins.DarkIconDispatcher
    public void removeDarkReceiver(DarkIconDispatcher.DarkReceiver darkReceiver) {
        this.mReceivers.remove(darkReceiver);
    }

    @Override // com.android.systemui.plugins.DarkIconDispatcher
    public void removeDarkReceiver(ImageView imageView) {
        this.mReceivers.remove(imageView);
    }

    @Override // com.android.systemui.plugins.DarkIconDispatcher
    public void applyDark(DarkIconDispatcher.DarkReceiver darkReceiver) {
        this.mReceivers.get(darkReceiver).onDarkChanged(this.mTintArea, this.mDarkIntensity, this.mIconTint);
    }

    @Override // com.android.systemui.plugins.DarkIconDispatcher
    public void setIconsDarkArea(Rect rect) {
        if (rect != null || !this.mTintArea.isEmpty()) {
            if (rect == null) {
                this.mTintArea.setEmpty();
            } else {
                this.mTintArea.set(rect);
            }
            applyIconTint();
        }
    }

    @Override // com.android.systemui.statusbar.phone.LightBarTransitionsController.DarkIntensityApplier
    public void applyDarkIntensity(float f) {
        this.mDarkIntensity = f;
        this.mIconTint = ((Integer) ArgbEvaluator.getInstance().evaluate(f, Integer.valueOf(this.mLightModeIconColorSingleTone), Integer.valueOf(this.mDarkModeIconColorSingleTone))).intValue();
        applyIconTint();
    }

    @Override // com.android.systemui.plugins.DarkIconDispatcher
    public void applyIconTint() {
        boolean z = this.mSetTintRetryTimes >= 2;
        View view = this.mTraceView;
        if ((view == null || view.isLayoutRequested()) && !z) {
            this.mHandler.postDelayed(this.mReApplyIconTint, 100);
            return;
        }
        this.mHandler.removeCallbacks(this.mReApplyIconTint);
        for (int i = 0; i < this.mReceivers.size(); i++) {
            this.mReceivers.valueAt(i).onDarkChanged(this.mTintArea, this.mDarkIntensity, this.mIconTint);
        }
        if (z) {
            Log.d(this.TAG, "apply tint time-out after retrying " + this.mSetTintRetryTimes + " times");
        }
        this.mSetTintRetryTimes = 0;
    }

    @Override // com.android.systemui.plugins.DarkIconDispatcher
    public void setTraceView(View view) {
        this.mTraceView = view;
    }

    @Override // com.android.systemui.plugins.DarkIconDispatcher
    public View getTraceView() {
        return this.mTraceView;
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("DarkIconDispatcher: ");
        printWriter.println("  mIconTint: 0x" + Integer.toHexString(this.mIconTint));
        printWriter.println("  mDarkIntensity: " + this.mDarkIntensity + "f");
        StringBuilder sb = new StringBuilder();
        sb.append("  mTintArea: ");
        sb.append(this.mTintArea);
        printWriter.println(sb.toString());
    }
}
