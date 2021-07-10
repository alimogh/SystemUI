package com.oneplus.systemui.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import com.oneplus.systemui.util.OpTetherUtils;
import com.oneplus.util.OpUtils;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;
public class OpTetherUtils {

    public interface OnDialogConfirmCallback {
        void onConfirm();
    }

    public static void showUstAlertDialog(Context context, OnDialogConfirmCallback onDialogConfirmCallback, boolean z) {
        if (context != null) {
            AtomicBoolean atomicBoolean = new AtomicBoolean(false);
            AlertDialog create = new AlertDialog.Builder(context, OpUtils.getThemeColor(context) == 1 ? 16974374 : 16974394).setTitle(84869378).setPositiveButton(17039370, new DialogInterface.OnClickListener(z, context, atomicBoolean) { // from class: com.oneplus.systemui.util.-$$Lambda$OpTetherUtils$nd-VyY5Cxcv5cwfaKen2K8mnWCA
                public final /* synthetic */ boolean f$1;
                public final /* synthetic */ Context f$2;
                public final /* synthetic */ AtomicBoolean f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                }

                @Override // android.content.DialogInterface.OnClickListener
                public final void onClick(DialogInterface dialogInterface, int i) {
                    OpTetherUtils.lambda$showUstAlertDialog$0(OpTetherUtils.OnDialogConfirmCallback.this, this.f$1, this.f$2, this.f$3, dialogInterface, i);
                }
            }).setCancelable(false).create();
            View inflate = LayoutInflater.from(create.getContext()).inflate(84606979, (ViewGroup) null);
            ViewGroup viewGroup = (ViewGroup) inflate.getParent();
            if (viewGroup != null) {
                viewGroup.removeView(inflate);
            }
            ((TextView) inflate.findViewById(84410420)).setText(context.getResources().getString(84869377));
            CheckBox checkBox = (CheckBox) inflate.findViewById(84410658);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(atomicBoolean) { // from class: com.oneplus.systemui.util.-$$Lambda$OpTetherUtils$rLpGJxhryIe0VhddJfcnXfYCDHQ
                public final /* synthetic */ AtomicBoolean f$0;

                {
                    this.f$0 = r1;
                }

                @Override // android.widget.CompoundButton.OnCheckedChangeListener
                public final void onCheckedChanged(CompoundButton compoundButton, boolean z2) {
                    OpTetherUtils.lambda$showUstAlertDialog$1(this.f$0, compoundButton, z2);
                }
            });
            checkBox.setChecked(false);
            checkBox.setText(context.getResources().getString(84869376));
            create.setView(inflate);
            create.setCanceledOnTouchOutside(false);
            create.getWindow().setType(2009);
            create.show();
        }
    }

    static /* synthetic */ void lambda$showUstAlertDialog$0(OnDialogConfirmCallback onDialogConfirmCallback, boolean z, Context context, AtomicBoolean atomicBoolean, DialogInterface dialogInterface, int i) {
        if (onDialogConfirmCallback != null) {
            Log.d("OpTetherUtils", "showUstAlertDialog isTethering = " + z);
            onDialogConfirmCallback.onConfirm();
        }
        Log.d("OpTetherUtils", "PositiveButton click:" + z);
        setDialogNotShowAgain(context, atomicBoolean.get());
    }

    static /* synthetic */ void lambda$showUstAlertDialog$1(AtomicBoolean atomicBoolean, CompoundButton compoundButton, boolean z) {
        atomicBoolean.set(z);
        Log.d("OpTetherUtils", "onCheckedChanged isChecked = " + z);
    }

    public static boolean isWifiEnable(Context context) {
        if (context == null) {
            return false;
        }
        return ((WifiManager) context.getApplicationContext().getSystemService("wifi")).isWifiEnabled();
    }

    public static void setTetherState(Context context, boolean z) {
        WifiManager wifiManager;
        if (context != null && (wifiManager = (WifiManager) context.getApplicationContext().getSystemService("wifi")) != null) {
            try {
                Method declaredMethod = wifiManager.getClass().getDeclaredMethod("setTetherState", Integer.TYPE, Boolean.TYPE);
                declaredMethod.setAccessible(true);
                declaredMethod.invoke(wifiManager, 0, Boolean.valueOf(z));
                Log.d("OpTetherUtils", "setTetherState state = " + z);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e2) {
                e2.printStackTrace();
            } catch (InvocationTargetException e3) {
                e3.printStackTrace();
            } catch (RuntimeException e4) {
                e4.printStackTrace();
            }
        }
    }

    private static void setTetherEnabled(Context context) {
        WifiManager wifiManager;
        if (context != null && (wifiManager = (WifiManager) context.getApplicationContext().getSystemService("wifi")) != null) {
            try {
                wifiManager.getClass().getDeclaredMethod("setTetherEnabled", Boolean.TYPE).invoke(wifiManager, Boolean.FALSE);
                Log.d("OpTetherUtils", "setTetherEnabled false");
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e2) {
                e2.printStackTrace();
            } catch (InvocationTargetException e3) {
                e3.printStackTrace();
            }
        }
    }

    public static boolean isTetheringOpen(Context context) {
        if (context == null) {
            return false;
        }
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService("wifi");
        try {
            Method declaredMethod = wifiManager.getClass().getDeclaredMethod("isWifiApEnabled", new Class[0]);
            declaredMethod.setAccessible(true);
            boolean booleanValue = ((Boolean) declaredMethod.invoke(wifiManager, new Object[0])).booleanValue();
            Method declaredMethod2 = wifiManager.getClass().getDeclaredMethod("getUsbTetherEnabled", new Class[0]);
            declaredMethod2.setAccessible(true);
            boolean booleanValue2 = ((Boolean) declaredMethod2.invoke(wifiManager, new Object[0])).booleanValue();
            Log.d("OpTetherUtils", "isTetheringOpen isWifiApEnabled = " + booleanValue + ", isUsbTetherEnabled = " + booleanValue2);
            if (booleanValue || booleanValue2) {
                return true;
            }
            return false;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            return false;
        } catch (InvocationTargetException e3) {
            e3.printStackTrace();
            return false;
        }
    }

    private static void setDialogNotShowAgain(Context context, boolean z) {
        if (context != null) {
            Settings.System.putInt(context.getApplicationContext().getContentResolver(), "tether_checkbox_not_show_again", z ? 1 : 0);
            Log.d("OpTetherUtils", "setDialogNotShowAgain notShowAgain =" + z);
        }
    }

    public static boolean isNeedShowDialog(Context context) {
        boolean z = true;
        if (context != null) {
            if (Settings.System.getInt(context.getApplicationContext().getContentResolver(), "tether_checkbox_not_show_again", 0) != 0) {
                z = false;
            }
            Log.d("OpTetherUtils", "isNeedShowDialog = " + z);
        }
        return z;
    }

    public static void setUstWifiTetheringStatus(Context context, int i) {
        if (context != null) {
            Settings.Global.putInt(context.getApplicationContext().getContentResolver(), "start_ust_tethering_wifi", i);
        }
    }

    private static void stopUsbTethering(Context context) {
        ConnectivityManager connectivityManager;
        Log.d("OpTetherUtils", "stopUsbTethering");
        if (context != null && (connectivityManager = (ConnectivityManager) context.getSystemService("connectivity")) != null) {
            connectivityManager.stopTethering(1);
        }
    }

    public static void disableTmoWifi(Context context) {
        setTetherState(context, true);
    }

    public static void disableTmoTethering(Context context) {
        setTetherEnabled(context);
        stopUsbTethering(context);
        setUstWifiTetheringStatus(context, 0);
    }
}
