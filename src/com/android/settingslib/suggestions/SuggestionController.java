package com.android.settingslib.suggestions;

import android.service.settings.suggestions.Suggestion;
import java.util.List;
public class SuggestionController {
    public abstract List<Suggestion> getSuggestions();

    public abstract void start();

    public abstract void stop();
}
