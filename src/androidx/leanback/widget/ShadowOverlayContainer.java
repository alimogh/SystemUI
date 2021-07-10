package androidx.leanback.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import androidx.leanback.R$dimen;
public class ShadowOverlayContainer extends FrameLayout {
    private static final Rect sTempRect = new Rect();
    private boolean mInitialized;
    int mOverlayColor;
    private Paint mOverlayPaint;
    private View mWrappedView;

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    public ShadowOverlayContainer(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ShadowOverlayContainer(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        useStaticShadow();
        useDynamicShadow();
    }

    public static boolean supportsShadow() {
        return StaticShadowHelper.supportsShadow();
    }

    public static boolean supportsDynamicShadow() {
        return ShadowHelper.supportsDynamicShadow();
    }

    public void useDynamicShadow() {
        useDynamicShadow(getResources().getDimension(R$dimen.lb_material_shadow_normal_z), getResources().getDimension(R$dimen.lb_material_shadow_focused_z));
    }

    public void useDynamicShadow(float f, float f2) {
        if (!this.mInitialized) {
            supportsDynamicShadow();
            return;
        }
        throw new IllegalStateException("Already initialized");
    }

    public void useStaticShadow() {
        if (!this.mInitialized) {
            supportsShadow();
            return;
        }
        throw new IllegalStateException("Already initialized");
    }

    @Override // android.view.View
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (this.mOverlayPaint != null && this.mOverlayColor != 0) {
            canvas.drawRect((float) this.mWrappedView.getLeft(), (float) this.mWrappedView.getTop(), (float) this.mWrappedView.getRight(), (float) this.mWrappedView.getBottom(), this.mOverlayPaint);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.FrameLayout, android.view.View, android.view.ViewGroup
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        View view;
        super.onLayout(z, i, i2, i3, i4);
        if (z && (view = this.mWrappedView) != null) {
            sTempRect.left = (int) view.getPivotX();
            sTempRect.top = (int) this.mWrappedView.getPivotY();
            offsetDescendantRectToMyCoords(this.mWrappedView, sTempRect);
            setPivotX((float) sTempRect.left);
            setPivotY((float) sTempRect.top);
        }
    }
}
