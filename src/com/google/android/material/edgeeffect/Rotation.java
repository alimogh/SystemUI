package com.google.android.material.edgeeffect;
public enum Rotation {
    LANDSCAPE(270),
    INVERSE_LANDSCAPE(90),
    PORTRAIT(0),
    INVERSE_PORTRAIT(180);
    
    private final int m_DeviceOrientation;

    private Rotation(int i) {
        this.m_DeviceOrientation = i;
    }

    public static Rotation fromScreenOrientation(int i) {
        Rotation rotation = PORTRAIT;
        if (i == 0) {
            return LANDSCAPE;
        }
        if (i == 1) {
            return rotation;
        }
        if (i == 8) {
            return INVERSE_LANDSCAPE;
        }
        if (i != 9) {
            return rotation;
        }
        return INVERSE_PORTRAIT;
    }

    public int getDeviceOrientation() {
        return this.m_DeviceOrientation;
    }

    /* renamed from: com.google.android.material.edgeeffect.Rotation$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$google$android$material$edgeeffect$Rotation;

        static {
            int[] iArr = new int[Rotation.values().length];
            $SwitchMap$com$google$android$material$edgeeffect$Rotation = iArr;
            try {
                iArr[Rotation.INVERSE_PORTRAIT.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$google$android$material$edgeeffect$Rotation[Rotation.INVERSE_LANDSCAPE.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$google$android$material$edgeeffect$Rotation[Rotation.LANDSCAPE.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$google$android$material$edgeeffect$Rotation[Rotation.PORTRAIT.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
        }
    }

    public boolean isLandscape() {
        int i = AnonymousClass1.$SwitchMap$com$google$android$material$edgeeffect$Rotation[ordinal()];
        return i == 2 || i == 3;
    }
}
