package com.android.systemui.bubbles;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.AsyncTask;
import android.util.Log;
import android.util.PathParser;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.android.internal.graphics.ColorUtils;
import com.android.launcher3.icons.BitmapInfo;
import com.android.systemui.C0011R$layout;
import com.android.systemui.bubbles.Bubble;
import java.lang.ref.WeakReference;
import java.util.Objects;
public class BubbleViewInfoTask extends AsyncTask<Void, Void, BubbleViewInfo> {
    private Bubble mBubble;
    private Callback mCallback;
    private WeakReference<Context> mContext;
    private BubbleIconFactory mIconFactory;
    private boolean mSkipInflation;
    private WeakReference<BubbleStackView> mStackView;

    public interface Callback {
        void onBubbleViewsReady(Bubble bubble);
    }

    BubbleViewInfoTask(Bubble bubble, Context context, BubbleStackView bubbleStackView, BubbleIconFactory bubbleIconFactory, boolean z, Callback callback) {
        this.mBubble = bubble;
        this.mContext = new WeakReference<>(context);
        this.mStackView = new WeakReference<>(bubbleStackView);
        this.mIconFactory = bubbleIconFactory;
        this.mSkipInflation = z;
        this.mCallback = callback;
    }

    /* access modifiers changed from: protected */
    public BubbleViewInfo doInBackground(Void... voidArr) {
        return BubbleViewInfo.populate(this.mContext.get(), this.mStackView.get(), this.mIconFactory, this.mBubble, this.mSkipInflation);
    }

    /* access modifiers changed from: protected */
    public void onPostExecute(BubbleViewInfo bubbleViewInfo) {
        if (bubbleViewInfo != null) {
            this.mBubble.setViewInfo(bubbleViewInfo);
            if (this.mCallback != null && !isCancelled()) {
                this.mCallback.onBubbleViewsReady(this.mBubble);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static class BubbleViewInfo {
        String appName;
        Drawable badgedAppIcon;
        Bitmap badgedBubbleImage;
        int dotColor;
        Path dotPath;
        BubbleExpandedView expandedView;
        Bubble.FlyoutMessage flyoutMessage;
        BadgedImageView imageView;
        ShortcutInfo shortcutInfo;

        BubbleViewInfo() {
        }

        static BubbleViewInfo populate(Context context, BubbleStackView bubbleStackView, BubbleIconFactory bubbleIconFactory, Bubble bubble, boolean z) {
            BubbleViewInfo bubbleViewInfo = new BubbleViewInfo();
            if (!z && !bubble.isInflated()) {
                LayoutInflater from = LayoutInflater.from(context);
                bubbleViewInfo.imageView = (BadgedImageView) from.inflate(C0011R$layout.bubble_view, (ViewGroup) bubbleStackView, false);
                BubbleExpandedView bubbleExpandedView = (BubbleExpandedView) from.inflate(C0011R$layout.bubble_expanded_view, (ViewGroup) bubbleStackView, false);
                bubbleViewInfo.expandedView = bubbleExpandedView;
                bubbleExpandedView.setStackView(bubbleStackView);
            }
            if (bubble.getShortcutInfo() != null) {
                bubbleViewInfo.shortcutInfo = bubble.getShortcutInfo();
            }
            PackageManager packageManager = context.getPackageManager();
            try {
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(bubble.getPackageName(), 795136);
                if (applicationInfo != null) {
                    bubbleViewInfo.appName = String.valueOf(packageManager.getApplicationLabel(applicationInfo));
                }
                Drawable applicationIcon = packageManager.getApplicationIcon(bubble.getPackageName());
                Drawable userBadgedIcon = packageManager.getUserBadgedIcon(applicationIcon, bubble.getUser());
                Drawable bubbleDrawable = bubbleIconFactory.getBubbleDrawable(context, bubbleViewInfo.shortcutInfo, bubble.getIcon());
                if (bubbleDrawable != null) {
                    applicationIcon = bubbleDrawable;
                }
                BitmapInfo badgeBitmap = bubbleIconFactory.getBadgeBitmap(userBadgedIcon, bubble.isImportantConversation());
                bubbleViewInfo.badgedAppIcon = userBadgedIcon;
                bubbleViewInfo.badgedBubbleImage = bubbleIconFactory.getBubbleBitmap(applicationIcon, badgeBitmap).icon;
                Path createPathFromPathData = PathParser.createPathFromPathData(context.getResources().getString(17039916));
                Matrix matrix = new Matrix();
                float scale = bubbleIconFactory.getNormalizer().getScale(applicationIcon, null, null, null);
                matrix.setScale(scale, scale, 50.0f, 50.0f);
                createPathFromPathData.transform(matrix);
                bubbleViewInfo.dotPath = createPathFromPathData;
                bubbleViewInfo.dotColor = ColorUtils.blendARGB(badgeBitmap.color, -1, 0.54f);
                Bubble.FlyoutMessage flyoutMessage = bubble.getFlyoutMessage();
                bubbleViewInfo.flyoutMessage = flyoutMessage;
                if (flyoutMessage != null) {
                    flyoutMessage.senderAvatar = BubbleViewInfoTask.loadSenderAvatar(context, flyoutMessage.senderIcon);
                }
                return bubbleViewInfo;
            } catch (PackageManager.NameNotFoundException unused) {
                Log.w("Bubbles", "Unable to find package: " + bubble.getPackageName());
                return null;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00ab, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00ac, code lost:
        r4.printStackTrace();
     */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00ab A[ExcHandler: ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException (r4v14 'e' java.lang.RuntimeException A[CUSTOM_DECLARE]), Splitter:B:2:0x0028] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static com.android.systemui.bubbles.Bubble.FlyoutMessage extractFlyoutMessage(com.android.systemui.statusbar.notification.collection.NotificationEntry r4) {
        /*
            java.util.Objects.requireNonNull(r4)
            android.service.notification.StatusBarNotification r4 = r4.getSbn()
            android.app.Notification r4 = r4.getNotification()
            java.lang.Class r0 = r4.getNotificationStyle()
            com.android.systemui.bubbles.Bubble$FlyoutMessage r1 = new com.android.systemui.bubbles.Bubble$FlyoutMessage
            r1.<init>()
            android.os.Bundle r2 = r4.extras
            java.lang.String r3 = "android.isGroupConversation"
            boolean r2 = r2.getBoolean(r3)
            r1.isGroupChat = r2
            java.lang.Class<android.app.Notification$BigTextStyle> r2 = android.app.Notification.BigTextStyle.class
            boolean r2 = r2.equals(r0)
            java.lang.String r3 = "android.text"
            if (r2 == 0) goto L_0x0040
            android.os.Bundle r0 = r4.extras     // Catch:{ ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab, ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab }
            java.lang.String r2 = "android.bigText"
            java.lang.CharSequence r0 = r0.getCharSequence(r2)     // Catch:{ ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab, ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab }
            boolean r2 = android.text.TextUtils.isEmpty(r0)     // Catch:{ ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab, ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab }
            if (r2 != 0) goto L_0x0037
            goto L_0x003d
        L_0x0037:
            android.os.Bundle r4 = r4.extras     // Catch:{ ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab, ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab }
            java.lang.CharSequence r0 = r4.getCharSequence(r3)     // Catch:{ ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab, ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab }
        L_0x003d:
            r1.message = r0     // Catch:{ ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab, ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab }
            return r1
        L_0x0040:
            java.lang.Class<android.app.Notification$MessagingStyle> r2 = android.app.Notification.MessagingStyle.class
            boolean r2 = r2.equals(r0)     // Catch:{ ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab, ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab }
            if (r2 == 0) goto L_0x007c
            android.os.Bundle r4 = r4.extras     // Catch:{ ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab, ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab }
            java.lang.String r0 = "android.messages"
            java.lang.Object r4 = r4.get(r0)     // Catch:{ ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab, ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab }
            android.os.Parcelable[] r4 = (android.os.Parcelable[]) r4     // Catch:{ ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab, ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab }
            java.util.List r4 = android.app.Notification.MessagingStyle.Message.getMessagesFromBundleArray(r4)     // Catch:{ ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab, ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab }
            android.app.Notification$MessagingStyle$Message r4 = android.app.Notification.MessagingStyle.findLatestIncomingMessage(r4)     // Catch:{ ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab, ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab }
            if (r4 == 0) goto L_0x00af
            java.lang.CharSequence r0 = r4.getText()     // Catch:{ ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab, ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab }
            r1.message = r0     // Catch:{ ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab, ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab }
            android.app.Person r4 = r4.getSenderPerson()     // Catch:{ ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab, ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab }
            r0 = 0
            if (r4 == 0) goto L_0x006e
            java.lang.CharSequence r2 = r4.getName()     // Catch:{ ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab, ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab }
            goto L_0x006f
        L_0x006e:
            r2 = r0
        L_0x006f:
            r1.senderName = r2     // Catch:{ ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab, ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab }
            r1.senderAvatar = r0     // Catch:{ ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab, ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab }
            if (r4 == 0) goto L_0x0079
            android.graphics.drawable.Icon r0 = r4.getIcon()     // Catch:{ ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab, ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab }
        L_0x0079:
            r1.senderIcon = r0     // Catch:{ ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab, ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab }
            return r1
        L_0x007c:
            java.lang.Class<android.app.Notification$InboxStyle> r2 = android.app.Notification.InboxStyle.class
            boolean r2 = r2.equals(r0)     // Catch:{ ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab, ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab }
            if (r2 == 0) goto L_0x0099
            android.os.Bundle r4 = r4.extras     // Catch:{ ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab, ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab }
            java.lang.String r0 = "android.textLines"
            java.lang.CharSequence[] r4 = r4.getCharSequenceArray(r0)     // Catch:{ ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab, ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab }
            if (r4 == 0) goto L_0x00af
            int r0 = r4.length     // Catch:{ ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab, ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab }
            if (r0 <= 0) goto L_0x00af
            int r0 = r4.length     // Catch:{ ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab, ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab }
            int r0 = r0 + -1
            r4 = r4[r0]     // Catch:{ ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab, ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab }
            r1.message = r4     // Catch:{ ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab, ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab }
            return r1
        L_0x0099:
            java.lang.Class<android.app.Notification$MediaStyle> r2 = android.app.Notification.MediaStyle.class
            boolean r0 = r2.equals(r0)     // Catch:{ ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab, ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab }
            if (r0 == 0) goto L_0x00a2
            return r1
        L_0x00a2:
            android.os.Bundle r4 = r4.extras     // Catch:{ ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab, ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab }
            java.lang.CharSequence r4 = r4.getCharSequence(r3)     // Catch:{ ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab, ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab }
            r1.message = r4     // Catch:{ ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab, ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException -> 0x00ab }
            return r1
        L_0x00ab:
            r4 = move-exception
            r4.printStackTrace()
        L_0x00af:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.bubbles.BubbleViewInfoTask.extractFlyoutMessage(com.android.systemui.statusbar.notification.collection.NotificationEntry):com.android.systemui.bubbles.Bubble$FlyoutMessage");
    }

    static Drawable loadSenderAvatar(Context context, Icon icon) {
        Objects.requireNonNull(context);
        if (icon == null) {
            return null;
        }
        if (icon.getType() == 4 || icon.getType() == 6) {
            context.grantUriPermission(context.getPackageName(), icon.getUri(), 1);
        }
        return icon.loadDrawable(context);
    }
}
