package com.android.systemui.qs;

import com.android.systemui.C0008R$id;
import com.android.systemui.qs.QuickStatusBarHeaderController;
public class QSContainerImplController {
    private final QuickStatusBarHeaderController mQuickStatusBarHeaderController;
    private final QSContainerImpl mView;

    private QSContainerImplController(QSContainerImpl qSContainerImpl, QuickStatusBarHeaderController.Builder builder) {
        this.mView = qSContainerImpl;
        builder.setQuickStatusBarHeader((QuickStatusBarHeader) qSContainerImpl.findViewById(C0008R$id.header));
        this.mQuickStatusBarHeaderController = builder.build();
    }

    public void setListening(boolean z) {
        this.mQuickStatusBarHeaderController.setListening(z);
    }

    public static class Builder {
        private final QuickStatusBarHeaderController.Builder mQuickStatusBarHeaderControllerBuilder;
        private QSContainerImpl mView;

        public Builder(QuickStatusBarHeaderController.Builder builder) {
            this.mQuickStatusBarHeaderControllerBuilder = builder;
        }

        public Builder setQSContainerImpl(QSContainerImpl qSContainerImpl) {
            this.mView = qSContainerImpl;
            return this;
        }

        public QSContainerImplController build() {
            return new QSContainerImplController(this.mView, this.mQuickStatusBarHeaderControllerBuilder);
        }
    }
}
