package com.oneplus.faceunlock.internal;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
public interface IOPFacelockCallback extends IInterface {
    void onBeginRecognize(int i) throws RemoteException;

    void onCompared(int i, int i2, int i3, int i4, int i5) throws RemoteException;

    void onEndRecognize(int i, int i2, int i3) throws RemoteException;

    public static abstract class Stub extends Binder implements IOPFacelockCallback {
        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public Stub() {
            attachInterface(this, "com.oneplus.faceunlock.internal.IOPFacelockCallback");
        }

        @Override // android.os.Binder
        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            if (i == 1) {
                parcel.enforceInterface("com.oneplus.faceunlock.internal.IOPFacelockCallback");
                onBeginRecognize(parcel.readInt());
                return true;
            } else if (i == 2) {
                parcel.enforceInterface("com.oneplus.faceunlock.internal.IOPFacelockCallback");
                onCompared(parcel.readInt(), parcel.readInt(), parcel.readInt(), parcel.readInt(), parcel.readInt());
                return true;
            } else if (i == 3) {
                parcel.enforceInterface("com.oneplus.faceunlock.internal.IOPFacelockCallback");
                onEndRecognize(parcel.readInt(), parcel.readInt(), parcel.readInt());
                return true;
            } else if (i != 1598968902) {
                return super.onTransact(i, parcel, parcel2, i2);
            } else {
                parcel2.writeString("com.oneplus.faceunlock.internal.IOPFacelockCallback");
                return true;
            }
        }
    }
}
