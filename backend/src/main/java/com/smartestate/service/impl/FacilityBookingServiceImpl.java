package com.smartestate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartestate.common.ErrorCode;
import com.smartestate.constants.FacilityConstants;
import com.smartestate.constants.LogTemplates;
import com.smartestate.constants.UserConstants;
import com.smartestate.dto.FacilityBookingRequest;
import com.smartestate.entity.Facility;
import com.smartestate.entity.FacilityBooking;
import com.smartestate.entity.FacilityTimeSlot;
import com.smartestate.entity.User;
import com.smartestate.mapper.FacilityBookingMapper;
import com.smartestate.mapper.UserMapper;
import com.smartestate.service.FacilityBookingService;
import com.smartestate.service.FacilityService;
import com.smartestate.service.FacilityTimeSlotService;
import com.smartestate.service.OperationLogService;
import com.smartestate.utils.LogUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FacilityBookingServiceImpl implements FacilityBookingService {
    private final FacilityBookingMapper bookingMapper;
    private final FacilityService facilityService;
    private final FacilityTimeSlotService slotService;
    private final UserMapper userMapper;
    private final OperationLogService operationLogService;

    public FacilityBookingServiceImpl(FacilityBookingMapper bookingMapper,
                                      FacilityService facilityService,
                                      FacilityTimeSlotService slotService,
                                      UserMapper userMapper,
                                      OperationLogService operationLogService) {
        this.bookingMapper = bookingMapper;
        this.facilityService = facilityService;
        this.slotService = slotService;
        this.userMapper = userMapper;
        this.operationLogService = operationLogService;
    }

    @Override
    public List<FacilityBooking> list(Long facilityId, String status, String role) {
        LambdaQueryWrapper<FacilityBooking> wrapper = new LambdaQueryWrapper<FacilityBooking>()
                .orderByDesc(FacilityBooking::getBookingDate)
                .orderByDesc(FacilityBooking::getCreatedAt);
        if (facilityId != null) {
            wrapper.eq(FacilityBooking::getFacilityId, facilityId);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(FacilityBooking::getStatus, status);
        }
        List<FacilityBooking> bookings = bookingMapper.selectList(wrapper);
        bookings.forEach(this::hydrate);
        LogUtil.info(LogTemplates.FACILITY_BOOKING_LIST, role);
        return bookings;
    }

    @Override
    public List<FacilityBooking> myBookings(Long userId, String role) {
        LambdaQueryWrapper<FacilityBooking> wrapper = new LambdaQueryWrapper<FacilityBooking>()
                .eq(FacilityBooking::getUserId, userId)
                .orderByDesc(FacilityBooking::getBookingDate)
                .orderByDesc(FacilityBooking::getCreatedAt);
        List<FacilityBooking> bookings = bookingMapper.selectList(wrapper);
        bookings.forEach(this::hydrate);
        LogUtil.info(LogTemplates.FACILITY_BOOKING_MY_LIST, userId, role);
        return bookings;
    }

    @Override
    public FacilityBooking create(Long userId, String role, FacilityBookingRequest request) {
        Facility facility = facilityService.getById(request.getFacilityId(), role);
        if (!FacilityConstants.FACILITY_ACTIVE.equals(facility.getStatus())) {
            throw new IllegalArgumentException(ErrorCode.FACILITY_NOT_FOUND.format(request.getFacilityId(), role));
        }

        List<FacilityTimeSlot> slots = slotService.listWithAvailability(
                request.getFacilityId(), request.getBookingDate(), role);

        FacilityTimeSlot targetSlot = slots.stream()
                .filter(s -> s.getId().equals(request.getSlotId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        ErrorCode.FACILITY_SLOT_NOT_FOUND.format(request.getSlotId(), role)));

        if (targetSlot.getBookedCount() == null) {
            targetSlot.setBookedCount(0);
        }

        if (targetSlot.getBookedCount() >= targetSlot.getCapacity()) {
            throw new IllegalArgumentException(ErrorCode.FACILITY_SLOT_FULL.format(request.getSlotId(), role));
        }

        if (request.getBookingDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(ErrorCode.FACILITY_BOOKING_DATE_INVALID.format(request.getBookingDate(), role));
        }

        LambdaQueryWrapper<FacilityBooking> duplicateWrapper = new LambdaQueryWrapper<FacilityBooking>()
                .eq(FacilityBooking::getUserId, userId)
                .eq(FacilityBooking::getSlotId, request.getSlotId())
                .eq(FacilityBooking::getBookingDate, request.getBookingDate())
                .eq(FacilityBooking::getStatus, FacilityConstants.BOOKING_BOOKED);
        if (bookingMapper.selectCount(duplicateWrapper) > 0) {
            throw new IllegalArgumentException(ErrorCode.FACILITY_BOOKING_DUPLICATE.format(request.getSlotId(), role));
        }

        FacilityBooking booking = new FacilityBooking();
        booking.setUserId(userId);
        booking.setFacilityId(request.getFacilityId());
        booking.setSlotId(request.getSlotId());
        booking.setBookingDate(request.getBookingDate());
        booking.setStatus(FacilityConstants.BOOKING_BOOKED);
        booking.setRemark(request.getRemark());
        booking.setCreatedAt(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());
        bookingMapper.insert(booking);

        operationLogService.record(userId, role, "facility.booking.create", "FacilityBooking", booking.getId(),
                String.format(LogTemplates.FACILITY_BOOKING_CREATE,
                        booking.getId(), request.getSlotId(), request.getBookingDate(), role));

        hydrate(booking);
        return booking;
    }

    @Override
    public FacilityBooking cancel(Long id, Long userId, String role) {
        FacilityBooking booking = bookingMapper.selectById(id);
        if (booking == null) {
            throw new IllegalArgumentException(ErrorCode.FACILITY_BOOKING_NOT_FOUND.format(id, role));
        }

        if (UserConstants.RESIDENT.equals(role) && !booking.getUserId().equals(userId)) {
            throw new IllegalArgumentException(ErrorCode.FACILITY_BOOKING_NOT_FOUND.format(id, role));
        }

        booking.setStatus(FacilityConstants.BOOKING_CANCELLED);
        booking.setUpdatedAt(LocalDateTime.now());
        bookingMapper.updateById(booking);

        operationLogService.record(userId, role, "facility.booking.cancel", "FacilityBooking", id,
                String.format(LogTemplates.FACILITY_BOOKING_CANCEL, id, role));

        hydrate(booking);
        return booking;
    }

    private void hydrate(FacilityBooking booking) {
        if (booking.getUserId() != null) {
            User user = userMapper.selectById(booking.getUserId());
            if (user != null) user.setPasswordHash(null);
            booking.setUser(user);
        }
        if (booking.getFacilityId() != null) {
            try {
                Facility facility = facilityService.getById(booking.getFacilityId(), "system");
                booking.setFacility(facility);
            } catch (Exception ignored) {
            }
        }
    }
}
