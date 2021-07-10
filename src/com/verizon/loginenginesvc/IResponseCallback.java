package com.verizon.loginenginesvc;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
public interface IResponseCallback extends IInterface {
    void onResponse(String str) throws RemoteException;

    public static abstract class Stub extends Binder implements IResponseCallback {
        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public Stub() {
            attachInterface(this, "com.verizon.loginenginesvc.IResponseCallback");
        }

        @Override // android.os.Binder
        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            if (i == 1) {
                parcel.enforceInterface("com.verizon.loginenginesvc.IResponseCallback");
                onResponse(parcel.readString());
                parcel2.writeNoException();
                return true;
            } else if (i != 1598968902) {
                return super.onTransact(i, parcel, parcel2, i2);
            } else {
                parcel2.writeString("com.verizon.loginenginesvc.IResponseCallback");
                return true;
            }
        }
    }
}
