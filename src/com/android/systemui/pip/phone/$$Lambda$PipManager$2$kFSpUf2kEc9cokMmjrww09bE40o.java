package com.android.systemui.pip.phone;

import com.android.systemui.shared.system.WindowManagerWrapper;
/* compiled from: lambda */
/* renamed from: com.android.systemui.pip.phone.-$$Lambda$PipManager$2$kFSpUf2kEc9cokMmjrww09bE40o  reason: invalid class name */
public final /* synthetic */ class $$Lambda$PipManager$2$kFSpUf2kEc9cokMmjrww09bE40o implements Runnable {
    public static final /* synthetic */ $$Lambda$PipManager$2$kFSpUf2kEc9cokMmjrww09bE40o INSTANCE = new $$Lambda$PipManager$2$kFSpUf2kEc9cokMmjrww09bE40o();

    private /* synthetic */ $$Lambda$PipManager$2$kFSpUf2kEc9cokMmjrww09bE40o() {
    }

    @Override // java.lang.Runnable
    public final void run() {
        WindowManagerWrapper.getInstance().setPipVisibility(true);
    }
}
