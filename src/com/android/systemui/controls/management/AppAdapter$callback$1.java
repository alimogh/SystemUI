package com.android.systemui.controls.management;

import android.content.res.Configuration;
import com.android.systemui.controls.ControlsServiceInfo;
import com.android.systemui.controls.management.ControlsListingController;
import java.text.Collator;
import java.util.List;
import java.util.concurrent.Executor;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: AppAdapter.kt */
public final class AppAdapter$callback$1 implements ControlsListingController.ControlsListingCallback {
    final /* synthetic */ Executor $backgroundExecutor;
    final /* synthetic */ Executor $uiExecutor;
    final /* synthetic */ AppAdapter this$0;

    AppAdapter$callback$1(AppAdapter appAdapter, Executor executor, Executor executor2) {
        this.this$0 = appAdapter;
        this.$backgroundExecutor = executor;
        this.$uiExecutor = executor2;
    }

    @Override // com.android.systemui.controls.management.ControlsListingController.ControlsListingCallback
    public void onServicesUpdated(@NotNull List<ControlsServiceInfo> list) {
        Intrinsics.checkParameterIsNotNull(list, "serviceInfos");
        this.$backgroundExecutor.execute(new Runnable(this, list) { // from class: com.android.systemui.controls.management.AppAdapter$callback$1$onServicesUpdated$1
            final /* synthetic */ List $serviceInfos;
            final /* synthetic */ AppAdapter$callback$1 this$0;

            {
                this.this$0 = r1;
                this.$serviceInfos = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                Configuration configuration = this.this$0.this$0.resources.getConfiguration();
                Intrinsics.checkExpressionValueIsNotNull(configuration, "resources.configuration");
                Collator instance = Collator.getInstance(configuration.getLocales().get(0));
                Intrinsics.checkExpressionValueIsNotNull(instance, "collator");
                AppAdapter$callback$1$onServicesUpdated$1$$special$$inlined$compareBy$1 appAdapter$callback$1$onServicesUpdated$1$$special$$inlined$compareBy$1 = 
                /*  JADX ERROR: Method code generation error
                    jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0025: CONSTRUCTOR  (r1v3 'appAdapter$callback$1$onServicesUpdated$1$$special$$inlined$compareBy$1' com.android.systemui.controls.management.AppAdapter$callback$1$onServicesUpdated$1$$special$$inlined$compareBy$1) = (r0v6 'instance' java.text.Collator) call: com.android.systemui.controls.management.AppAdapter$callback$1$onServicesUpdated$1$$special$$inlined$compareBy$1.<init>(java.util.Comparator):void type: CONSTRUCTOR in method: com.android.systemui.controls.management.AppAdapter$callback$1$onServicesUpdated$1.run():void, file: classes.dex
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:282)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                    	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:104)
                    	at jadx.core.dex.nodes.IBlock.generate(IBlock.java:15)
                    	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:64)
                    	at jadx.core.dex.regions.Region.generate(Region.java:35)
                    	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:64)
                    	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:261)
                    	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:254)
                    	at jadx.core.codegen.ClassGen.addMethodCode(ClassGen.java:345)
                    	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:298)
                    	at jadx.core.codegen.ClassGen.lambda$addInnerClsAndMethods$3(ClassGen.java:267)
                    	at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.accept(ForEachOps.java:183)
                    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1540)
                    	at java.base/java.util.stream.SortedOps$RefSortingSink.end(SortedOps.java:395)
                    	at java.base/java.util.stream.Sink$ChainedReference.end(Sink.java:258)
                    Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: com.android.systemui.controls.management.AppAdapter$callback$1$onServicesUpdated$1$$special$$inlined$compareBy$1, state: NOT_LOADED
                    	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:220)
                    	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:657)
                    	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:390)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:275)
                    	... 15 more
                    */
                /*
                    this = this;
                    com.android.systemui.controls.management.AppAdapter$callback$1 r0 = r3.this$0
                    com.android.systemui.controls.management.AppAdapter r0 = r0.this$0
                    android.content.res.Resources r0 = com.android.systemui.controls.management.AppAdapter.access$getResources$p(r0)
                    android.content.res.Configuration r0 = r0.getConfiguration()
                    java.lang.String r1 = "resources.configuration"
                    kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r0, r1)
                    android.os.LocaleList r0 = r0.getLocales()
                    r1 = 0
                    java.util.Locale r0 = r0.get(r1)
                    java.text.Collator r0 = java.text.Collator.getInstance(r0)
                    java.lang.String r1 = "collator"
                    kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r0, r1)
                    com.android.systemui.controls.management.AppAdapter$callback$1$onServicesUpdated$1$$special$$inlined$compareBy$1 r1 = new com.android.systemui.controls.management.AppAdapter$callback$1$onServicesUpdated$1$$special$$inlined$compareBy$1
                    r1.<init>(r0)
                    com.android.systemui.controls.management.AppAdapter$callback$1 r0 = r3.this$0
                    com.android.systemui.controls.management.AppAdapter r0 = r0.this$0
                    java.util.List r2 = r3.$serviceInfos
                    java.util.List r1 = kotlin.collections.CollectionsKt.sortedWith(r2, r1)
                    com.android.systemui.controls.management.AppAdapter.access$setListOfServices$p(r0, r1)
                    com.android.systemui.controls.management.AppAdapter$callback$1 r0 = r3.this$0
                    java.util.concurrent.Executor r0 = r0.$uiExecutor
                    com.android.systemui.controls.management.AppAdapter$callback$1$onServicesUpdated$1$1 r1 = new com.android.systemui.controls.management.AppAdapter$callback$1$onServicesUpdated$1$1
                    com.android.systemui.controls.management.AppAdapter$callback$1 r3 = r3.this$0
                    com.android.systemui.controls.management.AppAdapter r3 = r3.this$0
                    r1.<init>(r3)
                    com.android.systemui.controls.management.AppAdapter$sam$java_lang_Runnable$0 r3 = new com.android.systemui.controls.management.AppAdapter$sam$java_lang_Runnable$0
                    r3.<init>(r1)
                    r0.execute(r3)
                    return
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.controls.management.AppAdapter$callback$1$onServicesUpdated$1.run():void");
            }
        });
    }
}
