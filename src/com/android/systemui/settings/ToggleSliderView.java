package com.android.systemui.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import com.android.settingslib.RestrictedLockUtils;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0011R$layout;
import com.android.systemui.R$styleable;
import com.android.systemui.settings.ToggleSlider;
import com.android.systemui.statusbar.policy.BrightnessMirrorController;
public class ToggleSliderView extends RelativeLayout implements ToggleSlider {
    private final CompoundButton.OnCheckedChangeListener mCheckListener;
    private TextView mLabel;
    private ToggleSlider.Listener mListener;
    private ToggleSliderView mMirror;
    private BrightnessMirrorController mMirrorController;
    private final SeekBar.OnSeekBarChangeListener mSeekListener;
    private ToggleSeekBar mSlider;
    private CompoundButton mToggle;
    private boolean mTracking;

    public ToggleSliderView(Context context) {
        this(context, null);
    }

    public ToggleSliderView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ToggleSliderView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mCheckListener = new CompoundButton.OnCheckedChangeListener() { // from class: com.android.systemui.settings.ToggleSliderView.1
            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                ToggleSliderView.this.mSlider.setEnabled(!z);
                if (ToggleSliderView.this.mListener != null) {
                    ToggleSlider.Listener listener = ToggleSliderView.this.mListener;
                    ToggleSliderView toggleSliderView = ToggleSliderView.this;
                    listener.onChanged(toggleSliderView, toggleSliderView.mTracking, z, ToggleSliderView.this.mSlider.getProgress(), false);
                }
                if (ToggleSliderView.this.mMirror != null) {
                    ToggleSliderView.this.mMirror.mToggle.setChecked(z);
                }
            }
        };
        this.mSeekListener = new SeekBar.OnSeekBarChangeListener() { // from class: com.android.systemui.settings.ToggleSliderView.2
            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onProgressChanged(SeekBar seekBar, int i2, boolean z) {
                if (ToggleSliderView.this.mListener != null) {
                    ToggleSlider.Listener listener = ToggleSliderView.this.mListener;
                    ToggleSliderView toggleSliderView = ToggleSliderView.this;
                    listener.onChanged(toggleSliderView, toggleSliderView.mTracking, ToggleSliderView.this.mToggle.isChecked(), i2, false);
                }
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStartTrackingTouch(SeekBar seekBar) {
                ToggleSliderView.this.mTracking = true;
                if (ToggleSliderView.this.mListener != null) {
                    ToggleSlider.Listener listener = ToggleSliderView.this.mListener;
                    ToggleSliderView toggleSliderView = ToggleSliderView.this;
                    listener.onChanged(toggleSliderView, toggleSliderView.mTracking, ToggleSliderView.this.mToggle.isChecked(), ToggleSliderView.this.mSlider.getProgress(), false);
                }
                ToggleSliderView.this.mToggle.setChecked(false);
                if (ToggleSliderView.this.mMirrorController != null) {
                    ToggleSliderView.this.mMirrorController.showMirror();
                    ToggleSliderView.this.mMirrorController.setLocation((View) ToggleSliderView.this.getParent());
                }
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStopTrackingTouch(SeekBar seekBar) {
                ToggleSliderView.this.mTracking = false;
                if (ToggleSliderView.this.mListener != null) {
                    ToggleSlider.Listener listener = ToggleSliderView.this.mListener;
                    ToggleSliderView toggleSliderView = ToggleSliderView.this;
                    listener.onChanged(toggleSliderView, toggleSliderView.mTracking, ToggleSliderView.this.mToggle.isChecked(), ToggleSliderView.this.mSlider.getProgress(), true);
                }
                if (ToggleSliderView.this.mMirrorController != null) {
                    ToggleSliderView.this.mMirrorController.hideMirror();
                    ToggleSliderView.this.mMirrorController.hideMirrorImmediately();
                }
            }
        };
        View.inflate(context, C0011R$layout.status_bar_toggle_slider, this);
        context.getResources();
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.ToggleSliderView, i, 0);
        CompoundButton compoundButton = (CompoundButton) findViewById(C0008R$id.toggle);
        this.mToggle = compoundButton;
        compoundButton.setOnCheckedChangeListener(this.mCheckListener);
        ToggleSeekBar toggleSeekBar = (ToggleSeekBar) findViewById(C0008R$id.slider);
        this.mSlider = toggleSeekBar;
        toggleSeekBar.setOnSeekBarChangeListener(this.mSeekListener);
        TextView textView = (TextView) findViewById(C0008R$id.label);
        this.mLabel = textView;
        textView.setText(obtainStyledAttributes.getString(R$styleable.ToggleSliderView_text));
        this.mSlider.setAccessibilityLabel(getContentDescription().toString());
        obtainStyledAttributes.recycle();
    }

    public void setMirror(ToggleSliderView toggleSliderView) {
        this.mMirror = toggleSliderView;
        if (toggleSliderView != null) {
            toggleSliderView.setChecked(this.mToggle.isChecked());
            this.mMirror.setMax(this.mSlider.getMax());
            this.mMirror.setValue(this.mSlider.getProgress());
        }
    }

    public void setMirrorController(BrightnessMirrorController brightnessMirrorController) {
        this.mMirrorController = brightnessMirrorController;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ToggleSlider.Listener listener = this.mListener;
        if (listener != null) {
            listener.onInit(this);
        }
    }

    public void setEnforcedAdmin(RestrictedLockUtils.EnforcedAdmin enforcedAdmin) {
        boolean z = true;
        this.mToggle.setEnabled(enforcedAdmin == null);
        ToggleSeekBar toggleSeekBar = this.mSlider;
        if (enforcedAdmin != null) {
            z = false;
        }
        toggleSeekBar.setEnabled(z);
        this.mSlider.setEnforcedAdmin(enforcedAdmin);
    }

    @Override // com.android.systemui.settings.ToggleSlider
    public void setOnChangedListener(ToggleSlider.Listener listener) {
        this.mListener = listener;
    }

    @Override // com.android.systemui.settings.ToggleSlider
    public void setChecked(boolean z) {
        this.mToggle.setChecked(z);
    }

    @Override // com.android.systemui.settings.ToggleSlider
    public void setMax(int i) {
        this.mSlider.setMax(i);
        ToggleSliderView toggleSliderView = this.mMirror;
        if (toggleSliderView != null) {
            toggleSliderView.setMax(i);
        }
    }

    @Override // com.android.systemui.settings.ToggleSlider
    public void setValue(int i) {
        this.mSlider.setProgress(i);
        ToggleSliderView toggleSliderView = this.mMirror;
        if (toggleSliderView != null) {
            toggleSliderView.setValue(i);
        }
    }

    @Override // com.android.systemui.settings.ToggleSlider
    public int getValue() {
        return this.mSlider.getProgress();
    }

    @Override // android.view.View, android.view.ViewGroup
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (this.mMirror != null) {
            MotionEvent copy = motionEvent.copy();
            this.mMirror.dispatchTouchEvent(copy);
            copy.recycle();
        }
        return super.dispatchTouchEvent(motionEvent);
    }
}
