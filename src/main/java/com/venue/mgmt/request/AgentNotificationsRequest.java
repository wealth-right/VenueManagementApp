package com.venue.mgmt.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AgentNotificationsRequest {
    @NotEmpty(message = "Enter valid UserId")
    String userID;
    String notificationDate;
    String fromDate;
    String toDate;
    String notificationCode;
    String notificationStatus;
    @NotEmpty(message = "Enter valid LoginType")
    String loginType;
    @NotEmpty(message = "Enter valid CustomerId")
    String customerId;
}