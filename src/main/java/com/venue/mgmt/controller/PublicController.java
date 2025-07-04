package com.venue.mgmt.controller;

import com.venue.mgmt.dto.QuickTapAppInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/venue-app/v1/config")
public class PublicController {

    @GetMapping
    public QuickTapAppInfo getAppInfo() {
        QuickTapAppInfo info = new QuickTapAppInfo();
        info.setAppName("QuickTap");
        info.setLatestAndroidVersion("1.0.0");
        info.setLatestIosVersion("1.0.0");
        info.setForceUpdateEnabled(false);
        info.setMaintenanceMode(false);
        info.setMaintenanceStartTime("21:00:00");
        info.setMaintenanceEndTime("09:00:00");
        info.setMaintenanceMsgTitle("This service is temporarily unavailable due to routine maintenance.");
        info.setMaintenanceMsgDesc("We appreciate your patience & understanding. For any urgent assistance, please contact our support team.");
        info.setMaintenanceTimingMsg("It'll be back online by 9:00 AM tomorrow");
        info.setApiBaseUrl("https://edge.wealth-right.com/api/quicktap/venue-app/v1");
        return info;
    }
}
