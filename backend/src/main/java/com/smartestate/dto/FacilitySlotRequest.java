package com.smartestate.dto;

import lombok.Data;

import java.time.LocalTime;

@Data
public class FacilitySlotRequest {
    private Long facilityId;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer capacity;
    private Integer weekday;
    private String status;
}
