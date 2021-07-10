package com.android.systemui.util;

import android.content.Context;
import android.os.Build;
import android.os.Debug;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import com.android.keyguard.KeyguardClockSwitch;
import com.android.keyguard.KeyguardMessageArea;
import com.android.keyguard.KeyguardSliceView;
import com.android.systemui.dagger.SystemUIRootComponent;
import com.android.systemui.qs.QSFooterImpl;
import com.android.systemui.qs.QSPanel;
import com.android.systemui.qs.QuickQSPanel;
import com.android.systemui.qs.QuickStatusBarHeader;
import com.android.systemui.qs.customize.QSCustomizer;
import com.android.systemui.statusbar.NotificationShelf;
import com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
public class InjectionInflationController {
    private final LayoutInflater.Factory2 mFactory = new InjectionFactory();
    private final ArrayMap<String, Method> mInjectionMap = new ArrayMap<>();
    private final ViewCreator mViewCreator;

    public interface ViewCreator {
        ViewInstanceCreator createInstanceCreator(ViewAttributeProvider viewAttributeProvider);
    }

    public interface ViewInstanceCreator {
        NotificationShelf creatNotificationShelf();

        KeyguardClockSwitch createKeyguardClockSwitch();

        KeyguardMessageArea createKeyguardMessageArea();

        KeyguardSliceView createKeyguardSliceView();

        NotificationStackScrollLayout createNotificationStackScrollLayout();

        QSCustomizer createQSCustomizer();

        QSPanel createQSPanel();

        QSFooterImpl createQsFooter();

        QuickStatusBarHeader createQsHeader();

        QuickQSPanel createQuickQSPanel();
    }

    public InjectionInflationController(SystemUIRootComponent systemUIRootComponent) {
        this.mViewCreator = systemUIRootComponent.createViewCreator();
        initInjectionMap();
    }

    public LayoutInflater injectable(LayoutInflater layoutInflater) {
        LayoutInflater cloneInContext = layoutInflater.cloneInContext(layoutInflater.getContext());
        if (Build.DEBUG_ONEPLUS) {
            Log.d("InjectionInflation", "setPrivateFactory, " + Debug.getCallers(1));
        }
        cloneInContext.setPrivateFactory(this.mFactory);
        return cloneInContext;
    }

    private void initInjectionMap() {
        Method[] declaredMethods = ViewInstanceCreator.class.getDeclaredMethods();
        for (Method method : declaredMethods) {
            if (View.class.isAssignableFrom(method.getReturnType()) && (method.getModifiers() & 1) != 0) {
                this.mInjectionMap.put(method.getReturnType().getName(), method);
            }
        }
    }

    public class ViewAttributeProvider {
        private final AttributeSet mAttrs;
        private final Context mContext;

        private ViewAttributeProvider(InjectionInflationController injectionInflationController, Context context, AttributeSet attributeSet) {
            this.mContext = context;
            this.mAttrs = attributeSet;
        }

        public Context provideContext() {
            return this.mContext;
        }

        public AttributeSet provideAttributeSet() {
            return this.mAttrs;
        }
    }

    private class InjectionFactory implements LayoutInflater.Factory2 {
        private InjectionFactory() {
        }

        @Override // android.view.LayoutInflater.Factory
        public View onCreateView(String str, Context context, AttributeSet attributeSet) {
            Method method = (Method) InjectionInflationController.this.mInjectionMap.get(str);
            if (method == null) {
                return null;
            }
            try {
                return (View) method.invoke(InjectionInflationController.this.mViewCreator.createInstanceCreator(new ViewAttributeProvider(context, attributeSet)), new Object[0]);
            } catch (IllegalAccessException e) {
                throw new InflateException("Could not inflate " + str, e);
            } catch (InvocationTargetException e2) {
                throw new InflateException("Could not inflate " + str, e2);
            }
        }

        @Override // android.view.LayoutInflater.Factory2
        public View onCreateView(View view, String str, Context context, AttributeSet attributeSet) {
            return onCreateView(str, context, attributeSet);
        }
    }
}
