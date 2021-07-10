package com.android.systemui.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.android.systemui.broadcast.logging.BroadcastDispatcherLogger;
/* compiled from: ActionReceiver.kt */
final class ActionReceiver$onReceive$1 implements Runnable {
    final /* synthetic */ Context $context;
    final /* synthetic */ int $id;
    final /* synthetic */ Intent $intent;
    final /* synthetic */ ActionReceiver this$0;

    ActionReceiver$onReceive$1(ActionReceiver actionReceiver, Intent intent, Context context, int i) {
        this.this$0 = actionReceiver;
        this.$intent = intent;
        this.$context = context;
        this.$id = i;
    }

    @Override // java.lang.Runnable
    public final void run() {
        for (ReceiverData receiverData : ActionReceiver.access$getReceiverDatas$p(this.this$0)) {
            if (receiverData.getFilter().matchCategories(this.$intent.getCategories()) == null) {
                receiverData.getExecutor().execute(new Runnable(receiverData, this) { // from class: com.android.systemui.broadcast.ActionReceiver$onReceive$1$$special$$inlined$forEach$lambda$1
                    final /* synthetic */ ReceiverData $it;
                    final /* synthetic */ ActionReceiver$onReceive$1 this$0;

                    {
                        this.$it = r1;
                        this.this$0 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        this.$it.getReceiver().setPendingResult(this.this$0.this$0.getPendingResult());
                        BroadcastReceiver receiver = this.$it.getReceiver();
                        ActionReceiver$onReceive$1 actionReceiver$onReceive$1 = this.this$0;
                        receiver.onReceive(actionReceiver$onReceive$1.$context, actionReceiver$onReceive$1.$intent);
                        BroadcastDispatcherLogger broadcastDispatcherLogger = this.this$0.this$0.logger;
                        ActionReceiver$onReceive$1 actionReceiver$onReceive$12 = this.this$0;
                        broadcastDispatcherLogger.logBroadcastDispatched(actionReceiver$onReceive$12.$id, actionReceiver$onReceive$12.this$0.action, this.$it.getReceiver());
                    }
                });
            }
        }
    }
}
