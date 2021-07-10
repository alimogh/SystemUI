package androidx.core.os;

import android.os.Build;
public final class CancellationSignal {
    private boolean mCancelInProgress;
    private Object mCancellationSignalObj;
    private boolean mIsCanceled;
    private OnCancelListener mOnCancelListener;

    public interface OnCancelListener {
        void onCancel();
    }

    public boolean isCanceled() {
        boolean z;
        synchronized (this) {
            z = this.mIsCanceled;
        }
        return z;
    }

    public void cancel() {
        OnCancelListener onCancelListener;
        Object obj;
        synchronized (this) {
            if (!this.mIsCanceled) {
                this.mIsCanceled = true;
                this.mCancelInProgress = true;
                onCancelListener = this.mOnCancelListener;
                obj = this.mCancellationSignalObj;
            } else {
                return;
            }
        }
        if (onCancelListener != null) {
            try {
                onCancelListener.onCancel();
            } catch (Throwable th) {
                synchronized (this) {
                    this.mCancelInProgress = false;
                    notifyAll();
                    throw th;
                }
            }
        }
        if (obj != null && Build.VERSION.SDK_INT >= 16) {
            ((android.os.CancellationSignal) obj).cancel();
        }
        synchronized (this) {
            this.mCancelInProgress = false;
            notifyAll();
        }
    }

    public void setOnCancelListener(OnCancelListener onCancelListener) {
        synchronized (this) {
            waitForCancelFinishedLocked();
            if (this.mOnCancelListener != onCancelListener) {
                this.mOnCancelListener = onCancelListener;
                if (this.mIsCanceled) {
                    if (onCancelListener != null) {
                        onCancelListener.onCancel();
                    }
                }
            }
        }
    }

    private void waitForCancelFinishedLocked() {
        while (this.mCancelInProgress) {
            try {
                wait();
            } catch (InterruptedException unused) {
            }
        }
    }
}
