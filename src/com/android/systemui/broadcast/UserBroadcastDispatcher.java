package com.android.systemui.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Looper;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Preconditions;
import com.android.systemui.Dumpable;
import com.android.systemui.broadcast.logging.BroadcastDispatcherLogger;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import kotlin.Unit;
import kotlin.collections.CollectionsKt__MutableCollectionsKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlin.sequences.Sequence;
import kotlin.sequences.SequencesKt__SequencesKt;
import org.jetbrains.annotations.NotNull;
/* compiled from: UserBroadcastDispatcher.kt */
public class UserBroadcastDispatcher implements Dumpable {
    @NotNull
    private final ArrayMap<String, ActionReceiver> actionsToActionsReceivers = new ArrayMap<>();
    private final Executor bgExecutor;
    private final UserBroadcastDispatcher$bgHandler$1 bgHandler = new UserBroadcastDispatcher$bgHandler$1(this, this.bgLooper);
    private final Looper bgLooper;
    private final Context context;
    private final BroadcastDispatcherLogger logger;
    private final ArrayMap<BroadcastReceiver, Set<String>> receiverToActions = new ArrayMap<>();
    private final int userId;

    public static /* synthetic */ void actionsToActionsReceivers$annotations() {
    }

    @Override // com.android.systemui.Dumpable
    public void dump(@NotNull FileDescriptor fileDescriptor, @NotNull PrintWriter printWriter, @NotNull String[] strArr) {
        Intrinsics.checkParameterIsNotNull(fileDescriptor, "fd");
        Intrinsics.checkParameterIsNotNull(printWriter, "pw");
        Intrinsics.checkParameterIsNotNull(strArr, "args");
        boolean z = printWriter instanceof IndentingPrintWriter;
        if (z) {
            ((IndentingPrintWriter) printWriter).increaseIndent();
        }
        for (Map.Entry<String, ActionReceiver> entry : this.actionsToActionsReceivers.entrySet()) {
            printWriter.println(entry.getKey() + ':');
            entry.getValue().dump(fileDescriptor, printWriter, strArr);
        }
        if (z) {
            ((IndentingPrintWriter) printWriter).decreaseIndent();
        }
    }

    public UserBroadcastDispatcher(@NotNull Context context, int i, @NotNull Looper looper, @NotNull Executor executor, @NotNull BroadcastDispatcherLogger broadcastDispatcherLogger) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(looper, "bgLooper");
        Intrinsics.checkParameterIsNotNull(executor, "bgExecutor");
        Intrinsics.checkParameterIsNotNull(broadcastDispatcherLogger, "logger");
        this.context = context;
        this.userId = i;
        this.bgLooper = looper;
        this.bgExecutor = executor;
        this.logger = broadcastDispatcherLogger;
    }

    static {
        new AtomicInteger(0);
    }

    public final boolean isReceiverReferenceHeld$packages__apps__OPSystemUI__android_common__OPSystemUI_core(@NotNull BroadcastReceiver broadcastReceiver) {
        boolean z;
        Intrinsics.checkParameterIsNotNull(broadcastReceiver, "receiver");
        Collection<ActionReceiver> values = this.actionsToActionsReceivers.values();
        Intrinsics.checkExpressionValueIsNotNull(values, "actionsToActionsReceivers.values");
        if (!(values instanceof Collection) || !values.isEmpty()) {
            Iterator<T> it = values.iterator();
            while (true) {
                if (it.hasNext()) {
                    if (((ActionReceiver) it.next()).hasReceiver(broadcastReceiver)) {
                        z = true;
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        z = false;
        return z || this.receiverToActions.containsKey(broadcastReceiver);
    }

    public final void registerReceiver(@NotNull ReceiverData receiverData) {
        Intrinsics.checkParameterIsNotNull(receiverData, "receiverData");
        this.bgHandler.obtainMessage(0, receiverData).sendToTarget();
    }

    public final void unregisterReceiver(@NotNull BroadcastReceiver broadcastReceiver) {
        Intrinsics.checkParameterIsNotNull(broadcastReceiver, "receiver");
        this.bgHandler.obtainMessage(1, broadcastReceiver).sendToTarget();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void handleRegisterReceiver(ReceiverData receiverData) {
        Sequence sequence;
        Looper looper = this.bgHandler.getLooper();
        Intrinsics.checkExpressionValueIsNotNull(looper, "bgHandler.looper");
        Preconditions.checkState(looper.isCurrentThread(), "This method should only be called from BG thread");
        ArrayMap<BroadcastReceiver, Set<String>> arrayMap = this.receiverToActions;
        BroadcastReceiver receiver = receiverData.getReceiver();
        Set<String> set = arrayMap.get(receiver);
        if (set == null) {
            set = new ArraySet<>();
            arrayMap.put(receiver, set);
        }
        Set<String> set2 = set;
        Iterator<String> actionsIterator = receiverData.getFilter().actionsIterator();
        if (actionsIterator == null || (sequence = SequencesKt__SequencesKt.asSequence(actionsIterator)) == null) {
            sequence = SequencesKt__SequencesKt.emptySequence();
        }
        boolean unused = CollectionsKt__MutableCollectionsKt.addAll(set2, sequence);
        Iterator<String> actionsIterator2 = receiverData.getFilter().actionsIterator();
        Intrinsics.checkExpressionValueIsNotNull(actionsIterator2, "receiverData.filter.actionsIterator()");
        while (actionsIterator2.hasNext()) {
            String next = actionsIterator2.next();
            ArrayMap<String, ActionReceiver> arrayMap2 = this.actionsToActionsReceivers;
            ActionReceiver actionReceiver = arrayMap2.get(next);
            if (actionReceiver == null) {
                Intrinsics.checkExpressionValueIsNotNull(next, "it");
                actionReceiver = createActionReceiver$packages__apps__OPSystemUI__android_common__OPSystemUI_core(next);
                arrayMap2.put(next, actionReceiver);
            }
            actionReceiver.addReceiverData(receiverData);
        }
        this.logger.logReceiverRegistered(this.userId, receiverData.getReceiver());
    }

    @NotNull
    public ActionReceiver createActionReceiver$packages__apps__OPSystemUI__android_common__OPSystemUI_core(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "action");
        return new ActionReceiver(str, this.userId, new Function2<BroadcastReceiver, IntentFilter, Unit>(this) { // from class: com.android.systemui.broadcast.UserBroadcastDispatcher$createActionReceiver$1
            final /* synthetic */ UserBroadcastDispatcher this$0;

            {
                this.this$0 = r1;
            }

            /* Return type fixed from 'java.lang.Object' to match base method */
            /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object, java.lang.Object] */
            @Override // kotlin.jvm.functions.Function2
            public /* bridge */ /* synthetic */ Unit invoke(BroadcastReceiver broadcastReceiver, IntentFilter intentFilter) {
                invoke(broadcastReceiver, intentFilter);
                return Unit.INSTANCE;
            }

            public final void invoke(@NotNull BroadcastReceiver broadcastReceiver, @NotNull IntentFilter intentFilter) {
                Intrinsics.checkParameterIsNotNull(broadcastReceiver, "$receiver");
                Intrinsics.checkParameterIsNotNull(intentFilter, "it");
                this.this$0.context.registerReceiverAsUser(broadcastReceiver, UserHandle.of(this.this$0.userId), intentFilter, null, this.this$0.bgHandler);
                this.this$0.logger.logContextReceiverRegistered(this.this$0.userId, intentFilter);
            }
        }, new Function1<BroadcastReceiver, Unit>(this, str) { // from class: com.android.systemui.broadcast.UserBroadcastDispatcher$createActionReceiver$2
            final /* synthetic */ String $action;
            final /* synthetic */ UserBroadcastDispatcher this$0;

            {
                this.this$0 = r1;
                this.$action = r2;
            }

            /* Return type fixed from 'java.lang.Object' to match base method */
            /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
            @Override // kotlin.jvm.functions.Function1
            public /* bridge */ /* synthetic */ Unit invoke(BroadcastReceiver broadcastReceiver) {
                invoke(broadcastReceiver);
                return Unit.INSTANCE;
            }

            public final void invoke(@NotNull BroadcastReceiver broadcastReceiver) {
                Intrinsics.checkParameterIsNotNull(broadcastReceiver, "$receiver");
                try {
                    this.this$0.context.unregisterReceiver(broadcastReceiver);
                    this.this$0.logger.logContextReceiverUnregistered(this.this$0.userId, this.$action);
                } catch (IllegalArgumentException e) {
                    Log.e("UserBroadcastDispatcher", "Trying to unregister unregistered receiver for user " + this.this$0.userId + ", action " + this.$action, new IllegalStateException(e));
                }
            }
        }, this.bgExecutor, this.logger);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void handleUnregisterReceiver(BroadcastReceiver broadcastReceiver) {
        Looper looper = this.bgHandler.getLooper();
        Intrinsics.checkExpressionValueIsNotNull(looper, "bgHandler.looper");
        Preconditions.checkState(looper.isCurrentThread(), "This method should only be called from BG thread");
        Set<String> orDefault = this.receiverToActions.getOrDefault(broadcastReceiver, new LinkedHashSet());
        Intrinsics.checkExpressionValueIsNotNull(orDefault, "receiverToActions.getOrD…receiver, mutableSetOf())");
        for (String str : orDefault) {
            ActionReceiver actionReceiver = this.actionsToActionsReceivers.get(str);
            if (actionReceiver != null) {
                actionReceiver.removeReceiver(broadcastReceiver);
            }
        }
        this.receiverToActions.remove(broadcastReceiver);
        this.logger.logReceiverUnregistered(this.userId, broadcastReceiver);
    }
}
