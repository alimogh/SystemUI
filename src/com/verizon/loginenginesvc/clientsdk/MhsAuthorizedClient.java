package com.verizon.loginenginesvc.clientsdk;

import android.content.Context;
import android.os.Looper;
import com.verizon.loginenginesvc.clientsdk.internal.LoginSvcClient;
import org.json.JSONException;
import org.json.JSONObject;
public class MhsAuthorizedClient extends LoginSvcClient {
    private final ICallback mCallback;

    public interface ICallback {
        void onError(int i, String str);

        void onSuccess(boolean z);

        void onTimeout();
    }

    /* access modifiers changed from: protected */
    @Override // com.verizon.loginenginesvc.clientsdk.internal.LoginSvcClient
    public String getOperationName() {
        return "MobileHotspotAuthorized";
    }

    public MhsAuthorizedClient(Context context, ICallback iCallback, Integer num, Looper looper) {
        super(context, num, looper);
        this.mCallback = iCallback;
    }

    /* access modifiers changed from: protected */
    @Override // com.verizon.loginenginesvc.clientsdk.internal.LoginSvcClient
    public JSONObject buildJsonRequest() throws JSONException {
        return super.buildJsonRequest();
    }

    /* access modifiers changed from: protected */
    @Override // com.verizon.loginenginesvc.clientsdk.internal.LoginSvcClient
    public void handleResponse(JSONObject jSONObject) {
        int optInt = jSONObject.optInt("result-code", -99);
        String optString = jSONObject.optString("message");
        boolean optBoolean = jSONObject.optBoolean("mobile-hotspot-authorized", false);
        if (optInt != 0) {
            this.mCallback.onError(optInt, optString);
        } else if (!jSONObject.has("mobile-hotspot-authorized")) {
            this.mCallback.onError(100, "missing mobile hotspot status");
        } else {
            this.mCallback.onSuccess(optBoolean);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.verizon.loginenginesvc.clientsdk.internal.LoginSvcClient
    public void handleTimeout() {
        this.mCallback.onTimeout();
    }

    /* access modifiers changed from: protected */
    @Override // com.verizon.loginenginesvc.clientsdk.internal.LoginSvcClient
    public void handleError(String str, Throwable th) {
        this.mCallback.onError(100, str);
    }
}
