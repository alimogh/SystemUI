package com.oneplus.core.oimc;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
public interface IOIMCService extends IInterface {
    void addFuncRuleGlobal(OIMCRule oIMCRule) throws RemoteException;

    int getRemoteFuncStatus(String str) throws RemoteException;

    void notifyModeChange(String str, int i, int i2) throws RemoteException;

    void removeFuncRuleGlobal(OIMCRule oIMCRule) throws RemoteException;

    public static abstract class Stub extends Binder implements IOIMCService {
        public static IOIMCService asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface("com.oneplus.core.oimc.IOIMCService");
            if (queryLocalInterface == null || !(queryLocalInterface instanceof IOIMCService)) {
                return new Proxy(iBinder);
            }
            return (IOIMCService) queryLocalInterface;
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IOIMCService {
            public static IOIMCService sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder iBinder) {
                this.mRemote = iBinder;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            @Override // com.oneplus.core.oimc.IOIMCService
            public void addFuncRuleGlobal(OIMCRule oIMCRule) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.oneplus.core.oimc.IOIMCService");
                    if (oIMCRule != null) {
                        obtain.writeInt(1);
                        oIMCRule.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    if (this.mRemote.transact(3, obtain, obtain2, 0) || Stub.getDefaultImpl() == null) {
                        obtain2.readException();
                        obtain2.recycle();
                        obtain.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addFuncRuleGlobal(oIMCRule);
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.oneplus.core.oimc.IOIMCService
            public void removeFuncRuleGlobal(OIMCRule oIMCRule) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.oneplus.core.oimc.IOIMCService");
                    if (oIMCRule != null) {
                        obtain.writeInt(1);
                        oIMCRule.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    if (this.mRemote.transact(4, obtain, obtain2, 0) || Stub.getDefaultImpl() == null) {
                        obtain2.readException();
                        obtain2.recycle();
                        obtain.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeFuncRuleGlobal(oIMCRule);
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.oneplus.core.oimc.IOIMCService
            public void notifyModeChange(String str, int i, int i2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.oneplus.core.oimc.IOIMCService");
                    obtain.writeString(str);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    if (this.mRemote.transact(6, obtain, obtain2, 0) || Stub.getDefaultImpl() == null) {
                        obtain2.readException();
                        obtain2.recycle();
                        obtain.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyModeChange(str, i, i2);
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.oneplus.core.oimc.IOIMCService
            public int getRemoteFuncStatus(String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.oneplus.core.oimc.IOIMCService");
                    obtain.writeString(str);
                    if (!this.mRemote.transact(9, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRemoteFuncStatus(str);
                    }
                    obtain2.readException();
                    int readInt = obtain2.readInt();
                    obtain2.recycle();
                    obtain.recycle();
                    return readInt;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }

        public static IOIMCService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
