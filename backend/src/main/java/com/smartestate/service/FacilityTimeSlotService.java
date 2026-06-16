package com.smartestate.service;

import com.smartestate.dto.FacilitySlotRequest;
import com.smartestate.entity.FacilityTimeSlot;

import java.time.LocalDate;
import java.util.List;

public interface FacilityTimeSlotService {
    List<FacilityTimeSlot> listByFacility(Long facilityId, String role);
    List<FacilityTimeSlot> listWithAvailability(Long facilityId, LocalDate bookingDate, String role);
    FacilityTimeSlot create(String role, FacilitySlotRequest request);
    FacilityTimeSlot update(Long id, String role, FacilitySlotRequest request);
    void delete(Long id, String role);
}
