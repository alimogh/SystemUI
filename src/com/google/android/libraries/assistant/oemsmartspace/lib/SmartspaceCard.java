package com.google.android.libraries.assistant.oemsmartspace.lib;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import com.google.geo.sidekick.SmartspaceProto$SmartspaceUpdate;
import java.net.URISyntaxException;
import java.util.List;
public class SmartspaceCard {
    private final SmartspaceProto$SmartspaceUpdate.SmartspaceCard card;
    private final Context context;
    private Bitmap icon;

    /* access modifiers changed from: package-private */
    /* renamed from: com.google.android.libraries.assistant.oemsmartspace.lib.SmartspaceCard$1  reason: invalid class name */
    public /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$google$geo$sidekick$SmartspaceProto$SmartspaceUpdate$SmartspaceCard$Message$FormattedText$FormatParam$FormatParamArgs;
        static final /* synthetic */ int[] $SwitchMap$com$google$geo$sidekick$SmartspaceProto$SmartspaceUpdate$SmartspaceCard$TapAction$ActionType;

        static {
            int[] iArr = new int[SmartspaceProto$SmartspaceUpdate.SmartspaceCard.TapAction.ActionType.values().length];
            $SwitchMap$com$google$geo$sidekick$SmartspaceProto$SmartspaceUpdate$SmartspaceCard$TapAction$ActionType = iArr;
            try {
                iArr[SmartspaceProto$SmartspaceUpdate.SmartspaceCard.TapAction.ActionType.BROADCAST.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$google$geo$sidekick$SmartspaceProto$SmartspaceUpdate$SmartspaceCard$TapAction$ActionType[SmartspaceProto$SmartspaceUpdate.SmartspaceCard.TapAction.ActionType.START_ACTIVITY.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            int[] iArr2 = new int[SmartspaceProto$SmartspaceUpdate.SmartspaceCard.Message.FormattedText.FormatParam.FormatParamArgs.values().length];
            $SwitchMap$com$google$geo$sidekick$SmartspaceProto$SmartspaceUpdate$SmartspaceCard$Message$FormattedText$FormatParam$FormatParamArgs = iArr2;
            try {
                iArr2[SmartspaceProto$SmartspaceUpdate.SmartspaceCard.Message.FormattedText.FormatParam.FormatParamArgs.FIXED_STRING.ordinal()] = 1;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$google$geo$sidekick$SmartspaceProto$SmartspaceUpdate$SmartspaceCard$Message$FormattedText$FormatParam$FormatParamArgs[SmartspaceProto$SmartspaceUpdate.SmartspaceCard.Message.FormattedText.FormatParam.FormatParamArgs.EVENT_START_TIME.ordinal()] = 2;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$com$google$geo$sidekick$SmartspaceProto$SmartspaceUpdate$SmartspaceCard$Message$FormattedText$FormatParam$FormatParamArgs[SmartspaceProto$SmartspaceUpdate.SmartspaceCard.Message.FormattedText.FormatParam.FormatParamArgs.EVENT_END_TIME.ordinal()] = 3;
            } catch (NoSuchFieldError unused5) {
            }
        }
    }

    public SmartspaceCard(Context context, SmartspaceProto$SmartspaceUpdate.SmartspaceCard smartspaceCard, boolean z, Bitmap bitmap) {
        this.context = context;
        this.card = smartspaceCard;
        this.icon = bitmap;
    }

    private String getDurationText(SmartspaceProto$SmartspaceUpdate.SmartspaceCard.Message.FormattedText.FormatParam formatParam) {
        int minutesToEvent = getMinutesToEvent(formatParam);
        if (minutesToEvent < 60) {
            return this.context.getResources().getQuantityString(R$plurals.smartspace_minutes, minutesToEvent, Integer.valueOf(minutesToEvent));
        }
        int i = minutesToEvent / 60;
        int i2 = minutesToEvent % 60;
        String quantityString = this.context.getResources().getQuantityString(R$plurals.smartspace_hours, i, Integer.valueOf(i));
        if (i2 <= 0) {
            return quantityString;
        }
        return this.context.getString(R$string.smartspace_hours_mins, quantityString, this.context.getResources().getQuantityString(R$plurals.smartspace_minutes, i2, Integer.valueOf(i2)));
    }

    private SmartspaceProto$SmartspaceUpdate.SmartspaceCard.Message.FormattedText getFormattedText(boolean z) {
        SmartspaceProto$SmartspaceUpdate.SmartspaceCard.Message message = getMessage();
        if (message != null) {
            return z ? message.getTitle() : message.getSubtitle();
        }
        return null;
    }

    private SmartspaceProto$SmartspaceUpdate.SmartspaceCard.Message getMessage() {
        long currentTimeMillis = System.currentTimeMillis();
        long eventTimeMillis = this.card.getEventTimeMillis();
        long eventTimeMillis2 = this.card.getEventTimeMillis() + this.card.getEventDurationMillis();
        if (currentTimeMillis < eventTimeMillis) {
            return this.card.getPreEvent();
        }
        int i = (currentTimeMillis > eventTimeMillis2 ? 1 : (currentTimeMillis == eventTimeMillis2 ? 0 : -1));
        SmartspaceProto$SmartspaceUpdate.SmartspaceCard smartspaceCard = this.card;
        return i > 0 ? smartspaceCard.getPostEvent() : smartspaceCard.getDuringEvent();
    }

    private int getMinutesToEvent(SmartspaceProto$SmartspaceUpdate.SmartspaceCard.Message.FormattedText.FormatParam formatParam) {
        return (int) Math.ceil(((double) getMillisToEvent(formatParam)) / 60000.0d);
    }

    private Object[] getTextArgs(List list, String str) {
        int size = list.size();
        Object[] objArr = new Object[size];
        for (int i = 0; i < size; i++) {
            SmartspaceProto$SmartspaceUpdate.SmartspaceCard.Message.FormattedText.FormatParam formatParam = (SmartspaceProto$SmartspaceUpdate.SmartspaceCard.Message.FormattedText.FormatParam) list.get(i);
            int i2 = AnonymousClass1.$SwitchMap$com$google$geo$sidekick$SmartspaceProto$SmartspaceUpdate$SmartspaceCard$Message$FormattedText$FormatParam$FormatParamArgs[formatParam.getFormatParamArgs().ordinal()];
            if (i2 != 1) {
                if (i2 == 2 || i2 == 3) {
                    objArr[i] = getDurationText(formatParam);
                } else {
                    objArr[i] = "";
                }
            } else if (str == null || formatParam.getTruncateLocation() == SmartspaceProto$SmartspaceUpdate.SmartspaceCard.Message.FormattedText.TruncateLocation.UNSPECIFIED) {
                objArr[i] = formatParam.getText();
            } else {
                objArr[i] = str;
            }
        }
        return objArr;
    }

    private static boolean hasParams(SmartspaceProto$SmartspaceUpdate.SmartspaceCard.Message.FormattedText formattedText) {
        return !formattedText.getFormatParamList().isEmpty();
    }

    private String substitute(boolean z, String str) {
        if (z) {
            return getFormattedTitle();
        }
        SmartspaceProto$SmartspaceUpdate.SmartspaceCard.Message.FormattedText formattedText = getFormattedText(false);
        if (formattedText == null || formattedText.hasText()) {
            return "";
        }
        String text = formattedText.getText();
        return hasParams(formattedText) ? String.format(text, getTextArgs(formattedText.getFormatParamList(), str)) : text;
    }

    public String ellipsizeTitle(String str) {
        return substitute(true, str);
    }

    public SmartspaceProto$SmartspaceUpdate.SmartspaceCard getCard() {
        return this.card;
    }

    public long getExpiration() {
        return this.card.getExpiryCriteria().getExpirationTimeMillis();
    }

    public String getFormattedTitle() {
        SmartspaceProto$SmartspaceUpdate.SmartspaceCard.Message message = getMessage();
        if (message == null) {
            return "";
        }
        SmartspaceProto$SmartspaceUpdate.SmartspaceCard.Message.FormattedText title = message.getTitle();
        String text = title.getText();
        if (!hasParams(title)) {
            return text;
        }
        String str = null;
        String str2 = null;
        for (int i = 0; i < title.getFormatParamCount(); i++) {
            SmartspaceProto$SmartspaceUpdate.SmartspaceCard.Message.FormattedText.FormatParam formatParam = title.getFormatParam(i);
            int i2 = AnonymousClass1.$SwitchMap$com$google$geo$sidekick$SmartspaceProto$SmartspaceUpdate$SmartspaceCard$Message$FormattedText$FormatParam$FormatParamArgs[formatParam.getFormatParamArgs().ordinal()];
            if (i2 == 1) {
                str2 = formatParam.getText();
            } else if (i2 == 2 || i2 == 3) {
                str = getDurationText(formatParam);
            }
        }
        if (this.card.getCardType() == SmartspaceProto$SmartspaceUpdate.SmartspaceCard.CardType.COMMUTE_TIME && title.getFormatParamCount() == 2) {
            str = title.getFormatParam(0).getText();
            str2 = title.getFormatParam(1).getText();
        }
        if (str2 == null) {
            return "";
        }
        if (str == null) {
            if (!message.equals(this.card.getDuringEvent())) {
                return text;
            }
            str = this.context.getString(R$string.smartspace_calendar_now);
        }
        return this.context.getString(R$string.smartspace_calendar_reformatted_event, str, str2);
    }

    public String getFullWeatherAccessibilityDescription() {
        String title = getTitle();
        String weatherDescription = getWeatherDescription();
        return title.isEmpty() ? weatherDescription : weatherDescription.isEmpty() ? title : this.context.getString(R$string.weather_description, weatherDescription, title);
    }

    public Bitmap getIcon() {
        return this.icon;
    }

    public Intent getIntent() {
        try {
            String intent = this.card.getTapAction().getIntent();
            String valueOf = String.valueOf(intent);
            Log.e("SmartspaceCard", valueOf.length() != 0 ? "smartspace card intent uri: ".concat(valueOf) : new String("smartspace card intent uri: "));
            Intent parseUri = intent.isEmpty() ? null : Intent.parseUri(intent, 0);
            if (parseUri != null) {
                parseUri.setFlags(268468224);
            }
            return parseUri;
        } catch (URISyntaxException e) {
            String valueOf2 = String.valueOf(e);
            StringBuilder sb = new StringBuilder(String.valueOf(valueOf2).length() + 32);
            sb.append("cannot get the tap intent action");
            sb.append(valueOf2);
            Log.e("SmartspaceCard", sb.toString());
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public long getMillisToEvent(SmartspaceProto$SmartspaceUpdate.SmartspaceCard.Message.FormattedText.FormatParam formatParam) {
        return Math.abs(System.currentTimeMillis() - (formatParam.getFormatParamArgs() == SmartspaceProto$SmartspaceUpdate.SmartspaceCard.Message.FormattedText.FormatParam.FormatParamArgs.EVENT_END_TIME ? this.card.getEventTimeMillis() + this.card.getEventDurationMillis() : this.card.getEventTimeMillis()));
    }

    public String getSubtitle() {
        SmartspaceProto$SmartspaceUpdate.SmartspaceCard.Message.FormattedText subtitle = getMessage().getSubtitle();
        String text = subtitle.getText();
        return hasParams(subtitle) ? String.format(text, getTextArgs(subtitle.getFormatParamList(), null)) : text;
    }

    public String getTextNonTruncatable(boolean z) {
        return substitute(z, "");
    }

    public String getTextTruncatable(boolean z) {
        List<SmartspaceProto$SmartspaceUpdate.SmartspaceCard.Message.FormattedText.FormatParam> formatParamList = getFormattedText(z).getFormatParamList();
        if (formatParamList.isEmpty()) {
            return "";
        }
        for (SmartspaceProto$SmartspaceUpdate.SmartspaceCard.Message.FormattedText.FormatParam formatParam : formatParamList) {
            if (formatParam.getTruncateLocation() != SmartspaceProto$SmartspaceUpdate.SmartspaceCard.Message.FormattedText.TruncateLocation.UNSPECIFIED) {
                return formatParam.getText();
            }
        }
        return "";
    }

    public String getTitle() {
        return getFormattedTitle();
    }

    public TextUtils.TruncateAt getTruncateAt(boolean z) {
        SmartspaceProto$SmartspaceUpdate.SmartspaceCard.Message.FormattedText subtitle;
        SmartspaceProto$SmartspaceUpdate.SmartspaceCard.Message message = getMessage();
        if (message != null) {
            if (z && message.hasTitle()) {
                subtitle = message.getTitle();
            } else if (!z && message.hasSubtitle()) {
                subtitle = message.getSubtitle();
            }
            int number = subtitle.getTruncateLocation().getNumber();
            if (number == 1) {
                return TextUtils.TruncateAt.START;
            }
            if (number == 2) {
                return TextUtils.TruncateAt.MIDDLE;
            }
        }
        return TextUtils.TruncateAt.END;
    }

    public String getWeatherDescription() {
        if (this.card.hasIcon() && this.card.getIcon().hasContentDescription()) {
            return this.card.getIcon().getContentDescription();
        }
        Log.e("SmartspaceCard", "unable to get weatherDescription");
        return "";
    }

    public boolean hasParams() {
        SmartspaceProto$SmartspaceUpdate.SmartspaceCard.Message message = getMessage();
        if (message != null) {
            return hasParams(message.getTitle()) || hasParams(message.getSubtitle());
        }
        return false;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > getExpiration();
    }

    public void performCardAction(View view) {
        Intent intent = new Intent(getIntent());
        int i = AnonymousClass1.$SwitchMap$com$google$geo$sidekick$SmartspaceProto$SmartspaceUpdate$SmartspaceCard$TapAction$ActionType[this.card.getTapAction().getActionType().ordinal()];
        if (i == 1) {
            intent.addFlags(268435456);
            intent.setPackage("com.google.android.googlequicksearchbox");
            intent.putExtra("com.google.android.apps.gsa.staticplugins.opa.smartspace.extra.SURFACE_TYPE_EXTRA", 3);
            try {
                view.getContext().sendBroadcast(intent);
            } catch (SecurityException e) {
                Log.w("SmartspaceCard", "Cannot perform click action", e);
            }
        } else if (i != 2) {
            String valueOf = String.valueOf(this.card.getTapAction().getActionType().name());
            Log.w("SmartspaceCard", valueOf.length() != 0 ? "unknown action type".concat(valueOf) : new String("unknown action type"));
        } else {
            this.context.startActivity(intent);
        }
    }
}
