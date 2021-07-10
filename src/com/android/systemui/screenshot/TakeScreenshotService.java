package com.android.systemui.screenshot;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.UserManager;
import android.util.Log;
import com.android.internal.logging.UiEventLogger;
import com.android.internal.util.ScreenshotHelper;
import com.android.systemui.screenshot.TakeScreenshotService;
import java.util.function.Consumer;
public class TakeScreenshotService extends Service {
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() { // from class: com.android.systemui.screenshot.TakeScreenshotService.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(intent.getAction()) && TakeScreenshotService.this.mScreenshot != null) {
                TakeScreenshotService.this.mScreenshot.dismissScreenshot("close system dialogs", true);
            }
        }
    };
    private Handler mHandler = new Handler(Looper.myLooper()) { // from class: com.android.systemui.screenshot.TakeScreenshotService.2
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            Messenger messenger = message.replyTo;
            $$Lambda$TakeScreenshotService$2$lw7umVWkOvN_7ORxe6g8U2JVo4M r1 = new Consumer(messenger) { // from class: com.android.systemui.screenshot.-$$Lambda$TakeScreenshotService$2$lw7umVWkOvN_7ORxe6g8U2JVo4M
                public final /* synthetic */ Messenger f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    TakeScreenshotService.AnonymousClass2.lambda$handleMessage$0(this.f$0, (Uri) obj);
                }
            };
            $$Lambda$TakeScreenshotService$2$gR0841riU57UiM3DXnP9uiMvS7k r2 = new Runnable(messenger) { // from class: com.android.systemui.screenshot.-$$Lambda$TakeScreenshotService$2$gR0841riU57UiM3DXnP9uiMvS7k
                public final /* synthetic */ Messenger f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    TakeScreenshotService.AnonymousClass2.lambda$handleMessage$1(this.f$0);
                }
            };
            if (!TakeScreenshotService.this.mUserManager.isUserUnlocked()) {
                Log.w("TakeScreenshotService", "Skipping screenshot because storage is locked!");
                post(new Runnable(r1) { // from class: com.android.systemui.screenshot.-$$Lambda$TakeScreenshotService$2$amml4XPd92h7sVnqzN-RFeKOz-s
                    public final /* synthetic */ Consumer f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.accept(null);
                    }
                });
                post(r2);
                return;
            }
            TakeScreenshotService.this.mUiEventLogger.log(ScreenshotEvent.getScreenshotSource(((ScreenshotHelper.ScreenshotRequest) message.obj).getSource()));
        }

        static /* synthetic */ void lambda$handleMessage$0(Messenger messenger, Uri uri) {
            try {
                messenger.send(Message.obtain(null, 1, uri));
            } catch (RemoteException unused) {
            }
        }

        static /* synthetic */ void lambda$handleMessage$1(Messenger messenger) {
            try {
                messenger.send(Message.obtain((Handler) null, 2));
            } catch (RemoteException unused) {
            }
        }
    };
    private final GlobalScreenshot mScreenshot;
    private final UiEventLogger mUiEventLogger;
    private final UserManager mUserManager;

    public TakeScreenshotService(GlobalScreenshot globalScreenshot, UserManager userManager, UiEventLogger uiEventLogger) {
        this.mScreenshot = globalScreenshot;
        this.mUserManager = userManager;
        this.mUiEventLogger = uiEventLogger;
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        registerReceiver(this.mBroadcastReceiver, new IntentFilter("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
        return new Messenger(this.mHandler).getBinder();
    }

    @Override // android.app.Service
    public boolean onUnbind(Intent intent) {
        GlobalScreenshot globalScreenshot = this.mScreenshot;
        if (globalScreenshot != null) {
            globalScreenshot.stopScreenshot();
        }
        unregisterReceiver(this.mBroadcastReceiver);
        return true;
    }
}
