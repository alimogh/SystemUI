package com.oneplus.aod.utils.bitmoji.triggers.base;

import android.app.AlarmManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.AudioPlaybackConfiguration;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import com.android.internal.util.IndentingPrintWriter;
import com.oneplus.aod.utils.bitmoji.OpBitmojiManager;
import java.io.FileDescriptor;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
public abstract class AudioTrigger extends DelayTrigger {
    protected AlarmManager mAlarmManager;
    protected AudioManager mAudioManager;
    private final AudioManager.AudioPlaybackCallback mAudioPlaybackCallback = new AudioManager.AudioPlaybackCallback() { // from class: com.oneplus.aod.utils.bitmoji.triggers.base.AudioTrigger.2
        @Override // android.media.AudioManager.AudioPlaybackCallback
        public void onPlaybackConfigChanged(List<AudioPlaybackConfiguration> list) {
            if (list != null && list.size() > 0) {
                boolean z = false;
                for (AudioPlaybackConfiguration audioPlaybackConfiguration : list) {
                    if (AudioTrigger.this.checkAudioAttributes(audioPlaybackConfiguration.getAudioAttributes())) {
                        if (Build.DEBUG_ONEPLUS) {
                            Log.d(AudioTrigger.this.mTag, "onPlaybackConfigChanged: " + audioPlaybackConfiguration);
                        }
                        boolean checkConfigIsActive = AudioTrigger.this.checkConfigIsActive(audioPlaybackConfiguration);
                        AudioTrigger.this.setPlaybackStateActive(checkConfigIsActive);
                        if (checkConfigIsActive) {
                            AudioTrigger.this.cancelDelayToCheckStop();
                            AudioTrigger audioTrigger = AudioTrigger.this;
                            if (audioTrigger.mPlayStart == -1) {
                                if (Build.DEBUG_ONEPLUS) {
                                    Log.d(audioTrigger.mTag, "start playing");
                                }
                                AudioTrigger.this.mPlayStart = SystemClock.elapsedRealtime();
                                AudioTrigger.this.startCountToCheckPlay();
                                return;
                            } else if (Build.DEBUG_ONEPLUS) {
                                Log.d(audioTrigger.mTag, "already been played");
                                return;
                            } else {
                                return;
                            }
                        } else {
                            z = true;
                        }
                    }
                }
                if (z) {
                    AudioTrigger.this.setPlaybackStateActive(false);
                    AudioTrigger.this.startDelayToCheckIfStop();
                    return;
                }
            }
            AudioTrigger.this.setPlaybackStateActive(false);
            AudioTrigger.this.cancelDelayToCheckStop();
            AudioTrigger.this.audioStopOrPause();
        }
    };
    protected Runnable mCheckStartRunnable = new Runnable() { // from class: com.oneplus.aod.utils.bitmoji.triggers.base.-$$Lambda$Cd2L1jcyOZhjAEhNmww-TiuYazQ
        @Override // java.lang.Runnable
        public final void run() {
            AudioTrigger.this.checkAudioActive();
        }
    };
    protected Runnable mCheckStopRunnable = new Runnable() { // from class: com.oneplus.aod.utils.bitmoji.triggers.base.-$$Lambda$u-5f3jXsHJF4qlnXbdMxHvnd3m0
        @Override // java.lang.Runnable
        public final void run() {
            AudioTrigger.this.checkStopInner();
        }
    };
    private final AlarmManager.OnAlarmListener mCountToCheckPlay = new AlarmManager.OnAlarmListener() { // from class: com.oneplus.aod.utils.bitmoji.triggers.base.AudioTrigger.1
        @Override // android.app.AlarmManager.OnAlarmListener
        public void onAlarm() {
            AudioTrigger.this.checkAudioActive();
        }
    };
    protected int mDuration = 0;
    protected long mPlayStart = -1;
    protected AtomicBoolean mPlaybackStateActive = new AtomicBoolean(false);

    /* access modifiers changed from: protected */
    public abstract boolean checkAudioAttributes(AudioAttributes audioAttributes);

    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.CategoryTrigger, com.oneplus.aod.utils.bitmoji.triggers.base.Trigger
    public int getPriority() {
        return 3;
    }

    /* access modifiers changed from: protected */
    public abstract String tagStartCountToCheckPlay();

    public AudioTrigger(Context context, OpBitmojiManager opBitmojiManager) {
        super(context, opBitmojiManager);
        this.mAlarmManager = (AlarmManager) context.getSystemService(AlarmManager.class);
        this.mAudioManager = (AudioManager) context.getSystemService("audio");
        if (inCondition()) {
            if (Build.DEBUG_ONEPLUS) {
                Log.d(this.mTag, "audio is active");
            }
            this.mPlayStart = SystemClock.elapsedRealtime();
        }
    }

    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.DelayTrigger, com.oneplus.aod.utils.bitmoji.triggers.base.Trigger
    public void init() {
        super.init();
        this.mAudioManager.registerAudioPlaybackCallback(this.mAudioPlaybackCallback, this.mBitmojiManager.getHandler());
    }

    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.DelayTrigger
    public boolean isActiveInner() {
        if (this.mPlayStart == -1 || SystemClock.elapsedRealtime() - this.mPlayStart < ((long) this.mDuration)) {
            return false;
        }
        return true;
    }

    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.Trigger
    public void dynamicConfig(String[] strArr) {
        if (strArr == null || strArr.length != 3) {
            Log.w(this.mTag, "dynamicConfig: args error");
            return;
        }
        try {
            this.mDuration = Integer.parseInt(strArr[2]);
            if (this.mPlayStart == -1) {
                return;
            }
            if (isActiveInner()) {
                checkAudioActive();
            } else {
                this.mBitmojiManager.onTriggerChanged(getTriggerId(), false);
            }
        } catch (Exception e) {
            Log.e(this.mTag, "dynamicConfig: occur error", e);
        }
    }

    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.DelayTrigger, com.oneplus.aod.utils.bitmoji.triggers.base.CategoryTrigger, com.oneplus.aod.utils.bitmoji.triggers.base.Trigger
    public void dumpDetail(FileDescriptor fileDescriptor, IndentingPrintWriter indentingPrintWriter, String[] strArr) {
        super.dumpDetail(fileDescriptor, indentingPrintWriter, strArr);
        indentingPrintWriter.println("duration=" + this.mDuration);
        indentingPrintWriter.println("startTime=" + this.mPlayStart);
        if (this.mPlayStart != -1) {
            indentingPrintWriter.println("currentTime=" + SystemClock.elapsedRealtime());
        }
        indentingPrintWriter.println("isAudioActive=" + isAudioActive());
    }

    /* access modifiers changed from: protected */
    public boolean inCondition() {
        return isAudioActive();
    }

    /* access modifiers changed from: protected */
    public void checkAudioActive() {
        if (this.mPlayStart == -1) {
            Log.d(this.mTag, "not playing yet");
            return;
        }
        boolean isAudioActive = isAudioActive();
        if (Build.DEBUG_ONEPLUS) {
            String str = this.mTag;
            Log.d(str, "checkAudioActive: startTime= " + this.mPlayStart + ", current= " + SystemClock.elapsedRealtime() + ", duration= " + this.mDuration + ", isAudioActive= " + isAudioActive);
        }
        onTriggerChanged(getTriggerId(), isAudioActive);
    }

    /* access modifiers changed from: protected */
    public boolean isAudioActive() {
        return this.mAudioManager.isMusicActive();
    }

    /* access modifiers changed from: protected */
    public void audioStopOrPause() {
        if (this.mPlayStart != -1) {
            if (Build.DEBUG_ONEPLUS) {
                Log.d(this.mTag, "audio stop or pause");
            }
            this.mPlayStart = -1;
            cancelCountToCheckPlay();
            onTriggerChanged(getTriggerId(), false);
        }
    }

    /* access modifiers changed from: protected */
    public void cancelCountToCheckPlay() {
        if (this.mDuration > 0) {
            this.mAlarmManager.cancel(this.mCountToCheckPlay);
        } else {
            getHandler().removeCallbacks(this.mCheckStartRunnable);
        }
    }

    /* access modifiers changed from: protected */
    public void startCountToCheckPlay() {
        cancelCountToCheckPlay();
        if (this.mDuration > 0) {
            this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + ((long) this.mDuration), tagStartCountToCheckPlay(), this.mCountToCheckPlay, this.mBitmojiManager.getHandler());
            return;
        }
        acquireWakeLock(500);
        getHandler().postDelayed(this.mCheckStartRunnable, 500);
    }

    /* access modifiers changed from: protected */
    public void cancelDelayToCheckStop() {
        getHandler().removeCallbacks(this.mCheckStopRunnable);
    }

    /* access modifiers changed from: protected */
    public void startDelayToCheckIfStop() {
        if (Build.DEBUG_ONEPLUS) {
            String str = this.mTag;
            Log.d(str, "startDelayToCheckIfStop: mPlayStart= " + this.mPlayStart);
        }
        if (this.mPlayStart != -1) {
            cancelDelayToCheckStop();
            acquireWakeLock(500);
            getHandler().postDelayed(this.mCheckStopRunnable, 500);
        }
    }

    /* access modifiers changed from: protected */
    public void checkStopInner() {
        boolean isAudioActive = isAudioActive();
        if (Build.DEBUG_ONEPLUS) {
            String str = this.mTag;
            Log.d(str, "delay to check if audio is stopped. audioActive= " + isAudioActive + ", mPlayStart= " + this.mPlayStart);
        }
        if (!isAudioActive) {
            audioStopOrPause();
        }
    }

    /* access modifiers changed from: protected */
    public boolean checkConfigIsActive(AudioPlaybackConfiguration audioPlaybackConfiguration) {
        return audioPlaybackConfiguration.isActive();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setPlaybackStateActive(boolean z) {
        this.mPlaybackStateActive.set(z);
    }
}
