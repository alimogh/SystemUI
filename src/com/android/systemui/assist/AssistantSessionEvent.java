package com.android.systemui.assist;

import com.android.internal.logging.UiEventLogger;
/* compiled from: AssistantSessionEvent.kt */
public enum AssistantSessionEvent implements UiEventLogger.UiEventEnum {
    /* Fake field, exist only in values array */
    ASSISTANT_SESSION_UNKNOWN(523),
    /* Fake field, exist only in values array */
    ASSISTANT_SESSION_TIMEOUT_DISMISS(524),
    /* Fake field, exist only in values array */
    ASSISTANT_SESSION_INVOCATION_START(525),
    ASSISTANT_SESSION_INVOCATION_CANCELLED(526),
    /* Fake field, exist only in values array */
    ASSISTANT_SESSION_USER_DISMISS(527),
    ASSISTANT_SESSION_UPDATE(528),
    ASSISTANT_SESSION_CLOSE(529);
    
    private final int id;

    private AssistantSessionEvent(int i) {
        this.id = i;
    }

    public int getId() {
        return this.id;
    }
}
