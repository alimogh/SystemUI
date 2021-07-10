package com.android.systemui.qs;

import com.android.systemui.qs.carrier.QSCarrierGroupController$Builder;
public class QuickStatusBarHeaderController {
    private final QuickStatusBarHeader mView;

    private QuickStatusBarHeaderController(QuickStatusBarHeader quickStatusBarHeader, QSCarrierGroupController$Builder qSCarrierGroupController$Builder) {
        this.mView = quickStatusBarHeader;
    }

    public void setListening(boolean z) {
        this.mView.setListening(z);
    }

    public static class Builder {
        private final QSCarrierGroupController$Builder mQSCarrierGroupControllerBuilder;
        private QuickStatusBarHeader mView;

        public Builder(QSCarrierGroupController$Builder qSCarrierGroupController$Builder) {
            this.mQSCarrierGroupControllerBuilder = qSCarrierGroupController$Builder;
        }

        public Builder setQuickStatusBarHeader(QuickStatusBarHeader quickStatusBarHeader) {
            this.mView = quickStatusBarHeader;
            return this;
        }

        public QuickStatusBarHeaderController build() {
            return new QuickStatusBarHeaderController(this.mView, this.mQSCarrierGroupControllerBuilder);
        }
    }
}
