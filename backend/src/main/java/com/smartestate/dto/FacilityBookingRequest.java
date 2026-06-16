package com.smartestate.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class FacilityBookingRequest {
    private Long facilityId;
    private Long slotId;
    private LocalDate bookingDate;
    private String remark;
}
