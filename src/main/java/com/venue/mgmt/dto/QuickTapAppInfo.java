package com.venue.mgmt.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuickTapAppInfo {
    private String appName;
    private String latestAndroidVersion;
    private String latestIosVersion;
    private boolean isForceUpdateEnabled;
    private boolean maintenanceMode;
    private String maintenanceStartTime;
    private String maintenanceEndTime;
    private String maintenanceMsgTitle;
    private String maintenanceMsgDesc;
    private String maintenanceTimingMsg;
    private String apiBaseUrl;
}
