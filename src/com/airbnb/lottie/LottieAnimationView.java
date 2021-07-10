package com.airbnb.lottie;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.view.ViewCompat;
import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.utils.Logger;
import com.airbnb.lottie.utils.Utils;
import com.airbnb.lottie.value.LottieValueCallback;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
public class LottieAnimationView extends AppCompatImageView {
    private static final LottieListener<Throwable> DEFAULT_FAILURE_LISTENER = new LottieListener<Throwable>() { // from class: com.airbnb.lottie.LottieAnimationView.1
        public void onResult(Throwable th) {
            if (Utils.isNetworkException(th)) {
                Logger.warning("Unable to load composition.", th);
                return;
            }
            throw new IllegalStateException("Unable to parse composition", th);
        }
    };
    private static final String TAG = LottieAnimationView.class.getSimpleName();
    private String animationName;
    private int animationResId;
    private boolean autoPlay = false;
    private int buildDrawingCacheDepth = 0;
    private boolean cacheComposition = true;
    private LottieComposition composition;
    private LottieTask<LottieComposition> compositionTask;
    private LottieListener<Throwable> failureListener;
    private int fallbackResource = 0;
    private boolean isInitialized;
    private final LottieListener<LottieComposition> loadedListener = new LottieListener<LottieComposition>() { // from class: com.airbnb.lottie.LottieAnimationView.2
        public void onResult(LottieComposition lottieComposition) {
            LottieAnimationView.this.setComposition(lottieComposition);
        }
    };
    private final LottieDrawable lottieDrawable = new LottieDrawable();
    private Set<LottieOnCompositionLoadedListener> lottieOnCompositionLoadedListeners = new HashSet();
    private boolean playAnimationWhenShown = false;
    private RenderMode renderMode = RenderMode.AUTOMATIC;
    private boolean wasAnimatingWhenDetached = false;
    private boolean wasAnimatingWhenNotShown = false;
    private final LottieListener<Throwable> wrappedFailureListener = new LottieListener<Throwable>() { // from class: com.airbnb.lottie.LottieAnimationView.3
        public void onResult(Throwable th) {
            if (LottieAnimationView.this.fallbackResource != 0) {
                LottieAnimationView lottieAnimationView = LottieAnimationView.this;
                lottieAnimationView.setImageResource(lottieAnimationView.fallbackResource);
            }
            (LottieAnimationView.this.failureListener == null ? LottieAnimationView.DEFAULT_FAILURE_LISTENER : LottieAnimationView.this.failureListener).onResult(th);
        }
    };

    public LottieAnimationView(Context context) {
        super(context);
        init(null, R$attr.lottieAnimationViewStyle);
    }

    public LottieAnimationView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(attributeSet, R$attr.lottieAnimationViewStyle);
    }

    public LottieAnimationView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init(attributeSet, i);
    }

    private void init(AttributeSet attributeSet, int i) {
        String string;
        boolean z = false;
        TypedArray obtainStyledAttributes = getContext().obtainStyledAttributes(attributeSet, R$styleable.LottieAnimationView, i, 0);
        this.cacheComposition = obtainStyledAttributes.getBoolean(R$styleable.LottieAnimationView_lottie_cacheComposition, true);
        boolean hasValue = obtainStyledAttributes.hasValue(R$styleable.LottieAnimationView_lottie_rawRes);
        boolean hasValue2 = obtainStyledAttributes.hasValue(R$styleable.LottieAnimationView_lottie_fileName);
        boolean hasValue3 = obtainStyledAttributes.hasValue(R$styleable.LottieAnimationView_lottie_url);
        if (!hasValue || !hasValue2) {
            if (hasValue) {
                int resourceId = obtainStyledAttributes.getResourceId(R$styleable.LottieAnimationView_lottie_rawRes, 0);
                if (resourceId != 0) {
                    setAnimation(resourceId);
                }
            } else if (hasValue2) {
                String string2 = obtainStyledAttributes.getString(R$styleable.LottieAnimationView_lottie_fileName);
                if (string2 != null) {
                    setAnimation(string2);
                }
            } else if (hasValue3 && (string = obtainStyledAttributes.getString(R$styleable.LottieAnimationView_lottie_url)) != null) {
                setAnimationFromUrl(string);
            }
            setFallbackResource(obtainStyledAttributes.getResourceId(R$styleable.LottieAnimationView_lottie_fallbackRes, 0));
            if (obtainStyledAttributes.getBoolean(R$styleable.LottieAnimationView_lottie_autoPlay, false)) {
                this.wasAnimatingWhenDetached = true;
                this.autoPlay = true;
            }
            if (obtainStyledAttributes.getBoolean(R$styleable.LottieAnimationView_lottie_loop, false)) {
                this.lottieDrawable.setRepeatCount(-1);
            }
            if (obtainStyledAttributes.hasValue(R$styleable.LottieAnimationView_lottie_repeatMode)) {
                setRepeatMode(obtainStyledAttributes.getInt(R$styleable.LottieAnimationView_lottie_repeatMode, 1));
            }
            if (obtainStyledAttributes.hasValue(R$styleable.LottieAnimationView_lottie_repeatCount)) {
                setRepeatCount(obtainStyledAttributes.getInt(R$styleable.LottieAnimationView_lottie_repeatCount, -1));
            }
            if (obtainStyledAttributes.hasValue(R$styleable.LottieAnimationView_lottie_speed)) {
                setSpeed(obtainStyledAttributes.getFloat(R$styleable.LottieAnimationView_lottie_speed, 1.0f));
            }
            setImageAssetsFolder(obtainStyledAttributes.getString(R$styleable.LottieAnimationView_lottie_imageAssetsFolder));
            setProgress(obtainStyledAttributes.getFloat(R$styleable.LottieAnimationView_lottie_progress, 0.0f));
            enableMergePathsForKitKatAndAbove(obtainStyledAttributes.getBoolean(R$styleable.LottieAnimationView_lottie_enableMergePathsForKitKatAndAbove, false));
            if (obtainStyledAttributes.hasValue(R$styleable.LottieAnimationView_lottie_colorFilter)) {
                addValueCallback(new KeyPath("**"), LottieProperty.COLOR_FILTER, new LottieValueCallback(new SimpleColorFilter(obtainStyledAttributes.getColor(R$styleable.LottieAnimationView_lottie_colorFilter, 0))));
            }
            if (obtainStyledAttributes.hasValue(R$styleable.LottieAnimationView_lottie_scale)) {
                this.lottieDrawable.setScale(obtainStyledAttributes.getFloat(R$styleable.LottieAnimationView_lottie_scale, 1.0f));
            }
            if (obtainStyledAttributes.hasValue(R$styleable.LottieAnimationView_lottie_renderMode)) {
                int i2 = obtainStyledAttributes.getInt(R$styleable.LottieAnimationView_lottie_renderMode, RenderMode.AUTOMATIC.ordinal());
                if (i2 >= RenderMode.values().length) {
                    i2 = RenderMode.AUTOMATIC.ordinal();
                }
                setRenderMode(RenderMode.values()[i2]);
            }
            if (getScaleType() != null) {
                this.lottieDrawable.setScaleType(getScaleType());
            }
            obtainStyledAttributes.recycle();
            LottieDrawable lottieDrawable = this.lottieDrawable;
            if (Utils.getAnimationScale(getContext()) != 0.0f) {
                z = true;
            }
            lottieDrawable.setSystemAnimationsAreEnabled(Boolean.valueOf(z));
            enableOrDisableHardwareLayer();
            this.isInitialized = true;
            return;
        }
        throw new IllegalArgumentException("lottie_rawRes and lottie_fileName cannot be used at the same time. Please use only one at once.");
    }

    @Override // androidx.appcompat.widget.AppCompatImageView, android.widget.ImageView
    public void setImageResource(int i) {
        cancelLoaderTask();
        super.setImageResource(i);
    }

    @Override // androidx.appcompat.widget.AppCompatImageView, android.widget.ImageView
    public void setImageDrawable(Drawable drawable) {
        cancelLoaderTask();
        super.setImageDrawable(drawable);
    }

    @Override // androidx.appcompat.widget.AppCompatImageView, android.widget.ImageView
    public void setImageBitmap(Bitmap bitmap) {
        cancelLoaderTask();
        super.setImageBitmap(bitmap);
    }

    @Override // android.widget.ImageView, android.graphics.drawable.Drawable.Callback, android.view.View
    public void invalidateDrawable(Drawable drawable) {
        Drawable drawable2 = getDrawable();
        LottieDrawable lottieDrawable = this.lottieDrawable;
        if (drawable2 == lottieDrawable) {
            super.invalidateDrawable(lottieDrawable);
        } else {
            super.invalidateDrawable(drawable);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public Parcelable onSaveInstanceState() {
        SavedState savedState = new SavedState(super.onSaveInstanceState());
        savedState.animationName = this.animationName;
        savedState.animationResId = this.animationResId;
        savedState.progress = this.lottieDrawable.getProgress();
        savedState.isAnimating = this.lottieDrawable.isAnimating() || (!ViewCompat.isAttachedToWindow(this) && this.wasAnimatingWhenDetached);
        savedState.imageAssetsFolder = this.lottieDrawable.getImageAssetsFolder();
        savedState.repeatMode = this.lottieDrawable.getRepeatMode();
        savedState.repeatCount = this.lottieDrawable.getRepeatCount();
        return savedState;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onRestoreInstanceState(Parcelable parcelable) {
        if (!(parcelable instanceof SavedState)) {
            super.onRestoreInstanceState(parcelable);
            return;
        }
        SavedState savedState = (SavedState) parcelable;
        super.onRestoreInstanceState(savedState.getSuperState());
        String str = savedState.animationName;
        this.animationName = str;
        if (!TextUtils.isEmpty(str)) {
            setAnimation(this.animationName);
        }
        int i = savedState.animationResId;
        this.animationResId = i;
        if (i != 0) {
            setAnimation(i);
        }
        setProgress(savedState.progress);
        if (savedState.isAnimating) {
            playAnimation();
        }
        this.lottieDrawable.setImagesAssetsFolder(savedState.imageAssetsFolder);
        setRepeatMode(savedState.repeatMode);
        setRepeatCount(savedState.repeatCount);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onVisibilityChanged(View view, int i) {
        if (this.isInitialized) {
            if (isShown()) {
                if (this.wasAnimatingWhenNotShown) {
                    resumeAnimation();
                } else if (this.playAnimationWhenShown) {
                    playAnimation();
                }
                this.wasAnimatingWhenNotShown = false;
                this.playAnimationWhenShown = false;
            } else if (isAnimating()) {
                pauseAnimation();
                this.wasAnimatingWhenNotShown = true;
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.ImageView, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode() && (this.autoPlay || this.wasAnimatingWhenDetached)) {
            playAnimation();
            this.autoPlay = false;
            this.wasAnimatingWhenDetached = false;
        }
        if (Build.VERSION.SDK_INT < 23) {
            onVisibilityChanged(this, getVisibility());
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.ImageView, android.view.View
    public void onDetachedFromWindow() {
        if (isAnimating()) {
            cancelAnimation();
            this.wasAnimatingWhenDetached = true;
        }
        super.onDetachedFromWindow();
    }

    public void enableMergePathsForKitKatAndAbove(boolean z) {
        this.lottieDrawable.enableMergePathsForKitKatAndAbove(z);
    }

    public void setAnimation(int i) {
        this.animationResId = i;
        this.animationName = null;
        setCompositionTask(fromRawRes(i));
    }

    private LottieTask<LottieComposition> fromRawRes(final int i) {
        if (isInEditMode()) {
            return new LottieTask<>(new Callable<LottieResult<LottieComposition>>() { // from class: com.airbnb.lottie.LottieAnimationView.4
                @Override // java.util.concurrent.Callable
                public LottieResult<LottieComposition> call() throws Exception {
                    return LottieAnimationView.this.cacheComposition ? LottieCompositionFactory.fromRawResSync(LottieAnimationView.this.getContext(), i) : LottieCompositionFactory.fromRawResSync(LottieAnimationView.this.getContext(), i, null);
                }
            }, true);
        }
        return this.cacheComposition ? LottieCompositionFactory.fromRawRes(getContext(), i) : LottieCompositionFactory.fromRawRes(getContext(), i, null);
    }

    public void setAnimation(String str) {
        this.animationName = str;
        this.animationResId = 0;
        setCompositionTask(fromAssets(str));
    }

    private LottieTask<LottieComposition> fromAssets(final String str) {
        if (isInEditMode()) {
            return new LottieTask<>(new Callable<LottieResult<LottieComposition>>() { // from class: com.airbnb.lottie.LottieAnimationView.5
                @Override // java.util.concurrent.Callable
                public LottieResult<LottieComposition> call() throws Exception {
                    return LottieAnimationView.this.cacheComposition ? LottieCompositionFactory.fromAssetSync(LottieAnimationView.this.getContext(), str) : LottieCompositionFactory.fromAssetSync(LottieAnimationView.this.getContext(), str, null);
                }
            }, true);
        }
        return this.cacheComposition ? LottieCompositionFactory.fromAsset(getContext(), str) : LottieCompositionFactory.fromAsset(getContext(), str, null);
    }

    public void setAnimationFromUrl(String str) {
        setCompositionTask(this.cacheComposition ? LottieCompositionFactory.fromUrl(getContext(), str) : LottieCompositionFactory.fromUrl(getContext(), str, null));
    }

    public void setFallbackResource(int i) {
        this.fallbackResource = i;
    }

    private void setCompositionTask(LottieTask<LottieComposition> lottieTask) {
        clearComposition();
        cancelLoaderTask();
        lottieTask.addListener(this.loadedListener);
        lottieTask.addFailureListener(this.wrappedFailureListener);
        this.compositionTask = lottieTask;
    }

    private void cancelLoaderTask() {
        LottieTask<LottieComposition> lottieTask = this.compositionTask;
        if (lottieTask != null) {
            lottieTask.removeListener(this.loadedListener);
            this.compositionTask.removeFailureListener(this.wrappedFailureListener);
        }
    }

    public void setComposition(LottieComposition lottieComposition) {
        if (L.DBG) {
            String str = TAG;
            Log.v(str, "Set Composition \n" + lottieComposition);
        }
        this.lottieDrawable.setCallback(this);
        this.composition = lottieComposition;
        boolean composition = this.lottieDrawable.setComposition(lottieComposition);
        enableOrDisableHardwareLayer();
        if (getDrawable() != this.lottieDrawable || composition) {
            onVisibilityChanged(this, getVisibility());
            requestLayout();
            for (LottieOnCompositionLoadedListener lottieOnCompositionLoadedListener : this.lottieOnCompositionLoadedListeners) {
                lottieOnCompositionLoadedListener.onCompositionLoaded(lottieComposition);
            }
        }
    }

    public void playAnimation() {
        if (isShown()) {
            this.lottieDrawable.playAnimation();
            enableOrDisableHardwareLayer();
            return;
        }
        this.playAnimationWhenShown = true;
    }

    public void resumeAnimation() {
        if (isShown()) {
            this.lottieDrawable.resumeAnimation();
            enableOrDisableHardwareLayer();
            return;
        }
        this.playAnimationWhenShown = false;
        this.wasAnimatingWhenNotShown = true;
    }

    public void setSpeed(float f) {
        this.lottieDrawable.setSpeed(f);
    }

    public void setRepeatMode(int i) {
        this.lottieDrawable.setRepeatMode(i);
    }

    public void setRepeatCount(int i) {
        this.lottieDrawable.setRepeatCount(i);
    }

    public boolean isAnimating() {
        return this.lottieDrawable.isAnimating();
    }

    public void setImageAssetsFolder(String str) {
        this.lottieDrawable.setImagesAssetsFolder(str);
    }

    public <T> void addValueCallback(KeyPath keyPath, T t, LottieValueCallback<T> lottieValueCallback) {
        this.lottieDrawable.addValueCallback(keyPath, t, lottieValueCallback);
    }

    @Override // android.widget.ImageView
    public void setScaleType(ImageView.ScaleType scaleType) {
        super.setScaleType(scaleType);
        LottieDrawable lottieDrawable = this.lottieDrawable;
        if (lottieDrawable != null) {
            lottieDrawable.setScaleType(scaleType);
        }
    }

    public void cancelAnimation() {
        this.wasAnimatingWhenDetached = false;
        this.wasAnimatingWhenNotShown = false;
        this.playAnimationWhenShown = false;
        this.lottieDrawable.cancelAnimation();
        enableOrDisableHardwareLayer();
    }

    public void pauseAnimation() {
        this.autoPlay = false;
        this.wasAnimatingWhenDetached = false;
        this.wasAnimatingWhenNotShown = false;
        this.playAnimationWhenShown = false;
        this.lottieDrawable.pauseAnimation();
        enableOrDisableHardwareLayer();
    }

    public void setProgress(float f) {
        this.lottieDrawable.setProgress(f);
    }

    private void clearComposition() {
        this.composition = null;
        this.lottieDrawable.clearComposition();
    }

    @Override // android.view.View
    public void buildDrawingCache(boolean z) {
        L.beginSection("buildDrawingCache");
        this.buildDrawingCacheDepth++;
        super.buildDrawingCache(z);
        if (this.buildDrawingCacheDepth == 1 && getWidth() > 0 && getHeight() > 0 && getLayerType() == 1 && getDrawingCache(z) == null) {
            setRenderMode(RenderMode.HARDWARE);
        }
        this.buildDrawingCacheDepth--;
        L.endSection("buildDrawingCache");
    }

    public void setRenderMode(RenderMode renderMode) {
        this.renderMode = renderMode;
        enableOrDisableHardwareLayer();
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.airbnb.lottie.LottieAnimationView$7  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass7 {
        static final /* synthetic */ int[] $SwitchMap$com$airbnb$lottie$RenderMode;

        static {
            int[] iArr = new int[RenderMode.values().length];
            $SwitchMap$com$airbnb$lottie$RenderMode = iArr;
            try {
                iArr[RenderMode.HARDWARE.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$airbnb$lottie$RenderMode[RenderMode.SOFTWARE.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$airbnb$lottie$RenderMode[RenderMode.AUTOMATIC.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0042, code lost:
        if (r4 != false) goto L_0x0044;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void enableOrDisableHardwareLayer() {
        /*
            r6 = this;
            int r0 = android.os.Build.VERSION.SDK_INT
            int[] r1 = com.airbnb.lottie.LottieAnimationView.AnonymousClass7.$SwitchMap$com$airbnb$lottie$RenderMode
            com.airbnb.lottie.RenderMode r2 = r6.renderMode
            int r2 = r2.ordinal()
            r1 = r1[r2]
            r2 = 2
            r3 = 1
            if (r1 == r3) goto L_0x0044
            if (r1 == r2) goto L_0x0015
            r4 = 3
            if (r1 == r4) goto L_0x0017
        L_0x0015:
            r2 = r3
            goto L_0x0044
        L_0x0017:
            com.airbnb.lottie.LottieComposition r1 = r6.composition
            r4 = 0
            if (r1 == 0) goto L_0x0027
            boolean r1 = r1.hasDashPattern()
            if (r1 == 0) goto L_0x0027
            r1 = 28
            if (r0 >= r1) goto L_0x0027
            goto L_0x0042
        L_0x0027:
            com.airbnb.lottie.LottieComposition r1 = r6.composition
            if (r1 == 0) goto L_0x0033
            int r1 = r1.getMaskAndMatteCount()
            r5 = 4
            if (r1 <= r5) goto L_0x0033
            goto L_0x0042
        L_0x0033:
            r1 = 21
            if (r0 >= r1) goto L_0x0038
            goto L_0x0042
        L_0x0038:
            r1 = 24
            if (r0 == r1) goto L_0x0042
            r1 = 25
            if (r0 != r1) goto L_0x0041
            goto L_0x0042
        L_0x0041:
            r4 = r3
        L_0x0042:
            if (r4 == 0) goto L_0x0015
        L_0x0044:
            int r0 = r6.getLayerType()
            if (r2 == r0) goto L_0x004e
            r0 = 0
            r6.setLayerType(r2, r0)
        L_0x004e:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.airbnb.lottie.LottieAnimationView.enableOrDisableHardwareLayer():void");
    }

    /* access modifiers changed from: private */
    public static class SavedState extends View.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() { // from class: com.airbnb.lottie.LottieAnimationView.SavedState.1
            @Override // android.os.Parcelable.Creator
            public SavedState createFromParcel(Parcel parcel) {
                return new SavedState(parcel);
            }

            @Override // android.os.Parcelable.Creator
            public SavedState[] newArray(int i) {
                return new SavedState[i];
            }
        };
        String animationName;
        int animationResId;
        String imageAssetsFolder;
        boolean isAnimating;
        float progress;
        int repeatCount;
        int repeatMode;

        SavedState(Parcelable parcelable) {
            super(parcelable);
        }

        private SavedState(Parcel parcel) {
            super(parcel);
            this.animationName = parcel.readString();
            this.progress = parcel.readFloat();
            this.isAnimating = parcel.readInt() != 1 ? false : true;
            this.imageAssetsFolder = parcel.readString();
            this.repeatMode = parcel.readInt();
            this.repeatCount = parcel.readInt();
        }

        @Override // android.view.View.BaseSavedState, android.os.Parcelable, android.view.AbsSavedState
        public void writeToParcel(Parcel parcel, int i) {
            super.writeToParcel(parcel, i);
            parcel.writeString(this.animationName);
            parcel.writeFloat(this.progress);
            parcel.writeInt(this.isAnimating ? 1 : 0);
            parcel.writeString(this.imageAssetsFolder);
            parcel.writeInt(this.repeatMode);
            parcel.writeInt(this.repeatCount);
        }
    }
}
