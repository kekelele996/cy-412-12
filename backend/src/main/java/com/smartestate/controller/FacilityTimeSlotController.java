package com.smartestate.controller;

import com.smartestate.common.Constants;
import com.smartestate.common.Result;
import com.smartestate.dto.FacilitySlotRequest;
import com.smartestate.entity.FacilityTimeSlot;
import com.smartestate.service.FacilityTimeSlotService;
import com.smartestate.utils.LogUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/facilities/{facilityId}/slots")
public class FacilityTimeSlotController {
    private final FacilityTimeSlotService slotService;

    public FacilityTimeSlotController(FacilityTimeSlotService slotService) {
        this.slotService = slotService;
    }

    @GetMapping
    public Result<List<FacilityTimeSlot>> list(@PathVariable Long facilityId,
                                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bookingDate,
                                               HttpServletRequest request) {
        String role = (String) request.getAttribute(Constants.CURRENT_USER_ROLE);
        LogUtil.info("FacilityTimeSlotController list facilityId=%s bookingDate=%s role=%s", facilityId, bookingDate, role);
        if (bookingDate != null) {
            return Result.ok(slotService.listWithAvailability(facilityId, bookingDate, role));
        }
        return Result.ok(slotService.listByFacility(facilityId, role));
    }

    @PostMapping
    public Result<FacilityTimeSlot> create(@PathVariable Long facilityId,
                                           @RequestBody FacilitySlotRequest body,
                                           HttpServletRequest request) {
        String role = (String) request.getAttribute(Constants.CURRENT_USER_ROLE);
        body.setFacilityId(facilityId);
        LogUtil.info("FacilityTimeSlotController create facilityId=%s role=%s", facilityId, role);
        return Result.ok(slotService.create(role, body));
    }

    @PutMapping("/{id}")
    public Result<FacilityTimeSlot> update(@PathVariable Long facilityId,
                                           @PathVariable Long id,
                                           @RequestBody FacilitySlotRequest body,
                                           HttpServletRequest request) {
        String role = (String) request.getAttribute(Constants.CURRENT_USER_ROLE);
        LogUtil.info("FacilityTimeSlotController update id=%s role=%s", id, role);
        return Result.ok(slotService.update(id, role, body));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long facilityId,
                               @PathVariable Long id,
                               HttpServletRequest request) {
        String role = (String) request.getAttribute(Constants.CURRENT_USER_ROLE);
        LogUtil.info("FacilityTimeSlotController delete id=%s role=%s", id, role);
        slotService.delete(id, role);
        return Result.ok();
    }
}
