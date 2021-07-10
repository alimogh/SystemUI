package com.google.android.libraries.assistant.oemsmartspace.lib;

import android.util.Log;
public class SmartspaceData {
    SmartspaceCard currentCard = null;
    SmartspaceChip firstChip = null;
    SmartspaceChip secondChip = null;
    SmartspaceCard weatherCard = null;

    public void clear() {
        Log.d("SmartspaceData", "Set all smartspace data to null");
        this.weatherCard = null;
        this.currentCard = null;
        this.firstChip = null;
        this.secondChip = null;
    }

    public long getExpirationRemainingMillis() {
        SmartspaceCard smartspaceCard;
        long expiration;
        long currentTimeMillis = System.currentTimeMillis();
        if (!hasCurrent() || !hasWeather()) {
            if (hasCurrent()) {
                smartspaceCard = this.currentCard;
            } else if (!hasWeather()) {
                return 0;
            } else {
                smartspaceCard = this.weatherCard;
            }
            expiration = smartspaceCard.getExpiration();
        } else {
            expiration = Math.min(this.currentCard.getExpiration(), this.weatherCard.getExpiration());
        }
        return expiration - currentTimeMillis;
    }

    public long getExpiresAtMillis() {
        if (hasCurrent() && hasWeather()) {
            return Math.min(this.currentCard.getExpiration(), this.weatherCard.getExpiration());
        }
        if (hasCurrent()) {
            return this.currentCard.getExpiration();
        }
        if (hasWeather()) {
            return this.weatherCard.getExpiration();
        }
        return 0;
    }

    public boolean handleExpire() {
        boolean z = false;
        if (hasWeather() && this.weatherCard.isExpired()) {
            this.weatherCard = null;
            z = true;
        }
        if (!hasCurrent() || !this.currentCard.isExpired()) {
            return z;
        }
        this.currentCard = null;
        return true;
    }

    public boolean hasCurrent() {
        return this.currentCard != null;
    }

    public boolean hasWeather() {
        return this.weatherCard != null;
    }

    public String toString() {
        String valueOf = String.valueOf(this.currentCard);
        String valueOf2 = String.valueOf(this.weatherCard);
        String valueOf3 = String.valueOf(this.firstChip);
        String valueOf4 = String.valueOf(this.secondChip);
        int length = String.valueOf(valueOf).length();
        int length2 = String.valueOf(valueOf2).length();
        StringBuilder sb = new StringBuilder(length + 5 + length2 + String.valueOf(valueOf3).length() + String.valueOf(valueOf4).length());
        sb.append("{");
        sb.append(valueOf);
        sb.append(",");
        sb.append(valueOf2);
        sb.append(",");
        sb.append(valueOf3);
        sb.append(",");
        sb.append(valueOf4);
        sb.append("}");
        return sb.toString();
    }
}
