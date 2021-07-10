package com.google.android.libraries.assistant.oemsmartspace.lib;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.LocaleList;
import android.os.Process;
import android.os.UserHandle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.libraries.assistant.oemsmartspace.shared.SmartspaceUpdateListener;
import com.google.geo.sidekick.SmartspaceProto$SmartspaceUpdate;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
public class SmartspaceContainerController {
    private static final String[] supportLocaleList = {"af", "ar", "as", "az", "be", "bg", "bn", "bs", "ca", "cs", "da", "de", "el", "en", "es", "et", "eu", "fa", "fi", "fr", "gl", "gu", "hi", "hr", "hu", "hy", "is", "it", "iw", "ja", "ka", "kk", "km", "kn", "ko", "ky", "lo", "lt", "lv", "mk", "ml", "mn", "mr", "ms", "my", "ne", "nl", "pa", "pl", "pt", "ro", "ru", "si", "sk", "sl", "sq", "sr", "sv", "sw", "ta", "te", "th", "tr", "uk", "ur", "uz", "vi", "zh", "zu", "fil", "id", "no", "yue"};
    private final AlarmManager alarmManager;
    private boolean alarmRegistered;
    private final Handler backgroundHandler;
    private ViewGroup containerView;
    private final Context context;
    private UserHandle currentUserHandle;
    private String customizeResourcePackage = "";
    private boolean enableDate = true;
    private final AlarmManager.OnAlarmListener expireAlarmAction;
    private Bundle extraInfo;
    private volatile boolean hasContents = false;
    private boolean hideSensitiveData;
    private boolean isSupportedLocale;
    private int jarLibVersion;
    private final MyOnAttachStateChangeListener onAttachStateChangeListener;
    private List receiverList;
    private final SmartspaceData smartspaceData;
    private String smartspaceType;
    private SmartspaceView smartspaceView;
    private final ProtoStore store;
    private final Handler uiHandler;

    /* access modifiers changed from: package-private */
    public class DeviceStateChangeReceiver extends BroadcastReceiver {
        private DeviceStateChangeReceiver() {
        }

        /* access modifiers changed from: package-private */
        public final /* synthetic */ void lambda$onReceive$0$SmartspaceContainerController$DeviceStateChangeReceiver() {
            if (SmartspaceContainerController.this.smartspaceView != null) {
                SmartspaceContainerController.this.smartspaceView.setLanguageSupported(SmartspaceContainerController.this.isSupportedLocale);
            }
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            action.getClass();
            int hashCode = action.hashCode();
            if (hashCode != -19011148) {
                if (hashCode == 959232034 && action.equals("android.intent.action.USER_SWITCHED")) {
                    SmartspaceContainerController.this.currentUserHandle = Process.myUserHandle();
                    SmartspaceContainerController.this.smartspaceData.currentCard = null;
                    SmartspaceContainerController.this.onExpire(true);
                }
            } else if (action.equals("android.intent.action.LOCALE_CHANGED") && SmartspaceContainerController.this.isSupportedLocale != SmartspaceContainerController.isLanguageSupported()) {
                SmartspaceContainerController.this.isSupportedLocale = SmartspaceContainerController.isLanguageSupported();
                SmartspaceContainerController.this.uiHandler.post(new SmartspaceContainerController$DeviceStateChangeReceiver$$Lambda$0(this));
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class GeneralBroadcastReceiver extends BroadcastReceiver {
        private GeneralBroadcastReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            action.getClass();
            int hashCode = action.hashCode();
            if (hashCode != -1513032534) {
                if (hashCode != 502473491) {
                    if (hashCode != 505380757 || !action.equals("android.intent.action.TIME_SET")) {
                        return;
                    }
                } else if (!action.equals("android.intent.action.TIMEZONE_CHANGED")) {
                    return;
                }
            } else if (!action.equals("android.intent.action.TIME_TICK")) {
                return;
            }
            SmartspaceContainerController.this.onTimeChange();
        }
    }

    /* access modifiers changed from: package-private */
    public class MyOnAttachStateChangeListener implements View.OnAttachStateChangeListener {
        private MyOnAttachStateChangeListener() {
        }

        @Override // android.view.View.OnAttachStateChangeListener
        public void onViewAttachedToWindow(View view) {
            Log.d("SmartSpaceViewCtrl", "view attached, trying to bind");
            SmartspaceContainerController.this.viewAttached();
        }

        @Override // android.view.View.OnAttachStateChangeListener
        public void onViewDetachedFromWindow(View view) {
            Log.d("SmartSpaceViewCtrl", "view detached, cleaning up");
            SmartspaceContainerController.this.cleanup();
        }
    }

    /* access modifiers changed from: package-private */
    public class SmartSpaceBroadcastReceiver extends BroadcastReceiver {
        private SmartSpaceBroadcastReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String valueOf = String.valueOf(intent.getExtras());
            StringBuilder sb = new StringBuilder(String.valueOf(valueOf).length() + 22);
            sb.append("receiving updateView: ");
            sb.append(valueOf);
            Log.d("SmartspaceReceiver", sb.toString());
            byte[] byteArrayExtra = intent.getByteArrayExtra("com.google.android.apps.nexuslauncher.extra.SMARTSPACE_CARD");
            if (byteArrayExtra != null) {
                SmartspaceContainerController.this.onReceivedCard(context, intent, byteArrayExtra);
            }
            SmartspaceContainerController.this.onReceiveChip(context, intent);
            SmartspaceContainerController.this.uiHandler.post(new SmartspaceContainerController$SmartSpaceBroadcastReceiver$$Lambda$0(SmartspaceContainerController.this));
            if (!SmartspaceContainerController.this.hasContents) {
                SmartspaceContainerController.this.hasContents = true;
            }
        }
    }

    SmartspaceContainerController(Context context, SmartspaceData smartspaceData, Handler handler, Handler handler2, ProtoStore protoStore, AlarmManager alarmManager, List list) {
        Log.d("SmartSpaceViewCtrl", "start client smart space content viewer");
        this.context = context;
        this.smartspaceData = smartspaceData;
        this.uiHandler = handler;
        this.backgroundHandler = handler2;
        this.store = protoStore;
        this.alarmManager = alarmManager;
        this.receiverList = list;
        this.expireAlarmAction = new AlarmManager.OnAlarmListener(this, smartspaceData) { // from class: com.google.android.libraries.assistant.oemsmartspace.lib.SmartspaceContainerController$$Lambda$0
            private final SmartspaceContainerController arg$1;
            private final SmartspaceData arg$2;

            {
                this.arg$1 = r1;
                this.arg$2 = r2;
            }

            @Override // android.app.AlarmManager.OnAlarmListener
            public void onAlarm() {
                this.arg$1.lambda$new$0$SmartspaceContainerController(this.arg$2);
            }
        };
        this.onAttachStateChangeListener = new MyOnAttachStateChangeListener();
    }

    /* access modifiers changed from: private */
    /* renamed from: checkAndUpdateSmartspaceview */
    public void bridge$lambda$2$SmartspaceContainerController() {
        View updateView = bridge$lambda$1$SmartspaceContainerController();
        if (updateView != null) {
            this.containerView.removeAllViews();
            this.containerView.addView(updateView);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void cleanup() {
        Log.d("SmartSpaceViewCtrl", "clean up the listeners and remove the views");
        for (BroadcastReceiver broadcastReceiver : this.receiverList) {
            try {
                this.context.unregisterReceiver(broadcastReceiver);
            } catch (Throwable th) {
                String valueOf = String.valueOf(th);
                StringBuilder sb = new StringBuilder(String.valueOf(valueOf).length() + 40);
                sb.append("error thrown when unregistering receiver");
                sb.append(valueOf);
                Log.w("SmartSpaceViewCtrl", sb.toString());
            }
        }
        this.receiverList.clear();
        if (this.alarmRegistered) {
            this.alarmManager.cancel(this.expireAlarmAction);
            this.alarmRegistered = false;
        }
        this.hasContents = false;
    }

    public static SmartspaceContainerController create(Context context, Handler handler) {
        HandlerThread handlerThread = new HandlerThread("smartspace-background");
        handlerThread.start();
        ArrayList arrayList = new ArrayList();
        return new SmartspaceContainerController(context, new SmartspaceData(), handler, new Handler(handlerThread.getLooper()), new ProtoStore(context), (AlarmManager) context.getSystemService("alarm"), arrayList);
    }

    private static Bitmap createIconBitmap(Intent.ShortcutIconResource shortcutIconResource, Context context) {
        Resources resourcesForApplication = context.getPackageManager().getResourcesForApplication(shortcutIconResource.packageName);
        return BitmapFactory.decodeResource(resourcesForApplication, resourcesForApplication.getIdentifier(shortcutIconResource.resourceName, null, null));
    }

    protected static boolean isLanguageSupported() {
        LocaleList localeList = LocaleList.getDefault();
        if (localeList.size() == 0) {
            return true;
        }
        for (int i = 0; i < localeList.size(); i++) {
            Locale locale = localeList.get(i);
            for (String str : supportLocaleList) {
                if (locale.getLanguage().equals(new Locale(str).getLanguage())) {
                    return true;
                }
            }
            String language = locale.getLanguage();
            String country = locale.getCountry();
            StringBuilder sb = new StringBuilder(String.valueOf(language).length() + 24 + String.valueOf(country).length());
            sb.append("Locale ");
            sb.append(language);
            sb.append("_");
            sb.append(country);
            sb.append("is not supported");
            Log.w("SmartSpaceViewCtrl", sb.toString());
        }
        Log.w("SmartSpaceViewCtrl", "All the current Locale % is not supported");
        return false;
    }

    private SmartspaceCard loadSmartSpaceData(boolean z) {
        int number = (z ? SmartspaceProto$SmartspaceUpdate.SmartspaceCard.CardPriority.PRIMARY : SmartspaceProto$SmartspaceUpdate.SmartspaceCard.CardPriority.SECONDARY).getNumber();
        ProtoStore protoStore = this.store;
        String userHandle = this.currentUserHandle.toString();
        StringBuilder sb = new StringBuilder(String.valueOf(userHandle).length() + 23);
        sb.append("smartspace_");
        sb.append(userHandle);
        sb.append("_");
        sb.append(number);
        return protoStore.load(sb.toString(), z);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* renamed from: onDataUpdated */
    public void bridge$lambda$0$SmartspaceContainerController() {
        if (this.alarmRegistered) {
            this.alarmManager.cancel(this.expireAlarmAction);
            this.alarmRegistered = false;
        }
        long expiresAtMillis = this.smartspaceData.getExpiresAtMillis();
        if (expiresAtMillis > 0) {
            this.alarmManager.set(0, expiresAtMillis, "SmartSpace", this.expireAlarmAction, this.uiHandler);
            this.alarmRegistered = true;
        }
        if (this.smartspaceView != null) {
            bridge$lambda$2$SmartspaceContainerController();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onExpire(boolean z) {
        this.alarmRegistered = false;
        if (z) {
            Log.d("SmartSpaceViewCtrl", "onExpire - sent");
            this.context.sendBroadcast(new Intent("com.google.android.systemui.smartspace.EXPIRE_EVENT").setPackage("com.google.android.googlequicksearchbox").addFlags(268435456));
        }
        this.uiHandler.post(new Runnable(this) { // from class: com.google.android.libraries.assistant.oemsmartspace.lib.SmartspaceContainerController$$Lambda$4
            private final SmartspaceContainerController arg$1;

            {
                this.arg$1 = r1;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.arg$1.bridge$lambda$0$SmartspaceContainerController();
            }
        });
    }

    private void onNewCard(SmartspaceProto$SmartspaceUpdate.SmartspaceCard smartspaceCard, Bitmap bitmap) {
        if (smartspaceCard == null) {
            Log.d("SmartSpaceViewCtrl", "Smartspace card received is null.");
        } else if (smartspaceCard.hasShouldDiscard() && smartspaceCard.getShouldDiscard()) {
            Log.d("SmartSpaceViewCtrl", "updateView new card");
            if (smartspaceCard.getCardType() != SmartspaceProto$SmartspaceUpdate.SmartspaceCard.CardType.WEATHER) {
                this.smartspaceData.currentCard = null;
            } else {
                this.smartspaceData.weatherCard = null;
            }
            ProtoStore protoStore = this.store;
            String valueOf = String.valueOf(this.currentUserHandle);
            int number = smartspaceCard.getCardPriority().getNumber();
            StringBuilder sb = new StringBuilder(String.valueOf(valueOf).length() + 23);
            sb.append("smartspace_");
            sb.append(valueOf);
            sb.append("_");
            sb.append(number);
            protoStore.store(null, sb.toString());
        } else if (smartspaceCard.getCardType() == SmartspaceProto$SmartspaceUpdate.SmartspaceCard.CardType.WEATHER) {
            this.smartspaceData.weatherCard = new SmartspaceCard(this.context, smartspaceCard, true, bitmap);
            ProtoStore protoStore2 = this.store;
            SmartspaceCard smartspaceCard2 = this.smartspaceData.weatherCard;
            String valueOf2 = String.valueOf(this.currentUserHandle);
            int number2 = smartspaceCard.getCardPriority().getNumber();
            StringBuilder sb2 = new StringBuilder(String.valueOf(valueOf2).length() + 23);
            sb2.append("smartspace_");
            sb2.append(valueOf2);
            sb2.append("_");
            sb2.append(number2);
            protoStore2.store(smartspaceCard2, sb2.toString());
        } else if (!this.hideSensitiveData) {
            this.smartspaceData.currentCard = new SmartspaceCard(this.context, smartspaceCard, false, bitmap);
            ProtoStore protoStore3 = this.store;
            SmartspaceCard smartspaceCard3 = this.smartspaceData.currentCard;
            String valueOf3 = String.valueOf(this.currentUserHandle);
            int number3 = smartspaceCard.getCardPriority().getNumber();
            StringBuilder sb3 = new StringBuilder(String.valueOf(valueOf3).length() + 23);
            sb3.append("smartspace_");
            sb3.append(valueOf3);
            sb3.append("_");
            sb3.append(number3);
            protoStore3.store(smartspaceCard3, sb3.toString());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onReceiveChip(Context context, Intent intent) {
        this.backgroundHandler.post(new Runnable(this, context, intent.getBundleExtra("com.google.android.apps.nexuslauncher.extra.FIRST_SMARTSPACE_CHIP"), intent.getBundleExtra("com.google.android.apps.nexuslauncher.extra.SECOND_SMARTSPACE_CHIP")) { // from class: com.google.android.libraries.assistant.oemsmartspace.lib.SmartspaceContainerController$$Lambda$6
            private final SmartspaceContainerController arg$1;
            private final Context arg$2;
            private final Bundle arg$3;
            private final Bundle arg$4;

            {
                this.arg$1 = r1;
                this.arg$2 = r2;
                this.arg$3 = r3;
                this.arg$4 = r4;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.arg$1.lambda$onReceiveChip$5$SmartspaceContainerController(this.arg$2, this.arg$3, this.arg$4);
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onReceivedCard(Context context, Intent intent, byte[] bArr) {
        SmartspaceProto$SmartspaceUpdate.Builder newBuilder = SmartspaceProto$SmartspaceUpdate.newBuilder();
        try {
            newBuilder.mergeFrom(bArr);
            for (SmartspaceProto$SmartspaceUpdate.SmartspaceCard smartspaceCard : newBuilder.build().getCardList()) {
                boolean z = true;
                boolean z2 = smartspaceCard.getCardPriority() == SmartspaceProto$SmartspaceUpdate.SmartspaceCard.CardPriority.PRIMARY;
                if (smartspaceCard.getCardPriority() != SmartspaceProto$SmartspaceUpdate.SmartspaceCard.CardPriority.SECONDARY) {
                    z = false;
                }
                if (!z2) {
                    if (!z) {
                        Log.w("SmartSpaceViewCtrl", "unrecognized card priority");
                    }
                }
                this.backgroundHandler.post(new Runnable(this, context, smartspaceCard, intent) { // from class: com.google.android.libraries.assistant.oemsmartspace.lib.SmartspaceContainerController$$Lambda$7
                    private final SmartspaceContainerController arg$1;
                    private final Context arg$2;
                    private final SmartspaceProto$SmartspaceUpdate.SmartspaceCard arg$3;
                    private final Intent arg$4;

                    {
                        this.arg$1 = r1;
                        this.arg$2 = r2;
                        this.arg$3 = r3;
                        this.arg$4 = r4;
                    }

                    @Override // java.lang.Runnable
                    public void run() {
                        this.arg$1.lambda$onReceivedCard$6$SmartspaceContainerController(this.arg$2, this.arg$3, this.arg$4);
                    }
                });
            }
        } catch (InvalidProtocolBufferException e) {
            Log.e("SmartSpaceViewCtrl", "proto", e);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onTimeChange() {
        SmartspaceData smartspaceData = this.smartspaceData;
        if (smartspaceData != null && smartspaceData.hasCurrent() && this.smartspaceData.getExpirationRemainingMillis() > 0) {
            this.uiHandler.post(new Runnable(this) { // from class: com.google.android.libraries.assistant.oemsmartspace.lib.SmartspaceContainerController$$Lambda$5
                private final SmartspaceContainerController arg$1;

                {
                    this.arg$1 = r1;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.arg$1.bridge$lambda$1$SmartspaceContainerController();
                }
            });
        }
    }

    private void processExtraInformation(Bundle bundle) {
        this.jarLibVersion = bundle.getInt("com.google.android.apps.oemsmartspace.JAR_LIB_VERSION_KEY");
        this.smartspaceType = bundle.getString("com.google.android.apps.oemsmartspace.SMARTSPACE_TYPE_KEY");
        if (bundle.containsKey("com.google.android.apps.oemsmartspace.SMARTSPACE_ENABLE_DATE_KEY")) {
            this.enableDate = bundle.getBoolean("com.google.android.apps.oemsmartspace.SMARTSPACE_ENABLE_DATE_KEY");
        }
        String valueOf = String.valueOf(this.smartspaceType);
        Log.d("SmartSpaceViewCtrl", valueOf.length() != 0 ? "Setup client library, type: ".concat(valueOf) : new String("Setup client library, type: "));
        this.uiHandler.post(new Runnable(this, bundle) { // from class: com.google.android.libraries.assistant.oemsmartspace.lib.SmartspaceContainerController$$Lambda$2
            private final SmartspaceContainerController arg$1;
            private final Bundle arg$2;

            {
                this.arg$1 = r1;
                this.arg$2 = r2;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.arg$1.lambda$processExtraInformation$2$SmartspaceContainerController(this.arg$2);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: processTextStyle */
    public void lambda$processExtraInformation$2$SmartspaceContainerController(Bundle bundle) {
        if (this.smartspaceView == null) {
            Log.w("SmartSpaceViewCtrl", "SmartspaceView is not initialized yet, call setView first.");
            return;
        }
        if (bundle.containsKey("com.google.android.apps.oemsmartspace.SMARTSPACE_ENABLE_DATE_KEY")) {
            this.smartspaceView.enableDate(bundle.getBoolean("com.google.android.apps.oemsmartspace.SMARTSPACE_ENABLE_DATE_KEY"));
        }
        if (bundle.containsKey("com.google.android.apps.oemsmartspace.SMARTSPACE_ENABLE_WEATHER_KEY")) {
            this.smartspaceView.enableWeather(bundle.getBoolean("com.google.android.apps.oemsmartspace.SMARTSPACE_ENABLE_WEATHER_KEY"));
        }
        if (bundle.containsKey("com.google.android.apps.oemsmartspace.TEXT_COLOR_KEY")) {
            this.smartspaceView.setColor(bundle.getInt("com.google.android.apps.oemsmartspace.TEXT_COLOR_KEY"));
        }
        if (bundle.containsKey("com.google.android.apps.oemsmartspace.TEXT_FONT_KEY")) {
            this.smartspaceView.setFont(bundle.getString("com.google.android.apps.oemsmartspace.TEXT_FONT_KEY"));
        }
        if (bundle.containsKey("com.google.android.apps.oemsmartspace.SMARTSPACE_RESOURCE_PACKAGE")) {
            this.customizeResourcePackage = bundle.getString("com.google.android.apps.oemsmartspace.SMARTSPACE_RESOURCE_PACKAGE");
            this.smartspaceView.setResource(bundle.getString("com.google.android.apps.oemsmartspace.SMARTSPACE_RESOURCE_PACKAGE"));
        }
        if (bundle.containsKey("com.google.android.apps.oemsmartspace.SMARTSPACE_SET_LEFT_ALIGNED_KEY")) {
            this.smartspaceView.setLeftAligned(bundle.getBoolean("com.google.android.apps.oemsmartspace.SMARTSPACE_SET_LEFT_ALIGNED_KEY"));
        }
        if (bundle.containsKey("com.google.android.apps.oemsmartspace.SMARTSPACE_TEXT_SIZE_FACTOR_KEY")) {
            this.smartspaceView.setTextSizeFactor(bundle.getFloat("com.google.android.apps.oemsmartspace.SMARTSPACE_TEXT_SIZE_FACTOR_KEY"));
        }
        this.smartspaceView.onSmartspaceUpdated(this.smartspaceData, true);
    }

    private void registerCardExpireListeners() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.USER_SWITCHED");
        intentFilter.addAction("android.intent.action.LOCALE_CHANGED");
        DeviceStateChangeReceiver deviceStateChangeReceiver = new DeviceStateChangeReceiver();
        this.context.registerReceiver(deviceStateChangeReceiver, intentFilter);
        this.receiverList.add(deviceStateChangeReceiver);
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("android.intent.action.PACKAGE_ADDED");
        intentFilter2.addAction("android.intent.action.PACKAGE_CHANGED");
        intentFilter2.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter2.addAction("android.intent.action.PACKAGE_DATA_CLEARED");
        intentFilter2.addDataScheme("package");
        intentFilter2.addDataSchemeSpecificPart("com.google.android.googlequicksearchbox", 0);
        AnonymousClass1 r1 = new BroadcastReceiver() { // from class: com.google.android.libraries.assistant.oemsmartspace.lib.SmartspaceContainerController.1
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                SmartspaceContainerController.this.requestUiIfContentsEmpty(true);
            }
        };
        this.context.registerReceiver(r1, intentFilter2);
        this.receiverList.add(r1);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void requestUiIfContentsEmpty(boolean z) {
        if (!this.hasContents || z) {
            PendingIntent activity = PendingIntent.getActivity(this.context, 0, new Intent(), 201326592);
            Intent putExtra = new Intent("com.google.android.apps.oemsmartspace.ENABLE_UPDATE").setPackage("com.google.android.googlequicksearchbox").addFlags(268435456).putExtra("com.google.android.apps.oemsmartspace.SMARTSPACE_TYPE_KEY", this.smartspaceType).putExtra("com.google.android.apps.oemsmartspace.extra.VERIFICATION_KEY", activity).putExtra("com.google.android.apps.oemsmartspace.JAR_LIB_VERSION_KEY", this.jarLibVersion).putExtra("com.google.android.apps.oemsmartspace.SMARTSPACE_ENABLE_DATE_KEY", this.enableDate).putExtra("com.google.android.apps.oemsmartspace.HIDE_PERSONAL_NOTIFICATION", this.hideSensitiveData);
            if (!this.customizeResourcePackage.isEmpty()) {
                putExtra.putExtra("com.google.android.apps.oemsmartspace.SMARTSPACE_RESOURCE_PACKAGE", this.customizeResourcePackage);
            }
            String str = this.smartspaceType;
            String creatorPackage = activity.getCreatorPackage();
            StringBuilder sb = new StringBuilder(String.valueOf(str).length() + 69 + String.valueOf(creatorPackage).length());
            sb.append("Register package for smartspace update\nsmartspaceType: ");
            sb.append(str);
            sb.append("\npackage name:");
            sb.append(creatorPackage);
            Log.d("SmartSpaceViewCtrl", sb.toString());
            this.context.sendBroadcast(putExtra);
            restoreData();
            return;
        }
        Log.d("SmartSpaceViewCtrl", "already has contents, no need to request again");
    }

    private void restoreData() {
        this.backgroundHandler.post(new Runnable(this) { // from class: com.google.android.libraries.assistant.oemsmartspace.lib.SmartspaceContainerController$$Lambda$3
            private final SmartspaceContainerController arg$1;

            {
                this.arg$1 = r1;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.arg$1.lambda$restoreData$3$SmartspaceContainerController();
            }
        });
    }

    private static Drawable retrieveChipIcon(Context context, Bundle bundle) {
        String string;
        if (bundle == null || (string = bundle.getString("com.google.android.apps.nexuslauncher.extra.SMARTSPACE_CHIP_ICON_URI_EXTRA")) == null) {
            return null;
        }
        try {
            return ImageDecoder.decodeDrawable(ImageDecoder.createSource(context.getContentResolver(), Uri.parse(string)));
        } catch (Exception e) {
            Log.e("SmartSpaceViewCtrl", "Could not retrieve image", e);
            return null;
        }
    }

    private static Bitmap retrieveIcon(Context context, SmartspaceProto$SmartspaceUpdate.SmartspaceCard smartspaceCard, Intent intent) {
        Bitmap bitmap;
        if (intent != null && (bitmap = (Bitmap) intent.getParcelableExtra("com.google.android.apps.nexuslauncher.extra.SMARTSPACE_ICON")) != null) {
            return bitmap;
        }
        SmartspaceProto$SmartspaceUpdate.SmartspaceCard.Image icon = smartspaceCard.getIcon();
        try {
            if (!icon.getUri().isEmpty()) {
                return MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(icon.getUri()));
            }
            if (icon.getGsaResourceName().isEmpty()) {
                return null;
            }
            Intent.ShortcutIconResource shortcutIconResource = new Intent.ShortcutIconResource();
            shortcutIconResource.packageName = "com.google.android.googlequicksearchbox";
            shortcutIconResource.resourceName = icon.getGsaResourceName();
            return createIconBitmap(shortcutIconResource, context);
        } catch (Exception e) {
            String uri = icon.getUri();
            String gsaResourceName = icon.getGsaResourceName();
            StringBuilder sb = new StringBuilder(String.valueOf(uri).length() + 37 + String.valueOf(gsaResourceName).length());
            sb.append("retrieving bitmap failed uri=");
            sb.append(uri);
            sb.append(" gsaRes=");
            sb.append(gsaResourceName);
            Log.e("SmartSpaceViewCtrl", sb.toString(), e);
            return null;
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: updateChips */
    public void lambda$onReceiveChip$4$SmartspaceContainerController(Bundle bundle, Bundle bundle2, Drawable drawable, Drawable drawable2) {
        updateFirstChip(bundle, drawable);
        updateSecondChip(bundle2, drawable2);
    }

    private void updateFirstChip(Bundle bundle, Drawable drawable) {
        if (bundle == null || !SmartspaceChip.intentHasChip(bundle)) {
            this.smartspaceData.firstChip = null;
            return;
        }
        Log.d("SmartSpaceViewCtrl", "Updating first chip");
        this.smartspaceData.firstChip = new SmartspaceChip(bundle, drawable);
    }

    private void updateSecondChip(Bundle bundle, Drawable drawable) {
        if (bundle == null || !SmartspaceChip.intentHasChip(bundle)) {
            this.smartspaceData.secondChip = null;
            return;
        }
        Log.d("SmartSpaceViewCtrl", "Updating second chip");
        this.smartspaceData.secondChip = new SmartspaceChip(bundle, drawable);
    }

    /* access modifiers changed from: private */
    /* renamed from: updateView */
    public View bridge$lambda$1$SmartspaceContainerController() {
        try {
            SmartspaceView smartspaceView = this.smartspaceView;
            smartspaceView.getClass();
            return smartspaceView.onSmartspaceUpdated(this.smartspaceData, true);
        } catch (NullPointerException e) {
            String valueOf = String.valueOf(e);
            StringBuilder sb = new StringBuilder(String.valueOf(valueOf).length() + 52);
            sb.append("Cannot updateView because the SmartspaceView is null");
            sb.append(valueOf);
            Log.w("SmartSpaceViewCtrl", sb.toString());
            return null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void viewAttached() {
        if (this.hasContents) {
            Log.d("SmartSpaceViewCtrl", "register setup complete. Skip to avoid duplicate");
            return;
        }
        if (!this.receiverList.isEmpty()) {
            Log.d("SmartSpaceViewCtrl", "receiverList is not empty, clean up first to avoid duplicate");
            cleanup();
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.TIME_TICK");
        intentFilter.addAction("android.intent.action.TIME_SET");
        intentFilter.addAction("android.intent.action.TIMEZONE_CHANGED");
        GeneralBroadcastReceiver generalBroadcastReceiver = new GeneralBroadcastReceiver();
        this.context.registerReceiver(generalBroadcastReceiver, intentFilter);
        this.receiverList.add(generalBroadcastReceiver);
        registerCardExpireListeners();
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("com.google.android.apps.nexuslauncher.UPDATE_SMARTSPACE");
        SmartSpaceBroadcastReceiver smartSpaceBroadcastReceiver = new SmartSpaceBroadcastReceiver();
        this.context.registerReceiver(smartSpaceBroadcastReceiver, intentFilter2, "android.permission.CAPTURE_AUDIO_HOTWORD", this.uiHandler);
        this.receiverList.add(smartSpaceBroadcastReceiver);
        refresh();
    }

    /* access modifiers changed from: package-private */
    public final /* synthetic */ void lambda$new$0$SmartspaceContainerController(SmartspaceData smartspaceData) {
        smartspaceData.handleExpire();
        onExpire(false);
    }

    /* access modifiers changed from: package-private */
    public final /* synthetic */ void lambda$onReceiveChip$5$SmartspaceContainerController(Context context, Bundle bundle, Bundle bundle2) {
        this.uiHandler.post(new Runnable(this, bundle, bundle2, retrieveChipIcon(context, bundle), retrieveChipIcon(context, bundle2)) { // from class: com.google.android.libraries.assistant.oemsmartspace.lib.SmartspaceContainerController$$Lambda$8
            private final SmartspaceContainerController arg$1;
            private final Bundle arg$2;
            private final Bundle arg$3;
            private final Drawable arg$4;
            private final Drawable arg$5;

            {
                this.arg$1 = r1;
                this.arg$2 = r2;
                this.arg$3 = r3;
                this.arg$4 = r4;
                this.arg$5 = r5;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.arg$1.lambda$onReceiveChip$4$SmartspaceContainerController(this.arg$2, this.arg$3, this.arg$4, this.arg$5);
            }
        });
    }

    /* access modifiers changed from: package-private */
    public final /* synthetic */ void lambda$onReceivedCard$6$SmartspaceContainerController(Context context, SmartspaceProto$SmartspaceUpdate.SmartspaceCard smartspaceCard, Intent intent) {
        onNewCard(smartspaceCard, retrieveIcon(context, smartspaceCard, intent));
    }

    /* access modifiers changed from: package-private */
    public final /* synthetic */ void lambda$refresh$1$SmartspaceContainerController() {
        processExtraInformation(this.extraInfo);
        requestUiIfContentsEmpty(false);
    }

    /* access modifiers changed from: package-private */
    public final /* synthetic */ void lambda$restoreData$3$SmartspaceContainerController() {
        this.smartspaceData.weatherCard = loadSmartSpaceData(false);
        if (this.smartspaceView != null) {
            this.uiHandler.post(new Runnable(this) { // from class: com.google.android.libraries.assistant.oemsmartspace.lib.SmartspaceContainerController$$Lambda$9
                private final SmartspaceContainerController arg$1;

                {
                    this.arg$1 = r1;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.arg$1.bridge$lambda$2$SmartspaceContainerController();
                }
            });
        }
    }

    public void refresh() {
        if (this.containerView == null || this.smartspaceView == null) {
            Log.w("SmartSpaceViewCtrl", "containerView is null, please setView first!");
        } else {
            this.backgroundHandler.post(new Runnable(this) { // from class: com.google.android.libraries.assistant.oemsmartspace.lib.SmartspaceContainerController$$Lambda$1
                private final SmartspaceContainerController arg$1;

                {
                    this.arg$1 = r1;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.arg$1.lambda$refresh$1$SmartspaceContainerController();
                }
            });
        }
    }

    public void setHideSensitiveData(boolean z) {
        if (z != this.hideSensitiveData) {
            this.hideSensitiveData = z;
            this.context.sendBroadcast(new Intent("com.google.android.apps.oemsmartspace.ENABLE_UPDATE").setPackage("com.google.android.googlequicksearchbox").addFlags(268435456).putExtra("com.google.android.apps.oemsmartspace.extra.VERIFICATION_KEY", PendingIntent.getActivity(this.context, 0, new Intent(), 201326592)).putExtra("com.google.android.apps.oemsmartspace.JAR_LIB_VERSION_KEY", this.jarLibVersion).putExtra("com.google.android.apps.oemsmartspace.HIDE_PERSONAL_NOTIFICATION", z));
            if (z) {
                if (this.smartspaceData.hasCurrent()) {
                    this.smartspaceData.currentCard = null;
                    ProtoStore protoStore = this.store;
                    String valueOf = String.valueOf(this.currentUserHandle);
                    int number = SmartspaceProto$SmartspaceUpdate.SmartspaceCard.CardPriority.PRIMARY.getNumber();
                    StringBuilder sb = new StringBuilder(String.valueOf(valueOf).length() + 23);
                    sb.append("smartspace_");
                    sb.append(valueOf);
                    sb.append("_");
                    sb.append(number);
                    protoStore.store(null, sb.toString());
                    bridge$lambda$0$SmartspaceContainerController();
                }
            } else if (this.smartspaceData.hasCurrent()) {
                bridge$lambda$0$SmartspaceContainerController();
            } else {
                onExpire(true);
            }
        }
    }

    public void setStyle(Bundle bundle) {
        this.extraInfo.putAll(bundle);
        if (this.containerView.isAttachedToWindow()) {
            refresh();
        }
    }

    /* access modifiers changed from: protected */
    public void setView(ViewGroup viewGroup, Bundle bundle, SmartspaceView smartspaceView) {
        if (bundle == null) {
            bundle = new Bundle();
        }
        this.extraInfo = bundle;
        this.extraInfo.putInt("com.google.android.apps.oemsmartspace.JAR_LIB_VERSION_KEY", 1);
        this.isSupportedLocale = isLanguageSupported();
        this.currentUserHandle = Process.myUserHandle();
        this.smartspaceView = smartspaceView;
        smartspaceView.setLanguageSupported(this.isSupportedLocale);
        this.containerView = viewGroup;
        viewGroup.addOnAttachStateChangeListener(this.onAttachStateChangeListener);
        if (viewGroup.isAttachedToWindow()) {
            viewAttached();
        }
    }

    public void setView(ViewGroup viewGroup, Bundle bundle, SmartspaceUpdateListener smartspaceUpdateListener) {
        setView(viewGroup, bundle, new SmartspaceView(this.context, smartspaceUpdateListener));
    }

    public void unsetView() {
        if (this.containerView != null) {
            Log.d("SmartSpaceViewCtrl", "Clean up content views");
            this.containerView.removeAllViews();
            this.containerView.removeOnAttachStateChangeListener(this.onAttachStateChangeListener);
        }
        Log.d("SmartSpaceViewCtrl", "Clean up SmartspaceViews and unregister the listeners");
        cleanup();
        this.smartspaceView = null;
        this.smartspaceData.clear();
    }
}
