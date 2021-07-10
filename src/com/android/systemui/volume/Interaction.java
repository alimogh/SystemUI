package com.android.systemui.volume;

import android.view.MotionEvent;
import android.view.View;
public class Interaction {

    public interface Callback {
        void onInteraction();
    }

    public static void register(View view, final Callback callback) {
        view.setOnTouchListener(new View.OnTouchListener() { // from class: com.android.systemui.volume.Interaction.1
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view2, MotionEvent motionEvent) {
                Callback.this.onInteraction();
                return false;
            }
        });
        view.setOnGenericMotionListener(new View.OnGenericMotionListener() { // from class: com.android.systemui.volume.Interaction.2
            @Override // android.view.View.OnGenericMotionListener
            public boolean onGenericMotion(View view2, MotionEvent motionEvent) {
                Callback.this.onInteraction();
                return false;
            }
        });
    }
}
