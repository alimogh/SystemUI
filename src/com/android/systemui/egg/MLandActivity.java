package com.android.systemui.egg;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0011R$layout;
public class MLandActivity extends Activity {
    MLand mLand;

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(C0011R$layout.mland);
        MLand mLand = (MLand) findViewById(C0008R$id.world);
        this.mLand = mLand;
        mLand.setScoreFieldHolder((ViewGroup) findViewById(C0008R$id.scores));
        this.mLand.setSplash(findViewById(C0008R$id.welcome));
        int size = this.mLand.getGameControllers().size();
        if (size > 0) {
            this.mLand.setupPlayers(size);
        }
    }

    public void updateSplashPlayers() {
        int numPlayers = this.mLand.getNumPlayers();
        View findViewById = findViewById(C0008R$id.player_minus_button);
        View findViewById2 = findViewById(C0008R$id.player_plus_button);
        if (numPlayers == 1) {
            findViewById.setVisibility(4);
            findViewById2.setVisibility(0);
            findViewById2.requestFocus();
        } else if (numPlayers == 6) {
            findViewById.setVisibility(0);
            findViewById2.setVisibility(4);
            findViewById.requestFocus();
        } else {
            findViewById.setVisibility(0);
            findViewById2.setVisibility(0);
        }
    }

    @Override // android.app.Activity
    public void onPause() {
        this.mLand.stop();
        super.onPause();
    }

    @Override // android.app.Activity
    public void onResume() {
        super.onResume();
        this.mLand.onAttachedToWindow();
        updateSplashPlayers();
        this.mLand.showSplash();
    }

    public void playerMinus(View view) {
        this.mLand.removePlayer();
        updateSplashPlayers();
    }

    public void playerPlus(View view) {
        this.mLand.addPlayer();
        updateSplashPlayers();
    }

    public void startButtonPressed(View view) {
        findViewById(C0008R$id.player_minus_button).setVisibility(4);
        findViewById(C0008R$id.player_plus_button).setVisibility(4);
        this.mLand.start(true);
    }
}
