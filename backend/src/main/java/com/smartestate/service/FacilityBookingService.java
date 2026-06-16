package com.smartestate.service;

import com.smartestate.dto.FacilityBookingRequest;
import com.smartestate.entity.FacilityBooking;

import java.util.List;

public interface FacilityBookingService {
    List<FacilityBooking> list(Long facilityId, String status, String role);
    List<FacilityBooking> myBookings(Long userId, String role);
    FacilityBooking create(Long userId, String role, FacilityBookingRequest request);
    FacilityBooking cancel(Long id, Long userId, String role);
}
