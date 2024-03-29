package com.android.systemui.controls.management;

import android.content.ComponentName;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.RecyclerView;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0011R$layout;
import com.android.systemui.controls.ControlsServiceInfo;
import java.util.List;
import java.util.concurrent.Executor;
import kotlin.Unit;
import kotlin.collections.CollectionsKt__CollectionsKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: AppAdapter.kt */
public final class AppAdapter extends RecyclerView.Adapter<Holder> {
    private final AppAdapter$callback$1 callback;
    private final FavoritesRenderer favoritesRenderer;
    private final LayoutInflater layoutInflater;
    private List<ControlsServiceInfo> listOfServices = CollectionsKt__CollectionsKt.emptyList();
    private final Function1<ComponentName, Unit> onAppSelected;
    private final Resources resources;

    /* JADX DEBUG: Multi-variable search result rejected for r7v0, resolved type: kotlin.jvm.functions.Function1<? super android.content.ComponentName, kotlin.Unit> */
    /* JADX WARN: Multi-variable type inference failed */
    public AppAdapter(@NotNull Executor executor, @NotNull Executor executor2, @NotNull Lifecycle lifecycle, @NotNull ControlsListingController controlsListingController, @NotNull LayoutInflater layoutInflater, @NotNull Function1<? super ComponentName, Unit> function1, @NotNull FavoritesRenderer favoritesRenderer, @NotNull Resources resources) {
        Intrinsics.checkParameterIsNotNull(executor, "backgroundExecutor");
        Intrinsics.checkParameterIsNotNull(executor2, "uiExecutor");
        Intrinsics.checkParameterIsNotNull(lifecycle, "lifecycle");
        Intrinsics.checkParameterIsNotNull(controlsListingController, "controlsListingController");
        Intrinsics.checkParameterIsNotNull(layoutInflater, "layoutInflater");
        Intrinsics.checkParameterIsNotNull(function1, "onAppSelected");
        Intrinsics.checkParameterIsNotNull(favoritesRenderer, "favoritesRenderer");
        Intrinsics.checkParameterIsNotNull(resources, "resources");
        this.layoutInflater = layoutInflater;
        this.onAppSelected = function1;
        this.favoritesRenderer = favoritesRenderer;
        this.resources = resources;
        AppAdapter$callback$1 appAdapter$callback$1 = new AppAdapter$callback$1(this, executor, executor2);
        this.callback = appAdapter$callback$1;
        controlsListingController.observe(lifecycle, (Lifecycle) appAdapter$callback$1);
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    @NotNull
    public Holder onCreateViewHolder(@NotNull ViewGroup viewGroup, int i) {
        Intrinsics.checkParameterIsNotNull(viewGroup, "parent");
        View inflate = this.layoutInflater.inflate(C0011R$layout.controls_app_item, viewGroup, false);
        Intrinsics.checkExpressionValueIsNotNull(inflate, "layoutInflater.inflate(R…_app_item, parent, false)");
        return new Holder(inflate, this.favoritesRenderer);
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        return this.listOfServices.size();
    }

    public void onBindViewHolder(@NotNull Holder holder, int i) {
        Intrinsics.checkParameterIsNotNull(holder, "holder");
        holder.bindData(this.listOfServices.get(i));
        holder.itemView.setOnClickListener(new View.OnClickListener(this, i) { // from class: com.android.systemui.controls.management.AppAdapter$onBindViewHolder$1
            final /* synthetic */ int $index;
            final /* synthetic */ AppAdapter this$0;

            {
                this.this$0 = r1;
                this.$index = r2;
            }

            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                AppAdapter.access$getOnAppSelected$p(this.this$0).invoke(ComponentName.unflattenFromString(((ControlsServiceInfo) AppAdapter.access$getListOfServices$p(this.this$0).get(this.$index)).getKey()));
            }
        });
    }

    /* compiled from: AppAdapter.kt */
    public static final class Holder extends RecyclerView.ViewHolder {
        @NotNull
        private final FavoritesRenderer favRenderer;
        private final TextView favorites;
        private final ImageView icon;
        private final TextView title;

        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        public Holder(@NotNull View view, @NotNull FavoritesRenderer favoritesRenderer) {
            super(view);
            Intrinsics.checkParameterIsNotNull(view, "view");
            Intrinsics.checkParameterIsNotNull(favoritesRenderer, "favRenderer");
            this.favRenderer = favoritesRenderer;
            View requireViewById = this.itemView.requireViewById(16908294);
            Intrinsics.checkExpressionValueIsNotNull(requireViewById, "itemView.requireViewById…droid.internal.R.id.icon)");
            this.icon = (ImageView) requireViewById;
            View requireViewById2 = this.itemView.requireViewById(16908310);
            Intrinsics.checkExpressionValueIsNotNull(requireViewById2, "itemView.requireViewById…roid.internal.R.id.title)");
            this.title = (TextView) requireViewById2;
            View requireViewById3 = this.itemView.requireViewById(C0008R$id.favorites);
            Intrinsics.checkExpressionValueIsNotNull(requireViewById3, "itemView.requireViewById(R.id.favorites)");
            this.favorites = (TextView) requireViewById3;
        }

        public final void bindData(@NotNull ControlsServiceInfo controlsServiceInfo) {
            Intrinsics.checkParameterIsNotNull(controlsServiceInfo, "data");
            this.icon.setImageDrawable(controlsServiceInfo.loadIcon());
            this.title.setText(controlsServiceInfo.loadLabel());
            FavoritesRenderer favoritesRenderer = this.favRenderer;
            ComponentName componentName = controlsServiceInfo.componentName;
            Intrinsics.checkExpressionValueIsNotNull(componentName, "data.componentName");
            String renderFavoritesForComponent = favoritesRenderer.renderFavoritesForComponent(componentName);
            this.favorites.setText(renderFavoritesForComponent);
            this.favorites.setVisibility(renderFavoritesForComponent == null ? 8 : 0);
        }
    }
}
