package com.google.geo.sidekick;

import com.google.protobuf.GeneratedMessageLite;
import com.google.protobuf.Internal;
import com.google.protobuf.Parser;
import java.util.List;
public final class SmartspaceProto$SmartspaceUpdate extends GeneratedMessageLite<SmartspaceProto$SmartspaceUpdate, Builder> implements Object {
    public static final int CARD_FIELD_NUMBER = 1;
    private static final SmartspaceProto$SmartspaceUpdate DEFAULT_INSTANCE;
    private static volatile Parser<SmartspaceProto$SmartspaceUpdate> PARSER;
    private Internal.ProtobufList<SmartspaceCard> card_ = GeneratedMessageLite.emptyProtobufList();

    private SmartspaceProto$SmartspaceUpdate() {
    }

    public static final class SmartspaceCard extends GeneratedMessageLite<SmartspaceCard, Builder> implements Object {
        public static final int CARD_ID_FIELD_NUMBER = 2;
        public static final int CARD_PRIORITY_FIELD_NUMBER = 13;
        public static final int CARD_TYPE_FIELD_NUMBER = 7;
        private static final SmartspaceCard DEFAULT_INSTANCE;
        public static final int DURING_EVENT_FIELD_NUMBER = 4;
        public static final int DURING_EVENT_STATIC_FIELD_NUMBER = 15;
        public static final int EVENT_DURATION_MILLIS_FIELD_NUMBER = 11;
        public static final int EVENT_TIME_MILLIS_FIELD_NUMBER = 10;
        public static final int EXPIRY_CRITERIA_FIELD_NUMBER = 12;
        public static final int ICON_FIELD_NUMBER = 6;
        public static final int IS_DATA_FROM_3P_APP_FIELD_NUMBER = 19;
        public static final int IS_SENSITIVE_FIELD_NUMBER = 17;
        public static final int IS_WORK_PROFILE_FIELD_NUMBER = 18;
        private static volatile Parser<SmartspaceCard> PARSER = null;
        public static final int POST_EVENT_FIELD_NUMBER = 5;
        public static final int POST_EVENT_STATIC_FIELD_NUMBER = 16;
        public static final int PRE_EVENT_FIELD_NUMBER = 3;
        public static final int PRE_EVENT_STATIC_FIELD_NUMBER = 14;
        public static final int SHOULD_DISCARD_FIELD_NUMBER = 1;
        public static final int TAP_ACTION_FIELD_NUMBER = 8;
        public static final int UPDATE_TIME_MILLIS_FIELD_NUMBER = 9;
        private int bitField0_;
        private int cardId_;
        private int cardPriority_;
        private int cardType_;
        private Message duringEventStatic_;
        private Message duringEvent_;
        private long eventDurationMillis_;
        private long eventTimeMillis_;
        private ExpiryCriteria expiryCriteria_;
        private Image icon_;
        private boolean isDataFrom3PApp_;
        private boolean isSensitive_;
        private boolean isWorkProfile_;
        private Message postEventStatic_;
        private Message postEvent_;
        private Message preEventStatic_;
        private Message preEvent_;
        private boolean shouldDiscard_;
        private TapAction tapAction_;
        private long updateTimeMillis_;

        private SmartspaceCard() {
        }

        public enum CardPriority implements Internal.EnumLite {
            PRIORITY_UNDEFINED(0),
            PRIMARY(1),
            SECONDARY(2);
            
            private final int value;

            @Override // com.google.protobuf.Internal.EnumLite
            public final int getNumber() {
                return this.value;
            }

            public static CardPriority forNumber(int i) {
                if (i == 0) {
                    return PRIORITY_UNDEFINED;
                }
                if (i == 1) {
                    return PRIMARY;
                }
                if (i != 2) {
                    return null;
                }
                return SECONDARY;
            }

            public static Internal.EnumVerifier internalGetVerifier() {
                return CardPriorityVerifier.INSTANCE;
            }

            /* access modifiers changed from: private */
            public static final class CardPriorityVerifier implements Internal.EnumVerifier {
                static final Internal.EnumVerifier INSTANCE = new CardPriorityVerifier();

                private CardPriorityVerifier() {
                }

                @Override // com.google.protobuf.Internal.EnumVerifier
                public boolean isInRange(int i) {
                    return CardPriority.forNumber(i) != null;
                }
            }

            private CardPriority(int i) {
                this.value = i;
            }
        }

        public enum CardType implements Internal.EnumLite {
            UNDEFINED(0),
            WEATHER(1),
            CALENDAR(2),
            COMMUTE_TIME(3),
            FLIGHT(4),
            BIRTHDAY(5),
            AMBIENT_MUSIC(6),
            TIPS(7),
            REMINDER(8),
            ASSISTANT(9),
            ALARM(10),
            HAMMERSPACE_DEBUG(11),
            OOBE(12);
            
            private final int value;

            @Override // com.google.protobuf.Internal.EnumLite
            public final int getNumber() {
                return this.value;
            }

            public static CardType forNumber(int i) {
                switch (i) {
                    case 0:
                        return UNDEFINED;
                    case 1:
                        return WEATHER;
                    case 2:
                        return CALENDAR;
                    case 3:
                        return COMMUTE_TIME;
                    case 4:
                        return FLIGHT;
                    case 5:
                        return BIRTHDAY;
                    case 6:
                        return AMBIENT_MUSIC;
                    case 7:
                        return TIPS;
                    case 8:
                        return REMINDER;
                    case 9:
                        return ASSISTANT;
                    case 10:
                        return ALARM;
                    case 11:
                        return HAMMERSPACE_DEBUG;
                    case 12:
                        return OOBE;
                    default:
                        return null;
                }
            }

            public static Internal.EnumVerifier internalGetVerifier() {
                return CardTypeVerifier.INSTANCE;
            }

            /* access modifiers changed from: private */
            public static final class CardTypeVerifier implements Internal.EnumVerifier {
                static final Internal.EnumVerifier INSTANCE = new CardTypeVerifier();

                private CardTypeVerifier() {
                }

                @Override // com.google.protobuf.Internal.EnumVerifier
                public boolean isInRange(int i) {
                    return CardType.forNumber(i) != null;
                }
            }

            private CardType(int i) {
                this.value = i;
            }
        }

        public static final class Message extends GeneratedMessageLite<Message, Builder> implements Object {
            private static final Message DEFAULT_INSTANCE;
            private static volatile Parser<Message> PARSER = null;
            public static final int SUBTITLE_FIELD_NUMBER = 2;
            public static final int TITLE_FIELD_NUMBER = 1;
            private int bitField0_;
            private FormattedText subtitle_;
            private FormattedText title_;

            private Message() {
            }

            public static final class FormattedText extends GeneratedMessageLite<FormattedText, Builder> implements Object {
                private static final FormattedText DEFAULT_INSTANCE;
                public static final int FORMAT_PARAM_FIELD_NUMBER = 3;
                private static volatile Parser<FormattedText> PARSER = null;
                public static final int TEXT_FIELD_NUMBER = 1;
                public static final int TRUNCATE_LOCATION_FIELD_NUMBER = 2;
                private int bitField0_;
                private Internal.ProtobufList<FormatParam> formatParam_ = GeneratedMessageLite.emptyProtobufList();
                private String text_ = "";
                private int truncateLocation_;

                private FormattedText() {
                }

                public enum TruncateLocation implements Internal.EnumLite {
                    UNSPECIFIED(0),
                    START(1),
                    MIDDLE(2),
                    END(3);
                    
                    private final int value;

                    @Override // com.google.protobuf.Internal.EnumLite
                    public final int getNumber() {
                        return this.value;
                    }

                    public static TruncateLocation forNumber(int i) {
                        if (i == 0) {
                            return UNSPECIFIED;
                        }
                        if (i == 1) {
                            return START;
                        }
                        if (i == 2) {
                            return MIDDLE;
                        }
                        if (i != 3) {
                            return null;
                        }
                        return END;
                    }

                    public static Internal.EnumVerifier internalGetVerifier() {
                        return TruncateLocationVerifier.INSTANCE;
                    }

                    /* access modifiers changed from: private */
                    public static final class TruncateLocationVerifier implements Internal.EnumVerifier {
                        static final Internal.EnumVerifier INSTANCE = new TruncateLocationVerifier();

                        private TruncateLocationVerifier() {
                        }

                        @Override // com.google.protobuf.Internal.EnumVerifier
                        public boolean isInRange(int i) {
                            return TruncateLocation.forNumber(i) != null;
                        }
                    }

                    private TruncateLocation(int i) {
                        this.value = i;
                    }
                }

                public static final class FormatParam extends GeneratedMessageLite<FormatParam, Builder> implements Object {
                    private static final FormatParam DEFAULT_INSTANCE;
                    public static final int FORMAT_PARAM_ARGS_FIELD_NUMBER = 3;
                    private static volatile Parser<FormatParam> PARSER = null;
                    public static final int TEXT_FIELD_NUMBER = 1;
                    public static final int TRUNCATE_LOCATION_FIELD_NUMBER = 2;
                    public static final int UPDATE_TIME_LOCALLY_FIELD_NUMBER = 4;
                    private int bitField0_;
                    private int formatParamArgs_;
                    private String text_ = "";
                    private int truncateLocation_;
                    private boolean updateTimeLocally_;

                    private FormatParam() {
                    }

                    public enum FormatParamArgs implements Internal.EnumLite {
                        UNDEFINED(0),
                        EVENT_START_TIME(1),
                        EVENT_END_TIME(2),
                        FIXED_STRING(3);
                        
                        private final int value;

                        @Override // com.google.protobuf.Internal.EnumLite
                        public final int getNumber() {
                            return this.value;
                        }

                        public static FormatParamArgs forNumber(int i) {
                            if (i == 0) {
                                return UNDEFINED;
                            }
                            if (i == 1) {
                                return EVENT_START_TIME;
                            }
                            if (i == 2) {
                                return EVENT_END_TIME;
                            }
                            if (i != 3) {
                                return null;
                            }
                            return FIXED_STRING;
                        }

                        public static Internal.EnumVerifier internalGetVerifier() {
                            return FormatParamArgsVerifier.INSTANCE;
                        }

                        /* access modifiers changed from: private */
                        public static final class FormatParamArgsVerifier implements Internal.EnumVerifier {
                            static final Internal.EnumVerifier INSTANCE = new FormatParamArgsVerifier();

                            private FormatParamArgsVerifier() {
                            }

                            @Override // com.google.protobuf.Internal.EnumVerifier
                            public boolean isInRange(int i) {
                                return FormatParamArgs.forNumber(i) != null;
                            }
                        }

                        private FormatParamArgs(int i) {
                            this.value = i;
                        }
                    }

                    public String getText() {
                        return this.text_;
                    }

                    public TruncateLocation getTruncateLocation() {
                        TruncateLocation forNumber = TruncateLocation.forNumber(this.truncateLocation_);
                        return forNumber == null ? TruncateLocation.UNSPECIFIED : forNumber;
                    }

                    public FormatParamArgs getFormatParamArgs() {
                        FormatParamArgs forNumber = FormatParamArgs.forNumber(this.formatParamArgs_);
                        return forNumber == null ? FormatParamArgs.UNDEFINED : forNumber;
                    }

                    public static final class Builder extends GeneratedMessageLite.Builder<FormatParam, Builder> implements Object {
                        /* synthetic */ Builder(SmartspaceProto$1 smartspaceProto$1) {
                            this();
                        }

                        private Builder() {
                            super(FormatParam.DEFAULT_INSTANCE);
                        }
                    }

                    /* access modifiers changed from: protected */
                    @Override // com.google.protobuf.GeneratedMessageLite
                    public final Object dynamicMethod(GeneratedMessageLite.MethodToInvoke methodToInvoke, Object obj, Object obj2) {
                        switch (SmartspaceProto$1.$SwitchMap$com$google$protobuf$GeneratedMessageLite$MethodToInvoke[methodToInvoke.ordinal()]) {
                            case 1:
                                return new FormatParam();
                            case 2:
                                return new Builder(null);
                            case 3:
                                return GeneratedMessageLite.newMessageInfo(DEFAULT_INSTANCE, "\u0001\u0004\u0000\u0001\u0001\u0004\u0004\u0000\u0000\u0000\u0001\b\u0000\u0002\f\u0001\u0003\f\u0002\u0004\u0007\u0003", new Object[]{"bitField0_", "text_", "truncateLocation_", TruncateLocation.internalGetVerifier(), "formatParamArgs_", FormatParamArgs.internalGetVerifier(), "updateTimeLocally_"});
                            case 4:
                                return DEFAULT_INSTANCE;
                            case 5:
                                Parser<FormatParam> parser = PARSER;
                                if (parser == null) {
                                    synchronized (FormatParam.class) {
                                        parser = PARSER;
                                        if (parser == null) {
                                            parser = new GeneratedMessageLite.DefaultInstanceBasedParser<>(DEFAULT_INSTANCE);
                                            PARSER = parser;
                                        }
                                    }
                                }
                                return parser;
                            case 6:
                                return (byte) 1;
                            case 7:
                                return null;
                            default:
                                throw new UnsupportedOperationException();
                        }
                    }

                    static {
                        FormatParam formatParam = new FormatParam();
                        DEFAULT_INSTANCE = formatParam;
                        GeneratedMessageLite.registerDefaultInstance(FormatParam.class, formatParam);
                    }
                }

                public boolean hasText() {
                    return (this.bitField0_ & 1) != 0;
                }

                public String getText() {
                    return this.text_;
                }

                public TruncateLocation getTruncateLocation() {
                    TruncateLocation forNumber = TruncateLocation.forNumber(this.truncateLocation_);
                    return forNumber == null ? TruncateLocation.UNSPECIFIED : forNumber;
                }

                public List<FormatParam> getFormatParamList() {
                    return this.formatParam_;
                }

                public int getFormatParamCount() {
                    return this.formatParam_.size();
                }

                public FormatParam getFormatParam(int i) {
                    return this.formatParam_.get(i);
                }

                public static final class Builder extends GeneratedMessageLite.Builder<FormattedText, Builder> implements Object {
                    /* synthetic */ Builder(SmartspaceProto$1 smartspaceProto$1) {
                        this();
                    }

                    private Builder() {
                        super(FormattedText.DEFAULT_INSTANCE);
                    }
                }

                /* access modifiers changed from: protected */
                @Override // com.google.protobuf.GeneratedMessageLite
                public final Object dynamicMethod(GeneratedMessageLite.MethodToInvoke methodToInvoke, Object obj, Object obj2) {
                    switch (SmartspaceProto$1.$SwitchMap$com$google$protobuf$GeneratedMessageLite$MethodToInvoke[methodToInvoke.ordinal()]) {
                        case 1:
                            return new FormattedText();
                        case 2:
                            return new Builder(null);
                        case 3:
                            return GeneratedMessageLite.newMessageInfo(DEFAULT_INSTANCE, "\u0001\u0003\u0000\u0001\u0001\u0003\u0003\u0000\u0001\u0000\u0001\b\u0000\u0002\f\u0001\u0003\u001b", new Object[]{"bitField0_", "text_", "truncateLocation_", TruncateLocation.internalGetVerifier(), "formatParam_", FormatParam.class});
                        case 4:
                            return DEFAULT_INSTANCE;
                        case 5:
                            Parser<FormattedText> parser = PARSER;
                            if (parser == null) {
                                synchronized (FormattedText.class) {
                                    parser = PARSER;
                                    if (parser == null) {
                                        parser = new GeneratedMessageLite.DefaultInstanceBasedParser<>(DEFAULT_INSTANCE);
                                        PARSER = parser;
                                    }
                                }
                            }
                            return parser;
                        case 6:
                            return (byte) 1;
                        case 7:
                            return null;
                        default:
                            throw new UnsupportedOperationException();
                    }
                }

                static {
                    FormattedText formattedText = new FormattedText();
                    DEFAULT_INSTANCE = formattedText;
                    GeneratedMessageLite.registerDefaultInstance(FormattedText.class, formattedText);
                }

                public static FormattedText getDefaultInstance() {
                    return DEFAULT_INSTANCE;
                }
            }

            public boolean hasTitle() {
                return (this.bitField0_ & 1) != 0;
            }

            public FormattedText getTitle() {
                FormattedText formattedText = this.title_;
                return formattedText == null ? FormattedText.getDefaultInstance() : formattedText;
            }

            public boolean hasSubtitle() {
                return (this.bitField0_ & 2) != 0;
            }

            public FormattedText getSubtitle() {
                FormattedText formattedText = this.subtitle_;
                return formattedText == null ? FormattedText.getDefaultInstance() : formattedText;
            }

            public static final class Builder extends GeneratedMessageLite.Builder<Message, Builder> implements Object {
                /* synthetic */ Builder(SmartspaceProto$1 smartspaceProto$1) {
                    this();
                }

                private Builder() {
                    super(Message.DEFAULT_INSTANCE);
                }
            }

            /* access modifiers changed from: protected */
            @Override // com.google.protobuf.GeneratedMessageLite
            public final Object dynamicMethod(GeneratedMessageLite.MethodToInvoke methodToInvoke, Object obj, Object obj2) {
                switch (SmartspaceProto$1.$SwitchMap$com$google$protobuf$GeneratedMessageLite$MethodToInvoke[methodToInvoke.ordinal()]) {
                    case 1:
                        return new Message();
                    case 2:
                        return new Builder(null);
                    case 3:
                        return GeneratedMessageLite.newMessageInfo(DEFAULT_INSTANCE, "\u0001\u0002\u0000\u0001\u0001\u0002\u0002\u0000\u0000\u0000\u0001\t\u0000\u0002\t\u0001", new Object[]{"bitField0_", "title_", "subtitle_"});
                    case 4:
                        return DEFAULT_INSTANCE;
                    case 5:
                        Parser<Message> parser = PARSER;
                        if (parser == null) {
                            synchronized (Message.class) {
                                parser = PARSER;
                                if (parser == null) {
                                    parser = new GeneratedMessageLite.DefaultInstanceBasedParser<>(DEFAULT_INSTANCE);
                                    PARSER = parser;
                                }
                            }
                        }
                        return parser;
                    case 6:
                        return (byte) 1;
                    case 7:
                        return null;
                    default:
                        throw new UnsupportedOperationException();
                }
            }

            static {
                Message message = new Message();
                DEFAULT_INSTANCE = message;
                GeneratedMessageLite.registerDefaultInstance(Message.class, message);
            }

            public static Message getDefaultInstance() {
                return DEFAULT_INSTANCE;
            }
        }

        public static final class Image extends GeneratedMessageLite<Image, Builder> implements Object {
            public static final int CONTENT_DESCRIPTION_FIELD_NUMBER = 4;
            private static final Image DEFAULT_INSTANCE;
            public static final int GSA_RESOURCE_NAME_FIELD_NUMBER = 2;
            public static final int KEY_FIELD_NUMBER = 1;
            private static volatile Parser<Image> PARSER = null;
            public static final int URI_FIELD_NUMBER = 3;
            private int bitField0_;
            private String contentDescription_ = "";
            private String gsaResourceName_ = "";
            private String key_ = "";
            private String uri_ = "";

            private Image() {
            }

            public String getGsaResourceName() {
                return this.gsaResourceName_;
            }

            public String getUri() {
                return this.uri_;
            }

            public boolean hasContentDescription() {
                return (this.bitField0_ & 8) != 0;
            }

            public String getContentDescription() {
                return this.contentDescription_;
            }

            public static final class Builder extends GeneratedMessageLite.Builder<Image, Builder> implements Object {
                /* synthetic */ Builder(SmartspaceProto$1 smartspaceProto$1) {
                    this();
                }

                private Builder() {
                    super(Image.DEFAULT_INSTANCE);
                }
            }

            /* access modifiers changed from: protected */
            @Override // com.google.protobuf.GeneratedMessageLite
            public final Object dynamicMethod(GeneratedMessageLite.MethodToInvoke methodToInvoke, Object obj, Object obj2) {
                switch (SmartspaceProto$1.$SwitchMap$com$google$protobuf$GeneratedMessageLite$MethodToInvoke[methodToInvoke.ordinal()]) {
                    case 1:
                        return new Image();
                    case 2:
                        return new Builder(null);
                    case 3:
                        return GeneratedMessageLite.newMessageInfo(DEFAULT_INSTANCE, "\u0001\u0004\u0000\u0001\u0001\u0004\u0004\u0000\u0000\u0000\u0001\b\u0000\u0002\b\u0001\u0003\b\u0002\u0004\b\u0003", new Object[]{"bitField0_", "key_", "gsaResourceName_", "uri_", "contentDescription_"});
                    case 4:
                        return DEFAULT_INSTANCE;
                    case 5:
                        Parser<Image> parser = PARSER;
                        if (parser == null) {
                            synchronized (Image.class) {
                                parser = PARSER;
                                if (parser == null) {
                                    parser = new GeneratedMessageLite.DefaultInstanceBasedParser<>(DEFAULT_INSTANCE);
                                    PARSER = parser;
                                }
                            }
                        }
                        return parser;
                    case 6:
                        return (byte) 1;
                    case 7:
                        return null;
                    default:
                        throw new UnsupportedOperationException();
                }
            }

            static {
                Image image = new Image();
                DEFAULT_INSTANCE = image;
                GeneratedMessageLite.registerDefaultInstance(Image.class, image);
            }

            public static Image getDefaultInstance() {
                return DEFAULT_INSTANCE;
            }
        }

        public static final class TapAction extends GeneratedMessageLite<TapAction, Builder> implements Object {
            public static final int ACTION_TYPE_FIELD_NUMBER = 1;
            private static final TapAction DEFAULT_INSTANCE;
            public static final int INTENT_FIELD_NUMBER = 2;
            private static volatile Parser<TapAction> PARSER;
            private int actionType_;
            private int bitField0_;
            private String intent_ = "";

            private TapAction() {
            }

            public enum ActionType implements Internal.EnumLite {
                UNDEFINED(0),
                BROADCAST(1),
                START_ACTIVITY(2);
                
                private final int value;

                @Override // com.google.protobuf.Internal.EnumLite
                public final int getNumber() {
                    return this.value;
                }

                public static ActionType forNumber(int i) {
                    if (i == 0) {
                        return UNDEFINED;
                    }
                    if (i == 1) {
                        return BROADCAST;
                    }
                    if (i != 2) {
                        return null;
                    }
                    return START_ACTIVITY;
                }

                public static Internal.EnumVerifier internalGetVerifier() {
                    return ActionTypeVerifier.INSTANCE;
                }

                /* access modifiers changed from: private */
                public static final class ActionTypeVerifier implements Internal.EnumVerifier {
                    static final Internal.EnumVerifier INSTANCE = new ActionTypeVerifier();

                    private ActionTypeVerifier() {
                    }

                    @Override // com.google.protobuf.Internal.EnumVerifier
                    public boolean isInRange(int i) {
                        return ActionType.forNumber(i) != null;
                    }
                }

                private ActionType(int i) {
                    this.value = i;
                }
            }

            public ActionType getActionType() {
                ActionType forNumber = ActionType.forNumber(this.actionType_);
                return forNumber == null ? ActionType.UNDEFINED : forNumber;
            }

            public String getIntent() {
                return this.intent_;
            }

            public static final class Builder extends GeneratedMessageLite.Builder<TapAction, Builder> implements Object {
                /* synthetic */ Builder(SmartspaceProto$1 smartspaceProto$1) {
                    this();
                }

                private Builder() {
                    super(TapAction.DEFAULT_INSTANCE);
                }
            }

            /* access modifiers changed from: protected */
            @Override // com.google.protobuf.GeneratedMessageLite
            public final Object dynamicMethod(GeneratedMessageLite.MethodToInvoke methodToInvoke, Object obj, Object obj2) {
                switch (SmartspaceProto$1.$SwitchMap$com$google$protobuf$GeneratedMessageLite$MethodToInvoke[methodToInvoke.ordinal()]) {
                    case 1:
                        return new TapAction();
                    case 2:
                        return new Builder(null);
                    case 3:
                        return GeneratedMessageLite.newMessageInfo(DEFAULT_INSTANCE, "\u0001\u0002\u0000\u0001\u0001\u0002\u0002\u0000\u0000\u0000\u0001\f\u0000\u0002\b\u0001", new Object[]{"bitField0_", "actionType_", ActionType.internalGetVerifier(), "intent_"});
                    case 4:
                        return DEFAULT_INSTANCE;
                    case 5:
                        Parser<TapAction> parser = PARSER;
                        if (parser == null) {
                            synchronized (TapAction.class) {
                                parser = PARSER;
                                if (parser == null) {
                                    parser = new GeneratedMessageLite.DefaultInstanceBasedParser<>(DEFAULT_INSTANCE);
                                    PARSER = parser;
                                }
                            }
                        }
                        return parser;
                    case 6:
                        return (byte) 1;
                    case 7:
                        return null;
                    default:
                        throw new UnsupportedOperationException();
                }
            }

            static {
                TapAction tapAction = new TapAction();
                DEFAULT_INSTANCE = tapAction;
                GeneratedMessageLite.registerDefaultInstance(TapAction.class, tapAction);
            }

            public static TapAction getDefaultInstance() {
                return DEFAULT_INSTANCE;
            }
        }

        public static final class ExpiryCriteria extends GeneratedMessageLite<ExpiryCriteria, Builder> implements Object {
            private static final ExpiryCriteria DEFAULT_INSTANCE;
            public static final int EXPIRATION_TIME_MILLIS_FIELD_NUMBER = 1;
            public static final int MAX_IMPRESSIONS_FIELD_NUMBER = 2;
            private static volatile Parser<ExpiryCriteria> PARSER;
            private int bitField0_;
            private long expirationTimeMillis_;
            private int maxImpressions_;

            private ExpiryCriteria() {
            }

            public long getExpirationTimeMillis() {
                return this.expirationTimeMillis_;
            }

            public static final class Builder extends GeneratedMessageLite.Builder<ExpiryCriteria, Builder> implements Object {
                /* synthetic */ Builder(SmartspaceProto$1 smartspaceProto$1) {
                    this();
                }

                private Builder() {
                    super(ExpiryCriteria.DEFAULT_INSTANCE);
                }
            }

            /* access modifiers changed from: protected */
            @Override // com.google.protobuf.GeneratedMessageLite
            public final Object dynamicMethod(GeneratedMessageLite.MethodToInvoke methodToInvoke, Object obj, Object obj2) {
                switch (SmartspaceProto$1.$SwitchMap$com$google$protobuf$GeneratedMessageLite$MethodToInvoke[methodToInvoke.ordinal()]) {
                    case 1:
                        return new ExpiryCriteria();
                    case 2:
                        return new Builder(null);
                    case 3:
                        return GeneratedMessageLite.newMessageInfo(DEFAULT_INSTANCE, "\u0001\u0002\u0000\u0001\u0001\u0002\u0002\u0000\u0000\u0000\u0001\u0002\u0000\u0002\u0004\u0001", new Object[]{"bitField0_", "expirationTimeMillis_", "maxImpressions_"});
                    case 4:
                        return DEFAULT_INSTANCE;
                    case 5:
                        Parser<ExpiryCriteria> parser = PARSER;
                        if (parser == null) {
                            synchronized (ExpiryCriteria.class) {
                                parser = PARSER;
                                if (parser == null) {
                                    parser = new GeneratedMessageLite.DefaultInstanceBasedParser<>(DEFAULT_INSTANCE);
                                    PARSER = parser;
                                }
                            }
                        }
                        return parser;
                    case 6:
                        return (byte) 1;
                    case 7:
                        return null;
                    default:
                        throw new UnsupportedOperationException();
                }
            }

            static {
                ExpiryCriteria expiryCriteria = new ExpiryCriteria();
                DEFAULT_INSTANCE = expiryCriteria;
                GeneratedMessageLite.registerDefaultInstance(ExpiryCriteria.class, expiryCriteria);
            }

            public static ExpiryCriteria getDefaultInstance() {
                return DEFAULT_INSTANCE;
            }
        }

        public boolean hasShouldDiscard() {
            return (this.bitField0_ & 1) != 0;
        }

        public boolean getShouldDiscard() {
            return this.shouldDiscard_;
        }

        public CardPriority getCardPriority() {
            CardPriority forNumber = CardPriority.forNumber(this.cardPriority_);
            return forNumber == null ? CardPriority.PRIORITY_UNDEFINED : forNumber;
        }

        public Message getPreEvent() {
            Message message = this.preEvent_;
            return message == null ? Message.getDefaultInstance() : message;
        }

        public Message getDuringEvent() {
            Message message = this.duringEvent_;
            return message == null ? Message.getDefaultInstance() : message;
        }

        public Message getPostEvent() {
            Message message = this.postEvent_;
            return message == null ? Message.getDefaultInstance() : message;
        }

        public boolean hasIcon() {
            return (this.bitField0_ & 512) != 0;
        }

        public Image getIcon() {
            Image image = this.icon_;
            return image == null ? Image.getDefaultInstance() : image;
        }

        public CardType getCardType() {
            CardType forNumber = CardType.forNumber(this.cardType_);
            return forNumber == null ? CardType.UNDEFINED : forNumber;
        }

        public boolean hasTapAction() {
            return (this.bitField0_ & 2048) != 0;
        }

        public TapAction getTapAction() {
            TapAction tapAction = this.tapAction_;
            return tapAction == null ? TapAction.getDefaultInstance() : tapAction;
        }

        public long getEventTimeMillis() {
            return this.eventTimeMillis_;
        }

        public long getEventDurationMillis() {
            return this.eventDurationMillis_;
        }

        public ExpiryCriteria getExpiryCriteria() {
            ExpiryCriteria expiryCriteria = this.expiryCriteria_;
            return expiryCriteria == null ? ExpiryCriteria.getDefaultInstance() : expiryCriteria;
        }

        public static Builder newBuilder() {
            return DEFAULT_INSTANCE.createBuilder();
        }

        public static final class Builder extends GeneratedMessageLite.Builder<SmartspaceCard, Builder> implements Object {
            /* synthetic */ Builder(SmartspaceProto$1 smartspaceProto$1) {
                this();
            }

            private Builder() {
                super(SmartspaceCard.DEFAULT_INSTANCE);
            }
        }

        /* access modifiers changed from: protected */
        @Override // com.google.protobuf.GeneratedMessageLite
        public final Object dynamicMethod(GeneratedMessageLite.MethodToInvoke methodToInvoke, Object obj, Object obj2) {
            switch (SmartspaceProto$1.$SwitchMap$com$google$protobuf$GeneratedMessageLite$MethodToInvoke[methodToInvoke.ordinal()]) {
                case 1:
                    return new SmartspaceCard();
                case 2:
                    return new Builder(null);
                case 3:
                    return GeneratedMessageLite.newMessageInfo(DEFAULT_INSTANCE, "\u0001\u0013\u0000\u0001\u0001\u0013\u0013\u0000\u0000\u0000\u0001\u0007\u0000\u0002\u0004\u0002\u0003\t\u0003\u0004\t\u0004\u0005\t\u0005\u0006\t\t\u0007\f\n\b\t\u000b\t\u0002\f\n\u0002\r\u000b\u0002\u000e\f\t\u000f\r\f\u0001\u000e\t\u0006\u000f\t\u0007\u0010\t\b\u0011\u0007\u0010\u0012\u0007\u0011\u0013\u0007\u0012", new Object[]{"bitField0_", "shouldDiscard_", "cardId_", "preEvent_", "duringEvent_", "postEvent_", "icon_", "cardType_", CardType.internalGetVerifier(), "tapAction_", "updateTimeMillis_", "eventTimeMillis_", "eventDurationMillis_", "expiryCriteria_", "cardPriority_", CardPriority.internalGetVerifier(), "preEventStatic_", "duringEventStatic_", "postEventStatic_", "isSensitive_", "isWorkProfile_", "isDataFrom3PApp_"});
                case 4:
                    return DEFAULT_INSTANCE;
                case 5:
                    Parser<SmartspaceCard> parser = PARSER;
                    if (parser == null) {
                        synchronized (SmartspaceCard.class) {
                            parser = PARSER;
                            if (parser == null) {
                                parser = new GeneratedMessageLite.DefaultInstanceBasedParser<>(DEFAULT_INSTANCE);
                                PARSER = parser;
                            }
                        }
                    }
                    return parser;
                case 6:
                    return (byte) 1;
                case 7:
                    return null;
                default:
                    throw new UnsupportedOperationException();
            }
        }

        static {
            SmartspaceCard smartspaceCard = new SmartspaceCard();
            DEFAULT_INSTANCE = smartspaceCard;
            GeneratedMessageLite.registerDefaultInstance(SmartspaceCard.class, smartspaceCard);
        }
    }

    public List<SmartspaceCard> getCardList() {
        return this.card_;
    }

    public static Builder newBuilder() {
        return DEFAULT_INSTANCE.createBuilder();
    }

    public static final class Builder extends GeneratedMessageLite.Builder<SmartspaceProto$SmartspaceUpdate, Builder> implements Object {
        /* synthetic */ Builder(SmartspaceProto$1 smartspaceProto$1) {
            this();
        }

        private Builder() {
            super(SmartspaceProto$SmartspaceUpdate.DEFAULT_INSTANCE);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.google.protobuf.GeneratedMessageLite
    public final Object dynamicMethod(GeneratedMessageLite.MethodToInvoke methodToInvoke, Object obj, Object obj2) {
        switch (SmartspaceProto$1.$SwitchMap$com$google$protobuf$GeneratedMessageLite$MethodToInvoke[methodToInvoke.ordinal()]) {
            case 1:
                return new SmartspaceProto$SmartspaceUpdate();
            case 2:
                return new Builder(null);
            case 3:
                return GeneratedMessageLite.newMessageInfo(DEFAULT_INSTANCE, "\u0001\u0001\u0000\u0000\u0001\u0001\u0001\u0000\u0001\u0000\u0001\u001b", new Object[]{"card_", SmartspaceCard.class});
            case 4:
                return DEFAULT_INSTANCE;
            case 5:
                Parser<SmartspaceProto$SmartspaceUpdate> parser = PARSER;
                if (parser == null) {
                    synchronized (SmartspaceProto$SmartspaceUpdate.class) {
                        parser = PARSER;
                        if (parser == null) {
                            parser = new GeneratedMessageLite.DefaultInstanceBasedParser<>(DEFAULT_INSTANCE);
                            PARSER = parser;
                        }
                    }
                }
                return parser;
            case 6:
                return (byte) 1;
            case 7:
                return null;
            default:
                throw new UnsupportedOperationException();
        }
    }

    static {
        SmartspaceProto$SmartspaceUpdate smartspaceProto$SmartspaceUpdate = new SmartspaceProto$SmartspaceUpdate();
        DEFAULT_INSTANCE = smartspaceProto$SmartspaceUpdate;
        GeneratedMessageLite.registerDefaultInstance(SmartspaceProto$SmartspaceUpdate.class, smartspaceProto$SmartspaceUpdate);
    }
}
