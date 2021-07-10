package vendor.oneplus.hardware.display.V1_0;

import android.os.HidlSupport;
import android.os.HwBinder;
import android.os.HwParcel;
import android.os.IHwBinder;
import android.os.IHwInterface;
import android.os.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
public interface IOneplusDisplay extends IHwInterface {
    ArrayList<String> interfaceChain() throws RemoteException;

    void setMode(int i, int i2) throws RemoteException;

    static IOneplusDisplay asInterface(IHwBinder iHwBinder) {
        if (iHwBinder == null) {
            return null;
        }
        IOneplusDisplay queryLocalInterface = iHwBinder.queryLocalInterface("vendor.oneplus.hardware.display@1.0::IOneplusDisplay");
        if (queryLocalInterface != null && (queryLocalInterface instanceof IOneplusDisplay)) {
            return queryLocalInterface;
        }
        Proxy proxy = new Proxy(iHwBinder);
        try {
            Iterator<String> it = proxy.interfaceChain().iterator();
            while (it.hasNext()) {
                if (it.next().equals("vendor.oneplus.hardware.display@1.0::IOneplusDisplay")) {
                    return proxy;
                }
            }
        } catch (RemoteException unused) {
        }
        return null;
    }

    static IOneplusDisplay getService(String str) throws RemoteException {
        return asInterface(HwBinder.getService("vendor.oneplus.hardware.display@1.0::IOneplusDisplay", str));
    }

    static IOneplusDisplay getService() throws RemoteException {
        return getService("default");
    }

    public static final class Proxy implements IOneplusDisplay {
        private IHwBinder mRemote;

        public Proxy(IHwBinder iHwBinder) {
            Objects.requireNonNull(iHwBinder);
            this.mRemote = iHwBinder;
        }

        public IHwBinder asBinder() {
            return this.mRemote;
        }

        public String toString() {
            try {
                return interfaceDescriptor() + "@Proxy";
            } catch (RemoteException unused) {
                return "[class or subclass of vendor.oneplus.hardware.display@1.0::IOneplusDisplay]@Proxy";
            }
        }

        public final boolean equals(Object obj) {
            return HidlSupport.interfacesEqual(this, obj);
        }

        public final int hashCode() {
            return asBinder().hashCode();
        }

        @Override // vendor.oneplus.hardware.display.V1_0.IOneplusDisplay
        public void setMode(int i, int i2) throws RemoteException {
            HwParcel hwParcel = new HwParcel();
            hwParcel.writeInterfaceToken("vendor.oneplus.hardware.display@1.0::IOneplusDisplay");
            hwParcel.writeInt32(i);
            hwParcel.writeInt32(i2);
            HwParcel hwParcel2 = new HwParcel();
            try {
                this.mRemote.transact(1, hwParcel, hwParcel2, 0);
                hwParcel2.verifySuccess();
                hwParcel.releaseTemporaryStorage();
            } finally {
                hwParcel2.release();
            }
        }

        @Override // vendor.oneplus.hardware.display.V1_0.IOneplusDisplay
        public ArrayList<String> interfaceChain() throws RemoteException {
            HwParcel hwParcel = new HwParcel();
            hwParcel.writeInterfaceToken("android.hidl.base@1.0::IBase");
            HwParcel hwParcel2 = new HwParcel();
            try {
                this.mRemote.transact(256067662, hwParcel, hwParcel2, 0);
                hwParcel2.verifySuccess();
                hwParcel.releaseTemporaryStorage();
                return hwParcel2.readStringVector();
            } finally {
                hwParcel2.release();
            }
        }

        public String interfaceDescriptor() throws RemoteException {
            HwParcel hwParcel = new HwParcel();
            hwParcel.writeInterfaceToken("android.hidl.base@1.0::IBase");
            HwParcel hwParcel2 = new HwParcel();
            try {
                this.mRemote.transact(256136003, hwParcel, hwParcel2, 0);
                hwParcel2.verifySuccess();
                hwParcel.releaseTemporaryStorage();
                return hwParcel2.readString();
            } finally {
                hwParcel2.release();
            }
        }
    }
}
