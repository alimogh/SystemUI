package com.oneplus.worklife;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;
public final class OPWLBHelper_Factory implements Factory<OPWLBHelper> {
    private final Provider<Context> contextProvider;

    public OPWLBHelper_Factory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public OPWLBHelper get() {
        return provideInstance(this.contextProvider);
    }

    public static OPWLBHelper provideInstance(Provider<Context> provider) {
        return new OPWLBHelper(provider.get());
    }

    public static OPWLBHelper_Factory create(Provider<Context> provider) {
        return new OPWLBHelper_Factory(provider);
    }
}
