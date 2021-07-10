package androidx.appcompat.app;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
public class SoftKeyBoardListener {
    boolean isShow = false;
    private ViewTreeObserver.OnGlobalLayoutListener listener;
    private OnSoftKeyBoardChangeListener onSoftKeyBoardChangeListener;
    private View rootView;
    private int screenBottom;

    public interface OnSoftKeyBoardChangeListener {
        void keyBoardHide();

        void keyBoardShow(int i);
    }

    private SoftKeyBoardListener(Activity activity) {
        int i = activity.getWindow().getAttributes().softInputMode & 15;
        if (i == 4 || i == 5) {
            this.isShow = true;
        }
        this.rootView = activity.getWindow().getDecorView();
        this.screenBottom = activity.getWindowManager().getDefaultDisplay().getHeight();
        this.listener = new ViewTreeObserver.OnGlobalLayoutListener() { // from class: androidx.appcompat.app.SoftKeyBoardListener.1
            @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
            public void onGlobalLayout() {
                Rect rect = new Rect();
                SoftKeyBoardListener.this.rootView.getWindowVisibleDisplayFrame(rect);
                SoftKeyBoardListener softKeyBoardListener = SoftKeyBoardListener.this;
                if (softKeyBoardListener.isShow || softKeyBoardListener.screenBottom <= rect.bottom) {
                    SoftKeyBoardListener softKeyBoardListener2 = SoftKeyBoardListener.this;
                    if (softKeyBoardListener2.isShow && rect.bottom >= softKeyBoardListener2.screenBottom) {
                        SoftKeyBoardListener softKeyBoardListener3 = SoftKeyBoardListener.this;
                        softKeyBoardListener3.isShow = false;
                        if (softKeyBoardListener3.onSoftKeyBoardChangeListener != null) {
                            SoftKeyBoardListener.this.onSoftKeyBoardChangeListener.keyBoardHide();
                            return;
                        }
                        return;
                    }
                    return;
                }
                SoftKeyBoardListener softKeyBoardListener4 = SoftKeyBoardListener.this;
                softKeyBoardListener4.isShow = true;
                if (softKeyBoardListener4.onSoftKeyBoardChangeListener != null) {
                    SoftKeyBoardListener.this.onSoftKeyBoardChangeListener.keyBoardShow(SoftKeyBoardListener.this.screenBottom - rect.bottom);
                }
            }
        };
        this.rootView.getViewTreeObserver().addOnGlobalLayoutListener(this.listener);
        addLifeObServer(activity);
    }

    private void setOnSoftKeyBoardChangeListener(OnSoftKeyBoardChangeListener onSoftKeyBoardChangeListener) {
        this.onSoftKeyBoardChangeListener = onSoftKeyBoardChangeListener;
    }

    public static void setListener(Activity activity, OnSoftKeyBoardChangeListener onSoftKeyBoardChangeListener) {
        new SoftKeyBoardListener(activity).setOnSoftKeyBoardChangeListener(onSoftKeyBoardChangeListener);
    }

    public void addLifeObServer(Activity activity) {
        if (activity instanceof LifecycleOwner) {
            ((LifecycleOwner) activity).getLifecycle().addObserver(new LifecycleEventObserver() { // from class: androidx.appcompat.app.SoftKeyBoardListener.2
                @Override // androidx.lifecycle.LifecycleEventObserver
                public void onStateChanged(LifecycleOwner lifecycleOwner, Lifecycle.Event event) {
                    if (event == Lifecycle.Event.ON_DESTROY && SoftKeyBoardListener.this.rootView != null) {
                        SoftKeyBoardListener.this.rootView.getViewTreeObserver().removeOnGlobalLayoutListener(SoftKeyBoardListener.this.listener);
                    }
                }
            });
        }
    }
}
