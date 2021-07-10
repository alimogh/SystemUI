package com.google.android.libraries.assistant.oemsmartspace.shared;
public interface SmartspaceUpdateListener {
    void onBothCardAndChipShown(int i);

    void onCardShown(int i);

    void onChipShown(int i);

    void onNoCardAndChipShown(int i);
}
