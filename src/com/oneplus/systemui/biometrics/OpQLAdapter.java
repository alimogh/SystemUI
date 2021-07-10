package com.oneplus.systemui.biometrics;

import android.content.Context;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.recyclerview.widget.RecyclerView;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0006R$drawable;
import com.oneplus.util.OpUtils;
import java.util.ArrayList;
public class OpQLAdapter extends RecyclerView.Adapter<ViewHolder> {
    private final int mAppIconSize = OpUtils.convertDpToFixedPx2(this.mContext.getResources().getDimension(C0005R$dimen.op_quick_launch_app_icon_size));
    private ArrayList<ActionInfo> mAppMap;
    private ArrayList<Drawable> mAppShapeIcon;
    private ColorDrawable mBackgroundDrawable = new ColorDrawable(-1);
    private Context mContext;
    private final int mIconPadding = this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.op_quick_launch_icon_padding);
    private final int mIconSize;
    private final int mShortcutIconSize = OpUtils.convertDpToFixedPx2(this.mContext.getResources().getDimension(C0005R$dimen.op_quick_launch_shortcut_icon_size));

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private View mView;

        public ViewHolder(View view) {
            super(view);
            this.mView = view;
        }

        public View getView() {
            return this.mView;
        }
    }

    public OpQLAdapter(Context context, ArrayList<ActionInfo> arrayList) {
        this.mContext = context;
        this.mAppMap = arrayList;
        this.mIconSize = OpUtils.convertDpToFixedPx2(context.getResources().getDimension(C0005R$dimen.op_quick_launch_icon_size));
        initShapeIcon();
    }

    private void initShapeIcon() {
        this.mAppShapeIcon = new ArrayList<>();
        for (int i = 0; i < this.mAppMap.size(); i++) {
            ActionInfo actionInfo = this.mAppMap.get(i);
            LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{this.mBackgroundDrawable, actionInfo.mAppIcon});
            int i2 = this.mIconSize;
            layerDrawable.setLayerSize(0, i2, i2);
            layerDrawable.setLayerGravity(1, 17);
            if (actionInfo.mShortcutId != null) {
                int i3 = this.mShortcutIconSize;
                layerDrawable.setLayerSize(1, i3, i3);
            } else {
                int i4 = this.mAppIconSize;
                layerDrawable.setLayerSize(1, i4, i4);
            }
            this.mAppShapeIcon.add(new AdaptiveIconDrawable(null, layerDrawable));
        }
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        ImageView imageView = new ImageView(viewGroup.getContext());
        imageView.setImageResource(C0006R$drawable.ic_android);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setAlpha(0.5f);
        imageView.setScaleX(0.67f);
        imageView.setScaleY(0.67f);
        int i2 = this.mIconSize;
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(i2, i2);
        int i3 = this.mIconPadding;
        ((ViewGroup.MarginLayoutParams) layoutParams).leftMargin = i3;
        ((ViewGroup.MarginLayoutParams) layoutParams).rightMargin = i3;
        imageView.setLayoutParams(layoutParams);
        return new ViewHolder(imageView);
    }

    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        ActionInfo actionInfo = this.mAppMap.get(i);
        ImageView imageView = (ImageView) viewHolder.getView();
        Drawable drawable = this.mAppShapeIcon.get(i);
        if (!(imageView == null || drawable == null)) {
            imageView.setImageDrawable(drawable);
        }
        viewHolder.getView().setTag(actionInfo);
    }

    public void onQLExit() {
        this.mContext = null;
        this.mAppMap = null;
        this.mAppShapeIcon = null;
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        return this.mAppMap.size();
    }

    public int getIconPadding() {
        return this.mIconPadding;
    }

    public static class OPQuickPayConfig {
        Drawable appIcon = null;
        String className;
        int index;
        boolean isDefault = false;
        String packageName;
        String switchName;
        String targetClassName;
        String urlScheme;

        public String toString() {
            return "Index: " + this.index + " Package: " + this.packageName + " ClassName: " + this.className + " UrlScheme: " + this.urlScheme + " TartgetClassName: " + this.targetClassName + " SwitchName: " + this.switchName;
        }
    }

    public static class ActionInfo {
        String mActionName;
        Drawable mAppIcon;
        String mLabel;
        String mPackageName;
        int mPaymentWhich;
        OPQuickPayConfig mQuickPayConfig;
        String mShortcutId;
        int mUid;
        int mWxMiniProgramWhich;

        public void setActionName(String str) {
            this.mActionName = str;
        }

        public void setPackage(String str) {
            this.mPackageName = str;
        }

        public void setShortcutId(String str) {
            this.mShortcutId = str;
        }

        public void setUid(String str) {
            try {
                this.mUid = Integer.parseInt(str);
            } catch (NumberFormatException unused) {
                this.mUid = 0;
            }
        }

        public String toString() {
            return "Name:" + this.mActionName + " Package:" + this.mPackageName + " ShortcutId:" + this.mShortcutId + " uid:" + this.mUid;
        }
    }
}
