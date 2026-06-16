package com.smartestate.service;

import com.smartestate.dto.FacilityRequest;
import com.smartestate.entity.Facility;

import java.util.List;

public interface FacilityService {
    List<Facility> list(String status, String role);
    Facility getById(Long id, String role);
    Facility create(String role, FacilityRequest request);
    Facility update(Long id, String role, FacilityRequest request);
    void delete(Long id, String role);
}
