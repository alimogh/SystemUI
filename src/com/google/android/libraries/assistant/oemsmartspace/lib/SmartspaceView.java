package com.google.android.libraries.assistant.oemsmartspace.lib;

import android.animation.LayoutTransition;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;
import com.google.android.libraries.assistant.oemsmartspace.shared.SmartspaceUpdateListener;
import com.google.geo.sidekick.SmartspaceProto$SmartspaceUpdate;
public class SmartspaceView {
    private final int backgroundRes = R$drawable.bg_smartspace;
    private TextClock clock;
    private final LinearLayout contentView;
    private final Context context;
    private String customizeResourcePackage = "";
    private SmartspaceData data;
    private boolean enableDate = true;
    private boolean enableWeather = true;
    private boolean isLanguageSupported;
    private boolean isLeftAligned = false;
    private final LayoutInflater layoutInflater;
    private final SmartspaceUpdateListener listener;
    final int smartspaceHorizontalPadding;
    private final LinearLayout sscsContainer;
    private ImageView subtitleIcon;
    private LinearLayout subtitleLine;
    private TextView subtitleTextView;
    private TextView subtitleWeather;
    private int textColor = 0;
    private Typeface textFont = null;
    private final TextPaint textPaint = new TextPaint();
    private float textSizeFactor = 1.0f;
    private TextView titleTextView;
    private LinearLayout topLine;
    private ImageView weatherIcon;

    /* access modifiers changed from: package-private */
    /* renamed from: com.google.android.libraries.assistant.oemsmartspace.lib.SmartspaceView$4  reason: invalid class name */
    public /* synthetic */ class AnonymousClass4 {
        static final /* synthetic */ int[] $SwitchMap$com$google$geo$sidekick$SmartspaceProto$SmartspaceUpdate$SmartspaceCard$CardType;

        static {
            int[] iArr = new int[SmartspaceProto$SmartspaceUpdate.SmartspaceCard.CardType.values().length];
            $SwitchMap$com$google$geo$sidekick$SmartspaceProto$SmartspaceUpdate$SmartspaceCard$CardType = iArr;
            try {
                iArr[SmartspaceProto$SmartspaceUpdate.SmartspaceCard.CardType.CALENDAR.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$google$geo$sidekick$SmartspaceProto$SmartspaceUpdate$SmartspaceCard$CardType[SmartspaceProto$SmartspaceUpdate.SmartspaceCard.CardType.COMMUTE_TIME.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$google$geo$sidekick$SmartspaceProto$SmartspaceUpdate$SmartspaceCard$CardType[SmartspaceProto$SmartspaceUpdate.SmartspaceCard.CardType.ALARM.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$google$geo$sidekick$SmartspaceProto$SmartspaceUpdate$SmartspaceCard$CardType[SmartspaceProto$SmartspaceUpdate.SmartspaceCard.CardType.FLIGHT.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$com$google$geo$sidekick$SmartspaceProto$SmartspaceUpdate$SmartspaceCard$CardType[SmartspaceProto$SmartspaceUpdate.SmartspaceCard.CardType.REMINDER.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
        }
    }

    public SmartspaceView(Context context, SmartspaceUpdateListener smartspaceUpdateListener) {
        LayoutTransition layoutTransition = new LayoutTransition();
        layoutTransition.disableTransitionType(0);
        layoutTransition.disableTransitionType(1);
        this.context = context;
        this.smartspaceHorizontalPadding = context.getResources().getDimensionPixelSize(R$dimen.smartspace_horizontal_padding);
        context.getResources().getDimensionPixelSize(R$dimen.smartspace_title_icon_size_5x5);
        LayoutInflater from = LayoutInflater.from(context);
        this.layoutInflater = from;
        LinearLayout linearLayout = (LinearLayout) from.inflate(R$layout.smartspace_view, (ViewGroup) null, false);
        this.contentView = linearLayout;
        this.sscsContainer = (LinearLayout) linearLayout.findViewById(R$id.sscs_container);
        bindViews();
        this.listener = smartspaceUpdateListener;
        this.isLanguageSupported = SmartspaceContainerController.isLanguageSupported();
    }

    private boolean applyCurrentCard(SmartspaceData smartspaceData) {
        String str;
        TextView textView;
        this.contentView.setBackgroundResource(this.backgroundRes);
        if (!smartspaceData.hasCurrent()) {
            Log.e("SmartspaceView", "No current card available to display");
            this.titleTextView.setVisibility(8);
            this.subtitleTextView.setVisibility(8);
            this.subtitleIcon.setVisibility(8);
            this.topLine.setVisibility(8);
            return false;
        }
        final SmartspaceCard smartspaceCard = smartspaceData.currentCard;
        if (TextUtils.isEmpty(smartspaceCard.getTitle())) {
            return false;
        }
        if (smartspaceCard.hasParams()) {
            textView = this.titleTextView;
            str = ellipsizeTitle();
        } else {
            textView = this.titleTextView;
            str = smartspaceCard.getTitle();
        }
        textView.setText(str, (TextView.BufferType) null);
        this.titleTextView.setEllipsize(smartspaceCard.getTruncateAt(true));
        if (smartspaceCard.getCard() != null && smartspaceCard.getCard().hasTapAction()) {
            this.titleTextView.setOnClickListener(new View.OnClickListener(this) { // from class: com.google.android.libraries.assistant.oemsmartspace.lib.SmartspaceView.2
                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    smartspaceCard.performCardAction(view);
                }
            });
        }
        setCustomizeTextStyle(this.titleTextView, this.context.getResources().getDimension(R$dimen.smartspace_title_size));
        this.titleTextView.setVisibility(0);
        enableLayoutWithCustomizeParams(this.topLine);
        if (!TextUtils.isEmpty(smartspaceCard.getSubtitle())) {
            this.clock.setVisibility(8);
            if (smartspaceCard.getIcon() != null) {
                this.subtitleIcon.setImageDrawable(getCustomizedIcon(smartspaceCard));
                int i = this.textColor;
                if (i != 0) {
                    this.subtitleIcon.setColorFilter(i, PorterDuff.Mode.SRC_IN);
                }
                this.subtitleIcon.setVisibility(0);
            }
            this.subtitleTextView.setText(ellipsizeSubtitle());
            this.subtitleTextView.setOnClickListener(new View.OnClickListener(this) { // from class: com.google.android.libraries.assistant.oemsmartspace.lib.SmartspaceView.3
                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    smartspaceCard.performCardAction(view);
                }
            });
            float dimension = this.context.getResources().getDimension(R$dimen.smartspace_text_size);
            setSubtitleMargin();
            setCustomizeTextStyle(this.subtitleTextView, dimension);
            this.subtitleTextView.setVisibility(0);
        }
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x0051  */
    /* JADX WARNING: Removed duplicated region for block: B:12:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void applyDate(boolean r4) {
        /*
            r3 = this;
            android.widget.LinearLayout r0 = r3.contentView
            android.animation.LayoutTransition r0 = r0.getLayoutTransition()
            if (r4 == 0) goto L_0x0015
            if (r0 != 0) goto L_0x0015
            android.widget.LinearLayout r4 = r3.contentView
            android.animation.LayoutTransition r0 = new android.animation.LayoutTransition
            r0.<init>()
        L_0x0011:
            r4.setLayoutTransition(r0)
            goto L_0x001d
        L_0x0015:
            if (r4 != 0) goto L_0x001d
            if (r0 == 0) goto L_0x001d
            android.widget.LinearLayout r4 = r3.contentView
            r0 = 0
            goto L_0x0011
        L_0x001d:
            java.util.Locale r4 = java.util.Locale.getDefault()
            android.content.Context r0 = r3.context
            android.content.res.Resources r0 = r0.getResources()
            int r1 = com.google.android.libraries.assistant.oemsmartspace.lib.R$string.icu_w_month_day_no_year
            java.lang.String r0 = r0.getString(r1)
            java.lang.String r4 = android.text.format.DateFormat.getBestDateTimePattern(r4, r0)
            android.widget.TextView r0 = r3.titleTextView
            r1 = 8
            r0.setVisibility(r1)
            android.widget.LinearLayout r0 = r3.topLine
            r0.setVisibility(r1)
            android.widget.LinearLayout r0 = r3.contentView
            r2 = 0
            r0.setBackgroundResource(r2)
            android.widget.TextView r0 = r3.subtitleTextView
            r0.setVisibility(r1)
            android.widget.ImageView r0 = r3.subtitleIcon
            r0.setVisibility(r1)
            boolean r0 = r3.enableDate
            if (r0 == 0) goto L_0x0071
            android.widget.TextClock r0 = r3.clock
            r0.setFormat12Hour(r4)
            android.widget.TextClock r0 = r3.clock
            r0.setFormat24Hour(r4)
            android.widget.TextClock r4 = r3.clock
            r4.setVisibility(r2)
            android.content.Context r4 = r3.context
            android.content.res.Resources r4 = r4.getResources()
            int r0 = com.google.android.libraries.assistant.oemsmartspace.lib.R$dimen.smartspace_text_size
            float r4 = r4.getDimension(r0)
            android.widget.TextClock r0 = r3.clock
            r3.setCustomizeTextStyle(r0, r4)
        L_0x0071:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.libraries.assistant.oemsmartspace.lib.SmartspaceView.applyDate(boolean):void");
    }

    private void applyNewHeightToListener() {
        boolean z = true;
        boolean z2 = this.titleTextView.getVisibility() == 0;
        if (this.sscsContainer.getVisibility() != 0) {
            z = false;
        }
        ViewGroup.LayoutParams layoutParams = this.contentView.getLayoutParams();
        if (layoutParams != null) {
            layoutParams.height = -2;
            this.contentView.setLayoutParams(layoutParams);
        }
        this.contentView.measure(View.MeasureSpec.makeMeasureSpec(0, 0), View.MeasureSpec.makeMeasureSpec(0, 0));
        int measuredHeight = this.contentView.getMeasuredHeight();
        if (z2 && z) {
            StringBuilder sb = new StringBuilder(43);
            sb.append("onBothCardAndChipShown, height: ");
            sb.append(measuredHeight);
            Log.d("SmartspaceView", sb.toString());
            this.listener.onBothCardAndChipShown(measuredHeight);
        } else if (z) {
            StringBuilder sb2 = new StringBuilder(32);
            sb2.append("onChipShown, height: ");
            sb2.append(measuredHeight);
            Log.d("SmartspaceView", sb2.toString());
            this.listener.onChipShown(measuredHeight);
        } else if (z2) {
            StringBuilder sb3 = new StringBuilder(32);
            sb3.append("onCardShown, height: ");
            sb3.append(measuredHeight);
            Log.d("SmartspaceView", sb3.toString());
            this.listener.onCardShown(measuredHeight);
        } else {
            StringBuilder sb4 = new StringBuilder(41);
            sb4.append("onNoCardAndChipShown, height: ");
            sb4.append(measuredHeight);
            Log.d("SmartspaceView", sb4.toString());
            this.listener.onNoCardAndChipShown(measuredHeight);
        }
    }

    private void applyWeather(SmartspaceData smartspaceData) {
        if (this.subtitleWeather != null && this.weatherIcon != null) {
            if (!smartspaceData.hasWeather() || !this.enableWeather) {
                this.subtitleWeather.setVisibility(8);
                this.weatherIcon.setVisibility(8);
                return;
            }
            if (!this.enableDate && !smartspaceData.hasCurrent()) {
                this.clock.setVisibility(8);
                this.subtitleIcon.setVisibility(8);
                this.subtitleTextView.setText(smartspaceData.weatherCard.getWeatherDescription());
                this.subtitleTextView.setVisibility(0);
                setCustomizeTextStyle(this.subtitleTextView, this.context.getResources().getDimension(R$dimen.smartspace_text_size));
            }
            this.subtitleWeather.setContentDescription(smartspaceData.weatherCard.getFullWeatherAccessibilityDescription());
            setCustomizeTextStyle(this.subtitleWeather, this.context.getResources().getDimension(R$dimen.smartspace_text_size));
            this.subtitleWeather.setText(smartspaceData.weatherCard.getTitle(), (TextView.BufferType) null);
            this.weatherIcon.setBackground(new BitmapDrawable(smartspaceData.weatherCard.getIcon()));
            this.weatherIcon.setVisibility(0);
            this.subtitleWeather.setVisibility(0);
        }
    }

    private void bindViews() {
        this.subtitleWeather = (TextView) this.contentView.findViewById(R$id.subtitle_weather_text);
        this.weatherIcon = (ImageView) this.contentView.findViewById(R$id.subtitle_weather_icon);
        this.subtitleIcon = (ImageView) this.contentView.findViewById(R$id.subtitle_icon);
        this.clock = (TextClock) this.contentView.findViewById(R$id.date_text);
        this.topLine = (LinearLayout) this.contentView.findViewById(R$id.top_line);
        this.titleTextView = (TextView) this.contentView.findViewById(R$id.title_fixed_text);
        this.subtitleLine = (LinearLayout) this.contentView.findViewById(R$id.subtitle_line);
        this.subtitleTextView = (TextView) this.contentView.findViewById(R$id.subtitle_text);
        this.clock.setVisibility(8);
        this.subtitleWeather.setVisibility(8);
        this.weatherIcon.setVisibility(8);
        this.subtitleIcon.setVisibility(8);
        this.titleTextView.setVisibility(8);
        this.subtitleTextView.setVisibility(8);
    }

    private CharSequence ellipsizeSubtitle() {
        int dimensionPixelSize = ((this.context.getResources().getDimensionPixelSize(R$dimen.subtitle_max_width) - this.subtitleIcon.getWidth()) - getHorizontalMargin(this.subtitleTextView)) - getHorizontalMargin(this.subtitleLine);
        TextView textView = this.subtitleWeather;
        if (textView != null && textView.getVisibility() == 0) {
            dimensionPixelSize -= this.subtitleWeather.getWidth();
        }
        this.textPaint.setTextSize(this.subtitleTextView.getTextSize());
        SmartspaceCard smartspaceCard = this.data.currentCard;
        return TextUtils.ellipsize(smartspaceCard.getSubtitle(), this.textPaint, (float) dimensionPixelSize, smartspaceCard.getTruncateAt(false));
    }

    private String ellipsizeTitle() {
        SmartspaceCard smartspaceCard = this.data.currentCard;
        int maxLines = this.titleTextView.getMaxLines();
        int dimensionPixelSize = this.context.getResources().getDimensionPixelSize(R$dimen.subtitle_max_width);
        int paddingLeft = this.contentView.getPaddingLeft();
        int paddingRight = this.contentView.getPaddingRight();
        int i = this.smartspaceHorizontalPadding;
        this.textPaint.setTextSize(this.titleTextView.getTextSize());
        return smartspaceCard.ellipsizeTitle(TextUtils.ellipsize(smartspaceCard.getTextTruncatable(true), this.textPaint, ((float) (maxLines * (((dimensionPixelSize - paddingLeft) - paddingRight) - i))) - this.textPaint.measureText(smartspaceCard.getTextNonTruncatable(true)), TextUtils.TruncateAt.END).toString());
    }

    private void enableLayoutWithCustomizeParams(LinearLayout linearLayout) {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) linearLayout.getLayoutParams();
        layoutParams.gravity = this.isLeftAligned ? 8388611 : 17;
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setVisibility(0);
    }

    private static String getCardIconName(SmartspaceProto$SmartspaceUpdate.SmartspaceCard.CardType cardType) {
        int i = AnonymousClass4.$SwitchMap$com$google$geo$sidekick$SmartspaceProto$SmartspaceUpdate$SmartspaceCard$CardType[cardType.ordinal()];
        return i != 1 ? i != 2 ? i != 3 ? i != 4 ? i != 5 ? "" : "reminder" : "flight" : "alarm" : "commute" : "calendar";
    }

    private Drawable getCustomizedIcon(SmartspaceCard smartspaceCard) {
        int identifier;
        return (this.customizeResourcePackage.isEmpty() || (identifier = this.context.getResources().getIdentifier(getCardIconName(smartspaceCard.getCard().getCardType()), "drawable", this.customizeResourcePackage)) == 0) ? new BitmapDrawable(this.context.getResources(), smartspaceCard.getIcon()) : this.context.getResources().getDrawable(identifier, null);
    }

    private static int getHorizontalMargin(View view) {
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        return marginLayoutParams.getMarginStart() + marginLayoutParams.getMarginEnd();
    }

    private void setChipStrokeAndBackgroundColor(View view) {
        if (this.textColor != 0) {
            GradientDrawable gradientDrawable = (GradientDrawable) view.getBackground();
            int argb = Color.argb(Color.alpha(this.context.getColor(R$color.cw_chip_action_bg)), Color.red(this.textColor), Color.green(this.textColor), Color.blue(this.textColor));
            float dimension = this.context.getResources().getDimension(R$dimen.ssc_action_bg_stroke_width);
            int argb2 = Color.argb(Color.alpha(this.context.getColor(R$color.cw_chip_bg)), 255 - Color.red(this.textColor), 255 - Color.green(this.textColor), 255 - Color.blue(this.textColor));
            gradientDrawable.setStroke((int) dimension, argb);
            gradientDrawable.setColor(argb2);
        }
    }

    private void setCustomizeTextStyle(TextView textView, float f) {
        int i = this.textColor;
        if (i != 0) {
            textView.setTextColor(i);
        }
        Typeface typeface = this.textFont;
        if (typeface != null) {
            textView.setTypeface(typeface);
        }
        textView.setTextSize(0, f * this.textSizeFactor);
    }

    private void setSubtitleMargin() {
        float dimension = this.context.getResources().getDimension(R$dimen.vertical_margin);
        this.subtitleLine.getLayoutParams();
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.subtitleLine.getLayoutParams();
        layoutParams.topMargin = (int) (dimension * this.textSizeFactor);
        this.subtitleLine.setLayoutParams(layoutParams);
    }

    private void updateChip(final SmartspaceChip smartspaceChip) {
        if (smartspaceChip != null) {
            View inflate = this.layoutInflater.inflate(R$layout.ssc, (ViewGroup) this.sscsContainer, false);
            this.sscsContainer.addView(inflate);
            this.sscsContainer.setVisibility(0);
            ImageView imageView = (ImageView) inflate.findViewById(R$id.ssc_icon);
            TextView textView = (TextView) inflate.findViewById(R$id.ssc_textview);
            textView.setText(smartspaceChip.getTitle());
            setCustomizeTextStyle(textView, this.context.getResources().getDimension(R$dimen.ssc_text_size));
            View findViewById = inflate.findViewById(R$id.ssc_wrapper);
            findViewById.setVisibility(0);
            setChipStrokeAndBackgroundColor(findViewById);
            textView.setVisibility(0);
            if (smartspaceChip.hasIcon()) {
                imageView.setImageDrawable(smartspaceChip.getIcon());
                int i = this.textColor;
                if (i != 0) {
                    imageView.setColorFilter(i, PorterDuff.Mode.SRC_IN);
                }
                imageView.setVisibility(0);
            }
            if (smartspaceChip.hasPendingIntent()) {
                inflate.setOnClickListener(new View.OnClickListener(this) { // from class: com.google.android.libraries.assistant.oemsmartspace.lib.SmartspaceView.1
                    @Override // android.view.View.OnClickListener
                    public void onClick(View view) {
                        Log.d("SmartspaceView", "Open intent from lock screen");
                        try {
                            smartspaceChip.getChipIntent().send();
                        } catch (PendingIntent.CanceledException unused) {
                            Log.e("SmartspaceView", "Unhandled Chip exception");
                        }
                    }
                });
            }
        }
    }

    private void updateChips() {
        enableLayoutWithCustomizeParams(this.sscsContainer);
        this.sscsContainer.removeAllViews();
        this.sscsContainer.setVisibility(8);
        updateChip(this.data.firstChip);
        updateChip(this.data.secondChip);
    }

    private void updateView(boolean z) {
        enableLayoutWithCustomizeParams(this.subtitleLine);
        SmartspaceData smartspaceData = this.data;
        if (smartspaceData == null || !this.isLanguageSupported) {
            Log.d("SmartspaceView", "No smartspace data available right now");
            this.weatherIcon.setVisibility(8);
            this.subtitleWeather.setVisibility(8);
            this.sscsContainer.removeAllViews();
            this.sscsContainer.setVisibility(8);
            applyDate(z);
            return;
        }
        if (!smartspaceData.hasCurrent() || !applyCurrentCard(this.data)) {
            applyDate(z);
        }
        applyWeather(this.data);
        updateChips();
    }

    public void enableDate(boolean z) {
        this.enableDate = z;
    }

    public void enableWeather(boolean z) {
        this.enableWeather = z;
    }

    public View onSmartspaceUpdated(SmartspaceData smartspaceData, boolean z) {
        this.data = smartspaceData;
        updateView(z);
        this.contentView.setVisibility(0);
        applyNewHeightToListener();
        return this.contentView;
    }

    public void setColor(int i) {
        this.textColor = i;
    }

    public void setFont(String str) {
        try {
            this.textFont = Typeface.createFromAsset(this.context.getAssets(), str);
        } catch (RuntimeException unused) {
            String valueOf = String.valueOf(str);
            Log.e("SmartspaceView", valueOf.length() != 0 ? "Cannot find font with the provided path: ".concat(valueOf) : new String("Cannot find font with the provided path: "));
        }
    }

    public void setLanguageSupported(boolean z) {
        this.isLanguageSupported = z;
    }

    public void setLeftAligned(boolean z) {
        this.isLeftAligned = z;
    }

    public void setResource(String str) {
        this.customizeResourcePackage = str;
    }

    public void setTextSizeFactor(float f) {
        this.textSizeFactor = f;
    }
}
