package com.android.systemui;

import android.app.PendingIntent;
import android.content.Intent;
import android.view.View;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.statusbar.phone.StatusBar;
import dagger.Lazy;
import java.util.Optional;
import java.util.function.Consumer;
public class ActivityStarterDelegate implements ActivityStarter {
    private Optional<Lazy<StatusBar>> mActualStarter;

    public ActivityStarterDelegate(Optional<Lazy<StatusBar>> optional) {
        this.mActualStarter = optional;
    }

    @Override // com.android.systemui.plugins.ActivityStarter
    public void startPendingIntentDismissingKeyguard(PendingIntent pendingIntent) {
        this.mActualStarter.ifPresent(new Consumer(pendingIntent) { // from class: com.android.systemui.-$$Lambda$ActivityStarterDelegate$ADi9yiVtZ_7ObMe5Z0tk1YjrdVA
            public final /* synthetic */ PendingIntent f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ActivityStarterDelegate.lambda$startPendingIntentDismissingKeyguard$0(this.f$0, (Lazy) obj);
            }
        });
    }

    @Override // com.android.systemui.plugins.ActivityStarter
    public void startPendingIntentDismissingKeyguard(PendingIntent pendingIntent, Runnable runnable) {
        this.mActualStarter.ifPresent(new Consumer(pendingIntent, runnable) { // from class: com.android.systemui.-$$Lambda$ActivityStarterDelegate$INm749Eqo5FOmTBr8joulwrrt64
            public final /* synthetic */ PendingIntent f$0;
            public final /* synthetic */ Runnable f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ((StatusBar) ((Lazy) obj).get()).startPendingIntentDismissingKeyguard(this.f$0, this.f$1);
            }
        });
    }

    @Override // com.android.systemui.plugins.ActivityStarter
    public void startPendingIntentDismissingKeyguard(PendingIntent pendingIntent, Runnable runnable, View view) {
        this.mActualStarter.ifPresent(new Consumer(pendingIntent, runnable, view) { // from class: com.android.systemui.-$$Lambda$ActivityStarterDelegate$wcup9XfV8BD-xZsAFv2kWIfmGN0
            public final /* synthetic */ PendingIntent f$0;
            public final /* synthetic */ Runnable f$1;
            public final /* synthetic */ View f$2;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ((StatusBar) ((Lazy) obj).get()).startPendingIntentDismissingKeyguard(this.f$0, this.f$1, this.f$2);
            }
        });
    }

    @Override // com.android.systemui.plugins.ActivityStarter
    public void startActivity(Intent intent, boolean z, boolean z2, int i) {
        this.mActualStarter.ifPresent(new Consumer(intent, z, z2, i) { // from class: com.android.systemui.-$$Lambda$ActivityStarterDelegate$ILGza7s66HZ0nctdJ0wnDebSRW8
            public final /* synthetic */ Intent f$0;
            public final /* synthetic */ boolean f$1;
            public final /* synthetic */ boolean f$2;
            public final /* synthetic */ int f$3;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ((StatusBar) ((Lazy) obj).get()).startActivity(this.f$0, this.f$1, this.f$2, this.f$3);
            }
        });
    }

    static /* synthetic */ void lambda$startActivity$4(Intent intent, boolean z, Lazy lazy) {
        ((StatusBar) lazy.get()).startActivity(intent, z);
    }

    @Override // com.android.systemui.plugins.ActivityStarter
    public void startActivity(Intent intent, boolean z) {
        this.mActualStarter.ifPresent(new Consumer(intent, z) { // from class: com.android.systemui.-$$Lambda$ActivityStarterDelegate$EQWsLMWn8q7rwvIKj7BUOEWOer0
            public final /* synthetic */ Intent f$0;
            public final /* synthetic */ boolean f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ActivityStarterDelegate.lambda$startActivity$4(this.f$0, this.f$1, (Lazy) obj);
            }
        });
    }

    @Override // com.android.systemui.plugins.ActivityStarter
    public void startActivity(Intent intent, boolean z, boolean z2) {
        this.mActualStarter.ifPresent(new Consumer(intent, z, z2) { // from class: com.android.systemui.-$$Lambda$ActivityStarterDelegate$6Sj7OMH4lNAnb8MJLTpMcmyzi58
            public final /* synthetic */ Intent f$0;
            public final /* synthetic */ boolean f$1;
            public final /* synthetic */ boolean f$2;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ActivityStarterDelegate.lambda$startActivity$5(this.f$0, this.f$1, this.f$2, (Lazy) obj);
            }
        });
    }

    @Override // com.android.systemui.plugins.ActivityStarter
    public void startActivity(Intent intent, boolean z, ActivityStarter.Callback callback) {
        this.mActualStarter.ifPresent(new Consumer(intent, z, callback) { // from class: com.android.systemui.-$$Lambda$ActivityStarterDelegate$oudv1wNK3Nlq7Lmdo4di21Zs8MY
            public final /* synthetic */ Intent f$0;
            public final /* synthetic */ boolean f$1;
            public final /* synthetic */ ActivityStarter.Callback f$2;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ((StatusBar) ((Lazy) obj).get()).startActivity(this.f$0, this.f$1, this.f$2);
            }
        });
    }

    @Override // com.android.systemui.plugins.ActivityStarter
    public void postStartActivityDismissingKeyguard(Intent intent, int i) {
        this.mActualStarter.ifPresent(new Consumer(intent, i) { // from class: com.android.systemui.-$$Lambda$ActivityStarterDelegate$Bkt5K0j7l11YRIlpia_xFvXNPbk
            public final /* synthetic */ Intent f$0;
            public final /* synthetic */ int f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ActivityStarterDelegate.lambda$postStartActivityDismissingKeyguard$7(this.f$0, this.f$1, (Lazy) obj);
            }
        });
    }

    static /* synthetic */ void lambda$postStartActivityDismissingKeyguard$7(Intent intent, int i, Lazy lazy) {
        ((StatusBar) lazy.get()).postStartActivityDismissingKeyguard(intent, i);
    }

    @Override // com.android.systemui.plugins.ActivityStarter
    public void postStartActivityDismissingKeyguard(PendingIntent pendingIntent) {
        this.mActualStarter.ifPresent(new Consumer(pendingIntent) { // from class: com.android.systemui.-$$Lambda$ActivityStarterDelegate$ntMGdPXHlgGHJa34MKvZ31nUwKY
            public final /* synthetic */ PendingIntent f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ((StatusBar) ((Lazy) obj).get()).postStartActivityDismissingKeyguard(this.f$0);
            }
        });
    }

    @Override // com.android.systemui.plugins.ActivityStarter
    public void postQSRunnableDismissingKeyguard(Runnable runnable) {
        this.mActualStarter.ifPresent(new Consumer(runnable) { // from class: com.android.systemui.-$$Lambda$ActivityStarterDelegate$nAMiUKIuJCQJlUCym9gIzdU3mxI
            public final /* synthetic */ Runnable f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ((StatusBar) ((Lazy) obj).get()).postQSRunnableDismissingKeyguard(this.f$0);
            }
        });
    }

    @Override // com.android.systemui.plugins.ActivityStarter
    public void dismissKeyguardThenExecute(ActivityStarter.OnDismissAction onDismissAction, Runnable runnable, boolean z) {
        this.mActualStarter.ifPresent(new Consumer(runnable, z) { // from class: com.android.systemui.-$$Lambda$ActivityStarterDelegate$EdR7EnJaQsucB6gVTu3f0VVIJG0
            public final /* synthetic */ Runnable f$1;
            public final /* synthetic */ boolean f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ((StatusBar) ((Lazy) obj).get()).dismissKeyguardThenExecute(ActivityStarter.OnDismissAction.this, this.f$1, this.f$2);
            }
        });
    }
}
