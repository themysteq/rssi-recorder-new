package pl.mysteq.software.rssirecordernew.structures;

import android.net.wifi.ScanResult;

/**
 * Created by mysteq on 2017-04-19.
 */

public class CustomScanResult {
    public String BSSID;
    public String SSID;
    public int level;

    public CustomScanResult(){}

    public CustomScanResult(CustomScanResult customScanResult)
    {
        this.BSSID = customScanResult.BSSID;
        this.SSID = customScanResult.SSID;
        this.level = customScanResult.level;
    }
    public CustomScanResult(ScanResult scanResult)
    {
        this.BSSID = scanResult.BSSID;
        this.SSID = scanResult.SSID;
        this.level = scanResult.level;
    }
}
