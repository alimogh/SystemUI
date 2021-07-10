package com.android.systemui.qs;

import com.android.internal.logging.UiEventLogger;
/* compiled from: QSEvents.kt */
public enum QSEditEvent implements UiEventLogger.UiEventEnum {
    /* Fake field, exist only in values array */
    QS_EDIT_REMOVE(210),
    /* Fake field, exist only in values array */
    QS_EDIT_ADD(211),
    /* Fake field, exist only in values array */
    QS_EDIT_MOVE(212),
    QS_EDIT_OPEN(213),
    QS_EDIT_CLOSED(214),
    QS_EDIT_RESET(215);
    
    private final int _id;

    private QSEditEvent(int i) {
        this._id = i;
    }

    public int getId() {
        return this._id;
    }
}
