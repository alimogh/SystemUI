package com.oneplus.systemui.statusbar.phone;

import dagger.internal.Factory;
public final class OpHighlightHintControllerImpl_Factory implements Factory<OpHighlightHintControllerImpl> {
    private static final OpHighlightHintControllerImpl_Factory INSTANCE = new OpHighlightHintControllerImpl_Factory();

    @Override // javax.inject.Provider
    public OpHighlightHintControllerImpl get() {
        return provideInstance();
    }

    public static OpHighlightHintControllerImpl provideInstance() {
        return new OpHighlightHintControllerImpl();
    }

    public static OpHighlightHintControllerImpl_Factory create() {
        return INSTANCE;
    }
}
