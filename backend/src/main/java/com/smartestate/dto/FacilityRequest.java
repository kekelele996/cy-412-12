package com.smartestate.dto;

import lombok.Data;

@Data
public class FacilityRequest {
    private String name;
    private String description;
    private String image;
    private String location;
    private String status;
}
