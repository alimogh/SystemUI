package com.android.systemui.controls.management;

import android.content.ComponentName;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Icon;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.core.view.ViewCompat;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0015R$string;
import com.android.systemui.controls.ControlInterface;
import com.android.systemui.controls.management.ControlsModel;
import com.android.systemui.controls.ui.RenderInfo;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KDeclarationContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: ControlAdapter.kt */
public final class ControlHolder extends Holder {
    private final ControlHolderAccessibilityDelegate accessibilityDelegate;
    private final CheckBox favorite;
    @NotNull
    private final Function2<String, Boolean, Unit> favoriteCallback;
    private final String favoriteStateDescription;
    private final ImageView icon;
    @Nullable
    private final ControlsModel.MoveHelper moveHelper;
    private final String notFavoriteStateDescription;
    private final TextView removed;
    private final TextView subtitle;
    private final TextView title;

    @NotNull
    public final Function2<String, Boolean, Unit> getFavoriteCallback() {
        return this.favoriteCallback;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r4v0, resolved type: kotlin.jvm.functions.Function2<? super java.lang.String, ? super java.lang.Boolean, kotlin.Unit> */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public ControlHolder(@NotNull View view, @Nullable ControlsModel.MoveHelper moveHelper, @NotNull Function2<? super String, ? super Boolean, Unit> function2) {
        super(view, null);
        Intrinsics.checkParameterIsNotNull(view, "view");
        Intrinsics.checkParameterIsNotNull(function2, "favoriteCallback");
        this.moveHelper = moveHelper;
        this.favoriteCallback = function2;
        View view2 = this.itemView;
        Intrinsics.checkExpressionValueIsNotNull(view2, "itemView");
        this.favoriteStateDescription = view2.getContext().getString(C0015R$string.accessibility_control_favorite);
        View view3 = this.itemView;
        Intrinsics.checkExpressionValueIsNotNull(view3, "itemView");
        this.notFavoriteStateDescription = view3.getContext().getString(C0015R$string.accessibility_control_not_favorite);
        View requireViewById = this.itemView.requireViewById(C0008R$id.icon);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById, "itemView.requireViewById(R.id.icon)");
        this.icon = (ImageView) requireViewById;
        View requireViewById2 = this.itemView.requireViewById(C0008R$id.title);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById2, "itemView.requireViewById(R.id.title)");
        this.title = (TextView) requireViewById2;
        View requireViewById3 = this.itemView.requireViewById(C0008R$id.subtitle);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById3, "itemView.requireViewById(R.id.subtitle)");
        this.subtitle = (TextView) requireViewById3;
        View requireViewById4 = this.itemView.requireViewById(C0008R$id.status);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById4, "itemView.requireViewById(R.id.status)");
        this.removed = (TextView) requireViewById4;
        View requireViewById5 = this.itemView.requireViewById(C0008R$id.favorite);
        CheckBox checkBox = (CheckBox) requireViewById5;
        checkBox.setVisibility(0);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById5, "itemView.requireViewById…lity = View.VISIBLE\n    }");
        this.favorite = checkBox;
        ControlHolderAccessibilityDelegate controlHolderAccessibilityDelegate = new ControlHolderAccessibilityDelegate(new Function1<Boolean, CharSequence>(this) { // from class: com.android.systemui.controls.management.ControlHolder$accessibilityDelegate$1
            @Override // kotlin.jvm.internal.CallableReference
            public final String getName() {
                return "stateDescription";
            }

            @Override // kotlin.jvm.internal.CallableReference
            public final KDeclarationContainer getOwner() {
                return Reflection.getOrCreateKotlinClass(ControlHolder.class);
            }

            @Override // kotlin.jvm.internal.CallableReference
            public final String getSignature() {
                return "stateDescription(Z)Ljava/lang/CharSequence;";
            }

            /* Return type fixed from 'java.lang.Object' to match base method */
            /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
            @Override // kotlin.jvm.functions.Function1
            public /* bridge */ /* synthetic */ CharSequence invoke(Boolean bool) {
                return invoke(bool.booleanValue());
            }

            @Nullable
            public final CharSequence invoke(boolean z) {
                return ((ControlHolder) this.receiver).stateDescription(z);
            }
        }, new Function0<Integer>(this) { // from class: com.android.systemui.controls.management.ControlHolder$accessibilityDelegate$2
            @Override // kotlin.jvm.internal.CallableReference
            public final String getName() {
                return "getLayoutPosition";
            }

            @Override // kotlin.jvm.internal.CallableReference
            public final KDeclarationContainer getOwner() {
                return Reflection.getOrCreateKotlinClass(ControlHolder.class);
            }

            @Override // kotlin.jvm.internal.CallableReference
            public final String getSignature() {
                return "getLayoutPosition()I";
            }

            /* Return type fixed from 'int' to match base method */
            /* JADX WARN: Type inference failed for: r0v3, types: [int, java.lang.Integer] */
            /* JADX WARNING: Unknown variable types count: 1 */
            @Override // kotlin.jvm.functions.Function0
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public final java.lang.Integer invoke() {
                /*
                    r0 = this;
                    java.lang.Object r0 = r0.receiver
                    com.android.systemui.controls.management.ControlHolder r0 = (com.android.systemui.controls.management.ControlHolder) r0
                    int r0 = r0.getLayoutPosition()
                    return r0
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.controls.management.ControlHolder$accessibilityDelegate$2.invoke():int");
            }
        }, this.moveHelper);
        this.accessibilityDelegate = controlHolderAccessibilityDelegate;
        ViewCompat.setAccessibilityDelegate(this.itemView, controlHolderAccessibilityDelegate);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final CharSequence stateDescription(boolean z) {
        if (!z) {
            return this.notFavoriteStateDescription;
        }
        if (this.moveHelper == null) {
            return this.favoriteStateDescription;
        }
        View view = this.itemView;
        Intrinsics.checkExpressionValueIsNotNull(view, "itemView");
        return view.getContext().getString(C0015R$string.accessibility_control_favorite_position, Integer.valueOf(getLayoutPosition() + 1));
    }

    @Override // com.android.systemui.controls.management.Holder
    public void bindData(@NotNull ElementWrapper elementWrapper) {
        CharSequence charSequence;
        Intrinsics.checkParameterIsNotNull(elementWrapper, "wrapper");
        ControlInterface controlInterface = (ControlInterface) elementWrapper;
        RenderInfo renderInfo = getRenderInfo(controlInterface.getComponent(), controlInterface.getDeviceType());
        this.title.setText(controlInterface.getTitle());
        this.subtitle.setText(controlInterface.getSubtitle());
        updateFavorite(controlInterface.getFavorite());
        TextView textView = this.removed;
        if (controlInterface.getRemoved()) {
            View view = this.itemView;
            Intrinsics.checkExpressionValueIsNotNull(view, "itemView");
            charSequence = view.getContext().getText(C0015R$string.controls_removed);
        } else {
            charSequence = "";
        }
        textView.setText(charSequence);
        this.itemView.setOnClickListener(new View.OnClickListener(this, elementWrapper) { // from class: com.android.systemui.controls.management.ControlHolder$bindData$1
            final /* synthetic */ ElementWrapper $wrapper;
            final /* synthetic */ ControlHolder this$0;

            {
                this.this$0 = r1;
                this.$wrapper = r2;
            }

            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) {
                ControlHolder controlHolder = this.this$0;
                controlHolder.updateFavorite(!controlHolder.favorite.isChecked());
                this.this$0.getFavoriteCallback().invoke(((ControlInterface) this.$wrapper).getControlId(), Boolean.valueOf(this.this$0.favorite.isChecked()));
            }
        });
        applyRenderInfo(renderInfo, controlInterface);
    }

    @Override // com.android.systemui.controls.management.Holder
    public void updateFavorite(boolean z) {
        this.favorite.setChecked(z);
        this.accessibilityDelegate.setFavorite(z);
        View view = this.itemView;
        Intrinsics.checkExpressionValueIsNotNull(view, "itemView");
        view.setStateDescription(stateDescription(z));
    }

    private final RenderInfo getRenderInfo(ComponentName componentName, int i) {
        RenderInfo.Companion companion = RenderInfo.Companion;
        View view = this.itemView;
        Intrinsics.checkExpressionValueIsNotNull(view, "itemView");
        Context context = view.getContext();
        Intrinsics.checkExpressionValueIsNotNull(context, "itemView.context");
        return RenderInfo.Companion.lookup$default(companion, context, componentName, i, 0, 8, null);
    }

    private final void applyRenderInfo(RenderInfo renderInfo, ControlInterface controlInterface) {
        View view = this.itemView;
        Intrinsics.checkExpressionValueIsNotNull(view, "itemView");
        Context context = view.getContext();
        ColorStateList colorStateList = context.getResources().getColorStateList(renderInfo.getForeground(), context.getTheme());
        Icon customIcon = controlInterface.getCustomIcon();
        if (customIcon != null) {
            this.icon.setImageIcon(customIcon);
            return;
        }
        this.icon.setImageDrawable(renderInfo.getIcon());
        if (controlInterface.getDeviceType() != 52) {
            this.icon.setImageTintList(colorStateList);
        }
    }
}
