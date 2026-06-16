package com.smartestate.controller;

import com.smartestate.common.Constants;
import com.smartestate.common.Result;
import com.smartestate.dto.FacilityBookingRequest;
import com.smartestate.entity.FacilityBooking;
import com.smartestate.service.FacilityBookingService;
import com.smartestate.utils.LogUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/facility-bookings")
public class FacilityBookingController {
    private final FacilityBookingService bookingService;

    public FacilityBookingController(FacilityBookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public Result<List<FacilityBooking>> list(@RequestParam(required = false) Long facilityId,
                                              @RequestParam(required = false) String status,
                                              HttpServletRequest request) {
        String role = (String) request.getAttribute(Constants.CURRENT_USER_ROLE);
        LogUtil.info("FacilityBookingController list facilityId=%s status=%s role=%s", facilityId, status, role);
        return Result.ok(bookingService.list(facilityId, status, role));
    }

    @GetMapping("/my")
    public Result<List<FacilityBooking>> myBookings(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(Constants.CURRENT_USER_ID);
        String role = (String) request.getAttribute(Constants.CURRENT_USER_ROLE);
        LogUtil.info("FacilityBookingController myBookings userId=%s role=%s", userId, role);
        return Result.ok(bookingService.myBookings(userId, role));
    }

    @PostMapping
    public Result<FacilityBooking> create(@RequestBody FacilityBookingRequest body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(Constants.CURRENT_USER_ID);
        String role = (String) request.getAttribute(Constants.CURRENT_USER_ROLE);
        LogUtil.info("FacilityBookingController create facilityId=%s slotId=%s role=%s",
                body.getFacilityId(), body.getSlotId(), role);
        return Result.ok(bookingService.create(userId, role, body));
    }

    @PutMapping("/{id}/cancel")
    public Result<FacilityBooking> cancel(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(Constants.CURRENT_USER_ID);
        String role = (String) request.getAttribute(Constants.CURRENT_USER_ROLE);
        LogUtil.info("FacilityBookingController cancel id=%s role=%s", id, role);
        return Result.ok(bookingService.cancel(id, userId, role));
    }
}
