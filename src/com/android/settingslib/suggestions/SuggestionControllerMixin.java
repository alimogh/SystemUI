package com.android.settingslib.suggestions;

import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.service.settings.suggestions.Suggestion;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import java.util.List;
@Deprecated
public class SuggestionControllerMixin implements Object, LifecycleObserver {
    private final Context mContext;
    private final SuggestionControllerHost mHost;
    private final SuggestionController mSuggestionController;

    public interface SuggestionControllerHost {
        void onSuggestionReady(List<Suggestion> list);
    }

    public void onLoaderReset(Loader<List<Suggestion>> loader) {
    }

    public /* bridge */ /* synthetic */ void onLoadFinished(Loader loader, Object obj) {
        onLoadFinished((Loader<List<Suggestion>>) loader, (List) obj);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onStart() {
        this.mSuggestionController.start();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onStop() {
        this.mSuggestionController.stop();
    }

    public Loader<List<Suggestion>> onCreateLoader(int i, Bundle bundle) {
        if (i == 42) {
            return new SuggestionLoader(this.mContext, this.mSuggestionController);
        }
        throw new IllegalArgumentException("This loader id is not supported " + i);
    }

    public void onLoadFinished(Loader<List<Suggestion>> loader, List<Suggestion> list) {
        this.mHost.onSuggestionReady(list);
    }
}
