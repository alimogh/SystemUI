package com.oneplus.systemui.biometrics;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;
public abstract class AddRemoveRequestingView extends FrameLayout {
    protected OpBiometricDialogImpl mDialog;
    private boolean mShowingRequest;
    protected final String mTag;

    /* access modifiers changed from: protected */
    public void addAlready() {
    }

    /* access modifiers changed from: protected */
    public void removeAlready() {
    }

    public AddRemoveRequestingView(Context context) {
        this(context, null);
    }

    public AddRemoveRequestingView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mTag = getClass().getSimpleName();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        String str = this.mTag;
        Log.d(str, "onAttachedToWindow: mRequestShowing= " + this.mShowingRequest);
        if (!this.mShowingRequest) {
            selfRemoveFromWindow();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        String str = this.mTag;
        Log.d(str, "onDetachedFromWindow: mRequestShowing= " + this.mShowingRequest);
        if (this.mShowingRequest) {
            selfAddToWindow();
        }
    }

    public void setDialog(OpBiometricDialogImpl opBiometricDialogImpl) {
        this.mDialog = opBiometricDialogImpl;
    }

    public void addToWindow() {
        if (!this.mShowingRequest) {
            this.mShowingRequest = true;
            selfAddToWindow();
            return;
        }
        Log.d(this.mTag, "addToWindow: window already request to added.");
        addAlready();
    }

    public void removeFromWindow() {
        if (this.mShowingRequest) {
            this.mShowingRequest = false;
            selfRemoveFromWindow();
            return;
        }
        Log.d(this.mTag, "removeFromWindow: window already request to removed.");
        removeAlready();
    }

    /* access modifiers changed from: protected */
    public void selfAddToWindow() {
        if (!isAttachedToWindow()) {
            this.mDialog.mFodWindowManager.addView(this);
        } else {
            Log.d(this.mTag, "addToWindow: maybe during removing progress. wait...");
        }
    }

    /* access modifiers changed from: protected */
    public void selfRemoveFromWindow() {
        if (!isAttachedToWindow()) {
            Log.d(this.mTag, "removeFromWindow: maybe during adding progress. wait...");
        } else {
            this.mDialog.mFodWindowManager.removeView(this);
        }
    }

    public boolean isRequestShowing() {
        return this.mShowingRequest;
    }

    @Override // android.view.View, java.lang.Object
    public String toString() {
        return String.format("([%s]: mShowingRequest: %b, isAttachedToWindow: %b)", this.mTag, Boolean.valueOf(this.mShowingRequest), Boolean.valueOf(isAttachedToWindow())).toString();
    }
}
