package com.smartestate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartestate.common.ErrorCode;
import com.smartestate.constants.FacilityConstants;
import com.smartestate.constants.LogTemplates;
import com.smartestate.dto.FacilitySlotRequest;
import com.smartestate.entity.FacilityTimeSlot;
import com.smartestate.mapper.FacilityTimeSlotMapper;
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
public class FacilityTimeSlotServiceImpl implements FacilityTimeSlotService {
    private final FacilityTimeSlotMapper slotMapper;
    private final FacilityService facilityService;
    private final OperationLogService operationLogService;

    public FacilityTimeSlotServiceImpl(FacilityTimeSlotMapper slotMapper,
                                       FacilityService facilityService,
                                       OperationLogService operationLogService) {
        this.slotMapper = slotMapper;
        this.facilityService = facilityService;
        this.operationLogService = operationLogService;
    }

    @Override
    public List<FacilityTimeSlot> listByFacility(Long facilityId, String role) {
        facilityService.getById(facilityId, role);
        LambdaQueryWrapper<FacilityTimeSlot> wrapper = new LambdaQueryWrapper<FacilityTimeSlot>()
                .eq(FacilityTimeSlot::getFacilityId, facilityId)
                .orderByAsc(FacilityTimeSlot::getStartTime);
        List<FacilityTimeSlot> slots = slotMapper.selectList(wrapper);
        LogUtil.info(LogTemplates.FACILITY_SLOT_LIST, facilityId, role);
        return slots;
    }

    @Override
    public List<FacilityTimeSlot> listWithAvailability(Long facilityId, LocalDate bookingDate, String role) {
        facilityService.getById(facilityId, role);
        int weekday = bookingDate.getDayOfWeek().getValue();
        List<FacilityTimeSlot> slots = slotMapper.selectSlotsWithBookedCount(facilityId, bookingDate, weekday);
        for (FacilityTimeSlot slot : slots) {
            if (slot.getBookedCount() == null) {
                slot.setBookedCount(0);
            }
            slot.setAvailable(slot.getBookedCount() < slot.getCapacity());
        }
        LogUtil.info(LogTemplates.FACILITY_SLOT_LIST, facilityId, role);
        return slots;
    }

    @Override
    public FacilityTimeSlot create(String role, FacilitySlotRequest request) {
        facilityService.getById(request.getFacilityId(), role);
        FacilityTimeSlot slot = new FacilityTimeSlot();
        slot.setFacilityId(request.getFacilityId());
        slot.setStartTime(request.getStartTime());
        slot.setEndTime(request.getEndTime());
        slot.setCapacity(request.getCapacity() != null ? request.getCapacity() : 1);
        slot.setWeekday(request.getWeekday());
        slot.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : FacilityConstants.SLOT_ACTIVE);
        slot.setCreatedAt(LocalDateTime.now());
        slot.setUpdatedAt(LocalDateTime.now());
        slotMapper.insert(slot);
        operationLogService.record(null, role, "facility.slot.create", "FacilityTimeSlot", slot.getId(),
                String.format(LogTemplates.FACILITY_SLOT_CREATE, slot.getId(), request.getFacilityId(), role));
        return slot;
    }

    @Override
    public FacilityTimeSlot update(Long id, String role, FacilitySlotRequest request) {
        FacilityTimeSlot slot = slotMapper.selectById(id);
        if (slot == null) {
            throw new IllegalArgumentException(ErrorCode.FACILITY_SLOT_NOT_FOUND.format(id, role));
        }
        if (request.getStartTime() != null) {
            slot.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            slot.setEndTime(request.getEndTime());
        }
        if (request.getCapacity() != null) {
            slot.setCapacity(request.getCapacity());
        }
        if (request.getWeekday() != null) {
            slot.setWeekday(request.getWeekday());
        }
        if (StringUtils.hasText(request.getStatus())) {
            slot.setStatus(request.getStatus());
        }
        slot.setUpdatedAt(LocalDateTime.now());
        slotMapper.updateById(slot);
        operationLogService.record(null, role, "facility.slot.update", "FacilityTimeSlot", id,
                String.format(LogTemplates.FACILITY_SLOT_UPDATE, id, role));
        return slot;
    }

    @Override
    public void delete(Long id, String role) {
        FacilityTimeSlot slot = slotMapper.selectById(id);
        if (slot == null) {
            throw new IllegalArgumentException(ErrorCode.FACILITY_SLOT_NOT_FOUND.format(id, role));
        }
        slotMapper.deleteById(id);
        operationLogService.record(null, role, "facility.slot.delete", "FacilityTimeSlot", id,
                String.format(LogTemplates.FACILITY_SLOT_DELETE, id, role));
    }
}
