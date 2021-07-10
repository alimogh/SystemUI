package com.google.android.libraries.assistant.oemsmartspace.lib;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import com.google.geo.sidekick.SmartspaceProto$SmartspaceUpdate;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
public class ProtoStore {
    private final Context context;

    public ProtoStore(Context context) {
        this.context = context.getApplicationContext();
    }

    public SmartspaceCard load(String str, boolean z) {
        File fileStreamPath = this.context.getFileStreamPath(str);
        try {
            FileInputStream fileInputStream = new FileInputStream(fileStreamPath);
            int length = (int) fileStreamPath.length();
            byte[] bArr = new byte[length];
            fileInputStream.read(bArr, 0, length);
            SmartspaceProto$SmartspaceUpdate.SmartspaceCard.Builder newBuilder = SmartspaceProto$SmartspaceUpdate.SmartspaceCard.newBuilder();
            newBuilder.mergeFrom(bArr);
            String valueOf = String.valueOf(fileStreamPath);
            StringBuilder sb = new StringBuilder(String.valueOf(valueOf).length() + 4);
            sb.append(valueOf);
            sb.append("Icon");
            return new SmartspaceCard(this.context, newBuilder.build(), !z, BitmapFactory.decodeStream(new FileInputStream(sb.toString())));
        } catch (FileNotFoundException unused) {
            String str2 = z ? "current" : "weather";
            Log.e("ProtoStore", str2.length() != 0 ? "no cached data for card type: ".concat(str2) : new String("no cached data for card type: "));
            return null;
        } catch (Exception e) {
            Log.e("ProtoStore", "unable to load data", e);
            return null;
        }
    }

    public void store(SmartspaceCard smartspaceCard, String str) {
        if (smartspaceCard != null) {
            try {
                if (smartspaceCard.getCard() != null && !smartspaceCard.isExpired()) {
                    this.context.openFileOutput(str, 0).write(smartspaceCard.getCard().toByteArray());
                    String valueOf = String.valueOf(this.context.getFileStreamPath(str));
                    StringBuilder sb = new StringBuilder(String.valueOf(valueOf).length() + 20);
                    sb.append("store file to path: ");
                    sb.append(valueOf);
                    Log.d("ProtoStore", sb.toString());
                    if (smartspaceCard.getIcon() != null) {
                        Bitmap icon = smartspaceCard.getIcon();
                        Context context = this.context;
                        String valueOf2 = String.valueOf(str);
                        FileOutputStream openFileOutput = context.openFileOutput("Icon".length() != 0 ? valueOf2.concat("Icon") : new String(valueOf2), 0);
                        icon.compress(Bitmap.CompressFormat.PNG, 100, openFileOutput);
                        openFileOutput.close();
                        return;
                    }
                    return;
                }
            } catch (FileNotFoundException unused) {
                Log.e("ProtoStore", "file does not exist");
                return;
            } catch (Exception e) {
                Log.e("ProtoStore", "unable to write file", e);
                return;
            }
        }
        String valueOf3 = String.valueOf(str);
        Log.d("ProtoStore", valueOf3.length() != 0 ? "deleting ".concat(valueOf3) : new String("deleting "));
        this.context.deleteFile(str);
    }
}
