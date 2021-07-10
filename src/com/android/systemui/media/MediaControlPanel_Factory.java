package com.android.systemui.media;

import android.content.Context;
import com.android.systemui.plugins.ActivityStarter;
import dagger.internal.Factory;
import java.util.concurrent.Executor;
import javax.inject.Provider;
public final class MediaControlPanel_Factory implements Factory<MediaControlPanel> {
    private final Provider<ActivityStarter> activityStarterProvider;
    private final Provider<Executor> backgroundExecutorProvider;
    private final Provider<Context> contextProvider;
    private final Provider<MediaViewController> mediaViewControllerProvider;
    private final Provider<SeekBarViewModel> seekBarViewModelProvider;

    public MediaControlPanel_Factory(Provider<Context> provider, Provider<Executor> provider2, Provider<ActivityStarter> provider3, Provider<MediaViewController> provider4, Provider<SeekBarViewModel> provider5) {
        this.contextProvider = provider;
        this.backgroundExecutorProvider = provider2;
        this.activityStarterProvider = provider3;
        this.mediaViewControllerProvider = provider4;
        this.seekBarViewModelProvider = provider5;
    }

    @Override // javax.inject.Provider
    public MediaControlPanel get() {
        return provideInstance(this.contextProvider, this.backgroundExecutorProvider, this.activityStarterProvider, this.mediaViewControllerProvider, this.seekBarViewModelProvider);
    }

    public static MediaControlPanel provideInstance(Provider<Context> provider, Provider<Executor> provider2, Provider<ActivityStarter> provider3, Provider<MediaViewController> provider4, Provider<SeekBarViewModel> provider5) {
        return new MediaControlPanel(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get());
    }

    public static MediaControlPanel_Factory create(Provider<Context> provider, Provider<Executor> provider2, Provider<ActivityStarter> provider3, Provider<MediaViewController> provider4, Provider<SeekBarViewModel> provider5) {
        return new MediaControlPanel_Factory(provider, provider2, provider3, provider4, provider5);
    }
}
