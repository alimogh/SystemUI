package com.android.wifitrackerlib;

import android.net.wifi.ScanResult;
import java.util.List;
public class ScanResultUpdater {
    public abstract List<ScanResult> getScanResults(long j) throws IllegalArgumentException;

    public abstract void update(List<ScanResult> list);
}
