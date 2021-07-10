package com.oneplus.faceunlock.internal;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
public interface IOPFacelockService extends IInterface {
    void prepare() throws RemoteException;

    void registerCallback(IOPFacelockCallback iOPFacelockCallback) throws RemoteException;

    void release() throws RemoteException;

    void startFaceUnlock(int i) throws RemoteException;

    void stopFaceUnlock(int i) throws RemoteException;

    void unregisterCallback(IOPFacelockCallback iOPFacelockCallback) throws RemoteException;

    public static abstract class Stub extends Binder implements IOPFacelockService {
        public static IOPFacelockService asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface("com.oneplus.faceunlock.internal.IOPFacelockService");
            if (queryLocalInterface == null || !(queryLocalInterface instanceof IOPFacelockService)) {
                return new Proxy(iBinder);
            }
            return (IOPFacelockService) queryLocalInterface;
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IOPFacelockService {
            public static IOPFacelockService sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder iBinder) {
                this.mRemote = iBinder;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            @Override // com.oneplus.faceunlock.internal.IOPFacelockService
            public void prepare() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.oneplus.faceunlock.internal.IOPFacelockService");
                    if (this.mRemote.transact(1, obtain, obtain2, 0) || Stub.getDefaultImpl() == null) {
                        obtain2.readException();
                        obtain2.recycle();
                        obtain.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().prepare();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.oneplus.faceunlock.internal.IOPFacelockService
            public void release() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.oneplus.faceunlock.internal.IOPFacelockService");
                    if (this.mRemote.transact(2, obtain, obtain2, 0) || Stub.getDefaultImpl() == null) {
                        obtain2.readException();
                        obtain2.recycle();
                        obtain.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().release();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.oneplus.faceunlock.internal.IOPFacelockService
            public void startFaceUnlock(int i) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.oneplus.faceunlock.internal.IOPFacelockService");
                    obtain.writeInt(i);
                    if (this.mRemote.transact(3, obtain, obtain2, 0) || Stub.getDefaultImpl() == null) {
                        obtain2.readException();
                        obtain2.recycle();
                        obtain.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().startFaceUnlock(i);
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.oneplus.faceunlock.internal.IOPFacelockService
            public void stopFaceUnlock(int i) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.oneplus.faceunlock.internal.IOPFacelockService");
                    obtain.writeInt(i);
                    if (this.mRemote.transact(4, obtain, obtain2, 0) || Stub.getDefaultImpl() == null) {
                        obtain2.readException();
                        obtain2.recycle();
                        obtain.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().stopFaceUnlock(i);
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.oneplus.faceunlock.internal.IOPFacelockService
            public void registerCallback(IOPFacelockCallback iOPFacelockCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.oneplus.faceunlock.internal.IOPFacelockService");
                    obtain.writeStrongBinder(iOPFacelockCallback != null ? iOPFacelockCallback.asBinder() : null);
                    if (this.mRemote.transact(5, obtain, obtain2, 0) || Stub.getDefaultImpl() == null) {
                        obtain2.readException();
                        obtain2.recycle();
                        obtain.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerCallback(iOPFacelockCallback);
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.oneplus.faceunlock.internal.IOPFacelockService
            public void unregisterCallback(IOPFacelockCallback iOPFacelockCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.oneplus.faceunlock.internal.IOPFacelockService");
                    obtain.writeStrongBinder(iOPFacelockCallback != null ? iOPFacelockCallback.asBinder() : null);
                    if (this.mRemote.transact(6, obtain, obtain2, 0) || Stub.getDefaultImpl() == null) {
                        obtain2.readException();
                        obtain2.recycle();
                        obtain.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterCallback(iOPFacelockCallback);
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }

        public static IOPFacelockService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
